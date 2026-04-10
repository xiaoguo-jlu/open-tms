# 金融工具估值接口

**模块**: valuation  
**版本**: v1.0  
**路径**: `/api/v1/valuations`

---

## 1. 估值记录列表

```
GET /api/v1/valuations?instrumentId=xxx&valuationDate=xxx&pageNo=1&pageSize=20
```

## 2. 执行估值

```
POST /api/v1/valuations/execute
```

```json
{
  "instrumentIds": [1, 2, 3],
  "valuationDate": "2026-04-11"
}
```

---

## 3. 估值结果

```
GET /api/v1/valuations/{id}
```

### 响应
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "instrumentId": 1,
    "instrumentName": "USD/CNY",
    "valuationDate": "2026-04-11",
    "marketValue": 725000.00,
    "costValue": 720000.00,
    "valuationMethod": "市价法",
    "unrealizedPnl": 5000.00,
    "updateTime": "2026-04-11T18:00:00"
  }
}
```

---

## 4. 估值参数配置

```
GET /api/v1/valuations/parameters
PUT /api/v1/valuations/parameters
```

```json
{
  "discountRate": 0.035,
  "volatilitySurface": {...},
  "riskFreeRate": 0.02
}
```

---

## 5. 历史估值查询

```
GET /api/v1/valuations/history?instrumentId=xxx&startDate=xxx&endDate=xxx
```