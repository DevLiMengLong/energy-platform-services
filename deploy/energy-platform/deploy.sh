#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_REPO="$(cd "$SCRIPT_DIR/../.." && pwd)"
WEB_REPO="${WEB_REPO:-$SERVICE_REPO/../energy-platform-web}"
REMOTE_HOST="${REMOTE_HOST:-10.140.1.177}"
REMOTE_USER="${REMOTE_USER:-root}"
SSH_KEY="${SSH_KEY:-/Users/limenglong/.ssh/id_ed25519_menglong3_li}"
REMOTE_DIR="${REMOTE_DIR:-/data/energy-basic}"
STAMP="$(date +%Y%m%d%H%M%S)"
STAGE="/private/tmp/energy-platform-release-$STAMP"

mkdir -p "$STAGE/backend" "$STAGE/frontend/portal" "$STAGE/frontend/platform-admin" "$STAGE/frontend/basic-info" "$STAGE/nginx" "$STAGE/scripts"

cp "$SERVICE_REPO/platform-basic-service/target/platform-basic-service-0.1.0-SNAPSHOT.jar" "$STAGE/backend/platform-basic-service.jar"
cp "$SERVICE_REPO/log-service/target/log-service-0.1.0-SNAPSHOT.jar" "$STAGE/backend/log-service.jar"
cp -R "$WEB_REPO/apps/portal/dist/." "$STAGE/frontend/portal/"
cp -R "$WEB_REPO/apps/platform-admin/dist/." "$STAGE/frontend/platform-admin/"
cp -R "$WEB_REPO/apps/basic-info/dist/." "$STAGE/frontend/basic-info/"
cp "$SCRIPT_DIR/nginx/default.conf" "$STAGE/nginx/default.conf"
cp "$SCRIPT_DIR/scripts/"*.sh "$STAGE/scripts/"
chmod +x "$STAGE/scripts/"*.sh

REMOTE_STAGE="/tmp/energy-platform-release-$STAMP"
rsync -avP -e "ssh -i $SSH_KEY -o StrictHostKeyChecking=accept-new" "$STAGE/" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_STAGE/"

ssh -i "$SSH_KEY" -o StrictHostKeyChecking=accept-new "$REMOTE_USER@$REMOTE_HOST" "REMOTE_DIR='$REMOTE_DIR' REMOTE_STAGE='$REMOTE_STAGE' STAMP='$STAMP' bash -s" <<'REMOTE'
set -euo pipefail

BACKUP_DIR="$REMOTE_DIR/backups/$STAMP"
mkdir -p "$BACKUP_DIR"

backup_path() {
  local path="$1"
  if [ -e "$REMOTE_DIR/$path" ]; then
    mkdir -p "$BACKUP_DIR/$(dirname "$path")"
    cp -a "$REMOTE_DIR/$path" "$BACKUP_DIR/$path"
  fi
}

backup_path backend
backup_path frontend
backup_path nginx/default.conf
backup_path data
backup_path start.sh
backup_path stop.sh
backup_path smoke-test.sh
backup_path scripts

mkdir -p "$REMOTE_DIR/backend" "$REMOTE_DIR/frontend/portal" "$REMOTE_DIR/frontend/platform-admin" "$REMOTE_DIR/frontend/basic-info" "$REMOTE_DIR/nginx" "$REMOTE_DIR/scripts" "$REMOTE_DIR/logs" "$REMOTE_DIR/backups"

rm -rf "$REMOTE_DIR/backend/"* "$REMOTE_DIR/frontend/portal/"* "$REMOTE_DIR/frontend/platform-admin/"* "$REMOTE_DIR/frontend/basic-info/"*
cp -a "$REMOTE_STAGE/backend/." "$REMOTE_DIR/backend/"
cp -a "$REMOTE_STAGE/frontend/portal/." "$REMOTE_DIR/frontend/portal/"
cp -a "$REMOTE_STAGE/frontend/platform-admin/." "$REMOTE_DIR/frontend/platform-admin/"
cp -a "$REMOTE_STAGE/frontend/basic-info/." "$REMOTE_DIR/frontend/basic-info/"
cp -a "$REMOTE_STAGE/nginx/default.conf" "$REMOTE_DIR/nginx/default.conf"
cp -a "$REMOTE_STAGE/scripts/." "$REMOTE_DIR/scripts/"
chmod +x "$REMOTE_DIR/scripts/"*.sh

"$REMOTE_DIR/scripts/init-mysql.sh"
"$REMOTE_DIR/scripts/start.sh"
"$REMOTE_DIR/scripts/smoke-test.sh"

rm -rf "$REMOTE_STAGE"
echo "Remote deployment completed. Backup: $BACKUP_DIR"
REMOTE

rm -rf "$STAGE"
echo "Deployment completed: http://$REMOTE_HOST/"
