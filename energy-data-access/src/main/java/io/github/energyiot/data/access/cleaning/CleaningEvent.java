package io.github.energyiot.data.access.cleaning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CleaningEvent {

    private String tableName;

    private List<String> rawIds = new ArrayList<>();

    private String tenantMark;

    private String modelMark;

    private String deviceMark;

    private String messageId;

    public String getTableName() {
        return tableName;
    }

    public CleaningEvent setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<String> getRawIds() {
        return Collections.unmodifiableList(rawIds);
    }

    public CleaningEvent setRawIds(List<String> rawIds) {
        this.rawIds = new ArrayList<>(rawIds);
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public CleaningEvent setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public CleaningEvent setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public CleaningEvent setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public CleaningEvent setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
}
