-- 19-dealmap-v2-step.sql: 拆分执行版（避免 DO $$ 块）
-- v2.0 数据库迁移（分步执行）

-- 1) 移除 Action 表 deal_number UNIQUE 约束
ALTER TABLE tms_actions_t DROP CONSTRAINT IF EXISTS tms_actions_t_deal_number_key;
CREATE INDEX IF NOT EXISTS idx_action_deal_number ON tms_actions_t(deal_number);
ALTER TABLE tms_actions_t ADD COLUMN IF NOT EXISTS approval_opinion VARCHAR(500);

-- 2) 创建 DealMap 表（v2.0 精简版）
CREATE TABLE IF NOT EXISTS tms_deal_map_t (
    id                   BIGSERIAL       PRIMARY KEY,
    dealmap_number       VARCHAR(50)     NOT NULL UNIQUE,
    deal_number          VARCHAR(50)     NOT NULL,
    action_number        VARCHAR(50)     NOT NULL,
    event_type           VARCHAR(30)     NOT NULL,
    event_status         VARCHAR(20)     NOT NULL DEFAULT 'Active',
    amount               DECIMAL(38,18),
    currency             VARCHAR(10),
    direction            VARCHAR(10),
    event_date           DATE            NOT NULL,
    value_date           DATE,
    is_reversal          CHAR(1)         NOT NULL DEFAULT '0',
    reverses_event_id    BIGINT,
    reversed_by_event_id BIGINT,
    description          VARCHAR(500),
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',
    CONSTRAINT chk_dm_event_status CHECK (event_status IN ('Active', 'Inactive')),
    CONSTRAINT chk_dm_is_reversal CHECK (is_reversal IN ('0', '1'))
);

CREATE INDEX IF NOT EXISTS idx_dm_deal              ON tms_deal_map_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_dm_action            ON tms_deal_map_t(action_number);
CREATE INDEX IF NOT EXISTS idx_dm_event_type        ON tms_deal_map_t(event_type);
CREATE INDEX IF NOT EXISTS idx_dm_event_date        ON tms_deal_map_t(event_date);
CREATE INDEX IF NOT EXISTS idx_dm_deleted          ON tms_deal_map_t(deleted);
CREATE INDEX IF NOT EXISTS idx_dm_action_lookup     ON tms_deal_map_t(action_number, deleted);

-- 3) 创建 Cashflow 表
CREATE TABLE IF NOT EXISTS tms_cashflow_t (
    id                      BIGSERIAL       PRIMARY KEY,
    cflow_number            VARCHAR(50)     NOT NULL UNIQUE,
    deal_number             VARCHAR(50),
    dealmap_number          VARCHAR(50),
    business_unit           VARCHAR(50)     NOT NULL,
    bank_account            VARCHAR(50),
    counterparty_account    VARCHAR(50),
    direction               VARCHAR(10)     NOT NULL,
    amount                  DECIMAL(38,18)  NOT NULL,
    currency                VARCHAR(10)     NOT NULL,
    cflow_date              DATE            NOT NULL,
    value_date              DATE            NOT NULL,
    source_type             VARCHAR(20)     NOT NULL,
    source_ref              VARCHAR(50),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'Created',
    counterparty_name       VARCHAR(200),
    purpose                 VARCHAR(500),
    remark                  VARCHAR(500),
    created_by              VARCHAR(50)     NOT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(50),
    updated_at              TIMESTAMP,
    version                 INT             NOT NULL DEFAULT 0,
    deleted                 CHAR(1)         NOT NULL DEFAULT '0',
    CONSTRAINT chk_cf_status CHECK (status IN ('Created', 'Cleared', 'Reconciled', 'Canceled', 'Failed'))
);

CREATE INDEX IF NOT EXISTS idx_cf_deal              ON tms_cashflow_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_cf_dealmap_number    ON tms_cashflow_t(dealmap_number);
CREATE INDEX IF NOT EXISTS idx_cf_status            ON tms_cashflow_t(status);
CREATE INDEX IF NOT EXISTS idx_cf_date              ON tms_cashflow_t(cflow_date);

COMMENT ON TABLE  tms_deal_map_t IS 'DealMap 业务事件表（v2.0 精简版）- 记录交易生命周期业务事件';
COMMENT ON COLUMN tms_deal_map_t.dealmap_number IS '事件编号 格式DMP+yyyyMMdd+序号';
COMMENT ON COLUMN tms_deal_map_t.deal_number IS '关联交易编号';
COMMENT ON COLUMN tms_deal_map_t.action_number IS '触发此DealMap的Action编号';
COMMENT ON COLUMN tms_deal_map_t.event_type IS 'ActualCashflow/AccountTransfer/Unwind/RateFix/Coupon/MTM等';
COMMENT ON COLUMN tms_deal_map_t.event_status IS 'Active/Inactive（无审批相关字段）';
COMMENT ON COLUMN tms_deal_map_t.is_reversal IS '是否冲销事件 0否1是';

COMMENT ON TABLE  tms_cashflow_t IS '现金流表 - AC/AT执行后自动创建，关联DealMap';
COMMENT ON COLUMN tms_cashflow_t.dealmap_number IS '关联DealMap编号（v2.0新增字段，反向关联）';
