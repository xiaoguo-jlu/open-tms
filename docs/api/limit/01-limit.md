# 流动性限额监控接口

**模块**: limit  
**版本**: v1.0  
**路径**: `/api/v1/limits`

---

## 1. 限额列表

```
GET /api/v1/limits?pageNo=1&pageSize=20&limitType=xxx&businessUnitId=xxx&status=xxx
```

## 2. 新增限额

```
POST /api/v1/limits
```

```json
{
  "limitName": "日支出限额",
  "limitType": "日限额",
  "businessUnitId": 1,
  "limitAmount": 1000000.00,
  "currency": "CNY",
  "warningThreshold": 0.8,
  "controlThreshold": 0.95,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31",
  "status": "1"
}
```

---

## 3. 更新限额

```
PUT /api/v1/limits
```

---

## 4. 删除限额

```
DELETE /api/v1/limits/{id}
```

---

## 5. 限额监控

```
GET /api/v1/limits/{id}/monitor
```

### 响应
```json
{
  "code": 0,
  "data": {
    "limitId": 1,
    "limitAmount": 1000000.00,
    "usedAmount": 750000.00,
    "availableAmount": 250000.00,
    "usageRate": 75.0,
    "warningLevel": "NORMAL",
    "dailyUsage": 500000.00,
    "monthlyUsage": 750000.00,
    "updateTime": "2026-04-11T10:00:00"
  }
}
```

---

## 6. 超限预警

```
GET /api/v1/limits/alerts?status=xxx
```

### 响应
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 1,
        "limitId": 1,
        "limitName": "日支出限额",
        "limitAmount": 1000000.00,
        "usedAmount": 980000.00,
        "usageRate": 98.0,
        "alertLevel": "CRITICAL",
        "alertTime": "2026-04-11T09:30:00",
        "status": "UNREAD"
      }
    ]
  }
}
```

---

## 7. 限额执行记录

```
GET /api/v1/limits/{id}/records?startDate=xxx&endDate=xxx
```

## 限额类型

| 类型 | 说明 |
|------|------|
| 日限额 | 每日支出上限 |
| 月限额 | 每月支出上限 |
| 单笔限额 | 单笔交易上限 |
| 集中度限额 | 对单一对手方额度 |