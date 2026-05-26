# Open-TMS 测试用例 - 交易对手API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| 基础数据 | 交易对手管理 | `/api/v1/counterparties/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_CP_001 | 分页查询交易对手-正常 | API | P0 | GET /api/v1/counterparties/page |
| TC_CP_002 | 分页查询交易对手-关键字查询 | API | P1 | GET /api/v1/counterparties/page |
| TC_CP_003 | 分页查询交易对手-类型筛选 | API | P1 | GET /api/v1/counterparties/page |
| TC_CP_004 | 分页查询交易对手-空数据 | API | P1 | GET /api/v1/counterparties/page |
| TC_CP_005 | 查询交易对手详情-正常 | API | P0 | GET /api/v1/counterparties/{id} |
| TC_CP_006 | 查询交易对手详情-ID不存在 | API | P1 | GET /api/v1/counterparties/{id} |
| TC_CP_007 | 查询交易对手详情-ID格式非法 | API | P1 | GET /api/v1/counterparties/{id} |
| TC_CP_008 | 新增交易对手-正常 | API | P0 | POST /api/v1/counterparties |
| TC_CP_009 | 新增交易对手-必填项为空 | API | P0 | POST /api/v1/counterparties |
| TC_CP_010 | 新增交易对手-编码重复 | API | P1 | POST /api/v1/counterparties |
| TC_CP_011 | 更新交易对手-正常 | API | P0 | PUT /api/v1/counterparties |
| TC_CP_012 | 更新交易对手-ID为空 | API | P1 | PUT /api/v1/counterparties |
| TC_CP_013 | 更新交易对手-编码重复 | API | P1 | PUT /api/v1/counterparties |
| TC_CP_014 | 删除交易对手-正常 | API | P0 | DELETE /api/v1/counterparties/{id} |
| TC_CP_015 | 删除交易对手-ID不存在 | API | P1 | DELETE /api/v1/counterparties/{id} |
| TC_CP_016 | 批量删除交易对手-正常 | API | P1 | POST /api/v1/counterparties/batch-delete |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_CP_001 分页查询交易对手-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_001 |
| 用例名称 | 分页查询交易对手-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/counterparties/page |

**前置条件**
1. 数据库已初始化，tms_counterparty_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/page?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为交易对手列表
- `total` > 0

#### TC_CP_002 分页查询交易对手-关键字查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_002 |
| 用例名称 | 分页查询交易对手-关键字查询 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/counterparties/page |

**前置条件**
1. 数据库中存在编码为CP001的交易对手

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/page?keyword=CP001`

**预期结果**
- 响应状态码：200
- 返回数据中records包含编码为CP001的交易对手

#### TC_CP_003 分页查询交易对手-类型筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_003 |
| 用例名称 | 分页查询交易对手-类型筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/counterparties/page |

**前置条件**
1. 数据库中存在不同类型的交易对手

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/page?counterpartyType=BANK`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的counterpartyType都为"BANK"

#### TC_CP_004 分页查询交易对手-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_004 |
| 用例名称 | 分页查询交易对手-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/counterparties/page |

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/page?keyword=NONEXIST`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_CP_005 查询交易对手详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_005 |
| 用例名称 | 查询交易对手详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/counterparties/{id} |

**前置条件**
1. 数据库中存在ID为1的交易对手

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/1`

**预期结果**
- 响应状态码：200
- `data`包含交易对手详细信息（code, name, enName, counterpartyType, countryCode, swiftCode, status）

#### TC_CP_006 查询交易对手详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_006 |
| 用例名称 | 查询交易对手详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/counterparties/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/99999`

**预期结果**
- `code` = 404
- `message`包含"交易对手不存在"

#### TC_CP_007 查询交易对手详情-ID格式非法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_007 |
| 用例名称 | 查询交易对手详情-ID格式非法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/counterparties/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/counterparties/abc`

**预期结果**
- `code` = 400
- `message`包含"ID参数格式不正确"

### 3.3 新增交易对手

#### TC_CP_008 新增交易对手-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_008 |
| 用例名称 | 新增交易对手-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/counterparties |

**前置条件**
1. 数据库中没有编码为CP100的交易对手

**测试步骤**
1. 发送POST请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "code": "CP100",
  "name": "测试对手公司",
  "enName": "Test Counterparty",
  "counterpartyType": "CORPORATE",
  "countryCode": "CN",
  "swiftCode": "",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中存在编码为CP100的交易对手

#### TC_CP_009 新增交易对手-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_009 |
| 用例名称 | 新增交易对手-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/counterparties |

**测试步骤**
1. 发送POST请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "code": "",
  "name": ""
}
```

**预期结果**
- `code` 非0
- `message`包含错误提示

#### TC_CP_010 新增交易对手-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_010 |
| 用例名称 | 新增交易对手-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/counterparties |

**前置条件**
1. 数据库中存在编码为CP001的交易对手

**测试步骤**
1. 发送POST请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "code": "CP001",
  "name": "重复交易对手",
  "status": "1"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.4 更新交易对手

#### TC_CP_011 更新交易对手-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_011 |
| 用例名称 | 更新交易对手-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/counterparties |

**前置条件**
1. 数据库中存在ID为1的交易对手

**测试步骤**
1. 发送PUT请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "id": 1,
  "name": "更新后的对手名称",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的交易对手的name已更新

#### TC_CP_012 更新交易对手-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_012 |
| 用例名称 | 更新交易对手-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/counterparties |

**测试步骤**
1. 发送PUT请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "name": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

#### TC_CP_013 更新交易对手-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_013 |
| 用例名称 | 更新交易对手-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/counterparties |

**前置条件**
1. 数据库中存在编码为CP001和CP002的交易对手

**测试步骤**
1. 发送PUT请求至 `/api/v1/counterparties`
2. 请求体：
```json
{
  "id": 2,
  "code": "CP001"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.5 删除交易对手

#### TC_CP_014 删除交易对手-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_014 |
| 用例名称 | 删除交易对手-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/counterparties/{id} |

**前置条件**
1. 数据库中存在一个可删除的交易对手

**测试步骤**
1. 发送DELETE请求至 `/api/v1/counterparties/{该交易对手的ID}`

**预期结果**
- `code` = 0
- 数据库中该交易对手的deleted字段被标记为"1"

#### TC_CP_015 删除交易对手-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_015 |
| 用例名称 | 删除交易对手-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/counterparties/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/counterparties/99999`

**预期结果**
- `code` 非0
- `message`包含"交易对手不存在"

### 3.6 批量删除

#### TC_CP_016 批量删除交易对手-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CP_016 |
| 用例名称 | 批量删除交易对手-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/counterparties/batch-delete |

**前置条件**
1. 数据库中存在2个可删除的交易对手（ID已知）

**测试步骤**
1. 发送POST请求至 `/api/v1/counterparties/batch-delete`
2. 请求体：
```json
{
  "ids": [1, 2]
}
```

**预期结果**
- `code` = 0
- 数据库中对应的交易对手deleted字段被标记为"1"

---

## 四、测试数据

### 4.1 交易对手测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| code | string | CP100 | null, 空字符串 |
| name | string | 测试对手公司 | null, 空字符串 |
| enName | string | Test Corp | 超长字符串(>200) |
| counterpartyType | string | BANK | 超长字符串(>20) |
| countryCode | string | CN | 超长字符串(>10) |
| swiftCode | string | ABCNCNBJ | 超长字符串(>20) |
| status | string | 1 | 非0/1字符, null |

---

*QA产出 - v1.0*
