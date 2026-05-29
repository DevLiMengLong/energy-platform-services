package io.github.energyiot.data.access.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.ingress.InternalRawPublisher;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ExternalKafkaIngestListener {

    private final ObjectMapper objectMapper;

    private final InternalRawPublisher internalRawPublisher;

    public ExternalKafkaIngestListener(ObjectMapper objectMapper, InternalRawPublisher internalRawPublisher) {
        this.objectMapper = objectMapper;
        this.internalRawPublisher = internalRawPublisher;
    }

    @KafkaListener(topics = "${energy.access.external-kafka.topic}", groupId = "external-kafka-forwarder")
    public void onMessage(String value) throws Exception {
        internalRawPublisher.publish(objectMapper.readValue(value, UnifiedPayload.class));
    }
}
