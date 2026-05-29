-- point_clean_config 设备粒度迁移参考脚本。
-- 执行前请先确认当前表名与库名，并按环境备份旧表。

RENAME TABLE point_clean_config TO point_clean_config_backup_before_device_mark;

CREATE TABLE IF NOT EXISTS point_clean_config (
    tenant_mark String,
    model_mark String,
    device_mark String,
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
) ENGINE = ReplacingMergeTree(version)
ORDER BY (tenant_mark, model_mark, device_mark, param_mark);

-- 旧配置缺少 device_mark，不能无损映射到设备粒度。
-- 如需保留旧配置，请按基础服务设备清单展开后再写入新表。
