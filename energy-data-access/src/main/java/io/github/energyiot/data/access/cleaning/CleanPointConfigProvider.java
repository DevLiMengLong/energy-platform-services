package io.github.energyiot.data.access.cleaning;

public interface CleanPointConfigProvider {

    CleanPointConfig getConfig(String tenantMark, String modelMark, String deviceMark, String paramMark);

    default void reload() {
    }
}
