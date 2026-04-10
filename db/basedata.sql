-- Open-TMS 基础数据表
-- 数据库: opentms_dev

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
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_business_unit_code ON trm_business_unit_t(code);
CREATE INDEX idx_business_unit_status ON trm_business_unit_t(status);

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
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
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
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_currency_code ON trm_currency_t(code);
CREATE INDEX idx_currency_status ON trm_currency_t(status);

-- 国家表
CREATE TABLE trm_country_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    en_name VARCHAR(100),
    timezone VARCHAR(50),
    area_code VARCHAR(10),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_country_code ON trm_country_t(code);
CREATE INDEX idx_country_status ON trm_country_t(status);

-- 节假日表
CREATE TABLE trm_holiday_t (
    id BIGSERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    is_adjust CHAR(1) NOT NULL DEFAULT '0',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_holiday_date ON trm_holiday_t(holiday_date);
CREATE INDEX idx_holiday_country ON trm_holiday_t(country_code);

-- 银行表
CREATE TABLE trm_bank_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    swift_code VARCHAR(11),
    bank_no VARCHAR(20),
    country_code VARCHAR(10) NOT NULL,
    bank_type VARCHAR(20),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_bank_code ON trm_bank_t(code);
CREATE INDEX idx_bank_country ON trm_bank_t(country_code);
CREATE INDEX idx_bank_status ON trm_bank_t(status);

-- 对手方表
CREATE TABLE trm_counterparty_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    credit_rating VARCHAR(10),
    ext_rating VARCHAR(10),
    address VARCHAR(500),
    phone VARCHAR(30),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_counterparty_code ON trm_counterparty_t(code);
CREATE INDEX idx_counterparty_type ON trm_counterparty_t(type);
CREATE INDEX idx_counterparty_status ON trm_counterparty_t(status);

-- 对手方账户表
CREATE TABLE trm_counterparty_account_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    counterparty_id BIGINT NOT NULL,
    bank_id BIGINT NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    account_no VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_counterparty_account_code ON trm_counterparty_account_t(code);
CREATE INDEX idx_counterparty_account_counterparty ON trm_counterparty_account_t(counterparty_id);
CREATE INDEX idx_counterparty_account_bank ON trm_counterparty_account_t(bank_id);
CREATE INDEX idx_counterparty_account_status ON trm_counterparty_account_t(status);
