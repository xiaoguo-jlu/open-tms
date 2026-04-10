# 银行信息管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/banks`

---

## 1. 列表查询

### 请求
```
GET /api/v1/banks?pageNo=1&pageSize=20&keyword=xxx&countryCode=CN&bankType=xxx&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（代码/名称模糊搜索） |
| countryCode | string | 否 | 所属国家 |
| bankType | string | 否 | 银行类型 |
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
        "code": "BANK001",
        "name": "中国工商银行",
        "enName": "Industrial and Commercial Bank of China",
        "swiftCode": "ICBKCNBJ",
        "bankNo": "102100000000",
        "countryCode": "CN",
        "bankType": "商行",
        "status": "1",
        "createdBy": "admin",
        "createdAt": "2026-04-05T10:00:00"
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
GET /api/v1/banks/{id}
```

---

## 3. 新增

### 请求
```
POST /api/v1/banks
Content-Type: application/json
```

### 请求体
```json
{
  "code": "BANK001",
  "name": "中国工商银行",
  "enName": "Industrial and Commercial Bank of China",
  "swiftCode": "ICBKCNBJ",
  "bankNo": "102100000000",
  "countryCode": "CN",
  "bankType": "商行",
  "status": "1"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大50字符，唯一 |
| name | 必填，最大200字符 |
| enName | 最大200字符 |
| swiftCode | 最大11字符 |
| bankNo | 最大20字符 |
| countryCode | 必填，最大10字符 |
| bankType | 最大20字符（央行/商行/外资） |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/banks
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/banks/{id}
```

### 业务规则
- 有关联账户时不可删除

---

## 6. 批量删除

### 请求
```
POST /api/v1/banks/batch-delete
```

---

## 7. 导出

### 请求
```
GET /api/v1/banks/export
```

---

## 8. 导入

### 请求
```
POST /api/v1/banks/import
```
