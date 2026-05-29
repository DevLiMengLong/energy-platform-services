package io.github.energyiot.data.access.cleaning;

import java.math.BigDecimal;

public class CleanPointConfig {

    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private String transformFormula = "x";
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal maxDelta;
    private boolean cumulative;
    private boolean rolloverEnabled;
    private BigDecimal rolloverMaxValue;
    private BigDecimal rolloverMinPreviousValue;
    private BigDecimal rolloverMaxCurrentValue;

    public String getTenantMark() {
        return tenantMark;
    }

    public CleanPointConfig setTenantMark(String tenantMark) {
        this.tenantMark = tenantMark;
        return this;
    }

    public String getModelMark() {
        return modelMark;
    }

    public CleanPointConfig setModelMark(String modelMark) {
        this.modelMark = modelMark;
        return this;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public CleanPointConfig setDeviceMark(String deviceMark) {
        this.deviceMark = deviceMark;
        return this;
    }

    public String getParamMark() {
        return paramMark;
    }

    public CleanPointConfig setParamMark(String paramMark) {
        this.paramMark = paramMark;
        return this;
    }

    public String getTransformFormula() {
        return transformFormula == null || transformFormula.trim().isEmpty() ? "x" : transformFormula;
    }

    public CleanPointConfig setTransformFormula(String transformFormula) {
        this.transformFormula = transformFormula;
        return this;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public CleanPointConfig setMinValue(String minValue) {
        this.minValue = toDecimal(minValue);
        return this;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public CleanPointConfig setMaxValue(String maxValue) {
        this.maxValue = toDecimal(maxValue);
        return this;
    }

    public BigDecimal getMaxDelta() {
        return maxDelta;
    }

    public CleanPointConfig setMaxDelta(String maxDelta) {
        this.maxDelta = toDecimal(maxDelta);
        return this;
    }

    public boolean isCumulative() {
        return cumulative;
    }

    public CleanPointConfig setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
        return this;
    }

    public boolean isRolloverEnabled() {
        return rolloverEnabled;
    }

    public CleanPointConfig setRolloverEnabled(boolean rolloverEnabled) {
        this.rolloverEnabled = rolloverEnabled;
        return this;
    }

    public BigDecimal getRolloverMaxValue() {
        return rolloverMaxValue;
    }

    public CleanPointConfig setRolloverMaxValue(String rolloverMaxValue) {
        this.rolloverMaxValue = toDecimal(rolloverMaxValue);
        return this;
    }

    public BigDecimal getRolloverMinPreviousValue() {
        return rolloverMinPreviousValue;
    }

    public CleanPointConfig setRolloverMinPreviousValue(String rolloverMinPreviousValue) {
        this.rolloverMinPreviousValue = toDecimal(rolloverMinPreviousValue);
        return this;
    }

    public BigDecimal getRolloverMaxCurrentValue() {
        return rolloverMaxCurrentValue;
    }

    public CleanPointConfig setRolloverMaxCurrentValue(String rolloverMaxCurrentValue) {
        this.rolloverMaxCurrentValue = toDecimal(rolloverMaxCurrentValue);
        return this;
    }

    private static BigDecimal toDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }
}
