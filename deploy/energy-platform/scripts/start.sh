#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${BASE_DIR:-/data/energy-basic}"
NETWORK="${NETWORK:-energy-platform-net}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-aicicd-mysql}"
JRE_IMAGE="${JRE_IMAGE:-eclipse-temurin:17-jre}"
NGINX_IMAGE="${NGINX_IMAGE:-nginx:1.27-alpine}"
APP_ENV_FILE="${APP_ENV_FILE:-$BASE_DIR/.env}"

if [ ! -f "$APP_ENV_FILE" ]; then
  echo "Missing env file: $APP_ENV_FILE. Run scripts/init-mysql.sh first." >&2
  exit 1
fi

set -a
source "$APP_ENV_FILE"
set +a

docker network inspect "$NETWORK" >/dev/null 2>&1 || docker network create "$NETWORK" >/dev/null
docker network connect "$NETWORK" "$MYSQL_CONTAINER" >/dev/null 2>&1 || true

"$BASE_DIR/scripts/stop.sh"

COMMON_DB_PARAMS="useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
BASIC_DB_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${BASIC_DB_NAME}?${COMMON_DB_PARAMS}"
LOG_DB_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${LOG_DB_NAME}?${COMMON_DB_PARAMS}"

mkdir -p "$BASE_DIR/logs"

docker run -d \
  --name energy-log-service \
  --network "$NETWORK" \
  -p 8091:8091 \
  -v "$BASE_DIR/backend:/app:ro" \
  -v "$BASE_DIR/logs:/logs" \
  -e LOG_DB_URL="$LOG_DB_URL" \
  -e LOG_DB_USERNAME="$LOG_DB_USERNAME" \
  -e LOG_DB_PASSWORD="$LOG_DB_PASSWORD" \
  "$JRE_IMAGE" \
  java -jar /app/log-service.jar

docker run -d \
  --name energy-platform-basic-service \
  --network "$NETWORK" \
  -p 8090:8090 \
  -v "$BASE_DIR/backend:/app:ro" \
  -v "$BASE_DIR/logs:/logs" \
  -e BASIC_DB_URL="$BASIC_DB_URL" \
  -e BASIC_DB_USERNAME="$BASIC_DB_USERNAME" \
  -e BASIC_DB_PASSWORD="$BASIC_DB_PASSWORD" \
  -e LOG_SERVICE_URL="http://energy-log-service:8091" \
  "$JRE_IMAGE" \
  java -jar /app/platform-basic-service.jar

docker run -d \
  --name energy-basic-nginx \
  --network "$NETWORK" \
  -p 80:80 \
  -v "$BASE_DIR/frontend/portal:/usr/share/nginx/html/portal:ro" \
  -v "$BASE_DIR/frontend/platform-admin:/usr/share/nginx/html/platform-admin:ro" \
  -v "$BASE_DIR/frontend/basic-info:/usr/share/nginx/html/basic-info:ro" \
  -v "$BASE_DIR/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro" \
  "$NGINX_IMAGE"

echo "Energy platform started."
