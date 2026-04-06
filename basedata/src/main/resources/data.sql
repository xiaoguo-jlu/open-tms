-- Sample data for testing

-- Currency data
INSERT INTO trm_currency_t (currency_code, currency_name, currency_symbol, decimal_places, status, created_by) VALUES
('CNY', '人民币', '¥', 2, '1', 'system'),
('USD', '美元', '$', 2, '1', 'system'),
('EUR', '欧元', '€', 2, '1', 'system'),
('GBP', '英镑', '£', 2, '1', 'system'),
('JPY', '日元', '¥', 0, '1', 'system'),
('HKD', '港币', 'HK$', 2, '1', 'system'),
('SGD', '新加坡元', 'S$', 2, '1', 'system'),
('AUD', '澳元', 'A$', 2, '1', 'system'),
('CHF', '瑞士法郎', 'Fr', 2, '1', 'system'),
('KRW', '韩元', '₩', 0, '1', 'system');

-- Country data
INSERT INTO trm_country_t (country_code, country_name, country_name_en, timezone, status, created_by) VALUES
('CN', '中国', 'China', 'Asia/Shanghai', '1', 'system'),
('US', '美国', 'United States', 'America/New_York', '1', 'system'),
('GB', '英国', 'United Kingdom', 'Europe/London', '1', 'system'),
('JP', '日本', 'Japan', 'Asia/Tokyo', '1', 'system'),
('HK', '香港', 'Hong Kong', 'Asia/Hong_Kong', '1', 'system'),
('SG', '新加坡', 'Singapore', 'Asia/Singapore', '1', 'system'),
('AU', '澳大利亚', 'Australia', 'Australia/Sydney', '1', 'system'),
('DE', '德国', 'Germany', 'Europe/Berlin', '1', 'system'),
('FR', '法国', 'France', 'Europe/Paris', '1', 'system'),
('CH', '瑞士', 'Switzerland', 'Europe/Zurich', '1', 'system');

-- Business unit data
INSERT INTO trm_business_unit_t (unit_code, unit_name, unit_name_en, status, created_by) VALUES
('HQ', '总部', 'Headquarters', '1', 'system'),
('SH', '上海分公司', 'Shanghai Branch', '1', 'system'),
('BJ', '北京分公司', 'Beijing Branch', '1', 'system');

-- Trader data
INSERT INTO trm_trader_t (trader_code, trader_name, trader_name_en, department, status, created_by) VALUES
('TR001', '张三', 'Zhang San', '资金部', '1', 'system'),
('TR002', '李四', 'Li Si', '资金部', '1', 'system'),
('TR003', '王五', 'Wang Wu', '外汇部', '1', 'system');

-- Bank data
INSERT INTO trm_bank_t (bank_code, bank_name, bank_name_en, swift_code, country_code, status, created_by) VALUES
('ICBC', '中国工商银行', 'Industrial and Commercial Bank of China', 'ICBKCNBJ', 'CN', '1', 'system'),
('ABC', '中国农业银行', 'Agricultural Bank of China', 'ABOCCNBJ', 'CN', '1', 'system'),
('BOC', '中国银行', 'Bank of China', 'BKCHCNBJ', 'CN', '1', 'system'),
('CITI', '花旗银行', 'Citibank', 'CITIUS33', 'US', '1', 'system'),
('HSBC', '汇丰银行', 'HSBC', 'HSBCGB2L', 'GB', '1', 'system');

-- Counterparty data
INSERT INTO trm_counterparty_t (cp_code, cp_name, cp_name_en, cp_type, country_code, status, created_by) VALUES
('CP001', '中国石化', 'Sinopec', 'CORP', 'CN', '1', 'system'),
('CP002', '中国石油', 'CNPC', 'CORP', 'CN', '1', 'system'),
('CP003', '微软中国', 'Microsoft China', 'CORP', 'CN', '1', 'system'),
('CP004', '苹果公司', 'Apple Inc', 'CORP', 'US', '1', 'system');
