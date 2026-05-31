# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Open-TMS (Open Treasury Management System) - an enterprise-grade Treasury Management System targeting global conglomerate enterprises. Currently in M1 (Foundation Data Module) development phase.

**Tech Stack**:
- Backend: Java 17, SpringBoot 3.2.0, MyBatis Plus 3.5.5, Apache CXF 4.0.3
- Frontend: Vue3, Element Plus
- Database: PostgreSQL + Redis (Redisson 3.25.0)
- Build: Maven multi-module

## Build & Run Commands

### Backend
```bash
# Build all modules
mvn clean install

# Build specific module (from project root)
cd basedata && mvn clean package

# Run backend (port 8081)
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar

# Run with custom port
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081
```

### Frontend (Vite Dev Server)
```bash
cd web
npm install
npm run dev
```

### Testing (scripts/test/)
```bash
# Run all tests
python scripts/test/test_all.py

# Run specific API test
python scripts/test/test_country_api.py

# Run UI test
python scripts/test/test_country_ui.py

# Start backend and test endpoint
python scripts/test/start_test.py
```

### Database Tool (scripts/db/)
```bash
# List all tables
python scripts/db/db_tool.py -t

# Describe table structure
python db_tool.py -d tms_country_t

# Query table data
python db_tool.py -q tms_country_t

# Execute SQL
python db_tool.py -s "SELECT * FROM ..."

# Fix missing remark columns
python scripts/db/db_tool.py -f

# Execute SQL file
python scripts/db/db_tool.py -r db/schema/fix.sql
```

## Architecture

### Maven Module Structure
```
opentms-parent (pom.xml)
├── common/          # Shared config, constants, utilities, Result class
├── basedata/        # Foundation data (Bank, Country, Currency, Trader, Subsidiary, CurrencyPair, ApprovalRule, etc.)
├── dealing/         # Transaction management
├── cashpool/        # Cash pool management
├── fundplan/        # Fund planning
├── fx/              # Foreign exchange
├── irs/             # Interest rate swaps
├── valuation/       # Financial instrument valuation
└── var/             # Value-at-Risk reporting
```

### Code Organization (per module)
```
src/main/java/com/opentms/{module}/
├── controller/      # REST endpoints (/api/v1/{module})
├── service/         # Business logic
├── mapper/          # MyBatis Plus data access
├── entity/          # Database entity classes
├── dto/             # Request data transfer objects
└── vo/              # Response view objects
```

### API Response Format
All APIs return Result wrapper:
```json
{"code": 200, "message": "success", "data": {...}, "timestamp": 1704067200000}
```
- Success: `code: 200`
- Business error: `code: 400`
- Not found: `code: 404`
- System error: `code: 500`

### Pagination Response (MyBatis Plus)
```json
{"code": 200, "data": {"records": [...], "total": 100, "size": 20, "current": 1}}
```

## Key Conventions

### Database Naming
- Tables: `tms_{module}_{type}` (e.g., `tms_country_t`)
- Entity fields map to snake_case DB columns via MyBatis Plus
- Standard audit fields: `created_by`, `created_at`, `updated_by`, `updated_at`, `version`, `deleted`

### REST API Patterns
```
GET    /api/v1/{resource}/page        # Pagination query
GET    /api/v1/{resource}/{id}        # Get by ID
POST   /api/v1/{resource}             # Create
POST   /api/v1/{resource}/update      # Update (统一POST)
POST   /api/v1/{resource}/delete/{id} # Delete (统一POST)
```

> **Note**: API methods for update/delete are unified to POST (2026-05-31)

### Entity Conventions
- Country: `countryNo` field → database column `country_no` (MyBatis Plus auto-mapping)
- Use `@TableName` and `@TableField` annotations for custom mappings

## Important Notes

- The `summary.md` in project root contains comprehensive project documentation including feature list, architecture diagrams, module status, and recent development history
- The `docs/规范/Open-TMS开发规范文档.md` contains detailed coding standards for Java (backend) and Vue (frontend) - always follow these when implementing features
- The `docs/规范/Open-TMS开发规范文档.md` also contains database design templates and naming conventions
- Key files like pom.xml (Maven config), `scripts/db/db_tool.py` (database operations), and `scripts/test/start_test.py` (backend testing) should be consulted when working on their respective domains