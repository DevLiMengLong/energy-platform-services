package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CatalogActionService {

    private final JdbcClient jdbcClient;

    public CatalogActionService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Map<String, Object> apply(CurrentUser user, CatalogActionRequest request) {
        Map<String, Object> result = acceptedResult(request);
        String nextStatus = statusFromAction(request);
        if (nextStatus == null) {
            result.put("affectedRows", 0);
            result.put("mutated", false);
            return result;
        }

        StatusTarget target = statusTarget(request);
        List<Long> targetIds = targetIds(request);
        if (targetIds.isEmpty()) {
            throw new ApiException("TARGET_REQUIRED", "Action target is required");
        }
        int affectedRows = updateStatus(user, target, targetIds, nextStatus);
        result.put("affectedRows", affectedRows);
        result.put("mutated", affectedRows > 0);
        result.put("status", nextStatus);
        return result;
    }

    private Map<String, Object> acceptedResult(CatalogActionRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", true);
        result.put("moduleCode", request.moduleCode());
        result.put("actionCode", request.actionCode());
        result.put("targetId", request.targetId());
        return result;
    }

    private int updateStatus(CurrentUser user, StatusTarget target, List<Long> targetIds, String status) {
        if (target.tenantScoped() && user.tenantId() == null) {
            throw new ApiException("TENANT_REQUIRED", "Tenant context is required");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("status", status);
        params.put("ids", targetIds);

        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(target.table())
                .append(" SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE ")
                .append(target.idColumn())
                .append(" IN (:ids)");
        if (StringUtils.hasText(target.fixedCondition())) {
            sql.append(" AND ").append(target.fixedCondition());
        }
        if (target.tenantScoped()) {
            sql.append(" AND ").append(target.tenantColumn()).append(" = :tenantId");
            params.put("tenantId", user.tenantId());
        }
        return jdbcClient.sql(sql.toString()).params(params).update();
    }

    private StatusTarget statusTarget(CatalogActionRequest request) {
        String moduleCode = normalize(request.moduleCode());
        String actionCode = normalize(request.actionCode());
        return switch (moduleCode) {
            case "platform.tenants" -> new StatusTarget("basic_tenant", "id", false, null, "deleted = 0");
            case "platform.tenantadmins" -> new StatusTarget("basic_user", "id", false, null, "role_type = 'TENANT_ADMIN'");
            case "platform.subsystems" -> new StatusTarget("basic_subsystem", "id", false, null, null);
            case "platform.menus" -> new StatusTarget("basic_menu", "id", false, null, null);
            case "basic.orgnodes" -> actionCode.contains("orgmember")
                    ? new StatusTarget("basic_user", "id", true, "tenant_id", "role_type IN ('TENANT_ADMIN', 'TENANT_USER')")
                    : new StatusTarget("basic_org_node", "id", true, "tenant_id", "deleted = 0");
            case "basic.users" -> new StatusTarget("basic_user", "id", true, "tenant_id", "role_type IN ('TENANT_ADMIN', 'TENANT_USER')");
            case "basic.usergroups" -> new StatusTarget("basic_user_group", "id", true, "tenant_id", null);
            case "basic.roles" -> new StatusTarget("basic_role", "id", true, "tenant_id", null);
            case "basic.dictionaries" -> new StatusTarget("basic_dict_type", "id", true, "tenant_id", null);
            case "basic.energytypes" -> new StatusTarget("basic_energy_type", "id", true, "tenant_id", null);
            case "basic.energyprices" -> new StatusTarget("basic_energy_price", "id", true, "tenant_id", null);
            case "basic.devicemodels" -> new StatusTarget("basic_device_model", "id", true, "tenant_id", null);
            case "basic.collectionpoints" -> new StatusTarget("basic_collection_point", "id", true, "tenant_id", null);
            case "basic.statmodels" -> new StatusTarget("basic_stat_model", "id", true, "tenant_id", null);
            case "basic.shifts" -> new StatusTarget("basic_shift", "id", true, "tenant_id", null);
            default -> throw new ApiException("ACTION_NOT_SUPPORTED", "Status action is not supported for module: " + request.moduleCode());
        };
    }

    private List<Long> targetIds(CatalogActionRequest request) {
        List<Long> ids = new ArrayList<>();
        addId(ids, request.targetId());
        Object selectedIds = request.payload() == null ? null : request.payload().get("selectedIds");
        if (selectedIds instanceof Collection<?> collection) {
            collection.forEach(value -> addId(ids, value));
        } else {
            addId(ids, selectedIds);
        }
        return ids.stream().distinct().toList();
    }

    private void addId(List<Long> ids, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            ids.add(number.longValue());
            return;
        }
        try {
            ids.add(Long.parseLong(String.valueOf(value)));
        } catch (NumberFormatException ignored) {
            // Ignore non-id payload values; validation below catches an empty target set.
        }
    }

    private String statusFromAction(CatalogActionRequest request) {
        String statusAction = request.payload() == null ? "" : String.valueOf(request.payload().getOrDefault("statusAction", ""));
        String semantic = normalize(request.actionCode() + " " + request.actionName() + " " + statusAction);
        if (semantic.contains("enable") || semantic.contains("启用")) {
            return "ENABLED";
        }
        if (semantic.contains("disable") || semantic.contains("停用") || semantic.contains("禁用")) {
            return "DISABLED";
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
    }

    private record StatusTarget(
            String table,
            String idColumn,
            boolean tenantScoped,
            String tenantColumn,
            String fixedCondition
    ) {
    }
}
