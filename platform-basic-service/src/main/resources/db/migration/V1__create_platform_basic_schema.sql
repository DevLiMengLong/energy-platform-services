CREATE TABLE basic_tenant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_mark VARCHAR(64) NOT NULL,
  tenant_name VARCHAR(128) NOT NULL,
  industry VARCHAR(64),
  contact_name VARCHAR(64),
  contact_phone VARCHAR(32),
  init_status VARCHAR(32) NOT NULL DEFAULT 'NOT_INITIALIZED',
  init_message VARCHAR(512),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_tenant_mark (tenant_mark)
);

CREATE TABLE basic_subsystem (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subsystem_code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(128) NOT NULL,
  name_en VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  entry_url VARCHAR(256),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_subsystem_code (subsystem_code)
);

CREATE TABLE basic_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subsystem_id BIGINT NOT NULL,
  parent_id BIGINT,
  menu_type VARCHAR(16) NOT NULL,
  menu_code VARCHAR(64) NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  name_zh VARCHAR(128) NOT NULL,
  name_en VARCHAR(128) NOT NULL,
  icon VARCHAR(64),
  open_type VARCHAR(32) NOT NULL DEFAULT 'COMPONENT',
  route_path VARCHAR(256),
  component_path VARCHAR(256),
  api_path VARCHAR(512),
  sort_order INT NOT NULL DEFAULT 0,
  hidden TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_menu_code (menu_code),
  KEY idx_basic_menu_parent (parent_id),
  KEY idx_basic_menu_subsystem (subsystem_id)
);

CREATE TABLE basic_tenant_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  subsystem_id BIGINT,
  menu_id BIGINT,
  permission_type VARCHAR(32) NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  granted TINYINT NOT NULL DEFAULT 1,
  out_of_scope TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_tenant_perm (tenant_id, permission_type, permission_code),
  KEY idx_basic_tenant_perm_tenant (tenant_id),
  KEY idx_basic_tenant_perm_menu (menu_id)
);

CREATE TABLE basic_org_node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  parent_id BIGINT,
  org_code VARCHAR(64) NOT NULL,
  org_name VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_org_code (tenant_id, org_code),
  KEY idx_basic_org_parent (tenant_id, parent_id)
);

CREATE TABLE basic_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT,
  org_id BIGINT,
  account VARCHAR(64) NOT NULL,
  username VARCHAR(64) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  password_hash VARCHAR(256) NOT NULL,
  role_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  last_login_at TIMESTAMP NULL,
  data_scope VARCHAR(32) NOT NULL DEFAULT 'TENANT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_user_account (account),
  KEY idx_basic_user_tenant (tenant_id),
  KEY idx_basic_user_org (org_id)
);

CREATE TABLE basic_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  menu_scope VARCHAR(256),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  data_scope VARCHAR(32) NOT NULL DEFAULT 'TENANT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_role_code (tenant_id, role_code)
);

CREATE TABLE basic_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_basic_user_role (user_id, role_id),
  KEY idx_basic_user_role_tenant (tenant_id)
);

CREATE TABLE basic_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  menu_id BIGINT,
  permission_type VARCHAR(32) NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  granted TINYINT NOT NULL DEFAULT 1,
  out_of_scope TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_role_perm (role_id, permission_type, permission_code),
  KEY idx_basic_role_perm_tenant (tenant_id)
);

CREATE TABLE basic_user_group (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  group_code VARCHAR(64) NOT NULL,
  group_name VARCHAR(128) NOT NULL,
  remark VARCHAR(512),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_group_code (tenant_id, group_code)
);

CREATE TABLE basic_user_group_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_group_member (group_id, user_id),
  KEY idx_basic_group_member_tenant (tenant_id)
);

CREATE TABLE basic_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  dict_code VARCHAR(64) NOT NULL,
  dict_name VARCHAR(128) NOT NULL,
  dict_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  remark VARCHAR(512),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_dict_type (tenant_id, dict_code)
);

CREATE TABLE basic_dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  dict_type_id BIGINT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  remark VARCHAR(512),
  UNIQUE KEY uk_basic_dict_item (tenant_id, dict_type_id, item_code)
);

CREATE TABLE basic_energy_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  energy_code VARCHAR(64) NOT NULL,
  energy_name VARCHAR(128) NOT NULL,
  energy_unit VARCHAR(32) NOT NULL,
  standard_coal_factor DECIMAL(18,6) NOT NULL DEFAULT 0,
  standard_coal_unit VARCHAR(32),
  sort_order INT NOT NULL DEFAULT 0,
  icon VARCHAR(64),
  remark VARCHAR(512),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_energy_code (tenant_id, energy_code)
);

CREATE TABLE basic_energy_price (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  energy_type_id BIGINT NOT NULL,
  price_type VARCHAR(32) NOT NULL,
  period_name VARCHAR(64),
  start_time VARCHAR(8),
  end_time VARCHAR(8),
  unit_price DECIMAL(18,6) NOT NULL,
  price_unit VARCHAR(32) NOT NULL,
  effective_start DATE NOT NULL,
  effective_end DATE NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_basic_energy_price_scope (tenant_id, energy_type_id, effective_start, effective_end)
);

CREATE TABLE basic_device_model (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  model_code VARCHAR(64) NOT NULL,
  model_name VARCHAR(128) NOT NULL,
  model_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_device_model (tenant_id, model_code)
);

CREATE TABLE basic_device_model_param (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  model_id BIGINT NOT NULL,
  param_code VARCHAR(64) NOT NULL,
  param_name VARCHAR(128) NOT NULL,
  data_type VARCHAR(32) NOT NULL,
  unit VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  sort_order INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_basic_model_param (tenant_id, model_id, param_code)
);

CREATE TABLE basic_device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  model_id BIGINT NOT NULL,
  device_code VARCHAR(64) NOT NULL,
  device_name VARCHAR(128) NOT NULL,
  device_label VARCHAR(128),
  install_location VARCHAR(256),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_device_code (tenant_id, device_code),
  KEY idx_basic_device_model (tenant_id, model_id)
);

CREATE TABLE basic_device_param (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  param_code VARCHAR(64) NOT NULL,
  param_name VARCHAR(128) NOT NULL,
  unit VARCHAR(32),
  data_type VARCHAR(32) NOT NULL DEFAULT 'NUMERIC',
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  UNIQUE KEY uk_basic_device_param (tenant_id, device_id, param_code),
  KEY idx_basic_device_param_device (device_id)
);

CREATE TABLE basic_collection_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  collection_model_mark VARCHAR(64) NOT NULL,
  collection_model_name VARCHAR(128),
  collection_device_mark VARCHAR(64) NOT NULL,
  collection_device_name VARCHAR(128),
  collection_param_mark VARCHAR(64) NOT NULL,
  collection_param_name VARCHAR(128),
  business_name VARCHAR(128) NOT NULL,
  data_type VARCHAR(32) NOT NULL DEFAULT 'NUMERIC',
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_point_triplet (tenant_id, collection_model_mark, collection_device_mark, collection_param_mark)
);

CREATE TABLE basic_device_param_point_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  device_param_id BIGINT NOT NULL,
  collection_point_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_param_binding (tenant_id, device_param_id),
  UNIQUE KEY uk_basic_point_binding (tenant_id, collection_point_id)
);

CREATE TABLE basic_stat_model (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  energy_type_id BIGINT NOT NULL,
  stat_model_code VARCHAR(64) NOT NULL,
  stat_model_name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_stat_model (tenant_id, energy_type_id, stat_model_code)
);

CREATE TABLE basic_stat_model_node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  stat_model_id BIGINT NOT NULL,
  parent_id BIGINT,
  stat_node_code VARCHAR(64) NOT NULL,
  stat_node_name VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_basic_stat_node (tenant_id, stat_model_id, stat_node_code),
  KEY idx_basic_stat_node_parent (tenant_id, stat_model_id, parent_id)
);

CREATE TABLE basic_stat_node_param_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  stat_node_id BIGINT NOT NULL,
  device_param_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_stat_node_param (tenant_id, stat_node_id, device_param_id)
);

CREATE TABLE basic_capacity_center (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  parent_id BIGINT,
  center_code VARCHAR(64) NOT NULL,
  center_name VARCHAR(128) NOT NULL,
  output_unit VARCHAR(32) DEFAULT '件',
  value_unit VARCHAR(32) DEFAULT '万元',
  people_unit VARCHAR(32) DEFAULT '人',
  area_unit VARCHAR(32) DEFAULT 'm2',
  sort_order INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_basic_capacity_center (tenant_id, center_code),
  KEY idx_basic_capacity_parent (tenant_id, parent_id)
);

CREATE TABLE basic_capacity_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  capacity_center_id BIGINT NOT NULL,
  data_type VARCHAR(32) NOT NULL,
  period_type VARCHAR(16) NOT NULL,
  data_time VARCHAR(32) NOT NULL,
  data_value DECIMAL(18,6),
  unit VARCHAR(32),
  source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
  UNIQUE KEY uk_basic_capacity_data (tenant_id, capacity_center_id, data_type, period_type, data_time)
);

CREATE TABLE basic_unit_consumption_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  stat_node_id BIGINT NOT NULL,
  capacity_center_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_unit_relation (tenant_id, stat_node_id, capacity_center_id)
);

CREATE TABLE basic_indicator_definition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  energy_type_id BIGINT,
  stat_model_id BIGINT,
  indicator_code VARCHAR(64) NOT NULL,
  indicator_name VARCHAR(128) NOT NULL,
  indicator_type VARCHAR(64) NOT NULL,
  unit VARCHAR(32),
  source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
  UNIQUE KEY uk_basic_indicator_def (tenant_id, indicator_code, energy_type_id, stat_model_id)
);

CREATE TABLE basic_indicator_value (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  indicator_id BIGINT NOT NULL,
  stat_node_id BIGINT,
  period_type VARCHAR(16) NOT NULL,
  data_time VARCHAR(32) NOT NULL,
  indicator_value DECIMAL(18,6),
  source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
  UNIQUE KEY uk_basic_indicator_value (tenant_id, indicator_id, stat_node_id, period_type, data_time)
);

CREATE TABLE basic_shift (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  shift_code VARCHAR(64) NOT NULL,
  shift_name VARCHAR(128) NOT NULL,
  start_time VARCHAR(8) NOT NULL,
  end_time VARCHAR(8) NOT NULL,
  cross_day TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_basic_shift (tenant_id, shift_code)
);
