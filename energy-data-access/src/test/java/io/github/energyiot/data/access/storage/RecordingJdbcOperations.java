package io.github.energyiot.data.access.storage;

import java.util.ArrayList;
import java.util.List;

class RecordingJdbcOperations implements RawPointJdbcOperations {

    private final List<String> executedSql = new ArrayList<>();

    private String batchSql;

    private List<Object[]> batchArgs = new ArrayList<>();

    @Override
    public void execute(String sql) {
        executedSql.add(sql);
    }

    @Override
    public void batchUpdate(String sql, List<Object[]> args) {
        this.batchSql = sql;
        this.batchArgs = args;
    }

    List<String> getExecutedSql() {
        return executedSql;
    }

    String getBatchSql() {
        return batchSql;
    }

    List<Object[]> getBatchArgs() {
        return batchArgs;
    }
}
