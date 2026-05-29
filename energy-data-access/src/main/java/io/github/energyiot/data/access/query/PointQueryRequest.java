package io.github.energyiot.data.access.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PointQueryRequest {
    private String tenantMark;
    private String modelMark;
    private List<String> modelMarks = new ArrayList<>();
    private List<String> deviceMarks = new ArrayList<>();
    private List<String> paramMarks = new ArrayList<>();
    private List<PointKey> points = new ArrayList<>();
    private List<Integer> qualityCodes = new ArrayList<>();
    private Boolean effectiveOnly;
    private String granularity;
    private Instant startTime;
    private Instant endTime;
    private int pageNo = 1;
    private int pageSize = 100;

    public String getTenantMark() { return tenantMark; }
    public void setTenantMark(String tenantMark) { this.tenantMark = tenantMark; }
    public String getModelMark() { return modelMark; }
    public void setModelMark(String modelMark) { this.modelMark = modelMark; }
    public List<String> getModelMarks() { return modelMarks; }
    public void setModelMarks(List<String> modelMarks) { this.modelMarks = modelMarks; }
    public List<String> getDeviceMarks() { return deviceMarks; }
    public void setDeviceMarks(List<String> deviceMarks) { this.deviceMarks = deviceMarks; }
    public List<String> getParamMarks() { return paramMarks; }
    public void setParamMarks(List<String> paramMarks) { this.paramMarks = paramMarks; }
    public List<PointKey> getPoints() { return points; }
    public void setPoints(List<PointKey> points) { this.points = points; }
    public List<Integer> getQualityCodes() { return qualityCodes; }
    public void setQualityCodes(List<Integer> qualityCodes) { this.qualityCodes = qualityCodes; }
    public Boolean getEffectiveOnly() { return effectiveOnly; }
    public void setEffectiveOnly(Boolean effectiveOnly) { this.effectiveOnly = effectiveOnly; }
    public String getGranularity() { return granularity; }
    public void setGranularity(String granularity) { this.granularity = granularity; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public static class PointKey {
        private String modelMark;
        private String deviceMark;
        private String paramMark;

        public String getModelMark() { return modelMark; }
        public void setModelMark(String modelMark) { this.modelMark = modelMark; }
        public String getDeviceMark() { return deviceMark; }
        public void setDeviceMark(String deviceMark) { this.deviceMark = deviceMark; }
        public String getParamMark() { return paramMark; }
        public void setParamMark(String paramMark) { this.paramMark = paramMark; }
    }
}
