# Open-TMS 测试用例 - 节假日API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| 基础数据 | 节假日管理 | `/api/v1/holidays/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_HL_001 | 分页查询节假日-正常 | API | P0 | GET /api/v1/holidays/page |
| TC_HL_002 | 分页查询节假日-国家筛选 | API | P1 | GET /api/v1/holidays/page |
| TC_HL_003 | 分页查询节假日-年份筛选 | API | P1 | GET /api/v1/holidays/page |
| TC_HL_004 | 分页查询节假日-空数据 | API | P1 | GET /api/v1/holidays/page |
| TC_HL_005 | 查询节假日详情-正常 | API | P0 | GET /api/v1/holidays/{id} |
| TC_HL_006 | 查询节假日详情-ID不存在 | API | P1 | GET /api/v1/holidays/{id} |
| TC_HL_007 | 查询节假日详情-ID格式非法 | API | P1 | GET /api/v1/holidays/{id} |
| TC_HL_008 | 新增节假日-正常 | API | P0 | POST /api/v1/holidays |
| TC_HL_009 | 新增节假日-必填项为空 | API | P0 | POST /api/v1/holidays |
| TC_HL_010 | 新增节假日-日期重复 | API | P1 | POST /api/v1/holidays |
| TC_HL_011 | 更新节假日-正常 | API | P0 | PUT /api/v1/holidays |
| TC_HL_012 | 更新节假日-ID为空 | API | P1 | PUT /api/v1/holidays |
| TC_HL_013 | 更新节假日-日期重复 | API | P1 | PUT /api/v1/holidays |
| TC_HL_014 | 删除节假日-正常 | API | P0 | DELETE /api/v1/holidays/{id} |
| TC_HL_015 | 删除节假日-ID不存在 | API | P1 | DELETE /api/v1/holidays/{id} |
| TC_HL_016 | 判断日期是否为节假日 | API | P1 | GET /api/v1/holidays/isHoliday |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_HL_001 分页查询节假日-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_001 |
| 用例名称 | 分页查询节假日-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/holidays/page |

**前置条件**
1. 数据库已初始化，tms_holiday_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/page?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为节假日列表
- `total` > 0

#### TC_HL_002 分页查询节假日-国家筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_002 |
| 用例名称 | 分页查询节假日-国家筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/holidays/page |

**前置条件**
1. 数据库中存在国家代码为CN的节假日

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/page?countryCode=CN`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的countryCode都为"CN"

#### TC_HL_003 分页查询节假日-年份筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_003 |
| 用例名称 | 分页查询节假日-年份筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/holidays/page |

**前置条件**
1. 数据库中存在2026年的节假日

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/page?year=2026`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的holidayDate都在2026年内

#### TC_HL_004 分页查询节假日-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_004 |
| 用例名称 | 分页查询节假日-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/holidays/page |

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/page?countryCode=XX`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_HL_005 查询节假日详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_005 |
| 用例名称 | 查询节假日详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/holidays/{id} |

**前置条件**
1. 数据库中存在ID为1的节假日

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/1`

**预期结果**
- 响应状态码：200
- `data`包含节假日详细信息（holidayDate, name, countryCode, isAdjacent, remark）

#### TC_HL_006 查询节假日详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_006 |
| 用例名称 | 查询节假日详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/holidays/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/99999`

**预期结果**
- `code` = 404
- `message`包含"节假日不存在"

#### TC_HL_007 查询节假日详情-ID格式非法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_007 |
| 用例名称 | 查询节假日详情-ID格式非法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/holidays/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/holidays/abc`

**预期结果**
- `code` = 400
- `message`包含"ID参数格式不正确"

### 3.3 新增节假日

#### TC_HL_008 新增节假日-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_008 |
| 用例名称 | 新增节假日-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/holidays |

**前置条件**
1. 数据库中没有日期2026-10-01且国家CN的节假日

**测试步骤**
1. 发送POST请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "holidayDate": "2026-10-01",
  "name": "国庆节",
  "countryCode": "CN",
  "isAdjacent": "0",
  "remark": "国庆节假期"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中存在该节假日

#### TC_HL_009 新增节假日-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_009 |
| 用例名称 | 新增节假日-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/holidays |

**测试步骤**
1. 发送POST请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "name": "",
  "countryCode": ""
}
```

**预期结果**
- `code` 非0
- `message`包含错误提示

#### TC_HL_010 新增节假日-日期重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_010 |
| 用例名称 | 新增节假日-日期重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/holidays |

**前置条件**
1. 数据库中存在日期2026-01-01且国家CN的节假日

**测试步骤**
1. 发送POST请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "holidayDate": "2026-01-01",
  "name": "重复元旦",
  "countryCode": "CN"
}
```

**预期结果**
- `code` 非0
- `message`包含"节假日日期已存在"

### 3.4 更新节假日

#### TC_HL_011 更新节假日-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_011 |
| 用例名称 | 更新节假日-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/holidays |

**前置条件**
1. 数据库中存在ID为1的节假日

**测试步骤**
1. 发送PUT请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "id": 1,
  "name": "更新后的节假日名称"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的节假日的name已更新

#### TC_HL_012 更新节假日-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_012 |
| 用例名称 | 更新节假日-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/holidays |

**测试步骤**
1. 发送PUT请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "name": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

#### TC_HL_013 更新节假日-日期重复

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_013 |
| 用例名称 | 更新节假日-日期重复 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/holidays |

**前置条件**
1. 数据库中存在日期2026-01-01（ID=1）和日期2026-05-01（ID=2）的节假日

**测试步骤**
1. 发送PUT请求至 `/api/v1/holidays`
2. 请求体：
```json
{
  "id": 2,
  "holidayDate": "2026-01-01",
  "countryCode": "CN"
}
```

**预期结果**
- `code` 非0
- `message`包含"节假日日期已存在"

### 3.5 删除节假日

#### TC_HL_014 删除节假日-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_014 |
| 用例名称 | 删除节假日-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/holidays/{id} |

**前置条件**
1. 数据库中存在一个可删除的节假日

**测试步骤**
1. 发送DELETE请求至 `/api/v1/holidays/{该节假日的ID}`

**预期结果**
- `code` = 0
- 数据库中该节假日的deleted字段被标记为"1"

#### TC_HL_015 删除节假日-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_015 |
| 用例名称 | 删除节假日-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/holidays/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/holidays/99999`

**预期结果**
- `code` 非0
- `message`包含"节假日不存在"

### 3.6 判断节假日

#### TC_HL_016 判断日期是否为节假日

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_HL_016 |
| 用例名称 | 判断日期是否为节假日 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | (需扩展) |

**前置条件**
1. 数据库中存在日期2026-01-01且国家CN的节假日

**测试步骤**
1. 调用isHoliday方法校验 date=2026-01-01, countryCode=CN

**预期结果**
- 返回true

---

## 四、测试数据

### 4.1 节假日测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| holidayDate | date | 2026-10-01 | null, 非法日期格式 |
| name | string | 国庆节 | null, 空字符串 |
| countryCode | string | CN | null, 空字符串 |
| isAdjacent | string | 0 | 非0/1字符 |
| remark | string | 备注信息 | 超长字符串(>500) |

---

*QA产出 - v1.0*
