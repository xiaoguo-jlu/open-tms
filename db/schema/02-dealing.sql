-- Open-TMS 交易管理模块表
-- PostgreSQL
-- 执行顺序: 2

-- 交易表
CREATE TABLE trm_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    deal_subtype VARCHAR(20),
    instrument_id BIGINT,
    counterparty_id BIGINT,
    business_unit_id BIGINT,
    trader_id BIGINT,
    amount DECIMAL(18,2),
    currency VARCHAR(10),
    value_date DATE,
    maturity_date DATE,
    interest_rate DECIMAL(10,6),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_deal_t IS '交易表';
CREATE INDEX idx_deal_no ON trm_deal_t(deal_no);
CREATE INDEX idx_deal_type ON trm_deal_t(deal_type);
CREATE INDEX idx_deal_counterparty ON trm_deal_t(counterparty_id);
CREATE INDEX idx_deal_value_date ON trm_deal_t(value_date);
CREATE INDEX idx_deal_maturity_date ON trm_deal_t(maturity_date);
CREATE INDEX idx_deal_status ON trm_deal_t(status);

-- 添加外键约束 (单独执行)
-- ALTER TABLE trm_deal_t ADD CONSTRAINT fk_deal_instrument FOREIGN KEY (instrument_id) REFERENCES trm_instrument_t(id);
-- ALTER TABLE trm_deal_t ADD CONSTRAINT fk_deal_counterparty FOREIGN KEY (counterparty_id) REFERENCES trm_counterparty_t(id);
-- ALTER TABLE trm_deal_t ADD CONSTRAINT fk_deal_business_unit FOREIGN KEY (business_unit_id) REFERENCES trm_business_unit_t(id);
-- ALTER TABLE trm_deal_t ADD CONSTRAINT fk_deal_trader FOREIGN KEY (trader_id) REFERENCES trm_trader_t(id);
-- ALTER TABLE trm_deal_t ADD CONSTRAINT fk_deal_currency FOREIGN KEY (currency) REFERENCES trm_currency_t(code);