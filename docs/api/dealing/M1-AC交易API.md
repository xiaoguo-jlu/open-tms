# AC 交易接口（v2.0 - DealMap 自动生成版）

**模块**: dealing
**版本**: v2.0
**日期**: 2026-06-21
**路径前缀**: `/api/v1/dealing/ac-deals` (AC 交易) `/api/v1/dealing/dealmap` (DealMap) `/api/v1/dealing/actions` (Action)
**基于**: DealMap PRD v2.0（字段精简 + Action 多对一 + 审批仅作用于 Action）

---

## 一、设计要点

### 1.1 v2.0 关键设计变更

| 变更项 | 说明 |
|--------|------|
| Action 多对一 | 一笔 Deal 可有多个独立 Action（CREATE / UPDATE / DELETE / APPROVE / REJECT） |
| DealMap 自动生成 | CREATE 时自动生成 DealMap(ActualCashflow) + Cashflow |
| 软删+新建 | UPDATE 时软删旧 DealMap，新建新 DealMap 关联新 Action |
| 级联软删 | DELETE 时级联软删 Deal / AcDeal / DealMap / Cashflow |
| 审批不触达 DealMap/Cashflow | 审批仅更新 Action.approval_status1/2 |
| AC/AT 操作精简 | 只有 save / delete / approve / reject（无 submit / execute） |
| CREATE 不生成 DealImage | 仅修改/删除生成 DealImage |
| Cashflow 反向关联 | Cashflow.dealmap_number VARCHAR(50) 指向 DealMap |

### 1.2 状态码约定

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 系统错误 |

### 1.3 通用请求头

```
Content-Type: application/json
Authorization: Bearer {token}
```

---

## 二、AC 交易核心接口

### 2.1 列表查询（分页）

#### 请求
```
GET /api/v1/dealing/ac-deals/page
```

#### Query 参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 模糊搜索 dealNumber |
| dealType | string | 否 | 交易类型（默认 AC） |
| status | string | 否 | Deal 状态（New / Approved / Canceled） |
| direction | string | 否 | Inflow / Outflow |
| managementEntity | string | 否 | 业务主体 |
| startDate | date | 否 | 交易开始日期 |
| endDate | date | 否 | 交易结束日期 |
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 10 |

#### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "dealNumber": "AC202606210001",
        "dealType": "AC",
        "managementEntity": "BU001",
        "counterpartyId": 5001,
        "counterpartyName": "工商银行",
        "instrumentId": 301,
        "traderId": 401,
        "traderName": "张三",
        "direction": "Outflow",
        "amount": 1000000.00,
        "currency": "CNY",
        "dealDate": "2026-06-21",
        "valueDate": "2026-06-21",
        "status": "New",
        "description": "对外付款",
        "remark": "测试",
        "latestActionNumber": "ACT202606210001",
        "bankAccountId": 201,
        "bankAccountName": "中行北京",
        "counterpartyAccountId": 301,
        "counterpartyAccountName": "工行对公账户",
        "paymentMethod": "TRANSFER",
        "createdBy": "zhangsan",
        "createdAt": "2026-06-21T10:00:00",
        "updatedBy": "zhangsan",
        "updatedAt": "2026-06-21T10:00:00",
        "version": 1
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "timestamp": 1704067200000
}
```

---

### 2.2 获取详情（按 ID）

#### 请求
```
GET /api/v1/dealing/ac-deals/{id}
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | Deal 主键 ID |

#### 响应（200）
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "dealNumber": "AC202606210001",
    "dealType": "AC",
    "managementEntity": "BU001",
    "direction": "Outflow",
    "amount": 1000000.00,
    "currency": "CNY",
    "bankAccountId": 201,
    "counterpartyAccountId": 301,
    "paymentMethod": "TRANSFER",
    "status": "New",
    "version": 1,
    "latestActionNumber": "ACT202606210001"
  },
  "timestamp": 1704067200000
}
```

#### 响应（404）
```json
{
  "code": 404,
  "message": "Deal not found",
  "timestamp": 1704067200000
}
```

---

### 2.3 获取详情（按 dealNumber）

#### 请求
```
GET /api/v1/dealing/ac-deals/number/{dealNumber}
```

---

### 2.4 新增 AC 交易

#### 请求
```
POST /api/v1/dealing/ac-deals
Content-Type: application/json
```

#### 请求体

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| dealType | string | 是 | 固定 "AC" |
| managementEntity | string | 是 | 业务主体编码（启用状态） |
| counterpartyId | long | 否 | 交易对手 ID |
| instrumentId | long | 否 | 金融工具 ID |
| traderId | long | 是 | 交易员 ID |
| direction | string | 是 | Inflow / Outflow |
| amount | decimal | 是 | > 0，DECIMAL(38,18) |
| currency | string | 是 | 币种代码（如 CNY、USD） |
| dealDate | date | 是 | yyyy-MM-dd |
| valueDate | date | 是 | yyyy-MM-dd，≥ dealDate |
| description | string | 否 | 描述，最大 500 字符 |
| remark | string | 否 | 备注，最大 500 字符 |
| bankAccountId | long | 是 | 本方银行账户 ID |
| counterpartyAccountId | long | 否 | 对手方账户 ID |
| paymentMethod | string | 否 | TRANSFER / CHECK / OTHER |
| operator | string | 是 | 操作人（操作员/审批人） |

#### 请求示例
```json
{
  "dealType": "AC",
  "managementEntity": "BU001",
  "counterpartyId": 5001,
  "instrumentId": 301,
  "traderId": 401,
  "direction": "Outflow",
  "amount": 1000000.00,
  "currency": "CNY",
  "dealDate": "2026-06-21",
  "valueDate": "2026-06-21",
  "description": "对外付款测试",
  "remark": "API 测试",
  "operator": "zhangsan",
  "bankAccountId": 201,
  "counterpartyAccountId": 301,
  "paymentMethod": "TRANSFER"
}
```

#### 响应（200）
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1704067200000
}
```

#### v2.0 后端处理（事务内）

```
1. INSERT Action(action_type=CREATE, approval_status1=Pending)
   action_number=ACT202606210001
2. INSERT Deal(status=New)
   deal_number=AC202606210001
3. INSERT AcDeal
4. ✅ INSERT DealMap(event_type=ActualCashflow)
   dealmap_number=DMP202606210001
   deal_number=AC202606210001
   action_number=ACT202606210001
5. ✅ INSERT Cashflow(dealmap_number=DMP202606210001)
   cflow_number=CF202606210001
6. ❌ 不生成 DealImage
```

#### 错误响应

| code | message | 触发条件 |
|------|---------|----------|
| 400 | managementEntity 不能为空 | 业务主体为空 |
| 400 | amount 必须大于 0 | 金额 ≤ 0 |
| 400 | valueDate 不能早于 dealDate | 起息日 < 交易日期 |
| 400 | bankAccountId 不能为空 | 本方账户为空 |

---

### 2.5 更新 AC 交易

#### 请求
```
POST /api/v1/dealing/ac-deals/update
Content-Type: application/json
```

#### 请求体（在新增基础上增加 dealNumber）

```json
{
  "dealNumber": "AC202606210001",
  "dealType": "AC",
  "managementEntity": "BU001",
  "traderId": 401,
  "direction": "Outflow",
  "amount": 2000000.00,
  "currency": "CNY",
  "dealDate": "2026-06-21",
  "valueDate": "2026-06-22",
  "description": "修改后描述",
  "operator": "zhangsan",
  "bankAccountId": 202,
  "counterpartyAccountId": 302,
  "paymentMethod": "TELEX"
}
```

#### v2.0 后端处理（事务内）

```
1. INSERT Action(action_type=UPDATE, approval_status1=Pending)
   action_number=ACT202606210002
   deal_number=AC202606210001（与 CREATE 的 Action 独立）
2. UPDATE Deal（修改字段）
3. UPDATE AcDeal
4. ✅ 软删除旧 DealMap
   UPDATE tms_deal_map_t SET deleted='1'
   WHERE deal_number='AC202606210001' AND deleted='0'
5. ✅ INSERT 新 DealMap(ActualCashflow) - 关联新 Action
   dealmap_number=DMP202606210002
   action_number=ACT202606210002
6. ✅ UPDATE Cashflow
   UPDATE tms_cashflow_t SET dealmap_number='DMP202606210002'
   WHERE dealmap_number='DMP202606210001'
   ⚠️ 软删除的 DealMap 不会被 Cashflow 引用
7. ✅ INSERT DealImage(v+1) - 记录修改前字段旧值
```

---

### 2.6 删除 AC 交易

#### 请求
```
POST /api/v1/dealing/ac-deals/delete/{id}
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | Deal 主键 ID |

#### v2.0 后端处理（事务内）

```
1. INSERT Action(action_type=DELETE, approval_status1=Pending)
   action_number=ACT202606210003
2. ✅ 软删除 Deal
   UPDATE tms_deals_t SET deleted='1'
   WHERE deal_number='AC202606210001'
3. ✅ 软删除 AcDeal
4. ✅ 级联软删除 DealMap
   UPDATE tms_deal_map_t SET deleted='1'
   WHERE deal_number='AC202606210001' AND deleted='0'
5. ✅ 软删除 Cashflow（级联）
   UPDATE tms_cashflow_t SET deleted='1'
   WHERE dealmap_number IN (...) AND deleted='0'
6. ✅ INSERT DealImage(v+1) - 记录删除前完整状态
```

⚠️ **数据保留**：DealMap / Cashflow 数据仍保留在数据库中（deleted='1'），用于审计追溯。

#### 响应（200）
```json
{"code": 200, "message": "success", "data": null, "timestamp": 1704067200000}
```

---

## 三、Action 审批接口（v2.0 关键）

### 3.1 查询某 Deal 的所有 Action

#### 请求
```
GET /api/v1/dealing/actions/by-deal/{dealNumber}
```

#### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "actionNumber": "ACT202606210001",
      "dealNumber": "AC202606210001",
      "actionType": "CREATE",
      "actionStatus": "Pending",
      "approvalStatus1": "Pending",
      "approvalStatus2": "Pending",
      "operator": "zhangsan",
      "operateAt": "2026-06-21T10:00:00",
      "createdAt": "2026-06-21T10:00:00"
    },
    {
      "id": 2,
      "actionNumber": "ACT202606210002",
      "dealNumber": "AC202606210001",
      "actionType": "UPDATE",
      "approvalStatus1": "Pending",
      "operator": "lisi",
      "operateAt": "2026-06-21T14:30:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 3.2 审批通过（基于 Action）

#### 请求
```
POST /api/v1/dealing/actions/{actionNumber}/approve
Content-Type: application/json
```

#### 请求体
```json
{
  "approver": "manager01",
  "approvalRemark": "审批通过"
}
```

#### v2.0 关键行为
```
UPDATE tms_actions_t
SET approval_status1 = 'Approved',
    approver1 = 'manager01',
    approval_remark = '审批通过'
WHERE action_number = 'ACT202606210001'

⚠️ 关键：审批不改变 DealMap / Cashflow 的任何状态
   DealMap.event_status 始终保持 Active
   Cashflow.status 始终保持 Created
```

#### 响应（200）
```json
{"code": 200, "message": "success", "data": null, "timestamp": 1704067200000}
```

---

### 3.3 审批驳回（基于 Action）

#### 请求
```
POST /api/v1/dealing/actions/{actionNumber}/reject
Content-Type: application/json
```

#### 请求体
```json
{
  "approver": "manager01",
  "approvalRemark": "金额有误，请重新核对"
}
```

#### v2.0 关键行为
```
UPDATE tms_actions_t
SET approval_status1 = 'Rejected',
    approver1 = 'manager01',
    approval_remark = '金额有误'
WHERE action_number = 'ACT202606210001'

⚠️ DealMap / Cashflow 状态依然不变
```

#### 错误响应

| code | message | 触发条件 |
|------|---------|----------|
| 400 | 审批意见必填 | 驳回时 approvalRemark 为空 |
| 404 | Action 不存在 | actionNumber 不存在 |

---

### 3.4 查询待审批 Action 列表

#### 请求
```
GET /api/v1/dealing/actions/pending?pageNum=1&pageSize=20
```

#### Query 参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approver | string | 否 | 审批人 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页条数 |

#### 响应
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "actionNumber": "ACT202606210001",
        "dealNumber": "AC202606210001",
        "actionType": "CREATE",
        "approvalStatus1": "Pending",
        "operator": "zhangsan",
        "operateAt": "2026-06-21T10:00:00"
      }
    ],
    "total": 5,
    "size": 20,
    "current": 1
  }
}
```

---

## 四、DealMap 时间线接口

### 4.1 查询 DealMap 列表（按 deal）

#### 请求
```
GET /api/v1/dealing/dealmap/by-deal/{dealNumber}
```

#### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "dealmapNumber": "DMP202606210001",
      "dealNumber": "AC202606210001",
      "actionNumber": "ACT202606210001",
      "eventType": "ActualCashflow",
      "eventStatus": "Active",
      "amount": 1000000.00,
      "currency": "CNY",
      "direction": "Outflow",
      "eventDate": "2026-06-21",
      "valueDate": "2026-06-21",
      "isReversal": "0",
      "reversesEventId": null,
      "reversedByEventId": null,
      "description": "AC Deal created - actual cashflow event",
      "deleted": "0",
      "createdBy": "zhangsan",
      "createdAt": "2026-06-21T10:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

#### 排序
按 `event_date ASC, created_at ASC` 排序，形成业务事件时间线。

---

### 4.2 查询 DealMap 详情

#### 请求
```
GET /api/v1/dealing/dealmap/{id}
```

---

### 4.3 DealMap 分页查询

#### 请求
```
GET /api/v1/dealing/dealmap/page?eventType=ActualCashflow&pageNum=1&pageSize=20
```

#### Query 参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dealNumber | string | 否 | 关联 Deal 编号 |
| eventType | string | 否 | 事件类型 |
| eventStatus | string | 否 | Active / Inactive |
| startDate | date | 否 | 起始日期 |
| endDate | date | 否 | 截止日期 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页条数 |

---

### 4.4 DealMap 冲销

#### 请求
```
POST /api/v1/dealing/dealmap/{id}/reverse
```

#### 请求体
```json
{
  "operator": "manager01",
  "remark": "冲销原事件"
}
```

#### 后端处理
```
1. INSERT 新 DealMap
   is_reversal='1'
   reverses_event_id=原DealMap.id
2. UPDATE 原 DealMap
   reversed_by_event_id=新DealMap.id
   event_status='Inactive'
```

---

## 五、DealImage 接口

### 5.1 查询 Deal 镜像列表

#### 请求
```
GET /api/v1/dealing/images/by-deal/{dealNumber}
```

#### 响应
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "imageNumber": "IMG202606210001",
      "dealNumber": "AC202606210001",
      "version": 1,
      "imageType": "UPDATE",
      "amount": 1000000.00,
      "direction": "Outflow",
      "operator": "zhangsan",
      "operateAt": "2026-06-21T14:30:00",
      "createdAt": "2026-06-21T14:30:00"
    },
    {
      "id": 2,
      "imageNumber": "IMG202606210002",
      "dealNumber": "AC202606210001",
      "version": 2,
      "imageType": "DELETE",
      "amount": 2000000.00,
      "operator": "zhangsan",
      "operateAt": "2026-06-22T10:00:00"
    }
  ]
}
```

⚠️ v2.0 注意：CREATE 不生成 DealImage。AC 交易的 DealImage 仅在 UPDATE / DELETE 时生成。

---

## 六、错误码

| code | 含义 | 典型触发 |
|------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 必填字段为空、值不合法 |
| 404 | 资源不存在 | DealNumber / ActionNumber 不存在 |
| 500 | 系统错误 | 数据库异常、事务回滚 |

---

## 七、附录：事件类型字典

| event_type | 中文名 | 适用场景 |
|-----------|--------|----------|
| ActualCashflow | 实际现金流 | AC / AT / FX / Deposit / Loan |
| ExpectedCashflow | 预期现金流 | Deposit / Loan / Bond / IRS |
| AccountTransfer | 账户间资金划转 | AT |
| CashLeveling | 资金池归集 | Cashpool |
| RateSet | 利率设定 | Deposit / Loan / IRS |
| RateFix | 利率重置 | Loan / IRS / FloatingBond |
| Coupon | 付息 | Bond / IRS / Deposit |
| InterestAccrual | 利息计提 | Deposit / Loan / Bond |
| Unwind | 平仓 | FX / IRS / Option / Swap |
| Rollover | 续作 | Deposit / Loan |
| MTM | 市值评估 | FX / IRS / Bond / Option |

---

## 八、附录：Action 类型字典

| action_type | 触发场景 | 关联 DealMap |
|-------------|----------|--------------|
| CREATE | 保存新交易 | INSERT DealMap(ActualCashflow) |
| UPDATE | 修改交易 | 软删旧 + INSERT 新 DealMap |
| DELETE | 删除交易 | 级联软删 DealMap |
| APPROVE | 审批通过 Action | ❌ 不影响 DealMap |
| REJECT | 审批驳回 Action | ❌ 不影响 DealMap |

---

*API 产出 - v2.0 (2026-06-21)*
*基于 DealMap PRD v2.0 + UX 原型 v1.0*
