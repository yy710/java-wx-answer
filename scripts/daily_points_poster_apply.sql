-- J-01 apply。先执行 daily_points_poster_inspect.sql，并由发布人确认目标不是生产库。
CREATE TABLE IF NOT EXISTS daily_task_config (
  id TINYINT UNSIGNED NOT NULL PRIMARY KEY,
  enabled TINYINT(1) NOT NULL DEFAULT 0,
  quiz_enabled TINYINT(1) NOT NULL DEFAULT 1,
  video_enabled TINYINT(1) NOT NULL DEFAULT 1,
  affair_enabled TINYINT(1) NOT NULL DEFAULT 1,
  share_enabled TINYINT(1) NOT NULL DEFAULT 1,
  wallet_max_points DECIMAL(10,2) NOT NULL DEFAULT 4000.00,
  daily_positive_max_points DECIMAL(10,2) NOT NULL DEFAULT 60.00,
  quiz_daily_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  quiz_question_count SMALLINT UNSIGNED NOT NULL DEFAULT 2,
  quiz_reward_per_correct DECIMAL(10,2) NOT NULL DEFAULT 10.00,
  quiz_time_limit_seconds INT UNSIGNED NOT NULL DEFAULT 120,
  video_daily_count SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  video_reward_points DECIMAL(10,2) NOT NULL DEFAULT 20.00,
  video_min_watch_ratio DECIMAL(5,4) NOT NULL DEFAULT 0.5000,
  affair_daily_count SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  affair_reward_points DECIMAL(10,2) NOT NULL DEFAULT 10.00,
  share_daily_count SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  share_reward_points DECIMAL(10,2) NOT NULL DEFAULT 10.00,
  poster_cache_days SMALLINT UNSIGNED NOT NULL DEFAULT 7,
  version BIGINT UNSIGNED NOT NULL DEFAULT 1,
  updated_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT ck_daily_task_config_singleton CHECK (id = 1),
  CONSTRAINT ck_daily_task_config_ratio CHECK (video_min_watch_ratio >= 0 AND video_min_watch_ratio <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO daily_task_config (id) VALUES (1) ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS daily_task_quiz_session (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  task_date DATE NOT NULL,
  status VARCHAR(16) NOT NULL,
  question_count SMALLINT UNSIGNED NOT NULL,
  reward_per_correct DECIMAL(10,2) NOT NULL,
  time_limit_seconds INT UNSIGNED NOT NULL,
  started_at DATETIME NOT NULL,
  deadline_at DATETIME NOT NULL,
  submitted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_quiz_user_date (user_id, task_date),
  KEY idx_quiz_status_deadline (status, deadline_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_task_quiz_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT UNSIGNED NOT NULL,
  sequence_no SMALLINT UNSIGNED NOT NULL,
  topic_id BIGINT NOT NULL,
  question_snapshot JSON NOT NULL,
  correct_option_snapshot VARCHAR(32) NOT NULL,
  user_option VARCHAR(32) NULL,
  is_correct TINYINT(1) NULL,
  requested_points DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  awarded_points DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  UNIQUE KEY uk_quiz_question_order (session_id, sequence_no),
  KEY idx_quiz_question_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_task_video_session (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  task_date DATE NOT NULL,
  video_id BIGINT NOT NULL,
  video_duration_seconds INT UNSIGNED NOT NULL,
  min_watch_ratio DECIMAL(5,4) NOT NULL,
  required_watch_seconds INT UNSIGNED NOT NULL,
  credited_watch_seconds INT UNSIGNED NOT NULL DEFAULT 0,
  last_position_seconds DECIMAL(12,3) NOT NULL DEFAULT 0,
  last_heartbeat_at DATETIME NULL,
  status VARCHAR(16) NOT NULL,
  version BIGINT UNSIGNED NOT NULL DEFAULT 1,
  started_at DATETIME NOT NULL,
  claimed_at DATETIME NULL,
  UNIQUE KEY uk_video_user_date (user_id, task_date),
  KEY idx_video_last_heartbeat (last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_task_claim (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  task_date DATE NOT NULL,
  task_type VARCHAR(32) NOT NULL,
  source VARCHAR(64) NOT NULL,
  requested_points DECIMAL(10,2) NOT NULL,
  awarded_points DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  award_reason VARCHAR(32) NOT NULL,
  wallet_record_id BIGINT NULL,
  completed_at DATETIME NOT NULL,
  UNIQUE KEY uk_daily_task_claim (user_id, task_date, task_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_task_action_token (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  token_hash CHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  action_type VARCHAR(16) NOT NULL,
  asset_type VARCHAR(16) NOT NULL,
  asset_id VARCHAR(128) NOT NULL,
  expires_at DATETIME NOT NULL,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_action_token_hash (token_hash),
  KEY idx_action_token_user_expiry (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poster_template (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL,
  template_version INT UNSIGNED NOT NULL,
  canvas_width INT UNSIGNED NOT NULL,
  canvas_height INT UNSIGNED NOT NULL,
  restricted_json JSON NOT NULL,
  preview_url VARCHAR(512) NULL,
  published_at DATETIME NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_poster_template_version (id, template_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poster_template_asset (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  template_id BIGINT UNSIGNED NOT NULL,
  asset_key VARCHAR(128) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  mime_type VARCHAR(64) NOT NULL,
  width INT UNSIGNED NOT NULL,
  height INT UNSIGNED NOT NULL,
  sha256 CHAR(64) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_poster_asset_key (template_id, asset_key),
  UNIQUE KEY uk_poster_asset_hash (sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poster_generation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  template_id BIGINT UNSIGNED NOT NULL,
  template_version INT UNSIGNED NOT NULL,
  input_sha256 CHAR(64) NOT NULL,
  result_url VARCHAR(512) NULL,
  status VARCHAR(16) NOT NULL,
  expires_at DATETIME NOT NULL,
  error_code VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_poster_generation_cache (user_id, template_id, template_version, input_sha256),
  KEY idx_poster_generation_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reward_claims (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id VARCHAR(128) NOT NULL,
  event_type INT NOT NULL,
  task_date DATE NOT NULL,
  requested_points DECIMAL(10,2) NOT NULL,
  awarded_points DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  claimed_at DATETIME NULL,
  UNIQUE KEY uk_reward_claim_event (user_id, event_id),
  KEY idx_reward_claim_positive_day (user_id, task_date, awarded_points)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新表和索引创建完成后，再扩展既有题库和视频表。
SET @sql_topic_daily = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'topic' AND column_name = 'daily_task_enabled') = 0,
  'ALTER TABLE topic ADD COLUMN daily_task_enabled TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt_topic_daily FROM @sql_topic_daily; EXECUTE stmt_topic_daily; DEALLOCATE PREPARE stmt_topic_daily;
SET @sql_topic_category = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'topic' AND column_name = 'finance_category') = 0,
  'ALTER TABLE topic ADD COLUMN finance_category VARCHAR(64) NULL', 'SELECT 1');
PREPARE stmt_topic_category FROM @sql_topic_category; EXECUTE stmt_topic_category; DEALLOCATE PREPARE stmt_topic_category;
SET @sql_video_daily = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'video' AND column_name = 'daily_task_enabled') = 0,
  'ALTER TABLE video ADD COLUMN daily_task_enabled TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt_video_daily FROM @sql_video_daily; EXECUTE stmt_video_daily; DEALLOCATE PREPARE stmt_video_daily;
SET @sql_video_duration = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'video' AND column_name = 'duration_seconds') = 0,
  'ALTER TABLE video ADD COLUMN duration_seconds INT UNSIGNED NULL', 'SELECT 1');
PREPARE stmt_video_duration FROM @sql_video_duration; EXECUTE stmt_video_duration; DEALLOCATE PREPARE stmt_video_duration;
