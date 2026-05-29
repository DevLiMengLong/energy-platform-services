package io.github.energyiot.data.access.aggregation;

public class AggregateSourceKey {

    private final String tenantMark;
    private final String modelMark;
    private final String deviceMark;
    private final String paramMark;

    public AggregateSourceKey(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        this.tenantMark = tenantMark;
        this.modelMark = modelMark;
        this.deviceMark = deviceMark;
        this.paramMark = paramMark;
    }

    public String getTenantMark() {
        return tenantMark;
    }

    public String getModelMark() {
        return modelMark;
    }

    public String getDeviceMark() {
        return deviceMark;
    }

    public String getParamMark() {
        return paramMark;
    }
}
