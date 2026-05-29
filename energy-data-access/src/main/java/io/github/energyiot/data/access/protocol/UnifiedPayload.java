package io.github.energyiot.data.access.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

public class UnifiedPayload {

    private String protocolVersion;

    private String tenantMark;

    private String modelMark;

    private String deviceMark;

    private String messageId;

    private String timestamp;

    private Map<String, Object> data = new LinkedHashMap<>();

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public UnifiedPayload setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public UnifiedPayload setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public UnifiedPayload setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public UnifiedPayload setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public UnifiedPayload setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public UnifiedPayload setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public UnifiedPayload setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }
}
