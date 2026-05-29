#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8088}"
PROTOCOL_VERSION="${PROTOCOL_VERSION:-v2}"
TENANT_MARK="${TENANT_MARK:-tenant_demo}"
MODEL_MARK="${MODEL_MARK:-energy_meter}"
DEVICE_MARK="${DEVICE_MARK:-meter_001}"
MESSAGE_ID="${MESSAGE_ID:-reliability-$(date +%s)}"
REPEAT="${REPEAT:-3}"

endpoint="${BASE_URL}/ingest/${PROTOCOL_VERSION}/${TENANT_MARK}/${MODEL_MARK}/${DEVICE_MARK}/telemetry"

curl -fsS "${BASE_URL}/actuator/health" >/dev/null

for i in $(seq 1 "${REPEAT}"); do
  payload=$(printf '{"messageId":"%s","timestamp":"2026-05-22T04:00:00Z","data":{"electric_total":123.45,"water_total":67.89,"gas_total":10.11}}' "${MESSAGE_ID}")
  curl -fsS \
    -H 'Content-Type: application/json' \
    -X POST \
    -d "${payload}" \
    "${endpoint}" >/dev/null
done

echo "OK: sent ${REPEAT} duplicate telemetry messages with messageId=${MESSAGE_ID}"
