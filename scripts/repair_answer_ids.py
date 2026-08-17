#!/usr/bin/env python3
"""
恢复 2026-07-27 题库的稳定 ID，并按当前 CSV 修正题目内容。

默认只连接数据库做检查，不写入：
    python3 repair_answer_ids.py

完全离线检查备份和 CSV：
    python3 repair_answer_ids.py --offline-check

确认检查结果后执行：
    python3 repair_answer_ids.py --apply

设计目标：
1. 以 answer_backup/topic_20260727_111212.csv 及对应选项备份为 ID 基线。
2. 保留基线 topic/topic_item ID，只更新当前 CSV 中确实变化的内容。
3. 把 2026-07-27 多次误导入产生的历史 topic_id/topic_item_id 统一映射回基线 ID。
4. 执行前备份所有会修改的行；任意校验失败时回滚整个事务。
5. 正确答案允许清理单元格末尾的中英文问号等标点，但清理后必须是明确、合法的答案。

依赖：
    Python 3.9+
    pip install pymysql
"""

from __future__ import annotations

import argparse
import csv
import os
import sys
from collections import defaultdict
from collections.abc import Iterable, Sequence
from dataclasses import dataclass
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path

import pymysql

BASE_DIR = Path(__file__).resolve().parent
DEFAULT_BACKUP_DIR = BASE_DIR / "answer_backup"
DEFAULT_BASELINE_TIMESTAMP = "20260727_111212"
EXPECTED_TOPIC_COUNT = 599
EXPECTED_ITEM_COUNT = 1756
DEFAULT_REWARD_AMOUNT = Decimal("10.00")

CSV_FILES = (
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
)

TRUE_ANSWERS = {"正确", "对", "TRUE", "T", "YES", "Y", "√", "A"}
FALSE_ANSWERS = {"错误", "错", "FALSE", "F", "NO", "N", "×", "X", "B"}
SINGLE_ANSWERS = {"A", "B", "C", "D", "E", "F"}
ANSWER_EDGE_PUNCTUATION = "?!！？。.;；,，、"

# 2026-07-27 最新 CSV 唯一确认过的题干语义修改。
# key 是 11:12 基线备份中的旧题干，value 是当前 CSV 中的新题干。
TITLE_RENAMES = {
    "经过线上面试你被娉为“省老年协会招聘形象大使”，且月薪六千，但是需要先交纳保证金，你选择先交纳保证金。": "经过线上面试你被聘为“省老年协会招聘形象大使”，且月薪六千，但是需要先交纳保证金，你选择先交纳保证金。",
}

TOPIC_REFERENCE_TABLES = (
    ("topic_record_topic", "topic_id"),
    ("topic_record_single", "topic_id"),
)
ITEM_REFERENCE_TABLES = (
    ("topic_record_topic_item", "topic_item_id"),
    ("topic_record_single_item", "topic_item_id"),
)


class RepairError(RuntimeError):
    """可安全展示给操作人员的校验或修复错误。"""


@dataclass(frozen=True)
class DesiredItem:
    title: str
    seq: int
    answer_flag: bool


@dataclass(frozen=True)
class DesiredTopic:
    title: str
    reward_amount: Decimal
    items: tuple[DesiredItem, ...]


@dataclass(frozen=True)
class SnapshotItem:
    id: int
    topic_id: int
    title: str
    seq: int
    answer_flag: bool


@dataclass(frozen=True)
class SnapshotTopic:
    id: int
    source_title: str
    canonical_title: str
    reward_amount: Decimal
    create_time: str
    update_time: str
    items: tuple[SnapshotItem, ...]


@dataclass(frozen=True)
class CanonicalItem:
    id: int
    topic_id: int
    title: str
    seq: int
    answer_flag: bool


@dataclass(frozen=True)
class CanonicalTopic:
    id: int
    title: str
    reward_amount: Decimal
    items: tuple[CanonicalItem, ...]


@dataclass(frozen=True)
class CurrentItem:
    id: int
    topic_id: int
    title: str
    seq: int
    answer_flag: bool


@dataclass(frozen=True)
class CurrentTopic:
    id: int
    source_title: str
    canonical_title: str
    reward_amount: Decimal
    items: tuple[CurrentItem, ...]


def normalize_text(value: object) -> str:
    if value is None:
        return ""
    text = str(value).replace("\r", " ").replace("\n", " ").strip()
    return " ".join(text.split())


def canonical_title(value: object) -> str:
    title = normalize_text(value)
    return TITLE_RENAMES.get(title, title)


def normalize_answer(value: object) -> tuple[str, bool]:
    raw = normalize_text(value).replace("　", "").replace(" ", "").upper()
    cleaned = raw.strip(ANSWER_EDGE_PUNCTUATION)
    return cleaned, cleaned != raw


def parse_bool(value: object) -> bool:
    return normalize_text(value).lower() in {"1", "true", "yes", "y"}


def parse_judge_csv(
    file_path: Path,
    encoding: str,
) -> tuple[list[DesiredTopic], list[str]]:
    topics: list[DesiredTopic] = []
    notes: list[str] = []
    with file_path.open("r", encoding=encoding, newline="") as file_obj:
        reader = csv.reader(file_obj)
        next(reader, None)
        for row_num, row in enumerate(reader, start=2):
            if not row:
                continue
            title = normalize_text(row[0] if len(row) > 0 else "")
            if not title:
                continue
            answer, cleaned = normalize_answer(row[4] if len(row) > 4 else "")
            if answer in TRUE_ANSWERS:
                is_true = True
            elif answer in FALSE_ANSWERS:
                is_true = False
            else:
                raise RepairError(
                    f"{file_path.name} 第 {row_num} 行判断题答案无效：{answer!r}，题目：{title}"
                )
            if cleaned:
                notes.append(
                    f"{file_path.name} 第 {row_num} 行答案已清理为 {answer!r}：{title}"
                )
            topics.append(
                DesiredTopic(
                    title=title,
                    reward_amount=DEFAULT_REWARD_AMOUNT,
                    items=(
                        DesiredItem(title="正确", seq=1, answer_flag=is_true),
                        DesiredItem(title="错误", seq=2, answer_flag=not is_true),
                    ),
                )
            )
    return topics, notes


def parse_single_csv(
    file_path: Path,
    encoding: str,
) -> tuple[list[DesiredTopic], list[str]]:
    topics: list[DesiredTopic] = []
    notes: list[str] = []
    with file_path.open("r", encoding=encoding, newline="") as file_obj:
        reader = csv.reader(file_obj)
        next(reader, None)
        for row_num, row in enumerate(reader, start=2):
            if not row:
                continue
            title = normalize_text(row[0] if len(row) > 0 else "")
            if not title:
                continue
            answer, cleaned = normalize_answer(row[4] if len(row) > 4 else "")
            if answer not in SINGLE_ANSWERS:
                raise RepairError(
                    f"{file_path.name} 第 {row_num} 行单选题答案无效：{answer!r}，题目：{title}"
                )
            if cleaned:
                notes.append(
                    f"{file_path.name} 第 {row_num} 行答案已清理为 {answer!r}：{title}"
                )

            items: list[DesiredItem] = []
            for seq, letter in enumerate(("A", "B", "C", "D", "E", "F"), start=1):
                column_index = seq + 4
                option_title = normalize_text(
                    row[column_index] if len(row) > column_index else ""
                )
                if not option_title:
                    if answer == letter:
                        raise RepairError(
                            f"{file_path.name} 第 {row_num} 行正确答案 {letter} 没有选项内容：{title}"
                        )
                    continue
                items.append(
                    DesiredItem(
                        title=option_title,
                        seq=seq,
                        answer_flag=answer == letter,
                    )
                )

            if not items:
                raise RepairError(
                    f"{file_path.name} 第 {row_num} 行没有有效选项：{title}"
                )
            if sum(item.answer_flag for item in items) != 1:
                raise RepairError(
                    f"{file_path.name} 第 {row_num} 行必须且只能有一个正确选项：{title}"
                )
            topics.append(
                DesiredTopic(
                    title=title,
                    reward_amount=DEFAULT_REWARD_AMOUNT,
                    items=tuple(items),
                )
            )
    return topics, notes


def load_desired_topics() -> tuple[dict[str, DesiredTopic], list[str], list[str]]:
    parsed_topics: list[tuple[str, DesiredTopic]] = []
    answer_notes: list[str] = []
    for file_info in CSV_FILES:
        file_path = file_info["path"]
        if not file_path.exists():
            raise RepairError(f"CSV 文件不存在：{file_path}")
        if file_info["type"] == "judge":
            topics, notes = parse_judge_csv(file_path, file_info["encoding"])
        else:
            topics, notes = parse_single_csv(file_path, file_info["encoding"])
        parsed_topics.extend((file_path.name, topic) for topic in topics)
        answer_notes.extend(notes)

    desired: dict[str, DesiredTopic] = {}
    sources: dict[str, str] = {}
    duplicate_notes: list[str] = []
    for source_name, topic in parsed_topics:
        title = canonical_title(topic.title)
        normalized_topic = DesiredTopic(
            title=title,
            reward_amount=topic.reward_amount,
            items=topic.items,
        )
        previous = desired.get(title)
        if previous is None:
            desired[title] = normalized_topic
            sources[title] = source_name
            continue
        if previous != normalized_topic:
            raise RepairError(
                f"重复题目内容不一致：{title}，来源：{sources[title]} / {source_name}"
            )
        duplicate_notes.append(
            f"重复题目内容一致，保留首份：{title}（{sources[title]} / {source_name}）"
        )

    item_count = sum(len(topic.items) for topic in desired.values())
    if len(desired) != EXPECTED_TOPIC_COUNT or item_count != EXPECTED_ITEM_COUNT:
        raise RepairError(
            "CSV 解析数量与本次已确认状态不一致："
            f"实际 {len(desired)} 题/{item_count} 选项，"
            f"期望 {EXPECTED_TOPIC_COUNT} 题/{EXPECTED_ITEM_COUNT} 选项"
        )
    return desired, answer_notes, duplicate_notes


def load_snapshot(
    topic_path: Path,
    item_path: Path,
) -> dict[str, SnapshotTopic]:
    if not topic_path.exists():
        raise RepairError(f"题目备份不存在：{topic_path}")
    if not item_path.exists():
        raise RepairError(f"选项备份不存在：{item_path}")

    with topic_path.open("r", encoding="utf-8-sig", newline="") as file_obj:
        topic_rows = list(csv.DictReader(file_obj))
    with item_path.open("r", encoding="utf-8-sig", newline="") as file_obj:
        item_rows = list(csv.DictReader(file_obj))

    items_by_topic_id: dict[int, list[SnapshotItem]] = defaultdict(list)
    for row in item_rows:
        item = SnapshotItem(
            id=int(row["id"]),
            topic_id=int(row["topic_id"]),
            title=normalize_text(row["title"]),
            seq=int(row["seq"]),
            answer_flag=parse_bool(row["answer_flag"]),
        )
        items_by_topic_id[item.topic_id].append(item)

    result: dict[str, SnapshotTopic] = {}
    for row in topic_rows:
        topic_id = int(row["id"])
        source_title = normalize_text(row["title"])
        title = canonical_title(source_title)
        items = tuple(
            sorted(items_by_topic_id.get(topic_id, []), key=lambda item: item.seq)
        )
        if not items:
            raise RepairError(f"{topic_path.name} 中题目没有选项：{source_title}")
        if len({item.seq for item in items}) != len(items):
            raise RepairError(f"{item_path.name} 中题目选项序号重复：{source_title}")
        if title in result:
            raise RepairError(f"{topic_path.name} 中规范化后题目标题重复：{title}")
        result[title] = SnapshotTopic(
            id=topic_id,
            source_title=source_title,
            canonical_title=title,
            reward_amount=Decimal(row["reward_amount"]),
            create_time=row.get("create_time", ""),
            update_time=row.get("update_time", ""),
            items=items,
        )

    orphan_item_count = sum(
        len(items)
        for topic_id, items in items_by_topic_id.items()
        if all(topic.id != topic_id for topic in result.values())
    )
    if orphan_item_count:
        raise RepairError(
            f"{item_path.name} 中存在 {orphan_item_count} 个找不到题目的孤立选项"
        )
    return result


def build_canonical_topics(
    baseline: dict[str, SnapshotTopic],
    desired: dict[str, DesiredTopic],
) -> dict[str, CanonicalTopic]:
    missing = sorted(set(baseline) - set(desired))
    added = sorted(set(desired) - set(baseline))
    if missing or added:
        raise RepairError(
            "最新 CSV 与 ID 基线存在未确认的题目增删："
            f"缺少 {missing[:5]}，新增 {added[:5]}"
        )

    canonical: dict[str, CanonicalTopic] = {}
    for title, desired_topic in desired.items():
        baseline_topic = baseline[title]
        baseline_items = {item.seq: item for item in baseline_topic.items}
        desired_items = {item.seq: item for item in desired_topic.items}
        if set(baseline_items) != set(desired_items):
            raise RepairError(
                f"题目选项数量或序号发生未确认变化，无法安全复用 ID：{title}"
            )
        items = tuple(
            CanonicalItem(
                id=baseline_items[seq].id,
                topic_id=baseline_topic.id,
                title=desired_items[seq].title,
                seq=seq,
                answer_flag=desired_items[seq].answer_flag,
            )
            for seq in sorted(desired_items)
        )
        if sum(item.answer_flag for item in items) != 1:
            raise RepairError(f"题目必须且只能有一个正确选项：{title}")
        canonical[title] = CanonicalTopic(
            id=baseline_topic.id,
            title=title,
            reward_amount=desired_topic.reward_amount,
            items=items,
        )
    return canonical


def baseline_diff(
    baseline: dict[str, SnapshotTopic],
    canonical: dict[str, CanonicalTopic],
) -> tuple[list[str], list[str]]:
    topic_changes: list[str] = []
    item_changes: list[str] = []
    for title, canonical_topic in canonical.items():
        baseline_topic = baseline[title]
        if (
            baseline_topic.source_title != canonical_topic.title
            or baseline_topic.reward_amount != canonical_topic.reward_amount
        ):
            topic_changes.append(
                f"topic {baseline_topic.id}: "
                f"{baseline_topic.source_title!r} -> {canonical_topic.title!r}, "
                f"reward {baseline_topic.reward_amount} -> {canonical_topic.reward_amount}"
            )
        old_items = {item.seq: item for item in baseline_topic.items}
        for item in canonical_topic.items:
            old = old_items[item.seq]
            if old.title != item.title or old.answer_flag != item.answer_flag:
                item_changes.append(
                    f"topic {canonical_topic.id} / item {item.id} / seq {item.seq}: "
                    f"title {old.title!r} -> {item.title!r}, "
                    f"answer {int(old.answer_flag)} -> {int(item.answer_flag)}"
                )
    if len(topic_changes) != len(TITLE_RENAMES):
        raise RepairError(
            f"题目内容差异不是已确认的 {len(TITLE_RENAMES)} 处，实际 {len(topic_changes)} 处"
        )
    return topic_changes, item_changes


def load_current_topics(cursor) -> dict[str, CurrentTopic]:
    cursor.execute(
        """
        SELECT id, title, reward_amount
        FROM topic
        ORDER BY id
        """
    )
    topic_rows = cursor.fetchall()
    cursor.execute(
        """
        SELECT id, topic_id, title, seq, answer_flag
        FROM topic_item
        ORDER BY topic_id, seq, id
        """
    )
    item_rows = cursor.fetchall()

    items_by_topic_id: dict[int, list[CurrentItem]] = defaultdict(list)
    for row in item_rows:
        item = CurrentItem(
            id=int(row["id"]),
            topic_id=int(row["topic_id"]),
            title=normalize_text(row["title"]),
            seq=int(row["seq"]),
            answer_flag=bool(row["answer_flag"]),
        )
        items_by_topic_id[item.topic_id].append(item)

    current: dict[str, CurrentTopic] = {}
    topic_ids: set[int] = set()
    for row in topic_rows:
        topic_id = int(row["id"])
        topic_ids.add(topic_id)
        source_title = normalize_text(row["title"])
        title = canonical_title(source_title)
        items = tuple(
            sorted(items_by_topic_id.get(topic_id, []), key=lambda item: item.seq)
        )
        if not items:
            raise RepairError(f"当前数据库题目没有选项：{source_title}（{topic_id}）")
        if len({item.seq for item in items}) != len(items):
            raise RepairError(
                f"当前数据库题目选项序号重复：{source_title}（{topic_id}）"
            )
        if title in current:
            raise RepairError(f"当前数据库规范化后题目标题重复：{title}")
        current[title] = CurrentTopic(
            id=topic_id,
            source_title=source_title,
            canonical_title=title,
            reward_amount=Decimal(row["reward_amount"]),
            items=items,
        )

    orphan_count = sum(
        len(items)
        for topic_id, items in items_by_topic_id.items()
        if topic_id not in topic_ids
    )
    if orphan_count:
        raise RepairError(f"当前 topic_item 中存在 {orphan_count} 个孤立选项")
    return current


def validate_current_shape(
    current: dict[str, CurrentTopic],
    canonical: dict[str, CanonicalTopic],
) -> None:
    missing = sorted(set(canonical) - set(current))
    extra = sorted(set(current) - set(canonical))
    if missing or extra:
        raise RepairError(
            f"当前数据库题库结构与基线不一致：缺少 {missing[:5]}，多出 {extra[:5]}"
        )
    for title, current_topic in current.items():
        current_seqs = {item.seq for item in current_topic.items}
        canonical_seqs = {item.seq for item in canonical[title].items}
        if current_seqs != canonical_seqs:
            raise RepairError(f"当前数据库选项结构与基线不一致：{title}")


def merge_mapping(
    target: dict[int, int],
    old_id: int,
    new_id: int,
    label: str,
) -> None:
    previous = target.get(old_id)
    if previous is not None and previous != new_id:
        raise RepairError(
            f"{label} ID 映射冲突：{old_id} 同时指向 {previous} 和 {new_id}"
        )
    target[old_id] = new_id


def add_snapshot_mappings(
    snapshot: dict[str, SnapshotTopic],
    canonical: dict[str, CanonicalTopic],
    topic_map: dict[int, int],
    item_map: dict[int, int],
    source_name: str,
) -> None:
    unknown = sorted(set(snapshot) - set(canonical))
    if unknown:
        raise RepairError(f"{source_name} 中存在基线未识别题目：{unknown[:5]}")
    for title, topic in snapshot.items():
        canonical_topic = canonical[title]
        merge_mapping(topic_map, topic.id, canonical_topic.id, "topic")
        canonical_items = {item.seq: item for item in canonical_topic.items}
        for item in topic.items:
            target_item = canonical_items.get(item.seq)
            if target_item is None:
                raise RepairError(
                    f"{source_name} 中存在基线未识别选项序号：{title} / {item.seq}"
                )
            merge_mapping(item_map, item.id, target_item.id, "topic_item")


def build_id_mappings(
    backup_dir: Path,
    baseline_timestamp: str,
    current: dict[str, CurrentTopic],
    canonical: dict[str, CanonicalTopic],
) -> tuple[dict[int, int], dict[int, int], list[str]]:
    topic_map: dict[int, int] = {}
    item_map: dict[int, int] = {}
    source_names: list[str] = []
    date_prefix = baseline_timestamp.split("_", 1)[0] + "_"

    for topic_path in sorted(backup_dir.glob(f"topic_{date_prefix}*.csv")):
        timestamp = topic_path.stem.removeprefix("topic_")
        item_path = backup_dir / f"topic_item_{timestamp}.csv"
        if not item_path.exists():
            raise RepairError(f"缺少与 {topic_path.name} 配对的选项备份")
        snapshot = load_snapshot(topic_path, item_path)
        add_snapshot_mappings(
            snapshot,
            canonical,
            topic_map,
            item_map,
            topic_path.name,
        )
        source_names.append(timestamp)

    if baseline_timestamp not in source_names:
        raise RepairError(f"ID 映射备份中没有基线时间戳：{baseline_timestamp}")

    for title, current_topic in current.items():
        canonical_topic = canonical[title]
        merge_mapping(topic_map, current_topic.id, canonical_topic.id, "topic")
        canonical_items = {item.seq: item for item in canonical_topic.items}
        for item in current_topic.items:
            merge_mapping(
                item_map,
                item.id,
                canonical_items[item.seq].id,
                "topic_item",
            )

    return topic_map, item_map, source_names


def get_db_config(args: argparse.Namespace) -> dict[str, object]:
    try:
        from import_answer import DB_CONFIG as import_db_config
    except Exception as exc:
        raise RepairError(f"无法读取 import_answer.py 的数据库配置：{exc}") from exc

    config = dict(import_db_config)
    config.update(
        {
            "host": args.db_host or os.getenv("CITY_WALK_DB_HOST") or config["host"],
            "port": args.db_port
            or int(os.getenv("CITY_WALK_DB_PORT", str(config["port"]))),
            "user": args.db_user or os.getenv("CITY_WALK_DB_USER") or config["user"],
            "password": args.db_password
            or os.getenv("CITY_WALK_DB_PASSWORD")
            or config["password"],
            "database": args.db_name
            or os.getenv("CITY_WALK_DB_NAME")
            or config["database"],
            "autocommit": False,
            "cursorclass": pymysql.cursors.DictCursor,
        }
    )
    return config


def create_temp_mapping_table(
    cursor,
    table_name: str,
    mapping: dict[int, int],
) -> None:
    cursor.execute(f"DROP TEMPORARY TABLE IF EXISTS `{table_name}`")
    cursor.execute(
        f"""
        CREATE TEMPORARY TABLE `{table_name}` (
            old_id BIGINT NOT NULL PRIMARY KEY,
            new_id BIGINT NOT NULL,
            INDEX idx_new_id (new_id)
        ) ENGINE=InnoDB
        """
    )
    changed = [
        (old_id, new_id) for old_id, new_id in mapping.items() if old_id != new_id
    ]
    if changed:
        cursor.executemany(
            f"INSERT INTO `{table_name}` (old_id, new_id) VALUES (%s, %s)",
            changed,
        )


def count_mapping_rows(cursor, table_name: str) -> int:
    cursor.execute(f"SELECT COUNT(*) AS total FROM `{table_name}`")
    return int(cursor.fetchone()["total"])


def ensure_no_foreign_keys(cursor) -> None:
    cursor.execute(
        """
        SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME,
               REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
        FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND REFERENCED_TABLE_NAME IN ('topic', 'topic_item')
        """
    )
    foreign_keys = list(cursor.fetchall())
    if foreign_keys:
        raise RepairError(
            "检测到 topic/topic_item 外键，当前脚本拒绝自动修改主键，请人工确认："
            + "; ".join(
                f"{row['TABLE_NAME']}.{row['COLUMN_NAME']} ({row['CONSTRAINT_NAME']})"
                for row in foreign_keys
            )
        )


def ensure_target_ids_available(
    cursor,
    data_table: str,
    mapping_table: str,
) -> None:
    cursor.execute(
        f"""
        SELECT m.old_id, m.new_id
        FROM `{mapping_table}` m
        INNER JOIN `{data_table}` source ON source.id = m.old_id
        INNER JOIN `{data_table}` target ON target.id = m.new_id
        WHERE m.old_id <> m.new_id
        LIMIT 10
        """
    )
    collisions = cursor.fetchall()
    if collisions:
        raise RepairError(
            f"{data_table} 目标 ID 已被其他当前行占用，拒绝执行：{collisions}"
        )


def fetch_rows(cursor, sql: str) -> tuple[list[str], list[dict[str, object]]]:
    cursor.execute(sql)
    rows = list(cursor.fetchall())
    columns = [column[0] for column in cursor.description]
    return columns, rows


def write_csv(
    path: Path,
    columns: Sequence[str],
    rows: Iterable[dict[str, object]],
) -> None:
    with path.open("w", encoding="utf-8-sig", newline="") as file_obj:
        writer = csv.DictWriter(file_obj, fieldnames=list(columns))
        writer.writeheader()
        writer.writerows(rows)


def backup_affected_rows(
    cursor,
    backup_dir: Path,
    topic_map: dict[int, int],
    item_map: dict[int, int],
) -> Path:
    timestamp = datetime.now(timezone.utc).astimezone().strftime("%Y%m%d_%H%M%S")
    output_dir = backup_dir / f"repair_{timestamp}"
    output_dir.mkdir(parents=True, exist_ok=False)

    write_csv(
        output_dir / "topic_id_map.csv",
        ("old_id", "new_id"),
        (
            {"old_id": old_id, "new_id": new_id}
            for old_id, new_id in sorted(topic_map.items())
            if old_id != new_id
        ),
    )
    write_csv(
        output_dir / "topic_item_id_map.csv",
        ("old_id", "new_id"),
        (
            {"old_id": old_id, "new_id": new_id}
            for old_id, new_id in sorted(item_map.items())
            if old_id != new_id
        ),
    )

    columns, rows = fetch_rows(cursor, "SELECT * FROM topic ORDER BY id")
    write_csv(output_dir / "topic_before.csv", columns, rows)
    columns, rows = fetch_rows(
        cursor,
        "SELECT * FROM topic_item ORDER BY topic_id, seq, id",
    )
    write_csv(output_dir / "topic_item_before.csv", columns, rows)

    for table_name, column_name in TOPIC_REFERENCE_TABLES:
        columns, rows = fetch_rows(
            cursor,
            f"""
            SELECT ref.*
            FROM `{table_name}` ref
            INNER JOIN repair_topic_id_map m ON ref.`{column_name}` = m.old_id
            ORDER BY ref.`{column_name}`
            """,
        )
        write_csv(output_dir / f"{table_name}_before.csv", columns, rows)

    for table_name, column_name in ITEM_REFERENCE_TABLES:
        columns, rows = fetch_rows(
            cursor,
            f"""
            SELECT ref.*
            FROM `{table_name}` ref
            INNER JOIN repair_item_id_map m ON ref.`{column_name}` = m.old_id
            ORDER BY ref.`{column_name}`
            """,
        )
        write_csv(output_dir / f"{table_name}_before.csv", columns, rows)

    return output_dir


def reference_counts(cursor) -> dict[str, int]:
    result: dict[str, int] = {}
    for table_name, column_name in TOPIC_REFERENCE_TABLES:
        cursor.execute(
            f"""
            SELECT COUNT(*) AS total
            FROM `{table_name}` ref
            INNER JOIN repair_topic_id_map m ON ref.`{column_name}` = m.old_id
            """
        )
        result[f"{table_name}.{column_name}"] = int(cursor.fetchone()["total"])
    for table_name, column_name in ITEM_REFERENCE_TABLES:
        cursor.execute(
            f"""
            SELECT COUNT(*) AS total
            FROM `{table_name}` ref
            INNER JOIN repair_item_id_map m ON ref.`{column_name}` = m.old_id
            """
        )
        result[f"{table_name}.{column_name}"] = int(cursor.fetchone()["total"])
    return result


def build_content_updates(
    current: dict[str, CurrentTopic],
    canonical: dict[str, CanonicalTopic],
) -> tuple[
    list[tuple[str, Decimal, int]],
    list[tuple[str, int, int, int]],
]:
    topic_updates: list[tuple[str, Decimal, int]] = []
    item_updates: list[tuple[str, int, int, int]] = []
    for title, canonical_topic in canonical.items():
        current_topic = current[title]
        if (
            current_topic.source_title != canonical_topic.title
            or current_topic.reward_amount != canonical_topic.reward_amount
        ):
            topic_updates.append(
                (
                    canonical_topic.title,
                    canonical_topic.reward_amount,
                    canonical_topic.id,
                )
            )

        current_items = {item.seq: item for item in current_topic.items}
        for canonical_item in canonical_topic.items:
            current_item = current_items[canonical_item.seq]
            if (
                current_item.title != canonical_item.title
                or current_item.answer_flag != canonical_item.answer_flag
            ):
                item_updates.append(
                    (
                        canonical_item.title,
                        canonical_item.seq,
                        int(canonical_item.answer_flag),
                        canonical_item.id,
                    )
                )
    return topic_updates, item_updates


def apply_repair(
    cursor,
    topic_updates: Sequence[tuple[str, Decimal, int]],
    item_updates: Sequence[tuple[str, int, int, int]],
) -> dict[str, int]:
    affected: dict[str, int] = {}

    for table_name, column_name in TOPIC_REFERENCE_TABLES:
        affected[f"{table_name}.{column_name}"] = cursor.execute(
            f"""
            UPDATE `{table_name}` ref
            INNER JOIN repair_topic_id_map m ON ref.`{column_name}` = m.old_id
            SET ref.`{column_name}` = m.new_id
            """
        )
    for table_name, column_name in ITEM_REFERENCE_TABLES:
        affected[f"{table_name}.{column_name}"] = cursor.execute(
            f"""
            UPDATE `{table_name}` ref
            INNER JOIN repair_item_id_map m ON ref.`{column_name}` = m.old_id
            SET ref.`{column_name}` = m.new_id
            """
        )

    affected["topic_item.topic_id"] = cursor.execute(
        """
        UPDATE topic_item item
        INNER JOIN repair_topic_id_map m ON item.topic_id = m.old_id
        SET item.topic_id = m.new_id
        """
    )
    affected["topic.id"] = cursor.execute(
        """
        UPDATE topic data
        INNER JOIN repair_topic_id_map m ON data.id = m.old_id
        SET data.id = m.new_id
        """
    )
    affected["topic_item.id"] = cursor.execute(
        """
        UPDATE topic_item data
        INNER JOIN repair_item_id_map m ON data.id = m.old_id
        SET data.id = m.new_id
        """
    )

    affected["topic.content"] = 0
    if topic_updates:
        affected["topic.content"] = cursor.executemany(
            """
            UPDATE topic
            SET title = %s, reward_amount = %s, update_time = NOW()
            WHERE id = %s
            """,
            topic_updates,
        )

    affected["topic_item.content"] = 0
    if item_updates:
        affected["topic_item.content"] = cursor.executemany(
            """
            UPDATE topic_item
            SET title = %s, seq = %s, answer_flag = %s
            WHERE id = %s
            """,
            item_updates,
        )
    return affected


def verify_database(
    cursor,
    canonical: dict[str, CanonicalTopic],
) -> None:
    current = load_current_topics(cursor)
    validate_current_shape(current, canonical)
    for title, canonical_topic in canonical.items():
        current_topic = current[title]
        if current_topic.id != canonical_topic.id:
            raise RepairError(
                f"修复后 topic ID 不一致：{title}，"
                f"{current_topic.id} != {canonical_topic.id}"
            )
        if (
            current_topic.source_title != canonical_topic.title
            or current_topic.reward_amount != canonical_topic.reward_amount
        ):
            raise RepairError(f"修复后题目内容不一致：{title}")
        current_items = {item.seq: item for item in current_topic.items}
        for canonical_item in canonical_topic.items:
            current_item = current_items[canonical_item.seq]
            if (
                current_item.id != canonical_item.id
                or current_item.topic_id != canonical_topic.id
                or current_item.title != canonical_item.title
                or current_item.answer_flag != canonical_item.answer_flag
            ):
                raise RepairError(
                    f"修复后选项不一致：{title} / seq {canonical_item.seq}"
                )

    cursor.execute(
        """
        SELECT COUNT(*) AS total
        FROM (
            SELECT topic_id
            FROM topic_item
            GROUP BY topic_id
            HAVING SUM(answer_flag = 1) <> 1
        ) bad_topics
        """
    )
    bad_count = int(cursor.fetchone()["total"])
    if bad_count:
        raise RepairError(f"修复后仍有 {bad_count} 道题不是唯一正确答案")

    for table_name, column_name in TOPIC_REFERENCE_TABLES:
        cursor.execute(
            f"""
            SELECT COUNT(*) AS total
            FROM `{table_name}` ref
            INNER JOIN repair_topic_id_map m ON ref.`{column_name}` = m.old_id
            """
        )
        if int(cursor.fetchone()["total"]):
            raise RepairError(f"修复后 {table_name}.{column_name} 仍有旧 ID 引用")
    for table_name, column_name in ITEM_REFERENCE_TABLES:
        cursor.execute(
            f"""
            SELECT COUNT(*) AS total
            FROM `{table_name}` ref
            INNER JOIN repair_item_id_map m ON ref.`{column_name}` = m.old_id
            """
        )
        if int(cursor.fetchone()["total"]):
            raise RepairError(f"修复后 {table_name}.{column_name} 仍有旧 ID 引用")


def print_offline_report(
    baseline_timestamp: str,
    canonical: dict[str, CanonicalTopic],
    topic_changes: Sequence[str],
    item_changes: Sequence[str],
    answer_notes: Sequence[str],
    duplicate_notes: Sequence[str],
) -> None:
    item_count = sum(len(topic.items) for topic in canonical.values())
    print("=== 离线校验通过 ===")
    print(f"ID 基线：{baseline_timestamp}")
    print(f"目标题库：{len(canonical)} 题，{item_count} 个选项")
    print(f"题目内容变化：{len(topic_changes)}")
    for change in topic_changes:
        print(f"  - {change}")
    print(f"选项/正确答案变化：{len(item_changes)}")
    for change in item_changes:
        print(f"  - {change}")
    print(f"清理异常答案单元格：{len(answer_notes)}")
    for note in answer_notes:
        print(f"  - {note}")
    print(f"忽略完全一致的重复题目：{len(duplicate_notes)}")
    for note in duplicate_notes:
        print(f"  - {note}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="恢复稳定题目 ID，并按最新 CSV 修正题目内容"
    )
    parser.add_argument(
        "--offline-check",
        action="store_true",
        help="只检查备份和 CSV，不连接数据库",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="备份后在一个事务内执行修复；不传时只做数据库检查",
    )
    parser.add_argument(
        "--baseline-timestamp",
        default=DEFAULT_BASELINE_TIMESTAMP,
        help=f"ID 基线备份时间戳，默认 {DEFAULT_BASELINE_TIMESTAMP}",
    )
    parser.add_argument(
        "--backup-dir",
        type=Path,
        default=DEFAULT_BACKUP_DIR,
        help=f"备份目录，默认 {DEFAULT_BACKUP_DIR}",
    )
    parser.add_argument("--db-host", help="覆盖数据库地址")
    parser.add_argument("--db-port", type=int, help="覆盖数据库端口")
    parser.add_argument("--db-user", help="覆盖数据库用户")
    parser.add_argument("--db-password", help="覆盖数据库密码")
    parser.add_argument("--db-name", help="覆盖数据库名")
    args = parser.parse_args()
    if args.offline_check and args.apply:
        parser.error("--offline-check 和 --apply 不能同时使用")
    return args


def main() -> int:
    args = parse_args()
    backup_dir = args.backup_dir.resolve()
    baseline_topic_path = backup_dir / f"topic_{args.baseline_timestamp}.csv"
    baseline_item_path = backup_dir / f"topic_item_{args.baseline_timestamp}.csv"

    desired, answer_notes, duplicate_notes = load_desired_topics()
    baseline = load_snapshot(baseline_topic_path, baseline_item_path)
    canonical = build_canonical_topics(baseline, desired)
    topic_changes, item_changes = baseline_diff(baseline, canonical)
    print_offline_report(
        args.baseline_timestamp,
        canonical,
        topic_changes,
        item_changes,
        answer_notes,
        duplicate_notes,
    )

    if args.offline_check:
        print("当前为 offline-check，未连接数据库。")
        return 0

    config = get_db_config(args)
    connection = pymysql.connect(**config)
    try:
        with connection.cursor() as cursor:
            current = load_current_topics(cursor)
            validate_current_shape(current, canonical)
            topic_updates, item_updates = build_content_updates(
                current,
                canonical,
            )
            topic_map, item_map, source_names = build_id_mappings(
                backup_dir,
                args.baseline_timestamp,
                current,
                canonical,
            )
            create_temp_mapping_table(
                cursor,
                "repair_topic_id_map",
                topic_map,
            )
            create_temp_mapping_table(
                cursor,
                "repair_item_id_map",
                item_map,
            )
            connection.commit()

            topic_map_count = count_mapping_rows(cursor, "repair_topic_id_map")
            item_map_count = count_mapping_rows(cursor, "repair_item_id_map")
            refs = reference_counts(cursor)
            print("=== 数据库修复计划 ===")
            print(f"参与 ID 映射的备份：{', '.join(source_names)} + 当前数据库")
            print(f"需要恢复的 topic ID：{topic_map_count}")
            print(f"需要恢复的 topic_item ID：{item_map_count}")
            print(f"需要修改的 topic 内容：{len(topic_updates)}")
            print(f"需要修改的 topic_item 内容：{len(item_updates)}")
            for name, count in refs.items():
                print(f"需要同步的历史引用 {name}：{count}")

            ensure_no_foreign_keys(cursor)
            ensure_target_ids_available(
                cursor,
                "topic",
                "repair_topic_id_map",
            )
            ensure_target_ids_available(
                cursor,
                "topic_item",
                "repair_item_id_map",
            )

            if not args.apply:
                print("数据库检查通过。当前为检查模式，未修改数据库。")
                print("确认以上数量后，使用 --apply 执行。")
                return 0

            backup_output = backup_affected_rows(
                cursor,
                backup_dir,
                topic_map,
                item_map,
            )
            print(f"执行前备份已写入：{backup_output}")

            connection.begin()
            try:
                affected = apply_repair(
                    cursor,
                    topic_updates,
                    item_updates,
                )
                verify_database(cursor, canonical)
                connection.commit()
            except Exception:
                connection.rollback()
                raise

            print("=== 修复完成并通过事务内校验 ===")
            for name, count in affected.items():
                print(f"{name}: {count} 行")
            print(f"可回退备份：{backup_output}")
            return 0
    finally:
        connection.close()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RepairError as exc:
        print(f"修复已停止：{exc}", file=sys.stderr)
        raise SystemExit(1)
