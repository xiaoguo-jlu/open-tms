-- Open-TMS 资金计划模块表
-- PostgreSQL
-- 执行顺序: 5

-- 资金计划表
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
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_fund_plan_t IS '资金计划表';
CREATE INDEX idx_fp_no ON trm_fund_plan_t(plan_no);
CREATE INDEX idx_fp_type ON trm_fund_plan_t(plan_type);
CREATE INDEX idx_fp_business_unit ON trm_fund_plan_t(business_unit_id);
CREATE INDEX idx_fp_start_date ON trm_fund_plan_t(start_date);
CREATE INDEX idx_fp_status ON trm_fund_plan_t(status);

-- 资金计划明细表
CREATE TABLE trm_fund_plan_detail_t (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    period_date DATE NOT NULL,
    in_amount DECIMAL(18,2) DEFAULT 0,
    out_amount DECIMAL(18,2) DEFAULT 0,
    balance DECIMAL(18,2) DEFAULT 0,
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE trm_fund_plan_detail_t IS '资金计划明细表';
CREATE INDEX idx_fpd_plan ON trm_fund_plan_detail_t(plan_id);
CREATE INDEX idx_fpd_date ON trm_fund_plan_detail_t(period_date);