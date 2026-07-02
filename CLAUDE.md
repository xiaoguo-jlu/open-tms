# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**Open-TMS (Open Treasury Management System)** — 面向全球集团企业的企业级资金管理系统,对标 **FIS Quantum / SAP TRM / Murex MX.3 / Kyriba**。

**业务架构**:五层模型 — 决策支持层(Cockpit/AI) → 核心业务层(流动性/投融资/风险) → 基础操作层(账户/结算/对账) → 集成连接层(银企/ERP/市场) → 基础支撑层(权限/工作流/审计)。

**当前阶段**:**M1 基础数据 + AC/AT 交易全流程已贯通**,正在向 M2 资金运营迈进。完整路线图见 `summary.md`。

### Tech Stack
- **Backend**: Java 17, SpringBoot 3.2.0, **Apache CXF 4.0.3 (JAX-RS)**, MyBatis Plus 3.5.5, Lombok 1.18.30
- **Frontend**: Vue 3, Element Plus, Vite
- **Database**: PostgreSQL 42.7.1 + Redis (Redisson 3.25.0)
- **Build**: Maven 多模块(18 个子模块)
- **架构**:单库多服务 — 所有业务模块共享同一 PG 库 `opentms`,通过端口区分

---

## Build & Run Commands

### Backend
```bash
# 构建全部模块
mvn clean install

# 构建单个模块(项目根目录执行)
mvn clean package -pl basedata -am

# 启动后端(默认 8081)
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar

# 自定义端口
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081
```

**模块端口分配**(单库多服务):
| 模块 | 端口 | 状态 |
|------|------|------|
| basedata | 8081 | ✅ (含银行账户 + 金融工具,均已合并) |
| dealing | 8082 | ✅ AC/AT 全流程 |
| valuation | 8091 | 🔄 估值 |
| var | 8095 | 🔄 VaR |
| settlement | 8087 | 🔄 结算 |
| cockpit | 8096 | 📋 驾驶舱 |
| report | 8097 | 📋 报表 |
| fundplan | 8085 | 📋 |
| cashpool | 8086 | 📋 |
| fx/irs/valuation/var | 8089-8095 | 📋 |
| cockpit | 8096 | 📋 |

### Frontend (Vite)
```bash
cd web
npm install
npm run dev      # 启动 dev server
npm run build    # 生产构建
```

---

## Testing (scripts/test/)

```bash
# 全部测试
python scripts/test/test_all.py

# 基础数据全量测试
python scripts/test/test_basedata_all.py

# 特定 API 测试
python scripts/test/test_country_api.py
python scripts/test/test_ac_deal_api.py
python scripts/test/test_at_deal_api.py
python scripts/test/test_instrument_api.py
python scripts/test/test_deal_api.py

# 特定 UI 测试
python scripts/test/test_country_ui.py
python scripts/test/test_at_deal_ui.py
python scripts/test/test_instrument_ui.py
python scripts/test/test_deal_ui.py

# 启动后端并测试
python scripts/test/start_test.py
```

**测试质量要求**(强制):
- UI 测试必须执行**功能验证**,不只是元素检查
- API 测试必须验证**响应数据正确性**
- 任何操作后检查错误提示
- 慢请求(>1s)需要记录

---

## Database Tool (scripts/db/)

```bash
python scripts/db/db_tool.py -t                          # 列出所有表
python scripts/db/db_tool.py -d tms_country_t            # 描述表结构
python scripts/db/db_tool.py -q tms_country_t            # 查询表数据
python scripts/db/db_tool.py -s "SELECT * FROM ..."      # 执行 SQL
python scripts/db/db_tool.py -f                          # 修复缺失 remark 列
python scripts/db/db_tool.py -r db/schema/fix.sql        # 执行 SQL 文件
```

数据库:PostgreSQL `localhost:5432`,库名 `opentms`,user/pwd = `opentms/opentms123`。

---

## Architecture

### Maven Module Structure (18 modules)
```
opentms-parent (pom.xml)
├── common/          # 公共基础(Result/BaseEntity/BaseCodeEntity/GlobalConstants/MybatisPlusConfig)
├── basedata/        # 基础数据(14 Resource,含银行账户 + 金融工具,2026-07-01 合并自 instrument 模块)
├── dealing/         # 交易管理(AC/AT Deal + Action + Image)
├── fundplan/        # 资金计划
├── valuation/       # 估值
└── var/             # VaR
├── fundplan/        # 资金计划
├── fx/              # 外汇交易
├── irs/             # 利率掉期
├── valuation/       # 估值
├── var/             # VaR
├── exposure/        # 敞口
├── hedge/           # 套保
├── impairment/      # 减值(IFRS 9)
├── limit/           # 限额
├── settlement/      # 结算
├── cockpit/         # 驾驶舱
└── report/          # 报表
```

### Code Organization (per module)
```
src/main/java/com/opentms/{module}/
├── controller/      # REST 端点(/api/v1/{resource})
├── service/         # 业务逻辑(impl 实现类)
├── mapper/          # MyBatis Plus 数据访问
├── entity/          # 持久化实体(继承 BaseEntity/BaseCodeEntity)
├── dto/             # 入参对象
├── vo/              # 出参对象
└── enums/           # 业务枚举(若需)
```

**强制分层**:入参 `dto` / 出参 `vo` / 持久化 `entity` 必须分文件;Controller 接收 DTO,Service 返回 VO(用 `BeanUtil.copyProperties` 转换)。

### Core Business Object Relationship
```
Instrument(产品定义)
    │ 1:N
Deal(交易单据 AC/AT/FX) ──审批──▶ ApprovalTask
    │ 1:N (execute 后)
Cashflow(实际现金流,独立生命周期) ──对账──▶ Reconciliation
    │
Position(头寸更新)
```

### Transaction State Machine
- **DealStatus**: `New → Submitted → Approved → Settled` / `Rejected` / `Canceled`
- **ActionStatus**: `Pending → Approved/Rejected → Executed`
- **ApprovalStatus**: `Pending → Approved/Rejected`
- **ImageType**: `CREATE / UPDATE / DELETE`

---

## Key Conventions

### Database Naming
- 表名:`tms_{module}_{type}` — 类型后缀 `_t` 主表 / `_d` 字典 / `_log` 日志 / `_rel` 关联 / `_his` 历史
- 示例:`tms_country_t`、`tms_business_unit_t`、`tms_deal_t`、`tms_deal_action_t`
- 主键:`id BIGSERIAL PRIMARY KEY`(PG 自增)
- 业务编码:`xxx_code VARCHAR(50) NOT NULL UNIQUE`
- 业务流水号:`xxx_no VARCHAR(50) NOT NULL UNIQUE`(如 `AC20260629-0001`、`CF20260629-0001`)

### Required Audit Fields(全表必备)
```sql
created_by  VARCHAR(50)  NOT NULL
created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by  VARCHAR(50)
updated_at  TIMESTAMP
version     INT          DEFAULT 0   -- 乐观锁(@Version)
deleted     CHAR(1)      DEFAULT '0' -- 软删除(@TableLogic)
```

### Amount Precision
- 普通金额:`DECIMAL(18,2)`
- 汇率:`DECIMAL(18,8)`
- 利率:`DECIMAL(10,4)`
- **高精度(AC Deal/Cashflow):`DECIMAL(38,18)`** ← 资金交易强制

### REST API Patterns(POST 统一 update/delete)
```
GET    /api/v1/{resource}/page         # 分页查询
GET    /api/v1/{resource}/{id}         # 详情查询
POST   /api/v1/{resource}              # 新增
POST   /api/v1/{resource}/update       # 更新(2026-05-31 后统一)
POST   /api/v1/{resource}/delete/{id}  # 删除(2026-05-31 后统一)
POST   /api/v1/{resource}/{id}/submit  # 提交审批
POST   /api/v1/{resource}/{id}/approve # 审批通过
POST   /api/v1/{resource}/{id}/reject  # 审批驳回
POST   /api/v1/{resource}/{id}/execute # 执行(Deal 专属)
```

> ⚠️ **红线**:update/delete 一律 POST,不要用 PUT/DELETE(部分代理/网关不友好)。

### Idempotency
- 请求头:`X-Idempotency-Key: <唯一标识>`
- 重复请求返回原结果,响应体含 `idempotent: true`
- 交易表必须含 `idempotency_key VARCHAR(64)`,配套幂等表 `tms_idempotency_t`

### API Response Format (Result<T>)
```json
{ "code": 200, "message": "success", "data": {...}, "timestamp": 1704067200000 }
```
错误码:`200` 成功 / `400` 业务异常 / `401` 未授权 / `403` 无权限 / `404` 不存在 / `500` 系统异常

分页响应:
```json
{ "code": 200, "data": { "records": [...], "total": 100, "size": 20, "current": 1 } }
```

### Global Enums(GlobalConstants)
禁止各模块自建字符串,所有状态/类型/交易类型统一从 `com.opentms.common.constant.GlobalConstants` 取:
- **DealType**:`AC`(实体现金) / `AT`(内部转账) / `FX`(外汇)
- **InstrumentType**:`AC/AT/FX/DEPOSIT/LOAN/IR/EQ/BOND/SWAP/OPTION/FORWARD/OTHER`
- **Status**:`STATUS_ENABLED/DISABLED`(`1`/`0`)
- **ActionType**:`CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE`

### Entity Conventions
- 继承 `BaseCodeEntity`(代码-名称-状态型)或 `BaseEntity`(通用)
- 驼峰字段(`countryNo`)→ 蛇形列(`country_no`),MyBatis Plus 自动映射
- 自定义映射用 `@TableName` / `@TableField` 注解

### Frontend Conventions
- API 调用统一放 `web/src/api/{module}/{entity}.js`,函数命名 `listX/getX/saveX/updateX/deleteX`
- 列表页四件套:搜索区 + 工具栏 + 表格 + 分页
- 状态展示:`<el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>`
- 视图目录:`web/src/views/{module}/` — 已有 ac/approval/basedata/cashpool/dashboard/dealing/deposit/fundplan/fx/irs/loan/report/risk/transfer 等

### Transaction(写操作)
```java
@Transactional(rollbackFor = Exception.class)  // 写操作必须加 rollbackFor
@Transactional(readOnly = true)                // 读操作加 readOnly
```

### Logging
- 关键节点:`log.info("[{}] 用户[{}] 执行[{}]操作, 业务编号[{}]", opType, user, entity, no)`
- 异常:`log.error("交易[{}] 执行失败: {}", txNo, e.getMessage(), e)` — 必须含堆栈
- 不在生产输出 SQL 参数(`log.debug`)

### Git Commit Convention
```
<type>(<scope>): <subject>
type: feat/fix/docs/style/refactor/test/chore
示例: feat(dealing): AC 交易全流程研发
```

---

## Important Notes

### 必读文档
- `summary.md`(项目根) — 完整功能清单、模块状态、近期开发历史
- `docs/规范/Open-TMS开发规范文档.md`(32KB) — 详细后端/前端编码规范、数据库设计模板
- `docs/architecture/business/AC交易与现金流分离架构设计.md` — **AC/AT 核心架构**(Murex MX.3 模型)
- `docs/architecture/business/DealMap落地分析.md` — DealMap v2.0 落地方案
- `open-tms团队协作规范.md` — 团队协作与开发流程
- `m1研发计划.md` — M1 阶段研发任务
- `open-tms功能特性清单.md` — 产品功能全景

### 关键文件
- `pom.xml`(项目根) — Maven 父 POM,版本号锁定
- `common/src/main/java/com/opentms/common/` — 公共基础(禁止往这里加业务代码)
- `scripts/db/db_tool.py` — 数据库操作(改表前必查)
- `scripts/test/start_test.py` — 后端启动与测试入口
- `scripts/maintain/refresh_project_state.py` — 刷新项目状态快照

### 工作流红线
- 业务模块间禁止循环依赖;共享类必须放 `common`
- 业务异常抛 `BusinessException`,由 `GlobalExceptionHandler` 统一捕获
- 敏感数据必须脱敏(`@SensitiveInfo`),密码 BCrypt 加密
- 新功能先确定属于**五层架构**哪一层、哪个 Maven 模块
- 状态字符串先查 `GlobalConstants`,不要新建魔术字符串
- 新表/字段命名严格 snake_case,审计字段不可缺

### 当前重点(2026-06)
- ✅ AC/AT 交易全流程研发完成(基于 DealMap v2.0)— 提交 `a2f36ff`
- ✅ Instrument 模块回归基础数据
- 🔄 完善测试套件(API + UI 自动化)
- 📋 启动 M2 资金运营模块

### Claude 协作提示
- 项目配置了 `.claude/skills/` 体系(pm-lead / business-architect / api-design / db-design / frontend-dev / backend-dev / qa / ux / product / test 等)
- 复杂任务可调用 `opentms-feature-dev` skill 进行 PM→UX→DB→API→FE→BE→QA 全链路编排
- 项目记忆索引:`~/.claude/projects/E--code-project-open-tms-open-tms/memory/MEMORY.md`
