package io.github.energyiot.data.access.ingress;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalRawKafkaPublisherTest {

    @Test
    void publishesUnifiedPayloadWithTenantModelDeviceKey() {
        RecordingKafkaSender sender = new RecordingKafkaSender();
        InternalRawKafkaPublisher publisher = new InternalRawKafkaPublisher(
                sender,
                new ObjectMapper(),
                "energy.raw.ingest"
        );
        UnifiedPayload payload = new UnifiedPayload()
                .setProtocolVersion("v2")
                .setTenantMark("tenantA")
                .setModelMark("electric_meter")
                .setDeviceMark("device001")
                .setMessageId("msg-001");
        payload.getData().put("kwh_total", "123.45");

        publisher.publish(payload);

        assertThat(sender.getTopic()).isEqualTo("energy.raw.ingest");
        assertThat(sender.getKey()).isEqualTo("tenantA|electric_meter|device001");
        assertThat(sender.getValue()).contains("\"tenantMark\":\"tenantA\"");
        assertThat(sender.getValue()).contains("\"deviceMark\":\"device001\"");
    }

    private static class RecordingKafkaSender implements KafkaSender {
        private String topic;
        private String key;
        private String value;

        @Override
        public void send(String topic, String key, String value) {
            this.topic = topic;
            this.key = key;
            this.value = value;
        }

        String getTopic() {
            return topic;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }
    }
}
