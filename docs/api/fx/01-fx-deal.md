# 外汇交易接口

**模块**: fx  
**版本**: v1.0  
**路径**: `/api/v1/fx-deals`

---

## 1. 外汇交易列表

```
GET /api/v1/fx-deals?pageNo=1&pageSize=20&fxType=xxx&status=xxx
```

## 2. 新增外汇交易

```
POST /api/v1/fx-deals
```

```json
{
  "fxType": "即期外汇",
  "buyCurrency": "USD",
  "sellCurrency": "CNY",
  "amount": 100000.00,
  "rate": 7.25,
  "counterpartyId": 1,
  "accountId": 1,
  "valueDate": "2026-04-15",
  "remark": "测试"
}
```

---

## 3. 提交审批

```
POST /api/v1/fx-deals/{id}/submit
```

---

## 4. 审批通过/驳回

```
POST /api/v1/fx-deals/{id}/approve
POST /api/v1/fx-deals/{id}/reject
```

---

## 5. 执行交易

```
POST /api/v1/fx-deals/{id}/execute
```

---

## 6. 平仓

```
POST /api/v1/fx-deals/{id}/close
```

---

## 外汇类型

| 类型 | 说明 |
|------|------|
| 即期外汇 | SPOT |
| 远期外汇 | FORWARD |
| NDF | 无本金交割远期 |
| 外汇掉期 | FX SWAP |