# Open-TMS Bug单 - 基础数据模块

**项目**: Open-TMS  
**日期**: 2026-04-11  
**测试人员**: QA

---

## Bug汇总

| Bug编号 | 模块 | 严重级别 | 优先级 | 状态 |
|---------|------|----------|--------|------|
| BUG-001 | basedata | 高 | P0 | 待修复 |
| BUG-002 | basedata | 高 | P0 | 待修复 |
| BUG-003 | basedata | 高 | P0 | 待修复 |
| BUG-004 | basedata | 高 | P0 | 待修复 |
| BUG-005 | basedata | 高 | P0 | 待修复 |
| BUG-006 | basedata | 高 | P0 | 待修复 |
| BUG-007 | basedata | 高 | P0 | 待修复 |

---

## Bug详情

### BUG-001: 币种管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-001 |
| 模块 | 币种管理 (Currency) |
| 接口 | POST /api/v1/currencies |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用币种新增接口时，服务器返回500内部错误，无法完成币种新增操作。

**复现步骤**
1. 启动basedata服务（端口8081）
2. 发送POST请求至 `http://localhost:8081/api/v1/currencies`
3. 请求体：
```json
{
  "currencyCode": "TEST",
  "currencyName": "测试币种",
  "currencySymbol": "T",
  "decimalPlaces": 2,
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500
- 响应体：
```json
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "message": "..."
}
```

**期望结果**
- HTTP状态码：200 或 201
- 返回新增成功的币种数据，示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "currencyCode": "TEST",
    "currencyName": "测试币种",
    "currencySymbol": "T",
    "decimalPlaces": 2,
    "status": "1"
  }
}
```

**影响范围**
- 无法新增币种
- 无法进行币种重复校验测试
- 无法进行必填字段校验测试

**环境信息**
- 服务：basedata
- 端口：8081
- 数据库：PostgreSQL (opentms)
- Java版本：OpenJDK 17

---

### BUG-002: 业务单元管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-002 |
| 模块 | 业务单元管理 (Business Unit) |
| 接口 | POST /api/v1/business-units |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用业务单元新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/business-units`
2. 请求体：
```json
{
  "unitCode": "BU_TEST",
  "unitName": "测试业务单元",
  "unitNameEn": "Test Unit",
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201，返回新增成功的业务单元数据

**影响范围**
- 无法新增业务单元

---

### BUG-003: 交易员管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-003 |
| 模块 | 交易员管理 (Trader) |
| 接口 | POST /api/v1/traders |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用交易员新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/traders`
2. 请求体：
```json
{
  "traderCode": "TR_TEST",
  "traderName": "测试交易员",
  "department": "资金部",
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201

---

### BUG-004: 国家/地区管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-004 |
| 模块 | 国家/地区管理 (Country) |
| 接口 | POST /api/v1/countries |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用国家新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/countries`
2. 请求体：
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

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201

---

### BUG-005: 银行管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-005 |
| 模块 | 银行管理 (Bank) |
| 接口 | POST /api/v1/banks |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用银行新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/banks`
2. 请求体：
```json
{
  "bankCode": "BK_TEST",
  "bankName": "测试银行",
  "bankNameEn": "Test Bank",
  "swiftCode": "TESTUS33",
  "countryCode": "US",
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201

---

### BUG-006: 交易对手管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-006 |
| 模块 | 交易对手管理 (Counterparty) |
| 接口 | POST /api/v1/counterparties |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用交易对手新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/counterparties`
2. 请求体：
```json
{
  "counterpartyCode": "CP_TEST",
  "counterpartyName": "测试对手方",
  "counterpartyType": "企业",
  "countryCode": "CN",
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201

---

### BUG-007: 对手方账户管理-新增接口返回500错误

| 项目 | 内容 |
|------|------|
| Bug编号 | BUG-007 |
| 模块 | 对手方账户管理 (Counterparty Account) |
| 接口 | POST /api/v1/counterparty-accounts |
| 严重级别 | 高 |
| 优先级 | P0 |
| 状态 | 待修复 |

**缺陷描述**
调用对手方账户新增接口时，服务器返回500内部错误。

**复现步骤**
1. 发送POST请求至 `http://localhost:8081/api/v1/counterparty-accounts`
2. 请求体：
```json
{
  "accountNo": "CPA_TEST",
  "accountName": "测试账户",
  "counterpartyId": 1,
  "bankId": 1,
  "currencyCode": "CNY",
  "status": "1"
}
```

**实际结果**
- HTTP状态码：500

**期望结果**
- HTTP状态码：200 或 201

---

## 根因分析

所有7个模块的POST接口均返回500错误，可能的根因：
1. 后端Controller层接收请求后未正确处理
2. Service层业务逻辑异常
3. 数据库操作失败（如字段映射错误、外键约束等）
4. 实体类属性与数据库表字段不匹配

建议检查：
- Controller的@PostMapping注解和参数绑定
- Service层的事务处理
- Entity/DTO的字段映射
- 后端日志中的具体错误堆栈

---

## 修复建议

1. 检查basedata模块的Controller层POST接口实现
2. 查看后端日志获取具体错误堆栈信息
3. 验证请求参数与实体类字段的对应关系
4. 检查数据库表结构与实体类的映射

---

*End of Bug Report*