# Open-TMS M1-AC交易(AC Deal) PRD

**版本**: v3.0
**角色**: 产品经理 (PM)
**日期**: 2026-06-01
**状态**: 更新 - 简化设计

---

## 一、模块概述

**模块名称**: dealing - AC Deal交易管理
**功能定位**: 管理AC(Actual Cashflow)交易 - 纯粹的资金收付交易，用于资金头寸监控和流动性管理
**用户角色**: 资金管理人员、财务人员、资金经理

**设计原则**: AC交易是最简单的资金收付交易，不涉及外汇、不计息、不涉及复杂金融产品。

---

## 二、交易类型

| 类型 | 代码 | 说明 | 复杂度 |
|------|------|------|--------|
| 银行收付 | BankPayment | 银行账户的实际收付 | 简单 |
| 票据收付 | NotePayment | 票据的收付 | 简单 |
| 其他收付 | OtherPayment | 其他资金收付 | 简单 |

**简化说明**: AC交易仅为资金收付，不分类为货币市场/外汇/同业等产品类型。

---

## 三、功能清单

### 3.1 AC交易录入

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 手工录入 | 手工创建AC交易 | P0 |
| 批量导入 | Excel批量导入 | P1 |

### 3.2 AC交易字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dealId | BIGINT | 系统 | 主键 |
| dealNumber | VARCHAR(50) | 系统 | 自动生成，格式: AC + yyyyMMdd + 序号(4位) |
| managementEntity | VARCHAR(50) | Y | 关联管理主体代码 |
| bankAccount | VARCHAR(50) | Y | 银行账户编号 |
| counterpartyId | BIGINT | Y | 交易对手 |
| direction | VARCHAR(10) | Y | Inflow(流入)/Outflow(流出) |
| amount | DECIMAL(38,18) | Y | 交易金额，精度38,18 |
| currency | VARCHAR(10) | Y | 币种代码(仅限本币) |
| valueDate | DATE | Y | 起息日 |
| paymentMethod | VARCHAR(20) | N | 支付方式：转账/票据/其他 |
| remark | VARCHAR(500) | N | 备注 |
| status | VARCHAR(20) | 系统 | New/Submitted/Approved/Settled/Canceled |
| createdBy | VARCHAR(50) | 系统 | 创建人员 |
| createdAt | DATETIME | 系统 | 创建时间 |
| updatedBy | VARCHAR(50) | 系统 | 更新人员 |
| updatedAt | DATETIME | 系统 | 更新时间 |

### 3.3 AC交易状态机

```
New → Submitted → Approved → Settled
                    ↓            ↓
              Rejected     Canceled
```

| 状态 | 说明 | 可执行操作 |
|------|------|-----------|
| New | 新建 | submit, cancel |
| Submitted | 已提交 | approve, reject |
| Approved | 已审批 | execute |
| Settled | 已结算(终态) | - |
| Rejected | 已拒绝 | - |
| Canceled | 已取消 | - |

**状态流转规则**:
1. New → Submitted: 提交审批
2. Submitted → Approved: 审批通过(调用ApprovalRule)
3. Approved → Settled: 执行交易，生成Cashflow
4. Submitted → Rejected: 审批拒绝
5. New/Submitted → Canceled: 取消

### 3.4 AC交易查询

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 交易列表 | 分页展示交易记录 | P0 |
| 条件查询 | 按日期/状态/账户/对手查询 | P0 |
| 交易明细 | 查看交易详情 | P0 |
| 交易导出 | Excel导出 | P0 |

---

## 四、审批规则

### 4.1 审批流程

AC交易提交时调用ApprovalRule确定审批层级：

```
提交 → 检查ApprovalRule
    │
    ├── 需要审批 → 进入多级审批流程
    │
    └── 不需要审批 → 自动审批通过
```

### 4.2 ApprovalRule接口

```java
// 交易提交时调用
ApprovalRule rule = approvalRuleService.getRule("AC", amount);
if (rule != null && rule.getApprovalLevels() > 0) {
    // 需要审批
    deal.setStatus("Submitted");
    deal.setApprovalLevels(rule.getApprovalLevels());
} else {
    // 不需要审批，自动通过
    deal.setStatus("Approved");
}
```

---

## 五、与Cashflow的关系

### 5.1 生成关系

```
ACDeal.execute() → 生成 Cashflow
    │
    ├── Cashflow.direction = ACDeal.direction
    ├── Cashflow.amount = ACDeal.amount
    └── Cashflow.sourceRef = ACDeal.dealNumber
```

### 5.2 一对多

一笔AC交易产生一笔Cashflow（简单一对一关系）

---

## 六、API接口清单

### 6.1 AC交易管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/ac-deals/page` | GET | 分页查询交易 |
| `/api/v1/dealing/ac-deals/{id}` | GET | 获取交易详情 |
| `/api/v1/dealing/ac-deals` | POST | 创建交易 |
| `/api/v1/dealing/ac-deals/update` | POST | 更新交易 |
| `/api/v1/dealing/ac-deals/{id}` | DELETE | 删除交易 |
| `/api/v1/dealing/ac-deals/{id}/submit` | POST | 提交审批 |
| `/api/v1/dealing/ac-deals/{id}/approve` | POST | 审批通过 |
| `/api/v1/dealing/ac-deals/{id}/reject` | POST | 审批拒绝 |
| `/api/v1/dealing/ac-deals/{id}/execute` | POST | 执行交易→生成Cashflow |
| `/api/v1/dealing/ac-deals/{id}/cancel` | POST | 取消交易 |
| `/api/v1/dealing/ac-deals/export` | GET | 导出交易 |

---

## 七、与其他模块的关系

| 模块 | 关系 | 说明 |
|------|------|------|
| **Cashflow** | AC执行生成Cashflow | 一对一生成 |
| **ApprovalRule** | 审批层级判断 | 提交时调用 |
| **ManagementEntity** | 交易归属 | managementEntity字段 |
| **BankAccount** | 收付账户 | bankAccount字段 |
| **Counterparty** | 交易对手 | counterparty字段 |

---

## 八、验收标准

| 功能 | 验收条件 |
|------|----------|
| 手工录入 | 字段必填校验，保存成功，dealNumber自动生成 |
| 审批流程 | 提交后按ApprovalRule判断是否需要审批，多级审批正常 |
| 执行交易 | execute后自动生成Cashflow记录 |
| 查询导出 | 多条件组合查询正常，Excel导出正常 |
| 权限控制 | 无权限数据不可见 |

---

## 九、页面原型(FIS风格)

**参考**: FIS Quantum TMS - 专业金融系统界面风格

### 9.1 AC交易列表页
- 顶部: 筛选条件(日期范围/状态/账户/对手)
- 中部: 数据表格(分页展示)
- 支持状态操作按钮

### 9.2 AC交易录入页
- 基本信息(日期/账户/对手/方向/金额)
- 支付方式(可选)
- 备注

---

*PM产出 - M1 v3.0 (2026-06-01)*