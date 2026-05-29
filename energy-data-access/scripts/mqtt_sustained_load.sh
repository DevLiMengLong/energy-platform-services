#!/usr/bin/env bash
set -euo pipefail
export LC_ALL=C
export LANG=C

MQTT_HOST="${MQTT_HOST:-127.0.0.1}"
MQTT_PORT="${MQTT_PORT:-1884}"
MOSQUITTO_IMAGE="${MOSQUITTO_IMAGE:-docker.1ms.run/eclipse-mosquitto:2.0.18}"
MOSQUITTO_CONTAINER="${MOSQUITTO_CONTAINER:-energy-data-access-mqtt}"
TENANTS="${TENANTS:-3}"
MODELS="${MODELS:-10}"
DEVICES="${DEVICES:-4}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-10}"
DURATION_SECONDS="${DURATION_SECONDS:-1800}"
CONCURRENCY="${CONCURRENCY:-30}"
RUN_ID="${RUN_ID:-mqtt-sustained-$(date +%s)}"

cycles="$((DURATION_SECONDS / INTERVAL_SECONDS))"
messages_per_cycle="$((TENANTS * MODELS * DEVICES))"
expected_messages="$((messages_per_cycle * cycles))"
expected_points="$((expected_messages * 3))"

publish_cycle() {
  local cycle="$1"
  local work_dir
  work_dir="$(mktemp -d)"

  for tenant_index in $(seq 1 "${TENANTS}"); do
    for model_index in $(seq 1 "${MODELS}"); do
      for device_index in $(seq 1 "${DEVICES}"); do
        tenant_mark="tenant_${tenant_index}"
        model_mark="energy_meter_${model_index}"
        device_mark="device_${device_index}"
        message_id="${RUN_ID}-${tenant_mark}-${model_mark}-${device_mark}-${cycle}"
        topic="v2/${tenant_mark}/${model_mark}/${device_mark}/telemetry"
        electric_value=$((tenant_index * 100000 + model_index * 1000 + device_index * 10 + cycle))
        water_value=$((tenant_index * 10000 + model_index * 100 + device_index * 10 + cycle))
        gas_value=$((tenant_index * 1000 + model_index * 100 + device_index * 10 + cycle))
        payload=$(printf '{"protocolVersion":"v2","tenantMark":"%s","modelMark":"%s","deviceMark":"%s","messageId":"%s","timestamp":"2026-05-22T04:00:00Z","data":{"electric_total":%s,"water_total":%s,"gas_total":%s}}' \
          "${tenant_mark}" "${model_mark}" "${device_mark}" "${message_id}" "${electric_value}" "${water_value}" "${gas_value}")
        printf '%s\0%s\0' "${topic}" "${payload}" >> "${work_dir}/messages.bin"
      done
    done
  done

  export MOSQUITTO_IMAGE MOSQUITTO_CONTAINER MQTT_HOST MQTT_PORT
  xargs -0 -P "${CONCURRENCY}" -n 2 sh -c '
    topic="$1"
    payload="$2"
    if docker ps --format "{{.Names}}" | grep -qx "$MOSQUITTO_CONTAINER"; then
      docker exec "$MOSQUITTO_CONTAINER" mosquitto_pub -h 127.0.0.1 -p 1883 -q 1 -t "$topic" -m "$payload" >/dev/null 2>/dev/null
    else
      docker run --rm --network host "$MOSQUITTO_IMAGE" mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -q 1 -t "$topic" -m "$payload" >/dev/null 2>/dev/null
    fi
  ' _ < "${work_dir}/messages.bin"

  rm -rf "${work_dir}"
}

start_ms="$(date +%s%3N)"
echo "run_id=${RUN_ID} tenants=${TENANTS} models=${MODELS} devices=${DEVICES} interval_seconds=${INTERVAL_SECONDS} duration_seconds=${DURATION_SECONDS} cycles=${cycles} expected_messages=${expected_messages} expected_points=${expected_points}"

for cycle in $(seq 1 "${cycles}"); do
  cycle_start_ms="$(date +%s%3N)"
  publish_cycle "${cycle}"
  cycle_end_ms="$(date +%s%3N)"
  elapsed_cycle_ms="$((cycle_end_ms - cycle_start_ms))"
  sent_messages="$((cycle * messages_per_cycle))"
  echo "cycle=${cycle}/${cycles} sent_messages=${sent_messages}/${expected_messages} cycle_elapsed_ms=${elapsed_cycle_ms}"
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
