package io.github.energyiot.data.access.aggregation;

import java.math.BigDecimal;
import java.time.Instant;

public class AggregatePointRecord {

    private String id;
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private Instant windowStart;
    private Instant windowEnd;
    private BigDecimal startValue;
    private BigDecimal endValue;
    private BigDecimal usageValue;
    private BigDecimal sumValue;
    private BigDecimal avgValue;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private long sampleCount;
    private long sourceCount;
    private long rolloverCount;
    private int qualityLevel;
    private long version;
    private Instant createdTime;
    private Instant updatedTime;

    public String getId() {
        return id;
    }

    public AggregatePointRecord setId(String id) {
        this.id = id;
        return this;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public AggregatePointRecord setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public AggregatePointRecord setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public AggregatePointRecord setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getParamMark() {
        return paramMark;
    }

    public AggregatePointRecord setParamMark(String paramMark) {
        this.paramMark = paramMark;
        return this;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public AggregatePointRecord setWindowStart(Instant windowStart) {
        this.windowStart = windowStart;
        return this;
    }

    public Instant getWindowEnd() {
        return windowEnd;
    }

    public AggregatePointRecord setWindowEnd(Instant windowEnd) {
        this.windowEnd = windowEnd;
        return this;
    }

    public BigDecimal getStartValue() {
        return startValue;
    }

    public AggregatePointRecord setStartValue(BigDecimal startValue) {
        this.startValue = startValue;
        return this;
    }

    public BigDecimal getEndValue() {
        return endValue;
    }

    public AggregatePointRecord setEndValue(BigDecimal endValue) {
        this.endValue = endValue;
        return this;
    }

    public BigDecimal getUsageValue() {
        return usageValue;
    }

    public AggregatePointRecord setUsageValue(BigDecimal usageValue) {
        this.usageValue = usageValue;
        return this;
    }

    public BigDecimal getSumValue() {
        return sumValue;
    }

    public AggregatePointRecord setSumValue(BigDecimal sumValue) {
        this.sumValue = sumValue;
        return this;
    }

    public BigDecimal getAvgValue() {
        return avgValue;
    }

    public AggregatePointRecord setAvgValue(BigDecimal avgValue) {
        this.avgValue = avgValue;
        return this;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public AggregatePointRecord setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
        return this;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public AggregatePointRecord setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public AggregatePointRecord setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public long getSourceCount() {
        return sourceCount;
    }

    public AggregatePointRecord setSourceCount(long sourceCount) {
        this.sourceCount = sourceCount;
        return this;
    }

    public long getRolloverCount() {
        return rolloverCount;
    }

    public AggregatePointRecord setRolloverCount(long rolloverCount) {
        this.rolloverCount = rolloverCount;
        return this;
    }

    public int getQualityLevel() {
        return qualityLevel;
    }

    public AggregatePointRecord setQualityLevel(int qualityLevel) {
        this.qualityLevel = qualityLevel;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public AggregatePointRecord setVersion(long version) {
        this.version = version;
        return this;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public AggregatePointRecord setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public AggregatePointRecord setUpdatedTime(Instant updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }
}
