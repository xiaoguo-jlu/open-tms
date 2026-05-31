# Open-TMS AC Deal 与 Cashflow 分离架构设计

**版本**: v1.2
**角色**: 业务架构师 (BA)
**日期**: 2026-06-01
**状态**: 更新 - 命名规范调整(ACDeal/Cashflow/Stockflow)

---

## 一、命名规范

| 业务对象 | 英文名 | 编号字段 | 编号格式 | 说明 |
|----------|--------|----------|----------|------|
| AC交易 | AC Deal | dealNumber | AC + yyyyMMdd + 序号 | Actual Cashflow交易 |
| 现金流 | Cashflow | cflowNumber | CF + yyyyMMdd + 序号 | 资金实际变动 |
| 证券流 | Stockflow | sflowNumber | SF + yyyyMMdd + 序号 | 证券实际变动 |

---

## 二、核心设计原则

### 2.1 业界最佳实践

**FIS Quantum 模型**:
```
Deal (交易)
    │
    │ execute()
    ▼
Cashflow (现金流)
    │
    ├── 收支明细
    ├── 银行对账
    └── 头寸更新
```

**Murex MX.3 模型**:
```
Transaction Aggregate
    │
    ├── Deal (交易单据)
    │   └── 审批流程
    │
    └── Cashflow[] (现金流数组)
        ├── 本金流
        ├── 利息流
        └── 费用流
```

### 2.2 设计原则

1. **Deal是业务意图** - 用户发起、审批、执行
2. **Cashflow是执行结果** - 实际资金变动
3. **Deal产生Cashflow** - 一对多关系
4. **Cashflow独立生命周期** - 清分、对账
5. **Stockflow独立于Cashflow** - 证券变动记录

---

## 三、分离后的业务对象

### 3.1 ACDeal (AC交易)

**定义**: 用户录入的AC业务单据，描述一笔AC业务的完整信息

| 字段 | 类型 | 说明 |
|------|------|------|
| dealId | BIGINT | 主键 |
| dealNumber | VARCHAR(50) | 交易编号，格式: AC + yyyyMMdd + 序号(4位) |
| businessUnit | VARCHAR(50) | 业务单元 |
| bankAccount | VARCHAR(50) | 银行账户 |
| counterpartyId | BIGINT | 交易对手 |
| instrumentId | BIGINT | 金融工具 |
| direction | VARCHAR(10) | Inflow/Outflow |
| amount | DECIMAL(38,18) | 交易金额，精度38,18 |
| currency | VARCHAR(10) | 币种 |
| valueDate | DATE | 起息日 |
| sourceType | VARCHAR(20) | 来源：Manual/Statement/Sweep |
| sourceRef | VARCHAR(50) | 来源引用(如银行对账单号) |
| status | VARCHAR(20) | New/Submitted/Approved/Settled/Canceled |
| remark | VARCHAR(500) | 备注 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |
| updatedBy | VARCHAR(50) | 更新人 |
| updatedAt | DATETIME | 更新时间 |

**状态机**:
```
New → Submitted → Approved → Settled
                    ↓            ↓
              Rejected     Canceled
```

### 3.2 Cashflow (现金流)

**定义**: 实际现金流记录，描述一笔资金的实际进出

| 字段 | 类型 | 说明 |
|------|------|------|
| cflowId | BIGINT | 主键 |
| cflowNumber | VARCHAR(50) | 现金流编号，格式: CF + yyyyMMdd + 序号(4位) |
| dealId | BIGINT | 关联交易ID (可空，来源为银行导入时为空) |
| businessUnit | VARCHAR(50) | 业务单元 |
| bankAccount | VARCHAR(50) | 银行账户 |
| counterpartyAccount | VARCHAR(50) | 对手方账户 |
| direction | VARCHAR(10) | Inflow/Outflow |
| amount | DECIMAL(38,18) | 金额，精度38,18 |
| currency | VARCHAR(10) | 币种 |
| cflowDate | DATE | 现金流日期 |
| valueDate | DATE | 起息日 |
| sourceType | VARCHAR(20) | BankTransfer/Statement/Sweep/CashLeveling/Manual |
| sourceRef | VARCHAR(50) | 来源编号 |
| status | VARCHAR(20) | Created/Cleared/Reconciled/Canceled |
| counterpartyName | VARCHAR(200) | 对手方名称 |
| purpose | VARCHAR(500) | 用途 |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |
| updatedBy | VARCHAR(50) | 更新人 |
| updatedAt | DATETIME | 更新时间 |

**状态机**:
```
Created → Cleared → Reconciled
    ↓         ↓
 Canceled   Canceled
```

### 3.3 Stockflow (证券流)

**定义**: 实际证券变动记录，描述证券的买卖、过户等实际变动

| 字段 | 类型 | 说明 |
|------|------|------|
| sflowId | BIGINT | 主键 |
| sflowNumber | VARCHAR(50) | 证券流编号，格式: SF + yyyyMMdd + 序号(4位) |
| dealId | BIGINT | 关联交易ID (可空) |
| businessUnit | VARCHAR(50) | 业务单元 |
| accountId | VARCHAR(50) | 证券账户 |
| counterpartyAccount | VARCHAR(50) | 对手方账户 |
| direction | VARCHAR(10) | Buy/Sell/TransferIn/TransferOut |
| quantity | DECIMAL(38,18) | 数量 |
| securityCode | VARCHAR(20) | 证券代码 |
| price | DECIMAL(38,18) | 价格 |
| amount | DECIMAL(38,18) | 金额 |
| currency | VARCHAR(10) | 币种 |
| sflowDate | DATE | 证券流日期 |
| settlementDate | DATE | 结算日 |
| sourceType | VARCHAR(20) | Trade/Settlement/Transfer |
| sourceRef | VARCHAR(50) | 来源编号 |
| status | VARCHAR(20) | Created/Settled/Canceled |
| createdBy | VARCHAR(50) | 创建人 |
| createdAt | DATETIME | 创建时间 |
| updatedBy | VARCHAR(50) | 更新人 |
| updatedAt | DATETIME | 更新时间 |

**状态机**:
```
Created → Settled
    ↓
 Canceled
```

---

## 四、Deal与Cashflow的关系

### 4.1 关系类型

| 关系 | 说明 | 示例 |
|------|------|------|
| **1:N** | 一笔Deal产生多笔Cashflow | 本息分离、费用收取 |
| **N:1** | 多笔Cashflow合并清偿一笔Deal | 合并付款 |
| **独立** | Cashflow独立于Deal存在 | 银行流水导入 |

### 4.2 流转关系图

```
┌─────────────────────────────────────────────────────────┐
│                    ACDeal (交易)                        │
│                                                          │
│  New ──▶ Submitted ──▶ Approved ──▶ Settled           │
│                                                          │
└──────────────────────────┬──────────────────────────────┘
                           │
                    execute() 生成
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                 Cashflow (现金流)                        │
│                                                          │
│  Created ──▶ Cleared ──▶ Reconciled                     │
│                                                          │
│  (银行导入/手动创建的现金流无dealId)                      │
└─────────────────────────────────────────────────────────┘
```

---

## 五、使用场景

### 5.1 场景1：用户手工录入AC交易

```
用户录入AC Deal
    │
    ▼
ACDeal保存 (status=New)
    │
    ▼
提交审批
    │
    ▼
ACDeal状态变为 Submitted
    │
    ▼
审批通过 (status=Approved)
    │
    ▼
执行交易 (status=Settled)
    │
    ▼
自动生成Cashflow记录
    │
    ▼
现金流进入清分对账流程
```

### 5.2 场景2：银行流水导入

```
银行对账单导入
    │
    ▼
Cashflow自动创建 (dealId=null)
    │
    ▼
进入清分流程
    │
    ▼
清分员手工清分或自动匹配
    │
    ▼
状态变为 Cleared
    │
    ▼
对账确认
    │
    ▼
状态变为 Reconciled
```

### 5.3 场景3：本息分离

```
一笔定期存款AC Deal (本金100万，利率3%)

ACDeal: dealNumber=AC202606010001, amount=1,000,000
    │
    ▼ execute()
    │
    ├── Cashflow 本金流: cflowNumber=CF202606010001, amount=1,000,000
    │
    └── Cashflow 利息流: cflowNumber=CF202606010002, amount=30,000 (到期一次性支付)
```

---

## 六、API设计

### 6.1 ACDeal API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/ac-deals/page` | GET | 分页查询AC交易 |
| `/api/v1/dealing/ac-deals/{id}` | GET | 获取交易详情 |
| `/api/v1/dealing/ac-deals` | POST | 创建AC交易 |
| `/api/v1/dealing/ac-deals/update` | POST | 更新AC交易 |
| `/api/v1/dealing/ac-deals/{id}` | DELETE | 删除AC交易 |
| `/api/v1/dealing/ac-deals/{id}/submit` | POST | 提交审批 |
| `/api/v1/dealing/ac-deals/{id}/approve` | POST | 审批通过 |
| `/api/v1/dealing/ac-deals/{id}/reject` | POST | 审批拒绝 |
| `/api/v1/dealing/ac-deals/{id}/execute` | POST | 执行交易 |
| `/api/v1/dealing/ac-deals/{id}/cancel` | POST | 取消交易 |

### 6.2 Cashflow API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/cashflows/page` | GET | 分页查询现金流 |
| `/api/v1/dealing/cashflows/{id}` | GET | 获取现金流详情 |
| `/api/v1/dealing/cashflows` | POST | 创建现金流 |
| `/api/v1/dealing/cashflows/update` | POST | 更新现金流 |
| `/api/v1/dealing/cashflows/{id}` | DELETE | 删除现金流 |
| `/api/v1/dealing/cashflows/{id}/clear` | POST | 清分 |
| `/api/v1/dealing/cashflows/{id}/reconcile` | POST | 对账 |
| `/api/v1/dealing/cashflows/{id}/cancel` | POST | 取消 |
| `/api/v1/dealing/cashflows/import` | POST | 银行流水导入 |
| `/api/v1/dealing/cashflows/by-deal/{dealId}` | GET | 查询关联交易的现金流 |

### 6.3 Stockflow API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/stockflows/page` | GET | 分页查询证券流 |
| `/api/v1/dealing/stockflows/{id}` | GET | 获取证券流详情 |
| `/api/v1/dealing/stockflows` | POST | 创建证券流 |
| `/api/v1/dealing/stockflows/update` | POST | 更新证券流 |
| `/api/v1/dealing/stockflows/{id}` | DELETE | 删除证券流 |
| `/api/v1/dealing/stockflows/{id}/settle` | POST | 结算证券流 |
| `/api/v1/dealing/stockflows/by-deal/{dealId}` | GET | 查询关联交易的证券流 |

---

## 七、数据库设计

### 7.1 ACDeal 表

```sql
CREATE TABLE tms_ac_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,
    business_unit VARCHAR(50) NOT NULL,
    bank_account VARCHAR(50) NOT NULL,
    counterparty_id BIGINT,
    instrument_id BIGINT,
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    value_date DATE NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_ref VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'New',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_ac_deal_number ON tms_ac_deal_t(deal_number);
CREATE INDEX idx_ac_deal_status ON tms_ac_deal_t(status);
CREATE INDEX idx_ac_deal_bank ON tms_ac_deal_t(bank_account);
```

### 7.2 Cashflow 表

```sql
CREATE TABLE tms_cashflow_t (
    id BIGSERIAL PRIMARY KEY,
    cflow_number VARCHAR(50) NOT NULL UNIQUE,
    deal_id BIGINT,
    business_unit VARCHAR(50) NOT NULL,
    bank_account VARCHAR(50) NOT NULL,
    counterparty_account VARCHAR(50),
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    cflow_date DATE NOT NULL,
    value_date DATE NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_ref VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'Created',
    counterparty_name VARCHAR(200),
    purpose VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_cashflow_number ON tms_cashflow_t(cflow_number);
CREATE INDEX idx_cashflow_deal ON tms_cashflow_t(deal_id);
CREATE INDEX idx_cashflow_status ON tms_cashflow_t(status);
CREATE INDEX idx_cashflow_bank ON tms_cashflow_t(bank_account);
CREATE INDEX idx_cashflow_date ON tms_cashflow_t(cflow_date);
```

### 7.3 Stockflow 表

```sql
CREATE TABLE tms_stockflow_t (
    id BIGSERIAL PRIMARY KEY,
    sflow_number VARCHAR(50) NOT NULL UNIQUE,
    deal_id BIGINT,
    business_unit VARCHAR(50) NOT NULL,
    account_id VARCHAR(50) NOT NULL,
    counterparty_account VARCHAR(50),
    direction VARCHAR(10) NOT NULL,
    quantity DECIMAL(38,18) NOT NULL,
    security_code VARCHAR(20) NOT NULL,
    price DECIMAL(38,18) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    sflow_date DATE NOT NULL,
    settlement_date DATE,
    source_type VARCHAR(20) NOT NULL,
    source_ref VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'Created',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_stockflow_number ON tms_stockflow_t(sflow_number);
CREATE INDEX idx_stockflow_deal ON tms_stockflow_t(deal_id);
CREATE INDEX idx_stockflow_status ON tms_stockflow_t(status);
CREATE INDEX idx_stockflow_security ON tms_stockflow_t(security_code);
```

---

## 八、Deal类型说明

### 8.1 Deal类型分类

| 类型 | 代码 | 产生Cashflow | 产生Stockflow | 说明 |
|------|------|---------------|----------------|------|
| AC交易 | AC | ✅ | ❌ | 纯粹资金收付，不计息，不涉外汇 |
| AT交易 | AT | ✅ | ❌ | 账户转账 |
| FX交易 | FX | ✅ | ❌ | 外汇交易 |
| ST交易 | ST | ✅ | ✅ | 证券交易 |

**AC交易设计原则**: AC交易是最简单的资金收付交易，不涉及外汇、不计息、不涉及复杂金融产品。与货币市场、外汇、同业产品等概念完全分离。

### 8.2 交易执行时生成财务流

```
ACDeal.execute() → 生成 Cashflow (资金流)
ATDeal.execute() → 生成 Cashflow (资金流)
FXDeal.execute() → 生成 Cashflow (资金流)
STDeal.execute() → 生成 Cashflow (资金流) + Stockflow (证券流)
```

---

## 九、与现有设计的关系

### 9.1 迁移说明

| 现有概念 | 新概念 | 说明 |
|----------|--------|------|
| AC交易 | ACDeal | 交易对象 |
| 现金流 | Cashflow | 资金实际变动 |
| 证券流 | Stockflow | 证券实际变动(新增) |
| AC Instrument | 简化Instrument | 仅作为产品选择 |

### 9.2 依赖模块

| 模块 | 依赖关系 |
|------|----------|
| BusinessUnit | ACDeal.businessUnit, Cashflow.businessUnit |
| BankAccount | ACDeal.bankAccount, Cashflow.bankAccount |
| Counterparty | ACDeal.counterpartyId |
| Instrument | ACDeal.instrumentId |
| ApprovalRule | ACDeal提交时调用 |
| SecuritiesAccount | Stockflow.accountId |

---

## 十、评审要点

### 10.1 待确认问题

| 问题 | 选项 | 说明 |
|------|------|------|
| Cashflow生成时机 | 同步生成 / 异步生成 | 同步更简单，异步更灵活 |
| Cashflow清分匹配 | 基于dealId / 基于金额日期匹配 | Deal生成的无需匹配，银行导入的需要 |
| 多笔Cashflow合并清偿 | 支持 / 不支持 | 影响对账逻辑复杂度 |

### 10.2 风险

| 风险 | 影响 | 缓解 |
|------|------|------|
| 数据迁移 | 现有AC数据需要迁移 | 设计迁移脚本 |
| 前端页面重构 | AC交易页面需要拆分 | 分阶段实施 |
| API路径变更 | 旧接口需要废弃 | 保留旧接口别名 |

---

## 十一、实施建议

### Phase 1: 数据库设计
- 创建 tms_ac_deal_t 表
- 创建 tms_cashflow_t 表
- 创建 tms_stockflow_t 表

### Phase 2: 后端模块
- 实现 ACDealService
- 实现 CashflowService
- 实现 StockflowService
- 实现 Deal执行→Cashflow生成逻辑

### Phase 3: 前端页面
- ACDealList 交易列表页
- CashflowList 现金流列表页
- StockflowList 证券流列表页
- 交易录入页面

---

*BA产出 - 2026-06-01*