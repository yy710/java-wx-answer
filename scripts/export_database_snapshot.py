#!/usr/bin/env python3
"""
Export every base-table row from the City Walk MySQL database.

The output is a data-only SQL snapshot plus a manifest.  The SQL file does
not drop, truncate, or update existing tables; it only contains INSERT
statements and can be loaded into an already-created target schema.

Run on the server:

    python3 export_database_snapshot.py

Optional environment variables (the CITYWALK_* names take precedence):

    CITYWALK_DB_HOST / DB_HOST
    CITYWALK_DB_PORT / DB_PORT
    CITYWALK_DB_NAME / DB_NAME
    CITYWALK_DB_USER / DB_USER

Optional arguments:

    python3 export_database_snapshot.py --output /backup/city-walk-baseline
    python3 export_database_snapshot.py --batch-size 1000

The snapshot contains all database fields, including user credentials or
password hashes where those fields exist.  Store the generated directory
with the same access restrictions as the source database.
"""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import math
import os
import sys
from dataclasses import dataclass
from decimal import Decimal
from pathlib import Path
from typing import Any, Sequence


DB_HOST = os.getenv("CITYWALK_DB_HOST", os.getenv("DB_HOST", "127.0.0.1"))
DB_PORT = int(os.getenv("CITYWALK_DB_PORT", os.getenv("DB_PORT", "3306")))
DB_NAME = os.getenv("CITYWALK_DB_NAME", os.getenv("DB_NAME", "city-walk"))
DB_USER = os.getenv("CITYWALK_DB_USER", os.getenv("DB_USER", "city-walk"))
DB_PASSWORD = "KaTmAjEJxynhFh7S"

DEFAULT_BATCH_SIZE = 500
SNAPSHOT_FORMAT = "city-walk-java-database-snapshot/v1"


class SnapshotError(RuntimeError):
    """Raised when a snapshot cannot be completed safely."""


@dataclass(frozen=True)
class DatabaseConfig:
    host: str
    port: int
    name: str
    user: str
    password: str


@dataclass(frozen=True)
class TableInfo:
    name: str
    engine: str | None
    columns: tuple[str, ...]
    primary_key_columns: tuple[str, ...]


class DatabaseSession:
    """Small adapter for the supported MySQL Python drivers."""

    def __init__(self, connection: Any, driver: str):
        self.connection = connection
        self.driver = driver

    def cursor(self, *, streaming: bool = False) -> Any:
        if self.driver == "pymysql":
            import pymysql

            cursor_class = pymysql.cursors.SSCursor if streaming else pymysql.cursors.Cursor
            return self.connection.cursor(cursor_class)

        return self.connection.cursor(buffered=not streaming)

    def close(self) -> None:
        self.connection.close()

    def rollback(self) -> None:
        self.connection.rollback()


def connect_database(config: DatabaseConfig) -> DatabaseSession:
    """Connect with PyMySQL first, then mysql-connector-python."""

    try:
        import pymysql
    except ImportError:
        pymysql = None

    if pymysql is not None:
        connection = pymysql.connect(
            host=config.host,
            port=config.port,
            user=config.user,
            password=config.password,
            database=config.name,
            charset="utf8mb4",
            autocommit=False,
            connect_timeout=30,
        )
        return DatabaseSession(connection, "pymysql")

    try:
        import mysql.connector
    except ImportError as exc:
        raise SnapshotError(
            "未找到可用的 MySQL Python 驱动，请安装 pymysql 或 "
            "mysql-connector-python。"
        ) from exc

    connection = mysql.connector.connect(
        host=config.host,
        port=config.port,
        user=config.user,
        password=config.password,
        database=config.name,
        charset="utf8mb4",
        autocommit=False,
    )
    return DatabaseSession(connection, "mysql-connector-python")


def quote_identifier(identifier: str) -> str:
    """Quote a MySQL identifier without allowing backtick injection."""

    if not identifier:
        raise SnapshotError("数据库返回了空的表名或列名。")
    return "`" + identifier.replace("`", "``") + "`"


def sql_string(value: str) -> str:
    """Quote a string for the MySQL SQL mode used by the snapshot header."""

    escaped = value.translate(
        {
            ord("\\"): "\\\\",
            ord("'"): "\\'",
            ord("\0"): "\\0",
            ord("\n"): "\\n",
            ord("\r"): "\\r",
            ord("\x1a"): "\\Z",
        }
    )
    return "'" + escaped + "'"


def sql_literal(value: Any) -> str:
    """Convert a value returned by either MySQL driver to a SQL literal."""

    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (bytes, bytearray, memoryview)):
        return "X'" + bytes(value).hex().upper() + "'"
    if isinstance(value, dt.datetime):
        return sql_string(value.isoformat(sep=" ", timespec="microseconds"))
    if isinstance(value, dt.date):
        return sql_string(value.isoformat())
    if isinstance(value, dt.time):
        return sql_string(value.isoformat(timespec="microseconds"))
    if isinstance(value, Decimal):
        if not value.is_finite():
            raise SnapshotError(f"无法导出非有限 Decimal 值：{value!r}")
        return format(value, "f")
    if isinstance(value, float):
        if not math.isfinite(value):
            raise SnapshotError(f"无法导出非有限浮点值：{value!r}")
        return repr(value)
    if isinstance(value, int):
        return str(value)
    return sql_string(str(value))


def close_cursor(cursor: Any) -> None:
    try:
        cursor.close()
    except Exception:
        pass


def begin_consistent_snapshot(database: DatabaseSession) -> None:
    cursor = database.cursor()
    try:
        cursor.execute("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ")
        cursor.execute("START TRANSACTION WITH CONSISTENT SNAPSHOT")
    finally:
        close_cursor(cursor)


def fetch_scalar(database: DatabaseSession, sql: str, params: Sequence[Any] = ()) -> Any:
    cursor = database.cursor()
    try:
        cursor.execute(sql, params)
        row = cursor.fetchone()
        return row[0] if row else None
    finally:
        close_cursor(cursor)


def list_base_tables(database: DatabaseSession, database_name: str) -> list[tuple[str, str | None]]:
    cursor = database.cursor()
    try:
        cursor.execute(
            """
            SELECT TABLE_NAME, ENGINE
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = %s
              AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY TABLE_NAME
            """,
            (database_name,),
        )
        return [(str(row[0]), row[1]) for row in cursor.fetchall()]
    finally:
        close_cursor(cursor)


def get_table_info(
    database: DatabaseSession,
    database_name: str,
    table_name: str,
    engine: str | None,
) -> TableInfo:
    cursor = database.cursor()
    try:
        cursor.execute(
            """
            SELECT COLUMN_NAME, EXTRA
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = %s
              AND TABLE_NAME = %s
            ORDER BY ORDINAL_POSITION
            """,
            (database_name, table_name),
        )
        columns = []
        for column_name, extra in cursor.fetchall():
            # Generated columns are calculated by the target schema and must
            # not appear in an INSERT column list.
            extra_text = (extra or "").upper()
            if "STORED GENERATED" not in extra_text and "VIRTUAL GENERATED" not in extra_text:
                columns.append(str(column_name))

        cursor.execute(
            """
            SELECT COLUMN_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = %s
              AND TABLE_NAME = %s
              AND CONSTRAINT_NAME = 'PRIMARY'
            ORDER BY ORDINAL_POSITION
            """,
            (database_name, table_name),
        )
        primary_key_columns = tuple(str(row[0]) for row in cursor.fetchall())
    finally:
        close_cursor(cursor)

    return TableInfo(
        name=table_name,
        engine=engine,
        columns=tuple(columns),
        primary_key_columns=primary_key_columns,
    )


def build_select_sql(table: TableInfo) -> str:
    if not table.columns:
        raise SnapshotError(f"表 {table.name} 没有可导出的存储列。")

    sql = (
        "SELECT "
        + ", ".join(quote_identifier(column) for column in table.columns)
        + " FROM "
        + quote_identifier(table.name)
    )
    order_columns = [column for column in table.primary_key_columns if column in table.columns]
    if order_columns:
        sql += " ORDER BY " + ", ".join(quote_identifier(column) for column in order_columns)
    return sql


def write_sql_header(output: Any, generated_at: str) -> None:
    output.write("-- City Walk data-only database snapshot.\n")
    output.write("-- This file contains INSERT statements only; it does not drop or truncate tables.\n")
    output.write(f"-- Generated at UTC: {generated_at}\n")
    output.write("-- The snapshot may contain sensitive user and administrator data.\n\n")
    output.write("SET NAMES utf8mb4;\n")
    output.write("SET @OLD_SQL_MODE = @@SQL_MODE;\n")
    output.write("SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;\n")
    output.write("SET SQL_MODE = REPLACE(@@SQL_MODE, 'NO_BACKSLASH_ESCAPES', '');\n")
    output.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")


def write_sql_footer(output: Any) -> None:
    output.write("\nSET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;\n")
    output.write("SET SQL_MODE = @OLD_SQL_MODE;\n")


def write_table_data(
    database: DatabaseSession,
    output: Any,
    table: TableInfo,
    batch_size: int,
) -> int:
    output.write("-- ----------------------------\n")
    output.write(f"-- Data for table {quote_identifier(table.name)}\n")
    output.write("-- ----------------------------\n")

    cursor = database.cursor(streaming=True)
    row_count = 0
    try:
        cursor.execute(build_select_sql(table))
        column_sql = ", ".join(quote_identifier(column) for column in table.columns)
        while True:
            rows = cursor.fetchmany(batch_size)
            if not rows:
                break

            output.write(f"INSERT INTO {quote_identifier(table.name)} ({column_sql}) VALUES\n")
            values_sql = []
            for row in rows:
                values_sql.append("  (" + ", ".join(sql_literal(value) for value in row) + ")")
            output.write(",\n".join(values_sql))
            output.write(";\n\n")
            row_count += len(rows)
    finally:
        close_cursor(cursor)

    if row_count == 0:
        output.write("-- 0 rows; no INSERT statement generated.\n\n")
    return row_count


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as input_file:
        for chunk in iter(lambda: input_file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def restrict_permissions(path: Path, mode: int) -> None:
    """Restrict snapshot files on POSIX systems; preserve normal Windows behavior."""

    if os.name != "nt":
        os.chmod(path, mode)


def default_output_dir() -> Path:
    timestamp = dt.datetime.now(dt.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    return Path(__file__).resolve().parent / "database_snapshot" / timestamp


def export_snapshot(
    config: DatabaseConfig,
    output_dir: Path,
    batch_size: int,
) -> dict[str, Any]:
    if output_dir.exists():
        raise SnapshotError(f"输出目录已存在，为避免覆盖已有基准数据而停止：{output_dir}")

    database = connect_database(config)
    data_tmp = output_dir / "data.sql.part"
    data_file = output_dir / "data.sql"
    manifest_file = output_dir / "manifest.json"
    generated_at = dt.datetime.now(dt.timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")

    try:
        output_dir.mkdir(parents=True, exist_ok=False)
        restrict_permissions(output_dir, 0o700)
        begin_consistent_snapshot(database)
        server_version = fetch_scalar(database, "SELECT VERSION()")
        table_rows = list_base_tables(database, config.name)
        tables = [
            get_table_info(database, config.name, table_name, engine)
            for table_name, engine in table_rows
        ]

        table_manifest = []
        total_rows = 0
        with data_tmp.open("w", encoding="utf-8", newline="\n") as output:
            write_sql_header(output, generated_at)
            for table in tables:
                row_count = write_table_data(database, output, table, batch_size)
                total_rows += row_count
                table_manifest.append(
                    {
                        "name": table.name,
                        "engine": table.engine,
                        "columns": list(table.columns),
                        "primary_key_columns": list(table.primary_key_columns),
                        "row_count": row_count,
                    }
                )
            write_sql_footer(output)

        data_tmp.replace(data_file)
        restrict_permissions(data_file, 0o600)
        manifest = {
            "format": SNAPSHOT_FORMAT,
            "exported_at_utc": generated_at,
            "database": {
                "host": config.host,
                "port": config.port,
                "name": config.name,
                "user": config.user,
                "server_version": server_version,
            },
            "driver": database.driver,
            "consistent_snapshot": all(
                (table.engine or "").upper() == "INNODB" for table in tables
            ),
            "data_file": data_file.name,
            "data_bytes": data_file.stat().st_size,
            "data_sha256": sha256_file(data_file),
            "table_count": len(tables),
            "total_rows": total_rows,
            "tables": table_manifest,
        }
        manifest_file.write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        restrict_permissions(manifest_file, 0o600)
        return manifest
    except Exception:
        if data_tmp.exists():
            data_tmp.unlink()
        raise
    finally:
        try:
            database.rollback()
        except Exception:
            pass
        finally:
            try:
                database.close()
            except Exception:
                pass


def positive_int(value: str) -> int:
    parsed = int(value)
    if parsed <= 0:
        raise argparse.ArgumentTypeError("必须是大于 0 的整数")
    return parsed


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export all MySQL base-table data as a migration baseline snapshot."
    )
    parser.add_argument(
        "-o",
        "--output",
        help="输出目录；默认写入 scripts/database_snapshot/<UTC 时间戳>",
    )
    parser.add_argument("--host", help="覆盖数据库主机；优先使用 CITYWALK_DB_HOST")
    parser.add_argument("--port", type=positive_int, help="覆盖数据库端口")
    parser.add_argument("--database", dest="database_name", help="覆盖数据库名")
    parser.add_argument("--user", help="覆盖数据库用户名")
    parser.add_argument(
        "--batch-size",
        type=positive_int,
        default=DEFAULT_BATCH_SIZE,
        help=f"每条 INSERT 的行数，默认：{DEFAULT_BATCH_SIZE}",
    )
    return parser.parse_args()


def build_config(args: argparse.Namespace) -> DatabaseConfig:
    return DatabaseConfig(
        host=args.host or DB_HOST,
        port=args.port or DB_PORT,
        name=args.database_name or DB_NAME,
        user=args.user or DB_USER,
        password=DB_PASSWORD,
    )


def main() -> None:
    args = parse_args()
    config = build_config(args)
    output_dir = (
        Path(args.output).expanduser().resolve()
        if args.output
        else default_output_dir()
    )
    manifest = export_snapshot(config, output_dir, args.batch_size)

    print("数据库全量数据已导出。")
    print(f"数据库：{config.host}:{config.port}/{config.name}")
    print(f"表数量：{manifest['table_count']}")
    print(f"总行数：{manifest['total_rows']}")
    print(f"一致性快照：{'是' if manifest['consistent_snapshot'] else '否（存在非 InnoDB 表）'}")
    print(f"SQL 文件：{output_dir / manifest['data_file']}")
    print(f"清单文件：{output_dir / 'manifest.json'}")
    print("注意：快照可能包含用户和管理员敏感数据，请限制文件访问权限。")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"导出失败：{exc}", file=sys.stderr)
        sys.exit(1)
