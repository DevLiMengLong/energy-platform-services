package io.github.energyiot.data.access.query;

import io.github.energyiot.data.access.aggregation.AggregationGranularity;
import io.github.energyiot.data.access.latest.LatestCleanPoint;
import io.github.energyiot.data.access.latest.LatestCleanPointStore;
import io.github.energyiot.data.access.storage.RawPointTableNameResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PointQueryService {
    private final JdbcTemplate jdbcTemplate;
    private final LatestCleanPointStore latestStore;
    private final RawPointTableNameResolver rawResolver;
    private final RawPointTableNameResolver cleanResolver;
    private final RawPointTableNameResolver minuteResolver = new RawPointTableNameResolver(AggregationGranularity.MINUTE.getTablePrefix());
    private final RawPointTableNameResolver fifteenResolver = new RawPointTableNameResolver(AggregationGranularity.FIFTEEN_MINUTE.getTablePrefix());
    private final RawPointTableNameResolver hourResolver = new RawPointTableNameResolver(AggregationGranularity.HOUR.getTablePrefix());
    private final RawPointTableNameResolver dayResolver = new RawPointTableNameResolver(AggregationGranularity.DAY.getTablePrefix());

    public PointQueryService(JdbcTemplate jdbcTemplate,
                             LatestCleanPointStore latestStore,
                             String rawTablePrefix,
                             String cleanTablePrefix) {
        this.jdbcTemplate = jdbcTemplate;
        this.latestStore = latestStore;
        this.rawResolver = new RawPointTableNameResolver(rawTablePrefix);
        this.cleanResolver = new RawPointTableNameResolver(cleanTablePrefix);
    }

    public PageResult rawPoints(PointQueryRequest request) {
        validateSingleModel(request);
        String table = rawResolver.resolve(request.getTenantMark(), request.getModelMark());
        return queryPage(table, rawWhere(request, "normal_second"), request, "normal_second, device_mark, param_mark");
    }

    public PageResult cleanPoints(PointQueryRequest request) {
        validateSingleModel(request);
        String table = cleanResolver.resolve(request.getTenantMark(), request.getModelMark());
        String where = cleanWhere(request);
        return queryPage(table, where, request, "normal_second, device_mark, param_mark");
    }

    public List<LatestCleanPoint> latest(PointQueryRequest request) {
        validateSingleModel(request);
        if (CollectionUtils.isEmpty(request.getDeviceMarks()) || CollectionUtils.isEmpty(request.getParamMarks())) {
            throw new IllegalArgumentException("deviceMarks and paramMarks are required for latest query");
        }
        List<LatestCleanPoint> records = new ArrayList<>();
        for (String deviceMark : request.getDeviceMarks()) {
            for (String paramMark : request.getParamMarks()) {
                Optional<LatestCleanPoint> latest = latestStore.get(request.getTenantMark(), request.getModelMark(), deviceMark, paramMark);
                if (latest.isPresent()) {
                    records.add(latest.get());
                } else {
                    fallbackLatest(request, deviceMark, paramMark).ifPresent(records::add);
                }
            }
        }
        return records;
    }

    public PageResult aggregatePoints(PointQueryRequest request) {
        if (!StringUtils.hasText(request.getTenantMark())) {
            throw new IllegalArgumentException("tenantMark is required");
        }
        List<String> modelMarks = modelMarks(request);
        if (modelMarks.isEmpty()) {
            throw new IllegalArgumentException("modelMarks is required");
        }
        RawPointTableNameResolver resolver = aggregateResolver(request.getGranularity());
        List<Map<String, Object>> all = new ArrayList<>();
        long total = 0;
        int pageSize = boundedPageSize(request);
        int offset = Math.max(0, request.getPageNo() - 1) * pageSize;
        for (String modelMark : modelMarks) {
            String table = resolver.resolve(request.getTenantMark(), modelMark);
            String where = aggregateWhere(request);
            total += count(table, where);
            all.addAll(jdbcTemplate.queryForList("SELECT * FROM " + table + " FINAL WHERE " + where
                    + " ORDER BY window_start, model_mark, device_mark, param_mark LIMIT " + pageSize + " OFFSET " + offset));
        }
        if (all.size() > pageSize) {
            all = all.subList(0, pageSize);
        }
        return new PageResult(total, all);
    }

    private Optional<LatestCleanPoint> fallbackLatest(PointQueryRequest request, String deviceMark, String paramMark) {
        String table = cleanResolver.resolve(request.getTenantMark(), request.getModelMark());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + table + " WHERE effective_flag = 1 "
                + "AND device_mark = '" + esc(deviceMark) + "' AND param_mark = '" + esc(paramMark) + "' "
                + "ORDER BY normal_second DESC LIMIT 1");
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> row = rows.get(0);
        LatestCleanPoint latest = new LatestCleanPoint()
                .setCleanId(String.valueOf(row.get("id")))
                .setRawId(String.valueOf(row.get("raw_id")))
                .setTenantMark(String.valueOf(row.get("tenant_mark")))
                .setModelMark(String.valueOf(row.get("model_mark")))
                .setDeviceMark(String.valueOf(row.get("device_mark")))
                .setParamMark(String.valueOf(row.get("param_mark")))
                .setCleanValue((java.math.BigDecimal) row.get("clean_value"))
                .setQualityCode(((Number) row.get("quality_code")).intValue())
                .setNormalSecond(((Number) row.get("normal_second")).longValue());
        return Optional.of(latest);
    }

    private PageResult queryPage(String table, String where, PointQueryRequest request, String orderBy) {
        int pageSize = boundedPageSize(request);
        int offset = Math.max(0, request.getPageNo() - 1) * pageSize;
        long total = count(table, where);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE " + where + " ORDER BY " + orderBy
                        + " LIMIT " + pageSize + " OFFSET " + offset);
        return new PageResult(total, records);
    }

    private long count(String table, String where) {
        Number count = jdbcTemplate.queryForObject("SELECT count() FROM " + table + " WHERE " + where, Number.class);
        return count == null ? 0 : count.longValue();
    }

    private static String rawWhere(PointQueryRequest request, String timeColumn) {
        StringBuilder where = new StringBuilder("1=1");
        appendCommon(where, request, timeColumn);
        return where.toString();
    }

    private static String cleanWhere(PointQueryRequest request) {
        StringBuilder where = new StringBuilder("1=1");
        appendCommon(where, request, "normal_second");
        if (Boolean.TRUE.equals(request.getEffectiveOnly())) {
            where.append(" AND effective_flag = 1");
        }
        if (!CollectionUtils.isEmpty(request.getQualityCodes())) {
            where.append(" AND quality_code IN (").append(joinNumbers(request.getQualityCodes())).append(")");
        }
        return where.toString();
    }

    private static String aggregateWhere(PointQueryRequest request) {
        StringBuilder where = new StringBuilder("1=1");
        if (request.getStartTime() != null) {
            where.append(" AND window_start >= toDateTime('").append(escDate(request.getStartTime())).append("')");
        }
        if (request.getEndTime() != null) {
            where.append(" AND window_start < toDateTime('").append(escDate(request.getEndTime())).append("')");
        }
        appendPointPairsOrIn(where, request);
        return where.toString();
    }

    private static void appendCommon(StringBuilder where, PointQueryRequest request, String timeColumn) {
        if (request.getStartTime() != null) {
            where.append(" AND ").append(timeColumn).append(" >= ").append(request.getStartTime().getEpochSecond());
        }
        if (request.getEndTime() != null) {
            where.append(" AND ").append(timeColumn).append(" < ").append(request.getEndTime().getEpochSecond());
        }
        appendPointPairsOrIn(where, request);
    }

    private static void appendPointPairsOrIn(StringBuilder where, PointQueryRequest request) {
        if (!CollectionUtils.isEmpty(request.getPoints())) {
            List<String> pairs = new ArrayList<>();
            for (PointQueryRequest.PointKey point : request.getPoints()) {
                if (!StringUtils.hasText(point.getDeviceMark()) || !StringUtils.hasText(point.getParamMark())) {
                    continue;
                }
                pairs.add("(device_mark = '" + esc(point.getDeviceMark()) + "' AND param_mark = '" + esc(point.getParamMark()) + "')");
            }
            if (!pairs.isEmpty()) {
                where.append(" AND (").append(String.join(" OR ", pairs)).append(")");
            }
            return;
        }
        appendIn(where, "device_mark", request.getDeviceMarks());
        appendIn(where, "param_mark", request.getParamMarks());
    }

    private static void appendIn(StringBuilder where, String column, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        List<String> quoted = new ArrayList<>();
        for (String value : values) {
            quoted.add("'" + esc(value) + "'");
        }
        where.append(" AND ").append(column).append(" IN (").append(String.join(",", quoted)).append(")");
    }

    private RawPointTableNameResolver aggregateResolver(String granularity) {
        if ("minute".equalsIgnoreCase(granularity)) return minuteResolver;
        if ("15min".equalsIgnoreCase(granularity)) return fifteenResolver;
        if ("hour".equalsIgnoreCase(granularity)) return hourResolver;
        if ("day".equalsIgnoreCase(granularity)) return dayResolver;
        throw new IllegalArgumentException("granularity must be minute, 15min, hour or day");
    }

    private static List<String> modelMarks(PointQueryRequest request) {
        if (!CollectionUtils.isEmpty(request.getModelMarks())) return request.getModelMarks();
        if (StringUtils.hasText(request.getModelMark())) return Collections.singletonList(request.getModelMark());
        return Collections.emptyList();
    }

    private static void validateSingleModel(PointQueryRequest request) {
        if (!StringUtils.hasText(request.getTenantMark()) || !StringUtils.hasText(request.getModelMark())) {
            throw new IllegalArgumentException("tenantMark and modelMark are required");
        }
    }

    private static int boundedPageSize(PointQueryRequest request) {
        if (request.getPageSize() <= 0) return 100;
        return Math.min(request.getPageSize(), 1000);
    }

    private static String joinNumbers(List<Integer> values) {
        List<String> nums = new ArrayList<>();
        for (Integer value : values) nums.add(String.valueOf(value));
        return String.join(",", nums);
    }

    private static String escDate(java.time.Instant instant) {
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault()).format(instant);
    }

    private static String esc(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}
