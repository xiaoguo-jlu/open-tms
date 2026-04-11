# Open-TMS 基础数据模块 API 文档

**版本**: v1.0  
**日期**: 2026-04-10
**API版本**: v1

---

## 1. 业务单元管理 (Business Unit)

### 1.1 分页查询

**GET** `/api/v1/business-units/page`

**Query Parameters**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索编码/名称 |
| status | String | N | 状态 (0-停用, 1-启用) |
| pageNum | int | N | 页码，默认1 |
| pageSize | int | N | 每页条数，默认10 |

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 1.2 获取详情

**GET** `/api/v1/business-units/{id}`

**Response**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "unitCode": "BU001",
    "unitName": "北京分公司",
    "unitNameEn": "Beijing Branch",
    "legalRepresentative": "张三",
    "registeredAddress": "北京市朝阳区xxx",
    "taxNumber": "91110000xxx",
    "status": "1",
    "createdBy": "admin",
    "createdAt": "2026-04-10T10:00:00",
    "updatedBy": "admin",
    "updatedAt": "2026-04-10T10:00:00"
  }
}
```

### 1.3 新增

**POST** `/api/v1/business-units`

**Request Body**:
```json
{
  "unitCode": "BU001",
  "unitName": "北京分公司",
  "unitNameEn": "Beijing Branch",
  "legalRepresentative": "张三",
  "registeredAddress": "北京市朝阳区xxx",
  "taxNumber": "91110000xxx",
  "status": "1"
}
```

### 1.4 更新

**PUT** `/api/v1/business-units`

**Request Body**: 同新增，需包含id

### 1.5 删除

**DELETE** `/api/v1/business-units/{id}`

---

## 2. 交易员管理 (Trader)

### 2.1 分页查询

**GET** `/api/v1/traders/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索编号/姓名 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 2.2 获取详情

**GET** `/api/v1/traders/{id}`

### 2.3 新增

**POST** `/api/v1/traders`

```json
{
  "traderCode": "T001",
  "traderName": "李四",
  "traderNameEn": "Li Si",
  "department": "资金部",
  "phone": "13800138000",
  "email": "lisi@company.com",
  "status": "1"
}
```

### 2.4 更新

**PUT** `/api/v1/traders`

### 2.5 删除

**DELETE** `/api/v1/traders/{id}`

---

## 3. 币种管理 (Currency)

### 3.1 分页查询

**GET** `/api/v1/currencies/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索代码/名称 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 3.2 获取详情

**GET** `/api/v1/currencies/{id}`

### 3.3 新增

**POST** `/api/v1/currencies`

```json
{
  "currencyCode": "CNY",
  "currencyName": "人民币",
  "currencySymbol": "¥",
  "decimalPlaces": 2,
  "status": "1"
}
```

### 3.4 更新

**PUT** `/api/v1/currencies`

### 3.5 删除

**DELETE** `/api/v1/currencies/{id}`

---

## 4. 国家/地区管理 (Country)

### 4.1 分页查询

**GET** `/api/v1/countries/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索代码/名称 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 4.2 获取详情

**GET** `/api/v1/countries/{id}`

### 4.3 新增

**POST** `/api/v1/countries`

```json
{
  "countryCode": "CN",
  "countryName": "中国",
  "countryNameEn": "China",
  "timezone": "Asia/Shanghai",
  "countryCodePhone": "+86",
  "status": "1"
}
```

### 4.4 更新

**PUT** `/api/v1/countries`

### 4.5 删除

**DELETE** `/api/v1/countries/{id}`

---

## 5. 银行信息管理 (Bank)

### 5.1 分页查询

**GET** `/api/v1/banks/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索代码/名称 |
| countryCode | String | N | 国家代码 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 5.2 获取详情

**GET** `/api/v1/banks/{id}`

### 5.3 新增

**POST** `/api/v1/banks`

```json
{
  "bankCode": "B001",
  "bankName": "中国工商银行",
  "bankNameEn": "ICBC",
  "swiftCode": "ICBKCNBJ",
  "bankLineCode": "102100000000",
  "countryCode": "CN",
  "bankType": "商行",
  "status": "1"
}
```

### 5.4 更新

**PUT** `/api/v1/banks`

### 5.5 删除

**DELETE** `/api/v1/banks/{id}`

---

## 6. 交易对手管理 (Counterparty)

### 6.1 分页查询

**GET** `/api/v1/counterparties/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | N | 搜索编号/名称 |
| counterpartyType | String | N | 对手方类型 |
| countryCode | String | N | 国家代码 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 6.2 获取详情

**GET** `/api/v1/counterparties/{id}`

### 6.3 新增

**POST** `/api/v1/counterparties`

```json
{
  "counterpartyCode": "C001",
  "counterpartyName": "某公司",
  "counterpartyNameEn": "Some Company",
  "counterpartyType": "企业",
  "countryCode": "CN",
  "creditRating": "AAA",
  "externalRating": "Aa2",
  "address": "北京市xxx",
  "phone": "010-12345678",
  "status": "1"
}
```

### 6.4 更新

**PUT** `/api/v1/counterparties`

### 6.5 删除

**DELETE** `/api/v1/counterparties/{id}`

---

## 7. 对手方银行账户 (Counterparty Account)

### 7.1 分页查询

**GET** `/api/v1/counterparty-accounts/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| counterpartyId | Long | N | 对手方ID |
| keyword | String | N | 搜索编号/名称 |
| status | String | N | 状态 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 7.2 获取详情

**GET** `/api/v1/counterparty-accounts/{id}`

### 7.3 新增

**POST** `/api/v1/counterparty-accounts`

```json
{
  "accountCode": "A001",
  "counterpartyId": 1,
  "bankId": 1,
  "accountName": "某公司基本户",
  "accountNumber": "6222021234567890",
  "currencyCode": "CNY",
  "accountType": "基本户",
  "status": "1"
}
```

### 7.4 更新

**PUT** `/api/v1/counterparty-accounts`

### 7.5 删除

**DELETE** `/api/v1/counterparty-accounts/{id}`

---

## 8. 节假日管理 (Holiday)

### 8.1 分页查询

**GET** `/api/v1/holidays/page`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| countryCode | String | N | 国家代码 |
| year | Integer | N | 年份 |
| pageNum | int | N | 页码 |
| pageSize | int | N | 每页条数 |

### 8.2 获取详情

**GET** `/api/v1/holidays/{id}`

### 8.3 新增

**POST** `/api/v1/holidays`

```json
{
  "holidayDate": "2026-01-01",
  "holidayName": "元旦",
  "countryCode": "CN",
  "isAdjustment": "0",
  "remark": "假期第一天"
}
```

### 8.4 更新

**PUT** `/api/v1/holidays`

### 8.5 删除

**DELETE** `/api/v1/holidays/{id}`

---

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "error message",
  "data": null
}
```

### 未找到
```json
{
  "code": 404,
  "message": "not found",
  "data": null
}
```

---

*API文档 - v1.0*