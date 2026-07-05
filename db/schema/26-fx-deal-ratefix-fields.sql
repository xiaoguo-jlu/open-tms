-- NDF Rate Fix Phase 1: 新增 fix_date / fix_currency / fix_market_rate / verifier_by / fix_remark 字段
-- 关联: M3-NDF-Rate-Fix 设计评审 Phase 1
-- 日期: 2026-07-05

ALTER TABLE tms_fx_deals_t ADD COLUMN IF NOT EXISTS fix_date DATE;
ALTER TABLE tms_fx_deals_t ADD COLUMN IF NOT EXISTS fix_currency VARCHAR(10);
ALTER TABLE tms_fx_deals_t ADD COLUMN IF NOT EXISTS fix_market_rate DECIMAL(18,8);
ALTER TABLE tms_fx_deals_t ADD COLUMN IF NOT EXISTS verifier_by VARCHAR(50);
ALTER TABLE tms_fx_deals_t ADD COLUMN IF NOT EXISTS fix_remark VARCHAR(500);

COMMENT ON COLUMN tms_fx_deals_t.fix_date IS 'NDF fixing 执行日期(默认=value_date)';
COMMENT ON COLUMN tms_fx_deals_t.fix_currency IS 'NDF fixing 结算币种(默认=buyCurrency)';
COMMENT ON COLUMN tms_fx_deals_t.fix_market_rate IS 'NDF fixing 时的市场参考汇率(可选)';
COMMENT ON COLUMN tms_fx_deals_t.verifier_by IS 'NDF fixing 复核人(可空)';
COMMENT ON COLUMN tms_fx_deals_t.fix_remark IS 'NDF RATE_FIX 备注';
