# 对手方银行账户接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/counterparty-accounts`

---

## 1. 列表查询

### 请求
```
GET /api/v1/counterparty-accounts?pageNo=1&pageSize=20&counterpartyId=1&currency=CNY&accountType=xxx&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| counterpartyId | long | 否 | 对手方ID |
| currency | string | 否 | 币种 |
| accountType | string | 否 | 账户类型 |
| status | string | 否 | 状态筛选（0-停用 1-启用） |

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "code": "ACC001",
        "counterpartyId": 1,
        "counterpartyName": "中国平安",
        "bankId": 1,
        "bankName": "中国工商银行",
        "accountName": "中国平安保险股份有限公司",
        "accountNo": "6222021234567890",
        "currency": "CNY",
        "accountType": "基本户",
        "status": "1",
        "createdBy": "admin",
        "createdAt": "2026-04-05T10:00:00"
      }
    ],
    "total": 20,
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
GET /api/v1/counterparty-accounts/{id}
```

---

## 3. 新增

### 请求
```
POST /api/v1/counterparty-accounts
Content-Type: application/json
```

### 请求体
```json
{
  "code": "ACC001",
  "counterpartyId": 1,
  "bankId": 1,
  "accountName": "中国平安保险股份有限公司",
  "accountNo": "6222021234567890",
  "currency": "CNY",
  "accountType": "基本户",
  "status": "1"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大50字符，唯一 |
| counterpartyId | 必填，关联对手方 |
| bankId | 必填，关联银行 |
| accountName | 必填，最大200字符 |
| accountNo | 必填，最大50字符 |
| currency | 必填，最大10字符，关联币种 |
| accountType | 必填，最大20字符（基本户/一般户） |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/counterparty-accounts
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/counterparty-accounts/{id}
```

---

## 6. 批量删除

### 请求
```
POST /api/v1/counterparty-accounts/batch-delete
```

---

## 7. 导出

### 请求
```
GET /api/v1/counterparty-accounts/export
```

---

## 8. 导入

### 请求
```
POST /api/v1/counterparty-accounts/import
```

---

## 附录：通用错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| BUSINESS_ERROR | 业务异常 |
| VALIDATION_ERROR | 参数校验失败 |
| NOT_FOUND | 资源不存在 |
| DUPLICATE_CODE | 编码重复 |
| SYSTEM_ERROR | 系统异常 |

---

## 附录：通用响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```
