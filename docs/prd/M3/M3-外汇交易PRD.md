# Open-TMS M3-外汇交易 PRD

**版本**: v3.2(架构再调整版)
**角色**: 产品经理 (PM)
**日期**: 2026-07-04
**基于**:
- v3.1(2026-07-04,细化调整版)
- 2026-07-04 用户产品评审反馈(1 项录入架构 + 3 项数据库模型 + 2 项 DealMap 形态)
- `M1-Deal交易PRD-v5.md`(统一交易主表 v5.0)
- 2026-07-03 模块整合(`fx` 模块并入 dealing,端口 8082)
- 2026-07-04 命名统一(管理主体/业务主体 → **管理主体**)
**状态**: v3.2 草稿 - 后端 calculate 接口 + 日期字段移到公共表 + DealMap Amount_or_rate 单字段 + 多行独立 DealMap

---

## 〇、修订记录

### v3.2(2026-07-04) - 本次架构再调整(用户产品评审反馈)

| 修订项 | 修订内容 | 原因 |
|--------|---------|------|
| **后端 calculate 接口** | 金额/汇率/term 的计算全部由后端 `POST /api/v1/dealing/fx-deals/calculate` 封装,前端**只**录入,联动通过调用 calculate 自动填充 | 单一计算源,避免前后端逻辑不一致 |
| **日期字段移到公共表** | `交易日/交割日/到期日/期限` 统一存到 `tms_deals_t`,**FX 表不再存这些日期字段** | 跨 deal_type 共享日期语义,避免重复 |
| **DealMap 单字段化** | 原 3 字段 `buy_amount/sell_amount/rate` → **1 字段** `amount_or_rate` + `dealmap_type` 区分 | 简化 DealMap 模型,支持任意类型快照 |
| **DealMap 多行独立** | buy amount / sell amount / rate / Fix 各自独立成 1 行 DealMap,共 3-4 行 | 每行有独立生命周期,支持 1:1 映射 Cashflow |
| **1 DealMap → 1 Cashflow** | 一笔 DealMap 最多生成 1 条 Cashflow,CF 上存触发它的 dealmap_number | 简化对账,DealMap 与 Cashflow 严格 1:1(部分 DealMap 如 RATE 可能不生成 CF) |
| **移除 chk_dm_fx_amounts** | 删除 DealMap 3 字段非空约束(现在只 1 字段,约束不适用) | 字段模型已变,约束失效 |

### v3.1(2026-07-04) - 细化调整版(用户产品评审反馈)

| 修订项 | 修订内容 | 原因 |
|--------|---------|------|
| **新增"交易日"** | 新增 `trade_date` 字段(代表交易达成的日子) | 区分"交易日"与"交割日",清晰业务流 |
| **管理主体 FK 改为 id** | `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT NOT NULL`(关联 `tms_management_entity_t.id`) | 强类型 FK 约束,避免字符 code 误填 |
| **term 自动计算** | `term = 交割日 - 交易日`,系统自动算出 | 减少用户输入,避免不一致 |
| **到期日 = 交割日(不可改)** | 到期日默认 = 交割日,前端不暴露,后端强制相等 | FWD/NDF 的"到期"就是"交割",无需分开 |
| **交割日存为 value_date** | DB 字段名统一为 `value_date`,前端显示"交割日" | 与 AC/AT 的 `tms_deals_t.value_date` 对齐 |
| **共享主键** | `tms_fx_deals_t` 取消独立 `id PRIMARY KEY`,直接共用 `tms_deals_t.id` | FX 个性化字段无独立生命周期 |
| **移除 chk 约束** | 移除 `chk_fx_settlement` 和 `chk_fx_ndf_fields` | instrument 可能变化(SPOT→FWD),约束太严 |

### v3.0(2026-07-04) - 简化重构版(用户评审反馈)

| 修订项 | 修订内容 | 原因 |
|--------|---------|------|
| **录入联动 1** | 卖出金额 / 买入金额 / 成交汇率 三者联动,任意两者 → 第三者自动计算 | 用户录入体验 |
| **录入联动 2** | 成交汇率 / 市场汇率 / 点差 三者联动,任意两者 → 第三者自动计算 | 用户录入体验 |
| **字段重命名** | FX 起息日 `value_date` → 统一叫 `交割日 settlement_date` | 与业界术语对齐(SPOT = T+0/1/2 settlement) |
| **DealMap 极简化** | FX DealMap 字段从 9+ 个 → 只保留 `buy_amount / sell_amount / rate` | 不需要复杂 event_type,Action 记录已足够 |
| **Action 简化** | FX Action 类型从 7 个(AC/AT)→ 4 个:**DEAL / UPDATE / DELETE / RATE_FIX** | FX 无审批流,无 SUBMIT/APPROVE/REJECT/EXECUTE |
| **货币对约束** | 交易货币双方必须从 `tms_currency_pair_t` 配置中选取,不再支持任意币种对 | 业务约束,符合 FX 交易实务 |
| **产品类型隐式** | 录入界面无 `productType` 字段,产品类型由 `instrument` 自动决定 | 减少用户输入,产品类型已是 instrument 的一部分 |
| **现金流生成时机** | SPOT/FWD:创建即生成 Cashflow;NDF:DEAL 创建时**不**生成,RATE_FIX 后才生成 | 区分 FX 业务流 |
| **不做项明确** | 锁汇额度 / 询价 / MTM 实时计算 / 完整 FX(SWAP/OPTION) 全部 P2+ | 控制 M3 范围,聚焦核心 |

### v2.0(2026-07-04) - 字段补全 + 命名对齐 + 架构升级
- 新增 4 个通用字段(管理主体/对手方/交易员/金融工具)
- 命名统一为"管理主体"
- FX 并入 dealing 模块
- 9 态状态机 / 9 种 DealMap 事件类型 / 锁汇额度

### v1.0(2026-04-11) - 已废弃
- 独立 `fx` 模块,无通用字段
**基于**:
- v1.0(2026-04-11,已废弃)
- `M3-金融工具模块UX原型.md` + `docs/原型/M3/fx-trading-prototype.html`
- `M1-Deal交易PRD-v5.md`(统一交易主表 v5.0)
- `M1-AT交易PRD-v2.0`(DealMap v2.0 事件溯源架构)
- 2026-07-03 模块整合(`fx` 模块并入 dealing,端口 8082)
- 2026-07-04 命名统一(管理主体/业务主体 → **管理主体**)
**状态**: v2.0 草稿 - 字段补全 + 命名对齐 + 架构升级

---

## 〇、修订记录

### v2.0(2026-07-04) - 本次重大重构

| 修订项 | 修订内容 | 原因 |
|--------|---------|------|
| **新增 4 个通用交易字段** | 新增 `managementEntity` / `counterpartyId` / `traderId` / `instrumentId` | 与 AC/AT 统一使用 `tms_deals_t` 公共主表 |
| **统一命名** | 管理主体/业务主体 → **管理主体** | 2026-07-04 项目全局重命名 |
| **架构升级** | FX 交易从独立 `fx` 模块 → 并入 `dealing` 模块,共享 DealMap v2.0 事件溯源 | 2026-07-03 模块整合 |
| **API 路径调整** | `/api/fx/spot/create` → `/api/v1/dealing/fx-deals` | 统一 CXF + Spring MVC 风格 |
| **状态机对齐** | CREATED/EXECUTED/SETTLED → New/Pending/Approved/Active/Settled/Matured/NDFSettled/Rejected/Canceled | 与 AC/AT 状态机一致 |
| **DealMap 事件类型** | 引入 `FxSpotBooking` / `FxFwdBooking` / `FxNdfBooking` / `FxMtmIncrement` / `FxMtmDecrement` / `FxFwdMaturity` / `FxNdfSettle` | 支持 IFRS 9 衍生品会计 + MTM 估值 |
| **SPOT/FWD/NDF 字段对齐** | 三种产品统一字段集 + 各自特有字段 | 简化录入体验 |
| **SWAP/OPTION 标记 P1** | 外汇掉期、外汇期权标记为 P1,本版本仅设计,不实现 | 范围聚焦 |

### v1.0(2026-04-11) - 已废弃
- 原 `fx` 模块独立运行,API 路径 `/api/fx/*`
- 字段仅有交易要素(币种/金额/汇率/期限),**无** "谁/在哪主体/对手是谁/用的什么产品" 等通用字段
- 状态机仅 3-4 态,无 DealMap 事件

---

## 一、模块概述

### 1.1 模块名称

**fx** - 外汇交易(已并入 `dealing` 模块,统一端口 8082)

### 1.2 功能定位

支持**即期(SPOT)、远期(FWD)、NDF(无本金交割远期)** 三类核心外汇产品的全生命周期管理:
- 录入 → 审批 → 签约(MTM 占用锁汇额度) → 每日盯市 → 到期交割 → 冲销
- 完整的 DealMap 事件时间线,支持 IFRS 9 衍生品会计
- 与 AC/AT 共享审批流、Action 操作审计、DealMap 事件模型

### 1.3 用户角色

| 角色 | 典型操作 |
|------|---------|
| **外汇交易员** | 录入 FX 交易,询价,报价,签约 |
| **资金经理** | 审批 FX 交易,管理锁汇额度 |
| **风险经理** | 监控 MTM 估值,处理预警 |
| **结算员** | 处理到期交割(SPOT/FWD)和差额结算(NDF) |

### 1.4 与其他模块的关系

```
fx 交易
  │
  ├── 共享 dealing 基础设施
  │     ├── tms_deals_t (公共主表,所有 dealType=FX)
  │     ├── tms_actions_t (审批操作)
  │     ├── tms_deal_map_t (业务事件,event_type=Fx*)
  │     └── tms_cashflow_t (资金流,到期生成)
  │
  ├── 依赖 basedata
  │     ├── management-entity (资金管理主体,原"业务主体")
  │     ├── counterparty (交易对手,即 FX 银行/做市商)
  │     ├── trader (交易员)
  │     ├── instrument (金融工具,FX-SPOT/FWD/NDF)
  │     ├── bank-account (结算账户,签约/到期时引用)
  │     └── currency + currency-pair (币种/币种对,联动汇率)
  │
  └── 输出至下游
        ├── valuation (估值,每日 MTM)
        ├── var (VaR,FX 敞口)
        ├── exposure (敞口,FX 风险因子)
        └── accounting (M1.3 阶段,会计分录)
```

---

## 二、业界对标

| 特性 | FIS Quantum | Murex MX.3 | Open-TMS v3.0 |
|------|-------------|------------|---------------|
| 即期/远期/NDF | 支持 | 支持 | ✅ P0 |
| 录入联动(金额↔汇率↔点差) | ✅ | ✅ | ✅ P0 |
| 币种对约束(从配置选) | ✅ | ✅ | ✅ P0 |
| 无审批流(直入直出生成 CF) | ❌(有审批) | ❌(有审批) | ✅ P0(FX 特点) |
| MTM 盯市 | 每日自动 | 实时 | 📋 P2(本期不做) |
| 锁汇额度占用 | 实时 + 多级预警 | 实时 | 📋 P2(本期不做) |
| 衍生品会计 | IFRS 9 全自动 | 灵活规则 | 📋 P1 (M1.3 接入) |
| NDF 差额结算 | 支持 + fixing 自动取 | 支持 | ✅ P0 |
| 询价 | 支持 | 支持 | 📋 P2(本期不做) |
| 外汇期权 | 完整支持 | 完整支持 | 📋 P2(本期不做) |
| 货币掉期 | 完整支持 | 完整支持 | 📋 P2(本期不做) |

> **v3.0 与 v2.0 的本质差异**:v2.0 试图复刻"完整 FX 交易系统",v3.0 聚焦"FX 录入 + 现金流分离"核心闭环,其他功能(SWAP/OPTION/MTM/锁汇/询价)全部 P2+。**KISS 原则**。

---

## 三、功能清单

### 3.1 即期外汇(SPOT) - P0

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 成交 | 录入即期外汇交易,**创建即生成 2 条 Cashflow** | **P0** |
| 编辑/撤销 | UPDATE/DELETE Action(已 Active 后不可删) | **P0** |
| 冲销 | 录入反向交易冲销 | P2 |

### 3.2 远期外汇(FWD) - P0

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 成交 | 录入远期外汇交易,**创建即生成 2 条 Cashflow** | **P0** |
| 编辑/撤销 | UPDATE/DELETE Action(已 Active 后不可删) | **P0** |
| 展期 | 远期展期(开反向 + 新 FWD) | P2 |

### 3.3 NDF(无本金交割远期) - P0

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 成交 | 录入 NDF,**不**生成 Cashflow | **P0** |
| RATE_FIX | 到期日执行 RATE_FIX Action,固定 fixing 汇率,**生成 1 条差额 Cashflow** | **P0** |
| 编辑/撤销 | UPDATE/DELETE Action | **P0** |

### 3.4 通用交易字段(SPOT/FWD/NDF 共有) - P0

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| **管理主体** (`managementEntityId`) | BIGINT | Y | **FK → `tms_management_entity_t.id`**(v3.1:从 VARCHAR code 改为 BIGINT id 强关联) |
| **交易对手** (`counterpartyId`) | BIGINT | Y | 引用 `tms_counterparty_t.id`,即 FX 银行/做市商 |
| **交易员** (`traderId`) | BIGINT | Y | 引用 `tms_trader_t.id` |
| **金融工具** (`instrumentId`) | BIGINT | Y | 引用 `tms_instrument_t.id`,**含产品类型信息**,自动决定 SPOT/FWD/NDF |
| **币种对** (`currencyPairId`) | BIGINT | Y | 引用 `tms_currency_pair_t.id`,**两个币种必须从币种对配置中选** |
| **描述** (`description`) | VARCHAR(500) | N | 业务说明,例:"付货款"、"对冲进口汇率风险" |
| **备注** (`remark`) | VARCHAR(500) | N | 内部备注 |

> **v3.1 关键变化**:
> - `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT` 强 FK 关联 id
> - 新增"交易日"字段(见 3.6)
> - `value_date` 字段 DB 存为 `value_date`,前端显示"交割日"(见 3.6)

### 3.5 价值字段(三种产品共有) - P0(v3.2 后端 calculate 接管)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 卖出金额 (`sellAmount`) | DECIMAL(38,18) | Y | 卖出币种金额,高精度 |
| 买入金额 (`buyAmount`) | DECIMAL(38,18) | Y | 买入币种金额,**后端 calculate 自动算** |
| 成交汇率 (`exchangeRate`) | DECIMAL(18,8) | Y | 实际成交汇率 |
| 市场汇率 (`marketRate`) | DECIMAL(18,8) | Y | 录入时市场参考汇率 |
| 点差 (`spreadBp`) | DECIMAL(10,4) | Y | (成交汇率 - 市场汇率) × 10000,基点 |

**★ v3.2 重大变化:所有联动计算由后端 `calculate` 接口接管**

### 3.5.1 后端 calculate 接口(v3.2 新增)

**目的**:将所有联动计算(金额/汇率/term)集中在后端,前端**只录入 + 触发计算**,不存任何计算逻辑。

**接口设计**:
```
POST /api/v1/dealing/fx-deals/calculate
Content-Type: application/json
```

**Request 字段(用户已填的)**:
```json
{
  "sellAmount": 100000.00,        // 可选
  "buyAmount": 138000.00,         // 可选
  "exchangeRate": 7.2000,         // 可选
  "marketRate": 7.1900,           // 可选
  "spreadBp": 100.00,             // 可选
  "tradeDate": "2026-07-04",       // 可选
  "valueDate": "2026-10-04"        // 可选
}
```

> **用户至少填 2 个金额/汇率字段**;后端基于已有字段推算未填字段。

**Response**(后端补全后):
```json
{
  "sellAmount": 100000.00,
  "buyAmount": 720000.00,
  "exchangeRate": 7.2000,
  "marketRate": 7.1900,
  "spreadBp": 100.00,
  "tradeDate": "2026-07-04",
  "valueDate": "2026-10-04",
  "termDays": 92,                 // ★ v3.2 后端自动算 = valueDate - tradeDate
  "maturityDate": "2026-10-04"    // = valueDate(冗余,但便于查询)
}
```

**后端计算规则**(单一可信源):
- 联动 1:`buyAmount = sellAmount × exchangeRate`
- 联动 2:`exchangeRate = marketRate + spreadBp / 10000`
- 联动 3:`termDays = valueDate - tradeDate`(天数)
- 联动 4:`maturityDate = valueDate`(冗余)

**前端调用方式**(节流 300ms):
```js
import { debounce } from 'lodash'
const onFieldChange = debounce((form) => {
  const result = await calculateFxDeal(form)
  Object.assign(form, result)  // 覆盖空白字段
}, 300)
```

**错误码**:
- 400 `INPUT_INSUFFICIENT`:用户填的字段不足 2 个,无法计算
- 400 `VALUE_INCONSISTENT`:用户填的字段互相矛盾(如 buyAmount ≠ sellAmount × exchangeRate 误差 > 0.0001)
- 422 `DATE_INVALID`:交易日 > 交割日(导致 termDays < 0)

### 3.6 日期字段(v3.2 移到公共表)

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| **交易日** (`tradeDate`) | **tms_deals_t** | DATE | Y | 交易达成的日子 |
| **交割日** (`valueDate`) | **tms_deals_t** | DATE | Y | DB 字段 `value_date`,前端显示"交割日" |
| **期限(天数)** (`termDays`) | **tms_deals_t** | INT | N | **后端 calculate 算** = `valueDate - tradeDate` |
| **到期日** (`maturityDate`) | **tms_deals_t** | DATE | Y | = `valueDate`,不可修改 |

**★ v3.2 关键变化**:
- 这些字段**全部移到 `tms_deals_t` 公共表**(`tms_fx_deals_t` 不再存日期字段)
- **跨 deal_type 共享日期语义**(AC 也有 trade_date/value_date,AT 也有,统一管理)
- v3.1 时这些字段在 `tms_fx_deals_t`,v3.2 移到 `tms_deals_t`

**业务效果不变**:
- SPOT:tradeDate=今天,valueDate=今天+T+0/1/2
- FWD:tradeDate=今天,valueDate=今天+1M/3M/6M/1Y
- NDF:tradeDate=今天,valueDate=今天+1M/3M/6M/1Y(同 FWD,但差额结算)

### 3.7 NDF 特有字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 名义本金 (`notional`) | DECIMAL(38,18) | Y | = 卖出金额(冗余存储便于查询) |
| fixing 汇率来源 (`fixingSource`) | VARCHAR(50) | Y | 报价行/数据源,例:"BLOOMBERG BFIX" |
| 结算汇率 (`fixingRate`) | DECIMAL(18,8) | N | RATE_FIX 时由系统填入 |
| 结算金额 (`settlementAmount`) | DECIMAL(38,18) | N | RATE_FIX 时由系统计算 = notional × (fixingRate - exchangeRate) |

### 3.8 SWAP / OPTION - P2(本版本不实现)

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 外汇掉期 | 即期 + 远期组合 | P2 |
| 外汇期权 | 看涨/看跌/买方/卖方 | P2 |
| 货币掉期 | 本金交换 + 利息交换 | P3 |

---

## 四、字段设计(整合视图)

### 4.1 主表:`tms_fx_deals_t`(v3.1 共享主键版)

```sql
-- ★ v3.1 关键变化:tms_fx_deals_t 无独立主键,共用 tms_deals_t.id
CREATE TABLE tms_fx_deals_t (
    -- ★ v3.1:无独立 id 主键,无 deal_id FK(共用 tms_deals_t.id)
    id                   BIGINT          NOT NULL PRIMARY KEY,    -- 物理上仍叫 id,但其值 = tms_deals_t.id
    deal_number          VARCHAR(50)     NOT NULL UNIQUE,         -- FX + yyyyMMdd + 4位

    -- ★ v3.1:管理主体 FK 改为 id(强类型关联)
    management_entity_id BIGINT          NOT NULL,                 -- FK → tms_management_entity_t.id

    -- 币种对(从 tms_currency_pair_t 选取)
    currency_pair_id     BIGINT          NOT NULL,                 -- FK 约束:两个币种必须来自此币种对

    -- 卖出腿
    sell_currency        VARCHAR(10)     NOT NULL,                 -- 冗余自 currency_pair.base_currency
    sell_amount          DECIMAL(38,18)  NOT NULL,                 -- 高精度

    -- 买入腿
    buy_currency         VARCHAR(10)     NOT NULL,                 -- 冗余自 currency_pair.quote_currency
    buy_amount           DECIMAL(38,18)  NOT NULL,                 -- 自动计算 = sell × rate

    -- 汇率与点差(联动 2)
    exchange_rate        DECIMAL(18,8)   NOT NULL,                 -- 成交汇率
    market_rate          DECIMAL(18,8)   NOT NULL,                 -- 市场汇率
    spread_bp            DECIMAL(10,4)   NOT NULL,                 -- 点差 bp

    -- ★ v3.1:日期字段 — 交易日 + 交割日(value_date) + 自动 term
    trade_date           DATE            NOT NULL,                 -- ★ v3.1 新增:交易日(交易达成的日子)
    value_date           DATE            NOT NULL,                 -- ★ v3.1 统一:DB 字段名 value_date(前端显示"交割日")
    term_days            INT,                                    -- ★ v3.1 新增:期限(天数),自动 = value_date - trade_date
    -- maturity_date 在 v3.1 强制 = value_date,无需独立列(后端校验)

    -- NDF 特有
    notional             DECIMAL(38,18),                          -- 名义本金(= sell_amount,冗余)
    fixing_source        VARCHAR(50),                             -- fixing 汇率来源(BLOOMBERG BFIX 等)
    fixing_rate          DECIMAL(18,8),                           -- RATE_FIX 时填入
    settlement_amount    DECIMAL(38,18),                          -- RATE_FIX 时计算

    -- 描述
    description          VARCHAR(500),
    remark               VARCHAR(500),

    -- 审计
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    -- ★ v3.2:约束已大幅精简(应用层校验,不用 DB 约束)
    CONSTRAINT chk_fx_legs_diff    CHECK (sell_currency <> buy_currency)
    -- ★ v3.2 移除的约束:
    -- - chk_fx_settlement:原 SPOT/FWD 互斥校验(instrument 可能变化)
    -- - chk_fx_ndf_fields:原 NDF fixing_source 必填校验(instrument 可能变化)
    -- - chk_fx_term_calc:产品类型不在 currency_pair,在 instrument,改为应用层校验
);

-- ★ v3.1:product_type 是从 tms_instrument_t 关联得到的视图字段,无需独立存储
-- 但 term_days 计算时仍需知道是 SPOT/FWD/NDF,通常通过 instrument_id JOIN 得到

CREATE INDEX idx_fxd_mgmt_entity     ON tms_fx_deals_t(management_entity_id);
CREATE INDEX idx_fxd_currency_pair  ON tms_fx_deals_t(currency_pair_id);
CREATE INDEX idx_fxd_sell_ccy       ON tms_fx_deals_t(sell_currency);
CREATE INDEX idx_fxd_buy_ccy        ON tms_fx_deals_t(buy_currency);
CREATE INDEX idx_fxd_value_dt       ON tms_fx_deals_t(value_date);
CREATE INDEX idx_fxd_trade_dt       ON tms_fx_deals_t(trade_date);
```

### 4.1.1 v3.1 vs v3.0 DDL 差异

| 元素 | v3.0 | **v3.1** | 原因 |
|------|------|---------|------|
| 主键 | `id BIGSERIAL PK` + `deal_id FK` | **共享 `id` = `tms_deals_t.id`** | FX 个性化字段无独立生命周期 |
| 管理主体 | 无独立列(走 `tms_deals_t.management_entity`) | **`management_entity_id BIGINT FK`** | 强类型关联,直接在 FX 表可查询 |
| 交割日字段 | `settlement_date DATE` | **`value_date DATE`** | 与 AC/AT 的 `tms_deals_t.value_date` 对齐 |
| 交易日 | 无 | **`trade_date DATE NOT NULL`** | 新增业务字段 |
| 期限 | `term VARCHAR(10)`(下拉选择) | **`term_days INT`**(自动计算) | 减少用户输入,避免不一致 |
| 到期日 | `maturity_date DATE`(可选) | **省略**(强制 = value_date) | 简化模型 |
| chk_fx_settlement | 有 | **移除** | instrument 可能变化 |
| chk_fx_ndf_fields | 有 | **移除** | instrument 可能变化 |

### 4.2 v3.0 相对 v2.0 的字段差异

| 字段 | v2.0 | v3.0 | 原因 |
|------|------|------|------|
| `productType` | 表单字段(冗余存储) | **删除**(由 instrument 决定) | 用户要求:录入界面无产品类型字段 |
| `valueDate` 起息日 | 字段 | **重命名为** `settlementDate` 交割日 | 用户要求:统一叫"交割日" |
| `limitCurrency` / `limitUsed` | 字段 | **删除** | 暂不做锁汇额度 |
| `mtmAmount` / `lastMtmDate` | 字段 | **删除** | 暂不做 MTM |
| `currencyPairId` | 无 | **新增**(FK) | 用户要求:币种必须从币种对配置中选 |
| `spreadBp` | 字段(联动 2) | 保留(v3.0 强化联动) | 用户要求:汇率↔市场↔点差联动 |
| `notional` | 无(只用 sellAmount) | **新增**(NDF 冗余) | NDF 业务约定 |
| `currencyPairId` | 无 | **新增** | 用户要求:币种必须从币种对配置选 |

### 4.3 公共主表:`tms_deals_t`(共享 AC/AT/FX/ST)

FX 交易在 `tms_deals_t` 中存一行 `deal_type='FX'`,携带通用字段(与 AC/AT 保持一致):

```sql
INSERT INTO tms_deals_t (
    deal_number, deal_type, management_entity,
    counterparty_id, instrument_id, trader_id,
    amount, currency,
    deal_date, value_date, status, latest_action_number,
    description, remark,
    created_by, created_at
) VALUES (
    'FX202607040001', 'FX', 'BU001',
    5001, 401, 1,
    100000.00, 'USD',
    '2026-07-04', '2026-07-06', 'New', 'ACT202607040001',
    '对冲进口付款汇率风险', NULL,
    'trader01', NOW()
);
```

> **v3.1 重要说明**:
> - `tms_deals_t.value_date` 是公共字段,FX 这里 = 交割日(语义对齐)
> - `tms_fx_deals_t.value_date` = `tms_deals_t.value_date`,两份冗余但便于 FX 直接查询
> - **v3.1 起 `tms_deals_t` 不存 `managementEntity` 字段**,改为 `management_entity_id` BIGINT FK(与 FX 表一致)
> - **v3.1 起 `tms_fx_deals_t` 共享 `tms_deals_t.id` 主键**,无独立 PK

### 4.4 字段对齐 AC/AT(v3.1)

| 字段 | AC | AT | **FX v3.1** | 状态 |
|------|----|----|------------|------|
| managementEntityId (BIGINT FK) | ✅ | ✅ | ✅ | 统一(全部用 id 强类型) |
| counterpartyId | ✅ | N/A | ✅ | 统一 |
| instrumentId | ✅ | N/A | ✅ | 统一 |
| traderId | ✅ | N/A | ✅ | 统一 |
| direction | Inflow/Outflow | N/A | 由 sell/buy 表达 | FX 特有 |
| amount | ✅ | ✅ | ✅ sellAmount | 统一 |
| currency | ✅ | ✅ | ✅ sellCurrency | 统一 |
| value_date (= 交割日) | ✅ | ✅ | ✅ | **统一** (语义对齐) |
| trade_date (交易日) | ❌ 无 | ❌ 无 | ✅ **新增** | FX 特有 |
| term_days (天数) | ❌ 无 | ❌ 无 | ✅ **新增** | FX 特有 |
| 主键共享 | ❌ | ❌ | ✅ FX 特有 | tms_fx_deals_t.id = tms_deals_t.id |

---

## 五、DealMap 设计(v3.2 多行单字段版)

### 5.1 核心原则

**FX DealMap 单字段 + 多行存储**:
- 一个 dealmap 只存 1 个数值 (`amount_or_rate`)+ 1 个类型 (`dealmap_type`)
- 一笔 FX 交易产生 **3-4 行 DealMap**(BUY/SELL/RATE [+ FIX])
- 复杂的事件类型 → 全部由 `dealmap_type` 枚举表达

### 5.2 DealMap 数据形态(v3.2)

```sql
CREATE TABLE tms_deal_map_t (
    -- 公共字段(所有 deal_type 共享)
    id                  BIGSERIAL       PRIMARY KEY,
    dealmap_number      VARCHAR(50)     NOT NULL UNIQUE,    -- DM + yyyyMMdd + 4位
    deal_number         VARCHAR(50)     NOT NULL,            -- 关联 tms_deals_t
    action_number       VARCHAR(50)     NOT NULL,            -- 关联 tms_actions_t
    dealmap_type        VARCHAR(20)     NOT NULL,            -- 区分 AC/AT/FX,且区分 BUY/SELL/RATE/FIX
    event_status        VARCHAR(20)     NOT NULL DEFAULT 'Active',
    account_role        VARCHAR(20),                         -- AC/AT 用 SOURCE/DEST,FX 留空
    is_reversal         CHAR(1)         NOT NULL DEFAULT '0',
    reverses_event_id   BIGINT,
    reversed_by_event_id BIGINT,
    description         VARCHAR(500),
    created_by          VARCHAR(50)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT             NOT NULL DEFAULT 0,
    deleted             CHAR(1)         NOT NULL DEFAULT '0',

    -- ★ v3.2:单字段统一存储 buy_amount / sell_amount / rate / fix
    amount_or_rate      DECIMAL(38,18)  NOT NULL,             -- 兼容 amount(38,18) 和 rate(18,8)

    -- 审计
    CONSTRAINT chk_dm_fx_type_amount CHECK (
        dealmap_type NOT IN ('FX_BUY_AMOUNT', 'FX_SELL_AMOUNT', 'FX_RATE', 'FX_FIX') OR amount_or_rate IS NOT NULL
    )
    -- ★ v3.2 移除:原 chk_dm_fx_amounts(3 字段非空检查) — 现在是单字段,约束失效
);

CREATE INDEX idx_dm_deal_number ON tms_deal_map_t(deal_number);
CREATE INDEX idx_dm_action_number ON tms_deal_map_t(action_number);
CREATE INDEX idx_dm_dealmap_type ON tms_deal_map_t(dealmap_type);
```

### 5.3 `dealmap_type` 枚举(v3.2 扩展)

| `dealmap_type` | 含义 | 数量/笔 deal | 触发时机 | 触发 CF |
|---|---|---|---|---|
| `AC` / `AT` | AC/AT 类型(预留) | 1-2 | AC/AT 触发 | 视具体 |
| `FX_BUY_AMOUNT` | 买入金额快照 | 1 | DEAL | 1 CF(买账) |
| `FX_SELL_AMOUNT` | 卖出金额快照 | 1 | DEAL | 1 CF(卖账) |
| `FX_RATE` | 成交汇率快照 | 1 | DEAL | 0 CF(仅记录) |
| `FX_FIX` | NDF fixing 汇率 | 1 | RATE_FIX(仅 NDF) | 1 CF(差额) |

> **★ v3.2 关键约束**:**一笔 DealMap 最多只能生成一条 Cashflow**(`1:0 或 1:1`)。CF 上存触发它的 dealmap_number。

### 5.4 FX 典型 DealMap 时序(v3.2 多行)

**SPOT 交易**(3 行 DealMap + 2 条 CF):
```
DEAL Action(act=DEAL)
  ├─ DealMap #1: dealmap_type='FX_BUY_AMOUNT'  amount_or_rate=100000  → 生成 CF(买账)
  ├─ DealMap #2: dealmap_type='FX_SELL_AMOUNT' amount_or_rate=13750   → 生成 CF(卖账)
  └─ DealMap #3: dealmap_type='FX_RATE'        amount_or_rate=7.2727   → 0 CF(仅记录)
```

**FWD 交易**(同 SPOT 结构,3 行 + 2 条 CF):
```
DEAL Action(act=DEAL)
  ├─ DealMap #1: dealmap_type='FX_BUY_AMOUNT'  amount_or_rate=720000  → 生成 CF(买账)
  ├─ DealMap #2: dealmap_type='FX_SELL_AMOUNT' amount_or_rate=100000  → 生成 CF(卖账)
  └─ DealMap #3: dealmap_type='FX_RATE'        amount_or_rate=7.2000   → 0 CF(仅记录)
```

**NDF 交易**(创建 3 行 + RATE_FIX 后 1 行 + 1 条 CF):
```
DEAL Action(act=DEAL)
  ├─ DealMap #1: dealmap_type='FX_BUY_AMOUNT'  amount_or_rate=720000  → 0 CF(等 RATE_FIX)
  ├─ DealMap #2: dealmap_type='FX_SELL_AMOUNT' amount_or_rate=100000  → 0 CF(等 RATE_FIX)
  └─ DealMap #3: dealmap_type='FX_RATE'        amount_or_rate=7.1800  → 0 CF(仅记录)

RATE_FIX Action(act=RATE_FIX, fixing_rate=7.1500)
  └─ DealMap #4: dealmap_type='FX_FIX'         amount_or_rate=7.1500  → 1 CF(差额 = 100000 × (7.1500-7.1800) = -3000 CNY)
```

### 5.5 v3.2 与 v3.1/v3.0 DealMap 对比

| 字段 | v3.0 (FX) | v3.1 (FX) | **v3.2 (FX)** |
|------|----------|-----------|---------------|
| buy_amount | 1 字段 | 1 字段 | **删除** |
| sell_amount | 1 字段 | 1 字段 | **删除** |
| rate | 1 字段 | 1 字段 | **删除** |
| **amount_or_rate** | 无 | 无 | **新增(单字段)** |
| **dealmap_type** | 仅 `FX` 1 个 | 仅 `FX` 1 个 | **扩展 4 个**:`FX_BUY_AMOUNT` / `FX_SELL_AMOUNT` / `FX_RATE` / `FX_FIX` |
| DealMap 行数 / 笔 | 1 | 1 | **3-4**(多行) |
| 1 DealMap → 1 CF | 部分 | 部分 | **明确**:0 或 1(不强制) |
| chk_dm_fx_amounts | 有(3 字段) | 有(3 字段) | **移除** |
| chk_dm_fx_type_amount | 无 | 无 | **新增**(1 字段非空) |

### 5.6 1 DealMap → 1 Cashflow 约束

**业务规则**:
- 每条 DealMap 行最多生成 1 条 Cashflow(0 或 1)
- Cashflow 触发后,`cashflow.dealmap_number = dealmap.dealmap_number`(强引用)
- 反向:可通过 `dealmap_number` 查 CF;可通过 `cflow_number` 查触发它的 DealMap

**例外:哪些 DealMap 不生成 CF**:
- `FX_RATE` 快照:只是记录汇率,**不**生成 CF(无金额意义)
- `FX_BUY_AMOUNT` / `FX_SELL_AMOUNT` / `FX_FIX`:可生成 1 条 CF

(无 MTM 事件,v3.0 不做 MTM)
```

**NDF**:
```
DEAL Action(act=DEAL)
  └─ (无 DealMap,创建不生成)

RATE_FIX Action(act=RATE_FIX, fixing rate=7.1500)
  └─ DealMap #1: buy=-3000 CNY(差额), sell=100000 USD, rate=7.1500  ← fixing 后生成 1 条
```

> **关键**:NDF 的 DealMap 行直到 RATE_FIX 才有,buy_amount 表示差额(正负值)。

---

## 六、业务规则

### 6.1 通用规则(SPOT/FWD/NDF 共有)(v3.2)

| # | 规则 | 说明 |
|---|------|------|
| R1 | 卖出币种 ≠ 买入币种 | DB CHECK 约束 |
| R2 | 管理主体必填,FK 关联 `tms_management_entity_t.id` | v3.2 改:BIGINT id 强类型(非 VARCHAR code) |
| R3 | 交易对手必填 | 引用 `tms_counterparty_t.id`,类型 = 'BANK' |
| R4 | 交易员必填 | 引用 `tms_trader_t.id` |
| R5 | 金融工具必填 | 引用 `tms_instrument_t.id`,FX 类(产品类型嵌入) |
| R6 | 币种对 必填 | 引用 `tms_currency_pair_t.id`,sell/buy 币种必须 = currency_pair 的 base/quote |
| R7 | **v3.2 联动由后端 calculate** | 前端调 `POST /api/v1/dealing/fx-deals/calculate` 算 buyAmount / termDays / 其他联动 |
| R8 | 交易日 ≤ 交割日 | 后端校验,否则 termDays 为负,报错 |
| R9 | 成交汇率 = 市场汇率 + 点差 / 10000 | 后端 calculate 保证一致性 |
| R9 | 金额 > 0 | DB CHECK |
| R10 | 交割日 不能早于今天 | 前端校验 |

### 6.2 SPOT 特有规则(v3.2)

| # | 规则 | 说明 |
|---|------|------|
| S1 | **v3.2 期限 = 0** | 交易日 = 交割日(T+0),后端 calculate 校验 |
| S2 | 交割日 = 交易日 + 0/1/2 工作日(按币种对规则) | 后端 calculate 自动算 |
| S3 | 成交汇率与市场汇率偏离 ≤ 200 bp | 提示超限,允许保存(标记 Warning) |
| S4 | **v3.2 DealMap 多行**:3 行(BUY/SELL/RATE) | DEAL Action 同步触发 |
| S5 | **v3.2 Cashflow 数量**:2 条(BUY+SELL 各 1) | 由对应 DealMap 触发 |

### 6.3 FWD 特有规则(v3.2)

| # | 规则 | 说明 |
|---|------|------|
| F1 | **v3.2 期限 ≥ 1 天** | termDays = valueDate - tradeDate,后端 calculate 算 |
| F2 | 到期日 = 交割日(不可改,v3.2) | 与 valueDate 相等 |
| F3 | ~~期限 ∈ {1W, 1M, 3M}~~ | v3.2 改为 termDays(整数),自由填写 |
| F4 | **v3.2 DealMap 多行**:3 行(BUY/SELL/RATE) | DEAL Action 同步触发 |
| F5 | **v3.2 Cashflow 数量**:2 条(BUY+SELL 各 1) | 由对应 DealMap 触发 |

### 6.4 NDF 特有规则(v3.2)

| # | 规则 | 说明 |
|---|------|------|
| N1 | fixing 汇率来源 必填 | 引用报价行/数据源 |
| N2 | 不交割本金 | 系统标记,到期只生成 1 条 Cashflow(差额) |
| N3 | 到期日 = fixing 汇率日期 | - |
| N4 | 结算金额 = 名义本金 × (fixing - 锁定汇率) | 系统计算 |
| N5 | **v3.2 DEAL 时**:3 行 DealMap(BUY/SELL/RATE)+ **0 条 CF** | 等 RATE_FIX |
| N6 | **v3.2 RATE_FIX 时**:1 行 DealMap(FIX)+ **1 条 CF(差额)** | 系统触发 |
| N6 | **DEAL 不生成 Cashflow** | 等 RATE_FIX 后才生成 |
| N7 | RATE_FIX 后生成 1 条 Cashflow(差额方向,正值或负值) | 系统触发 |
| N8 | RATE_FIX 只能执行一次 | 状态机保护 |

### 6.5 字段联动规则(详)

**联动 1:金额 ↔ 汇率**

| 输入字段 | 自动计算字段 | 公式 |
|---------|-------------|------|
| 卖出金额 + 成交汇率 | 买入金额 | `buy = sell × rate` |
| 卖出金额 + 买入金额 | 成交汇率 | `rate = buy / sell` |
| 买入金额 + 成交汇率 | 卖出金额 | `sell = buy / rate` |

**联动 2:汇率 ↔ 市场 ↔ 点差**

| 输入字段 | 自动计算字段 | 公式 |
|---------|-------------|------|
| 成交汇率 + 市场汇率 | 点差(bp) | `spread = (rate - market) × 10000` |
| 成交汇率 + 点差 | 市场汇率 | `market = rate - spread / 10000` |
| 市场汇率 + 点差 | 成交汇率 | `rate = market + spread / 10000` |

**前端实现**:使用 `watch()` 监听输入字段,任何字段变更时触发计算并更新其它两个字段(仅当其它两个字段为空或为派生状态时)。

**后端校验**:保存前 R7 + R8 验证一致性,允许极小浮点误差(0.00000001)。

---

## 七、业务流程

### 7.1 SPOT 流程(v3.0 - 极简)

```
交易员录入 → 校验 → DEAL Action
  │
  ▼
DealMap #1: buy + sell + rate  ← 创建即生成
  │
  ▼
Cashflow × 2(SELL 账户 + BUY 账户)← 同步生成
  │
  ▼
Active(完成)
```

### 7.2 FWD 流程(v3.0 - 极简)

```
交易员录入 → 校验 → DEAL Action
  │
  ▼
DealMap #1: buy + sell + rate  ← 创建即生成
  │
  ▼
Cashflow × 2(SELL + BUY)← 同步生成
  │
  ▼
Active(完成,等交割日系统触发)
```

### 7.3 NDF 流程(v3.0 - 极简,两阶段)

**阶段 1:DEAL(签约)**
```
交易员录入(含 fixing 来源) → 校验 → DEAL Action
  │
  ▼
DealMap: (无)
Cashflow: (无)  ← 关键差异:不生成
  │
  ▼
Active(待 RATE_FIX)
```

**阶段 2:RATE_FIX(到期)**——系统调度或手工触发
```
fixing 汇率确定(系统从报价源获取)→ RATE_FIX Action
  │
  ▼
DealMap #1: buy=差额, sell=notional, rate=fixing_rate
  │
  ▼
Cashflow #1: 差额(正值或负值)← 生成
  │
  ▼
Active(完成)
```

---

## 八、状态机(v3.0 - 极简)

### 8.1 状态定义

| 状态 | 说明 |
|------|------|
| **New** | 刚 DEAL 创建(只 NDF 在 RATE_FIX 前停留) |
| **Active** | 已生成 Cashflow(SPOT/FWD 立即进入,NDF 在 RATE_FIX 后进入) |
| **Deleted** | UPDATE 不可逆 / 手工取消 |

### 8.2 状态流转图

```
                 DEAL Action
                     │
                     ▼
        ┌─────────────────────────┐
        │   New(SPOT/FWD 立即转 Active)│
        │   New(NDF 等 RATE_FIX)        │
        └────────────┬────────────┬────┘
                     │            │
        (SPOT/FWD)  │            │  (NDF)
        生成 CF +   │            │  不生成
        DealMap     │            │
                     ▼            ▼
                 Active ←─── RATE_FIX Action
                            生成 CF + DealMap

        UPDATE 任意状态:  New/Active → Active(更新)
        DELETE 任意状态:  New/Active → Deleted
```

### 8.3 状态机规则

| 规则 | 说明 |
|------|------|
| New 不可直跳 Deleted | 必须 UPDATE → Active 或 RATE_FIX → Active 后才能 DELETE |
| NDF New 状态特有 | NDF 停留 New 状态等 RATE_FIX;SPOT/FWD 立即转 Active |
| RATE_FIX 只能一次 | NDF 第二次 RATE_FIX 报错 |
| DEAL 必含 DealMap/Cashflow 验证 | 保存时校验一致性 |

---

## 九、Action 设计(v3.0 - 简化)

### 9.1 FX Action 类型(只 4 个)

| Action 类型 | 说明 | 触发 |
|------------|------|------|
| **DEAL** | 创建 FX 交易(SPOT/FWD 即时生成 CF;NDF 不生成) | 录入时 |
| **UPDATE** | 修改 FX 交易(已 Active 也可改) | 编辑保存时 |
| **DELETE** | 删除/撤销 FX 交易(软删) | 手工撤销时 |
| **RATE_FIX** | 锁定 fixing 汇率(只 NDF) | NDF 到期,系统/手工触发 |

### 9.2 v3.0 与 v2.0 Action 对比

| Action 类型 | v2.0 (FX) | v3.0 (FX) | 说明 |
|------------|-----------|-----------|------|
| CREATE | ✅ | **改名为 DEAL** | 语义统一 |
| UPDATE | ✅ | ✅ | 保留 |
| DELETE | ✅ | ✅ | 保留 |
| SUBMIT | ✅ | **删除** | 无审批流 |
| APPROVE | ✅ | **删除** | 无审批流 |
| REJECT | ✅ | **删除** | 无审批流 |
| EXECUTE | ✅ | **删除** | 创建即执行 |
| **RATE_FIX** | NDF 隐含在 MTM 流程 | **显式** | 专用于 NDF fixing |

### 9.3 Action 与 DealMap/Cashflow 关系

| Action | DealMap 触发 | Cashflow 触发 |
|--------|-------------|---------------|
| DEAL(SPOT) | 创建 1 行 DealMap | 同步生成 2 条(SELL + BUY) |
| DEAL(FWD) | 创建 1 行 DealMap | 同步生成 2 条(SELL + BUY) |
| DEAL(NDF) | **不**创建 | **不**生成 |
| UPDATE | 不创建 DealMap 行(只修改 deal 表) | 不触发 |
| DELETE | 不创建 DealMap 行 | 同步**软删**关联的 CF(deleted='1') |
| RATE_FIX | 创建 1 行 DealMap(NDF fixing 快照) | 同步生成 1 条 NDF 差额 CF |

---

## 十、API 清单(v3.0 - 简化)

### 10.1 FX 交易 CRUD(4 个 Action 对应 4 个接口)

| 接口 | 方法 | 说明 | Action |
|------|------|------|--------|
| `/api/v1/dealing/fx-deals` | POST | 创建 FX 交易 | **DEAL**(SPOT/FWD 同步生成 CF;NDF 不生成) |
| `/api/v1/dealing/fx-deals/{dealNumber}` | GET | 详情(按 dealNumber) | - |
| `/api/v1/dealing/fx-deals/page` | GET | 分页查询 | - |
| `/api/v1/dealing/fx-deals/update` | POST | 更新 FX 交易 | **UPDATE** |
| `/api/v1/dealing/fx-deals/delete/{id}` | POST | 删除 FX 交易(软删 + 关联 CF 软删) | **DELETE** |
| `/api/v1/dealing/fx-deals/{id}/rate-fix` | POST | NDF fixing 触发 | **RATE_FIX**(只 NDF,生成 1 条 DealMap + 1 条 CF) |

### 10.2 FX 关联查询

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/fx-deals/{dealNumber}/dealmaps` | GET | 该 FX 的所有 DealMap(SPOT/FWD 1 条;NDF 1 条,等 RATE_FIX) |
| `/api/v1/dealing/fx-deals/{dealNumber}/cashflows` | GET | 该 FX 的所有 Cashflow(SPOT/FWD 2 条;NDF 1 条) |
| `/api/v1/dealing/fx-deals/{dealNumber}/actions` | GET | 该 FX 的所有 Action(DEAL/UPDATE/DELETE/RATE_FIX) |
| `/api/v1/dealing/fx-deals/{dealNumber}/instrument` | GET | 关联的金融工具详情(含产品类型) |

### 10.3 币种对查询(用于币种对下拉)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/opentms/basedata/api/v1/currency-pairs/page?keyword=USD&status=1` | GET | 列出所有启用的币种对(从 basedata) |

### 10.4 v3.0 相对 v2.0 的 API 差异

| 接口 | v2.0 | v3.0 |
|------|------|------|
| `/fx-deals/{id}/submit` | POST 提交审批 | **删除** |
| `/fx-deals/{id}/approve` | POST 审批 | **删除** |
| `/fx-deals/{id}/reject` | POST 驳回 | **删除** |
| `/fx-deals/{id}/execute` | POST 执行 | **删除** |
| `/fx-deals/{id}/mature` | POST 到期 | **删除** |
| `/fx-deals/{id}/cancel` | POST 撤销 | **删除**(改用 DELETE) |
| `/fx-deals/{id}/rate-fix` | 无 | **新增**(RATE_FIX Action) |
| `/fx-limit/*` | 锁汇额度 3 个 | **全部删除**(暂不做) |
| `/fx-ndf/fixing/{date}` | fixing 获取 | **保留**(NDF RATE_FIX 必备) |

---

## 十一、界面原型

### 11.1 录入页(以 FWD 为例,v3.0 简化版)

```
┌─ 资金管理主体 ────────────────────────┐
│ [BU001 集团总部      ▼] [BaseDataPicker]   │
└────────────────────────────────────────┘
┌─ 交易对手 ────────────────────────────┐
│ [5001 中行总行        ▼] [BaseDataPicker]   │
└────────────────────────────────────────┘
┌─ 交易员 ──────────────────────────────┐
│ [001  李四            ▼] [BaseDataPicker]   │
└────────────────────────────────────────┘
┌─ 金融工具 ────────────────────────────┐
│ [401 FX-FWD-USD-CNY-3M  ▼] [BaseDataPicker] │
│   ↑ 选中后自动决定产品类型(FWD)             │
└────────────────────────────────────────┘
┌─ 币种对 ──────────────────────────────┐
│ [USD/CNY (基础/计价) ▼] [BaseDataPicker]   │
│   ↑ 选后自动设置 sell=USD, buy=CNY          │
└────────────────────────────────────────┘

─────────── 远期交易要素(联动 1:金额↔汇率)─────────────

卖出金额  [100,000.00]  USD      ← 任一字段变化
成交汇率  [7.2000]                    其它两字段自动计算
买入金额  [720,000.00]  CNY    ← 此处自动算出

─────────── 汇率联动 2(汇率↔市场↔点差)─────────────

成交汇率  [7.2000]
市场汇率  [7.1900]     ← 任一字段变化
点差 (bp) [100.00]                  其它字段自动算出

─────────── 期限与交割日 ─────────────

期限     [3M ▼]
交割日   [2026-07-04]  ← 选中后自动按"币种对 + 节假日"算出
到期日   [2026-10-04]  ← 自动按营业日规则调整

─────────── 描述 ─────────────

[对冲进口付款汇率风险                          ]
```

> **NDF 特有字段**(只在 instrument = NDF 时显示):
> ```
> ─────────── NDF 特有 ─────────────
> 
> 名义本金    [100,000.00] USD
> fixing 来源 [BLOOMBERG BFIX ▼]  ← 必填
> 结算汇率    [— —] (待 RATE_FIX 时填入)
> 结算金额    [— —] (待 RATE_FIX 时填入)
> 
> ⚠️ NDF 交易创建后不立即生成 Cashflow,
>     等到期日 RATE_FIX 后才生成 1 条差额 Cashflow
> ```

### 11.2 详情页(4 个 Tab,v3.0 简化)

```
[基本信息] [DealMap (n)] [Cashflow (n)] [Action (n)]
```

- **基本信息**:管理主体/对手方/交易员/金融工具/币种对 + 卖出/买入/汇率/期限/交割日
- **DealMap**:仅显示 buy_amount / sell_amount / rate 3 列(SPOT/FWD 1 行;NDF 1 行[RATE_FIX 后])
- **Cashflow**:SPOT/FWD 2 条;NDF 1 条
- **Action**:DEAL / UPDATE / DELETE / RATE_FIX 列表

> **v3.0 删除的 Tab**:`MTM 历史`(不做 MTM)/ `会计分录`(M1.3 占位)

---

## 十二、验收标准

### 12.1 P0 核心验收(v3.1 验证)

| # | 功能 | 验收条件 |
|---|------|----------|
| A1 | 通用字段 | 录入时 4 个通用字段(管理主体/对手方/交易员/金融工具)+ 1 个币种对,均从 basedata picker 获取 |
| A2 | 联动 1(金额↔汇率) | 卖出金额/买入金额/成交汇率三者任两者输入,第三者自动计算,精度符合 0.00000001 误差 |
| A3 | 联动 2(汇率↔市场↔点差) | 成交汇率/市场汇率/点差三者任两者输入,第三者自动计算,公式 `rate = market + spread/10000` |
| A4 | 币种对约束 | sell/buy 币种必须来自所选币种对的 base/quote,前端 picker 强约束,后端 CHECK 约束 |
| A5 | 产品类型隐式 | 录入界面无 productType 字段,产品类型由 instrument 决定(SPOT/FWD/NDF) |
| A6 | 交割日命名 | FX 录入界面统一显示"交割日"标签(不再叫"起息日") |
| A7 | **v3.1** 交易日字段 | `tradeDate` 字段必填,代表交易达成的日子,前端 DatePicker |
| A8 | **v3.1** term 自动算 | `term_days` 后端计算 = `value_date - trade_date`,前端只读显示 |
| A9 | **v3.1** 到期日 = 交割日 | `maturity_date` 字段可省略,后端强制 `value_date` 即到期日,前端不可改 |
| A10 | **v3.1** 管理主体 FK id | `managementEntityId BIGINT FK`,强类型关联,无字符 code |
| A11 | **v3.1** 共享主键 | `tms_fx_deals_t.id = tms_deals_t.id`,无独立 PK,关联查询 JOIN 即可 |
| A12 | **v3.1** 移除 chk 约束 | `chk_fx_settlement` 和 `chk_fx_ndf_fields` 已移除(instrument 可能变化) |
| A13 | SPOT 创建 | DEAL Action 触发后:1 条 DealMap + 2 条 Cashflow(同步生成) |
| A14 | FWD 创建 | DEAL Action 触发后:1 条 DealMap + 2 条 Cashflow(同步生成) |
| A15 | NDF 创建 | DEAL Action 触发后:**0 条 DealMap + 0 条 Cashflow**(等 RATE_FIX) |
| A16 | NDF RATE_FIX | RATE_FIX Action 触发后:1 条 DealMap(fixing 快照)+ 1 条差额 Cashflow(正值或负值) |
| A17 | Action 4 种 | DEAL/UPDATE/DELETE/RATE_FIX 类型完整 |
| A18 | 状态机 | New/Active/Deleted 三态流转正确 |
| A19 | DealMap 极简 | FX DealMap 行只含 buy_amount/sell_amount/rate 3 个数值字段 |
| A20 | 字段精度 | 卖出/买入金额 DECIMAL(38,18);汇率 DECIMAL(18,8);点差 DECIMAL(10,4);term_days INT |
| A21 | 命名 | 全系统统一为"管理主体" |

### 12.2 暂不做项(明确不做)

| # | 功能 | 状态 |
|---|------|------|
| Z1 | 锁汇额度(签约即占、到期释放、多级预警) | **P2+** 不在本版本 |
| Z2 | 询价(询价接口、市场数据对接) | **P2+** 不在本版本 |
| Z3 | MTM 实时计算(每日盯市) | **P2+** 不在本版本 |
| Z4 | 完整 FX 套件(外汇掉期、外汇期权、货币掉期) | **P2+** 不在本版本 |
| Z5 | 衍生品会计(IFRS 9 双借双贷) | **P1** M1.3 阶段 |
| Z6 | FX 审批流(多级审批) | **不做** FX 业务本身无审批需求 |
| Z7 | MTM 历史 Tab | **不做** v3.0 不做 MTM |
| Z8 | 会计分录 Tab(占位) | **不做** M1.3 阶段 |

### 12.3 兼容性验证

| 项 | 验收 |
|----|------|
| 与 AC 的一致性 | 通用字段定义(管理主体/对手方/交易员/金融工具)完全一致 |
| 与 AT 的一致性 | 同上 |
| 与 M1-Deal 公共主表 | `tms_deals_t` 字段定义、命名、约束与 `M1-Deal交易PRD-v5.md` 完全一致 |
| 命名统一 | 全文搜索"管理主体"/"业务主体"无残留,均为"管理主体" |
| API 路径规范 | `/api/v1/dealing/fx-deals` 对齐 `/api/v1/{module}/{resource}` 规范 |
| 币种对 base data | sell/buy 币种必须从 `tms_currency_pair_t` 选取 |

---

## 十三、待确认事项

| # | 事项 | 选项 | 当前决定 |
|---|------|------|---------|
| Q1 | FWD 期限 > 1Y 是否需要 2 级审批? | 是/否 | **不需要** v3.0 FX 无审批流 |
| Q2 | NDF fixing 数据源:对接 Bloomberg API 还是手动录入? | Bloomberg / 手动 / 双模式 | 暂定手动(系统调度),后续接 API |
| Q3 | FWD/NDF 是否支持部分交割? | 支持/不支持 | 不支持(到期全额交割) |
| Q4 | 联动 1/联动 2 的小数位显示:保留 4 位还是 8 位? | 4 / 8 | 暂定 4 位显示,8 位存储 |
| Q5 | 联动 1/联动 2 的输入框:可手动改 vs 只读? | 手动可改 / 只读 | 任意字段都可手动改,改后其它两字段同步 |
| Q6 | 币种对营业日规则数据来源? | 手工维护 / 数据接口 | 暂用手工维护,基于 holiday 表 |
| Q7 | NDF RATE_FIX 时,fixing 汇率是手工录入还是系统自动取? | 手工 / 自动 | **手工**(v3.0),后续接 Bloomberg API |

---

## 十四、Phase 计划(v3.0 更新)

### Phase 1(已完成 v1.0 + v2.0 + v3.0 PRD)

- [x] 写 M3-外汇交易 PRD v1.0(2026-04-11)
- [x] 写 fx-trading-prototype.html(交互原型,1620 行)
- [x] 写 M3-金融工具估值 PRD
- [x] 写 M3-外汇交易 PRD v2.0(2026-07-04)- 字段补全 + 命名对齐 + 架构升级
- [x] **本任务**:PRD v3.0(2026-07-04)- 6 项用户反馈简化

### Phase 2(v3.0 实施 - 范围大幅缩减)

- [ ] DB 设计:DDL `db/schema/24-fx-deal-v3.sql`(3 字段 DealMap + 4 Action + 币种对约束)
- [ ] 后端:`fx_deal` 实体 + Service + Controller(4 Action)
- [ ] 前端:`FxDealList.vue` + `FxDealForm.vue`(联动 1+2 + 币种对 picker) + 详情页(4 Tab)
- [ ] NDF RATE_FIX 调度(可手工触发,后续自动化)
- [ ] 测试:API + UI(无 MTM/无锁汇/无询价)

### Phase 3(后续 M3 完善 - P2+ 暂不做)

- [ ] SWAP / OPTION / 货币掉期
- [ ] 衍生品会计集成(M1.3 阶段)
- [ ] Bloomberg fixing API 对接
- [ ] 锁汇额度(可考虑 M3.1)
- [ ] 询价(可考虑 M3.1)
- [ ] MTM 实时计算(可考虑 M3.1)

---

## 十四、相关文档

- `M3-利率掉期PRD.md` - 利率掉期设计参考
- `M3-估值PRD-v1.md` (renamed from `M3-金融工具估值PRD.md`) - 估值/MTM 设计
- `M1-Deal交易PRD-v5.md` - 公共主表定义
- `M1-AT交易PRD-v2.md` - DealMap v2.0 架构参考
- `docs/architecture/business/AC交易与现金流分离架构设计.md` - 现金流分离架构
- `docs/原型/M3/fx-trading-prototype.html` - 现有交互原型
- `docs/原型/M3-金融工具模块UX原型.md` - M3 UX 规范

---

*PM产出 - M3 v2.0 (2026-07-04)*
*核心变更:补全 4 个通用交易字段(管理主体/对手方/交易员/金融工具)+ 命名统一(管理主体)+ DealMap v2.0 架构*
*SPOT/FWD/NDF 三种产品统一字段集 + 各自特有字段*
