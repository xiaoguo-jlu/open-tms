# 币种管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/currencies`

---

## 1. 列表查询

### 请求
```
GET /api/v1/currencies?pageNo=1&pageSize=20&keyword=xxx&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（代码/名称模糊搜索） |
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
        "code": "CNY",
        "name": "人民币",
        "symbol": "¥",
        "decimalPlaces": 2,
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
GET /api/v1/currencies/{id}
```

---

## 3. 新增

### 请求
```
POST /api/v1/currencies
Content-Type: application/json
```

### 请求体
```json
{
  "code": "CNY",
  "name": "人民币",
  "symbol": "¥",
  "decimalPlaces": 2,
  "status": "1"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大10字符，唯一，ISO代码 |
| name | 必填，最大50字符 |
| symbol | 最大10字符 |
| decimalPlaces | 必填，0-4位小数 |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/currencies
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/currencies/{id}
```

---

## 6. 批量删除

### 请求
```
POST /api/v1/currencies/batch-delete
```

---

## 7. 导出

### 请求
```
GET /api/v1/currencies/export
```

---

## 8. 导入

### 请求
```
POST /api/v1/currencies/import
```

### 请求体
```json
{
  "successCount": 95,
  "failCount": 5,
  "failDetails": [
    {"row": 5, "message": "编码已存在"}
  ]
}
```
