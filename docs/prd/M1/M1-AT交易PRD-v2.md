# Open-TMS M1-账户转账(AT) 交易 PRD

**版本**: v2.0（重大重构版）
**角色**: PM + UX 子代理
**日期**: 2026-06-21
**基于**: DealMap PRD v2.0（2026-06-21）+ AC 交易 PRD v2.0/v3.0
**状态**: v2.0 - 字段精简 + 双腿 DealMap 设计

---

## 〇、修订记录

### v2.0（2026-06-21）- 本次重大重构

| 修订项 | 修订内容 | 原因 |
|--------|---------|------|
| **设计理念升级** | 整体重构为 DealMap v2.0 设计（Action 多对一、DealMap 自动生成、Cashflow 反向关联） | 与 AC PRD v2.0/v3.0 保持一致 |
| **双腿 DealMap** | AT 触发 **2 TRANSFER + 2 CASHFLOW** 共 **4 条 DealMap**（非 AC 的 1 条） | AT 是双边业务：付出方 SOURCE + 收入方 DESTINATION |
| **字段精简** | 移除 transfer_channel / settlement_method / fee / authorization_required 等冗余字段 | 对齐 DealMap v2.0 字段精简原则 |
| **操作精简** | 只有 save / delete / approve / reject（无 submit / execute / retry） | 用户最新决策：v2.0 决策 #29 |
| **增加 transfer_type** | 新增 `transfer_type` 枚举：`SAME_COMPANY` / `CROSS_COMPANY` / `CROSS_BORDER` | AT 特有场景区分 |
| **增加 account_role 字段** | DealMap 新增 `account_role`（SOURCE / DESTINATION）以区分双腿 | AT 双账户需要明确标记 |
| **增加 exchange_rate** | 跨币种时启用 exchange_rate + dest_amount 自动计算 | 跨境/跨币种场景必需 |
| **移除 action 双腿** | 双腿体现在 DealMap 而非 Action：1 个 Action 触发 2 TRANSFER + 2 CASHFLOW DealMap | 保持 Action 单一语义 |
| **状态机简化** | 去掉 Validated / Authorized / Settlement In Process / Settled / Failed / Canceled 等复杂状态；保留 New / Pending / Approved / Deleted | 对齐 AC 状态机 |
| **编号规则** | 转账编号 = AT + yyyyMMdd + 4位序号（与 AC 保持一致） | 统一编号规则 |

### v1.x 历史（已被 v2.0 取代）

- v1.0（2026-04-06）：定义了 4 种转账类型（Intra/Inter/Domestic/International），复杂状态机（Validated/Authorized/Settling/Settled/Failed/Canceled），包含 submit/execute/retry/cancel 操作。**已被 v2.0 全面取代**。

---

## 一、模块概述

### 1.1 模块名称与定位

**模块名称**: AT - Account Transfer（账户转账交易）

**功能定位**: 管理企业银行账户之间的**双边**资金划转交易，记录转账的**付出方**与**收入方**双腿 DealMap 业务事件，支持同公司/跨公司/跨境/跨币种等多种场景。

**用户角色**: 资金管理人员、出纳、资金经理

**与 AC 的核心差异**: AC 是**单边**（一笔 Deal 一笔 Cashflow）；AT 是**双边**（一笔 Deal **两笔** Cashflow + 两笔 TRANSFER DealMap）。

### 1.2 业务场景

| 场景 | 示例 | 适用 transfer_type |
|------|------|------------------|
| 集团总部下拨资金给子公司 | 集团账户 → 子公司账户（同币种 CNY） | SAME_COMPANY（如果同一 BU） 或 CROSS_COMPANY（不同 BU） |
| 子公司之间资金调拨 | 子公司A账户 → 子公司B账户 | CROSS_COMPANY |
| 境内账户向境外账户汇款 | 境内人民币账户 → 境外美元账户 | CROSS_BORDER + 跨币种 |
| 同公司不同账户间调拨 | 集团总部分两个账户间划转 | SAME_COMPANY |
| 资金池归集/下拨 | 子公司账户 → 资金池主账户 | SAME/CROSS_COMPANY |

---

## 二、AT vs AC 差异对比表

| 维度 | AC（ActualCashflow） | AT（AccountTransfer） | 说明 |
|------|----------------------|----------------------|------|
| **业务性质** | 单边：与外部对手方的资金进出 | 双边：内部账户之间的资金划转 | AT 双账户是核心特征 |
| **账户数** | 1 个银行账户 + 1 个对手方账户 | **2 个银行账户**（source + dest） | 双账户是必要条件 |
| **交易对手** | 必须有对手方（外部） | **无对手方**（内部账户划转） | 内部账户不涉及外部对手方 |
| **Cashflow 数量** | 1 笔（Outflow 或 Inflow） | **2 笔**（1 笔 Outflow 付出 + 1 笔 Inflow 收入） | 双 Cashflow 是核心 |
| **DealMap 数量** | 1 条 ActualCashflow | **4 条**（2 TRANSFER + 2 CASHFLOW） | 双腿 DealMap 设计 |
| **币种** | 单币种 | 同币种 或 跨币种（exchange_rate） | AT 支持跨币种 |
| **方向字段** | 必填（Inflow/Outflow） | **不需要**（通过 source/dest 自动确定） | 方向由账户角色确定 |
| **跨境场景** | 不涉及 | 可能涉及（CROSS_BORDER） | AT 特有 |
| **account_role 字段** | 不需要 | **必需**（SOURCE/DESTINATION） | 用于标记双腿 |
| **Cashflow 关联** | 1 个 Cashflow 关联 1 个 DealMap | **2 个 Cashflow 各关联 1 个 TRANSFER DealMap** | 一一对应 |
| **业务实体** | tms_ac_deals_t | **tms_at_deals_t** | 不同子表 |
| **deal_type 字段** | 'AC' | **'AT'** | 区分交易类型 |
| **状态机** | New / Pending / Approved / Deleted | 同 AC | 对齐 |

### 双腿核心设计（AT 专属）

```
AT Deal（1 笔）
 ├─ Action(CREATE/UPDATE/DELETE) — 单一 Action
 ├─ source_account (付出方) ──┐
 │                            ├─ TRANSFER DealMap（2 条，方向对偶）
 │                            └─ CASHFLOW DealMap（2 条，方向对偶）
 └─ dest_account (收入方)  ──┘
```

---

## 三、功能清单

### 3.1 AT 交易管理

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 新建 AT 交易 | 录入双边转账信息 | P0 |
| 编辑 AT 交易 | 修改源账户/目标账户/金额/汇率等 | P0 |
| 删除 AT 交易 | 软删 AT Deal + 级联软删 DealMap/Cashflow | P0 |
| 查询 AT 交易 | 分页+多条件筛选 | P0 |
| 详情查看 | 查看双腿 DealMap 时间线 | P0 |
| 审批 AT 交易 | 基于 Action 的审批 | P0 |
| 驳回 AT 交易 | 驳回 Action | P0 |
| 批量导入 | Excel 批量导入 AT | P1 |
| 导出 AT | Excel 导出 | P1 |
| 复制 AT | 复制为新 AT | P2 |

### 3.2 特有功能（区别于 AC）

| 功能 | 说明 | 优先级 |
|------|------|--------|
| **同/跨公司识别** | 根据源账户与目标账户的 management_entity 自动判断 transfer_type | P0 |
| **同/跨币种识别** | 根据源账户与目标账户的 currency 自动判断是否启用 exchange_rate | P0 |
| **跨境标记** | 根据源账户与目标账户的 country_code 自动标记 transfer_type=CROSS_BORDER | P0 |
| **汇率录入** | 跨币种时录入 exchange_rate + 自动计算 dest_amount | P0 |
| **dest_amount 联动** | source_amount × exchange_rate = dest_amount（自动） | P0 |
| **双腿预览** | 创建前预览 4 条 DealMap 的展开效果 | P1 |

### 3.3 退款/撤销（后续阶段）

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 冲销 AT 交易 | 通过 DealMap 反向冲销 | P2（M2+） |

---

## 四、字段设计

### 4.1 主表：`tms_at_deals_t`（v2.0）

```sql
CREATE TABLE tms_at_deals_t (
    -- ============================================================
    -- 主键与编号
    -- ============================================================
    id                       BIGSERIAL       PRIMARY KEY,
    deal_number              VARCHAR(50)     NOT NULL UNIQUE,
    -- 编号格式: AT + yyyyMMdd + 4位序号，如 AT202606210001

    -- ============================================================
    -- 业务主体
    -- ============================================================
    management_entity            VARCHAR(50)     NOT NULL,
    -- 关联交易主体（管理主体 code，如 BU001 集团总部）

    -- ============================================================
    -- 转账类型（AT 特有）
    -- ============================================================
    transfer_type            VARCHAR(20)     NOT NULL,
    -- SAME_COMPANY: 同公司转账
    -- CROSS_COMPANY: 跨公司转账（不同 BU）
    -- CROSS_BORDER: 跨境转账（涉及不同国家）

    -- ============================================================
    -- 源账户（付出方）
    -- ============================================================
    source_account_id        BIGINT          NOT NULL,
    source_account_no        VARCHAR(50)     NOT NULL,
    source_amount            DECIMAL(38,18)  NOT NULL,
    source_currency          VARCHAR(10)     NOT NULL,

    -- ============================================================
    -- 目标账户（收入方）
    -- ============================================================
    dest_account_id          BIGINT          NOT NULL,
    dest_account_no          VARCHAR(50)     NOT NULL,
    dest_amount              DECIMAL(38,18),
    dest_currency            VARCHAR(10)     NOT NULL,

    -- ============================================================
    -- 汇率（跨币种时使用）
    -- ============================================================
    exchange_rate            DECIMAL(20,10),
    -- 当 source_currency = dest_currency 时为 1.0
    -- 跨币种时由用户输入或从币种对汇率表获取

    -- ============================================================
    -- 时间
    -- ============================================================
    value_date               DATE            NOT NULL,
    -- 起息日 / 预计到账日

    -- ============================================================
    -- 支付方式
    -- ============================================================
    payment_method           VARCHAR(20)     NOT NULL DEFAULT 'TRANSFER',
    -- TRANSFER: 同城/同行转账
    -- WIRE: 电汇
    -- RTGS: 实时全额结算
    -- INTERNAL: 内部账户调拨

    -- ============================================================
    -- 用途与备注
    -- ============================================================
    purpose                  VARCHAR(200),
    -- 转账用途描述

    -- ============================================================
    -- 状态
    -- ============================================================
    status                   VARCHAR(20)     NOT NULL DEFAULT 'New',
    -- New: 新建
    -- Pending: 待审批（有 Pending Action）
    -- Approved: 已审批（所有 Action Approved）
    -- Deleted: 已删除（deleted='1'）

    -- ============================================================
    -- 审计字段
    -- ============================================================
    created_by               VARCHAR(50)     NOT NULL,
    created_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               VARCHAR(50),
    updated_at               TIMESTAMP,
    version                  INT             NOT NULL DEFAULT 0,
    deleted                  CHAR(1)         NOT NULL DEFAULT '0',

    -- ============================================================
    -- 关联
    -- ============================================================
    latest_action_number     VARCHAR(50),
    -- 最近一次 Action 编号

    CONSTRAINT chk_transfer_type CHECK (
        transfer_type IN ('SAME_COMPANY', 'CROSS_COMPANY', 'CROSS_BORDER')
    ),
    CONSTRAINT chk_status CHECK (
        status IN ('New', 'Pending', 'Approved', 'Deleted')
    ),
    CONSTRAINT chk_payment_method CHECK (
        payment_method IN ('TRANSFER', 'WIRE', 'RTGS', 'INTERNAL')
    ),
    CONSTRAINT chk_diff_accounts CHECK (source_account_id <> dest_account_id)
);

-- 索引
CREATE INDEX idx_atd_management_entity      ON tms_at_deals_t(management_entity);
CREATE INDEX idx_atd_transfer_type      ON tms_at_deals_t(transfer_type);
CREATE INDEX idx_atd_source_account     ON tms_at_deals_t(source_account_id);
CREATE INDEX idx_atd_dest_account       ON tms_at_deals_t(dest_account_id);
CREATE INDEX idx_atd_value_date         ON tms_at_deals_t(value_date);
CREATE INDEX idx_atd_status             ON tms_at_deals_t(status);
CREATE INDEX idx_atd_deleted            ON tms_at_deals_t(deleted);
```

### 4.2 字段规范说明

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|---------|------|
| deal_number | VARCHAR(50) | 系统 | 系统生成（AT+yyyyMMdd+序号） | 唯一 |
| management_entity | VARCHAR(50) | Y | 管理主体必须启用 | 对齐 tms_management_entity_t |
| transfer_type | VARCHAR(20) | Y | 枚举 | 系统可自动识别 |
| source_account_id | BIGINT | Y | 必须存在且启用 | 银行账户 ID |
| source_account_no | VARCHAR(50) | Y | - | 银行账户编号 |
| source_amount | DECIMAL(38,18) | Y | > 0 | 付出方金额 |
| source_currency | VARCHAR(10) | Y | 3位 ISO 4217 | 付出方币种 |
| dest_account_id | BIGINT | Y | ≠ source_account_id | 银行账户 ID |
| dest_account_no | VARCHAR(50) | Y | - | 银行账户编号 |
| dest_amount | DECIMAL(38,18) | C（同币种可空，跨币种必填） | 跨币种时 = source_amount × exchange_rate | 收入方金额 |
| dest_currency | VARCHAR(10) | Y | 3位 ISO 4217 | 收入方币种 |
| exchange_rate | DECIMAL(20,10) | C（跨币种必填） | > 0 | 汇率 |
| value_date | DATE | Y | 默认为今天 | 起息日 |
| payment_method | VARCHAR(20) | Y | 默认 TRANSFER | 支付方式 |
| purpose | VARCHAR(200) | N | - | 转账用途 |
| status | VARCHAR(20) | 系统 | - | New/Pending/Approved/Deleted |
| latest_action_number | VARCHAR(50) | 系统 | - | 最新 Action 编号 |

### 4.3 tms_deals_t 中 AT 交易记录

```sql
-- tms_deals_t 增加一条记录（与 AT Deal 共享 deal_number）
INSERT INTO tms_deals_t (
    deal_number, deal_type, management_entity,
    amount, currency,
    deal_date, value_date, status, latest_action_number,
    created_by, created_at
) VALUES (
    'AT202606210001', 'AT', 'BU001',
    1000000.00, 'CNY',
    '2026-06-21', '2026-06-21', 'New', 'ACT202606210001',
    'zhangsan', NOW()
);
```

### 4.4 DealMap 表新增 `account_role` 字段

```sql
-- tms_deal_map_t 新增字段（AT 双腿必需）
ALTER TABLE tms_deal_map_t ADD COLUMN account_role VARCHAR(20);
COMMENT ON COLUMN tms_deal_map_t.account_role IS '账户角色：SOURCE 付出方 / DESTINATION 收入方（AT 双腿）';

-- event_type 扩展：增加 AccountTransfer
-- event_type: ActualCashflow / AccountTransfer / ...
```

### 4.5 v2.0 移除的字段（与 v1.0 对比）

| 移除字段 | 原用途 | 移除原因 |
|---------|--------|----------|
| ❌ transfer_channel | 转账通道（网银/银企直连/手工） | 不影响核心业务，由后台配置 |
| ❌ settlement_method | 结算方式 | 同上 |
| ❌ fee | 手续费 | 由计费模块单独管理 |
| ❌ authorization_required | 是否需要授权 | 审批基于 Action 自动判断 |
| ❌ transfer_subtype | 转账子类型 | 简化为 transfer_type 单一字段 |
| ❌ settlement_date | 实际结算日 | 简化为 value_date 单一时间 |
| ❌ priority | 优先级 | 不影响核心业务 |

---

## 五、业务流程图

### 5.1 AT 创建（保存）

```
用户填写 AT 表单 → 点击"保存"
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=CREATE, approval_status1=Pending) │
│    action_number=ACT202606210001                           │
│    deal_number=AT202606210001                              │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ② INSERT Deal(status=New)                                 │
│    deal_number=AT202606210001                              │
│    deal_type='AT'                                          │
│    latest_action_number=ACT202606210001                    │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ③ INSERT AtDeal                                           │
│    source_account_id, dest_account_id, exchange_rate, ... │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ④ ✅ INSERT DealMap(AccountTransfer, SOURCE) - 自动创建   │
│    dealmap_number=DMP202606210001                          │
│    deal_number=AT202606210001                              │
│    action_number=ACT202606210001                           │
│    event_type='AccountTransfer'                            │
│    account_role='SOURCE'                                   │
│    amount=source_amount, currency=source_currency           │
│    direction='Outflow'                                     │
│    event_status='Active'                                   │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑤ ✅ INSERT DealMap(AccountTransfer, DESTINATION) - 自动  │
│    dealmap_number=DMP202606210002                          │
│    event_type='AccountTransfer'                            │
│    account_role='DESTINATION'                              │
│    amount=dest_amount, currency=dest_currency              │
│    direction='Inflow'                                      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑥ ✅ INSERT DealMap(ActualCashflow, SOURCE) - 自动创建    │
│    dealmap_number=DMP202606210003                          │
│    event_type='ActualCashflow'                             │
│    account_role='SOURCE'                                   │
│    amount=source_amount, currency=source_currency           │
│    direction='Outflow'                                     │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑦ ✅ INSERT DealMap(ActualCashflow, DESTINATION) - 自动   │
│    dealmap_number=DMP202606210004                          │
│    event_type='ActualCashflow'                             │
│    account_role='DESTINATION'                              │
│    amount=dest_amount, currency=dest_currency              │
│    direction='Inflow'                                      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑧ ✅ INSERT 2 Cashflow - 自动创建（各关联一个 TRANSFER）   │
│    CF202606210001: account_role=SOURCE                     │
│       dealmap_number=DMP202606210001                       │
│       direction=Outflow, amount=source_amount              │
│    CF202606210002: account_role=DESTINATION                │
│       dealmap_number=DMP202606210002                       │
│       direction=Inflow, amount=dest_amount                 │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑨ ❌ 不生成 DealImage（v2.0 决策 #27）                     │
└──────────────────────────────────────────────────────────┘
```

**双腿 DealMap 总览**:

| 序号 | dealmap_number | event_type | account_role | direction | amount | currency |
|------|----------------|------------|--------------|-----------|--------|----------|
| 1 | DMP202606210001 | AccountTransfer | SOURCE | Outflow | 1,000,000.00 | CNY |
| 2 | DMP202606210002 | AccountTransfer | DESTINATION | Inflow | 1,000,000.00 | CNY |
| 3 | DMP202606210003 | ActualCashflow | SOURCE | Outflow | 1,000,000.00 | CNY |
| 4 | DMP202606210004 | ActualCashflow | DESTINATION | Inflow | 1,000,000.00 | CNY |

### 5.2 AT 修改（保存）

```
用户修改 AT → 点击"保存"
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=UPDATE)                       │
│    action_number=ACT202606210002                           │
│    deal_number=AT202606210001                              │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ② UPDATE Deal（修改字段）                                  │
│ ③ UPDATE AtDeal                                            │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ④ 软删除旧 4 条 DealMap                                     │
│    UPDATE tms_deal_map_t SET deleted='1'                   │
│    WHERE deal_number='AT202606210001' AND deleted='0'      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑤ ✅ INSERT 新 4 条 DealMap（关联新 Action）               │
│    DMP202606210005-008 (AccountTransfer + ActualCashflow)  │
│    × SOURCE + DESTINATION                                  │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑥ UPDATE 2 Cashflow（dealmap_number 指向新 DealMap）      │
│    CF...001 → dealmap_number=DMP202606210005               │
│    CF...002 → dealmap_number=DMP202606210006               │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑦ INSERT DealImage(v+1) - 记录修改前旧值                  │
└──────────────────────────────────────────────────────────┘
```

### 5.3 AT 删除

```
用户点击"删除" → 二次确认
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=DELETE)                       │
│ ② 软删除 Deal                                              │
│ ③ 软删除 AtDeal                                            │
│ ④ 软删除全部 DealMap（级联）                                │
│ ⑤ 软删除全部 Cashflow（级联）                               │
│ ⑥ INSERT DealImage(v+1)                                   │
└──────────────────────────────────────────────────────────┘
```

### 5.4 AT 审批（基于 Action）

```
审批界面展示该 Deal 的 Action 列表
    ↓
勾选 Action → 点击"审批通过"或"驳回"
    ↓
UPDATE tms_actions_t SET approval_status1='Approved'

⚠️ 审批不改变 DealMap / Cashflow 任何状态
```

### 5.5 AT 操作菜单（v2.0 决策 #29）

| 操作 | Action 类型 | DealMap 变化 | Cashflow 变化 | DealImage |
|------|------------|------------|--------------|-----------|
| **保存**（创建/修改） | INSERT Action(CREATE/UPDATE) | CREATE: 4 条 INSERT；UPDATE: 软删 4 条 + INSERT 4 条 | CREATE: 2 条 INSERT；UPDATE: 2 条 UPDATE dealmap_number | UPDATE/DELETE 时生成 |
| **删除** | INSERT Action(DELETE) | 软删 4 条 | 软删 2 条 | ✅ INSERT |
| **审批** | UPDATE Action.approval_status1 | ❌ 不变 | ❌ 不变 | ❌ 不变 |
| **驳回** | UPDATE Action.approval_status1=Rejected | ❌ 不变 | ❌ 不变 | ❌ 不变 |
| ~~提交~~ | - | - | - | - |
| ~~执行~~ | - | - | - | - |
| ~~重试~~ | - | - | - | - |
| ~~取消~~ | - | - | - | - |

---

## 六、AT 特有场景矩阵

### 6.1 按 transfer_type 分类

| transfer_type | 业务描述 | transfer_type 判定逻辑 |
|---------------|---------|---------------------|
| **SAME_COMPANY** | 同公司内账户转账 | source_account.management_entity = dest_account.management_entity |
| **CROSS_COMPANY** | 跨公司账户转账（不同 BU） | source_account.management_entity ≠ dest_account.management_entity |
| **CROSS_BORDER** | 跨境转账 | source_account.country ≠ dest_account.country |

### 6.2 按币种分类

| 场景 | source_currency | dest_currency | exchange_rate | dest_amount |
|------|----------------|---------------|---------------|-------------|
| **同币种** | CNY | CNY | 1.0（自动） | = source_amount |
| **跨币种** | CNY | USD | 用户输入 | = source_amount × exchange_rate |
| **跨币种反向** | USD | CNY | 用户输入 | = source_amount × exchange_rate |

### 6.3 场景矩阵（4×2 = 8 种组合）

| 序号 | transfer_type | 币种 | 业务示例 |
|------|---------------|------|---------|
| 1 | SAME_COMPANY | 同币种 | 集团总部账户 A → 集团总部账户 B（CNY → CNY） |
| 2 | SAME_COMPANY | 跨币种 | 集团总部 CNY 账户 → 集团总部 USD 账户 |
| 3 | CROSS_COMPANY | 同币种 | 集团总部 → 子公司（CNY → CNY） |
| 4 | CROSS_COMPANY | 跨币种 | 集团总部 CNY 账户 → 子公司 USD 账户 |
| 5 | CROSS_BORDER | 同币种 | 境内美元账户 → 境外美元账户（USD → USD） |
| 6 | CROSS_BORDER | 跨币种 | 境内 CNY 账户 → 境外 USD 账户（典型跨境） |
| 7 | CROSS_BORDER（含公司变更） | 同币种 | 境内子公司 → 境外子公司（USD → USD） |
| 8 | CROSS_BORDER（含公司变更） | 跨币种 | 境内子公司 CNY → 境外子公司 USD |

### 6.4 业务规则约束

| 规则 | 说明 | 实现位置 |
|------|------|---------|
| 账户不同 | source_account_id ≠ dest_account_id | 数据库 CHECK 约束 |
| 跨币种汇率必填 | source_currency ≠ dest_currency 时 exchange_rate 必填 | 前端 + 后端校验 |
| 同币种汇率=1 | source_currency = dest_currency 时 exchange_rate=1.0 | 后端自动设置 |
| dest_amount 计算 | dest_amount = source_amount × exchange_rate | 前端实时计算 + 后端校验 |
| 跨境业务必填 SWIFT | 跨境时需要 SWIFT code（M1+ 暂留空，M2+ 引入） | 预留字段 |
| 跨公司业务记录对方 BU | dest_account.management_entity 与 source_account.management_entity 不一致 | 自动记录 |

---

## 七、DealMap 双腿事件设计

### 7.1 双腿设计原理

AT 业务的本质是"一笔资金从 A 账户流向 B 账户"，这一过程在 DealMap 模型中需要被**同时记录**为 4 个不同维度的事件：

| 维度 | AccountTransfer | ActualCashflow | 数量 |
|------|----------------|----------------|------|
| **业务语义** | 账户间资金划转事件 | 实际现金流事件 | - |
| **业务作用** | 记录转账的发生 | 记录现金的流入/流出 | - |
| **双腿** | SOURCE + DESTINATION | SOURCE + DESTINATION | 2+2=4 |

**为什么需要 2 种 event_type**：
- **AccountTransfer**：表达"这是账户间的资金划转"，是 AT 业务的**业务事件**
- **ActualCashflow**：表达"这笔资金产生了实际现金流"，是**会计/资金头寸**事件
- 两者记录的是同一笔业务的不同视角，缺一不可

### 7.2 双腿 DealMap 关系图

```
                   AT Deal: AT202606210001
                              │
                              │ 1 Action(ACT202606210001)
                              ▼
        ┌─────────────────────────────────────────┐
        │       4 条 DealMap（双腿对称）          │
        │                                         │
        │   SOURCE 腿               DESTINATION 腿 │
        │   ┌──────┐                ┌──────┐      │
        │   │ DMP1 │  AccountTransfer(Outflow)    │
        │   └──────┘                └──────┘      │
        │   ┌──────┐                ┌──────┐      │
        │   │ DMP3 │  ActualCashflow(Outflow)     │
        │   └──────┘                └──────┘      │
        │                                         │
        │   Cashflow 关联：                       │
        │   ┌──────┐                ┌──────┐      │
        │   │ CF1  │ ←DMP1          │ CF2  │←DMP2 │
        │   └──────┘                └──────┘      │
        └─────────────────────────────────────────┘
```

### 7.3 详细字段填充示例

**AT 同币种场景（CNY → CNY）**：

```sql
-- TRANSFER SOURCE
INSERT INTO tms_deal_map_t (
    dealmap_number, deal_number, action_number, event_type, event_status,
    account_role, amount, currency, direction, event_date, value_date,
    description, created_by, created_at
) VALUES (
    'DMP202606210001', 'AT202606210001', 'ACT202606210001',
    'AccountTransfer', 'Active',
    'SOURCE', 1000000.00, 'CNY', 'Outflow', '2026-06-21', '2026-06-21',
    'AT 资金划出 - 中行北京 → 招行上海', 'zhangsan', NOW()
);

-- TRANSFER DESTINATION
INSERT INTO tms_deal_map_t (
    dealmap_number, deal_number, action_number, event_type, event_status,
    account_role, amount, currency, direction, event_date, value_date,
    description, created_by, created_at
) VALUES (
    'DMP202606210002', 'AT202606210001', 'ACT202606210001',
    'AccountTransfer', 'Active',
    'DESTINATION', 1000000.00, 'CNY', 'Inflow', '2026-06-21', '2026-06-21',
    'AT 资金划入 - 中行北京 → 招行上海', 'zhangsan', NOW()
);

-- ActualCashflow SOURCE
INSERT INTO tms_deal_map_t (
    dealmap_number, deal_number, action_number, event_type, event_status,
    account_role, amount, currency, direction, event_date, value_date,
    description, created_by, created_at
) VALUES (
    'DMP202606210003', 'AT202606210001', 'ACT202606210001',
    'ActualCashflow', 'Active',
    'SOURCE', 1000000.00, 'CNY', 'Outflow', '2026-06-21', '2026-06-21',
    'AT 现金流流出', 'zhangsan', NOW()
);

-- ActualCashflow DESTINATION
INSERT INTO tms_deal_map_t (
    dealmap_number, deal_number, action_number, event_type, event_status,
    account_role, amount, currency, direction, event_date, value_date,
    description, created_by, created_at
) VALUES (
    'DMP202606210004', 'AT202606210001', 'ACT202606210001',
    'ActualCashflow', 'Active',
    'DESTINATION', 1000000.00, 'CNY', 'Inflow', '2026-06-21', '2026-06-21',
    'AT 现金流流入', 'zhangsan', NOW()
);
```

**AT 跨币种场景（CNY 1000000 → USD 138000，汇率 0.138）**：

| DealMap | event_type | account_role | amount | currency | direction |
|--------|------------|--------------|--------|----------|-----------|
| DMP...005 | AccountTransfer | SOURCE | 1,000,000.00 | CNY | Outflow |
| DMP...006 | AccountTransfer | DESTINATION | 138,000.00 | USD | Inflow |
| DMP...007 | ActualCashflow | SOURCE | 1,000,000.00 | CNY | Outflow |
| DMP...008 | ActualCashflow | DESTINATION | 138,000.00 | USD | Inflow |

### 7.4 Cashflow 关联规则

```sql
-- CF1: 关联 SOURCE TRANSFER DealMap
INSERT INTO tms_cashflow_t (
    cflow_number, deal_number, management_entity,
    bank_account, direction, amount, currency,
    cflow_date, value_date, source_type, source_ref, status,
    dealmap_number, account_role,
    created_by, created_at
) VALUES (
    'CF202606210001', 'AT202606210001', 'BU001',
    'BANK_ACC_201', 'Outflow', 1000000.00, 'CNY',
    '2026-06-21', '2026-06-21', 'AT_DEAL', 'AT202606210001', 'Created',
    'DMP202606210001', 'SOURCE',
    'zhangsan', NOW()
);

-- CF2: 关联 DESTINATION TRANSFER DealMap
INSERT INTO tms_cashflow_t (
    cflow_number, deal_number, management_entity,
    bank_account, direction, amount, currency,
    cflow_date, value_date, source_type, source_ref, status,
    dealmap_number, account_role,
    created_by, created_at
) VALUES (
    'CF202606210002', 'AT202606210001', 'BU001',
    'BANK_ACC_202', 'Inflow', 1000000.00, 'CNY',
    '2026-06-21', '2026-06-21', 'AT_DEAL', 'AT202606210001', 'Created',
    'DMP202606210002', 'DESTINATION',
    'zhangsan', NOW()
);
```

**注意**：Cashflow 只关联 **AccountTransfer** DealMap（不关联 ActualCashflow），保持 Cashflow 与账户转账的一对一对应。

---

## 八、业务规则

### 8.1 编号规则

| 对象 | 编号格式 | 示例 |
|------|---------|------|
| 转账编号 | AT + yyyyMMdd + 4位序号 | AT202606210001 |
| Action 编号 | ACT + yyyyMMdd + 4位序号 | ACT202606210001 |
| DealMap 编号 | DMP + yyyyMMdd + 4位序号 | DMP202606210001 |
| Cashflow 编号 | CF + yyyyMMdd + 4位序号 | CF202606210001 |

### 8.2 字段校验规则

| 规则 | 说明 | 校验位置 |
|------|------|---------|
| 源账户 ≠ 目标账户 | source_account_id ≠ dest_account_id | 数据库 + 前端 |
| 跨币种必填汇率 | source_currency ≠ dest_currency → exchange_rate 必填 | 前端 + 后端 |
| 同币种汇率=1.0 | source_currency = dest_currency → exchange_rate=1.0 | 后端自动 |
| dest_amount = source_amount × exchange_rate | 自动计算并校验 | 后端 |
| 金额 > 0 | amount > 0 | 前端 + 后端 |
| 起息日合法 | value_date 不能早于今天 | 前端 |
| 管理主体必填 | management_entity 必填 | 前端 |

### 8.3 transfer_type 自动识别规则

```java
// 服务端自动识别 transfer_type
public String detectTransferType(BankAccount source, BankAccount dest) {
    // 跨境优先
    if (!source.getCountryCode().equals(dest.getCountryCode())) {
        return "CROSS_BORDER";
    }
    // 跨公司
    if (!source.getManagementEntity().equals(dest.getManagementEntity())) {
        return "CROSS_COMPANY";
    }
    // 同公司
    return "SAME_COMPANY";
}
```

### 8.4 状态流转规则

```
New (新建) ──→ Pending (待审批) ──→ Approved (已审批)
   ↑                                       │
   │                                       ↓
   └─────────── (重新编辑) ──────── Deleted (已删除)
```

| 触发 | 状态变化 |
|------|---------|
| 创建 AT | status=New |
| 创建 AT（自动） | status=Pending（因为 Action.approval_status1=Pending） |
| 审批通过所有 Action | status=Approved |
| 删除 AT | status=Deleted |
| 修改已删除的 AT | 不允许 |

### 8.5 操作权限规则

| 操作 | 权限 |
|------|------|
| 创建 AT | 资金管理人员 |
| 编辑 AT | 创建人或资金经理 |
| 删除 AT | 资金经理（仅限 New 状态） |
| 审批 AT | 一级/二级审批人（不能与创建人相同） |
| 驳回 AT | 一级/二级审批人 |

### 8.6 与 AC 的边界规则

| 业务 | 使用 AC 还是 AT |
|------|---------------|
| 公司向外部对手方付款 | **AC**（单边现金流 + 对手方账户） |
| 公司从外部对手方收款 | **AC** |
| 公司内部账户间调拨 | **AT**（双边转账） |
| 集团下拨资金给子公司 | **AT**（如果两边都是公司内部账户） |
| 子公司向银行还款 | **AC**（银行为外部对手方） |

---

## 九、验收标准

### 9.1 v2.0 核心验收点

| 功能 | 验收条件 |
|------|----------|
| **AT 双腿 DealMap 自动生成** | AT 创建后立即生成 **4 条** DealMap（2 TRANSFER + 2 CASHFLOW）+ **2 条** Cashflow |
| **account_role 字段正确填充** | 4 条 DealMap 中 SOURCE/DESTINATION 各 2 条（对称） |
| **Cashflow 通过 dealmap_number 关联** | Cashflow.dealmap_number 指向 AccountTransfer DealMap（不是 ActualCashflow） |
| **transfer_type 自动识别** | 系统根据源/目标账户自动判断 SAME_COMPANY/CROSS_COMPANY/CROSS_BORDER |
| **跨币种汇率联动** | 跨币种时 exchange_rate 必填；dest_amount = source_amount × exchange_rate 自动计算 |
| **同币种汇率默认 1.0** | source_currency = dest_currency 时 exchange_rate 自动设为 1.0 |
| **CREATE 不生成 DealImage** | AT 创建后查询 tms_deals_image_t 无新增记录 |
| **UPDATE 软删+新建 4 条 DealMap** | AT 修改时旧 4 条 DealMap.deleted='1'，新建 4 条 DealMap 关联新 Action |
| **DELETE 级联软删 4 条 DealMap + 2 条 Cashflow** | AT 删除时 Deal/AtDeal/DealMap(4)/Cashflow(2) 全部 deleted='1' |
| **审批不影响 DealMap/Cashflow** | Action.approval_status1 变更后，DealMap.event_status 与 Cashflow.status 均不变 |
| **AT 操作菜单** | 只有 save / delete / approve / reject；无 submit / execute / retry |
| **Action 多对一** | 一笔 AT Deal 可有多个 Action（CREATE + UPDATE + APPROVE 等）独立存在 |
| **源账户 ≠ 目标账户** | 数据库 CHECK 约束阻止相同账户转账 |

### 9.2 场景验证

| 场景 | 验证点 |
|------|--------|
| 同公司同币种 | 4 条 DealMap 全部 CNY，汇率=1.0，dest_amount=source_amount |
| 同公司跨币种 | 4 条 DealMap 源/目标币种不同，dest_amount 正确计算 |
| 跨公司同币种 | transfer_type=CROSS_COMPANY；4 条 DealMap 业务主体可能不同 |
| 跨公司跨币种 | transfer_type=CROSS_COMPANY；汇率联动正确 |
| 跨境同币种 | transfer_type=CROSS_BORDER；4 条 DealMap 国家不同 |
| 跨境跨币种 | transfer_type=CROSS_BORDER；典型外汇兑换 |
| 修改金额 | 软删旧 4 条 DealMap；新建 4 条 DealMap；Cashflow 重新指向 |
| 修改账户 | 双账户变化时 4 条 DealMap 全部更新 |
| 删除 AT | 级联软删全部 DealMap + Cashflow |

### 9.3 兼容性验收

| 项 | 验收 |
|----|------|
| 与 AC 的一致性 | 状态机、Action 设计、DealMap 自动生成逻辑与 AC 完全一致 |
| 与 DealMap PRD v2.0 一致 | 严格遵循 DealMap v2.0 字段精简 + Action 多对一 + Cashflow 反向关联 |
| API 路径规范 | `/api/v1/dealing/at-deals` 对齐 `/api/v1/{module}/{resource}` 规范 |
| 表名规范 | `tms_at_deals_t` 对齐 `tms_{module}_{type}_t` 规范 |

---

## 十、API 清单

### 10.1 AT 交易 CRUD

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/at-deals/page` | GET | 分页查询 AT 交易 |
| `/api/v1/dealing/at-deals/{id}` | GET | 获取 AT 详情 |
| `/api/v1/dealing/at-deals/by-deal-number/{dealNumber}` | GET | 通过 deal_number 查询 |
| `/api/v1/dealing/at-deals` | POST | 创建 AT（触发 4 DealMap + 2 Cashflow 自动生成） |
| `/api/v1/dealing/at-deals/update` | POST | 更新 AT（软删旧 4 DealMap + 新建 4 DealMap + 2 Cashflow 重新指向） |
| `/api/v1/dealing/at-deals/delete/{id}` | POST | 删除 AT（级联软删） |
| `/api/v1/dealing/at-deals/export` | GET | 导出 AT |
| `/api/v1/dealing/at-deals/import` | POST | 批量导入 AT |

### 10.2 AT 关联查询

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/at-deals/{dealNumber}/dealmaps` | GET | 查询该 AT 的 4 条 DealMap |
| `/api/v1/dealing/at-deals/{dealNumber}/cashflows` | GET | 查询该 AT 的 2 条 Cashflow |
| `/api/v1/dealing/at-deals/{dealNumber}/actions` | GET | 查询该 AT 的所有 Action |
| `/api/v1/dealing/at-deals/detect-transfer-type` | POST | 自动识别 transfer_type |

### 10.3 AT 审批（与 AC 共享 Action 接口）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/actions/by-deal/{dealNumber}` | GET | 查询该 AT 的所有 Action |
| `/api/v1/dealing/actions/{actionNumber}/approve` | POST | 审批通过 |
| `/api/v1/dealing/actions/{actionNumber}/reject` | POST | 驳回 |
| `/api/v1/dealing/actions/pending` | GET | 待我审批的 Action 列表 |

### 10.4 请求/响应示例

**创建 AT（POST /api/v1/dealing/at-deals）**：

```json
{
  "managementEntity": "BU001",
  "transferType": "CROSS_COMPANY",
  "sourceAccountId": 201,
  "sourceAccountNo": "BANK_ACC_201",
  "sourceAmount": 1000000.00,
  "sourceCurrency": "CNY",
  "destAccountId": 202,
  "destAccountNo": "BANK_ACC_202",
  "destAmount": 1000000.00,
  "destCurrency": "CNY",
  "exchangeRate": 1.0,
  "valueDate": "2026-06-21",
  "paymentMethod": "TRANSFER",
  "purpose": "集团下拨子公司运营资金"
}
```

**创建 AT 成功响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dealNumber": "AT202606210001",
    "actionNumber": "ACT202606210001",
    "dealMaps": [
      {"dealmapNumber": "DMP202606210001", "eventType": "AccountTransfer", "accountRole": "SOURCE", "amount": 1000000.00, "currency": "CNY"},
      {"dealmapNumber": "DMP202606210002", "eventType": "AccountTransfer", "accountRole": "DESTINATION", "amount": 1000000.00, "currency": "CNY"},
      {"dealmapNumber": "DMP202606210003", "eventType": "ActualCashflow", "accountRole": "SOURCE", "amount": 1000000.00, "currency": "CNY"},
      {"dealmapNumber": "DMP202606210004", "eventType": "ActualCashflow", "accountRole": "DESTINATION", "amount": 1000000.00, "currency": "CNY"}
    ],
    "cashflows": [
      {"cflowNumber": "CF202606210001", "accountRole": "SOURCE", "dealmapNumber": "DMP202606210001"},
      {"cflowNumber": "CF202606210002", "accountRole": "DESTINATION", "dealmapNumber": "DMP202606210002"}
    ]
  },
  "timestamp": 1704067200000
}
```

---

## 十一、决策记录

### v2.0 决策（2026-06-21）

| # | 决策项 | 决策 | 决策依据 |
|---|--------|------|---------|
| 31 | AT 双腿 DealMap 设计 | 1 笔 AT 触发 2 TRANSFER + 2 CASHFLOW 共 4 条 DealMap | AT 业务本质是双边资金划转 |
| 32 | account_role 字段 | DealMap 增加 account_role（SOURCE/DESTINATION） | 区分双腿 |
| 33 | transfer_type 三分类 | SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER | 业务场景足够覆盖 |
| 34 | 跨币种汇率联动 | source_amount × exchange_rate = dest_amount 自动计算 | 跨境业务必需 |
| 35 | 状态机简化 | New / Pending / Approved / Deleted 四态 | 对齐 AC 简化 |
| 36 | 操作精简 | save / delete / approve / reject | 沿用 v2.0 决策 #29 |
| 37 | tms_at_deals_t 子表 | AT 单独建表 tms_at_deals_t | 与 AcDeal 对齐 |
| 38 | Cashflow 关联 TRANSFER | Cashflow 只关联 AccountTransfer DealMap（不关联 ActualCashflow） | 保持 Cashflow 与账户一一对应 |
| 39 | dest_amount 联动计算 | 跨币种时后端校验 dest_amount = source_amount × exchange_rate | 防止数据不一致 |
| 40 | transfer_type 自动识别 | 系统按 management_entity + country 自动判断 | 减少用户输入 |

### v1.0 历史决策（已被 v2.0 取代）

| # | 决策项 | 状态 |
|---|--------|------|
| 1-10 | v1.0 全部决策 | ⚠️ 大部分被 v2.0 推翻 |

---

## 十二、后续工作计划

### Phase 1（M1 阶段）

- ✅ v2.0 PRD 设计完成（含双腿 DealMap 重构）
- ⏳ 数据库改造：
  - 新增 tms_at_deals_t 表
  - tms_deal_map_t 增加 account_role 字段
  - tms_cashflow_t 增加 account_role 字段
- ⏳ 后端 Service 改造：
  - AtDealServiceImpl.createAtDeal() 自动生成 4 DealMap + 2 Cashflow
  - AtDealServiceImpl.updateAtDeal() 软删旧 4 DealMap + 新建 4 DealMap
  - AtDealServiceImpl.deleteAtDeal() 级联软删
  - AtDealServiceImpl.detectTransferType() 自动识别
- ⏳ 测试用例：
  - 验证双腿 DealMap 自动生成
  - 验证 4 条 DealMap 的 account_role 对称
  - 验证跨币种汇率联动
  - 验证 8 种场景矩阵
- ⏳ UX 原型（本文档配套）

### Phase 2（M2+ 阶段）

- ⏳ AT 冲销流程（基于 DealMap reverses_event_id）
- ⏳ SWIFT code 集成
- ⏳ AT 资金头寸联动
- ⏳ 跨公司转账的会计过账

---

*PM + UX 子代理产出 - M1 v2.0 (2026-06-21)*
*核心设计：AT 双腿 DealMap（2 TRANSFER + 2 CASHFLOW）+ transfer_type 三分类 + 跨币种汇率联动*
*严格遵循 DealMap PRD v2.0 设计理念*
