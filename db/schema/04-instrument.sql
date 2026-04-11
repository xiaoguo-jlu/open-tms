-- Open-TMS 金融工具模块表
-- PostgreSQL
-- 执行顺序: 4

-- 金融工具表
CREATE TABLE trm_instrument_t (
    id BIGSERIAL PRIMARY KEY,
    inst_code VARCHAR(50) NOT NULL UNIQUE,
    inst_name VARCHAR(200) NOT NULL,
    inst_type VARCHAR(20) NOT NULL,
    inst_subtype VARCHAR(20),
    currency VARCHAR(10),
    face_value DECIMAL(18,2),
    issue_date DATE,
    maturity_date DATE,
    interest_rate DECIMAL(10,6),
    counterparty_id BIGINT,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_instrument_t IS '金融工具表';
CREATE INDEX idx_inst_code ON trm_instrument_t(inst_code);
CREATE INDEX idx_inst_type ON trm_instrument_t(inst_type);
CREATE INDEX idx_inst_counterparty ON trm_instrument_t(counterparty_id);
CREATE INDEX idx_inst_maturity ON trm_instrument_t(maturity_date);
CREATE INDEX idx_inst_status ON trm_instrument_t(status);