-- Open-TMS 银行账户表
-- PostgreSQL
-- 执行顺序: 12
-- 所属模块: basedata
-- 说明: 简化版银行账户，无银企直连、无审批流程

-- 银行账户表
CREATE TABLE tms_bank_account_t (
    id BIGSERIAL PRIMARY KEY,
    account_no VARCHAR(50) NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    bank_id BIGINT,
    currency VARCHAR(10),
    account_type VARCHAR(20) COMMENT '账户类型: CURRENT(活期)/TERM(定期)/MARGIN(保证金)',
    business_unit_id BIGINT,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE tms_bank_account_t IS '银行账户表';
COMMENT ON COLUMN tms_bank_account_t.account_no IS '账户编号';
COMMENT ON COLUMN tms_bank_account_t.account_name IS '账户名称';
COMMENT ON COLUMN tms_bank_account_t.bank_id IS '开户行(关联tms_bank_t)';
COMMENT ON COLUMN tms_bank_account_t.currency IS '币种';
COMMENT ON COLUMN tms_bank_account_t.account_type IS '账户类型: CURRENT-活期/TERM-定期/MARGIN-保证金';
COMMENT ON COLUMN tms_bank_account_t.business_unit_id IS '所属业务单元(关联tms_business_unit_t)';
COMMENT ON COLUMN tms_bank_account_t.status IS '状态: 0-停用 1-启用';

-- 索引
CREATE INDEX idx_ba_no ON tms_bank_account_t(account_no);
CREATE INDEX idx_ba_bank ON tms_bank_account_t(bank_id);
CREATE INDEX idx_ba_type ON tms_bank_account_t(account_type);
CREATE INDEX idx_ba_currency ON tms_bank_account_t(currency);
CREATE INDEX idx_ba_status ON tms_bank_account_t(status);
CREATE INDEX idx_ba_business_unit ON tms_bank_account_t(business_unit_id);
CREATE UNIQUE INDEX idx_ba_no_unique ON tms_bank_account_t(account_no) WHERE deleted = '0';
