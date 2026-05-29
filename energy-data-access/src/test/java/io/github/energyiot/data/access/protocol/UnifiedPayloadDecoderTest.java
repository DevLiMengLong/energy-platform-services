package io.github.energyiot.data.access.protocol;

import io.github.energyiot.data.access.raw.RawPointRecord;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedPayloadDecoderTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-22T02:00:03Z"), ZoneId.of("UTC"));

    @Test
    void decodesDataObjectIntoRawPointRecordsAndKeepsDuplicateUploads() {
        UnifiedPayload payload = new UnifiedPayload();
        payload.setProtocolVersion("v2");
        payload.setTenantMark("tenantA");
        payload.setModelMark("electric_meter");
        payload.setDeviceMark("device001");
        payload.setMessageId("msg-001");
        payload.setTimestamp("2026-05-22T02:00:00Z");
        payload.getData().put("kwh_total", "123.45");
        payload.getData().put("kwh_total_duplicate", "123.45");

        UnifiedPayloadDecoder decoder = new UnifiedPayloadDecoder(new SequentialIdGenerator(), clock);

        List<RawPointRecord> records = decoder.decode(payload);

        assertThat(records).hasSize(2);
        assertThat(records).extracting(RawPointRecord::getRawValue)
                .containsExactly("123.45", "123.45");
        assertThat(records).extracting(RawPointRecord::getId)
                .containsExactly("raw-1", "raw-2");
        assertThat(records).extracting(RawPointRecord::getTenantMark)
                .containsOnly("tenantA");
        assertThat(records).extracting(RawPointRecord::getModelMark)
                .containsOnly("electric_meter");
        assertThat(records).extracting(RawPointRecord::getDeviceMark)
                .containsOnly("device001");
        assertThat(records).extracting(RawPointRecord::getMessageId)
                .containsOnly("msg-001");
        assertThat(records).extracting(RawPointRecord::getProtocolVersion)
                .containsOnly("v2");
        assertThat(records).extracting(RawPointRecord::getDeviceTime)
                .containsOnly(Instant.parse("2026-05-22T02:00:00Z"));
        assertThat(records).extracting(RawPointRecord::getReceiveTime)
                .containsOnly(Instant.parse("2026-05-22T02:00:03Z"));
        assertThat(records).extracting(RawPointRecord::getNormalSecond)
                .containsOnly(1779415200L);
        assertThat(records).extracting(RawPointRecord::getCreatedTime)
                .containsOnly(Instant.parse("2026-05-22T02:00:03Z"));
    }

    @Test
    void usesReceiveTimeWhenDeviceTimestampIsMissing() {
        UnifiedPayload payload = new UnifiedPayload();
        payload.setProtocolVersion("v2");
        payload.setTenantMark("tenantA");
        payload.setModelMark("water_meter");
        payload.setDeviceMark("device002");
        payload.setMessageId("msg-002");
        payload.getData().put("flow_total", 19.8);

        UnifiedPayloadDecoder decoder = new UnifiedPayloadDecoder(new SequentialIdGenerator(), clock);

        List<RawPointRecord> records = decoder.decode(payload);

        assertThat(records).hasSize(1);
        RawPointRecord record = records.get(0);
        assertThat(record.getRawValue()).isEqualTo("19.8");
        assertThat(record.getDeviceTime()).isEqualTo(Instant.parse("2026-05-22T02:00:03Z"));
        assertThat(record.getNormalSecond()).isEqualTo(1779415203L);
    }

    @Test
    void createsInternalKafkaKeyFromTenantModelAndDevice() {
        UnifiedPayload payload = new UnifiedPayload();
        payload.setTenantMark("tenantA");
        payload.setModelMark("gas_meter");
        payload.setDeviceMark("device003");

        assertThat(UnifiedPayloadDecoder.kafkaKey(payload))
                .isEqualTo("tenantA|gas_meter|device003");
    }
}
