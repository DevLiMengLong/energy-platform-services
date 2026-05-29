package io.github.energyiot.data.access.cleaning;

import io.github.energyiot.data.access.raw.RawPointRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcCleanPointRepository implements CleanPointRepository {

    private final JdbcTemplate jdbcTemplate;
    private final boolean autoCreateTable;
    private final Set<String> initializedTables = Collections.synchronizedSet(new HashSet<>());

    public JdbcCleanPointRepository(JdbcTemplate jdbcTemplate, boolean autoCreateTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.autoCreateTable = autoCreateTable;
    }

    @Override
    public List<RawPointRecord> findRawRecords(String rawTable, List<String> rawIds) {
        if (CollectionUtils.isEmpty(rawIds)) {
            return Collections.emptyList();
        }
        String sql = "SELECT id, message_id, protocol_version, tenant_mark, model_mark, device_mark, param_mark, "
                + "raw_value, device_time, receive_time, normal_second, created_time FROM " + rawTable
                + " WHERE id IN (" + quoted(rawIds) + ")";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRaw(rs));
    }

    @Override
    public String findDuplicateEffectiveId(String cleanTable, CleanPointRecord record) {
        ensureCleanTable(cleanTable);
        String sql = "SELECT id FROM " + cleanTable
                + " WHERE tenant_mark = ? AND device_mark = ? AND param_mark = ? AND normal_second = ? "
                + "AND clean_value = ? AND effective_flag = 1 LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, String.class,
                    record.getTenantMark(),
                    record.getDeviceMark(),
                    record.getParamMark(),
                    record.getNormalSecond(),
                    record.getCleanValue());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String findPreviousEffectiveRawValue(String cleanTable, CleanPointRecord record) {
        ensureCleanTable(cleanTable);
        String sql = "SELECT raw_value FROM " + cleanTable
                + " WHERE tenant_mark = ? AND device_mark = ? AND param_mark = ? AND effective_flag = 1 "
                + "AND normal_second < ? ORDER BY normal_second DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, String.class,
                    record.getTenantMark(),
                    record.getDeviceMark(),
                    record.getParamMark(),
                    record.getNormalSecond());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(String cleanTable, List<CleanPointRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        ensureCleanTable(cleanTable);
        jdbcTemplate.batchUpdate(insertSql(cleanTable), toArgs(records));
    }

    private void ensureCleanTable(String tableName) {
        if (!autoCreateTable || initializedTables.contains(tableName)) {
            return;
        }
        synchronized (initializedTables) {
            if (!initializedTables.contains(tableName)) {
                jdbcTemplate.execute(createCleanTableSql(tableName));
                initializedTables.add(tableName);
            }
        }
    }

    private static RawPointRecord mapRaw(ResultSet rs) throws SQLException {
        return new RawPointRecord()
                .setId(rs.getString("id"))
                .setMessageId(rs.getString("message_id"))
                .setProtocolVersion(rs.getString("protocol_version"))
                .setTenantMark(rs.getString("tenant_mark"))
                .setModelMark(rs.getString("model_mark"))
                .setDeviceMark(rs.getString("device_mark"))
                .setParamMark(rs.getString("param_mark"))
                .setRawValue(rs.getString("raw_value"))
                .setDeviceTime(toInstant(rs.getTimestamp("device_time")))
                .setReceiveTime(toInstant(rs.getTimestamp("receive_time")))
                .setNormalSecond(rs.getLong("normal_second"))
                .setCreatedTime(toInstant(rs.getTimestamp("created_time")));
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static List<Object[]> toArgs(List<CleanPointRecord> records) {
        List<Object[]> args = new ArrayList<>();
        for (CleanPointRecord record : records) {
            args.add(new Object[]{
                    record.getId(),
                    record.getRawId(),
                    record.getMessageId(),
                    record.getProtocolVersion(),
                    record.getTenantMark(),
                    record.getModelMark(),
                    record.getDeviceMark(),
                    record.getParamMark(),
                    record.getRawValue(),
                    record.getCleanValue(),
                    toTimestamp(record.getDeviceTime()),
                    toTimestamp(record.getReceiveTime()),
                    record.getNormalSecond(),
                    record.getQualityCode(),
                    record.isEffective() ? 1 : 0,
                    record.getDuplicateOfId(),
                    record.getCleanRule(),
                    toTimestamp(record.getCreatedTime())
            });
        }
        return args;
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static String createCleanTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id String, "
                + "raw_id String, "
                + "message_id String, "
                + "protocol_version String, "
                + "tenant_mark String, "
                + "model_mark String, "
                + "device_mark String, "
                + "param_mark String, "
                + "raw_value String, "
                + "clean_value Nullable(Decimal(38, 10)), "
                + "device_time DateTime, "
                + "receive_time DateTime, "
                + "normal_second UInt64, "
                + "quality_code UInt16, "
                + "effective_flag UInt8, "
                + "duplicate_of_id Nullable(String), "
                + "clean_rule String, "
                + "created_time DateTime"
                + ") ENGINE = MergeTree ORDER BY (device_mark, param_mark, normal_second)";
    }

    private static String insertSql(String tableName) {
        return "INSERT INTO " + tableName + " (id, raw_id, message_id, protocol_version, tenant_mark, model_mark, "
                + "device_mark, param_mark, raw_value, clean_value, device_time, receive_time, normal_second, "
                + "quality_code, effective_flag, duplicate_of_id, clean_rule, created_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static String quoted(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add("'" + value.replace("'", "''") + "'");
        }
        return String.join(",", escaped);
    }
}
