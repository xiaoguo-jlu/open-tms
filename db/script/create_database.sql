-- Open-TMS 数据库初始化脚本
-- 步骤1: 创建用户

-- 1. 创建用户 (如果不存在则创建)
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'opentms') THEN
      CREATE USER opentms WITH PASSWORD 'opentms123';
   END IF;
END
$do$;

-- 2. 创建数据库 (需要单独执行，不能在DO块中)
-- 请在 pgAdmin 中单独执行以下语句:
-- CREATE DATABASE opentms OWNER opentms;

-- 3. 授权
GRANT ALL PRIVILEGES ON DATABASE opentms TO opentms;
