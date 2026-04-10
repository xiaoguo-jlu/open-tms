# 支付结算接口

**模块**: settlement  
**版本**: v1.0  
**路径**: `/api/v1/settlements`

---

## 1. 支付指令列表

```
GET /api/v1/settlements?pageNo=1&pageSize=20&status=xxx&startDate=xxx&endDate=xxx
```

## 2. 新增支付指令

```
POST /api/v1/settlements
```

```json
{
  "settlementType": "支付",
  "accountId": 1,
  "payeeId": 1,
  "payeeName": "供应商A",
  "payeeBank": "中国工商银行",
  "payeeAccountNo": "6222021234567890",
  "amount": 100000.00,
  "currency": "CNY",
  "purpose": "货款",
  "executeDate": "2026-04-15"
}
```

---

## 3. 提交审批

```
POST /api/v1/settlements/{id}/submit
```

---

## 4. 审批通过

```
POST /api/v1/settlements/{id}/approve
```

---

## 5. 审批驳回

```
POST /api/v1/settlements/{id}/reject
```

---

## 6. 执行支付

```
POST /api/v1/settlements/{id}/execute
```

---

## 7. 支付结果查询

```
GET /api/v1/settlements/{id}/result
```

### 响应
```json
{
  "code": 0,
  "data": {
    "settlementId": 1,
    "status": "SUCCESS",
    "amount": 100000.00,
    "executeTime": "2026-04-11T14:30:00",
    "bankSerialNo": "BK202604110001"
  }
}
```

---

## 8. 批量支付

```
POST /api/v1/settlements/batch-execute
```

---

## 状态

| 状态 | 说明 |
|------|------|
| PENDING | 待审批 |
| APPROVED | 已审批 |
| EXECUTING | 执行中 |
| SUCCESS | 成功 |
| FAILED | 失败 |