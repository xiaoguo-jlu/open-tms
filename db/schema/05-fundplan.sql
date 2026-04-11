-- Open-TMS 资金计划模块表
-- PostgreSQL

CREATE TABLE trm_fund_plan_t (
    id BIGSERIAL PRIMARY KEY,
    plan_no VARCHAR(50) NOT NULL UNIQUE,
    plan_name VARCHAR(200) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    business_unit_id BIGINT,
    currency VARCHAR(10),
    plan_amount DECIMAL(18,2),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    period_type VARCHAR(20),
    version_no INT DEFAULT 1,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_plan_no ON trm_fund_plan_t(plan_no);
CREATE INDEX idx_plan_type ON trm_fund_plan_t(plan_type);
CREATE INDEX idx_business_unit ON trm_fund_plan_t(business_unit_id);
CREATE INDEX idx_start_date ON trm_fund_plan_t(start_date);