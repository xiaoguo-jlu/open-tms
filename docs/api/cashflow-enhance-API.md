# 现金流增强 + Audit History — API 契约

> 特性: 增强交易现金流管理 + AC/AT/FX 审计历史
> 范围: Phase 4 API 设计
> 依赖: PRD `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md` + DDL `db/schema/29-cashflow-enhance.sql`

---

## 1. 影响现有端点(透明下沉)

下列端点的**响应体**新增 2 字段,**请求体不变**。前端无需修改调用代码(OpenAPI 扫描器自动检测)。

| 端点 | 方法 | 影响 |
|------|------|------|
| `/api/v1/dealing/ac-deals` | POST 创建 | 响应 `CashflowVO` 加 `bankAccountId` / `counterpartyBankAccountId` |
| `/api/v1/dealing/ac-deals/{id}` | GET 详情 | 同上 |
| `/api/v1/dealing/at-deals` | POST | 同上 |
| `/api/v1/dealing/fx-deals` | POST | 同上(含 BUY_AMOUNT/SELL_AMOUNT 对应的 2 条 cashflow) |
| `/api/v1/dealing/fx-deals/{id}/rate-fix` | POST | 新增 1 条 RATE_FIX 镜像的 cashflow,响应加新字段 |

**注意**:服务**内部自动**调默认银行账户规则(`/api/v1/default-bank-account-rules/match`)填充,无需前端传。

---

## 2. 新增端点(2 个)

### 2.1 版本列表

```http
GET /api/v1/dealing/deals/{dealNumber}/versions
```

**路径参数**:
- `dealNumber` (String, 必填) — 交易编号

**Query 参数**:
- `imageType` (String, 可选) — 筛选 `CREATE` / `UPDATE` / `DELETE` / `RATE_FIX` / `STATUS_CHANGE`
- `pageNum` (Int, 默认 1)
- `pageSize` (Int, 默认 20)

**响应 200**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 123,
        "imageNumber": "IMG-FX20260710-0023-V3",
        "version": 3,
        "imageType": "UPDATE",
        "operator": "张三",
        "operateAt": "2026-07-10T15:30:00Z",
        "changeSummary": "修改买入币种 USD→EUR"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

**错误码**:
- 404 — 交易不存在
- 500 — 后端异常

### 2.2 版本详情(3 段合并)

```http
GET /api/v1/dealing/deals/{dealNumber}/versions/{version}
```

**路径参数**:
- `dealNumber` (String)
- `version` (Int) — 版本号,对应 `tms_deals_t.version`

**响应 200**:
```json
{
  "code": 200,
  "data": {
    "deal": {
      "dealNumber": "FX20260710-0023",
      "version": 3,
      "status": "Approved",
      "tradeDate": "2026-07-10",
      "valueDate": "2026-07-12",
      "operator": "张三",
      "operateAt": "2026-07-10T15:30:00Z",
      "imageType": "UPDATE"
    },
    "dealMap": [
      {
        "dealmapNumber": "DM-FX-0023-1",
        "fieldKey": "FX_BUY_AMOUNT",
        "value": "200000.00",
        "currency": "EUR",
        "version": 3
      },
      {
        "dealmapNumber": "DM-FX-0023-2",
        "fieldKey": "FX_SELL_AMOUNT",
        "value": "218000.00",
        "currency": "USD",
        "version": 3
      }
    ],
    "cashflows": [
      {
        "cflowNumber": "CF-FX-0023-1",
        "version": 3,
        "direction": "Inflow",
        "amount": "200000.000000000000000000",
        "currency": "EUR",
        "bankAccountId": 5,
        "counterpartyBankAccountId": 12,
        "imageType": "UPDATE",
        "operator": "张三",
        "operateAt": "2026-07-10T15:30:00Z"
      }
    ]
  }
}
```

**3 段式 LEFT JOIN 语义**:
- `deal` 一定有(主表镜像)
- `dealMap` 列表可能为空(早期版本无 dealmap)
- `cashflows` 可能为空(AC/AT 早期镜像未启,backfill v1.1)

**错误码**:
- 404 — 版本不存在
- 500

---

## 3. 内部服务调用(非对外契约)

CashflowService 在 create/update/delete/changeStatus 时:

```java
// 1) 调基于数据默认银行账户规则 match
DefaultBankAccountRuleMatchRequest req = new DefaultBankAccountRuleMatchRequest();
req.setManagementEntityId(cf.getBusinessUnitId());
req.setCounterpartyId(cf.getCounterpartyId());
req.setCurrency(cf.getCurrency());
req.setDirection(cf.getDirection());
DefaultBankAccountRuleVO rule = basedataClient.match(req); // 内部 HTTP,5min 缓存

// 2) 填充
cf.setBankAccountId(rule.getBankAccountId());
cf.setCounterpartyBankAccountId(rule.getCounterpartyAccountId());

// 3) 保存 cashflow
cashflowMapper.insert(cf);

// 4) 写镜像(@Transactional 保证一起成功/回滚)
CashflowImage image = new CashflowImage();
// 复制 cf 全部字段 + imageType='CREATE'
cashflowImageMapper.insert(image);
```

---

## 4. 前端契约(详情页新增)

### 4.1 详情页 audit history 按钮

- 路径:`/dealing/{ac,at,fx}-deal/detail/:dealNumber?mode=readonly`
- 新增按钮(在现有 4 模式按钮旁):**`审计历史`**
- 点击 → 打开 `<AuditHistoryDialog>` → 调 `GET /deals/{dealNumber}/versions`
- 选中某版本 → 跳:`/dealing/{type}/audit-history?dealNumber=X&version=Y`

### 4.2 历史详情页

- 新页面:`/dealing/{type}/audit-history`(新 Vue 组件 `AuditHistoryView.vue`)
- 调 `GET /deals/{dealNumber}/versions/{version}`
- 3 段式展示(可折叠)
- 顶部"返回"按钮回到原详情页

---

## 5. 幂等性 + 锁

- 沿用 v1.1 `lockToken`:CashflowService.update 必须传 `lockToken`,否则 400
- 镜像表是 append-only,**无需锁**(只读)
- 版本号与 `tms_deals_t.version` 同步递增:`@Version` 触发 `version+1` 时同步

---

## 6. 性能预算

- `GET /versions`: p95 < 100ms(单 SQL 命中 `idx_cf_image_deal_number`)
- `GET /versions/{version}`: p95 < 200ms(3 段 SQL LEFT JOIN)
- 跨服务调用 match 缓存命中时 p99 < 10ms,未命中时 < 80ms

---

## 7. 与 CLAUDE.md 一致性

- ✅ 写操作 `@Transactional(rollbackFor=Exception.class)`
- ✅ 状态用 GlobalConstants 枚举(不新建魔术字符串)
- ✅ 镜像表 image_type 用 VARCHAR(20) + CHECK 约束,状态字符串白名单
- ✅ 审计字段齐全(created_by/created_at/updated_by/updated_at/version/deleted)
- ✅ 金额精度 DECIMAL(38,18) 与现有 cashflow 一致
- ✅ 不跨服务循环依赖(dealing 调 basedata HTTP,反向无)
