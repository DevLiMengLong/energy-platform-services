#!/usr/bin/env bash
set -euo pipefail
export LC_ALL=C
export LANG=C

MQTT_HOST="${MQTT_HOST:-127.0.0.1}"
MQTT_PORT="${MQTT_PORT:-1884}"
MQTT_CONTAINER="${MQTT_CONTAINER:-energy-data-access-mqtt}"
MOSQUITTO_IMAGE="${MOSQUITTO_IMAGE:-docker.1ms.run/eclipse-mosquitto:2.0.18}"
CLICKHOUSE_URL="${CLICKHOUSE_URL:-http://127.0.0.1:8123/}"

TENANTS="${TENANTS:-3}"
MODELS="${MODELS:-5}"
DEVICES="${DEVICES:-2}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-10}"
DURATION_SECONDS="${DURATION_SECONDS:-600}"
CONFIG_CHANGE_AFTER_SECONDS="${CONFIG_CHANGE_AFTER_SECONDS:-180}"
CONCURRENCY="${CONCURRENCY:-30}"
RUN_ID="${RUN_ID:-mqtt-clean-load-$(date +%s)}"
BASE_TS="${BASE_TS:-2026-05-22T04:00:00Z}"
SEED_ONLY="${SEED_ONLY:-0}"
SKIP_INITIAL_CONFIG="${SKIP_INITIAL_CONFIG:-0}"

cycles="$((DURATION_SECONDS / INTERVAL_SECONDS))"
messages_per_cycle="$((TENANTS * MODELS * DEVICES))"
points_per_message=7
expected_messages="$((messages_per_cycle * cycles))"
expected_points="$((expected_messages * points_per_message))"
change_cycle="$((CONFIG_CHANGE_AFTER_SECONDS / INTERVAL_SECONDS + 1))"

clickhouse() {
  curl -sSf --data-binary @- "${CLICKHOUSE_URL}" >/dev/null
}

ensure_config_table() {
  clickhouse <<'SQL'
CREATE TABLE IF NOT EXISTS point_clean_config (
  tenant_mark String,
  model_mark String,
  param_mark String,
  transform_formula String,
  min_value Nullable(String),
  max_value Nullable(String),
  max_delta Nullable(String),
  is_cumulative UInt8,
  rollover_enabled UInt8,
  rollover_max_value Nullable(String),
  rollover_min_previous_value Nullable(String),
  rollover_max_current_value Nullable(String),
  enabled UInt8,
  version UInt64,
  updated_time DateTime
) ENGINE = ReplacingMergeTree(version) ORDER BY (tenant_mark, model_mark, param_mark)
SQL
}

seed_config_version() {
  local version="$1"
  local range_max="$2"
  local formula="$3"
  local rows_file
  rows_file="$(mktemp)"
  for tenant_index in $(seq 1 "${TENANTS}"); do
    for model_index in $(seq 1 "${MODELS}"); do
      tenant_mark="load_tenant_${tenant_index}"
      model_mark="load_meter_${model_index}"
      cat >> "${rows_file}" <<SQL
('${tenant_mark}', '${model_mark}', 'normal_param', 'x', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'format_param', 'x', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'range_param', 'x', '0', '${range_max}', NULL, 0, 0, NULL, NULL, NULL, 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'spike_param', 'x', NULL, NULL, '50', 0, 0, NULL, NULL, NULL, 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'rollback_param', 'x', NULL, NULL, NULL, 1, 0, NULL, NULL, NULL, 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'rollover_param', 'x', NULL, NULL, '50', 1, 1, '999999', '990000', '1000', 1, ${version}, now()),
('${tenant_mark}', '${model_mark}', 'formula_param', '${formula}', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${version}, now()),
SQL
    done
  done
  sed -i.bak '$ s/,$/;/' "${rows_file}" 2>/dev/null || sed -i '$ s/,$/;/' "${rows_file}"
  {
    cat <<'SQL'
INSERT INTO point_clean_config
(tenant_mark, model_mark, param_mark, transform_formula, min_value, max_value, max_delta,
 is_cumulative, rollover_enabled, rollover_max_value, rollover_min_previous_value,
 rollover_max_current_value, enabled, version, updated_time)
VALUES
SQL
    cat "${rows_file}"
  } | clickhouse
  rm -f "${rows_file}" "${rows_file}.bak"
}

json_time_for_cycle() {
  local cycle="$1"
  date -u -d "${BASE_TS} + $((cycle * INTERVAL_SECONDS)) seconds" +"%Y-%m-%dT%H:%M:%SZ"
}

value_for_param() {
  local param="$1"
  local cycle="$2"
  case "${param}" in
    normal_param) echo "$((1000 + cycle))" ;;
    format_param)
      if [ "$((cycle % 9))" -eq 0 ]; then echo "not-a-number"; else echo "$((200 + cycle))"; fi
      ;;
    range_param) echo "80" ;;
    spike_param)
      if [ "${cycle}" -eq 2 ]; then echo "1000"; else echo "$((10 + cycle))"; fi
      ;;
    rollback_param)
      if [ "${cycle}" -eq 1 ]; then echo "1000"; else echo "10"; fi
      ;;
    rollover_param)
      if [ "${cycle}" -eq 1 ]; then echo "999980"; elif [ "${cycle}" -eq 2 ]; then echo "25"; else echo "$((25 + cycle))"; fi
      ;;
    formula_param) echo "8000" ;;
  esac
}

publish_cycle() {
  local cycle="$1"
  local work_dir
  local timestamp
  work_dir="$(mktemp -d)"
  timestamp="$(json_time_for_cycle "${cycle}")"

  for tenant_index in $(seq 1 "${TENANTS}"); do
    for model_index in $(seq 1 "${MODELS}"); do
      for device_index in $(seq 1 "${DEVICES}"); do
        tenant_mark="load_tenant_${tenant_index}"
        model_mark="load_meter_${model_index}"
        device_mark="device_${device_index}_${RUN_ID//[^A-Za-z0-9_]/_}"
        message_id="${RUN_ID}-${tenant_mark}-${model_mark}-${device_mark}-${cycle}"
        topic="v2/${tenant_mark}/${model_mark}/${device_mark}/telemetry"
        normal_value="$(value_for_param normal_param "${cycle}")"
        format_value="$(value_for_param format_param "${cycle}")"
        range_value="$(value_for_param range_param "${cycle}")"
        spike_value="$(value_for_param spike_param "${cycle}")"
        rollback_value="$(value_for_param rollback_param "${cycle}")"
        rollover_value="$(value_for_param rollover_param "${cycle}")"
        formula_value="$(value_for_param formula_param "${cycle}")"
        payload=$(printf '{"protocolVersion":"v2","tenantMark":"%s","modelMark":"%s","deviceMark":"%s","messageId":"%s","timestamp":"%s","data":{"normal_param":"%s","format_param":"%s","range_param":"%s","spike_param":"%s","rollback_param":"%s","rollover_param":"%s","formula_param":"%s"}}' \
          "${tenant_mark}" "${model_mark}" "${device_mark}" "${message_id}" "${timestamp}" \
          "${normal_value}" "${format_value}" "${range_value}" "${spike_value}" "${rollback_value}" "${rollover_value}" "${formula_value}")
        printf '%s\0%s\0' "${topic}" "${payload}" >> "${work_dir}/messages.bin"
      done
    done
  done

  export MQTT_CONTAINER MQTT_HOST MQTT_PORT MOSQUITTO_IMAGE
  xargs -0 -P "${CONCURRENCY}" -n 2 sh -c '
    topic="$1"
    payload="$2"
    if docker ps --format "{{.Names}}" | grep -qx "$MQTT_CONTAINER"; then
      docker exec "$MQTT_CONTAINER" mosquitto_pub -h 127.0.0.1 -p 1883 -q 1 -t "$topic" -m "$payload" >/dev/null 2>/dev/null
    else
      docker run --rm --network host "$MOSQUITTO_IMAGE" mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -q 1 -t "$topic" -m "$payload" >/dev/null 2>/dev/null
    fi
  ' _ < "${work_dir}/messages.bin"
  rm -rf "${work_dir}"
}

ensure_config_table
if [ "${SKIP_INITIAL_CONFIG}" -ne 1 ]; then
  seed_config_version "$(date +%s)" "100" "x / 1000"
fi
if [ "${SEED_ONLY}" -eq 1 ]; then
  echo "SEEDED_ONLY tenants=${TENANTS} models=${MODELS} range_param_max=100 formula_param='x / 1000'"
  exit 0
fi

start_ms="$(date +%s%3N)"
changed=0
echo "run_id=${RUN_ID} tenants=${TENANTS} models=${MODELS} devices=${DEVICES} interval_seconds=${INTERVAL_SECONDS} duration_seconds=${DURATION_SECONDS} cycles=${cycles} expected_messages=${expected_messages} expected_points=${expected_points} config_change_after_seconds=${CONFIG_CHANGE_AFTER_SECONDS} change_cycle=${change_cycle}"

for cycle in $(seq 1 "${cycles}"); do
  cycle_start_ms="$(date +%s%3N)"
  if [ "${changed}" -eq 0 ] && [ "${cycle}" -ge "${change_cycle}" ]; then
    seed_config_version "$(date +%s)" "50" "x / 100"
    changed=1
    echo "CONFIG_CHANGED cycle=${cycle} range_param_max=50 formula_param='x / 100'"
  fi
  publish_cycle "${cycle}"
  cycle_end_ms="$(date +%s%3N)"
  elapsed_cycle_ms="$((cycle_end_ms - cycle_start_ms))"
  sent_messages="$((cycle * messages_per_cycle))"
  echo "cycle=${cycle}/${cycles} sent_messages=${sent_messages}/${expected_messages} sent_points=$((sent_messages * points_per_message))/${expected_points} cycle_elapsed_ms=${elapsed_cycle_ms}"
  if [ "${cycle}" -lt "${cycles}" ]; then
    sleep_seconds="${INTERVAL_SECONDS}"
    if [ "${elapsed_cycle_ms}" -lt "$((INTERVAL_SECONDS * 1000))" ]; then
      sleep_seconds="$(((INTERVAL_SECONDS * 1000 - elapsed_cycle_ms) / 1000))"
      [ "${sleep_seconds}" -lt 1 ] && sleep_seconds=1
    fi
    sleep "${sleep_seconds}"
  fi
done

end_ms="$(date +%s%3N)"
elapsed_ms="$((end_ms - start_ms))"
qps="$(awk -v total="${expected_messages}" -v elapsed="${elapsed_ms}" 'BEGIN { if (elapsed == 0) print 0; else printf "%.2f", total * 1000 / elapsed }')"
echo "DONE run_id=${RUN_ID} expected_messages=${expected_messages} expected_points=${expected_points} elapsed_ms=${elapsed_ms} avg_mqtt_qps=${qps}"
