-- Open-TMS M1-AT交易(Account Transfer Deal)数据库设计
-- 版本: v1.0
-- 日期: 2026-06-21
-- 依赖: 13-deal.sql (tms_deals_t / tms_actions_t), 19-dealmap-v2.sql (tms_deal_map_t / tms_cashflow_t)
-- 设计依据: M1-DealMap 生命周期事件PRD v2.0

-- ============================================================================
-- 设计理念（v2.0 双腿设计）
-- ============================================================================
-- 1. AT 交易是账户间资金划转交易（Account Transfer），是 M1 基础数据交易类型的核心模块
-- 2. 双腿设计核心：source_account_id（付出方）→ dest_account_id（收入方）
--    - 与 AC 单账户设计的关键差异：AT 强调账户间的资金流转关系
-- 3. 跨币种支持：source_currency ≠ dest_currency 时记录 exchange_rate
--    - 同币种时 exchange_rate = 1.0
-- 4. 转账类型分类：
--    - SAME_COMPANY 同公司转账（同一 BU 下账户间）
--    - CROSS_COMPANY 跨公司转账（不同 BU 账户间）
--    - CROSS_BORDER 跨境转账（涉及外汇管制）
-- 5. 支付方式：
--    - INTERNAL 内部转账（行内）
--    - SWIFT 环球银行间金融电讯协会电汇
--    - RTGS 实时全额结算系统
-- 6. 镜像规则：CREATE 不生成 Image；UPDATE/DELETE 生成 Image（v2.0 理念）
-- 7. DealMap 自动生成：AT 创建时自动生成 4 条 DealMap + 2 条 Cashflow
--    - AccountTransfer × 2（SOURCE Outflow / DESTINATION Inflow）
--    - ActualCashflow × 2（SOURCE Outflow / DESTINATION Inflow）
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. AT 交易个性化表 (tms_at_deals_t)
-- ----------------------------------------------------------------------------
-- 存储 AT 交易的个性化字段
-- 关联交易编号 deal_number 与 tms_deals_t.deal_number 保持一致（UNIQUE）
CREATE TABLE tms_at_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,           -- 关联交易编号（与 tms_deals_t.deal_number 保持一致）

    -- 转账类型
    transfer_type VARCHAR(30) NOT NULL,                 -- SAME_COMPANY 同公司 / CROSS_COMPANY 跨公司 / CROSS_BORDER 跨境

    -- 双账户字段（双腿设计核心）
    source_account_id BIGINT NOT NULL,                  -- 付出方银行账户 ID
    dest_account_id BIGINT NOT NULL,                    -- 收入方银行账户 ID

    -- 金额与币种（支持跨币种）
    source_amount DECIMAL(38,18) NOT NULL,              -- 付出方金额
    dest_amount DECIMAL(38,18) NOT NULL,                -- 收入方金额
    source_currency VARCHAR(10) NOT NULL,               -- 付出方币种
    dest_currency VARCHAR(10) NOT NULL,                 -- 收入方币种
    exchange_rate DECIMAL(38,18),                       -- 跨币种时记录汇率（同币种为 1）

    -- 业务字段
    business_unit VARCHAR(50) NOT NULL,
    value_date DATE NOT NULL,                           -- 预计到账日
    payment_method VARCHAR(20) NOT NULL,                -- INTERNAL 内部转账 / SWIFT 电汇 / RTGS 实时结算
    purpose VARCHAR(500),                               -- 资金用途

    -- 状态字段
    status VARCHAR(20) NOT NULL DEFAULT 'New',          -- New / Approved / Rejected / Settled / Canceled

    -- 关联 Action
    latest_action_number VARCHAR(50),                   -- 最近一次操作的 Action 编号

    -- 标准审计字段
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0',

    -- 校验约束
    CONSTRAINT chk_at_transfer_type CHECK (transfer_type IN ('SAME_COMPANY', 'CROSS_COMPANY', 'CROSS_BORDER')),
    CONSTRAINT chk_at_status CHECK (status IN ('New', 'Approved', 'Rejected', 'Settled', 'Canceled')),
    CONSTRAINT chk_at_payment_method CHECK (payment_method IN ('INTERNAL', 'SWIFT', 'RTGS')),
    CONSTRAINT chk_at_diff_account CHECK (source_account_id != dest_account_id),
    CONSTRAINT chk_at_source_amount_positive CHECK (source_amount > 0),
    CONSTRAINT chk_at_dest_amount_positive CHECK (dest_amount > 0),
    CONSTRAINT chk_at_exchange_rate_positive CHECK (exchange_rate IS NULL OR exchange_rate > 0)
);

-- 索引
CREATE INDEX idx_at_deal_number ON tms_at_deals_t(deal_number);
CREATE INDEX idx_at_source_account ON tms_at_deals_t(source_account_id);
CREATE INDEX idx_at_dest_account ON tms_at_deals_t(dest_account_id);
CREATE INDEX idx_at_business_unit ON tms_at_deals_t(business_unit);
CREATE INDEX idx_at_transfer_type ON tms_at_deals_t(transfer_type);
CREATE INDEX idx_at_status ON tms_at_deals_t(status);
CREATE INDEX idx_at_value_date ON tms_at_deals_t(value_date);
CREATE INDEX idx_at_deleted ON tms_at_deals_t(deleted);
CREATE INDEX idx_at_account_pair ON tms_at_deals_t(source_account_id, dest_account_id);

-- 表与字段注释
COMMENT ON TABLE tms_at_deals_t IS 'AT 账户转账交易个性化表（v2.0 双腿设计：source_account → dest_account）';
COMMENT ON COLUMN tms_at_deals_t.deal_number IS '关联交易编号（与 tms_deals_t.deal_number 一致）';
COMMENT ON COLUMN tms_at_deals_t.transfer_type IS '转账类型：SAME_COMPANY 同公司 / CROSS_COMPANY 跨公司 / CROSS_BORDER 跨境';
COMMENT ON COLUMN tms_at_deals_t.source_account_id IS '付出方银行账户 ID（基于 tms_bank_account_t）';
COMMENT ON COLUMN tms_at_deals_t.dest_account_id IS '收入方银行账户 ID（基于 tms_bank_account_t）';
COMMENT ON COLUMN tms_at_deals_t.source_amount IS '付出方金额（DECIMAL(38,18)）';
COMMENT ON COLUMN tms_at_deals_t.dest_amount IS '收入方金额（DECIMAL(38,18)）';
COMMENT ON COLUMN tms_at_deals_t.source_currency IS '付出方币种（ISO 4217 货币代码）';
COMMENT ON COLUMN tms_at_deals_t.dest_currency IS '收入方币种（ISO 4217 货币代码）';
COMMENT ON COLUMN tms_at_deals_t.exchange_rate IS '跨币种汇率（source → dest）；同币种为 1';
COMMENT ON COLUMN tms_at_deals_t.business_unit IS '业务单元编码';
COMMENT ON COLUMN tms_at_deals_t.value_date IS '预计到账日（资金到达 dest_account 的日期）';
COMMENT ON COLUMN tms_at_deals_t.payment_method IS '支付方式：INTERNAL 内部转账 / SWIFT 国际电汇 / RTGS 实时全额结算';
COMMENT ON COLUMN tms_at_deals_t.purpose IS '资金用途说明';
COMMENT ON COLUMN tms_at_deals_t.status IS '交易状态：New 新建 / Approved 已审批 / Rejected 已驳回 / Settled 已清算 / Canceled 已取消';
COMMENT ON COLUMN tms_at_deals_t.latest_action_number IS '最近一次 Action 编号（关联 tms_actions_t.action_number）';

-- ----------------------------------------------------------------------------
-- 2. AT 交易镜像表 (tms_at_deals_image_t)
-- ----------------------------------------------------------------------------
-- 用于 UPDATE / DELETE 时保存字段快照
-- CREATE 不生成 Image（v2.0 理念）
CREATE TABLE tms_at_deals_image_t (
    id BIGSERIAL PRIMARY KEY,
    image_number VARCHAR(50) NOT NULL UNIQUE,           -- 镜像编号 IMG+yyyyMMdd+序号
    deal_number VARCHAR(50) NOT NULL,                   -- 关联交易编号
    version INT NOT NULL,                               -- 镜像版本号

    -- AT 个性化字段快照（与 tms_at_deals_t 保持一致）
    transfer_type VARCHAR(30),
    source_account_id BIGINT,
    dest_account_id BIGINT,
    source_amount DECIMAL(38,18),
    dest_amount DECIMAL(38,18),
    source_currency VARCHAR(10),
    dest_currency VARCHAR(10),
    exchange_rate DECIMAL(38,18),
    business_unit VARCHAR(50),
    value_date DATE,
    payment_method VARCHAR(20),
    purpose VARCHAR(500),
    status VARCHAR(20),
    latest_action_number VARCHAR(50),

    -- 镜像操作元数据
    image_type VARCHAR(20) NOT NULL,                    -- CREATE / UPDATE / DELETE（AT 中仅 UPDATE/DELETE 会生成）
    operator VARCHAR(50) NOT NULL,                      -- 操作人
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 操作时间

    -- 标准审计字段（精简版）
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

-- 索引
CREATE INDEX idx_at_image_deal ON tms_at_deals_image_t(deal_number);
CREATE INDEX idx_at_image_version ON tms_at_deals_image_t(deal_number, version);
CREATE INDEX idx_at_image_type ON tms_at_deals_image_t(image_type);

-- 表与字段注释
COMMENT ON TABLE tms_at_deals_image_t IS 'AT 镜像快照表（UPDATE/DELETE 时生成，CREATE 不生成 - v2.0 理念）';
COMMENT ON COLUMN tms_at_deals_image_t.image_number IS '镜像编号 IMG+yyyyMMdd+序号';
COMMENT ON COLUMN tms_at_deals_image_t.deal_number IS '关联交易编号';
COMMENT ON COLUMN tms_at_deals_image_t.version IS '镜像版本号（与 Deal.version 对应）';
COMMENT ON COLUMN tms_at_deals_image_t.image_type IS '镜像类型：UPDATE / DELETE（AT 中不会生成 CREATE 类型）';
COMMENT ON COLUMN tms_at_deals_image_t.operator IS '触发镜像的操作人';
COMMENT ON COLUMN tms_at_deals_image_t.operate_at IS '镜像生成时间';

-- ============================================================================
-- 跨币种汇率示例
-- ============================================================================
-- 示例 1（同币种）：
--   transfer_type = SAME_COMPANY
--   source_amount = 1000000.00, source_currency = 'CNY'
--   dest_amount   = 1000000.00, dest_currency   = 'CNY'
--   exchange_rate = 1.0
--
-- 示例 2（跨币种）：
--   transfer_type = CROSS_BORDER
--   source_amount = 100000.00, source_currency = 'USD'
--   dest_amount   = 720000.00, dest_currency   = 'CNY'
--   exchange_rate = 7.2
--   说明：100,000 USD × 7.2 = 720,000 CNY
--
-- 示例 3（跨境反向）：
--   transfer_type = CROSS_BORDER
--   source_amount = 720000.00, source_currency = 'CNY'
--   dest_amount   = 100000.00, dest_currency   = 'USD'
--   exchange_rate = 0.1389（≈ 1/7.2，CNY → USD 的反向汇率）

-- ============================================================================
-- 双腿 DealMap 自动生成规则（AT 交易创建时触发）
-- ============================================================================
-- 1 笔 AT Deal 创建后，将自动生成 4 条 DealMap + 2 条 Cashflow：
--
-- DealMap #1: AccountTransfer SOURCE
--   dealmap_number = DMP+yyyyMMdd+0001
--   event_type     = 'AccountTransfer'
--   account_role   = SOURCE（通过 deal_number + source_account_id 关联识别）
--   direction      = 'Outflow'
--   amount         = source_amount
--   currency       = source_currency
--   description    = '账户间资金划转（付出方）'
--
-- DealMap #2: AccountTransfer DESTINATION
--   dealmap_number = DMP+yyyyMMdd+0002
--   event_type     = 'AccountTransfer'
--   account_role   = DESTINATION
--   direction      = 'Inflow'
--   amount         = dest_amount
--   currency       = dest_currency
--   description    = '账户间资金划转（收入方）'
--
-- DealMap #3: ActualCashflow SOURCE
--   dealmap_number = DMP+yyyyMMdd+0003
--   event_type     = 'ActualCashflow'
--   account_role   = SOURCE
--   direction      = 'Outflow'
--   amount         = source_amount
--   currency       = source_currency
--   description    = '实际现金流（付出方）'
--
-- DealMap #4: ActualCashflow DESTINATION
--   dealmap_number = DMP+yyyyMMdd+0004
--   event_type     = 'ActualCashflow'
--   account_role   = DESTINATION
--   direction      = 'Inflow'
--   amount         = dest_amount
--   currency       = dest_currency
--   description    = '实际现金流（收入方）'
--
-- Cashflow #1 (SOURCE):
--   cflow_number   = CF+yyyyMMdd+0001
--   dealmap_number = DMP+yyyyMMdd+0001（关联 DealMap #1 或 #3）
--   bank_account   = source_account_id
--   direction      = 'Outflow'
--   amount         = source_amount
--   currency       = source_currency
--
-- Cashflow #2 (DESTINATION):
--   cflow_number   = CF+yyyyMMdd+0002
--   dealmap_number = DMP+yyyyMMdd+0002（关联 DealMap #2 或 #4）
--   bank_account   = dest_account_id
--   direction      = 'Inflow'
--   amount         = dest_amount
--   currency       = dest_currency
--
-- 注：
-- 1. DealMap 表本身仅有 direction 字段（Inflow/Outflow），account_role（SOURCE/DESTINATION）
--    通过 deal_number + source_account_id/dest_account_id 联合识别
-- 2. 若 DealMap 需要显式 account_role 字段，应后续在 v2.1 ALTER TABLE 增加
-- 3. UPDATE AT Deal 时：软删除上述 4 条 DealMap + 2 条 Cashflow，再创建新一批
-- 4. DELETE AT Deal 时：级联软删 Deal + AtDeal + DealMap + Cashflow

-- ============================================================================
-- 验证脚本（DDL 执行后请运行）
-- ============================================================================
-- 检查 tms_at_deals_t 表已创建
SELECT tablename FROM pg_tables WHERE tablename = 'tms_at_deals_t';

-- 检查 tms_at_deals_t 关键字段
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'tms_at_deals_t'
ORDER BY ordinal_position;

-- 检查 tms_at_deals_t 约束
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'tms_at_deals_t'::regclass;

-- 检查 tms_at_deals_t 索引
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'tms_at_deals_t';

-- 检查 tms_at_deals_image_t 表已创建
SELECT tablename FROM pg_tables WHERE tablename = 'tms_at_deals_image_t';

-- 检查 tms_at_deals_image_t 索引
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'tms_at_deals_image_t';

-- ============================================================================
-- 与 AC DDL 的差异点
-- ============================================================================
-- 1. 单账户 vs 双账户：
--    AC：1 个 bank_account_id + 1 个 counterparty_account_id（用户与对手方账户）
--    AT：1 个 source_account_id + 1 个 dest_account_id（账户间资金流转）
--
-- 2. 跨币种支持：
--    AC：单一 currency 字段（用户账户币种）
--    AT：source_currency + dest_currency + exchange_rate（支持跨币种转换）
--
-- 3. 转账类型与支付方式：
--    AC：无 transfer_type 概念；payment_method 较少
--    AT：3 种 transfer_type（SAME_COMPANY/CROSS_COMPANY/CROSS_BORDER）
--        + 3 种 payment_method（INTERNAL/SWIFT/RTGS）
--
-- 4. 校验约束更严格：
--    AC：基本字段非空校验
--    AT：增加 chk_at_diff_account（双账户不能相同）、
--        chk_at_source_amount_positive / chk_at_dest_amount_positive（金额必须正数）、
--        chk_at_exchange_rate_positive（汇率必须正数）
--
-- 5. DealMap 自动生成数量：
--    AC：1 条 DealMap(ActualCashflow) + 1 条 Cashflow
--    AT：4 条 DealMap（2×AccountTransfer + 2×ActualCashflow）+ 2 条 Cashflow（双腿对称）
--
-- 6. 索引差异：
--    AC：按 bank_account_id 索引
--    AT：按 source_account_id + dest_account_id 联合索引
--        + 按 (source_account_id, dest_account_id) 复合索引
--
-- 7. 镜像表字段：
--    AC：仅镜像 bank_account_id / counterparty_account_id / payment_method（3 字段）
--    AT：镜像 12 字段（含双账户、双金额、双币种、汇率、transfer_type 等）

-- ============================================================================
-- AT 交易全生命周期（与 AC 保持一致，遵循 v2.0 理念）
-- ============================================================================
-- CREATE：保存 AT 表单
--   1) INSERT Action(action_type=CREATE)
--   2) INSERT Deal(deal_type='AT', status='New')
--   3) INSERT AtDeal（保存 source_account_id/dest_account_id 等个性化字段）
--   4) ✅ INSERT DealMap × 4（2×AccountTransfer + 2×ActualCashflow）
--   5) ✅ INSERT Cashflow × 2（SOURCE/DESTINATION）
--   6) ❌ 不生成 AtDealImage
--
-- UPDATE：修改 AT 表单
--   1) INSERT Action(action_type=UPDATE)
--   2) UPDATE Deal + UPDATE AtDeal
--   3) 软删除旧 DealMap × 4 + 旧 Cashflow × 2
--   4) ✅ INSERT 新 DealMap × 4 + 新 Cashflow × 2
--   5) INSERT AtDealImage（记录修改前字段快照）
--
-- DELETE：删除 AT 表单
--   1) INSERT Action(action_type=DELETE)
--   2) 软删除 Deal + AtDeal
--   3) 级联软删除 DealMap × 4 + Cashflow × 2
--   4) INSERT AtDealImage（记录删除前完整快照）
--
-- APPROVE / REJECT：基于 Action
--   - UPDATE Action.approval_status1/2
--   - DealMap / Cashflow 状态不变