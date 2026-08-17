-- 仅在发布负责人确认回滚窗口、并完成备份后执行。不会删除历史 user_wallet_record。
DROP TABLE IF EXISTS poster_template_asset;
DROP TABLE IF EXISTS poster_generation;
DROP TABLE IF EXISTS poster_template;
DROP TABLE IF EXISTS daily_task_action_token;
DROP TABLE IF EXISTS daily_task_claim;
DROP TABLE IF EXISTS daily_task_video_session;
DROP TABLE IF EXISTS daily_task_quiz_question;
DROP TABLE IF EXISTS daily_task_quiz_session;
DROP TABLE IF EXISTS reward_claims;
DROP TABLE IF EXISTS daily_task_config;
SET @sql_topic_category = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'topic' AND column_name = 'finance_category') = 1,
  'ALTER TABLE topic DROP COLUMN finance_category', 'SELECT 1');
PREPARE stmt_topic_category FROM @sql_topic_category; EXECUTE stmt_topic_category; DEALLOCATE PREPARE stmt_topic_category;
SET @sql_topic_daily = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'topic' AND column_name = 'daily_task_enabled') = 1,
  'ALTER TABLE topic DROP COLUMN daily_task_enabled', 'SELECT 1');
PREPARE stmt_topic_daily FROM @sql_topic_daily; EXECUTE stmt_topic_daily; DEALLOCATE PREPARE stmt_topic_daily;
SET @sql_video_duration = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'video' AND column_name = 'duration_seconds') = 1,
  'ALTER TABLE video DROP COLUMN duration_seconds', 'SELECT 1');
PREPARE stmt_video_duration FROM @sql_video_duration; EXECUTE stmt_video_duration; DEALLOCATE PREPARE stmt_video_duration;
SET @sql_video_daily = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'video' AND column_name = 'daily_task_enabled') = 1,
  'ALTER TABLE video DROP COLUMN daily_task_enabled', 'SELECT 1');
PREPARE stmt_video_daily FROM @sql_video_daily; EXECUTE stmt_video_daily; DEALLOCATE PREPARE stmt_video_daily;
