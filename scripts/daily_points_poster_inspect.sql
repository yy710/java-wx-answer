-- 只读检查；J-01 apply 之前必须在非生产数据库执行。
SELECT DATABASE() AS database_name, @@hostname AS db_host, VERSION() AS mysql_version;
SELECT table_name, table_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'daily_task_config','daily_task_quiz_session','daily_task_quiz_question',
    'daily_task_video_session','daily_task_claim','daily_task_action_token',
    'poster_template','poster_template_asset','poster_generation','reward_claims'
  )
ORDER BY table_name;
SELECT column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('topic','video')
  AND column_name IN ('daily_task_enabled','finance_category','duration_seconds')
ORDER BY table_name, ordinal_position;
SELECT wallet_id, event_type, event_id, COUNT(*) AS duplicate_count
FROM user_wallet_record
WHERE status = 1 AND change_amount > 0
GROUP BY wallet_id, event_type, event_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
