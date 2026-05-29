package io.github.energyiot.data.access.aggregation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AggregateRecomputeRequest {

    private String tenantMark;
    private String modelMark;
    private List<String> deviceMarks = new ArrayList<>();
    private List<String> paramMarks = new ArrayList<>();
    private Instant startTime;
    private Instant endTime;
    private String reason;

    public String getTenantMark() {
        return tenantMark;
    }

    public void setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
    }

    public String getModelMark() {
        return modelMark;
    }

    public void setModelMark(String modelMark) {
        this.modelMark = modelMark;
    }

    public List<String> getDeviceMarks() {
        return deviceMarks;
    }

    public void setDeviceMarks(List<String> deviceMarks) {
        this.deviceMarks = deviceMarks;
    }

    public List<String> getParamMarks() {
        return paramMarks;
    }

    public void setParamMarks(List<String> paramMarks) {
        this.paramMarks = paramMarks;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
