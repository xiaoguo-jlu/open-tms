# 交易对手管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/counterparties`

---

## 1. 列表查询

### 请求
```
GET /api/v1/counterparties?pageNo=1&pageSize=20&keyword=xxx&type=xxx&countryCode=CN&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（编号/名称模糊搜索） |
| type | string | 否 | 对手方类型 |
| countryCode | string | 否 | 所属国家 |
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
        "code": "CP001",
        "name": "中国平安",
        "enName": "Ping An Insurance",
        "type": "保险公司",
        "countryCode": "CN",
        "creditRating": "AAA",
        "extRating": "Aa3",
        "address": "深圳市福田区",
        "phone": "95511",
        "status": "1",
        "createdBy": "admin",
        "createdAt": "2026-04-05T10:00:00"
      }
    ],
    "total": 50,
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
GET /api/v1/counterparties/{id}
```

---

## 3. 新增

### 请求
```
POST /api/v1/counterparties
Content-Type: application/json
```

### 请求体
```json
{
  "code": "CP001",
  "name": "中国平安",
  "enName": "Ping An Insurance",
  "type": "保险公司",
  "countryCode": "CN",
  "creditRating": "AAA",
  "extRating": "Aa3",
  "address": "深圳市福田区",
  "phone": "95511",
  "status": "1"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大50字符，唯一 |
| name | 必填，最大200字符 |
| enName | 最大200字符 |
| type | 必填，最大20字符（银行/企业/券商/保险公司） |
| countryCode | 必填，最大10字符 |
| creditRating | 最大10字符 |
| extRating | 最大10字符 |
| address | 最大500字符 |
| phone | 最大30字符 |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/counterparties
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/counterparties/{id}
```

### 业务规则
- 有关联账户时不可删除

---

## 6. 批量删除

### 请求
```
POST /api/v1/counterparties/batch-delete
```

---

## 7. 导出

### 请求
```
GET /api/v1/counterparties/export
```

---

## 8. 导入

### 请求
```
POST /api/v1/counterparties/import
```
