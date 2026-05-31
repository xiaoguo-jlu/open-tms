-- Open-TMS M1-AC交易(Deal)数据库设计
-- 版本: v1.0
-- 日期: 2026-06-01

-- ============================================================================
-- 1. 交易公共表 (tms_deals_t)
-- ============================================================================
CREATE TABLE tms_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    business_unit VARCHAR(50) NOT NULL,
    counterparty_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    trader_id BIGINT NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    deal_date DATE NOT NULL,
    value_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'New',
    description VARCHAR(500),
    remark VARCHAR(500),
    latest_action_number VARCHAR(50),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_deal_number ON tms_deals_t(deal_number);
CREATE INDEX idx_deal_type ON tms_deals_t(deal_type);
CREATE INDEX idx_deal_status ON tms_deals_t(status);
CREATE INDEX idx_deal_unit ON tms_deals_t(business_unit);

-- ============================================================================
-- 2. AC交易个性化表 (tms_ac_deals_t)
-- ============================================================================
CREATE TABLE tms_ac_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    bank_account_id BIGINT NOT NULL,
    counterparty_account_id BIGINT,
    payment_method VARCHAR(20),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_ac_deal_number ON tms_ac_deals_t(deal_number);

-- ============================================================================
-- 3. Action表 (tms_actions_t)
-- ============================================================================
CREATE TABLE tms_actions_t (
    id BIGSERIAL PRIMARY KEY,
    action_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    deal_type VARCHAR(20) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    approver1 VARCHAR(50),
    approver2 VARCHAR(50),
    approval_status1 VARCHAR(20) DEFAULT 'Pending',
    approval_status2 VARCHAR(20) DEFAULT 'Pending',
    approval_remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_action_number ON tms_actions_t(action_number);
CREATE INDEX idx_action_deal ON tms_actions_t(deal_number);
CREATE INDEX idx_action_status ON tms_actions_t(action_status);

-- ============================================================================
-- 4. 公共镜像表 (tms_deals_image_t)
-- ============================================================================
CREATE TABLE tms_deals_image_t (
    id BIGSERIAL PRIMARY KEY,
    image_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    deal_type VARCHAR(20) NOT NULL,
    version INT NOT NULL,
    business_unit VARCHAR(50),
    counterparty_id BIGINT,
    instrument_id BIGINT,
    trader_id BIGINT,
    direction VARCHAR(10),
    amount DECIMAL(38,18),
    currency VARCHAR(10),
    deal_date DATE,
    value_date DATE,
    status VARCHAR(20),
    description VARCHAR(500),
    remark VARCHAR(500),
    latest_action_number VARCHAR(50),
    image_type VARCHAR(20) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_image_deal ON tms_deals_image_t(deal_number);
CREATE INDEX idx_image_version ON tms_deals_image_t(deal_number, version);

-- ============================================================================
-- 5. AC交易镜像表 (tms_ac_deals_image_t)
-- ============================================================================
CREATE TABLE tms_ac_deals_image_t (
    id BIGSERIAL PRIMARY KEY,
    image_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    version INT NOT NULL,
    bank_account_id BIGINT,
    counterparty_account_id BIGINT,
    payment_method VARCHAR(20),
    image_type VARCHAR(20) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_ac_image_deal ON tms_ac_deals_image_t(deal_number);
CREATE INDEX idx_ac_image_version ON tms_ac_deals_image_t(deal_number, version);