-- Open-TMS 交易审批规则表
-- PostgreSQL
-- 执行顺序: 18

-- 审批规则表
CREATE TABLE tms_approval_rule_t (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL UNIQUE,
    rule_name VARCHAR(200) NOT NULL,
    biz_type VARCHAR(20) NOT NULL COMMENT '业务类型：DEAL交易/TRANSFER转账/FX外汇等',
    amount_limit DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10),
    approval_level INT NOT NULL DEFAULT 1 COMMENT '审批级别：1一级审批/2二级审批/3三级审批',
    approver_type VARCHAR(20) COMMENT '审批人类型：ROLE角色/USER用户',
    approver_expr VARCHAR(500) COMMENT '审批人表达式，如 role:TM 或 user:zhangsan',
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_approval_rule_t IS '交易审批规则表';
CREATE INDEX idx_ar_rule_code ON tms_approval_rule_t(rule_code);
CREATE INDEX idx_ar_biz_type ON tms_approval_rule_t(biz_type);
CREATE INDEX idx_ar_status ON tms_approval_rule_t(status);