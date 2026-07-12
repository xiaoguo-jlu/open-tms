-- =====================================================================
-- 30-fix-business-unit-residue.sql
-- =====================================================================
-- Purpose  : Cleanup commit 23 + 23b residuals. Rename DB business_unit
--            columns / indexes / constraints / tables to management_entity
-- Date     : 2026-07-12
-- Auditor  : scripts/scan_business_unit.py: Grade D -> A
-- Author   : opentms-feature-dev subagent
-- Notes    : Comments live AFTER each statement so db_tool.py naive
--            split-by-semicolon executor works. 31 P0 DB items fixed.
-- =====================================================================

-- Section A: column renames
ALTER TABLE IF EXISTS public.tms_cashflow_t RENAME COLUMN business_unit TO management_entity_id;        -- A.1 tms_cashflow_t.business_unit
ALTER TABLE IF EXISTS public.tms_cashflow_image_t RENAME COLUMN business_unit TO management_entity_id;  -- A.2 tms_cashflow_image_t.business_unit
ALTER TABLE IF EXISTS public.tms_subsidiary_t RENAME COLUMN business_unit_code TO management_entity_id; -- A.3 tms_subsidiary_t.business_unit_code
ALTER TABLE IF EXISTS public.trm_transaction_t RENAME COLUMN business_unit_id TO management_entity_id;   -- A.4 trm_transaction_t.business_unit_id

-- Section B: table rename  - orphan trm_business_unit_t becomes legacy archive
ALTER TABLE IF EXISTS public.trm_business_unit_t RENAME TO tms_management_entity_legacy_t;               -- B.1 trm_business_unit_t -> tms_management_entity_legacy_t

-- Section C: sequence rename
ALTER SEQUENCE IF EXISTS public.trm_business_unit_t_id_seq RENAME TO tms_management_entity_legacy_t_id_seq; -- C.1 sequence

-- Section D: index renames 8 items
ALTER INDEX IF EXISTS public.idx_ba_business_unit       RENAME TO idx_ba_management_entity;        -- D.1
ALTER INDEX IF EXISTS public.idx_cp_business_unit       RENAME TO idx_cp_management_entity;        -- D.2
ALTER INDEX IF EXISTS public.idx_exp_business_unit      RENAME TO idx_exp_management_entity;       -- D.3
ALTER INDEX IF EXISTS public.idx_fp_business_unit       RENAME TO idx_fp_management_entity;        -- D.4
ALTER INDEX IF EXISTS public.idx_limit_business_unit    RENAME TO idx_limit_management_entity;     -- D.5
ALTER INDEX IF EXISTS public.idx_rpt_business_unit      RENAME TO idx_rpt_management_entity;       -- D.6
ALTER INDEX IF EXISTS public.trm_business_unit_t_pkey            RENAME TO tms_management_entity_legacy_t_pkey;            -- D.7 PK
ALTER INDEX IF EXISTS public.trm_business_unit_t_unit_code_key   RENAME TO tms_management_entity_legacy_t_unit_code_key;   -- D.8 UNIQUE

-- Section E: constraint renames 17 items
ALTER TABLE IF EXISTS public.tms_cashflow_t RENAME CONSTRAINT tms_cashflow_t_business_unit_not_null TO tms_cashflow_t_management_entity_id_not_null;                       -- E.1
ALTER TABLE IF EXISTS public.tms_deals_t RENAME CONSTRAINT tms_deals_t_business_unit_not_null TO tms_deals_t_management_entity_id_not_null;                               -- E.2
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_id_not_null TO tms_management_entity_t_id_not_null;                            -- E.3
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_code_not_null TO tms_management_entity_t_code_not_null;                        -- E.4
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_name_not_null TO tms_management_entity_t_name_not_null;                        -- E.5
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_status_not_null TO tms_management_entity_t_status_not_null;                     -- E.6
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_created_by_not_null TO tms_management_entity_t_created_by_not_null;             -- E.7
ALTER TABLE IF EXISTS public.tms_management_entity_t RENAME CONSTRAINT tms_business_unit_t_created_at_not_null TO tms_management_entity_t_created_at_not_null;             -- E.8
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_id_not_null TO tms_management_entity_legacy_t_id_not_null;                  -- E.9
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_unit_code_not_null TO tms_management_entity_legacy_t_unit_code_not_null;  -- E.10
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_unit_name_not_null TO tms_management_entity_legacy_t_unit_name_not_null;  -- E.11
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_status_not_null TO tms_management_entity_legacy_t_status_not_null;        -- E.12
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_created_by_not_null TO tms_management_entity_legacy_t_created_by_not_null; -- E.13
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_created_at_not_null TO tms_management_entity_legacy_t_created_at_not_null; -- E.14
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_pkey TO tms_management_entity_legacy_t_pkey;                                -- E.15 PK
ALTER TABLE IF EXISTS public.tms_management_entity_legacy_t RENAME CONSTRAINT trm_business_unit_t_unit_code_key TO tms_management_entity_legacy_t_unit_code_key;            -- E.16 UNIQUE
ALTER TABLE IF EXISTS public.trm_transaction_t RENAME CONSTRAINT trm_transaction_t_business_unit_id_not_null TO trm_transaction_t_management_entity_id_not_null;           -- E.17

-- Verification: zero rows expected
SELECT 'columns_remaining' AS metric, COUNT(*) AS value FROM information_schema.columns WHERE table_schema = 'public' AND column_name IN ('business_unit', 'business_unit_id', 'business_unit_code')
UNION ALL SELECT 'indexes_remaining', COUNT(*) FROM pg_indexes WHERE schemaname = 'public' AND indexname LIKE '%business_unit%'
UNION ALL SELECT 'tables_remaining', COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '%business_unit%'
UNION ALL SELECT 'sequences_remaining', COUNT(*) FROM information_schema.sequences WHERE sequence_schema = 'public' AND sequence_name LIKE '%business_unit%'
UNION ALL SELECT 'constraints_remaining', COUNT(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid JOIN pg_namespace n ON t.relnamespace = n.oid WHERE n.nspname = 'public' AND c.conname LIKE '%business_unit%';
