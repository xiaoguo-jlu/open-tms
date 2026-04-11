-- Open-TMS 估值模块表
-- PostgreSQL
-- 执行顺序: 11

-- 估值表
CREATE TABLE trm_valuation_t (
    id BIGSERIAL PRIMARY KEY,
    inst_id BIGINT NOT NULL,
    valuation_date DATE NOT NULL,
    market_value DECIMAL(18,2),
    cost_value DECIMAL(18,2),
    unrealized_pl DECIMAL(18,2),
    valuation_method VARCHAR(20),
    discount_rate DECIMAL(10,6),
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_valuation_t IS '估值表';
CREATE INDEX idx_val_inst ON trm_valuation_t(inst_id);
CREATE INDEX idx_val_date ON trm_valuation_t(valuation_date);
CREATE INDEX idx_val_status ON trm_valuation_t(status);