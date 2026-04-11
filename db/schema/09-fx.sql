-- Open-TMS 外汇交易模块表
-- PostgreSQL

CREATE TABLE trm_fx_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    buy_currency VARCHAR(10),
    sell_currency VARCHAR(10),
    buy_amount DECIMAL(18,2),
    sell_amount DECIMAL(18,2),
    rate DECIMAL(12,6),
    counterparty_id BIGINT,
    value_date DATE,
    maturity_date DATE,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_fx_deal_no ON trm_fx_deal_t(deal_no);
CREATE INDEX idx_deal_type ON trm_fx_deal_t(deal_type);
CREATE INDEX idx_counterparty ON trm_fx_deal_t(counterparty_id);
CREATE INDEX idx_value_date ON trm_fx_deal_t(value_date);