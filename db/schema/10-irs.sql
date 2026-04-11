-- Open-TMS 利率掉期模块表
-- PostgreSQL
-- 执行顺序: 10

-- 利率掉期表
CREATE TABLE trm_irs_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    notional_amount DECIMAL(18,2),
    currency VARCHAR(10),
    fixed_rate DECIMAL(10,6),
    floating_rate_type VARCHAR(20),
    swap_direction VARCHAR(20),
    start_date DATE,
    end_date DATE,
    payment_frequency VARCHAR(20),
    counterparty_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_irs_deal_t IS '利率掉期表';
CREATE INDEX idx_irs_no ON trm_irs_deal_t(deal_no);
CREATE INDEX idx_irs_type ON trm_irs_deal_t(deal_type);
CREATE INDEX idx_irs_counterparty ON trm_irs_deal_t(counterparty_id);
CREATE INDEX idx_irs_end_date ON trm_irs_deal_t(end_date);
CREATE INDEX idx_irs_status ON trm_irs_deal_t(status);