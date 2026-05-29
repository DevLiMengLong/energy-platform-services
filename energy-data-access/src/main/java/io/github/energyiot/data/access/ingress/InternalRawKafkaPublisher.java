package io.github.energyiot.data.access.ingress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import io.github.energyiot.data.access.protocol.UnifiedPayloadDecoder;

public class InternalRawKafkaPublisher implements InternalRawPublisher {

    private final KafkaSender kafkaSender;

    private final ObjectMapper objectMapper;

    private final String topic;

    public InternalRawKafkaPublisher(KafkaSender kafkaSender, ObjectMapper objectMapper, String topic) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(UnifiedPayload payload) {
        try {
            kafkaSender.send(topic, UnifiedPayloadDecoder.kafkaKey(payload), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize unified payload", e);
        }
    }
}
