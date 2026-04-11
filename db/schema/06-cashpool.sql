-- Open-TMS 现金池模块表
-- PostgreSQL
-- 执行顺序: 6

-- 现金池表
CREATE TABLE trm_cash_pool_t (
    id BIGSERIAL PRIMARY KEY,
    pool_no VARCHAR(50) NOT NULL UNIQUE,
    pool_name VARCHAR(200) NOT NULL,
    pool_type VARCHAR(20) NOT NULL,
    business_unit_id BIGINT,
    currency VARCHAR(10),
    balance DECIMAL(18,2) DEFAULT 0,
    threshold_amount DECIMAL(18,2),
    auto_transfer CHAR(1) DEFAULT '0',
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_cash_pool_t IS '现金池表';
CREATE INDEX idx_cp_no ON trm_cash_pool_t(pool_no);
CREATE INDEX idx_cp_type ON trm_cash_pool_t(pool_type);
CREATE INDEX idx_cp_business_unit ON trm_cash_pool_t(business_unit_id);
CREATE INDEX idx_cp_status ON trm_cash_pool_t(status);

-- 现金池账户关联表
CREATE TABLE trm_cash_pool_account_t (
    id BIGSERIAL PRIMARY KEY,
    pool_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    is_master CHAR(1) DEFAULT '0',
    display_order INT DEFAULT 0,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE trm_cash_pool_account_t IS '现金池账户关联表';
CREATE INDEX idx_cpa_pool ON trm_cash_pool_account_t(pool_id);
CREATE INDEX idx_cpa_account ON trm_cash_pool_account_t(account_id);