package io.github.energyiot.data.access.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import io.github.energyiot.data.access.service.RawPointIngestionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InternalRawIngestListener {

    private final ObjectMapper objectMapper;

    private final RawPointIngestionService ingestionService;

    public InternalRawIngestListener(ObjectMapper objectMapper, RawPointIngestionService ingestionService) {
        this.objectMapper = objectMapper;
        this.ingestionService = ingestionService;
    }

    @KafkaListener(topics = "${energy.access.internal-raw-topic}", groupId = "raw-point-writer")
    public void onMessage(String value) throws Exception {
        ingestionService.ingest(objectMapper.readValue(value, UnifiedPayload.class));
    }
}
