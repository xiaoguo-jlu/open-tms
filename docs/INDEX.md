# Open-TMS Document Index (Module-Based)

This index maps each Maven module to its associated documents. Use it to find PRDs, API docs, prototypes, and test cases for a specific area.

## Module Status

| Module | Status | Port | Documents Available |
|--------|--------|------|---------------------|
| basedata | ✅ Implemented | 8081 | Full PRD + API + tests |
| dealing | ✅ Implemented | 8082 | Full PRD + API + tests |
| fundplan | 📋 Planned | 8085 | PRD only |
| valuation | 📋 Planned | 8091 | PRD only |
| var | 📋 Planned | 8095 | PRD only |
| settlement | 📋 Planned | 8087 | PRD only |
| cockpit | 📋 Planned | 8096 | PRD only |
| report | 📋 Planned | 8097 | PRD only |
| common | ✅ Shared | — | Result/BaseEntity/Constants |

---

## basedata (基础数据)

**Status**: ✅ Implemented (merged bank account + instrument 2026-07-01)
**Port**: 8081
**Entities**: currency, country, bank, counterparty, trader, management-entity (formerly business-unit), currency-pair, rate, holiday, bank-account, instrument

### PRDs
- ✅ `M1-AC交易基础数据PRD.md` (archived — covered by main basedata docs)
- ✅ `M1-资金管理主体PRD.md` (formerly 业务单元)
- 📋 `M1-金融工具InstrumentPRD.md` (archived — merged into basedata)

### API Docs
- ✅ `api/basedata/01-instruments.md`
- ✅ `api/basedata/01-bank-accounts.md`
- ✅ `api/基础数据API文档.md` (master reference)
- ✅ `api/03-currency.md`

### Test Cases
- ✅ `testcase/api/基础数据API测试用例.md`
- ✅ `testcase/ui/基础数据UI测试用例.md`
- ✅ `testcase/BUG单_基础数据模块.md`
- ✅ `testcase/测试报告_基础数据模块.md`

### Prototypes
- ✅ `原型/country.html`, `bank.html`, `counterparty.html`, `trader.html`, `account.html`, `holiday.html`, `currency.html`, `management-entity.html` (renamed from business-unit.html)
- ✅ `原型/M1-基础数据模块UX原型.md`

---

## dealing (交易管理)

**Status**: ✅ Implemented (AC/AT full flow based on DealMap v2.0)
**Port**: 8082
**Entities**: deal, action, image, approval, cashflow

### PRDs
- ✅ `M1-Deal交易PRD-v5.md` (renamed from `M1-Deal交易 PRD v5.md`)
- ✅ `M1-AC交易PRD-v3.md` (renamed from `M1-AC交易ActualCashflow PRD v3.md`)
- ✅ `M1-AT交易PRD-v2.md`
- ✅ `M1-DealMapPRD-v2.md` (renamed from `M1-DealMap 生命周期事件PRD.md`)
- ✅ `M1-交易审批流程PRD.md`
- ✅ `M1-交易录入PRD-v1.md` (renamed from `M1-交易录入与管理PRD.md`)
- ✅ `M1-银行头寸管理PRD.md`
- ✅ `M1-账户转账AT交易PRD.md`

### API Docs
- ✅ `api/dealing/01-deal.md`
- ✅ `api/dealing/M1-AC交易API.md`
- ✅ `api/dealing/M1-AT交易API.md`

### Test Cases
- ✅ `testcase/dealing/AC交易测试用例.md`
- ✅ `testcase/dealing/M1-AT交易测试用例.md`
- ✅ `testcase/api/实际现金流AC测试用例.md`
- ✅ `testcase/api/账户转账AT测试用例.md`

### Test Reports
- ✅ `testreport/dealing/AC交易测试报告.md`
- ✅ `testreport/dealing/M1-AT交易测试报告.md`
- ✅ `testreport/dealing/集成测试报告-AC-AT.md`

### Prototypes
- ✅ `原型/ac-deal.html`, `at-deal.html`
- ✅ `原型/M1/M1-AC交易UX原型.md`
- ✅ `原型/M1/M1-AT交易UX原型.md`

### Bug Reports
- ✅ `bugreport/basedata/BUG_BASEDATA_001.md`

---

## fundplan (资金计划)

**Status**: 📋 Planned (M2)
**Port**: 8085

### PRDs
- 📋 `M2/M2-资金计划管理PRD.md` (will be renamed to `M2-资金计划PRD-v1.md`)

### API Docs
- 📋 `api/fundplan/01-fund-plan.md` (⚠️ [PLANNED — Not Implemented])

---

## valuation (估值)

**Status**: 📋 Planned (M3)
**Port**: 8091

### PRDs
- 📋 `M3/M3-金融工具估值PRD.md` (will be renamed to `M3-估值PRD-v1.md`)

### API Docs
- 📋 `api/valuation/01-valuation.md` (⚠️ [PLANNED — Not Implemented])

---

## var (市场风险 VaR)

**Status**: 📋 Planned (M4)
**Port**: 8095

### PRDs
- 📋 `M4/M4-市场风险VaRPRD.md` (will be renamed to `M4-VaRPRD-v1.md`)

### API Docs
- 📋 `api/var/01-var-report.md` (⚠️ [PLANNED — Not Implemented])

---

## settlement (结算)

**Status**: 📋 Planned (M2)
**Port**: 8087

### PRDs
- 📋 `M2/M2-支付结算PRD.md`

### API Docs
- 📋 `api/settlement/01-settlement.md` (⚠️ [PLANNED — Not Implemented])

---

## cockpit (驾驶舱)

**Status**: 📋 Planned (M5)
**Port**: 8096

### PRDs
- 📋 `M5/M5-管理驾驶舱PRD.md`

### API Docs
- 📋 `api/cockpit/01-cockpit.md` (⚠️ [PLANNED — Not Implemented])

---

## report (报表)

**Status**: 📋 Planned (M5)
**Port**: 8097

### PRDs
- 📋 `M5/M5-报表分析PRD.md`

### API Docs
- 📋 `api/report/01-report.md` (⚠️ [PLANNED — Not Implemented])

---

## Cross-Module Documents

### Shared Reference
- `prd/TMS交易类型及字段参考.md` → moved to `shared/reference/TMS交易类型及字段参考.md`
- `业务架构与功能清单.md` — Business architecture & feature inventory

### Architecture
- `architecture/business/AC交易与现金流分离架构设计.md` (REQUIRED READING)
- `architecture/business/DealMap落地分析.md` (REQUIRED READING)

### Standards
- `规范/Open-TMS开发规范文档.md` (REQUIRED READING)

### Reviews
- `reviews/code-review-20260604.md`

---

## Archive Index

Documents that have been moved to `docs/archive/`:

### `archive/2026-07-03-module-merge/`
PRDs and prototypes from modules that were merged or deleted in the 2026-07-03 refactor (18→6 modules):
- `M3-利率掉期PRD.md` (irs module deleted)
- `M2-现金池管理PRD.md` (cashpool module deleted)
- `M1-银行账户管理PRD.md` (bankaccount merged into basedata)
- `M1-金融工具InstrumentPRD.md` (instrument merged into basedata)
- `M1-AT金融工具Instrument PRD v1.md` (AC/AT instrument split)
- `原型/M3/fx-trading-prototype.html` (FX merged into dealing)

### `archive/2026-07-04-redundant-versions/`
Older PRD versions superseded by newer ones:
- `M1-AC交易ActualCashflow PRD v2.md` (v3 exists)
- `M1-AC金融工具Instrument PRD v2.md` (v3 exists)
- `M1-Deal交易 PRD v4.md` (v5 exists)
- `M1-实际现金流AC交易PRD.md` (covered by AC v3)

### `archive/2026-07-04-superseded/`
- `M1-基础数据PRD-v0.md` (early version, superseded by module-specific PRDs)

### `archive/2026-06-01-deal-design-discussion/`
- `交易设计-AC-chat-0601.txt` (original design discussion for AC deals)

### `archive/2026-04-06-basedata-impl/`
- `2026-04-06-basedata-impl.md` (early implementation plan, now complete)