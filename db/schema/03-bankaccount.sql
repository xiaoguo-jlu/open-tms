-- Open-TMS 银行账户模块表
-- PostgreSQL
-- 执行顺序: 3

-- 银行账户表
CREATE TABLE tms_bank_account_t (
    id BIGSERIAL PRIMARY KEY,
    account_no VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    bank_id BIGINT,
    account_type VARCHAR(20),
    currency VARCHAR(10),
    balance DECIMAL(24,4) DEFAULT 0,
    available_balance DECIMAL(24,4) DEFAULT 0,
    frozen_balance DECIMAL(24,4) DEFAULT 0,
    is_main CHAR(1) DEFAULT '0',
    business_unit_id BIGINT,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_bank_account_t IS '银行账户表';
CREATE INDEX idx_ba_no ON tms_bank_account_t(account_no);
CREATE INDEX idx_ba_bank ON tms_bank_account_t(bank_id);
CREATE INDEX idx_ba_type ON tms_bank_account_t(account_type);
CREATE INDEX idx_ba_currency ON tms_bank_account_t(currency);
CREATE INDEX idx_ba_status ON tms_bank_account_t(status);
CREATE INDEX idx_ba_business_unit ON tms_bank_account_t(business_unit_id);

-- 账户流水表 (新增)
CREATE TABLE tms_account_transaction_t (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_no VARCHAR(50) NOT NULL UNIQUE,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(24,4) NOT NULL,
    balance_after DECIMAL(24,4),
    opposite_account_id BIGINT,
    opposite_account_name VARCHAR(200),
    reference_type VARCHAR(20),
    reference_id BIGINT,
    reference_no VARCHAR(50),
    remark VARCHAR(500),
    operator VARCHAR(50),
    operate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status CHAR(1) NOT NULL DEFAULT '1'
);
COMMENT ON TABLE tms_account_transaction_t IS '账户流水表';
CREATE INDEX idx_at_account ON tms_account_transaction_t(account_id);
CREATE INDEX idx_at_no ON tms_account_transaction_t(transaction_no);
CREATE INDEX idx_at_type ON tms_account_transaction_t(transaction_type);
CREATE INDEX idx_at_reference ON tms_account_transaction_t(reference_type, reference_id);
CREATE INDEX idx_at_time ON tms_account_transaction_t(operate_time);