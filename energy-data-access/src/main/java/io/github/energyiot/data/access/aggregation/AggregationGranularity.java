package io.github.energyiot.data.access.aggregation;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public enum AggregationGranularity {
    MINUTE("minute", "agg_minute_param", Duration.ofMinutes(1), 1),
    FIFTEEN_MINUTE("15min", "agg_15min_param", Duration.ofMinutes(15), 15),
    HOUR("hour", "agg_hour_param", Duration.ofHours(1), 4),
    DAY("day", "agg_day_param", Duration.ofDays(1), 24);

    private final String code;
    private final String tablePrefix;
    private final Duration duration;
    private final int expectedSourceCount;

    AggregationGranularity(String code, String tablePrefix, Duration duration, int expectedSourceCount) {
        this.code = code;
        this.tablePrefix = tablePrefix;
        this.duration = duration;
        this.expectedSourceCount = expectedSourceCount;
    }

    public String getCode() {
        return code;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getExpectedSourceCount() {
        return expectedSourceCount;
    }

    public Instant truncate(Instant instant) {
        ZonedDateTime time = instant.atZone(ZoneOffset.UTC);
        switch (this) {
            case MINUTE:
                return time.withSecond(0).withNano(0).toInstant();
            case FIFTEEN_MINUTE:
                return time.withMinute((time.getMinute() / 15) * 15).withSecond(0).withNano(0).toInstant();
            case HOUR:
                return time.withMinute(0).withSecond(0).withNano(0).toInstant();
            case DAY:
                return time.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
            default:
                throw new IllegalStateException("unsupported granularity " + this);
        }
    }

    public Instant nextWindowStart(Instant windowStart) {
        return windowStart.plus(duration);
    }

    public AggregationGranularity parent() {
        switch (this) {
            case MINUTE:
                return FIFTEEN_MINUTE;
            case FIFTEEN_MINUTE:
                return HOUR;
            case HOUR:
                return DAY;
            default:
                return null;
        }
    }

    public AggregationGranularity source() {
        switch (this) {
            case FIFTEEN_MINUTE:
                return MINUTE;
            case HOUR:
                return FIFTEEN_MINUTE;
            case DAY:
                return HOUR;
            default:
                return null;
        }
    }
}
