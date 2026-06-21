-- Open-TMS M1 DealMap v2.0 数据库迁移脚本
-- 版本: v2.0
-- 日期: 2026-06-21
-- 依赖: 13-deal.sql, 02-dealing.sql

-- ============================================================================
-- v2.0 核心变更：
-- 1. Action 表：移除 deal_number UNIQUE 约束（多 Action/Deal）
-- 2. 新增 DealMap 表：精简字段（25字段）
-- 3. Cashflow 表：新增 dealmap_number 字段（反向关联）
-- 4. 撤销 v1.x 引入的 event_category/event_timing/event_subtype 等冗余字段
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Action 表结构调整（v2.0）
-- ----------------------------------------------------------------------------
-- 移除 Action 表的 deal_number UNIQUE 约束
-- 允许一笔 Deal 有多个独立 Action（CREATE/UPDATE/DELETE/APPROVE/REJECT）
ALTER TABLE tms_actions_t DROP CONSTRAINT IF EXISTS tms_actions_t_deal_number_key;

-- 验证 UNIQUE 约束已存在则删除
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'tms_actions_t_deal_number_key'
        AND conrelid = 'tms_actions_t'::regclass
    ) THEN
        ALTER TABLE tms_actions_t DROP CONSTRAINT tms_actions_t_deal_number_key;
    END IF;
END $$;

-- 添加索引（替代 UNIQUE 约束）
CREATE INDEX IF NOT EXISTS idx_action_deal_number ON tms_actions_t(deal_number);

-- 添加 Action 类型扩展（用于 APPROVE/REJECT）
-- action_type: CREATE / UPDATE / DELETE / APPROVE / REJECT
ALTER TABLE tms_actions_t
    ADD COLUMN IF NOT EXISTS approval_opinion VARCHAR(500);

COMMENT ON COLUMN tms_actions_t.action_type IS 'CREATE/UPDATE/DELETE/APPROVE/REJECT';
COMMENT ON COLUMN tms_actions_t.action_status IS 'Pending/Approved/Rejected/Executed/Canceled';
COMMENT ON COLUMN tms_actions_t.approval_status1 IS '一级审批状态：Pending/Approved/Rejected';
COMMENT ON COLUMN tms_actions_t.approval_status2 IS '二级审批状态：Pending/Approved/Rejected';

-- ----------------------------------------------------------------------------
-- 2. DealMap 表（v2.0 精简版）—— 新增
-- ----------------------------------------------------------------------------
-- 核心字段：dealmap_number / deal_number / action_number / event_type
-- 辅助字段：amount / currency / direction / event_date / value_date / event_status
-- 冲销字段：is_reversal / reverses_event_id / reversed_by_event_id
-- 审计字段：created_by/at / updated_by/at / version / deleted

CREATE TABLE IF NOT EXISTS tms_deal_map_t (
    id                   BIGSERIAL       PRIMARY KEY,
    dealmap_number       VARCHAR(50)     NOT NULL UNIQUE,    -- DMP+yyyyMMdd+序号
    deal_number          VARCHAR(50)     NOT NULL,
    action_number        VARCHAR(50)     NOT NULL,
    event_type           VARCHAR(30)     NOT NULL,
    event_status         VARCHAR(20)     NOT NULL DEFAULT 'Active',

    -- 金融字段
    amount               DECIMAL(38,18),
    currency             VARCHAR(10),
    direction            VARCHAR(10),

    -- 时间字段
    event_date           DATE            NOT NULL,
    value_date           DATE,

    -- 冲销关系
    is_reversal          CHAR(1)         NOT NULL DEFAULT '0',
    reverses_event_id    BIGINT,
    reversed_by_event_id BIGINT,

    -- 描述
    description          VARCHAR(500),

    -- 审计字段
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    CONSTRAINT chk_dm_event_status CHECK (event_status IN ('Active', 'Inactive')),
    CONSTRAINT chk_dm_is_reversal CHECK (is_reversal IN ('0', '1'))
);

-- 索引
CREATE INDEX idx_dm_deal              ON tms_deal_map_t(deal_number);
CREATE INDEX idx_dm_action            ON tms_deal_map_t(action_number);
CREATE INDEX idx_dm_event_type        ON tms_deal_map_t(event_type);
CREATE INDEX idx_dm_event_date        ON tms_deal_map_t(event_date);
CREATE INDEX idx_dm_deleted          ON tms_deal_map_t(deleted);
CREATE INDEX idx_dm_action_lookup     ON tms_deal_map_t(action_number, deleted);

COMMENT ON TABLE  tms_deal_map_t IS 'DealMap 业务事件表（v2.0 精简版）- 记录交易生命周期业务事件';
COMMENT ON COLUMN tms_deal_map_t.dealmap_number IS '事件编号 格式DMP+yyyyMMdd+序号';
COMMENT ON COLUMN tms_deal_map_t.deal_number IS '关联交易编号';
COMMENT ON COLUMN tms_deal_map_t.action_number IS '触发此DealMap的Action编号';
COMMENT ON COLUMN tms_deal_map_t.event_type IS 'ActualCashflow/AccountTransfer/Unwind/RateFix/Coupon/MTM等';
COMMENT ON COLUMN tms_deal_map_t.event_status IS 'Active/Inactive（无审批相关字段）';
COMMENT ON COLUMN tms_deal_map_t.is_reversal IS '是否冲销事件 0否1是';

-- ----------------------------------------------------------------------------
-- 3. Cashflow 表新增字段（v2.0）—— 改造 tms_cashflow_t
-- ----------------------------------------------------------------------------
-- 添加 dealmap_number 字段（字符串引用，非外键）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tms_cashflow_t') THEN
        ALTER TABLE tms_cashflow_t ADD COLUMN IF NOT EXISTS dealmap_number VARCHAR(50);
        CREATE INDEX IF NOT EXISTS idx_cf_dealmap_number ON tms_cashflow_t(dealmap_number);
        COMMENT ON COLUMN tms_cashflow_t.dealmap_number IS '关联DealMap编号（v2.0新增，字符串引用）';
    END IF;
END $$;

-- 若 tms_cashflow_t 表不存在则创建（AC/AT 现金流场景）
CREATE TABLE IF NOT EXISTS tms_cashflow_t (
    id                      BIGSERIAL       PRIMARY KEY,
    cflow_number            VARCHAR(50)     NOT NULL UNIQUE,
    deal_number             VARCHAR(50),
    dealmap_number          VARCHAR(50),                          -- v2.0 新增
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

CREATE INDEX idx_cf_deal              ON tms_cashflow_t(deal_number);
CREATE INDEX idx_cf_dealmap_number    ON tms_cashflow_t(dealmap_number);
CREATE INDEX idx_cf_status            ON tms_cashflow_t(status);
CREATE INDEX idx_cf_date              ON tms_cashflow_t(cflow_date);

COMMENT ON TABLE  tms_cashflow_t IS '现金流表 - AC/AT执行后自动创建，关联DealMap';
COMMENT ON COLUMN tms_cashflow_t.dealmap_number IS '关联DealMap编号（v2.0新增字段，反向关联）';

-- ============================================================================
-- 验证脚本
-- ============================================================================
-- 检查 Action 表 UNIQUE 约束已移除
SELECT conname, contype FROM pg_constraint
WHERE conrelid = 'tms_actions_t'::regclass AND contype = 'u';

-- 检查 DealMap 表已创建
SELECT tablename FROM pg_tables WHERE tablename = 'tms_deal_map_t';

-- 检查 Cashflow 表新增字段
SELECT column_name FROM information_schema.columns
WHERE table_name = 'tms_cashflow_t' AND column_name = 'dealmap_number';

-- ============================================================================
-- v2.0 重要变更总结
-- ============================================================================
-- 1. Action 表：移除 deal_number UNIQUE → 允许多个 Action/Deal
-- 2. DealMap 表（新增）：25 字段，记录业务事件（仅 Past/Present/Future 不分）
-- 3. Cashflow 表：新增 dealmap_number 字段（反向关联 DealMap）
-- 4. 移除字段：event_category / event_subtype / event_timing / trigger_source
--              action_id / deal_id / cflow_id / bank_account_id 等冗余字段
-- 5. 业务规则：
--    - CREATE 不生成 DealImage（仅修改/删除生成）
--    - UPDATE 软删旧 DealMap + 新建新 DealMap
--    - DELETE 级联软删 Deal + AcDeal + DealMap + Cashflow
--    - 审批仅作用于 Action（不影响 DealMap/Cashflow）
-- ============================================================================