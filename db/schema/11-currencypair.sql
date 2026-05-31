-- 币种对表
-- PostgreSQL
-- 表名: tms_currency_pair_t

CREATE TABLE tms_currency_pair_t (
    id BIGSERIAL PRIMARY KEY,
    pair_code VARCHAR(20) NOT NULL UNIQUE COMMENT '货币对编码，如 EURUSD',
    base_currency VARCHAR(10) NOT NULL COMMENT '基础货币，如 EUR',
    quote_currency VARCHAR(10) NOT NULL COMMENT '报价货币，如 USD',
    bid_decimal INT NOT NULL DEFAULT 4 COMMENT '买方小数位',
    ask_decimal INT NOT NULL DEFAULT 4 COMMENT '卖方小数位',
    status CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态 1启用 0停用',
    remark VARCHAR(500) COMMENT '备注',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_currency_pair_t IS '币种对表';

CREATE INDEX idx_cp_pair_code ON tms_currency_pair_t(pair_code);
CREATE INDEX idx_cp_base_currency ON tms_currency_pair_t(base_currency);
CREATE INDEX idx_cp_quote_currency ON tms_currency_pair_t(quote_currency);
CREATE INDEX idx_cp_status ON tms_currency_pair_t(status);

-- 初始数据
INSERT INTO tms_currency_pair_t (pair_code, base_currency, quote_currency, bid_decimal, ask_decimal, status, created_by) VALUES
('EURUSD', 'EUR', 'USD', 5, 5, '1', 'system'),
('GBPUSD', 'GBP', 'USD', 5, 5, '1', 'system'),
('USDJPY', 'USD', 'JPY', 2, 2, '1', 'system'),
('USDCHF', 'USD', 'CHF', 5, 5, '1', 'system'),
('AUDUSD', 'AUD', 'USD', 5, 5, '1', 'system'),
('USDCNY', 'USD', 'CNY', 4, 4, '1', 'system'),
('EURGBP', 'EUR', 'GBP', 5, 5, '1', 'system'),
('GBPJPY', 'GBP', 'JPY', 2, 2, '1', 'system');