-- ============================================================================
-- Open-TMS M3-外汇交易特性 DDL v3.2 (2026-07-04)
-- 基于 PRD: docs/prd/M3/M3-外汇交易PRD.md
-- 特性: 后端 calculate + 日期字段移到公共表 + DealMap Amount_or_rate + 多行独立
-- 注: 每条 SQL 一行简单语句,避免 db_tool 解析问题
-- ============================================================================

-- 1. tms_deals_t: 加 trade_date 字段
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS trade_date DATE;

-- 1b. tms_deals_t: 加 maturity_date 字段
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS maturity_date DATE;

-- 1c. tms_deals_t 注释
COMMENT ON COLUMN tms_deals_t.trade_date IS '交易日 v3.2(交易达成的日子)';
COMMENT ON COLUMN tms_deals_t.maturity_date IS '到期日 v3.2(= value_date 不可改)';

-- 2. tms_fx_deals_t: FX 特性表(共享主键)
CREATE TABLE IF NOT EXISTS tms_fx_deals_t (
    id BIGINT NOT NULL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    management_entity_id BIGINT NOT NULL,
    currency_pair_id BIGINT NOT NULL,
    sell_currency VARCHAR(10) NOT NULL,
    sell_amount DECIMAL(38,18) NOT NULL,
    buy_currency VARCHAR(10) NOT NULL,
    buy_amount DECIMAL(38,18) NOT NULL,
    exchange_rate DECIMAL(18,8) NOT NULL,
    market_rate DECIMAL(18,8) NOT NULL,
    spread_bp DECIMAL(10,4) NOT NULL,
    notional DECIMAL(38,18),
    fixing_source VARCHAR(50),
    fixing_rate DECIMAL(18,8),
    settlement_amount DECIMAL(38,18),
    description VARCHAR(500),
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    deleted CHAR(1) NOT NULL DEFAULT '0',
    CONSTRAINT chk_fx_legs_diff CHECK (sell_currency <> buy_currency)
);

-- 2b. tms_fx_deals_t 索引
CREATE INDEX IF NOT EXISTS idx_fxd_mgmt_entity ON tms_fx_deals_t(management_entity_id);
CREATE INDEX IF NOT EXISTS idx_fxd_currency_pair ON tms_fx_deals_t(currency_pair_id);
CREATE INDEX IF NOT EXISTS idx_fxd_sell_ccy ON tms_fx_deals_t(sell_currency);
CREATE INDEX IF NOT EXISTS idx_fxd_buy_ccy ON tms_fx_deals_t(buy_currency);

-- 2c. tms_fx_deals_t 注释
COMMENT ON TABLE tms_fx_deals_t IS '外汇交易特性表 v3.2(共享 tms_deals_t.id)';
COMMENT ON COLUMN tms_fx_deals_t.id IS '★ 共享主键,值=tms_deals_t.id';
COMMENT ON COLUMN tms_fx_deals_t.management_entity_id IS 'FK tms_management_entity_t.id(强类型)';
COMMENT ON COLUMN tms_fx_deals_t.currency_pair_id IS 'FK tms_currency_pair_t.id';
COMMENT ON COLUMN tms_fx_deals_t.notional IS 'NDF 名义本金';
COMMENT ON COLUMN tms_fx_deals_t.fixing_source IS 'NDF fixing 汇率来源';
COMMENT ON COLUMN tms_fx_deals_t.fixing_rate IS 'NDF RATE_FIX 时填入';
COMMENT ON COLUMN tms_fx_deals_t.settlement_amount IS 'NDF 差额';

-- 3. tms_deal_map_t: 加 dealmap_type 字段(v3.2 4 种 FX DealMap 类型)
ALTER TABLE tms_deal_map_t ADD COLUMN IF NOT EXISTS dealmap_type VARCHAR(30) NOT NULL DEFAULT 'FX';

-- 3b. tms_deal_map_t: 加 amount_or_rate 字段(v3.2 单字段多行)
ALTER TABLE tms_deal_map_t ADD COLUMN IF NOT EXISTS amount_or_rate DECIMAL(38,18) NOT NULL DEFAULT 0;

-- 3c. tms_deal_map_t 注释
COMMENT ON COLUMN tms_deal_map_t.dealmap_type IS 'v3.2:FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE/FX_FIX';
COMMENT ON COLUMN tms_deal_map_t.amount_or_rate IS 'v3.2:单字段(替代 buy_amount/sell_amount/rate 3 字段)';

-- 3d. tms_deal_map_t 索引
CREATE INDEX IF NOT EXISTS idx_dm_dealmap_type ON tms_deal_map_t(dealmap_type);

-- 3e. tms_deal_map_t 新增 chk 约束(单字段非空)
ALTER TABLE tms_deal_map_t DROP CONSTRAINT IF EXISTS chk_dm_fx_type_amount;
ALTER TABLE tms_deal_map_t ADD CONSTRAINT chk_dm_fx_type_amount CHECK (
    dealmap_type NOT IN ('FX_BUY_AMOUNT','FX_SELL_AMOUNT','FX_RATE','FX_FIX') OR amount_or_rate IS NOT NULL
);

-- 3f. tms_deal_map_t 移除 v3.1 老 chk 约束(3 字段约束失效)
ALTER TABLE tms_deal_map_t DROP CONSTRAINT IF EXISTS chk_dm_fx_amounts;

-- 4. tms_cashflow_t: 加 dealmap_number 字段(v3.2:1 DealMap → 1 CF 强引用)
ALTER TABLE tms_cashflow_t ADD COLUMN IF NOT EXISTS dealmap_number VARCHAR(50);

-- 4b. tms_cashflow_t 注释
COMMENT ON COLUMN tms_cashflow_t.dealmap_number IS 'v3.2:触发此 CF 生成的 DealMap(1:0/1 强引用)';

-- ============================================================================
-- 验证脚本(应用后手动执行,确认所有 DDL 生效)
-- ============================================================================
-- SELECT column_name FROM information_schema.columns
--  WHERE table_name = 'tms_deals_t' AND column_name IN ('trade_date', 'maturity_date');
-- SELECT 1 FROM pg_tables WHERE tablename = 'tms_fx_deals_t';
-- SELECT column_name FROM information_schema.columns
--  WHERE table_name = 'tms_deal_map_t' AND column_name IN ('dealmap_type', 'amount_or_rate');
-- SELECT column_name FROM information_schema.columns
--  WHERE table_name = 'tms_cashflow_t' AND column_name = 'dealmap_number';
