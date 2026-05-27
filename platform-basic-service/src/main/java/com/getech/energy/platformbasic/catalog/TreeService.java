package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class TreeService {

    private final JdbcClient jdbcClient;

    public TreeService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Map<String, Object>> platformMenuTree(String subsystemCode, String keyword) {
        String sql = """
                SELECT m.id, m.parent_id AS parentId, s.subsystem_code AS subsystemCode,
                       m.menu_type AS menuType, m.menu_code AS menuCode, m.permission_code AS permissionCode,
                       m.name_zh AS nameZh, m.name_en AS nameEn, m.icon, m.route_path AS routePath,
                       m.component_path AS componentPath, m.open_type AS openType, m.sort_order AS sortOrder,
                       m.hidden, m.status, m.created_at AS createdAt
                FROM basic_menu m
                JOIN basic_subsystem s ON s.id = m.subsystem_id
                WHERE (:subsystemCode IS NULL OR s.subsystem_code = :subsystemCode)
                  AND (:keyword IS NULL OR LOWER(m.name_zh) LIKE :keywordLike OR LOWER(m.name_en) LIKE :keywordLike OR LOWER(m.menu_code) LIKE :keywordLike)
                ORDER BY s.sort_order, m.parent_id, m.sort_order, m.id
                """;
        return buildTree(jdbcClient.sql(sql)
                .param("subsystemCode", subsystemCode == null || subsystemCode.isBlank() ? null : subsystemCode)
                .param("keyword", keyword == null || keyword.isBlank() ? null : keyword.toLowerCase())
                .param("keywordLike", keyword == null || keyword.isBlank() ? null : "%" + keyword.toLowerCase() + "%")
                .query()
                .listOfRows());
    }

    public List<Map<String, Object>> currentMenus(CurrentUser user) {
        if (user.platformAdmin()) {
            return modulesWithMenus(null, true, user);
        }
        return modulesWithMenus(user.tenantId(), false, user);
    }

    public List<Map<String, Object>> orgTree(CurrentUser user, String keyword, String parentKeyword) {
        requireTenant(user);
        return buildTree(jdbcClient.sql("""
                        SELECT o.id, o.parent_id AS parentId, o.org_code AS code, o.org_name AS name,
                               o.org_code AS orgCode, o.org_name AS orgName, o.sort_order AS sortOrder, o.status
                        FROM basic_org_node o
                        LEFT JOIN basic_org_node p ON p.id = o.parent_id
                        WHERE o.tenant_id = :tenantId AND o.deleted = 0
                          AND (:keyword IS NULL OR LOWER(o.org_code) LIKE :keywordLike OR LOWER(o.org_name) LIKE :keywordLike)
                          AND (:parentKeyword IS NULL OR LOWER(p.org_code) LIKE :parentKeywordLike OR LOWER(p.org_name) LIKE :parentKeywordLike)
                        ORDER BY o.parent_id, o.sort_order, o.id
                        """)
                .param("tenantId", user.tenantId())
                .param("keyword", keyword == null || keyword.isBlank() ? null : keyword.toLowerCase())
                .param("keywordLike", keyword == null || keyword.isBlank() ? null : "%" + keyword.toLowerCase() + "%")
                .param("parentKeyword", parentKeyword == null || parentKeyword.isBlank() ? null : parentKeyword.toLowerCase())
                .param("parentKeywordLike", parentKeyword == null || parentKeyword.isBlank() ? null : "%" + parentKeyword.toLowerCase() + "%")
                .query()
                .listOfRows());
    }

    public List<Map<String, Object>> statModelTree(CurrentUser user, Long statModelId) {
        requireTenant(user);
        return buildTree(jdbcClient.sql("""
                        SELECT id, parent_id AS parentId, stat_node_code AS code, stat_node_name AS name,
                               stat_node_code AS statNodeCode, stat_node_name AS statNodeName, sort_order AS sortOrder
                        FROM basic_stat_model_node
                        WHERE tenant_id = :tenantId AND stat_model_id = :statModelId
                        ORDER BY parent_id, sort_order, id
                        """)
                .param("tenantId", user.tenantId())
                .param("statModelId", statModelId)
                .query()
                .listOfRows());
    }

    public List<Map<String, Object>> capacityTree(CurrentUser user) {
        requireTenant(user);
        return buildTree(jdbcClient.sql("""
                        SELECT id, parent_id AS parentId, center_code AS code, center_name AS name,
                               center_code AS centerCode, center_name AS centerName, output_unit AS outputUnit,
                               value_unit AS valueUnit, people_unit AS peopleUnit, area_unit AS areaUnit,
                               sort_order AS sortOrder
                        FROM basic_capacity_center
                        WHERE tenant_id = :tenantId
                        ORDER BY parent_id, sort_order, id
                        """)
                .param("tenantId", user.tenantId())
                .query()
                .listOfRows());
    }

    private List<Map<String, Object>> modulesWithMenus(Long tenantId, boolean platformAdmin, CurrentUser user) {
        String sql;
        Map<String, Object> params = new LinkedHashMap<>();
        if (platformAdmin) {
            sql = """
                    SELECT s.id AS subsystemId, s.subsystem_code AS subsystemCode, s.name_zh AS subsystemNameZh,
                           s.name_en AS subsystemNameEn, s.entry_url AS entryUrl, s.sort_order AS subsystemSort,
                           m.id, m.parent_id AS parentId, m.menu_type AS menuType, m.menu_code AS menuCode,
                           m.permission_code AS permissionCode, m.name_zh AS nameZh, m.name_en AS nameEn,
                           m.icon, m.route_path AS routePath, m.component_path AS componentPath,
                           m.open_type AS openType, m.sort_order AS sortOrder
                    FROM basic_subsystem s
                    LEFT JOIN basic_menu m ON m.subsystem_id = s.id AND m.status = 'ENABLED' AND m.hidden = 0
                    WHERE s.status = 'ENABLED' AND s.subsystem_code = 'platform'
                    ORDER BY s.sort_order, m.parent_id, m.sort_order, m.id
                    """;
        } else {
            sql = """
                    SELECT s.id AS subsystemId, s.subsystem_code AS subsystemCode, s.name_zh AS subsystemNameZh,
                           s.name_en AS subsystemNameEn, s.entry_url AS entryUrl, s.sort_order AS subsystemSort,
                           m.id, m.parent_id AS parentId, m.menu_type AS menuType, m.menu_code AS menuCode,
                           m.permission_code AS permissionCode, m.name_zh AS nameZh, m.name_en AS nameEn,
                           m.icon, m.route_path AS routePath, m.component_path AS componentPath,
                           m.open_type AS openType, m.sort_order AS sortOrder
                    FROM basic_tenant_permission sp
                    JOIN basic_subsystem s ON s.id = sp.subsystem_id
                    LEFT JOIN basic_tenant_permission mp ON mp.tenant_id = sp.tenant_id AND mp.permission_type = 'MENU' AND mp.subsystem_id = s.id AND mp.granted = 1
                    LEFT JOIN basic_menu m ON m.id = mp.menu_id AND m.status = 'ENABLED' AND m.hidden = 0
                    WHERE sp.tenant_id = :tenantId AND sp.permission_type = 'SUBSYSTEM' AND sp.granted = 1
                      AND s.status = 'ENABLED' AND s.subsystem_code = 'basic'
                    ORDER BY s.sort_order, m.parent_id, m.sort_order, m.id
                    """;
            params.put("tenantId", tenantId);
        }
        List<Map<String, Object>> rows = jdbcClient.sql(sql).params(params).query().listOfRows();
        return moduleRowsToTree(rows);
    }

    private List<Map<String, Object>> moduleRowsToTree(List<Map<String, Object>> rows) {
        Map<Long, Map<String, Object>> modules = new LinkedHashMap<>();
        Map<Long, List<Map<String, Object>>> menuRowsByModule = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long subsystemId = ((Number) row.get("subsystemId")).longValue();
            modules.computeIfAbsent(subsystemId, id -> {
                Map<String, Object> module = new LinkedHashMap<>();
                module.put("id", id);
                module.put("code", row.get("subsystemCode"));
                module.put("nameZh", row.get("subsystemNameZh"));
                module.put("nameEn", row.get("subsystemNameEn"));
                module.put("entryUrl", row.get("entryUrl"));
                module.put("menus", new ArrayList<>());
                return module;
            });
            if (row.get("id") != null) {
                menuRowsByModule.computeIfAbsent(subsystemId, id -> new ArrayList<>()).add(row);
            }
        }
        for (Map.Entry<Long, List<Map<String, Object>>> entry : menuRowsByModule.entrySet()) {
            modules.get(entry.getKey()).put("menus", buildTree(entry.getValue()));
        }
        return new ArrayList<>(modules.values());
    }

    private List<Map<String, Object>> buildTree(List<Map<String, Object>> rows) {
        Map<Long, Map<String, Object>> byId = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> node = new LinkedHashMap<>(row);
            node.put("children", new ArrayList<Map<String, Object>>());
            byId.put(((Number) row.get("id")).longValue(), node);
        }
        for (Map<String, Object> node : byId.values()) {
            Object parentIdValue = node.get("parentId");
            if (parentIdValue == null) {
                roots.add(node);
                continue;
            }
            Map<String, Object> parent = byId.get(((Number) parentIdValue).longValue());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
            children.add(node);
        }
        return roots;
    }

    private void requireTenant(CurrentUser user) {
        if (user.tenantId() == null) {
            throw new ApiException("TENANT_REQUIRED", "Tenant context is required");
        }
    }
}
