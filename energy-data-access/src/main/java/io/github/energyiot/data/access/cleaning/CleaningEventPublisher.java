package io.github.energyiot.data.access.cleaning;

public interface CleaningEventPublisher {

    void publish(CleaningEvent event);
}
