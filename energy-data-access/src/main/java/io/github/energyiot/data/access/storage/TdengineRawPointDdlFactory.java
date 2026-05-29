package io.github.energyiot.data.access.storage;

public class TdengineRawPointDdlFactory implements RawPointDdlFactory {

    @Override
    public String createTableSql(String tableName) {
        return "CREATE STABLE IF NOT EXISTS `" + tableName + "` (\n" +
                "  ts TIMESTAMP,\n" +
                "  id NCHAR(64),\n" +
                "  message_id NCHAR(128),\n" +
                "  protocol_version NCHAR(16),\n" +
                "  raw_value NCHAR(512),\n" +
                "  device_time TIMESTAMP,\n" +
                "  receive_time TIMESTAMP,\n" +
                "  normal_second BIGINT,\n" +
                "  created_time TIMESTAMP\n" +
                ") TAGS (\n" +
                "  tenant_mark NCHAR(128),\n" +
                "  model_mark NCHAR(128),\n" +
                "  device_mark NCHAR(128),\n" +
                "  param_mark NCHAR(128)\n" +
                ")";
    }
}
