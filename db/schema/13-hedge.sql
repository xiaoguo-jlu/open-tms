-- Open-TMS 套期保值模块表
-- PostgreSQL

CREATE TABLE trm_hedge_relation_t (
    id BIGSERIAL PRIMARY KEY,
    hedge_no VARCHAR(50) NOT NULL UNIQUE,
    hedge_type VARCHAR(20) NOT NULL,
    exposure_id BIGINT NOT NULL,
    instrument_id BIGINT,
    hedge_ratio DECIMAL(8,4),
    hedge_amount DECIMAL(18,2),
    hedge_effectiveness DECIMAL(5,2),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_hedge_no ON trm_hedge_relation_t(hedge_no);
CREATE INDEX idx_hedge_type ON trm_hedge_relation_t(hedge_type);
CREATE INDEX idx_exposure ON trm_hedge_relation_t(exposure_id);