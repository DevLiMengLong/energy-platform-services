CREATE TABLE seed_seq_25 (
  n INT PRIMARY KEY
);

INSERT INTO seed_seq_25 (n) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25);

INSERT INTO basic_tenant (tenant_mark, tenant_name, industry, contact_name, contact_phone, init_status, status)
SELECT CONCAT('tenant_demo_', LPAD(n, 2, '0')),
       CONCAT('演示租户', LPAD(n, 2, '0')),
       CASE MOD(n, 3) WHEN 0 THEN '电子制造' WHEN 1 THEN '制造业' ELSE '能源管理' END,
       CONCAT('演示联系人', LPAD(n, 2, '0')),
       CONCAT('1389000', LPAD(n, 4, '0')),
       CASE WHEN MOD(n, 4) = 0 THEN 'NOT_INITIALIZED' ELSE 'INITIALIZED' END,
       CASE WHEN MOD(n, 6) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

INSERT INTO basic_subsystem (subsystem_code, name_zh, name_en, description, entry_url, status, sort_order)
SELECT CONCAT('demo-subsystem-', LPAD(n, 2, '0')),
       CONCAT('演示子系统', LPAD(n, 2, '0')),
       CONCAT('Demo Subsystem ', LPAD(n, 2, '0')),
       '用于分页和筛选验证的演示子系统',
       CONCAT('/placeholder/demo-', LPAD(n, 2, '0')),
       CASE WHEN MOD(n, 7) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       100 + n
FROM seed_seq_25;

INSERT INTO basic_menu (id, subsystem_id, parent_id, menu_type, menu_code, permission_code, name_zh, name_en, icon, route_path, component_path, sort_order, hidden, status)
VALUES (90, 2, NULL, 'MENU', 'basic.root', 'BASIC', '基础信息', 'Basic Info', 'ShieldCheck', NULL, 'basic/index.vue', 0, 0, 'ENABLED');

UPDATE basic_menu
SET parent_id = 90
WHERE subsystem_id = 2 AND id BETWEEN 10 AND 24;

INSERT INTO basic_menu (subsystem_id, parent_id, menu_type, menu_code, permission_code, name_zh, name_en, icon, route_path, component_path, sort_order, hidden, status)
SELECT s.id, NULL, 'MENU',
       CONCAT('demo.menu.', LPAD(q.n, 2, '0')),
       CONCAT('DEMO_MENU_', LPAD(q.n, 2, '0')),
       CONCAT('演示菜单', LPAD(q.n, 2, '0')),
       CONCAT('Demo Menu ', LPAD(q.n, 2, '0')),
       'Menu',
       CONCAT('/demo/menu-', LPAD(q.n, 2, '0')),
       CONCAT('demo/Menu', LPAD(q.n, 2, '0')),
       q.n,
       CASE WHEN MOD(q.n, 8) = 0 THEN 1 ELSE 0 END,
       CASE WHEN MOD(q.n, 9) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25 q
JOIN basic_subsystem s ON s.subsystem_code = CONCAT('demo-subsystem-', LPAD(q.n, 2, '0'));

INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted)
SELECT t.id, s.id, NULL, 'SUBSYSTEM', s.subsystem_code, 1
FROM basic_tenant t
JOIN basic_subsystem s ON s.subsystem_code = 'basic'
WHERE t.tenant_mark LIKE 'tenant_demo_%';

INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted)
SELECT t.id, m.subsystem_id, m.id, 'MENU', m.permission_code, 1
FROM basic_tenant t
JOIN basic_menu m ON m.subsystem_id = 2 AND m.id BETWEEN 10 AND 24
WHERE t.tenant_mark LIKE 'tenant_demo_%';

INSERT INTO basic_user (tenant_id, org_id, account, username, phone, email, password_hash, role_type, status, last_login_at)
SELECT t.id, NULL,
       CONCAT('tenant_demo_admin_', LPAD(q.n, 2, '0')),
       CONCAT('演示租户管理员', LPAD(q.n, 2, '0')),
       CONCAT('1391000', LPAD(q.n, 4, '0')),
       CONCAT('tenant_demo_admin_', LPAD(q.n, 2, '0'), '@example.com'),
       '{noop}admin123',
       'TENANT_ADMIN',
       CASE WHEN MOD(q.n, 5) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       '2026-05-25 09:10:00'
FROM seed_seq_25 q
JOIN basic_tenant t ON t.tenant_mark = CONCAT('tenant_demo_', LPAD(q.n, 2, '0'));

INSERT INTO basic_org_node (tenant_id, parent_id, org_code, org_name, sort_order, status)
SELECT 1, CASE WHEN n <= 8 THEN 1 WHEN n <= 16 THEN 2 ELSE 4 END,
       CONCAT('ORG_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示组织', LPAD(n, 2, '0')),
       20 + n,
       CASE WHEN MOD(n, 9) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

INSERT INTO basic_user (tenant_id, org_id, account, username, phone, email, password_hash, role_type, status, last_login_at)
SELECT 1, CASE WHEN MOD(n, 3) = 0 THEN 5 WHEN MOD(n, 3) = 1 THEN 2 ELSE 4 END,
       CONCAT('demo_user_', LPAD(n, 2, '0')),
       CONCAT('演示用户', LPAD(n, 2, '0')),
       CONCAT('1381000', LPAD(n, 4, '0')),
       CONCAT('demo_user_', LPAD(n, 2, '0'), '@example.com'),
       '{noop}admin123',
       'TENANT_USER',
       CASE WHEN MOD(n, 6) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       '2026-05-25 10:10:00'
FROM seed_seq_25;

INSERT INTO basic_user_role (tenant_id, user_id, role_id)
SELECT 1, u.id, CASE WHEN MOD(q.n, 3) = 0 THEN 3 WHEN MOD(q.n, 3) = 1 THEN 2 ELSE 1 END
FROM seed_seq_25 q
JOIN basic_user u ON u.account = CONCAT('demo_user_', LPAD(q.n, 2, '0'));

INSERT INTO basic_user_group (tenant_id, group_code, group_name, remark, status)
SELECT 1,
       CONCAT('UG_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示用户组', LPAD(n, 2, '0')),
       '用于分页、筛选和回归验证的用户组',
       CASE WHEN MOD(n, 8) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

INSERT INTO basic_role (tenant_id, role_code, role_name, menu_scope, status, data_scope)
SELECT 1,
       CONCAT('ROLE_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示角色', LPAD(n, 2, '0')),
       CASE MOD(n, 3) WHEN 0 THEN '基础信息' WHEN 1 THEN '基础信息/报表' ELSE '全部菜单' END,
       CASE WHEN MOD(n, 9) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       'TENANT'
FROM seed_seq_25;

INSERT INTO basic_dict_type (tenant_id, dict_code, dict_name, dict_type, status, remark)
SELECT 1,
       CONCAT('demo_dict_', LPAD(n, 2, '0')),
       CONCAT('演示字典', LPAD(n, 2, '0')),
       CASE WHEN MOD(n, 2) = 0 THEN 'SYSTEM' ELSE 'BUSINESS' END,
       CASE WHEN MOD(n, 7) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       '用于字典列表分页和筛选验证'
FROM seed_seq_25;

INSERT INTO basic_energy_type (tenant_id, energy_code, energy_name, energy_unit, standard_coal_factor, standard_coal_unit, sort_order, icon, remark, status)
SELECT 1,
       CONCAT('ENERGY_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示能源', LPAD(n, 2, '0')),
       CASE MOD(n, 4) WHEN 0 THEN 'kWh' WHEN 1 THEN 't' WHEN 2 THEN 'Nm3' ELSE 'MJ' END,
       0.100000 + n * 0.001000,
       'kgce/unit',
       20 + n,
       'Leaf',
       '用于能源类型分页和筛选验证',
       CASE WHEN MOD(n, 10) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

INSERT INTO basic_energy_price (tenant_id, energy_type_id, price_type, period_name, start_time, end_time, unit_price, price_unit, effective_start, effective_end, status)
SELECT 1, e.id,
       CASE WHEN MOD(q.n, 2) = 0 THEN 'TIME_OF_USE' ELSE 'AVERAGE' END,
       CASE WHEN MOD(q.n, 2) = 0 THEN '峰' ELSE NULL END,
       CASE WHEN MOD(q.n, 2) = 0 THEN '09:00' ELSE NULL END,
       CASE WHEN MOD(q.n, 2) = 0 THEN '18:00' ELSE NULL END,
       0.500000 + q.n * 0.030000,
       CASE e.energy_unit WHEN 't' THEN '元/t' WHEN 'Nm3' THEN '元/Nm3' WHEN 'MJ' THEN '元/MJ' ELSE '元/kWh' END,
       '2026-01-01',
       '2026-12-31',
       CASE WHEN MOD(q.n, 8) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25 q
JOIN basic_energy_type e ON e.tenant_id = 1 AND e.energy_code = CONCAT('ENERGY_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_device_model (tenant_id, model_code, model_name, model_type, status, sort_order)
SELECT 1,
       CONCAT('MODEL_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示设备模型', LPAD(n, 2, '0')),
       CASE WHEN MOD(n, 2) = 0 THEN 'METER' ELSE 'DEVICE' END,
       CASE WHEN MOD(n, 8) = 0 THEN 'DISABLED' ELSE 'ENABLED' END,
       20 + n
FROM seed_seq_25;

INSERT INTO basic_device_model_param (tenant_id, model_id, param_code, param_name, data_type, unit, status, sort_order)
SELECT 1, m.id,
       CONCAT('PARAM_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示参数', LPAD(q.n, 2, '0')),
       CASE WHEN MOD(q.n, 2) = 0 THEN 'ACCUMULATED' ELSE 'INSTANT' END,
       CASE WHEN MOD(q.n, 2) = 0 THEN 'kWh' ELSE 'kW' END,
       'ENABLED',
       1
FROM seed_seq_25 q
JOIN basic_device_model m ON m.tenant_id = 1 AND m.model_code = CONCAT('MODEL_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_device (tenant_id, model_id, device_code, device_name, device_label, install_location)
SELECT 1, m.id,
       CONCAT('DEV_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示设备', LPAD(q.n, 2, '0')),
       CONCAT('演示标签', LPAD(q.n, 2, '0')),
       CONCAT('演示区域', LPAD(q.n, 2, '0'))
FROM seed_seq_25 q
JOIN basic_device_model m ON m.tenant_id = 1 AND m.model_code = CONCAT('MODEL_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_device_param (tenant_id, device_id, param_code, param_name, unit, data_type, status)
SELECT 1, d.id,
       CONCAT('PARAM_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示参数', LPAD(q.n, 2, '0')),
       CASE WHEN MOD(q.n, 2) = 0 THEN 'kWh' ELSE 'kW' END,
       CASE WHEN MOD(q.n, 2) = 0 THEN 'ACCUMULATED' ELSE 'INSTANT' END,
       'ENABLED'
FROM seed_seq_25 q
JOIN basic_device d ON d.tenant_id = 1 AND d.device_code = CONCAT('DEV_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_collection_point (tenant_id, collection_model_mark, collection_model_name, collection_device_mark, collection_device_name, collection_param_mark, collection_param_name, business_name, data_type, status)
SELECT 1,
       CONCAT('demo_model_', LPAD(n, 2, '0')),
       CONCAT('演示采集模型', LPAD(n, 2, '0')),
       CONCAT('demo_device_', LPAD(n, 2, '0')),
       CONCAT('演示采集设备', LPAD(n, 2, '0')),
       CONCAT('demo_param_', LPAD(n, 2, '0')),
       CONCAT('演示采集参数', LPAD(n, 2, '0')),
       CONCAT('演示业务', LPAD(n, 2, '0')),
       'NUMERIC',
       CASE WHEN MOD(n, 9) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

INSERT INTO basic_device_param_point_binding (tenant_id, device_param_id, collection_point_id)
SELECT 1, dp.id, cp.id
FROM seed_seq_25 q
JOIN basic_device d ON d.tenant_id = 1 AND d.device_code = CONCAT('DEV_DEMO_', LPAD(q.n, 2, '0'))
JOIN basic_device_param dp ON dp.device_id = d.id AND dp.param_code = CONCAT('PARAM_DEMO_', LPAD(q.n, 2, '0'))
JOIN basic_collection_point cp ON cp.tenant_id = 1 AND cp.collection_model_mark = CONCAT('demo_model_', LPAD(q.n, 2, '0'));

INSERT INTO basic_stat_model (tenant_id, energy_type_id, stat_model_code, stat_model_name, status)
SELECT 1, e.id,
       CONCAT('STAT_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示统计模型', LPAD(q.n, 2, '0')),
       CASE WHEN MOD(q.n, 8) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25 q
JOIN basic_energy_type e ON e.tenant_id = 1 AND e.energy_code = CONCAT('ENERGY_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_stat_model_node (tenant_id, stat_model_id, parent_id, stat_node_code, stat_node_name, sort_order)
SELECT 1, m.id, NULL,
       CONCAT('STAT_NODE_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示统计节点', LPAD(q.n, 2, '0')),
       q.n
FROM seed_seq_25 q
JOIN basic_stat_model m ON m.tenant_id = 1 AND m.stat_model_code = CONCAT('STAT_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_capacity_center (tenant_id, parent_id, center_code, center_name, output_unit, value_unit, people_unit, area_unit, sort_order)
SELECT 1, CASE WHEN n <= 8 THEN 1 WHEN n <= 16 THEN 2 ELSE 3 END,
       CONCAT('CAP_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示产能中心', LPAD(n, 2, '0')),
       '件', '万元', '人', 'm2', 20 + n
FROM seed_seq_25;

INSERT INTO basic_capacity_data (tenant_id, capacity_center_id, data_type, period_type, data_time, data_value, unit, source_type)
SELECT 1, c.id,
       'OUTPUT',
       CASE WHEN MOD(q.n, 2) = 0 THEN 'MONTH' ELSE 'DAY' END,
       CASE WHEN MOD(q.n, 2) = 0 THEN '2026-05' ELSE '2026-05-01' END,
       1000 + q.n * 37,
       '件',
       CASE WHEN MOD(q.n, 3) = 0 THEN 'IMPORT' ELSE 'MANUAL' END
FROM seed_seq_25 q
JOIN basic_capacity_center c ON c.tenant_id = 1 AND c.center_code = CONCAT('CAP_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_unit_consumption_relation (tenant_id, stat_node_id, capacity_center_id)
SELECT 1, n.id, c.id
FROM seed_seq_25 q
JOIN basic_stat_model_node n ON n.tenant_id = 1 AND n.stat_node_code = CONCAT('STAT_NODE_DEMO_', LPAD(q.n, 2, '0'))
JOIN basic_capacity_center c ON c.tenant_id = 1 AND c.center_code = CONCAT('CAP_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_indicator_definition (tenant_id, energy_type_id, stat_model_id, indicator_code, indicator_name, indicator_type, unit, source_type)
SELECT 1, e.id, m.id,
       CONCAT('IND_DEMO_', LPAD(q.n, 2, '0')),
       CONCAT('演示指标', LPAD(q.n, 2, '0')),
       CASE MOD(q.n, 3) WHEN 0 THEN 'OUTPUT' WHEN 1 THEN 'ENERGY_USAGE' ELSE 'UNIT_ENERGY' END,
       CASE MOD(q.n, 3) WHEN 0 THEN '件' WHEN 1 THEN 'kWh' ELSE 'kWh/件' END,
       CASE WHEN MOD(q.n, 2) = 0 THEN 'STAT_MODEL' ELSE 'MANUAL' END
FROM seed_seq_25 q
JOIN basic_energy_type e ON e.tenant_id = 1 AND e.energy_code = CONCAT('ENERGY_DEMO_', LPAD(q.n, 2, '0'))
JOIN basic_stat_model m ON m.tenant_id = 1 AND m.stat_model_code = CONCAT('STAT_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_indicator_value (tenant_id, indicator_id, stat_node_id, period_type, data_time, indicator_value, source_type)
SELECT 1, d.id, n.id,
       'MONTH',
       '2026-05',
       500 + q.n * 18.5,
       CASE WHEN MOD(q.n, 2) = 0 THEN 'STAT_MODEL' ELSE 'MANUAL' END
FROM seed_seq_25 q
JOIN basic_indicator_definition d ON d.tenant_id = 1 AND d.indicator_code = CONCAT('IND_DEMO_', LPAD(q.n, 2, '0'))
JOIN basic_stat_model_node n ON n.tenant_id = 1 AND n.stat_node_code = CONCAT('STAT_NODE_DEMO_', LPAD(q.n, 2, '0'));

INSERT INTO basic_shift (tenant_id, shift_code, shift_name, start_time, end_time, cross_day, status)
SELECT 1,
       CONCAT('SHIFT_DEMO_', LPAD(n, 2, '0')),
       CONCAT('演示班次', LPAD(n, 2, '0')),
       CASE WHEN MOD(n, 3) = 0 THEN '00:00' WHEN MOD(n, 3) = 1 THEN '08:00' ELSE '16:00' END,
       CASE WHEN MOD(n, 3) = 0 THEN '08:00' WHEN MOD(n, 3) = 1 THEN '16:00' ELSE '00:00' END,
       CASE WHEN MOD(n, 3) = 2 THEN 1 ELSE 0 END,
       CASE WHEN MOD(n, 8) = 0 THEN 'DISABLED' ELSE 'ENABLED' END
FROM seed_seq_25;

DROP TABLE seed_seq_25;
