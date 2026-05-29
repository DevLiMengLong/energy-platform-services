package io.github.energyiot.data.access.cleaning;

import java.math.BigDecimal;
import java.time.Instant;

public class CleanPointRecord {

    private String id;
    private String cleanTable;
    private String rawId;
    private String messageId;
    private String protocolVersion;
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private String rawValue;
    private BigDecimal cleanValue;
    private Instant deviceTime;
    private Instant receiveTime;
    private long normalSecond;
    private int qualityCode;
    private boolean effective;
    private String duplicateOfId;
    private String cleanRule;
    private Instant createdTime;

    public String getId() {
        return id;
    }

    public CleanPointRecord setId(String id) {
        this.id = id;
        return this;
    }

    public String getCleanTable() {
        return cleanTable;
    }

    public CleanPointRecord setCleanTable(String cleanTable) {
        this.cleanTable = cleanTable;
        return this;
    }

    public String getRawId() {
        return rawId;
    }

    public CleanPointRecord setRawId(String rawId) {
        this.rawId = rawId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public CleanPointRecord setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public CleanPointRecord setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public CleanPointRecord setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public CleanPointRecord setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public CleanPointRecord setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getParamMark() {
        return paramMark;
    }

    public CleanPointRecord setParamMark(String paramMark) {
        this.paramMark = paramMark;
        return this;
    }

    public String getRawValue() {
        return rawValue;
    }

    public CleanPointRecord setRawValue(String rawValue) {
        this.rawValue = rawValue;
        return this;
    }

    public BigDecimal getCleanValue() {
        return cleanValue;
    }

    public CleanPointRecord setCleanValue(BigDecimal cleanValue) {
        this.cleanValue = cleanValue;
        return this;
    }

    public Instant getDeviceTime() {
        return deviceTime;
    }

    public CleanPointRecord setDeviceTime(Instant deviceTime) {
        this.deviceTime = deviceTime;
        return this;
    }

    public Instant getReceiveTime() {
        return receiveTime;
    }

    public CleanPointRecord setReceiveTime(Instant receiveTime) {
        this.receiveTime = receiveTime;
        return this;
    }

    public long getNormalSecond() {
        return normalSecond;
    }

    public CleanPointRecord setNormalSecond(long normalSecond) {
        this.normalSecond = normalSecond;
        return this;
    }

    public int getQualityCode() {
        return qualityCode;
    }

    public CleanPointRecord setQualityCode(int qualityCode) {
        this.qualityCode = qualityCode;
        return this;
    }

    public boolean isEffective() {
        return effective;
    }

    public CleanPointRecord setEffective(boolean effective) {
        this.effective = effective;
        return this;
    }

    public String getDuplicateOfId() {
        return duplicateOfId;
    }

    public CleanPointRecord setDuplicateOfId(String duplicateOfId) {
        this.duplicateOfId = duplicateOfId;
        return this;
    }

    public String getCleanRule() {
        return cleanRule;
    }

    public CleanPointRecord setCleanRule(String cleanRule) {
        this.cleanRule = cleanRule;
        return this;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public CleanPointRecord setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
        return this;
    }
}
