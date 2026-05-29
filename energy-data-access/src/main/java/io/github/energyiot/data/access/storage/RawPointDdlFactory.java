package io.github.energyiot.data.access.storage;

public interface RawPointDdlFactory {

    String createTableSql(String tableName);
}
