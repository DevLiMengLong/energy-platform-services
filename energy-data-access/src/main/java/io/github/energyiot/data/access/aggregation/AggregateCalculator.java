package io.github.energyiot.data.access.aggregation;

import io.github.energyiot.data.access.cleaning.CleanPointConfig;
import io.github.energyiot.data.access.cleaning.CleanPointRecord;
import io.github.energyiot.data.access.cleaning.CleanQualityCode;
import io.github.energyiot.data.access.cleaning.FormulaEvaluator;
import io.github.energyiot.data.access.protocol.IdGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class AggregateCalculator {

    private final IdGenerator idGenerator;
    private final Clock clock;
    private final FormulaEvaluator formulaEvaluator;

    public AggregateCalculator(IdGenerator idGenerator, Clock clock) {
        this(idGenerator, clock, new FormulaEvaluator());
    }

    public AggregateCalculator(IdGenerator idGenerator, Clock clock, FormulaEvaluator formulaEvaluator) {
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.formulaEvaluator = formulaEvaluator;
    }

    public AggregatePointRecord fromCleanRecords(AggregateSourceKey key,
                                                 Instant windowStart,
                                                 Instant windowEnd,
                                                 List<CleanPointRecord> records,
                                                 BigDecimal previousValue,
                                                 CleanPointConfig config) {
        if (records.isEmpty()) {
            return null;
        }
        records.sort(Comparator.comparingLong(CleanPointRecord::getNormalSecond).thenComparing(CleanPointRecord::getId));
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = null;
        long rolloverCount = 0;
        for (CleanPointRecord record : records) {
            BigDecimal value = record.getCleanValue();
            if (value == null) {
                continue;
            }
            sum = sum.add(value);
            min = min == null || value.compareTo(min) < 0 ? value : min;
            max = max == null || value.compareTo(max) > 0 ? value : max;
            if (record.getQualityCode() == CleanQualityCode.ROLLOVER) {
                rolloverCount++;
            }
        }
        CleanPointRecord first = records.get(0);
        CleanPointRecord last = records.get(records.size() - 1);
        BigDecimal startValue = config.isCumulative() && previousValue != null ? previousValue : first.getCleanValue();
        BigDecimal endValue = last.getCleanValue();
        BigDecimal usage = null;
        if (config.isCumulative() && startValue != null && endValue != null) {
            usage = endValue.compareTo(startValue) >= 0
                    ? endValue.subtract(startValue)
                    : rolloverUsage(config, startValue, endValue);
        }
        return baseRecord(key, windowStart, windowEnd)
                .setStartValue(startValue)
                .setEndValue(endValue)
                .setUsageValue(usage)
                .setSumValue(sum)
                .setAvgValue(avg(sum, records.size()))
                .setMinValue(min)
                .setMaxValue(max)
                .setSampleCount(records.size())
                .setSourceCount(records.size())
                .setRolloverCount(rolloverCount)
                .setQualityLevel(rolloverCount > 0 ? AggregateQualityLevel.HAS_ROLLOVER : AggregateQualityLevel.NORMAL);
    }

    public AggregatePointRecord fromLowerAggregates(AggregateSourceKey key,
                                                    AggregationGranularity granularity,
                                                    Instant windowStart,
                                                    Instant windowEnd,
                                                    List<AggregatePointRecord> records) {
        if (records.isEmpty()) {
            return null;
        }
        records.sort(Comparator.comparing(AggregatePointRecord::getWindowStart));
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal usage = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = null;
        long sampleCount = 0;
        long rolloverCount = 0;
        boolean hasUsage = false;
        for (AggregatePointRecord record : records) {
            if (record.getSumValue() != null) {
                sum = sum.add(record.getSumValue());
            }
            if (record.getUsageValue() != null) {
                usage = usage.add(record.getUsageValue());
                hasUsage = true;
            }
            if (record.getMinValue() != null) {
                min = min == null || record.getMinValue().compareTo(min) < 0 ? record.getMinValue() : min;
            }
            if (record.getMaxValue() != null) {
                max = max == null || record.getMaxValue().compareTo(max) > 0 ? record.getMaxValue() : max;
            }
            sampleCount += record.getSampleCount();
            rolloverCount += record.getRolloverCount();
        }
        AggregatePointRecord first = records.get(0);
        AggregatePointRecord last = records.get(records.size() - 1);
        int quality = qualityLevel(granularity, records.size(), rolloverCount);
        return baseRecord(key, windowStart, windowEnd)
                .setStartValue(first.getStartValue())
                .setEndValue(last.getEndValue())
                .setUsageValue(hasUsage ? usage : null)
                .setSumValue(sum)
                .setAvgValue(avg(sum, sampleCount))
                .setMinValue(min)
                .setMaxValue(max)
                .setSampleCount(sampleCount)
                .setSourceCount(records.size())
                .setRolloverCount(rolloverCount)
                .setQualityLevel(quality);
    }

    private AggregatePointRecord baseRecord(AggregateSourceKey key, Instant windowStart, Instant windowEnd) {
        Instant now = Instant.now(clock);
        return new AggregatePointRecord()
                .setId(idGenerator.nextId())
                .setTenantMark(key.getTenantMark())
                .setModelMark(key.getModelMark())
                .setDeviceMark(key.getDeviceMark())
                .setParamMark(key.getParamMark())
                .setWindowStart(windowStart)
                .setWindowEnd(windowEnd)
                .setVersion(now.toEpochMilli())
                .setCreatedTime(now)
                .setUpdatedTime(now);
    }

    private static BigDecimal avg(BigDecimal sum, long count) {
        if (count <= 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal rolloverUsage(CleanPointConfig config, BigDecimal previous, BigDecimal current) {
        if (config.getRolloverMaxValue() == null) {
            return null;
        }
        BigDecimal cleanRolloverMaxValue = formulaEvaluator.evaluate(config.getTransformFormula(), config.getRolloverMaxValue());
        return cleanRolloverMaxValue.subtract(previous).add(current);
    }

    private static int qualityLevel(AggregationGranularity granularity, int sourceCount, long rolloverCount) {
        if (sourceCount < granularity.getExpectedSourceCount()) {
            return AggregateQualityLevel.INCOMPLETE;
        }
        if (rolloverCount > 0) {
            return AggregateQualityLevel.HAS_ROLLOVER;
        }
        return AggregateQualityLevel.NORMAL;
    }
}
