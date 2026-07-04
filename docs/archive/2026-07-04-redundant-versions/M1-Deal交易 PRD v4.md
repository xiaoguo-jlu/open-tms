# Open-TMS M1-Deal交易 PRD

**版本**: v5.0
**角色**: 产品经理 (PM)
**日期**: 2026-06-01
**状态**: 重大更新 - 增加镜像表和审计日志表

---

## 一、模块概述

**模块名称**: dealing - 交易管理
**功能定位**: 管理所有类型的交易(Deal)，包括AC交易、AT交易、FX交易等，提供统一的交易生命周期管理
**用户角色**: 资金管理人员、财务人员、资金经理

**核心设计原则**:
1. 所有交易类型共享同一套交易基础设施(公共表)
2. 交易操作产生Action，审批审批的是Action
3. 所有操作都记录审计日志和快照
4. 交易镜像用于记录历史状态，支持回溯

---

## 二、交易公共架构

### 2.1 交易公共表 (tms_deals_t)

所有交易类型的公共信息统一存储在 `tms_deals_t` 表：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dealId | BIGINT | 系统 | 主键 |
| dealNumber | VARCHAR(50) | 系统 | 自动生成，格式: DEAL + yyyyMMdd + 序号(4位) |
| dealType | VARCHAR(20) | Y | 交易类型：AC/AT/FX/ST等 |
| businessUnit | VARCHAR(50) | Y | 关联业务单元代码 |
| counterpartyId | BIGINT | Y | 交易对手 |
| instrumentId | BIGINT | Y | 金融工具 |
| traderId | BIGINT | Y | 交易员 |
| direction | VARCHAR(10) | Y | Inflow/Outflow |
| amount | DECIMAL(38,18) | Y | 交易金额，精度38,18 |
| currency | VARCHAR(10) | Y | 币种代码 |
| dealDate | DATE | Y | 交易日期 |
| valueDate | DATE | Y | 起息日/结算日 |
| status | VARCHAR(20) | 系统 | New/Submitted/Approved/Settled/Canceled |
| description | VARCHAR(500) | N | 交易描述 |
| remark | VARCHAR(500) | N | 备注 |
| createdBy | VARCHAR(50) | 系统 | 创建人员 |
| createdAt | DATETIME | 系统 | 创建时间 |
| updatedBy | VARCHAR(50) | 系统 | 更新人员 |
| updatedAt | DATETIME | 系统 | 更新时间 |

**设计说明**: `tms_deals_t` 是所有交易类型的公共表，通过 `dealType` 区分不同交易类型。

### 2.2 交易类型

| 类型 | 代码 | 说明 |
|------|------|------|
| AC交易 | AC | Actual Cashflow - 纯粹资金收付 |
| AT交易 | AT | Account Transfer - 账户转账 |
| FX交易 | FX | Foreign Exchange - 外汇交易 |
| ST交易 | ST | Securities - 证券交易 |

---

## 三、Action机制

### 3.1 Action定义

每次交易操作(创建/修改/删除/审批)都会生成一个Action记录：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| actionId | BIGINT | 主键 |
| actionNumber | VARCHAR(50) | 动作编号，格式: ACT + yyyyMMdd + 序号(4位) |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| actionType | VARCHAR(20) | 动作类型：CREATE/UPDATE/DELETE/APPROVE/REJECT/SUBMIT/EXECUTE |
| beforeImage | TEXT | 变化前的交易完整信息(JSON) |
| afterImage | TEXT | 变化后的交易完整信息(JSON) |
| changeFields | TEXT | 变化的字段列表(JSON数组) |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| approveLevel | INT | 当前审批层级(用于多级审批) |
| remark | VARCHAR(500) | 动作备注 |

### 3.2 Action生成时机

| 交易操作 | 生成Action | actionType |
|----------|-----------|------------|
| 创建交易 | ✅ | CREATE |
| 修改交易 | ✅ | UPDATE |
| 删除交易 | ✅ | DELETE |
| 提交审批 | ✅ | SUBMIT |
| 审批通过 | ✅ | APPROVE |
| 审批拒绝 | ✅ | REJECT |
| 执行交易 | ✅ | EXECUTE |

### 3.3 审批流程

```
交易提交 → 生成Action(SUBMIT) → 进入审批流程
    │
    ├── 审批通过 → 生成Action(APPROVE) → 交易状态更新
    │
    └── 审批拒绝 → 生成Action(REJECT) → 交易状态更新
```

**审批对象**: 审批的实际是Action，而非直接修改交易。通过Action实现审批的幂等性和可追溯性。

---

## 四、审计日志与快照

### 4.1 审计历史 (Audit History)

每次交易字段变化都记录在 `tms_actions_t` 表的 `changeFields` 字段：

```json
// changeFields 示例
[
  {"field": "amount", "oldValue": "1000", "newValue": "2000"},
  {"field": "counterpartyId", "oldValue": "1", "newValue": "2"}
]
```

### 4.2 快照 (Image)

交易变化前的完整信息存储在 `beforeImage` 字段：

```json
// beforeImage 示例
{
  "dealNumber": "DEAL202606010001",
  "dealType": "AC",
  "amount": "1000",
  "currency": "CNY",
  ...
}
```

---

## 四、交易镜像表

### 4.1 公共镜像表 (tms_deals_image_t)

记录交易公共信息的每次变化快照：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| imageId | BIGINT | 主键 |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| actionNumber | VARCHAR(50) | 关联Action编号 |
| imageData | TEXT | 交易公共信息快照(JSON) |
| imageType | VARCHAR(20) | 镜像类型：CREATE/UPDATE/DELETE |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

**说明**: 每次交易变化(创建/修改/删除)都会生成一条镜像记录，存储变化前的完整公共信息。

### 4.2 AC交易镜像表 (tms_ac_deals_image_t)

记录AC交易特有的扩展信息：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| imageId | BIGINT | 主键 |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| actionNumber | VARCHAR(50) | 关联Action编号 |
| imageData | TEXT | AC交易扩展信息快照(JSON) |
| paymentMethod | VARCHAR(20) | 支付方式 |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

**说明**: 镜像表记录的是变化前的完整信息，用于交易回溯和恢复。

### 4.3 镜像表设计原则

```
交易变化 → 生成Action → 生成镜像
    │
    ├── CREATE: 存储创建前的空状态(或null)
    ├── UPDATE: 存储更新前的完整信息
    └── DELETE: 存储删除前的完整信息
```

---

## 五、审计日志表

### 5.1 审计日志头表 (tms_audit_log_hdr_t)

记录每笔交易的审计汇总信息：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| hdrId | BIGINT | 主键 |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| actionNumber | VARCHAR(50) | 关联Action编号 |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| changeType | VARCHAR(20) | 变化类型：CREATE/UPDATE/DELETE/APPROVE/REJECT |
| totalFields | INT | 变化的字段数量 |
| status | VARCHAR(20) | 审计状态：Active/Reversed |
| remark | VARCHAR(500) | 审计备注 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

### 5.2 审计日志行表 (tms_audit_log_line_t)

记录每个字段的详细变化：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| lineId | BIGINT | 主键 |
| hdrId | BIGINT | 关联审计头ID |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| fieldName | VARCHAR(50) | 字段名 |
| fieldLabel | VARCHAR(100) | 字段中文名 |
| oldValue | VARCHAR(500) | 旧值 |
| newValue | VARCHAR(500) | 新值 |
| oldDisplay | VARCHAR(500) | 旧值显示(格式化) |
| newDisplay | VARCHAR(500) | 新值显示(格式化) |
| changeType | VARCHAR(20) | 变化类型：Create/Update/Delete |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

### 5.3 审计日志设计示例

**审计头记录**:
| hdrId | dealNumber | actionNumber | operator | changeType | totalFields |
|-------|------------|--------------|----------|------------|-------------|
| 1 | DEAL202606010001 | ACT202606010001 | zhangsan | UPDATE | 3 |

**审计行记录**:
| lineId | hdrId | fieldName | fieldLabel | oldValue | newValue |
|--------|-------|-----------|------------|----------|----------|
| 1 | 1 | amount | 交易金额 | 1000 | 2000 |
| 2 | 1 | counterpartyId | 交易对手 | 1 | 2 |
| 3 | 1 | valueDate | 起息日 | 2026-06-01 | 2026-06-02 |

### 5.4 审计日志生成时机

| 操作 | 生成审计日志 | 说明 |
|------|-------------|------|
| 创建交易 | ✅ | 记录所有字段的初始值 |
| 修改交易 | ✅ | 记录每个变化的字段(旧值→新值) |
| 删除交易 | ✅ | 记录所有字段的最终值 |
| 审批通过 | ✅ | 记录审批信息 |
| 审批拒绝 | ✅ | 记录拒绝原因 |

---

## 六、AC交易字段(扩展)

在公共表基础上，AC交易可能有扩展字段(如有的话)：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| (继承) | - | - | 公共字段见2.1 |
| paymentMethod | VARCHAR(20) | N | 支付方式：转账/票据 |

---

## 六、基础数据选择

### 6.1 弹框选择设计

所有关联的基础数据通过弹框选择，弹框只显示有效数据：

| 字段 | 弹框类型 | 数据源 |
|------|----------|--------|
| businessUnit | 业务单元选择器 | businessUnit表，status=Active |
| counterparty | 交易对手选择器 | counterparty表，status=Active |
| instrument | 金融工具选择器 | instrument表，status=Active |
| trader | 交易员选择器 | trader表，status=Active |
| bankAccount | 银行账户选择器(AC交易用) | bankAccount表，status=Active |

### 6.2 弹框通用功能

- 只显示有效(Active)数据
- 支持搜索过滤
- 支持分页
- 选择后显示选中项名称

---

## 七、交易状态机

```
New → Submitted → Approved → Settled
                    ↓            ↓
              Rejected     Canceled
```

| 状态 | 说明 | 可执行操作 |
|------|------|-----------|
| New | 新建 | submit, cancel |
| Submitted | 已提交审批 | approve, reject |
| Approved | 已审批 | execute |
| Settled | 已结算(终态) | - |
| Rejected | 已拒绝 | - |
| Canceled | 已取消 | - |

---

## 八、API接口清单

### 8.1 交易管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/deals/page` | GET | 分页查询交易(可按type筛选) |
| `/api/v1/dealing/deals/{id}` | GET | 获取交易详情 |
| `/api/v1/dealing/deals` | POST | 创建交易 |
| `/api/v1/dealing/deals/update` | POST | 更新交易 |
| `/api/v1/dealing/deals/{id}` | DELETE | 删除交易 |
| `/api/v1/dealing/deals/{id}/submit` | POST | 提交审批 |
| `/api/v1/dealing/deals/{id}/approve` | POST | 审批通过 |
| `/api/v1/dealing/deals/{id}/reject` | POST | 审批拒绝 |
| `/api/v1/dealing/deals/{id}/execute` | POST | 执行交易 |
| `/api/v1/dealing/deals/{id}/cancel` | POST | 取消交易 |

### 8.2 Action管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/actions/page` | GET | 分页查询Action |
| `/api/v1/dealing/actions/by-deal/{dealNumber}` | GET | 查询关联交易的Action列表 |
| `/api/v1/dealing/actions/{id}` | GET | 获取Action详情 |

### 8.3 基础数据选择器API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/selector/business-units` | GET | 业务单元选择器数据 |
| `/api/v1/dealing/selector/counterparties` | GET | 交易对手选择器数据 |
| `/api/v1/dealing/selector/instruments` | GET | 金融工具选择器数据(按type筛选) |
| `/api/v1/dealing/selector/traders` | GET | 交易员选择器数据 |
| `/api/v1/dealing/selector/bank-accounts` | GET | 银行账户选择器数据 |

---

## 九、数据库设计

### 9.1 交易公共表

```sql
CREATE TABLE tms_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    business_unit VARCHAR(50) NOT NULL,
    counterparty_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    trader_id BIGINT NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    deal_date DATE NOT NULL,
    value_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'New',
    description VARCHAR(500),
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_deal_number ON tms_deals_t(deal_number);
CREATE INDEX idx_deal_type ON tms_deals_t(deal_type);
CREATE INDEX idx_deal_status ON tms_deals_t(status);
CREATE INDEX idx_deal_unit ON tms_deals_t(business_unit);
```

### 9.2 Action表

```sql
CREATE TABLE tms_actions_t (
    id BIGSERIAL PRIMARY KEY,
    action_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    deal_type VARCHAR(20) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_image TEXT,
    after_image TEXT,
    change_fields TEXT,
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approve_level INT DEFAULT 0,
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_action_number ON tms_actions_t(action_number);
CREATE INDEX idx_action_deal ON tms_actions_t(deal_number);
CREATE INDEX idx_action_type ON tms_actions_t(action_type);
```

---

## 十、与其他模块的关系

| 模块 | 关系 | 说明 |
|------|------|------|
| **Cashflow** | Deal执行生成Cashflow | 一对一生成 |
| **Action** | 交易操作产生Action | 审批对象 |
| **ApprovalRule** | 审批层级判断 | 提交时调用 |
| **BusinessUnit** | 交易归属 | businessUnit字段 |
| **Counterparty** | 交易对手 | counterparty字段 |
| **Instrument** | 金融工具 | instrument字段 |
| **Trader** | 交易员 | trader字段 |
| **BankAccount** | 账户(AC交易用) | 通过扩展字段关联 |

---

## 十一、验收标准

| 功能 | 验收条件 |
|------|----------|
| 交易创建 | 字段必填校验，保存成功，dealNumber自动生成，生成Action(CREATE) |
| 交易修改 | 生成Action(UPDATE)，记录beforeImage/afterImage/changeFields |
| 交易删除 | 生成Action(DELETE)，记录完整快照 |
| 审批流程 | 提交后按ApprovalRule判断是否需要审批，多级审批正常，审批生成Action |
| 弹框选择 | 只显示有效数据，支持搜索和分页 |
| 审计追溯 | 可查看交易的完整Action历史 |
| 查询导出 | 多条件组合查询正常，Excel导出正常 |

---

## 十二、页面原型(FIS风格)

**参考**: FIS Quantum TMS - 专业金融系统界面风格

### 12.1 交易列表页
- 顶部: 筛选条件(日期范围/状态/类型/账户)
- 中部: 数据表格(分页展示)
- 支持状态操作按钮
- 显示交易类型标签

### 12.2 交易录入/编辑页
- 基本信息区域(主体/对手/Instrument/交易员 - 弹框选择)
- 交易信息(类型/方向/金额/币种/日期)
- 操作按钮
- 底部: 操作历史/Action列表

### 12.3 弹框选择器(通用)
- 标题: 选择{数据类型}
- 搜索框
- 数据表格(单选/多选)
- 底部: 确认/取消按钮

---

*PM产出 - M1 v4.0 (2026-06-01)*
*重大更新: 交易公共架构 + Action机制 + 审计追溯*