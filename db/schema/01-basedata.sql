-- Open-TMS 基础数据模块表
-- PostgreSQL
-- 执行顺序: 1

-- 业务单元表
CREATE TABLE tms_business_unit_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    legal_person VARCHAR(50),
    address VARCHAR(500),
    tax_no VARCHAR(50),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_business_unit_t IS '业务单元表';
CREATE INDEX idx_bu_code ON tms_business_unit_t(code);
CREATE INDEX idx_bu_status ON tms_business_unit_t(status);

-- 交易员表
CREATE TABLE tms_trader_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    en_name VARCHAR(50),
    department VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(100),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_trader_t IS '交易员表';
CREATE INDEX idx_trader_code ON tms_trader_t(code);
CREATE INDEX idx_trader_status ON tms_trader_t(status);

-- 币种表
CREATE TABLE tms_currency_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    decimal_places INT NOT NULL DEFAULT 2,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_currency_t IS '币种表';
CREATE INDEX idx_currency_code ON tms_currency_t(code);

-- 国家/地区表
CREATE TABLE tms_country_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    en_name VARCHAR(100),
    timezone VARCHAR(50),
    country_no VARCHAR(10),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_country_t IS '国家/地区表';
CREATE INDEX idx_country_code ON tms_country_t(code);

-- 节假日表
CREATE TABLE tms_holiday_t (
    id BIGSERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    is_adjacent CHAR(1) NOT NULL DEFAULT '0',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_holiday_t IS '节假日表';
CREATE INDEX idx_holiday_date ON tms_holiday_t(holiday_date);
CREATE INDEX idx_holiday_country ON tms_holiday_t(country_code);

-- 银行表
CREATE TABLE tms_bank_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    swift_code VARCHAR(20),
    country_code VARCHAR(10),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_bank_t IS '银行表';
CREATE INDEX idx_bank_code ON tms_bank_t(code);
CREATE INDEX idx_bank_swift ON tms_bank_t(swift_code);

-- 交易对手表 (修复: cp_type -> counterparty_type)
CREATE TABLE tms_counterparty_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    counterparty_type VARCHAR(20),
    country_code VARCHAR(10),
    swift_code VARCHAR(20),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_counterparty_t IS '交易对手表';
CREATE INDEX idx_cp_code ON tms_counterparty_t(code);
CREATE INDEX idx_cp_type ON tms_counterparty_t(counterparty_type);

-- 对手账户表 (修复: cp_id -> counterparty_id)
CREATE TABLE tms_counterparty_account_t (
    id BIGSERIAL PRIMARY KEY,
    counterparty_id BIGINT NOT NULL,
    account_no VARCHAR(50) NOT NULL,
    account_name VARCHAR(200),
    bank_id BIGINT,
    currency VARCHAR(10),
    account_type VARCHAR(20),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_counterparty_account_t IS '对手账户表';
CREATE INDEX idx_cp_account_cp ON tms_counterparty_account_t(counterparty_id);
CREATE INDEX idx_cp_account_no ON tms_counterparty_account_t(account_no);

-- 审计日志表 (新增)
CREATE TABLE tms_audit_log_t (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    field_name VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    operator VARCHAR(50) NOT NULL,
    operate_ip VARCHAR(50),
    operate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500)
);
COMMENT ON TABLE tms_audit_log_t IS '审计日志表';
CREATE INDEX idx_audit_table_record ON tms_audit_log_t(table_name, record_id);
CREATE INDEX idx_audit_operation ON tms_audit_log_t(operation);
CREATE INDEX idx_audit_time ON tms_audit_log_t(operate_time);

-- 工作流模板表 (新增)
CREATE TABLE tms_workflow_template_t (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    biz_type VARCHAR(20) NOT NULL,
    approval_level INT NOT NULL DEFAULT 1,
    approver_type VARCHAR(20),
    approver_expr VARCHAR(500),
    config_json TEXT,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_workflow_template_t IS '工作流模板表';
CREATE INDEX idx_wft_code ON tms_workflow_template_t(template_code);
CREATE INDEX idx_wft_biz_type ON tms_workflow_template_t(biz_type);

-- 审批记录表 (新增)
CREATE TABLE tms_approval_record_t (
    id BIGSERIAL PRIMARY KEY,
    biz_type VARCHAR(20) NOT NULL,
    biz_id BIGINT NOT NULL,
    biz_no VARCHAR(50),
    template_id BIGINT,
    approval_level INT NOT NULL DEFAULT 1,
    approver VARCHAR(50),
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approval意见 VARCHAR(500),
    approval_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE tms_approval_record_t IS '审批记录表';
CREATE INDEX idx_ar_biz ON tms_approval_record_t(biz_type, biz_id);
CREATE INDEX idx_ar_status ON tms_approval_record_t(approval_status);

-- 初始数据
INSERT INTO tms_currency_t (code, name, symbol, decimal_places, created_by) VALUES
('CNY', '人民币', '¥', 2, 'system'),
('USD', '美元', '$', 2, 'system'),
('EUR', '欧元', '€', 2, 'system'),
('GBP', '英镑', '£', 2, 'system'),
('JPY', '日元', '¥', 0, 'system'),
('HKD', '港币', 'HK$', 2, 'system'),
('SGD', '新加坡元', 'S$', 2, 'system'),
('AUD', '澳元', 'A$', 2, 'system'),
('CHF', '瑞士法郎', 'Fr', 2, 'system');

INSERT INTO tms_country_t (code, name, en_name, timezone, country_no, created_by) VALUES
('CN', '中国', 'China', 'Asia/Shanghai', '156', 'system'),
('US', '美国', 'United States', 'America/New_York', '840', 'system'),
('GB', '英国', 'United Kingdom', 'Europe/London', '826', 'system'),
('JP', '日本', 'Japan', 'Asia/Tokyo', '392', 'system'),
('HK', '香港', 'Hong Kong', 'Asia/Hong_Kong', '344', 'system'),
('SG', '新加坡', 'Singapore', 'Asia/Singapore', '702', 'system');

INSERT INTO tms_business_unit_t (code, name, legal_person, status, created_by) VALUES
('BU001', '集团总部', '张三', '1', 'system');

INSERT INTO tms_trader_t (code, name, department, email, status, created_by) VALUES
('T001', '李四', '资金部', 'li.si@company.com', '1', 'system'),
('T002', '王五', '资金部', 'wang.wu@company.com', '1', 'system');