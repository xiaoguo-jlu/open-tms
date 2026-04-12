# Open-TMS 测试用例 - 业务单元管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_BU_PG_001 | 业务单元分页查询 | /api/v1/business-units/page | GET | P0 |
| TC_BU_PG_002 | 业务单元关键字搜索 | /api/v1/business-units/page?keyword=北京 | GET | P1 |
| TC_BU_PG_003 | 业务单元状态筛选 | /api/v1/business-units/page?status=1 | GET | P1 |
| TC_BU_DET_001 | 业务单元详情查询 | /api/v1/business-units/{id} | GET | P0 |
| TC_BU_ADD_001 | 业务单元新增成功 | /api/v1/business-units | POST | P0 |
| TC_BU_ADD_002 | 业务单元编码重复校验 | /api/v1/business-units | POST | P0 |
| TC_BU_ADD_003 | 业务单元必填字段校验 | /api/v1/business-units | POST | P0 |
| TC_BU_UPD_001 | 业务单元编辑成功 | /api/v1/business-units | PUT | P0 |
| TC_BU_DEL_001 | 业务单元删除成功 | /api/v1/business-units/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_BU_PG_001 业务单元分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_PG_001 |
| 接口 | GET /api/v1/business-units/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/business-units/page?pageNum=1&pageSize=10`
2. 验证响应状态码为200

**预期结果**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

---

### TC_BU_DET_001 业务单元详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_DET_001 |
| 接口 | GET /api/v1/business-units/{id} |
| 前置条件 | 存在ID=1的业务单元 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/business-units/1`
2. 验证响应包含unitCode, unitName字段

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "unitCode": "BU001",
    "unitName": "北京分公司",
    "status": "1"
  }
}
```

---

### TC_BU_ADD_001 业务单元新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_ADD_001 |
| 接口 | POST /api/v1/business-units |

**测试步骤**
1. 发送POST请求: `http://localhost:8081/api/v1/business-units`
2. 请求体:
```json
{
  "unitCode": "BU_TEST",
  "unitName": "测试业务单元",
  "unitNameEn": "Test Unit",
  "legalRepresentative": "张三",
  "registeredAddress": "北京市朝阳区",
  "taxNumber": "91110000",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0
- data包含新增的业务单元信息

---

### TC_BU_ADD_002 业务单元编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_ADD_002 |
| 接口 | POST /api/v1/business-units |
| 前置条件 | 已存在unitCode="BU001"的记录 |

**测试步骤**
1. 发送POST请求，unitCode使用已存在的值

**预期结果**
- 返回code为DUPLICATE_CODE或400/409

---

### TC_BU_ADD_003 业务单元必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_ADD_003 |
| 接口 | POST /api/v1/business-units |

**测试步骤**
1. 发送POST请求，只包含unitName，缺少unitCode

**预期结果**
- 返回code为VALIDATION_ERROR或400

---

### TC_BU_UPD_001 业务单元编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_UPD_001 |
| 接口 | PUT /api/v1/business-units |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "unitCode": "BU001",
  "unitName": "北京分公司-修改",
  "status": "1"
}
```

**预期结果**
- 返回code=200，编辑成功

---

### TC_BU_DEL_001 业务单元删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_DEL_001 |
| 接口 | DELETE /api/v1/business-units/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/business-units/999`

**预期结果**
- 返回code=200或404（记录不存在也视为成功）

---

*End of Business Unit Test Cases*