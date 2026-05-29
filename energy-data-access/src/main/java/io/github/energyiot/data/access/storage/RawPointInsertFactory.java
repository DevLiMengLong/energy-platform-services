package io.github.energyiot.data.access.storage;

public interface RawPointInsertFactory {

    String createInsertSql(String tableName);
}
