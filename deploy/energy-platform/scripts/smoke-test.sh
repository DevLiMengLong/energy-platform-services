#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1}"
BASE_DIR="${BASE_DIR:-/data/energy-basic}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-aicicd-mysql}"
APP_ENV_FILE="${APP_ENV_FILE:-$BASE_DIR/.env}"

set -a
source "$APP_ENV_FILE"
set +a

wait_for_url() {
  local url="$1"
  local pattern="${2:-}"
  for _ in $(seq 1 60); do
    if [ -z "$pattern" ]; then
      curl -fsS "$url" >/dev/null 2>&1 && return 0
    else
      curl -fsS "$url" 2>/dev/null | grep -q "$pattern" && return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for $url" >&2
  return 1
}

wait_for_url "$BASE_URL/"
wait_for_url "$BASE_URL/api/basic/actuator/health" '"status":"UP"'
wait_for_url "$BASE_URL/api/logs/actuator/health" '"status":"UP"'

LOGIN_BODY="$(curl -fsS -H 'Content-Type: application/json' \
  -d '{"account":"admin","password":"admin123"}' \
  "$BASE_URL/api/basic/auth/login")"
echo "$LOGIN_BODY" | grep -q '"code":"SUCCESS"'
TOKEN="$(printf '%s' "$LOGIN_BODY" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
if [ -z "$TOKEN" ]; then
  echo "Failed to parse login token" >&2
  exit 1
fi

curl -fsS -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/basic/me/menus" | grep -q '"code":"SUCCESS"'

docker exec -i "$MYSQL_CONTAINER" mysql -u"$BASIC_DB_USERNAME" -p"$BASIC_DB_PASSWORD" "$BASIC_DB_NAME" \
  -e "SELECT COUNT(*) AS tenant_count FROM basic_tenant; SELECT COUNT(*) AS menu_count FROM basic_menu;" >/tmp/energy-basic-db-check.txt
docker exec -i "$MYSQL_CONTAINER" mysql -u"$LOG_DB_USERNAME" -p"$LOG_DB_PASSWORD" "$LOG_DB_NAME" \
  -e "SELECT COUNT(*) AS login_log_count FROM log_login;" >/tmp/energy-log-db-check.txt

cat /tmp/energy-basic-db-check.txt
cat /tmp/energy-log-db-check.txt
echo "Smoke test passed."
