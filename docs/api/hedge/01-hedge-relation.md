# 套期保值接口

**模块**: hedge  
**版本**: v1.0  
**路径**: `/api/v1/hedge-relations`

---

## 1. 套期关系列表

```
GET /api/v1/hedge-relations?pageNo=1&pageSize=20&hedgeType=xxx&status=xxx
```

## 2. 新增套期关系

```
POST /api/v1/hedge-relations
```

```json
{
  "hedgeName": "USD存款套期",
  "hedgeType": "公允价值套期",
  "hedgeInstrumentId": 1,
  "hedgeInstrumentName": "远期外汇",
  "hedgedItemId": 2,
  "hedgedItemName": "USD定期存款",
  "hedgeRatio": 1.0,
  "startDate": "2026-04-01",
  "endDate": "2027-04-01",
  "status": "1"
}
```

---

## 3. 更新套期关系

```
PUT /api/v1/hedge-relations
```

---

## 4. 终止套期

```
POST /api/v1/hedge-relations/{id}/terminate
```

---

## 5. 套期有效性测试

```
GET /api/v1/hedge-relations/{id}/effectiveness
```

### 响应
```json
{
  "code": 0,
  "data": {
    "relationId": 1,
    "testDate": "2026-04-11",
    "hedgeEffectiveness": 95.5,
    "result": "EFFECTIVE",
    "changeInFairValue": 10000.00,
    "changeInHedgedItem": 9800.00,
    "ratio": 98.0
  }
}
```

---

## 6. 套期损益

```
GET /api/v1/hedge-relations/{id}/pnl?startDate=xxx&endDate=xxx
```

## 套期类型

| 类型 | 说明 |
|------|------|
| 公允价值套期 | Fair Value Hedge |
| 现金流套期 | Cash Flow Hedge |
| 境外经营套期 | Net Investment Hedge |