package io.github.energyiot.data.access.cleaning;

public class AggregateEvent {

    private String cleanTable;
    private String cleanId;
    private String messageId;
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private long normalSecond;

    public String getCleanTable() {
        return cleanTable;
    }

    public AggregateEvent setCleanTable(String cleanTable) {
        this.cleanTable = cleanTable;
        return this;
    }

    public String getCleanId() {
        return cleanId;
    }

    public AggregateEvent setCleanId(String cleanId) {
        this.cleanId = cleanId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public AggregateEvent setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public AggregateEvent setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public AggregateEvent setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public AggregateEvent setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getParamMark() {
        return paramMark;
    }

    public AggregateEvent setParamMark(String paramMark) {
        this.paramMark = paramMark;
        return this;
    }

    public long getNormalSecond() {
        return normalSecond;
    }

    public AggregateEvent setNormalSecond(long normalSecond) {
        this.normalSecond = normalSecond;
        return this;
    }
}
