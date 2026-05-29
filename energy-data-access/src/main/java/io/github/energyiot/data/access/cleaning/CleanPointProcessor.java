package io.github.energyiot.data.access.cleaning;

import io.github.energyiot.data.access.protocol.IdGenerator;
import io.github.energyiot.data.access.latest.LatestCleanPointStore;
import io.github.energyiot.data.access.raw.RawPointRecord;
import io.github.energyiot.data.access.storage.RawPointTableNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleanPointProcessor {
    private static final Logger log = LoggerFactory.getLogger(CleanPointProcessor.class);

    private final IdGenerator idGenerator;
    private final FormulaEvaluator formulaEvaluator;
    private final CleanPointConfigProvider configProvider;
    private final CleanPointRepository repository;
    private final AggregateEventPublisher aggregateEventPublisher;
    private final LatestCleanPointStore latestCleanPointStore;
    private final RawPointTableNameResolver cleanTableResolver;
    private final Clock clock;

    public CleanPointProcessor(IdGenerator idGenerator,
                               FormulaEvaluator formulaEvaluator,
                               CleanPointConfigProvider configProvider,
                               CleanPointRepository repository,
                               AggregateEventPublisher aggregateEventPublisher) {
        this(idGenerator, formulaEvaluator, configProvider, repository, aggregateEventPublisher, new io.github.energyiot.data.access.latest.NoopLatestCleanPointStore(), Clock.systemUTC());
    }

    public CleanPointProcessor(IdGenerator idGenerator,
                               FormulaEvaluator formulaEvaluator,
                               CleanPointConfigProvider configProvider,
                               CleanPointRepository repository,
                               AggregateEventPublisher aggregateEventPublisher,
                               Clock clock) {
        this(idGenerator, formulaEvaluator, configProvider, repository, aggregateEventPublisher, new io.github.energyiot.data.access.latest.NoopLatestCleanPointStore(), clock);
    }

    public CleanPointProcessor(IdGenerator idGenerator,
                               FormulaEvaluator formulaEvaluator,
                               CleanPointConfigProvider configProvider,
                               CleanPointRepository repository,
                               AggregateEventPublisher aggregateEventPublisher,
                               LatestCleanPointStore latestCleanPointStore,
                               Clock clock) {
        this.idGenerator = idGenerator;
        this.formulaEvaluator = formulaEvaluator;
        this.configProvider = configProvider;
        this.repository = repository;
        this.aggregateEventPublisher = aggregateEventPublisher;
        this.latestCleanPointStore = latestCleanPointStore;
        this.clock = clock;
        this.cleanTableResolver = new RawPointTableNameResolver("clean_param");
    }

    public CleanPointProcessor(IdGenerator idGenerator,
                               FormulaEvaluator formulaEvaluator,
                               CleanPointConfigProvider configProvider,
                               CleanPointRepository repository,
                               AggregateEventPublisher aggregateEventPublisher,
                               LatestCleanPointStore latestCleanPointStore,
                               Clock clock,
                               String cleanTablePrefix) {
        this.idGenerator = idGenerator;
        this.formulaEvaluator = formulaEvaluator;
        this.configProvider = configProvider;
        this.repository = repository;
        this.aggregateEventPublisher = aggregateEventPublisher;
        this.latestCleanPointStore = latestCleanPointStore;
        this.clock = clock;
        this.cleanTableResolver = new RawPointTableNameResolver(cleanTablePrefix);
    }

    public List<CleanPointRecord> process(CleaningEvent event) {
        return process(event, repository.findRawRecords(event.getTableName(), event.getRawIds()));
    }

    public List<CleanPointRecord> process(CleaningEvent event, List<RawPointRecord> rawRecords) {
        List<CleanPointRecord> cleanRecords = new ArrayList<>();
        Set<String> batchEffectiveKeys = new HashSet<>();
        for (RawPointRecord raw : rawRecords) {
            CleanPointRecord clean = clean(raw);
            String duplicateOfId = repository.findDuplicateEffectiveId(clean.getCleanTable(), clean);
            if (duplicateOfId != null || batchEffectiveKeys.contains(duplicateKey(clean))) {
                clean.setQualityCode(CleanQualityCode.DUPLICATE)
                        .setEffective(false)
                        .setDuplicateOfId(duplicateOfId);
            } else if (clean.isEffective()) {
                batchEffectiveKeys.add(duplicateKey(clean));
            }
            cleanRecords.add(clean);
        }
        if (!cleanRecords.isEmpty()) {
            repository.save(cleanRecords.get(0).getCleanTable(), cleanRecords);
        }
        for (CleanPointRecord cleanRecord : cleanRecords) {
            if (cleanRecord.isEffective()) {
                try {
                    latestCleanPointStore.update(cleanRecord);
                } catch (Exception e) {
                    log.warn("failed to update latest clean point tenant={} model={} device={} param={}",
                            cleanRecord.getTenantMark(), cleanRecord.getModelMark(), cleanRecord.getDeviceMark(),
                            cleanRecord.getParamMark(), e);
                }
                aggregateEventPublisher.publish(toAggregateEvent(cleanRecord));
            }
        }
        return cleanRecords;
    }

    private CleanPointRecord clean(RawPointRecord raw) {
        String cleanTable = cleanTableResolver.resolve(raw.getTenantMark(), raw.getModelMark());
        CleanPointConfig config = configProvider.getConfig(raw.getTenantMark(), raw.getModelMark(), raw.getDeviceMark(), raw.getParamMark());
        CleanPointRecord record = new CleanPointRecord()
                .setId(idGenerator.nextId())
                .setCleanTable(cleanTable)
                .setRawId(raw.getId())
                .setMessageId(raw.getMessageId())
                .setProtocolVersion(raw.getProtocolVersion())
                .setTenantMark(raw.getTenantMark())
                .setModelMark(raw.getModelMark())
                .setDeviceMark(raw.getDeviceMark())
                .setParamMark(raw.getParamMark())
                .setRawValue(raw.getRawValue())
                .setDeviceTime(raw.getDeviceTime())
                .setReceiveTime(raw.getReceiveTime())
                .setNormalSecond(raw.getNormalSecond())
                .setCreatedTime(Instant.now(clock))
                .setQualityCode(CleanQualityCode.NORMAL)
                .setEffective(true)
                .setCleanRule("normal");
        BigDecimal parsedValue;
        try {
            parsedValue = parseRawValue(raw.getRawValue());
        } catch (IllegalArgumentException e) {
            return record.setQualityCode(CleanQualityCode.FORMAT_ERROR).setEffective(false).setCleanRule("parse");
        }
        if (isInvalidTime(raw.getDeviceTime())) {
            return record.setQualityCode(CleanQualityCode.TIME_ERROR).setEffective(false).setCleanRule("time");
        }
        if (config.getMinValue() != null && parsedValue.compareTo(config.getMinValue()) < 0
                || config.getMaxValue() != null && parsedValue.compareTo(config.getMaxValue()) > 0) {
            return record.setQualityCode(CleanQualityCode.OUT_OF_RANGE).setEffective(false).setCleanRule("range");
        }
        BigDecimal previousCheckValue = previousCheckValue(record);
        if (previousCheckValue != null) {
            if (config.isCumulative() && parsedValue.compareTo(previousCheckValue) < 0) {
                if (isRollover(config, previousCheckValue, parsedValue)) {
                    record.setQualityCode(CleanQualityCode.ROLLOVER).setEffective(true).setCleanRule("rollover");
                    return applyFormula(record, config, parsedValue);
                }
                return record.setQualityCode(CleanQualityCode.CUMULATIVE_ROLLBACK)
                        .setEffective(false)
                        .setCleanRule("cumulative_rollback");
            }
            if (config.getMaxDelta() != null && parsedValue.subtract(previousCheckValue).abs().compareTo(config.getMaxDelta()) > 0) {
                return record.setQualityCode(CleanQualityCode.SPIKE).setEffective(false).setCleanRule("delta");
            }
        }
        return applyFormula(record, config, parsedValue);
    }

    private CleanPointRecord applyFormula(CleanPointRecord record, CleanPointConfig config, BigDecimal parsedValue) {
        try {
            return record.setCleanValue(formulaEvaluator.evaluate(config.getTransformFormula(), parsedValue));
        } catch (Exception e) {
            return record.setQualityCode(CleanQualityCode.FORMULA_ERROR).setEffective(false).setCleanRule("formula");
        }
    }

    private BigDecimal previousCheckValue(CleanPointRecord record) {
        String previousRawValue = repository.findPreviousEffectiveRawValue(record.getCleanTable(), record);
        return previousRawValue == null ? null : parseRawValue(previousRawValue);
    }

    private static BigDecimal parseRawValue(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("raw value is null");
        }
        String normalized = rawValue.trim().replace(",", "");
        if (normalized.isEmpty()
                || "--".equals(normalized)
                || "null".equalsIgnoreCase(normalized)
                || "nan".equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("raw value is empty");
        }
        return new BigDecimal(normalized);
    }

    private static boolean isInvalidTime(Instant deviceTime) {
        return deviceTime == null || deviceTime.isBefore(Instant.parse("2000-01-01T00:00:00Z"));
    }

    private static boolean isRollover(CleanPointConfig config, BigDecimal previous, BigDecimal current) {
        return config.isRolloverEnabled()
                && config.getRolloverMaxValue() != null
                && config.getRolloverMinPreviousValue() != null
                && config.getRolloverMaxCurrentValue() != null
                && previous.compareTo(config.getRolloverMinPreviousValue()) >= 0
                && current.compareTo(config.getRolloverMaxCurrentValue()) <= 0;
    }

    private static String duplicateKey(CleanPointRecord record) {
        return record.getTenantMark() + "|" + record.getDeviceMark() + "|" + record.getParamMark()
                + "|" + record.getNormalSecond() + "|" + record.getCleanValue();
    }

    private static AggregateEvent toAggregateEvent(CleanPointRecord cleanRecord) {
        return new AggregateEvent()
                .setCleanTable(cleanRecord.getCleanTable())
                .setCleanId(cleanRecord.getId())
                .setMessageId(cleanRecord.getMessageId())
                .setTenantMark(cleanRecord.getTenantMark())
                .setModelMark(cleanRecord.getModelMark())
                .setDeviceMark(cleanRecord.getDeviceMark())
                .setParamMark(cleanRecord.getParamMark())
                .setNormalSecond(cleanRecord.getNormalSecond());
    }
}
