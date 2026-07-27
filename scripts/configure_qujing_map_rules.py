#!/usr/bin/env python3
"""
Configure the Qujing map quiz rules directly in MySQL without changing backend code.

The default mode is read-only. Examples:

    python3 configure_qujing_map_rules.py --activity-id 123
    python3 configure_qujing_map_rules.py --dry-run --activity-id 123
    python3 configure_qujing_map_rules.py --apply --activity-id 123
    python3 configure_qujing_map_rules.py --rollback qujing_map_rule_backup/backup_123_20260727_130000.json

Environment variables:

    CITYWALK_DB_HOST / DB_HOST
    CITYWALK_DB_PORT / DB_PORT
    CITYWALK_DB_NAME / DB_NAME
    CITYWALK_DB_USER / DB_USER
    CITYWALK_DB_PASSWORD / DB_PASSWORD
    CITYWALK_REDIS_HOST / REDIS_HOST
    CITYWALK_REDIS_PORT / REDIS_PORT
    CITYWALK_REDIS_DB / REDIS_DB
    CITYWALK_REDIS_PASSWORD / REDIS_PASSWORD

Only these data fields are changed:

* topic_activity.start_time/end_time/limit_num/reward_extra/update_time
* topic_line.status/topic_num/seq/light_seq for the selected activity
* topic.reward_amount/update_time

The script never changes historical answer records or wallet records.
"""

from __future__ import annotations

import argparse
import datetime as dt
import decimal
import json
import os
import re
import shutil
import subprocess
import sys
from collections import Counter
from contextlib import contextmanager
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable, Mapping, Sequence


BASE_DIR = Path(__file__).resolve().parent
DEFAULT_BACKUP_DIR = BASE_DIR / "qujing_map_rule_backup"

TARGET_START_TIME = dt.datetime(2026, 7, 30, 0, 1, 0)
TARGET_END_TIME = dt.datetime(2026, 8, 13, 17, 0, 0)
TARGET_TOPIC_NUM = 5
TARGET_REWARD_AMOUNT = decimal.Decimal("10.00")
TARGET_ACTIVITY_REWARD_EXTRA = decimal.Decimal("0.00")
TOPIC_ACTIVITY_CACHE_KEY = "topic:activity"

DB_HOST = os.getenv("CITYWALK_DB_HOST", os.getenv("DB_HOST", "127.0.0.1"))
DB_PORT = int(os.getenv("CITYWALK_DB_PORT", os.getenv("DB_PORT", "3306")))
DB_NAME = os.getenv("CITYWALK_DB_NAME", os.getenv("DB_NAME", "city-walk"))
DB_USER = os.getenv("CITYWALK_DB_USER", os.getenv("DB_USER", "city-walk"))
DB_PASSWORD = os.getenv(
    "CITYWALK_DB_PASSWORD",
    os.getenv("DB_PASSWORD", "KaTmAjEJxynhFh7S"),
)

REDIS_HOST = os.getenv(
    "CITYWALK_REDIS_HOST",
    os.getenv("REDIS_HOST", "127.0.0.1"),
)
REDIS_PORT = int(os.getenv("CITYWALK_REDIS_PORT", os.getenv("REDIS_PORT", "6379")))
REDIS_DB = int(os.getenv("CITYWALK_REDIS_DB", os.getenv("REDIS_DB", "1")))
REDIS_PASSWORD = os.getenv(
    "CITYWALK_REDIS_PASSWORD",
    os.getenv("REDIS_PASSWORD", "citywalkRedis"),
)


@dataclass(frozen=True)
class StationRule:
    key: str
    label: str
    keywords: tuple[str, ...]
    order: int


STATION_RULES = (
    StationRule(
        key="zhanyi-pear-river-source",
        label="珠江源",
        keywords=("珠江源", "珠江正源", "沾益"),
        order=1,
    ),
    StationRule(
        key="qilin-south-gate",
        label="南城门",
        keywords=("南城门", "曲靖南城门", "麒麟"),
        order=2,
    ),
    StationRule(
        key="luliang-cuanlongyan-monument",
        label="爨龙颜碑",
        keywords=("爨龙颜碑", "爨宝子碑", "二爨", "爨文化"),
        order=3,
    ),
    StationRule(
        key="luliang-highland-vegetables",
        label="高原蔬菜",
        keywords=("高原蔬菜", "蔬菜产业基地", "陆良蔬菜", "南菜北运"),
        order=4,
    ),
    StationRule(
        key="luoping-canola-fields",
        label="油菜花海",
        keywords=("油菜花海", "油菜花", "罗平"),
        order=5,
    ),
)


class ConfigurationError(RuntimeError):
    """Raised when production data does not satisfy safe update preconditions."""


class DatabaseConnection:
    def __init__(self, connection: Any, driver_name: str):
        self.connection = connection
        self.driver_name = driver_name

    @contextmanager
    def cursor(self):
        if self.driver_name == "pymysql":
            cursor = self.connection.cursor()
        else:
            cursor = self.connection.cursor(dictionary=True)
        try:
            yield cursor
        finally:
            cursor.close()

    def commit(self) -> None:
        self.connection.commit()

    def rollback(self) -> None:
        self.connection.rollback()

    def close(self) -> None:
        self.connection.close()


def connect_database() -> DatabaseConnection:
    errors: list[str] = []

    try:
        import pymysql

        connection = pymysql.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            charset="utf8mb4",
            autocommit=False,
            cursorclass=pymysql.cursors.DictCursor,
        )
        with connection.cursor() as cursor:
            cursor.execute("SET time_zone = '+08:00'")
        return DatabaseConnection(connection, "pymysql")
    except ImportError:
        errors.append("pymysql 未安装")
    except Exception as exc:
        errors.append(f"pymysql 连接失败：{exc}")

    try:
        import mysql.connector

        connection = mysql.connector.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            charset="utf8mb4",
            autocommit=False,
        )
        cursor = connection.cursor()
        try:
            cursor.execute("SET time_zone = '+08:00'")
        finally:
            cursor.close()
        return DatabaseConnection(connection, "mysql-connector")
    except ImportError:
        errors.append("mysql-connector-python 未安装")
    except Exception as exc:
        errors.append(f"mysql-connector 连接失败：{exc}")

    raise RuntimeError(
        "无法连接 MySQL。请安装 pymysql 或 mysql-connector-python，并检查数据库配置。\n"
        + "\n".join(f"- {message}" for message in errors)
    )


def fetch_all(cursor: Any, sql: str, params: Sequence[Any] = ()) -> list[dict[str, Any]]:
    cursor.execute(sql, params)
    return [dict(row) for row in cursor.fetchall()]


def fetch_one(cursor: Any, sql: str, params: Sequence[Any] = ()) -> dict[str, Any] | None:
    cursor.execute(sql, params)
    row = cursor.fetchone()
    return dict(row) if row is not None else None


def normalize_text(value: Any) -> str:
    if value is None:
        return ""
    return " ".join(str(value).replace("\r", " ").replace("\n", " ").split())


def _matching_lines(
    lines: Sequence[Mapping[str, Any]],
    rule: StationRule,
    include_description: bool,
) -> list[Mapping[str, Any]]:
    matches = []
    for line in lines:
        title = normalize_text(line.get("title"))
        text = title
        if include_description:
            text = f"{title} {normalize_text(line.get('descr'))}".strip()
        if any(keyword in text for keyword in rule.keywords):
            matches.append(line)
    return matches


def match_station_lines(
    lines: Sequence[Mapping[str, Any]],
) -> list[tuple[StationRule, Mapping[str, Any]]]:
    if not lines:
        raise ConfigurationError("目标活动没有 topic_line 数据。")

    matched: list[tuple[StationRule, Mapping[str, Any]]] = []
    used_line_ids: set[str] = set()

    for rule in STATION_RULES:
        candidates = _matching_lines(lines, rule, include_description=False)
        match_source = "标题"
        if not candidates:
            candidates = _matching_lines(lines, rule, include_description=True)
            match_source = "标题或描述"

        if len(candidates) != 1:
            candidate_text = ", ".join(
                f"{row.get('id')}:{normalize_text(row.get('title'))}"
                for row in candidates
            ) or "无"
            raise ConfigurationError(
                f"站点“{rule.label}”按{match_source}匹配到 {len(candidates)} 条线路："
                f"{candidate_text}。请先修正生产数据，脚本不会自动猜测。"
            )

        line = candidates[0]
        line_id = str(line["id"])
        if line_id in used_line_ids:
            raise ConfigurationError(
                f"线路 {line_id}:{normalize_text(line.get('title'))} 同时匹配多个站点。"
            )
        used_line_ids.add(line_id)
        matched.append((rule, line))

    return matched


def validate_question_items(
    topics: Sequence[Mapping[str, Any]],
    topic_items: Sequence[Mapping[str, Any]],
) -> None:
    if not topics:
        raise ConfigurationError("topic 题库为空，无法配置地图随机答题。")

    item_count: Counter[str] = Counter()
    correct_count: Counter[str] = Counter()
    known_topic_ids = {str(row["id"]) for row in topics}

    for item in topic_items:
        topic_id = str(item["topic_id"])
        if topic_id not in known_topic_ids:
            continue
        item_count[topic_id] += 1
        if bool(item.get("answer_flag")):
            correct_count[topic_id] += 1

    invalid = []
    for topic in topics:
        topic_id = str(topic["id"])
        option_total = item_count[topic_id]
        answer_total = correct_count[topic_id]
        if option_total < 2 or answer_total != 1:
            invalid.append(
                {
                    "id": topic_id,
                    "title": normalize_text(topic.get("title")),
                    "options": option_total,
                    "correct": answer_total,
                }
            )

    if invalid:
        preview = "; ".join(
            f"{row['id']}:{row['title']}（选项{row['options']}，正确答案{row['correct']}）"
            for row in invalid[:10]
        )
        suffix = f"；另有 {len(invalid) - 10} 道" if len(invalid) > 10 else ""
        raise ConfigurationError(
            f"发现 {len(invalid)} 道题目结构不合规：{preview}{suffix}。"
        )


def get_activity(cursor: Any, activity_id: str, lock: bool) -> dict[str, Any]:
    suffix = " FOR UPDATE" if lock else ""
    row = fetch_one(
        cursor,
        """
        SELECT id, title, start_time, end_time, limit_num, reward_extra, update_time
        FROM topic_activity
        WHERE id = %s
        """
        + suffix,
        (activity_id,),
    )
    if row is None:
        raise ConfigurationError(f"未找到答题活动 ID：{activity_id}")
    return row


def get_activity_lines(cursor: Any, activity_id: str, lock: bool) -> list[dict[str, Any]]:
    suffix = " FOR UPDATE" if lock else ""
    return fetch_all(
        cursor,
        """
        SELECT id, topic_activity_id, title, seq, light_seq, status, topic_num, descr
        FROM topic_line
        WHERE topic_activity_id = %s
        ORDER BY seq ASC, id ASC
        """
        + suffix,
        (activity_id,),
    )


def get_topics(cursor: Any, lock: bool) -> list[dict[str, Any]]:
    suffix = " FOR UPDATE" if lock else ""
    return fetch_all(
        cursor,
        """
        SELECT id, title, reward_amount, update_time
        FROM topic
        ORDER BY id ASC
        """
        + suffix,
    )


def get_topic_items(cursor: Any, lock: bool) -> list[dict[str, Any]]:
    suffix = " FOR UPDATE" if lock else ""
    return fetch_all(
        cursor,
        """
        SELECT id, topic_id, answer_flag
        FROM topic_item
        ORDER BY topic_id ASC, seq ASC, id ASC
        """
        + suffix,
    )


def get_overlapping_activities(
    cursor: Any,
    activity_id: str,
    lock: bool = False,
) -> list[dict[str, Any]]:
    suffix = " FOR UPDATE" if lock else ""
    return fetch_all(
        cursor,
        """
        SELECT id, title, start_time, end_time
        FROM topic_activity
        WHERE id <> %s
          AND start_time < %s
          AND end_time > %s
        ORDER BY start_time ASC, id ASC
        """
        + suffix,
        (activity_id, TARGET_END_TIME, TARGET_START_TIME),
    )


def load_and_validate(
    cursor: Any,
    activity_id: str,
    lock: bool,
) -> tuple[
    dict[str, Any],
    list[dict[str, Any]],
    list[dict[str, Any]],
    list[dict[str, Any]],
    list[tuple[StationRule, Mapping[str, Any]]],
]:
    activity = get_activity(cursor, activity_id, lock)
    lines = get_activity_lines(cursor, activity_id, lock)
    topics = get_topics(cursor, lock)
    topic_items = get_topic_items(cursor, lock)

    overlaps = get_overlapping_activities(cursor, activity_id, lock=lock)
    if overlaps:
        overlap_text = "; ".join(
            f"{row['id']}:{normalize_text(row.get('title'))}"
            f"（{row.get('start_time')} 至 {row.get('end_time')}）"
            for row in overlaps
        )
        raise ConfigurationError(
            "目标积分时间与其他答题活动重叠，不能安全应用：" + overlap_text
        )

    matched_lines = match_station_lines(lines)
    validate_question_items(topics, topic_items)
    return activity, lines, topics, topic_items, matched_lines


def json_value(value: Any) -> Any:
    if isinstance(value, (dt.datetime, dt.date, dt.time)):
        return value.isoformat(sep=" ")
    if isinstance(value, decimal.Decimal):
        return str(value)
    if isinstance(value, bytes):
        return value.decode("utf-8", errors="replace")
    return value


def normalize_rows_for_json(rows: Iterable[Mapping[str, Any]]) -> list[dict[str, Any]]:
    return [
        {str(key): json_value(value) for key, value in row.items()}
        for row in rows
    ]


def safe_filename_component(value: str) -> str:
    cleaned = re.sub(r"[^0-9A-Za-z_-]+", "_", value).strip("_")
    return cleaned or "activity"


def create_backup_payload(
    activity: Mapping[str, Any],
    lines: Sequence[Mapping[str, Any]],
    topics: Sequence[Mapping[str, Any]],
) -> dict[str, Any]:
    return {
        "formatVersion": 1,
        "createdAt": dt.datetime.now().isoformat(timespec="seconds"),
        "database": DB_NAME,
        "activityId": str(activity["id"]),
        "targetRules": {
            "startTime": TARGET_START_TIME.isoformat(sep=" "),
            "endTime": TARGET_END_TIME.isoformat(sep=" "),
            "stationCount": len(STATION_RULES),
            "topicNumPerStation": TARGET_TOPIC_NUM,
            "rewardPerCorrectTopic": str(TARGET_REWARD_AMOUNT),
            "theoreticalMapMaximum": str(
                len(STATION_RULES) * TARGET_TOPIC_NUM * TARGET_REWARD_AMOUNT
            ),
        },
        "data": {
            "topicActivity": normalize_rows_for_json([activity])[0],
            "topicLines": normalize_rows_for_json(lines),
            "topics": normalize_rows_for_json(topics),
        },
    }


def sql_literal(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float, decimal.Decimal)):
        return str(value)
    if isinstance(value, (dt.datetime, dt.date, dt.time)):
        value = value.isoformat(sep=" ")
    text = str(value).replace("\\", "\\\\").replace("'", "''")
    return f"'{text}'"


def build_rollback_sql(payload: Mapping[str, Any]) -> str:
    data = payload["data"]
    activity = data["topicActivity"]
    lines = data["topicLines"]
    topics = data["topics"]

    statements = [
        "-- Generated by configure_qujing_map_rules.py",
        "-- This restores configuration fields only; it does not alter answer or wallet records.",
        "SET time_zone = '+08:00';",
        "START TRANSACTION;",
        (
            "UPDATE topic_activity SET "
            f"start_time = {sql_literal(activity.get('start_time'))}, "
            f"end_time = {sql_literal(activity.get('end_time'))}, "
            f"limit_num = {sql_literal(activity.get('limit_num'))}, "
            f"reward_extra = {sql_literal(activity.get('reward_extra'))}, "
            f"update_time = {sql_literal(activity.get('update_time'))} "
            f"WHERE id = {sql_literal(activity.get('id'))};"
        ),
    ]

    for line in lines:
        statements.append(
            "UPDATE topic_line SET "
            f"status = {sql_literal(line.get('status'))}, "
            f"topic_num = {sql_literal(line.get('topic_num'))}, "
            f"seq = {sql_literal(line.get('seq'))}, "
            f"light_seq = {sql_literal(line.get('light_seq'))} "
            f"WHERE id = {sql_literal(line.get('id'))} "
            f"AND topic_activity_id = {sql_literal(line.get('topic_activity_id'))};"
        )

    for topic in topics:
        statements.append(
            "UPDATE topic SET "
            f"reward_amount = {sql_literal(topic.get('reward_amount'))}, "
            f"update_time = {sql_literal(topic.get('update_time'))} "
            f"WHERE id = {sql_literal(topic.get('id'))};"
        )

    statements.extend(
        [
            "COMMIT;",
            "",
            "-- After the SQL commit, clear only the map activity cache:",
            f"-- redis-cli -n {REDIS_DB} DEL {TOPIC_ACTIVITY_CACHE_KEY}",
            "",
        ]
    )
    return "\n".join(statements)


def write_backup_files(
    payload: Mapping[str, Any],
    backup_dir: Path,
) -> tuple[Path, Path]:
    backup_dir.mkdir(parents=True, exist_ok=True)
    timestamp = dt.datetime.now().strftime("%Y%m%d_%H%M%S")
    activity_component = safe_filename_component(str(payload["activityId"]))
    stem = f"backup_{activity_component}_{timestamp}"
    json_path = backup_dir / f"{stem}.json"
    sql_path = backup_dir / f"{stem}_rollback.sql"

    json_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    sql_path.write_text(build_rollback_sql(payload), encoding="utf-8")
    return json_path, sql_path


def reward_histogram(topics: Sequence[Mapping[str, Any]]) -> dict[str, int]:
    histogram: Counter[str] = Counter()
    for topic in topics:
        value = topic.get("reward_amount")
        histogram[str(value if value is not None else "NULL")] += 1
    return dict(sorted(histogram.items()))


def print_plan(
    activity: Mapping[str, Any],
    lines: Sequence[Mapping[str, Any]],
    topics: Sequence[Mapping[str, Any]],
    topic_items: Sequence[Mapping[str, Any]],
    matched_lines: Sequence[tuple[StationRule, Mapping[str, Any]]],
) -> None:
    selected_ids = {str(line["id"]) for _, line in matched_lines}
    disabled_count = sum(1 for line in lines if str(line["id"]) not in selected_ids)

    print("=== 曲靖站地图答题配置检查 ===")
    print(f"数据库：{DB_HOST}:{DB_PORT}/{DB_NAME}")
    print(f"活动：{activity['id']} / {normalize_text(activity.get('title'))}")
    print(
        "当前活动时间："
        f"{activity.get('start_time')} 至 {activity.get('end_time')}"
    )
    print(f"目标活动时间：{TARGET_START_TIME} 至 {TARGET_END_TIME}")
    print(f"当前 limit_num：{activity.get('limit_num')}")
    print("目标 limit_num：NULL（现有 Java 将其解释为不限次数）")
    print()
    print("站点匹配：")
    for rule, line in matched_lines:
        print(
            f"  {rule.order}. {rule.label} -> "
            f"{line['id']} / {normalize_text(line.get('title'))} "
            f"[status={line.get('status')}, topic_num={line.get('topic_num')}, "
            f"seq={line.get('seq')}, light_seq={line.get('light_seq')}]"
        )
    print(f"目标活动其他线路：{disabled_count} 条，将在 apply 时设为停用")
    print()
    print(f"题库：{len(topics)} 道，选项：{len(topic_items)} 条")
    print(
        "当前题目奖励分布："
        + ", ".join(
            f"{amount}={count}道"
            for amount, count in reward_histogram(topics).items()
        )
    )
    print(f"目标题目奖励：全部 {TARGET_REWARD_AMOUNT}")
    print("注意：topic.reward_amount 是全局题库字段，也会同步影响趣味答题。")
    print(
        "地图理论积分上限："
        f"{len(STATION_RULES)} × {TARGET_TOPIC_NUM} × "
        f"{TARGET_REWARD_AMOUNT} = "
        f"{len(STATION_RULES) * TARGET_TOPIC_NUM * TARGET_REWARD_AMOUNT}"
    )


def apply_updates(
    cursor: Any,
    activity_id: str,
    matched_lines: Sequence[tuple[StationRule, Mapping[str, Any]]],
) -> None:
    cursor.execute(
        """
        UPDATE topic_activity
        SET start_time = %s,
            end_time = %s,
            limit_num = NULL,
            reward_extra = %s,
            update_time = NOW()
        WHERE id = %s
        """,
        (
            TARGET_START_TIME,
            TARGET_END_TIME,
            TARGET_ACTIVITY_REWARD_EXTRA,
            activity_id,
        ),
    )
    cursor.execute(
        "UPDATE topic_line SET status = 0 WHERE topic_activity_id = %s",
        (activity_id,),
    )

    for rule, line in matched_lines:
        cursor.execute(
            """
            UPDATE topic_line
            SET status = 1,
                topic_num = %s,
                seq = %s,
                light_seq = %s
            WHERE id = %s
              AND topic_activity_id = %s
            """,
            (
                TARGET_TOPIC_NUM,
                rule.order,
                rule.order,
                line["id"],
                activity_id,
            ),
        )
    cursor.execute(
        """
        UPDATE topic
        SET reward_amount = %s,
            update_time = NOW()
        WHERE reward_amount IS NULL OR reward_amount <> %s
        """,
        (TARGET_REWARD_AMOUNT, TARGET_REWARD_AMOUNT),
    )


def assert_applied_state(cursor: Any, activity_id: str) -> None:
    activity = get_activity(cursor, activity_id, lock=False)
    if activity.get("start_time") != TARGET_START_TIME:
        raise ConfigurationError("应用后 start_time 校验失败。")
    if activity.get("end_time") != TARGET_END_TIME:
        raise ConfigurationError("应用后 end_time 校验失败。")
    if activity.get("limit_num") is not None:
        raise ConfigurationError("应用后 limit_num 应为 NULL。")
    reward_extra = decimal.Decimal(str(activity.get("reward_extra") or "0"))
    if reward_extra != TARGET_ACTIVITY_REWARD_EXTRA:
        raise ConfigurationError("应用后 reward_extra 校验失败。")

    lines = get_activity_lines(cursor, activity_id, lock=False)
    enabled = [line for line in lines if bool(line.get("status"))]
    if len(enabled) != len(STATION_RULES):
        raise ConfigurationError(
            f"应用后启用线路应为 {len(STATION_RULES)} 条，实际 {len(enabled)} 条。"
        )
    expected_orders = set(range(1, len(STATION_RULES) + 1))
    if {int(line["seq"]) for line in enabled} != expected_orders:
        raise ConfigurationError("应用后线路 seq 不是完整的 1..5。")
    if {int(line["light_seq"]) for line in enabled} != expected_orders:
        raise ConfigurationError("应用后线路 light_seq 不是完整的 1..5。")
    if any(int(line["topic_num"]) != TARGET_TOPIC_NUM for line in enabled):
        raise ConfigurationError("应用后存在 topic_num 不为 5 的启用线路。")

    incorrect_reward = fetch_one(
        cursor,
        """
        SELECT COUNT(*) AS total
        FROM topic
        WHERE reward_amount IS NULL OR reward_amount <> %s
        """,
        (TARGET_REWARD_AMOUNT,),
    )
    if incorrect_reward is None or int(incorrect_reward["total"]) != 0:
        raise ConfigurationError("应用后仍存在奖励积分不为 10.00 的题目。")

    overlaps = get_overlapping_activities(cursor, activity_id)
    if overlaps:
        raise ConfigurationError("应用后目标活动时间仍与其他答题活动重叠。")


def load_backup(backup_path: Path) -> dict[str, Any]:
    if not backup_path.is_file():
        raise ConfigurationError(f"回滚备份不存在：{backup_path}")
    payload = json.loads(backup_path.read_text(encoding="utf-8"))
    if payload.get("formatVersion") != 1:
        raise ConfigurationError("不支持的备份格式版本。")
    if payload.get("database") != DB_NAME:
        raise ConfigurationError(
            f"备份属于数据库 {payload.get('database')}，当前数据库为 {DB_NAME}。"
        )
    data = payload.get("data")
    if not isinstance(data, dict):
        raise ConfigurationError("备份缺少 data。")
    if not isinstance(data.get("topicActivity"), dict):
        raise ConfigurationError("备份缺少 topicActivity。")
    if not isinstance(data.get("topicLines"), list):
        raise ConfigurationError("备份缺少 topicLines。")
    if not isinstance(data.get("topics"), list):
        raise ConfigurationError("备份缺少 topics。")
    return payload


def ensure_rows_exist_for_rollback(cursor: Any, payload: Mapping[str, Any]) -> None:
    data = payload["data"]
    activity_id = str(data["topicActivity"]["id"])
    get_activity(cursor, activity_id, lock=True)

    current_lines = get_activity_lines(cursor, activity_id, lock=True)
    expected_line_ids = {str(row["id"]) for row in data["topicLines"]}
    current_line_ids = {str(row["id"]) for row in current_lines}
    missing_lines = sorted(expected_line_ids - current_line_ids)
    if missing_lines:
        raise ConfigurationError(
            "无法回滚，以下原线路已不存在：" + ", ".join(missing_lines)
        )

    current_topics = get_topics(cursor, lock=True)
    expected_topic_ids = {str(row["id"]) for row in data["topics"]}
    current_topic_ids = {str(row["id"]) for row in current_topics}
    missing_topics = sorted(expected_topic_ids - current_topic_ids)
    if missing_topics:
        preview = ", ".join(missing_topics[:10])
        suffix = f" 等 {len(missing_topics)} 道" if len(missing_topics) > 10 else ""
        raise ConfigurationError(f"无法回滚，以下原题目已不存在：{preview}{suffix}")


def restore_backup(cursor: Any, payload: Mapping[str, Any]) -> None:
    data = payload["data"]
    activity = data["topicActivity"]
    activity_id = str(activity["id"])

    cursor.execute(
        """
        UPDATE topic_activity
        SET start_time = %s,
            end_time = %s,
            limit_num = %s,
            reward_extra = %s,
            update_time = %s
        WHERE id = %s
        """,
        (
            activity.get("start_time"),
            activity.get("end_time"),
            activity.get("limit_num"),
            activity.get("reward_extra"),
            activity.get("update_time"),
            activity_id,
        ),
    )
    for line in data["topicLines"]:
        cursor.execute(
            """
            UPDATE topic_line
            SET status = %s,
                topic_num = %s,
                seq = %s,
                light_seq = %s
            WHERE id = %s
              AND topic_activity_id = %s
            """,
            (
                line.get("status"),
                line.get("topic_num"),
                line.get("seq"),
                line.get("light_seq"),
                line.get("id"),
                line.get("topic_activity_id"),
            ),
        )
    for topic in data["topics"]:
        cursor.execute(
            """
            UPDATE topic
            SET reward_amount = %s,
                update_time = %s
            WHERE id = %s
            """,
            (
                topic.get("reward_amount"),
                topic.get("update_time"),
                topic.get("id"),
            ),
        )

def redis_python_client():
    try:
        import redis
    except ImportError:
        return None
    return redis.Redis(
        host=REDIS_HOST,
        port=REDIS_PORT,
        db=REDIS_DB,
        password=REDIS_PASSWORD or None,
        socket_timeout=5,
        socket_connect_timeout=5,
        decode_responses=True,
    )


def redis_cli_command(*arguments: str) -> list[str] | None:
    redis_cli = shutil.which("redis-cli")
    if not redis_cli:
        return None
    command = [
        redis_cli,
        "-h",
        REDIS_HOST,
        "-p",
        str(REDIS_PORT),
        "-n",
        str(REDIS_DB),
        *arguments,
    ]
    return command


def run_redis_cli(*arguments: str) -> str | None:
    command = redis_cli_command(*arguments)
    if command is None:
        return None
    environment = os.environ.copy()
    if REDIS_PASSWORD:
        environment["REDISCLI_AUTH"] = REDIS_PASSWORD
    result = subprocess.run(
        command,
        env=environment,
        text=True,
        capture_output=True,
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "redis-cli 执行失败")
    return result.stdout.strip()


def check_cache_connection() -> str:
    client = redis_python_client()
    if client is not None:
        client.ping()
        return "redis-py"

    output = run_redis_cli("PING")
    if output is not None:
        if output.upper() != "PONG":
            raise RuntimeError(f"Redis PING 返回异常：{output}")
        return "redis-cli"

    raise RuntimeError("未找到 redis Python 包或 redis-cli，无法安全刷新缓存。")


def clear_topic_activity_cache(method: str) -> int:
    if method == "redis-py":
        client = redis_python_client()
        if client is None:
            raise RuntimeError("redis Python 客户端在执行期间不可用。")
        return int(client.delete(TOPIC_ACTIVITY_CACHE_KEY))

    output = run_redis_cli("DEL", TOPIC_ACTIVITY_CACHE_KEY)
    if output is None:
        raise RuntimeError("redis-cli 在执行期间不可用。")
    return int(output)


def dry_run(activity_id: str) -> int:
    database = connect_database()
    try:
        with database.cursor() as cursor:
            activity, lines, topics, topic_items, matched_lines = load_and_validate(
                cursor,
                activity_id,
                lock=False,
            )
            print_plan(
                activity,
                lines,
                topics,
                topic_items,
                matched_lines,
            )
        database.rollback()
    finally:
        database.close()

    print()
    print("检查通过。当前为 dry-run，数据库和 Redis 均未修改。")
    print("确认结果后，增加 --apply 执行修改。")
    return 0


def apply(activity_id: str, backup_dir: Path) -> int:
    cache_method = check_cache_connection()
    database = connect_database()
    backup_json: Path | None = None
    backup_sql: Path | None = None

    try:
        with database.cursor() as cursor:
            activity, lines, topics, topic_items, matched_lines = load_and_validate(
                cursor,
                activity_id,
                lock=True,
            )
            print_plan(
                activity,
                lines,
                topics,
                topic_items,
                matched_lines,
            )

            payload = create_backup_payload(activity, lines, topics)
            backup_json, backup_sql = write_backup_files(payload, backup_dir)
            print()
            print(f"备份 JSON：{backup_json}")
            print(f"回滚 SQL：{backup_sql}")

            apply_updates(cursor, activity_id, matched_lines)
            assert_applied_state(cursor, activity_id)
        database.commit()
    except Exception:
        database.rollback()
        raise
    finally:
        database.close()

    try:
        deleted = clear_topic_activity_cache(cache_method)
    except Exception as exc:
        raise RuntimeError(
            "数据库修改已经提交，但 Redis 缓存清理失败。"
            f"请立即删除键 {TOPIC_ACTIVITY_CACHE_KEY} 后再开放服务：{exc}"
        ) from exc

    print()
    print("应用成功：")
    print(f"- 活动时间：{TARGET_START_TIME} 至 {TARGET_END_TIME}")
    print("- 答题次数：不限（limit_num = NULL）")
    print("- 地图站点：5 个，每站随机 5 题")
    print("- 每题：10 积分，理论地图上限 250 积分")
    print(f"- Redis 缓存已清理：{TOPIC_ACTIVITY_CACHE_KEY}（删除结果 {deleted}）")
    print(f"- 如需回滚：python3 {Path(__file__).name} --rollback {backup_json}")
    return 0


def rollback(backup_path: Path) -> int:
    payload = load_backup(backup_path)
    cache_method = check_cache_connection()
    database = connect_database()
    try:
        with database.cursor() as cursor:
            ensure_rows_exist_for_rollback(cursor, payload)
            restore_backup(cursor, payload)
        database.commit()
    except Exception:
        database.rollback()
        raise
    finally:
        database.close()

    try:
        deleted = clear_topic_activity_cache(cache_method)
    except Exception as exc:
        raise RuntimeError(
            "数据库回滚已经提交，但 Redis 缓存清理失败。"
            f"请立即删除键 {TOPIC_ACTIVITY_CACHE_KEY}：{exc}"
        ) from exc

    print("回滚成功。")
    print(f"备份：{backup_path}")
    print(f"Redis 缓存已清理：{TOPIC_ACTIVITY_CACHE_KEY}（删除结果 {deleted}）")
    print("历史答题记录和钱包流水未修改。")
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "只通过 MySQL 数据配置曲靖站地图答题规则。"
            "默认执行 dry-run，不修改数据库。"
        )
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument(
        "--dry-run",
        action="store_true",
        help="只读检查（默认模式）",
    )
    mode.add_argument(
        "--apply",
        action="store_true",
        help="备份后在单个事务内应用配置",
    )
    mode.add_argument(
        "--rollback",
        type=Path,
        metavar="BACKUP_JSON",
        help="从脚本生成的 JSON 备份恢复配置",
    )
    parser.add_argument(
        "--activity-id",
        help="目标 topic_activity.id；dry-run/apply 必填",
    )
    parser.add_argument(
        "--backup-dir",
        type=Path,
        default=DEFAULT_BACKUP_DIR,
        help=f"备份目录，默认：{DEFAULT_BACKUP_DIR}",
    )
    args = parser.parse_args()

    if args.rollback is None and not args.activity_id:
        parser.error("dry-run/apply 必须提供 --activity-id")
    if args.rollback is not None and args.activity_id:
        parser.error("--rollback 不接受 --activity-id，活动 ID 已包含在备份中")
    return args


def main() -> int:
    args = parse_args()
    if args.rollback is not None:
        return rollback(args.rollback.expanduser().resolve())
    if args.apply:
        return apply(
            str(args.activity_id),
            args.backup_dir.expanduser().resolve(),
        )
    return dry_run(str(args.activity_id))


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("已取消。", file=sys.stderr)
        raise SystemExit(130)
    except Exception as exc:
        print(f"执行失败：{exc}", file=sys.stderr)
        raise SystemExit(1)
