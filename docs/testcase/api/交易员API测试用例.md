# Open-TMS 测试用例 - 交易员管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_TR_PG_001 | 交易员分页查询 | /api/v1/traders/page | GET | P0 |
| TC_TR_PG_002 | 交易员关键字搜索 | /api/v1/traders/page?keyword=张 | GET | P1 |
| TC_TR_PG_003 | 交易员状态筛选 | /api/v1/traders/page?status=1 | GET | P1 |
| TC_TR_DET_001 | 交易员详情查询 | /api/v1/traders/{id} | GET | P0 |
| TC_TR_ADD_001 | 交易员新增成功 | /api/v1/traders | POST | P0 |
| TC_TR_ADD_002 | 交易员编码重复校验 | /api/v1/traders | POST | P0 |
| TC_TR_ADD_003 | 交易员必填字段校验 | /api/v1/traders | POST | P0 |
| TC_TR_UPD_001 | 交易员编辑成功 | /api/v1/traders | PUT | P0 |
| TC_TR_DEL_001 | 交易员删除成功 | /api/v1/traders/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_TR_PG_001 交易员分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_PG_001 |
| 接口 | GET /api/v1/traders/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/traders/page?pageNum=1&pageSize=10`
2. 验证响应状态码为200

**预期结果**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

---

### TC_TR_DET_001 交易员详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_DET_001 |
| 接口 | GET /api/v1/traders/{id} |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/traders/1`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "traderCode": "TR001",
    "traderName": "张三",
    "department": "资金部",
    "status": "1"
  }
}
```

---

### TC_TR_ADD_001 交易员新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_ADD_001 |
| 接口 | POST /api/v1/traders |

**测试步骤**
1. 发送POST请求:
```json
{
  "traderCode": "TR_TEST",
  "traderName": "测试交易员",
  "traderNameEn": "Test Trader",
  "department": "外汇部",
  "phone": "13800138000",
  "email": "test@company.com",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0

---

### TC_TR_ADD_002 交易员编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_ADD_002 |
| 接口 | POST /api/v1/traders |

**测试步骤**
1. 使用已存在的traderCode发送POST请求

**预期结果**
- 返回DUPLICATE_CODE或400/409

---

### TC_TR_ADD_003 交易员必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_ADD_003 |
| 接口 | POST /api/v1/traders |

**测试步骤**
1. 发送不包含traderCode和traderName的请求

**预期结果**
- 返回VALIDATION_ERROR或400

---

### TC_TR_UPD_001 交易员编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_UPD_001 |
| 接口 | PUT /api/v1/traders |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "traderCode": "TR001",
  "traderName": "张三-修改",
  "department": "资金部",
  "status": "1"
}
```

**预期结果**
- 返回code=200

---

### TC_TR_DEL_001 交易员删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_DEL_001 |
| 接口 | DELETE /api/v1/traders/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/traders/999`

**预期结果**
- 返回code=200或404

---

*End of Trader Test Cases*