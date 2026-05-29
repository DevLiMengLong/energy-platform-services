package io.github.energyiot.data.access.aggregation;

public final class AggregateQualityLevel {

    public static final int NORMAL = 0;
    public static final int INCOMPLETE = 1;
    public static final int HAS_ROLLOVER = 2;

    private AggregateQualityLevel() {
    }
}
