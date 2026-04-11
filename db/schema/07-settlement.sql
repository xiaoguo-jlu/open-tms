-- Open-TMS 支付结算模块表
-- PostgreSQL
-- 执行顺序: 7

-- 支付结算表 (精度提高)
CREATE TABLE trm_settlement_t (
    id BIGSERIAL PRIMARY KEY,
    settle_no VARCHAR(50) NOT NULL UNIQUE,
    settle_type VARCHAR(20) NOT NULL,
    deal_id BIGINT,
    from_account_id BIGINT,
    to_account_id BIGINT,
    amount DECIMAL(24,4),
    currency VARCHAR(10),
    value_date DATE,
    settle_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_settlement_t IS '支付结算表';
CREATE INDEX idx_st_no ON trm_settlement_t(settle_no);
CREATE INDEX idx_st_type ON trm_settlement_t(settle_type);
CREATE INDEX idx_st_deal ON trm_settlement_t(deal_id);
CREATE INDEX idx_st_value_date ON trm_settlement_t(value_date);
CREATE INDEX idx_st_status ON trm_settlement_t(settle_status);