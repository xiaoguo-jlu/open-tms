-- Open-TMS 交易管理模块表
-- PostgreSQL

CREATE TABLE trm_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    deal_subtype VARCHAR(20),
    instrument_id BIGINT,
    counterparty_id BIGINT,
    business_unit_id BIGINT,
    amount DECIMAL(18,2),
    currency VARCHAR(10),
    value_date DATE,
    maturity_date DATE,
    interest_rate DECIMAL(10,6),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_deal_no ON trm_deal_t(deal_no);
CREATE INDEX idx_deal_type ON trm_deal_t(deal_type);
CREATE INDEX idx_counterparty ON trm_deal_t(counterparty_id);
CREATE INDEX idx_value_date ON trm_deal_t(value_date);
CREATE INDEX idx_maturity_date ON trm_deal_t(maturity_date);