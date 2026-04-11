# 基础数据模块接口

**模块**: basedata  
**版本**: v1.1  
**路径**: `/api/v1/currencies`

---

## 1. 币种管理

### 1.1 列表查询

#### 请求
```
GET /api/v1/currencies
```

#### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "CNY",
      "name": "人民币",
      "symbol": "¥",
      "decimalPlaces": 2,
      "status": "1",
      "createdAt": "2026-04-11 10:00:00",
      "updatedAt": "2026-04-11 10:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

### 1.2 分页查询

#### 请求
```
GET /api/v1/currencies/page?keyword=CNY&status=1&pageNo=1&pageSize=20
```

#### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 关键字（编码/名称模糊搜索） |
| status | string | 否 | 状态筛选 |
| pageNo | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |

### 1.3 根据ID查询

#### 请求
```
GET /api/v1/currencies/{id}
```

#### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "code": "CNY",
    "name": "人民币",
    "symbol": "¥",
    "decimalPlaces": 2,
    "status": "1"
  }
}
```

### 1.4 新增币种

#### 请求
```
POST /api/v1/currencies
Content-Type: application/json

{
  "code": "USD",
  "name": "美元",
  "symbol": "$",
  "decimalPlaces": 2,
  "status": "1",
  "remark": "美元主货币"
}
```

### 1.5 更新币种

#### 请求
```
PUT /api/v1/currencies
Content-Type: application/json

{
  "id": 1,
  "code": "USD",
  "name": "美元",
  "symbol": "$",
  "decimalPlaces": 2,
  "status": "1",
  "remark": "备注"
}
```

### 1.6 删除币种

#### 请求
```
DELETE /api/v1/currencies/{id}
```

---

## 通用响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

## 通用错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| BUSINESS_ERROR | 业务异常 |
| VALIDATION_ERROR | 参数校验失败 |
| NOT_FOUND | 资源不存在 |
| SYSTEM_ERROR | 系统异常 |