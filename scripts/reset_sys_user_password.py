#!/usr/bin/env python3
"""
Reset the backend admin password directly in MySQL.

Run on the server:
    python3 reset_sys_user_password.py
"""

import hashlib
import shutil
import subprocess
import sys


DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_NAME = "city-walk"
DB_USER = "city-walk"
DB_PASSWORD = "KaTmAjEJxynhFh7S"

ADMIN_USERNAME = "admin"
NEW_PASSWORD = "yk6868"


def reset_with_pymysql(password_md5):
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
        with connection.cursor() as cursor:
            cursor.execute("SELECT id FROM sys_user WHERE username = %s LIMIT 1", (ADMIN_USERNAME,))
            if not cursor.fetchone():
                raise RuntimeError(f"未找到后台账号：{ADMIN_USERNAME}")

            cursor.execute(
                "UPDATE sys_user SET password = %s, update_time = NOW() WHERE username = %s",
                (password_md5, ADMIN_USERNAME),
            )
        connection.commit()
        return True
    finally:
        connection.close()


def reset_with_mysql_connector(password_md5):
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
        cursor = connection.cursor()
        cursor.execute("SELECT id FROM sys_user WHERE username = %s LIMIT 1", (ADMIN_USERNAME,))
        if not cursor.fetchone():
            raise RuntimeError(f"未找到后台账号：{ADMIN_USERNAME}")

        cursor.execute(
            "UPDATE sys_user SET password = %s, update_time = NOW() WHERE username = %s",
            (password_md5, ADMIN_USERNAME),
        )
        connection.commit()
        return True
    finally:
        connection.close()


def sql_quote(value):
    return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"


def reset_with_mysql_cli(password_md5):
    mysql_bin = shutil.which("mysql")
    if not mysql_bin:
        return False

    count_sql = f"SELECT COUNT(1) FROM sys_user WHERE username = {sql_quote(ADMIN_USERNAME)};"
    update_sql = (
        "UPDATE sys_user "
        f"SET password = {sql_quote(password_md5)}, update_time = NOW() "
        f"WHERE username = {sql_quote(ADMIN_USERNAME)};"
    )

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

    count_result = subprocess.run(
        [*base_command, "-e", count_sql],
        text=True,
        capture_output=True,
        check=False,
    )
    if count_result.returncode != 0:
        raise RuntimeError(count_result.stderr.strip() or "MySQL 查询失败")

    count_text = count_result.stdout.strip()
    if count_text != "1":
        raise RuntimeError(f"未找到唯一后台账号：{ADMIN_USERNAME}，匹配数量：{count_text or 0}")

    update_result = subprocess.run(
        [*base_command, "-e", update_sql],
        text=True,
        capture_output=True,
        check=False,
    )
    if update_result.returncode != 0:
        raise RuntimeError(update_result.stderr.strip() or "MySQL 更新失败")
    return True


def main():
    password_md5 = hashlib.md5(NEW_PASSWORD.encode("utf-8")).hexdigest()

    ok = (
        reset_with_pymysql(password_md5)
        or reset_with_mysql_connector(password_md5)
        or reset_with_mysql_cli(password_md5)
    )

    if not ok:
        raise RuntimeError("未找到可用的 MySQL 连接方式，请安装 pymysql 或 mysql 命令行客户端。")

    print(f"后台账号 {ADMIN_USERNAME} 的密码已重置为：{NEW_PASSWORD}")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"重置失败：{exc}", file=sys.stderr)
        sys.exit(1)
