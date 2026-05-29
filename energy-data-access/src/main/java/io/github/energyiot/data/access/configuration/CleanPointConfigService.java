package io.github.energyiot.data.access.configuration;

import io.github.energyiot.data.access.cleaning.CleanPointConfigProvider;
import io.github.energyiot.data.access.query.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CleanPointConfigService {
    private static final Pattern FORMULA_PATTERN = Pattern.compile("[xX0-9+\\-*/().\\s]+");

    private final JdbcTemplate jdbcTemplate;
    private final String configTable;
    private final CleanPointConfigProvider configProvider;
    private final BasicCollectionPointClient basicCollectionPointClient;

    public CleanPointConfigService(JdbcTemplate jdbcTemplate,
                                   String configTable,
                                   CleanPointConfigProvider configProvider,
                                   BasicCollectionPointClient basicCollectionPointClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.configTable = configTable;
        this.configProvider = configProvider;
        this.basicCollectionPointClient = basicCollectionPointClient;
    }

    public void save(CleanPointConfigSaveRequest request) {
        validate(request);
        String sql = "INSERT INTO " + configTable + " (tenant_mark, model_mark, device_mark, param_mark, transform_formula, min_value, max_value, max_delta, "
                + "is_cumulative, rollover_enabled, rollover_max_value, rollover_min_previous_value, rollover_max_current_value, enabled, version, updated_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";
        jdbcTemplate.update(sql,
                request.getTenantMark(), request.getModelMark(), request.getDeviceMark(), request.getParamMark(),
                StringUtils.hasText(request.getTransformFormula()) ? request.getTransformFormula() : "x",
                request.getMinValue(), request.getMaxValue(), request.getMaxDelta(),
                request.isCumulative() ? 1 : 0,
                request.isRolloverEnabled() ? 1 : 0,
                request.getRolloverMaxValue(), request.getRolloverMinPreviousValue(), request.getRolloverMaxCurrentValue(),
                request.isEnabled() ? 1 : 0,
                System.currentTimeMillis());
        configProvider.reload();
    }

    public void disable(CleanPointConfigQueryRequest request) {
        CleanPointConfigSaveRequest save = new CleanPointConfigSaveRequest();
        save.setTenantMark(request.getTenantMark());
        save.setModelMark(request.getModelMark());
        save.setDeviceMark(request.getDeviceMark());
        save.setParamMark(request.getParamMark());
        save.setEnabled(false);
        save(save);
    }

    public List<Map<String, Object>> list(CleanPointConfigQueryRequest request) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(configTable).append(" FINAL WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (StringUtils.hasText(request.getTenantMark())) {
            sql.append(" AND tenant_mark = ?");
            args.add(request.getTenantMark());
        }
        if (StringUtils.hasText(request.getModelMark())) {
            sql.append(" AND model_mark = ?");
            args.add(request.getModelMark());
        }
        if (StringUtils.hasText(request.getDeviceMark())) {
            sql.append(" AND device_mark = ?");
            args.add(request.getDeviceMark());
        }
        if (StringUtils.hasText(request.getParamMark())) {
            sql.append(" AND param_mark = ?");
            args.add(request.getParamMark());
        }
        sql.append(" ORDER BY tenant_mark, model_mark, device_mark, param_mark");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    public PageResult page(CleanPointConfigPageRequest request, String authorization) {
        if (!StringUtils.hasText(request.getTenantMark())) {
            throw new IllegalArgumentException("tenantMark is required");
        }
        BasicCollectionPointClient.BasicPointPage pointPage = basicCollectionPointClient.page(request, authorization);
        Map<String, Map<String, Object>> configs = configMap(request);
        List<Map<String, Object>> merged = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pointPage.getRows())) {
            for (Map<String, Object> point : pointPage.getRows()) {
                Map<String, Object> row = mergePointAndConfig(request.getTenantMark(), point, configs);
                if (matchesStatus(row, request.getConfigStatus()) && matchesCumulative(row, request.getCumulative())) {
                    merged.add(row);
                }
            }
        } else {
            for (Map<String, Object> config : configs.values()) {
                Map<String, Object> row = configOnlyRow(request.getTenantMark(), config);
                if (matchesStatus(row, request.getConfigStatus()) && matchesCumulative(row, request.getCumulative())) {
                    merged.add(row);
                }
            }
        }
        int pageSize = Math.min(Math.max(request.getPageSize(), 1), 200);
        int pageNo = Math.max(request.getPageNo(), 1);
        int from = Math.min((pageNo - 1) * pageSize, merged.size());
        int to = Math.min(from + pageSize, merged.size());
        return new PageResult(merged.size(), merged.subList(from, to));
    }

    public void reload() {
        configProvider.reload();
    }

    private static void validate(CleanPointConfigSaveRequest request) {
        if (!StringUtils.hasText(request.getTenantMark())
                || !StringUtils.hasText(request.getModelMark())
                || !StringUtils.hasText(request.getDeviceMark())
                || !StringUtils.hasText(request.getParamMark())) {
            throw new IllegalArgumentException("tenantMark, modelMark, deviceMark and paramMark are required");
        }
        if (StringUtils.hasText(request.getTransformFormula())
                && !FORMULA_PATTERN.matcher(request.getTransformFormula()).matches()) {
            throw new IllegalArgumentException("transformFormula contains unsupported characters");
        }
        BigDecimal minValue = decimal(request.getMinValue(), "minValue");
        BigDecimal maxValue = decimal(request.getMaxValue(), "maxValue");
        BigDecimal maxDelta = decimal(request.getMaxDelta(), "maxDelta");
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
        }
        if (maxDelta != null && maxDelta.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxDelta must be greater than or equal to 0");
        }
        if (request.isRolloverEnabled()) {
            requireText(request.getRolloverMaxValue(), "rolloverMaxValue");
            requireText(request.getRolloverMinPreviousValue(), "rolloverMinPreviousValue");
            requireText(request.getRolloverMaxCurrentValue(), "rolloverMaxCurrentValue");
            decimal(request.getRolloverMaxValue(), "rolloverMaxValue");
            decimal(request.getRolloverMinPreviousValue(), "rolloverMinPreviousValue");
            decimal(request.getRolloverMaxCurrentValue(), "rolloverMaxCurrentValue");
        } else {
            decimal(request.getRolloverMaxValue(), "rolloverMaxValue");
            decimal(request.getRolloverMinPreviousValue(), "rolloverMinPreviousValue");
            decimal(request.getRolloverMaxCurrentValue(), "rolloverMaxCurrentValue");
        }
    }

    private Map<String, Map<String, Object>> configMap(CleanPointConfigPageRequest request) {
        CleanPointConfigQueryRequest query = new CleanPointConfigQueryRequest();
        query.setTenantMark(request.getTenantMark());
        query.setModelMark(request.getModelMark());
        query.setDeviceMark(request.getDeviceMark());
        query.setParamMark(request.getParamMark());
        List<Map<String, Object>> rows = list(query);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(key(value(row.get("tenant_mark")), value(row.get("model_mark")), value(row.get("device_mark")), value(row.get("param_mark"))), row);
        }
        return result;
    }

    private static Map<String, Object> mergePointAndConfig(String tenantMark, Map<String, Object> point, Map<String, Map<String, Object>> configs) {
        String modelMark = first(point, "collectionModelMark", "collection_model_mark");
        String deviceMark = first(point, "collectionDeviceMark", "collection_device_mark");
        String paramMark = first(point, "collectionParamMark", "collectionPointMark", "collection_param_mark");
        Map<String, Object> config = configs.get(key(tenantMark, modelMark, deviceMark, paramMark));
        Map<String, Object> row = baseRow(tenantMark, modelMark, deviceMark, paramMark);
        row.put("pointName", first(point, "businessName", "collectionPointName", "collectionParamName", "collection_param_name"));
        row.put("unit", first(point, "unit"));
        row.put("modelName", first(point, "collectionModelName", "collection_model_name"));
        row.put("deviceName", first(point, "collectionDeviceName", "collection_device_name"));
        row.put("paramName", first(point, "collectionParamName", "collection_param_name"));
        applyConfig(row, config);
        return row;
    }

    private static Map<String, Object> configOnlyRow(String tenantMark, Map<String, Object> config) {
        Map<String, Object> row = baseRow(
                tenantMark,
                value(config.get("model_mark")),
                value(config.get("device_mark")),
                value(config.get("param_mark"))
        );
        row.put("pointName", row.get("paramMark"));
        row.put("unit", "");
        applyConfig(row, config);
        return row;
    }

    private static Map<String, Object> baseRow(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("tenantMark", tenantMark);
        row.put("modelMark", modelMark);
        row.put("deviceMark", deviceMark);
        row.put("paramMark", paramMark);
        row.put("transformFormula", "x");
        row.put("minValue", null);
        row.put("maxValue", null);
        row.put("maxDelta", null);
        row.put("cumulative", false);
        row.put("rolloverEnabled", false);
        row.put("rolloverMaxValue", null);
        row.put("rolloverMinPreviousValue", null);
        row.put("rolloverMaxCurrentValue", null);
        row.put("enabled", false);
        row.put("configStatus", "unconfigured");
        return row;
    }

    private static void applyConfig(Map<String, Object> row, Map<String, Object> config) {
        if (config == null) {
            return;
        }
        row.put("transformFormula", valueOr(config.get("transform_formula"), "x"));
        row.put("minValue", config.get("min_value"));
        row.put("maxValue", config.get("max_value"));
        row.put("maxDelta", config.get("max_delta"));
        row.put("cumulative", asBoolean(config.get("is_cumulative")));
        row.put("rolloverEnabled", asBoolean(config.get("rollover_enabled")));
        row.put("rolloverMaxValue", config.get("rollover_max_value"));
        row.put("rolloverMinPreviousValue", config.get("rollover_min_previous_value"));
        row.put("rolloverMaxCurrentValue", config.get("rollover_max_current_value"));
        boolean enabled = asBoolean(config.get("enabled"));
        row.put("enabled", enabled);
        row.put("configStatus", enabled ? "enabled" : "disabled");
    }

    private static boolean matchesStatus(Map<String, Object> row, String status) {
        return !StringUtils.hasText(status) || "all".equalsIgnoreCase(status) || status.equalsIgnoreCase(value(row.get("configStatus")));
    }

    private static boolean matchesCumulative(Map<String, Object> row, Boolean cumulative) {
        return cumulative == null || cumulative.booleanValue() == Boolean.TRUE.equals(row.get("cumulative"));
    }

    private static String first(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String valueOr(Object value, String defaultValue) {
        return StringUtils.hasText(value(value)) ? String.valueOf(value) : defaultValue;
    }

    private static boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return "1".equals(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static String key(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        return tenantMark + "|" + modelMark + "|" + deviceMark + "|" + paramMark;
    }

    private static void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is required when rolloverEnabled is true");
        }
    }

    private static BigDecimal decimal(String value, String field) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be numeric");
        }
    }
}
