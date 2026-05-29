package io.github.energyiot.data.access.aggregation;

import java.util.EnumMap;
import java.util.Map;

public class AggregateRecomputeResult {

    private final Map<AggregationGranularity, Integer> recordsByGranularity =
            new EnumMap<>(AggregationGranularity.class);

    public void add(AggregationGranularity granularity, int records) {
        recordsByGranularity.put(granularity, recordsByGranularity.getOrDefault(granularity, 0) + records);
    }

    public Map<AggregationGranularity, Integer> getRecordsByGranularity() {
        return recordsByGranularity;
    }
}
