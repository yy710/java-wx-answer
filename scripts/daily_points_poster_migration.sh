#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MODE="${1:-inspect}"
: "${MYSQL_HOST:?set MYSQL_HOST to a non-production host}"
: "${MYSQL_PORT:=3306}"
: "${MYSQL_DATABASE:?set MYSQL_DATABASE}"
: "${MYSQL_USER:?set MYSQL_USER}"
MYSQL_PASSWORD_VALUE="${MYSQL_PWD:-${MYSQL_PASSWORD:-}}"
if [[ -z "$MYSQL_PASSWORD_VALUE" ]]; then
  echo "set MYSQL_PWD or MYSQL_PASSWORD in the environment, never in this script" >&2
  exit 2
fi
export MYSQL_PWD="$MYSQL_PASSWORD_VALUE"

if [[ "${CITY_WALK_MIGRATION_TARGET:-}" != "NON_PRODUCTION" ]]; then
  echo "Refusing to run: set CITY_WALK_MIGRATION_TARGET=NON_PRODUCTION explicitly." >&2
  exit 2
fi
if [[ "$MODE" != inspect && "$MODE" != apply && "$MODE" != rollback && "$MODE" != verify ]]; then
  echo "usage: $0 inspect|apply|rollback|verify" >&2
  exit 2
fi
if [[ "$MODE" == rollback && "${ALLOW_DAILY_POINTS_ROLLBACK:-}" != "YES" ]]; then
  echo "Refusing rollback: set ALLOW_DAILY_POINTS_ROLLBACK=YES after checking the backup target." >&2
  exit 2
fi

MYSQL_CMD=(mysql --protocol=tcp --host="$MYSQL_HOST" --port="$MYSQL_PORT" --user="$MYSQL_USER" --database="$MYSQL_DATABASE" --batch --raw)
if [[ "$MODE" == inspect ]]; then
  "${MYSQL_CMD[@]}" < "$SCRIPT_DIR/daily_points_poster_inspect.sql"
  exit 0
fi
if [[ "$MODE" == apply ]]; then
  BACKUP_DIR="${CITY_WALK_BACKUP_DIR:-$SCRIPT_DIR/backup-$(date +%Y%m%d%H%M%S)}"
  mkdir -p "$BACKUP_DIR"
  for table in user_wallet user_wallet_record topic video daily_task_config daily_task_quiz_session daily_task_quiz_question daily_task_video_session daily_task_claim daily_task_action_token poster_template poster_template_asset poster_generation reward_claims; do
    "${MYSQL_CMD[@]}" -e "SHOW CREATE TABLE \`$table\`;" > "$BACKUP_DIR/$table.create.sql" || true
    "${MYSQL_CMD[@]}" -e "SELECT * FROM \`$table\`;" > "$BACKUP_DIR/$table.tsv" || true
  done
  "${MYSQL_CMD[@]}" < "$SCRIPT_DIR/daily_points_poster_apply.sql"
  exit 0
fi
if [[ "$MODE" == rollback ]]; then
  BACKUP_DIR="${CITY_WALK_BACKUP_DIR:-$SCRIPT_DIR/rollback-backup-$(date +%Y%m%d%H%M%S)}"
  mkdir -p "$BACKUP_DIR"
  for table in user_wallet user_wallet_record topic video daily_task_config daily_task_quiz_session daily_task_quiz_question daily_task_video_session daily_task_claim daily_task_action_token poster_template poster_template_asset poster_generation reward_claims; do
    "${MYSQL_CMD[@]}" -e "SHOW CREATE TABLE \`$table\`;" > "$BACKUP_DIR/$table.create.sql" || true
    "${MYSQL_CMD[@]}" -e "SELECT * FROM \`$table\`;" > "$BACKUP_DIR/$table.tsv" || true
  done
  "${MYSQL_CMD[@]}" < "$SCRIPT_DIR/daily_points_poster_rollback.sql"
  exit 0
fi
"${MYSQL_CMD[@]}" < "$SCRIPT_DIR/daily_points_poster_verify.sql"
