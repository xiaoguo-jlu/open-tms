# M1-主体默认银行账户规则 PRD

**版本**: v1.0
**角色**: 产品经理 (PM)
**日期**: 2026-07-05
**基于**:
- `M3-外汇交易PRD.md` v3.2 (FX 录入架构)
- `docs/api/basedata/01-bank-accounts.md` v1.2 (银行账户 API)
- `M1-资金管理主体PRD-v1.md` (管理主体定义)
- 2026-07-03 模块整合 (basedata 端口 8081,dealing 端口 8082)
**状态**: v1.0 草稿 - 待评审

---

## 〇、修订记录

| 版本 | 日期 | 变更 | 作者 |
|------|------|------|------|
| v1.0 | 2026-07-05 | 初版,覆盖主体默认银行账户规则维护 + FX 自动匹配 | PM |

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

**业务诉求**:在交易录入时,系统根据交易要素(**主体 / 对手方 / 金融产品 / 方向 / 币种**)自动匹配出"应付账户"和"应收账户",允许用户手动覆盖。

### 1.3 功能定位

为 Open-TMS 提供"主体维度"的银行账户默认规则维护与运行时匹配能力:

- **规则维护**:基于树形维度(主体 → 对手方 → 金融产品 → 方向 → 币种)定义"该交易场景下,默认用哪个银行账户"
- **规则匹配引擎**:FX/AC 录入时,根据交易要素实时返回默认账户 ID,前端自动填充
- **优先级冲突**:同一维度组合有多条规则时,按 `priority DESC, created_at ASC` 取首条

### 1.4 用户角色

| 角色 | 典型操作 |
|------|---------|
| **资金主管 (Treasury Admin)** | 维护主体默认银行账户规则(新增/编辑/启用/停用) |
| **外汇/资金交易员** | FX 录入时自动获取默认账户,确认或手动覆盖 |
| **会计/审计** | 查阅规则变更审计(基于 `version` + `updated_by/updated_at`) |
| **系统管理员** | 全量规则查询、批量停用 |

### 1.5 与其他模块的关系

```
default-bank-account-rule (本特性)
  │
  ├── 宿主模块: basedata (8081)
  │     ├── 新增 1 张表: tms_default_bank_account_rule_t
  │     └── 新增 3 个 REST 端点: /api/v1/default-bank-account-rules
  │
  ├── 依赖 basedata 既有数据
  │     ├── tms_management_entity_t (管理主体)
  │     ├── tms_counterparty_t (交易对手)
  │     ├── tms_instrument_t (金融工具)
  │     ├── tms_currency_t (币种)
  │     └── tms_bank_account_t (银行账户)
  │
  ├── 被 dealing 调用 (FX/AC 录入)
  │     ├── 同步调用: GET /api/v1/default-bank-account-rules/match
  │     └── 入参: { managementEntityId, counterpartyId, instrumentId, direction, currency }
  │     └── 出参: { bankAccountId, ruleId, ruleNumber }
  │
  └── 不影响: valuation / var / fundplan (本特性只涉及交易录入)
```

### 1.6 范围说明

**本次范围 (P0)**:
- 主体默认银行账户规则(单维度主体)
- 规则维护 (CRUD + 启用/停用)
- 规则匹配引擎(FX 录入时自动带出)
- FX 场景(SPOT/FWD/NDF)联动;AC 场景预留接口

**本次不在范围 (P1/P2+)**:
- **交易对手默认银行账户**(本次只做主体侧;架构允许后续扩展,见第九节)
- 规则导入/导出 (P1)
- 规则批量复制(同主体下复制到子主体)(P2)
- 规则变更审批流(P2)
- AI 推荐账户(P3+)

---

## 二、业界对标

| 特性 | FIS Quantum | SAP TRM | Murex MX.3 | Open-TMS v1.0 |
|------|-------------|---------|-----------|---------------|
| 默认账户规则(Default Account Rule) | "Settlement Account Rules" 模块 | "House Bank Determination" 配置 | "Account Determination Rules" | **P0 主体维度** |
| 优先级排序 | Yes (sequence) | Yes (search sequence) | Yes (priority + validity) | ✅ priority DESC, created_at ASC |
| 通配符(ALL) | Yes (wildcard) | Yes (*) | Yes (ANY) | ✅ 4 个非主体维度支持 ALL |
| 开始生效日 | Yes (effective date) | Yes (valid from) | Yes (effective date) | ✅ start_date |
| 维度组合 | 多达 8 维(主体/公司代码/银行/币种/用途/...) | 5-6 维 | 多达 10 维 | 5 维(主体 + 4 ALL) |
| 跨主体继承 | Yes (parent → child) | Yes (hierarchy) | Yes (hierarchy) | **P2+**(本版本不做) |
| 运行时匹配 | 自动(配置驱动) | 自动(IMG 配置) | 自动(Market Rule) | ✅ 自动(基于 5 维规则) |
| 对手方默认账户 | 单独配置 | 单独配置 | 单独配置 | **P2+**(本版本不做) |

> **核心差异**:Open-TMS 本次聚焦"主体维度",维度精简到 5 个(主体固定 + 4 个 ALL 通配),**KISS 原则**。

---

## 三、功能清单

### 3.1 规则维护 (P0)

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 新增规则 | 配置主体 × 对手方 × 金融产品 × 方向 × 币种 → 默认银行账户 | **P0** |
| 编辑规则 | 修改任意维度字段(主体不可改)或优先级、生失效 | **P0** |
| 删除规则 | 软删(基于 `deleted='1'`),保留审计痕迹 | **P0** |
| 启用/停用规则 | `status` 切换 (Active/Inactive),不删数据 | **P0** |
| 分页查询 | 按主体/状态/金融产品等过滤,优先级 DESC 排序 | **P0** |
| 详情查询 | 单条规则完整字段 | **P0** |

### 3.2 规则匹配引擎 (P0)

| 功能 | 说明 | 优先级 |
|------|------|--------|
| FX 录入自动匹配 | FX 录入时(选完 5 个维度后)调 match 接口,自动带出默认账户 ID | **P0** |
| AC 录入自动匹配 | AC 录入时(选完主体 + 币种)调 match 接口 | **P1**(预留,本版本接口实现,前端接入 P2) |
| 手动覆盖 | 自动匹配后用户仍可手动选择其他账户 | **P0**(默认行为) |
| 无匹配兜底 | 无匹配规则时,账户字段留空,提示"无默认账户,请手动选择" | **P0** |
| 多匹配取首条 | 命中多条规则时,按 priority DESC, created_at ASC 取首条 | **P0** |

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

```sql
CREATE TABLE tms_default_bank_account_rule_t (
    -- ★ 业务主键
    id                 BIGSERIAL       PRIMARY KEY,
    rule_number        VARCHAR(50)     NOT NULL UNIQUE,           -- 规则编号,如 RULE202607050001

    -- ★ 核心匹配维度(5 维)
    management_entity_id BIGINT        NOT NULL,                  -- 主体(FK→tms_management_entity_t.id),单选,不能 ALL
    counterparty        VARCHAR(20)    NOT NULL DEFAULT 'ALL',    -- 对手方,ALL 表示通配(本表存 ID 或 'ALL',建议 ID)
    counterparty_id     BIGINT,                                   -- 对手方 FK(可空,NULL=ALL),引用 tms_counterparty_t.id
    instrument          VARCHAR(20)    NOT NULL DEFAULT 'ALL',    -- 金融产品(可空=ALL),引用 tms_instrument_t.id
    instrument_id       BIGINT,                                   -- 金融产品 FK(可空,NULL=ALL)
    direction           VARCHAR(20)    NOT NULL,                  -- 方向:Inflow(收)/Outflow(付)/ALL
    currency            VARCHAR(10)    NOT NULL DEFAULT 'ALL',    -- 币种(可空=ALL),引用 tms_currency_t.code

    -- ★ 输出:默认银行账户
    bank_account_id     BIGINT         NOT NULL,                  -- FK→tms_bank_account_t.id,且 bank_account.management_entity_id = management_entity_id

    -- ★ 标识/状态
    status              VARCHAR(20)    NOT NULL DEFAULT 'Active', -- Active/Inactive
    priority            INT            NOT NULL DEFAULT 0,        -- 优先级,数字越大越优先
    start_date          DATE,                                     -- 开始生效日(可空=立即生效);rule_date <= today 才算生效

    -- ★ 描述
    description         VARCHAR(500),                             -- 业务说明,例:"USD SPOT 默认收账账户"
    remark              VARCHAR(500),                             -- 备注

    -- ★ 审计(全表必备)
    created_by          VARCHAR(50)    NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT            NOT NULL DEFAULT 0,        -- 乐观锁(@Version)
    deleted             CHAR(1)        NOT NULL DEFAULT '0',      -- 软删除(@TableLogic)

    -- ★ 约束
    CONSTRAINT chk_rule_direction CHECK (direction IN ('Inflow', 'Outflow', 'ALL')),
    CONSTRAINT chk_rule_status CHECK (status IN ('Active', 'Inactive')),
    CONSTRAINT chk_rule_bank_entity CHECK (bank_account_id IS NOT NULL)
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
```

> **★ 字段设计决策**:
> - `counterparty` / `instrument` 用 `_id BIGINT` 强类型 FK,不用 VARCHAR code(对齐 FX v3.1 风格)
> - `ALL` 通配用 `NULL` 值表达(可空字段 = ALL),不用 `'ALL'` 字符串(更节省索引 + 语义清晰)
> - 但同时保留 VARCHAR 字段做冗余,用于前端显示友好(`counterparty='ALL'`、`instrument='FX-FWD-USD-CNY-3M'`)
> - `bank_account_id` 必须属于 `management_entity_id`(应用层校验,DB 不强约束)
> - `status` 用 VARCHAR(20),不用 CHAR(1)(项目规范要求)

### 4.2 字段详细表

| 字段 | DB 列 | Java 类型 | 前端类型 | 必填 | 默认 | 说明 |
|------|-------|----------|---------|------|------|------|
| 规则主键 | `id` | `Long` | - | - | auto | DB 主键 |
| 规则编号 | `rule_number` | `String` | - | ✓ | 系统生成 | 格式 `RULEyyyyMMddxxxx`,4 位流水 |
| **主体** | `management_entity_id` | `Long` | **BaseDataPicker** | ✓ | - | FK→管理主体,单选,**不能 ALL** |
| **对手方** | `counterparty_id` | `Long` | BaseDataPicker | - | NULL=ALL | FK→对手方;空=通配 |
| **金融产品** | `instrument_id` | `Long` | BaseDataPicker | - | NULL=ALL | FK→金融工具;空=通配 |
| **方向** | `direction` | `String` | **Select** | ✓ | - | `Inflow`/`Outflow`/`ALL` |
| **币种** | `currency` | `String` | Select | - | NULL=ALL | ISO 4217;空=通配 |
| **默认账户** | `bank_account_id` | `Long` | BaseDataPicker | ✓ | - | **必须先选主体才能选账户**,前端联动 |
| 状态 | `status` | `String` | Switch | ✓ | Active | Active/Inactive |
| 优先级 | `priority` | `Integer` | InputNumber | ✓ | 0 | 数字越大越优先 |
| 开始生效日 | `start_date` | `LocalDate` | DatePicker | - | NULL=立即 | `start_date IS NULL OR start_date <= today` 才算生效 |
| 描述 | `description` | `String` | Input | - | - | 业务说明 |
| 备注 | `remark` | `String` | Input | - | - | 内部备注 |
| 审计字段 | created_by/created_at/updated_by/updated_at/version/deleted | - | - | ✓ | - | 项目强制审计 |

### 4.3 编号生成规则

`rule_number` 格式: `RULE + yyyyMMdd + 4 位流水`

例: `RULE202607050001`、`RULE202607050002`、`RULE202607060001`

**生成方式**:同交易日下递增;跨日重置。
**实现位置**:`basedata` 模块 `RuleNumberGenerator` 工具类(或复用 `GlobalConstants` 现有 `SerialNumberGenerator`)。

### 4.4 全局枚举值

| 枚举 | 取值 | 说明 |
|------|------|------|
| 方向 Direction | `Inflow` / `Outflow` / `ALL` | 收款 / 付款 / 通配 |
| 状态 Status | `Active` / `Inactive` | 启用 / 停用 |
| 通配符 ALL | `NULL` (DB 层) / `'ALL'` (前端显示) | - |

> **枚举来源**:方向枚举遵循 `GlobalConstants.Direction`(需新增,见第七章验收标准)。
> **状态枚举**:遵循 `GlobalConstants.STATUS_ENABLED/DISABLED` 或新建 `RULE_STATUS_ACTIVE/INACTIVE`。

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
| R6 | **ALL 维度 DB 存储为 NULL**,前端显示为 "ALL" | 节省索引 + 语义清晰 |

### 5.2 优先级与排序规则

| # | 规则 | 说明 |
|---|------|------|
| R7 | **多匹配排序**: `priority DESC, created_at ASC` | 数字越大越优先;同优先级先创建先生效 |
| R8 | **首条生效**: 取排序后的第一条规则 | 后续规则视为"兜底" |
| R9 | **优先级可同值** | 同优先级时按创建时间排序 |
| R10 | **优先级必须 ≥ 0** | DB CHECK 或应用层校验 |

### 5.3 生失效规则

| # | 规则 | 说明 |
|---|------|------|
| R11 | **规则生效条件**: `status = 'Active'` AND (`start_date IS NULL` OR `start_date <= today`) AND `deleted = '0'` | 三者全部满足才参与匹配 |
| R12 | **停用规则不匹配** | `status = 'Inactive'` 立即停止匹配 |
| R13 | **未到生效日不匹配** | `today < start_date` 的规则不参与 |
| R14 | **生效后无结束日** | 本版本不支持结束日期(P2+) |

### 5.4 账户归属规则

| # | 规则 | 说明 |
|---|------|------|
| R15 | **账户必须属于主体** | `bank_account.management_entity_id = rule.management_entity_id`,应用层校验 |
| R16 | **一个规则只能输出一个账户** | 不支持"主备账户"自动切换(P2+) |
| R17 | **账户停用不影响规则匹配** | 规则中的账户即使 `bank_account.status='0'`,仍会带出(警告用户账户已停用) |

### 5.5 唯一性规则

| # | 规则 | 说明 |
|---|------|------|
| R18 | **同一维度组合允许重复** | 允许"两条规则维度完全一样但优先级不同"(用户可手动调优先级) |
| R19 | **不强制唯一约束** | DB 不加 UNIQUE(主体,对手方,金融产品,方向,币种),留给优先级解决 |

### 5.6 编辑/删除规则

| # | 规则 | 说明 |
|---|------|------|
| R20 | **编辑时主体不可改** | 主体是规则的"根",改主体等于删旧建新 |
| R21 | **其他维度可改** | 对手方/金融产品/方向/币种/账户/优先级/状态均可编辑 |
| R22 | **软删除** | 基于 `deleted='1'`,保留完整审计痕迹 |
| R23 | **已被引用的规则允许停用,但不推荐删除** | 系统提示"该规则已被 N 笔交易引用" |

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
│     [7] 设置优先级 (默认 0)                                       │
│     [8] 设置开始生效日 (可空=立即生效)                              │
│     [9] 保存 → 系统生成 rule_number,写库,返回详情                  │
│                                                            │
│  3. 编辑规则                                                  │
│     [1] 列表点击「编辑」进入详情页                                 │
│     [2] 主体字段灰显不可改                                       │
│     [3] 修改其他字段后保存 → version +1,updated_at 更新           │
│                                                            │
│  4. 启用/停用                                                 │
│     [1] 列表行内 Switch 切换                                    │
│     [2] 停用后规则不参与匹配,但不删数据                            │
│                                                            │
│  5. 删除                                                     │
│     [1] 列表点击「删除」 → 二次确认                                │
│     [2] 系统提示"该规则已被 N 笔交易引用"                          │
│     [3] 确认 → 软删 (deleted='1'),前端列表不再展示                 │
└─────────────────────────────────────────────────────────┘
```

### 6.2 FX 录入自动匹配流程(核心场景)

```
┌──────────────────────────────────────────────────────────┐
│  场景:交易员录入 SPOT 交易                                    │
│                                                            │
│  Step 1: 选择「管理主体」                                     │
│     → 前端触发: GET /api/v1/default-bank-account-rules/match?     │
│        managementEntityId=1&currency=USD                    │
│     → 后端缓存命中空(尚未选完维度),返回空                          │
│     → 账户字段留空                                             │
│                                                            │
│  Step 2: 选择「金融产品」 (FX-SPOT-USD-CNY)                    │
│     → 前端触发: GET /api/v1/default-bank-account-rules/match?     │
│        managementEntityId=1                                 │
│        &instrumentId=401                                     │
│        &direction=Inflow (默认 Inflow 视买入币种)               │
│        &currency=USD                                         │
│     → 后端匹配算法(详见 6.3)→ 命中规则 #RULE202607050001        │
│     → 返回: { bankAccountId: 1001, ruleNumber: "RULE..." }   │
│     → 前端自动填充「收账账户 = 1001」                            │
│                                                            │
│  Step 3: 选择「币种对」(USD/CNY)                                │
│     → 重新触发 match (因 direction 和 currency 可能变化)          │
│     → 命中规则 #RULE202607050001 (与 Step 2 同)                │
│     → 「收账账户 = 1001 USD」 / 「付账账户 = 1002 CNY」 自动填充    │
│                                                            │
│  Step 4: 用户可手动覆盖                                       │
│     → 在账户字段下拉选择其他账户                                  │
│     → 不修改规则表,仅影响当前这笔交易                              │
│                                                            │
│  Step 5: 保存交易                                            │
│     → 交易表中记录 payAccountId / receiveAccountId              │
│     → 规则本身不被修改                                           │
└──────────────────────────────────────────────────────────┘
```

### 6.3 匹配算法(后端实现)

**输入** (URL Query 或 JSON Body):
```json
{
  "managementEntityId": 1,
  "counterpartyId": 5001,
  "instrumentId": 401,
  "direction": "Inflow",
  "currency": "USD"
}
```

**算法步骤**:
```java
public MatchResult match(MatchRequest req) {
    // 1. 基础过滤:status=Active + (start_date 生效) + deleted=0
    List<DefaultBankAccountRule> candidates = ruleRepository.findActiveAndEffective(today);
    
    // 2. 主体过滤(必须相等)
    candidates = candidates.stream()
        .filter(r -> r.getManagementEntityId().equals(req.getManagementEntityId()))
        .collect(Collectors.toList());
    if (candidates.isEmpty()) return MatchResult.empty();
    
    // 3. 对手方过滤(rule.cp == NULL=ALL OR == req.cp)
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
    
    // 5. 方向过滤(必传)
    candidates = candidates.stream()
        .filter(r -> "ALL".equals(r.getDirection()) 
            || r.getDirection().equals(req.getDirection()))
        .collect(Collectors.toList());
    
    // 6. 币种过滤
    if (req.getCurrency() != null) {
        candidates = candidates.stream()
            .filter(r -> r.getCurrency() == null 
                || r.getCurrency().equals(req.getCurrency()))
            .collect(Collectors.toList());
    }
    
    // 7. 排序:priority DESC, created_at ASC
    candidates.sort(Comparator
        .comparingInt(DefaultBankAccountRule::getPriority).reversed()
        .thenComparing(DefaultBankAccountRule::getCreatedAt));
    
    // 8. 取首条
    if (candidates.isEmpty()) return MatchResult.empty();
    DefaultBankAccountRule top = candidates.get(0);
    return MatchResult.of(top.getBankAccountId(), top.getRuleNumber());
}
```

**输出**:
```json
{
  "code": 200,
  "data": {
    "matched": true,
    "bankAccountId": 1001,
    "ruleId": 12,
    "ruleNumber": "RULE202607050001",
    "priority": 100
  }
}
```

**无匹配输出**:
```json
{
  "code": 200,
  "data": {
    "matched": false,
    "bankAccountId": null,
    "ruleId": null,
    "ruleNumber": null,
    "priority": null
  }
}
```

### 6.4 时序图(FX 录入联动)

```
[Frontend FX Form]                  [Backend dealing]                [Backend basedata]
       │                                  │                                  │
       │ ① 选管理主体=1                     │                                  │
       │──────────────────────────────────>│                                  │
       │                                  │  GET /match?...                 │
       │                                  │───────────────────────────────>│
       │                                  │<───────────────────────────────│
       │<──────────────────────────────────│                                  │
       │  (空或部分匹配)                    │                                  │
       │                                  │                                  │
       │ ② 选金融产品=401 + 币种=USD        │                                  │
       │──────────────────────────────────>│                                  │
       │                                  │  GET /match?mgmt=1&ins=401&cur=USD
       │                                  │───────────────────────────────>│
       │                                  │                                  │
       │                                  │   [匹配算法 8 步]                │
       │                                  │   → 返回 bankAccountId=1001     │
       │                                  │<───────────────────────────────│
       │<──────────────────────────────────│                                  │
       │  收账账户=1001 自动填充            │                                  │
       │                                  │                                  │
       │ ③ 用户可手动覆盖                  │                                  │
       │  (不调后端)                       │                                  │
       │                                  │                                  │
       │ ④ 保存交易                       │                                  │
       │──────────────────────────────────>│  POST /api/v1/dealing/fx-deals  │
       │                                  │                                  │
```

---

## 七、验收标准

### 7.1 P0 核心验收

| # | 功能 | 验收条件 |
|---|------|----------|
| A1 | 表结构 | `tms_default_bank_account_rule_t` 字段齐全,审计字段完整,索引齐备 |
| A2 | 编号生成 | `rule_number` 格式 `RULEyyyyMMddxxxx`,同日内递增,跨日重置 |
| A3 | 新增规则 | 可正常新增一条规则,前端 5 个维度(主体必填)+ 账户必填(账户属于主体) + 优先级 + 状态,后端写库成功 |
| A4 | 编辑规则 | 编辑后 `version +1`, `updated_at` / `updated_by` 更新 |
| A5 | 删除规则 | 软删后 `deleted='1'`,列表不再展示,DB 数据保留 |
| A6 | 启用/停用 | 切换 `status` 后,Active 参与匹配,Inactive 不参与 |
| A7 | 分页查询 | 支持按主体/对手方/金融产品/币种/状态过滤,默认按 `priority DESC` 排序 |
| A8 | 详情查询 | 单条规则返回完整字段(含 4 个 ALL 通配维度) |
| A9 | 匹配接口 | `GET /api/v1/default-bank-account-rules/match` 入参 5 维,出参银行账户 ID |
| A10 | ALL 通配 | 4 个非主体维度为 NULL 时,视为通配,匹配任意值 |
| A11 | 主体不可 ALL | 主体必填且必须等于 `req.managementEntityId`,否则无匹配 |
| A12 | 优先级排序 | 多条规则命中时,按 `priority DESC, created_at ASC` 取首条 |
| A13 | 开始生效日 | `today >= start_date` 才算生效;`today < start_date` 不参与匹配;`start_date IS NULL` 立即生效 |
| A14 | 账户归属校验 | 新增/编辑时,前端校验账户属于主体;后端再校验一次 |
| A15 | FX 联动 | FX 录入页选完 5 维度后,自动调用 match 接口,账户字段自动填充 |
| A16 | 手动覆盖 | 自动填充后用户仍可手动选择其他账户,不影响规则表 |
| A17 | 无匹配兜底 | 无规则命中时,账户字段留空,前端提示"无默认账户,请手动选择" |
| A18 | 字段命名 | DB snake_case / Java camelCase / JS lowerCamelCase,与项目规范一致 |
| A19 | 状态字段 | `status` VARCHAR(20),不用 CHAR(1)(项目规范) |
| A20 | 枚举规范 | 方向枚举 `Inflow`/`Outflow`/`ALL` 来自 `GlobalConstants`;不用魔术字符串 |

### 7.2 兼容性验收

| # | 项 | 验收 |
|---|----|------|
| C1 | 与银行账户 API 兼容 | 不修改 `tms_bank_account_t`,仅消费其数据 |
| C2 | 与 FX PRD v3.2 兼容 | FX 录入联动不破坏 4 个 Tab,不影响 DealMap/Cashflow 生成 |
| C3 | 与 basedata 端口兼容 | API 部署在 `basedata` 模块,端口 8081,匹配现有 Vite 代理 |
| C4 | 与 AC/AT 兼容 | match 接口预留,AC/AT 调用方未实现前不影响 AC/AT 现有逻辑 |
| C5 | 与 `dealType` 兼容 | 不在规则表中存 `dealType`,匹配时由调用方根据 deal 类型决定 direction 传值 |

### 7.3 暂不做项(明确范围)

| # | 功能 | 状态 |
|---|------|------|
| Z1 | 交易对手默认银行账户 | **P2+** 不在本版本(架构允许,见第九节) |
| Z2 | 规则导入/导出 | **P1** 后续迭代 |
| Z3 | 规则批量复制 | **P2** 后续迭代 |
| Z4 | 规则变更审批流 | **P2** 后续迭代 |
| Z5 | AI 推荐账户 | **P3+** 暂不规划 |
| Z6 | 结束日期 | **P2+** 暂不支持(本版本 start_date 单边) |
| Z7 | 跨主体继承 | **P2+** 不在本版本 |

---

## 八、接口需求

### 8.1 新增端点(共 3 个)

部署在 `basedata` 模块,路径 `/api/v1/default-bank-account-rules`,遵循 CXF + JAX-RS 风格。

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/api/v1/default-bank-account-rules/page` | 分页查询(注:项目基于 CXF 的 GET page 与 Spring MVC 略有差异,本特性按 CXF 风格 POST 调 page) |
| 2 | GET | `/api/v1/default-bank-account-rules/{id}` | 详情 |
| 3 | POST | `/api/v1/default-bank-account-rules` | 新增 |
| 4 | POST | `/api/v1/default-bank-account-rules/update` | 更新 |
| 5 | POST | `/api/v1/default-bank-account-rules/delete/{id}` | 删除(软删) |
| 6 | POST | `/api/v1/default-bank-account-rules/{id}/enable` | 启用 |
| 7 | POST | `/api/v1/default-bank-account-rules/{id}/disable` | 停用 |
| 8 | GET | `/api/v1/default-bank-account-rules/match` | **核心**:运行时匹配接口 |

> **推荐端点(2-3 个)**:
> 1. **运行时匹配**:`GET /match` (核心,被 dealing 调用)
> 2. **CRUD**: `POST` 新增 / `POST /update` / `GET /{id}` / `POST /delete/{id}` (4 个标准端点)

### 8.2 详细接口定义

#### 8.2.1 运行时匹配接口(核心)

```
GET /opentms/basedata/api/v1/default-bank-account-rules/match
```

**Query 参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `managementEntityId` | Long | ✓ | 主体 |
| `counterpartyId` | Long | - | 对手方(可省=ALL) |
| `instrumentId` | Long | - | 金融产品(可省=ALL) |
| `direction` | String | ✓ | `Inflow`/`Outflow` |
| `currency` | String | - | 币种(可省=ALL) |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matched": true,
    "bankAccountId": 1001,
    "ruleId": 12,
    "ruleNumber": "RULE202607050001",
    "priority": 100
  },
  "timestamp": 1751731200000
}
```

**无匹配响应**:
```json
{
  "code": 200,
  "data": {
    "matched": false,
    "bankAccountId": null,
    "ruleId": null
  }
}
```

#### 8.2.2 新增规则

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
  "startDate": "2026-07-05",
  "status": "Active",
  "description": "USD SPOT 默认收账账户",
  "remark": "适用于中行对手方"
}
```

**响应**:完整规则 VO(含 `rule_number`)

#### 8.2.3 分页查询

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

#### 8.2.4 更新 / 删除 / 启用 / 停用

| 端点 | 说明 |
|------|------|
| `POST /api/v1/default-bank-account-rules/update` | 更新规则,主体字段禁止修改(后端校验) |
| `POST /api/v1/default-bank-account-rules/delete/{id}` | 软删 |
| `POST /api/v1/default-bank-account-rules/{id}/enable` | status → Active |
| `POST /api/v1/default-bank-account-rules/{id}/disable` | status → Inactive |

### 8.3 跨模块调用(dealing → basedata)

**调用方**: `dealing` 模块 FX/AC 录入 Service
**调用方式**: HTTP 同步调用(项目内网,基于 Apache HttpClient 或 OpenFeign)
**调用端点**: `GET /opentms/basedata/api/v1/default-bank-account-rules/match`

**前端调用**(避开跨域,Vite 代理):
```js
// web/src/api/basedata/default-bank-account-rule.js
import request from '@/utils/request'

export function matchDefaultBankAccount(params) {
  return request({
    url: '/basedata/api/v1/default-bank-account-rules/match',
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
- **匹配接口**:纯读,不需幂等

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
- 现有审计字段(`version` + `updated_by/updated_at`)已足够追溯

---

## 十、待评审问题

| # | 问题 | 建议方案 | 待决策 |
|---|------|---------|--------|
| Q1 | 规则匹配是否需要返回多条候选(让用户选)? | 当前设计:只返回首条 + 提示"另有 N 条可匹配" | **决策**:只返回首条,P2+ 考虑多候选 |
| Q2 | 主体变更时,规则是否需要级联迁移? | 当前:主体不可改,只能删除重建 | **决策**:本期不允许主体变更,简化设计 |
| Q3 | 规则中的账户被停用时,是否阻断交易录入? | 当前:仅警告,允许保存 | **决策**:警告但允许(用户可手动覆盖) |
| Q4 | 优先级是否支持负数(如"-1 表示禁用")? | 当前:仅 ≥ 0 | **决策**:仅 ≥ 0,负数用 `status=Inactive` 表达 |
| Q5 | match 接口是否需要返回规则详情(供前端展示"自动填充原因")? | 当前:仅返回 `bankAccountId` + `ruleNumber` | **决策**:本期只返回 rule_number,详情 P1 |
| Q6 | 方向为 ALL 时,实际匹配 Inflow 还是 Outflow? | 当前:方向 ALL 时,任意方向都匹配 | **决策**:符合用户预期,任意匹配 |
| Q7 | 跨主体 FX 录入时(如主体 A 的币种账户在主体 B 名下),规则如何处理? | 当前:规则只匹配主体 A 下的账户 | **决策**:本期不处理(需先做跨主体账户关系,P2+) |

---

## 十一、Phase 计划

### Phase 1 - 设计(本 PRD)

- [x] 编写 M1-主体默认银行账户规则 PRD v1.0
- [x] 字段设计 + 表结构 + 索引
- [x] 匹配算法 + 时序图
- [x] 验收标准 + 待评审问题

### Phase 2 - 实施

- [ ] **DB 设计**:`db/schema/25-default-bank-account-rule-v1.sql`
- [ ] **后端**:`basedata` 模块新增 `DefaultBankAccountRule` 实体 + Service + Controller
- [ ] **后端**:`DefaultBankAccountRuleMatchService` 匹配引擎实现
- [ ] **前端**:`DefaultBankAccountRuleList.vue` + `DefaultBankAccountRuleForm.vue`
- [ ] **前端**:FX 录入页(`FxDealForm.vue`)接入 match 接口
- [ ] **GlobalConstants**:新增 `Direction` 枚举(`Inflow`/`Outflow`/`ALL`)
- [ ] **测试**:`scripts/test/test_default_bank_account_rule_api.py`
- [ ] **测试**:`scripts/test/test_default_bank_account_rule_ui.py`

### Phase 3 - 增强 (后续)

- [ ] 交易对手默认银行账户 (Z1)
- [ ] 规则导入/导出 (Z2)
- [ ] 跨主体继承 (Z7)
- [ ] 规则变更审批流 (Z4)
- [ ] 多候选匹配(Q1 升级)

---

## 十二、相关文档

- `M3-外汇交易PRD.md` v3.2 — FX 录入架构 + Cashflow 生成机制
- `M1-资金管理主体PRD-v1.md` — 主体定义
- `docs/api/basedata/01-bank-accounts.md` v1.2 — 银行账户 API
- `CLAUDE.md` — 项目总规范
- `open-tms功能特性清单.md` — 业界对标参考
- `docs/规范/Open-TMS开发规范文档.md` — 字段命名/类型/审计规范

---

## 附录 A:DDL 草案

```sql
-- ==========================================
-- M1-主体默认银行账户规则 (v1.0)
-- 文件: db/schema/25-default-bank-account-rule-v1.sql
-- 作者: PM
-- 日期: 2026-07-05
-- ==========================================

CREATE TABLE tms_default_bank_account_rule_t (
    id                   BIGSERIAL       PRIMARY KEY,
    rule_number          VARCHAR(50)     NOT NULL UNIQUE,

    -- 5 维匹配维度
    management_entity_id BIGINT          NOT NULL,
    counterparty_id      BIGINT,
    instrument_id        BIGINT,
    direction            VARCHAR(20)     NOT NULL,
    currency             VARCHAR(10),

    -- 输出
    bank_account_id      BIGINT          NOT NULL,

    -- 标识/状态
    status               VARCHAR(20)     NOT NULL DEFAULT 'Active',
    priority             INT             NOT NULL DEFAULT 0,
    start_date           DATE,

    -- 描述
    description          VARCHAR(500),
    remark               VARCHAR(500),

    -- 审计
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    -- 约束
    CONSTRAINT chk_rule_direction CHECK (direction IN ('Inflow', 'Outflow', 'ALL')),
    CONSTRAINT chk_rule_status    CHECK (status IN ('Active', 'Inactive'))
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
COMMENT ON TABLE  tms_default_bank_account_rule_t IS '主体默认银行账户规则';
COMMENT ON COLUMN tms_default_bank_account_rule_t.rule_number          IS '规则编号,格式 RULEyyyyMMddxxxx';
COMMENT ON COLUMN tms_default_bank_account_rule_t.management_entity_id IS '主体(单选,不能 ALL)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.counterparty_id      IS '对手方(NULL=ALL 通配)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.instrument_id        IS '金融产品(NULL=ALL 通配)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.direction            IS '方向:Inflow/Outflow/ALL';
COMMENT ON COLUMN tms_default_bank_account_rule_t.currency             IS '币种(NULL=ALL 通配)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.bank_account_id      IS '默认银行账户(FK→tms_bank_account_t.id,且必须属于主体)';
COMMENT ON COLUMN tms_default_bank_account_rule_t.status               IS 'Active/Inactive';
COMMENT ON COLUMN tms_default_bank_account_rule_t.priority             IS '优先级,数字越大越优先';
COMMENT ON COLUMN tms_default_bank_account_rule_t.start_date           IS '开始生效日(NULL=立即生效)';
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

---

*PM产出 - M1 v1.0 (2026-07-05)*
*核心设计:5 维规则匹配 + 优先级排序 + ALL 通配 + 运行时匹配接口*
*本次只做主体维度,交易对手维度 P2+ 扩展(架构已预留)*