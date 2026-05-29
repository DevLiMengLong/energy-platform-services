package io.github.energyiot.data.access.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RawPointTableTest {

    @Test
    void buildsSafeTenantModelTableName() {
        RawPointTableNameResolver resolver = new RawPointTableNameResolver("raw_param");

        assertThat(resolver.resolve("tenant-A", "electric.meter"))
                .isEqualTo("raw_param_tenant_a_electric_meter");
    }

    @Test
    void createsClickHouseDdlForTenantModelTable() {
        RawPointDdlFactory factory = new ClickHouseRawPointDdlFactory();

        String ddl = factory.createTableSql("raw_param_tenant_a_electric_meter");

        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS `raw_param_tenant_a_electric_meter`");
        assertThat(ddl).contains("id String");
        assertThat(ddl).contains("message_id String");
        assertThat(ddl).contains("protocol_version LowCardinality(String)");
        assertThat(ddl).contains("tenant_mark LowCardinality(String)");
        assertThat(ddl).contains("model_mark LowCardinality(String)");
        assertThat(ddl).contains("device_mark String");
        assertThat(ddl).contains("param_mark String");
        assertThat(ddl).contains("raw_value String");
        assertThat(ddl).contains("device_time DateTime64(3, 'UTC')");
        assertThat(ddl).contains("receive_time DateTime64(3, 'UTC')");
        assertThat(ddl).contains("normal_second UInt64");
        assertThat(ddl).contains("created_time DateTime64(3, 'UTC')");
        assertThat(ddl).contains("ENGINE = MergeTree");
        assertThat(ddl).contains("ORDER BY (device_mark, param_mark, normal_second)");
    }

    @Test
    void createsTdengineDdlForTenantModelStable() {
        RawPointDdlFactory factory = new TdengineRawPointDdlFactory();

        String ddl = factory.createTableSql("raw_param_tenant_a_electric_meter");

        assertThat(ddl).contains("CREATE STABLE IF NOT EXISTS `raw_param_tenant_a_electric_meter`");
        assertThat(ddl).contains("ts TIMESTAMP");
        assertThat(ddl).contains("id NCHAR(64)");
        assertThat(ddl).contains("raw_value NCHAR(512)");
        assertThat(ddl).contains("TAGS");
        assertThat(ddl).contains("device_mark NCHAR(128)");
        assertThat(ddl).contains("param_mark NCHAR(128)");
    }
}
