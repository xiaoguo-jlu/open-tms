# Open-TMS 测试用例 - 交易对手管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_CP_PG_001 | 对手方分页查询 | /api/v1/counterparties/page | GET | P0 |
| TC_CP_PG_002 | 对手方关键字搜索 | /api/v1/counterparties/page?keyword=公司 | GET | P1 |
| TC_CP_PG_003 | 对手方类型筛选 | /api/v1/counterparties/page?counterpartyType=企业 | GET | P1 |
| TC_CP_PG_004 | 对手方国家筛选 | /api/v1/counterparties/page?countryCode=CN | GET | P1 |
| TC_CP_PG_005 | 对手方状态筛选 | /api/v1/counterparties/page?status=1 | GET | P1 |
| TC_CP_DET_001 | 对手方详情查询 | /api/v1/counterparties/{id} | GET | P0 |
| TC_CP_ADD_001 | 对手方新增成功 | /api/v1/counterparties | POST | P0 |
| TC_CP_ADD_002 | 对手方编码重复校验 | /api/v1/counterparties | POST | P0 |
| TC_CP_ADD_003 | 对手方类型必选校验 | /api/v1/counterparties | POST | P0 |
| TC_CP_ADD_004 | 对手方必填字段校验 | /api/v1/counterparties | POST | P0 |
| TC_CP_UPD_001 | 对手方编辑成功 | /api/v1/counterparties | PUT | P0 |
| TC_CP_DEL_001 | 对手方删除成功 | /api/v1/counterparties/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_CP_PG_001 对手方分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_PG_001 |
| 接口 | GET /api/v1/counterparties/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/counterparties/page?pageNum=1&pageSize=10`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

---

### TC_CP_DET_001 对手方详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_DET_001 |
| 接口 | GET /api/v1/counterparties/{id} |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/counterparties/1`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "counterpartyCode": "C001",
    "counterpartyName": "某公司",
    "counterpartyType": "企业",
    "countryCode": "CN",
    "creditRating": "AAA",
    "status": "1"
  }
}
```

---

### TC_CP_ADD_001 对手方新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_ADD_001 |
| 接口 | POST /api/v1/counterparties |

**测试步骤**
1. 发送POST请求:
```json
{
  "counterpartyCode": "CP_TEST",
  "counterpartyName": "测试对手方",
  "counterpartyNameEn": "Test Counterparty",
  "counterpartyType": "企业",
  "countryCode": "CN",
  "creditRating": "AA",
  "externalRating": "Aa3",
  "address": "北京市朝阳区",
  "phone": "010-12345678",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0

---

### TC_CP_ADD_002 对手方编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_ADD_002 |
| 接口 | POST /api/v1/counterparties |

**测试步骤**
1. 使用已存在的counterpartyCode发送POST请求

**预期结果**
- 返回DUPLICATE_CODE或400/409

---

### TC_CP_ADD_003 对手方类型必选校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_ADD_003 |
| 接口 | POST /api/v1/counterparties |

**测试步骤**
1. 发送不包含counterpartyType的请求

**预期结果**
- 返回VALIDATION_ERROR，提示对手方类型必填

---

### TC_CP_ADD_004 对手方必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_ADD_004 |
| 接口 | POST /api/v1/counterparties |

**测试步骤**
1. 发送只包含counterpartyName的请求

**预期结果**
- 返回VALIDATION_ERROR

---

### TC_CP_UPD_001 对手方编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_UPD_001 |
| 接口 | PUT /api/v1/counterparties |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "counterpartyCode": "C001",
  "counterpartyName": "某公司-修改",
  "counterpartyType": "企业",
  "status": "1"
}
```

**预期结果**
- 返回code=200

---

### TC_CP_DEL_001 对手方删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_DEL_001 |
| 接口 | DELETE /api/v1/counterparties/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/counterparties/999`

**预期结果**
- 返回code=200或404

---

*End of Counterparty Test Cases*