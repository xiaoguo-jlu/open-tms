# 利率掉期交易接口

**模块**: irs  
**版本**: v1.0  
**路径**: `/api/v1/irs-deals`

---

## 1. IRS交易列表

```
GET /api/v1/irs-deals?pageNo=1&pageSize=20&status=xxx
```

## 2. 新增IRS交易

```
POST /api/v1/irs-deals
```

```json
{
  "dealType": "固定利率换浮动",
  "notionalAmount": 10000000.00,
  "currency": "CNY",
  "startDate": "2026-04-15",
  "endDate": "2027-04-15",
  "fixedRate": 0.035,
  "floatingRateType": "LPR",
  "floatingRateSpread": 0.001,
  "paymentFrequency": "季付",
  "counterpartyId": 1,
  "accountId": 1
}
```

---

## 3. 提交审批

```
POST /api/v1/irs-deals/{id}/submit
```

---

## 4. 审批通过/驳回

```
POST /api/v1/irs-deals/{id}/approve
POST /api/v1/irs-deals/{id}/reject
```

---

## 5. 估值

```
GET /api/v1/irs-deals/{id}/valuation
```

### 响应
```json
{
  "code": 0,
  "data": {
    "dealId": 1,
    "marketValue": 150000.00,
    "pv01": 5000.00,
    "valuationDate": "2026-04-11"
  }
}
```

---

## 6. 现金流计算

```
GET /api/v1/irs-deals/{id}/cashflows
```

## 交易类型

| 类型 | 说明 |
|------|------|
| 固定利率换浮动 | 普通IRS |
| 浮动利率换固定 | 普通IRS |
| 基准互换 | 基差互换 |