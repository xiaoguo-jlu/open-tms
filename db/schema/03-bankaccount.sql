-- Open-TMS 银行账户模块表
-- PostgreSQL
-- 执行顺序: 3

-- 银行账户表
CREATE TABLE trm_bank_account_t (
    id BIGSERIAL PRIMARY KEY,
    account_no VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    bank_id BIGINT,
    account_type VARCHAR(20),
    currency VARCHAR(10),
    balance DECIMAL(18,2) DEFAULT 0,
    available_balance DECIMAL(18,2) DEFAULT 0,
    frozen_balance DECIMAL(18,2) DEFAULT 0,
    is_main CHAR(1) DEFAULT '0',
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_bank_account_t IS '银行账户表';
CREATE INDEX idx_ba_no ON trm_bank_account_t(account_no);
CREATE INDEX idx_ba_bank ON trm_bank_account_t(bank_id);
CREATE INDEX idx_ba_type ON trm_bank_account_t(account_type);
CREATE INDEX idx_ba_currency ON trm_bank_account_t(currency);
CREATE INDEX idx_ba_status ON trm_bank_account_t(status);