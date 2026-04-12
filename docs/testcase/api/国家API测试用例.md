# Open-TMS 测试用例 - 国家/地区管理

**模块**: basedata  
**版本**: v1.0  
**日期**: 2026-04-11

---

## 一、测试用例汇总

| 用例编号 | 用例名称 | 接口 | 方法 | 优先级 |
|----------|----------|------|------|--------|
| TC_CO_PG_001 | 国家分页查询 | /api/v1/countries/page | GET | P0 |
| TC_CO_PG_002 | 国家关键字搜索 | /api/v1/countries/page?keyword=中国 | GET | P1 |
| TC_CO_PG_003 | 国家状态筛选 | /api/v1/countries/page?status=1 | GET | P1 |
| TC_CO_DET_001 | 国家详情查询 | /api/v1/countries/{id} | GET | P0 |
| TC_CO_ADD_001 | 国家新增成功 | /api/v1/countries | POST | P0 |
| TC_CO_ADD_002 | 国家编码重复校验 | /api/v1/countries | POST | P0 |
| TC_CO_ADD_003 | 国家必填字段校验 | /api/v1/countries | POST | P0 |
| TC_CO_UPD_001 | 国家编辑成功 | /api/v1/countries | PUT | P0 |
| TC_CO_DEL_001 | 国家删除成功 | /api/v1/countries/{id} | DELETE | P0 |

---

## 二、详细测试用例

### TC_CO_PG_001 国家分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_PG_001 |
| 接口 | GET /api/v1/countries/page |
| 前置条件 | basedata服务运行于8081端口 |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/countries/page?pageNum=1&pageSize=10`

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

### TC_CO_DET_001 国家详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_DET_001 |
| 接口 | GET /api/v1/countries/{id} |

**测试步骤**
1. 发送GET请求: `http://localhost:8081/api/v1/countries/1`

**预期结果**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "countryCode": "CN",
    "countryName": "中国",
    "countryNameEn": "China",
    "timezone": "Asia/Shanghai",
    "countryCodePhone": "+86",
    "status": "1"
  }
}
```

---

### TC_CO_ADD_001 国家新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_ADD_001 |
| 接口 | POST /api/v1/countries |

**测试步骤**
1. 发送POST请求:
```json
{
  "countryCode": "JP",
  "countryName": "日本",
  "countryNameEn": "Japan",
  "timezone": "Asia/Tokyo",
  "countryCodePhone": "+81",
  "status": "1"
}
```

**预期结果**
- 返回code=200或code=0

---

### TC_CO_ADD_002 国家编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_ADD_002 |
| 接口 | POST /api/v1/countries |

**测试步骤**
1. 使用已存在的countryCode发送POST请求

**预期结果**
- 返回DUPLICATE_CODE或400/409

---

### TC_CO_ADD_003 国家必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_ADD_003 |
| 接口 | POST /api/v1/countries |

**测试步骤**
1. 发送只包含countryName的请求，缺少countryCode

**预期结果**
- 返回VALIDATION_ERROR或400

---

### TC_CO_UPD_001 国家编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_UPD_001 |
| 接口 | PUT /api/v1/countries |

**测试步骤**
1. 发送PUT请求:
```json
{
  "id": 1,
  "countryCode": "CN",
  "countryName": "中国-修改",
  "countryNameEn": "China",
  "status": "1"
}
```

**预期结果**
- 返回code=200

---

### TC_CO_DEL_001 国家删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CO_DEL_001 |
| 接口 | DELETE /api/v1/countries/{id} |

**测试步骤**
1. 发送DELETE请求: `http://localhost:8081/api/v1/countries/999`

**预期结果**
- 返回code=200或404

---

*End of Country Test Cases*