package io.github.energyiot.data.access.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.cleaning.CleanPointProcessor;
import io.github.energyiot.data.access.cleaning.CleaningEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CleanPendingEventListener {

    private final ObjectMapper objectMapper;
    private final CleanPointProcessor cleanPointProcessor;

    public CleanPendingEventListener(ObjectMapper objectMapper, CleanPointProcessor cleanPointProcessor) {
        this.objectMapper = objectMapper;
        this.cleanPointProcessor = cleanPointProcessor;
    }

    @KafkaListener(topics = "${energy.access.clean-pending-topic}", groupId = "clean-point-writer")
    public void listen(String value) throws Exception {
        CleaningEvent event = objectMapper.readValue(value, CleaningEvent.class);
        cleanPointProcessor.process(event);
    }
}
