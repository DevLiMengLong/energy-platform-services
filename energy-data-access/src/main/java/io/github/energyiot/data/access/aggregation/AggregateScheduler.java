package io.github.energyiot.data.access.aggregation;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;

public class AggregateScheduler {

    private final AggregateService aggregateService;
    private final Clock clock;
    private final long minuteDelaySeconds;
    private final long fifteenMinuteDelaySeconds;
    private final long hourDelaySeconds;
    private final long dayDelaySeconds;

    public AggregateScheduler(AggregateService aggregateService,
                              Clock clock,
                              long minuteDelaySeconds,
                              long fifteenMinuteDelaySeconds,
                              long hourDelaySeconds,
                              long dayDelaySeconds) {
        this.aggregateService = aggregateService;
        this.clock = clock;
        this.minuteDelaySeconds = minuteDelaySeconds;
        this.fifteenMinuteDelaySeconds = fifteenMinuteDelaySeconds;
        this.hourDelaySeconds = hourDelaySeconds;
        this.dayDelaySeconds = dayDelaySeconds;
    }

    @Scheduled(cron = "${energy.access.aggregation.minute-cron:30 * * * * *}")
    public void aggregateMinute() {
        aggregateService.aggregateClosedWindow(AggregationGranularity.MINUTE, Instant.now(clock), minuteDelaySeconds);
    }

    @Scheduled(cron = "${energy.access.aggregation.fifteen-minute-cron:0 3/15 * * * *}")
    public void aggregateFifteenMinute() {
        aggregateService.aggregateClosedWindow(AggregationGranularity.FIFTEEN_MINUTE, Instant.now(clock), fifteenMinuteDelaySeconds);
    }

    @Scheduled(cron = "${energy.access.aggregation.hour-cron:0 5 * * * *}")
    public void aggregateHour() {
        aggregateService.aggregateClosedWindow(AggregationGranularity.HOUR, Instant.now(clock), hourDelaySeconds);
    }

    @Scheduled(cron = "${energy.access.aggregation.day-cron:0 10 0 * * *}")
    public void aggregateDay() {
        aggregateService.aggregateClosedWindow(AggregationGranularity.DAY, Instant.now(clock), dayDelaySeconds);
    }
}
