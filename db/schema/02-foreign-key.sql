-- Open-TMS 外键约束脚本
-- PostgreSQL
-- 在所有表创建完成后执行
-- 执行顺序: 最后

-- ==================== basedata 外键 ====================
ALTER TABLE tms_counterparty_account_t ADD CONSTRAINT fk_cp_account_cp FOREIGN KEY (counterparty_id) REFERENCES tms_counterparty_t(id);
ALTER TABLE tms_counterparty_account_t ADD CONSTRAINT fk_cp_account_bank FOREIGN KEY (bank_id) REFERENCES tms_bank_t(id);

-- ==================== dealing 外键 ====================
ALTER TABLE tms_deal_t ADD CONSTRAINT fk_deal_instrument FOREIGN KEY (instrument_id) REFERENCES tms_instrument_t(id);
ALTER TABLE tms_deal_t ADD CONSTRAINT fk_deal_counterparty FOREIGN KEY (counterparty_id) REFERENCES tms_counterparty_t(id);
ALTER TABLE tms_deal_t ADD CONSTRAINT fk_deal_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_deal_t ADD CONSTRAINT fk_deal_trader FOREIGN KEY (trader_id) REFERENCES tms_trader_t(id);
ALTER TABLE tms_deal_t ADD CONSTRAINT fk_deal_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);

-- ==================== bankaccount 外键 ====================
ALTER TABLE tms_bank_account_t ADD CONSTRAINT fk_ba_bank FOREIGN KEY (bank_id) REFERENCES tms_bank_t(id);
ALTER TABLE tms_bank_account_t ADD CONSTRAINT fk_ba_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);
ALTER TABLE tms_bank_account_t ADD CONSTRAINT fk_ba_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);

-- ==================== instrument 外键 ====================
ALTER TABLE tms_instrument_t ADD CONSTRAINT fk_inst_counterparty FOREIGN KEY (counterparty_id) REFERENCES tms_counterparty_t(id);
ALTER TABLE tms_instrument_t ADD CONSTRAINT fk_inst_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);

-- ==================== fundplan 外键 ====================
ALTER TABLE tms_fund_plan_t ADD CONSTRAINT fk_fp_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_fund_plan_t ADD CONSTRAINT fk_fp_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);
ALTER TABLE tms_fund_plan_detail_t ADD CONSTRAINT fk_fpd_plan FOREIGN KEY (plan_id) REFERENCES tms_fund_plan_t(id);

-- ==================== cashpool 外键 ====================
ALTER TABLE tms_cash_pool_t ADD CONSTRAINT fk_cp_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_cash_pool_t ADD CONSTRAINT fk_cp_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);
ALTER TABLE tms_cash_pool_account_t ADD CONSTRAINT fk_cpa_pool FOREIGN KEY (pool_id) REFERENCES tms_cash_pool_t(id);
ALTER TABLE tms_cash_pool_account_t ADD CONSTRAINT fk_cpa_account FOREIGN KEY (account_id) REFERENCES tms_bank_account_t(id);

-- ==================== settlement 外键 ====================
ALTER TABLE tms_settlement_t ADD CONSTRAINT fk_st_deal FOREIGN KEY (deal_id) REFERENCES tms_deal_t(id);
ALTER TABLE tms_settlement_t ADD CONSTRAINT fk_st_from_account FOREIGN KEY (from_account_id) REFERENCES tms_bank_account_t(id);
ALTER TABLE tms_settlement_t ADD CONSTRAINT fk_st_to_account FOREIGN KEY (to_account_id) REFERENCES tms_bank_account_t(id);
ALTER TABLE tms_settlement_t ADD CONSTRAINT fk_st_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);

-- ==================== limit 外键 ====================
ALTER TABLE tms_limit_t ADD CONSTRAINT fk_limit_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_limit_t ADD CONSTRAINT fk_limit_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);
ALTER TABLE tms_limit_warning_t ADD CONSTRAINT fk_lw_limit FOREIGN KEY (limit_id) REFERENCES tms_limit_t(id);

-- ==================== fx 外键 ====================
ALTER TABLE tms_fx_deal_t ADD CONSTRAINT fk_fx_counterparty FOREIGN KEY (counterparty_id) REFERENCES tms_counterparty_t(id);
ALTER TABLE tms_fx_deal_t ADD CONSTRAINT fk_fx_buy_currency FOREIGN KEY (buy_currency) REFERENCES tms_currency_t(code);
ALTER TABLE tms_fx_deal_t ADD CONSTRAINT fk_fx_sell_currency FOREIGN KEY (sell_currency) REFERENCES tms_currency_t(code);

-- ==================== irs 外键 ====================
ALTER TABLE tms_irs_deal_t ADD CONSTRAINT fk_irs_counterparty FOREIGN KEY (counterparty_id) REFERENCES tms_counterparty_t(id);
ALTER TABLE tms_irs_deal_t ADD CONSTRAINT fk_irs_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);

-- ==================== valuation 外键 ====================
ALTER TABLE tms_valuation_t ADD CONSTRAINT fk_val_inst FOREIGN KEY (inst_id) REFERENCES tms_instrument_t(id);

-- ==================== exposure 外键 ====================
ALTER TABLE tms_exposure_t ADD CONSTRAINT fk_exp_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_exposure_t ADD CONSTRAINT fk_exp_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);

-- ==================== hedge 外键 ====================
ALTER TABLE tms_hedge_relation_t ADD CONSTRAINT fk_hr_exposure FOREIGN KEY (exposure_id) REFERENCES tms_exposure_t(id);
ALTER TABLE tms_hedge_relation_t ADD CONSTRAINT fk_hr_instrument FOREIGN KEY (instrument_id) REFERENCES tms_instrument_t(id);

-- ==================== impairment 外键 ====================
ALTER TABLE tms_impairment_t ADD CONSTRAINT fk_imp_inst FOREIGN KEY (inst_id) REFERENCES tms_instrument_t(id);

-- ==================== report 外键 ====================
ALTER TABLE tms_report_t ADD CONSTRAINT fk_rpt_business_unit FOREIGN KEY (business_unit_id) REFERENCES tms_business_unit_t(id);
ALTER TABLE tms_report_t ADD CONSTRAINT fk_rpt_currency FOREIGN KEY (currency) REFERENCES tms_currency_t(code);