CREATE TABLE log_operation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL,
  tenant_id BIGINT,
  user_id BIGINT,
  account VARCHAR(64),
  subsystem_code VARCHAR(64) NOT NULL,
  module_code VARCHAR(64) NOT NULL,
  action_code VARCHAR(64) NOT NULL,
  action_name VARCHAR(128) NOT NULL,
  resource_type VARCHAR(64),
  resource_id VARCHAR(64),
  request_method VARCHAR(16),
  request_uri VARCHAR(512),
  client_ip VARCHAR(64),
  success TINYINT NOT NULL DEFAULT 1,
  message VARCHAR(512),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_log_operation_tenant_time (tenant_id, created_at),
  KEY idx_log_operation_trace (trace_id)
);

CREATE TABLE log_login (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL,
  tenant_id BIGINT,
  user_id BIGINT,
  account VARCHAR(64) NOT NULL,
  login_status VARCHAR(32) NOT NULL,
  failure_reason VARCHAR(256),
  client_ip VARCHAR(64),
  user_agent VARCHAR(512),
  login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_log_login_account_time (account, login_at),
  KEY idx_log_login_tenant_time (tenant_id, login_at),
  KEY idx_log_login_trace (trace_id)
);
