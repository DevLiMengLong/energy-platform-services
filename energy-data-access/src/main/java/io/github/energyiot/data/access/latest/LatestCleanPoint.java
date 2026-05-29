package io.github.energyiot.data.access.latest;

import java.math.BigDecimal;
import java.time.Instant;

public class LatestCleanPoint {
    private String cleanId;
    private String rawId;
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private BigDecimal cleanValue;
    private int qualityCode;
    private Instant deviceTime;
    private long normalSecond;

    public String getCleanId() { return cleanId; }
    public LatestCleanPoint setCleanId(String cleanId) { this.cleanId = cleanId; return this; }
    public String getRawId() { return rawId; }
    public LatestCleanPoint setRawId(String rawId) { this.rawId = rawId; return this; }
    public String getTenantMark() { return tenantMark; }
    public LatestCleanPoint setTenantMark(String tenantMark) { this.tenantMark = tenantMark; return this; }
    public String getModelMark() { return modelMark; }
    public LatestCleanPoint setModelMark(String modelMark) { this.modelMark = modelMark; return this; }
    public String getDeviceMark() { return deviceMark; }
    public LatestCleanPoint setDeviceMark(String deviceMark) { this.deviceMark = deviceMark; return this; }
    public String getParamMark() { return paramMark; }
    public LatestCleanPoint setParamMark(String paramMark) { this.paramMark = paramMark; return this; }
    public BigDecimal getCleanValue() { return cleanValue; }
    public LatestCleanPoint setCleanValue(BigDecimal cleanValue) { this.cleanValue = cleanValue; return this; }
    public int getQualityCode() { return qualityCode; }
    public LatestCleanPoint setQualityCode(int qualityCode) { this.qualityCode = qualityCode; return this; }
    public Instant getDeviceTime() { return deviceTime; }
    public LatestCleanPoint setDeviceTime(Instant deviceTime) { this.deviceTime = deviceTime; return this; }
    public long getNormalSecond() { return normalSecond; }
    public LatestCleanPoint setNormalSecond(long normalSecond) { this.normalSecond = normalSecond; return this; }
}
