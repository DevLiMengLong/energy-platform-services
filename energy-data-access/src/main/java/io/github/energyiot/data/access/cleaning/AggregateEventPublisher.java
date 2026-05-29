package io.github.energyiot.data.access.cleaning;

public interface AggregateEventPublisher {

    void publish(AggregateEvent event);
}
