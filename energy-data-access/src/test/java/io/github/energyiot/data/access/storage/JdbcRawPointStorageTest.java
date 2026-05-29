package io.github.energyiot.data.access.storage;

import io.github.energyiot.data.access.raw.RawPointRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcRawPointStorageTest {

    @Test
    void createsTableOnceAndBatchesRecordsIntoResolvedTable() {
        RecordingJdbcOperations jdbc = new RecordingJdbcOperations();
        RawPointTableNameResolver resolver = new RawPointTableNameResolver("raw_param");
        RawPointStorage storage = new JdbcRawPointStorage(
                jdbc,
                resolver,
                new ClickHouseRawPointDdlFactory(),
                new ClickHouseRawPointInsertFactory(),
                true
        );

        RawPointRecord first = sample("raw-1", "tenantA", "electric_meter", "device001", "kwh");
        RawPointRecord second = sample("raw-2", "tenantA", "electric_meter", "device001", "voltage");

        RawPointWriteResult result = storage.save(Arrays.asList(first, second));

        assertThat(result.getTableName()).isEqualTo("raw_param_tenanta_electric_meter");
        assertThat(result.getRecords()).containsExactly(first, second);
        assertThat(jdbc.getExecutedSql()).hasSize(1);
        assertThat(jdbc.getExecutedSql().get(0)).contains("CREATE TABLE IF NOT EXISTS `raw_param_tenanta_electric_meter`");
        assertThat(jdbc.getBatchSql()).startsWith("INSERT INTO `raw_param_tenanta_electric_meter`");
        assertThat(jdbc.getBatchSql()).contains("id,message_id,protocol_version");
        assertThat(jdbc.getBatchSql()).contains("VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
        assertThat(jdbc.getBatchArgs()).hasSize(2);

        storage.save(Arrays.asList(first));

        assertThat(jdbc.getExecutedSql()).hasSize(1);
    }

    private static RawPointRecord sample(String id, String tenant, String model, String device, String param) {
        Instant now = Instant.parse("2026-05-22T02:00:00Z");
        return new RawPointRecord()
                .setId(id)
                .setMessageId("msg-001")
                .setProtocolVersion("v2")
                .setTenantMark(tenant)
                .setModelMark(model)
                .setDeviceMark(device)
                .setParamMark(param)
                .setRawValue("1.23")
                .setDeviceTime(now)
                .setReceiveTime(now)
                .setNormalSecond(now.getEpochSecond())
                .setCreatedTime(now);
    }
}
