# 资金计划管理接口

**模块**: fundplan  
**版本**: v1.0  
**路径**: `/api/v1/fund-plans`

---

## 1. 年度计划列表

### 请求
```
GET /api/v1/fund-plans/annual?pageNo=1&pageSize=20&year=2026&businessUnitId=xxx&status=xxx
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份 |
| businessUnitId | long | 否 | 业务单元ID |
| status | string | 否 | 状态 |

### 响应
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 1,
        "planYear": 2026,
        "businessUnitId": 1,
        "businessUnitName": "测试公司",
        "totalIncome": 10000000.00,
        "totalExpense": 8000000.00,
        "netCashFlow": 2000000.00,
        "status": "DRAFT",
        "createdBy": "admin",
        "createdAt": "2026-04-11T10:00:00"
      }
    ],
    "total": 10,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

---

## 2. 新增年度计划

```
POST /api/v1/fund-plans/annual
```

```json
{
  "planYear": 2026,
  "businessUnitId": 1,
  "totalIncome": 10000000.00,
  "totalExpense": 8000000.00,
  "planItems": [
    {"category": "销售收入", "amount": 5000000.00, "monthlyPlans": [500000, 500000, ...]},
    {"category": "采购支出", "amount": 3000000.00, "monthlyPlans": [300000, 300000, ...]}
  ]
}
```

---

## 3. 提交审批

```
POST /api/v1/fund-plans/annual/{id}/submit
```

---

## 4. 审批通过

```
POST /api/v1/fund-plans/annual/{id}/approve
```

---

## 5. 审批驳回

```
POST /api/v1/fund-plans/annual/{id}/reject
```

---

## 6. 锁定计划

```
POST /api/v1/fund-plans/annual/{id}/lock
```

---

## 7. 月度计划列表

```
GET /api/v1/fund-plans/monthly?annualPlanId=xxx&month=4&status=xxx
```

---

## 8. 月度计划调整

```
PUT /api/v1/fund-plans/monthly/{id}
```

---

## 9. 执行追踪

```
GET /api/v1/fund-plans/{id}/execution?month=4
```

### 响应
```json
{
  "code": 0,
  "data": {
    "planId": 1,
    "month": 4,
    "plannedIncome": 800000.00,
    "actualIncome": 750000.00,
    "incomeRate": 93.75,
    "plannedExpense": 600000.00,
    "actualExpense": 620000.00,
    "expenseRate": 103.33
  }
}
```

---

## 10. 完成率统计

```
GET /api/v1/fund-plans/{id}/completion-rate?startMonth=1&endMonth=12
```

## 状态

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿 |
| PENDING_APPROVE | 待审批 |
| APPROVED | 已审批 |
| LOCKED | 已锁定 |