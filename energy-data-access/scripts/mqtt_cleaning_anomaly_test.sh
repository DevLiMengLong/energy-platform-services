#!/usr/bin/env bash
set -euo pipefail
export LC_ALL=C
export LANG=C

MQTT_CONTAINER="${MQTT_CONTAINER:-energy-data-access-mqtt}"
MQTT_HOST="${MQTT_HOST:-127.0.0.1}"
MQTT_PORT="${MQTT_PORT:-1884}"
MOSQUITTO_IMAGE="${MOSQUITTO_IMAGE:-docker.1ms.run/eclipse-mosquitto:2.0.18}"
RUN_ID="${RUN_ID:-clean-anomaly-$(date +%s)}"
TENANT_MARK="${TENANT_MARK:-tenant_clean}"
MODEL_MARK="${MODEL_MARK:-energy_meter_clean}"
DEFAULT_DEVICE_MARK="device_clean_${RUN_ID//[^A-Za-z0-9_]/_}"
DEVICE_MARK="${DEVICE_MARK:-${DEFAULT_DEVICE_MARK}}"

publish() {
  local message_id="$1"
  local param_mark="$2"
  local value="$3"
  local timestamp="$4"
  local topic="v2/${TENANT_MARK}/${MODEL_MARK}/${DEVICE_MARK}/telemetry"
  local payload
  payload=$(printf '{"protocolVersion":"v2","tenantMark":"%s","modelMark":"%s","deviceMark":"%s","messageId":"%s","timestamp":"%s","data":{"%s":"%s"}}' \
    "${TENANT_MARK}" "${MODEL_MARK}" "${DEVICE_MARK}" "${message_id}" "${timestamp}" "${param_mark}" "${value}")
  if docker ps --format "{{.Names}}" | grep -qx "${MQTT_CONTAINER}"; then
    docker exec "${MQTT_CONTAINER}" mosquitto_pub -h 127.0.0.1 -p 1883 -q 1 -t "${topic}" -m "${payload}" >/dev/null
  else
    docker run --rm --network host "${MOSQUITTO_IMAGE}" mosquitto_pub -h "${MQTT_HOST}" -p "${MQTT_PORT}" -q 1 -t "${topic}" -m "${payload}" >/dev/null
  fi
}

publish "${RUN_ID}-normal-1" "normal_param" "10" "2026-05-22T04:00:00Z"
publish "${RUN_ID}-normal-2" "normal_param" "10" "2026-05-22T04:00:00Z"
publish "${RUN_ID}-format" "format_param" "not-a-number" "2026-05-22T04:00:10Z"
publish "${RUN_ID}-range" "range_param" "200" "2026-05-22T04:00:20Z"
publish "${RUN_ID}-spike-1" "spike_param" "10" "2026-05-22T04:00:30Z"
publish "${RUN_ID}-spike-2" "spike_param" "1000" "2026-05-22T04:00:40Z"
publish "${RUN_ID}-rollback-1" "rollback_param" "1000" "2026-05-22T04:00:50Z"
publish "${RUN_ID}-rollback-2" "rollback_param" "10" "2026-05-22T04:01:00Z"
publish "${RUN_ID}-rollover-1" "rollover_param" "999980" "2026-05-22T04:01:10Z"
publish "${RUN_ID}-rollover-2" "rollover_param" "25" "2026-05-22T04:01:20Z"
publish "${RUN_ID}-formula" "formula_param" "8000" "2026-05-22T04:01:30Z"

echo "RUN_ID=${RUN_ID}"
echo "EXPECTED_RAW_MESSAGES=11"
echo "EXPECTED_CLEAN_ROWS=11"
echo "EXPECTED_EFFECTIVE_ROWS=6"
echo "EXPECTED_QUALITY_CODES=0:5,1:1,4:1,6:1,7:1,8:1,10:1"
