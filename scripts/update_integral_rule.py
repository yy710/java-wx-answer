#!/usr/bin/env python3
"""
Replace the points rule agreement content directly in MySQL.

Run on the server:
    python3 update_integral_rule.py
"""

from pathlib import Path
import shutil
import subprocess
import sys


DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_NAME = "city-walk"
DB_USER = "city-walk"
DB_PASSWORD = "KaTmAjEJxynhFh7S"

RULE_FILE_NAME = "积分规则.txt"
AGREEMENT_TABLE = "yk_agreement"
SEARCH_KEYWORDS = ("积分规则", "奖品兑换")
TARGET_TITLE = "积分规则"

# 如需强制按协议 type 更新，将 None 改为对应数字。
TARGET_TYPE = None


def read_rule_content():
    rule_path = Path(__file__).resolve().parent / RULE_FILE_NAME
    if not rule_path.exists():
        raise RuntimeError(f"未找到积分规则文件：{rule_path}")

    content = rule_path.read_text(encoding="utf-8").strip()
    if not content:
        raise RuntimeError(f"积分规则文件为空：{rule_path}")
    return content


def build_search_sql():
    if TARGET_TYPE is not None:
        return (
            f"SELECT id, type, IFNULL(title, ''), IFNULL(CHAR_LENGTH(content), 0) "
            f"FROM {AGREEMENT_TABLE} WHERE type = %s"
        ), [TARGET_TYPE]

    if TARGET_TITLE:
        return (
            f"SELECT id, type, IFNULL(title, ''), IFNULL(CHAR_LENGTH(content), 0) "
            f"FROM {AGREEMENT_TABLE} WHERE title = %s"
        ), [TARGET_TITLE]

    title_conditions = " OR ".join(["title LIKE %s" for _ in SEARCH_KEYWORDS])
    content_conditions = " OR ".join(["content LIKE %s" for _ in SEARCH_KEYWORDS])
    params = [f"%{keyword}%" for keyword in SEARCH_KEYWORDS]
    params.extend(f"%{keyword}%" for keyword in SEARCH_KEYWORDS)
    return (
        f"SELECT id, type, IFNULL(title, ''), IFNULL(CHAR_LENGTH(content), 0) "
        f"FROM {AGREEMENT_TABLE} "
        f"WHERE {title_conditions} OR {content_conditions}"
    ), params


def select_target(fetch_rows):
    rows = fetch_rows()
    if len(rows) == 1:
        return rows[0]

    if not rows:
        if TARGET_TITLE:
            raise RuntimeError(f"未找到标题为“{TARGET_TITLE}”的协议记录。")

        keyword_text = "、".join(SEARCH_KEYWORDS)
        raise RuntimeError(f"未找到标题或内容包含“{keyword_text}”的协议记录。")

    lines = ["找到多条疑似积分规则协议，请先设置 TARGET_TYPE / TARGET_TITLE 或清理数据后重试："]
    for row_id, row_type, row_title, content_len in rows:
        lines.append(f"- id={row_id}, type={row_type}, title={row_title}, content_len={content_len}")
    raise RuntimeError("\n".join(lines))


def update_with_pymysql(new_content):
    try:
        import pymysql
    except ImportError:
        return False

    connection = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        autocommit=False,
    )
    try:
        search_sql, params = build_search_sql()
        with connection.cursor() as cursor:
            target = select_target(lambda: fetch_all(cursor, search_sql, params))
            cursor.execute(
                f"UPDATE {AGREEMENT_TABLE} SET content = %s WHERE id = %s",
                (new_content, target[0]),
            )
        connection.commit()
        print_success(target, new_content)
        return True
    finally:
        connection.close()


def update_with_mysql_connector(new_content):
    try:
        import mysql.connector
    except ImportError:
        return False

    connection = mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
    )
    try:
        search_sql, params = build_search_sql()
        cursor = connection.cursor()
        target = select_target(lambda: fetch_all(cursor, search_sql, params))
        cursor.execute(
            f"UPDATE {AGREEMENT_TABLE} SET content = %s WHERE id = %s",
            (new_content, target[0]),
        )
        connection.commit()
        print_success(target, new_content)
        return True
    finally:
        connection.close()


def fetch_all(cursor, sql, params):
    cursor.execute(sql, params)
    return cursor.fetchall()


def sql_quote(value):
    return "'" + str(value).replace("\\", "\\\\").replace("'", "\\'") + "'"


def build_cli_search_sql():
    if TARGET_TYPE is not None:
        where_sql = f"type = {int(TARGET_TYPE)}"
    elif TARGET_TITLE:
        where_sql = f"title = {sql_quote(TARGET_TITLE)}"
    else:
        title_conditions = " OR ".join(
            [f"title LIKE {sql_quote('%' + keyword + '%')}" for keyword in SEARCH_KEYWORDS]
        )
        content_conditions = " OR ".join(
            [f"content LIKE {sql_quote('%' + keyword + '%')}" for keyword in SEARCH_KEYWORDS]
        )
        where_sql = f"{title_conditions} OR {content_conditions}"

    return (
        "SELECT id, type, IFNULL(title, ''), IFNULL(CHAR_LENGTH(content), 0) "
        f"FROM {AGREEMENT_TABLE} WHERE {where_sql};"
    )


def parse_cli_rows(output):
    rows = []
    for line in output.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t", 3)
        if len(parts) != 4:
            raise RuntimeError(f"MySQL 返回格式异常：{line}")
        rows.append(tuple(parts))
    return rows


def update_with_mysql_cli(new_content):
    mysql_bin = shutil.which("mysql")
    if not mysql_bin:
        return False

    base_command = [
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
    ]

    search_result = subprocess.run(
        [*base_command, "-e", build_cli_search_sql()],
        text=True,
        capture_output=True,
        check=False,
    )
    if search_result.returncode != 0:
        raise RuntimeError(search_result.stderr.strip() or "MySQL 查询失败")

    target = select_target(lambda: parse_cli_rows(search_result.stdout))
    update_sql = (
        f"UPDATE {AGREEMENT_TABLE} "
        f"SET content = {sql_quote(new_content)} "
        f"WHERE id = {sql_quote(target[0])};"
    )
    update_result = subprocess.run(
        [*base_command, "-e", update_sql],
        text=True,
        capture_output=True,
        check=False,
    )
    if update_result.returncode != 0:
        raise RuntimeError(update_result.stderr.strip() or "MySQL 更新失败")

    print_success(target, new_content)
    return True


def print_success(target, new_content):
    row_id, row_type, row_title, _ = target
    print("积分规则内容已更新。")
    print(f"协议 id：{row_id}")
    print(f"协议 type：{row_type}")
    print(f"协议标题：{row_title}")
    print(f"新内容长度：{len(new_content)}")


def main():
    new_content = read_rule_content()
    ok = (
        update_with_pymysql(new_content)
        or update_with_mysql_connector(new_content)
        or update_with_mysql_cli(new_content)
    )

    if not ok:
        raise RuntimeError("未找到可用的 MySQL 连接方式，请安装 pymysql 或 mysql 命令行客户端。")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"更新失败：{exc}", file=sys.stderr)
        sys.exit(1)
