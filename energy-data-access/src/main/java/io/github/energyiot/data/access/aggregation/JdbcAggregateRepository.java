package io.github.energyiot.data.access.aggregation;

import io.github.energyiot.data.access.cleaning.CleanPointRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcAggregateRepository implements AggregateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final boolean autoCreateTable;
    private final Set<String> initializedTables = Collections.synchronizedSet(new HashSet<>());
    private static final DateTimeFormatter CLICKHOUSE_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public JdbcAggregateRepository(JdbcTemplate jdbcTemplate, boolean autoCreateTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.autoCreateTable = autoCreateTable;
    }

    @Override
    public List<String> listTables(String prefix) {
        return jdbcTemplate.queryForList(
                "SELECT name FROM system.tables WHERE database = database() AND name LIKE ? ORDER BY name",
                String.class,
                prefix + "\\_%"
        );
    }

    @Override
    public List<AggregateSourceKey> findCleanSourceKeys(String cleanTable, Instant windowStart, Instant windowEnd) {
        String sql = "SELECT DISTINCT tenant_mark, model_mark, device_mark, param_mark FROM " + cleanTable
                + " WHERE effective_flag = 1 AND clean_value IS NOT NULL AND normal_second >= ? AND normal_second < ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapKey(rs), windowStart.getEpochSecond(), windowEnd.getEpochSecond());
    }

    @Override
    public List<CleanPointRecord> findCleanRecords(String cleanTable,
                                                   AggregateSourceKey key,
                                                   Instant windowStart,
                                                   Instant windowEnd) {
        String sql = "SELECT id, message_id, protocol_version, tenant_mark, model_mark, device_mark, param_mark, "
                + "raw_value, clean_value, device_time, receive_time, normal_second, quality_code, effective_flag, "
                + "duplicate_of_id, clean_rule, created_time FROM " + cleanTable
                + " WHERE effective_flag = 1 AND clean_value IS NOT NULL "
                + "AND tenant_mark = ? AND model_mark = ? AND device_mark = ? AND param_mark = ? "
                + "AND normal_second >= ? AND normal_second < ? ORDER BY normal_second, id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapClean(rs),
                key.getTenantMark(),
                key.getModelMark(),
                key.getDeviceMark(),
                key.getParamMark(),
                windowStart.getEpochSecond(),
                windowEnd.getEpochSecond());
    }

    @Override
    public BigDecimal findPreviousCleanValue(String cleanTable, AggregateSourceKey key, Instant windowStart) {
        String sql = "SELECT clean_value FROM " + cleanTable
                + " WHERE effective_flag = 1 AND clean_value IS NOT NULL "
                + "AND tenant_mark = ? AND model_mark = ? AND device_mark = ? AND param_mark = ? "
                + "AND normal_second < ? ORDER BY normal_second DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class,
                    key.getTenantMark(),
                    key.getModelMark(),
                    key.getDeviceMark(),
                    key.getParamMark(),
                    windowStart.getEpochSecond());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<AggregateSourceKey> findAggregateSourceKeys(String aggregateTable, Instant windowStart, Instant windowEnd) {
        ensureAggregateTable(aggregateTable);
        String sql = "SELECT DISTINCT tenant_mark, model_mark, device_mark, param_mark FROM " + aggregateTable + " FINAL "
                + "WHERE window_start >= toDateTime(?) AND window_start < toDateTime(?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapKey(rs), toClickHouseDateTime(windowStart), toClickHouseDateTime(windowEnd));
    }

    @Override
    public List<AggregatePointRecord> findAggregateRecords(String aggregateTable,
                                                           AggregateSourceKey key,
                                                           Instant windowStart,
                                                           Instant windowEnd) {
        ensureAggregateTable(aggregateTable);
        String sql = "SELECT id, tenant_mark, model_mark, device_mark, param_mark, window_start, window_end, "
                + "start_value, end_value, usage_value, sum_value, avg_value, min_value, max_value, "
                + "sample_count, source_count, rollover_count, quality_level, version, created_time, updated_time "
                + "FROM " + aggregateTable + " FINAL "
                + "WHERE tenant_mark = ? AND model_mark = ? AND device_mark = ? AND param_mark = ? "
                + "AND window_start >= toDateTime(?) AND window_start < toDateTime(?) ORDER BY window_start";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapAggregate(rs),
                key.getTenantMark(),
                key.getModelMark(),
                key.getDeviceMark(),
                key.getParamMark(),
                toClickHouseDateTime(windowStart),
                toClickHouseDateTime(windowEnd));
    }

    @Override
    public void save(String aggregateTable, List<AggregatePointRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        ensureAggregateTable(aggregateTable);
        jdbcTemplate.batchUpdate(insertSql(aggregateTable), toArgs(records));
    }

    private void ensureAggregateTable(String tableName) {
        if (!autoCreateTable || initializedTables.contains(tableName)) {
            return;
        }
        synchronized (initializedTables) {
            if (!initializedTables.contains(tableName)) {
                jdbcTemplate.execute(createAggregateTableSql(tableName));
                initializedTables.add(tableName);
            }
        }
    }

    private static AggregateSourceKey mapKey(ResultSet rs) throws SQLException {
        return new AggregateSourceKey(
                rs.getString("tenant_mark"),
                rs.getString("model_mark"),
                rs.getString("device_mark"),
                rs.getString("param_mark")
        );
    }

    private static CleanPointRecord mapClean(ResultSet rs) throws SQLException {
        return new CleanPointRecord()
                .setId(rs.getString("id"))
                .setMessageId(rs.getString("message_id"))
                .setProtocolVersion(rs.getString("protocol_version"))
                .setTenantMark(rs.getString("tenant_mark"))
                .setModelMark(rs.getString("model_mark"))
                .setDeviceMark(rs.getString("device_mark"))
                .setParamMark(rs.getString("param_mark"))
                .setRawValue(rs.getString("raw_value"))
                .setCleanValue(rs.getBigDecimal("clean_value"))
                .setDeviceTime(toInstant(rs.getTimestamp("device_time")))
                .setReceiveTime(toInstant(rs.getTimestamp("receive_time")))
                .setNormalSecond(rs.getLong("normal_second"))
                .setQualityCode(rs.getInt("quality_code"))
                .setEffective(rs.getInt("effective_flag") == 1)
                .setDuplicateOfId(rs.getString("duplicate_of_id"))
                .setCleanRule(rs.getString("clean_rule"))
                .setCreatedTime(toInstant(rs.getTimestamp("created_time")));
    }

    private static AggregatePointRecord mapAggregate(ResultSet rs) throws SQLException {
        return new AggregatePointRecord()
                .setId(rs.getString("id"))
                .setTenantMark(rs.getString("tenant_mark"))
                .setModelMark(rs.getString("model_mark"))
                .setDeviceMark(rs.getString("device_mark"))
                .setParamMark(rs.getString("param_mark"))
                .setWindowStart(toInstant(rs.getTimestamp("window_start")))
                .setWindowEnd(toInstant(rs.getTimestamp("window_end")))
                .setStartValue(rs.getBigDecimal("start_value"))
                .setEndValue(rs.getBigDecimal("end_value"))
                .setUsageValue(rs.getBigDecimal("usage_value"))
                .setSumValue(rs.getBigDecimal("sum_value"))
                .setAvgValue(rs.getBigDecimal("avg_value"))
                .setMinValue(rs.getBigDecimal("min_value"))
                .setMaxValue(rs.getBigDecimal("max_value"))
                .setSampleCount(rs.getLong("sample_count"))
                .setSourceCount(rs.getLong("source_count"))
                .setRolloverCount(rs.getLong("rollover_count"))
                .setQualityLevel(rs.getInt("quality_level"))
                .setVersion(rs.getLong("version"))
                .setCreatedTime(toInstant(rs.getTimestamp("created_time")))
                .setUpdatedTime(toInstant(rs.getTimestamp("updated_time")));
    }

    private static List<Object[]> toArgs(List<AggregatePointRecord> records) {
        List<Object[]> args = new ArrayList<>();
        for (AggregatePointRecord record : records) {
            args.add(new Object[]{
                    record.getId(),
                    record.getTenantMark(),
                    record.getModelMark(),
                    record.getDeviceMark(),
                    record.getParamMark(),
                    toTimestamp(record.getWindowStart()),
                    toTimestamp(record.getWindowEnd()),
                    record.getStartValue(),
                    record.getEndValue(),
                    record.getUsageValue(),
                    record.getSumValue(),
                    record.getAvgValue(),
                    record.getMinValue(),
                    record.getMaxValue(),
                    record.getSampleCount(),
                    record.getSourceCount(),
                    record.getRolloverCount(),
                    record.getQualityLevel(),
                    record.getVersion(),
                    toTimestamp(record.getCreatedTime()),
                    toTimestamp(record.getUpdatedTime())
            });
        }
        return args;
    }

    private static String createAggregateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id String, "
                + "tenant_mark String, "
                + "model_mark String, "
                + "device_mark String, "
                + "param_mark String, "
                + "window_start DateTime, "
                + "window_end DateTime, "
                + "start_value Nullable(Decimal(38, 10)), "
                + "end_value Nullable(Decimal(38, 10)), "
                + "usage_value Nullable(Decimal(38, 10)), "
                + "sum_value Nullable(Decimal(38, 10)), "
                + "avg_value Nullable(Decimal(38, 10)), "
                + "min_value Nullable(Decimal(38, 10)), "
                + "max_value Nullable(Decimal(38, 10)), "
                + "sample_count UInt64, "
                + "source_count UInt64, "
                + "rollover_count UInt64, "
                + "quality_level UInt16, "
                + "version UInt64, "
                + "created_time DateTime, "
                + "updated_time DateTime"
                + ") ENGINE = ReplacingMergeTree(version) ORDER BY (device_mark, param_mark, window_start)";
    }

    private static String insertSql(String tableName) {
        return "INSERT INTO " + tableName + " (id, tenant_mark, model_mark, device_mark, param_mark, "
                + "window_start, window_end, start_value, end_value, usage_value, sum_value, avg_value, min_value, "
                + "max_value, sample_count, source_count, rollover_count, quality_level, version, created_time, updated_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static String toClickHouseDateTime(Instant instant) {
        return CLICKHOUSE_DATETIME_FORMATTER.format(instant);
    }
}
