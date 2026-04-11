-- Open-TMS 流动性限额模块表
-- PostgreSQL
-- 执行顺序: 8

-- 流动性限额表 (精度提高)
CREATE TABLE trm_limit_t (
    id BIGSERIAL PRIMARY KEY,
    limit_no VARCHAR(50) NOT NULL UNIQUE,
    limit_name VARCHAR(200) NOT NULL,
    limit_type VARCHAR(20) NOT NULL,
    business_unit_id BIGINT,
    currency VARCHAR(10),
    limit_amount DECIMAL(24,4),
    used_amount DECIMAL(24,4) DEFAULT 0,
    warning_percent DECIMAL(5,2) DEFAULT 80,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_limit_t IS '流动性限额表';
CREATE INDEX idx_limit_no ON trm_limit_t(limit_no);
CREATE INDEX idx_limit_type ON trm_limit_t(limit_type);
CREATE INDEX idx_limit_business_unit ON trm_limit_t(business_unit_id);
CREATE INDEX idx_limit_status ON trm_limit_t(status);

-- 限额预警记录表
CREATE TABLE trm_limit_warning_t (
    id BIGSERIAL PRIMARY KEY,
    limit_id BIGINT NOT NULL,
    warn_type VARCHAR(20),
    warn_level VARCHAR(20),
    used_percent DECIMAL(5,2),
    message VARCHAR(500),
    is_read CHAR(1) DEFAULT '0',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE trm_limit_warning_t IS '限额预警记录表';
CREATE INDEX idx_lw_limit ON trm_limit_warning_t(limit_id);
CREATE INDEX idx_lw_read ON trm_limit_warning_t(is_read);