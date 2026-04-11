-- Open-TMS 流动性限额模块表
-- PostgreSQL

CREATE TABLE trm_limit_t (
    id BIGSERIAL PRIMARY KEY,
    limit_no VARCHAR(50) NOT NULL UNIQUE,
    limit_name VARCHAR(200) NOT NULL,
    limit_type VARCHAR(20) NOT NULL,
    business_unit_id BIGINT,
    currency VARCHAR(10),
    limit_amount DECIMAL(18,2),
    used_amount DECIMAL(18,2) DEFAULT 0,
    warning_percent DECIMAL(5,2) DEFAULT 80,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_limit_no ON trm_limit_t(limit_no);
CREATE INDEX idx_limit_type ON trm_limit_t(limit_type);
CREATE INDEX idx_business_unit ON trm_limit_t(business_unit_id);