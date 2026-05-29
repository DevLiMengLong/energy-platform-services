package io.github.energyiot.data.access.raw;

import java.time.Instant;

public class RawPointRecord {

    private String id;

    private String messageId;

    private String protocolVersion;

    private String tenantMark;

    private String modelMark;

    private String deviceMark;

    private String paramMark;

    private String rawValue;

    private Instant deviceTime;

    private Instant receiveTime;

    private long normalSecond;

    private Instant createdTime;

    public String getId() {
        return id;
    }

    public RawPointRecord setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public RawPointRecord setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public RawPointRecord setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public RawPointRecord setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public RawPointRecord setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public RawPointRecord setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getParamMark() {
        return paramMark;
    }

    public RawPointRecord setParamMark(String paramMark) {
        this.paramMark = paramMark;
        return this;
    }

    public String getRawValue() {
        return rawValue;
    }

    public RawPointRecord setRawValue(String rawValue) {
        this.rawValue = rawValue;
        return this;
    }

    public Instant getDeviceTime() {
        return deviceTime;
    }

    public RawPointRecord setDeviceTime(Instant deviceTime) {
        this.deviceTime = deviceTime;
        return this;
    }

    public Instant getReceiveTime() {
        return receiveTime;
    }

    public RawPointRecord setReceiveTime(Instant receiveTime) {
        this.receiveTime = receiveTime;
        return this;
    }

    public long getNormalSecond() {
        return normalSecond;
    }

    public RawPointRecord setNormalSecond(long normalSecond) {
        this.normalSecond = normalSecond;
        return this;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public RawPointRecord setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
        return this;
    }
}
