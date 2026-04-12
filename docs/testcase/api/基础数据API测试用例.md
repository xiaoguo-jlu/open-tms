# Open-TMS 测试用例 - 基础数据模块 (M1)

**模块**: basedata  
**版本**: v2.0  
**角色**: 测试工程师 (QA)  
**日期**: 2026-04-11

---

## 一、测试范围

| 模块 | 功能 | 端口 |
|------|------|------|
| basedata | 基础数据管理 | 8081 |

### 测试类型
- [x] API接口测试
- [x] UI功能测试
- [x] 接口文档验证
- [x] E2E测试

---

## 二、测试用例汇总

### 2.1 币种管理 (Currencies) - `/api/v1/currencies`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_CY_001 | 币种列表查询 | API | P0 | GET /currencies |
| TC_CY_002 | 币种分页查询 | API | P0 | GET /currencies/page |
| TC_CY_003 | 币种详情查询 | API | P0 | GET /currencies/{id} |
| TC_CY_004 | 币种新增成功 | API | P0 | POST /currencies |
| TC_CY_005 | 币种编码重复校验 | API | P0 | POST /currencies |
| TC_CY_006 | 币种必填字段校验 | API | P0 | POST /currencies |
| TC_CY_007 | 币种编辑成功 | API | P0 | PUT /currencies |
| TC_CY_008 | 币种删除成功 | API | P0 | DELETE /currencies/{id} |
| TC_CY_009 | 币种批量删除 | API | P1 | POST /currencies/batch-delete |
| TC_CY_010 | 币种状态筛选 | API | P1 | GET /currencies?status=1 |
| TC_CY_011 | 币种关键字搜索 | API | P1 | GET /currencies?keyword=CNY |

### 2.2 业务单元管理 (Business Units) - `/api/v1/business-units`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_BU_001 | 业务单元列表查询 | API | P0 | GET /business-units |
| TC_BU_002 | 业务单元分页查询 | API | P0 | GET /business-units/page |
| TC_BU_003 | 业务单元详情查询 | API | P0 | GET /business-units/{id} |
| TC_BU_004 | 业务单元新增成功 | API | P0 | POST /business-units |
| TC_BU_005 | 业务单元编码重复校验 | API | P0 | POST /business-units |
| TC_BU_006 | 业务单元必填字段校验 | API | P0 | POST /business-units |
| TC_BU_007 | 业务单元编辑成功 | API | P0 | PUT /business-units |
| TC_BU_008 | 业务单元删除成功 | API | P0 | DELETE /business-units/{id} |
| TC_BU_009 | 业务单元停用状态验证 | API | P1 | GET /business-units?status=0 |

### 2.3 交易员管理 (Traders) - `/api/v1/traders`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_TR_001 | 交易员列表查询 | API | P0 | GET /traders |
| TC_TR_002 | 交易员分页查询 | API | P0 | GET /traders/page |
| TC_TR_003 | 交易员详情查询 | API | P0 | GET /traders/{id} |
| TC_TR_004 | 交易员新增成功 | API | P0 | POST /traders |
| TC_TR_005 | 交易员编码重复校验 | API | P0 | POST /traders |
| TC_TR_006 | 交易员必填字段校验 | API | P0 | POST /traders |
| TC_TR_007 | 交易员编辑成功 | API | P0 | PUT /traders |
| TC_TR_008 | 交易员删除成功 | API | P0 | DELETE /traders/{id} |

### 2.4 国家/地区管理 (Countries) - `/api/v1/countries`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_CO_001 | 国家列表查询 | API | P0 | GET /countries |
| TC_CO_002 | 国家分页查询 | API | P0 | GET /countries/page |
| TC_CO_003 | 国家详情查询 | API | P0 | GET /countries/{id} |
| TC_CO_004 | 国家新增成功 | API | P0 | POST /countries |
| TC_CO_005 | 国家编码重复校验 | API | P0 | POST /countries |
| TC_CO_006 | 国家编辑成功 | API | P0 | PUT /countries |
| TC_CO_007 | 国家删除成功 | API | P0 | DELETE /countries/{id} |

### 2.5 银行管理 (Banks) - `/api/v1/banks`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_BK_001 | 银行列表查询 | API | P0 | GET /banks |
| TC_BK_002 | 银行分页查询 | API | P0 | GET /banks/page |
| TC_BK_003 | 银行详情查询 | API | P0 | GET /banks/{id} |
| TC_BK_004 | 银行新增成功 | API | P0 | POST /banks |
| TC_BK_005 | 银行编码重复校验 | API | P0 | POST /banks |
| TC_BK_006 | 银行SWIFT码格式校验 | API | P1 | POST /banks |
| TC_BK_007 | 银行编辑成功 | API | P0 | PUT /banks |
| TC_BK_008 | 银行删除成功 | API | P0 | DELETE /banks/{id} |

### 2.6 交易对手管理 (Counterparties) - `/api/v1/counterparties`

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_CP_001 | 对手方列表查询 | API | P0 | GET /counterparties |
| TC_CP_002 | 对手方分页查询 | API | P0 | GET /counterparties/page |
| TC_CP_003 | 对手方详情查询 | API | P0 | GET /counterparties/{id} |
| TC_CP_004 | 对手方新增成功 | API | P0 | POST /counterparties |
| TC_CP_005 | 对手方编码重复校验 | API | P0 | POST /counterparties |
| TC_CP_006 | 对手方类型必选校验 | API | P0 | POST /counterparties |
| TC_CP_007 | 对手方编辑成功 | API | P0 | PUT /counterparties |
| TC_CP_008 | 对手方删除成功 | API | P0 | DELETE /counterparties/{id} |

---

## 三、详细测试用例

### 3.1 币种管理

#### TC_CY_001 币种列表查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_001 |
| 用例名称 | 币种列表查询 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/currencies |

**前置条件**
1. basedata服务已启动（端口8081）
2. 数据库已初始化，包含至少1条币种数据

**测试步骤**
1. 发送GET请求至 `http://localhost:8081/api/v1/currencies`
2. 验证响应状态码为200
3. 验证响应结构符合通用模式

**预期结果**
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

---

#### TC_CY_002 币种分页查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_002 |
| 用例名称 | 币种分页查询 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/currencies/page |

**前置条件**
1. basedata服务已启动
2. 数据库包含多条币种数据

**测试步骤**
1. 发送GET请求至 `http://localhost:8081/api/v1/currencies/page?pageNo=1&pageSize=10`
2. 验证响应状态码为200
3. 验证返回分页数据结构

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [...],
    "total": 10,
    "pageNo": 1,
    "pageSize": 10
  }
}
```

---

#### TC_CY_003 币种详情查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_003 |
| 用例名称 | 币种详情查询 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/currencies/{id} |

**前置条件**
1. 已知存在ID=1的币种记录

**测试步骤**
1. 发送GET请求至 `http://localhost:8081/api/v1/currencies/1`
2. 验证响应状态码为200

**预期结果**
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

---

#### TC_CY_004 币种新增成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_004 |
| 用例名称 | 币种新增成功 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/currencies |

**前置条件**
1. basedata服务已启动

**测试步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/currencies`
2. 请求体：
```json
{
  "code": "USD",
  "name": "美元",
  "symbol": "$",
  "decimalPlaces": 2,
  "status": "1"
}
```
3. 验证响应状态码为200
4. 验证返回数据包含新增的币种信息

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 2,
    "code": "USD",
    "name": "美元",
    "symbol": "$",
    "decimalPlaces": 2,
    "status": "1"
  }
}
```

---

#### TC_CY_005 币种编码重复校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_005 |
| 用例名称 | 币种编码重复校验 |
| 测试类型 | API异常测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/currencies |

**前置条件**
1. 数据库已存在code="CNY"的币种

**测试步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/currencies`
2. 请求体：
```json
{
  "code": "CNY",
  "name": "人民币-重复",
  "symbol": "¥",
  "decimalPlaces": 2,
  "status": "1"
}
```
3. 验证响应返回错误码DUPLICATE_CODE

**预期结果**
```json
{
  "code": "DUPLICATE_CODE",
  "message": "币种编码已存在",
  "data": null
}
```

---

#### TC_CY_006 币种必填字段校验

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_006 |
| 用例名称 | 币种必填字段校验 |
| 测试类型 | API异常测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/currencies |

**前置条件**
1. basedata服务已启动

**测试步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/currencies`
2. 请求体（缺少必填字段）：
```json
{
  "name": "人民币"
}
```
3. 验证响应返回VALIDATION_ERROR

**预期结果**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "币种代码不能为空",
  "data": null
}
```

---

#### TC_CY_007 币种编辑成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_007 |
| 用例名称 | 币种编辑成功 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/currencies |

**前置条件**
1. 存在ID=1的币种记录

**测试步骤**
1. 发送PUT请求至 `http://localhost:8081/api/v1/currencies`
2. 请求体：
```json
{
  "id": 1,
  "code": "CNY",
  "name": "人民币-修改",
  "symbol": "¥",
  "decimalPlaces": 2,
  "status": "1"
}
```
3. 验证响应状态码为200

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "code": "CNY",
    "name": "人民币-修改",
    "symbol": "¥",
    "decimalPlaces": 2,
    "status": "1"
  }
}
```

---

#### TC_CY_008 币种删除成功

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CY_008 |
| 用例名称 | 币种删除成功 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/currencies/{id} |

**前置条件**
1. 存在ID=2的币种记录

**测试步骤**
1. 发送DELETE请求至 `http://localhost:8081/api/v1/currencies/2`
2. 验证响应状态码为200

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

### 3.2 业务单元管理

#### TC_BU_001 业务单元列表查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_BU_001 |
| 用例名称 | 业务单元列表查询 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/business-units |

**前置条件**
1. basedata服务已启动（端口8081）

**测试步骤**
1. 发送GET请求至 `http://localhost:8081/api/v1/business-units`
2. 验证响应状态码为200

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": [...]
}
```

---

### 3.3 交易员管理

#### TC_TR_001 交易员列表查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_001 |
| 用例名称 | 交易员列表查询 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/traders |

**前置条件**
1. basedata服务已启动（端口8081）

**测试步骤**
1. 发送GET请求至 `http://localhost:8081/api/v1/traders`
2. 验证响应状态码为200

**预期结果**
```json
{
  "code": 0,
  "message": "success",
  "data": [...]
}
```

---

## 四、测试数据

### 4.1 币种测试数据

| ID | code | name | symbol | decimalPlaces | status |
|----|------|------|--------|---------------|--------|
| 1 | CNY | 人民币 | ¥ | 2 | 1 |
| 2 | USD | 美元 | $ | 2 | 1 |
| 3 | EUR | 欧元 | € | 2 | 1 |
| 4 | JPY | 日元 | ¥ | 0 | 1 |
| 5 | GBP | 英镑 | £ | 2 | 0 |

### 4.2 业务单元测试数据

| ID | code | name | englishName | legalRep | status |
|----|------|------|-------------|---------|--------|
| 1 | HQ | 集团总部 | Headquarters | 张三 | 1 |
| 2 | SH | 上海分公司 | Shanghai Branch | 李四 | 1 |
| 3 | BJ | 北京分公司 | Beijing Branch | 王五 | 0 |

### 4.3 交易员测试数据

| ID | code | name | englishName | department | phone | email | status |
|----|------|------|-------------|-------------|-------|-------|--------|
| 1 | TR001 | 张伟 | Zhang Wei | 资金部 | 13800138000 | zhangwei@example.com | 1 |
| 2 | TR002 | 李娜 | Li Na | 外汇部 | 13900139000 | lina@example.com | 1 |

---

## 五、环境配置

### 5.1 服务配置

| 服务 | 主机 | 端口 | 数据库 |
|------|------|------|--------|
| basedata | localhost | 8081 | PostgreSQL:5432/opentms |

### 5.2 通用请求头

```
Content-Type: application/json
Accept: application/json
```

### 5.3 通用响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

---

## 六、缺陷记录

| 缺陷编号 | 用例编号 | 缺陷描述 | 严重级别 | 状态 |
|----------|----------|----------|----------|------|
| - | - | 待测试执行后记录 | - | - |

---

*End of Test Cases*