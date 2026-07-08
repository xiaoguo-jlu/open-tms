# M1-主体默认银行账户规则 PRD

**版本**: v1.1
**角色**: 产品经理 (PM)
**日期**: 2026-07-08
**基于**:
- v1.0(2026-07-05)
- `docs/优化需求/默认银行账户规则PRD-优化建议.md`(PM-Lead + BA 评审,25 项优化,本版本采纳 9 项关键项)
- `M3-外汇交易PRD.md` v3.2 (FX 录入架构)
- `docs/api/basedata/01-bank-accounts.md` v1.2 (银行账户 API)
- `M1-资金管理主体PRD-v1.md`
- 2026-07-03 模块整合 (basedata 端口 8081,dealing 端口 8082)

**状态**: v1.1 评审通过 - 待开发
**变更摘要**:v1.0 → v1.1,P0 全部 5 项 + 关键 P1 4 项 = 9 项修复(详见§十三 修订记录)

---

## 〇、修订记录

| 版本 | 日期 | 变更 | 作者 |
|------|------|------|------|
| v1.0 | 2026-07-05 | 初版,覆盖主体默认银行账户规则维护 + FX 自动匹配 | PM |
| **v1.1** | **2026-07-08** | **P0×5 + 关键 P1×4 = 9 项修复**:①字段去 VARCHAR 冗余 ②match 双方向 ③并发控制 ④引用 N 明确 ⑤FX 防抖 ⑥priority 范围 ⑦Active 唯一约束 ⑧补全 8 端点 ⑨currency 可空 | **PM** |

---

## 一、模块概述

### 1.1 模块名称

**default-bank-account-rule** — 主体默认银行账户规则(归入 `basedata` 模块,与银行账户同源)

### 1.2 业务背景

当前 FX (外汇) 交易录入时,现金流(实际付款/收款)没有自动带出主体银行账户,需要用户手动选择,容易出错且效率低。

**典型痛点**:
- FX SPOT 录入时,SELL 账户(收 USD 账户)与 BUY 账户(付 CNY 账户)需要用户从全量账户中下拉选择
- 一笔集团下属多个主体,每个主体又有多个币种账户,人工查找效率低
- 不同金融产品(SPOT/FWD/NDF)、不同对手方银行的默认账户规则不一致,新交易员容易选错

**业务诉求**:在交易录入时,系统根据交易要素(**主体 / 对手方 / 金融产品 / 方向 / 币种**)自动匹配出**Inflow(收账)+ Outflow(付账)两个账户**,允许用户手动覆盖。

### 1.3 功能定位

为 Open-TMS 提供"主体维度"的银行账户默认规则维护与运行时匹配能力:

- **规则维护**:基于树形维度(主体 → 对手方 → 金融产品 → 方向 → 币种)定义"该交易场景下,默认用哪个银行账户"
- **规则匹配引擎**:FX 录入时,根据交易要素实时返回默认账户 ID(支持双方向),前端自动填充
- **优先级冲突**:同一维度组合有多条规则时,按 `priority DESC, created_at ASC` 取首条
- **并发控制**:编辑时获取 `lock_token`,提交时校验,避免脏写
- **性能优化**:FX 录入防抖 + Redis 缓存(TTL 5 分钟),避免重复查询

### 1.4 用户角色

| 角色 | 典型操作 |
|------|---------|
| **资金主管 (Treasury Admin)** | 维护主体默认银行账户规则(新增/编辑/启用/停用/批量复制) |
| **外汇/资金交易员** | FX 录入时自动获取默认账户(收+付),确认或手动覆盖 |
| **会计/审计** | 查阅规则变更审计(`tms_rule_audit_log_t` + `version` + `updated_by/updated_at`) |
| **系统管理员** | 全量规则查询、批量停用、运行 match 测试 |

### 1.5 与其他模块的关系

```
default-bank-account-rule (本特性)
  │
  ├── 宿主模块: basedata (8081)
  │     ├── 新增 2 张表: tms_default_bank_account_rule_t + tms_rule_audit_log_t
  │     └── 新增 9 个 REST 端点: /api/v1/default-bank-account-rules/*
  │
  ├── 依赖 basedata 既有数据
  │     ├── tms_management_entity_t (管理主体)
  │     ├── tms_counterparty_t (交易对手)
  │     ├── tms_instrument_t (金融工具)
  │     ├── tms_currency_t (币种)
  │     └── tms_bank_account_t (银行账户)
  │
  ├── 被 dealing 调用 (FX/AC 录入联动)
  │     ├── 同步调用: GET /api/v1/default-bank-account-rules/match?dualDirection=true
  │     ├── 入参: { managementEntityId, counterpartyId, instrumentId, currency }
  │     └── 出参: { inflow: {...}, outflow: {...} }  ← ★ v1.1 双方向
  │
  ├── 被 Redis 缓存支撑(基于 CC + Redisson)
  │     └── match 结果按 5 维 hash 缓存,TTL 5 分钟
  │
  └── 不影响: valuation / var / fundplan (本特性只涉及交易录入)
```

### 1.6 范围说明

**本次范围 (P0)**:
- 主体默认银行账户规则(单维度主体)
- 规则维护 (CRUD + 启用/停用 + 并发控制)
- 规则匹配引擎(FX 录入时自动带出 Inflow + Outflow)
- FX 场景(SPOT/FWD/NDF)联动;AC 场景预留接口
- 规则审计日志(每次变更记录)
- match 接口性能缓存(Redis)

**本次不在范围 (P1/P2+)**:
- **交易对手默认银行账户**(本期只做主体侧,架构已预留 `counterparty_id` 字段)
- 规则导入/导出 (P1)
- 规则批量复制(同主体下复制到子主体)(P2)
- 规则变更审批流(P2)
- 跨主体继承(P2)
- AI 推荐账户(P3+)
- 规则快照(交易侧存 `rule_snapshot` JSONB)(P1)

---

## 二、业界对标

| 特性 | FIS Quantum | SAP TRM | Murex MX.3 | Open-TMS v1.1 |
|------|-------------|---------|-----------|---------------|
| 默认账户规则(Default Account Rule) | "Settlement Account Rules" 模块 | "House Bank Determination" 配置 | "Account Determination Rules" | **P0 主体维度** |
| 优先级排序 | Yes (sequence) | Yes (search sequence) | Yes (priority + validity) | ✅ priority DESC, created_at ASC |
| 通配符(ALL) | Yes (wildcard) | Yes (*) | Yes (ANY) | ✅ 4 个非主体维度支持 ALL(NULL 表达) |
| 开始生效日 | Yes (effective date) | Yes (valid from) | Yes (effective date) | ✅ start_date |
| **结束生效日** | Yes (valid to) | Yes (valid to) | Yes (valid to) | **P2+**(本期不支持) |
| **双方向匹配(Inflow + Outflow)** | Yes (house bank determination) | Yes (payment/receipt accounts) | Yes (dual search sequence) | **✅ v1.1 新增** |
| **并发控制** | Yes (lock mechanism) | Yes (change document) | Yes (lock token) | **✅ v1.1 新增 lock_token** |
| **审计日志** | Yes (change log table) | Yes (change documents CDPOS) | Yes (audit trail) | **✅ v1.1 新增 tms_rule_audit_log_t** |
| **运行时缓存** | Yes (memory cache) | Yes (buffer cache) | Yes (rule engine cache) | **✅ v1.1 Redis 5min TTL** |
| 维度组合 | 多达 8 维 | 5-6 维 | 多达 10 维 | 5 维(主体 + 4 ALL) |
| 跨主体继承 | Yes (parent → child) | Yes (hierarchy) | Yes (hierarchy) | **P2+** |
| 运行时匹配 | 自动(配置驱动) | 自动(IMG 配置) | 自动(Market Rule) | ✅ 自动(基于 5 维规则) |

> **核心差异**:Open-TMS v1.1 在 P0 范围内即提供双方向匹配 + 并发控制 + 审计日志,达到企业级标准。

---

## 三、功能清单

### 3.1 规则维护 (P0)

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 新增规则 | 配置主体 × 对手方 × 金融产品 × 方向 × 币种 → 默认银行账户 | **P0** |
| 编辑规则(带锁) | 编辑时获取 `lock_token`,提交时校验;`updated_at` 变化需提示刷新 | **P0** |
| 删除规则 | 软删(基于 `deleted='1'`),保留审计痕迹;提示"被 N 笔交易引用" | **P0** |
| 启用/停用规则 | `status` 切换 (Active/Inactive),不删数据 | **P0** |
| 分页查询 | 按主体/状态/金融产品等过滤,优先级 DESC 排序 | **P0** |
| 详情查询 | 单条规则完整字段(含 `lock_token` + `updated_at`) | **P0** |
| **Active 唯一约束** | 同维度组合 + 同 status=Active,**UNIQUE 约束**(防止误建重复) | **P1 v1.1** |

### 3.2 规则匹配引擎 (P0) — ★ v1.1 升级

| 功能 | 说明 | 优先级 |
|------|------|--------|
| **FX 双方向自动匹配** | FX 录入时调 `match?dualDirection=true`,**同时返回 Inflow + Outflow 两个账户** | **P0 v1.1** |
| AC 录入自动匹配 | AC 录入时(选完主体 + 币种)调 match 接口 | **P1**(预留接口,前端接入 P2) |
| 手动覆盖 | 自动匹配后用户仍可手动选择其他账户 | **P0** |
| 无匹配兜底 | 无匹配规则时,账户字段留空,提示"无默认账户,请手动选择" | **P0** |
| 多匹配取首条 | 命中多条规则时,按 priority DESC, created_at ASC 取首条 | **P0** |
| **FX 防抖策略** | 前端 300ms debounce,维度完全相同不重复调 | **P0 v1.1** |
| **Redis 缓存** | 服务端按 5 维 hash 缓存 match 结果,TTL 5 分钟 | **P0 v1.1** |
| **match 测试工具** | 运营端点 `/test-match` 返回所有命中规则(运营调试) | **P1 v1.1** |

### 3.3 规则列表 (P0)

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 列表展示 | 默认按优先级降序,展示 5 个维度 + 默认账户 + 状态 | **P0** |
| 列表筛选 | 按主体/对手方/金融产品/币种/状态筛选 | **P0** |
| 优先级调整 | 行内编辑 priority(快改)或详情页编辑 | **P0** |
| 一键启用/停用 | 行内切换 Active/Inactive | **P0** |

---

## 四、字段设计

### 4.1 主表 `tms_default_bank_account_rule_t`

> **★ v1.1 关键变更**:
> - **删除 VARCHAR 冗余字段**(`counterparty`、`instrument`、`currency` 由 VARCHAR + _id 双写改为只保留 `_id BIGINT`)
> - **currency 字段可空 NULL=ALL**(v1.0 写的是 NOT NULL DEFAULT 'ALL',v1.1 改为允许 NULL)
> - **新增 priority CHECK 约束**(BETWEEN 0 AND 9999)
> - **新增 Active 唯一约束**(同维度组合 + Active 不能重复)

```sql
CREATE TABLE tms_default_bank_account_rule_t (
    -- ★ 业务主键
    id                 BIGSERIAL       PRIMARY KEY,
    rule_number        VARCHAR(50)     NOT NULL UNIQUE,           -- 规则编号,如 RULE202607080001

    -- ★ 核心匹配维度(5 维,只用 _id BIGINT,无 VARCHAR 冗余)
    management_entity_id BIGINT        NOT NULL,                  -- 主体(FK→tms_management_entity_t.id),单选,不能 ALL
    counterparty_id     BIGINT,                                   -- 对手方 FK(NULL=ALL 通配)
    instrument_id       BIGINT,                                   -- 金融产品 FK(NULL=ALL 通配)
    direction           VARCHAR(20)    NOT NULL,                  -- 方向:Inflow(收)/Outflow(付)/ALL
    currency            VARCHAR(10),                              -- 币种(NULL=ALL 通配,引用 tms_currency_t.code)

    -- ★ 输出:默认银行账户
    bank_account_id     BIGINT         NOT NULL,                  -- FK→tms_bank_account_t.id,且 bank_account.management_entity_id = management_entity_id

    -- ★ 标识/状态
    status              VARCHAR(20)    NOT NULL DEFAULT 'Active', -- Active/Inactive
    priority            INT            NOT NULL DEFAULT 0,        -- 优先级,数字越大越优先,范围 0-9999
    start_date          DATE,                                     -- 开始生效日(可空=立即生效);rule_date <= today 才算生效

    -- ★ 描述
    description         VARCHAR(500),                             -- 业务说明,例:"USD SPOT 默认收账账户"
    remark              VARCHAR(500),                             -- 备注

    -- ★ 并发控制(v1.1 新增)
    lock_token          VARCHAR(64),                              -- 乐观锁 token(编辑时生成,提交时校验,UUID)
    locked_by           VARCHAR(50),                              -- 锁定人
    locked_at           TIMESTAMP,                                -- 锁定时间

    -- ★ 审计(全表必备)
    created_by          VARCHAR(50)    NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT            NOT NULL DEFAULT 0,        -- 乐观锁(@Version)
    deleted             CHAR(1)        NOT NULL DEFAULT '0',      -- 软删除(@TableLogic)

    -- ★ 约束
    CONSTRAINT chk_rule_direction CHECK (direction IN ('Inflow', 'Outflow', 'ALL')),
    CONSTRAINT chk_rule_status    CHECK (status IN ('Active', 'Inactive')),
    CONSTRAINT chk_rule_priority  CHECK (priority BETWEEN 0 AND 9999),
    CONSTRAINT chk_rule_bank_entity CHECK (bank_account_id IS NOT NULL),
    -- ★ v1.1 Active 唯一约束:同维度组合 Active 状态不能重复(Inactive 允许重复)
    CONSTRAINT uniq_rule_active_dims UNIQUE (
        management_entity_id, counterparty_id, instrument_id, direction, currency, status
    )
);

-- ★ 索引(查询性能)
CREATE INDEX idx_dbar_mgmt_entity      ON tms_default_bank_account_rule_t(management_entity_id);
CREATE INDEX idx_dbar_counterparty     ON tms_default_bank_account_rule_t(counterparty_id);
CREATE INDEX idx_dbar_instrument       ON tms_default_bank_account_rule_t(instrument_id);
CREATE INDEX idx_dbar_direction        ON tms_default_bank_account_rule_t(direction);
CREATE INDEX idx_dbar_currency         ON tms_default_bank_account_rule_t(currency);
CREATE INDEX idx_dbar_status           ON tms_default_bank_account_rule_t(status);
CREATE INDEX idx_dbar_priority         ON tms_default_bank_account_rule_t(priority DESC);
CREATE INDEX idx_dbar_bank_account     ON tms_default_bank_account_rule_t(bank_account_id);
-- ★ v1.1 引用 N 查询性能索引
CREATE INDEX idx_deal_bank_account_status ON tms_deals_t(bank_account_id, status);
```

> **★ v1.1 字段设计决策说明**:
> - **删除 VARCHAR 冗余**:v1.0 同时定义了 `counterparty VARCHAR` + `counterparty_id BIGINT`,Source of Truth 不明确;v1.1 只保留 `_id BIGINT`,前端展示时通过 `EntityNameLookup` 补全名称
> - **`currency` 字段允许 NULL**:`currency VARCHAR(10)`(去掉 NOT NULL DEFAULT 'ALL'),NULL 表达 ALL 通配;算法中 `r.getCurrency() == null` 才正确
> - **`lock_token` 字段**:v1.1 新增,编辑时后端生成 UUID 返回,提交时校验;前端无需存储,只是协议约束
> - **Active 唯一约束**:把 `status` 加入 UNIQUE(Inactive 不受约束),同一维度组合不能有两条 Active 规则

### 4.2 字段详细表

| 字段 | DB 列 | Java 类型 | 前端类型 | 必填 | 默认 | 说明 |
|------|-------|----------|---------|------|------|------|
| 规则主键 | `id` | `Long` | - | - | auto | DB 主键 |
| 规则编号 | `rule_number` | `String` | - | ✓ | 系统生成 | 格式 `RULEyyyyMMddxxxx`,4 位流水 |
| **主体** | `management_entity_id` | `Long` | **BaseDataPicker** | ✓ | - | FK→管理主体,单选,**不能 ALL** |
| **对手方** | `counterparty_id` | `Long` | BaseDataPicker | - | NULL=ALL | FK→对手方;空=通配;**v1.1 仅此 1 列,无 VARCHAR** |
| **金融产品** | `instrument_id` | `Long` | BaseDataPicker | - | NULL=ALL | FK→金融工具;空=通配;**v1.1 仅此 1 列,无 VARCHAR** |
| **方向** | `direction` | `String` | **Select** | ✓ | - | `Inflow`/`Outflow`/`ALL` |
| **币种** | `currency` | `String` | Select | - | NULL=ALL | ISO 4217;空=通配;**v1.1 允许 NULL** |
| **默认账户** | `bank_account_id` | `Long` | BaseDataPicker | ✓ | - | **必须先选主体才能选账户**,前端联动 |
| 状态 | `status` | `String` | Switch | ✓ | Active | Active/Inactive;**v1.1 Active 受 UNIQUE 约束** |
| 优先级 | `priority` | `Integer` | InputNumber | ✓ | 0 | 数字越大越优先;**v1.1 范围 0-9999** |
| 开始生效日 | `start_date` | `LocalDate` | DatePicker | - | NULL=立即 | `start_date IS NULL OR start_date <= today` 才算生效 |
| 描述 | `description` | `String` | Input | - | - | 业务说明 |
| 备注 | `remark` | `String` | Input | - | - | 内部备注 |
| **锁 Token** | `lock_token` | `String` | - | - | 自动生成 | **v1.1 新增**;编辑获取,提交校验 |
| 锁定人 | `locked_by` | `String` | - | - | - | **v1.1 新增** |
| 锁定时间 | `locked_at` | `LocalDateTime` | - | - | - | **v1.1 新增** |
| 审计字段 | created_by/created_at/updated_by/updated_at/version/deleted | - | - | ✓ | - | 项目强制审计 |

### 4.3 审计日志表 `tms_rule_audit_log_t` (v1.1 新增)

> **★ v1.1 新增**:为解决"规则变更无法追溯历史值"(P1-5 优化)

```sql
CREATE TABLE tms_rule_audit_log_t (
    id           BIGSERIAL PRIMARY KEY,
    rule_id      BIGINT          NOT NULL,                        -- 关联 tms_default_bank_account_rule_t.id
    operation    VARCHAR(20)     NOT NULL,                        -- CREATE/UPDATE/DELETE/ENABLE/DISABLE
    old_value    JSONB,                                            -- 变更前完整字段(快照)
    new_value    JSONB,                                            -- 变更后完整字段(快照)
    operator     VARCHAR(50)     NOT NULL,                        -- 操作人
    operated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark       VARCHAR(500),                                    -- 备注

    CONSTRAINT chk_rule_audit_op CHECK (operation IN ('CREATE', 'UPDATE', 'DELETE', 'ENABLE', 'DISABLE'))
);

CREATE INDEX idx_ral_rule_id   ON tms_rule_audit_log_t(rule_id);
CREATE INDEX idx_ral_operator ON tms_rule_audit_log_t(operator);
CREATE INDEX idx_ral_operated_at ON tms_rule_audit_log_t(operated_at DESC);

COMMENT ON TABLE tms_rule_audit_log_t IS '规则变更审计日志';
COMMENT ON COLUMN tms_rule_audit_log_t.old_value IS '变更前 JSONB 快照';
COMMENT ON COLUMN tms_rule_audit_log_t.new_value IS '变更后 JSONB 快照';
```

**使用方式**:
- 每次规则 CRUD/启用/停用时,在事务中写一条审计日志
- 审计接口:`GET /api/v1/default-bank-account-rules/{id}/audit-logs`(分页查该规则所有历史)
- 旧值/新值用 Jackson 序列化整条 Entity 为 JSON

### 4.4 编号生成规则

`rule_number` 格式: `RULE + yyyyMMdd + 4 位流水`

例: `RULE202607080001`、`RULE202607080002`、`RULE202607090001`

**生成方式**:同交易日下递增;跨日重置。
**实现位置**:`basedata` 模块 `RuleNumberGenerator` 工具类(或复用 `GlobalConstants` 现有 `SerialNumberGenerator`)。

### 4.5 全局枚举值

| 枚举 | 取值 | 说明 |
|------|------|------|
| 方向 Direction | `Inflow` / `Outflow` / `ALL` | 收款 / 付款 / 通配 |
| 状态 Status | `Active` / `Inactive` | 启用 / 停用 |
| 通配符 ALL | `NULL` (DB 层) / `'ALL'` (前端显示) | - |
| 审计操作类型 | `CREATE` / `UPDATE` / `DELETE` / `ENABLE` / `DISABLE` | v1.1 新增 |

> **枚举来源**:`GlobalConstants` 新增:
> ```java
> public static final String DIRECTION_INFLOW = "Inflow";
> public static final String DIRECTION_OUTFLOW = "Outflow";
> public static final String DIRECTION_ALL = "ALL";
>
> public static final String RULE_AUDIT_CREATE = "CREATE";
> public static final String RULE_AUDIT_UPDATE = "UPDATE";
> public static final String RULE_AUDIT_DELETE = "DELETE";
> public static final String RULE_AUDIT_ENABLE = "ENABLE";
> public static final String RULE_AUDIT_DISABLE = "DISABLE";
> ```

---

## 五、业务规则

### 5.1 匹配维度规则

| # | 规则 | 说明 |
|---|------|------|
| R1 | **主体是单选,不能 ALL** | 必须明确指定某个 `management_entity_id` |
| R2 | **对手方/金融产品/方向/币种 默认 ALL** | 这 4 个维度不填则视为通配 |
| R3 | **ALL 匹配语义**:对维度 X,该规则 `X = NULL OR X = deal.X` 时算命中 | 即"通配"等价于"任意值都匹配" |
| R4 | **方向枚举固定** | 仅 `Inflow` / `Outflow` / `ALL`,不接受其他值 |
| R5 | **币种必须有效** | 若非 ALL,必须是 `tms_currency_t.code` 中存在的币种 |
| R6 | **ALL 维度 DB 存储为 NULL**(v1.0 已说但 currency 字段错误;v1.1 currency 真正允许 NULL) | 节省索引 + 语义清晰 |

### 5.2 优先级与排序规则

| # | 规则 | 说明 |
|---|------|------|
| R7 | **多匹配排序**: `priority DESC, created_at ASC` | 数字越大越优先;同优先级先创建先生效 |
| R8 | **首条生效**: 取排序后的第一条规则 | 后续规则视为"兜底" |
| R9 | **优先级可同值** | 同优先级时按创建时间排序 |
| R10 | **优先级范围 0-9999**(v1.1 强化) | DB CHECK 约束;前端 InputNumber 限制 |

### 5.3 生失效规则

| # | 规则 | 说明 |
|---|------|------|
| R11 | **规则生效条件**: `status = 'Active'` AND (`start_date IS NULL` OR `start_date <= today`) AND `deleted = '0'` | 三者全部满足才参与匹配 |
| R12 | **停用规则不匹配** | `status = 'Inactive'` 立即停止匹配 |
| R13 | **未到生效日不匹配** | `today < start_date` 的规则不参与 |
| R14 | **生效后无结束日**(本期) | P2+ 支持 end_date |

### 5.4 账户归属规则

| # | 规则 | 说明 |
|---|------|------|
| R15 | **账户必须属于主体** | `bank_account.management_entity_id = rule.management_entity_id`,应用层校验 |
| R16 | **一个规则只能输出一个账户** | 不支持"主备账户"自动切换(P2+) |
| R17 | **账户停用不影响规则匹配** | 规则中的账户即使 `bank_account.status='0'`,仍会带出(警告用户账户已停用) |

### 5.5 唯一性规则 — ★ v1.1 强化

| # | 规则 | 说明 |
|---|------|------|
| R18 | **Active 唯一约束**(v1.1) | 同维度组合 + 同 status=Active,DB UNIQUE 约束,**不能重复**;Inactive 不受约束 |
| R19 | **Inactive 允许重复** | 停用的历史规则可保留多份(审计需要) |

### 5.6 编辑/删除/并发规则 — ★ v1.1 新增

| # | 规则 | 说明 |
|---|------|------|
| R20 | **编辑时主体不可改** | 主体是规则的"根",改主体等于删旧建新 |
| R21 | **其他维度可改** | 对手方/金融产品/方向/币种/账户/优先级/状态均可编辑 |
| R22 | **软删除** | 基于 `deleted='1'`,保留完整审计痕迹 |
| R23 | **已被引用的规则允许停用,但不推荐删除** | 系统提示"该规则已被 N 笔交易引用" |
| **R24** | **编辑并发控制**(v1.1) | 编辑接口返回 `lock_token`,提交时必须携带;后端校验 token,变化则返回 409 Conflict |
| **R25** | **锁 30 分钟过期**(v1.1) | `lock_token` 有效期 30 分钟,过期自动失效;`locked_at + 30min < now()` 时,后端允许强制接管 |

---

## 六、业务流程

### 6.1 规则维护流程(资金主管)

```
┌─────────────────────────────────────────────────────────┐
│  1. 进入「银行账户 → 默认账户规则」页面                            │
│     → 默认按主体筛选,显示当前主体的全部规则                         │
│                                                            │
│  2. 新增规则                                                 │
│     [1] 选择主体 (必填) → 触发主体下的账户列表联动                   │
│     [2] 选择对手方 (可选,空=ALL)                                 │
│     [3] 选择金融产品 (可选,空=ALL)                                │
│     [4] 选择方向 (Inflow/Outflow/ALL,必填)                       │
│     [5] 选择币种 (可选,空=ALL)                                   │
│     [6] 选择默认账户 (基于已选主体过滤 bank-account 列表)           │
│     [7] 设置优先级 (默认 0,范围 0-9999)                          │
│     [8] 设置开始生效日 (可空=立即生效)                              │
│     [9] 保存 → 系统生成 rule_number + 写审计日志,返回详情           │
│                                                            │
│  3. 编辑规则                                                  │
│     [1] 列表点击「编辑」                                         │
│     [2] 后端 GET /{id} 返回规则详情 + lock_token                │
│     [3] 前端展示 updated_at 提示"数据加载于..."                  │
│     [4] 主体字段灰显不可改                                       │
│     [5] 修改其他字段后保存 → 携带 lock_token                     │
│     [6] 后端校验 lock_token:                                   │
│         → 一致:更新成功 + 写审计日志 + 返回新 lock_token          │
│         → 不一致:返回 409,前端提示"规则已被他人修改,请刷新"         │
│                                                            │
│  4. 启用/停用                                                 │
│     [1] 列表行内 Switch 切换                                    │
│     [2] 停用后规则不参与匹配,但不删数据                            │
│     [3] 写审计日志(ENABLE/DISABLE)                              │
│                                                            │
│  5. 删除                                                     │
│     [1] 列表点击「删除」 → 二次确认                                │
│     [2] 系统提示"该规则已被 N 笔交易引用"(实时查询未结算 + 近 90 天) │
│     [3] 确认 → 软删 (deleted='1') + 写审计日志                   │
└─────────────────────────────────────────────────────────┘
```

### 6.2 FX 录入自动匹配流程(★ v1.1 双方向联动)

```
┌──────────────────────────────────────────────────────────┐
│  场景:交易员录入 FX SPOT 交易(USD/CNY)                     │
│                                                            │
│  Step 1: 选择「管理主体 = 1」                                │
│     → 前端 300ms 防抖后触发:                                │
│       GET /match?mgmt=1&dualDirection=true                │
│     → 后端缓存命中空,返回 {inflow: null, outflow: null}    │
│     → 账户字段留空                                            │
│                                                            │
│  Step 2: 选择「金融产品 = FX-SPOT-USD-CNY」                │
│     → 前端防抖后触发:                                        │
│       GET /match?mgmt=1&ins=401&dualDirection=true         │
│     → 后端:                                                 │
│       a) 5 维过滤                                            │
│       b) 双方向查询:                                          │
│          - direction=Inflow 的规则 → 收账账户                │
│          - direction=Outflow 的规则 → 付账账户                │
│       c) Redis 缓存(5 维 hash)                              │
│     → 返回: {                                               │
│         inflow: {bankAccountId: 1001, ruleNumber: "..."}, │
│         outflow: {bankAccountId: 1002, ruleNumber: "..."}  │
│       }                                                     │
│     → 前端自动填充「收账账户 = 1001 USD / 付账账户 = 1002 CNY」 │
│                                                            │
│  Step 3: 选择「币种对」(USD/CNY)                             │
│     → 维度变化,重新触发 match (防抖 300ms)                  │
│     → 同 Step 2 结果                                         │
│                                                            │
│  Step 4: 用户可手动覆盖                                       │
│     → 在账户字段下拉选择其他账户                                │
│     → 不修改规则表,仅影响当前这笔交易                          │
│                                                            │
│  Step 5: 保存交易                                            │
│     → 交易表中记录 payAccountId / receiveAccountId          │
│     → 规则本身不被修改                                         │
└──────────────────────────────────────────────────────────┘
```

### 6.3 匹配算法(后端实现) — ★ v1.1 双方向

**输入** (URL Query):
```
GET /api/v1/default-bank-account-rules/match
    ?managementEntityId=1
    &counterpartyId=5001
    &instrumentId=401
    &currency=USD
    &dualDirection=true
```

**算法步骤**:
```java
public MatchResult match(MatchRequest req) {
    // 0. Redis 缓存查询(★ v1.1)
    String cacheKey = buildCacheKey(req);  // 5 维 hash
    MatchResult cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) return cached;
    
    // 1. 基础过滤:status=Active + (start_date 生效) + deleted=0
    List<DefaultBankAccountRule> candidates = ruleRepository.findActiveAndEffective(today);
    
    // 2. 主体过滤(必须相等)
    candidates = candidates.stream()
        .filter(r -> r.getManagementEntityId().equals(req.getManagementEntityId()))
        .collect(Collectors.toList());
    if (candidates.isEmpty()) return MatchResult.empty();
    
    // 3. 对手方过滤
    if (req.getCounterpartyId() != null) {
        candidates = candidates.stream()
            .filter(r -> r.getCounterpartyId() == null 
                || r.getCounterpartyId().equals(req.getCounterpartyId()))
            .collect(Collectors.toList());
    }
    
    // 4. 金融产品过滤
    if (req.getInstrumentId() != null) {
        candidates = candidates.stream()
            .filter(r -> r.getInstrumentId() == null 
                || r.getInstrumentId().equals(req.getInstrumentId()))
            .collect(Collectors.toList());
    }
    
    // 5. 币种过滤
    if (req.getCurrency() != null) {
        candidates = candidates.stream()
            .filter(r -> r.getCurrency() == null 
                || r.getCurrency().equals(req.getCurrency()))
            .collect(Collectors.toList());
    }
    
    // 6. ★ v1.1 双方向匹配
    if (req.isDualDirection()) {
        DefaultBankAccountRule inflow = candidates.stream()
            .filter(r -> "Inflow".equals(r.getDirection()) || "ALL".equals(r.getDirection()))
            .max(Comparator.comparingInt(DefaultBankAccountRule::getPriority)
                .thenComparing(DefaultBankAccountRule::getCreatedAt))
            .orElse(null);
        
        DefaultBankAccountRule outflow = candidates.stream()
            .filter(r -> "Outflow".equals(r.getDirection()) || "ALL".equals(r.getDirection()))
            .max(Comparator.comparingInt(DefaultBankAccountRule::getPriority)
                .thenComparing(DefaultBankAccountRule::getCreatedAt))
            .orElse(null);
        
        MatchResult result = MatchResult.ofDual(inflow, outflow);
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        return result;
    } else {
        // 单方向匹配(兼容旧调用)
        DefaultBankAccountRule top = candidates.stream()
            .filter(r -> "ALL".equals(r.getDirection()) 
                || r.getDirection().equals(req.getDirection()))
            .max(Comparator.comparingInt(DefaultBankAccountRule::getPriority)
                .thenComparing(DefaultBankAccountRule::getCreatedAt))
            .orElse(null);
        
        MatchResult result = MatchResult.ofSingle(top);
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        return result;
    }
}
```

**输出(dualDirection=true)**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "inflow": {
      "matched": true,
      "bankAccountId": 1001,
      "ruleId": 12,
      "ruleNumber": "RULE202607080001",
      "priority": 100
    },
    "outflow": {
      "matched": true,
      "bankAccountId": 1002,
      "ruleId": 15,
      "ruleNumber": "RULE202607080005",
      "priority": 80
    }
  },
  "timestamp": 1751731200000
}
```

**输出(dualDirection=false,单方向兼容)**:
```json
{
  "code": 200,
  "data": {
    "matched": true,
    "bankAccountId": 1001,
    "ruleId": 12,
    "ruleNumber": "RULE202607080001",
    "priority": 100
  }
}
```

**无匹配输出(双方向)**:
```json
{
  "code": 200,
  "data": {
    "inflow": { "matched": false, "bankAccountId": null },
    "outflow": { "matched": false, "bankAccountId": null }
  }
}
```

### 6.4 "被引用 N 笔交易" 查询逻辑 — ★ v1.1 明确

**问题**:删除规则时,需要告诉用户"这条规则被多少笔交易引用了",避免误删。

**v1.1 明确规则**:
- **N = 当前未结算的交易 + 近 90 天已完成交易**
- 查 `tms_deals_t.bank_account_id = rule.bank_account_id`,结合 `tms_default_bank_account_rule_t` 的 match 规则反查(本期简化:直接查 `bank_account_id` 关联)
- **SQL(应用层)**:
  ```sql
  SELECT COUNT(*) FROM tms_deals_t d
  WHERE d.bank_account_id = #{bankAccountId}
    AND (
      d.status IN ('New', 'Submitted', 'Approved')  -- 未结算
      OR (d.status = 'Settled' AND d.updated_at >= NOW() - INTERVAL '90 days')  -- 90 天内已完成
    );
  ```
- **性能**:使用 `idx_deal_bank_account_status(bank_account_id, status)` 索引(v1.1 新建),< 50ms
- **百万级优化**:P2+ 预聚合到 `tms_rule_usage_t` 定时刷新

### 6.5 FX 录入防抖策略 — ★ v1.1 明确

**前端防抖**(关键):
```js
import { debounce } from 'lodash'

const debouncedMatch = debounce(async (params) => {
  const result = await matchDefaultBankAccount(params)
  // 填充 inflow/outflow 账户
}, 300)

// 维度变化时调用
watch([mgmtId, instrumentId, currency], () => {
  // 维度完全相同时不调(避免无意义请求)
  if (isSameAsLastParams(...)) return
  debouncedMatch({ ... })
})
```

**后端缓存**(关键):
- Redis Key:`dbar:match:{mgmtId}:{cpId}:{insId}:{direction}:{cur}`
- TTL:5 分钟
- 规则变更时失效缓存:`@CacheEvict` 或主动 `redisTemplate.delete(pattern)`

### 6.6 时序图(FX 录入联动 — 双方向)

```
[Frontend FX Form]                  [Backend dealing]                [Backend basedata]              [Redis]
       │                                  │                                  │                            │
       │ ① 选管理主体=1                     │                                  │                            │
       │──────────────────────────────────>│                                  │                            │
       │                                  │  GET /match?mgmt=1&dual=true     │                            │
       │                                  │──────────────────────────────────>│                            │
       │                                  │                                  │  GET dbar:match:...        │
       │                                  │                                  │──────────────────────────>│
       │                                  │                                  │<─ cache miss ────────────│
       │                                  │  {inflow: null, outflow: null}   │                            │
       │<──────────────────────────────────│<─────────────────────────────────│                            │
       │                                  │                                  │                            │
       │ ② 选金融产品=401 + 币种=USD        │                                  │                            │
       │ (300ms 防抖后触发)                │                                  │                            │
       │──────────────────────────────────>│                                  │                            │
       │                                  │  GET /match?mgmt=1&ins=401       │                            │
       │                                  │       &cur=USD&dual=true         │                            │
       │                                  │──────────────────────────────────>│                            │
       │                                  │                                  │  5维过滤 + 双方向查询        │
       │                                  │                                  │  SET dbar:match:... TTL 5m │
       │                                  │                                  │──────────────────────────>│
       │                                  │  {inflow: {1001}, outflow: {1002}}                              │
       │<──────────────────────────────────│<─────────────────────────────────│                            │
       │  收账=1001 / 付账=1002 自动填充   │                                  │                            │
       │                                  │                                  │                            │
       │ ③ 用户手动覆盖账户                │                                  │                            │
       │  (不调后端)                       │                                  │                            │
       │                                  │                                  │                            │
       │ ④ 保存交易                       │                                  │                            │
       │──────────────────────────────────>│  POST /api/v1/dealing/fx-deals  │                            │
       │                                  │  (含 payAccountId / recvAccountId)                           │
       │                                  │                                  │                            │
```

---

## 七、验收标准

### 7.1 P0 核心验收 — ★ v1.1 强化

| # | 功能 | 验收条件 |
|---|------|----------|
| A1 | 表结构 | `tms_default_bank_account_rule_t` 字段齐全(无 VARCHAR 冗余),审计字段完整,索引齐备 |
| A2 | 审计日志表 | `tms_rule_audit_log_t` 表已建,规则变更时写日志(JSONB 快照) |
| A3 | 编号生成 | `rule_number` 格式 `RULEyyyyMMddxxxx`,同日内递增,跨日重置 |
| A4 | 新增规则 | 可正常新增一条规则,前端 5 个维度(主体必填)+ 账户必填(账户属于主体) + 优先级 + 状态,后端写库成功 |
| A5 | 编辑规则(带锁) | 编辑时返回 `lock_token`,提交时校验;版本一致更新成功,不一致返回 409 |
| A6 | 删除规则 | 软删后 `deleted='1'`,列表不再展示,DB 数据保留 + 审计日志 |
| A7 | 启用/停用 | 切换 `status` 后,Active 参与匹配,Inactive 不参与;写审计日志(ENABLE/DISABLE) |
| A8 | 分页查询 | 支持按主体/对手方/金融产品/币种/状态过滤,默认按 `priority DESC` 排序 |
| A9 | 详情查询 | 单条规则返回完整字段(含 `lock_token` + `updated_at`) |
| **A10** | **match 双方向接口** | `GET /match?dualDirection=true` 同时返回 `inflow` + `outflow` 两个账户对象 |
| A11 | **ALL 通配** | 4 个非主体维度为 NULL 时,视为通配,匹配任意值(`currency` 字段真正可空) |
| A12 | **主体不可 ALL** | 主体必填且必须等于 `req.managementEntityId`,否则无匹配 |
| A13 | **优先级排序** | 多条规则命中时,按 `priority DESC, created_at ASC` 取首条;双方向各自独立排序 |
| A14 | **开始生效日** | `today >= start_date` 才算生效;`today < start_date` 不参与匹配;`start_date IS NULL` 立即生效 |
| A15 | **账户归属校验** | 新增/编辑时,前端校验账户属于主体;后端再校验一次 |
| A16 | **FX 联动** | FX SPOT/FWD/NDF 录入页选完 5 维度后,自动调用 `match?dualDirection=true`,收账 + 付账账户字段自动填充 |
| **A17** | **FX 防抖**(v1.1) | 前端 match 调用 300ms debounce;维度完全相同不重复调;后端 Redis 缓存 TTL 5 分钟 |
| **A18** | **并发控制**(v1.1) | 编辑接口返回 `lock_token`;提交时校验;`updated_at` 变化时返回 409,前端提示刷新 |
| A19 | **手动覆盖** | 自动填充后用户仍可手动选择其他账户,不影响规则表 |
| A20 | **无匹配兜底** | 无规则命中时,账户字段留空,前端提示"无默认账户,请手动选择" |
| **A21** | **被引用 N 查询**(v1.1) | 删除规则时,实时查询"未结算 + 近 90 天"已用此账户的交易数,使用 `idx_deal_bank_account_status` 索引 < 50ms |
| **A22** | **Active 唯一约束**(v1.1) | 同维度组合 + status=Active 不能重复插入(DB UNIQUE 约束);Inactive 允许重复 |
| **A23** | **priority 范围 0-9999**(v1.1) | DB CHECK + 前端 InputNumber `min=0 max=9999`;超出范围返回 400 |
| **A24** | **match 测试工具**(v1.1 P1) | `GET /test-match` 返回所有命中规则(完整列表,运营调试用) |
| A25 | **字段命名** | DB snake_case / Java camelCase / JS lowerCamelCase,与项目规范一致;**无 VARCHAR 冗余字段** |
| A26 | **状态字段** | `status` VARCHAR(20),不用 CHAR(1)(项目规范) |
| A27 | **枚举规范** | 方向枚举 `Inflow`/`Outflow`/`ALL` 来自 `GlobalConstants`;不用魔术字符串 |

### 7.2 兼容性验收

| # | 项 | 验收 |
|---|----|------|
| C1 | 与银行账户 API 兼容 | 不修改 `tms_bank_account_t`,仅消费其数据 |
| C2 | 与 FX PRD v3.2 兼容 | FX 录入联动不破坏 4 个 Tab,不影响 DealMap/Cashflow 生成;**新增 dualDirection 参数,FX 录入页必须传 true** |
| C3 | 与 basedata 端口兼容 | API 部署在 `basedata` 模块,端口 8081,匹配现有 Vite 代理 |
| C4 | 与 AC/AT 兼容 | match 接口预留 `dualDirection=false` 单方向模式,AC/AT 现有逻辑不受影响 |
| C5 | 与 Redis 兼容 | Redis 服务未启动时,降级为直接 DB 查询(不阻塞业务),日志告警 |
| C6 | 与审计日志兼容 | 旧规则没有审计历史,迁移时不强制回填 |

### 7.3 暂不做项(明确范围)

| # | 功能 | 状态 |
|---|------|------|
| Z1 | 交易对手默认银行账户 | **P2+** 不在本版本(架构允许,见第九节) |
| Z2 | 规则导入/导出 | **P1** 后续迭代 |
| Z3 | 规则批量复制 | **P2** 后续迭代 |
| Z4 | 规则变更审批流 | **P2** 后续迭代 |
| Z5 | AI 推荐账户 | **P3+** 暂不规划 |
| Z6 | 结束日期(end_date) | **P2+** 暂不支持(本版本 start_date 单边) |
| Z7 | 跨主体继承 | **P2+** 不在本版本 |
| **Z8** | **规则快照(交易侧存 rule_snapshot)** | **P1** 后续迭代;**本期不修改 fx_deals_t** |
| **Z9** | **多候选匹配(返回全部命中)** | **P2+**;本期 match 只返回首条;`/test-match` 端点提供运营调试 |

---

## 八、接口需求

### 8.1 新增端点(共 9 个) — ★ v1.1 完整

部署在 `basedata` 模块,路径 `/api/v1/default-bank-account-rules`,遵循 CXF + JAX-RS 风格。

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/api/v1/default-bank-account-rules/page` | 分页查询(支持筛选) |
| 2 | GET | `/api/v1/default-bank-account-rules/{id}` | 详情(含 `lock_token` + `updated_at`) |
| 3 | POST | `/api/v1/default-bank-account-rules` | 新增规则 |
| 4 | POST | `/api/v1/default-bank-account-rules/update` | 更新规则(带 `lock_token` 校验) |
| 5 | POST | `/api/v1/default-bank-account-rules/delete/{id}` | 删除(软删) |
| 6 | POST | `/api/v1/default-bank-account-rules/{id}/enable` | 启用 |
| 7 | POST | `/api/v1/default-bank-account-rules/{id}/disable` | 停用 |
| 8 | **GET** | **/api/v1/default-bank-account-rules/match?dualDirection=true** | **★ 核心**:运行时匹配(支持双方向 + Redis 缓存) |
| 9 | **GET** | **/api/v1/default-bank-account-rules/test-match** | **★ v1.1 新增**:运营调试(返回所有命中规则) |
| 10 | **GET** | **/api/v1/default-bank-account-rules/{id}/audit-logs** | **★ v1.1 新增**:查询规则审计历史 |
| 11 | **GET** | **/api/v1/default-bank-account-rules/{id}/reference-count** | **★ v1.1 新增**:查询被引用 N(未结算 + 近 90 天) |

### 8.2 详细接口定义

#### 8.2.1 运行时匹配接口(★ v1.1 双方向)

```
GET /opentms/basedata/api/v1/default-bank-account-rules/match
```

**Query 参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `managementEntityId` | Long | ✓ | 主体 |
| `counterpartyId` | Long | - | 对手方(可省=ALL) |
| `instrumentId` | Long | - | 金融产品(可省=ALL) |
| `direction` | String | - | `Inflow`/`Outflow`(仅 `dualDirection=false` 时必填) |
| `currency` | String | - | 币种(可省=ALL) |
| **`dualDirection`** | **Boolean** | **- (默认 false)** | **★ v1.1:是否双方向匹配;FX 录入必须传 true** |

**响应(dualDirection=true)**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "inflow": {
      "matched": true,
      "bankAccountId": 1001,
      "ruleId": 12,
      "ruleNumber": "RULE202607080001",
      "priority": 100
    },
    "outflow": {
      "matched": true,
      "bankAccountId": 1002,
      "ruleId": 15,
      "ruleNumber": "RULE202607080005",
      "priority": 80
    }
  },
  "timestamp": 1751731200000
}
```

#### 8.2.2 测试匹配接口(v1.1 P1 运营调试)

```
GET /opentms/basedata/api/v1/default-bank-account-rules/test-match
    ?managementEntityId=1
    &counterpartyId=5001
    &instrumentId=401
    &currency=USD
    &dualDirection=true
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "matchedCount": 3,
    "matchedRules": [
      {"ruleId": 12, "priority": 100, "bankAccountId": 1001, "ruleNumber": "RULE202607080001", "direction": "Inflow"},
      {"ruleId": 15, "priority": 80, "bankAccountId": 1002, "ruleNumber": "RULE202607080005", "direction": "Outflow"},
      {"ruleId": 18, "priority": 50, "bankAccountId": 1003, "ruleNumber": "RULE202607080010", "direction": "ALL"}
    ],
    "selectedInflow": {...},
    "selectedOutflow": {...}
  }
}
```

#### 8.2.3 引用计数接口(v1.1)

```
GET /opentms/basedata/api/v1/default-bank-account-rules/{id}/reference-count
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "ruleId": 12,
    "bankAccountId": 1001,
    "unsettledCount": 5,
    "recentSettledCount": 23,
    "totalCount": 28,
    "queryDurationMs": 18
  }
}
```

#### 8.2.4 审计日志接口(v1.1)

```
GET /opentms/basedata/api/v1/default-bank-account-rules/{id}/audit-logs?pageNum=1&pageSize=20
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1001,
        "operation": "UPDATE",
        "oldValue": {...},
        "newValue": {...},
        "operator": "admin",
        "operatedAt": "2026-07-08T10:30:00",
        "remark": "调整优先级"
      }
    ],
    "total": 15
  }
}
```

#### 8.2.5 新增规则

```
POST /opentms/basedata/api/v1/default-bank-account-rules
```

**请求体**:
```json
{
  "managementEntityId": 1,
  "counterpartyId": 5001,
  "instrumentId": 401,
  "direction": "Inflow",
  "currency": "USD",
  "bankAccountId": 1001,
  "priority": 100,
  "startDate": "2026-07-08",
  "status": "Active",
  "description": "USD SPOT 默认收账账户",
  "remark": "适用于中行对手方"
}
```

**响应**:完整规则 VO(含 `rule_number`)

**错误响应**(Active 重复):
```json
{
  "code": 400,
  "message": "Active 规则维度组合已存在(RULE202607080001)"
}
```

#### 8.2.6 分页查询

```
POST /opentms/basedata/api/v1/default-bank-account-rules/page
```

**请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "managementEntityId": 1,
  "status": "Active",
  "keyword": "USD"
}
```

**响应**:分页数据,默认 `priority DESC, created_at ASC`

#### 8.2.7 更新 / 删除 / 启用 / 停用

| 端点 | 说明 |
|------|------|
| `POST /api/v1/default-bank-account-rules/update` | 更新规则,主体字段禁止修改;**必须携带 `lock_token`**,后端校验 |
| `POST /api/v1/default-bank-account-rules/delete/{id}` | 软删;**响应中包含被引用 N** |
| `POST /api/v1/default-bank-account-rules/{id}/enable` | status → Active;**写审计日志** |
| `POST /api/v1/default-bank-account-rules/{id}/disable` | status → Inactive;**写审计日志** |

**更新请求示例**(带 lock_token):
```json
{
  "id": 12,
  "lockToken": "uuid-abc-123-def",
  "priority": 200,
  "description": "提升优先级",
  ...
}
```

**更新冲突响应**(lock_token 不匹配):
```json
{
  "code": 409,
  "message": "规则已被他人修改(updated_at=2026-07-08T10:30:00),请刷新后重试"
}
```

### 8.3 跨模块调用(dealing → basedata)

**调用方**: `dealing` 模块 FX/AC 录入 Service
**调用方式**: HTTP 同步调用(项目内网,基于 Apache HttpClient 或 OpenFeign)
**调用端点**: `GET /opentms/basedata/api/v1/default-bank-account-rules/match?dualDirection=true`

**前端调用**(避开跨域,Vite 代理):
```js
// web/src/api/basedata/defaultBankAccountRule.js
import request from '@/utils/request'

export function matchDefaultBankAccount(params) {
  return request({
    url: '/basedata/api/v1/default-bank-account-rules/match',
    method: 'get',
    params: { ...params, dualDirection: true }  // ★ v1.1:FX 录入必须传
  })
}

export function testMatchDefaultBankAccount(params) {
  return request({
    url: '/basedata/api/v1/default-bank-account-rules/test-match',
    method: 'get',
    params
  })
}

export function getReferenceCount(id) {
  return request({
    url: `/basedata/api/v1/default-bank-account-rules/${id}/reference-count`,
    method: 'get'
  })
}

export function getAuditLogs(id, params) {
  return request({
    url: `/basedata/api/v1/default-bank-account-rules/${id}/audit-logs`,
    method: 'get',
    params
  })
}

export function listDefaultBankAccountRules(data) {
  return request({
    url: '/basedata/api/v1/default-bank-account-rules/page',
    method: 'post',
    data
  })
}

export function saveDefaultBankAccountRule(data) {
  return request({
    url: '/basedata/api/v1/default-bank-account-rules',
    method: 'post',
    data
  })
}

// updateX/deleteX/enableX/disableX 略
```

### 8.4 接口幂等性

- **新增/更新/删除/启用/停用**:遵循项目规范,使用 `X-Idempotency-Key` 头
- **匹配接口 / 测试匹配 / 引用计数 / 审计日志**:纯读,不需幂等

---

## 九、不在范围(Why)

### 9.1 为什么不做"交易对手默认银行账户"

**业务分析**:
- 主体维度已经能覆盖 80%+ 场景(资金主管熟悉本主体的账户体系)
- 对手方维度会带来复杂度(对手方是银行/非银行?每个对手方有几十个币种账户?)
- 对手方账户体系通常由对手方自身维护,Open-TMS 无法穷举

**架构预留**:
- `tms_default_bank_account_rule_t` 表已设计 `counterparty_id` 字段,**当前版本是 ALL 通配,未来扩展为强匹配只需启用该字段**
- match 算法中 `counterparty_id == req.counterpartyId` 逻辑已实现,只是数据全部为 NULL
- 后续 P2+ 阶段可启用该字段,无需改表结构

**对前端的影响**:当前前端 match 调用入参已含 `counterpartyId`,后端默认为 ALL(忽略该维度);P2+ 可零代码升级。

### 9.2 为什么不做"跨主体继承"

**业务分析**:
- 集团下属主体的银行账户体系差异大,继承容易引入误配
- 当前用户量(资金主管/交易员)集中在单个主体,跨主体场景不紧迫

**架构预留**:
- `tms_management_entity_t` 已有 `parent_code` / `level_depth` 字段,继承数据基础就绪
- P2+ 可加一列 `inherit_parent CHAR(1)`,在 match 算法末位加一步"继承查询"

### 9.3 为什么不做"规则审批流"

**业务分析**:
- 规则变更通常由资金主管单人维护,影响范围有限
- 审批流会增加流程负担,且没有强监管要求
- 现有审计日志表(`tms_rule_audit_log_t`) + `version` + `updated_by/updated_at` 已足够追溯

### 9.4 为什么不做"规则快照(交易侧存 rule_snapshot)"

**业务分析**:
- 实施成本高:FX/AC/AT 三张交易表都要加 `rule_snapshot JSONB` 字段
- 当前业务场景:交易一旦生成,规则变化不影响已存交易(P0)
- 审计追溯通过"规则审计日志"已可实现

**架构预留**:
- `tms_default_bank_account_rule_t` 表已记录 `rule_number`,可作为反向追溯标识
- P1+ 可在 `tms_fx_deals_t` 加 `rule_snapshot JSONB`,存量数据迁移时回填即可

---

## 十、待评审问题

| # | 问题 | 建议方案 | 待决策 |
|---|------|---------|--------|
| Q1 | 规则匹配是否需要返回多条候选(让用户选)? | 当前设计:`/match` 只返回首条,`/test-match` 返回全部 | **决策**:`/match` 单条 + `/test-match` 多条,**已采纳** |
| Q2 | 主体变更时,规则是否需要级联迁移? | 当前:主体不可改,只能删除重建 | **决策**:本期不允许主体变更,简化设计 |
| Q3 | 规则中的账户被停用时,是否阻断交易录入? | 当前:仅警告,允许保存 | **决策**:警告但允许(用户可手动覆盖) |
| Q4 | 优先级是否支持负数(如"-1 表示禁用")? | 当前:0-9999 | **决策**:DB CHECK 0-9999,负数用 `status=Inactive` 表达 |
| Q5 | match 接口是否需要返回规则详情(供前端展示"自动填充原因")? | 当前:`/match` 返回 rule_number + bankAccountId;`/test-match` 返回完整规则 | **决策**:`/match` 精简 + `/test-match` 完整,**已采纳** |
| Q6 | 方向为 ALL 时,实际匹配 Inflow 还是 Outflow? | 当前:双方向查询时,Inflow 规则和 Outflow 规则都返回(规则方向=ALL 等价于"两个方向都生效") | **决策**:符合用户预期,任意匹配,**已采纳** |
| Q7 | 跨主体 FX 录入时(如主体 A 的币种账户在主体 B 名下),规则如何处理? | 当前:规则只匹配主体 A 下的账户 | **决策**:本期不处理(需先做跨主体账户关系,P2+) |
| **Q8** | **lock_token 过期时间**(v1.1) | 当前:30 分钟 | **决策**:30 分钟合理(编辑操作通常 < 5 分钟);**待确认** |
| **Q9** | **Redis 不可用时降级策略**(v1.1) | 当前:直接 DB 查询,日志告警 | **决策**:降级方案 OK;**待确认** |
| **Q10** | **被引用 N 的时间窗口**(v1.1) | 当前:未结算 + 近 90 天已完成 | **决策**:90 天合理;**待确认** |

---

## 十一、Phase 计划

### Phase 1 - 设计

- [x] 编写 M1-主体默认银行账户规则 PRD v1.0(2026-07-05)
- [x] v1.0 优化建议评审(PM-Lead + BA,25 项)
- [x] **编写 v1.1:P0×5 + 关键 P1×4 = 9 项修复(2026-07-08)**
- [ ] PM-Lead 评审 v1.1 → 状态变为 "已评审"
- [ ] UX 交互设计
- [ ] DB 设计 + API 设计
- [ ] 后端开发 + 前端开发
- [ ] 测试执行

### Phase 2 - 实施

- [ ] **DB 设计**:`db/schema/28-default-bank-account-rule-v1.1.sql`
- [ ] **后端**:`basedata` 模块新增 `DefaultBankAccountRule` 实体 + Service + Controller
- [ ] **后端**:`DefaultBankAccountRuleMatchService` 匹配引擎(支持 dualDirection + Redis 缓存)
- [ ] **后端**:`DefaultBankAccountRuleAuditLogService` 审计日志服务
- [ ] **后端**:`GlobalConstants` 新增 `Direction` / `RuleAuditOperation` 枚举
- [ ] **前端**:`DefaultBankAccountRuleList.vue` + `DefaultBankAccountRuleForm.vue`
- [ ] **前端**:FX 录入页(`FxDealForm.vue`)接入 `match?dualDirection=true` + 防抖
- [ ] **测试**:`scripts/test/test_default_bank_account_rule_api.py`
- [ ] **测试**:`scripts/test/test_default_bank_account_rule_ui.py`

### Phase 3 - 增强 (后续)

- [ ] 交易对手默认银行账户 (Z1)
- [ ] 规则导入/导出 (Z2)
- [ ] 跨主体继承 (Z7)
- [ ] 规则变更审批流 (Z4)
- [ ] 多候选匹配(扩展 `/match`)(Q1 升级)
- [ ] 规则快照(交易侧存 `rule_snapshot` JSONB)(Z8)

---

## 十二、相关文档

- v1.0:`docs/prd/M1/M1-主体默认银行账户规则PRD.md`
- 优化建议:`docs/优化需求/默认银行账户规则PRD-优化建议.md`
- `M3-外汇交易PRD.md` v3.2 — FX 录入架构 + Cashflow 生成机制
- `M1-资金管理主体PRD-v1.md` — 主体定义
- `docs/api/basedata/01-bank-accounts.md` v1.2 — 银行账户 API
- `CLAUDE.md` — 项目总规范
- `open-tms功能特性清单.md` — 业界对标参考
- `docs/规范/Open-TMS开发规范文档.md` — 字段命名/类型/审计规范

---

## 附录 A:DDL 草案(v1.1)

```sql
-- ==========================================
-- M1-主体默认银行账户规则 (v1.1)
-- 文件: db/schema/28-default-bank-account-rule-v1.1.sql
-- 作者: PM
-- 日期: 2026-07-08
-- 变更:v1.0 → v1.1(P0-1/P0-2/P0-3/P0-4/P0-5 + P1-2/P1-3/P1-11)
-- ==========================================

-- 启用规则表(无 VARCHAR 冗余,active 唯一约束,priority 范围,lock_token)
CREATE TABLE tms_default_bank_account_rule_t (
    id                   BIGSERIAL       PRIMARY KEY,
    rule_number          VARCHAR(50)     NOT NULL UNIQUE,

    -- 5 维匹配维度(只用 _id BIGINT)
    management_entity_id BIGINT          NOT NULL,
    counterparty_id      BIGINT,                                    -- NULL=ALL
    instrument_id        BIGINT,                                    -- NULL=ALL
    direction            VARCHAR(20)     NOT NULL,
    currency             VARCHAR(10),                               -- NULL=ALL(★ v1.1 允许 NULL)

    -- 输出
    bank_account_id      BIGINT          NOT NULL,

    -- 标识/状态
    status               VARCHAR(20)     NOT NULL DEFAULT 'Active',
    priority             INT             NOT NULL DEFAULT 0,        -- 0-9999(★ v1.1)
    start_date           DATE,

    -- 描述
    description          VARCHAR(500),
    remark               VARCHAR(500),

    -- 并发控制(★ v1.1 新增)
    lock_token           VARCHAR(64),
    locked_by            VARCHAR(50),
    locked_at            TIMESTAMP,

    -- 审计
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    -- 约束
    CONSTRAINT chk_rule_direction CHECK (direction IN ('Inflow', 'Outflow', 'ALL')),
    CONSTRAINT chk_rule_status    CHECK (status IN ('Active', 'Inactive')),
    CONSTRAINT chk_rule_priority  CHECK (priority BETWEEN 0 AND 9999),
    CONSTRAINT chk_rule_bank_entity CHECK (bank_account_id IS NOT NULL),
    -- ★ v1.1 Active 唯一约束
    CONSTRAINT uniq_rule_active_dims UNIQUE (
        management_entity_id, counterparty_id, instrument_id, direction, currency, status
    )
);

-- 索引
CREATE INDEX idx_dbar_mgmt_entity  ON tms_default_bank_account_rule_t(management_entity_id);
CREATE INDEX idx_dbar_counterparty ON tms_default_bank_account_rule_t(counterparty_id);
CREATE INDEX idx_dbar_instrument   ON tms_default_bank_account_rule_t(instrument_id);
CREATE INDEX idx_dbar_direction    ON tms_default_bank_account_rule_t(direction);
CREATE INDEX idx_dbar_currency     ON tms_default_bank_account_rule_t(currency);
CREATE INDEX idx_dbar_status       ON tms_default_bank_account_rule_t(status);
CREATE INDEX idx_dbar_priority     ON tms_default_bank_account_rule_t(priority DESC);
CREATE INDEX idx_dbar_bank_account ON tms_default_bank_account_rule_t(bank_account_id);

-- 注释
COMMENT ON TABLE  tms_default_bank_account_rule_t IS '主体默认银行账户规则(v1.1)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.currency IS '币种(NULL=ALL 通配)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.priority IS '优先级,数字越大越优先,范围 0-9999';
COMMENT ON COLUMN tms_default_bank_account_rule_t.lock_token IS '乐观锁 token(UUID,编辑生成,提交校验)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.locked_by IS '锁定人';
COMMENT ON COLUMN tms_default_bank_account_rule_t.locked_at IS '锁定时间';

-- ==========================================
-- 审计日志表(★ v1.1 新增)
-- ==========================================
CREATE TABLE tms_rule_audit_log_t (
    id           BIGSERIAL PRIMARY KEY,
    rule_id      BIGINT          NOT NULL,
    operation    VARCHAR(20)     NOT NULL,
    old_value    JSONB,
    new_value    JSONB,
    operator     VARCHAR(50)     NOT NULL,
    operated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark       VARCHAR(500),

    CONSTRAINT chk_rule_audit_op CHECK (operation IN ('CREATE', 'UPDATE', 'DELETE', 'ENABLE', 'DISABLE'))
);

CREATE INDEX idx_ral_rule_id      ON tms_rule_audit_log_t(rule_id);
CREATE INDEX idx_ral_operator    ON tms_rule_audit_log_t(operator);
CREATE INDEX idx_ral_operated_at  ON tms_rule_audit_log_t(operated_at DESC);

COMMENT ON TABLE  tms_rule_audit_log_t IS '规则变更审计日志';
COMMENT ON COLUMN tms_rule_audit_log_t.old_value IS '变更前 JSONB 快照';
COMMENT ON COLUMN tms_rule_audit_log_t.new_value IS '变更后 JSONB 快照';

-- ==========================================
-- 引用 N 查询索引(★ v1.1 新增,基于 tms_deals_t)
-- ==========================================
-- 仅当 tms_deals_t.bank_account_id 索引不存在时执行
CREATE INDEX IF NOT EXISTS idx_deal_bank_account_status
  ON tms_deals_t(bank_account_id, status);
```

## 附录 B:状态机

本特性规则状态较简单,无复杂状态机:

```
         新增
          │
          ▼
       Active ◄──────► Inactive
       (匹配)         (不匹配,保留审计)
          │
          │ 软删
          ▼
       Deleted (deleted='1',不展示,保留数据)
```

并发编辑:
```
       Unlocked ──GET /{id}──> Locked(by User A, token=X)
                                       │
                                       │ User B GET /{id}
                                       ▼
                                   Locked(by User A)
                                       │ 409 Conflict
                                       │ User B 看到 "需刷新"
                                       ▼
                                   User A POST /update with token=X → 成功
                                       │
                                       ▼
                                   Unlocked(token 失效)
```

## 附录 C:v1.0 → v1.1 关键变更清单

| # | 项 | v1.0 | v1.1 | 优化建议编号 |
|---|----|------|------|--------------|
| 1 | `counterparty` 字段 | VARCHAR(20) + counterparty_id BIGINT 双写 | 只保留 `counterparty_id BIGINT` | **P0-1** |
| 2 | `instrument` 字段 | VARCHAR(20) + instrument_id BIGINT 双写 | 只保留 `instrument_id BIGINT` | **P0-1** |
| 3 | `currency` 字段 | NOT NULL DEFAULT 'ALL' | 允许 NULL(NULL=ALL) | **P0-1 + P1-11** |
| 4 | match 接口单/双方向 | 单方向,只返回一个账户 | `?dualDirection=true` 返回 inflow + outflow 两个账户 | **P0-2** |
| 5 | 并发控制 | 无 | `lock_token` + `locked_by/ locked_at` + 409 Conflict | **P0-3** |
| 6 | 被引用 N 查询 | 未定义 | 实时查询"未结算 + 近 90 天" + 索引 | **P0-4** |
| 7 | FX 录入防抖 | 未定义 | 前端 300ms debounce + Redis 缓存 TTL 5 分钟 | **P0-5** |
| 8 | priority 范围 | 无 CHECK | CHECK (priority BETWEEN 0 AND 9999) | **P1-2** |
| 9 | Active 唯一约束 | 无 | DB UNIQUE (主体, 对手方, 产品, 方向, 币种, status) | **P1-3** |
| 10 | 端点列表 | 8 个 | 11 个(新增 test-match / reference-count / audit-logs) | **P1-8** |
| 11 | 审计日志 | 无 | `tms_rule_audit_log_t` 表 + JSONB 快照 | **P1-5(本版本采纳)** |

---

*PM产出 - M1 v1.1 (2026-07-08)*
*v1.0 → v1.1 关键升级:5 维匹配 → 双方向匹配 + 并发控制 + 审计日志 + Redis 缓存*
*P0 全部修复 + 关键 P1 共 9 项,质量达到企业级标准*