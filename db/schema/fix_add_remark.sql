-- Open-TMS 修复表结构-添加remark字段
-- PostgreSQL
-- 执行: psql -U opentms -d opentms -f fix_add_remark.sql


ALTER TABLE tms_bank_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_counterparty_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_counterparty_account_t ADD COLUMN remark VARCHAR(500);

-- 交易模块
ALTER TABLE tms_deal_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_deal_idempotency_t ADD COLUMN remark VARCHAR(500);

-- 银行账户模块
ALTER TABLE tms_bank_account_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_account_transaction_t ADD COLUMN remark VARCHAR(500);

-- 金融工具模块
ALTER TABLE tms_instrument_t ADD COLUMN remark VARCHAR(500);

-- 资金计划模块
ALTER TABLE tms_fund_plan_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_fund_plan_detail_t ADD COLUMN remark VARCHAR(500);

-- 现金池模块
ALTER TABLE tms_cash_pool_t ADD COLUMN remark VARCHAR(500);

-- 支付结算模块
ALTER TABLE tms_settlement_t ADD COLUMN remark VARCHAR(500);

-- 流动性限额模块
ALTER TABLE tms_limit_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_limit_warning_t ADD COLUMN remark VARCHAR(500);

-- 外汇交易模块
ALTER TABLE tms_fx_deal_t ADD COLUMN remark VARCHAR(500);

-- 利率掉期模块
ALTER TABLE tms_irs_deal_t ADD COLUMN remark VARCHAR(500);

-- 估值模块
ALTER TABLE tms_valuation_t ADD COLUMN remark VARCHAR(500);

-- 敞口管理模块
ALTER TABLE tms_exposure_t ADD COLUMN remark VARCHAR(500);

-- 套期保值模块
ALTER TABLE tms_hedge_relation_t ADD COLUMN remark VARCHAR(500);

-- 减值计算模块
ALTER TABLE tms_impairment_t ADD COLUMN remark VARCHAR(500);

-- VaR模块
ALTER TABLE tms_var_report_t ADD COLUMN remark VARCHAR(500);

-- 驾驶舱模块
ALTER TABLE tms_cockpit_dashboard_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_cockpit_widget_t ADD COLUMN remark VARCHAR(500);

-- 报表模块
ALTER TABLE tms_report_t ADD COLUMN remark VARCHAR(500);

-- 工作流模块
ALTER TABLE tms_workflow_template_t ADD COLUMN remark VARCHAR(500);
ALTER TABLE tms_approval_record_t ADD COLUMN remark VARCHAR(500);

-- 审计日志
ALTER TABLE tms_audit_log_t ADD COLUMN remark VARCHAR(500);