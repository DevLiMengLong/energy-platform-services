package io.github.energyiot.data.access.storage;

import io.github.energyiot.data.access.raw.RawPointRecord;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcRawPointStorage implements RawPointStorage {

    private final RawPointJdbcOperations jdbcOperations;

    private final RawPointTableNameResolver tableNameResolver;

    private final RawPointDdlFactory ddlFactory;

    private final RawPointInsertFactory insertFactory;

    private final boolean autoCreateTable;

    private final Set<String> initializedTables = Collections.synchronizedSet(new HashSet<>());

    public JdbcRawPointStorage(RawPointJdbcOperations jdbcOperations,
                               RawPointTableNameResolver tableNameResolver,
                               RawPointDdlFactory ddlFactory,
                               RawPointInsertFactory insertFactory,
                               boolean autoCreateTable) {
        this.jdbcOperations = jdbcOperations;
        this.tableNameResolver = tableNameResolver;
        this.ddlFactory = ddlFactory;
        this.insertFactory = insertFactory;
        this.autoCreateTable = autoCreateTable;
    }

    @Override
    public RawPointWriteResult save(List<RawPointRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            throw new IllegalArgumentException("records must not be empty");
        }
        RawPointRecord first = records.get(0);
        String tableName = tableNameResolver.resolve(first.getTenantMark(), first.getModelMark());
        ensureTable(tableName);
        jdbcOperations.batchUpdate(insertFactory.createInsertSql(tableName), toArgs(records));
        return new RawPointWriteResult(tableName, new ArrayList<>(records));
    }

    private void ensureTable(String tableName) {
        if (!autoCreateTable || initializedTables.contains(tableName)) {
            return;
        }
        synchronized (initializedTables) {
            if (!initializedTables.contains(tableName)) {
                jdbcOperations.execute(ddlFactory.createTableSql(tableName));
                initializedTables.add(tableName);
            }
        }
    }

    private static List<Object[]> toArgs(List<RawPointRecord> records) {
        List<Object[]> args = new ArrayList<>();
        for (RawPointRecord record : records) {
            args.add(new Object[]{
                    record.getId(),
                    record.getMessageId(),
                    record.getProtocolVersion(),
                    record.getTenantMark(),
                    record.getModelMark(),
                    record.getDeviceMark(),
                    record.getParamMark(),
                    record.getRawValue(),
                    Timestamp.from(record.getDeviceTime()),
                    Timestamp.from(record.getReceiveTime()),
                    record.getNormalSecond(),
                    Timestamp.from(record.getCreatedTime())
            });
        }
        return args;
    }
}
