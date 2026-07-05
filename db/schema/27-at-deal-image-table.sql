-- tms_at_deals_image_t: AT 交易镜像表 (用于 UPDATE/DELETE 时记录字段快照)
-- AT 交易本身不需要图片(image_type=AT),但因业务需要保留历史快照

CREATE TABLE IF NOT EXISTS tms_at_deals_image_t (
    id                      BIGSERIAL       PRIMARY KEY,
    image_number            VARCHAR(50)     NOT NULL UNIQUE,
    deal_number             VARCHAR(50)     NOT NULL,
    version                 INT             NOT NULL DEFAULT 1,
    transfer_type           VARCHAR(30),
    source_account_id       BIGINT,
    dest_account_id         BIGINT,
    source_amount           DECIMAL(38,18),
    dest_amount             DECIMAL(38,18),
    source_currency         VARCHAR(10),
    dest_currency           VARCHAR(10),
    exchange_rate           DECIMAL(18,8),
    management_entity       VARCHAR(50),
    value_date              DATE,
    payment_method          VARCHAR(20),
    purpose                 VARCHAR(500),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'New',
    latest_action_number    VARCHAR(50),
    image_type              VARCHAR(20)     NOT NULL DEFAULT 'UPDATE',
    operator                VARCHAR(50)     NOT NULL,
    operate_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(50)     NOT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                 CHAR(1)         NOT NULL DEFAULT '0'
);

CREATE INDEX IF NOT EXISTS idx_at_deals_image_deal_number ON tms_at_deals_image_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_at_deals_image_deleted ON tms_at_deals_image_t(deleted);
CREATE INDEX IF NOT EXISTS idx_at_deals_image_image_type ON tms_at_deals_image_t(image_type);

COMMENT ON TABLE tms_at_deals_image_t IS 'AT 交易镜像表 (UPDATE/DELETE 时记录字段快照)';
COMMENT ON COLUMN tms_at_deals_image_t.image_number IS '镜像编号';
COMMENT ON COLUMN tms_at_deals_image_t.deal_number IS '交易编号';
COMMENT ON COLUMN tms_at_deals_image_t.version IS '版本号';
COMMENT ON COLUMN tms_at_deals_image_t.image_type IS '镜像类型 (UPDATE/DELETE)';
COMMENT ON COLUMN tms_at_deals_image_t.operator IS '操作人';
