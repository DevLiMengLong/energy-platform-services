package io.github.energyiot.data.access.cleaning;

import io.github.energyiot.data.access.raw.RawPointRecord;

import java.util.List;

public interface CleanPointRepository {

    List<RawPointRecord> findRawRecords(String rawTable, List<String> rawIds);

    String findDuplicateEffectiveId(String cleanTable, CleanPointRecord record);

    String findPreviousEffectiveRawValue(String cleanTable, CleanPointRecord record);

    void save(String cleanTable, List<CleanPointRecord> records);
}
