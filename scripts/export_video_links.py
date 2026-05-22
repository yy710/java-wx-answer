#!/usr/bin/env python3
"""
Export video links from MySQL to a CSV file.

Run on the server:
    python3 export_video_links.py

Optional environment variables:
    CITYWALK_DB_HOST
    CITYWALK_DB_PORT
    CITYWALK_DB_NAME
    CITYWALK_DB_USER
    CITYWALK_DB_PASSWORD

Optional arguments:
    python3 export_video_links.py --output /tmp/video_links.csv
    python3 export_video_links.py --include-empty
"""

from pathlib import Path
import argparse
import csv
import os
import shutil
import subprocess
import sys


DB_HOST = os.getenv("CITYWALK_DB_HOST", os.getenv("DB_HOST", "127.0.0.1"))
DB_PORT = int(os.getenv("CITYWALK_DB_PORT", os.getenv("DB_PORT", "3306")))
DB_NAME = os.getenv("CITYWALK_DB_NAME", os.getenv("DB_NAME", "city-walk"))
DB_USER = os.getenv("CITYWALK_DB_USER", os.getenv("DB_USER", "city-walk"))
DB_PASSWORD = os.getenv("CITYWALK_DB_PASSWORD", os.getenv("DB_PASSWORD", "KaTmAjEJxynhFh7S"))

CSV_COLUMNS = [
    "id",
    "title",
    "descr",
    "status",
    "url_video",
    "url_pic",
    "seq",
    "ticket_total",
    "create_time",
    "update_time",
]


def build_select_sql(include_empty):
    where_sql = ""
    if not include_empty:
        where_sql = "WHERE url_video IS NOT NULL AND TRIM(url_video) <> ''"

    columns = ", ".join(CSV_COLUMNS)
    return (
        f"SELECT {columns} "
        "FROM video "
        f"{where_sql} "
        "ORDER BY seq ASC, create_time ASC, id ASC"
    )


def normalize_row(row):
    normalized = []
    for value in row:
        if value is None:
            normalized.append("")
        else:
            normalized.append(str(value))
    return normalized


def fetch_with_pymysql(sql):
    try:
        import pymysql
    except ImportError:
        return None

    connection = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        autocommit=True,
    )
    try:
        with connection.cursor() as cursor:
            cursor.execute(sql)
            return [normalize_row(row) for row in cursor.fetchall()]
    finally:
        connection.close()


def fetch_with_mysql_connector(sql):
    try:
        import mysql.connector
    except ImportError:
        return None

    connection = mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
    )
    try:
        cursor = connection.cursor()
        cursor.execute(sql)
        return [normalize_row(row) for row in cursor.fetchall()]
    finally:
        connection.close()


def fetch_with_mysql_cli(sql):
    mysql_bin = shutil.which("mysql")
    if not mysql_bin:
        return None

    command = [
        mysql_bin,
        "-h",
        DB_HOST,
        "-P",
        str(DB_PORT),
        "-u",
        DB_USER,
        f"-p{DB_PASSWORD}",
        "-D",
        DB_NAME,
        "-N",
        "-B",
        "--raw",
        "-e",
        sql,
    ]
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "MySQL 查询失败")

    rows = []
    for line in result.stdout.splitlines():
        rows.append(normalize_row(line.split("\t")))
    return rows


def fetch_rows(sql):
    for fetcher in (fetch_with_pymysql, fetch_with_mysql_connector, fetch_with_mysql_cli):
        rows = fetcher(sql)
        if rows is not None:
            return rows

    raise RuntimeError("未找到可用的 MySQL 连接方式，请安装 pymysql、mysql-connector-python 或 mysql 命令行客户端。")


def write_csv(rows, output_path):
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8-sig", newline="") as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(CSV_COLUMNS)
        writer.writerows(rows)


def parse_args():
    default_output = Path(__file__).resolve().parent / "video_links.csv"
    parser = argparse.ArgumentParser(description="Export MySQL video links to CSV. This script only reads data.")
    parser.add_argument(
        "-o",
        "--output",
        default=str(default_output),
        help=f"CSV 输出路径，默认：{default_output}",
    )
    parser.add_argument(
        "--include-empty",
        action="store_true",
        help="导出 url_video 为空的记录；默认只导出有视频链接的记录。",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    output_path = Path(args.output).expanduser().resolve()
    sql = build_select_sql(args.include_empty)
    rows = fetch_rows(sql)
    write_csv(rows, output_path)

    print("视频链接已导出。")
    print(f"数据库：{DB_HOST}:{DB_PORT}/{DB_NAME}")
    print(f"记录数：{len(rows)}")
    print(f"CSV 文件：{output_path}")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"导出失败：{exc}", file=sys.stderr)
        sys.exit(1)
