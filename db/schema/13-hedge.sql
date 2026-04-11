-- Open-TMS 套期保值模块表
-- PostgreSQL
-- 执行顺序: 13

-- 套期保值关联表 (修复: 精度提高)
CREATE TABLE tms_hedge_relation_t (
    id BIGSERIAL PRIMARY KEY,
    hedge_no VARCHAR(50) NOT NULL UNIQUE,
    hedge_type VARCHAR(20) NOT NULL,
    exposure_id BIGINT NOT NULL,
    instrument_id BIGINT,
    hedge_ratio DECIMAL(8,4),
    hedge_amount DECIMAL(24,4),
    hedge_effectiveness DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_hedge_relation_t IS '套期保值关联表';
CREATE INDEX idx_hr_no ON tms_hedge_relation_t(hedge_no);
CREATE INDEX idx_hr_type ON tms_hedge_relation_t(hedge_type);
CREATE INDEX idx_hr_exposure ON tms_hedge_relation_t(exposure_id);
CREATE INDEX idx_hr_status ON tms_hedge_relation_t(status);