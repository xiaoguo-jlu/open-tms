-- ============================================================
-- 23-rename-business-unit.sql (verified, idempotent notes)
-- 重命名 业务单元/业务主体 → 管理主体 (Management Entity)
-- 状态: 已执行 (2026-07-04), 5 行测试数据保留
-- 涉及:
--   1) 表: tms_business_unit_t → tms_management_entity_t  ✓
--   2) 索引: tms_business_unit_t_pkey, _code_key           ✓
--   3) 序列: tms_business_unit_t (旧名,简写) → tms_management_entity_t_id_seq ✓
--   4) 8 个 FK 列: business_unit_id → management_entity_id
--      tms_bank_account_t, tms_cash_pool_t, tms_deal_t, tms_exposure_t,
--      tms_fund_plan_t, tms_limit_t, tms_report_t, tms_cashflow_t
--      (tms_transaction_t 没有该列, 实际表名为 trm_transaction_t 且已软删除)
--   5) 3 个 VARCHAR 列: business_unit → management_entity
--      tms_cashflow_t, tms_deals_t, tms_deals_image_t
--   6) FK 约束: fk_deal_business_unit → fk_deal_management_entity  ✓
-- ============================================================

-- (可重复执行,但已执行过,无需再次运行;参考初始 inventory 见 23-prefix commits)

ALTER TABLE IF EXISTS tms_business_unit_t RENAME TO tms_management_entity_t;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'tms_management_entity_t_pkey') THEN
    -- ok
  ELSIF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'tms_business_unit_t_pkey') THEN
    ALTER INDEX tms_business_unit_t_pkey RENAME TO tms_management_entity_t_pkey;
  END IF;
  IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'tms_business_unit_t_code_key') THEN
    ALTER INDEX tms_business_unit_t_code_key RENAME TO tms_management_entity_t_code_key;
  END IF;
END $$;

-- 8 张表的 FK 列 (重复运行安全)
ALTER TABLE IF EXISTS tms_cash_pool_t    RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_limit_t        RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_exposure_t     RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_report_t       RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_deal_t         RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_bank_account_t RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_fund_plan_t    RENAME COLUMN business_unit_id TO management_entity_id;
ALTER TABLE IF EXISTS tms_cashflow_t     RENAME COLUMN business_unit TO management_entity;

ALTER TABLE IF EXISTS tms_deals_t        RENAME COLUMN business_unit TO management_entity;
ALTER TABLE IF EXISTS tms_deals_image_t  RENAME COLUMN business_unit TO management_entity;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_deal_business_unit') THEN
    ALTER TABLE tms_deal_t RENAME CONSTRAINT fk_deal_business_unit TO fk_deal_management_entity;
  END IF;
END $$;
