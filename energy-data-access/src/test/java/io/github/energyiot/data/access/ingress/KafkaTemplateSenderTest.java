package io.github.energyiot.data.access.ingress;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KafkaTemplateSenderTest {

    @Test
    void sendShouldWaitForKafkaAck() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<SendResult<String, String>>();
        future.set(null);
        when(kafkaTemplate.send("topic", "key", "value")).thenReturn(future);

        new KafkaTemplateSender(kafkaTemplate).send("topic", "key", "value");
    }

    @Test
    void sendShouldFailWhenKafkaAckFails() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<SendResult<String, String>>();
        future.setException(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send("topic", "key", "value")).thenReturn(future);

        assertThatThrownBy(() -> new KafkaTemplateSender(kafkaTemplate).send("topic", "key", "value"))
                .isInstanceOf(IllegalStateException.class);
    }
}
