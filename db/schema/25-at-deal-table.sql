-- tms_at_deals_t: AT (Account Transfer) 个性化字段
-- 与 tms_deals_t 共享主键 (1:1 关系)
-- AT 交易 = 同管理主体 + 同币种 的内部转账

CREATE TABLE IF NOT EXISTS tms_at_deals_t (
    id                    BIGSERIAL       PRIMARY KEY,
    deal_number           VARCHAR(50)     NOT NULL UNIQUE,
    transfer_type         VARCHAR(30)     NOT NULL DEFAULT 'SAME_COMPANY',  -- SAME_COMPANY / CROSS_COMPANY / INTERNAL
    source_account_id     BIGINT          NOT NULL,
    dest_account_id       BIGINT          NOT NULL,
    source_amount         DECIMAL(38,18)  NOT NULL,
    dest_amount           DECIMAL(38,18)  NOT NULL,
    source_currency       VARCHAR(10)     NOT NULL,
    dest_currency         VARCHAR(10)     NOT NULL,
    exchange_rate         DECIMAL(18,8)   NOT NULL DEFAULT 1.0,
    management_entity     VARCHAR(50),
    value_date            DATE,
    payment_method        VARCHAR(20),
    purpose               VARCHAR(500),
    status                VARCHAR(20)     NOT NULL DEFAULT 'New',
    latest_action_number  VARCHAR(50),
    created_by            VARCHAR(50)     NOT NULL,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            VARCHAR(50),
    updated_at            TIMESTAMP,
    version               INT             NOT NULL DEFAULT 0,
    deleted               CHAR(1)         NOT NULL DEFAULT '0',
    remark                VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_at_deals_deal_number ON tms_at_deals_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_at_deals_source_account ON tms_at_deals_t(source_account_id);
CREATE INDEX IF NOT EXISTS idx_at_deals_dest_account ON tms_at_deals_t(dest_account_id);
CREATE INDEX IF NOT EXISTS idx_at_deals_status ON tms_at_deals_t(status);
CREATE INDEX IF NOT EXISTS idx_at_deals_deleted ON tms_at_deals_t(deleted);

-- CHECK: 源金额/目标金额必须 > 0
ALTER TABLE tms_at_deals_t ADD CONSTRAINT chk_at_amount_positive
    CHECK (source_amount > 0 AND dest_amount > 0);

-- CHECK: 同币种（AT 不支持跨币种）
ALTER TABLE tms_at_deals_t ADD CONSTRAINT chk_at_same_currency
    CHECK (source_currency = dest_currency);

-- CHECK: 源/目标账户必须不同
ALTER TABLE tms_at_deals_t ADD CONSTRAINT chk_at_diff_account
    CHECK (source_account_id <> dest_account_id);