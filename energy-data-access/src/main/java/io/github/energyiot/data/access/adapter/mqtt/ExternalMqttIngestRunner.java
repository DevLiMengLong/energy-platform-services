package io.github.energyiot.data.access.adapter.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.config.EnergyAccessProperties;
import io.github.energyiot.data.access.ingress.InternalRawPublisher;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ExternalMqttIngestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExternalMqttIngestRunner.class);

    private final EnergyAccessProperties properties;

    private final ObjectMapper objectMapper;

    private final InternalRawPublisher internalRawPublisher;

    private MqttClient client;

    public ExternalMqttIngestRunner(EnergyAccessProperties properties,
                                    ObjectMapper objectMapper,
                                    InternalRawPublisher internalRawPublisher) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.internalRawPublisher = internalRawPublisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EnergyAccessProperties.ExternalMqtt mqtt = properties.getExternalMqtt();
        if (!mqtt.isEnabled()) {
            log.info("external mqtt ingest is disabled");
            return;
        }
        this.client = new MqttClient(mqtt.getBrokerUrl(), mqtt.getClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        this.client.connect(options);
        this.client.subscribe(mqtt.getTopic(), new IngestMessageListener());
        log.info("external mqtt ingest subscribed topic {}", mqtt.getTopic());
    }

    private class IngestMessageListener implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            UnifiedPayload unifiedPayload = objectMapper.readValue(payload, UnifiedPayload.class);
            internalRawPublisher.publish(unifiedPayload);
            log.debug("external mqtt message forwarded topic {} messageId {}", topic, unifiedPayload.getMessageId());
        }
    }
}
