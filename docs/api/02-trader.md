# 交易员管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/traders`

---

## 1. 列表查询

### 请求
```
GET /api/v1/traders?pageNo=1&pageSize=20&keyword=xxx&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（编号/姓名模糊搜索） |
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
        "code": "TR001",
        "name": "李四",
        "enName": "Li Si",
        "department": "资金部",
        "phone": "13800138000",
        "email": "lisi@company.com",
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
GET /api/v1/traders/{id}
```

---

## 3. 新增

### 请求
```
POST /api/v1/traders
Content-Type: application/json
```

### 请求体
```json
{
  "code": "TR001",
  "name": "李四",
  "enName": "Li Si",
  "department": "资金部",
  "phone": "13800138000",
  "email": "lisi@company.com",
  "status": "1"
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大50字符，唯一 |
| name | 必填，最大50字符 |
| enName | 最大50字符 |
| department | 最大100字符 |
| phone | 最大30字符 |
| email | 最大100字符，邮箱格式 |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/traders
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/traders/{id}
```

---

## 6. 批量删除

### 请求
```
POST /api/v1/traders/batch-delete
```

---

## 7. 导出

### 请求
```
GET /api/v1/traders/export
```

---

## 8. 导入

### 请求
```
POST /api/v1/traders/import
```
