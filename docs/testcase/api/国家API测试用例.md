# Open-TMS 测试用例 - 国家API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| 基础数据 | 国家管理 | `/api/v1/countries/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_CN_001 | 分页查询国家-正常 | API | P0 | GET /api/v1/countries/page |
| TC_CN_002 | 分页查询国家-关键字查询 | API | P1 | GET /api/v1/countries/page |
| TC_CN_003 | 分页查询国家-状态筛选 | API | P1 | GET /api/v1/countries/page |
| TC_CN_004 | 分页查询国家-空数据 | API | P1 | GET /api/v1/countries/page |
| TC_CN_005 | 查询国家详情-正常 | API | P0 | GET /api/v1/countries/{id} |
| TC_CN_006 | 查询国家详情-ID不存在 | API | P1 | GET /api/v1/countries/{id} |
| TC_CN_007 | 查询国家详情-ID格式非法 | API | P1 | GET /api/v1/countries/{id} |
| TC_CN_008 | 新增国家-正常 | API | P0 | POST /api/v1/countries |
| TC_CN_009 | 新增国家-必填项为空 | API | P0 | POST /api/v1/countries |
| TC_CN_010 | 新增国家-编码重复 | API | P1 | POST /api/v1/countries |
| TC_CN_011 | 更新国家-正常 | API | P0 | PUT /api/v1/countries |
| TC_CN_012 | 更新国家-ID为空 | API | P1 | PUT /api/v1/countries |
| TC_CN_013 | 更新国家-编码重复 | API | P1 | PUT /api/v1/countries |
| TC_CN_014 | 删除国家-正常 | API | P0 | DELETE /api/v1/countries/{id} |
| TC_CN_015 | 删除国家-ID不存在 | API | P1 | DELETE /api/v1/countries/{id} |
| TC_CN_016 | 批量删除国家-正常 | API | P1 | POST /api/v1/countries/batch-delete |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_CN_001 分页查询国家-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_001 |
| 用例名称 | 分页查询国家-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/countries/page |

**前置条件**
1. 数据库已初始化，tms_country_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/page?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为国家列表
- `total` > 0

#### TC_CN_002 分页查询国家-关键字查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_002 |
| 用例名称 | 分页查询国家-关键字查询 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/countries/page |

**前置条件**
1. 数据库中存在编码为CN的国家

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/page?keyword=CN`

**预期结果**
- 响应状态码：200
- 返回数据中records包含编码为CN的国家

#### TC_CN_003 分页查询国家-状态筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_003 |
| 用例名称 | 分页查询国家-状态筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/countries/page |

**前置条件**
1. 数据库中存在启用和停用的国家

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/page?status=1`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的status都为"1"

#### TC_CN_004 分页查询国家-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_004 |
| 用例名称 | 分页查询国家-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/countries/page |

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/page?keyword=NONEXIST`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_CN_005 查询国家详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_005 |
| 用例名称 | 查询国家详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/countries/{id} |

**前置条件**
1. 数据库中存在ID为1的国家

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/1`

**预期结果**
- 响应状态码：200
- `data`包含国家详细信息（code, name, enName, timezone, countryNo, status）

#### TC_CN_006 查询国家详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_006 |
| 用例名称 | 查询国家详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/countries/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/99999`

**预期结果**
- `code` = 404
- `message`包含"国家不存在"

#### TC_CN_007 查询国家详情-ID格式非法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_007 |
| 用例名称 | 查询国家详情-ID格式非法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/countries/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/countries/abc`

**预期结果**
- `code` = 400
- `message`包含"ID参数格式不正确"

### 3.3 新增国家

#### TC_CN_008 新增国家-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_008 |
| 用例名称 | 新增国家-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/countries |

**前置条件**
1. 数据库中没有编码为DE的国家

**测试步骤**
1. 发送POST请求至 `/api/v1/countries`
2. 请求体：
```json
{
  "code": "DE",
  "name": "德国",
  "enName": "Germany",
  "timezone": "Europe/Berlin",
  "countryNo": "276",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中存在编码为DE的国家

#### TC_CN_009 新增国家-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_009 |
| 用例名称 | 新增国家-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/countries |

**测试步骤**
1. 发送POST请求至 `/api/v1/countries`
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

#### TC_CN_010 新增国家-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_010 |
| 用例名称 | 新增国家-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/countries |

**前置条件**
1. 数据库中存在编码为CN的国家

**测试步骤**
1. 发送POST请求至 `/api/v1/countries`
2. 请求体：
```json
{
  "code": "CN",
  "name": "重复国家",
  "status": "1"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.4 更新国家

#### TC_CN_011 更新国家-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_011 |
| 用例名称 | 更新国家-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/countries |

**前置条件**
1. 数据库中存在ID为1的国家

**测试步骤**
1. 发送PUT请求至 `/api/v1/countries`
2. 请求体：
```json
{
  "id": 1,
  "name": "更新后的国家名称",
  "status": "1"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的国家的name已更新

#### TC_CN_012 更新国家-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_012 |
| 用例名称 | 更新国家-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/countries |

**测试步骤**
1. 发送PUT请求至 `/api/v1/countries`
2. 请求体：
```json
{
  "name": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

#### TC_CN_013 更新国家-编码重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_013 |
| 用例名称 | 更新国家-编码重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/countries |

**前置条件**
1. 数据库中存在编码为CN和US的国家

**测试步骤**
1. 发送PUT请求至 `/api/v1/countries`
2. 请求体：
```json
{
  "id": 2,
  "code": "CN"
}
```

**预期结果**
- `code` 非0
- `message`包含"代码已存在"

### 3.5 删除国家

#### TC_CN_014 删除国家-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_014 |
| 用例名称 | 删除国家-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/countries/{id} |

**前置条件**
1. 数据库中存在一个可删除的国家

**测试步骤**
1. 发送DELETE请求至 `/api/v1/countries/{该国家的ID}`

**预期结果**
- `code` = 0
- 数据库中该国家的deleted字段被标记为"1"

#### TC_CN_015 删除国家-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_015 |
| 用例名称 | 删除国家-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/countries/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/countries/99999`

**预期结果**
- `code` 非0
- `message`包含"国家不存在"

### 3.6 批量删除

#### TC_CN_016 批量删除国家-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_CN_016 |
| 用例名称 | 批量删除国家-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/countries/batch-delete |

**前置条件**
1. 数据库中存在2个可删除的国家（ID已知）

**测试步骤**
1. 发送POST请求至 `/api/v1/countries/batch-delete`
2. 请求体：
```json
{
  "ids": [1, 2]
}
```

**预期结果**
- `code` = 0
- 数据库中对应的国家deleted字段被标记为"1"

---

## 四、测试数据

### 4.1 国家测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| code | string | DE | null, 空字符串 |
| name | string | 德国 | null, 空字符串 |
| enName | string | Germany | 超长字符串(>100) |
| timezone | string | Europe/Berlin | 超长字符串(>50) |
| countryNo | string | 276 | 超长字符串(>10) |
| status | string | 1 | 非0/1字符, null |

---

*QA产出 - v1.0*
