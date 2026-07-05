# M3-货币市场(Money Market, MM)交易 PRD

**版本**: v1.0
**角色**: 产品经理 (PM)
**日期**: 2026-07-05
**基于**:
- `M3-外汇交易PRD.md` v3.2(共享 DealMap v2.0 + 后端 calculate + 单字段多行 + 1:1 CF)
- `M1-DealMap 生命周期事件PRD-v2.md`(DealMap 业务事件模型)
- `M1-主体默认银行账户规则PRD-v1.md`(基于 5 维运行时匹配)
- `M1-资金管理主体PRD-v1.md`(管理主体定义)
- `docs/规范/Open-TMS开发规范文档.md`(命名、类型、审计规范)
- `open-tms功能特性清单.md`(业界对标参考)
- 2026-07-03 模块整合(dealing 端口 8082)
- 2026-07-04 命名统一(管理主体/业务主体 → **管理主体**)
**状态**: v1.0 草稿 - 初版,本期只做 P0 同业拆借(Borrow/Lend)

---

## 〇、修订记录

| 版本 | 日期 | 变更 | 作者 |
|------|------|------|------|
| v1.0 | 2026-07-05 | 初版,覆盖同业拆借(拆出 LEND / 拆入 BORROW),回购(REPO)与同业存单(NCD)占位 P1/P2 | PM |

---

## 一、模块概述

### 1.1 模块名称

**mm** — Money Market 货币市场交易(归入 `dealing` 模块,统一端口 8082,与 FX 共享基础设施)

### 1.2 业务背景

货币市场(Money Market)是**短期资金的批发市场**,交易期限通常 ≤ 1 年,核心业务包括:

- **同业拆借**(Interbank Loan/Borrow):隔夜 O/N、定期 7D/14D/1M/3M/6M/1Y 的资金借贷
- **回购协议**(Repo/Reverse Repo):以债券/票据为担保的短期融资
- **同业存单**(NCD):可转让的大额存单

企业资金管理中,资金经理需要在以下场景使用 MM 工具:

1. **流动性调度** — 日终富余资金拆出赚取利息(O/N Lend)
2. **头寸填补** — 短期流动性不足时拆入填补(Borrow)
3. **现金管理** — 替代活期存款,获取更高收益(Lend)
4. **监管要求** — 同业授信额度管理、IFRS 9 减值计量

### 1.3 功能定位

支持同业拆借(拆出/拆入)为核心的全生命周期管理:

- 录入 → 提交 → 审批 → 起息(本金流出/流入) → 计息 → 到期(本金/利息回流/支付)
- 拆出/拆入两个方向的完整状态机
- 完整 DealMap v2.0 业务事件(本金腿 + 利息腿)
- 利息计提(按月/按日)+ 提前还款罚息 + 浮动利率重置预留
- 与 AC/AT/FX 共享 `tms_deals_t` / `tms_actions_t` / `tms_deal_map_t` / `tms_cashflow_t` 公共表

### 1.4 用户角色

| 角色 | 典型操作 |
|------|---------|
| **资金交易员** | 录入同业拆借(拆出/拆入),询价,报价,签约 |
| **资金经理** | 审批拆借交易(金额超阈值需 2 级审批),管理额度 |
| **风险经理** | 监控对手方授信占用、利率风险、ECL 减值 |
| **结算员** | 处理本金/利息现金流打款、到期结算、提前还款 |
| **财务主管** | 月末利息计提入账、报表核对 |

### 1.5 与其他模块的关系

```
mm 交易
  │
  ├── 共享 dealing 基础设施(沿用 FX v3.2 设计)
  │     ├── tms_deals_t (公共主表,所有 dealType='MM')
  │     │     ├── 通用字段:管理主体/对手方/交易员/金融工具/币种/金额/状态
  │     │     └── 日期字段(公共):trade_date / value_date / maturity_date(在 tms_deals_t)
  │     ├── tmm_mm_deals_t (MM 特性表,共享 tms_deals_t.id)
  │     ├── tms_actions_t (操作历史,审批流)
  │     ├── tms_deal_map_t (业务事件,event_type=MM*)
  │     └── tms_cashflow_t (本金/利息现金流,关联 dealmap_number)
  │
  ├── 依赖 basedata
  │     ├── management-entity (资金管理主体)
  │     ├── counterparty (交易对手,必须是银行类 BANK 类型)
  │     ├── trader (交易员)
  │     ├── instrument (金融工具,MM 拆借的产品定义)
  │     ├── bank-account (结算账户,起息/到期时引用)
  │     ├── currency + currency (币种,本币与外币,与 m1-主体默认银行账户规则联动)
  │     └── rate-base (利率基准,SHIBOR/LIBOR/SOFR,本期占位 P1)
  │
  ├── 依赖 M1 特性
  │     └── tms_default_bank_account_rule_t (本金收/付账户自动匹配)
  │
  └── 输出至下游
        ├── valuation (估值,P1:按市价计算浮动利率拆借估值)
        ├── var (VaR,MM 利率风险敞口)
        ├── exposure (敞口,银行对手方敞口)
        ├── impairment (减值,IFRS 9 ECL,银行业务高优先级)
        ├── limit (限额,对手方授信占用)
        └── settlement (结算,起息日/到期日打款)
```

### 1.6 范围说明

**本次范围 (P0 - 同业拆借)**:
- 同业拆出 (LEND):隔夜 O/N + 7 定期(7D/14D/1M/3M/6M/1Y)
- 同业拆入 (BORROW):同上方向
- 固定利率(Fixed) + 单利 + ACT/360 计算基准
- 到期一次性还本付息(本期默认)
- 现金流规则:起息日 -1(本金流出,LEND)/ +1(本金流入,BORROW),到期日本金回流(LEND)/支付(BORROW) + 利息支付
- 5 维主体默认银行账户规则联动(基于方向 + 币种)
- 利息计提(月末按日计提,本期支持)
- 提前还款罚息(本期支持,简化罚息率字段)

**本次不在范围 (P1/P2+)**:
- 回购协议 REPO / REPO_REVERSE(P1,本期占位预留字段)
- 同业存单 NCD(P2,本期占位预留字段)
- 浮动利率(Floating Rate,浮动基准 SHIBOR/LIBOR,占位 P1)
- 复利计息(本期默认单利)
- 担保物管理(P1,本期 `collateral_id` 字段预留)
- 跨币种拆借 + 汇率联动(P1)
- 多期付息(每月/每季付息,本期默认到期一次性)
- 利率衍生品 IRS / CCS(P2)

---

## 二、业界对标

### 2.1 综合对比表

| 特性 | FIS Quantum | SAP TRM | Murex MX.3 | Kyriba | Open-TMS v1.0 |
|------|-------------|---------|------------|--------|---------------|
| 同业拆借(Interbank Loan) | ✅ Interbank 模块 | ✅ Money Market | ✅ MM Desk | ✅ Cash & Investments | ✅ P0 |
| 拆出 / 拆入双向 | ✅ Lend / Borrow | ✅ Inward / Outward | ✅ Receive / Pay | ✅ In / Out | ✅ **LEND / BORROW** |
| 期限类型 | O/N / Term / Open | O/N / Term / Callable | O/N / 1W / 1M / 3M / 6M | Daily / Term / Fixed | ✅ **本期: O/N + Term** |
| 利率类型 | Fixed / Floating / Step | Fixed / Floating | Fixed / Float | Fixed / Variable | ✅ **Fixed(本期)**, Float 占位 |
| 利率基准 | SOFR / EURIBOR / SHIBOR | EURIBOR / LIBOR | LIBOR / FED FUND | Fed Funds / SHIBOR | 📋 **P1 占位** (本期用固定利差) |
| 计息基础 | ACT/360, ACT/365, 30/360 | ACT/360, ACT/365, 30E/360 | ACT/360, ACT/365 | ACT/360, ACT/365 | ✅ **ACT/360, ACT/365, 30/360**(均支持) |
| 现金流规则 | 起息 -1 / 到期 +1 起 / +1 息 | 同 | 同 | 同 | ✅ **同** |
| 利息计提(Accrual) | 月末按日 + 期末调整 | 月末按日 / 期末调整 | 月末按日 | 日计提 / 期末调整 | ✅ **月末按日计提** |
| 提前还款 + 罚息 | 支持 + Breakage Cost | 支持 + Prepayment Penalty | 支持 + Make-whole | 支持 + Penalty Rate | ✅ **本期支持**(简化罚息率) |
| 浮动利率重置 | 重置日 + 公告利率 | 调整日 + 索引利率 | 重置日 + 报价 | Reset Date + Index | 📋 **P1**(本期固定利率) |
| 回购协议 Repo | ✅ Repo / Reverse Repo | ✅ Securities Lending | ✅ | ✅ Securities | 📋 **P1**(架构预留) |
| 同业存单 NCD | ✅ CD Module | ✅ Deposit | ✅ NCD | ✅ NCD | 📋 **P2**(架构预留) |
| 估值(MTM) | ✅ MTM / Fair Value | ✅ Mark-to-Market | ✅ MTM/Mark-to-Model | ✅ Mark-to-Market | 📋 **P1**(固定利率暂不估) |
| 风险敞口 ECL/IFRS 9 | ✅ Credit Adjustment | ✅ IFRS 9 ECL | ✅ CVA/DVA | ✅ IFRS 9 ECL | 📋 **M4**(对接 impairment 模块) |
| 利率衍生品 IRS | ✅ Swap Module | ✅ IRS | ✅ | ✅ Hedging | 📋 **P2**(M3 已有 IRS 模块) |
| 业务架构核心 | Deal + Event + Cashflow | Transaction + Flow + Position | Deal + Event + Cashflow | Transaction + Cash + Forecast | ✅ **共享 DealMap v2.0** |

> **核心对齐**:Open-TMS v1.0 借鉴 **FIS Quantum Interbank 模块 + Murex MM Desk** 的核心能力 —— 现金流分离 + 业务事件溯源 + 月末计提。功能范围聚焦 P0 拆借,把 FIS/SAP 的复杂能力(Pool/Repo/CDS/Swap)留至 P1/P2+ 迭代。

### 2.2 业界拆借现金流规则详解

**FIS Quantum 同业拆借标准现金流模式**:

| 类型 | 起息日现金流 | 计息期 | 到期日现金流 | 说明 |
|------|--------------|--------|--------------|------|
| O/N Lend(隔夜拆出) | -1 本金流出 | 1 天 | +1 本金+利息流入 | 本金 100w USD × 5.3% × 1/360 ≈ 147 USD |
| 7D Lend(7 天拆出) | -1 本金流出 | 7 天 | +1 本金+利息流入 | 本金 100w USD × 5.3% × 7/360 ≈ 1031 USD |
| 30D Lend | -1 本金流出 | 30 天 | +1 本金+利息流入 | 本金 100w USD × 5.3% × 30/360 ≈ 4417 USD |
| 3M Borrow | +1 本金流入(净) | 3 个月 | -1 本金流出 + -1 利息流出 | 方向相反 |

**OPEN-TMS v1.0 对齐方案**:
- 笔笔拆借在起息日生成 1 条 CF(本金方向 LEND:- / BORROW:+)
- 到期日生成 1 条 CF(本金回收 + 利息结算 共 1 条,LEND:+ 本金 + 利息,BORROW:- 本金 + 利息)
- 利息金额由 `tms_mm_deals_t.interest_calc_basis` + 实际天数差计算
- 利息计提通过 `tms_mm_interest_schedules_t` 表记录(月末按日计提)

### 2.3 利息计算公式(对标业界)

**利息 = 本金 × 利率 × (计息天数 / 年度计息基数)**

| 计息基础 | 年度基数 | 适用场景 |
|----------|---------|---------|
| **ACT/360** | 360 | 货币市场惯例(USD/EUR 拆借) |
| **ACT/365** | 365 | GBP / 部分币种 |
| **ACT/365F** | 365(固定) | AUD / CAD |
| **30/360** | 360 / 12×30 | 长期债券类 |
| **ACT/ACT** | 实际天数/实际年份 | 部分监管市场 |

**OPEN-TMS v1.0 支持**:`ACT/360` / `ACT/365` / `ACT/365F` / `30/360` / `ACT/ACT`,枚举值固定。

---

## 三、功能清单

### 3.1 同业拆借(Interbank Loan) - P0

#### 3.1.1 拆出(LEND)

```
资金调度场景:集团日终富余资金 → 拆出给银行 → 赚取短期利息
```

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 录入拆出 | 交易员选择"拆出"方向 + 银行对手方 + 期限 + 利率 + 金额,**生成 2 条 DealMap(PRINCIPAL_OUT/INTEREST_IN) + 2 条 CF** | **P0** |
| 提交 / 审批 | 录入 → 提交 → 一级审批 → (金额超阈值)二级审批 | **P0** |
| 执行(approve 后) | 起息日 -1 生成 CF 本金流出;到期日 +1 生成 CF 本金+利息流入 | **P0** |
| 利息计提 | 月末按日计提入账,在 `tms_mm_interest_schedules_t` 记录 | **P0** |
| 到期结算 | 到期日 CF 自动 Cleared / Reconciled | **P0** |
| 提前还款(对方发起) | 罚息计算 + 部分 CF 反向 | **P0** |
| 编辑 / 删除 | 已 Active 后不可修改利率;可修改备注/描述 | **P0** |
| 查询 | 按对手方/期限/日期/状态过滤 | **P0** |

#### 3.1.2 拆入(BORROW)

```
资金调度场景:头寸不足 → 从银行拆入 → 填补短期流动性
```

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 录入拆入 | 交易员选择"拆入"方向 + 银行对手方 + 期限 + 利率 + 金额,**生成 2 条 DealMap(PRINCIPAL_IN/INTEREST_OUT) + 2 条 CF** | **P0** |
| 审批流 | 同 LEND | **P0** |
| 起息日 +1 | 本金流入(到本行账户) | **P0** |
| 到期日 -1 | 本金 + 利息 支付(从本行账户) | **P0** |
| 利息计提 | 月末按日计提(应付利息) | **P0** |
| 提前还款(本方发起) | 罚息计算 + 部分 CF 反向(本方主动还款) | **P0** |
| 占用对手方授信 | 起息时占用 BORROW 金额到对手方授信额度 | **P0** |

#### 3.1.3 期限类型(本期支持)

| 期限代码 | 全称 | 天数 | 说明 |
|---------|------|------|------|
| **ON** | Overnight | 1 天 | 隔夜 |
| **TN** | Tomorrow-Next | 2 天 | 明天到后天(预留 P1) |
| **1W** | 1 Week | 7 天 | 1 周 |
| **2W** | 2 Weeks | 14 天 | 2 周 |
| **1M** | 1 Month | 30 天 | 1 个月 |
| **3M** | 3 Months | 90 天 | 3 个月 |
| **6M** | 6 Months | 180 天 | 6 个月 |
| **1Y** | 1 Year | 365 天 | 1 年 |

> **本期支持**:ON / 1W / 2W / 1M / 3M / 6M / 1Y(TN 占位预留)

> **非标准期限**:用户可手动输入起息日 + 到期日,系统自动计算天数(`term_days INT`)

#### 3.1.4 还款方式(本期默认)

| 还款方式 | 说明 | 优先级 |
|---------|------|--------|
| **BULLET** | 到期一次性还本付息(本期默认) | **P0** |
| INTEREST_ONLY | 期间只付息,到期还本 | P1 |
| AMORTIZING | 等额本息分期(P2 占位) | P2 |

### 3.2 回购协议(REPO / REVERSE REPO) - P1 占位

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 质押式回购 REPO_OUT | 押券融出资金 | P1 |
| 买断式回购 REPO_BUYOUT | 卖断+到期购回 | P1 |
| 担保物管理 | 债券/票据抵押,折扣率维护 | P1 |
| 估值与盯市 | 担保物市值监控、折扣调整 | P2 |

> **本期处理**:`event_type / dealmap_type` 枚举保留 REPO / REPO_REVERSE 字面值;`tms_mm_collaterals_t` 表本期**不创建**,字段 `collateral_id` 在 `tms_mm_deals_t` 上**预留可空**。

### 3.3 同业存单(NCD - Negotiable CD) - P2 占位

| 功能 | 说明 | 优先级 |
|------|------|--------|
| NCD 发行 | 大额可转让存单发行 | P2 |
| 流通转让 | 二级市场转让 | P2 |
| 到期兑付 | 到期日自动兑付本息 | P2 |

> **本期处理**:`deal_type` 枚举保留 NCD 字面值;`tms_mm_deals_t` 字段可承载。

### 3.4 通用交易字段(拆借特有)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| **管理主体** (`managementEntityId`) | BIGINT | Y | FK → `tms_management_entity_t.id`(沿用 FX v3.1 强类型) |
| **交易对手** (`counterpartyId`) | BIGINT | Y | 引用 `tms_counterparty_t.id`,**`counterparty_type='BANK'`** 强制校验 |
| **交易员** (`traderId`) | BIGINT | Y | 引用 `tms_trader_t.id` |
| **金融工具** (`instrumentId`) | BIGINT | Y | 引用 `tms_instrument_t.id`,**`instrument_type IN ('MM_LEND','MM_BORROW')`** |
| **方向** (`direction`) | VARCHAR(20) | Y | **本期只有 LEND / BORROW 两种**(GlobalConstants 新增) |
| **本金** (`principal`) | DECIMAL(38,18) | Y | 高精度 |
| **币种** (`currency`) | VARCHAR(10) | Y | 单币种(P1 才支持跨币种) |
| **利率** (`rate`) | DECIMAL(10,4) | Y | 百分比,如 `5.3000` = 5.30%;本期固定利率 |
| **利差** (`spreadBp`) | DECIMAL(10,4) | N | 本期:浮动基准未启用时填 0;P1 浮动利率时启用 |
| **浮动基准** (`floatingIndex`) | VARCHAR(20) | N | 本期:必须为 NULL;P1 时填 SHIBOR/LIBOR/SOFR |
| **计息基础** (`interestCalcBasis`) | VARCHAR(20) | Y | ACT_360 / ACT_365 / ACT_365F / THIRTY_360 / ACT_ACT |
| **还款方式** (`repaymentMethod`) | VARCHAR(20) | Y | 本期 BULLET(预留 INTEREST_ONLY / AMORTIZING) |
| **期限类型** (`termType`) | VARCHAR(20) | Y | ON / 1W / 2W / 1M / 3M / 6M / 1Y / CUSTOM |
| **期限天数** (`termDays`) | INT | Y | 后端自动算 = `maturityDate - valueDate` |
| **起息日** (`valueDate`) | DATE | Y | 在 `tms_deals_t` 公共表 |
| **到期日** (`maturityDate`) | DATE | Y | 在 `tms_deals_t` 公共表 = valueDate + term |
| **交易日** (`tradeDate`) | DATE | Y | 在 `tms_deals_t` 公共表 |
| **提前还款罚息率** (`earlyTerminationPenaltyRate`) | DECIMAL(10,4) | N | 百分比;NULL=不允许提前还款 |
| **担保物 ID** (`collateralId`) | BIGINT | N | P1 回购时启用,本期 NULL |
| **本金收付账户** (`principalAccountId`) | BIGINT | Y | FK → `tms_bank_account_t.id`,**通过默认账户规则自动匹配** |
| **利息收付账户** (`interestAccountId`) | BIGINT | Y | FK → `tms_bank_account_t.id` |
| **描述** (`description`) | VARCHAR(500) | N | 例:"O/N 拆出补足流动性" |
| **备注** (`remark`) | VARCHAR(500) | N | 内部备注 |

### 3.5 关键日期字段(在 `tms_deals_t` 公共表,沿用 FX v3.2)

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| **交易日** (`tradeDate`) | `tms_deals_t` | DATE | Y | 交易达成的日子 |
| **起息日** (`valueDate`) | `tms_deals_t` | DATE | Y | **关键**:资金实际流出/流入日 |
| **到期日** (`maturityDate`) | `tms_deals_t` | DATE | Y | 到期结算日 = valueDate + term |
| **期限(天数)** (`termDays`) | `tms_deals_t` | INT | Y | **后端自动算** = `maturityDate - valueDate` |

**业务规则**:
- 拆借交易日通常 = 起息日(拆借是"今天签约明天起息"的实务,但允许交易日 ≤ 起息日)
- 起息日不能早于交易日(P1+ 预约拆借才允许 > 起息日,本期不允许)
- 到期日必须 > 起息日(整数天)
- `termDays = maturityDate - valueDate`(后端校验,与 `termType` 编码对照)

### 3.6 利息计算(后端 `calculate` 接口)

**目的**:与 FX v3.2 一致,所有计算由后端封装,前端**只录入** + 触发计算。

#### 3.6.1 后端 calculate 接口

```
POST /api/v1/dealing/mm-deals/calculate
Content-Type: application/json
```

**Request**(用户已填的):
```json
{
  "principal":  1000000.00,         // 本金(可选)
  "rate":       5.3000,             // 利率 % (可选)
  "termType":   "ON",                // 期限类型(可选:ON/1W/1M/CUSTOM)
  "termDays":   1,                   // 期限天数(可选)
  "interestCalcBasis": "ACT_360",   // 计息基础(可选)
  "valueDate":  "2026-07-06",        // 起息日(可选)
  "maturityDate": "2026-07-07"      // 到期日(可选)
}
```

> 用户至少填 2 个本金/利率/期限字段,后端基于已有字段推算未填字段。

**Response**(后端补全后):
```json
{
  "principal":  1000000.00,
  "rate":       5.3000,
  "termType":   "ON",
  "termDays":   1,
  "interestCalcBasis": "ACT_360",
  "valueDate":  "2026-07-06",
  "maturityDate": "2026-07-07",
  "interestAmount": 147.22,         // ★ 后端自动算 = principal × rate × termDays / 36500 (ACT/365)
                                    //                  = 1000000 × 5.3% × 1 / 360 = 147.22 (ACT/360)
  "dayCountFactor": 0.002777,        // 日计息因子(便于前端显示)
  "totalRepayAmount": 1000147.22    // 本金 + 利息(LEND 视角)
}
```

**后端计算规则**(单一可信源):

| 公式 | 用途 |
|------|------|
| `maturityDate = valueDate + termDays`(若 termType 是 ON/1W 等固定值则先转为天数) | 到期日算 |
| `termDays = maturityDate - valueDate`(若用户只给起息日+到期日) | 期限天数算 |
| `interestAmount = principal × rate × termDays / yearFactor`(按计息基础) | 利息金额算 |
| `totalRepayAmount = principal + interestAmount`(LEND) | 还款总额 |
| `dayCountFactor = termDays / yearFactor` | 日计息因子(报表展示用) |

**yearFactor 映射**:

| interestCalcBasis | yearFactor | 说明 |
|-------------------|------------|------|
| ACT_360 | 360 | 实际天数 / 360 |
| ACT_365 | 365 | 实际天数 / 365 |
| ACT_365F | 365 | 同 ACT_365 但不区分闰年 |
| THIRTY_360 | 360 | 30×12=360,通用 |
| ACT_ACT | 实际年份(闰年 366 / 平年 365) | IFRS 标准 |

**前端调用方式**(节流 300ms,与 FX v3.2 一致):
```js
import { debounce } from 'lodash'
const onFieldChange = debounce((form) => {
  const result = await calculateMmDeal(form)
  Object.assign(form, result)  // 覆盖空白字段
}, 300)
```

**错误码**:
- 400 `INPUT_INSUFFICIENT`:用户填的字段不足 2 个
- 400 `VALUE_INCONSISTENT`:用户填的字段互相矛盾
- 422 `DATE_INVALID`:起息日 > 到期日
- 422 `TERM_INVALID`:到期日 - 起息日 不等于 termType 推断

### 3.7 利息计提(Accrual)

**目的**:**月末按日计提**,符合 IFRS / US GAAP 利息确认规则。

#### 3.7.1 计提规则

| # | 规则 | 说明 |
|---|------|------|
| AC1 | 按月计提 | 每月最后一天,系统生成当月计提记录 |
| AC2 | 按日计算 | 计提金额 = 本金 × 利率 × 当月实际占用天数 / yearFactor |
| AC3 | 未到期的拆出 | 应收利息 = 累计计提(应收方向 Inflow) |
| AC4 | 未到期的拆入 | 应付利息 = 累计计提(应付方向 Outflow) |
| AC5 | 到期日当天 | 不计提(到期日本金结算日,已包含全天利息) |
| AC6 | 错月调整 | 系统自动计算"上月最后一天 → 本月最后一天"的天数差 |

#### 3.7.2 计提表(本期创建)

**`tms_mm_interest_schedules_t`**:记录每月计提金额(详见 4.3 节)。

#### 3.7.3 计提接口

```
POST /api/v1/dealing/mm-deals/calculate-accrual
Content-Type: application/json
```

**Request**:
```json
{
  "scheduledDate": "2026-07-31"  // 计提日
}
```

**Response**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "accrualCount": 5,         // 本月处理 5 笔 MM 交易
    "accrualSchedules": [
      {
        "dealNumber": "MM202607050001",
        "direction": "LEND",
        "principal": 1000000.00,
        "rate": 5.3000,
        "interestCalcBasis": "ACT_360",
        "accruedDays": 26,      // 7月 实际占用 26 天(避开起息日首日)
        "accrualAmount": 3827.78,
        "currency": "CNY",
        "interestAccount": "1050 CNY"
      }
    ]
  }
}
```

> **本期实现**:基础计提(按月);**P1+** 计划月底自动调度 + 报表生成。

### 3.8 提前还款(Early Termination)

**目的**:对方主动还款(LEND) 或 本方主动还款(BORROW) 时,**罚息计算** + 部分 CF 反向。

#### 3.8.1 罚息规则

| # | 规则 | 说明 |
|---|------|------|
| ET1 | 必须填 `earlyTerminationPenaltyRate` | NULL = 不允许提前还款 |
| ET2 | 罚息金额 = 提前还款本金 × `earlyTerminationPenaltyRate` × (剩余天数 / yearFactor) | 罚息按比例 |
| ET3 | 对方发起(LEND 视角) | 本金部分回流 + 罚息收款 |
| ET4 | 本方发起(BORROW 视角) | 本金部分支付 + 罚息支付 |
| ET5 | 已付出/已收利息不退 | **不冲销历史计提** |

#### 3.8.2 提前还款接口

```
POST /api/v1/dealing/mm-deals/{dealNumber}/early-terminate
Content-Type: application/json
```

**Request**:
```json
{
  "earlyTerminationDate": "2026-07-30",   // 实际提前还款日
  "earlyTerminationAmount": 500000.00,    // 提前还款本金金额(可部分)
  "penaltyRate": 0.5000,                  // 罚息率(本期允许覆盖表中值)
  "operator": "trader01"
}
```

**业务效果**:
1. INSERT 1 行 `tms_actions_t`(action_type='EARLY_TERMINATE',action_status='Pending')
2. 待审批(EARLY_TERMINATE 是 P0 简化为"业务审批"流)
3. 审批通过后:
   - 生成 1 行 DealMap(`MM_EARLY_TERMINATE`,记录本金反向 + 罚息)
   - 生成反向 CF(实际还款日) + 罚息 CF(同方向)
   - 更新 `tms_mm_deals_t.principal_remaining`(用于多期提前还款;本期默认未还本金不变,后续 v1.1 支持多次部分还款)

**Response**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "MM202607050001",
    "penaltyAmount": 458.33,
    "earlyTerminationCashflowNumber": "CF202607300001",
    "penaltyCashflowNumber": "CF202607300002"
  }
}
```

---

## 四、字段设计

### 4.1 MM 特性主表:`tms_mm_deals_t`(共享主键版)

> **沿用 FX v3.2 设计**:`tms_mm_deals_t.id = tms_deals_t.id`,无独立 PK;通用字段在 `tms_deals_t`。

```sql
-- ★ 共享主键,与 FX v3.2 风格一致
CREATE TABLE tms_mm_deals_t (
    -- ★ 共享主键(物理上是 PK,语义上 = tms_deals_t.id)
    id                   BIGINT          NOT NULL PRIMARY KEY,
    deal_number          VARCHAR(50)     NOT NULL UNIQUE,         -- MM + yyyyMMdd + 4位

    -- ★ 通用字段(strong FK 强类型,与 FX v3.1 对齐)
    management_entity_id BIGINT          NOT NULL,                 -- FK → tms_management_entity_t.id

    -- ★ MM 特有:交易方向(LEND 拆出 / BORROW 拆入)
    direction            VARCHAR(20)     NOT NULL,                 -- LEND / BORROW
    -- 校验:direction IN ('LEND', 'BORROW', 'REPO_OUT', 'REPO_IN', 'NCD')
    -- 本期:active only LEND / BORROW;REPO / NCD P1/P2 占位

    -- ★ MM 特有:本金币种(本期单币种,P1+ 支持跨币种)
    currency             VARCHAR(10)     NOT NULL,                 -- 本金币种,如 CNY / USD / HKD

    -- ★ MM 核心:本金 + 利率
    principal            DECIMAL(38,18)  NOT NULL,                 -- 本金,高精度
    rate                 DECIMAL(10,4)   NOT NULL,                 -- 利率(百分比,如 5.30 = 5.30%)
    rate_type            VARCHAR(20)     NOT NULL DEFAULT 'Fixed', -- Fixed / Floating (P1: Floating)
    spread_bp            DECIMAL(10,4)   NOT NULL DEFAULT 0,       -- 利差(bp);P1 浮动利率启用
    floating_index       VARCHAR(20),                              -- SHIBOR / LIBOR / SOFR (P1)
    rate_reset_date      DATE,                                      -- P1 浮动利率的重置日

    -- ★ MM 核心:利率计算基础
    interest_calc_basis  VARCHAR(20)     NOT NULL,                 -- ACT_360 / ACT_365 / ACT_365F / THIRTY_360 / ACT_ACT

    -- ★ MM 核心:还款方式 + 期限
    repayment_method     VARCHAR(20)     NOT NULL DEFAULT 'BULLET',-- BULLET / INTEREST_ONLY / AMORTIZING
    term_type            VARCHAR(20)     NOT NULL,                 -- ON / 1W / 2W / 1M / 3M / 6M / 1Y / CUSTOM
    term_days            INT             NOT NULL,                 -- 后端自动算 = maturityDate - valueDate

    -- ★ MM 核心:日期字段(在公共表 tms_deals_t 也有,这里冗余便于查询)
    trade_date           DATE            NOT NULL,                 -- 交易日(签约日)
    value_date           DATE            NOT NULL,                 -- 起息日(资金实际流动日)
    maturity_date        DATE            NOT NULL,                 -- 到期日

    -- ★ MM 核心:本金账户 + 利息账户
    principal_account_id BIGINT          NOT NULL,                 -- FK → tms_bank_account_t.id (本金收付账户)
    interest_account_id  BIGINT          NOT NULL,                 -- FK → tms_bank_account_t.id (利息收付账户)

    -- ★ MM 核心:利息计算结果(后端算完填入,持久化便于查询)
    accrued_interest     DECIMAL(38,18)  NOT NULL DEFAULT 0,       -- 累计已计提利息(每月底更新)
    total_interest       DECIMAL(38,18)  NOT NULL DEFAULT 0,       -- 全期总利息(创建时一次性算)
    day_count_factor     DECIMAL(18,10),                           -- 日计息因子 = termDays / yearFactor

    -- ★ MM 特有:提前还款
    early_termination_penalty_rate DECIMAL(10,4),                  -- 罚息率(百分比);NULL=不允许提前还款
    early_termination_allowed      CHAR(1) NOT NULL DEFAULT '1',   -- '0'不允许 '1'允许

    -- ★ 预留:P1 回购 / 担保物
    collateral_id        BIGINT,                                   -- P1 占位:FK → tms_collateral_t.id (本期 NULL)
    collateral_type      VARCHAR(20),                              -- BOND / NOTE / UNDEFINED (预留)

    -- ★ 描述
    description          VARCHAR(500),
    remark               VARCHAR(500),

    -- ★ 审计(项目强制)
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    -- ★ 约束(本期最小集,instrument 灵活)
    CONSTRAINT chk_mm_direction CHECK (direction IN (
        'LEND','BORROW','REPO_OUT','REPO_IN','NCD'
    )),
    CONSTRAINT chk_mm_rate_type CHECK (rate_type IN ('Fixed', 'Floating')),
    CONSTRAINT chk_mm_basis CHECK (interest_calc_basis IN (
        'ACT_360','ACT_365','ACT_365F','THIRTY_360','ACT_ACT'
    )),
    CONSTRAINT chk_mm_term_type CHECK (term_type IN (
        'ON','TN','1W','2W','1M','3M','6M','1Y','CUSTOM'
    )),
    CONSTRAINT chk_mm_repayment CHECK (repayment_method IN (
        'BULLET','INTEREST_ONLY','AMORTIZING'
    )),
    CONSTRAINT chk_mm_principal CHECK (principal > 0),
    CONSTRAINT chk_mm_rate CHECK (rate >= 0 AND rate <= 100),
    CONSTRAINT chk_mm_term_days CHECK (term_days >= 1 AND term_days <= 366),
    CONSTRAINT chk_mm_early_termination CHECK (early_termination_allowed IN ('0','1'))
);

-- ★ 索引(查询性能)
CREATE INDEX idx_mmd_mgmt_entity       ON tms_mm_deals_t(management_entity_id);
CREATE INDEX idx_mmd_counterparty      ON tms_mm_deals_t(direction, principal_account_id);
CREATE INDEX idx_mmd_currency          ON tms_mm_deals_t(currency);
CREATE INDEX idx_mmd_value_date        ON tms_mm_deals_t(value_date);
CREATE INDEX idx_mmd_maturity_date     ON tms_mm_deals_t(maturity_date);
CREATE INDEX idx_mmd_term_days         ON tms_mm_deals_t(term_days);
CREATE INDEX idx_mmd_status_via_deals  ON tms_mm_deals_t(id);  -- 状态走 tms_deals_t.status

COMMENT ON TABLE tms_mm_deals_t IS '货币市场交易特性表 v1.0(共享 tms_deals_t.id,基于 FX v3.2 风格)';
COMMENT ON COLUMN tms_mm_deals_t.id          IS '★ 共享主键,值=tms_deals_t.id';
COMMENT ON COLUMN tms_mm_deals_t.direction   IS 'LEND(拆出)/BORROW(拆入)/REPO_OUT/REPO_IN/NCD (本期只用 LEND/BORROW)';
COMMENT ON COLUMN tms_mm_deals_t.principal   IS '本金,高精度 DECIMAL(38,18)';
COMMENT ON COLUMN tms_mm_deals_t.rate        IS '利率(百分比数值,如 5.30 代表 5.30%)';
COMMENT ON COLUMN tms_mm_deals_t.rate_type   IS 'Fixed(本期默认)/Floating(P1)';
COMMENT ON COLUMN tms_mm_deals_t.interest_calc_basis IS 'ACT_360/ACT_365/ACT_365F/THIRTY_360/ACT_ACT';
COMMENT ON COLUMN tms_mm_deals_t.term_type   IS 'ON/1W/2W/1M/3M/6M/1Y/CUSTOM';
COMMENT ON COLUMN tms_mm_deals_t.collateral_id IS 'P1 占位字段,本期 NULL';
COMMENT ON COLUMN tms_mm_deals_t.accrued_interest IS '累计已计提利息(每月底更新,月初清零)';
```

### 4.2 公共主表:`tms_deals_t`(与 FX 共享)

**沿用 FX v3.2 风格 + 新增字段**。`tms_deals_t` 已有字段:

- `id / deal_number / deal_type / business_unit / counterparty_id / instrument_id / trader_id`
- `direction / amount / currency / deal_date / value_date / status / latest_action_number`
- `description / remark / 审计字段`

**本期 MM 需新增字段**(若尚未存在):

```sql
-- 1. tms_deals_t: 加 trade_date 字段(FX v3.2 已加,这里只为完整)
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS trade_date DATE;
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS maturity_date DATE;
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS term_days INT;

-- 2. 注释更新
COMMENT ON COLUMN tms_deals_t.deal_type IS 'AC / AT / FX / MM / DEPOSIT / LOAN';
COMMENT ON COLUMN tms_deals_t.trade_date IS '交易日 v3.2(MM/FX 通用)';
COMMENT ON COLUMN tms_deals_t.maturity_date IS '到期日 v3.2(= value_date + term)';
COMMENT ON COLUMN tms_deals_t.term_days IS '期限天数 v3.2(后端算)';
```

### 4.3 利息计提表:`tms_mm_interest_schedules_t`

```sql
CREATE TABLE tms_mm_interest_schedules_t (
    -- 主键
    id                   BIGSERIAL       PRIMARY KEY,
    schedule_number      VARCHAR(50)     NOT NULL UNIQUE,    -- ACS+yyyyMMdd+序号

    -- 关联交易
    deal_number          VARCHAR(50)     NOT NULL,            -- FK 引用 tms_mm_deals_t.deal_number
    direction            VARCHAR(20)     NOT NULL,            -- 冗余:便于查询 Inflow/Outflow
    currency             VARCHAR(10)     NOT NULL,            -- 冗余:便于查询

    -- ★ 计提期间
    accrual_period_start DATE            NOT NULL,            -- 计提期开始日
    accrual_period_end   DATE            NOT NULL,            -- 计提期结束日(月末)
    accrual_days         INT             NOT NULL,            -- 当月实际占用天数

    -- ★ 计提金额
    principal            DECIMAL(38,18)  NOT NULL,            -- 当期本金(冗余,跨期时本金变化)
    rate                 DECIMAL(10,4)   NOT NULL,            -- 当期利率(冗余,浮动利率跨期变化)
    interest_calc_basis  VARCHAR(20)     NOT NULL,            -- 当期计息基础(冗余)
    accrued_interest     DECIMAL(38,18)  NOT NULL,            -- 当期计提金额 = 本金 × 利率 × accrual_days / yearFactor

    -- ★ 状态
    status               VARCHAR(20)     NOT NULL DEFAULT 'Posted',  -- Posted / Reversed / Adjusted

    -- ★ 跨期调整
    is_adjustment        CHAR(1)         NOT NULL DEFAULT '0',       -- 是否错月调整
    reversal_schedule_id BIGINT,                                       -- 关联反向计提

    -- ★ 描述
    description          VARCHAR(500),
    remark               VARCHAR(500),

    -- ★ 审计
    created_by           VARCHAR(50)     NOT NULL,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50),
    updated_at           TIMESTAMP,
    version              INT             NOT NULL DEFAULT 0,
    deleted              CHAR(1)         NOT NULL DEFAULT '0',

    CONSTRAINT chk_accrual_direction CHECK (direction IN ('LEND','BORROW')),
    CONSTRAINT chk_accrual_status CHECK (status IN ('Posted','Reversed','Adjusted')),
    CONSTRAINT chk_accrual_days CHECK (accrual_days >= 0),
    CONSTRAINT chk_accrual_amount CHECK (accrued_interest >= 0)
);

CREATE INDEX idx_mis_deal_number      ON tms_mm_interest_schedules_t(deal_number);
CREATE INDEX idx_mis_period_end       ON tms_mm_interest_schedules_t(accrual_period_end);
CREATE INDEX idx_mis_status           ON tms_mm_interest_schedules_t(status);
CREATE INDEX idx_mis_direction        ON tms_mm_interest_schedules_t(direction);

COMMENT ON TABLE tms_mm_interest_schedules_t IS 'MM 利息计提表(每月按日计提记录)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.schedule_number IS '计提编号 格式 ACS+yyyyMMdd+序号';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrual_period_start IS '计提期开始日(本月起息日或月初)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrual_period_end IS '计提期结束日(本月最后一天)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrual_days IS '当月实际占用天数(首日不计数)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrued_interest IS '当期计提金额(单位:本金币种)';
```

### 4.4 担保物表(预留 P1 - 本期不创建)

> **本期不创建** `tms_mm_collaterals_t`,仅在 `tms_mm_deals_t` 上保留 `collateral_id` 字段。P1 启用。

### 4.5 字段详细表

| 字段 | DB 列 | Java 类型 | 前端类型 | 必填 | 默认 | 说明 |
|------|-------|----------|---------|------|------|------|
| 共享主键 | `id` | `Long` | - | Y | = tms_deals_t.id | DB PK |
| 业务编号 | `deal_number` | `String` | - | Y | 系统生成 | MM + yyyyMMdd + 4 位流水 |
| **管理主体** | `management_entity_id` | `Long` | BaseDataPicker | Y | - | 强 FK,自动带出账户 |
| **交易方向** | `direction` | `String` | Radio | Y | - | LEND / BORROW;GlobalConstants 新增 |
| **本金** | `principal` | `BigDecimal` | InputNumber | Y | - | 高精度 38,18 |
| **币种** | `currency` | `String` | Select | Y | - | ISO 4217 |
| **利率(%)** | `rate` | `BigDecimal` | InputNumber | Y | - | 0-100; 后端联动 |
| **利率类型** | `rate_type` | `String` | Radio | Y | Fixed | Fixed(本期)/ Floating(P1) |
| **利差(bp)** | `spread_bp` | `BigDecimal` | InputNumber | N | 0 | P1 浮动利率启用 |
| **浮动基准** | `floating_index` | `String` | Select | N | NULL | P1 启用,SHIBOR/LIBOR/SOFR |
| **重置日** | `rate_reset_date` | `LocalDate` | DatePicker | N | NULL | P1 浮动利率启用 |
| **计息基础** | `interest_calc_basis` | `String` | Select | Y | ACT_360 | 5 种枚举 |
| **还款方式** | `repayment_method` | `String` | Select | Y | BULLET | 本期 BULLET |
| **期限类型** | `term_type` | `String` | Select | Y | - | ON/1W/2W/1M/3M/6M/1Y/CUSTOM |
| **期限天数** | `term_days` | `Integer` | InputNumber | Y | 后端算 | = maturityDate - valueDate |
| **交易日** | `trade_date` | `LocalDate` | DatePicker | Y | today | tms_deals_t.trade_date |
| **起息日** | `value_date` | `LocalDate` | DatePicker | Y | - | tms_deals_t.value_date |
| **到期日** | `maturity_date` | `LocalDate` | DatePicker | Y | - | tms_deals_t.maturity_date |
| **本金账户** | `principal_account_id` | `Long` | BaseDataPicker | Y | 默认规则 | 自动带出,可改 |
| **利息账户** | `interest_account_id` | `Long` | BaseDataPicker | Y | 默认规则 | 自动带出,可改 |
| **累计已计提利息** | `accrued_interest` | `BigDecimal` | Text(只读) | Y | 0 | 月底更新 |
| **总利息** | `total_interest` | `BigDecimal` | Text(只读) | Y | 后端算 | 创建时算 |
| **日计息因子** | `day_count_factor` | `BigDecimal` | Text(只读) | N | 后端算 | 报表展示用 |
| **提前还款罚息率(%)** | `early_termination_penalty_rate` | `BigDecimal` | InputNumber | N | NULL | NULL=不允许 |
| **允许提前还款** | `early_termination_allowed` | `String` | Switch | Y | Y | '0' / '1' |
| **担保物 ID** | `collateral_id` | `Long` | Select | N | NULL | P1 占位 |
| **担保物类型** | `collateral_type` | `String` | Select | N | NULL | P1 占位 |
| 描述 | `description` | `String` | Textarea | N | - | 业务说明 |
| 备注 | `remark` | `String` | Textarea | N | - | 内部备注 |
| 审计字段 | created_by/created_at/updated_by/updated_at/version/deleted | - | - | Y | - | 项目强制审计 |

### 4.6 编号生成规则

**`deal_number` 格式**:`MM + yyyyMMdd + 4 位流水`

例:`MM202607050001`、`MM202607050002`、`MM202607060001`

**生成方式**:同交易日下递增,跨日重置。沿用 `GlobalConstants.SerialNumberGenerator`。

### 4.7 全局枚举值(沿用 + 新增)

| 枚举 | 取值 | 来源 | 说明 |
|------|------|------|------|
| 交易类型 DealType | `AC / AT / FX / MM / DEPOSIT / LOAN` | GlobalConstants | 本期新增 `MM` |
| 方向 Direction | `Inflow / Outflow` | GlobalConstants.M1 已加 | 通用收付(账户匹配时用) |
| **MM 方向** | `LEND / BORROW` | **GlobalConstants 新增** | MM 业务方向 |
| MM 预留方向 | `REPO_OUT / REPO_IN / NCD` | GlobalConstants 占位 | P1/P2 |
| 利率类型 RateType | `Fixed / Floating` | GlobalConstants 新增 | |
| 计息基础 InterestCalcBasis | `ACT_360 / ACT_365 / ACT_365F / THIRTY_360 / ACT_ACT` | **GlobalConstants 新增** | |
| 期限类型 TermType | `ON / TN / 1W / 2W / 1M / 3M / 6M / 1Y / CUSTOM` | **GlobalConstants 新增** | |
| 还款方式 RepaymentMethod | `BULLET / INTEREST_ONLY / AMORTIZING` | **GlobalConstants 新增** | |
| 浮动基准 FloatingIndex | `SHIBOR / LIBOR / SOFR / HIBOR` | GlobalConstants 新增 | P1 启用 |
| 状态 Status | `New / Submitted / Approved / Active / Matured / Settled / Canceled / Overdue` | 沿用 | 本期: New/Approved/Active/Matured/Settled/Canceled |

---

## 五、业务规则

### 5.1 通用规则(MM 通用)

| # | 规则 | 说明 |
|---|------|------|
| **R1** | 利率必须 ≥ 0 且 ≤ 100(百分比) | DB CHECK |
| **R2** | 本金必须 > 0 | DB CHECK |
| **R3** | 期限天数 ∈ [1, 366] | DB CHECK,排除 0 天和长期(本期上限 1Y) |
| **R4** | 利率类型 Fixed 时,`spread_bp / floating_index / rate_reset_date` 必须为 NULL | 应用层校验 |
| **R5** | 利率类型 Floating 时(P1),`floating_index` 必填 | P1 启用 |
| **R6** | 到期日必须 > 起息日 | DB CHECK |
| **R7** | 交易日期 ≤ 起息日(本期不允许预约拆借) | 应用层校验 |
| **R8** | `principal_account_id` 和 `interest_account_id` 必须属于 `management_entity_id` | 应用层校验 |
| **R9** | 跨币种拆借(P1)需要指定汇率 + FX 关联 | 本期不支持 |
| **R10** | 对手方 `counterparty.type` 必须是 `BANK`(银行类) | 应用层校验,防止与非银行做拆借 |
| **R11** | 金融工具 `instrument.instrument_type` 必须是 `MM_LEND` 或 `MM_BORROW` | 应用层校验 |
| **R12** | 编号 `MM{yyyyMMdd}{4位流水}` | 同日递增,跨日重置 |
| **R13** | 字段命名严格 snake_case(DB)/ camelCase(Java) | 项目规范 |
| **R14** | 审计字段全表必备 | 项目强制 |
| **R15** | 状态用 VARCHAR(20) | 项目规范 |

### 5.2 拆出(LEND)特有规则

| # | 规则 | 说明 |
|---|------|------|
| **L1** | 现金流方向:起息日 -1 本金流出,到期日 +1 本金+利息流入 | DEAL 触发时按 DealMap 计算 |
| **L2** | 利息计提方向 Inflow(应收) | `tms_mm_interest_schedules_t.direction='LEND'` |
| **L3** | 占用对手方授信额度:起息日 BORROW 金额(LEND 视角是"我方借给对方",占用对方对本公司的授信) | P1 对接 limit 模块 |
| **L4** | 利息账户(Inflow)与本金账户(Inflow)可以不同账户 | 实务中:本金回主账户,利息回利息收入户 |
| **L5** | 提前还款:对方主动,本方被动 | 生成反向 CF(对方把本金 + 罚息转回给我方) |

### 5.3 拆入(BORROW)特有规则

| # | 规则 | 说明 |
|---|------|------|
| **B1** | 现金流方向:起息日 +1 本金流入,到期日 -1 本金+利息流出 | 与 LEND 反向 |
| **B2** | 利息计提方向 Outflow(应付) | `direction='BORROW'` |
| **B3** | 占用对手方授信:起息日 BORROW 金额占用本公司对银行的授信(LEND 视角) | P1 对接 limit 模块 |
| **B4** | 利息账户(Outflow)从本行利息支出户 | 实务中:利息支出户专门核算 |
| **B5** | 提前还款:本方主动,本方发起审批 | 生成本金反向 + 罚息(同方向,本方支付罚息) |
| **B6** | BORROW 必须过审批(借贷金额大) | 默认走 1 级审批,≥ 5000万 等值 走 2 级审批(具体阈值由审批流配置) |

### 5.4 利率与计算规则

| # | 规则 | 说明 |
|---|------|------|
| **IR1** | 固定利率:期内不变 | 本期实现 |
| **IR2** | 浮动利率:每个 reset 日按基准利率 + 利差重新计算 | P1 占位 |
| **IR3** | 利率变更:已 Active 的交易不允许修改利率 | 状态机保护 |
| **IR4** | `total_interest = principal × rate × term_days / yearFactor`(计息基础对应) | DEAL 创建时算 |
| **IR5** | 利息精度:8 位小数(JS 显示 4 位) | 后端 calculate 算 |
| **IR6** | 计息基础合法性:5 个枚举值之一 | DB CHECK |
| **IR7** | 跨期计提:不同月份按实际天数重新算 | 月末计提任务 |
| **IR8** | 计提基期:`accrual_period_start = max(value_date, monthly_first_day)` | P1 实现 |

### 5.5 现金流规则(对齐 FX v3.2)

| # | 规则 | 说明 |
|---|------|------|
| **CF1** | DEAL Action 触发:生成 2 行 DealMap(PRINCIPAL / INTEREST)+ 2 条 Cashflow(本金 CF + 利息 CF 指示) | 沿用 1 行 DealMap → 1 条 CF |
| **CF2** | 起息日 CF:`value_date +/- 1`(按 LEND/BORROW 方向) | 实际操作日 = 起息日 |
| **CF3** | 到期日 CF:`maturity_date`(本金 + 利息合并一条 OR 分开) | 本期合并为到期日本金 CF + 当天利息 CF,合并提交 |
| **CF4** | CF 上存 `dealmap_number`(1:0/1 强引用) | 沿用 FX v3.2 |
| **CF5** | CF direction(LEND:-本金/+利息;BORROW:+本金/-利息) | 状态计算 |
| **CF6** | CF currency = MM `currency`,本金/利息不拆分多币种 | 本期单币种 |
| **CF7** | CF `cflow_date` = 实际操作日(银行工作日调整) | P1 工作日调整;本期:日历日 |
| **CF8** | CF `value_date` = 操作的"价值日" | 通常 = cflow_date |

### 5.6 利息计提(Accrual)规则

| # | 规则 | 说明 |
|---|------|------|
| **AC1** | 按月计提 | 每月最后一天触发 |
| **AC2** | 起息日不在当月:不计提 | 跨月后从下月开始 |
| **AC3** | 当月最后一天 = 到期日:按实际天数计提最后一段 + 到期日合并结算 | 与 CF 一致 |
| **AC4** | `accrued_interest` 每次计提累加,月初清零(在计算累计时考虑期初值) | 数据库字段语义"累计";累计基础 = 之前所有计提 + 本次 |
| **AC5** | 计提金额精度:8 位小数(同 FX 利率) | |
| **AC6** | 计提状态机:Posted → Reversed(撤回时)→ Adjusted(错月调整) | |
| **AC7** | 计提失败:对账时自动补提 | P1+ 自动化 |

### 5.7 提前还款(Early Termination)规则

| # | 规则 | 说明 |
|---|------|------|
| **ET1** | `early_termination_allowed='1'` 才允许提前还款 | |
| **ET2** | `early_termination_penalty_rate` 必填 | 否则不允许 |
| **ET3** | 罚息计算:`penalty_amount = principal × penalty_rate × remaining_days / yearFactor` |  |
| **ET4** | 已计提利息不退还(会计准则) | |
| **ET5** | 提前还款必须审批(2 级:金额超阈值) | |
| **ET6** | 部分提前还款:本期支持;剩余本金按原到期日继续 | 计提继续累积 |
| **ET7** | 全额提前还款:Status 转为 Matured(或 Settled) | |
| **ET8** | 跨期合并:已计提 + 当期计提 + 罚息 = 实际结算金额 | P1+ 报表 |

### 5.8 状态机规则

| # | 规则 | 说明 |
|---|------|------|
| **S1** | New(已创建但未提交) → Submitted(待审批) → Approved(已批准) | 标准流程 |
| **S2** | Approved(批准) → Active(已起息,本金已流) | DEAL Action 触发 |
| **S3** | Active → Matured(到期) | 到期日 CF Cleared 后 |
| **S4** | Matured → Settled(完全结算) | 利息 CF 也 Cleared |
| **S5** | Active → Canceled | 审批撤回/异常撤销 |
| **S6** | Approved 后 BORROW 可占用对手方额度(本方) | P1+ 集成 limit |
| **S7** | 不允许 New → Direct Active | 必须经 Approved |
| **S8** | Settled 不可再操作 | 终态 |
| **S9** | Active → Overdue(逾期,P1) | P1 占位 |

### 5.9 Action 规则(MM)

| Action 类型 | 说明 | 触发 |
|------------|------|------|
| `CREATE` | 创建 MM 交易 | 录入保存 |
| `UPDATE` | 修改 MM 交易(已 Active 后只允许改备注) | 编辑保存 |
| `DELETE` | 删除/撤销(软删) | 手工撤销(仅 New / Approved 状态) |
| `SUBMIT` | 提交审批 | 录入完成后提交 |
| `APPROVE` | 审批通过 | 审批人 |
| `REJECT` | 审批驳回 | 审批人 |
| `EXECUTE` | 执行(Approved → Active) | 审批通过后 |
| `MATURE` | 到期 | 自动触发(系统调度) |
| `SETTLE` | 结算 | 现金流 Cleared 后 |
| `CALC_ACCRUAL` | 计提利息 | 月末调度 |
| `EARLY_TERMINATE` | 提前还款 | 手工触发(需审批) |

> **MM 与 FX Action 差异**:MM 因借贷性质,需要完整 **SUBMIT / APPROVE / REJECT / EXECUTE** 流程;FX 因即期/远期直接入市,无审批流(v3.2 设计)。

---

## 六、业务流程

### 6.1 拆出(LEND)完整流程

```
                 录入                 提交               审批              执行                到期               计提(月末)
                  │                   │                 │                │                  │                    │
                  ▼                   ▼                 ▼                ▼                  ▼                    ▼
            ┌─────────┐         ┌──────────┐       ┌──────────┐    ┌──────────┐        ┌──────────┐        ┌──────────┐
            │   New   │────────>│Submitted │──────>│ Approved │───>│  Active  │───────>│  Matured │───────>│  Settled │
            └─────────┘         └──────────┘       └──────────┘    └──────────┘        └──────────┘        └──────────┘
                  │                  │                  │                │                  │
                  │                  │                  │                │  (到期日 CF       │
                  │                  │ Reject           │                │   Cleared)        │
                  │                  ▼                  │                │                   │
                  │            ┌──────────┐             │                │                   │
                  │            │ Rejected │             │                │                   │
                  │            └──────────┘             │                │                   │
                  │                                     │                │                   │
                  │ DELETE (撤销)                       │                │                   │
                  └────────────────────>───────────────┴────────────────┴───────────────────┘
                                          Canceled (软删,deletes='1')
```

**详细步骤**:

1. **录入(NEW)**
   - 交易员填字段(管理主体/对手方/本金/利率/期限/账户)
   - 选账户时触发 `match` 接口(主体默认银行账户规则),自动带出
   - 保存 → INSERT `tms_deals_t`(deal_type='MM')+ `tms_mm_deals_t` + `tms_actions_t`(CREATE)

2. **提交(SUBMITTED)**
   - 触发 `tms_actions_t.action_type='SUBMIT'`,`action_status='Pending'`
   - 状态:`tms_deals_t.status='Submitted'`

3. **审批(APPROVED / REJECTED)**
   - 一级审批(默认):金额 ≤ 5000 万(默认阈值,可配置)
   - 二级审批(可选):金额 > 阈值,需 2 个审批人
   - APPROVE → status='Approved'
   - REJECT → status='Rejected'(可编辑后重新 SUBMIT)

4. **执行(ACTIVE)**
   - 审批通过后,系统调度或手工触发 EXECUTE
   - 生成 2 行 `tms_deal_map_t`(`MM_PRINCIPAL_OUT` + `MM_INTEREST_IN`)+ 2 条 `tms_cashflow_t`
     - CF1:起息日 value_date,本金 Outflow 1000000 CNY(cflow_date=value_date)
     - CF2:到期日 maturity_date,本金 + 利息 Inflow 10000147.22 CNY(cflow_date=maturity_date)
   - 状态:`Active`
   - INSERT `tms_actions_t.action_type='EXECUTE'`

5. **计提(月末)**
   - 每月最后一天触发 `tms_mm_interest_schedules_t` 写入
   - 系统调度(或手工):`POST /api/v1/dealing/mm-deals/calculate-accrual?scheduledDate=2026-07-31`
   - 生成本期应收利息记录
   - 累计 `accrued_interest` 字段更新

6. **到期(MATURED)**
   - 到期日当日系统调度检查 CF 状态
   - CF Cleared 后状态转 `Matured`
   - INSERT `tms_actions_t.action_type='MATURE'`

7. **结算(SETTLED)**
   - 利息 CF 也 Cleared 后转 `Settled`
   - INSERT `tms_actions_t.action_type='SETTLE'`

### 6.2 拆入(BORROW)完整流程

与 LEND 类似,差异:

- 现金流方向相反(起息 +1 / 到期 -1)
- BORROW 必须过审批(MM 借贷通常有授信审批)
- 利息方向为 Outflow(应付)

### 6.3 利息计提流程

```
┌────────────────────────────────────────────┐
│  1. 月末最后一天(23:00 系统调度)             │
│     ↓                                       │
│  2. 扫描所有 status='Active' 的 MM 交易        │
│     ↓                                       │
│  3. 按 deal 逐笔计算当月应计利息               │
│     - accrual_period_start = MAX(value_date, 当月第一天) │
│     - accrual_period_end = MIN(maturity_date, 当月最后一天) │
│     - accrual_days = end - start + 1(包含首尾)           │
│     - accrued_interest = principal × rate × accrual_days / yearFactor  │
│     ↓                                       │
│  4. INSERT tms_mm_interest_schedules_t 行        │
│     ↓                                       │
│  5. UPDATE tms_mm_deals_t.accrued_interest += accrued_interest │
│     ↓                                       │
│  6. 报表:本月利息计提明细发布                    │
└────────────────────────────────────────────┘
```

> **本期实现**:同步调用 calculate-accrual 接口(手工触发);P1+ 接入月底定时调度任务。

### 6.4 提前还款流程

```
Step 1: 用户发起提前还款
   ↓
Step 2: 校验
   - early_termination_allowed='1'?
   - 当前在 Active 状态?
   - early_termination_penalty_rate 非 NULL?
   ↓ (校验通过)
Step 3: 录入提前还款参数(amount + date + penaltyRate)
   ↓
Step 4: 生成 EARLY_TERMINATE Action(待审批)
   ↓
Step 5: 审批
   - 1 级(默认)/ 2 级(金额超阈值)
   ↓ (审批通过)
Step 6: 计算
   - penalty_amount = early_termination_amount × penalty_rate × remaining_days / yearFactor
   - 本金应退 LEND / 应还 BORROW = early_termination_amount
   ↓
Step 7: 生成 DealMap
   - 1 行 DealMap:M_MARK_EARLY_TERMINATE(amount_or_rate=early_termination_amount)
   ↓
Step 8: 生成 CF(2 条)
   - CF1:本金反向,方向按 LEND/BORROW 反转
   - CF2:罚息,方向按 LEND/BORROW 流转
   - cflow_date = 提前还款操作日
   ↓
Step 9: UPDATE tms_mm_deals_t.early_termination_penalty_rate / accrued_interest(可选)
   ↓
Step 10: INSERT tms_actions_t.action_type='EARLY_TERMINATE',action_status='Approved'
```

---

## 七、DealMap 事件设计

### 7.1 核心原则

**沿用 DealMap v2.0 架构 + FX v3.2 多行单字段设计**:

- 一个 DealMap 只存 1 个数值(`amount_or_rate`)+ 1 个类型(`dealmap_type`)+ 1 个事件分类(`event_type`)
- 一笔 MM 交易产生 **2-4 行 DealMap**(PRINCIPAL / INTEREST / [可选 EARLY_TERMINATE])
- 复杂事件由 `dealmap_type` 枚举 + `event_type` 联合表达

### 7.2 DealMap 类型(MM 新增)

**在 `tms_deal_map_t.dealmap_type` 枚举基础上扩展**:

| `dealmap_type` | `event_type` | 含义 | 数量/笔 deal | 触发时机 | 触发 CF |
|---|---|---|---|---|---|
| `MM_PRINCIPAL_OUT` | `CASHFLOW` | 拆出本金(Outflow) | 1 | EXECUTE | 1 CF(本金流出) |
| `MM_INTEREST_IN` | `INTEREST` | 拆出利息(Inflow) | 1 | EXECUTE | 1 CF(利息流入) |
| `MM_PRINCIPAL_IN` | `CASHFLOW` | 拆入本金(Inflow) | 1 | EXECUTE | 1 CF(本金流入) |
| `MM_INTEREST_OUT` | `INTEREST` | 拆入利息(Outflow) | 1 | EXECUTE | 1 CF(利息流出) |
| `MM_ACCRUAL` | `INTEREST` | 利息计提 | N(每月) | 月末调度 | 0 CF(仅计提) |
| `MM_EARLY_TERMINATE` | `LIFECYCLE` | 提前还款 | 1 | EARLY_TERMINATE 审批后 | 1 CF(本金 + 罚息反向) |
| `MM_MATURE` | `LIFECYCLE` | 到期事件 | 1(系统) | 系统调度 | 0 CF(仅记录) |

> **本期实现**:`MM_PRINCIPAL_OUT/IN` + `MM_INTEREST_IN/OUT` + `MM_ACCRUAL` + `MM_EARLY_TERMINATE`
> **P1+ 启用**:`MM_MATURE` 等更多事件类型(系统生成)

### 7.3 DMP 事件示例(拆出 LEND 一笔)

**触发条件**:拆出 100 万 CNY,利率 5.3%,ON(隔夜),ACT/360

```
EXECUTE Action(act=EXECUTE, status=Pending→Approved)
  ├─ DealMap #1: dealmap_type='MM_PRINCIPAL_OUT' event_type='CASHFLOW' amount_or_rate=1000000.00  → 生成 CF(本金流出,value_date)
  └─ DealMap #2: dealmap_type='MM_INTEREST_IN'   event_type='INTEREST' amount_or_rate=147.22     → 生成 CF(利息流入,maturity_date)

定时期或月末:
  └─ DealMap #3: dealmap_type='MM_ACCRUAL' event_type='INTEREST' amount_or_rate=?(第 1 个月计提)
```

**Cashflow 详细**:

```
CF #1: dealmap_number=DMP202607050001, cflow_date=value_date(2026-07-06)
       direction=Outflow, amount=1000000.00, currency=CNY
       (拆出本金离开本公司账户)

CF #2: dealmap_number=DMP202607050002, cflow_date=maturity_date(2026-07-07)
       direction=Inflow, amount=1000147.22, currency=CNY
       (本金 + 利息回流:1000147.22 = 1000000 + 1000000 × 5.3% × 1 / 360)
```

### 7.4 拆借类型与 DealMap 对应关系

| MM 业务类型 | dealmap_type | event_type | 现金流方向 | 现金流日期 |
|------------|--------------|------------|-----------|-----------|
| **LEND 拆出** | MM_PRINCIPAL_OUT | CASHFLOW | Outflow | value_date |
| | MM_INTEREST_IN | INTEREST | Inflow | maturity_date |
| **BORROW 拆入** | MM_PRINCIPAL_IN | CASHFLOW | Inflow | value_date |
| | MM_INTEREST_OUT | INTEREST | Outflow | maturity_date |
| **LEND 提前还款** | MM_EARLY_TERMINATE | LIFECYCLE | Inflow(本方收入) | 操作日 |
| **BORROW 提前还款** | MM_EARLY_TERMINATE | LIFECYCLE | Outflow(本方支付) | 操作日 |

### 7.5 1 DealMap → 1 Cashflow 约束(沿用 FX v3.2)

**业务规则**:
- 每条 DealMap 行最多生成 1 条 Cashflow(0 或 1)
- Cashflow 上 `tms_cashflow_t.dealmap_number` 强引用
- 例外:`MM_ACCRUAL` 不生成 CF(纯计提记录)

**MM 与 FX 的 DealMap 兼容性**:

| 维度 | FX v3.2 | MM v1.0 |
|------|---------|---------|
| 数量/笔 deal | 3-4 | 2-N(每笔 + 每月计提 + 提前还款) |
| 单字段 | `amount_or_rate` | `amount_or_rate`(沿用) |
| 1:0/1 CF | ✅ 强制 | ✅ 强制 |
| `dealmap_number` 命名 | `DMP+yyyyMMdd+序号` | 同 |
| `dealmap_type` 扩展 | `FX_BUY/SELL/RATE/FIX` | `MM_*` 多种 |
| 共享表 | `tms_deal_map_t` | 同(不创建新表) |

---

## 八、状态机

### 8.1 状态定义

| 状态 | 说明 | 可达状态 |
|------|------|----------|
| **New** | 交易员刚录入,未提交 | → Submitted, → Canceled |
| **Submitted** | 已提交,待审批 | → Approved, → Rejected |
| **Approved** | 审批通过,未起息 | → Active, → Canceled |
| **Active** | 已起息,本金已流,存续中 | → Matured, → EarlyTerminated, → Canceled |
| **Matured** | 到期日已到,本金回流 | → Settled |
| **Settled** | 本金 + 利息 全 Cleared,终态 | 终态 |
| **Rejected** | 审批驳回,需重新编辑 | → New(编辑后重新 SUBMIT) |
| **Canceled** | 已取消,可能未起息 | 终态(软删) |
| **EarlyTerminated** | 提前还款(P1 占位) | → Settled |
| **Overdue** | 逾期(P1 占位) | |

### 8.2 状态流转图

```
                    创建                     提交                审批                执行(起息)              计提(月末)                 到期                 结算
                     │                       │                  │                    │                      │                      │                    │
                     ▼                       ▼                  ▼                    ▼                      ▼                      ▼                    ▼
                 ┌─────────┐            ┌──────────┐        ┌──────────┐         ┌──────────┐         ┌──────────┐          ┌──────────┐          ┌──────────┐
   ──────────────│   New    │───────────>│Submitted │───────>│ Approved │────────>│  Active  │────────>│ Active   │─────────>│ Matured  │─────────>│  Settled │
                 └─────────┘            └──────────┘        └──────────┘         └──────────┘         └──────────┘          └──────────┘          └──────────┘
                     │                       │                  │                    │                  (累计计提)
                     │                       │ Reject           │                    │
                     │                       ▼                  │                    │
                     │                 ┌──────────┐             │                    │
                     │                 │ Rejected │             │                    │
                     │                 └──────────┘             │                    │
                     │                   (可编辑)               │                    │
                     │                                          │                    │
                     │ DELETE                                  │                    │
                     └───────────────────────────────────────────┴────────────────────┘
                                                  Canceled(软删,deleted='1')

   P1:Active ─Early Terminate(after approve)─> EarlyTerminated ─> Settled
   P1:Active ─逾期(到期未 CF Cleared)─> Overdue
```

### 8.3 状态机规则(MM 特定)

| 状态机规则 | 说明 |
|-----------|------|
| **SM1** | New → Active 不可直跳,必须经 Submitted → Approved |
| **SM2** | Approved 后系统调度(或手工)EXECUTE 才进入 Active |
| **SM3** | Active 期满才可转 Matured(到期日后) |
| **SM4** | Matured 后,所有 CF Cleared 才能转 Settled |
| **SM5** | New / Submitted / Approved 状态可 Canceled;Active 不可直接 Canceled(P1+ 引入强冲销流程) |
| **SM6** | Rejected 可编辑后回 New 状态 |
| **SM7** | 终态(Settled / Canceled)不可再操作 |

---

## 九、验收标准

### 9.1 P0 核心验收(MM 全量)

| # | 功能 | 验收条件 |
|---|------|----------|
| **A1** | 表结构 | `tms_mm_deals_t` 和 `tms_mm_interest_schedules_t` 字段齐全,审计字段完整,索引齐备 |
| **A2** | 编号生成 | `deal_number` 格式 `MMyyyyMMddxxxx`,同日内递增,跨日重置 |
| **A3** | 共享主键 | `tms_mm_deals_t.id = tms_deals_t.id`,无独立 PK |
| **A4** | 通用字段强类型 | `management_entity_id BIGINT FK`,无字符 code |
| **A5** | MM 方向枚举 | `direction IN ('LEND','BORROW','REPO_OUT','REPO_IN','NCD')`,本期只用 LEND/BORROW |
| **A6** | 计息基础枚举 | `interest_calc_basis IN ('ACT_360','ACT_365','ACT_365F','THIRTY_360','ACT_ACT')` |
| **A7** | 期限类型枚举 | `term_type IN ('ON','TN','1W','2W','1M','3M','6M','1Y','CUSTOM')`,本期不用 TN |
| **A8** | 还款方式枚举 | `repayment_method IN ('BULLET','INTEREST_ONLY','AMORTIZING')`,本期只用 BULLET |
| **A9** | 后端 calculate | `POST /api/v1/dealing/mm-deals/calculate` 联动算:termDays / interestAmount / dayCountFactor / totalRepayAmount |
| **A10** | ACT/360 利息公式 | `interestAmount = principal × rate × termDays / 360`(允许浮点误差 0.01) |
| **A11** | ACT/365 利息公式 | `interestAmount = principal × rate × termDays / 365` |
| **A12** | THIRTY_360 利息公式 | `interestAmount = principal × rate × termDays / 360`(同 ACT/360 数值) |
| **A13** | 利率联动 | 利率/本金/期限 任一变化,后端 calculate 自动补全 |
| **A14** | LEND 创建 | EXECUTE Action 触发:2 行 DealMap(PRINCIPAL_OUT + INTEREST_IN)+ 2 条 CF(value_date + maturity_date) |
| **A15** | BORROW 创建 | EXECUTE Action 触发:2 行 DealMap(PRINCIPAL_IN + INTEREST_OUT)+ 2 条 CF(方向反转) |
| **A16** | CF 方向正确性 | LEND:本金 Outflow / 利息 Inflow;BORROW:本金 Inflow / 利息 Outflow |
| **A17** | CF 金额正确性 | `principal + interest = totalRepayAmount`(LEND); 利息方向与本金反向(BORROW) |
| **A18** | 利息计提 | `POST /calculate-accrual` 写入 `tms_mm_interest_schedules_t`,`accrued_interest` 字段累加正确 |
| **A19** | 月末跨期 | 起息日 7-15,期末 7-31,占用 17 天(31-15+1 不算首日:31-15=16 或 17?)按公司政策 |
| **A20** | 提前还款 | `POST /{dealNumber}/early-terminate` 校验:提前还款需审批 + 计算罚息 + 生成反向 CF |
| **A21** | 提前还款罚息计算 | `penalty = early_termination_amount × penalty_rate × remaining_days / yearFactor` |
| **A22** | 状态机 9 态 | New → Submitted → Approved → Active → Matured → Settled,符合度 |
| **A23** | Action 11 种 | CREATE / UPDATE / DELETE / SUBMIT / APPROVE / REJECT / EXECUTE / MATURE / SETTLE / CALC_ACCRUAL / EARLY_TERMINATE |
| **A24** | 审批流 | 金额 > 5000 万 走 2 级审批;默认 1 级 |
| **A25** | 跨币种 | 本期不支持;校验 currency 字段与 principal 货币一致 |
| **A26** | 默认账户规则联动 | 输入 5 维(主体 + direction + currency + instrument)自动调 match 带出 account |
| **A27** | 命名 | 全文使用"管理主体",无"业务主体"残留 |
| **A28** | 字段命名规范 | DB snake_case / Java camelCase / JS lowerCamelCase |
| **A29** | 审计字段 | created_by/created_at/updated_by/updated_at/version/deleted 全表必备 |
| **A30** | 状态字段 | `status VARCHAR(20)`,不用 CHAR(1) |
| **A31** | 金额精度 | principal DECIMAL(38,18);rate DECIMAL(10,4);day_count_factor DECIMAL(18,10) |
| **A32** | 索引性能 | 5+ 索引(mgmt_entity_id, currency, value_date, maturity_date, term_days) |
| **A33** | Action Reject 流程 | REJECT Action 可把 Approved 转回 Rejected,允许编辑后重提交 |
| **A34** | DealMap v2.0 兼容 | MM 与 AC/AT/FX 共享 `tms_deal_map_t` 表,字段对齐(detail_type 与 event_type 区分) |
| **A35** | Cashflow v2.0 兼容 | MM 与 AC/AT/FX 共享 `tms_cashflow_t` 表,`dealmap_number` 强引用 |

### 9.2 兼容性验收

| # | 项 | 验收 |
|---|----|------|
| **C1** | 与 AC/AT/FX 共享 `tms_deals_t` | `tms_deals_t.deal_type IN ('AC','AT','FX','MM')` |
| **C2** | 与 M1-默认账户规则兼容 | `direction` 字段复用 GlobalConstants.Direction(INFLOW/OUTFLOW);`MM.direction` 业务方向 |
| **C3** | 与 FX v3.2 DealMap 单字段兼容 | `amount_or_rate` 字段复用 |
| **C4** | 共享 Action 类型 | `CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE` 复用;MM 新增 `MATURE/SETTLE/CALC_ACCRUAL/EARLY_TERMINATE` |
| **C5** | 共享 Cashflow 字段 | `amount/currency/cflow_date/value_date/direction/dealmap_number` 全对齐 |
| **C6** | 字段命名统一 | 全部 snake_case(DB)+ camelCase(Java),无业务术语缩写歧义 |
| **C7** | API 路径 | `/api/v1/dealing/mm-deals` 对齐 `/api/v1/{module}/{resource}` 规范 |

### 9.3 暂不做项(明确范围)

| # | 功能 | 状态 |
|---|------|------|
| **Z1** | 回购协议 REPO | **P1** 占位预留字段(本期不实现,字段全占位但表不创建) |
| **Z2** | 同业存单 NCD | **P2** 占位 |
| **Z3** | 浮动利率 + 重置 | **P1** 字段占位,本期用 Fixed |
| **Z4** | 利率衍生品 IRS | **P2**(已存在 IRS 模块) |
| **Z5** | 担保物管理 | **P1** 字段预留 |
| **Z6** | 跨币种拆借 | **P1** 字段预留 |
| **Z7** | 利息多期付息 | **P1** 字段预留(本期 BULLET) |
| **Z8** | 利息自动调度 | **P1+** 手工触发 |
| **Z9** | 估值 MTM | **P1+** 浮动利率不估,固定利率无需估 |
| **Z10** | 减值 ECL | **M4** 对接 impairment 模块(IFRS 9) |
| **Z11** | 占用对手方授信 | **P1** 对接 limit 模块 |
| **Z12** | 资金池联动 | **P2** 跨模块 |
| **Z13** | 多部分提前还款 | **P2** 本期支持 1 次 |
| **Z14** | 跨月错月调整自动 | **P1+** |

---

## 十、接口需求

### 10.1 MM 交易 CRUD

部署在 `dealing` 模块,端口 8082,沿用 `/api/v1/dealing/mm-deals` 路径。

| # | 端点 | 方法 | 说明 | Action |
|---|------|------|------|--------|
| 1 | `/api/v1/dealing/mm-deals` | POST | 创建 MM 交易(LEND / BORROW) | CREATE |
| 2 | `/api/v1/dealing/mm-deals/page` | POST | 列表分页查询 | - |
| 3 | `/api/v1/dealing/mm-deals/{dealNumber}` | GET | 详情查询 | - |
| 4 | `/api/v1/dealing/mm-deals/update` | POST | 更新 MM 交易 | UPDATE |
| 5 | `/api/v1/dealing/mm-deals/delete/{id}` | POST | 删除 MM 交易(软删 + 关联级联) | DELETE |
| 6 | `/api/v1/dealing/mm-deals/{dealNumber}/submit` | POST | 提交审批 | SUBMIT |
| 7 | `/api/v1/dealing/mm-deals/{dealNumber}/approve` | POST | 审批通过 | APPROVE |
| 8 | `/api/v1/dealing/mm-deals/{dealNumber}/reject` | POST | 审批驳回 | REJECT |
| 9 | `/api/v1/dealing/mm-deals/{dealNumber}/execute` | POST | 执行(APPROVED → ACTIVE) | EXECUTE |
| 10 | `/api/v1/dealing/mm-deals/{dealNumber}/mature` | POST | 到期(ACTIVE → MATURED) | MATURE |
| 11 | `/api/v1/dealing/mm-deals/{dealNumber}/settle` | POST | 结算(MATURED → SETTLED) | SETTLE |
| 12 | `/api/v1/dealing/mm-deals/{dealNumber}/early-terminate` | POST | 提前还款 | EARLY_TERMINATE |
| 13 | `/api/v1/dealing/mm-deals/calculate` | POST | 后端联动计算 | - |
| 14 | `/api/v1/dealing/mm-deals/calculate-accrual` | POST | 月末利息计提 | CALC_ACCRUAL |

### 10.2 MM 关联查询

| # | 端点 | 方法 | 说明 |
|---|------|------|------|
| 1 | `/api/v1/dealing/mm-deals/{dealNumber}/dealmaps` | GET | 该 MM 的所有 DealMap(本金腿 + 利息腿 + 计提 + 提前还款) |
| 2 | `/api/v1/dealing/mm-deals/{dealNumber}/cashflows` | GET | 该 MM 的所有 Cashflow(LEND 2 条;BORROW 2 条;+ 提前还款 2 条) |
| 3 | `/api/v1/dealing/mm-deals/{dealNumber}/actions` | GET | 该 MM 的所有 Action(11 种) |
| 4 | `/api/v1/dealing/mm-deals/{dealNumber}/accrual-schedules` | GET | 该 MM 的所有利息计提记录(`tms_mm_interest_schedules_t`) |
| 5 | `/api/v1/dealing/mm-deals/{dealNumber}/instrument` | GET | 关联的金融工具详情 |

### 10.3 联表对接(basedata 模块)

| # | 端点 | 方法 | 说明 |
|---|------|------|------|
| 1 | `/opentms/basedata/api/v1/default-bank-account-rules/match` | GET | 运行时匹配,自动带出本金/利息账户(沿用 M1-v1) |
| 2 | `/opentms/basedata/api/v1/counterparties/page?type=BANK` | GET | 仅显示银行类对手方 |
| 3 | `/opentms/basedata/api/v1/instruments/page?type=MM_*` | GET | 显示 MM 类金融工具 |

### 10.4 接口幂等性

- **写操作**(CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE/MATURE/SETTLE/EARLY_TERMINATE):使用 `X-Idempotency-Key` 头
- **读操作**(GET /page / GET /{dealNumber} / GET 关联查询):无需幂等

### 10.5 核心接口请求/响应样例

#### 10.5.1 CREATE 接口

```
POST /api/v1/dealing/mm-deals
```

**Request**:
```json
{
  "managementEntityId": 1,
  "counterpartyId": 5001,
  "traderId": 1,
  "instrumentId": 501,
  "direction": "LEND",
  "currency": "CNY",
  "principal": 1000000.00,
  "rate": 5.3000,
  "rateType": "Fixed",
  "interestCalcBasis": "ACT_360",
  "repaymentMethod": "BULLET",
  "termType": "ON",
  "tradeDate": "2026-07-05",
  "valueDate": "2026-07-06",
  "maturityDate": "2026-07-07",
  "principalAccountId": 1001,
  "interestAccountId": 1002,
  "earlyTerminationPenaltyRate": null,
  "earlyTerminationAllowed": "0",
  "description": "O/N 拆出补足流动性",
  "remark": null
}
```

**Side effects(在事务内)**:
1. INSERT `tms_deals_t`(deal_type='MM',trade_date/value_date/maturity_date/term_days 同步)
2. INSERT `tms_mm_deals_t`(id = tms_deals_t.id,共享主键)
3. INSERT `tms_actions_t`(action_type='CREATE',action_status='Pending')
4. INSERT `tms_deal_map_t` × 1(占位:仅记录 deal 创建,DMP#0,event_type='CREATE')
5. UPDATE `tms_deals_t.status='New'`

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "MM202607050001",
    "status": "New",
    "dealMapCount": 1,
    "cashflowCount": 0
  }
}
```

#### 10.5.2 CALCULATE 接口

```
POST /api/v1/dealing/mm-deals/calculate
```

**Request**(用户已填):
```json
{
  "principal": 1000000.00,
  "rate": 5.3000,
  "termType": "ON",
  "interestCalcBasis": "ACT_360",
  "valueDate": "2026-07-06",
  "maturityDate": "2026-07-07"
}
```

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "principal": 1000000.00,
    "rate": 5.3000,
    "termType": "ON",
    "termDays": 1,
    "interestCalcBasis": "ACT_360",
    "valueDate": "2026-07-06",
    "maturityDate": "2026-07-07",
    "interestAmount": 147.22,
    "dayCountFactor": 0.00277777,
    "totalRepayAmount": 1000147.22
  }
}
```

#### 10.5.3 EXECUTE 接口

```
POST /api/v1/dealing/mm-deals/{dealNumber}/execute
```

**Path 参数**:`dealNumber`(string)

**Side effects(在事务内)**:
1. 校验当前状态 = 'Approved'
2. 计算 `total_interest = principal × rate × term_days / yearFactor`,持久化
3. INSERT 2 行 `tms_deal_map_t`:
   - DMP#2:`dealmap_type='MM_PRINCIPAL_OUT'`,amount_or_rate=1000000.00 → 触发 CF(本金流出,value_date)
   - DMP#3:`dealmap_type='MM_INTEREST_IN'`,amount_or_rate=147.22 → 触发 CF(利息流入,maturity_date)
4. INSERT 2 行 `tms_cashflow_t`(dealmap_number 强引用各 DealMap)
5. UPDATE `tms_deals_t.status='Active'`
6. INSERT `tms_actions_t`(action_type='EXECUTE',action_status='Approved')

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "MM202607050001",
    "status": "Active",
    "dealMapCount": 3,
    "cashflowCount": 2,
    "principalCashflow": "CF202607050001",
    "interestCashflow": "CF202607050002"
  }
}
```

#### 10.5.4 CALCULATE-ACCRUAL 接口

```
POST /api/v1/dealing/mm-deals/calculate-accrual
```

**Request**:
```json
{
  "scheduledDate": "2026-07-31"
}
```

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "accrualCount": 5,
    "accrualSchedules": [
      {
        "scheduleNumber": "ACS202607310001",
        "dealNumber": "MM202607050001",
        "direction": "LEND",
        "currency": "CNY",
        "accrualPeriodStart": "2026-07-06",
        "accrualPeriodEnd": "2026-07-31",
        "accrualDays": 26,
        "principal": 1000000.00,
        "rate": 5.3000,
        "interestCalcBasis": "ACT_360",
        "accruedInterest": 3827.78,
        "interestAccountId": 1002,
        "status": "Posted"
      }
    ]
  }
}
```

#### 10.5.5 EARLY-TERMINATE 接口

```
POST /api/v1/dealing/mm-deals/{dealNumber}/early-terminate
```

**Request**:
```json
{
  "earlyTerminationDate": "2026-07-30",
  "earlyTerminationAmount": 500000.00,
  "penaltyRate": 0.5000,
  "operator": "trader01"
}
```

**Side effects(审批通过后)**:
1. 校验 `early_termination_allowed='1'` AND `status='Active'`
2. 计算 `penalty_amount = early_termination_amount × penalty_rate × remaining_days / yearFactor`
3. INSERT 1 行 `tms_deal_map_t`(dealmap_type='MM_EARLY_TERMINATE')
4. INSERT 2 行 `tms_cashflow_t`:
   - CF(本金反向,LEND Inflow / BORROW Outflow)
   - CF(罚息,LEND Inflow / BORROW Outflow)
5. INSERT `tms_actions_t`(action_type='EARLY_TERMINATE',action_status='Approved')
6. UPDATE `tms_mm_deals_t.accrued_interest`(可选,本期不变)

**Response 200**:
```json
{
  "code": 200, "message": "success",
  "data": {
    "dealNumber": "MM202607050001",
    "penaltyAmount": 458.33,
    "principalCashflowNumber": "CF202607300001",
    "penaltyCashflowNumber": "CF202607300002"
  }
}
```

### 10.6 错误码

| HTTP | code | 含义 | 触发场景 |
|------|------|------|---------|
| 200 | 200 | 成功 | 正常 |
| 400 | 400 | 请求参数错误 | 必填字段缺失、字段格式错误 |
| 400 | 40001 | INPUT_INSUFFICIENT | calculate 接口用户填的字段不足 2 个 |
| 400 | 40002 | VALUE_INCONSISTENT | calculate 接口字段互相矛盾 |
| 400 | 40003 | STATE_INVALID | 状态机非法跳转(如 New 直接 Active) |
| 400 | 40004 | EARLY_TERMINATION_NOT_ALLOWED | 提前还款被禁用 |
| 400 | 40005 | COUNTERPARTY_NOT_BANK | 对手方类型不是银行 |
| 400 | 40006 | ACCOUNT_NOT_BELONG_ENTITY | 银行账户不属于主体 |
| 400 | 40007 | INSTRUMENT_TYPE_INVALID | 金融工具不是 MM 类 |
| 401 | 401 | 未授权 | Token 失效 |
| 403 | 403 | 无权限 | 当前用户无审批权限 |
| 404 | 404 | 资源不存在 | dealNumber 不存在 |
| 422 | 42201 | DATE_INVALID | 交易日 > 起息日 / 起息日 >= 到期日 |
| 422 | 42202 | TERM_INVALID | 期限类型与到期日 - 起息日不匹配 |
| 422 | 42203 | RATE_INVALID | 利率超出 [0, 100] |
| 500 | 500 | 系统错误 | 内部异常 |

---

## 十一、与现有模块的复用

### 11.1 公共表复用

| 表 | AC/AT/FX 现状 | MM v1.0 复用 | 字段对齐 |
|----|---------------|---------------|---------|
| `tms_deals_t` | 共享公共主表,`deal_type IN ('AC','AT','FX')` | **新增 deal_type='MM'** | ✅ 全字段对齐,无 schema 改动 |
| `tms_actions_t` | 5 种 Action(AC)→ 4 种(FX) | **复用全部 + 新增 4 种** | ✅ action_type 扩展枚举 |
| `tms_deal_map_t` | 25 字段,FX 单字段扩展 | **沿用 + 扩展 `dealmap_type`** | ✅ 字段对齐,event_type 枚举扩展 |
| `tms_cashflow_t` | DECIMAL(38,18) 精度 + `dealmap_number` 强引用 | **沿用全字段** | ✅ |
| `tms_deals_image_t` | 镜像表 | **复用** | ✅ |

### 11.2 AC/AT/FX 既有业务架构

| 复用点 | 说明 |
|--------|------|
| **DealMap v2.0** | MM 不创建新事件模型,直接用 `tms_deal_map_t` + `event_type` 扩展 |
| **Cashflow 1:1** | 沿用 FX v3.2 严格 `1 DealMap → 0/1 Cashflow` 约束 |
| **后端 calculate** | MM 与 FX 风格一致,POST /calculate 统一算 termDays/interest |
| **状态机分层** | MM 有完整 9 态(因借贷性质需审批),FX 仅 3 态(即时生效) |
| **编号生成** | MM + yyyyMMdd + 4 位流水,沿用 `SerialNumberGenerator` |

### 11.3 BaseData 依赖

| 依赖 | 既有 / 新增 | 说明 |
|------|------------|------|
| `tms_management_entity_t` | 既有 | 直接引用 id(强类型 FK) |
| `tms_counterparty_t` | 既有 + 校验 | **新增校验 `counterparty_type='BANK'`** |
| `tms_trader_t` | 既有 | 直接引用 id |
| `tms_instrument_t` | 既有 + 新增 | **新增 instrument_type ∈ ('MM_LEND','MM_BORROW')** |
| `tms_bank_account_t` | 既有 | 直接引用 id,沿用默认账户规则 |
| `tms_currency_t` | 既有 | 直接引用 code |
| `tms_default_bank_account_rule_t` | **M1 v1.0 既有** | 运行时匹配,5 维 + Direction |

### 11.4 后续与"主体默认银行账户规则"联动

**典型场景**:MM 拆借录入时,选完 5 维(主体 + 方向 + 金融产品 + 币种 + 期限类型)后,自动调 `match` 接口带出本金/利息账户。

**前端调用**:
```js
// web/src/api/dealing/mm-deal.js
import { matchDefaultBankAccount } from '@/api/basedata/default-bank-account-rule'

// 主账户
const principalAccount = await matchDefaultBankAccount({
  managementEntityId: form.managementEntityId,
  instrumentId: form.instrumentId,
  direction: form.direction === 'LEND' ? 'Outflow' : 'Inflow',  // LEND 拆出本金是 Outflow
  currency: form.currency
})

// 利息账户(同方向)
const interestAccount = await matchDefaultBankAccount({
  managementEntityId: form.managementEntityId,
  instrumentId: form.instrumentId,
  direction: form.direction === 'LEND' ? 'Inflow' : 'Outflow',  // LEND 利息是 Inflow
  currency: form.currency
})
```

---

## 十二、不在本期范围(明确范围控制)

### 12.1 P1 - 回购协议 REPO / REVERSE REPO

**本期不实现,但字段已预留**:

| 字段 | tms_mm_deals_t | 说明 |
|------|----------------|------|
| `collateral_id` | 预留 BIGINT | P1 启用,FK → `tms_collateral_t.id` |
| `collateral_type` | 预留 VARCHAR(20) | BOND / NOTE / UNDEFINED |
| `direction IN ('REPO_OUT','REPO_IN')` | 枚举值预留 | 本期只用 LEND/BORROW |

**P1 实施范围**:
- `tms_mm_collaterals_t` 表创建(担保物主数据)
- `tms_mm_collateral_positions_t` 表(每日盯市)
- REPO_OUT / REPO_IN DealMap type 实现
- 折扣率(Discount)字段 + 抵押率维护
- 担保物处置流程

### 12.2 P2 - 同业存单 NCD

**本期不实现**:

| 字段 | tms_mm_deals_t | 说明 |
|------|----------------|------|
| `direction IN ('NCD')` | 枚举值预留 | 本期不用 |

**P2 实施范围**:
- NCD 发行登记
- 二级市场转让流程
- 到期兑付 + 自动转回

### 12.3 P2 - 利率衍生品 IRS

**已在 IRS 模块实现,本期不重复设计**。MM 与 IRS 集成限于利率曲线共享(P2+ 浮利基准复用)。

### 12.4 P2 - 复杂现金流模型

- 多期还款(等额本息)
- 看涨/看跌期权型拆借

### 12.5 自动化特性(本期不实现)

| # | 自动化 | 状态 |
|---|--------|------|
| 1 | 月末利息自动调度 | P1+ 调度任务 |
| 2 | 对手方授信自动占用 | P1+ 对接 limit |
| 3 | 减值 ECL 自动计算 | M4 对接 impairment |
| 4 | 现金流银行直连自动对账 | M5 对接 settlement |
| 5 | 利率曲线实时同步 | P2 接 basedata |

---

## 十三、实施阶段

### Phase 1 - 设计(本 PRD)

- [x] 编写 M3-货币市场 MM 交易 PRD v1.0(2026-07-05)
- [x] 字段设计 + 表结构 + 索引
- [x] 后端 calculate 接口设计
- [x] 业务规则 + 状态机 + 验收标准
- [x] DealMap 事件设计(MM_PRINCIPAL/INTEREST/ACCRUAL/EARLY_TERMINATE)

### Phase 2 - MVP 实施(预计 3 周)

**Week 1:核心 CRUD + 计算**:
- [ ] DB 设计:`db/schema/26-mm-deal-v1.sql`(主表 + 计提表 + tms_deals_t 字段同步)
- [ ] 后端:`tms_mm_deals_t` 实体 + Service + Controller(2 个 Action 接口:CREATE / UPDATE)
- [ ] 后端:Calculate 接口(`POST /api/v1/dealing/mm-deals/calculate`)
- [ ] 前端:`MmDealList.vue` + `MmDealForm.vue`(基础录入 + calculate 联动)

**Week 2:审批流 + 执行**:
- [ ] 后端:SUBMIT / APPROVE / REJECT / EXECUTE Action(4 个接口)
- [ ] 后端:DealMap v2.0 触发(`MM_PRINCIPAL_OUT/IN`, `MM_INTEREST_IN/OUT`)
- [ ] 后端:Cashflow 自动生成(2 条: value_date + maturity_date)
- [ ] 前端:详情页 4 Tab(基本信息 / DealMap / Cashflow / Action)
- [ ] 前端:审批流程接入(基础审批岗)

**Week 3:利息计提 + 提前还款 + 测试**:
- [ ] 后端:CALC_ACCRUAL 接口(`POST /calculate-accrual`)
- [ ] 后端:EARLY_TERMINATE 接口 + 罚息计算
- [ ] 前端:计提报表展示 + 提前还款 Modal
- [ ] 测试:`scripts/test/test_mm_deal_api.py`(100% 主流程)
- [ ] 测试:`scripts/test/test_mm_deal_ui.py`(UI 自动化,本金利息现金流)

### Phase 3 - P1 增强(预计 4 周)

- [ ] REPO / REVERSE REPO 回购协议
- [ ] 浮动利率 + 重置
- [ ] 跨币种拆借
- [ ] 月末自动调度任务
- [ ] 对手方授信占用集成(对接 limit 模块)

### Phase 4 - P2 延伸(预计 6 周)

- [ ] NCD 同业存单
- [ ] IRS / 利率衍生品集成
- [ ] 减值 ECL 自动计算
- [ ] AI 推荐最优期限

---

## 十四、相关文档

- `M3-外汇交易PRD.md` v3.2 — FX 架构参考(共享 DealMap + 后端 calculate)
- `M3-NDF-Rate-Fix设计评审.md` — NDF/REPO 借鉴的 Fix 流程
- `M1-DealMap 生命周期事件PRD-v2.md` — DealMap v2.0 基础
- `M1-主体默认银行账户规则PRD-v1.md` — 5 维匹配 + Direction 枚举复用
- `M1-资金管理主体PRD-v1.md` — 管理主体定义
- `M1-AT交易PRD-v2.0.md` — AT 交易与 DealMap 流程参考
- `docs/api/dealing/M3-fx-deals-v3.md` — FX API 文档风格参考
- `docs/规范/Open-TMS开发规范文档.md` — 命名/类型/审计规范
- `CLAUDE.md` — 项目总规范
- `open-tms功能特性清单.md` — 业界对标参考

---

## 附录 A:DDL 草案

```sql
-- ==========================================
-- M3-货币市场 MM 交易 (v1.0)
-- 文件: db/schema/26-mm-deal-v1.sql
-- 作者: PM
-- 日期: 2026-07-05
-- 基于: PRD docs/prd/M3/M3-货币市场MM交易PRD.md
-- 风格: 沿用 FX v3.2 (共享主键 + 单字段多行 DealMap + 1:1 CF)
-- ==========================================

-- -----------------------------------------------------------------------------
-- 1. tms_deals_t: 字段同步(FX v3.2 已加,这里只为完整)
-- -----------------------------------------------------------------------------
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS trade_date DATE;
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS maturity_date DATE;
ALTER TABLE tms_deals_t ADD COLUMN IF NOT EXISTS term_days INT;

COMMENT ON COLUMN tms_deals_t.deal_type IS 'AC / AT / FX / MM / DEPOSIT / LOAN';
COMMENT ON COLUMN tms_deals_t.trade_date IS '交易日 v3.2(MM/FX 通用)';
COMMENT ON COLUMN tms_deals_t.maturity_date IS '到期日 v3.2(= value_date + term)';
COMMENT ON COLUMN tms_deals_t.term_days IS '期限天数 v3.2(后端算)';

-- -----------------------------------------------------------------------------
-- 2. tms_mm_deals_t: MM 特性表(共享主键)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tms_mm_deals_t (
    id BIGINT NOT NULL PRIMARY KEY,
    deal_number VARCHAR(50) NOT NULL UNIQUE,

    -- 通用字段(强 FK)
    management_entity_id BIGINT NOT NULL,

    -- MM 特有
    direction VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    principal DECIMAL(38,18) NOT NULL,
    rate DECIMAL(10,4) NOT NULL,
    rate_type VARCHAR(20) NOT NULL DEFAULT 'Fixed',
    spread_bp DECIMAL(10,4) NOT NULL DEFAULT 0,
    floating_index VARCHAR(20),
    rate_reset_date DATE,
    interest_calc_basis VARCHAR(20) NOT NULL,
    repayment_method VARCHAR(20) NOT NULL DEFAULT 'BULLET',
    term_type VARCHAR(20) NOT NULL,
    term_days INT NOT NULL,

    -- 日期字段(冗余便于查询,真实数据在 tms_deals_t)
    trade_date DATE NOT NULL,
    value_date DATE NOT NULL,
    maturity_date DATE NOT NULL,

    -- 账户
    principal_account_id BIGINT NOT NULL,
    interest_account_id BIGINT NOT NULL,

    -- 利息计算结果
    accrued_interest DECIMAL(38,18) NOT NULL DEFAULT 0,
    total_interest DECIMAL(38,18) NOT NULL DEFAULT 0,
    day_count_factor DECIMAL(18,10),

    -- 提前还款
    early_termination_penalty_rate DECIMAL(10,4),
    early_termination_allowed CHAR(1) NOT NULL DEFAULT '1',

    -- 预留:P1 担保物
    collateral_id BIGINT,
    collateral_type VARCHAR(20),

    -- 描述
    description VARCHAR(500),
    remark VARCHAR(500),

    -- 审计
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    deleted CHAR(1) NOT NULL DEFAULT '0',

    -- ★ 约束
    CONSTRAINT chk_mm_direction CHECK (direction IN (
        'LEND','BORROW','REPO_OUT','REPO_IN','NCD'
    )),
    CONSTRAINT chk_mm_rate_type CHECK (rate_type IN ('Fixed','Floating')),
    CONSTRAINT chk_mm_basis CHECK (interest_calc_basis IN (
        'ACT_360','ACT_365','ACT_365F','THIRTY_360','ACT_ACT'
    )),
    CONSTRAINT chk_mm_term_type CHECK (term_type IN (
        'ON','TN','1W','2W','1M','3M','6M','1Y','CUSTOM'
    )),
    CONSTRAINT chk_mm_repayment CHECK (repayment_method IN (
        'BULLET','INTEREST_ONLY','AMORTIZING'
    )),
    CONSTRAINT chk_mm_principal CHECK (principal > 0),
    CONSTRAINT chk_mm_rate CHECK (rate >= 0 AND rate <= 100),
    CONSTRAINT chk_mm_term_days CHECK (term_days >= 1 AND term_days <= 366),
    CONSTRAINT chk_mm_early_termination CHECK (early_termination_allowed IN ('0','1'))
);

CREATE INDEX IF NOT EXISTS idx_mmd_mgmt_entity     ON tms_mm_deals_t(management_entity_id);
CREATE INDEX IF NOT EXISTS idx_mmd_counterparty    ON tms_mm_deals_t(direction, principal_account_id);
CREATE INDEX IF NOT EXISTS idx_mmd_currency        ON tms_mm_deals_t(currency);
CREATE INDEX IF NOT EXISTS idx_mmd_value_date      ON tms_mm_deals_t(value_date);
CREATE INDEX IF NOT EXISTS idx_mmd_maturity_date   ON tms_mm_deals_t(maturity_date);
CREATE INDEX IF NOT EXISTS idx_mmd_term_days       ON tms_mm_deals_t(term_days);

COMMENT ON TABLE tms_mm_deals_t IS '货币市场交易特性表 v1.0(共享 tms_deals_t.id)';
COMMENT ON COLUMN tms_mm_deals_t.id IS '★ 共享主键,值=tms_deals_t.id';
COMMENT ON COLUMN tms_mm_deals_t.direction IS 'LEND(拆出)/BORROW(拆入)/REPO_OUT/REPO_IN/NCD (本期只用 LEND/BORROW)';
COMMENT ON COLUMN tms_mm_deals_t.principal IS '本金,高精度 DECIMAL(38,18)';
COMMENT ON COLUMN tms_mm_deals_t.rate IS '利率(百分比数值,如 5.30 代表 5.30%)';
COMMENT ON COLUMN tms_mm_deals_t.interest_calc_basis IS 'ACT_360/ACT_365/ACT_365F/THIRTY_360/ACT_ACT';
COMMENT ON COLUMN tms_mm_deals_t.term_type IS 'ON/1W/2W/1M/3M/6M/1Y/CUSTOM';
COMMENT ON COLUMN tms_mm_deals_t.collateral_id IS 'P1 占位字段,本期 NULL';

-- -----------------------------------------------------------------------------
-- 3. tms_mm_interest_schedules_t: 利息计提表(本期新建)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tms_mm_interest_schedules_t (
    id BIGSERIAL PRIMARY KEY,
    schedule_number VARCHAR(50) NOT NULL UNIQUE,
    deal_number VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    accrual_period_start DATE NOT NULL,
    accrual_period_end DATE NOT NULL,
    accrual_days INT NOT NULL,
    principal DECIMAL(38,18) NOT NULL,
    rate DECIMAL(10,4) NOT NULL,
    interest_calc_basis VARCHAR(20) NOT NULL,
    accrued_interest DECIMAL(38,18) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Posted',
    is_adjustment CHAR(1) NOT NULL DEFAULT '0',
    reversal_schedule_id BIGINT,
    description VARCHAR(500),
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    deleted CHAR(1) NOT NULL DEFAULT '0',
    CONSTRAINT chk_accrual_direction CHECK (direction IN ('LEND','BORROW')),
    CONSTRAINT chk_accrual_status CHECK (status IN ('Posted','Reversed','Adjusted')),
    CONSTRAINT chk_accrual_days CHECK (accrual_days >= 0),
    CONSTRAINT chk_accrual_amount CHECK (accrued_interest >= 0)
);

CREATE INDEX IF NOT EXISTS idx_mis_deal_number    ON tms_mm_interest_schedules_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_mis_period_end     ON tms_mm_interest_schedules_t(accrual_period_end);
CREATE INDEX IF NOT EXISTS idx_mis_status         ON tms_mm_interest_schedules_t(status);
CREATE INDEX IF NOT EXISTS idx_mis_direction      ON tms_mm_interest_schedules_t(direction);

COMMENT ON TABLE tms_mm_interest_schedules_t IS 'MM 利息计提表(每月按日计提记录)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.schedule_number IS '计提编号 格式 ACS+yyyyMMdd+序号';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrual_days IS '当月实际占用天数(首日不计数)';
COMMENT ON COLUMN tms_mm_interest_schedules_t.accrued_interest IS '当期计提金额(单位:本金币种)';

-- -----------------------------------------------------------------------------
-- 4. tms_deal_map_t: DealMap 类型扩展(v1.0 添加 MM_* 枚举值)
-- -----------------------------------------------------------------------------
ALTER TABLE tms_deal_map_t DROP CONSTRAINT IF EXISTS chk_dm_dealmap_type;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_dm_dealmap_type' AND conrelid = 'tms_deal_map_t'::regclass) THEN
        ALTER TABLE tms_deal_map_t DROP CONSTRAINT chk_dm_dealmap_type;
    END IF;
END $$;

-- (本期不加枚举 CHECK,延续 v3.2 风格:由应用层校验)

-- -----------------------------------------------------------------------------
-- 验证脚本(应用后手动执行)
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'tms_mm_deals_t' AND column_name IN ('id', 'direction', 'principal');
-- SELECT 1 FROM pg_tables WHERE tablename = 'tms_mm_interest_schedules_t';
```

---

## 附录 B:术语表

| 术语 | 全称 | 说明 |
|------|------|------|
| **MM** | Money Market | 货币市场(短期资金批发市场,期限 ≤ 1Y) |
| **LEND** | Lend | 拆出(资金贷给对手方) |
| **BORROW** | Borrow | 拆入(从对手方借入资金) |
| **O/N** | Overnight | 隔夜(期限 1 天) |
| **T/N** | Tomorrow-Next | 明天到后天(期限 2 天,P1 占位) |
| **DMP** | DealMap | 业务事件快照编号 |
| **ACS** | Accrual Schedule | 利息计提编号 |
| **REPO** | Repurchase Agreement | 回购协议 |
| **NCD** | Negotiable Certificate of Deposit | 同业存单 |
| **MTM** | Mark-to-Market | 盯市估值 |
| **ECL** | Expected Credit Loss | 预期信用损失(IFRS 9) |
| **ACT** | Actual | 实际天数 |
| **bp** | Basis Point | 基点(利率 0.01%) |
| **spread** | Spread | 利差(基准利率上浮部分) |
| **BULLET** | Bullet Repayment | 到期一次性还本付息 |
| **INTEREST_ONLY** | Interest-Only | 期间只付息,到期还本 |
| **AMORTIZING** | Amortizing | 等额本息分期 |
| **Fixing Rate** | Fixing Rate | 浮动利率基准重置利率 |
| **Counterparty** | Counterparty | 交易对手方 |
| **Principal** | Principal | 本金 |
| **Maturity** | Maturity | 到期 |

---

*PM产出 - M3 v1.0 (2026-07-05)*
*核心设计:共享主键 MM 表 + 后端 calculate + DealMap v2.0 多行单字段 + 9 态状态机 + 月末计提 + 提前还款罚息*
*本期聚焦 P0 同业拆借(LEND/BORROW),回购 REPO 占位 P1,同业存单 NCD 占位 P2*
