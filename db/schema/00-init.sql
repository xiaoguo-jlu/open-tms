-- Open-TMS Complete Database Schema
-- PostgreSQL
-- 执行顺序: 01 -> 17

-- 执行方式:
-- psql -U opentms -d opentms -f 01-basedata.sql
-- psql -U opentms -d opentms -f 02-dealing.sql
-- ... 以此类推

-- 或使用psql执行整个目录:
-- for f in db/schema/*.sql; do psql -U opentms -d opentms -f "$f"; done