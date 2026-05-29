package io.github.energyiot.data.access.configuration;

public class CleanPointConfigPageRequest {
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private String pointName;
    private String configStatus;
    private Boolean cumulative;
    private int pageNo = 1;
    private int pageSize = 10;

    public String getTenantMark() { return tenantMark; }
    public void setTenantMark(String tenantMark) { this.tenantMark = tenantMark; }
    public String getModelMark() { return modelMark; }
    public void setModelMark(String modelMark) { this.modelMark = modelMark; }
    public String getDeviceMark() { return deviceMark; }
    public void setDeviceMark(String deviceMark) { this.deviceMark = deviceMark; }
    public String getParamMark() { return paramMark; }
    public void setParamMark(String paramMark) { this.paramMark = paramMark; }
    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }
    public String getConfigStatus() { return configStatus; }
    public void setConfigStatus(String configStatus) { this.configStatus = configStatus; }
    public Boolean getCumulative() { return cumulative; }
    public void setCumulative(Boolean cumulative) { this.cumulative = cumulative; }
    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
