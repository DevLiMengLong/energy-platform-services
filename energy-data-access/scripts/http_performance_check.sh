#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8088}"
PROTOCOL_VERSION="${PROTOCOL_VERSION:-v2}"
TENANT_MARK="${TENANT_MARK:-tenant_demo}"
MODEL_MARK="${MODEL_MARK:-energy_meter}"
DEVICE_MARK="${DEVICE_MARK:-meter_001}"
TOTAL="${TOTAL:-1000}"
CONCURRENCY="${CONCURRENCY:-20}"

endpoint="${BASE_URL}/ingest/${PROTOCOL_VERSION}/${TENANT_MARK}/${MODEL_MARK}/${DEVICE_MARK}/telemetry"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

curl -fsS "${BASE_URL}/actuator/health" >/dev/null

start_ms="$(date +%s%3N)"
seq 1 "${TOTAL}" | xargs -P "${CONCURRENCY}" -I {} sh -c '
  id="$1"
  endpoint="$2"
  payload=$(printf "{\"messageId\":\"perf-%s-%s\",\"timestamp\":\"2026-05-22T04:00:00Z\",\"data\":{\"electric_total\":%s,\"water_total\":%s,\"gas_total\":%s}}" "$(date +%s)" "$id" "$id" "$id" "$id")
  curl -sS -o /dev/null -w "%{http_code} %{time_total}\n" -H "Content-Type: application/json" -X POST -d "$payload" "$endpoint"
' _ {} "${endpoint}" > "${work_dir}/results.txt"
end_ms="$(date +%s%3N)"

success_count="$(awk '$1 >= 200 && $1 < 300 { count++ } END { print count + 0 }' "${work_dir}/results.txt")"
fail_count="$((TOTAL - success_count))"
elapsed_ms="$((end_ms - start_ms))"

awk -v total="${TOTAL}" -v ok="${success_count}" -v failed="${fail_count}" -v elapsed="${elapsed_ms}" '
  {
    times[NR] = $2 * 1000
    sum += times[NR]
  }
  END {
    n = NR
    asort(times)
    p50 = times[int(n * 0.50) < 1 ? 1 : int(n * 0.50)]
    p95 = times[int(n * 0.95) < 1 ? 1 : int(n * 0.95)]
    p99 = times[int(n * 0.99) < 1 ? 1 : int(n * 0.99)]
    avg = n == 0 ? 0 : sum / n
    qps = elapsed == 0 ? 0 : ok * 1000 / elapsed
    printf "total=%d success=%d failed=%d elapsed_ms=%d qps=%.2f avg_ms=%.2f p50_ms=%.2f p95_ms=%.2f p99_ms=%.2f\n", total, ok, failed, elapsed, qps, avg, p50, p95, p99
  }
' "${work_dir}/results.txt"
