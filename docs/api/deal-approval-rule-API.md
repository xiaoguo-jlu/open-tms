# 交易审批规则(Deal Approval Rule)— API 契约

> 特性: 基于 5 维要素(主体 / 对手方 / 工具 / 交易员 / 操作类型)的灵活审批规则
> 范围: Phase 4 API 设计
> 依赖: PRD `docs/prd/M3/M3-交易审批规则PRD.md` + DDL `db/schema/30-deal-approval-rule.sql`
> 模块归属: **basedata**(规则配置,端口 8081,Apache CXF JAX-RS)
> 对标参考: `docs/api/cashflow-enhance-API.md` + v1.1 default-bank-account-rule 11 端点

---

## 1. 总体设计

### 1.1 基础路径

```
/api/v1/deal-approval-rules
```

### 1.2 风格一致性(与 CLAUDE.md + v1.1 一致)

- ✅ Apache CXF 4.0.3 + JAX-RS,基于 `DefaultBankAccountRuleResource` 模式
- ✅ 写操作统一 `POST`(update / delete / enable / disable / save)
- ✅ 响应 `Result<T> = {code, message, data, timestamp}`
- ✅ 状态字符串走 `GlobalConstants`,**不新建魔术字符串**
- ✅ 审计字段齐全,主表 + image 表 + audit_log 表三表分立
- ✅ 金额精度沿用各表 DECIMAL(38,18) / DECIMAL(18,8)(本特性无金额字段)
- ✅ 并发控制 `lockToken` + 409 Conflict(沿用 v1.1)
- ✅ 唯一约束 + partial unique index(Active 状态生效)

### 1.3 端点清单(12 个)

| # | 方法 | Path | 说明 | 对标 v1.1 |
|---|------|------|------|-----------|
| 1 | POST | `/api/v1/deal-approval-rules/page` | 分页查询 | `DefaultBankAccountRuleResource.page` |
| 2 | GET | `/api/v1/deal-approval-rules/{id}` | 详情(id 或 ruleNumber) | `getById(String)` |
| 3 | POST | `/api/v1/deal-approval-rules` | 新增规则 | `save` |
| 4 | POST | `/api/v1/deal-approval-rules/update` | 更新规则(校验 lockToken) | `update` |
| 5 | POST | `/api/v1/deal-approval-rules/delete/{id}` | 软删除 | `delete` |
| 6 | POST | `/api/v1/deal-approval-rules/{id}/enable` | 启用 | `enable` |
| 7 | POST | `/api/v1/deal-approval-rules/{id}/disable` | 停用 | `disable` |
| 8 | **GET** | **`/api/v1/deal-approval-rules/match`** | **★ 运行时匹配(本特性关键)** | **本特性新增,见 §3** |
| 9 | GET | `/api/v1/deal-approval-rules/test-match` | 测试匹配(返回所有候选) | `test-match` |
| 10 | GET | `/api/v1/deal-approval-rules/{id}/audit-logs` | 审计日志分页 | `audit-logs` |
| 11 | GET | `/api/v1/deal-approval-rules/{id}/reference-count` | 被引用数 | `reference-count` |
| 12 | GET | `/api/v1/deal-approval-rules/{id}/images` | 镜像版本列表 | **本特性新增,见 §4** |

> 对比 v1.1:本特性 = v1.1 的 11 端点 + 1 个 image 端点(本特性独有)。

---

## 2. 基础 CRUD 端点(沿用 v1.1)

### 2.1 分页查询

```http
POST /api/v1/deal-approval-rules/page
Content-Type: application/json
```

**请求体 DealApprovalRuleQueryDTO**:

```json
{
  "ruleNumber": "DAR202607110001",
  "managementEntityId": 10,
  "counterpartyId": 1001,
  "instrumentId": 301,
  "dealerId": 5,
  "actionType": "SUBMIT",
  "approvalLevel": "LEVEL_1",
  "status": "Active",
  "keyword": "FX",
  "startDateFrom": "2026-07-01",
  "startDateTo": "2026-07-31",
  "pageNum": 1,
  "pageSize": 20,
  "orderBy": "priority",
  "orderDirection": "DESC"
}
```

**响应 200**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 12,
        "ruleNumber": "DAR202607110001",
        "managementEntityId": null,
        "managementEntityName": null,
        "counterpartyId": 1001,
        "counterpartyName": "HSBC Global",
        "instrumentId": 301,
        "instrumentName": "FX Forward",
        "dealerId": null,
        "dealerName": null,
        "actionType": "SUBMIT",
        "actionTypeLabel": "提交审批",
        "approvalLevel": "LEVEL_1",
        "level1Roles": ["RISK_MANAGER"],
        "level2Roles": [],
        "priority": 100,
        "status": "Active",
        "startDate": null,
        "endDate": null,
        "description": "FX 一层审批规则",
        "remark": "合规部 2026-07 配置",
        "lockToken": "uuid-v4-token-001",
        "createdBy": "compliance_officer",
        "createdAt": "2026-07-11T10:00:00Z",
        "updatedBy": "compliance_officer",
        "updatedAt": "2026-07-11T10:30:00Z",
        "version": 2
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1752230400000
}
```

### 2.2 详情

```http
GET /api/v1/deal-approval-rules/{id}
```

- `id` (String):Long id 或 ruleNumber(沿用 v1.1,自动识别)

**响应 200**:

```json
{
  "code": 200,
  "data": {
    "dealApprovalRule": { /* 同 page 列表元素结构 */ },
    "imageNumber": "IMG-DAR202607110001-V3",
    "actionNumber": "ACT-DAR202607110001-005"
  }
}
```

### 2.3 新增

```http
POST /api/v1/deal-approval-rules
Content-Type: application/json
```

**请求体 DealApprovalRuleSaveDTO**:

```json
{
  "managementEntityId": null,
  "counterpartyId": 1001,
  "instrumentId": 301,
  "dealerId": null,
  "actionType": "SUBMIT",
  "approvalLevel": "LEVEL_2",
  "level1Roles": ["RISK_MANAGER"],
  "level2Roles": ["COMPLIANCE_OFFICER"],
  "priority": 200,
  "status": "Active",
  "startDate": "2026-07-15",
  "endDate": "2026-12-31",
  "description": "HSBC FX 跨主体合规审批",
  "remark": "合规部 2026-07 配置"
}
```

**响应 200**(返回生成规则 + lockToken):

```json
{
  "code": 200,
  "data": {
    "id": 13,
    "ruleNumber": "DAR202607110002",
    "lockToken": "uuid-v4-token-013",
    "...": "其余字段同 DTO"
  }
}
```

**错误码**:

| code | message | 触发场景 |
|------|---------|----------|
| 400 | 操作类型必填 | `actionType` 为空 |
| 400 | 审批层级非法 | `approvalLevel` ∉ {LEVEL_0, LEVEL_1, LEVEL_2} |
| 400 | 一层审批必须配置 L1 角色 | `approvalLevel=LEVEL_1` 且 `level1Roles=[]` |
| 400 | 二层审批必须配置 L1 和 L2 角色 | `approvalLevel=LEVEL_2` 且 L1/L2 任一为空 |
| 400 | 无需审批时 L1/L2 角色必须为空 | `approvalLevel=LEVEL_0` 且 `level1Roles/level2Roles` 非空 |
| 400 | 优先级超出范围 0-9999 | `priority` 越界 |
| 400 | 已存在相同维度的启用规则 | 唯一约束冲突 |
| 500 | 系统异常 | 数据库/服务异常 |

### 2.4 更新(★ lockToken 必传)

```http
POST /api/v1/deal-approval-rules/update
Content-Type: application/json
```

**请求体 DealApprovalRuleUpdateDTO**:

```json
{
  "id": 13,
  "lockToken": "uuid-v4-token-013",
  "approvalLevel": "LEVEL_2",
  "level1Roles": ["RISK_MANAGER"],
  "level2Roles": ["TREASURY_DIRECTOR"],
  "priority": 250,
  "description": "升级 L2 角色为财务总监",
  "version": 2
}
```

**响应 200**:

```json
{
  "code": 200,
  "data": { "id": 13, "ruleNumber": "DAR202607110002", "lockToken": "uuid-v4-token-013-new" }
}
```

**错误码**:

| code | message | 触发场景 |
|------|---------|----------|
| 404 | 规则不存在: DAR202607110002 | id 无效 |
| **409** | **规则已被他人修改,请刷新后重试** | **lockToken 不匹配(v1.1 风格)** |
| 400 | Active 规则维度重复 | 唯一约束冲突 |

### 2.5 删除 / 启用 / 停用

```http
POST /api/v1/deal-approval-rules/delete/{id}
POST /api/v1/deal-approval-rules/{id}/enable
POST /api/v1/deal-approval-rules/{id}/disable
```

- 软删除:`deleted='1'`,写 DELETE 审计日志
- 启用 / 停用:切换 `status`,写 ENABLE / DISABLE 审计日志
- 启用时若遇唯一约束冲突(同维度已有 Active 规则)→ `code=400` + 可读错误信息

**响应 200**: `{"code": 200, "message": "删除成功"}`

---

## 3. ★ match 端点(本特性关键)

### 3.1 设计目标

- 供 **dealing 服务内部调用**(`AcDealServiceImpl.submit/approve/reject` 触发 match)
- 同时供 **前端 test-match 抽屉** 调用(返回候选列表)
- 必须返回 **命中的规则 + 实际需要审批层级 + 角色列表**,不只是规则本身(对标 v1.1 match)

### 3.2 运行时 match 端点

```http
GET /api/v1/deal-approval-rules/match
  ?managementEntityId=10
  &counterpartyId=1001
  &instrumentId=301
  &dealerId=5
  &actionType=SUBMIT
```

**Query 参数**:

| 参数 | 必填 | 类型 | 说明 |
|------|------|------|------|
| `managementEntityId` | N | Long | 主体 ID;NULL=通配 |
| `counterpartyId` | N | Long | 对手方 ID;NULL=通配 |
| `instrumentId` | N | Long | 金融工具 ID;NULL=通配 |
| `dealerId` | N | Long | 交易员 ID;NULL=通配 |
| `actionType` | **Y** | String | CREATE/SUBMIT/APPROVE/REJECT/EXECUTE |

**匹配算法**(对标 v1.1 + PRD §5.4):

```
ORDER BY specificityScore DESC, priority DESC, created_at ASC, id ASC
WHERE action_type = ? AND status='Active' AND deleted='0'
  AND (start_date IS NULL OR start_date <= today)
  AND (end_date   IS NULL OR end_date   >= today)
  AND (management_entity_id = ? OR management_entity_id IS NULL)
  AND (counterparty_id      = ? OR counterparty_id      IS NULL)
  AND (instrument_id        = ? OR instrument_id        IS NULL)
  AND (dealer_id            = ? OR dealer_id            IS NULL)
LIMIT 1
```

**specificityScore 计算**(JDBC 层算):

| 维度精确 | 得分 |
|---------|------|
| `management_entity_id` 精确 | +300 |
| `counterparty_id` 精确 | +200 |
| `instrument_id` 精确 | +100 |
| `dealer_id` 精确 | +50 |
| `action_type` 精确 | +20 |
| 通配(NULL) | +0 |

**响应 200 — 命中 LEVEL_1**(常规情况):

```json
{
  "code": 200,
  "data": {
    "matched": true,
    "approvalLevel": "LEVEL_1",
    "level1Roles": ["RISK_MANAGER"],
    "level2Roles": [],
    "matchedRule": {
      "ruleNumber": "DAR202607110002",
      "priority": 100,
      "specificityScore": 320,
      "description": "FX 一层审批规则"
    },
    "candidates": [
      { "ruleNumber": "DAR202607110002", "specificityScore": 320, "priority": 100, "won": true },
      { "ruleNumber": "DAR202607110001", "specificityScore":  20, "priority":  50, "won": false }
    ],
    "matchedDimensions": ["counterparty", "instrument", "actionType"],
    "fallbackStrategy": null
  }
}
```

**响应 200 — 命中 LEVEL_0**(FX 直接 Approved,沿用 `d08181e` 修复):

```json
{
  "code": 200,
  "data": {
    "matched": true,
    "approvalLevel": "LEVEL_0",
    "level1Roles": [],
    "level2Roles": [],
    "matchedRule": {
      "ruleNumber": "DAR202607110005",
      "priority": 50,
      "specificityScore": 20
    },
    "candidates": [
      { "ruleNumber": "DAR202607110005", "specificityScore": 20, "priority": 50, "won": true }
    ],
    "matchedDimensions": ["actionType"],
    "fallbackStrategy": null
  }
}
```

**响应 200 — 未命中(降级到旧表)**:

```json
{
  "code": 200,
  "data": {
    "matched": false,
    "approvalLevel": null,
    "level1Roles": [],
    "level2Roles": [],
    "matchedRule": null,
    "candidates": [],
    "matchedDimensions": [],
    "fallbackStrategy": "DEFAULT_LEGACY_RULES"
  }
}
```

> 降级不可见:前端无需展示"用的是新规则还是旧规则";**match 端点异常(500/超时)不阻断交易**,使用 fallback + 警告日志。

**错误码**:

| code | message | 触发场景 |
|------|---------|----------|
| 400 | 操作类型必填 | `actionType` 为空 |
| 500 | match 异常,已降级 | 数据库/服务异常,内部 fallback |

### 3.3 test-match 端点(返回所有候选)

```http
GET /api/v1/deal-approval-rules/test-match
  ?managementEntityId=10
  &counterpartyId=1001
  &instrumentId=301
  &dealerId=5
  &actionType=SUBMIT
  &limit=50
```

**响应 200**:

```json
{
  "code": 200,
  "data": [
    {
      "ruleNumber": "DAR202607110002",
      "specificityScore": 320,
      "priority": 100,
      "approvalLevel": "LEVEL_1",
      "matchedDimensions": ["counterparty", "instrument", "actionType"],
      "won": true
    },
    { "ruleNumber": "DAR202607110001", "specificityScore": 20, "priority": 50, "...": "...", "won": false }
  ]
}
```

### 3.4 curl 示例

```bash
# 运行时匹配 — AC 交易风控场景
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/match?managementEntityId=10&counterpartyId=1001&instrumentId=301&actionType=SUBMIT" \
  -H "Authorization: Bearer xxx"

# test-match — 前端配置页"试匹配"按钮
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/test-match?managementEntityId=10&counterpartyId=1001&instrumentId=301&actionType=SUBMIT&limit=50"
```

---

## 4. 镜像 + 审计 + 引用 端点

### 4.1 镜像版本列表(★ 本特性新增)

```http
GET /api/v1/deal-approval-rules/{id}/images
```

**Query 参数**:
- `imageType` (String, 可选) — 筛选 `CREATE` / `UPDATE` / `DELETE` / `ENABLE` / `DISABLE`
- `pageNum` (Int, 默认 1)
- `pageSize` (Int, 默认 20)

**响应 200**:

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 45,
        "imageNumber": "IMG-DAR202607110001-V3",
        "version": 3,
        "imageType": "UPDATE",
        "operator": "compliance_officer",
        "operateAt": "2026-07-11T15:30:00Z",
        "changeSummary": "升级 L2 角色为财务总监"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 4.2 审计日志

```http
GET /api/v1/deal-approval-rules/{id}/audit-logs
```

**Query 参数**:
- `operation` (String, 可选) — `CREATE` / `UPDATE` / `DELETE` / `ENABLE` / `DISABLE`
- `pageNum` (Int, 默认 1)
- `pageSize` (Int, 默认 20)

**响应 200**:

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 78,
        "ruleId": 13,
        "operation": "UPDATE",
        "oldValue": { "approvalLevel": "LEVEL_1", "level2Roles": [] },
        "newValue": { "approvalLevel": "LEVEL_2", "level2Roles": ["TREASURY_DIRECTOR"] },
        "operator": "compliance_officer",
        "operatedAt": "2026-07-11T15:30:00Z",
        "remark": "升级 L2"
      }
    ],
    "total": 12,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 4.3 被引用数

```http
GET /api/v1/deal-approval-rules/{id}/reference-count
```

**响应 200**:

```json
{
  "code": 200,
  "data": {
    "ruleId": 13,
    "totalCount": 28,
    "byActionType": {
      "CREATE": 0,
      "SUBMIT": 28,
      "APPROVE": 0,
      "REJECT": 0,
      "EXECUTE": 0
    }
  }
}
```

> 引用源:`tms_actions_t` 中匹配该规则的 Action 数(Phase 5 实装时反查 SQL)。

---

## 5. 与 dealing 审批端点集成(非对外契约)

> 本节描述 **dealing 服务内部** 如何调用 match 端点,不属于对外契约,但 Phase 5 后端必读。

### 5.1 submit 端点联动(`POST /api/v1/{ac,at,fx}-deals/{id}/submit`)

```java
// 伪代码,基于 AcDealServiceImpl.submit()
public void submit(Deal deal) {
    DealApprovalRuleMatchResponseVO match = matchClient.match(
        deal.getManagementEntityId(),
        deal.getCounterpartyId(),
        deal.getInstrumentId(),
        deal.getDealerId(),
        "SUBMIT"
    );

    Action action = new Action();
    if (match.getApprovalLevel() == LEVEL_0) {
        // ★ 沿用 d08181e 修复:FX 直接 Approved
        action.setActionStatus("Approved");
        action.setApprovalStatus1("Approved");
        action.setApprovalStatus2("Approved");
    } else if (match.getApprovalLevel() == LEVEL_1) {
        action.setActionStatus("Submitted");
        action.setCurrentApproverLevel(1);
        action.setCurrentApproverRole(match.getLevel1Roles().get(0));
    } else if (match.getApprovalLevel() == LEVEL_2) {
        action.setActionStatus("Submitted");
        action.setCurrentApproverLevel(1);
        action.setCurrentApproverRole(match.getLevel1Roles().get(0));
        action.setLevel2Roles(match.getLevel2Roles());
    }
    // ... 落库,事件触发 Action 待办
}
```

### 5.2 approve / reject 联动

- **approve 端点**:`AcDealServiceImpl.approve()` 校验当前用户角色是否在 `currentApproverRole` 列表中
  - L1 阶段:用户在任一 L1 role 中 → 通过,Action 进入 L2 阶段(如适用)
  - L2 阶段:用户在任一 L2 role 中 → 通过,Action Approved
  - 跨阶段不允许跳批
- **reject 端点**:REJECT 流程单独配置规则(`actionType=REJECT`),沿用 approve 校验

### 5.3 跨服务调用 + 缓存

- basedata 8081 → dealing 8082 调 match:**HTTP + 5 分钟内存缓存**(Redisson)
- 缓存 key:`dar:match:{mgmt}:{ctp}:{instr}:{dealer}:{action}`
- 缓存失效:规则 enable / disable / update 时主动清除(基于 Redis Pub/Sub 或定时轮询)
- 软失败:match 5xx 或超时 → fallback 到旧 `tms_approval_rule_t`,**不阻断交易**

---

## 6. 字段与枚举

### 6.1 关键 VO 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `managementEntityName` / `counterpartyName` / `instrumentName` / `dealerName` | String | 基于 `EntityNameLookup` 跨模块补全 |
| `actionTypeLabel` | String | "提交审批" / "审批通过" / "驳回" 中文标签 |
| `level1Roles` / `level2Roles` | `List<String>` | JSONB 序列化(VO 中为数组) |
| `level1RolesDisplay` / `level2RolesDisplay` | `List<String>` | Phase 1 与 role 列表同值,P2 走 user-permission 解析 |
| `specificityScore` | Integer | **test-match 返回**,精确度得分 |
| `matchedDimensions` | `List<String>` | **test-match 返回**,命中维度清单 |
| `candidates` | `List<Object>` | **test-match 返回**,所有候选规则 |
| `won` | Boolean | **test-match 返回**,是否最终胜出 |

### 6.2 枚举值(沿用 GlobalConstants)

| 枚举 | 取值 |
|------|------|
| `ActionType` | CREATE / SUBMIT / APPROVE / REJECT / EXECUTE |
| `ApprovalLevel`(本特性新增) | LEVEL_0(无需) / LEVEL_1(一层) / LEVEL_2(二层) |
| `RuleStatus`(沿用 v1.1) | Active / Inactive |
| `ImageType`(本特性) | CREATE / UPDATE / DELETE / ENABLE / DISABLE |

---

## 7. 幂等性 + 并发 + 错误码

### 7.1 幂等性

- 写操作支持 `X-Idempotency-Key` 请求头(沿用 CLAUDE.md)
- 镜像表是 append-only,**无需幂等**

### 7.2 并发控制

- 沿用 v1.1 `lockToken`:更新必须传 `lockToken`,否则 400
- 旧 lockToken 更新 → `code=409 Conflict`
- `enable` 时若遇唯一约束冲突 → `code=400` + 可读错误信息

### 7.3 错误码汇总

| code | 场景 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误(操作类型必填 / 层级与角色不匹配 / 优先级越界 / 唯一约束冲突) |
| 404 | 规则不存在 |
| **409** | **lockToken 不匹配(并发冲突)** |
| 500 | 系统异常(match 失败已自动降级,不返回 500 给前端) |

---

## 8. 性能预算

| 端点 | 目标 P95 |
|------|---------|
| `/page` | <= 200ms(20 条/页,命中 `idx_dar_*`) |
| `/{id}` | <= 50ms |
| `/match` | **<= 50ms**(命中 `idx_dar_match_core`,包含 specificityScore 计算) |
| `/match` 缓存命中 | **<= 10ms** |
| `/test-match` | <= 100ms(Top 50 候选) |
| `/{id}/audit-logs` | <= 200ms |
| `/{id}/images` | <= 100ms |
| `/{id}/reference-count` | <= 100ms |

- 规则数量支持:10,000 条总规则 / 5,000 条 Active
- match 调用:`basedata 8081 → dealing 8082`,5min Redisson 缓存

---

## 9. 与 CLAUDE.md 一致性自检

- ✅ 写操作 `@Transactional(rollbackFor=Exception.class)`(Phase 5 落地)
- ✅ 状态用 GlobalConstants 枚举(不新建魔术字符串)
- ✅ 主表 + 镜像表 + 审计表三表分立,审计字段齐全
- ✅ 金额精度按表决定(本特性无金额字段)
- ✅ 不跨服务循环依赖(dealing 调 basedata HTTP,反向无)
- ✅ 跨模块数据查找走 `EntityNameLookup`(详情接口必补 *Name 字段)
- ✅ 镜像表 image_type 用 VARCHAR(20) + CHECK 约束
- ✅ partial unique index + NULLS NOT DISTINCT(与 v1.1 一致)
- ✅ 12 端点遵循 Open-TMS REST 规范(`/api/v1/{resource}` + POST 写)

---

## 10. curl 速查

```bash
# 1. 分页查询
curl -X POST http://localhost:8081/api/v1/deal-approval-rules/page \
  -H "Content-Type: application/json" \
  -d '{"actionType":"SUBMIT","status":"Active","pageNum":1,"pageSize":20}'

# 2. 详情(支持 ruleNumber)
curl -X GET http://localhost:8081/api/v1/deal-approval-rules/DAR202607110001

# 3. 新增 — LEVEL_1 单角色
curl -X POST http://localhost:8081/api/v1/deal-approval-rules \
  -H "Content-Type: application/json" \
  -d '{
    "actionType":"SUBMIT","approvalLevel":"LEVEL_1",
    "level1Roles":["RISK_MANAGER"],"level2Roles":[],
    "priority":100,"status":"Active",
    "description":"FX 一层审批"
  }'

# 4. 更新 — 必须带 lockToken
curl -X POST http://localhost:8081/api/v1/deal-approval-rules/update \
  -H "Content-Type: application/json" \
  -d '{
    "id":13,"lockToken":"uuid-v4-token-013",
    "approvalLevel":"LEVEL_2","level2Roles":["TREASURY_DIRECTOR"],
    "version":2
  }'

# 5. 删除
curl -X POST http://localhost:8081/api/v1/deal-approval-rules/delete/13

# 6. 启用 / 停用
curl -X POST http://localhost:8081/api/v1/deal-approval-rules/13/enable
curl -X POST http://localhost:8081/api/v1/deal-approval-rules/13/disable

# 7. ★ 运行时 match(本特性关键)
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/match?managementEntityId=10&counterpartyId=1001&instrumentId=301&actionType=SUBMIT"

# 8. test-match
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/test-match?actionType=SUBMIT&limit=50"

# 9. 审计日志
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/13/audit-logs?pageNum=1&pageSize=20"

# 10. 被引用数
curl -X GET http://localhost:8081/api/v1/deal-approval-rules/13/reference-count

# 11. 镜像列表(本特性新增)
curl -X GET "http://localhost:8081/api/v1/deal-approval-rules/13/images?imageType=UPDATE&pageNum=1&pageSize=20"
```