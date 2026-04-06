# 业务单元管理接口

**模块**: basedata  
**版本**: v1.0  
**路径**: `/api/v1/business-units`

---

## 1. 列表查询

### 请求
```
GET /api/v1/business-units?pageNo=1&pageSize=20&keyword=xxx&status=1
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键字（编码/名称模糊搜索） |
| status | string | 否 | 状态筛选（0-停用 1-启用） |
| sortField | string | 否 | 排序字段，默认createdAt |
| sortOrder | string | 否 | 排序方向 asc/desc |

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "code": "BU001",
        "name": "测试单元",
        "enName": "Test Unit",
        "legalPerson": "张三",
        "address": "北京市朝阳区",
        "taxNo": "91110000xxx",
        "status": "1",
        "createdBy": "admin",
        "createdAt": "2026-04-05T10:00:00",
        "updatedBy": null,
        "updatedAt": null
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
GET /api/v1/business-units/{id}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "code": "BU001",
    "name": "测试单元",
    "enName": "Test Unit",
    "legalPerson": "张三",
    "address": "北京市朝阳区",
    "taxNo": "91110000xxx",
    "status": "1",
    "createdBy": "admin",
    "createdAt": "2026-04-05T10:00:00",
    "updatedBy": null,
    "updatedAt": null
  },
  "timestamp": 1704067200000
}
```

---

## 3. 新增

### 请求
```
POST /api/v1/business-units
Content-Type: application/json
```

### 请求体
```json
{
  "code": "BU001",
  "name": "测试单元",
  "enName": "Test Unit",
  "legalPerson": "张三",
  "address": "北京市朝阳区",
  "taxNo": "91110000xxx",
  "status": "1"
}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "timestamp": 1704067200000
}
```

### 校验规则
| 字段 | 规则 |
|------|------|
| code | 必填，最大50字符，唯一 |
| name | 必填，最大200字符 |
| enName | 最大200字符 |
| legalPerson | 最大50字符 |
| address | 最大500字符 |
| taxNo | 最大50字符 |
| status | 必填，0或1 |

---

## 4. 更新

### 请求
```
PUT /api/v1/business-units
Content-Type: application/json
```

### 请求体
```json
{
  "id": 1,
  "code": "BU001",
  "name": "测试单元",
  "enName": "Test Unit Updated",
  "legalPerson": "张三",
  "address": "北京市朝阳区",
  "taxNo": "91110000xxx",
  "status": "1"
}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 5. 删除

### 请求
```
DELETE /api/v1/business-units/{id}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "timestamp": 1704067200000
}
```

### 业务规则
- 逻辑删除，更新deleted字段为'1'
- 关联数据校验：若有下级组织或交易记录，不可删除

---

## 6. 批量删除

### 请求
```
POST /api/v1/business-units/batch-delete
Content-Type: application/json
```

### 请求体
```json
{
  "ids": [1, 2, 3]
}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 7. 导出

### 请求
```
GET /api/v1/business-units/export?keyword=xxx&status=1
```

### 响应
- 返回Excel文件下载

---

## 8. 导入

### 请求
```
POST /api/v1/business-units/import
Content-Type: multipart/form-data
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | Excel文件 |

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "successCount": 95,
    "failCount": 5,
    "failDetails": [
      {"row": 5, "message": "编码已存在"}
    ]
  },
  "timestamp": 1704067200000
}
```
