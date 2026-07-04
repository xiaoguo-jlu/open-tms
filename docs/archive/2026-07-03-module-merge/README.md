# Archive: 2026-07-03 Module Merge / Deletion

This directory archives PRD and prototype documents from modules that were merged into other modules or deleted in the 2026-07-03 module refactor (commit `105a4dc`, 18→6 modules).

## Archived Files

### PRDs (from deleted/merged modules)

| Archived File | Original Location | Reason |
|---------------|-------------------|--------|
| `M3-利率掉期PRD.md` | `prd/M3/M3-利率掉期PRD.md` | `irs` module deleted in refactor |
| `M2-现金池管理PRD.md` | `prd/M2/M2-现金池管理PRD.md` | `cashpool` module deleted in refactor |
| `M1-银行账户管理PRD.md` | `prd/M1/M1-银行账户管理PRD.md` | `bankaccount` merged into `basedata` |
| `M1-金融工具InstrumentPRD.md` | `prd/M1/M1-金融工具InstrumentPRD.md` | `instrument` merged into `basedata` |
| `M1-AT金融工具Instrument PRD v1.md` | `prd/M1/M1-AT金融工具Instrument PRD v1.md` | AC/AT instrument split — only v1 was for AT; superseded |

### Prototypes

| Archived File | Original Location | Reason |
|---------------|-------------------|--------|
| `fx-trading-prototype.html` | `原型/M3/fx-trading-prototype.html` | FX merged into `dealing` |

## Module Refactor Context

Before 2026-07-03: 18 Maven modules
After: 6 Maven modules (basedata, dealing, fundplan, valuation, var, settlement)

Mergers:
- `bankaccount` + `instrument` → `basedata` (single port 8081)
- `fx` → `dealing` (FX deals handled in dealing module)

Deletions:
- `cashpool` (cash pool management deferred)
- `irs` (interest rate swap deferred)

## Archive Date

2026-07-03 (commit `105a4dc`) — Phase 1.4 of docs/ cleanup on 2026-07-04