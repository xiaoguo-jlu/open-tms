-- Open-TMS 交易管理模块表
-- PostgreSQL
-- 执行顺序: 2
-- 依赖: 01-basedata.sql (instrument需要等04-instrument.sql创建后才能添加外键)

-- 交易幂等表 (新增)
CREATE TABLE tms_deal_idempotency_t (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    biz_type VARCHAR(20) NOT NULL,
    biz_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    request_data TEXT,
    response_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP
);
COMMENT ON TABLE tms_deal_idempotency_t IS '交易幂等表';
CREATE INDEX idx_di_key ON tms_deal_idempotency_t(idempotency_key);
CREATE INDEX idx_di_status ON tms_deal_idempotency_t(status);
CREATE INDEX idx_di_expired ON tms_deal_idempotency_t(expired_at);

-- 交易表 (修复: 精度提高, 添加幂等键, 复合索引)
CREATE TABLE tms_deal_t (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100),
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    deal_subtype VARCHAR(20),
    instrument_id BIGINT,
    counterparty_id BIGINT,
    business_unit_id BIGINT,
    trader_id BIGINT,
    amount DECIMAL(24,4),
    currency VARCHAR(10),
    value_date DATE,
    maturity_date DATE,
    interest_rate DECIMAL(12,8),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0',
    CONSTRAINT uq_deal_no_status UNIQUE (deal_no, status)
);
COMMENT ON TABLE tms_deal_t IS '交易表';
CREATE INDEX idx_deal_no ON tms_deal_t(deal_no);
CREATE INDEX idx_deal_type ON tms_deal_t(deal_type);
CREATE INDEX idx_deal_counterparty ON tms_deal_t(counterparty_id);
CREATE INDEX idx_deal_value_date ON tms_deal_t(value_date);
CREATE INDEX idx_deal_maturity_date ON tms_deal_t(maturity_date);
CREATE INDEX idx_deal_status ON tms_deal_t(status);
-- 复合索引 (新增)
CREATE INDEX idx_deal_no_status ON tms_deal_t(deal_no, status);
CREATE INDEX idx_deal_type_status ON tms_deal_t(deal_type, status);