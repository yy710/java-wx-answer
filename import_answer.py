#!/usr/bin/env python3
"""
批量导入题库到项目数据库。

默认行为：
1. 清空旧题库：topic_item / topic
2. 导入 4 个 CSV
3. 判断题生成“正确/错误”两个选项
4. 单选题按 A-F 选项导入

依赖：
    pip install pymysql

示例：
    python3 import_answer.py
    python3 import_answer.py --keep-old
    python3 import_answer.py --dry-run
    python3 import_answer.py --backup-only
"""

from __future__ import annotations

import argparse
import csv
import sys
import time
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable

import pymysql


BASE_DIR = Path(__file__).resolve().parent

CSV_FILES = [
    {
        "path": BASE_DIR / "金融知识问答题库(202603第二批)判断题.csv",
        "type": "judge",
        "encoding": "gbk",
    },
    {
        "path": BASE_DIR / "金融知识问答题库(202603第二批)单选题.csv",
        "type": "single",
        "encoding": "utf-8",
    },
    {
        "path": BASE_DIR / "金融知识问答题库模板(2026315)判断题.csv",
        "type": "judge",
        "encoding": "gbk",
    },
    {
        "path": BASE_DIR / "金融知识问答题库模板(2026315)单选题.csv",
        "type": "single",
        "encoding": "gbk",
    },
]

DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "city-walk",
    "password": "KaTmAjEJxynhFh7S",
    "database": "city-walk",
    "charset": "utf8mb4",
    "autocommit": False,
    "cursorclass": pymysql.cursors.Cursor,
}

DEFAULT_REWARD_AMOUNT = "1.00"
BACKUP_DIR = BASE_DIR / "answer_backup"
_LAST_ID = int(time.time() * 1_000_000)


@dataclass
class TopicItemRecord:
    title: str
    seq: int
    answer_flag: bool


@dataclass
class TopicRecord:
    title: str
    reward_amount: str
    items: list[TopicItemRecord]


def normalize_text(value: object) -> str:
    if value is None:
        return ""
    text = str(value).replace("\r", " ").replace("\n", " ").strip()
    return " ".join(text.split())


def normalize_answer(value: object) -> str:
    return normalize_text(value).replace("　", "").replace(" ", "").upper()


def new_id() -> int:
    global _LAST_ID
    current = int(time.time() * 1_000_000)
    if current <= _LAST_ID:
        current = _LAST_ID + 1
    _LAST_ID = current
    return current


def parse_judge_csv(file_path: Path, encoding: str) -> list[TopicRecord]:
    topics: list[TopicRecord] = []
    with file_path.open("r", encoding=encoding, newline="") as f:
        reader = csv.reader(f)
        next(reader, None)
        for row in reader:
            if not row:
                continue
            title = normalize_text(row[0] if len(row) > 0 else "")
            correct_answer = normalize_answer(row[4] if len(row) > 4 else "")
            if not title:
                continue

            is_true = correct_answer in {"正确", "对", "TRUE", "T", "YES", "Y", "√", "A"}
            topics.append(
                TopicRecord(
                    title=title,
                    reward_amount=DEFAULT_REWARD_AMOUNT,
                    items=[
                        TopicItemRecord(title="正确", seq=1, answer_flag=is_true),
                        TopicItemRecord(title="错误", seq=2, answer_flag=not is_true),
                    ],
                )
            )
    return topics


def parse_single_csv(file_path: Path, encoding: str) -> list[TopicRecord]:
    topics: list[TopicRecord] = []
    with file_path.open("r", encoding=encoding, newline="") as f:
        reader = csv.reader(f)
        next(reader, None)
        for row_num, row in enumerate(reader, start=2):
            if not row:
                continue
            title = normalize_text(row[0] if len(row) > 0 else "")
            correct_answer = normalize_answer(row[4] if len(row) > 4 else "")
            if not title:
                continue

            option_values = []
            for idx in range(5, 11):
                option_values.append(normalize_text(row[idx] if len(row) > idx else ""))

            letters = ["A", "B", "C", "D", "E", "F"]
            items: list[TopicItemRecord] = []
            for seq, (letter, option_title) in enumerate(zip(letters, option_values), start=1):
                if not option_title:
                    continue
                items.append(
                    TopicItemRecord(
                        title=option_title,
                        seq=seq,
                        answer_flag=(correct_answer == letter),
                    )
                )

            if not items:
                print(f"警告：单选题没有有效选项，已跳过：{file_path.name} 第 {row_num} 行", file=sys.stderr)
                continue

            topics.append(
                TopicRecord(
                    title=title,
                    reward_amount=DEFAULT_REWARD_AMOUNT,
                    items=items,
                )
            )
    return topics


def load_topics() -> list[TopicRecord]:
    all_topics: list[TopicRecord] = []
    seen_titles: set[str] = set()

    for file_info in CSV_FILES:
        file_path = file_info["path"]
        question_type = file_info["type"]
        encoding = file_info["encoding"]

        if not file_path.exists():
            raise FileNotFoundError(f"文件不存在: {file_path}")

        if question_type == "judge":
            topics = parse_judge_csv(file_path, encoding)
        else:
            topics = parse_single_csv(file_path, encoding)

        for topic in topics:
            if topic.title in seen_titles:
                continue
            seen_titles.add(topic.title)
            all_topics.append(topic)

    return all_topics


def connect_db():
    return pymysql.connect(**DB_CONFIG)


def backup_old_data(cursor) -> tuple[Path, Path, int, int]:
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    topic_csv = BACKUP_DIR / f"topic_{timestamp}.csv"
    topic_item_csv = BACKUP_DIR / f"topic_item_{timestamp}.csv"

    cursor.execute(
        """
        SELECT id, title, reward_amount, create_time, update_time
        FROM topic
        ORDER BY create_time ASC, id ASC
        """
    )
    topic_rows = cursor.fetchall()

    cursor.execute(
        """
        SELECT id, topic_id, title, seq, answer_flag
        FROM topic_item
        ORDER BY topic_id ASC, seq ASC, id ASC
        """
    )
    topic_item_rows = cursor.fetchall()

    with topic_csv.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "title", "reward_amount", "create_time", "update_time"])
        writer.writerows(topic_rows)

    with topic_item_csv.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "topic_id", "title", "seq", "answer_flag"])
        writer.writerows(topic_item_rows)

    return topic_csv, topic_item_csv, len(topic_rows), len(topic_item_rows)


def clear_old_data(cursor) -> None:
    cursor.execute("DELETE FROM topic_item")
    cursor.execute("DELETE FROM topic")


def insert_topics(cursor, topics: Iterable[TopicRecord]) -> tuple[int, int]:
    topic_sql = """
        INSERT INTO topic (id, title, reward_amount, create_time, update_time)
        VALUES (%s, %s, %s, NOW(), NOW())
    """
    item_sql = """
        INSERT INTO topic_item (id, topic_id, title, seq, answer_flag)
        VALUES (%s, %s, %s, %s, %s)
    """

    topic_count = 0
    item_count = 0

    for topic in topics:
        topic_id = new_id()
        cursor.execute(topic_sql, (topic_id, topic.title, topic.reward_amount))
        topic_count += 1

        for item in topic.items:
            cursor.execute(item_sql, (new_id(), topic_id, item.title, item.seq, int(item.answer_flag)))
            item_count += 1

    return topic_count, item_count


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="批量导入题库到 MySQL")
    parser.add_argument("--keep-old", action="store_true", help="保留旧题库，不先清空 topic_item/topic")
    parser.add_argument("--dry-run", action="store_true", help="只解析 CSV 并输出统计，不写数据库")
    parser.add_argument("--backup-only", action="store_true", help="只备份当前题库到 CSV，不执行导入")
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    if args.backup_only:
        conn = connect_db()
        try:
            with conn.cursor() as cursor:
                print("开始备份旧题库数据...")
                topic_csv, topic_item_csv, old_topic_count, old_item_count = backup_old_data(cursor)
            conn.commit()
            print(
                f"备份完成：topic {old_topic_count} 条 -> {topic_csv}，"
                f"topic_item {old_item_count} 条 -> {topic_item_csv}"
            )
            print("当前为 backup-only，仅备份未导入")
            return 0
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()

    topics = load_topics()
    item_total = sum(len(topic.items) for topic in topics)

    print(f"解析完成：题目 {len(topics)} 道，选项 {item_total} 条")

    if args.dry_run:
        print("当前为 dry-run，仅解析未写库")
        return 0

    conn = connect_db()
    try:
        with conn.cursor() as cursor:
            if not args.keep_old:
                print("开始备份旧题库数据...")
                topic_csv, topic_item_csv, old_topic_count, old_item_count = backup_old_data(cursor)
                print(
                    f"备份完成：topic {old_topic_count} 条 -> {topic_csv}，"
                    f"topic_item {old_item_count} 条 -> {topic_item_csv}"
                )
                print("开始清空旧题库数据...")
                clear_old_data(cursor)

            print("开始写入数据库...")
            topic_count, topic_item_count = insert_topics(cursor, topics)
        conn.commit()
        print(f"导入成功：topic {topic_count} 条，topic_item {topic_item_count} 条")
        return 0
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
