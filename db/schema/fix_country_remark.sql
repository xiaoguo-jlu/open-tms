-- Fix: Add missing remark column to tms_country_t
-- Run this against your PostgreSQL database:
-- psql -U opentms -d opentms -f fix_country_remark.sql

ALTER TABLE tms_country_t ADD COLUMN IF NOT EXISTS remark VARCHAR(500);