# Open-TMS Documentation

Open-TMS (Open Treasury Management System) is an enterprise-grade treasury management system for global corporations, benchmarked against FIS Quantum, SAP TRM, Murex MX.3, and Kyriba. It covers the full treasury lifecycle from foundational master data through trading, settlement, valuation, risk analytics, and executive dashboards.

## Documentation Index

| File | Purpose |
|------|---------|
| [INDEX.md](./INDEX.md) | Module-based document index — find docs by Maven module |
| [SUMMARY.md](./SUMMARY.md) | Project state snapshot — module status, recent changes, pending TODOs |
| [CHANGELOG.md](./CHANGELOG.md) | Documentation change log — reverse chronological |

## Directory Structure

```
docs/
├── README.md                        # This file — docs entry point
├── INDEX.md                         # Module-based document index
├── SUMMARY.md                       # Project state snapshot
├── CHANGELOG.md                     # Documentation change log
├── 规范/                            # Mandatory coding standards (read first)
├── architecture/                    # Business architecture & design
│   └── business/                    # AC/Cashflow separation, DealMap
├── prd/                             # Product Requirements Documents
│   ├── M1/  M2/  M3/  M4/  M5/      # Phase-organized PRDs
│   └── shared reference docs        # TMS field reference, etc.
├── api/                             # REST API documentation
│   ├── basedata/  dealing/  ...
│   └── 基础数据API文档.md
├── testcase/                        # Test cases (API / UI / reports)
├── testreport/                      # Test execution reports
├── bugreport/                       # Bug reports by module
├── 原型/                            # UX prototypes (HTML + MD)
├── reviews/                         # Code reviews
├── superpowers/plans/               # Implementation plans
├── 关键对话/                        # Important design discussions
├── shared/reference/                # Cross-cutting reference docs
└── archive/                         # Archived documents (read-only)
```

## Quick Start

New to the project? Read in this order:

1. [CLAUDE.md](../CLAUDE.md) — Project overview, tech stack, build commands, key conventions
2. [规范/Open-TMS开发规范文档.md](./规范/Open-TMS开发规范文档.md) — Mandatory development standards
3. [INDEX.md](./INDEX.md) — Find docs for your module
4. [SUMMARY.md](./SUMMARY.md) — Current project state and recent changes

## Required Reading

These three files are referenced by `CLAUDE.md` and must not be moved or renamed:

1. **[规范/Open-TMS开发规范文档.md](./规范/Open-TMS开发规范文档.md)** — Mandatory coding standards (DB naming, REST patterns, audit fields, etc.)
2. **[architecture/business/AC交易与现金流分离架构设计.md](./architecture/business/AC交易与现金流分离架构设计.md)** — AC/Cashflow separation (Murex MX.3 model)
3. **[architecture/business/DealMap落地分析.md](./architecture/business/DealMap落地分析.md)** — DealMap v2.0 implementation analysis

## Document Conventions

### Naming

- **PRD files**: `M{n}-{中文名}PRD-v{m}.md` (e.g., `M1-AC交易PRD-v3.md`)
- **API docs**: `docs/api/{module}/NN-{entity}.md`
- **Test cases**: `docs/testcase/{api|ui|dealing}/{entity}测试用例.md`
- **Archive directories**: `docs/archive/YYYY-MM-DD-{reason}/`

### Status Markers

- ✅ Implemented and live
- 🔄 In progress
- 📋 Planned
- ❌ Not started
- ⚠️ [PLANNED — Not Implemented] — Header marker on API docs for unimplemented modules

### Content Standards

- Use snake_case for DB columns (auto-mapped from camelCase Java fields)
- Audit fields required on all tables: `created_by`, `created_at`, `updated_by`, `updated_at`, `version`, `deleted`
- Decimal precision: `DECIMAL(18,2)` standard, `DECIMAL(18,8)` FX rates, `DECIMAL(10,4)` interest rates, `DECIMAL(38,18)` for AC Deal/Cashflow
- REST: POST for all write operations (`/update`, `/delete/{id}`), never PUT/DELETE