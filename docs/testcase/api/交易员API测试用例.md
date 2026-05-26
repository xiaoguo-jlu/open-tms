# Open-TMS 测试用例 - 交易员API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| 基础数据 | 交易员管理 | `/api/v1/traders/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_TR_001 | 分页查询交易员-正常 | API | P0 | GET /api/v1/traders/page |
| TC_TR_002 | 分页查询交易员-关键字查询 | API | P1 | GET /api/v1/traders/page |
| TC_TR_003 | 分页查询交易员-状态筛选 | API | P1 | GET /api/v1/traders/page |
| TC_TR_004 | 分页查询交易员-空数据 | API | P1 | GET /api/v1/traders/page |
| TC_TR_005 | 查询交易员详情-正常 | API | P0 | GET /api/v1/traders/{id} |
| TC_TR_006 | 查询交易员详情-ID不存在 | API | P1 | GET /api/v1/traders/{id} |
| TC_TR_007 | 查询交易员详情-ID格式非法 | API | P1 | GET /api/v1/traders/{id} |
| TC_TR_008 | 新增交易员-正常 | API | P0 | POST /api/v1/traders |
| TC_TR_009 | 新增交易员-必填项为空 | API | P0 | POST /api/v1/traders |
| TC_TR_010 | 新增交易员-编码重复 | API | P1 | POST /api/v1/traders |
| TC_TR_011 | 更新交易员-正常 | API | P0 | PUT /api/v1/traders |
| TC_TR_012 | 更新交易员-ID为空 | API | P1 | PUT /api/v1/traders |
| TC_TR_013 | 更新交易员-编码重复 | API | P1 | PUT /api/v1/traders |
| TC_TR_014 | 删除交易员-正常 | API | P0 | DELETE /api/v1/traders/{id} |
| TC_TR_015 | 删除交易员-ID不存在 | API | P1 | DELETE /api/v1/traders/{id} |
| TC_TR_016 | 批量删除交易员-正常 | API | P1 | POST /api/v1/traders/batch-delete |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_TR_001 分页查询交易员-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_001 |
| 用例名称 | 分页查询交易员-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/traders/page |

**前置条件**
1. 数据库已初始化，tms_trader_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/page?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为交易员列表
- `total` > 0

#### TC_TR_002 分页查询交易员-关键字查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_002 |
| 用例名称 | 分页查询交易员-关键字查询 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/traders/page |

**前置条件**
1. 数据库中存在编码为T001的交易员

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/page?keyword=T001`

**预期结果**
- 响应状态码：200
- 返回数据中records包含编码为T001的交易员

#### TC_TR_003 分页查询交易员-状态筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_003 |
| 用例名称 | 分页查询交易员-状态筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/traders/page |

**前置条件**
1. 数据库中存在启用和停用的交易员

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/page?status=1`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的status都为"1"

#### TC_TR_004 分页查询交易员-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_004 |
| 用例名称 | 分页查询交易员-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/traders/page |

**前置条件**
1. 使用一个不存在的关键字

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/page?keyword=NONEXIST`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_TR_005 查询交易员详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_005 |
| 用例名称 | 查询交易员详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/traders/{id} |

**前置条件**
1. 数据库中存在ID为1的交易员

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/1`

**预期结果**
- 响应状态码：200
- `code` = 0
- `data`包含交易员详细信息（code, name, enName, department, phone, email, status）

#### TC_TR_006 查询交易员详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_006 |
| 用例名称 | 查询交易员详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/traders/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/99999`

**预期结果**
- `code` = 404
- `message`包含"交易员不存在"

#### TC_TR_007 查询交易员详情-ID格式非法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_007 |
| 用例名称 | 查询交易员详情-ID格式非法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/traders/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/traders/abc`

**预期结果**
- `code` = 400
- `message`包含"ID参数格式不正确"

### 3.3 新增交易员

#### TC_TR_008 新增交易员-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_008 |
| 用例名称 | 新增交易员-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/traders |

**前置条件**
1. 数据库中没有编码为T100的交易员

**测试步骤**
1. 发送POST请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "code": "T100",
  "name": "张三",
  "enName": "Zhang San",
  "department": "资金部",
  "phone": "13800138000",
  "email": "zhang.san@company.com",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中存在编码为T100的交易员

#### TC_TR_009 新增交易员-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_009 |
| 用例名称 | 新增交易员-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/traders |

**测试步骤**
1. 发送POST请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "code": "",
  "name": ""
}
```

**预期结果**
- 响应状态码：200
- `code` 非0
- `message`包含错误提示

#### TC_TR_010 新增交易员-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_010 |
| 用例名称 | 新增交易员-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/traders |

**前置条件**
1. 数据库中存在编码为T001的交易员

**测试步骤**
1. 发送POST请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "code": "T001",
  "name": "重复交易员",
  "status": "1"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.4 更新交易员

#### TC_TR_011 更新交易员-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_011 |
| 用例名称 | 更新交易员-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/traders |

**前置条件**
1. 数据库中存在ID为1的交易员

**测试步骤**
1. 发送PUT请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "id": 1,
  "name": "更新后的姓名",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的交易员name已更新

#### TC_TR_012 更新交易员-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_012 |
| 用例名称 | 更新交易员-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/traders |

**测试步骤**
1. 发送PUT请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "name": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

#### TC_TR_013 更新交易员-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_013 |
| 用例名称 | 更新交易员-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/traders |

**前置条件**
1. 数据库中存在编码为T001和T002的交易员

**测试步骤**
1. 发送PUT请求至 `/api/v1/traders`
2. 请求体：
```json
{
  "id": 2,
  "code": "T001"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.5 删除交易员

#### TC_TR_014 删除交易员-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_014 |
| 用例名称 | 删除交易员-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/traders/{id} |

**前置条件**
1. 数据库中存在一个可删除的交易员

**测试步骤**
1. 发送DELETE请求至 `/api/v1/traders/{该交易员的ID}`

**预期结果**
- `code` = 0
- 数据库中该交易员的deleted字段被标记为"1"

#### TC_TR_015 删除交易员-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_015 |
| 用例名称 | 删除交易员-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/traders/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/traders/99999`

**预期结果**
- `code` 非0
- `message`包含"交易员不存在"

### 3.6 批量删除

#### TC_TR_016 批量删除交易员-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_TR_016 |
| 用例名称 | 批量删除交易员-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/traders/batch-delete |

**前置条件**
1. 数据库中存在2个可删除的交易员（ID已知）

**测试步骤**
1. 发送POST请求至 `/api/v1/traders/batch-delete`
2. 请求体：
```json
{
  "ids": [1, 2]
}
```

**预期结果**
- `code` = 0
- 数据库中对应的交易员deleted字段被标记为"1"

---

## 四、测试数据

### 4.1 交易员测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| code | string | T100 | null, 空字符串 |
| name | string | 张三 | null, 空字符串 |
| enName | string | Zhang San | 超长字符串(>50) |
| department | string | 资金部 | 超长字符串(>100) |
| phone | string | 13800138000 | 超长字符串(>30) |
| email | string | test@company.com | 非法邮箱格式 |
| status | string | 1 | 非0/1字符, null |

---

*QA产出 - v1.0*
