package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiException;
import com.getech.energy.platformbasic.common.PageResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ResourceService {

    private final JdbcClient jdbcClient;
    private final ResourceRegistry registry;

    public ResourceService(JdbcClient jdbcClient, ResourceRegistry registry) {
        this.jdbcClient = jdbcClient;
        this.registry = registry;
    }

    public PageResult page(String key, CurrentUser user, String keyword, int page, int size) {
        ResourceDefinition definition = registry.require(key);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(definition, user, keyword, params);
        String countSql = "SELECT COUNT(1) FROM " + definition.fromClause() + " WHERE " + where;
        long total = jdbcClient.sql(countSql).params(params).query(Long.class).single();
        params.put("limit", safeSize);
        params.put("offset", (safePage - 1) * safeSize);
        String listSql = "SELECT " + definition.selectColumns()
                + " FROM " + definition.fromClause()
                + " WHERE " + where
                + " ORDER BY " + definition.defaultSort()
                + " LIMIT :limit OFFSET :offset";
        List<Map<String, Object>> rows = jdbcClient.sql(listSql)
                .params(params)
                .query()
                .listOfRows();
        return new PageResult(total, safePage, safeSize, rows);
    }

    public List<Map<String, Object>> all(String key, CurrentUser user, String keyword) {
        ResourceDefinition definition = registry.require(key);
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(definition, user, keyword, params);
        String listSql = "SELECT " + definition.selectColumns()
                + " FROM " + definition.fromClause()
                + " WHERE " + where
                + " ORDER BY " + definition.defaultSort();
        return jdbcClient.sql(listSql)
                .params(params)
                .query()
                .listOfRows();
    }

    private String buildWhere(ResourceDefinition definition, CurrentUser user, String keyword, Map<String, Object> params) {
        StringBuilder where = new StringBuilder("(").append(definition.fixedCondition()).append(")");
        if (definition.tenantScoped()) {
            if (user.tenantId() == null) {
                throw new ApiException("TENANT_REQUIRED", "Tenant context is required");
            }
            where.append(" AND ").append(definition.tenantColumn()).append(" = :tenantId");
            params.put("tenantId", user.tenantId());
        }
        if (StringUtils.hasText(keyword) && !definition.searchColumns().isEmpty()) {
            where.append(" AND (");
            for (int i = 0; i < definition.searchColumns().size(); i++) {
                if (i > 0) {
                    where.append(" OR ");
                }
                where.append("LOWER(").append(definition.searchColumns().get(i)).append(") LIKE :keyword");
            }
            where.append(")");
            params.put("keyword", "%" + keyword.toLowerCase() + "%");
        }
        return where.toString();
    }
}
