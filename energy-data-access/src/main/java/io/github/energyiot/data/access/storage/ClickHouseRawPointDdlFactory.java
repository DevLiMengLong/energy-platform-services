package io.github.energyiot.data.access.storage;

public class ClickHouseRawPointDdlFactory implements RawPointDdlFactory {

    @Override
    public String createTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
                "  id String,\n" +
                "  message_id String,\n" +
                "  protocol_version LowCardinality(String),\n" +
                "  tenant_mark LowCardinality(String),\n" +
                "  model_mark LowCardinality(String),\n" +
                "  device_mark String,\n" +
                "  param_mark String,\n" +
                "  raw_value String,\n" +
                "  device_time DateTime64(3, 'UTC'),\n" +
                "  receive_time DateTime64(3, 'UTC'),\n" +
                "  normal_second UInt64,\n" +
                "  created_time DateTime64(3, 'UTC')\n" +
                ") ENGINE = MergeTree\n" +
                "ORDER BY (device_mark, param_mark, normal_second)";
    }
}
