# 交易管理接口

**模块**: dealing  
**版本**: v1.0  
**路径**: `/api/v1/deals`

---

## 1. 列表查询

### 请求
```
GET /api/v1/deals?pageNo=1&pageSize=20&keyword=xxx&dealType=xxx&status=xxx&startDate=xxx&endDate=xxx
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（编号/名称模糊搜索） |
| dealType | string | 否 | 交易类型 |
| dealSubtype | string | 否 | 交易子类型 |
| status | string | 否 | 状态筛选 |
| startDate | string | 否 | 交易开始日期 |
| endDate | string | 否 | 交易结束日期 |
| counterpartyId | long | 否 | 对手方ID |
| businessUnitId | long | 否 | 业务单元ID |

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "dealNo": "TD202604110001",
        "dealType": "DEPOSIT",
        "dealSubtype": "定期存款",
        "instrumentId": 1,
        "instrumentName": "定期存款",
        "counterpartyId": 1,
        "counterpartyName": "中国工商银行",
        "amount": 1000000.00,
        "currency": "CNY",
        "status": "DRAFT",
        "createdBy": "trader01",
        "createdAt": "2026-04-11T10:00:00"
      }
    ],
    "total": 100,
    "pageNo": 1,
    "pageSize": 20
  },
  "timestamp": 1704067200000
}
```

---

## 2. 详情查询

### 请求
```
GET /api/v1/deals/{id}
```

---

## 3. 新增交易

### 请求
```
POST /api/v1/deals
Content-Type: application/json
```

### 请求体
```json
{
  "dealType": "DEPOSIT",
  "dealSubtype": "定期存款",
  "instrumentId": 1,
  "counterpartyId": 1,
  "businessUnitId": 1,
  "amount": 1000000.00,
  "currency": "CNY",
  "valueDate": "2026-04-15",
  "maturityDate": "2027-04-15",
  "interestRate": 0.035,
  "remark": "测试交易"
}
```

---

## 4. 更新交易

### 请求
```
PUT /api/v1/deals
```

---

## 5. 删除交易

### 请求
```
DELETE /api/v1/deals/{id}
```

---

## 6. 暂存交易

### 请求
```
POST /api/v1/deals/{id}/save-draft
```

---

## 7. 提交审批

### 请求
```
POST /api/v1/deals/{id}/submit
```

---

## 8. 审批通过

### 请求
```
POST /api/v1/deals/{id}/approve
```

### 请求体
```json
{
  "comment": "审批通过"
}
```

---

## 9. 审批驳回

### 请求
```
POST /api/v1/deals/{id}/reject
```

### 请求体
```json
{
  "comment": "金额有误，请修改"
}
```

---

## 10. 撤销交易

### 请求
```
POST /api/v1/deals/{id}/cancel
```

---

## 11. 交易复制

### 请求
```
POST /api/v1/deals/{id}/copy
```

---

## 12. 批量删除

### 请求
```
POST /api/v1/deals/batch-delete
```

---

## 13. 导出

### 请求
```
GET /api/v1/deals/export?keyword=xxx&status=xxx
```

---

## 14. 导入

### 请求
```
POST /api/v1/deals/import
```

---

## 交易状态

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿 |
| PENDING_APPROVE | 待审批 |
| APPROVED | 已审批 |
| REJECTED | 已驳回 |
| SETTLED | 已结算 |
| CANCELLED | 已撤销 |

## 交易类型

| 类型 | 子类型 |
|------|--------|
| DEPOSIT | 定期存款、活期存款、大额存单、通知存款 |
| LOAN | 短期贷款、中长期贷款、信用贷款、抵押贷款 |
| FX | 即期外汇、远期外汇、NDF、外汇掉期 |
| INTERBANK | 同业存放、存放同业、同业拆借 |