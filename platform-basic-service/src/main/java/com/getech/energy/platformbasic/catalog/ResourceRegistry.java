package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.common.ApiException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ResourceRegistry {

    private final Map<String, ResourceDefinition> definitions = new LinkedHashMap<>();

    public ResourceRegistry() {
        platform();
        basic();
    }

    public ResourceDefinition require(String key) {
        ResourceDefinition definition = definitions.get(key);
        if (definition == null) {
            throw new ApiException("RESOURCE_NOT_FOUND", "Unsupported resource: " + key);
        }
        return definition;
    }

    private void add(ResourceDefinition definition) {
        definitions.put(definition.key(), definition);
    }

    private void platform() {
        add(new ResourceDefinition(
                "platform.tenants",
                "basic_tenant t",
                "t.id, t.tenant_mark AS tenantMark, t.tenant_name AS tenantName, t.industry, t.contact_name AS contactName, t.contact_phone AS contactPhone, t.init_status AS initStatus, t.status, t.created_at AS createdAt",
                "t.deleted = 0",
                List.of("t.tenant_mark", "t.tenant_name", "t.industry"),
                Map.of(
                        "industry", "t.industry",
                        "status", "t.status",
                        "initStatus", "t.init_status",
                        "createdAt", "t.created_at"
                ),
                false,
                null,
                "t.id DESC"));
        add(new ResourceDefinition(
                "platform.tenant-admins",
                "basic_user u JOIN basic_tenant t ON t.id = u.tenant_id",
                "u.id, t.tenant_mark AS tenantMark, t.tenant_name AS tenantName, u.account, u.username, u.phone, u.email, u.last_login_at AS lastLoginAt, u.status",
                "u.role_type = 'TENANT_ADMIN'",
                List.of("u.account", "u.username", "u.email", "t.tenant_mark", "t.tenant_name"),
                Map.of(
                        "tenantMark", "t.tenant_mark",
                        "status", "u.status",
                        "lastLoginAt", "u.last_login_at"
                ),
                false,
                null,
                "u.id DESC"));
        add(new ResourceDefinition(
                "platform.subsystems",
                "basic_subsystem s",
                "s.id, s.subsystem_code AS subsystemCode, s.name_zh AS nameZh, s.name_en AS nameEn, s.description, s.entry_url AS entryUrl, s.status, s.sort_order AS sortOrder",
                "1 = 1",
                List.of("s.subsystem_code", "s.name_zh", "s.name_en"),
                Map.of("status", "s.status"),
                false,
                null,
                "s.sort_order ASC, s.id ASC"));
        add(new ResourceDefinition(
                "platform.tenant-permissions",
                """
                (SELECT t.id, t.tenant_mark AS tenantMark, t.tenant_name AS tenantName,
                        COALESCE(GROUP_CONCAT(DISTINCT s.name_zh), '-') AS subsystemNames,
                        COALESCE(GROUP_CONCAT(DISTINCT s.subsystem_code), '') AS subsystemCodes,
                        COUNT(DISTINCT p.permission_code) AS permissionCount,
                        CASE WHEN COUNT(p.id) > 0 THEN 'AUTHORIZED' ELSE 'UNAUTHORIZED' END AS authStatus
                 FROM basic_tenant t
                 LEFT JOIN basic_tenant_permission p ON p.tenant_id = t.id AND p.granted = 1
                 LEFT JOIN basic_subsystem s ON s.id = p.subsystem_id AND p.permission_type = 'SUBSYSTEM'
                 WHERE t.deleted = 0
                 GROUP BY t.id, t.tenant_mark, t.tenant_name) tp
                """,
                "tp.id, tp.tenantMark, tp.tenantName, tp.subsystemNames, tp.subsystemCodes, tp.permissionCount, tp.authStatus",
                "1 = 1",
                List.of("tp.tenantMark", "tp.tenantName"),
                Map.of(
                        "subsystemCode", "tp.subsystemCodes",
                        "authStatus", "tp.authStatus"
                ),
                false,
                null,
                "tp.id ASC"));
    }

    private void basic() {
        String userRoleName = "(SELECT r.role_name FROM basic_user_role ur JOIN basic_role r ON r.id = ur.role_id WHERE ur.user_id = u.id ORDER BY r.id LIMIT 1)";
        add(new ResourceDefinition(
                "basic.users",
                "basic_user u LEFT JOIN basic_org_node o ON o.id = u.org_id",
                "u.id, u.account, u.username, u.phone, u.email, " + userRoleName + " AS roleName, o.org_name AS orgName, u.status",
                "u.role_type IN ('TENANT_ADMIN', 'TENANT_USER')",
                List.of("u.account", "u.username", "u.phone", "u.email"),
                Map.of(
                        "phone", "u.phone",
                        "email", "u.email",
                        "roleName", userRoleName,
                        "status", "u.status"
                ),
                true,
                "u.tenant_id",
                "u.id DESC"));
        add(new ResourceDefinition(
                "basic.user-groups",
                "basic_user_group g",
                "g.id, g.group_code AS groupCode, g.group_name AS groupName, (SELECT COUNT(1) FROM basic_user_group_member m WHERE m.group_id = g.id) AS memberCount, g.remark, g.status",
                "1 = 1",
                List.of("g.group_code", "g.group_name"),
                Map.of("status", "g.status"),
                true,
                "g.tenant_id",
                "g.id DESC"));
        add(new ResourceDefinition(
                "basic.roles",
                "basic_role r",
                "r.id, r.role_code AS roleCode, r.role_name AS roleName, r.menu_scope AS menuScope, r.status, (SELECT COUNT(1) FROM basic_user_role ur WHERE ur.role_id = r.id) AS userCount, r.data_scope AS dataScope, r.created_at AS createdAt",
                "1 = 1",
                List.of("r.role_code", "r.role_name", "r.menu_scope"),
                Map.of(
                        "status", "r.status",
                        "menuScope", "r.menu_scope",
                        "createdAt", "r.created_at"
                ),
                true,
                "r.tenant_id",
                "r.id ASC"));
        add(new ResourceDefinition(
                "basic.dictionaries",
                "basic_dict_type d",
                "d.id, d.dict_code AS dictCode, d.dict_name AS dictName, d.dict_type AS dictType, (SELECT COUNT(1) FROM basic_dict_item i WHERE i.dict_type_id = d.id) AS itemCount, d.status, d.remark",
                "1 = 1",
                List.of("d.dict_code", "d.dict_name", "d.dict_type"),
                Map.of(
                        "dictType", "d.dict_type",
                        "status", "d.status"
                ),
                true,
                "d.tenant_id",
                "d.id ASC"));
        add(new ResourceDefinition(
                "basic.energy-types",
                "basic_energy_type e",
                "e.id, e.energy_code AS energyCode, e.energy_name AS energyName, e.energy_unit AS energyUnit, e.standard_coal_factor AS standardCoalFactor, e.standard_coal_unit AS standardCoalUnit, e.sort_order AS sortOrder, e.icon, e.remark, e.status",
                "1 = 1",
                List.of("e.energy_code", "e.energy_name", "e.energy_unit"),
                Map.of(
                        "energyName", "e.energy_name",
                        "energyUnit", "e.energy_unit",
                        "status", "e.status"
                ),
                true,
                "e.tenant_id",
                "e.sort_order ASC, e.id ASC"));
        add(new ResourceDefinition(
                "basic.energy-prices",
                "basic_energy_price p JOIN basic_energy_type e ON e.id = p.energy_type_id",
                "p.id, e.energy_name AS energyName, e.energy_unit AS energyUnit, p.price_type AS priceType, p.period_name AS periodName, p.start_time AS startTime, p.end_time AS endTime, p.unit_price AS unitPrice, p.price_unit AS priceUnit, p.effective_start AS effectiveStart, p.effective_end AS effectiveEnd, p.status",
                "1 = 1",
                List.of("e.energy_name", "p.price_type", "p.period_name"),
                Map.of(
                        "energyName", "e.energy_name",
                        "priceType", "p.price_type",
                        "effectiveStart", "p.effective_start",
                        "effectiveEnd", "p.effective_end",
                        "status", "p.status"
                ),
                true,
                "p.tenant_id",
                "p.effective_start DESC, p.id ASC"));
        add(new ResourceDefinition(
                "basic.device-models",
                "basic_device_model m",
                "m.id, m.model_code AS modelCode, m.model_name AS modelName, m.model_type AS modelType, (SELECT COUNT(1) FROM basic_device_model_param p WHERE p.model_id = m.id) AS paramCount, m.status, m.sort_order AS sortOrder",
                "1 = 1",
                List.of("m.model_code", "m.model_name", "m.model_type"),
                Map.of(
                        "modelType", "m.model_type",
                        "status", "m.status"
                ),
                true,
                "m.tenant_id",
                "m.sort_order ASC, m.id ASC"));
        String bindingStatus = "CASE WHEN EXISTS (SELECT 1 FROM basic_device_param p JOIN basic_device_param_point_binding b ON b.device_param_id = p.id WHERE p.device_id = d.id) THEN 'BOUND' ELSE 'UNBOUND' END";
        add(new ResourceDefinition(
                "basic.devices",
                "basic_device d JOIN basic_device_model m ON m.id = d.model_id",
                "d.id, " + bindingStatus + " AS bindingStatus, m.model_type AS modelType, m.model_name AS modelName, m.model_code AS modelCode, d.device_name AS deviceName, d.device_code AS deviceCode, d.device_label AS deviceLabel, d.install_location AS installLocation",
                "1 = 1",
                List.of("d.device_code", "d.device_name", "m.model_name"),
                Map.of(
                        "modelName", "m.model_name",
                        "bindingStatus", bindingStatus
                ),
                true,
                "d.tenant_id",
                "d.id ASC"));
        add(new ResourceDefinition(
                "basic.collection-points",
                "basic_collection_point p",
                "p.id, p.collection_model_mark AS collectionModelMark, p.collection_model_name AS collectionModelName, p.collection_device_mark AS collectionDeviceMark, p.collection_device_name AS collectionDeviceName, p.collection_param_mark AS collectionParamMark, p.collection_param_name AS collectionParamName, p.business_name AS businessName, p.data_type AS dataType, p.status",
                "1 = 1",
                List.of("p.collection_model_mark", "p.collection_device_mark", "p.collection_param_mark", "p.business_name"),
                Map.of(
                        "collectionModelMark", "p.collection_model_mark",
                        "collectionDeviceMark", "p.collection_device_mark",
                        "collectionParamMark", "p.collection_param_mark",
                        "businessName", "p.business_name",
                        "dataType", "p.data_type",
                        "status", "p.status"
                ),
                true,
                "p.tenant_id",
                "p.id ASC"));
        add(new ResourceDefinition(
                "basic.stat-models",
                "basic_stat_model m JOIN basic_energy_type e ON e.id = m.energy_type_id",
                "m.id, e.energy_name AS energyName, m.stat_model_code AS statModelCode, m.stat_model_name AS statModelName, m.status",
                "1 = 1",
                List.of("m.stat_model_code", "m.stat_model_name", "e.energy_name"),
                Map.of("status", "m.status"),
                true,
                "m.tenant_id",
                "m.id ASC"));
        add(new ResourceDefinition(
                "basic.capacity-data",
                "basic_capacity_data d JOIN basic_capacity_center c ON c.id = d.capacity_center_id",
                "d.id, c.center_code AS centerCode, c.center_name AS centerName, d.data_type AS dataType, d.period_type AS periodType, d.data_time AS dataTime, d.data_value AS dataValue, d.unit, d.source_type AS sourceType",
                "1 = 1",
                List.of("c.center_code", "c.center_name", "d.data_type", "d.data_time"),
                Map.of(
                        "dataType", "d.data_type",
                        "periodType", "d.period_type",
                        "dataTime", "d.data_time",
                        "sourceType", "d.source_type"
                ),
                true,
                "d.tenant_id",
                "d.id DESC"));
        add(new ResourceDefinition(
                "basic.unit-consumption-relations",
                "basic_unit_consumption_relation r JOIN basic_stat_model_node n ON n.id = r.stat_node_id JOIN basic_capacity_center c ON c.id = r.capacity_center_id",
                "r.id, n.stat_node_code AS statNodeCode, n.stat_node_name AS statNodeName, c.center_code AS centerCode, c.center_name AS centerName, c.output_unit AS outputUnit, c.value_unit AS valueUnit, c.people_unit AS peopleUnit, c.area_unit AS areaUnit",
                "1 = 1",
                List.of("n.stat_node_name", "c.center_name", "c.center_code"),
                Map.of(),
                true,
                "r.tenant_id",
                "r.id ASC"));
        add(new ResourceDefinition(
                "basic.indicator-data",
                "basic_indicator_value v JOIN basic_indicator_definition d ON d.id = v.indicator_id LEFT JOIN basic_stat_model_node n ON n.id = v.stat_node_id",
                "v.id, n.stat_node_name AS statNodeName, d.indicator_code AS indicatorCode, d.indicator_name AS indicatorName, d.indicator_type AS indicatorType, v.period_type AS periodType, v.data_time AS dataTime, v.indicator_value AS indicatorValue, d.unit, v.source_type AS sourceType",
                "1 = 1",
                List.of("n.stat_node_name", "d.indicator_code", "d.indicator_name", "d.indicator_type"),
                Map.of(
                        "indicatorType", "d.indicator_type",
                        "periodType", "v.period_type",
                        "dataTime", "v.data_time",
                        "sourceType", "v.source_type"
                ),
                true,
                "v.tenant_id",
                "v.id DESC"));
        add(new ResourceDefinition(
                "basic.shifts",
                "basic_shift s",
                "s.id, s.shift_code AS shiftCode, s.shift_name AS shiftName, s.start_time AS startTime, s.end_time AS endTime, s.cross_day AS crossDay, s.status",
                "1 = 1",
                List.of("s.shift_code", "s.shift_name"),
                Map.of(
                        "crossDay", "s.cross_day",
                        "status", "s.status"
                ),
                true,
                "s.tenant_id",
                "s.id ASC"));
    }
}
