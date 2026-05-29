package io.github.energyiot.data.access.cleaning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.ingress.KafkaSender;

public class KafkaCleaningEventPublisher implements CleaningEventPublisher {

    private final KafkaSender kafkaSender;

    private final ObjectMapper objectMapper;

    private final String topic;

    public KafkaCleaningEventPublisher(KafkaSender kafkaSender, ObjectMapper objectMapper, String topic) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(CleaningEvent event) {
        try {
            String key = event.getTenantMark() + "|" + event.getModelMark() + "|" + event.getDeviceMark();
            kafkaSender.send(topic, key, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize cleaning event", e);
        }
    }
}
