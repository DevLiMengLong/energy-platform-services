package io.github.energyiot.data.access.cleaning;

import io.github.energyiot.data.access.protocol.SequentialIdGenerator;
import io.github.energyiot.data.access.raw.RawPointRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CleanPointProcessorTest {

    @Test
    void convertsValueByFormulaAndPublishesEffectiveData() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("energy_meter_1")
                .setDeviceMark("device_1")
                .setParamMark("electric_total")
                .setTransformFormula("x / 1000")
                .setMinValue("0")
                .setMaxValue("20000");
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "12345")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getCleanTable()).isEqualTo("clean_param_tenant_1_energy_meter_1");
        assertThat(saved.getCleanValue()).isEqualByComparingTo("12.345");
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.NORMAL);
        assertThat(saved.isEffective()).isTrue();
        assertThat(publisher.getPublished()).hasSize(1);
        assertThat(publisher.getPublished().get(0).getMessageId()).isEqualTo("message-1");
    }

    @Test
    void appliesRangeRuleToParsedValueBeforeFormulaTransform() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("energy_meter_1")
                .setDeviceMark("device_1")
                .setParamMark("electric_total")
                .setTransformFormula("x / 1000")
                .setMinValue("0")
                .setMaxValue("100");
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "12345")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.OUT_OF_RANGE);
        assertThat(saved.isEffective()).isFalse();
        assertThat(saved.getCleanValue()).isNull();
        assertThat(publisher.getPublished()).isEmpty();
    }

    @Test
    void usesParsedPreviousCheckValueForCumulativeRuleBeforeFormulaTransform() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        repository.setPreviousValue("1000");
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = defaultConfig("electric_total")
                .setTransformFormula("x / 1000")
                .setCumulative(true);
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "1500")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.NORMAL);
        assertThat(saved.isEffective()).isTrue();
        assertThat(saved.getCleanValue()).isEqualByComparingTo("1.5");
        assertThat(publisher.getPublished()).hasSize(1);
    }

    @Test
    void marksDuplicateDataIneffectiveAndLinksOriginal() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        repository.setDuplicateOfId("clean-001");
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointProcessor processor = processor(repository, publisher, defaultConfig("electric_total"));

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "10")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.DUPLICATE);
        assertThat(saved.isEffective()).isFalse();
        assertThat(saved.getDuplicateOfId()).isEqualTo("clean-001");
        assertThat(publisher.getPublished()).isEmpty();
    }

    @Test
    void marksCumulativeRollbackIneffective() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        repository.setPreviousValue("1000");
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = defaultConfig("electric_total").setCumulative(true);
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "10")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.CUMULATIVE_ROLLBACK);
        assertThat(saved.isEffective()).isFalse();
        assertThat(publisher.getPublished()).isEmpty();
    }

    @Test
    void acceptsConfiguredRolloverAsEffective() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        repository.setPreviousValue("999980");
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = defaultConfig("electric_total")
                .setCumulative(true)
                .setRolloverEnabled(true)
                .setRolloverMaxValue("999999")
                .setRolloverMinPreviousValue("990000")
                .setRolloverMaxCurrentValue("1000");
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "25")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.ROLLOVER);
        assertThat(saved.isEffective()).isTrue();
        assertThat(saved.getCleanRule()).isEqualTo("rollover");
        assertThat(publisher.getPublished()).hasSize(1);
    }

    @Test
    void acceptsConfiguredRolloverBeforeDeltaCheck() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        repository.setPreviousValue("999980");
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = defaultConfig("electric_total")
                .setCumulative(true)
                .setMaxDelta("50")
                .setRolloverEnabled(true)
                .setRolloverMaxValue("999999")
                .setRolloverMinPreviousValue("990000")
                .setRolloverMaxCurrentValue("1000");
        CleanPointProcessor processor = processor(repository, publisher, config);

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "25")));

        CleanPointRecord saved = repository.getSaved().get(0);
        assertThat(saved.getQualityCode()).isEqualTo(CleanQualityCode.ROLLOVER);
        assertThat(saved.isEffective()).isTrue();
        assertThat(saved.getCleanRule()).isEqualTo("rollover");
        assertThat(publisher.getPublished()).hasSize(1);
    }

    @Test
    void loadsConfigByDeviceMark() {
        RecordingCleanPointRepository repository = new RecordingCleanPointRepository();
        RecordingAggregatePublisher publisher = new RecordingAggregatePublisher();
        CleanPointConfig config = defaultConfig("electric_total");
        AtomicReference<String> requestedDevice = new AtomicReference<>();
        CleanPointProcessor processor = new CleanPointProcessor(
                new SequentialIdGenerator(),
                new FormulaEvaluator(),
                (tenant, model, device, param) -> {
                    requestedDevice.set(device);
                    return config;
                },
                repository,
                publisher
        );

        processor.process(event("raw_table", "raw-1"), Collections.singletonList(raw("raw-1", "electric_total", "25")));

        assertThat(requestedDevice).hasValue("device_1");
    }

    private static CleanPointProcessor processor(RecordingCleanPointRepository repository,
                                                 RecordingAggregatePublisher publisher,
                                                 CleanPointConfig config) {
        return new CleanPointProcessor(
                new SequentialIdGenerator(),
                new FormulaEvaluator(),
                (tenant, model, device, param) -> config,
                repository,
                publisher
        );
    }

    private static CleaningEvent event(String tableName, String rawId) {
        return new CleaningEvent()
                .setTableName(tableName)
                .setRawIds(Collections.singletonList(rawId))
                .setTenantMark("tenant_1")
                .setModelMark("energy_meter_1")
                .setDeviceMark("device_1")
                .setMessageId("message-1");
    }

    private static CleanPointConfig defaultConfig(String paramMark) {
        return new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("energy_meter_1")
                .setDeviceMark("device_1")
                .setParamMark(paramMark)
                .setTransformFormula("x");
    }

    private static RawPointRecord raw(String id, String paramMark, String value) {
        Instant time = Instant.parse("2026-05-22T04:00:00Z");
        return new RawPointRecord()
                .setId(id)
                .setMessageId("message-1")
                .setProtocolVersion("v2")
                .setTenantMark("tenant_1")
                .setModelMark("energy_meter_1")
                .setDeviceMark("device_1")
                .setParamMark(paramMark)
                .setRawValue(value)
                .setDeviceTime(time)
                .setReceiveTime(time)
                .setNormalSecond(time.getEpochSecond())
                .setCreatedTime(time);
    }

    private static class RecordingCleanPointRepository implements CleanPointRepository {
        private final List<CleanPointRecord> saved = new java.util.ArrayList<>();
        private String duplicateOfId;
        private String previousRawValue;

        @Override
        public List<RawPointRecord> findRawRecords(String rawTable, List<String> rawIds) {
            return Collections.emptyList();
        }

        @Override
        public String findDuplicateEffectiveId(String cleanTable, CleanPointRecord record) {
            return duplicateOfId;
        }

        @Override
        public String findPreviousEffectiveRawValue(String cleanTable, CleanPointRecord record) {
            return previousRawValue;
        }

        @Override
        public void save(String cleanTable, List<CleanPointRecord> records) {
            saved.addAll(records);
        }

        List<CleanPointRecord> getSaved() {
            return saved;
        }

        void setDuplicateOfId(String duplicateOfId) {
            this.duplicateOfId = duplicateOfId;
        }

        void setPreviousValue(String previousValue) {
            this.previousRawValue = previousValue;
        }
    }

    private static class RecordingAggregatePublisher implements AggregateEventPublisher {
        private final List<AggregateEvent> published = new java.util.ArrayList<>();

        @Override
        public void publish(AggregateEvent event) {
            published.add(event);
        }

        List<AggregateEvent> getPublished() {
            return published;
        }
    }
}
