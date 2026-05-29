package io.github.energyiot.data.access.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanExportRequest {
    private String tenantMark;
    private String modelMark;
    private List<PointQueryRequest.PointKey> points = new ArrayList<>();
    private List<Integer> qualityCodes = new ArrayList<>();
    private Instant startTime;
    private Instant endTime;
    private List<Map<String, Object>> rows = new ArrayList<>();

    public String getTenantMark() { return tenantMark; }
    public void setTenantMark(String tenantMark) { this.tenantMark = tenantMark; }
    public String getModelMark() { return modelMark; }
    public void setModelMark(String modelMark) { this.modelMark = modelMark; }
    public List<PointQueryRequest.PointKey> getPoints() { return points; }
    public void setPoints(List<PointQueryRequest.PointKey> points) { this.points = points; }
    public List<Integer> getQualityCodes() { return qualityCodes; }
    public void setQualityCodes(List<Integer> qualityCodes) { this.qualityCodes = qualityCodes; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}
