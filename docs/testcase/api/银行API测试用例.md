# Open-TMS 测试用例 - 银行管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_BK_PG_001 | 银行分页查询 | /api/v1/banks/page | GET | P0 |
| TC_BK_PG_002 | 银行关键字搜索 | /api/v1/banks/page?keyword=工商 | GET | P1 |
| TC_BK_PG_003 | 银行国家筛选 | /api/v1/banks/page?countryCode=CN | GET | P1 |
| TC_BK_PG_004 | 银行状态筛选 | /api/v1/banks/page?status=1 | GET | P1 |
| TC_BK_DET_001 | 银行详情查询 | /api/v1/banks/{id} | GET | P0 |
| TC_BK_ADD_001 | 银行新增成功 | /api/v1/banks | POST | P0 |
| TC_BK_ADD_002 | 银行编码重复校验 | /api/v1/banks | POST | P0 |
| TC_BK_ADD_003 | 银行SWIFT码格式校验 | /api/v1/banks | POST | P1 |
| TC_BK_ADD_004 | 银行必填字段校验 | /api/v1/banks | POST | P0 |
| TC_BK_UPD_001 | 银行编辑成功 | /api/v1/banks | PUT | P0 |
| TC_BK_DEL_001 | 银行删除成功 | /api/v1/banks/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_BK_PG_001 银行分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_PG_001 |
| 接口 | GET /api/v1/banks/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/banks/page?pageNum=1&pageSize=10`

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

### TC_BK_DET_001 银行详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_DET_001 |
| 接口 | GET /api/v1/banks/{id} |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/banks/1`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "bankCode": "B001",
    "bankName": "中国工商银行",
    "bankNameEn": "ICBC",
    "swiftCode": "ICBKCNBJ",
    "countryCode": "CN",
    "status": "1"
  }
}
```

---

### TC_BK_ADD_001 银行新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_ADD_001 |
| 接口 | POST /api/v1/banks |

**测试步骤**
1. 发送POST请求:
```json
{
  "bankCode": "BK_TEST",
  "bankName": "测试银行",
  "bankNameEn": "Test Bank",
  "swiftCode": "TESTUS33",
  "bankLineCode": "123456789012",
  "countryCode": "US",
  "bankType": "商行",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0

---

### TC_BK_ADD_002 银行编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_ADD_002 |
| 接口 | POST /api/v1/banks |

**测试步骤**
1. 使用已存在的bankCode发送POST请求

**预期结果**
- 返回DUPLICATE_CODE或400/409

---

### TC_BK_ADD_003 银行SWIFT码格式校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_ADD_003 |
| 接口 | POST /api/v1/banks |
| 优先级 | P1 |

**测试步骤**
1. 发送SWIFT码格式错误的请求（如少于8位）

**预期结果**
- 返回VALIDATION_ERROR，提示SWIFT码格式错误

---

### TC_BK_ADD_004 银行必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_ADD_004 |
| 接口 | POST /api/v1/banks |

**测试步骤**
1. 发送只包含bankName的请求

**预期结果**
- 返回VALIDATION_ERROR

---

### TC_BK_UPD_001 银行编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_UPD_001 |
| 接口 | PUT /api/v1/banks |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "bankCode": "B001",
  "bankName": "中国工商银行-修改",
  "swiftCode": "ICBKCNBJ",
  "status": "1"
}
```

**预期结果**
- 返回code=200

---

### TC_BK_DEL_001 银行删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BK_DEL_001 |
| 接口 | DELETE /api/v1/banks/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/banks/999`

**预期结果**
- 返回code=200或404

---

*End of Bank Test Cases*