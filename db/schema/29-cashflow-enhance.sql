-- ============================================================
-- 29-cashflow-enhance.sql
-- 增强交易现金流管理 + Audit History 镜像
-- 特性: 现金流增加主体/对手方银行账号字段 + 现金流镜像表
-- 范围: Phase 3 DB 设计
-- 依赖: tms_cashflow_t (19-dealmap-v2-step.sql)
-- ============================================================

-- 1) tms_cashflow_t 增加 2 字段(主体银行账号 + 对手方银行账号)
ALTER TABLE tms_cashflow_t
    ADD COLUMN IF NOT EXISTS bank_account_id           BIGINT,
    ADD COLUMN IF NOT EXISTS counterparty_bank_account_id BIGINT;

-- 2) 索引(主体方 / 对手方 bank account 查询加速)
CREATE INDEX IF NOT EXISTS idx_cf_bank_account_id          ON tms_cashflow_t(bank_account_id);
CREATE INDEX IF NOT EXISTS idx_cf_counterparty_bank_account_id ON tms_cashflow_t(counterparty_bank_account_id);

-- 3) 注释
COMMENT ON COLUMN tms_cashflow_t.bank_account_id             IS '主体银行账号 ID(由默认银行账户规则自动填充或人工选)';
COMMENT ON COLUMN tms_cashflow_t.counterparty_bank_account_id IS '对手方银行账号 ID(由规则匹配或人工选)';


-- ============================================================
-- 4) 新建 tms_cashflow_image_t 镜像表
-- 模式严格参考 27-at-deal-image-table.sql + 19-dealmap-v2-step.sql
-- 写入时机: CashflowService.create / update / delete / changeStatus
-- 镜像保留: 永不过期(监管 ≥7 年,partition 归档延后)
-- ============================================================
CREATE TABLE IF NOT EXISTS tms_cashflow_image_t (
    id                              BIGSERIAL       PRIMARY KEY,
    image_number                    VARCHAR(50)     NOT NULL UNIQUE,
    cflow_number                    VARCHAR(50)     NOT NULL,
    deal_number                     VARCHAR(50)     NOT NULL,
    version                         INT             NOT NULL DEFAULT 1,
    -- 业务字段(与 tms_cashflow_t 同步快照,变更时全部保存)
    dealmap_number                  VARCHAR(50),
    business_unit                   VARCHAR(50),
    bank_account                    VARCHAR(50),
    counterparty_account            VARCHAR(50),
    bank_account_id                 BIGINT,
    counterparty_bank_account_id    BIGINT,
    direction                       VARCHAR(10),
    amount                          DECIMAL(38,18),
    currency                        VARCHAR(10),
    cflow_date                      DATE,
    value_date                      DATE,
    source_type                     VARCHAR(20),
    source_ref                      VARCHAR(50),
    status                          VARCHAR(20),
    counterparty_name               VARCHAR(200),
    purpose                         VARCHAR(500),
    remark                          VARCHAR(500),
    -- 镜像元信息
    image_type                      VARCHAR(20)     NOT NULL DEFAULT 'UPDATE',
    operator                        VARCHAR(50)     NOT NULL,
    operate_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 审计字段
    created_by                      VARCHAR(50)     NOT NULL,
    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         CHAR(1)         NOT NULL DEFAULT '0'
);

-- 5) 索引(版本查询 / 镜像类型 / 关联交易)
CREATE INDEX IF NOT EXISTS idx_cf_image_cflow_number  ON tms_cashflow_image_t(cflow_number);
CREATE INDEX IF NOT EXISTS idx_cf_image_deal_number   ON tms_cashflow_image_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_cf_image_version       ON tms_cashflow_image_t(cflow_number, version);
CREATE INDEX IF NOT EXISTS idx_cf_image_image_type    ON tms_cashflow_image_t(image_type);
CREATE INDEX IF NOT EXISTS idx_cf_image_deleted      ON tms_cashflow_image_t(deleted);
CREATE INDEX IF NOT EXISTS idx_cf_image_operate_at    ON tms_cashflow_image_t(operate_at DESC);

-- 6) 注释
COMMENT ON TABLE  tms_cashflow_image_t IS '现金流镜像表(创建/修改/删除时记录字段快照,Audit History 视图数据源)';
COMMENT ON COLUMN tms_cashflow_image_t.image_number  IS '镜像编号(全局唯一)';
COMMENT ON COLUMN tms_cashflow_image_t.cflow_number  IS '原现金流编号';
COMMENT ON COLUMN tms_cashflow_image_t.deal_number   IS '关联交易编号(冗余便于审计查询)';
COMMENT ON COLUMN tms_cashflow_image_t.version       IS '版本号(与 tms_deals_t.version 同步递增)';
COMMENT ON COLUMN tms_cashflow_image_t.image_type    IS '镜像类型: CREATE / UPDATE / DELETE / STATUS_CHANGE / RATE_FIX';
COMMENT ON COLUMN tms_cashflow_image_t.operator      IS '操作人';
COMMENT ON COLUMN tms_cashflow_image_t.operate_at    IS '操作时间(降序索引便于按时间查)';
COMMENT ON COLUMN tms_cashflow_image_t.deleted       IS '软删标记(0=正常,1=已删)';


-- ============================================================
-- 7) 业务规则约束(CHECK)
-- ============================================================
ALTER TABLE tms_cashflow_image_t
    ADD CONSTRAINT chk_cf_image_type CHECK (
        image_type IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'RATE_FIX')
    );

-- 8) 数据回填(可选,生产部署前评估)— v1.0 之前的 cashflow 无镜像,backfill 由 v1.1 Job 处理
--    本 SQL 暂不包含 backfill,避免历史数据污染

-- ============================================================
-- 9) 对外引用(说明性,无物理外键 — dealing 与 basedata 跨服务)
--    bank_account_id          → basedata.tms_bank_account_t.id        (基于数据 8081)
--    counterparty_bank_account_id → basedata.tms_counterparty_account_t.id (基于数据 8081)
--    cflow_number             → dealing.tms_cashflow_t.cflow_number
--    deal_number              → dealing.tms_deals_t.deal_number
-- ============================================================
