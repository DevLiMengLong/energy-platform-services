package io.github.energyiot.data.access.configuration;

public class CleanPointConfigSaveRequest {
    private String tenantMark;
    private String modelMark;
    private String deviceMark;
    private String paramMark;
    private String transformFormula = "x";
    private String minValue;
    private String maxValue;
    private String maxDelta;
    private boolean cumulative;
    private boolean rolloverEnabled;
    private String rolloverMaxValue;
    private String rolloverMinPreviousValue;
    private String rolloverMaxCurrentValue;
    private boolean enabled = true;

    public String getTenantMark() { return tenantMark; }
    public void setTenantMark(String tenantMark) { this.tenantMark = tenantMark; }
    public String getModelMark() { return modelMark; }
    public void setModelMark(String modelMark) { this.modelMark = modelMark; }
    public String getDeviceMark() { return deviceMark; }
    public void setDeviceMark(String deviceMark) { this.deviceMark = deviceMark; }
    public String getParamMark() { return paramMark; }
    public void setParamMark(String paramMark) { this.paramMark = paramMark; }
    public String getTransformFormula() { return transformFormula; }
    public void setTransformFormula(String transformFormula) { this.transformFormula = transformFormula; }
    public String getMinValue() { return minValue; }
    public void setMinValue(String minValue) { this.minValue = minValue; }
    public String getMaxValue() { return maxValue; }
    public void setMaxValue(String maxValue) { this.maxValue = maxValue; }
    public String getMaxDelta() { return maxDelta; }
    public void setMaxDelta(String maxDelta) { this.maxDelta = maxDelta; }
    public boolean isCumulative() { return cumulative; }
    public void setCumulative(boolean cumulative) { this.cumulative = cumulative; }
    public boolean isRolloverEnabled() { return rolloverEnabled; }
    public void setRolloverEnabled(boolean rolloverEnabled) { this.rolloverEnabled = rolloverEnabled; }
    public String getRolloverMaxValue() { return rolloverMaxValue; }
    public void setRolloverMaxValue(String rolloverMaxValue) { this.rolloverMaxValue = rolloverMaxValue; }
    public String getRolloverMinPreviousValue() { return rolloverMinPreviousValue; }
    public void setRolloverMinPreviousValue(String rolloverMinPreviousValue) { this.rolloverMinPreviousValue = rolloverMinPreviousValue; }
    public String getRolloverMaxCurrentValue() { return rolloverMaxCurrentValue; }
    public void setRolloverMaxCurrentValue(String rolloverMaxCurrentValue) { this.rolloverMaxCurrentValue = rolloverMaxCurrentValue; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
