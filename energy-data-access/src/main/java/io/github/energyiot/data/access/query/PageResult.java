package io.github.energyiot.data.access.query;

import java.util.List;
import java.util.Map;

public class PageResult {
    private long total;
    private List<Map<String, Object>> records;

    public PageResult(long total, List<Map<String, Object>> records) {
        this.total = total;
        this.records = records;
    }

    public long getTotal() { return total; }
    public List<Map<String, Object>> getRecords() { return records; }
}
