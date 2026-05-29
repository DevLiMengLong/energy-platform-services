package io.github.energyiot.data.access.latest;

import io.github.energyiot.data.access.cleaning.CleanPointRecord;

import java.util.Optional;

public interface LatestCleanPointStore {
    void update(CleanPointRecord record);
    Optional<LatestCleanPoint> get(String tenantMark, String modelMark, String deviceMark, String paramMark);
}
