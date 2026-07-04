-- ============================================================
-- 23b-rename-business-unit-cleanup.sql
-- 补漏:
--   - PG sequence 名是 tms_business_unit_t (早于改名后)
--   - 外键约束 fk_deal_business_unit
-- ============================================================

-- 重命名 sequence
ALTER SEQUENCE tms_business_unit_t RENAME TO tms_management_entity_t_id_seq;

-- 重命名外键约束
ALTER TABLE tms_deal_t RENAME CONSTRAINT fk_deal_business_unit TO fk_deal_management_entity;
