# AT 交易（账户间资金调拨）接口

**模块**: dealing
**子模块**: AT (Account Transfer)
**版本**: v1.0
**日期**: 2026-06-21
**路径前缀**: `/api/v1/dealing/at-deals`
**依据 PRD**: `M1-DealMapPRD-v2.md` (renamed from `M1-DealMap 生命周期事件PRD.md`)

---

## 〇、修订记录

| 版本 | 日期 | 修订内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-06-21 | 初版，基于 DealMap 生命周期 PRD v2.0 设计 | API 架构师 |

---

## 一、概述

### 1.1 背景

AT（Account Transfer，账户间资金调拨）是 Open-TMS 交易管理模块的核心交易类型之一，用于记录集团内部或外部的账户间资金划转。与 AC（实际现金流）单腿交易不同，**AT 在一笔交易中包含两条腿**：

- **源账户腿（Source Leg）**：资金流出方
- **目标账户腿（Destination Leg）**：资金流入方

### 1.2 AT 与 AC 的核心差异

| 维度 | AC（实际现金流） | AT（账户转账） |
|------|------------------|----------------|
| 交易腿数 | 1 条（单腿） | 2 条（双腿：Source + Dest） |
| DealMap 生成数 | 1 条（ActualCashflow） | 4 条（2 TRANSFER + 2 CASHFLOW） |
| Cashflow 生成数 | 1 条 | 2 条 |
| 跨币种支持 | 同币种 | 支持（exchange_rate 字段） |
| 金额关系 | - | source_amount × exchange_rate = dest_amount |
| 业务字段 | bank_account_id, counterparty_account_id | source_account_id, dest_account_id, transferType |

### 1.3 DealMap 自动生成机制（关键）

| 操作 | Action | DealMap 变化 | Cashflow 变化 | DealImage |
|------|--------|--------------|---------------|-----------|
| **创建** | INSERT Action(CREATE) | INSERT 4 条（2 TRANSFER + 2 CASHFLOW） | INSERT 2 条 | ❌ 不生成 |
| **修改** | INSERT Action(UPDATE) | 软删旧 4 条 + INSERT 新 4 条 | UPDATE 2 条的 dealmap_number | INSERT(v+1) |
| **删除** | INSERT Action(DELETE) | 级联软删 4 条 | 级联软删 2 条 | INSERT(v+1) |
| **审批** | UPDATE Action.approval_status1/2 | ❌ 不变 | ❌ 不变 | ❌ 不变 |
| **驳回** | UPDATE Action.approval_status1/2=Rejected | ❌ 不变 | ❌ 不变 | ❌ 不变 |

**双腿 DealMap 自动生成详情**：

```
AT 创建事务：
├─ ① Action(CREATE)         ──┐
├─ ② Deal(status=New)        ──┤
├─ ③ AtDeal                  ──┤
├─ ④ DealMap(TRANSFER, SourceLeg)  ─┤  dealmap_number=DMP202606210001
│     amount=sourceAmount, currency=sourceCurrency, direction=Outflow
├─ ⑤ DealMap(TRANSFER, DestLeg)    ─┤  dealmap_number=DMP202606210002
│     amount=destAmount, currency=destCurrency, direction=Inflow
├─ ⑥ DealMap(CASHFLOW, SourceLeg)  ─┤  dealmap_number=DMP202606210003
├─ ⑦ DealMap(CASHFLOW, DestLeg)    ─┤  dealmap_number=DMP202606210004
├─ ⑧ Cashflow(Source)        ──┤    dealmap_number=DMP202606210003
└─ ⑨ Cashflow(Dest)          ──┘    dealmap_number=DMP202606210004
```

---

## 二、接口清单

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/api/v1/dealing/at-deals/page` | 分页查询 AT 列表 |
| 2 | GET | `/api/v1/dealing/at-deals/{id}` | 获取 AT 详情 |
| 3 | GET | `/api/v1/dealing/at-deals/number/{dealNumber}` | 通过 dealNumber 获取详情 |
| 4 | POST | `/api/v1/dealing/at-deals` | 创建 AT 交易（自动生成 4 DealMap + 2 Cashflow） |
| 5 | POST | `/api/v1/dealing/at-deals/update` | 更新 AT 交易（软删旧 4 DealMap + 新建 4 + UPDATE 2 Cashflow + INSERT DealImage） |
| 6 | POST | `/api/v1/dealing/at-deals/{id}/delete` | 删除 AT 交易（级联软删 Deal + AtDeal + 4 DealMap + 2 Cashflow + INSERT DealImage） |
| 7 | GET | `/api/v1/dealing/at-deals/{dealNumber}/dealmap` | 查询 AT 的双腿 DealMap 时间线（4 条） |
| 8 | GET | `/api/v1/dealing/at-deals/{dealNumber}/cashflow` | 查询 AT 的 Cashflow 列表（2 条） |
| 9 | GET | `/api/v1/dealing/at-deals/{dealNumber}/actions` | 查询 AT 的所有 Action |
| 10 | GET | `/api/v1/dealing/at-deals/{dealNumber}/images` | 查询 AT 的镜像快照列表 |
| 11 | POST | `/api/v1/dealing/at-deals/actions/{actionNumber}/approve` | 审批通过 Action |
| 12 | POST | `/api/v1/dealing/at-deals/actions/{actionNumber}/reject` | 驳回 Action |

---

## 三、详细接口定义

---

### 3.1 分页查询 AT 列表

### 请求
```
GET /api/v1/dealing/at-deals/page
Content-Type: application/json
```

### 参数（Query）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 20 |
| keyword | string | 否 | 关键字（dealNumber / purpose 模糊搜索） |
| transferType | string | 否 | 转账类型：INTERNAL / CROSS_BORDER / INTERBANK |
| status | string | 否 | 状态：New / Pending / Approved / Rejected / Deleted |
| managementEntity | string | 否 | 管理主体编码 |
| sourceAccountId | long | 否 | 源账户 ID |
| destAccountId | long | 否 | 目标账户 ID |
| startDate | string | 否 | 起息日开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 起息日结束日期（yyyy-MM-dd） |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1001,
        "dealNumber": "AT202606210001",
        "transferType": "CROSS_BORDER",
        "managementEntity": "BU001",
        "sourceAccountId": 201,
        "sourceAccountName": "招行纽约 USD账户",
        "destAccountId": 301,
        "destAccountName": "德银法兰克福 EUR账户",
        "sourceAmount": 1000000.00,
        "sourceCurrency": "USD",
        "destAmount": 145000.00,
        "destCurrency": "EUR",
        "exchangeRate": 0.145,
        "valueDate": "2026-06-22",
        "paymentMethod": "SWIFT",
        "status": "New",
        "operator": "test_user",
        "createdAt": "2026-06-21T10:00:00"
      }
    ],
    "total": 50,
    "pageNo": 1,
    "pageSize": 20
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 500 | 系统错误 |

---

### 3.2 获取 AT 详情（按 ID）

### 请求
```
GET /api/v1/dealing/at-deals/{id}
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | AT 主键 ID |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "dealNumber": "AT202606210001",
    "transferType": "CROSS_BORDER",
    "managementEntity": "BU001",
    "sourceAccountId": 201,
    "sourceAccountName": "招行纽约 USD账户",
    "destAccountId": 301,
    "destAccountName": "德银法兰克福 EUR账户",
    "sourceAmount": 1000000.00,
    "sourceCurrency": "USD",
    "destAmount": 145000.00,
    "destCurrency": "EUR",
    "exchangeRate": 0.145,
    "valueDate": "2026-06-22",
    "paymentMethod": "SWIFT",
    "purpose": "跨境资金调拨",
    "status": "New",
    "operator": "test_user",
    "latestActionNumber": "ACT202606210001",
    "createdBy": "test_user",
    "createdAt": "2026-06-21T10:00:00",
    "updatedBy": null,
    "updatedAt": null,
    "version": 0
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.3 获取 AT 详情（按 dealNumber）

### 请求
```
GET /api/v1/dealing/at-deals/number/{dealNumber}
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dealNumber | string | 是 | AT 交易编号（如 AT202606210001） |

### 响应
同 3.2 接口。

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.4 创建 AT 交易（核心）

### 请求
```
POST /api/v1/dealing/at-deals
Content-Type: application/json
```

### 请求体（AtDealDTO）

```json
{
  "managementEntity": "BU001",
  "transferType": "CROSS_BORDER",
  "sourceAccountId": 201,
  "destAccountId": 301,
  "sourceAmount": 1000000.00,
  "destAmount": 145000.00,
  "sourceCurrency": "USD",
  "destCurrency": "EUR",
  "exchangeRate": 0.145,
  "valueDate": "2026-06-22",
  "paymentMethod": "SWIFT",
  "purpose": "跨境资金调拨",
  "operator": "test_user"
}
```

### 参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| managementEntity | string | 是 | 管理主体编码 |
| transferType | string | 是 | 转账类型：INTERNAL / CROSS_BORDER / INTERBANK |
| sourceAccountId | long | 是 | 源账户 ID |
| destAccountId | long | 是 | 目标账户 ID（不能等于 sourceAccountId） |
| sourceAmount | decimal | 是 | 源币种金额（精度 38,18） |
| destAmount | decimal | 是 | 目标币种金额（精度 38,18） |
| sourceCurrency | string | 是 | 源币种代码 |
| destCurrency | string | 是 | 目标币种代码 |
| exchangeRate | decimal | 否 | 汇率；同币种时 = 1；跨币种时必填 |
| valueDate | string | 是 | 起息日（yyyy-MM-dd） |
| paymentMethod | string | 否 | 支付方式：SWIFT / TT / INTERNAL |
| purpose | string | 否 | 转账用途 |
| operator | string | 是 | 操作人 |

### 后端处理逻辑（事务）

```
① INSERT Action(action_type=CREATE, approval_status1=Pending)
   action_number=ACT202606210001
   deal_number=AT202606210001 (创建时先生成 deal_number)

② INSERT Deal(deal_type='AT', status='New', latest_action_number=ACT202606210001)

③ INSERT AtDeal
   deal_number, transfer_type, source_account_id, dest_account_id,
   source_amount, dest_amount, source_currency, dest_currency,
   exchange_rate, value_date, payment_method, purpose

④ ✅ INSERT DealMap(TRANSFER, SourceLeg)   ← 双腿 DealMap 自动生成
   dealmap_number=DMP202606210001
   deal_number=AT202606210001
   action_number=ACT202606210001
   event_type='AccountTransfer'
   amount=1000000.00, currency='USD', direction='Outflow'
   event_status='Active'
   description='AT transfer - source leg outflow'

⑤ ✅ INSERT DealMap(TRANSFER, DestLeg)
   dealmap_number=DMP202606210002
   amount=145000.00, currency='EUR', direction='Inflow'
   description='AT transfer - dest leg inflow'

⑥ ✅ INSERT DealMap(CASHFLOW, SourceLeg)
   dealmap_number=DMP202606210003
   event_type='ActualCashflow'
   amount=1000000.00, currency='USD', direction='Outflow'

⑦ ✅ INSERT DealMap(CASHFLOW, DestLeg)
   dealmap_number=DMP202606210004
   amount=145000.00, currency='EUR', direction='Inflow'

⑧ ✅ INSERT Cashflow(Source)
   cflow_number=CF202606210001
   dealmap_number=DMP202606210003
   direction='Outflow', amount=1000000.00, currency='USD'

⑨ ✅ INSERT Cashflow(Dest)
   cflow_number=CF202606210002
   dealmap_number=DMP202606210004
   direction='Inflow', amount=145000.00, currency='EUR'

⑩ ❌ 不生成 DealImage
```

### 业务校验

| 校验项 | 错误码 | 错误消息 |
|--------|--------|----------|
| sourceAccountId == destAccountId | 400 | 同账户禁止转账 |
| 跨币种（sourceCurrency != destCurrency）且 exchangeRate 为空/0 | 400 | 跨币种必须填写汇率 |
| 同币种且 exchangeRate != 1 | 400 | 同币种汇率必须为 1 |
| abs(sourceAmount * exchangeRate - destAmount) > 0.01 | 400 | 金额校验失败：source × rate ≠ dest |
| valueDate < 当前日期 | 400 | 起息日不能早于今天 |
| 账户不存在 / 账户状态异常 | 400 | 账户不可用 |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dealNumber": "AT202606210001",
    "status": "New",
    "createdAt": "2026-06-21T10:00:00",
    "dealmapCount": 4,
    "cashflowCount": 2
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 业务校验失败（同账户、金额错误、账户不可用等） |
| 500 | 系统错误 |

---

### 3.5 更新 AT 交易（核心）

### 请求
```
POST /api/v1/dealing/at-deals/update
Content-Type: application/json
```

### 请求体（AtDealDTO，需带 id / dealNumber）

```json
{
  "id": 1001,
  "dealNumber": "AT202606210001",
  "managementEntity": "BU001",
  "transferType": "CROSS_BORDER",
  "sourceAccountId": 201,
  "destAccountId": 302,
  "sourceAmount": 1100000.00,
  "destAmount": 159500.00,
  "sourceCurrency": "USD",
  "destCurrency": "EUR",
  "exchangeRate": 0.145,
  "valueDate": "2026-06-23",
  "paymentMethod": "SWIFT",
  "purpose": "跨境资金调拨-修订",
  "operator": "test_user"
}
```

### 后端处理逻辑（事务）

```
① INSERT Action(action_type=UPDATE, approval_status1=Pending)
   action_number=ACT202606210002
   deal_number=AT202606210001

② UPDATE Deal（修改 latest_action_number 等元字段）

③ UPDATE AtDeal（修改所有业务字段）

④ 软删旧 4 条 DealMap
   UPDATE tms_deal_map_t SET deleted='1', updated_at=NOW()
   WHERE deal_number='AT202606210001' AND deleted='0'

⑤ ✅ INSERT 新 DealMap(TRANSFER, SourceLeg)
   dealmap_number=DMP202606210005
   action_number=ACT202606210002
   amount=1100000.00 (新值)

⑥ ✅ INSERT 新 DealMap(TRANSFER, DestLeg)
   dealmap_number=DMP202606210006
   amount=159500.00 (新值)

⑦ ✅ INSERT 新 DealMap(CASHFLOW, SourceLeg)
   dealmap_number=DMP202606210007

⑧ ✅ INSERT 新 DealMap(CASHFLOW, DestLeg)
   dealmap_number=DMP202606210008

⑨ UPDATE Cashflow（指向新 DealMap）
   UPDATE tms_cashflow_t SET dealmap_number='DMP202606210007'
   WHERE dealmap_number='DMP202606210003'
   UPDATE tms_cashflow_t SET dealmap_number='DMP202606210008'
   WHERE dealmap_number='DMP202606210004'

⑩ ✅ INSERT DealImage(v+1)
   version=2, image_type='UPDATE'
   snapshot_data=修改前的完整 AtDeal JSON
```

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dealNumber": "AT202606210001",
    "status": "New",
    "updatedAt": "2026-06-21T11:00:00",
    "newActionNumber": "ACT202606210002",
    "newDealmapCount": 4,
    "imageVersion": 2
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 业务校验失败 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.6 删除 AT 交易（核心）

### 请求
```
POST /api/v1/dealing/at-deals/{id}/delete
Content-Type: application/json
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | AT 主键 ID |

### 请求体（可选）
```json
{
  "remark": "误操作删除"
}
```

### 后端处理逻辑（事务）

```
① INSERT Action(action_type=DELETE, approval_status1=Pending)
   action_number=ACT202606210003
   deal_number=AT202606210001

② 软删除 Deal
   UPDATE tms_deals_t SET deleted='1', updated_at=NOW()
   WHERE id=1001

③ 软删除 AtDeal
   UPDATE tms_ac_deals_t SET deleted='1'  -- AcDeal/AtDeal 同表
   WHERE deal_number='AT202606210001'

④ ✅ 级联软删除 4 条 DealMap
   UPDATE tms_deal_map_t SET deleted='1'
   WHERE deal_number='AT202606210001' AND deleted='0'

⑤ 软删除 2 条 Cashflow（级联）
   UPDATE tms_cashflow_t SET deleted='1'
   WHERE dealmap_number IN (
       SELECT dealmap_number FROM tms_deal_map_t
       WHERE deal_number='AT202606210001'
   )

⑥ ✅ INSERT DealImage(v+1)
   image_type='DELETE'
   snapshot_data=删除前的完整 AT 状态
```

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dealNumber": "AT202606210001",
    "deletedAt": "2026-06-21T12:00:00",
    "imageVersion": 3
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.7 查询 AT 的双腿 DealMap 时间线

### 请求
```
GET /api/v1/dealing/at-deals/{dealNumber}/dealmap
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dealNumber | string | 是 | AT 交易编号 |

### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| includeSoftDeleted | boolean | 否 | 是否包含软删除记录，默认 false |

### 响应（典型场景：4 条 DealMap）

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10001,
      "dealmapNumber": "DMP202606210001",
      "dealNumber": "AT202606210001",
      "actionNumber": "ACT202606210001",
      "eventType": "AccountTransfer",
      "eventStatus": "Active",
      "leg": "SOURCE",
      "amount": 1000000.00,
      "currency": "USD",
      "direction": "Outflow",
      "eventDate": "2026-06-22",
      "valueDate": "2026-06-22",
      "description": "AT transfer - source leg outflow",
      "createdAt": "2026-06-21T10:00:00"
    },
    {
      "id": 10002,
      "dealmapNumber": "DMP202606210002",
      "eventType": "AccountTransfer",
      "eventStatus": "Active",
      "leg": "DEST",
      "amount": 145000.00,
      "currency": "EUR",
      "direction": "Inflow",
      "eventDate": "2026-06-22",
      "valueDate": "2026-06-22",
      "description": "AT transfer - dest leg inflow"
    },
    {
      "id": 10003,
      "dealmapNumber": "DMP202606210003",
      "eventType": "ActualCashflow",
      "eventStatus": "Active",
      "leg": "SOURCE",
      "amount": 1000000.00,
      "currency": "USD",
      "direction": "Outflow",
      "description": "AT cashflow - source side"
    },
    {
      "id": 10004,
      "dealmapNumber": "DMP202606210004",
      "eventType": "ActualCashflow",
      "eventStatus": "Active",
      "leg": "DEST",
      "amount": 145000.00,
      "currency": "EUR",
      "direction": "Inflow",
      "description": "AT cashflow - dest side"
    }
  ],
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.8 查询 AT 的 Cashflow 列表

### 请求
```
GET /api/v1/dealing/at-deals/{dealNumber}/cashflow
```

### 响应（典型场景：2 条 Cashflow）

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 20001,
      "cflowNumber": "CF202606210001",
      "dealNumber": "AT202606210001",
      "dealmapNumber": "DMP202606210003",
      "managementEntity": "BU001",
      "bankAccount": "BANK_ACC_201",
      "counterpartyAccount": "BANK_ACC_301",
      "direction": "Outflow",
      "amount": 1000000.00,
      "currency": "USD",
      "cflowDate": "2026-06-22",
      "valueDate": "2026-06-22",
      "sourceType": "AT_DEAL",
      "sourceRef": "AT202606210001",
      "status": "Created"
    },
    {
      "id": 20002,
      "cflowNumber": "CF202606210002",
      "dealmapNumber": "DMP202606210004",
      "direction": "Inflow",
      "amount": 145000.00,
      "currency": "EUR",
      "sourceType": "AT_DEAL",
      "sourceRef": "AT202606210001",
      "status": "Created"
    }
  ],
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.9 查询 AT 的所有 Action

### 请求
```
GET /api/v1/dealing/at-deals/{dealNumber}/actions
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 30001,
      "actionNumber": "ACT202606210001",
      "dealNumber": "AT202606210001",
      "actionType": "CREATE",
      "approver1": null,
      "approver2": null,
      "approvalStatus1": "Pending",
      "approvalStatus2": "Pending",
      "approvalRemark": null,
      "operator": "test_user",
      "operateAt": "2026-06-21T10:00:00",
      "remark": null
    },
    {
      "id": 30002,
      "actionNumber": "ACT202606210002",
      "actionType": "UPDATE",
      "approvalStatus1": "Pending",
      "operator": "test_user",
      "operateAt": "2026-06-21T11:00:00"
    },
    {
      "id": 30003,
      "actionNumber": "ACT202606210003",
      "actionType": "DELETE",
      "approvalStatus1": "Pending",
      "operator": "test_user",
      "operateAt": "2026-06-21T12:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.10 查询 AT 的镜像快照列表

### 请求
```
GET /api/v1/dealing/at-deals/{dealNumber}/images
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 40001,
      "imageNumber": "IMG202606210002",
      "dealNumber": "AT202606210001",
      "version": 1,
      "imageType": "UPDATE",
      "triggerActionNumber": "ACT202606210002",
      "snapshotData": "{...修改前的完整 AtDeal JSON...}",
      "createdBy": "test_user",
      "createdAt": "2026-06-21T11:00:00"
    },
    {
      "id": 40002,
      "imageNumber": "IMG202606210003",
      "version": 2,
      "imageType": "DELETE",
      "triggerActionNumber": "ACT202606210003",
      "snapshotData": "{...删除前的完整状态...}",
      "createdAt": "2026-06-21T12:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

**说明**：CREATE 操作不生成 DealImage，因此 v1 不存在。

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 404 | AT 不存在 |
| 500 | 系统错误 |

---

### 3.11 审批通过 Action

### 请求
```
POST /api/v1/dealing/at-deals/actions/{actionNumber}/approve
Content-Type: application/json
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| actionNumber | string | 是 | Action 编号 |

### 请求体
```json
{
  "approverLevel": 1,
  "approver": "manager01",
  "remark": "审批通过"
}
```

### 参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approverLevel | int | 否 | 审批级别 1 或 2，默认 1 |
| approver | string | 是 | 审批人 |
| remark | string | 否 | 审批备注 |

### 后端处理逻辑

```
仅更新 Action 审批状态：
  UPDATE tms_actions_t
  SET approver1 = 'manager01',
      approval_status1 = 'Approved',
      approval_remark = '审批通过',
      updated_at = NOW()
  WHERE action_number = 'ACT202606210001'

⚠️ 关键：审批不影响 DealMap / Cashflow 任何状态
   DealMap.event_status 始终保持 Active
   Cashflow.status 始终保持 Created
```

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "actionNumber": "ACT202606210001",
    "approvalStatus1": "Approved",
    "approvedAt": "2026-06-21T13:00:00"
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | Action 已被审批（不可重复审批） |
| 404 | Action 不存在 |
| 500 | 系统错误 |

---

### 3.12 驳回 Action

### 请求
```
POST /api/v1/dealing/at-deals/actions/{actionNumber}/reject
Content-Type: application/json
```

### 参数（Path）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| actionNumber | string | 是 | Action 编号 |

### 请求体
```json
{
  "approverLevel": 1,
  "approver": "manager01",
  "remark": "金额有误，请重新提交"
}
```

### 后端处理逻辑

```
仅更新 Action 审批状态：
  UPDATE tms_actions_t
  SET approval_status1 = 'Rejected',
      approval_remark = '金额有误，请重新提交'
  WHERE action_number = 'ACT202606210001'

⚠️ 关键：驳回不影响 DealMap / Cashflow 任何状态
```

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "actionNumber": "ACT202606210001",
    "approvalStatus1": "Rejected",
    "rejectedAt": "2026-06-21T13:00:00"
  },
  "timestamp": 1704067200000
}
```

### 错误码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | Action 已被审批（不可重复驳回） |
| 404 | Action 不存在 |
| 500 | 系统错误 |

---

## 四、数据结构

### 4.1 AtDealDTO（请求体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 否 | 更新时必填 |
| dealNumber | string | 否 | 更新时必填 |
| managementEntity | string | 是 | 管理主体编码 |
| transferType | string | 是 | INTERNAL / CROSS_BORDER / INTERBANK |
| sourceAccountId | long | 是 | 源账户 ID |
| destAccountId | long | 是 | 目标账户 ID |
| sourceAmount | decimal(38,18) | 是 | 源币种金额 |
| destAmount | decimal(38,18) | 是 | 目标币种金额 |
| sourceCurrency | string | 是 | ISO 4217 币种代码 |
| destCurrency | string | 是 | ISO 4217 币种代码 |
| exchangeRate | decimal(38,18) | 否 | 汇率；同币种=1，跨币种必填 |
| valueDate | string | 是 | 起息日 yyyy-MM-dd |
| paymentMethod | string | 否 | SWIFT / TT / INTERNAL |
| purpose | string | 否 | 转账用途 |
| operator | string | 是 | 操作人 |

### 4.2 AtDealVO（响应体）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| dealNumber | string | AT 交易编号 |
| transferType | string | 转账类型 |
| managementEntity | string | 管理主体 |
| sourceAccountId | long | 源账户 ID |
| sourceAccountName | string | 源账户名称 |
| destAccountId | long | 目标账户 ID |
| destAccountName | string | 目标账户名称 |
| sourceAmount | decimal | 源币种金额 |
| sourceCurrency | string | 源币种 |
| destAmount | decimal | 目标币种金额 |
| destCurrency | string | 目标币种 |
| exchangeRate | decimal | 汇率 |
| valueDate | string | 起息日 |
| paymentMethod | string | 支付方式 |
| purpose | string | 转账用途 |
| status | string | 状态 |
| operator | string | 操作人 |
| latestActionNumber | string | 最新 Action 编号 |
| createdBy / createdAt | string | 创建信息 |
| updatedBy / updatedAt | string | 修改信息 |
| version | int | 乐观锁版本号 |

### 4.3 DealMapVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| dealmapNumber | string | DealMap 编号 |
| dealNumber | string | 关联 Deal 编号 |
| actionNumber | string | 触发 Action 编号 |
| eventType | string | AccountTransfer / ActualCashflow |
| eventStatus | string | Active / Inactive |
| leg | string | SOURCE / DEST（AT 特有字段） |
| amount | decimal | 金额 |
| currency | string | 币种 |
| direction | string | Inflow / Outflow |
| eventDate | string | 事件日期 |
| valueDate | string | 起息日 |
| description | string | 描述 |
| createdAt | string | 创建时间 |

### 4.4 CashflowVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| cflowNumber | string | Cashflow 编号 |
| dealNumber | string | 关联 Deal 编号 |
| dealmapNumber | string | **反向关联 DealMap 编号（v2.0 新增）** |
| managementEntity | string | 管理主体 |
| bankAccount | string | 银行账户 |
| counterpartyAccount | string | 对手/目标账户 |
| direction | string | Inflow / Outflow |
| amount | decimal | 金额 |
| currency | string | 币种 |
| cflowDate | string | 现金流日期 |
| valueDate | string | 起息日 |
| sourceType | string | 来源类型：AT_DEAL / AC_DEAL / FX_DEAL |
| sourceRef | string | 来源引用（Deal 编号） |
| status | string | Created / Posted / Cancelled |

### 4.5 ActionVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| actionNumber | string | Action 编号 |
| dealNumber | string | 关联 Deal 编号 |
| actionType | string | CREATE / UPDATE / DELETE / APPROVE / REJECT |
| approver1 / approver2 | string | 审批人 |
| approvalStatus1 / approvalStatus2 | string | Pending / Approved / Rejected |
| approvalRemark | string | 审批备注 |
| operator | string | 操作人 |
| operateAt | string | 操作时间 |
| remark | string | 备注 |

### 4.6 DealImageVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| imageNumber | string | 镜像编号 |
| dealNumber | string | 关联 Deal 编号 |
| version | int | 版本号（从 1 开始） |
| imageType | string | UPDATE / DELETE |
| triggerActionNumber | string | 触发 Action 编号 |
| snapshotData | text | 完整快照 JSON |
| createdBy / createdAt | string | 创建信息 |

---

## 五、业务规则

### 5.1 AT 创建业务规则

| # | 规则 | 校验位置 |
|---|------|----------|
| 1 | sourceAccountId ≠ destAccountId（同账户禁止转账） | 前端 + 后端 |
| 2 | 跨币种（sourceCurrency ≠ destCurrency）时 exchangeRate 必填 | 前端 + 后端 |
| 3 | 同币种时 exchangeRate = 1 | 前端 + 后端 |
| 4 | abs(sourceAmount × exchangeRate − destAmount) ≤ 0.01 | 前端 + 后端 |
| 5 | valueDate ≥ 当前日期 | 前端 + 后端 |
| 6 | 源/目标账户状态必须为 Active | 后端 |
| 7 | 同一 BU 下账户允许跨账户转账，跨 BU 需审批 | 后端 |

### 5.2 AT 修改业务规则

| # | 规则 |
|---|------|
| 1 | 修改后生成新的 Action(UPDATE) |
| 2 | 旧 4 条 DealMap 软删除（deleted='1'），新建 4 条新 DealMap |
| 3 | 2 条 Cashflow 的 dealmap_number 字段更新为新 DealMap 编号 |
| 4 | 生成 DealImage(v+1) 记录修改前快照 |
| 5 | 仅 status ∈ {New, Rejected} 的 AT 可被修改 |
| 6 | 已 Approved 的 AT 需先创建新 UPDATE Action 走审批流程 |

### 5.3 AT 删除业务规则

| # | 规则 |
|---|------|
| 1 | 删除生成 Action(DELETE) |
| 2 | Deal / AtDeal / 4 DealMap / 2 Cashflow 全部软删除（deleted='1'） |
| 3 | 生成 DealImage(v+1) 记录删除前完整状态 |
| 4 | 仅 status ∈ {New, Pending, Rejected} 的 AT 可被删除 |
| 5 | 已 Approved 的 AT 不可直接删除，需走冲销流程 |

### 5.4 审批业务规则

| # | 规则 |
|---|------|
| 1 | 审批仅作用于 Action（UPDATE Action.approval_status1/2） |
| 2 | 审批不改变 DealMap.event_status（始终 Active） |
| 3 | 审批不改变 Cashflow.status（始终 Created） |
| 4 | 同一 Action 不能重复审批或驳回 |
| 5 | 一级审批通过后状态为 Approved，驳回后状态为 Rejected |

### 5.5 双腿 DealMap 自动生成规则（AT 特有）

| # | 规则 |
|---|------|
| 1 | CREATE 自动生成 4 条 DealMap（2 TRANSFER + 2 CASHFLOW） |
| 2 | 2 条 TRANSFER 记录资金划转事实（AccountTransfer event_type） |
| 3 | 2 条 CASHFLOW 记录现金流事实（ActualCashflow event_type） |
| 4 | SOURCE leg: amount=sourceAmount, currency=sourceCurrency, direction=Outflow |
| 5 | DEST leg: amount=destAmount, currency=destCurrency, direction=Inflow |
| 6 | UPDATE 软删旧 4 条 + 新建新 4 条 |
| 7 | DELETE 级联软删 4 条 |
| 8 | CREATE 不生成 DealImage；UPDATE/DELETE 生成 DealImage(v+1) |

---

## 六、错误码

| HTTP code | 业务 code | 说明 |
|-----------|-----------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 参数错误 / 业务校验失败 |
| 400 | 40001 | 同账户禁止转账 |
| 400 | 40002 | 跨币种必须填写汇率 |
| 400 | 40003 | 同币种汇率必须为 1 |
| 400 | 40004 | 金额校验失败 |
| 400 | 40005 | 起息日无效 |
| 400 | 40006 | 账户不可用 |
| 400 | 40007 | Action 状态不允许该操作 |
| 404 | 404 | 资源不存在（AT / Action） |
| 500 | 500 | 系统错误 |

---

## 七、AT vs AC 接口差异

| 维度 | AC 接口 | AT 接口 | 差异原因 |
|------|---------|---------|----------|
| 路径 | `/api/v1/dealing/ac-deals` | `/api/v1/dealing/at-deals` | 不同子资源 |
| 请求字段 | counterpartyId, amount, currency | sourceAccountId, destAccountId, sourceAmount, destAmount, exchangeRate | AT 双腿 + 跨币种 |
| 创建 DealMap 数 | 1（ActualCashflow） | 4（2 TRANSFER + 2 CASHFLOW） | AT 双腿 |
| 创建 Cashflow 数 | 1 | 2 | AT 双腿 |
| 删除 DealMap 数 | 1 | 4 | AT 双腿 |
| 修改 Cashflow 行为 | UPDATE 1 条 dealmap_number | UPDATE 2 条 dealmap_number | AT 双腿 |
| 跨币种支持 | 否 | 是（exchangeRate 字段） | AT 支持跨境调拨 |
| 业务校验 | counterpartyId ≠ 0 | sourceAccountId ≠ destAccountId | AT 内部转账 |

---

## 八、跨模块接口契约

### 8.1 对内契约

| 调用方 | 提供方 | 契约 | 说明 |
|--------|--------|------|------|
| AT 控制器 | basedata-账户 | 校验账户状态 | GET /api/v1/bank-accounts/{id}/active |
| AT 控制器 | basedata-币种 | 校验币种有效性 | GET /api/v1/currencies/{code}/valid |
| AT 控制器 | basedata-管理主体 | 校验 BU 存在 | GET /api/v1/management-entities/{code} |

### 8.2 对外契约（v1.0 不开放）

| 接口 | 方法 | 计划开放版本 |
|------|------|--------------|
| `/api/v1/dealing/at-deals/{dealNumber}/images/{imageNumber}/restore` | POST | v1.1（DealImage 恢复） |
| `/api/v1/dealing/at-deals/{dealNumber}/dealmap/{dealmapNumber}/reverse` | POST | v1.1（DealMap 冲销） |

---

## 九、参考文档

- [M1-DealMap 生命周期事件 PRD v2.0](../../prd/M1/M1-DealMap%20生命周期事件PRD.md)
- [交易管理接口 AC v1.0](./01-deal.md)
- [Open-TMS 全模块接口文档](../README.md)
- [Open-TMS 开发规范文档](../../规范/Open-TMS开发规范文档.md)

---

## 十、版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-06-21 | 初版，基于 DealMap PRD v2.0 设计 | API 架构师 |

---

*API 产出 - v1.0 (2026-06-21)*
*核心特性：AT 双腿交易；自动生成 4 DealMap + 2 Cashflow；UPDATE 软删+新建；CREATE 不生成 DealImage*