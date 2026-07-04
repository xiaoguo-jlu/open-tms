# Documentation Change Log

Reverse chronological order (newest first).

## 2026-07-04 — docs/ Phase 1+2 cleanup

**Phase 1 — Quick Wins**

- Archived 4 redundant PRD versions to `docs/archive/2026-07-04-redundant-versions/`:
  - `M1-AC交易ActualCashflow PRD v2.md` (v3 exists)
  - `M1-AC金融工具Instrument PRD v2.md` (v3 exists)
  - `M1-Deal交易 PRD v4.md` (v5 exists)
  - `M1-实际现金流AC交易PRD.md` (covered by AC v3)
- Created 4 top-level index files: `docs/README.md`, `docs/INDEX.md`, `docs/SUMMARY.md`, `docs/CHANGELOG.md`
- Added `[PLANNED — Not Implemented]` header marker to 6 unimplemented module API docs:
  - `api/cockpit/01-cockpit.md` (M5)
  - `api/fundplan/01-fund-plan.md` (M2)
  - `api/settlement/01-settlement.md` (M2)
  - `api/valuation/01-valuation.md` (M3)
  - `api/var/01-var-report.md` (M4)
  - `api/report/01-report.md` (M5)
- Archived 6 documents from merged/deleted modules to `docs/archive/2026-07-03-module-merge/`:
  - `M3-利率掉期PRD.md` (irs deleted)
  - `M2-现金池管理PRD.md` (cashpool deleted)
  - `M1-银行账户管理PRD.md` (bankaccount → basedata)
  - `M1-金融工具InstrumentPRD.md` (instrument → basedata)
  - `M1-AT金融工具Instrument PRD v1.md` (AC/AT split)
  - `原型/M3/fx-trading-prototype.html` (FX → dealing)

**Phase 2 — Medium Effort**

- Standardized PRD file naming to `M{n}-{中文名}PRD-v{m}.md`:
  - `M1-AC交易ActualCashflow PRD v3.md` → `M1-AC交易PRD-v3.md`
  - `M1-Deal交易 PRD v5.md` → `M1-Deal交易PRD-v5.md`
  - `M1-DealMap 生命周期事件PRD.md` → `M1-DealMapPRD-v2.md`
  - `M1-交易审批流程PRD.md` → `M1-交易审批流程PRD-v1.md`
  - `M1-银行头寸管理PRD.md` → `M1-银行头寸管理PRD-v1.md`
  - `M1-交易录入与管理PRD.md` → `M1-交易录入PRD-v1.md`
  - `M3-金融工具估值PRD.md` → `M3-估值PRD-v1.md`
  - `M4-市场风险VaRPRD.md` → `M4-VaRPRD-v1.md`
  - `M2-资金计划管理PRD.md` → `M2-资金计划PRD-v1.md`
  - `M1-资金管理主体PRD.md` → `M1-资金管理主体PRD-v1.md`
  - `M1-组织架构与权限管理PRD.md` → `M1-组织权限PRD-v1.md`
- Fixed REST API path conventions in API docs: PUT/DELETE → POST `/update` / `/delete/{id}`
- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUnit` → management variants) across all docs, test cases, and prototypes after DB/API rename verified
- Moved `prd/交易录入PRD.md` → `prd/M1/M1-交易录入PRD-v1.md`
- Archived `prd/基础数据PRD.md` → `archive/2026-07-04-superseded/M1-基础数据PRD-v0.md`
- Moved `prd/TMS交易类型及字段参考.md` → `shared/reference/TMS交易类型及字段参考.md`
- Renamed `业务架构与功能清单` (no ext) → `业务架构与功能清单.md`
- Moved `关键对话/交易设计-AC-chat-0601.txt` → `archive/2026-06-01-deal-design-discussion/`
- Moved `superpowers/plans/2026-04-06-basedata-impl.md` → `archive/2026-04-06-basedata-impl/`
- Renamed prototype: `原型/business-unit.html` → `原型/management-entity.html`

## 2026-07-03 — Module refactor (18→6 modules)

- Module consolidation: basedata, dealing, fundplan, valuation, var, settlement (6 active)
- Bank account + instrument merged into basedata (single port 8081)
- FX module merged into dealing
- API normalization across all modules
- Test suite hardening
- Commit: `105a4dc` (merge PR #8)
- Commit: `a47f49a` refactor: 模块整合 18→6 + 接口规范化 + 测试加固

## 2026-07-01 — Business Unit → Management Entity rename

- DB: `tms_business_unit_t` → `tms_management_entity_t`
- API path: `/api/v1/business-units/` → `/api/v1/management-entities/`
- Java: `BusinessUnit` → `ManagementEntity`
- All test cases and documentation updated
- Aligns with treasury domain terminology (管理主体 is the standard term in Murex/FIS)

## 2026-06-29 — AC/AT full flow (DealMap v2.0)

- AC deal full lifecycle implemented
- AT deal with deal map
- Action + Image tracking
- Idempotency on writes
- Test reports filed in `testreport/dealing/`
- Commit: `a2f36ff` feat(dealing): AC/AT交易全流程研发完成

## 2026-06-14 — AC/Cashflow separation architecture

- `architecture/business/AC交易与现金流分离架构设计.md` finalized
- Murex MX.3 model adopted

## 2026-06-04 — Code review

- `reviews/code-review-20260604.md` produced
- 18 findings, all closed

## 2026-05-31 — Standards finalized

- `规范/Open-TMS开发规范文档.md` finalized (32KB)

## 2026-04-06 — Basedata plan

- Initial basedata implementation plan produced
- Now archived to `archive/2026-04-06-basedata-impl/` (work complete)