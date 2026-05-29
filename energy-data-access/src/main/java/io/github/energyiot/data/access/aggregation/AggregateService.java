package io.github.energyiot.data.access.aggregation;

import io.github.energyiot.data.access.cleaning.CleanPointConfig;
import io.github.energyiot.data.access.cleaning.CleanPointConfigProvider;
import io.github.energyiot.data.access.cleaning.CleanPointRecord;
import io.github.energyiot.data.access.storage.RawPointTableNameResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AggregateService {

    private final AggregateRepository repository;
    private final AggregateCalculator calculator;
    private final CleanPointConfigProvider configProvider;
    private final String cleanTablePrefix;
    private final RawPointTableNameResolver cleanTableResolver;
    private final RawPointTableNameResolver minuteTableResolver;
    private final RawPointTableNameResolver fifteenMinuteTableResolver;
    private final RawPointTableNameResolver hourTableResolver;
    private final RawPointTableNameResolver dayTableResolver;

    public AggregateService(AggregateRepository repository,
                            AggregateCalculator calculator,
                            CleanPointConfigProvider configProvider,
                            String cleanTablePrefix) {
        this.repository = repository;
        this.calculator = calculator;
        this.configProvider = configProvider;
        this.cleanTablePrefix = cleanTablePrefix;
        this.cleanTableResolver = new RawPointTableNameResolver(cleanTablePrefix);
        this.minuteTableResolver = new RawPointTableNameResolver(AggregationGranularity.MINUTE.getTablePrefix());
        this.fifteenMinuteTableResolver = new RawPointTableNameResolver(AggregationGranularity.FIFTEEN_MINUTE.getTablePrefix());
        this.hourTableResolver = new RawPointTableNameResolver(AggregationGranularity.HOUR.getTablePrefix());
        this.dayTableResolver = new RawPointTableNameResolver(AggregationGranularity.DAY.getTablePrefix());
    }

    public int aggregateClosedWindow(AggregationGranularity granularity, Instant now, long delaySeconds) {
        Instant windowStart = granularity.truncate(now.minusSeconds(delaySeconds));
        return aggregateWindow(granularity, windowStart, null, null);
    }

    public AggregateRecomputeResult recompute(AggregateRecomputeRequest request) {
        validate(request);
        AggregateRecomputeResult result = new AggregateRecomputeResult();
        for (Instant windowStart = AggregationGranularity.MINUTE.truncate(request.getStartTime());
             windowStart.isBefore(request.getEndTime());
             windowStart = AggregationGranularity.MINUTE.nextWindowStart(windowStart)) {
            result.add(AggregationGranularity.MINUTE, aggregateWindow(AggregationGranularity.MINUTE, windowStart, request, null));
        }
        for (Instant windowStart = AggregationGranularity.FIFTEEN_MINUTE.truncate(request.getStartTime());
             windowStart.isBefore(request.getEndTime());
             windowStart = AggregationGranularity.FIFTEEN_MINUTE.nextWindowStart(windowStart)) {
            result.add(AggregationGranularity.FIFTEEN_MINUTE, aggregateWindow(AggregationGranularity.FIFTEEN_MINUTE, windowStart, request, null));
        }
        for (Instant windowStart = AggregationGranularity.HOUR.truncate(request.getStartTime());
             windowStart.isBefore(request.getEndTime());
             windowStart = AggregationGranularity.HOUR.nextWindowStart(windowStart)) {
            result.add(AggregationGranularity.HOUR, aggregateWindow(AggregationGranularity.HOUR, windowStart, request, null));
        }
        for (Instant windowStart = AggregationGranularity.DAY.truncate(request.getStartTime());
             windowStart.isBefore(request.getEndTime());
             windowStart = AggregationGranularity.DAY.nextWindowStart(windowStart)) {
            result.add(AggregationGranularity.DAY, aggregateWindow(AggregationGranularity.DAY, windowStart, request, null));
        }
        return result;
    }

    public int aggregateWindow(AggregationGranularity granularity,
                               Instant windowStart,
                               AggregateRecomputeRequest request,
                               List<String> sourceTablesOverride) {
        Instant windowEnd = granularity.nextWindowStart(windowStart);
        if (granularity == AggregationGranularity.MINUTE) {
            return aggregateMinute(windowStart, windowEnd, request, sourceTablesOverride);
        }
        return aggregateUpper(granularity, windowStart, windowEnd, request, sourceTablesOverride);
    }

    private int aggregateMinute(Instant windowStart,
                                Instant windowEnd,
                                AggregateRecomputeRequest request,
                                List<String> sourceTablesOverride) {
        List<String> cleanTables = sourceTablesOverride == null ? sourceTables(AggregationGranularity.MINUTE, request) : sourceTablesOverride;
        int saved = 0;
        for (String cleanTable : cleanTables) {
            List<AggregatePointRecord> aggregateRecords = new ArrayList<>();
            for (AggregateSourceKey key : repository.findCleanSourceKeys(cleanTable, windowStart, windowEnd)) {
                if (!matches(request, key)) {
                    continue;
                }
                List<CleanPointRecord> cleanRecords = repository.findCleanRecords(cleanTable, key, windowStart, windowEnd);
                CleanPointConfig config = configProvider.getConfig(key.getTenantMark(), key.getModelMark(), key.getDeviceMark(), key.getParamMark());
                BigDecimal previous = config.isCumulative() ? repository.findPreviousCleanValue(cleanTable, key, windowStart) : null;
                AggregatePointRecord aggregate = calculator.fromCleanRecords(key, windowStart, windowEnd, cleanRecords, previous, config);
                if (aggregate != null) {
                    aggregateRecords.add(aggregate);
                }
            }
            if (!aggregateRecords.isEmpty()) {
                repository.save(aggregateTable(AggregationGranularity.MINUTE, aggregateRecords.get(0)), aggregateRecords);
                saved += aggregateRecords.size();
            }
        }
        return saved;
    }

    private int aggregateUpper(AggregationGranularity granularity,
                               Instant windowStart,
                               Instant windowEnd,
                               AggregateRecomputeRequest request,
                               List<String> sourceTablesOverride) {
        List<String> sourceTables = sourceTablesOverride == null ? sourceTables(granularity, request) : sourceTablesOverride;
        int saved = 0;
        for (String sourceTable : sourceTables) {
            List<AggregatePointRecord> aggregateRecords = new ArrayList<>();
            for (AggregateSourceKey key : repository.findAggregateSourceKeys(sourceTable, windowStart, windowEnd)) {
                if (!matches(request, key)) {
                    continue;
                }
                List<AggregatePointRecord> sourceRecords = repository.findAggregateRecords(sourceTable, key, windowStart, windowEnd);
                AggregatePointRecord aggregate = calculator.fromLowerAggregates(key, granularity, windowStart, windowEnd, sourceRecords);
                if (aggregate != null) {
                    aggregateRecords.add(aggregate);
                }
            }
            if (!aggregateRecords.isEmpty()) {
                repository.save(aggregateTable(granularity, aggregateRecords.get(0)), aggregateRecords);
                saved += aggregateRecords.size();
            }
        }
        return saved;
    }

    private List<String> sourceTables(AggregationGranularity granularity, AggregateRecomputeRequest request) {
        if (request != null && StringUtils.hasText(request.getTenantMark()) && StringUtils.hasText(request.getModelMark())) {
            List<String> tables = new ArrayList<>();
            if (granularity == AggregationGranularity.MINUTE) {
                tables.add(cleanTableResolver.resolve(request.getTenantMark(), request.getModelMark()));
            } else {
                tables.add(resolveTable(granularity.source(), request.getTenantMark(), request.getModelMark()));
            }
            return tables;
        }
        if (granularity == AggregationGranularity.MINUTE) {
            return repository.listTables(cleanTablePrefix);
        }
        return repository.listTables(granularity.source().getTablePrefix());
    }

    private String aggregateTable(AggregationGranularity granularity, AggregatePointRecord record) {
        return resolveTable(granularity, record.getTenantMark(), record.getModelMark());
    }

    private String resolveTable(AggregationGranularity granularity, String tenantMark, String modelMark) {
        switch (granularity) {
            case MINUTE:
                return minuteTableResolver.resolve(tenantMark, modelMark);
            case FIFTEEN_MINUTE:
                return fifteenMinuteTableResolver.resolve(tenantMark, modelMark);
            case HOUR:
                return hourTableResolver.resolve(tenantMark, modelMark);
            case DAY:
                return dayTableResolver.resolve(tenantMark, modelMark);
            default:
                throw new IllegalArgumentException("unsupported granularity " + granularity);
        }
    }

    private static boolean matches(AggregateRecomputeRequest request, AggregateSourceKey key) {
        if (request == null) {
            return true;
        }
        if (!CollectionUtils.isEmpty(request.getDeviceMarks()) && !request.getDeviceMarks().contains(key.getDeviceMark())) {
            return false;
        }
        return CollectionUtils.isEmpty(request.getParamMarks()) || request.getParamMarks().contains(key.getParamMark());
    }

    private static void validate(AggregateRecomputeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (!StringUtils.hasText(request.getTenantMark())) {
            throw new IllegalArgumentException("tenantMark must not be blank");
        }
        if (!StringUtils.hasText(request.getModelMark())) {
            throw new IllegalArgumentException("modelMark must not be blank");
        }
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
