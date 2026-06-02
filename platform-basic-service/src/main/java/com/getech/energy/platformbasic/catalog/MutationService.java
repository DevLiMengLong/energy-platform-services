package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiException;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MutationService {

    private static final String DEFAULT_USER_PASSWORD = "admin123";

    private final JdbcClient jdbcClient;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;

    public MutationService(JdbcClient jdbcClient, CodeGenerator codeGenerator, PasswordEncoder passwordEncoder) {
        this.jdbcClient = jdbcClient;
        this.codeGenerator = codeGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> createTenant(TenantRequest request) {
        String tenantMark = StringUtils.hasText(request.tenantMark())
                ? request.tenantMark()
                : codeGenerator.next("TENANT", "basic_tenant", "tenant_mark", null);
        try {
            jdbcClient.sql("""
                            INSERT INTO basic_tenant (tenant_mark, tenant_name, industry, contact_name, contact_phone, init_status, status)
                            VALUES (:tenantMark, :tenantName, :industry, :contactName, :contactPhone, 'NOT_INITIALIZED', :status)
                            """)
                    .param("tenantMark", tenantMark)
                    .param("tenantName", request.tenantName())
                    .param("industry", request.industry())
                    .param("contactName", request.contactName())
                    .param("contactPhone", request.contactPhone())
                    .param("status", defaultStatus(request.status()))
                    .update();
        } catch (DuplicateKeyException ex) {
            throw new ApiException("DUPLICATE_TENANT_MARK", "Tenant mark already exists");
        }
        return jdbcClient.sql("SELECT id, tenant_mark AS tenantMark, tenant_name AS tenantName, industry, init_status AS initStatus, status FROM basic_tenant WHERE tenant_mark = :tenantMark")
                .param("tenantMark", tenantMark)
                .query()
                .singleRow();
    }

    @Transactional
    public Map<String, Object> createEnergyType(CurrentUser user, EnergyTypeRequest request) {
        requireTenant(user);
        String energyCode = StringUtils.hasText(request.energyCode())
                ? request.energyCode()
                : codeGenerator.next("ENERGY", "basic_energy_type", "energy_code", user.tenantId());
        try {
            jdbcClient.sql("""
                            INSERT INTO basic_energy_type
                            (tenant_id, energy_code, energy_name, energy_unit, standard_coal_factor, standard_coal_unit, sort_order, icon, remark, status)
                            VALUES (:tenantId, :energyCode, :energyName, :energyUnit, :factor, :standardCoalUnit, :sortOrder, :icon, :remark, :status)
                            """)
                    .param("tenantId", user.tenantId())
                    .param("energyCode", energyCode)
                    .param("energyName", request.energyName())
                    .param("energyUnit", request.energyUnit())
                    .param("factor", request.standardCoalFactor())
                    .param("standardCoalUnit", request.standardCoalUnit())
                    .param("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder())
                    .param("icon", request.icon())
                    .param("remark", request.remark())
                    .param("status", defaultStatus(request.status()))
                    .update();
        } catch (DuplicateKeyException ex) {
            throw new ApiException("DUPLICATE_ENERGY_CODE", "Energy code already exists");
        }
        return jdbcClient.sql("SELECT id, energy_code AS energyCode, energy_name AS energyName, energy_unit AS energyUnit, standard_coal_factor AS standardCoalFactor, standard_coal_unit AS standardCoalUnit, sort_order AS sortOrder, icon, remark, status FROM basic_energy_type WHERE tenant_id = :tenantId AND energy_code = :energyCode")
                .param("tenantId", user.tenantId())
                .param("energyCode", energyCode)
                .query()
                .singleRow();
    }

    @Transactional
    public Map<String, Object> createUser(CurrentUser user, UserRequest request) {
        requireTenant(user);
        Long orgId = resolveOrgId(user, request.orgName());
        List<Map<String, Object>> roles = resolveRoles(user, request.roleName());
        String roleType = roles.stream()
                .anyMatch(role -> "TENANT_ADMIN".equals(role.get("roleCode"))) ? "TENANT_ADMIN" : "TENANT_USER";
        try {
            jdbcClient.sql("""
                            INSERT INTO basic_user
                            (tenant_id, org_id, account, username, phone, email, password_hash, role_type, status)
                            VALUES (:tenantId, :orgId, :account, :username, :phone, :email, :passwordHash, :roleType, :status)
                            """)
                    .param("tenantId", user.tenantId())
                    .param("orgId", orgId)
                    .param("account", request.account())
                    .param("username", request.username())
                    .param("phone", request.phone())
                    .param("email", request.email())
                    .param("passwordHash", passwordEncoder.encode(DEFAULT_USER_PASSWORD))
                    .param("roleType", roleType)
                    .param("status", defaultStatus(request.status()))
                    .update();
        } catch (DuplicateKeyException ex) {
            throw new ApiException("DUPLICATE_USER_ACCOUNT", "User account already exists");
        }
        Long userId = jdbcClient.sql("SELECT id FROM basic_user WHERE account = :account")
                .param("account", request.account())
                .query(Long.class)
                .single();
        for (Map<String, Object> role : roles) {
            jdbcClient.sql("INSERT INTO basic_user_role (tenant_id, user_id, role_id) VALUES (:tenantId, :userId, :roleId)")
                    .param("tenantId", user.tenantId())
                    .param("userId", userId)
                    .param("roleId", role.get("id"))
                    .update();
        }
        return userById(user.tenantId(), userId);
    }

    @Transactional
    public Map<String, Object> createShift(CurrentUser user, ShiftRequest request) {
        requireTenant(user);
        String shiftCode = StringUtils.hasText(request.shiftCode())
                ? request.shiftCode()
                : codeGenerator.next("SHIFT", "basic_shift", "shift_code", user.tenantId());
        int crossDay = Boolean.TRUE.equals(request.crossDay()) ? 1 : 0;
        try {
            jdbcClient.sql("""
                            INSERT INTO basic_shift (tenant_id, shift_code, shift_name, start_time, end_time, cross_day, status)
                            VALUES (:tenantId, :shiftCode, :shiftName, :startTime, :endTime, :crossDay, :status)
                            """)
                    .param("tenantId", user.tenantId())
                    .param("shiftCode", shiftCode)
                    .param("shiftName", request.shiftName())
                    .param("startTime", request.startTime())
                    .param("endTime", request.endTime())
                    .param("crossDay", crossDay)
                    .param("status", defaultStatus(request.status()))
                    .update();
        } catch (DuplicateKeyException ex) {
            throw new ApiException("DUPLICATE_SHIFT_CODE", "Shift code already exists");
        }
        return jdbcClient.sql("SELECT id, shift_code AS shiftCode, shift_name AS shiftName, start_time AS startTime, end_time AS endTime, cross_day AS crossDay, status FROM basic_shift WHERE tenant_id = :tenantId AND shift_code = :shiftCode")
                .param("tenantId", user.tenantId())
                .param("shiftCode", shiftCode)
                .query()
                .singleRow();
    }

    @Transactional
    public void moveOrg(CurrentUser user, MoveOrgRequest request) {
        requireTenant(user);
        if (request.parentId() != null && request.parentId().equals(request.orgId())) {
            throw new ApiException("INVALID_ORG_MOVE", "Organization cannot move under itself");
        }
        int updated = jdbcClient.sql("""
                        UPDATE basic_org_node
                        SET parent_id = :parentId, sort_order = :sortOrder
                        WHERE tenant_id = :tenantId AND id = :orgId
                        """)
                .param("parentId", request.parentId())
                .param("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder())
                .param("tenantId", user.tenantId())
                .param("orgId", request.orgId())
                .update();
        if (updated == 0) {
            throw new ApiException("ORG_NOT_FOUND", "Organization does not exist");
        }
    }

    @Transactional
    public void deleteOrg(CurrentUser user, Long orgId) {
        requireTenant(user);
        List<Map<String, Object>> orgRows = jdbcClient.sql("""
                        SELECT id, parent_id AS parentId
                        FROM basic_org_node
                        WHERE tenant_id = :tenantId AND id = :orgId AND deleted = 0
                        """)
                .param("tenantId", user.tenantId())
                .param("orgId", orgId)
                .query()
                .listOfRows();
        if (orgRows.isEmpty()) {
            throw new ApiException("ORG_NOT_FOUND", "Organization does not exist");
        }
        if (orgRows.get(0).get("parentId") == null) {
            throw new ApiException("ORG_ROOT_DELETE_NOT_ALLOWED", "Root organization cannot be deleted");
        }

        Long childCount = jdbcClient.sql("""
                        SELECT COUNT(1)
                        FROM basic_org_node
                        WHERE tenant_id = :tenantId AND parent_id = :orgId AND deleted = 0
                        """)
                .param("tenantId", user.tenantId())
                .param("orgId", orgId)
                .query(Long.class)
                .single();
        if (childCount > 0) {
            throw new ApiException("ORG_HAS_CHILDREN", "Organization with child nodes cannot be deleted");
        }

        Long userCount = jdbcClient.sql("""
                        SELECT COUNT(1)
                        FROM basic_user
                        WHERE tenant_id = :tenantId AND org_id = :orgId
                        """)
                .param("tenantId", user.tenantId())
                .param("orgId", orgId)
                .query(Long.class)
                .single();
        if (userCount > 0) {
            throw new ApiException("ORG_HAS_USERS", "Organization with users cannot be deleted");
        }

        jdbcClient.sql("""
                        UPDATE basic_org_node
                        SET deleted = 1, status = 'DISABLED', updated_at = CURRENT_TIMESTAMP
                        WHERE tenant_id = :tenantId AND id = :orgId AND deleted = 0
                        """)
                .param("tenantId", user.tenantId())
                .param("orgId", orgId)
                .update();
    }

    @Transactional
    public void replaceUserGroupMembers(CurrentUser user, Long groupId, UserGroupMembersRequest request) {
        requireTenant(user);
        Long exists = jdbcClient.sql("SELECT COUNT(1) FROM basic_user_group WHERE tenant_id = :tenantId AND id = :groupId")
                .param("tenantId", user.tenantId())
                .param("groupId", groupId)
                .query(Long.class)
                .single();
        if (exists == 0) {
            throw new ApiException("GROUP_NOT_FOUND", "User group does not exist");
        }
        jdbcClient.sql("DELETE FROM basic_user_group_member WHERE tenant_id = :tenantId AND group_id = :groupId")
                .param("tenantId", user.tenantId())
                .param("groupId", groupId)
                .update();
        for (Long userId : request.userIds()) {
            Long userExists = jdbcClient.sql("SELECT COUNT(1) FROM basic_user WHERE tenant_id = :tenantId AND id = :userId")
                    .param("tenantId", user.tenantId())
                    .param("userId", userId)
                    .query(Long.class)
                    .single();
            if (userExists > 0) {
                jdbcClient.sql("INSERT INTO basic_user_group_member (tenant_id, group_id, user_id) VALUES (:tenantId, :groupId, :userId)")
                        .param("tenantId", user.tenantId())
                        .param("groupId", groupId)
                        .param("userId", userId)
                        .update();
            }
        }
    }

    @Transactional
    public void bindPoint(CurrentUser user, Long deviceParamId, BindPointRequest request) {
        requireTenant(user);
        Long paramExists = jdbcClient.sql("SELECT COUNT(1) FROM basic_device_param WHERE tenant_id = :tenantId AND id = :id")
                .param("tenantId", user.tenantId())
                .param("id", deviceParamId)
                .query(Long.class)
                .single();
        Long pointExists = jdbcClient.sql("SELECT COUNT(1) FROM basic_collection_point WHERE tenant_id = :tenantId AND id = :id")
                .param("tenantId", user.tenantId())
                .param("id", request.collectionPointId())
                .query(Long.class)
                .single();
        if (paramExists == 0 || pointExists == 0) {
            throw new ApiException("BIND_TARGET_NOT_FOUND", "Device parameter or collection point does not exist");
        }
        jdbcClient.sql("DELETE FROM basic_device_param_point_binding WHERE tenant_id = :tenantId AND device_param_id = :deviceParamId")
                .param("tenantId", user.tenantId())
                .param("deviceParamId", deviceParamId)
                .update();
        try {
            jdbcClient.sql("INSERT INTO basic_device_param_point_binding (tenant_id, device_param_id, collection_point_id) VALUES (:tenantId, :deviceParamId, :collectionPointId)")
                    .param("tenantId", user.tenantId())
                    .param("deviceParamId", deviceParamId)
                    .param("collectionPointId", request.collectionPointId())
                    .update();
        } catch (DuplicateKeyException ex) {
            throw new ApiException("POINT_ALREADY_BOUND", "Collection point has already been bound");
        }
    }

    @Transactional
    public void unbindPoint(CurrentUser user, Long deviceParamId) {
        requireTenant(user);
        jdbcClient.sql("DELETE FROM basic_device_param_point_binding WHERE tenant_id = :tenantId AND device_param_id = :deviceParamId")
                .param("tenantId", user.tenantId())
                .param("deviceParamId", deviceParamId)
                .update();
    }

    @Transactional
    public void saveTenantPermissions(TenantPermissionRequest request) {
        jdbcClient.sql("DELETE FROM basic_tenant_permission WHERE tenant_id = :tenantId")
                .param("tenantId", request.tenantId())
                .update();
        for (Long subsystemId : safeList(request.subsystemIds())) {
            String code = jdbcClient.sql("SELECT subsystem_code FROM basic_subsystem WHERE id = :id")
                    .param("id", subsystemId)
                    .query(String.class)
                    .optional()
                    .orElse(null);
            if (code != null) {
                jdbcClient.sql("INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, permission_type, permission_code, granted) VALUES (:tenantId, :subsystemId, 'SUBSYSTEM', :code, 1)")
                        .param("tenantId", request.tenantId())
                        .param("subsystemId", subsystemId)
                        .param("code", code)
                        .update();
            }
        }
        for (Long menuId : safeList(request.menuIds())) {
            List<Map<String, Object>> menus = jdbcClient.sql("SELECT id, subsystem_id, permission_code FROM basic_menu WHERE id = :id")
                    .param("id", menuId)
                    .query()
                    .listOfRows();
            Map<String, Object> menu = menus.isEmpty() ? null : menus.get(0);
            if (menu != null) {
                jdbcClient.sql("INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted) VALUES (:tenantId, :subsystemId, :menuId, 'MENU', :code, 1)")
                        .param("tenantId", request.tenantId())
                        .param("subsystemId", menu.get("subsystem_id"))
                        .param("menuId", menuId)
                        .param("code", menu.get("permission_code"))
                        .update();
            }
        }
    }

    @Transactional
    public void saveCapacityData(CurrentUser user, BatchValueRequest request) {
        requireTenant(user);
        for (BatchValueRequest.Item item : request.items()) {
            jdbcClient.sql("""
                            INSERT INTO basic_capacity_data
                            (tenant_id, capacity_center_id, data_type, period_type, data_time, data_value, unit, source_type)
                            VALUES (:tenantId, :centerId, :dataType, :periodType, :dataTime, :value, :unit, :sourceType)
                            ON DUPLICATE KEY UPDATE data_value = :value, unit = :unit, source_type = :sourceType
                            """)
                    .param("tenantId", user.tenantId())
                    .param("centerId", item.capacityCenterId())
                    .param("dataType", item.dataType())
                    .param("periodType", item.periodType())
                    .param("dataTime", item.dataTime())
                    .param("value", item.value())
                    .param("unit", item.unit())
                    .param("sourceType", item.sourceType() == null ? "MANUAL" : item.sourceType())
                    .update();
        }
    }

    @Transactional
    public void saveIndicatorData(CurrentUser user, BatchValueRequest request) {
        requireTenant(user);
        for (BatchValueRequest.Item item : request.items()) {
            jdbcClient.sql("""
                            INSERT INTO basic_indicator_value
                            (tenant_id, indicator_id, stat_node_id, period_type, data_time, indicator_value, source_type)
                            VALUES (:tenantId, :indicatorId, :statNodeId, :periodType, :dataTime, :value, :sourceType)
                            ON DUPLICATE KEY UPDATE indicator_value = :value, source_type = :sourceType
                            """)
                    .param("tenantId", user.tenantId())
                    .param("indicatorId", item.indicatorId())
                    .param("statNodeId", item.statNodeId())
                    .param("periodType", item.periodType())
                    .param("dataTime", item.dataTime())
                    .param("value", item.value())
                    .param("sourceType", item.sourceType() == null ? "MANUAL" : item.sourceType())
                    .update();
        }
    }

    private void requireTenant(CurrentUser user) {
        if (user.tenantId() == null) {
            throw new ApiException("TENANT_REQUIRED", "Tenant context is required");
        }
    }

    private String defaultStatus(String status) {
        return StringUtils.hasText(status) ? status : "ENABLED";
    }

    private Long resolveOrgId(CurrentUser user, String orgName) {
        List<Map<String, Object>> rows = jdbcClient.sql("""
                        SELECT id
                        FROM basic_org_node
                        WHERE tenant_id = :tenantId AND deleted = 0 AND status = 'ENABLED' AND org_name = :orgName
                        ORDER BY id
                        LIMIT 1
                        """)
                .param("tenantId", user.tenantId())
                .param("orgName", orgName)
                .query()
                .listOfRows();
        if (rows.isEmpty()) {
            throw new ApiException("ORG_NOT_FOUND", "Organization does not exist");
        }
        return ((Number) rows.get(0).get("id")).longValue();
    }

    private List<Map<String, Object>> resolveRoles(CurrentUser user, List<String> roleNames) {
        return roleNames.stream()
                .distinct()
                .map(roleName -> {
                    List<Map<String, Object>> rows = jdbcClient.sql("""
                                    SELECT id, role_code AS roleCode, role_name AS roleName
                                    FROM basic_role
                                    WHERE tenant_id = :tenantId AND status = 'ENABLED' AND role_name = :roleName
                                    ORDER BY id
                                    LIMIT 1
                                    """)
                            .param("tenantId", user.tenantId())
                            .param("roleName", roleName)
                            .query()
                            .listOfRows();
                    if (rows.isEmpty()) {
                        throw new ApiException("ROLE_NOT_FOUND", "Role does not exist");
                    }
                    return rows.get(0);
                })
                .toList();
    }

    private Map<String, Object> userById(Long tenantId, Long userId) {
        return jdbcClient.sql("""
                        SELECT u.id, u.account, u.username, u.phone, u.email,
                               (SELECT r.role_name FROM basic_user_role ur JOIN basic_role r ON r.id = ur.role_id WHERE ur.user_id = u.id ORDER BY r.id LIMIT 1) AS roleName,
                               o.org_name AS orgName, u.status
                        FROM basic_user u
                        LEFT JOIN basic_org_node o ON o.id = u.org_id
                        WHERE u.tenant_id = :tenantId AND u.id = :userId
                        """)
                .param("tenantId", tenantId)
                .param("userId", userId)
                .query()
                .singleRow();
    }

    private List<Long> safeList(List<Long> values) {
        return values == null ? List.of() : values;
    }
}
