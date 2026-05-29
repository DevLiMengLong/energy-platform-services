package io.github.energyiot.data.access.aggregation;

import io.github.energyiot.data.access.cleaning.CleanPointConfig;
import io.github.energyiot.data.access.cleaning.CleanPointRecord;
import io.github.energyiot.data.access.cleaning.CleanQualityCode;
import io.github.energyiot.data.access.protocol.SequentialIdGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateCalculatorTest {

    private final AggregateCalculator calculator = new AggregateCalculator(
            new SequentialIdGenerator(),
            Clock.fixed(Instant.parse("2026-05-23T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void calculatesCumulativeMinuteUsageFromPreviousValueBeforeWindow() {
        AggregateSourceKey key = new AggregateSourceKey("tenant_1", "model_1", "device_1", "electric_total");
        CleanPointConfig config = new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("model_1")
                .setParamMark("electric_total")
                .setTransformFormula("x")
                .setCumulative(true);

        AggregatePointRecord record = calculator.fromCleanRecords(
                key,
                Instant.parse("2026-05-23T00:00:00Z"),
                Instant.parse("2026-05-23T00:01:00Z"),
                Arrays.asList(clean("c1", "110", 10), clean("c2", "115", 50)),
                new BigDecimal("100"),
                config
        );

        assertThat(record.getStartValue()).isEqualByComparingTo("100");
        assertThat(record.getEndValue()).isEqualByComparingTo("115");
        assertThat(record.getUsageValue()).isEqualByComparingTo("15");
        assertThat(record.getSampleCount()).isEqualTo(2);
    }

    @Test
    void rollsUpAverageBySumAndSampleCount() {
        AggregateSourceKey key = new AggregateSourceKey("tenant_1", "model_1", "device_1", "power");

        AggregatePointRecord record = calculator.fromLowerAggregates(
                key,
                AggregationGranularity.FIFTEEN_MINUTE,
                Instant.parse("2026-05-23T00:00:00Z"),
                Instant.parse("2026-05-23T00:15:00Z"),
                Arrays.asList(
                        aggregate("2026-05-23T00:00:00Z", "30", 3),
                        aggregate("2026-05-23T00:01:00Z", "70", 7)
                )
        );

        assertThat(record.getSumValue()).isEqualByComparingTo("100");
        assertThat(record.getSampleCount()).isEqualTo(10);
        assertThat(record.getAvgValue()).isEqualByComparingTo("10");
        assertThat(record.getSourceCount()).isEqualTo(2);
        assertThat(record.getQualityLevel()).isEqualTo(AggregateQualityLevel.INCOMPLETE);
    }

    @Test
    void usesFirstValueAsCumulativeBaselineWhenPreviousValueMissing() {
        AggregateSourceKey key = new AggregateSourceKey("tenant_1", "model_1", "device_1", "electric_total");
        CleanPointConfig config = new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("model_1")
                .setParamMark("electric_total")
                .setTransformFormula("x")
                .setCumulative(true);

        AggregatePointRecord record = calculator.fromCleanRecords(
                key,
                Instant.parse("2026-05-23T00:00:00Z"),
                Instant.parse("2026-05-23T00:01:00Z"),
                Arrays.asList(clean("c1", "110", 10), clean("c2", "115", 50)),
                null,
                config
        );

        assertThat(record.getStartValue()).isEqualByComparingTo("110");
        assertThat(record.getEndValue()).isEqualByComparingTo("115");
        assertThat(record.getUsageValue()).isEqualByComparingTo("5");
    }

    @Test
    void convertsRolloverMaxValueBeforeCalculatingUsage() {
        AggregateSourceKey key = new AggregateSourceKey("tenant_1", "model_1", "device_1", "electric_total");
        CleanPointConfig config = new CleanPointConfig()
                .setTenantMark("tenant_1")
                .setModelMark("model_1")
                .setParamMark("electric_total")
                .setTransformFormula("x / 1000")
                .setCumulative(true)
                .setRolloverMaxValue("1000");

        AggregatePointRecord record = calculator.fromCleanRecords(
                key,
                Instant.parse("2026-05-23T00:00:00Z"),
                Instant.parse("2026-05-23T00:01:00Z"),
                Arrays.asList(clean("c1", "0.01", 10).setQualityCode(CleanQualityCode.ROLLOVER)),
                new BigDecimal("0.99"),
                config
        );

        assertThat(record.getUsageValue()).isEqualByComparingTo("0.02");
    }

    private static CleanPointRecord clean(String id, String value, long secondInMinute) {
        Instant time = Instant.parse("2026-05-23T00:00:00Z").plusSeconds(secondInMinute);
        return new CleanPointRecord()
                .setId(id)
                .setTenantMark("tenant_1")
                .setModelMark("model_1")
                .setDeviceMark("device_1")
                .setParamMark("electric_total")
                .setCleanValue(new BigDecimal(value))
                .setNormalSecond(time.getEpochSecond())
                .setDeviceTime(time)
                .setEffective(true);
    }

    private static AggregatePointRecord aggregate(String windowStart, String sumValue, long sampleCount) {
        return new AggregatePointRecord()
                .setWindowStart(Instant.parse(windowStart))
                .setWindowEnd(Instant.parse(windowStart).plusSeconds(60))
                .setStartValue(new BigDecimal("0"))
                .setEndValue(new BigDecimal("0"))
                .setSumValue(new BigDecimal(sumValue))
                .setMinValue(new BigDecimal("1"))
                .setMaxValue(new BigDecimal("20"))
                .setSampleCount(sampleCount)
                .setSourceCount(sampleCount)
                .setQualityLevel(AggregateQualityLevel.NORMAL);
    }
}
