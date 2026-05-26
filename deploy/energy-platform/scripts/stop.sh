#!/usr/bin/env bash
set -euo pipefail

docker rm -f energy-basic-nginx >/dev/null 2>&1 || true
docker rm -f energy-platform-basic-service >/dev/null 2>&1 || true
docker rm -f energy-log-service >/dev/null 2>&1 || true

for pattern in 'energy-basic-service-.*\.jar' 'platform-basic-service\.jar' 'log-service\.jar'; do
  OLD_PIDS="$(pgrep -f "$pattern" || true)"
  if [ -n "$OLD_PIDS" ]; then
    echo "$OLD_PIDS" | xargs kill || true
  fi
done

for port in 8090 8091; do
  PORT_PIDS="$(ss -ltnp 2>/dev/null | sed -n "s/.*:${port} .*pid=\([0-9][0-9]*\).*/\1/p" | sort -u || true)"
  if [ -n "$PORT_PIDS" ]; then
    echo "$PORT_PIDS" | xargs kill || true
  fi
done

echo "Energy platform containers stopped."
