package io.github.energyiot.data.access.service;

import io.github.energyiot.data.access.cleaning.CleaningEvent;
import io.github.energyiot.data.access.cleaning.CleaningEventPublisher;
import io.github.energyiot.data.access.protocol.SequentialIdGenerator;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import io.github.energyiot.data.access.protocol.UnifiedPayloadDecoder;
import io.github.energyiot.data.access.raw.RawPointRecord;
import io.github.energyiot.data.access.storage.RawPointStorage;
import io.github.energyiot.data.access.storage.RawPointWriteResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPointIngestionServiceTest {

    @Test
    void publishesCleaningEventAfterRawPointsAreSaved() {
        RecordingStorage storage = new RecordingStorage();
        RecordingCleaningPublisher publisher = new RecordingCleaningPublisher();
        UnifiedPayloadDecoder decoder = new UnifiedPayloadDecoder(
                new SequentialIdGenerator(),
                Clock.fixed(Instant.parse("2026-05-22T02:00:03Z"), ZoneId.of("UTC"))
        );
        RawPointIngestionService service = new RawPointIngestionService(decoder, storage, publisher);

        UnifiedPayload payload = payload();

        RawPointWriteResult result = service.ingest(payload);

        assertThat(result.getTableName()).isEqualTo("raw_param_tenanta_electric_meter");
        assertThat(storage.getSavedRecords()).hasSize(2);
        assertThat(publisher.getEvents()).hasSize(1);
        CleaningEvent event = publisher.getEvents().get(0);
        assertThat(event.getTableName()).isEqualTo("raw_param_tenanta_electric_meter");
        assertThat(event.getRawIds()).containsExactly("raw-1", "raw-2");
        assertThat(event.getTenantMark()).isEqualTo("tenantA");
        assertThat(event.getModelMark()).isEqualTo("electric_meter");
        assertThat(event.getDeviceMark()).isEqualTo("device001");
        assertThat(event.getMessageId()).isEqualTo("msg-001");
    }

    @Test
    void doesNotPublishCleaningEventWhenRawStorageFails() {
        RecordingCleaningPublisher publisher = new RecordingCleaningPublisher();
        UnifiedPayloadDecoder decoder = new UnifiedPayloadDecoder(
                new SequentialIdGenerator(),
                Clock.fixed(Instant.parse("2026-05-22T02:00:03Z"), ZoneId.of("UTC"))
        );
        RawPointIngestionService service = new RawPointIngestionService(
                decoder,
                records -> {
                    throw new IllegalStateException("storage unavailable");
                },
                publisher
        );

        assertThatThrownBy(() -> service.ingest(payload()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("storage unavailable");
        assertThat(publisher.getEvents()).isEmpty();
    }

    private static UnifiedPayload payload() {
        UnifiedPayload payload = new UnifiedPayload();
        payload.setProtocolVersion("v2");
        payload.setTenantMark("tenantA");
        payload.setModelMark("electric_meter");
        payload.setDeviceMark("device001");
        payload.setMessageId("msg-001");
        payload.setTimestamp("2026-05-22T02:00:00Z");
        payload.getData().put("kwh_total", "123.45");
        payload.getData().put("voltage", "220.1");
        return payload;
    }

    private static class RecordingStorage implements RawPointStorage {
        private List<RawPointRecord> savedRecords = new ArrayList<>();

        @Override
        public RawPointWriteResult save(List<RawPointRecord> records) {
            this.savedRecords = new ArrayList<>(records);
            return new RawPointWriteResult("raw_param_tenanta_electric_meter", records);
        }

        List<RawPointRecord> getSavedRecords() {
            return savedRecords;
        }
    }

    private static class RecordingCleaningPublisher implements CleaningEventPublisher {
        private final List<CleaningEvent> events = new ArrayList<>();

        @Override
        public void publish(CleaningEvent event) {
            events.add(event);
        }

        List<CleaningEvent> getEvents() {
            return events;
        }
    }
}
