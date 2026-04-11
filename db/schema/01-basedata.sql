-- Open-TMS 基础数据模块表
-- PostgreSQL
-- 执行顺序: 1

-- 业务单元表
CREATE TABLE trm_business_unit_t (
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
COMMENT ON TABLE trm_business_unit_t IS '业务单元表';
CREATE INDEX idx_bu_code ON trm_business_unit_t(code);
CREATE INDEX idx_bu_status ON trm_business_unit_t(status);

-- 交易员表
CREATE TABLE trm_trader_t (
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
COMMENT ON TABLE trm_trader_t IS '交易员表';
CREATE INDEX idx_trader_code ON trm_trader_t(code);
CREATE INDEX idx_trader_status ON trm_trader_t(status);

-- 币种表
CREATE TABLE trm_currency_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    decimal_places INT NOT NULL DEFAULT 2,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_currency_t IS '币种表';
CREATE INDEX idx_currency_code ON trm_currency_t(code);

-- 国家/地区表
CREATE TABLE trm_country_t (
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
COMMENT ON TABLE trm_country_t IS '国家/地区表';
CREATE INDEX idx_country_code ON trm_country_t(code);

-- 节假日表
CREATE TABLE trm_holiday_t (
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
COMMENT ON TABLE trm_holiday_t IS '节假日表';
CREATE INDEX idx_holiday_date ON trm_holiday_t(holiday_date);
CREATE INDEX idx_holiday_country ON trm_holiday_t(country_code);

-- 银行表
CREATE TABLE trm_bank_t (
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
COMMENT ON TABLE trm_bank_t IS '银行表';
CREATE INDEX idx_bank_code ON trm_bank_t(code);
CREATE INDEX idx_bank_swift ON trm_bank_t(swift_code);

-- 交易对手表
CREATE TABLE trm_counterparty_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    cp_type VARCHAR(20),
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
COMMENT ON TABLE trm_counterparty_t IS '交易对手表';
CREATE INDEX idx_cp_code ON trm_counterparty_t(code);
CREATE INDEX idx_cp_type ON trm_counterparty_t(cp_type);

-- 对手账户表
CREATE TABLE trm_counterparty_account_t (
    id BIGSERIAL PRIMARY KEY,
    cp_id BIGINT NOT NULL,
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
    deleted CHAR(1) DEFAULT '0',
    CONSTRAINT fk_cp_account_cp FOREIGN KEY (cp_id) REFERENCES trm_counterparty_t(id),
    CONSTRAINT fk_cp_account_bank FOREIGN KEY (bank_id) REFERENCES trm_bank_t(id)
);
COMMENT ON TABLE trm_counterparty_account_t IS '对手账户表';
CREATE INDEX idx_cp_account_cp ON trm_counterparty_account_t(cp_id);
CREATE INDEX idx_cp_account_no ON trm_counterparty_account_t(account_no);

-- 初始数据
INSERT INTO trm_currency_t (code, name, symbol, decimal_places, created_by) VALUES
('CNY', '人民币', '¥', 2, 'system'),
('USD', '美元', '$', 2, 'system'),
('EUR', '欧元', '€', 2, 'system'),
('GBP', '英镑', '£', 2, 'system'),
('JPY', '日元', '¥', 0, 'system'),
('HKD', '港币', 'HK$', 2, 'system'),
('SGD', '新加坡元', 'S$', 2, 'system'),
('AUD', '澳元', 'A$', 2, 'system'),
('CHF', '瑞士法郎', 'Fr', 2, 'system');

INSERT INTO trm_country_t (code, name, en_name, timezone, country_no, created_by) VALUES
('CN', '中国', 'China', 'Asia/Shanghai', '156', 'system'),
('US', '美国', 'United States', 'America/New_York', '840', 'system'),
('GB', '英国', 'United Kingdom', 'Europe/London', '826', 'system'),
('JP', '日本', 'Japan', 'Asia/Tokyo', '392', 'system'),
('HK', '香港', 'Hong Kong', 'Asia/Hong_Kong', '344', 'system'),
('SG', '新加坡', 'Singapore', 'Asia/Singapore', '702', 'system');

INSERT INTO trm_business_unit_t (code, name, legal_person, status, created_by) VALUES
('BU001', '集团总部', '张三', '1', 'system');

INSERT INTO trm_trader_t (code, name, department, email, status, created_by) VALUES
('T001', '李四', '资金部', 'li.si@company.com', '1', 'system'),
('T002', '王五', '资金部', 'wang.wu@company.com', '1', 'system');