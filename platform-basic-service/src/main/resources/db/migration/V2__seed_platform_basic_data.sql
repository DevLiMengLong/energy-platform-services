INSERT INTO basic_tenant (id, tenant_mark, tenant_name, industry, contact_name, contact_phone, init_status, status) VALUES
(1, 'tenant_a', '全链路测试工厂', '制造业', '张工', '13800001001', 'INITIALIZED', 'ENABLED'),
(2, 'tenant_b', '华东示例园区', '能源管理', '李工', '13800001002', 'INITIALIZED', 'ENABLED'),
(3, 'tenant_c', '碳管理演示企业', '电子制造', '王工', '13800001003', 'NOT_INITIALIZED', 'ENABLED');

INSERT INTO basic_subsystem (id, subsystem_code, name_zh, name_en, description, entry_url, status, sort_order) VALUES
(1, 'platform', '平台管理', 'Platform Admin', '租户、子系统、菜单和租户授权', '/platform-admin/', 'ENABLED', 1),
(2, 'basic', '基础信息', 'Basic Info', '组织、用户、能源、设备、点位、统计模型等基础数据', '/basic-info/', 'ENABLED', 2),
(3, 'data-cleaning', '数据清洗', 'Data Cleaning', '工业数据清洗预留模块', '/placeholder/data-cleaning', 'ENABLED', 3),
(4, 'report', '报表分析', 'Reports', '报表分析预留模块', '/placeholder/report', 'ENABLED', 4),
(5, 'government', '政府平台对接', 'Government', '政府平台对接预留模块', '/placeholder/government', 'ENABLED', 5),
(6, 'alarm', '告警通知', 'Alarms', 'Alarms', '/placeholder/alarm', 'ENABLED', 6);

INSERT INTO basic_menu (id, subsystem_id, parent_id, menu_type, menu_code, permission_code, name_zh, name_en, icon, route_path, component_path, sort_order, hidden, status) VALUES
(1, 1, NULL, 'MENU', 'platform.tenants', 'PLATFORM_TENANT', '租户管理', 'Tenants', 'Building2', '/platform/tenants', 'platform/TenantPage', 1, 0, 'ENABLED'),
(2, 1, NULL, 'MENU', 'platform.tenantAdmins', 'PLATFORM_TENANT_ADMIN', '租户管理员管理', 'Tenant Admins', 'UserCog', '/platform/tenant-admins', 'platform/TenantAdminPage', 2, 0, 'ENABLED'),
(3, 1, NULL, 'MENU', 'platform.subsystems', 'PLATFORM_SUBSYSTEM', '子系统管理', 'Subsystems', 'Boxes', '/platform/subsystems', 'platform/SubsystemPage', 3, 0, 'ENABLED'),
(4, 1, NULL, 'MENU', 'platform.menus', 'PLATFORM_MENU', '菜单管理', 'Menus', 'MenuSquare', '/platform/menus', 'platform/MenuPage', 4, 0, 'ENABLED'),
(5, 1, NULL, 'MENU', 'platform.tenantPermissions', 'PLATFORM_TENANT_PERMISSION', '租户权限分配', 'Tenant Permissions', 'ShieldCheck', '/platform/tenant-permissions', 'platform/TenantPermissionPage', 5, 0, 'ENABLED'),
(10, 2, NULL, 'MENU', 'basic.orgNodes', 'BASIC_ORG', '组织管理', 'Organizations', 'Network', '/basic/org-nodes', 'basic/OrgNodePage', 1, 0, 'ENABLED'),
(11, 2, NULL, 'MENU', 'basic.users', 'BASIC_USER', '用户管理', 'Users', 'Users', '/basic/users', 'basic/UserPage', 2, 0, 'ENABLED'),
(12, 2, NULL, 'MENU', 'basic.userGroups', 'BASIC_USER_GROUP', '用户组管理', 'User Groups', 'UsersRound', '/basic/user-groups', 'basic/UserGroupPage', 3, 0, 'ENABLED'),
(13, 2, NULL, 'MENU', 'basic.roles', 'BASIC_ROLE', '角色权限', 'Roles', 'KeyRound', '/basic/roles', 'basic/RolePage', 4, 0, 'ENABLED'),
(14, 2, NULL, 'MENU', 'basic.dictionaries', 'BASIC_DICT', '数据字典', 'Dictionaries', 'BookOpen', '/basic/dictionaries', 'basic/DictionaryPage', 5, 0, 'ENABLED'),
(15, 2, NULL, 'MENU', 'basic.energyTypes', 'BASIC_ENERGY_TYPE', '能源类型', 'Energy Types', 'Leaf', '/basic/energy-types', 'basic/EnergyTypePage', 6, 0, 'ENABLED'),
(16, 2, NULL, 'MENU', 'basic.energyPrices', 'BASIC_ENERGY_PRICE', '能源价格', 'Energy Prices', 'BadgeDollarSign', '/basic/energy-prices', 'basic/EnergyPricePage', 7, 0, 'ENABLED'),
(17, 2, NULL, 'MENU', 'basic.deviceModels', 'BASIC_DEVICE_MODEL', '设备模型', 'Device Models', 'Cpu', '/basic/device-models', 'basic/DeviceModelPage', 8, 0, 'ENABLED'),
(18, 2, NULL, 'MENU', 'basic.devices', 'BASIC_DEVICE', '设备管理', 'Devices', 'Gauge', '/basic/devices', 'basic/DevicePage', 9, 0, 'ENABLED'),
(19, 2, NULL, 'MENU', 'basic.statModels', 'BASIC_STAT_MODEL', '统计模型', 'Stat Models', 'GitBranch', '/basic/stat-models', 'basic/StatModelPage', 10, 0, 'ENABLED'),
(20, 2, NULL, 'MENU', 'basic.capacityCenters', 'BASIC_CAPACITY', '产能中心', 'Capacity Centers', 'Factory', '/basic/capacity-centers', 'basic/CapacityCenterPage', 11, 0, 'ENABLED'),
(21, 2, NULL, 'MENU', 'basic.unitConsumption', 'BASIC_UNIT_CONSUMPTION', '单耗配置', 'Unit Consumption', 'Scale', '/basic/unit-consumption', 'basic/UnitConsumptionPage', 12, 0, 'ENABLED'),
(22, 2, NULL, 'MENU', 'basic.indicators', 'BASIC_INDICATOR', '指标配置', 'Indicators', 'Activity', '/basic/indicators', 'basic/IndicatorPage', 13, 0, 'ENABLED'),
(23, 2, NULL, 'MENU', 'basic.collectionPoints', 'BASIC_COLLECTION_POINT', '采集点位', 'Collection Points', 'RadioTower', '/basic/collection-points', 'basic/CollectionPointPage', 14, 0, 'ENABLED'),
(24, 2, NULL, 'MENU', 'basic.shifts', 'BASIC_SHIFT', '班次配置', 'Shifts', 'Clock3', '/basic/shifts', 'basic/ShiftPage', 15, 0, 'ENABLED');

INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted) VALUES
(1, 2, NULL, 'SUBSYSTEM', 'basic', 1),
(1, 3, NULL, 'SUBSYSTEM', 'data-cleaning', 1),
(1, 4, NULL, 'SUBSYSTEM', 'report', 1),
(2, 2, NULL, 'SUBSYSTEM', 'basic', 1),
(2, 3, NULL, 'SUBSYSTEM', 'data-cleaning', 1),
(3, 2, NULL, 'SUBSYSTEM', 'basic', 1);

INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted)
SELECT 1, subsystem_id, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2;
INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted)
SELECT 2, subsystem_id, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2 AND id <= 18;
INSERT INTO basic_tenant_permission (tenant_id, subsystem_id, menu_id, permission_type, permission_code, granted)
SELECT 3, subsystem_id, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2 AND id IN (10,11,15,23,24);

INSERT INTO basic_org_node (id, tenant_id, parent_id, org_code, org_name, sort_order, status) VALUES
(1, 1, NULL, 'ORG_ROOT', '全链路测试工厂', 1, 'ENABLED'),
(2, 1, 1, 'ORG_WS_01', '一号车间', 1, 'ENABLED'),
(3, 1, 2, 'ORG_LINE_A', 'A 产线', 1, 'ENABLED'),
(4, 1, 1, 'ORG_DEVICE', '设备部', 2, 'ENABLED'),
(5, 1, 1, 'ORG_ADMIN', '管理部', 3, 'ENABLED'),
(20, 2, NULL, 'ORG_ROOT', '华东示例园区', 1, 'ENABLED');

INSERT INTO basic_user (id, tenant_id, org_id, account, username, phone, email, password_hash, role_type, status, last_login_at) VALUES
(1, NULL, NULL, 'admin', '平台管理员', '13800000000', 'admin@example.com', '{noop}admin123', 'PLATFORM_ADMIN', 'ENABLED', '2026-05-25 09:00:00'),
(2, 1, 1, 'tenant_a_admin', '租户超管', '13800001000', 'tenant_a_admin@example.com', '{noop}admin123', 'TENANT_ADMIN', 'ENABLED', '2026-05-25 09:10:00'),
(3, 2, 20, 'tenant_b_admin', '园区管理员', '13800002000', 'tenant_b_admin@example.com', '{noop}admin123', 'TENANT_ADMIN', 'ENABLED', '2026-05-24 16:40:00'),
(10, 1, 2, 'energy_mgr', '能源主管', '13800001001', 'energy_mgr@example.com', '{noop}admin123', 'TENANT_USER', 'ENABLED', '2026-05-25 10:10:00'),
(11, 1, 4, 'device_ops', '设备维护', '13800001002', 'device_ops@example.com', '{noop}admin123', 'TENANT_USER', 'ENABLED', '2026-05-25 10:20:00'),
(12, 1, 5, 'report_user', '报表专员', '13800001003', 'report_user@example.com', '{noop}admin123', 'TENANT_USER', 'DISABLED', '2026-05-20 11:30:00'),
(13, 2, 20, 'park_energy', '园区能源主管', '13800002001', 'park_energy@example.com', '{noop}admin123', 'TENANT_USER', 'ENABLED', '2026-05-25 11:30:00');

INSERT INTO basic_role (id, tenant_id, role_code, role_name, menu_scope, status, data_scope) VALUES
(1, 1, 'TENANT_ADMIN', '租户超管', '全部菜单', 'ENABLED', 'TENANT'),
(2, 1, 'ENERGY_MANAGER', '能源主管', '基础信息/报表', 'ENABLED', 'TENANT'),
(3, 1, 'DEVICE_OPERATOR', '设备维护', '基础信息', 'ENABLED', 'TENANT'),
(4, 2, 'TENANT_ADMIN', '租户超管', '全部菜单', 'ENABLED', 'TENANT');

INSERT INTO basic_user_role (tenant_id, user_id, role_id) VALUES
(1, 2, 1),
(1, 10, 2),
(1, 11, 3),
(1, 12, 2),
(2, 3, 4);

INSERT INTO basic_role_permission (tenant_id, role_id, menu_id, permission_type, permission_code, granted)
SELECT 1, 1, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2;
INSERT INTO basic_role_permission (tenant_id, role_id, menu_id, permission_type, permission_code, granted)
SELECT 1, 2, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2 AND id IN (10,11,15,16,19,20,21,22,23,24);
INSERT INTO basic_role_permission (tenant_id, role_id, menu_id, permission_type, permission_code, granted)
SELECT 1, 3, id, 'MENU', permission_code, 1 FROM basic_menu WHERE subsystem_id = 2 AND id IN (17,18,23);

INSERT INTO basic_user_group (id, tenant_id, group_code, group_name, remark, status) VALUES
(1, 1, 'UG_ENERGY_OPS', '能源运营组', '负责能源运营和报表查看', 'ENABLED'),
(2, 1, 'UG_DEVICE_MAINT', '设备维护组', '负责设备巡检和点位维护', 'ENABLED'),
(3, 1, 'UG_REPORT_VIEW', '报表查看组', '负责报表查看和数据核对', 'ENABLED');

INSERT INTO basic_user_group_member (tenant_id, group_id, user_id) VALUES
(1, 1, 10),
(1, 1, 12),
(1, 2, 11),
(1, 3, 12);

INSERT INTO basic_dict_type (id, tenant_id, dict_code, dict_name, dict_type, status, remark) VALUES
(1, 1, 'energy_indicator_type', '指标类型', 'BUSINESS', 'ENABLED', '用能、产量、产值、单耗等指标分类'),
(2, 1, 'device_param_data_type', '参数数据类型', 'SYSTEM', 'ENABLED', '瞬时量、累计量'),
(3, 1, 'shift_cross_day', '班次跨天标识', 'SYSTEM', 'ENABLED', '是、否'),
(4, 1, 'price_type', '价格类型', 'BUSINESS', 'ENABLED', '平均价格、分时价格');

INSERT INTO basic_dict_item (tenant_id, dict_type_id, item_code, item_name, sort_order, status) VALUES
(1, 1, 'ENERGY_USAGE', '用能指标', 1, 'ENABLED'),
(1, 1, 'OUTPUT', '产量指标', 2, 'ENABLED'),
(1, 1, 'UNIT_ENERGY', '单位能耗指标', 3, 'ENABLED'),
(1, 2, 'INSTANT', '瞬时量', 1, 'ENABLED'),
(1, 2, 'ACCUMULATED', '累计量', 2, 'ENABLED'),
(1, 3, 'YES', '是', 1, 'ENABLED'),
(1, 3, 'NO', '否', 2, 'ENABLED');

INSERT INTO basic_energy_type (id, tenant_id, energy_code, energy_name, energy_unit, standard_coal_factor, standard_coal_unit, sort_order, icon, remark, status) VALUES
(1, 1, 'ENERGY_ELECTRIC', '电', 'kWh', 0.122900, 'kgce/kWh', 1, 'Zap', '电力当量值，引用标准：GB/T2589-2020 综合能耗计算通则', 'ENABLED'),
(2, 1, 'ENERGY_WATER', '水', 't', 0.257100, 'kgce/t', 2, 'Droplets', '新水，引用标准：GB/T2589-2020 综合能耗计算通则', 'ENABLED'),
(3, 1, 'ENERGY_GAS', '天然气', 'Nm3', 1.214300, 'kgce/m3', 3, 'Flame', '天然气折标系数预置，可按企业标准维护', 'ENABLED'),
(4, 1, 'ENERGY_AIR', '压缩空气', 'Nm3', 0.040000, 'kgce/m3', 4, 'Wind', '压缩空气折标系数', 'ENABLED'),
(5, 1, 'ENERGY_HEAT', '热力', 'MJ', 0.034120, 'kgce/MJ', 5, 'SunMedium', '外购热力折标系数', 'ENABLED'),
(20, 2, 'ENERGY_ELECTRIC', '电', 'kWh', 0.122900, 'kgce/kWh', 1, 'Zap', '园区电力', 'ENABLED');

INSERT INTO basic_energy_price (tenant_id, energy_type_id, price_type, period_name, start_time, end_time, unit_price, price_unit, effective_start, effective_end, status) VALUES
(1, 1, 'AVERAGE', NULL, NULL, NULL, 0.820000, '元/kWh', '2026-01-01', '2026-12-31', 'ENABLED'),
(1, 1, 'TIME_OF_USE', '尖峰', '09:00', '11:00', 1.180000, '元/kWh', '2027-01-01', '2027-12-31', 'ENABLED'),
(1, 1, 'TIME_OF_USE', '峰', '14:00', '18:00', 1.020000, '元/kWh', '2027-01-01', '2027-12-31', 'ENABLED'),
(1, 1, 'TIME_OF_USE', '谷', '22:00', '06:00', 0.420000, '元/kWh', '2027-01-01', '2027-12-31', 'ENABLED'),
(1, 2, 'AVERAGE', NULL, NULL, NULL, 4.300000, '元/t', '2026-01-01', '2026-12-31', 'ENABLED'),
(1, 3, 'AVERAGE', NULL, NULL, NULL, 3.680000, '元/Nm3', '2026-01-01', '2026-12-31', 'ENABLED');

INSERT INTO basic_device_model (id, tenant_id, model_code, model_name, model_type, status, sort_order) VALUES
(1, 1, 'MODEL_ELECTRIC_METER', '电表', 'METER', 'ENABLED', 1),
(2, 1, 'MODEL_WATER_METER', '水表', 'METER', 'ENABLED', 2),
(3, 1, 'MODEL_PRODUCTION', '产量设备', 'DEVICE', 'ENABLED', 3);

INSERT INTO basic_device_model_param (id, tenant_id, model_id, param_code, param_name, data_type, unit, status, sort_order) VALUES
(1, 1, 1, 'KWH_TOTAL', '正向有功总电量', 'ACCUMULATED', 'kWh', 'ENABLED', 1),
(2, 1, 1, 'P_TOTAL', '总有功功率', 'INSTANT', 'kW', 'ENABLED', 2),
(3, 1, 2, 'FLOW_TOTAL', '用水量', 'ACCUMULATED', 't', 'ENABLED', 1),
(4, 1, 3, 'OUTPUT_TOTAL', '产量', 'ACCUMULATED', '件', 'ENABLED', 1);

INSERT INTO basic_device (id, tenant_id, model_id, device_code, device_name, device_label, install_location) VALUES
(1, 1, 1, 'DEV_EM_001', '一号车间总电表', '总表', '一号车间配电室'),
(2, 1, 1, 'DEV_EM_LINE_A', 'A 产线电表', '产线电表', 'A 产线配电柜'),
(3, 1, 2, 'DEV_WM_001', '一号车间水表', '用水总表', '一号车间水泵房'),
(4, 1, 3, 'DEV_PM_LINE_A', 'A 产线产量设备', '产量采集', 'A 产线末端');

INSERT INTO basic_device_param (id, tenant_id, device_id, param_code, param_name, unit, data_type, status) VALUES
(1, 1, 1, 'KWH_TOTAL', '正向有功总电量', 'kWh', 'ACCUMULATED', 'ENABLED'),
(2, 1, 1, 'P_TOTAL', '总有功功率', 'kW', 'INSTANT', 'ENABLED'),
(3, 1, 2, 'KWH_TOTAL', '正向有功总电量', 'kWh', 'ACCUMULATED', 'ENABLED'),
(4, 1, 3, 'FLOW_TOTAL', '用水量', 't', 'ACCUMULATED', 'ENABLED'),
(5, 1, 4, 'OUTPUT_TOTAL', '产量', '件', 'ACCUMULATED', 'ENABLED');

INSERT INTO basic_collection_point (id, tenant_id, collection_model_mark, collection_model_name, collection_device_mark, collection_device_name, collection_param_mark, collection_param_name, business_name, data_type, status) VALUES
(1, 1, 'electric', '电力采集', 'gw_a_001', '一号车间网关', 'kwh_total', '总电量', '总电量', 'NUMERIC', 'ENABLED'),
(2, 1, 'electric', '电力采集', 'gw_a_001', '一号车间网关', 'p_total', '总功率', '总功率', 'NUMERIC', 'ENABLED'),
(3, 1, 'electric', '电力采集', 'gw_line_a', 'A 产线网关', 'kwh_total', '总电量', 'A 产线总电量', 'NUMERIC', 'ENABLED'),
(4, 1, 'water', '水务采集', 'gw_w_001', '水务网关', 'flow_total', '用水量', '用水量', 'NUMERIC', 'ENABLED');

INSERT INTO basic_device_param_point_binding (tenant_id, device_param_id, collection_point_id) VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3),
(1, 4, 4);

INSERT INTO basic_stat_model (id, tenant_id, energy_type_id, stat_model_code, stat_model_name, status) VALUES
(1, 1, 1, 'STAT_ELECTRIC_MAIN', '用电统计模型', 'ENABLED'),
(2, 1, 2, 'STAT_WATER_MAIN', '用水统计模型', 'ENABLED');

INSERT INTO basic_stat_model_node (id, tenant_id, stat_model_id, parent_id, stat_node_code, stat_node_name, sort_order) VALUES
(1, 1, 1, NULL, 'STAT_NODE_FACTORY', '全厂用电', 1),
(2, 1, 1, 1, 'STAT_NODE_WS_01', '一号车间用电', 1),
(3, 1, 1, 2, 'STAT_NODE_LINE_A', 'A 产线用电', 1),
(4, 1, 2, NULL, 'STAT_NODE_WATER_FACTORY', '全厂用水', 1);

INSERT INTO basic_stat_node_param_binding (tenant_id, stat_node_id, device_param_id) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 3, 3),
(1, 4, 4);

INSERT INTO basic_capacity_center (id, tenant_id, parent_id, center_code, center_name, output_unit, value_unit, people_unit, area_unit, sort_order) VALUES
(1, 1, NULL, 'CAP_ROOT', '全厂产能中心', '件', '万元', '人', 'm2', 1),
(2, 1, 1, 'CAP_WS_01', '一号车间产能中心', '件', '万元', '人', 'm2', 1),
(3, 1, 2, 'CAP_LINE_A', 'A 产线产能中心', '件', '万元', '人', 'm2', 1);

INSERT INTO basic_capacity_data (tenant_id, capacity_center_id, data_type, period_type, data_time, data_value, unit, source_type) VALUES
(1, 1, 'OUTPUT', 'DAY', '2026-05-01', 3820, '件', 'MANUAL'),
(1, 2, 'OUTPUT', 'DAY', '2026-05-01', 2180, '件', 'IMPORT'),
(1, 3, 'OUTPUT', 'DAY', '2026-05-01', 1260, '件', 'MANUAL');

INSERT INTO basic_unit_consumption_relation (tenant_id, stat_node_id, capacity_center_id) VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3);

INSERT INTO basic_indicator_definition (id, tenant_id, energy_type_id, stat_model_id, indicator_code, indicator_name, indicator_type, unit, source_type) VALUES
(1, 1, 1, 1, 'POWER_USAGE', '用电量', 'ENERGY_USAGE', 'kWh', 'STAT_MODEL'),
(2, 1, 1, 1, 'UNIT_POWER_OUTPUT', '单位产量电耗', 'UNIT_ENERGY', 'kWh/件', 'MANUAL'),
(3, 1, NULL, NULL, 'OUTPUT', '产量', 'OUTPUT', '件', 'CAPACITY');

INSERT INTO basic_indicator_value (tenant_id, indicator_id, stat_node_id, period_type, data_time, indicator_value, source_type) VALUES
(1, 1, 1, 'MONTH', '2026-05', 12880.420000, 'STAT_MODEL'),
(1, 1, 2, 'MONTH', '2026-05', 8620.350000, 'STAT_MODEL'),
(1, 1, 3, 'MONTH', '2026-05', 4210.180000, 'STAT_MODEL');

INSERT INTO basic_shift (id, tenant_id, shift_code, shift_name, start_time, end_time, cross_day, status) VALUES
(1, 1, 'SHIFT_DAY', '白班', '08:00', '20:00', 0, 'ENABLED'),
(2, 1, 'SHIFT_NIGHT', '夜班', '20:00', '08:00', 1, 'ENABLED'),
(3, 1, 'SHIFT_MID', '中班', '14:00', '22:00', 0, 'DISABLED');
