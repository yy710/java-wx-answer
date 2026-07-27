from __future__ import annotations

import datetime as dt
import decimal
import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT_PATH = Path(__file__).with_name("configure_qujing_map_rules.py")
SPEC = importlib.util.spec_from_file_location("configure_qujing_map_rules", SCRIPT_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"无法加载脚本：{SCRIPT_PATH}")
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


def line(line_id: int, title: str, descr: str = "") -> dict:
    return {
        "id": str(line_id),
        "topic_activity_id": "100",
        "title": title,
        "descr": descr,
        "status": 1,
        "topic_num": 3,
        "seq": 9,
        "light_seq": 9,
    }


class StationMatchingTest(unittest.TestCase):
    def test_matches_all_five_stations_in_h5_order(self):
        lines = [
            line(5, "罗平油菜花海"),
            line(3, "陆良爨龙颜碑"),
            line(1, "沾益珠江源"),
            line(4, "陆良高原蔬菜"),
            line(2, "曲靖南城门"),
        ]

        matched = MODULE.match_station_lines(lines)

        self.assertEqual(
            [str(item["id"]) for _, item in matched],
            ["1", "2", "3", "4", "5"],
        )
        self.assertEqual([rule.order for rule, _ in matched], [1, 2, 3, 4, 5])

    def test_uses_description_only_when_title_has_no_match(self):
        lines = [
            line(1, "站点一", "珠江正源"),
            line(2, "站点二", "麒麟南城门"),
            line(3, "站点三", "爨文化"),
            line(4, "站点四", "南菜北运高原蔬菜"),
            line(5, "站点五", "罗平油菜花"),
        ]

        matched = MODULE.match_station_lines(lines)

        self.assertEqual(len(matched), 5)

    def test_rejects_ambiguous_station_match(self):
        lines = [
            line(1, "沾益珠江源"),
            line(6, "珠江正源"),
            line(2, "曲靖南城门"),
            line(3, "陆良爨龙颜碑"),
            line(4, "陆良高原蔬菜"),
            line(5, "罗平油菜花海"),
        ]

        with self.assertRaises(MODULE.ConfigurationError):
            MODULE.match_station_lines(lines)


class QuestionValidationTest(unittest.TestCase):
    def test_accepts_two_or_more_options_with_one_correct_answer(self):
        topics = [{"id": "1", "title": "题目一"}, {"id": "2", "title": "题目二"}]
        items = [
            {"id": "11", "topic_id": "1", "answer_flag": 1},
            {"id": "12", "topic_id": "1", "answer_flag": 0},
            {"id": "21", "topic_id": "2", "answer_flag": 0},
            {"id": "22", "topic_id": "2", "answer_flag": 1},
            {"id": "23", "topic_id": "2", "answer_flag": 0},
        ]

        MODULE.validate_question_items(topics, items)

    def test_rejects_missing_or_multiple_correct_answers(self):
        topics = [{"id": "1", "title": "题目一"}]
        items = [
            {"id": "11", "topic_id": "1", "answer_flag": 1},
            {"id": "12", "topic_id": "1", "answer_flag": 1},
        ]

        with self.assertRaises(MODULE.ConfigurationError):
            MODULE.validate_question_items(topics, items)


class BackupTest(unittest.TestCase):
    def test_generates_executable_rollback_sql_and_json(self):
        activity = {
            "id": "100",
            "title": "普法活动",
            "start_time": dt.datetime(2026, 7, 11, 0, 0),
            "end_time": dt.datetime(2026, 8, 11, 0, 0),
            "limit_num": 50,
            "reward_extra": decimal.Decimal("3.00"),
            "update_time": None,
        }
        lines = [line(1, "沾益珠江源")]
        topics = [
            {
                "id": "200",
                "title": "题目'一",
                "reward_amount": decimal.Decimal("1.00"),
                "update_time": dt.datetime(2026, 7, 27, 12, 0),
            }
        ]
        payload = MODULE.create_backup_payload(activity, lines, topics)

        rollback_sql = MODULE.build_rollback_sql(payload)

        self.assertIn("START TRANSACTION;", rollback_sql)
        self.assertIn("limit_num = 50", rollback_sql)
        self.assertIn("reward_amount = '1.00'", rollback_sql)
        self.assertIn("DEL topic:activity", rollback_sql)

        with tempfile.TemporaryDirectory() as temporary_dir:
            json_path, sql_path = MODULE.write_backup_files(
                payload,
                Path(temporary_dir),
            )
            loaded = json.loads(json_path.read_text(encoding="utf-8"))
            self.assertEqual(loaded["activityId"], "100")
            self.assertTrue(sql_path.read_text(encoding="utf-8").endswith("\n"))


if __name__ == "__main__":
    unittest.main()
