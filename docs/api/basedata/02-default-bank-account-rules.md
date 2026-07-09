# 主体默认银行账户规则接口

**模块**: basedata
**版本**: v1.1 (2026-07-08 — P0×5 + 关键 P1×4 = 9 项修复)
**路径**: `/api/v1/default-bank-account-rules`
**完整 URL**: `/opentms/basedata/api/v1/default-bank-account-rules` (CXF 前缀)
**风格**: JAX-RS (@Path + @GET/@POST)
**依据 PRD**: `docs/prd/M1/M1-主体默认银行账户规则PRD-v1.1.md`

> **注意**:写操作统一 `POST:/update` 和 `POST:/delete/{id}`,禁止 `@PUT`/`@DELETE` (项目规范 2026-05-31)。

---

## 接口列表

| # | 方法 | 路径 | 说明 | v1.1 变更 |
|---|------|------|------|----------|
| 1 | POST | `/api/v1/default-bank-account-rules/page` | 分页查询 | 沿用 |
| 2 | GET | `/api/v1/default-bank-account-rules/{id}` | 详情(含 lockToken) | ★ 新增 lockToken 字段 |
| 3 | POST | `/api/v1/default-bank-account-rules` | 新增规则 | 沿用 |
| 4 | POST | `/api/v1/default-bank-account-rules/update` | 更新(带 lockToken) | ★ 新增 409 Conflict |
| 5 | POST | `/api/v1/default-bank-account-rules/delete/{id}` | 删除(软删) | 沿用 |
| 6 | POST | `/api/v1/default-bank-account-rules/{id}/enable` | 启用 | 沿用 |
| 7 | POST | `/api/v1/default-bank-account-rules/{id}/disable` | 停用 | 沿用 |
| 8 | **GET** | **`/api/v1/default-bank-account-rules/match`** | **★ 运行时匹配(支持 dualDirection)** | **★ v1.1 双方向 + Redis 缓存** |
| 9 | **GET** | **`/api/v1/default-bank-account-rules/test-match`** | **★ 测试匹配(返回所有命中)** | **★ v1.1 新增** |
| 10 | **GET** | **`/api/v1/default-bank-account-rules/{id}/audit-logs`** | **★ 审计历史** | **★ v1.1 新增** |
| 11 | **GET** | **`/api/v1/default-bank-account-rules/{id}/reference-count`** | **★ 被引用 N** | **★ v1.1 新增** |

**总计**: 11 个端点 (8 个 v1.0 基础 + 3 个 v1.1 新增)

---

## 字段定义(★ v1.1 关键变更)

| 字段 | DB 列 | 类型 | 必填 | 默认 | v1.1 变更 | 说明 |
|------|-------|------|------|------|----------|------|
| id | id | BIGSERIAL | - | auto | | 主键 |
| ruleNumber | rule_number | VARCHAR(50) | ✓ | 系统生成 | | RULEyyyyMMddxxxx |
| **managementEntityId** | management_entity_id | BIGINT | ✓ | - | | 主体(不能 ALL) |
| **counterpartyId** | counterparty_id | BIGINT | - | NULL=ALL | **删除 VARCHAR** | 对手方 |
| **instrumentId** | instrument_id | BIGINT | - | NULL=ALL | **删除 VARCHAR** | 金融产品 |
| direction | direction | VARCHAR(20) | ✓ | - | | Inflow/Outflow/ALL |
| **currency** | currency | VARCHAR(10) | - | **NULL=ALL** | **★ v1.1 允许 NULL** | 币种 |
| bankAccountId | bank_account_id | BIGINT | ✓ | - | | 默认银行账户 |
| status | status | VARCHAR(20) | ✓ | Active | | Active/Inactive |
| **priority** | priority | INT | ✓ | 0 | **★ v1.1 范围 0-9999** | 优先级 |
| startDate | start_date | DATE | - | NULL=立即 | | 开始生效日 |
| description | description | VARCHAR(500) | - | - | | 业务说明 |
| remark | remark | VARCHAR(500) | - | - | | 备注 |
| **lockToken** | lock_token | VARCHAR(64) | - | 自动 | **★ v1.1 新增** | 乐观锁 token(UUID) |
| **lockedBy** | locked_by | VARCHAR(50) | - | - | **★ v1.1 新增** | 锁定人 |
| **lockedAt** | locked_at | TIMESTAMP | - | - | **★ v1.1 新增** | 锁定时间 |
| createdBy | created_by | VARCHAR(50) | ✓ | system | | 创建人 |
| createdAt | created_at | TIMESTAMP | ✓ | now | | 创建时间 |
| updatedBy | updated_by | VARCHAR(50) | - | - | | 更新人 |
| **updatedAt** | updated_at | TIMESTAMP | - | - | | **更新时间(并发控制基准)** |
| version | version | INT | ✓ | 0 | | 乐观锁版本号 |
| deleted | deleted | CHAR(1) | ✓ | '0' | | 软删除标记 |

---

## 全局错误码

| HTTP | code | message | 触发场景 |
|------|------|---------|----------|
| 200 | 0 | success | 正常 |
| 400 | 400 | 参数错误 | 入参校验失败(priority 越界、必填缺失等) |
| 400 | 4001001 | 优先级超出范围 0-9999 | priority < 0 或 > 9999 |
| 400 | 4001002 | Active 规则维度组合已存在 | UNIQUE 约束冲突 |
| 400 | 4001003 | 账户不属于当前主体 | bank_account.management_entity_id ≠ rule.management_entity_id |
| 404 | 4041001 | 规则不存在 | id 不存在或已删除 |
| 409 | 4091001 | 规则已被他人修改 | **★ v1.1**:lockToken 不匹配或 updatedAt 变化 |
| 500 | 500 | 系统异常 | 未捕获异常 |

---

## 1. 分页查询

**`POST /api/v1/default-bank-account-rules/page`**

**请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "managementEntityId": 1,        // 必填(资金主管必选主体)
  "counterpartyId": 5001,          // 可选
  "instrumentId": 401,             // 可选
  "direction": "Inflow",           // 可选
  "currency": "USD",               // 可选
  "status": "Active",              // 可选,默认全部
  "keyword": "USD"                 // 可选,模糊匹配 rule_number/description
}
```

**排序**: 默认 `priority DESC, created_at ASC`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 12,
        "ruleNumber": "RULE202607080001",
        "managementEntityId": 1,
        "managementEntityName": "集团总部",
        "counterpartyId": 5001,
        "counterpartyName": "中国银行",
        "instrumentId": 401,
        "instrumentName": "FX-SPOT-USD-CNY",
        "direction": "Inflow",
        "currency": "USD",
        "bankAccountId": 1001,
        "bankAccountName": "中行美元账户 #1001",
        "status": "Active",
        "priority": 200,
        "startDate": "2026-07-08",
        "description": "USD SPOT 默认收账账户",
        "lockToken": "uuid-abc-123-def",
        "createdBy": "admin",
        "createdAt": "2026-07-08T09:00:00",
        "updatedBy": "admin",
        "updatedAt": "2026-07-08T10:30:00"
      }
    ],
    "total": 18,
    "size": 20,
    "current": 1
  },
  "timestamp": 1751731200000
}
```

> **说明**:响应中通过 `EntityNameLookup` 自动补全 `*Name` 字段(基于 5 维 FK → 名称映射)。

---

## 2. 详情查询

**`GET /api/v1/default-bank-account-rules/{id}`**

**路径参数**:
- `id` — 规则主键(Long)或 rule_number(String,如 "RULE202607080001")

**响应**(完整字段,含 ★ v1.1 lockToken):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12,
    "ruleNumber": "RULE202607080001",
    "managementEntityId": 1,
    "managementEntityName": "集团总部",
    "counterpartyId": 5001,
    "counterpartyName": "中国银行",
    "instrumentId": 401,
    "instrumentName": "FX-SPOT-USD-CNY",
    "direction": "Inflow",
    "currency": "USD",
    "bankAccountId": 1001,
    "bankAccountName": "中行美元账户 #1001",
    "status": "Active",
    "priority": 200,
    "startDate": "2026-07-08",
    "description": "USD SPOT 默认收账账户",
    "remark": "适用于中行对手方",
    "lockToken": "uuid-abc-123-def",
    "lockedBy": null,
    "lockedAt": null,
    "createdBy": "admin",
    "createdAt": "2026-07-08T09:00:00",
    "updatedBy": "admin",
    "updatedAt": "2026-07-08T10:30:00",
    "version": 3,
    "deleted": "0"
  },
  "timestamp": 1751731200000
}
```

**错误响应**:
- 404 — 规则不存在

---

## 3. 新增规则

**`POST /api/v1/default-bank-account-rules`**

**请求体**:
```json
{
  "managementEntityId": 1,          // 必填
  "counterpartyId": 5001,            // 可选,NULL=ALL
  "instrumentId": 401,               // 可选,NULL=ALL
  "direction": "Inflow",             // 必填,Inflow/Outflow/ALL
  "currency": "USD",                 // 可选,NULL=ALL
  "bankAccountId": 1001,             // 必填
  "priority": 100,                   // 必填,范围 0-9999
  "startDate": "2026-07-08",         // 可选,NULL=立即生效
  "status": "Active",                // 必填,Active/Inactive
  "description": "USD SPOT 默认收账账户",
  "remark": "适用于中行对手方"
}
```

**响应**(完整规则 VO):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12,
    "ruleNumber": "RULE202607080001",
    "lockToken": "uuid-abc-123-def",
    ...
  },
  "timestamp": 1751731200000
}
```

**副作用**:
- 服务端生成 `rule_number`(`RULE + yyyyMMdd + 4 位流水`)
- 服务端生成 `lockToken`(UUID)
- **写入审计日志**(operation=CREATE)

**错误响应**:
- 4001001 — priority 越界
- 4001002 — Active 唯一约束冲突(`RULE202607080005 已存在`)
- 4001003 — 账户不属于当前主体
- 400 — 必填缺失

---

## 4. 更新规则(★ v1.1 带锁)

**`POST /api/v1/default-bank-account-rules/update`**

**请求体**:
```json
{
  "id": 12,
  "lockToken": "uuid-abc-123-def",   // ★ v1.1 必填,从前次查询的 lockToken
  "priority": 200,                   // 可修改
  "description": "调整优先级",        // 可修改
  "bankAccountId": 1001,             // 可修改
  "status": "Active",                // 可修改
  "remark": "...",
  "version": 3                       // 乐观锁
}
```

**校验逻辑**:
1. 根据 id 查出当前规则
2. 比较 `currentRule.lockToken == request.lockToken`
3. 若不一致:返回 **409 Conflict**
4. 若一致:更新字段,version+1,生成新 lockToken
5. **写入审计日志**(operation=UPDATE,old_value/new_value JSONB)

**响应**(成功):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12,
    "lockToken": "uuid-xyz-789-new",   // ★ 新 token
    "version": 4,
    ...
  }
}
```

**错误响应**(并发冲突):
```json
{
  "code": 409,
  "message": "规则已被他人修改(updated_at=2026-07-08T11:00:00),请刷新后重试",
  "data": null,
  "timestamp": 1751731200000
}
```

**限制**:
- `managementEntityId` 不可修改(主体的根)
- `ruleNumber` 不可修改

---

## 5. 删除规则(软删)

**`POST /api/v1/default-bank-account-rules/delete/{id}`**

**路径参数**:
- `id` — 规则主键(Long)或 rule_number(String)

**响应**(删除前返回被引用数):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted": true,
    "ruleId": 12,
    "referenceCount": {
      "unsettledCount": 5,
      "recentSettledCount": 23,
      "totalCount": 28,
      "queryDurationMs": 18
    }
  },
  "timestamp": 1751731200000
}
```

**副作用**:
- `deleted = '1'`(软删)
- **写入审计日志**(operation=DELETE)
- **清理 lockToken**(`lockToken = NULL`)

**注意**:
- 前端在调用前需先调用 `/reference-count` 获取 N,弹出确认"该规则已被 N 笔交易引用"
- 即使有引用,本设计仍允许删除(数据保留在审计日志中)

---

## 6. 启用规则

**`POST /api/v1/default-bank-account-rules/{id}/enable`**

**路径参数**: `id`

**响应**:
```json
{ "code": 200, "message": "启用成功" }
```

**副作用**:
- `status = 'Active'`
- **写入审计日志**(operation=ENABLE)
- 失效 Redis 缓存(规则变更)

---

## 7. 停用规则

**`POST /api/v1/default-bank-account-rules/{id}/disable`**

**路径参数**: `id`

**响应**:
```json
{ "code": 200, "message": "停用成功" }
```

**副作用**:
- `status = 'Inactive'`
- **写入审计日志**(operation=DISABLE)
- 失效 Redis 缓存

---

## 8. ★ 运行时匹配接口(核心 — v1.1 双方向)

**`GET /api/v1/default-bank-account-rules/match`**

**Query 参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| managementEntityId | Long | ✓ | 主体 |
| counterpartyId | Long | - | 对手方(可省=ALL) |
| instrumentId | Long | - | 金融产品(可省=ALL) |
| direction | String | - | Inflow/Outflow(仅 dualDirection=false 必填) |
| currency | String | - | 币种(可省=ALL) |
| **dualDirection** | **Boolean** | - (默认 false) | **★ v1.1 是否双方向匹配** |

**算法**:
1. 基础过滤:`status='Active'` AND `(start_date IS NULL OR start_date <= today)` AND `deleted='0'`
2. 5 维过滤(主体相等 + 其他维度 NULL OR 相等)
3. **Redis 缓存查询**(key=`dbar:match:{mgmtId}:{cpId}:{insId}:{direction}:{cur}`,TTL=5min)
4. 排序:`priority DESC, created_at ASC`
5. **双方向各自取首条**

**响应(dualDirection=true,FX 录入联动)**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "inflow": {
      "matched": true,
      "bankAccountId": 1001,
      "bankAccountName": "中行美元账户 #1001",
      "ruleId": 12,
      "ruleNumber": "RULE202607080001",
      "priority": 200
    },
    "outflow": {
      "matched": true,
      "bankAccountId": 1002,
      "bankAccountName": "中行人民币账户 #1002",
      "ruleId": 15,
      "ruleNumber": "RULE202607080005",
      "priority": 180
    },
    "cacheHit": true,
    "queryDurationMs": 12
  },
  "timestamp": 1751731200000
}
```

**响应(dualDirection=false,单方向兼容)**:
```json
{
  "code": 200,
  "data": {
    "matched": true,
    "bankAccountId": 1001,
    "ruleId": 12,
    "ruleNumber": "RULE202607080001",
    "priority": 200
  }
}
```

**响应(无匹配)**:
```json
{
  "code": 200,
  "data": {
    "matched": false,
    "bankAccountId": null,
    "ruleId": null
  }
}
```

**响应(双方向无匹配)**:
```json
{
  "code": 200,
  "data": {
    "inflow": { "matched": false, "bankAccountId": null },
    "outflow": { "matched": false, "bankAccountId": null }
  }
}
```

**缓存约定**:
- Key: `dbar:match:{mgmtId}:{cpId}:{insId}:{direction}:{cur}`
- TTL: 5 分钟
- 失效时机:规则 CRUD/启用/停用时,主动 `redisTemplate.delete(pattern)`

**降级**:
- Redis 不可用时 → 直接 DB 查询 + 日志告警 + 正常返回(不阻塞业务)

---

## 9. ★ 测试匹配接口(运营调试)

**`GET /api/v1/default-bank-account-rules/test-match`**

**Query 参数**: 与 /match 相同

**响应**(返回所有命中规则,不取首条):
```json
{
  "code": 200,
  "data": {
    "matchedCount": 3,
    "matchedRules": [
      {
        "ruleId": 12,
        "ruleNumber": "RULE202607080001",
        "direction": "Inflow",
        "priority": 200,
        "bankAccountId": 1001,
        "bankAccountName": "中行美元账户 #1001",
        "status": "Active",
        "description": "USD SPOT 默认收账账户"
      },
      {
        "ruleId": 15,
        "ruleNumber": "RULE202607080005",
        "direction": "Outflow",
        "priority": 180,
        "bankAccountId": 1002,
        "bankAccountName": "中行人民币账户 #1002",
        "status": "Active"
      },
      {
        "ruleId": 18,
        "ruleNumber": "RULE202607080010",
        "direction": "ALL",
        "priority": 50,
        "bankAccountId": 1003,
        "bankAccountName": "通用美元账户 #1003",
        "status": "Active"
      }
    ],
    "selectedInflow": { "ruleId": 12, ... },
    "selectedOutflow": { "ruleId": 15, ... },
    "queryDurationMs": 18
  }
}
```

---

## 10. ★ 审计历史接口

**`GET /api/v1/default-bank-account-rules/{id}/audit-logs`**

**Query 参数**:
| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| pageNum | int | 1 | 页码 |
| pageSize | int | 20 | 每页大小 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1001,
        "operation": "UPDATE",
        "oldValue": { "priority": 100, "description": "..." },
        "newValue": { "priority": 200, "description": "..." },
        "operator": "admin",
        "operatedAt": "2026-07-08T10:30:00",
        "remark": "调整优先级"
      },
      {
        "id": 1000,
        "operation": "CREATE",
        "newValue": { "ruleNumber": "RULE202607080001", "priority": 100 },
        "operator": "admin",
        "operatedAt": "2026-07-08T09:00:00"
      }
    ],
    "total": 15,
    "size": 20,
    "current": 1
  }
}
```

---

## 11. ★ 被引用数查询

**`GET /api/v1/default-bank-account-rules/{id}/reference-count`**

**响应**:
```json
{
  "code": 200,
  "data": {
    "ruleId": 12,
    "bankAccountId": 1001,
    "unsettledCount": 5,
    "recentSettledCount": 23,
    "totalCount": 28,
    "queryDurationMs": 18
  }
}
```

**查询逻辑**:
- `unsettledCount`: 查 `tms_deals_t` 中 status IN ('New','Submitted','Approved') 且 bank_account_id 关联
- `recentSettledCount`: 查 status='Settled' 且 updated_at >= NOW() - INTERVAL '90 days'
- 注意:当前 tms_deals_t 无 bank_account_id 列,实际查询通过 deal_image 快照或后续 P1+ 改造

---

## 跨模块接口契约

### 12. dealing → basedata(运行时调用)

**调用方**: `dealing` 模块 FX/AC/AT 录入 Service

**调用方式**: HTTP 同步(Apache HttpClient 或 OpenFeign)

**调用端点**: `GET /opentms/basedata/api/v1/default-bank-account-rules/match?managementEntityId=...&counterpartyId=...&instrumentId=...&currency=...&dualDirection=true`

**契约**:
- 调用方必须传 `dualDirection=true`(FX 录入需要同时拿 Inflow + Outflow)
- 调用方负责 Redis 缓存的本地管理(避免重复请求)
- 调用方处理 200/400/404/500 错误码
- 调用方不需要 lockToken(match 是纯读)

### 13. Vite 代理(前端 → 后端)

**web/vite.config.js**:
```js
'/api/v1/default-bank-account-rules': {
  target: 'http://localhost:8081/opentms/basedata',
  changeOrigin: true
}
```

### 14. 跨域 & 同源

- 前端(Vue) 与 basedata(8081) 跨端口,通过 Vite 代理转发
- 生产环境 Nginx 统一代理

---

## 幂等性设计

| 端点 | 是否幂等 | 实现 |
|------|----------|------|
| POST /page | - | 纯读,无需幂等 |
| GET /{id} | - | 纯读 |
| POST / | 否 | 依赖 `X-Idempotency-Key` 请求头(项目通用规范) |
| POST /update | **是** | 依赖 `lockToken`(业务级幂等) |
| POST /delete/{id} | **是** | 依赖 `X-Idempotency-Key` |
| POST /{id}/enable | **是** | 依赖 `X-Idempotency-Key`(状态变更幂等) |
| POST /{id}/disable | **是** | 依赖 `X-Idempotency-Key` |
| GET /match | - | 纯读 |
| GET /test-match | - | 纯读 |
| GET /{id}/audit-logs | - | 纯读 |
| GET /{id}/reference-count | - | 纯读 |

---

## 性能 SLA(★ v1.1 明确)

| 端点 | P99 响应时间 | 并发 |
|------|--------------|------|
| POST /page | < 100ms | 100 QPS |
| GET /{id} | < 30ms | 100 QPS |
| POST / | < 150ms | 50 QPS |
| POST /update | < 150ms | 50 QPS |
| POST /delete/{id} | < 100ms | 50 QPS |
| GET /match(缓存命中) | < 20ms | 200 QPS |
| GET /match(缓存未命中) | < 50ms | 100 QPS |
| GET /test-match | < 100ms | 50 QPS |
| GET /{id}/audit-logs | < 100ms | 50 QPS |
| GET /{id}/reference-count | < 50ms | 50 QPS |

---

## DDL ↔ API 字段映射(对账)

| DDL 列 | Java 字段 | API 字段 | 类型 |
|--------|-----------|----------|------|
| id | id | id | Long |
| rule_number | ruleNumber | ruleNumber | String |
| management_entity_id | managementEntityId | managementEntityId | Long |
| counterparty_id | counterpartyId | counterpartyId | Long |
| instrument_id | instrumentId | instrumentId | Long |
| direction | direction | direction | String |
| currency | currency | currency | String |
| bank_account_id | bankAccountId | bankAccountId | Long |
| status | status | status | String |
| priority | priority | priority | Integer |
| start_date | startDate | startDate | LocalDate |
| description | description | description | String |
| remark | remark | remark | String |
| **lock_token** | **lockToken** | **lockToken** | **String** |
| **locked_by** | **lockedBy** | **lockedBy** | **String** |
| **locked_at** | **lockedAt** | **lockedAt** | **LocalDateTime** |
| created_by | createdBy | createdBy | String |
| created_at | createdAt | createdAt | LocalDateTime |
| updated_by | updatedBy | updatedBy | String |
| updated_at | updatedAt | updatedAt | LocalDateTime |
| version | version | version | Integer |
| deleted | deleted | deleted | String |

★ 标记字段为 v1.1 新增。

---

## 与 v1.0 关键差异

| # | 变更 | v1.0 | v1.1 |
|---|------|------|------|
| 1 | match 接口 | 单方向,返回 1 账户 | ★ 双方向 `dualDirection=true`,返回 inflow + outflow |
| 2 | 更新接口 | 无锁 | ★ `lockToken` + 409 Conflict |
| 3 | 新增端点 | 8 个 | 11 个(新增 test-match / audit-logs / reference-count) |
| 4 | 删除返回 | 仅成功标志 | ★ 同时返回被引用 N |
| 5 | 缓存 | 无 | ★ Redis 5min TTL(降级方案) |
| 6 | 字段 | 含 VARCHAR 冗余 | ★ 删除 VARCHAR,仅 _id BIGINT |
| 7 | currency 字段 | NOT NULL DEFAULT 'ALL' | ★ 允许 NULL |
| 8 | priority 字段 | 无范围 | ★ CHECK 0-9999 |
| 9 | 唯一约束 | 无 | ★ Active 6 列 UNIQUE |

---

## 相关文档

- PRD v1.1: `docs/prd/M1/M1-主体默认银行账户规则PRD-v1.1.md`
- DDL: `db/schema/28-default-bank-account-rule-v1.1.sql`
- 银行账户 API 参考: `docs/api/basedata/01-bank-accounts.md`
- 接口规范: `docs/规范/Open-TMS开发规范文档.md`

---

*API 产出 - v1.1 (2026-07-08)*
*11 端点 · CXF JAX-RS · 支持 dualDirection 双方向匹配 + 并发控制 + 审计日志 + Redis 缓存*