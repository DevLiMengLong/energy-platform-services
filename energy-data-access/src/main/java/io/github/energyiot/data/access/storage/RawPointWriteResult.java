package io.github.energyiot.data.access.storage;

import io.github.energyiot.data.access.raw.RawPointRecord;

import java.util.Collections;
import java.util.List;

public class RawPointWriteResult {

    private final String tableName;

    private final List<RawPointRecord> records;

    public RawPointWriteResult(String tableName, List<RawPointRecord> records) {
        this.tableName = tableName;
        this.records = Collections.unmodifiableList(records);
    }

    public String getTableName() {
        return tableName;
    }

    public List<RawPointRecord> getRecords() {
        return records;
    }
}
