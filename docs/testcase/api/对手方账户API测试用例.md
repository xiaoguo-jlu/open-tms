# Open-TMS 测试用例 - 对手方账户管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_CPA_PG_001 | 对手方账户分页查询 | /api/v1/counterparty-accounts/page | GET | P0 |
| TC_CPA_PG_002 | 对手方账户关键字搜索 | /api/v1/counterparty-accounts/page?keyword=账户 | GET | P1 |
| TC_CPA_PG_003 | 对手方账户对手方筛选 | /api/v1/counterparty-accounts/page?counterpartyId=1 | GET | P1 |
| TC_CPA_PG_004 | 对手方账户状态筛选 | /api/v1/counterparty-accounts/page?status=1 | GET | P1 |
| TC_CPA_DET_001 | 对手方账户详情查询 | /api/v1/counterparty-accounts/{id} | GET | P0 |
| TC_CPA_ADD_001 | 对手方账户新增成功 | /api/v1/counterparty-accounts | POST | P0 |
| TC_CPA_ADD_002 | 对手方账户编号重复校验 | /api/v1/counterparty-accounts | POST | P0 |
| TC_CPA_ADD_003 | 对手方账户必填字段校验 | /api/v1/counterparty-accounts | POST | P0 |
| TC_CPA_ADD_004 | 对手方账户关联校验 | /api/v1/counterparty-accounts | POST | P0 |
| TC_CPA_UPD_001 | 对手方账户编辑成功 | /api/v1/counterparty-accounts | PUT | P0 |
| TC_CPA_DEL_001 | 对手方账户删除成功 | /api/v1/counterparty-accounts/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_CPA_PG_001 对手方账户分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_PG_001 |
| 接口 | GET /api/v1/counterparty-accounts/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/counterparty-accounts/page?pageNum=1&pageSize=10`

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

### TC_CPA_DET_001 对手方账户详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_DET_001 |
| 接口 | GET /api/v1/counterparty-accounts/{id} |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/counterparty-accounts/1`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "accountNo": "ACC001",
    "accountName": "某公司账户",
    "counterpartyId": 1,
    "bankId": 1,
    "accountType": "基本户",
    "currencyCode": "CNY",
    "status": "1"
  }
}
```

---

### TC_CPA_ADD_001 对手方账户新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_ADD_001 |
| 接口 | POST /api/v1/counterparty-accounts |

**测试步骤**
1. 发送POST请求:
```json
{
  "accountNo": "CPA_TEST",
  "accountName": "测试对手方账户",
  "counterpartyId": 1,
  "bankId": 1,
  "accountType": "基本户",
  "currencyCode": "CNY",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0

---

### TC_CPA_ADD_002 对手方账户编号重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_ADD_002 |
| 接口 | POST /api/v1/counterparty-accounts |

**测试步骤**
1. 使用已存在的accountNo发送POST请求

**预期结果**
- 返回DUPLICATE_CODE或400/409

---

### TC_CPA_ADD_003 对手方账户必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_ADD_003 |
| 接口 | POST /api/v1/counterparty-accounts |

**测试步骤**
1. 发送只包含accountName的请求

**预期结果**
- 返回VALIDATION_ERROR

---

### TC_CPA_ADD_004 对手方账户关联校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_ADD_004 |
| 接口 | POST /api/v1/counterparty-accounts |

**测试步骤**
1. 发送counterpartyId为不存在对手方的请求

**预期结果**
- 返回VALIDATION_ERROR，提示关联的对手方不存在

---

### TC_CPA_UPD_001 对手方账户编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_UPD_001 |
| 接口 | PUT /api/v1/counterparty-accounts |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "accountNo": "ACC001",
  "accountName": "某公司账户-修改",
  "status": "1"
}
```

**预期结果**
- 返回code=200

---

### TC_CPA_DEL_001 对手方账户删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CPA_DEL_001 |
| 接口 | DELETE /api/v1/counterparty-accounts/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/counterparty-accounts/999`

**预期结果**
- 返回code=200或404

---

*End of Counterparty Account Test Cases*