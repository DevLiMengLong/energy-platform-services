package io.github.energyiot.data.access.adapter.http;

import io.github.energyiot.data.access.ingress.InternalRawPublisher;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ingest")
public class HttpIngestController {

    private final InternalRawPublisher internalRawPublisher;

    public HttpIngestController(InternalRawPublisher internalRawPublisher) {
        this.internalRawPublisher = internalRawPublisher;
    }

    @PostMapping("/{protocolVersion}/{tenantMark}/{modelMark}/{deviceMark}/telemetry")
    public IngestResult ingest(@PathVariable String protocolVersion,
                               @PathVariable String tenantMark,
                               @PathVariable String modelMark,
                               @PathVariable String deviceMark,
                               @RequestBody UnifiedPayload body) {
        if (body.getMessageId() == null || body.getMessageId().trim().isEmpty()) {
            body.setMessageId(UUID.randomUUID().toString());
        }
        body.setProtocolVersion(protocolVersion)
                .setTenantMark(tenantMark)
                .setModelMark(modelMark)
                .setDeviceMark(deviceMark);
        internalRawPublisher.publish(body);
        return new IngestResult(true, body.getMessageId());
    }

    public static class IngestResult {
        private final boolean success;
        private final String messageId;

        public IngestResult(boolean success, String messageId) {
            this.success = success;
            this.messageId = messageId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessageId() {
            return messageId;
        }
    }
}
