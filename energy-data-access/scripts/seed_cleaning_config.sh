#!/usr/bin/env bash
set -euo pipefail
export LC_ALL=C
export LANG=C

CLICKHOUSE_URL="${CLICKHOUSE_URL:-http://127.0.0.1:8123/}"
TENANT_MARK="${TENANT_MARK:-tenant_clean}"
MODEL_MARK="${MODEL_MARK:-energy_meter_clean}"
VERSION="${VERSION:-$(date +%s)}"

create_sql=$(cat <<SQL
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
) ENGINE = ReplacingMergeTree(version) ORDER BY (tenant_mark, model_mark, param_mark);
SQL
)

insert_sql=$(cat <<SQL
INSERT INTO point_clean_config
(tenant_mark, model_mark, param_mark, transform_formula, min_value, max_value, max_delta,
 is_cumulative, rollover_enabled, rollover_max_value, rollover_min_previous_value,
 rollover_max_current_value, enabled, version, updated_time)
VALUES
('${TENANT_MARK}', '${MODEL_MARK}', 'normal_param', 'x', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'format_param', 'x', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'range_param', 'x', '0', '100', NULL, 0, 0, NULL, NULL, NULL, 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'spike_param', 'x', NULL, NULL, '50', 0, 0, NULL, NULL, NULL, 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'rollback_param', 'x', NULL, NULL, NULL, 1, 0, NULL, NULL, NULL, 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'rollover_param', 'x', NULL, NULL, '50', 1, 1, '999999', '990000', '1000', 1, ${VERSION}, now()),
('${TENANT_MARK}', '${MODEL_MARK}', 'formula_param', 'x / 1000', NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, 1, ${VERSION}, now());
SQL
)

printf '%s\n' "${create_sql}" | curl -sSf --data-binary @- "${CLICKHOUSE_URL}" >/dev/null
printf '%s\n' "${insert_sql}" | curl -sSf --data-binary @- "${CLICKHOUSE_URL}" >/dev/null
echo "seeded_cleaning_config tenant=${TENANT_MARK} model=${MODEL_MARK} version=${VERSION}"
