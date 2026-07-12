-- ============================================================
-- 30-deal-approval-rule.sql
-- 交易审批规则(Deal Approval Rule, DAR)
-- 特性: 基于 5 维要素(主体/对手方/工具/交易员/操作)的灵活审批规则
--       + LEVEL_0/1/2 审批层级 + JSONB 角色列表 + 专用审计 + 镜像
-- 范围: Phase 3 DB 设计
-- 依赖: GlobalConstants.ActionType / 基于数据 / dealing 服务
-- 参考: v1.1 default-bank-account-rule (28-default-bank-account-rule-v1.1.sql)
--       PRD: docs/prd/M3/M3-交易审批规则PRD.md
-- ============================================================


-- ============================================================
-- 1) 主表 — tms_deal_approval_rule_t (25 字段)
-- 模式严格参考 28-default-bank-account-rule-v1.1.sql
-- 写入时机: DealApprovalRuleService.save / update / delete / enable / disable
-- ============================================================
CREATE TABLE IF NOT EXISTS tms_deal_approval_rule_t (
    id                      BIGSERIAL       PRIMARY KEY,
    rule_number             VARCHAR(50)     NOT NULL UNIQUE,         -- DARyyyyMMdd0001
    management_entity_id    BIGINT,                                 -- NULL=通配
    counterparty_id         BIGINT,                                 -- NULL=通配
    instrument_id           BIGINT,                                 -- NULL=通配
    dealer_id               BIGINT,                                 -- NULL=通配
    action_type             VARCHAR(20)     NOT NULL,                -- CREATE/SUBMIT/APPROVE/REJECT/EXECUTE
    approval_level          VARCHAR(20)     NOT NULL,                -- LEVEL_0/LEVEL_1/LEVEL_2
    level1_roles            JSONB           NOT NULL DEFAULT '[]'::jsonb,
    level2_roles            JSONB           NOT NULL DEFAULT '[]'::jsonb,
    priority                INT             NOT NULL DEFAULT 0,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'Active',
    start_date              DATE,
    end_date                DATE,
    description             VARCHAR(500),
    remark                  VARCHAR(500),
    lock_token              VARCHAR(64),
    locked_by               VARCHAR(50),
    locked_at               TIMESTAMP,
    created_by              VARCHAR(50)     NOT NULL DEFAULT 'system',
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(50),
    updated_at              TIMESTAMP,
    version                 INT             NOT NULL DEFAULT 0,
    deleted                 CHAR(1)         NOT NULL DEFAULT '0',
    -- 业务校验
    CONSTRAINT chk_dar_action_type    CHECK (action_type IN ('CREATE','SUBMIT','APPROVE','REJECT','EXECUTE')),
    CONSTRAINT chk_dar_approval_level CHECK (approval_level IN ('LEVEL_0','LEVEL_1','LEVEL_2')),
    CONSTRAINT chk_dar_status         CHECK (status IN ('Active','Inactive')),
    CONSTRAINT chk_dar_priority       CHECK (priority BETWEEN 0 AND 9999),
    CONSTRAINT chk_dar_date_range     CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_dar_level0_empty   CHECK (
        approval_level <> 'LEVEL_0'
        OR (level1_roles = '[]'::jsonb AND level2_roles = '[]'::jsonb)
    ),
    CONSTRAINT chk_dar_level1_l1_not_empty CHECK (
        approval_level <> 'LEVEL_1'
        OR jsonb_array_length(level1_roles) >= 1
    ),
    CONSTRAINT chk_dar_level2_both_not_empty CHECK (
        approval_level <> 'LEVEL_2'
        OR (jsonb_array_length(level1_roles) >= 1 AND jsonb_array_length(level2_roles) >= 1)
    )
);

-- 2) 唯一约束:Active 状态下同 5 维 + actionType + approvalLevel 不可重复
--    模式参考 v1.1 uniq_dbar_active_dims (NULLS NOT DISTINCT)
CREATE UNIQUE INDEX IF NOT EXISTS uniq_dar_active_dims
    ON tms_deal_approval_rule_t (
        management_entity_id,
        counterparty_id,
        instrument_id,
        dealer_id,
        action_type,
        approval_level,
        status
    )
    NULLS NOT DISTINCT
    WHERE deleted = '0' AND status = 'Active';

-- 3) 业务字段索引
CREATE INDEX IF NOT EXISTS idx_dar_mgmt_entity     ON tms_deal_approval_rule_t(management_entity_id);
CREATE INDEX IF NOT EXISTS idx_dar_counterparty    ON tms_deal_approval_rule_t(counterparty_id);
CREATE INDEX IF NOT EXISTS idx_dar_instrument      ON tms_deal_approval_rule_t(instrument_id);
CREATE INDEX IF NOT EXISTS idx_dar_dealer          ON tms_deal_approval_rule_t(dealer_id);
CREATE INDEX IF NOT EXISTS idx_dar_action_type     ON tms_deal_approval_rule_t(action_type);
CREATE INDEX IF NOT EXISTS idx_dar_approval_level  ON tms_deal_approval_rule_t(approval_level);
CREATE INDEX IF NOT EXISTS idx_dar_status          ON tms_deal_approval_rule_t(status);
CREATE INDEX IF NOT EXISTS idx_dar_priority        ON tms_deal_approval_rule_t(priority DESC);
CREATE INDEX IF NOT EXISTS idx_dar_lock_token      ON tms_deal_approval_rule_t(lock_token);
CREATE INDEX IF NOT EXISTS idx_dar_start_date      ON tms_deal_approval_rule_t(start_date);
CREATE INDEX IF NOT EXISTS idx_dar_end_date        ON tms_deal_approval_rule_t(end_date);

-- 4) 匹配核心索引 — match 端点用(5 维 + actionType + status)
--    模式参考 v1.1 idx_dbar_match_core
CREATE INDEX IF NOT EXISTS idx_dar_match_core
    ON tms_deal_approval_rule_t(action_type, status, priority DESC)
    WHERE deleted = '0' AND status = 'Active';

-- 5) JSONB GIN 索引 — 角色包含查询扩展(P2 备用,Phase 1 不强制使用)
CREATE INDEX IF NOT EXISTS idx_dar_level1_roles_gin
    ON tms_deal_approval_rule_t USING GIN (level1_roles);
CREATE INDEX IF NOT EXISTS idx_dar_level2_roles_gin
    ON tms_deal_approval_rule_t USING GIN (level2_roles);

-- 6) 注释
COMMENT ON TABLE  tms_deal_approval_rule_t IS '交易审批规则主表(基于 5 维要素 + actionType 匹配的灵活审批规则)';
COMMENT ON COLUMN tms_deal_approval_rule_t.id                   IS '主键';
COMMENT ON COLUMN tms_deal_approval_rule_t.rule_number          IS '规则编号(DARyyyyMMdd0001)';
COMMENT ON COLUMN tms_deal_approval_rule_t.management_entity_id IS '交易主体 ID(NULL=通配)';
COMMENT ON COLUMN tms_deal_approval_rule_t.counterparty_id      IS '交易对手 ID(NULL=通配)';
COMMENT ON COLUMN tms_deal_approval_rule_t.instrument_id        IS '金融工具 ID(NULL=通配)';
COMMENT ON COLUMN tms_deal_approval_rule_t.dealer_id            IS '交易员 ID(NULL=通配)';
COMMENT ON COLUMN tms_deal_approval_rule_t.action_type          IS '操作类型(CREATE/SUBMIT/APPROVE/REJECT/EXECUTE,沿用 GlobalConstants.ActionType)';
COMMENT ON COLUMN tms_deal_approval_rule_t.approval_level       IS '审批层级(LEVEL_0=无需/LEVEL_1=一层/LEVEL_2=二层)';
COMMENT ON COLUMN tms_deal_approval_rule_t.level1_roles         IS 'L1 角色列表(JSONB 数组,如 ["RISK_MANAGER"])';
COMMENT ON COLUMN tms_deal_approval_rule_t.level2_roles         IS 'L2 角色列表(JSONB 数组,仅 LEVEL_2 使用)';
COMMENT ON COLUMN tms_deal_approval_rule_t.priority             IS '优先级 0-9999(同 specificityScore 下数值越大越优先)';
COMMENT ON COLUMN tms_deal_approval_rule_t.status               IS '状态(Active=启用/Inactive=停用)';
COMMENT ON COLUMN tms_deal_approval_rule_t.start_date           IS '生效开始日(NULL=立即)';
COMMENT ON COLUMN tms_deal_approval_rule_t.end_date             IS '生效结束日(NULL=长期)';
COMMENT ON COLUMN tms_deal_approval_rule_t.description          IS '业务说明';
COMMENT ON COLUMN tms_deal_approval_rule_t.remark               IS '内部备注';
COMMENT ON COLUMN tms_deal_approval_rule_t.lock_token           IS '并发控制 token(更新时必传,UUID v4)';
COMMENT ON COLUMN tms_deal_approval_rule_t.locked_by            IS '锁定人';
COMMENT ON COLUMN tms_deal_approval_rule_t.locked_at            IS '锁定时间';
COMMENT ON COLUMN tms_deal_approval_rule_t.deleted              IS '软删标记(0=正常,1=已删)';


-- ============================================================
-- 7) 镜像表 — tms_deal_approval_rule_image_t
-- 模式参考 27-at-deal-image-table.sql + 29-cashflow-enhance.sql
-- 写入时机: DealApprovalRuleService.create / update / delete / enable / disable
-- 镜像保留: 永不过期(监管 ≥7 年,partition 归档延后)
-- ============================================================
CREATE TABLE IF NOT EXISTS tms_deal_approval_rule_image_t (
    id              BIGSERIAL       PRIMARY KEY,
    image_number    VARCHAR(50)     NOT NULL UNIQUE,
    rule_number     VARCHAR(50)     NOT NULL,
    rule_id         BIGINT          NOT NULL,
    version         INT             NOT NULL DEFAULT 1,
    -- 全字段快照(JSONB,变更时全部保存)
    snapshot_json   JSONB           NOT NULL,
    image_type      VARCHAR(20)     NOT NULL DEFAULT 'UPDATE',
    operator        VARCHAR(50)     NOT NULL,
    operate_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(500),
    created_by      VARCHAR(50)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         CHAR(1)         NOT NULL DEFAULT '0',
    CONSTRAINT chk_dari_image_type CHECK (
        image_type IN ('CREATE','UPDATE','DELETE','ENABLE','DISABLE')
    )
);

CREATE INDEX IF NOT EXISTS idx_dari_rule_number   ON tms_deal_approval_rule_image_t(rule_number);
CREATE INDEX IF NOT EXISTS idx_dari_rule_id       ON tms_deal_approval_rule_image_t(rule_id);
CREATE INDEX IF NOT EXISTS idx_dari_version       ON tms_deal_approval_rule_image_t(rule_number, version);
CREATE INDEX IF NOT EXISTS idx_dari_image_type    ON tms_deal_approval_rule_image_t(image_type);
CREATE INDEX IF NOT EXISTS idx_dari_operator      ON tms_deal_approval_rule_image_t(operator);
CREATE INDEX IF NOT EXISTS idx_dari_operate_at    ON tms_deal_approval_rule_image_t(operate_at DESC);
CREATE INDEX IF NOT EXISTS idx_dari_deleted       ON tms_deal_approval_rule_image_t(deleted);

COMMENT ON TABLE  tms_deal_approval_rule_image_t IS '交易审批规则镜像表(创建/修改/删除时记录全字段快照,Audit History 视图数据源)';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.image_number  IS '镜像编号(全局唯一)';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.rule_number   IS '原规则编号';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.rule_id       IS '原规则 ID';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.version       IS '镜像版本号(与 tms_deal_approval_rule_t.version 同步递增)';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.snapshot_json IS '规则全字段 JSONB 快照';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.image_type    IS '镜像类型: CREATE / UPDATE / DELETE / ENABLE / DISABLE';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.operator      IS '操作人';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.operate_at    IS '操作时间';
COMMENT ON COLUMN tms_deal_approval_rule_image_t.deleted       IS '软删标记(0=正常,1=已删)';


-- ============================================================
-- 8) 专用审计日志表 — tms_deal_approval_rule_audit_log_t
-- 不复用 v1.1 tms_rule_audit_log_t(rule_id 跨表冲突风险)
-- 模式参考 28-default-bank-account-rule-v1.1.sql 第 58-77 行
-- 写入时机: CREATE/UPDATE/DELETE/ENABLE/DISABLE 全量记录
-- ============================================================
CREATE TABLE IF NOT EXISTS tms_deal_approval_rule_audit_log_t (
    id              BIGSERIAL       PRIMARY KEY,
    rule_id         BIGINT          NOT NULL,
    operation       VARCHAR(20)     NOT NULL,
    old_value       JSONB,
    new_value       JSONB,
    operator        VARCHAR(50)     NOT NULL DEFAULT 'system',
    operated_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(500),
    CONSTRAINT chk_darl_operation CHECK (
        operation IN ('CREATE','UPDATE','DELETE','ENABLE','DISABLE')
    )
);

CREATE INDEX IF NOT EXISTS idx_darl_rule_id     ON tms_deal_approval_rule_audit_log_t(rule_id);
CREATE INDEX IF NOT EXISTS idx_darl_operator    ON tms_deal_approval_rule_audit_log_t(operator);
CREATE INDEX IF NOT EXISTS idx_darl_operated_at ON tms_deal_approval_rule_audit_log_t(operated_at DESC);
CREATE INDEX IF NOT EXISTS idx_darl_operation   ON tms_deal_approval_rule_audit_log_t(operation);

COMMENT ON TABLE  tms_deal_approval_rule_audit_log_t IS '交易审批规则专用审计日志(不复用 tms_rule_audit_log_t,避免 rule_id 跨表冲突)';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.rule_id     IS '规则 ID';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.operation   IS '操作类型(CREATE/UPDATE/DELETE/ENABLE/DISABLE)';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.old_value   IS '变更前快照(JSONB)';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.new_value   IS '变更后快照(JSONB)';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.operator    IS '操作人';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.operated_at IS '操作时间';
COMMENT ON COLUMN tms_deal_approval_rule_audit_log_t.remark      IS '备注';


-- ============================================================
-- 9) 对外引用(说明性,无物理外键 — dealing 与 basedata 跨服务)
--    management_entity_id → basedata.tms_management_entity_t.id
--    counterparty_id      → basedata.tms_counterparty_t.id
--    instrument_id        → basedata.tms_instrument_t.id
--    dealer_id            → user-permission.sys_user_t.id(待 user-permission 模块落地)
--    level1/2_roles       → 引用 role name 字符串(待 user-permission sys_role_t 落地,Phase 1 仅校验非空)
-- ============================================================

-- ============================================================
-- 10) 旧 tms_approval_rule_t 不迁移,保留作为 fallback
--     新表上线初期,match 未命中时降级到旧表(基于金额)
--     Phase 5+ 起新表为权威,旧表置只读
-- ============================================================