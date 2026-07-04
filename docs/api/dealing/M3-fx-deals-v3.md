# M3-外汇交易 API 文档 v3.2

**版本**: v3.2
**日期**: 2026-07-04
**基于**: PRD `docs/prd/M3/M3-外汇交易PRD.md` v3.2
**基线**: DDL `db/schema/24-fx-deal-v3.sql`

---

## 一、概述

本文档描述 Open-TMS 外汇交易(FX)模块的 REST API 端点。

### 设计要点

- **统一后端计算**:`POST /calculate` 提供唯一可信源,前端无计算逻辑
- **日期字段在公共表**:`tms_deals_t` 持有 trade_date / value_date / maturity_date
- **DealMap 单字段多行**:`tms_deal_map_t.amount_or_rate` + `dealmap_type` 区分
- **1 DealMap → 1 CF**:`tms_cashflow_t.dealmap_number` 强引用
- **共享主键**:`tms_fx_deals_t.id = tms_deals_t.id`
- **Action 4 种**:DEAL / UPDATE / DELETE / RATE_FIX

### 通用约定

- **Base URL**: `http://localhost:8082`
- **Content-Type**: `application/json`
- **认证**: Bearer Token(`Authorization: Bearer {token}`)
- **响应格式**: `{ "code": 200, "message": "success", "data": {...}, "timestamp": ... }`
- **时间格式**: ISO 8601 (`2026-07-04T15:30:00`)

### 错误码

| HTTP | code | 含义 | 触发场景 |
|------|------|------|---------|
| 200 | 200 | 成功 | 正常 |
| 400 | 400 | 请求参数错误 | 必填字段缺失、字段格式错误 |
| 400 | 40001 | INPUT_INSUFFICIENT | calculate 接口用户填的字段不足 2 个 |
| 400 | 40002 | VALUE_INCONSISTENT | calculate 接口字段互相矛盾 |
| 401 | 401 | 未授权 | Token 失效 |
| 404 | 404 | 资源不存在 | dealNumber 不存在 |
| 422 | 42201 | DATE_INVALID | calculate 接口交易日 > 交割日 |
| 500 | 500 | 系统错误 | 内部异常 |

---

## 二、API 端点清单

| # | 端点 | 方法 | 说明 |
|---|------|------|------|
| 1 | `/api/v1/dealing/fx-deals/calculate` | POST | 后端统一计算接口 |
| 2 | `/api/v1/dealing/fx-deals` | POST | DEAL 创建 |
| 3 | `/api/v1/dealing/fx-deals/page` | GET | 列表分页 |
| 4 | `/api/v1/dealing/fx-deals/{dealNumber}` | GET | 详情(按 dealNumber) |
| 5 | `/api/v1/dealing/fx-deals/update` | POST | UPDATE 修改 |
| 6 | `/api/v1/dealing/fx-deals/delete/{id}` | POST | DELETE 软删 |
| 7 | `/api/v1/dealing/fx-deals/{id}/rate-fix` | POST | RATE_FIX(NDF) |

---

## 三、详细端点

### 3.1 `POST /calculate` — 后端统一计算接口

**用途**:前端录入联动字段后,自动调用此接口获取计算结果。**后端是唯一计算源**。

**Request**:
```json
{
  "sellAmount":   100000.00,
  "buyAmount":    null,
  "exchangeRate": 7.2000,
  "marketRate":   7.1900,
  "spreadBp":     null,
  "tradeDate":    "2026-07-04",
  "valueDate":    "2026-10-04"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sellAmount | decimal | 否 | 卖出金额 |
| buyAmount | decimal | 否 | 买入金额 |
| exchangeRate | decimal | 否 | 成交汇率 |
| marketRate | decimal | 否 | 市场汇率 |
| spreadBp | decimal | 否 | 点差(基点) |
| tradeDate | date | 否 | 交易日(YYYY-MM-DD) |
| valueDate | date | 否 | 交割日(YYYY-MM-DD) |

**约束**:用户至少填 2 个金额/汇率字段,否则 40001。

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "sellAmount":   100000.00,
    "buyAmount":    720000.00,
    "exchangeRate": 7.2000,
    "marketRate":   7.1900,
    "spreadBp":     100.00,
    "tradeDate":    "2026-07-04",
    "valueDate":    "2026-10-04",
    "termDays":     92,
    "maturityDate": "2026-10-04"
  }
}
```

**计算规则(单一可信源)**:
- 联动 1:`buyAmount = sellAmount × exchangeRate`
- 联动 2:`exchangeRate = marketRate + spreadBp / 10000`
- 联动 3:`termDays = valueDate - tradeDate`(天数)
- 联动 4:`maturityDate = valueDate`

**Curl 示例**:
```bash
curl -X POST http://localhost:8082/api/v1/dealing/fx-deals/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "sellAmount": 100000.00,
    "exchangeRate": 7.2000,
    "marketRate": 7.1900,
    "tradeDate": "2026-07-04",
    "valueDate": "2026-10-04"
  }'
```

---

### 3.2 `POST /` — DEAL 创建

**用途**:创建一笔 FX 交易(SPOT/FWD/NDF)。

**Request**:
```json
{
  "managementEntityId": 1,
  "counterpartyId": 5001,
  "traderId": 1,
  "instrumentId": 401,
  "currencyPairId": 1,
  "sellCurrency": "USD",
  "sellAmount": 100000.00,
  "buyCurrency": "CNY",
  "buyAmount": 720000.00,
  "exchangeRate": 7.2000,
  "marketRate": 7.1900,
  "spreadBp": 100.00,
  "tradeDate": "2026-07-04",
  "valueDate": "2026-10-04",
  "fixingSource": null,
  "description": "锁定付汇成本",
  "remark": null
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| managementEntityId | long | Y | FK → tms_management_entity_t.id |
| counterpartyId | long | Y | FK → tms_counterparty_t.id |
| traderId | long | Y | FK → tms_trader_t.id |
| instrumentId | long | Y | FK → tms_instrument_t.id(产品类型内嵌) |
| currencyPairId | long | Y | FK → tms_currency_pair_t.id |
| sellCurrency | string(10) | Y | 卖出币种 |
| sellAmount | decimal(38,18) | Y | 卖出金额 |
| buyCurrency | string(10) | Y | 买入币种(≠ sellCurrency) |
| buyAmount | decimal(38,18) | Y | 买入金额 |
| exchangeRate | decimal(18,8) | Y | 成交汇率 |
| marketRate | decimal(18,8) | Y | 市场汇率 |
| spreadBp | decimal(10,4) | Y | 点差(基点) |
| tradeDate | date | Y | 交易日 |
| valueDate | date | Y | 交割日 |
| fixingSource | string(50) | N | NDF 必填,其他 null |
| description | string(500) | N | 描述 |
| remark | string(500) | N | 备注 |

**Side effects(在事务内)**:
1. INSERT `tms_deals_t`(`deal_type='FX'`, 含 trade_date/value_date/maturity_date)
2. INSERT `tms_fx_deals_t`(`id = tms_deals_t.id`, 共享主键)
3. INSERT `tms_actions_t`(`action_type='DEAL'`)
4. INSERT 3 行 `tms_deal_map_t`(`FX_BUY_AMOUNT` / `FX_SELL_AMOUNT` / `FX_RATE`)
5. INSERT 2 行 `tms_cashflow_t`(SPOT/FWD),`dealmap_number` 关联 BUY/SELL DealMap
6. NDF 暂不生成 CF(等 RATE_FIX)

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "FX202607040001",
    "status": "New",
    "dealMapCount": 3,
    "cashflowCount": 2
  }
}
```

---

### 3.3 `GET /page` — 列表分页

**Query 参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码(默认 1) |
| pageSize | int | 否 | 每页大小(默认 10) |
| managementEntityId | long | 否 | 按管理主体过滤 |
| counterpartyId | long | 否 | 按对手方过滤 |
| productType | string | 否 | SPOT / FWD / NDF(JOIN 推算) |
| status | string | 否 | New / Active / Settled |
| startDate | date | 否 | trade_date 起始 |
| endDate | date | 否 | trade_date 截止 |

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "dealNumber": "FX202607040001",
        "sellCurrency": "USD",
        "sellAmount": 100000.00,
        "buyCurrency": "CNY",
        "buyAmount": 720000.00,
        "exchangeRate": 7.2000,
        "status": "New",
        "tradeDate": "2026-07-04",
        "valueDate": "2026-10-04"
      }
    ],
    "total": 12,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

---

### 3.4 `GET /{dealNumber}` — 详情

**Path 参数**:`dealNumber`(string)

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "id": 1,
    "dealNumber": "FX202607040001",
    "managementEntityId": 1,
    "counterpartyId": 5001,
    "traderId": 1,
    "instrumentId": 401,
    "currencyPairId": 1,
    "sellCurrency": "USD",
    "sellAmount": 100000.00,
    "buyCurrency": "CNY",
    "buyAmount": 720000.00,
    "exchangeRate": 7.2000,
    "marketRate": 7.1900,
    "spreadBp": 100.00,
    "tradeDate": "2026-07-04",
    "valueDate": "2026-10-04",
    "maturityDate": "2026-10-04",
    "fixingSource": null,
    "fixingRate": null,
    "settlementAmount": null,
    "description": "锁定付汇成本",
    "status": "New",
    "dealMapList": [
      {"dealmapNumber": "DMP202607040001", "dealmapType": "FX_SELL_AMOUNT", "amountOrRate": 100000.00, "createdAt": "..."},
      {"dealmapNumber": "DMP202607040002", "dealmapType": "FX_BUY_AMOUNT",  "amountOrRate": 720000.00, "createdAt": "..."},
      {"dealmapNumber": "DMP202607040003", "dealmapType": "FX_RATE",        "amountOrRate": 7.2000,    "createdAt": "..."}
    ],
    "cashflowList": [
      {"cflowNumber": "CF202607040001", "dealmapNumber": "DMP202607040001", "amount": 100000.00, "currency": "USD"},
      {"cflowNumber": "CF202607040002", "dealmapNumber": "DMP202607040002", "amount": 720000.00, "currency": "CNY"}
    ],
    "actionList": [
      {"actionNumber": "ACT202607040001", "actionType": "DEAL", "operator": "李四", "approveStatus": "Pending"}
    ]
  }
}
```

---

### 3.5 `POST /update` — UPDATE 修改

**Request**(与 DEAL 类似,加 dealNumber):
```json
{
  "dealNumber": "FX202607040001",
  "sellAmount": 200000.00,
  "buyAmount": 1440000.00,
  "exchangeRate": 7.2000,
  "description": "更新金额"
}
```

**Side effects**:
- UPDATE `tms_deals_t` + `tms_fx_deals_t`
- INSERT 1 行 `tms_actions_t`(`action_type='UPDATE'`)

---

### 3.6 `POST /delete/{id}` — DELETE 软删

**Path 参数**:`id`(long)

**Side effects**:
- UPDATE `deleted='1'`
- INSERT 1 行 `tms_actions_t`(`action_type='DELETE'`)

---

### 3.7 `POST /{id}/rate-fix` — RATE_FIX(NDF 专用)

**用途**:NDF 到期时执行,固定 fixing 汇率,生成差额 CF。

**Path 参数**:`id`(long)

**Request**:
```json
{
  "fixingRate": 7.1500,
  "operator": "李四"
}
```

**Side effects(在事务内)**:
1. INSERT 1 行 `tms_deal_map_t`(`dealmap_type='FX_FIX'`, `amount_or_rate=7.1500`)
2. 计算 `settlement_amount = notional × (fixingRate - exchangeRate)`
3. INSERT 1 行 `tms_cashflow_t`(`dealmap_number` = 上一步 dealmap, `amount=settlementAmount`)
4. UPDATE `tms_fx_deals_t`(`fixing_rate`, `settlement_amount`)
5. INSERT 1 行 `tms_actions_t`(`action_type='RATE_FIX'`)

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "FX202607040001",
    "status": "Active",
    "settlementAmount": -3000.00,
    "dealmapNumber": "DMP202607040004"
  }
}
```

---

## 四、数据类型参考

| 字段类型 | DB 类型 | Java 类型 | 精度 |
|---------|---------|----------|------|
| 金额 | DECIMAL(38,18) | BigDecimal | 18 位小数 |
| 汇率 | DECIMAL(18,8) | BigDecimal | 8 位小数 |
| 点差 | DECIMAL(10,4) | BigDecimal | 4 位小数 |
| 期限(天数) | INT | Integer | 整数 |
| 日期 | DATE | LocalDate | YYYY-MM-DD |
| 编号 | VARCHAR(50) | String | 长度 50 |

---

## 五、关联表

| 表 | 说明 | 与 FX 关系 |
|----|------|-----------|
| tms_deals_t | 公共主表 | FX 持有 1 行(deal_type='FX') |
| tms_fx_deals_t | FX 特性表 | 共享主键(id = tms_deals_t.id) |
| tms_actions_t | Action 历史 | 每状态变化 1 行 |
| tms_deal_map_t | DealMap | 1 笔 FX 3-4 行(BUY/SELL/RATE[/FIX]) |
| tms_cashflow_t | 现金流 | SPOT/FWD 2 条,NDF RATE_FIX 后 1 条 |
| tms_management_entity_t | 管理主体 | FK id(强类型) |
| tms_counterparty_t | 对手方 | FK id |
| tms_trader_t | 交易员 | FK id |
| tms_instrument_t | 金融工具 | FK id(产品类型内嵌) |
| tms_currency_pair_t | 币种对 | FK id(两币种必须从该币种对取) |

---

## 六、ChangeLog

| 版本 | 日期 | 变更 |
|------|------|------|
| v3.2 | 2026-07-04 | 初版:后端 calculate + 共享主键 + DealMap 单字段多行 + 1→1 CF |
| v3.0 | 2026-07-04 | 简化重构:无审批流 + 无 MTM + 4 Action |
| v1.0 | 2026-04-11 | 已废弃(独立 fx 模块) |

---

*API 文档 - 2026-07-04 v3.2*
