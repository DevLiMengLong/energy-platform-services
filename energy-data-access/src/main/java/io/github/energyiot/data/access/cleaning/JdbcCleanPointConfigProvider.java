package io.github.energyiot.data.access.cleaning;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcCleanPointConfigProvider implements CleanPointConfigProvider, InitializingBean {

    private final JdbcTemplate jdbcTemplate;
    private final String configTable;
    private volatile Map<String, CleanPointConfig> cache = new HashMap<>();

    public JdbcCleanPointConfigProvider(JdbcTemplate jdbcTemplate, String configTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.configTable = configTable;
    }

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate.execute(createConfigTableSql());
        reload();
    }

    @Scheduled(fixedDelayString = "${energy.access.cleaning.config-refresh-ms:240000}")
    public void reload() {
        List<CleanPointConfig> configs = jdbcTemplate.query(
                "SELECT tenant_mark, model_mark, device_mark, param_mark, transform_formula, min_value, max_value, max_delta, "
                        + "is_cumulative, rollover_enabled, rollover_max_value, rollover_min_previous_value, "
                        + "rollover_max_current_value FROM " + configTable + " FINAL WHERE enabled = 1",
                (rs, rowNum) -> mapConfig(rs)
        );
        Map<String, CleanPointConfig> next = new HashMap<>();
        for (CleanPointConfig config : configs) {
            next.put(key(config.getTenantMark(), config.getModelMark(), config.getDeviceMark(), config.getParamMark()), config);
        }
        this.cache = next;
    }

    @Override
    public CleanPointConfig getConfig(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        CleanPointConfig config = cache.get(key(tenantMark, modelMark, deviceMark, paramMark));
        if (config != null) {
            return config;
        }
        return new CleanPointConfig()
                .setTenantMark(tenantMark)
                .setModelMark(modelMark)
                .setDeviceMark(deviceMark)
                .setParamMark(paramMark)
                .setTransformFormula("x");
    }

    private CleanPointConfig mapConfig(ResultSet rs) throws SQLException {
        return new CleanPointConfig()
                .setTenantMark(rs.getString("tenant_mark"))
                .setModelMark(rs.getString("model_mark"))
                .setDeviceMark(rs.getString("device_mark"))
                .setParamMark(rs.getString("param_mark"))
                .setTransformFormula(rs.getString("transform_formula"))
                .setMinValue(rs.getString("min_value"))
                .setMaxValue(rs.getString("max_value"))
                .setMaxDelta(rs.getString("max_delta"))
                .setCumulative(rs.getInt("is_cumulative") == 1)
                .setRolloverEnabled(rs.getInt("rollover_enabled") == 1)
                .setRolloverMaxValue(rs.getString("rollover_max_value"))
                .setRolloverMinPreviousValue(rs.getString("rollover_min_previous_value"))
                .setRolloverMaxCurrentValue(rs.getString("rollover_max_current_value"));
    }

    private String createConfigTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + configTable + " ("
                + "tenant_mark String, "
                + "model_mark String, "
                + "device_mark String, "
                + "param_mark String, "
                + "transform_formula String, "
                + "min_value Nullable(String), "
                + "max_value Nullable(String), "
                + "max_delta Nullable(String), "
                + "is_cumulative UInt8, "
                + "rollover_enabled UInt8, "
                + "rollover_max_value Nullable(String), "
                + "rollover_min_previous_value Nullable(String), "
                + "rollover_max_current_value Nullable(String), "
                + "enabled UInt8, "
                + "version UInt64, "
                + "updated_time DateTime"
                + ") ENGINE = ReplacingMergeTree(version) ORDER BY (tenant_mark, model_mark, device_mark, param_mark)";
    }

    private static String key(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        return tenantMark + "|" + modelMark + "|" + deviceMark + "|" + paramMark;
    }
}
