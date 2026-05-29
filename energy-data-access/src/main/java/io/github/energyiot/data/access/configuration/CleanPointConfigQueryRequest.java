package io.github.energyiot.data.access.configuration;

public class CleanPointConfigQueryRequest {
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;

    public String getTenantMark() { return tenantMark; }
    public void setTenantMark(String tenantMark) { this.tenantMark = tenantMark; }
    public String getModelMark() { return modelMark; }
    public void setModelMark(String modelMark) { this.modelMark = modelMark; }
    public String getDeviceMark() { return deviceMark; }
    public void setDeviceMark(String deviceMark) { this.deviceMark = deviceMark; }
    public String getParamMark() { return paramMark; }
    public void setParamMark(String paramMark) { this.paramMark = paramMark; }
}
