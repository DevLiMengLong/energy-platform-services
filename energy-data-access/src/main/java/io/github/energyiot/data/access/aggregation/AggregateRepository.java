package io.github.energyiot.data.access.aggregation;

import io.github.energyiot.data.access.cleaning.CleanPointRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface AggregateRepository {

    List<String> listTables(String prefix);

    List<AggregateSourceKey> findCleanSourceKeys(String cleanTable, Instant windowStart, Instant windowEnd);

    List<CleanPointRecord> findCleanRecords(String cleanTable, AggregateSourceKey key, Instant windowStart, Instant windowEnd);

    BigDecimal findPreviousCleanValue(String cleanTable, AggregateSourceKey key, Instant windowStart);

    List<AggregateSourceKey> findAggregateSourceKeys(String aggregateTable, Instant windowStart, Instant windowEnd);

    List<AggregatePointRecord> findAggregateRecords(String aggregateTable,
                                                    AggregateSourceKey key,
                                                    Instant windowStart,
                                                    Instant windowEnd);

    void save(String aggregateTable, List<AggregatePointRecord> records);
}
