# 业界对标参考标准

> 参考成熟资金管理系统:FIS Quantum / Murex MX.3 / SAP TRM / Kyriba / Bloomberg AIM
> 用以补强 Open-TMS 现有规范未覆盖的审核点。

---

## FIS Quantum 业务对象清单(节选)

| 域 | 对象 | 核心字段数 |
|----|------|-----------|
| Static | Currency, Country, Holiday | 30+ |
| Static | Bank, Counterparty, Issuer | 50+ |
| Static | Instrument (Bond/Loan/Deposit/FX/IRS) | 80+ |
| Trading | Deal, Ticket, Order | 60+ |
| Trading | Action, Amendment, Cancellation | 40+ |
| Position | Position, P&L, Exposure | 50+ |
| Cashflow | Cashflow, Schedule, Settlement | 70+ |
| Risk | VaR, Greeks, Sensitivities | 60+ |

> FIS Quantum 单个 Deal 对象约 60+ 字段,Open-TMS 当前 AC Deal 应保证 ≥ 30 个核心字段。

---

## Murex MX.3 业务对象清单(节选)

| 模块 | 对象 | 状态机复杂度 |
|------|------|--------------|
| MX.3 Trade | Trade Event | 12 个状态 |
| MX.3 Trade | Confirmation | 10 个状态 |
| MX.3 Cashflow | Cashflow Generation | 8 个状态 |
| MX.3 Limits | Limit Breach | 15 个状态 |
| MX.3 Limits | Approval Workflow | 10 个状态 |

> Murex 平均每个核心对象 8-15 个状态,含初始/中间/终态/异常。

---

## SAP TRM 业务对象清单(节选)

| 模块 | 对象 | 关键字段 |
|------|------|----------|
| TRM Treasury | Transaction (TPM) | 70+ |
| TRM Money Market | Deposit / Loan | 50+ |
| TRM FX | FX Deal | 60+ |
| TRM Securities | Security | 100+ |

---

## Kyriba 业务对象清单(节选)

| 模块 | 对象 |
|------|------|
| Cash | Bank Account, Cash Position |
| Liquidity | Forecast, Scenario |
| Payments | Payment Order, Approval |
| FX | FX Deal, Hedge |

---

## 业界核心能力覆盖对标

| 能力 | FIS Quantum | Murex MX.3 | SAP TRM | Kyriba | Open-TMS M1 | Open-TMS M2 |
|------|-------------|------------|---------|--------|-------------|-------------|
| 多法律实体 | ✓ | ✓ | ✓ | ✓ | 🔄 部分 | 📋 待规划 |
| 多账套/多账簿 | ✓ | ✓ | ✓ | ✓ | 📋 未规划 | 📋 待规划 |
| 多币种 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 完整状态机 | ✓ | ✓ | ✓ | ✓ | ✓ 部分 | 📋 待规划 |
| 软删除 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 乐观锁 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 审计日志 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 幂等接口 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 现金流独立 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 影像归档 | ✓ | ✓ | ✓ | ✓ | ✓ 已贯通 | — |
| 工作流引擎 | ✓ | ✓ | ✓ | ✓ | 📋 未规划 | 📋 待规划 |
| 实时风控 | ✓ | ✓ | ✓ | ✓ | 📋 未规划 | 📋 待规划 |
| 多机构汇率 | ✓ | ✓ | ✓ | ✓ | 🔄 部分 | 📋 待规划 |

> 图例:✓ 已贯通 / 🔄 部分覆盖 / 📋 未规划

---

## Open-TMS 现有规范补强(CLAUDE.md 提炼)

### 强制审计字段(全表必备)

```
created_by  VARCHAR(50)  NOT NULL
created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by  VARCHAR(50)
updated_at  TIMESTAMP
version     INT          DEFAULT 0
deleted     CHAR(1)      DEFAULT '0'
```

### 金额精度

| 类型 | 精度 |
|------|------|
| 普通金额 | DECIMAL(18,2) |
| 汇率 | DECIMAL(18,8) |
| 利率 | DECIMAL(10,4) |
| AC Deal / Cashflow | DECIMAL(38,18) |

### REST API 路径规范

```
/api/v1/{resource}/page         # 分页
/api/v1/{resource}/{id}         # 详情
/api/v1/{resource}              # 新增
/api/v1/{resource}/update       # 更新
/api/v1/{resource}/delete/{id}  # 删除
```

### 响应格式

```json
{ "code": 200, "message": "success", "data": {...}, "timestamp": 1704067200000 }
```

---

## 业界审核项补强清单

> 以下条目已在 SKILL.md 的 checklist 中结构化,此处补充解释。

### REQ-005 多法律实体/多账套

- FIS Quantum 单实例支持 50+ legal entity
- Murex MX.3 通过 partition 实现多账套隔离
- Open-TMS 当前 M1 仅单 legal entity,M2 需扩展

### REQ-006 字段单义性

- FIS Quantum 字段命名严格自解释,如 `trade_date` / `value_date` / `settlement_date`
- 禁止 status 同时表达"业务状态"和"启用/禁用"(拆分为 status + enabled)

### REQ-007 业务规则可配置

- 阈值/税率/限额类必须配置化,不得硬编码
- Murex 通过 @ConfigurableRule 实现,FIS Quantum 通过 Rule Engine

### REQ-008 状态机完整性

- Murex 平均每个对象 8-15 个状态
- 必须含 New/中间态/Terminal/Exception 四类

### REQ-009 金额/币种成对

- 任何 amount 字段必须有对应 currency 字段
- 例外:币种无关字段(如汇率本身)

### REQ-010 时区/日期语义

- trade_date(交易日) / value_date(起息日) / settlement_date(交割日)
- 三个日期语义独立,不可混用
- 时间字段应带时区(TIMESTAMP WITH TIME ZONE)

### REQ-013 合规审计

- 金融监管要求数据保留 ≥ 5 年(中国 / 美国 / 欧盟)
- 审计日志不可篡改(append-only + 数字签名)

### REQ-014 术语一致性

Open-TMS 统一术语:

| 中文 | 英文 | 字段 |
|------|------|------|
| 管理主体 | Management Entity | legal_entity |
| 对手方 | Counterparty | counterparty |
| 币种对 | Currency Pair | currency_pair |
| 金融工具 | Instrument | instrument |
| 账套 | Book | book |
| 交易日 | Trade Date | trade_date |
| 起息日 | Value Date | value_date |
| 交割日 | Settlement Date | settlement_date |