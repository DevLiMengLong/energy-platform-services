package io.github.energyiot.data.access.latest;

import io.github.energyiot.data.access.cleaning.CleanPointRecord;

import java.util.Optional;

public class NoopLatestCleanPointStore implements LatestCleanPointStore {
    @Override
    public void update(CleanPointRecord record) {
    }

    @Override
    public Optional<LatestCleanPoint> get(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        return Optional.empty();
    }
}
