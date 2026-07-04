# Open-TMS M1-Deal交易 PRD

**版本**: v5.0
**角色**: 产品经理 (PM)
**日期**: 2026-06-01
**状态**: 重大更新 - Action为审批对象 + 镜像表结构化设计

---

## 一、模块概述

**模块名称**: dealing - 交易管理
**功能定位**: 管理所有类型的交易(Deal)，包括AC交易、AT交易、FX交易等，提供统一的交易生命周期管理
**用户角色**: 资金管理人员、财务人员、资金经理

**核心设计原则**:
1. 所有交易类型共享同一套交易基础设施(公共表)
2. **Action是审批的作用对象**，审批人审批的是Action
3. **只有数据变化操作才创建Action**，状态流转操作只更新Action状态
4. 镜像表结构化存储每个字段的旧值，支持交易回溯
5. 交易表新增字段时，镜像表同步新增对应字段

---

## 二、交易公共架构

### 2.1 交易公共表 (tms_deals_t)

所有交易类型的公共信息统一存储在 `tms_deals_t` 表：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dealId | BIGINT | 系统 | 主键 |
| dealNumber | VARCHAR(50) | 系统 | 自动生成，格式: DEAL + yyyyMMdd + 序号(4位) |
| dealType | VARCHAR(20) | Y | 交易类型：AC/AT/FX/ST等 |
| managementEntity | VARCHAR(50) | Y | 资金管理主体 |
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
| latestActionNumber | VARCHAR(50) | N | 最新Action编号，关联tms_actions_t |
| createdBy | VARCHAR(50) | 系统 | 创建人员 |
| createdAt | DATETIME | 系统 | 创建时间 |
| updatedBy | VARCHAR(50) | 系统 | 更新人员 |
| updatedAt | DATETIME | 系统 | 更新时间 |

### 2.2 交易类型

| 类型 | 代码 | 说明 |
|------|------|------|
| AC交易 | AC | Actual Cashflow - 纯粹资金收付 |
| AT交易 | AT | Account Transfer - 账户转账 |
| FX交易 | FX | Foreign Exchange - 外汇交易 |
| ST交易 | ST | Securities - 证券交易 |

### 2.3 AC交易个性化表 (tms_ac_deals_t)

AC交易的个性化信息存储在 `tms_ac_deals_t` 表：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| acDealId | BIGINT | 系统 | 主键 |
| dealNumber | VARCHAR(50) | Y | 关联交易编号，关联tms_deals_t |
| bankAccountId | BIGINT | Y | 主体银行账户 |
| counterpartyAccountId | BIGINT | N | 对手方账户 |
| paymentMethod | VARCHAR(20) | N | 支付方式：转账/票据/其他 |
| createdBy | VARCHAR(50) | 系统 | 创建人员 |
| createdAt | DATETIME | 系统 | 创建时间 |
| updatedBy | VARCHAR(50) | 系统 | 更新人员 |
| updatedAt | DATETIME | 系统 | 更新时间 |

---

## 三、Action机制

### 3.1 Action的本质

**Action是审批的作用对象**，同时记录交易的操作历史：
- 交易创建时创建Action(CREATE)，后续状态流转只更新这个Action的状态
- 审批人审批的是Action
- 一笔交易只有一个Action，贯穿交易的整个生命周期

### 3.2 Action定义 (tms_actions_t)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| actionId | BIGINT | 主键 |
| actionNumber | VARCHAR(50) | 动作编号，格式: ACT + yyyyMMdd + 序号(4位) |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| actionType | VARCHAR(20) |动作类型：CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE |
| actionStatus | VARCHAR(20) | Action状态：Pending/Approved/Rejected/Executed |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| remark | VARCHAR(500) | 动作备注 |
| approver1 | VARCHAR(50) | 一级审批人 |
| approver2 | VARCHAR(50) | 二级审批人 |
| approvalStatus1 | VARCHAR(20) | 一级审批状态：Pending/Approved/Rejected |
| approvalStatus2 | VARCHAR(20) | 二级审批状态：Pending/Approved/Rejected |
| approvalRemark | VARCHAR(500) | 审批备注 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

**设计说明**:
- 一笔交易只有一个Action，贯穿整个生命周期
- `actionType` 记录最近一次操作类型
- `actionStatus` 记录当前状态：Pending(待审批)/Approved(已审批)/Rejected(已拒绝)/Executed(已执行)
- 审批相关字段用于多级审批

### 3.3 Action操作规则

**创建Action的操作（数据变化）**：
| 交易操作 | actionType | 说明 |
|----------|-----------|------|
| 创建交易 | CREATE | 创建新Action |
| 修改交易 | UPDATE | 更新Action的actionType |
| 删除交易 | DELETE | 更新Action的actionType |

**更新Action的操作（状态流转，不创建新Action）**：
| 交易操作 | actionType | 说明 |
|----------|-----------|------|
| 提交审批 | SUBMIT | 更新Action的actionType和actionStatus |
| 审批通过 | APPROVE | 更新Action的approvalStatus1/2 |
| 审批拒绝 | REJECT | 更新Action的approvalStatus1/2和actionStatus |
| 执行交易 | EXECUTE | 更新Action的actionType和actionStatus |

**核心原则**：
- 只有数据变化操作（CREATE/UPDATE/DELETE）才**创建新Action**
- 状态流转操作（SUBMIT/APPROVE/REJECT/EXECUTE）只**更新现有Action**的状态和审批字段

### 3.4 审批流程详解

**审批对象是Action，不是交易**：

```
交易创建 → Action(CREATE)创建，actionStatus='Pending'
    │
    ▼
用户提交审批
    │
    ▼
更新Action.actionType='SUBMIT'，交易.status='Submitted'
    │
    ▼
一级审批人审批Action
    │
    ├── 拒绝 → Action.approvalStatus1='Rejected'，Action.actionStatus='Rejected'，交易.status='Rejected'
    │
    └── 通过 → Action.approvalStatus1='Approved'
                │
                ├── 需要二级审批 → 二级审批人审批
                │                 ├── 拒绝 → Action.approvalStatus2='Rejected'，交易.status='Rejected'
                │                 └── 通过 → Action.approvalStatus2='Approved'，交易.status='Approved'
                │
                └── 不需要二级审批 → 交易.status='Approved'
```

**关键点**：
- 提交审批不创建新Action，只更新现有Action
- 审批通过/拒绝也不创建新Action，只更新审批字段
- 一笔交易只有一个Action，所有操作都作用在这个Action上

---

## 四、交易镜像表

### 4.1 镜像的本质

**镜像表记录交易变化前的每个字段的旧值**，而非JSON：
- 结构化存储，每个字段独立列
- 支持精确查询某个字段的历史值
- 支持交易回溯（通过版本号串联）
- **交易表新增字段时，镜像表同步新增对应字段**

### 4.2 公共镜像表 (tms_deals_image_t)

记录交易公共信息的每次变化快照：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| imageId | BIGINT | 主键 |
| imageNumber | VARCHAR(50) | 镜像编号，格式: IMG + yyyyMMdd + 序号(4位) |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| version | INT | 版本号，从1开始，每次数据变化+1 |
| managementEntity | VARCHAR(50) | 资金管理主体（旧值） |
| counterpartyId | BIGINT | 交易对手（旧值） |
| instrumentId | BIGINT | 金融工具（旧值） |
| traderId | BIGINT | 交易员（旧值） |
| direction | VARCHAR(10) | 方向（旧值） |
| amount | DECIMAL(38,18) | 交易金额（旧值） |
| currency | VARCHAR(10) | 币种（旧值） |
| dealDate | DATE | 交易日期（旧值） |
| valueDate | DATE | 起息日（旧值） |
| status | VARCHAR(20) | 状态（旧值） |
| description | VARCHAR(500) | 交易描述（旧值） |
| remark | VARCHAR(500) | 备注（旧值） |
| latestActionNumber | VARCHAR(50) | 最新Action编号（旧值） |
| imageType | VARCHAR(20) | 镜像类型：CREATE/UPDATE/DELETE |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

**说明**:
- `version` 字段记录这是该交易的第几个版本
- 每次数据变化（CREATE/UPDATE/DELETE）生成一条新镜像，version +1
- 状态流转操作（SUBMIT/APPROVE/REJECT/EXECUTE）**不生成镜像**
- 所有字段都存储变化前的旧值
- `latestActionNumber` 字段与交易表同步，镜像表中存储的是变化前的旧值

### 4.3 AC交易镜像表 (tms_ac_deals_image_t)

记录AC交易个性化信息的每次变化快照：

| 字段名 | 类型 | 说明 |
|--------|------|------|------|
| imageId | BIGINT | 主键 |
| imageNumber | VARCHAR(50) | 镜像编号，格式: IMG + yyyyMMdd + 序号(4位) |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| version | INT | 版本号，与公共镜像一致 |
| bankAccountId | BIGINT | 主体银行账户（旧值） |
| counterpartyAccountId | BIGINT | 对手方账户（旧值） |
| paymentMethod | VARCHAR(20) | 支付方式（旧值） |
| imageType | VARCHAR(20) | 镜像类型：CREATE/UPDATE/DELETE |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |

**说明**: AC交易镜像与公共镜像通过 `dealNumber` + `version` 关联。

### 4.4 镜像表设计原则

```
数据变化操作 → 生成镜像
    │
    ├── CREATE: version=1，存储初始值（或null表示无旧值）
    ├── UPDATE: version=n+1，存储更新前的完整信息
    └── DELETE: version=n+1，存储删除前的完整信息

状态流转操作 → 不生成镜像
    │
    ├── SUBMIT: 更新Action状态
    ├── APPROVE: 更新Action审批字段
    ├── REJECT: 更新Action审批字段
    └── EXECUTE: 更新Action状态
```

### 4.5 镜像表字段同步规则

**规则**：交易表新增字段时，镜像表必须同步新增对应字段，存储该字段变化前的旧值。

示例：
```sql
-- 交易表新增"到期日期"字段
ALTER TABLE tms_deals_t ADD COLUMN maturity_date DATE;

-- 镜像表同步新增"到期日期"字段
ALTER TABLE tms_deals_image_t ADD COLUMN maturity_date DATE;
```

---

## 五、交易状态机

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

## 六、API接口清单

### 6.1 交易管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/deals/page` | GET | 分页查询交易(可按type筛选) |
| `/api/v1/dealing/deals/{id}` | GET | 获取交易详情 |
| `/api/v1/dealing/deals` | POST | 创建交易 → 创建Action(CREATE) + 镜像(version=1) |
| `/api/v1/dealing/deals/update` | POST | 更新交易 → 更新Action + 镜像(version=n+1) |
| `/api/v1/dealing/deals/{id}` | DELETE | 删除交易 → 更新Action + 镜像(version=n+1) |
| `/api/v1/dealing/deals/{id}/submit` | POST | 提交审批 → 更新Action状态 |
| `/api/v1/dealing/deals/{id}/approve` | POST | 审批通过 → 更新Action审批字段 |
| `/api/v1/dealing/deals/{id}/reject` | POST | 审批拒绝 → 更新Action审批字段 |
| `/api/v1/dealing/deals/{id}/execute` | POST | 执行交易 → 更新Action状态 |
| `/api/v1/dealing/deals/{id}/cancel` | POST | 取消交易 |

### 6.2 Action管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/actions/page` | GET | 分页查询Action |
| `/api/v1/dealing/actions/by-deal/{dealNumber}` | GET | 查询关联交易的Action |
| `/api/v1/dealing/actions/{id}` | GET | 获取Action详情（含审批信息） |
| `/api/v1/dealing/actions/pending` | GET | 查询待我审批的Action列表 |

### 6.3 镜像管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/images/by-deal/{dealNumber}` | GET | 查询关联交易的镜像列表 |
| `/api/v1/dealing/images/{dealNumber}/{version}` | GET | 查询指定版本的镜像 |

---

## 七、数据库设计

### 7.1 交易公共表

```sql
CREATE TABLE tms_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    management_entity VARCHAR(50) NOT NULL,
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
    latest_action_number VARCHAR(50),
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
CREATE INDEX idx_deal_unit ON tms_deals_t(management_entity);
CREATE INDEX idx_deal_latest_action ON tms_deals_t(latest_action_number);
```

### 7.2 AC交易个性化表

```sql
CREATE TABLE tms_ac_deals_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    bank_account_id BIGINT NOT NULL,
    counterparty_account_id BIGINT,
    payment_method VARCHAR(20),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_ac_deal_number ON tms_ac_deals_t(deal_number);
```

### 7.3 Action表

```sql
CREATE TABLE tms_actions_t (
    id BIGSERIAL PRIMARY KEY,
    action_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    approver1 VARCHAR(50),
    approver2 VARCHAR(50),
    approval_status1 VARCHAR(20) DEFAULT 'Pending',
    approval_status2 VARCHAR(20) DEFAULT 'Pending',
    approval_remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_action_number ON tms_actions_t(action_number);
CREATE INDEX idx_action_deal ON tms_actions_t(deal_number);
CREATE INDEX idx_action_status ON tms_actions_t(action_status);
```

**注意**：`deal_number` 在Action表中是UNIQUE，确保一笔交易只有一个Action。

### 7.4 公共镜像表

```sql
CREATE TABLE tms_deals_image_t (
    id BIGSERIAL PRIMARY KEY,
    image_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    deal_type VARCHAR(20) NOT NULL,
    version INT NOT NULL,
    management_entity VARCHAR(50),
    counterparty_id BIGINT,
    instrument_id BIGINT,
    trader_id BIGINT,
    direction VARCHAR(10),
    amount DECIMAL(38,18),
    currency VARCHAR(10),
    deal_date DATE,
    value_date DATE,
    status VARCHAR(20),
    description VARCHAR(500),
    remark VARCHAR(500),
    latest_action_number VARCHAR(50),
    image_type VARCHAR(20) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_image_deal ON tms_deals_image_t(deal_number);
CREATE INDEX idx_image_version ON tms_deals_image_t(deal_number, version);
```

### 7.5 AC交易镜像表

```sql
CREATE TABLE tms_ac_deals_image_t (
    id BIGSERIAL PRIMARY KEY,
    image_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    version INT NOT NULL,
    bank_account_id BIGINT,
    counterparty_account_id BIGINT,
    payment_method VARCHAR(20),
    image_type VARCHAR(20) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    operate_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_ac_image_deal ON tms_ac_deals_image_t(deal_number);
CREATE INDEX idx_ac_image_version ON tms_ac_deals_image_t(deal_number, version);
```

---

## 八、与其他模块的关系

| 模块 | 关系 | 说明 |
|------|------|------|
| **Cashflow** | Deal执行生成Cashflow | 一对一生成 |
| **ManagementEntity** | 交易归属 | managementEntity字段 |
| **Counterparty** | 交易对手 | counterparty字段 |
| **Instrument** | 金融工具 | instrument字段 |
| **Trader** | 交易员 | trader字段 |
| **BankAccount** | 账户(AC交易用) | bankAccountId字段 |

---

## 九、验收标准

| 功能 | 验收条件 |
|------|----------|
| 交易创建 | 字段必填校验，保存成功，dealNumber自动生成，Action(CREATE)创建，生成镜像(version=1) |
| 交易修改 | 更新Action的actionType，生成新镜像(version=n+1)，存储所有字段旧值 |
| 提交审批 | 更新Action的actionType='SUBMIT'和actionStatus，**不创建新Action，不生成镜像** |
| 审批流程 | 审批人审批的是Action，更新Action的approvalStatus1/2，**不创建新Action** |
| 审批历史 | 可查看Action的完整审批历史 |
| 交易回溯 | 可通过镜像表查询任意版本的数据 |
| 字段同步 | 交易表新增字段时，镜像表同步新增对应字段 |

---

## 十、页面原型(FIS风格)

**参考**: FIS Quantum TMS - 专业金融系统界面风格

### 10.1 交易列表页
- 顶部: 筛选条件(日期范围/状态/类型/账户)
- 中部: 数据表格(分页展示)
- 支持状态操作按钮
- 显示交易类型标签

### 10.2 交易录入/编辑页
- 基本信息区域(主体/对手/Instrument/交易员 - 弹框选择)
- 交易信息(类型/方向/金额/币种/日期)
- 操作按钮
- 底部: 操作历史/Action详情

### 10.3 待审批列表页
- 显示待当前用户审批的Action列表
- 可对Action进行审批通过/拒绝操作
- Action详情页显示审批信息和审批历史

### 10.4 弹框选择器(通用)
- 标题: 选择{数据类型}
- 搜索框
- 数据表格(单选/多选)
- 底部: 确认/取消按钮

---

*PM产出 - M1 v5.0 (2026-06-01)*
*核心更新: Action为审批对象 + 状态流转只更新Action不创建新Action + 镜像表字段同步*