# 银行账户管理接口

**模块**: basedata
**版本**: v1.2 (2026-06-29 字段扩展 + 规范化)
**路径**: `/api/v1/bank-accounts`
**完整 URL**: `/opentms/basedata/api/v1/bank-accounts`(CXF 前缀)
**风格**: JAX-RS(@Path + @GET/@POST)

> **注意**:写操作统一 `POST:/update` 和 `POST:/delete/{id}`,禁止 `@PUT`/`@DELETE`(项目规范 2026-05-31)。

---

## 接口列表

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/api/v1/bank-accounts/page` | 分页查询 |
| 2 | GET | `/api/v1/bank-accounts/{id}` | 详情 |
| 3 | GET | `/api/v1/bank-accounts/{id}/balance` | 余额查询 |
| 4 | POST | `/api/v1/bank-accounts` | 新增 |
| 5 | POST | `/api/v1/bank-accounts/update` | 更新 |
| 6 | POST | `/api/v1/bank-accounts/delete/{id}` | 删除 |
| 7 | POST | `/api/v1/bank-accounts/{id}/sync` | 银企同步(Stub) |

---

## 字段定义

| 字段 | DB 列 | 类型 | 必填 | 说明 |
|------|-------|------|------|------|
| id | id | BIGSERIAL | - | 主键 |
| accountNo | account_no | VARCHAR | ✓ | 账号 |
| accountName | account_name | VARCHAR | ✓ | 账户名 |
| account | account | VARCHAR(100) |  | **新增**:账户别名 |
| accountNature | account_nature | VARCHAR(20) |  | **新增**:Internal/External |
| isCollected | is_collected | CHAR(1) |  | **新增**:0=否 1=是 是否归集 |
| collectDirection | collect_direction | VARCHAR(20) |  | **新增**:Up/Down |
| mainAccountId | main_account_id | BIGINT |  | **新增**:主账户ID |
| dayLimit | day_limit | DECIMAL(18,2) |  | **新增**:日累计限额 |
| nightLimit | night_limit | DECIMAL(18,2) |  | **新增**:夜间限额 |
| balance | balance | DECIMAL(38,18) |  | **新增**:当前余额 |
| availableBalance | available_balance | DECIMAL(38,18) |  | **新增**:可用余额 |
| frozenBalance | frozen_balance | DECIMAL(38,18) |  | **新增**:冻结余额 |
| bankId | bank_id | BIGINT | ✓ | 银行ID |
| currency | currency | VARCHAR | ✓ | 币种 |
| accountType | account_type | VARCHAR |  | Savings/Current |
| businessUnitId | business_unit_id | BIGINT | ✓ | 业务单元 |
| status | status | CHAR(1) |  | 1=启用 0=禁用 |
| remark | remark | VARCHAR |  | 备注 |
| 审计字段 | created_by, created_at, updated_by, updated_at, version, deleted | | | 必含审计字段 |

---

## 1. 分页查询

**`GET /api/v1/bank-accounts/page`**

查询参数:
| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| keyword | String |  |  | 模糊匹配 accountNo/accountName |
| bankId | Long |  |  | 银行ID |
| currency | String |  |  | 币种 |
| accountType | String |  |  | 账户类型 |
| businessUnitId | Long |  |  | 业务单元 |
| status | String |  |  | 1=启用 0=禁用 |
| pageNum | int |  | 1 | 页码 |
| pageSize | int |  | 10 | 每页大小 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

## 2. 详情

**`GET /api/v1/bank-accounts/{id}`**

**响应**:单个 BankAccount 完整字段

## 3. 余额查询(新增)

**`GET /api/v1/bank-accounts/{id}/balance`**

**响应**:
```json
{
  "code": 200,
  "data": {
    "balance": 1000000.00,
    "availableBalance": 950000.00,
    "frozenBalance": 50000.00
  }
}
```

## 4. 新增

**`POST /api/v1/bank-accounts`**

**请求体**:BankAccount 对象(JSON)

## 5. 更新

**`POST /api/v1/bank-accounts/update`**

**请求体**:BankAccount 对象(JSON,含 id 与 version)

## 6. 删除

**`POST /api/v1/bank-accounts/delete/{id}`**

**路径参数**:id

## 7. 银企同步(Stub)

**`POST /api/v1/bank-accounts/{id}/sync`**

**响应**:
```json
{
  "code": 200,
  "data": {
    "accountId": 1,
    "accountNo": "6225888888888888",
    "message": "同步任务已提交(Stub)"
  }
}
```

> 注:实际对接银企接口异步同步余额与流水。

---

## 变更历史

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-06-29 | v1.2 | 字段扩展(10 个)+ @PUT/@DELETE → POST 规范化 + 新增 /balance 和 /sync 端点;**银行账户从独立 bankaccount 模块合并到 basedata** |
| 2026-05-31 | v1.1 | POST 统一 update/delete |
| 2026-04-15 | v1.0 | 初版,基于 bankaccount 模块 |
