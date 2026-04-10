# 现金池管理接口

**模块**: cashpool  
**版本**: v1.0  
**路径**: `/api/v1/cash-pools`

---

## 1. 现金池列表

```
GET /api/v1/cash-pools?pageNo=1&pageSize=20&keyword=xxx&poolType=xxx&status=xxx
```

## 2. 新增现金池

```
POST /api/v1/cash-pools
```

```json
{
  "poolName": "集团现金池",
  "poolType": "虚拟池",
  "businessUnitId": 1,
  "memberAccounts": [1, 2, 3],
  "autoTransfer": "1",
  "thresholdAmount": 100000.00,
  "status": "1"
}
```

---

## 3. 更新现金池

```
PUT /api/v1/cash-pools
```

---

## 4. 删除现金池

```
DELETE /api/v1/cash-pools/{id}
```

---

## 5. 成员账户管理

```
GET /api/v1/cash-pools/{id}/members
```

```
POST /api/v1/cash-pools/{id}/members
```

```
DELETE /api/v1/cash-pools/{id}/members/{memberId}
```

---

## 6. 自动调拨规则

```
GET /api/v1/cash-pools/{id}/rules
```

```
POST /api/v1/cash-pools/{id}/rules
```

```json
{
  "ruleName": "自动上划",
  "triggerType": "阈值",
  "thresholdAmount": 500000.00,
  "direction": "UP",
  "status": "1"
}
```

---

## 7. 手动调拨

```
POST /api/v1/cash-pools/{id}/transfer
```

```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100000.00,
  "currency": "CNY",
  "remark": "测试调拨"
}
```

---

## 8. 调拨记录

```
GET /api/v1/cash-pools/{id}/transfers?startDate=xxx&endDate=xxx
```

---

## 9. 池余额查询

```
GET /api/v1/cash-pools/{id}/balance
```

```json
{
  "code": 0,
  "data": {
    "poolId": 1,
    "poolName": "集团现金池",
    "totalBalance": 5000000.00,
    "availableBalance": 4500000.00,
    "frozenBalance": 500000.00,
    "memberBalances": [
      {"accountId": 1, "accountName": "账户1", "balance": 2000000.00},
      {"accountId": 2, "accountName": "账户2", "balance": 3000000.00}
    ]
  }
}
```

---

## 池类型

| 类型 | 说明 |
|------|------|
| 物理池 | 实际资金归集 |
| 虚拟池 | 额度共享 |