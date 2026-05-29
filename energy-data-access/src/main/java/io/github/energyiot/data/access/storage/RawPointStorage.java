package io.github.energyiot.data.access.storage;

import io.github.energyiot.data.access.raw.RawPointRecord;

import java.util.List;

public interface RawPointStorage {

    RawPointWriteResult save(List<RawPointRecord> records);
}
