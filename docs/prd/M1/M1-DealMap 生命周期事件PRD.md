# Open-TMS M1-DealMap（交易生命周期事件）PRD

**版本**: v2.0
**角色**: 业务架构师 + 产品经理 (BA + PM)
**日期**: 2026-06-21（重大重构）
**状态**: v2.0 - 字段精简 + 流程重构 + Action 多对一

---

## 〇、修订记录

### v2.0（2026-06-21）- 本次重大重构

| 修订项 | 修订内容 |
|--------|----------|
| **DealMap 字段精简** | 从 50+ 字段精简到 25 字段；移除 event_category / subtype / event_timing / trigger_source / action_id / deal_id / cflow_id / bank_account 等 |
| **Action 多对一** | 移除 Action 表 deal_number UNIQUE 约束；一笔 Deal 可有多个独立 Action（CREATE/UPDATE/DELETE/APPROVE/REJECT） |
| **Cashflow 反向关联** | Cashflow 表新增 `dealmap_number VARCHAR(50)` 字段（非 FK，仅字符串引用） |
| **DealMap 自动生成** | 交易创建后自动创建 DealMap + Cashflow；DealMap 无审批相关状态字段 |
| **DealImage CREATE 不生成** | 创建时不生成 DealImage；修改/删除仍生成 |
| **AC/AT 操作精简** | 只有 save / delete / approve / reject（无 submit / execute） |
| **审批基于 Action** | 审批作用于 Action，**不改变** DealMap / Cashflow 任何状态 |
| **DealMap 修改软删+新建** | 修改时软删除旧 DealMap，新建一条新 DealMap 关联新 Action |

### v1.x 历史（已被 v2.0 取代）

- v1.2：表结构补充业务维度字段 + 完善 AC/AT 事件模板
- v1.1：精简事件分类、冲销机制、Action 触发机制

---

## 一、背景与定位

### 1.1 背景

Open-TMS 已完成 Deal（交易）、Action（操作/审批对象）、DealImage（镜像快照）三件套设计。**DealMap** 用于记录交易生命周期中的业务事件（如实际现金流、账户转账、平仓、利率确定等），是 Deal / Action / DealImage 之外的重要补充。

### 1.2 DealMap 与现有概念的关系

| 概念 | 与 DealMap 的关系 | 说明 |
|------|------------------|------|
| **Deal** | DealMap 的**父对象**（1:N） | 一笔 Deal 可拥有多个 DealMap 事件 |
| **Action** | DealMap 的**触发源**（1:1） | 每个 DealMap 由一个 Action 触发；DealMap 通过 `action_number` 关联 Action |
| **DealImage** | **互补关系** | DealImage 记录字段历史快照（CREATE 不生成，UPDATE/DELETE 生成） |
| **Cashflow** | DealMap 的**关联下游**（1:1，可空） | Cashflow 表存 `dealmap_number` 字段（字符串引用），标识生成的 DealMap |

**核心区别**：
- **Action**：记录对交易的动作，是审批的操作对象
- **DealImage**：字段级历史快照（CREATE 不生成）
- **DealMap**：记录交易产生的业务事件（自动生成，无审批状态字段）

---

## 二、DealMap 核心设计原则

1. **核心字段精简**：DealMap 仅保留核心 4 字段（dealmap_number / deal_number / action_number / event_type）+ 必要辅助字段
2. **自动生成**：交易创建后**自动**创建 DealMap + Cashflow（无需业务触发）
3. **Action 多对一**：一笔 Deal 可有多个独立 Action（CREATE / UPDATE / DELETE / APPROVE / REJECT）
4. **无审批状态**：DealMap / Cashflow **无审批相关字段**；审批仅作用于 Action
5. **CRUD 触发 DealMap 变化**：
   - CREATE：自动创建 DealMap
   - UPDATE：软删除旧 DealMap + 创建新 DealMap
   - DELETE：软删除 Deal + 级联软删除 DealMap
6. **镜像差异化**：CREATE 不生成 DealImage；UPDATE / DELETE 生成 DealImage
7. **冲销支持**：每个 DealMap 支持反向冲销（is_reversal + reverses_event_id）
8. **交易类型无关**：AC / AT / FX / IRS / Deposit / Loan 等共享同一 DealMap 模型

---

## 三、DealMap 事件类型

### 3.1 事件类型清单（扁平化，无 category/subtype）

| event_type | 说明 | 适用交易 |
|-----------|------|----------|
| **ActualCashflow** | 实际现金流（资金已发生实际进出） | AC / AT / FX / Deposit / Loan / IRS / Bond |
| **ExpectedCashflow** | 预期现金流（用于头寸预测） | Deposit / Loan / Bond / IRS |
| **AccountTransfer** | 账户间资金划转 | AT |
| **CashLeveling** | 资金池归集与回拨 | AT / Cashpool |
| **RateSet** | 利率设定（合同生效时） | Deposit / Loan / IRS / Bond |
| **RateFix** | 利率重置/确定 | Loan / IRS / FloatingBond |
| **Coupon** | 付息事件 | Bond / IRS / Deposit |
| **InterestAccrual** | 利息计提 | Deposit / Loan / Bond |
| **Unwind** | 平仓/解除 | FX / IRS / Option / Swap |
| **Rollover** | 续作 | Deposit / Loan |
| **Exercise** | 行权/到期 | Option |
| **MTM** | 市值评估 | FX / IRS / Bond / Option / Swap |
| **CreditEvent** | 信用事件 | 全部 |
| **AccountingPost** | 会计过账（M2+） | 全部 |

---

## 四、DealMap 表结构设计

### 4.1 主表：`tms_deal_map_t`（v2.0 精简版）

```sql
CREATE TABLE tms_deal_map_t (
    -- ============================================================
    -- 主键与编号
    -- ============================================================
    id                       BIGSERIAL       PRIMARY KEY,
    dealmap_number           VARCHAR(50)     NOT NULL UNIQUE,
    -- 编号格式: DMP + yyyyMMdd + 4位序号，如 DMP202606210001

    -- ============================================================
    -- 关联（核心 4 字段之一）
    -- ============================================================
    deal_number              VARCHAR(50)     NOT NULL,  -- 关联交易编号
    action_number            VARCHAR(50)     NOT NULL,  -- 触发此 DealMap 的 Action 编号

    -- ============================================================
    -- 事件类型（核心 4 字段之一）
    -- ============================================================
    event_type               VARCHAR(30)     NOT NULL,
    -- ActualCashflow / AccountTransfer / Unwind / RateFix / Coupon / ...

    -- ============================================================
    -- 事件状态（仅 Active/Inactive，无审批相关）
    -- ============================================================
    event_status             VARCHAR(20)     NOT NULL DEFAULT 'Active',
    -- Active / Inactive（被冲销后变 Inactive）

    -- ============================================================
    -- 金融字段（按 event_type 选择性填充）
    -- ============================================================
    amount                   DECIMAL(38,18),
    currency                 VARCHAR(10),
    direction                VARCHAR(10),   -- Inflow / Outflow / Pay / Receive

    -- ============================================================
    -- 时间字段
    -- ============================================================
    event_date               DATE            NOT NULL,
    value_date               DATE,

    -- ============================================================
    -- 冲销关系
    -- ============================================================
    is_reversal              CHAR(1)         NOT NULL DEFAULT '0',
    reverses_event_id        BIGINT,          -- 冲销原事件
    reversed_by_event_id     BIGINT,          -- 被哪个事件冲销

    -- ============================================================
    -- 描述
    -- ============================================================
    description              VARCHAR(500),

    -- ============================================================
    -- 审计字段（项目统一规范）
    -- ============================================================
    created_by               VARCHAR(50)     NOT NULL,
    created_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               VARCHAR(50),
    updated_at               TIMESTAMP,
    version                  INT             NOT NULL DEFAULT 0,
    deleted                  CHAR(1)         NOT NULL DEFAULT '0',

    CONSTRAINT chk_event_status CHECK (event_status IN ('Active', 'Inactive'))
);

-- 索引
CREATE INDEX idx_dm_deal              ON tms_deal_map_t(deal_number);
CREATE INDEX idx_dm_action            ON tms_deal_map_t(action_number);
CREATE INDEX idx_dm_event_type        ON tms_deal_map_t(event_type);
CREATE INDEX idx_dm_event_date        ON tms_deal_map_t(event_date);
CREATE INDEX idx_dm_deleted          ON tms_deal_map_t(deleted);
```

### 4.2 v2.0 移除的字段（对比 v1.2）

| 移除字段 | 原用途 | 移除原因 |
|---------|--------|----------|
| ❌ event_category | 事件大类分类 | 合并到 event_type（扁平化） |
| ❌ event_subtype | 事件子类型 | 不再细分 |
| ❌ event_timing | PAST/PRESENT/FUTURE | DealMap 只记录业务事件，无需时间维度区分 |
| ❌ trigger_source | MANUAL/SYSTEM/BANK/... | 由 action 触发，无需额外标记 |
| ❌ action_id | Action 外键 | 保留 action_number（VARCHAR）即可 |
| ❌ deal_id | Deal 外键 | 保留 deal_number（VARCHAR）即可 |
| ❌ cflow_id | Cashflow 外键 | Cashflow 上存 dealmap_number（反向关联） |
| ❌ sflow_id, sflow_number | Stockflow 关联 | 暂不需要 |
| ❌ bank_account_id, counterparty_account_id | 账户信息 | 通过 Deal / AcDeal 反查 |
| ❌ counterparty_id | 对手方 ID | 通过 Deal 反查 |
| ❌ instrument_id | 金融工具 | 通过 Deal 反查 |
| ❌ rate | 利率/汇率 | 通过 Deal / Instrument 反查 |
| ❌ quantity, price, security_code | 证券字段 | 通过 Deal 反查 |
| ❌ business_unit, product_type, account_role | 业务维度 | 通过 Deal 反查 |
| ❌ event_time | 时间戳 | 简化（保留 event_date） |
| ❌ business_date | 业务日期 | 简化 |
| ❌ remark | 备注 | 合并到 description |
| ❌ trigger_ref | 触发引用 | 简化 |

---

## 五、Cashflow 表结构调整

### 5.1 新增字段

```sql
-- tms_cashflow_t 表新增字段（v2.0）
ALTER TABLE tms_cashflow_t ADD COLUMN dealmap_number VARCHAR(50);
COMMENT ON COLUMN tms_cashflow_t.dealmap_number IS '关联的 DealMap 编号（字符串引用，非外键）';
CREATE INDEX idx_cflow_dealmap_number ON tms_cashflow_t(dealmap_number);
```

**说明**：Cashflow 通过 `dealmap_number` 字段（VARCHAR）反向关联 DealMap，不使用外键约束。

---

## 六、Action 表结构调整

### 6.1 允许多个 Action / Deal

```sql
-- 移除 UNIQUE 约束
ALTER TABLE tms_actions_t DROP CONSTRAINT IF EXISTS unique_action_deal;
-- deal_number 不再 UNIQUE

-- Action 表保留审批字段（用户决策 #1：预留审批字段）
ALTER TABLE tms_actions_t 
    ADD COLUMN approver1 VARCHAR(50),
    ADD COLUMN approver2 VARCHAR(50),
    ADD COLUMN approval_status1 VARCHAR(20) DEFAULT 'Pending',
    ADD COLUMN approval_status2 VARCHAR(20) DEFAULT 'Pending',
    ADD COLUMN approval_remark VARCHAR(500);

-- action_type 扩展
-- CREATE / UPDATE / DELETE / APPROVE / REJECT
```

### 6.2 Action 完整字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| action_number | VARCHAR(50) UNIQUE | Action 编号（ACT + yyyyMMdd + 序号） |
| deal_number | VARCHAR(50) | 关联交易编号（**不再 UNIQUE**） |
| action_type | VARCHAR(20) | CREATE / UPDATE / DELETE / APPROVE / REJECT |
| approver1 | VARCHAR(50) | 一级审批人 |
| approver2 | VARCHAR(50) | 二级审批人 |
| approval_status1 | VARCHAR(20) | Pending / Approved / Rejected |
| approval_status2 | VARCHAR(20) | Pending / Approved / Rejected |
| approval_remark | VARCHAR(500) | 审批备注 |
| operator | VARCHAR(50) | 操作人 |
| operate_at | TIMESTAMP | 操作时间 |
| remark | VARCHAR(500) | 备注 |
| created_by/created_at/updated_by/updated_at/version/deleted | 标准审计字段 | |

---

## 七、AC 交易全生命周期（v2.0 新流程）

### 7.1 AC 创建（保存）

```
用户填写 AC 表单 → 点击"保存"
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=CREATE, approval_status1=Pending) │
│    action_number=ACT202606210001                           │
│    deal_number=AC202606210001                              │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ② INSERT Deal(status=New)                                 │
│    deal_number=AC202606210001                              │
│    latest_action_number=ACT202606210001                    │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ③ INSERT AcDeal                                           │
│    bank_account_id, counterparty_account_id, payment_method│
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ④ ✅ INSERT DealMap(ActualCashflow) - 自动创建             │
│    dealmap_number=DMP202606210001                          │
│    deal_number=AC202606210001                              │
│    action_number=ACT202606210001                           │
│    event_type='ActualCashflow'                             │
│    amount/currency/direction 从 AC 表单获取                │
│    event_status='Active'                                   │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑤ ✅ INSERT Cashflow - 自动创建（dealmap_number 关联）     │
│    cflow_number=CF202606210001                             │
│    dealmap_number=DMP202606210001  ← 字符串引用            │
│    status='Created'                                        │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑥ ❌ 不生成 DealImage（用户最新理念）                      │
└──────────────────────────────────────────────────────────┘
```

**关键 SQL（创建时事务）**：

```sql
BEGIN;

-- ① Action
INSERT INTO tms_actions_t (
    action_number, deal_number, action_type, approval_status1,
    operator, created_by, created_at
) VALUES (
    'ACT202606210001', 'AC202606210001', 'CREATE', 'Pending',
    'zhangsan', 'zhangsan', NOW()
);

-- ② Deal
INSERT INTO tms_deals_t (
    deal_number, deal_type, business_unit, counterparty_id,
    instrument_id, trader_id, direction, amount, currency,
    deal_date, value_date, status, latest_action_number,
    created_by, created_at
) VALUES (
    'AC202606210001', 'AC', 'BU001', 5001,
    301, 401, 'Outflow', 1000000.00, 'CNY',
    '2026-06-21', '2026-06-21', 'New', 'ACT202606210001',
    'zhangsan', NOW()
);

-- ③ AcDeal
INSERT INTO tms_ac_deals_t (
    deal_number, bank_account_id, counterparty_account_id, payment_method,
    created_by, created_at
) VALUES (
    'AC202606210001', 201, 301, 'TRANSFER',
    'zhangsan', NOW()
);

-- ④ DealMap（自动创建）
INSERT INTO tms_deal_map_t (
    dealmap_number, deal_number, action_number, event_type, event_status,
    amount, currency, direction, event_date, value_date,
    description, created_by, created_at
) VALUES (
    'DMP202606210001', 'AC202606210001', 'ACT202606210001',
    'ActualCashflow', 'Active',
    1000000.00, 'CNY', 'Outflow', '2026-06-21', '2026-06-21',
    'AC Deal created - actual cashflow event',
    'zhangsan', NOW()
);

-- ⑤ Cashflow（自动创建，dealmap_number 关联）
INSERT INTO tms_cashflow_t (
    cflow_number, deal_number, business_unit, bank_account,
    counterparty_account, direction, amount, currency,
    cflow_date, value_date, source_type, source_ref, status,
    dealmap_number,
    created_by, created_at
) VALUES (
    'CF202606210001', 'AC202606210001', 'BU001', 'BANK_ACC_201',
    'CP_ACC_301', 'Outflow', 1000000.00, 'CNY',
    '2026-06-21', '2026-06-21', 'AC_DEAL', 'AC202606210001', 'Created',
    'DMP202606210001',
    'zhangsan', NOW()
);

COMMIT;
```

---

### 7.2 AC 修改（保存）

```
用户修改 AC → 点击"保存"
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=UPDATE, approval_status1=Pending) │
│    action_number=ACT202606210002                           │
│    deal_number=AC202606210001 (与 CREATE 的 Action 独立)    │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ② UPDATE Deal（修改字段）                                  │
│    amount = 新值, updated_at = NOW()                       │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ③ UPDATE AcDeal                                            │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ④ 软删除旧 DealMap                                          │
│    UPDATE tms_deal_map_t SET deleted='1'                   │
│    WHERE deal_number='AC202606210001' AND deleted='0'      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑤ ✅ INSERT 新 DealMap(ActualCashflow) - 关联新 Action      │
│    dealmap_number=DMP202606210002                           │
│    action_number=ACT202606210002                            │
│    amount/currency = 修改后的新值                          │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑥ UPDATE Cashflow（指向新 DealMap）                        │
│    UPDATE tms_cashflow_t SET dealmap_number='DMP202606210002'│
│    WHERE dealmap_number='DMP202606210001'                  │
│    ⚠️ 软删除的 DealMap 不会被 Cashflow 引用                 │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑦ INSERT DealImage(v+1) - 记录修改前字段旧值                │
└──────────────────────────────────────────────────────────┘
```

---

### 7.3 AC 删除

```
用户点击"删除"
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ① INSERT Action(action_type=DELETE, approval_status1=Pending)│
│    action_number=ACT202606210003                           │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ② 软删除 Deal                                              │
│    UPDATE tms_deals_t SET deleted='1'                     │
│    WHERE deal_number='AC202606210001'                      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ③ 软删除 AcDeal                                            │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ④ ✅ 级联软删除 DealMap                                     │
│    UPDATE tms_deal_map_t SET deleted='1'                   │
│    WHERE deal_number='AC202606210001' AND deleted='0'      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑤ 软删除 Cashflow（级联）                                  │
│    UPDATE tms_cashflow_t SET deleted='1'                   │
│    WHERE dealmap_number IN (                               │
│        SELECT dealmap_number FROM tms_deal_map_t           │
│        WHERE deal_number='AC202606210001'                  │
│    )                                                       │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│ ⑥ INSERT DealImage(v+1) - 记录删除前完整状态                │
└──────────────────────────────────────────────────────────┘
```

---

### 7.4 AC 审批（基于 Action）

```
审批界面展示: 该笔 Deal 的所有 Action 列表
┌──────────────────────────────────────────────────────────┐
│ ACT202606210001 | CREATE | Pending   | [审批] [驳回]     │
│ ACT202606210002 | UPDATE | Pending   | [审批] [驳回]     │
│ ACT202606210003 | DELETE | Pending   | [审批] [驳回]     │
└──────────────────────────────────────────────────────────┘

用户点击某个 Action 的"审批通过"
    │
    ▼
UPDATE tms_actions_t SET approval_status1='Approved' 
WHERE action_number='ACT202606210001';

⚠️ 关键：审批**不改变** DealMap / Cashflow 的任何状态
   DealMap.event_status 始终保持 Active
   Cashflow.status 始终保持 Created
```

**AC/AT 完整操作菜单**（用户最新理念）：
- ✅ 保存（创建或修改）
- ✅ 删除
- ✅ 审批（针对 Action）
- ✅ 驳回（针对 Action）
- ❌ ~~提交~~（无此功能）
- ❌ ~~执行~~（无此功能）

---

## 八、AC/AT 操作汇总表（v2.0）

| 操作 | Action | DealMap | Cashflow | DealImage |
|------|--------|---------|----------|-----------|
| **创建** | INSERT Action(CREATE) | ✅ INSERT（ActualCashflow） | ✅ INSERT | ❌ 不生成 |
| **修改** | INSERT Action(UPDATE) | 软删旧 + INSERT 新 | UPDATE dealmap_number | ✅ INSERT(v+1) |
| **删除** | INSERT Action(DELETE) | 级联软删 | 级联软删 | ✅ INSERT(v+1) |
| **审批** | UPDATE Action(approval_status1/2) | ❌ 不变 | ❌ 不变 | ❌ 不变 |
| **驳回** | UPDATE Action(approval_status1/2=Rejected) | ❌ 不变 | ❌ 不变 | ❌ 不变 |

---

## 九、与现有架构的一致性检查

| 规范项 | 本设计 | Open-TMS 开发规范 | 一致性 |
|--------|--------|------------------|--------|
| 表名命名 | `tms_deal_map_t` | `tms_{module}_{type}_t` | ✅ |
| 主键 | BIGSERIAL | BIGSERIAL | ✅ |
| 金额精度 | DECIMAL(38,18) | DECIMAL(38,18) | ✅ |
| 币种 | VARCHAR(10) | VARCHAR(10) | ✅ |
| 审计字段 | 标准 6 字段 | 同 | ✅ |
| 删除标记 | deleted CHAR(1) | deleted CHAR(1) DEFAULT '0' | ✅ |
| API 路径 | `/api/v1/dealing/dealmap/...` | `/api/v1/{module}/{resource}` | ✅ |

---

## 十、决策记录

### v2.0 决策（2026-06-21）- 重大重构

| # | 决策项 | 决策 | 决策依据 |
|---|--------|------|----------|
| 20 | DealMap 字段精简 | 仅保留核心 4 字段 + 必要辅助（25 字段） | 用户指定 |
| 21 | DealMap 无审批相关字段 | event_status 仅 Active/Inactive，审批由 Action 处理 | 用户决策 |
| 22 | Action 多对一 | 移除 Action 表 deal_number UNIQUE 约束 | 用户决策 |
| 23 | Cashflow 反向关联字段 | `dealmap_number VARCHAR(50)`（非 FK，字符串引用） | 用户决策 |
| 24 | 交易创建自动生成 DealMap + Cashflow | CREATE 时立即创建 DealMap(ActualCashflow) + Cashflow | 用户决策 |
| 25 | 交易修改软删+新建 | UPDATE 时软删除旧 DealMap，新建新 DealMap | 用户决策 |
| 26 | 交易删除级联软删 | DELETE 时软删 Deal + 级联软删 DealMap + Cashflow | 用户决策 |
| 27 | CREATE 不生成 DealImage | 修改/删除仍生成 | 用户决策 |
| 28 | 审批仅作用于 Action | Action.approval_status1/2 更新，DealMap/Cashflow 状态不变 | 用户决策 |
| 29 | AC/AT 操作精简 | 只有 save/delete/approve/reject | 用户决策 |
| 30 | Action 字段保留 | 保留 approver1/2/approval_status1/2（与原 Action 设计一致） | 用户决策 |

### v1.x 历史决策（已被 v2.0 取代）

| # | 决策项 | 状态 |
|---|--------|------|
| 1-19 | v1.1 / v1.2 全部决策 | ⚠️ 大部分被 v2.0 推翻 |

---

## 十一、API 设计

### 11.1 DealMap 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/dealmap/page` | GET | 分页查询 DealMap |
| `/api/v1/dealing/dealmap/{id}` | GET | 获取 DealMap 详情 |
| `/api/v1/dealing/dealmap/by-deal/{dealNumber}` | GET | 查询某 Deal 的 DealMap 列表 |
| `/api/v1/dealing/dealmap/{id}/reverse` | POST | 冲销 DealMap |

### 11.2 Action 接口（审批用）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/actions/page` | GET | 分页查询 Action |
| `/api/v1/dealing/actions/by-deal/{dealNumber}` | GET | 查询某 Deal 的所有 Action |
| `/api/v1/dealing/actions/pending` | GET | 查询待我审批的 Action |
| `/api/v1/dealing/actions/{actionNumber}/approve` | POST | 审批通过 Action |
| `/api/v1/dealing/actions/{actionNumber}/reject` | POST | 驳回 Action |

---

## 十二、验收标准

### v2.0 验收点（核心）

| 功能 | 验收条件 |
|------|----------|
| **DealMap 自动生成** | AC Deal 创建后**立即**生成 DealMap(ActualCashflow) + Cashflow（dealmap_number 关联） |
| **DealMap 字段精简** | tms_deal_map_t 仅含 25 个字段；不含 event_category/subtype/event_timing 等已移除字段 |
| **CREATE 不生成 DealImage** | 创建 AC Deal 后查询 tms_deals_image_t 无新增记录 |
| **UPDATE 软删+新建** | 修改 AC 时旧 DealMap.deleted='1'，新建 DealMap 关联新 Action |
| **DELETE 级联软删** | 删除 AC 时 Deal/AcDeal/DealMap/Cashflow 全部 deleted='1' |
| **审批不影响 DealMap/Cashflow** | Action.approval_status1 变更后，DealMap.event_status 与 Cashflow.status 均不变 |
| **AC/AT 操作菜单** | 只有 save / delete / approve / reject；无 submit / execute |
| **Action 多对一** | 一笔 AC Deal 可有多个 Action（CREATE + UPDATE + APPROVE 等）独立存在 |

### 继承验收点

| 功能 | 验收条件 |
|------|----------|
| DealMap 时间线 | 按 deal_number 查询 DealMap 列表，按 event_date 排序 |
| DealMap 冲销 | 支持反向冲销（is_reversal='1'） |
| Cashflow 反查 DealMap | Cashflow.dealmap_number 字段非空，可通过该字段反查 DealMap |
| Action 审批 | Action.approval_status1 字段支持 Pending/Approved/Rejected |

---

## 十三、后续工作计划

### Phase 1（M1 阶段）
- ✅ v2.0 PRD 设计完成（含重大重构）
- ⏳ 数据库改造：
  - 简化 tms_deal_map_t 表结构（移除多余字段）
  - 修改 tms_actions_t 移除 deal_number UNIQUE 约束
  - tms_cashflow_t 新增 dealmap_number 字段
- ⏳ 后端 Service 改造：
  - DealServiceImpl.createDeal() 自动生成 DealMap + Cashflow
  - DealServiceImpl.updateDeal() 软删旧 DealMap + 新建 DealMap
  - DealServiceImpl.deleteDeal() 级联软删
- ⏳ 审批界面改造：
  - 基于 Action 的审批列表
  - 审批/驳回 Action 接口
- ⏳ 测试用例：
  - 验证 DealMap 自动生成
  - 验证软删+新建逻辑
  - 验证审批不影响 DealMap/Cashflow

### Phase 2（M2+ 阶段）
- ⏳ 引入更多 event_type（FX/IRS/Deposit/Loan 等）
- ⏳ 完善冲销流程
- ⏳ M2 报表分析模块对接 DealMap

---

*BA + PM 产出 - M1 v2.0 (2026-06-21)*
*核心修订：DealMap 字段精简；Action 多对一；DealMap 无审批状态；Cashflow 反向关联 dealmap_number*