SELECT id, enabled, wallet_max_points, daily_positive_max_points, quiz_question_count,
       quiz_reward_per_correct, quiz_time_limit_seconds, video_reward_points,
       video_min_watch_ratio, affair_reward_points, share_reward_points, version
FROM daily_task_config WHERE id = 1;
SELECT table_name, table_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('daily_task_config','daily_task_quiz_session','daily_task_quiz_question',
    'daily_task_video_session','daily_task_claim','daily_task_action_token','poster_template',
    'poster_template_asset','poster_generation','reward_claims')
ORDER BY table_name;
SELECT COUNT(*) AS duplicate_reward_claims
FROM (SELECT user_id, event_id FROM reward_claims GROUP BY user_id, event_id HAVING COUNT(*) > 1) d;
SELECT COUNT(*) AS enabled_quiz_topics
FROM topic WHERE daily_task_enabled = 1;
SELECT COUNT(*) AS enabled_videos_with_duration
FROM video WHERE daily_task_enabled = 1 AND status = 1 AND duration_seconds > 0;
