# Open-TMS 测试用例 - 账户转账AT API

**模块**: basedata  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**测试工程师**: QA

---

## 一、测试范围

| 模块 | 功能 | 接口 |
|------|------|------|
| AT交易 | 账户转账管理 | `/api/v1/transfer/transactions/*` |

### 测试类型
- [x] API接口测试
- [x] 功能测试

---

## 二、用例汇总

| 用例编号 | 用例名称 | 测试类型 | 优先级 | 接口 |
|----------|----------|----------|--------|------|
| TC_AT_001 | 分页查询转账-正常 | API | P0 | GET /api/v1/transfer/transactions |
| TC_AT_002 | 分页查询转账-关键字查询 | API | P1 | GET /api/v1/transfer/transactions |
| TC_AT_003 | 分页查询转账-状态筛选 | API | P1 | GET /api/v1/transfer/transactions |
| TC_AT_004 | 分页查询转账-空数据 | API | P1 | GET /api/v1/transfer/transactions |
| TC_AT_005 | 查询转账详情-正常 | API | P0 | GET /api/v1/transfer/transactions/{id} |
| TC_AT_006 | 查询转账详情-ID不存在 | API | P1 | GET /api/v1/transfer/transactions/{id} |
| TC_AT_007 | 新增转账-正常 | API | P0 | POST /api/v1/transfer/transactions |
| TC_AT_008 | 新增转账-必填项为空 | API | P0 | POST /api/v1/transfer/transactions |
| TC_AT_009 | 新增转账-收款付款账户相同 | API | P1 | POST /api/v1/transfer/transactions |
| TC_AT_010 | 更新转账-正常 | API | P0 | PUT /api/v1/transfer/transactions |
| TC_AT_011 | 更新转账-ID为空 | API | P1 | PUT /api/v1/transfer/transactions |
| TC_AT_012 | 删除转账-正常 | API | P0 | DELETE /api/v1/transfer/transactions/{id} |
| TC_AT_013 | 删除转账-ID不存在 | API | P1 | DELETE /api/v1/transfer/transactions/{id} |
| TC_AT_014 | 提交审批-正常 | API | P0 | POST /api/v1/transfer/transactions/{id}/submit |
| TC_AT_015 | 提交审批-状态不合法 | API | P1 | POST /api/v1/transfer/transactions/{id}/submit |
| TC_AT_016 | 执行转账-正常 | API | P0 | POST /api/v1/transfer/transactions/{id}/execute |
| TC_AT_017 | 取消转账-正常 | API | P0 | POST /api/v1/transfer/transactions/{id}/cancel |
| TC_AT_018 | 查询账户列表 | API | P1 | GET /api/v1/transfer/accounts |

---

## 三、详细测试用例

### 3.1 分页查询

#### TC_AT_001 分页查询转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_001 |
| 用例名称 | 分页查询转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/transfer/transactions |

**前置条件**
1. 数据库已初始化，tms_transfer_t表至少有2条数据

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions?pageNum=1&pageSize=10`

**预期结果**
- 响应状态码：200
- 返回数据结构包含 `records`、`total`、`current`、`size`
- `records`为转账列表
- `total` > 0

#### TC_AT_002 分页查询转账-关键字查询

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_002 |
| 用例名称 | 分页查询转账-关键字查询 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/transfer/transactions |

**前置条件**
1. 数据库中存在编号为TR202604060001的转账

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions?keyword=TR202604060001`

**预期结果**
- 响应状态码：200
- 返回数据中records包含该转账

#### TC_AT_003 分页查询转账-状态筛选

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_003 |
| 用例名称 | 分页查询转账-状态筛选 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/transfer/transactions |

**前置条件**
1. 数据库中存在不同状态的转账

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions?status=New`

**预期结果**
- 响应状态码：200
- 返回数据中所有records的status都为"New"

#### TC_AT_004 分页查询转账-空数据

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_004 |
| 用例名称 | 分页查询转账-空数据 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/transfer/transactions |

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions?keyword=NONEXIST`

**预期结果**
- 响应状态码：200
- `total` = 0
- `records`为空列表

### 3.2 查询详情

#### TC_AT_005 查询转账详情-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_005 |
| 用例名称 | 查询转账详情-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | GET /api/v1/transfer/transactions/{id} |

**前置条件**
1. 数据库中存在ID为1的转账

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions/1`

**预期结果**
- 响应状态码：200
- `data`包含转账详细信息（transferNo, fromAccount, toAccount, amount, currency, status等）

#### TC_AT_006 查询转账详情-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_006 |
| 用例名称 | 查询转账详情-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/transfer/transactions/{id} |

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/transactions/99999`

**预期结果**
- `code` = 404
- `message`包含"转账不存在"

### 3.3 新增转账

#### TC_AT_007 新增转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_007 |
| 用例名称 | 新增转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/transfer/transactions |

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions`
2. 请求体：
```json
{
  "transferDate": "2026-04-06",
  "managementEntity": "BU001",
  "fromAccount": "6222021234567890",
  "toAccount": "6222029876543210",
  "amount": 100000.00,
  "currency": "CNY",
  "expectedDate": "2026-04-07",
  "paymentMethod": "Transfer",
  "transferType": "Internal",
  "needAuthorization": "1",
  "applicant": "张三",
  "transferReason": "内部资金调拨"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 返回数据中包含系统生成的transferNo
- 数据库中状态为"New"

#### TC_AT_008 新增转账-必填项为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_008 |
| 用例名称 | 新增转账-必填项为空 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/transfer/transactions |

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions`
2. 请求体：
```json
{
  "fromAccount": "",
  "toAccount": "",
  "amount": null
}
```

**预期结果**
- `code` 非0
- `message`包含错误提示

#### TC_AT_009 新增转账-收款付款账户相同

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_009 |
| 用例名称 | 新增转账-收款付款账户相同 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/transfer/transactions |

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions`
2. 请求体：
```json
{
  "transferDate": "2026-04-06",
  "managementEntity": "BU001",
  "fromAccount": "6222021234567890",
  "toAccount": "6222021234567890",
  "amount": 100000.00,
  "currency": "CNY",
  "expectedDate": "2026-04-07",
  "paymentMethod": "Transfer",
  "transferType": "Internal",
  "needAuthorization": "0",
  "applicant": "张三"
}
```

**预期结果**
- `code` 非0
- `message`包含"付款账户和收款账户不能相同"

### 3.4 更新转账

#### TC_AT_010 更新转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_010 |
| 用例名称 | 更新转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | PUT /api/v1/transfer/transactions |

**前置条件**
1. 数据库中存在ID为1的转账

**测试步骤**
1. 发送PUT请求至 `/api/v1/transfer/transactions`
2. 请求体：
```json
{
  "id": 1,
  "transferReason": "更新后的转账原因"
}
```

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的转账的transferReason已更新

#### TC_AT_011 更新转账-ID为空

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_011 |
| 用例名称 | 更新转账-ID为空 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | PUT /api/v1/transfer/transactions |

**测试步骤**
1. 发送PUT请求至 `/api/v1/transfer/transactions`
2. 请求体：
```json
{
  "transferReason": "无ID更新"
}
```

**预期结果**
- `code` = 400
- `message`包含"ID不能为空"

### 3.5 删除转账

#### TC_AT_012 删除转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_012 |
| 用例名称 | 删除转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | DELETE /api/v1/transfer/transactions/{id} |

**前置条件**
1. 数据库中存在一个可删除的转账

**测试步骤**
1. 发送DELETE请求至 `/api/v1/transfer/transactions/{该转账的ID}`

**预期结果**
- `code` = 0
- 数据库中该转账的deleted字段被标记为"1"

#### TC_AT_013 删除转账-ID不存在

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_013 |
| 用例名称 | 删除转账-ID不存在 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | DELETE /api/v1/transfer/transactions/{id} |

**测试步骤**
1. 发送DELETE请求至 `/api/v1/transfer/transactions/99999`

**预期结果**
- `code` 非0
- `message`包含"转账不存在"

### 3.6 转账状态流转

#### TC_AT_014 提交审批-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_014 |
| 用例名称 | 提交审批-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/transfer/transactions/{id}/submit |

**前置条件**
1. 数据库中存在ID为1且状态为"New"的转账

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions/1/submit`

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=1的转账状态更新为"Validated"

#### TC_AT_015 提交审批-状态不合法

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_015 |
| 用例名称 | 提交审批-状态不合法 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | POST /api/v1/transfer/transactions/{id}/submit |

**前置条件**
1. 数据库中存在ID为2且状态已为"Validated"的转账

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions/2/submit`

**预期结果**
- `code` 非0
- `message`包含"只有New状态的转账可以提交"

#### TC_AT_016 执行转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_016 |
| 用例名称 | 执行转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/transfer/transactions/{id}/execute |

**前置条件**
1. 数据库中存在ID为3且状态为"Authorized"的转账

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions/3/execute`

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=3的转账状态更新为"SettlementInProcess"

#### TC_AT_017 取消转账-正常

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_017 |
| 用例名称 | 取消转账-正常 |
| 测试类型 | API功能测试 |
| 优先级 | P0 |
| 接口 | POST /api/v1/transfer/transactions/{id}/cancel |

**前置条件**
1. 数据库中存在ID为4且状态为"New"的转账

**测试步骤**
1. 发送POST请求至 `/api/v1/transfer/transactions/4/cancel`

**预期结果**
- 响应状态码：200
- `code` = 0
- 数据库中ID=4的转账状态更新为"Canceled"

### 3.7 账户查询

#### TC_AT_018 查询账户列表

| 项目 | 内容 |
|------|------|
| 用例编号 | TC_AT_018 |
| 用例名称 | 查询账户列表 |
| 测试类型 | API功能测试 |
| 优先级 | P1 |
| 接口 | GET /api/v1/transfer/accounts |

**测试步骤**
1. 发送GET请求至 `/api/v1/transfer/accounts`

**预期结果**
- 响应状态码：200
- 返回账户列表数据
- 每个账户包含 accountNo, accountName, currency 信息

---

## 四、测试数据

### 4.1 转账测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| managementEntity | string | BU001 | null, 空字符串 |
| fromAccount | string | 6222021234567890 | null, 空字符串 |
| toAccount | string | 6222029876543210 | null, 等于fromAccount |
| amount | decimal | 100000.00 | 0, 负数 |
| currency | string | CNY | null, 空字符串 |
| paymentMethod | string | Transfer | null, 非法值 |
| transferType | string | Internal | null, 非法值 |
| needAuthorization | string | 1 | 非0/1字符 |
| status | string | New | 非法状态值 |

### 4.2 转账状态流转

| 当前状态 | 可执行操作 | 下一状态 |
|----------|-----------|---------|
| New | 提交/修改/取消 | Validated/Canceled |
| Validated | 授权/修改/取消 | Authorized/Canceled |
| Authorized | 执行/取消 | SettlementInProcess/Canceled |
| SettlementInProcess | - | Settled/Failed |
| Failed | 重试/取消 | SettlementInProcess/Canceled |

---

*QA产出 - v1.0*
