-- Open-TMS 数据库设计 (表名规范: trm_xxx_t)
-- 模块: basedata + dealing + cashpool + workflow + admin
-- 版本: v1.1
-- 日期: 2026-04-05

-- ============================================
-- 公共字段（所有表必须包含）
-- ============================================
-- created_by   VARCHAR(50)  创建人
-- created_at   TIMESTAMP    创建时间  
-- updated_by   VARCHAR(50)  最后更新人
-- updated_at   TIMESTAMP    最后更新时间

-- ============================================
-- 一、基础数据模块 (basedata)
-- ============================================

-- 1. 业务单元表
CREATE TABLE trm_business_unit_t (
    id              BIGSERIAL PRIMARY KEY,
    unit_code       VARCHAR(50) NOT NULL UNIQUE,
    unit_name       VARCHAR(200) NOT NULL,
    unit_name_en    VARCHAR(200),
    legal_person    VARCHAR(50),
    register_addr   VARCHAR(500),
    tax_number      VARCHAR(50),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_business_unit_t IS '业务单元表';
CREATE INDEX idx_bu_code ON trm_business_unit_t(unit_code);
CREATE INDEX idx_bu_status ON trm_business_unit_t(status);

-- 2. 交易员表
CREATE TABLE trm_trader_t (
    id              BIGSERIAL PRIMARY KEY,
    trader_code     VARCHAR(50) NOT NULL UNIQUE,
    trader_name     VARCHAR(50) NOT NULL,
    trader_name_en  VARCHAR(50),
    department      VARCHAR(100),
    phone           VARCHAR(30),
    email           VARCHAR(100),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_trader_t IS '交易员表';
CREATE INDEX idx_trader_code ON trm_trader_t(trader_code);

-- 3. 币种表
CREATE TABLE trm_currency_t (
    id              BIGSERIAL PRIMARY KEY,
    currency_code   VARCHAR(10) NOT NULL UNIQUE,
    currency_name   VARCHAR(50) NOT NULL,
    currency_symbol VARCHAR(10),
    decimal_places  INT NOT NULL DEFAULT 2,
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_currency_t IS '币种表';
CREATE INDEX idx_currency_code ON trm_currency_t(currency_code);

-- 4. 国家/地区表
CREATE TABLE trm_country_t (
    id              BIGSERIAL PRIMARY KEY,
    country_code    VARCHAR(10) NOT NULL UNIQUE,
    country_name    VARCHAR(100) NOT NULL,
    country_name_en VARCHAR(100),
    timezone        VARCHAR(50),
    country_code_no VARCHAR(10),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_country_t IS '国家/地区表';
CREATE INDEX idx_country_code ON trm_country_t(country_code);

-- 5. 节假日表
CREATE TABLE trm_holiday_t (
    id              BIGSERIAL PRIMARY KEY,
    holiday_date    DATE NOT NULL,
    holiday_name    VARCHAR(100) NOT NULL,
    country_code    VARCHAR(10) NOT NULL,
    is_adjacent     CHAR(1) NOT NULL DEFAULT '0',
    remark          VARCHAR(500),
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_holiday_t IS '节假日表';
CREATE INDEX idx_holiday_date ON trm_holiday_t(holiday_date);
CREATE INDEX idx_holiday_country ON trm_holiday_t(country_code);

-- 6. 银行信息表
CREATE TABLE trm_bank_t (
    id              BIGSERIAL PRIMARY KEY,
    bank_code       VARCHAR(50) NOT NULL UNIQUE,
    bank_name       VARCHAR(200) NOT NULL,
    bank_name_en    VARCHAR(200),
    swift_code      VARCHAR(11),
    bank_line_no    VARCHAR(20),
    country_code    VARCHAR(10) NOT NULL,
    bank_type       VARCHAR(20),
    status          CHAR(1) NOT NULL DEFAULT '1',
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE trm_bank_t IS '银行信息表';
CREATE INDEX idx_bank_code ON trm_bank_t(bank_code);
CREATE INDEX idx_bank_country ON trm_bank_t(country_code);

-- 7. 交易对手表
CREATE TABLE trm_counterparty_t (
    id                  BIGSERIAL PRIMARY KEY,
    counterparty_code   VARCHAR(50) NOT NULL UNIQUE,
    counterparty_name   VARCHAR(200) NOT NULL,
    counterparty_name_en VARCHAR(200),
    counterparty_type   VARCHAR(20) NOT NULL,
    country_code        VARCHAR(10) NOT NULL,
    credit_rating       VARCHAR(10),
    external_rating     VARCHAR(10),
    address             VARCHAR(500),
    phone               VARCHAR(30),
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_counterparty_t IS '交易对手表';
CREATE INDEX idx_counterparty_code ON trm_counterparty_t(counterparty_code);

-- 8. 对手方银行账户表
CREATE TABLE trm_counterparty_account_t (
    id                  BIGSERIAL PRIMARY KEY,
    account_no          VARCHAR(50) NOT NULL UNIQUE,
    counterparty_id     BIGINT NOT NULL,
    bank_id             BIGINT NOT NULL,
    account_name        VARCHAR(200) NOT NULL,
    account_number      VARCHAR(50) NOT NULL,
    currency_code       VARCHAR(10) NOT NULL,
    account_type        VARCHAR(20) NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_counterparty_account_t IS '对手方银行账户表';
CREATE INDEX idx_cpa_no ON trm_counterparty_account_t(account_no);
CREATE INDEX idx_cpa_counterparty ON trm_counterparty_account_t(counterparty_id);

-- ============================================
-- 二、账户管理模块 (basedata)
-- ============================================

-- 9. 银行账户表
CREATE TABLE trm_bank_account_t (
    id                  BIGSERIAL PRIMARY KEY,
    account_no          VARCHAR(50) NOT NULL UNIQUE,
    account_name        VARCHAR(200) NOT NULL,
    account_number      VARCHAR(50) NOT NULL,
    bank_id             BIGINT NOT NULL,
    currency_code       VARCHAR(10) NOT NULL,
    account_type        VARCHAR(20) NOT NULL,
    balance             DECIMAL(18,2) DEFAULT 0,
    available_balance   DECIMAL(18,2) DEFAULT 0,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_bank_account_t IS '银行账户表';
CREATE INDEX idx_ba_no ON trm_bank_account_t(account_no);
CREATE INDEX idx_ba_bank ON trm_bank_account_t(bank_id);

-- ============================================
-- 三、交易模块 (dealing)
-- ============================================

-- 10. 交易主表
CREATE TABLE trm_transaction_t (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_no      VARCHAR(50) NOT NULL UNIQUE,
    transaction_type    VARCHAR(20) NOT NULL,
    sub_type            VARCHAR(20),
    business_unit_id    BIGINT NOT NULL,
    trader_id           BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_transaction_t IS '交易主表';
CREATE INDEX idx_txn_no ON trm_transaction_t(transaction_no);
CREATE INDEX idx_txn_type ON trm_transaction_t(transaction_type);
CREATE INDEX idx_txn_status ON trm_transaction_t(status);

-- 11. 存款交易明细
CREATE TABLE trm_transaction_deposit_t (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_id      BIGINT NOT NULL UNIQUE,
    counterparty_id     BIGINT,
    counterparty_acc_id BIGINT,
    currency_code       VARCHAR(10) NOT NULL,
    amount              DECIMAL(18,2) NOT NULL,
    interest_rate       DECIMAL(10,4),
    value_date          DATE NOT NULL,
    maturity_date       DATE NOT NULL,
    interest_type       VARCHAR(20),
    remark              VARCHAR(500),
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_transaction_deposit_t IS '存款交易明细表';
CREATE INDEX idx_deposit_txn ON trm_transaction_deposit_t(transaction_id);

-- 12. 贷款交易明细
CREATE TABLE trm_transaction_loan_t (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_id      BIGINT NOT NULL UNIQUE,
    bank_id             BIGINT,
    currency_code       VARCHAR(10) NOT NULL,
    loan_amount         DECIMAL(18,2) NOT NULL,
    interest_rate       DECIMAL(10,4) NOT NULL,
    loan_date           DATE NOT NULL,
    maturity_date       DATE NOT NULL,
    repayment_type      VARCHAR(20),
    guarantee_type      VARCHAR(20),
    remark              VARCHAR(500),
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_transaction_loan_t IS '贷款交易明细表';
CREATE INDEX idx_loan_txn ON trm_transaction_loan_t(transaction_id);

-- 13. 外汇交易明细
CREATE TABLE trm_transaction_fx_t (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_id      BIGINT NOT NULL UNIQUE,
    direction           VARCHAR(10) NOT NULL,
    source_currency     VARCHAR(10) NOT NULL,
    target_currency     VARCHAR(10) NOT NULL,
    source_amount       DECIMAL(18,2) NOT NULL,
    target_amount       DECIMAL(18,2),
    exchange_rate       DECIMAL(18,8) NOT NULL,
    value_date          DATE NOT NULL,
    bank_id             BIGINT,
    remark              VARCHAR(500),
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_transaction_fx_t IS '外汇交易明细表';
CREATE INDEX idx_fx_txn ON trm_transaction_fx_t(transaction_id);

-- ============================================
-- 四、工作流模块 (workflow)
-- ============================================

-- 14. 审批流程配置表
CREATE TABLE trm_workflow_config_t (
    id                  BIGSERIAL PRIMARY KEY,
    workflow_code       VARCHAR(50) NOT NULL UNIQUE,
    workflow_name       VARCHAR(100) NOT NULL,
    workflow_type       VARCHAR(20) NOT NULL,
    amount_threshold    DECIMAL(18,2),
    level               INT NOT NULL DEFAULT 1,
    approver_role       VARCHAR(50),
    approver_user       VARCHAR(50),
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_workflow_config_t IS '审批流程配置表';
CREATE INDEX idx_wfc_code ON trm_workflow_config_t(workflow_code);

-- 15. 审批记录表
CREATE TABLE trm_approval_record_t (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_id      BIGINT NOT NULL,
    workflow_id         BIGINT NOT NULL,
    approver            VARCHAR(50) NOT NULL,
    approval_time       TIMESTAMP,
    approval_result     VARCHAR(20) NOT NULL,
    approval_comment    VARCHAR(500),
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_approval_record_t IS '审批记录表';
CREATE INDEX idx_ar_txn ON trm_approval_record_t(transaction_id);

-- ============================================
-- 五、后端管理模块 (admin)
-- ============================================

-- 16. 用户表
CREATE TABLE trm_user_t (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50) NOT NULL UNIQUE,
    password            VARCHAR(200) NOT NULL,
    real_name           VARCHAR(50),
    email               VARCHAR(100),
    phone               VARCHAR(30),
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_user_t IS '用户表';
CREATE INDEX idx_user_name ON trm_user_t(username);

-- 17. 角色表
CREATE TABLE trm_role_t (
    id                  BIGSERIAL PRIMARY KEY,
    role_code           VARCHAR(50) NOT NULL UNIQUE,
    role_name           VARCHAR(100) NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_role_t IS '角色表';

-- 18. 用户角色关联表
CREATE TABLE trm_user_role_t (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    role_id             BIGINT NOT NULL,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_user_role_t IS '用户角色关联表';

-- 19. 权限表
CREATE TABLE trm_permission_t (
    id                  BIGSERIAL PRIMARY KEY,
    permission_code    VARCHAR(100) NOT NULL UNIQUE,
    permission_name    VARCHAR(100) NOT NULL,
    parent_id           BIGINT,
    permission_type    VARCHAR(20),
    path                VARCHAR(200),
    component           VARCHAR(200),
    sort_order          INT DEFAULT 0,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_permission_t IS '权限表';

-- 20. 角色权限关联表
CREATE TABLE trm_role_permission_t (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL,
    permission_id      BIGINT NOT NULL,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_role_permission_t IS '角色权限关联表';

-- 21. 数据字典表
CREATE TABLE trm_dict_t (
    id                  BIGSERIAL PRIMARY KEY,
    dict_type           VARCHAR(50) NOT NULL,
    dict_code           VARCHAR(50) NOT NULL,
    dict_value          VARCHAR(100) NOT NULL,
    sort_order          INT DEFAULT 0,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_dict_t IS '数据字典表';
CREATE INDEX idx_dict_type ON trm_dict_t(dict_type);

-- 22. 值集表
CREATE TABLE trm_value_set_t (
    id                  BIGSERIAL PRIMARY KEY,
    value_set_code      VARCHAR(50) NOT NULL UNIQUE,
    value_set_name      VARCHAR(100) NOT NULL,
    description         VARCHAR(500),
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_value_set_t IS '值集表';

-- 23. 值集值表
CREATE TABLE trm_value_set_value_t (
    id                  BIGSERIAL PRIMARY KEY,
    value_set_id        BIGINT NOT NULL,
    value_code          VARCHAR(50) NOT NULL,
    value_name          VARCHAR(100) NOT NULL,
    sort_order          INT DEFAULT 0,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_value_set_value_t IS '值集值表';
CREATE INDEX idx_vsv_set ON trm_value_set_value_t(value_set_id);

-- 24. 定时任务表
CREATE TABLE trm_job_t (
    id                  BIGSERIAL PRIMARY KEY,
    job_code            VARCHAR(50) NOT NULL UNIQUE,
    job_name            VARCHAR(100) NOT NULL,
    job_class           VARCHAR(200) NOT NULL,
    cron_expression     VARCHAR(50),
    status              CHAR(1) NOT NULL DEFAULT '1',
    last_run_time       TIMESTAMP,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_job_t IS '定时任务表';

-- ============================================
-- 六、资金池模块 (cashpool)
-- ============================================

-- 25. 现金池配置表
CREATE TABLE trm_cash_pool_t (
    id                  BIGSERIAL PRIMARY KEY,
    pool_code           VARCHAR(50) NOT NULL UNIQUE,
    pool_name           VARCHAR(200) NOT NULL,
    pool_type           VARCHAR(20) NOT NULL,
    master_account_id  BIGINT,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_cash_pool_t IS '现金池配置表';
CREATE INDEX idx_cp_code ON trm_cash_pool_t(pool_code);

-- 26. 现金池成员账户表
CREATE TABLE trm_cash_pool_member_t (
    id                  BIGSERIAL PRIMARY KEY,
    pool_id             BIGINT NOT NULL,
    account_id          BIGINT NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_cash_pool_member_t IS '现金池成员账户表';
CREATE INDEX idx_cpm_pool ON trm_cash_pool_member_t(pool_id);

-- 27. 自动调拨规则表
CREATE TABLE trm_auto_transfer_rule_t (
    id                  BIGSERIAL PRIMARY KEY,
    pool_id             BIGINT NOT NULL,
    rule_name           VARCHAR(100) NOT NULL,
    rule_type           VARCHAR(20) NOT NULL,
    threshold_amount    DECIMAL(18,2),
    target_account_id  BIGINT,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE trm_auto_transfer_rule_t IS '自动调拨规则表';

-- ============================================
-- 审计追踪表
-- ============================================

-- 28. 审计日志表
CREATE TABLE trm_audit_log_t (
    id                  BIGSERIAL PRIMARY KEY,
    table_name          VARCHAR(50) NOT NULL,
    operation_type      VARCHAR(20) NOT NULL,
    operation_user      VARCHAR(50) NOT NULL,
    operation_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_value        JSONB,
    after_value         JSONB,
    ip_address          VARCHAR(50),
    remark              VARCHAR(500)
);

COMMENT ON TABLE trm_audit_log_t IS '审计日志表';
CREATE INDEX idx_al_table ON trm_audit_log_t(table_name);
CREATE INDEX idx_al_user ON trm_audit_log_t(operation_user);
CREATE INDEX idx_al_time ON trm_audit_log_t(operation_time);

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化币种
INSERT INTO trm_currency_t (currency_code, currency_name, currency_symbol, decimal_places, created_by) VALUES 
('CNY', '人民币', '¥', 2, 'SYSTEM'),
('USD', '美元', '$', 2, 'SYSTEM'),
('EUR', '欧元', '€', 2, 'SYSTEM'),
('GBP', '英镑', '£', 2, 'SYSTEM'),
('JPY', '日元', '¥', 0, 'SYSTEM'),
('HKD', '港币', 'HK$', 2, 'SYSTEM');

-- 初始化国家
INSERT INTO trm_country_t (country_code, country_name, country_name_en, timezone, country_code_no, created_by) VALUES 
('CN', '中国', 'China', 'Asia/Shanghai', '+86', 'SYSTEM'),
('US', '美国', 'United States', 'America/New_York', '+1', 'SYSTEM'),
('GB', '英国', 'United Kingdom', 'Europe/London', '+44', 'SYSTEM'),
('JP', '日本', 'Japan', 'Asia/Tokyo', '+81', 'SYSTEM'),
('HK', '香港', 'Hong Kong', 'Asia/Hong_Kong', '+852', 'SYSTEM');

-- 初始化管理员用户 (密码: 123456)
INSERT INTO trm_user_t (username, password, real_name, created_by) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'SYSTEM');