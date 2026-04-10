# 节假日管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/holidays`

---

## 1. 列表查询

### 请求
```
GET /api/v1/holidays?year=2026&countryCode=CN&pageNo=1&pageSize=20
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份，默认当前年 |
| countryCode | string | 否 | 国家代码 |
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "date": "2026-01-01",
        "name": "元旦",
        "countryCode": "CN",
        "isAdjust": "0",
        "remark": "节假日",
        "createdBy": "admin",
        "createdAt": "2026-04-05T10:00:00"
      }
    ],
    "total": 10,
    "pageNo": 1,
    "pageSize": 20
  },
  "timestamp": 1704067200000
}
```

---

## 2. 新增

### 请求
```
POST /api/v1/holidays
Content-Type: application/json
```

### 请求体
```json
{
  "date": "2026-01-01",
  "name": "元旦",
  "countryCode": "CN",
  "isAdjust": "0",
  "remark": "节假日"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| date | 必填，日期格式 |
| name | 必填，最大100字符 |
| countryCode | 必填，最大10字符 |
| isAdjust | 必填，0或1（是否调休） |
| remark | 最大500字符 |

---

## 3. 删除

### 请求
```
DELETE /api/v1/holidays/{id}
```

---

## 4. 批量删除

### 请求
```
POST /api/v1/holidays/batch-delete
```

---

## 5. 导入

### 请求
```
POST /api/v1/holidays/import
```

### 请求体
```json
{
  "successCount": 50,
  "failCount": 0
}
```
