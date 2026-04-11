-- Open-TMS 敞口管理模块表
-- PostgreSQL
-- 执行顺序: 12

-- 敞口表 (修复: 精度提高)
CREATE TABLE trm_exposure_t (
    id BIGSERIAL PRIMARY KEY,
    exposure_no VARCHAR(50) NOT NULL UNIQUE,
    exposure_type VARCHAR(20) NOT NULL,
    business_unit_id BIGINT,
    currency VARCHAR(10),
    exposure_amount DECIMAL(24,4),
    net_exposure DECIMAL(24,4),
    exposure_date DATE,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_exposure_t IS '敞口表';
CREATE INDEX idx_exp_no ON trm_exposure_t(exposure_no);
CREATE INDEX idx_exp_type ON trm_exposure_t(exposure_type);
CREATE INDEX idx_exp_business_unit ON trm_exposure_t(business_unit_id);
CREATE INDEX idx_exp_date ON trm_exposure_t(exposure_date);
CREATE INDEX idx_exp_status ON trm_exposure_t(status);