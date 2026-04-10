# 敞口管理接口

**模块**: exposure  
**版本**: v1.0  
**路径**: `/api/v1/exposures`

---

## 1. 敞口列表

```
GET /api/v1/exposures?pageNo=1&pageSize=20&exposureType=xxx&currency=xxx&businessUnitId=xxx
```

## 2. 实时敞口

```
GET /api/v1/exposures/realtime
```

### 响应
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "exposureType": "外汇敞口",
        "currency": "USD",
        "longAmount": 1000000.00,
        "shortAmount": 800000.00,
        "netAmount": 200000.00,
        "equivalentCNY": 1450000.00
      },
      {
        "exposureType": "利率敞口",
        "currency": "CNY",
        "longAmount": 50000000.00,
        "shortAmount": 30000000.00,
        "netAmount": 20000000.00
      }
    ]
  }
}
```

---

## 3. 敞口监控

```
GET /api/v1/exposures/monitor?businessUnitId=xxx
```

---

## 4. 敞口限额检查

```
GET /api/v1/exposures/{id}/limit-check
```

### 响应
```json
{
  "code": 0,
  "data": {
    "exposureId": 1,
    "limitAmount": 5000000.00,
    "currentAmount": 3500000.00,
    "usageRate": 70.0,
    "limitStatus": "NORMAL"
  }
}
```

---

## 5. 敞口历史

```
GET /api/v1/exposures/history?startDate=xxx&endDate=xxx
```

## 敞口类型

| 类型 | 说明 |
|------|------|
| 外汇敞口 | FX Exposure |
| 利率敞口 | Interest Rate Exposure |
| 信用敞口 | Credit Exposure |
| 流动性敞口 | Liquidity Exposure |