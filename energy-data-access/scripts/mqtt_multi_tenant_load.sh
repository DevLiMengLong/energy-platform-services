#!/usr/bin/env bash
set -euo pipefail

MQTT_HOST="${MQTT_HOST:-127.0.0.1}"
MQTT_PORT="${MQTT_PORT:-1883}"
MOSQUITTO_IMAGE="${MOSQUITTO_IMAGE:-docker.1ms.run/eclipse-mosquitto:2.0.18}"
TENANTS="${TENANTS:-3}"
MODELS="${MODELS:-2}"
DEVICES="${DEVICES:-4}"
REPEAT="${REPEAT:-5}"
CONCURRENCY="${CONCURRENCY:-20}"
RUN_ID="${RUN_ID:-mqtt-$(date +%s)}"

work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

for tenant_index in $(seq 1 "${TENANTS}"); do
  for model_index in $(seq 1 "${MODELS}"); do
    for device_index in $(seq 1 "${DEVICES}"); do
      for repeat_index in $(seq 1 "${REPEAT}"); do
        tenant_mark="tenant_${tenant_index}"
        model_mark="energy_meter_${model_index}"
        device_mark="device_${device_index}"
        message_id="${RUN_ID}-${tenant_mark}-${model_mark}-${device_mark}-${repeat_index}"
        topic="v2/${tenant_mark}/${model_mark}/${device_mark}/telemetry"
        electric_value=$((tenant_index * 100000 + model_index * 1000 + device_index * 10 + repeat_index))
        water_value=$((tenant_index * 10000 + model_index * 100 + device_index * 10 + repeat_index))
        gas_value=$((tenant_index * 1000 + model_index * 100 + device_index * 10 + repeat_index))
        payload=$(printf '{"protocolVersion":"v2","tenantMark":"%s","modelMark":"%s","deviceMark":"%s","messageId":"%s","timestamp":"2026-05-22T04:00:00Z","data":{"electric_total":%s,"water_total":%s,"gas_total":%s}}' \
          "${tenant_mark}" "${model_mark}" "${device_mark}" "${message_id}" "${electric_value}" "${water_value}" "${gas_value}")
        printf '%s\0%s\0' "${topic}" "${payload}" >> "${work_dir}/messages.bin"
      done
    done
  done
done

message_count="$((TENANTS * MODELS * DEVICES * REPEAT))"
expected_points="$((message_count * 3))"
start_ms="$(date +%s%3N)"
export MOSQUITTO_IMAGE MQTT_HOST MQTT_PORT

xargs -0 -P "${CONCURRENCY}" -n 2 sh -c '
  topic="$1"
  payload="$2"
  docker run --rm --network host "$MOSQUITTO_IMAGE" mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -q 1 -t "$topic" -m "$payload" >/dev/null
' _ < "${work_dir}/messages.bin"

end_ms="$(date +%s%3N)"
elapsed_ms="$((end_ms - start_ms))"
qps="$(awk -v total="${message_count}" -v elapsed="${elapsed_ms}" 'BEGIN { if (elapsed == 0) print 0; else printf "%.2f", total * 1000 / elapsed }')"

echo "run_id=${RUN_ID} messages=${message_count} expected_points=${expected_points} elapsed_ms=${elapsed_ms} mqtt_qps=${qps}"
