package io.github.energyiot.data.access.ingress;

public interface KafkaSender {

    void send(String topic, String key, String value);
}
