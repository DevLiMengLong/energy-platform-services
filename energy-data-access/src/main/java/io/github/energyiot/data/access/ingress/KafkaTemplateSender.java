package io.github.energyiot.data.access.ingress;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

public class KafkaTemplateSender implements KafkaSender {

    private static final int SEND_ACK_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaTemplateSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, String key, String value) {
        try {
            kafkaTemplate.send(topic, key, value).get(SEND_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("failed to send kafka message to topic " + topic, e);
        }
    }
}
