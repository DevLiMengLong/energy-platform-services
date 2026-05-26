#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${BASE_DIR:-/data/energy-basic}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-aicicd-mysql}"
MYSQL_SECRET_FILE="${MYSQL_SECRET_FILE:-/opt/aicicd/secrets/mysql.env}"
APP_ENV_FILE="${APP_ENV_FILE:-$BASE_DIR/.env}"

mkdir -p "$BASE_DIR"

if [ ! -f "$APP_ENV_FILE" ]; then
  BASIC_DB_PASSWORD="$(tr -dc 'A-Za-z0-9' </dev/urandom | head -c 24 || true)"
  LOG_DB_PASSWORD="$(tr -dc 'A-Za-z0-9' </dev/urandom | head -c 24 || true)"
  cat > "$APP_ENV_FILE" <<ENV
BASIC_DB_NAME=energy_platform_basic
BASIC_DB_USERNAME=energy_basic_user
BASIC_DB_PASSWORD=${BASIC_DB_PASSWORD:-energyBasicPass2026}
LOG_DB_NAME=energy_log
LOG_DB_USERNAME=energy_log_user
LOG_DB_PASSWORD=${LOG_DB_PASSWORD:-energyLogPass2026}
MYSQL_HOST=${MYSQL_CONTAINER}
MYSQL_PORT=3306
ENV
  chmod 600 "$APP_ENV_FILE"
fi

set -a
source "$APP_ENV_FILE"
if [ -f "$MYSQL_SECRET_FILE" ]; then
  source "$MYSQL_SECRET_FILE"
fi
set +a

ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-${MYSQL_PASSWORD:-${MYSQL_PWD:-}}}"
if [ -z "$ROOT_PASSWORD" ]; then
  echo "Cannot resolve MySQL root password from $MYSQL_SECRET_FILE" >&2
  exit 1
fi

docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p"$ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS \`${BASIC_DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS \`${LOG_DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '${BASIC_DB_USERNAME}'@'%' IDENTIFIED BY '${BASIC_DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${LOG_DB_USERNAME}'@'%' IDENTIFIED BY '${LOG_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON \`${BASIC_DB_NAME}\`.* TO '${BASIC_DB_USERNAME}'@'%';
GRANT ALL PRIVILEGES ON \`${LOG_DB_NAME}\`.* TO '${LOG_DB_USERNAME}'@'%';
FLUSH PRIVILEGES;
SQL

echo "MySQL databases and users are ready."
