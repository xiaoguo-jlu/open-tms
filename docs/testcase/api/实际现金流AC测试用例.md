# Open-TMS 测试用例 - 实际现金流AC API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| AC交易 | 现金流管理 | `/api/v1/ac/cashflows/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_AC_001 | 分页查询现金流-正常 | API | P0 | GET /api/v1/ac/cashflows |
| TC_AC_002 | 分页查询现金流-关键字查询 | API | P1 | GET /api/v1/ac/cashflows |
| TC_AC_003 | 分页查询现金流-状态筛选 | API | P1 | GET /api/v1/ac/cashflows |
| TC_AC_004 | 分页查询现金流-空数据 | API | P1 | GET /api/v1/ac/cashflows |
| TC_AC_005 | 查询现金流详情-正常 | API | P0 | GET /api/v1/ac/cashflows/{id} |
| TC_AC_006 | 查询现金流详情-ID不存在 | API | P1 | GET /api/v1/ac/cashflows/{id} |
| TC_AC_007 | 新增现金流-正常(流入) | API | P0 | POST /api/v1/ac/cashflows |
| TC_AC_008 | 新增现金流-正常(流出) | API | P0 | POST /api/v1/ac/cashflows |
| TC_AC_009 | 新增现金流-必填项为空 | API | P0 | POST /api/v1/ac/cashflows |
| TC_AC_010 | 新增现金流-金额为0 | API | P1 | POST /api/v1/ac/cashflows |
| TC_AC_011 | 更新现金流-正常 | API | P0 | PUT /api/v1/ac/cashflows |
| TC_AC_012 | 更新现金流-ID为空 | API | P1 | PUT /api/v1/ac/cashflows |
| TC_AC_013 | 删除现金流-正常 | API | P0 | DELETE /api/v1/ac/cashflows/{id} |
| TC_AC_014 | 删除现金流-ID不存在 | API | P1 | DELETE /api/v1/ac/cashflows/{id} |
| TC_AC_015 | 确认现金流(清分)-正常 | API | P0 | POST /api/v1/ac/cashflows/{id}/confirm |
| TC_AC_016 | 确认现金流-状态不合法 | API | P1 | POST /api/v1/ac/cashflows/{id}/confirm |
| TC_AC_017 | 从交易生成现金流 | API | P1 | POST /api/v1/ac/generate/{dealId} |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_AC_001 分页查询现金流-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_001 |
| 用例名称 | 分页查询现金流-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/ac/cashflows |

**前置条件**
1. 数据库已初始化，tms_cashflow_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为现金流列表
- `total` > 0

#### TC_AC_002 分页查询现金流-关键字查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_002 |
| 用例名称 | 分页查询现金流-关键字查询 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/ac/cashflows |

**前置条件**
1. 数据库中存在编号为CF202604060001的现金流

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows?keyword=CF202604060001`

**预期结果**
- 响应状态码：200
- 返回数据中records包含该现金流

#### TC_AC_003 分页查询现金流-状态筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_003 |
| 用例名称 | 分页查询现金流-状态筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/ac/cashflows |

**前置条件**
1. 数据库中存在不同状态的现金流

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows?status=Created`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的status都为"Created"

#### TC_AC_004 分页查询现金流-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_004 |
| 用例名称 | 分页查询现金流-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/ac/cashflows |

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows?keyword=NONEXIST`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_AC_005 查询现金流详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_005 |
| 用例名称 | 查询现金流详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/ac/cashflows/{id} |

**前置条件**
1. 数据库中存在ID为1的现金流

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows/1`

**预期结果**
- 响应状态码：200
- `data`包含现金流详细信息（cashflowNo, bankAccount, direction, amount, currency, status等）

#### TC_AC_006 查询现金流详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_006 |
| 用例名称 | 查询现金流详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/ac/cashflows/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/ac/cashflows/99999`

**预期结果**
- `code` = 404
- `message`包含"现金流不存在"

### 3.3 新增现金流

#### TC_AC_007 新增现金流-正常(流入)

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_007 |
| 用例名称 | 新增现金流-正常(流入) |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/ac/cashflows |

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "businessUnit": "BU001",
  "bankAccount": "6222021234567890",
  "counterpartyAccount": "6222029876543210",
  "direction": "Inflow",
  "amount": 100000.00,
  "currency": "CNY",
  "cashflowDate": "2026-04-06",
  "valueDate": "2026-04-06",
  "sourceType": "Bank Transfer",
  "sourceRef": "TR202604060001",
  "counterpartyName": "对手公司",
  "purpose": "货款"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 返回数据中包含系统生成的cashflowNo
- 数据库中存在该现金流

#### TC_AC_008 新增现金流-正常(流出)

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_008 |
| 用例名称 | 新增现金流-正常(流出) |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/ac/cashflows |

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "businessUnit": "BU001",
  "bankAccount": "6222021234567890",
  "direction": "Outflow",
  "amount": 50000.00,
  "currency": "CNY",
  "cashflowDate": "2026-04-06",
  "valueDate": "2026-04-06",
  "sourceType": "Bank Transfer",
  "sourceRef": "TR202604060002"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中存在该现金流

#### TC_AC_009 新增现金流-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_009 |
| 用例名称 | 新增现金流-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/ac/cashflows |

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "businessUnit": "",
  "bankAccount": "",
  "direction": "",
  "amount": null
}
```

**预期结果**
- `code` 非0
- `message`包含错误提示

#### TC_AC_010 新增现金流-金额为0

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_010 |
| 用例名称 | 新增现金流-金额为0 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/ac/cashflows |

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "businessUnit": "BU001",
  "bankAccount": "6222021234567890",
  "direction": "Inflow",
  "amount": 0,
  "currency": "CNY",
  "cashflowDate": "2026-04-06",
  "valueDate": "2026-04-06",
  "sourceType": "Bank Transfer",
  "sourceRef": "TR202604060003"
}
```

**预期结果**
- `code` 非0
- `message`包含"金额必须大于0"

### 3.4 更新现金流

#### TC_AC_011 更新现金流-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_011 |
| 用例名称 | 更新现金流-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/ac/cashflows |

**前置条件**
1. 数据库中存在ID为1的现金流

**测试步骤**
1. 发送PUT请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "id": 1,
  "purpose": "更新后的用途说明"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的现金流的purpose已更新

#### TC_AC_012 更新现金流-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_012 |
| 用例名称 | 更新现金流-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/ac/cashflows |

**测试步骤**
1. 发送PUT请求至 `/api/v1/ac/cashflows`
2. 请求体：
```json
{
  "purpose": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

### 3.5 删除现金流

#### TC_AC_013 删除现金流-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_013 |
| 用例名称 | 删除现金流-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/ac/cashflows/{id} |

**前置条件**
1. 数据库中存在一个可删除的现金流

**测试步骤**
1. 发送DELETE请求至 `/api/v1/ac/cashflows/{该现金流的ID}`

**预期结果**
- `code` = 0
- 数据库中该现金流的deleted字段被标记为"1"

#### TC_AC_014 删除现金流-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_014 |
| 用例名称 | 删除现金流-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/ac/cashflows/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/ac/cashflows/99999`

**预期结果**
- `code` 非0
- `message`包含"现金流不存在"

### 3.6 确认现金流

#### TC_AC_015 确认现金流(清分)-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_015 |
| 用例名称 | 确认现金流(清分)-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/ac/cashflows/{id}/confirm |

**前置条件**
1. 数据库中存在ID为1且状态为"Created"的现金流

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows/1/confirm`

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的现金流状态更新为"Cleared"

#### TC_AC_016 确认现金流-状态不合法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_016 |
| 用例名称 | 确认现金流-状态不合法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/ac/cashflows/{id}/confirm |

**前置条件**
1. 数据库中存在ID为2且状态为"Reconciled"的现金流

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/cashflows/2/confirm`

**预期结果**
- `code` 非0
- `message`包含"只有Created状态的现金流可以确认"

### 3.7 生成现金流

#### TC_AC_017 从交易生成现金流

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AC_017 |
| 用例名称 | 从交易生成现金流 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/ac/generate/{dealId} |

**测试步骤**
1. 发送POST请求至 `/api/v1/ac/generate/1`

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中新生成一条关联的现金流记录

---

## 四、测试数据

### 4.1 现金流测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| businessUnit | string | BU001 | null, 空字符串 |
| bankAccount | string | 6222021234567890 | null, 空字符串 |
| direction | string | Inflow | null, 非法值 |
| amount | decimal | 100000.00 | 0, 负数 |
| currency | string | CNY | null, 空字符串 |
| cashflowDate | date | 2026-04-06 | null, 未来日期 |
| valueDate | date | 2026-04-06 | null |
| sourceType | string | Bank Transfer | null, 空字符串 |
| sourceRef | string | TR202604060001 | null, 空字符串 |
| status | string | Created | 非法状态值 |

---

*QA产出 - v1.0*
