-- Open-TMS H2 Schema (basedata module)
-- H2 1.4.x compatible syntax

-- 1. 业务单元表
CREATE TABLE trm_business_unit_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_code       VARCHAR(50) NOT NULL UNIQUE,
    unit_name       VARCHAR(200) NOT NULL,
    unit_name_en    VARCHAR(200),
    legal_person    VARCHAR(50),
    register_addr   VARCHAR(500),
    tax_number      VARCHAR(50),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 2. 交易员表
CREATE TABLE trm_trader_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    trader_code     VARCHAR(50) NOT NULL UNIQUE,
    trader_name     VARCHAR(50) NOT NULL,
    trader_name_en  VARCHAR(50),
    department      VARCHAR(100),
    phone           VARCHAR(30),
    email           VARCHAR(100),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 3. 币种表
CREATE TABLE trm_currency_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    currency_code   VARCHAR(10) NOT NULL UNIQUE,
    currency_name   VARCHAR(50) NOT NULL,
    currency_symbol VARCHAR(10),
    decimal_places  INT NOT NULL DEFAULT 2,
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 4. 国家/地区表
CREATE TABLE trm_country_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    country_code    VARCHAR(10) NOT NULL UNIQUE,
    country_name    VARCHAR(100) NOT NULL,
    country_name_en VARCHAR(100),
    timezone        VARCHAR(50),
    country_code_no VARCHAR(10),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 5. 节假日表
CREATE TABLE trm_holiday_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    holiday_date    DATE NOT NULL,
    holiday_name    VARCHAR(100) NOT NULL,
    country_code    VARCHAR(10) NOT NULL,
    is_adjacent     CHAR(1) NOT NULL DEFAULT '0',
    remark          VARCHAR(500),
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 6. 银行表
CREATE TABLE trm_bank_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_code       VARCHAR(50) NOT NULL UNIQUE,
    bank_name       VARCHAR(200) NOT NULL,
    bank_name_en    VARCHAR(200),
    swift_code      VARCHAR(20),
    country_code    VARCHAR(10),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

-- 7. 交易对手表
CREATE TABLE trm_counterparty_t (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    cp_code         VARCHAR(50) NOT NULL UNIQUE,
    cp_name         VARCHAR(200) NOT NULL,
    cp_name_en      VARCHAR(200),
    cp_type         VARCHAR(20),
    country_code    VARCHAR(10),
    swift_code      VARCHAR(20),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);
