-- Open-TMS Bank Account 字段扩展
-- 用途:将原 bankaccount 模块功能合并到 basedata
-- 扩展 10 个字段:账户别名、性质、归集、主账户、限额、余额
-- 兼容策略:全部使用 IF NOT EXISTS,支持幂等执行

ALTER TABLE tms_bank_account_t
  ADD COLUMN IF NOT EXISTS account            VARCHAR(100),
  ADD COLUMN IF NOT EXISTS account_nature     VARCHAR(20)   DEFAULT 'Internal',
  ADD COLUMN IF NOT EXISTS is_collected       CHAR(1)       DEFAULT '0',
  ADD COLUMN IF NOT EXISTS collect_direction  VARCHAR(20),
  ADD COLUMN IF NOT EXISTS main_account_id    BIGINT,
  ADD COLUMN IF NOT EXISTS day_limit          DECIMAL(18,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS night_limit        DECIMAL(18,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS balance            DECIMAL(38,18) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS available_balance  DECIMAL(38,18) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS frozen_balance     DECIMAL(38,18) DEFAULT 0;

COMMENT ON COLUMN tms_bank_account_t.account IS '账户别名';
COMMENT ON COLUMN tms_bank_account_t.account_nature IS 'Internal/External';
COMMENT ON COLUMN tms_bank_account_t.is_collected IS '0=否 1=是 是否归集';
COMMENT ON COLUMN tms_bank_account_t.collect_direction IS 'Up/Down 上拨/下拨';
COMMENT ON COLUMN tms_bank_account_t.main_account_id IS '主账户ID(归集时)';
COMMENT ON COLUMN tms_bank_account_t.day_limit IS '日累计限额';
COMMENT ON COLUMN tms_bank_account_t.night_limit IS '夜间限额';
COMMENT ON COLUMN tms_bank_account_t.balance IS '当前余额';
COMMENT ON COLUMN tms_bank_account_t.available_balance IS '可用余额';
COMMENT ON COLUMN tms_bank_account_t.frozen_balance IS '冻结余额';
