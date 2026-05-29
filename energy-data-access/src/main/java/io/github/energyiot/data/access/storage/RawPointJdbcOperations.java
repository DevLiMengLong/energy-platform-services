package io.github.energyiot.data.access.storage;

import java.util.List;

public interface RawPointJdbcOperations {

    void execute(String sql);

    void batchUpdate(String sql, List<Object[]> args);
}
