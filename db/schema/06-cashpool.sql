-- Open-TMS 现金池模块表
-- PostgreSQL

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
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_pool_no ON trm_cash_pool_t(pool_no);
CREATE INDEX idx_pool_type ON trm_cash_pool_t(pool_type);
CREATE INDEX idx_business_unit ON trm_cash_pool_t(business_unit_id);