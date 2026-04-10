# 银行账户管理接口

**模块**: bankaccount  
**版本**: v1.0  
**路径**: `/api/v1/bank-accounts`

---

## 1. 银行账户列表

### 请求
```
GET /api/v1/bank-accounts?pageNo=1&pageSize=20&keyword=xxx&bankId=xxx&accountType=xxx&status=xxx
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码 |
| pageSize | int | 否 | 每页条数 |
| keyword | string | 否 | 关键字 |
| bankId | long | 否 | 银行ID |
| accountType | string | 否 | 账户类型 |
| status | string | 否 | 状态 |

### 响应
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 1,
        "accountNo": "6222021234567890",
        "accountName": "测试公司基本户",
        "bankId": 1,
        "bankName": "中国工商银行",
        "accountType": "基本户",
        "currency": "CNY",
        "balance": 1000000.00,
        "availableBalance": 1000000.00,
        "status": "1",
        "isMain": "1"
      }
    ],
    "total": 50,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

---

## 2. 新增账户

### 请求
```
POST /api/v1/bank-accounts
```

```json
{
  "accountNo": "6222021234567890",
  "accountName": "测试公司基本户",
  "bankId": 1,
  "accountType": "基本户",
  "currency": "CNY",
  "isMain": "1",
  "status": "1"
}
```

---

## 3. 更新账户

### 请求
```
PUT /api/v1/bank-accounts
```

---

## 4. 删除账户

### 请求
```
DELETE /api/v1/bank-accounts/{id}
```

---

## 5. 账户详情

### 请求
```
GET /api/v1/bank-accounts/{id}
```

---

## 6. 获取余额

### 请求
```
GET /api/v1/bank-accounts/{id}/balance
```

### 响应
```json
{
  "code": 0,
  "data": {
    "accountId": 1,
    "balance": 1000000.00,
    "availableBalance": 1000000.00,
    "frozenBalance": 0.00,
    "updateTime": "2026-04-11T10:00:00"
  }
}
```

---

## 7. 获取流水

### 请求
```
GET /api/v1/bank-accounts/{id}/transactions?startDate=xxx&endDate=xxx&pageNo=1&pageSize=20
```

---

## 8. 批量删除

### 请求
```
POST /api/v1/bank-accounts/batch-delete
```

---

## 账户类型

| 类型 | 说明 |
|------|------|
| 基本户 | 基本存款账户 |
| 一般户 | 一般存款账户 |
| 专户 | 专用存款账户 |
| 临时户 | 临时存款账户 |