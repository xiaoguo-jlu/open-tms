CREATE TABLE IF NOT EXISTS tms_default_bank_account_rule_t (
    id BIGSERIAL PRIMARY KEY,
    rule_number VARCHAR(50) NOT NULL UNIQUE,
    management_entity_id BIGINT NOT NULL,
    counterparty_id BIGINT,
    instrument_id BIGINT,
    direction VARCHAR(20) NOT NULL,
    currency VARCHAR(10),
    bank_account_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    priority INT NOT NULL DEFAULT 0,
    start_date DATE,
    description VARCHAR(500),
    remark VARCHAR(500),
    lock_token VARCHAR(64),
    locked_by VARCHAR(50),
    locked_at TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    deleted CHAR(1) NOT NULL DEFAULT '0',
    CONSTRAINT chk_dbar_direction CHECK (direction IN ('Inflow', 'Outflow', 'ALL')),
    CONSTRAINT chk_dbar_status CHECK (status IN ('Active', 'Inactive')),
    CONSTRAINT chk_dbar_priority CHECK (priority BETWEEN 0 AND 9999),
    CONSTRAINT chk_dbar_bank_entity CHECK (bank_account_id IS NOT NULL)
);

CREATE UNIQUE INDEX IF NOT EXISTS uniq_dbar_active_dims
    ON tms_default_bank_account_rule_t (management_entity_id, counterparty_id, instrument_id, direction, currency, status)
    NULLS NOT DISTINCT
    WHERE deleted = '0' AND status = 'Active';

CREATE INDEX IF NOT EXISTS idx_dbar_mgmt_entity ON tms_default_bank_account_rule_t(management_entity_id);

CREATE INDEX IF NOT EXISTS idx_dbar_counterparty ON tms_default_bank_account_rule_t(counterparty_id);

CREATE INDEX IF NOT EXISTS idx_dbar_instrument ON tms_default_bank_account_rule_t(instrument_id);

CREATE INDEX IF NOT EXISTS idx_dbar_direction ON tms_default_bank_account_rule_t(direction);

CREATE INDEX IF NOT EXISTS idx_dbar_currency ON tms_default_bank_account_rule_t(currency);

CREATE INDEX IF NOT EXISTS idx_dbar_status ON tms_default_bank_account_rule_t(status);

CREATE INDEX IF NOT EXISTS idx_dbar_priority ON tms_default_bank_account_rule_t(priority DESC);

CREATE INDEX IF NOT EXISTS idx_dbar_bank_account ON tms_default_bank_account_rule_t(bank_account_id);

CREATE INDEX IF NOT EXISTS idx_dbar_start_date ON tms_default_bank_account_rule_t(start_date);

CREATE INDEX IF NOT EXISTS idx_dbar_lock_token ON tms_default_bank_account_rule_t(lock_token);

CREATE INDEX IF NOT EXISTS idx_dbar_match_core
    ON tms_default_bank_account_rule_t(management_entity_id, direction, status, priority DESC);

CREATE TABLE IF NOT EXISTS tms_rule_audit_log_t (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    operator VARCHAR(50) NOT NULL DEFAULT 'system',
    operated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    CONSTRAINT chk_ral_operation CHECK (operation IN ('CREATE', 'UPDATE', 'DELETE', 'ENABLE', 'DISABLE'))
);

CREATE INDEX IF NOT EXISTS idx_ral_rule_id ON tms_rule_audit_log_t(rule_id);

CREATE INDEX IF NOT EXISTS idx_ral_operator ON tms_rule_audit_log_t(operator);

CREATE INDEX IF NOT EXISTS idx_ral_operated_at ON tms_rule_audit_log_t(operated_at DESC);

CREATE INDEX IF NOT EXISTS idx_ral_operation ON tms_rule_audit_log_t(operation);

-- idx_deal_bank_account_status 暂不创建
-- 原因: tms_deals_t / tms_fx_deal_t 等交易表当前没有 bank_account_id 列
--       (属于 FX/AC/AT 表结构扩展范围,后续 P1+ 单独处理)
-- 临时方案: 引用 N 查询直接用 bank_account_id 主键关联,响应 < 50ms(规则表本身有 idx_dbar_bank_account)