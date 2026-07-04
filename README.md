# Open-TMS (Open Treasury Management System)

面向全球集团企业的**企业级资金管理系统**,对标 **FIS Quantum / SAP TRM / Murex MX.3 / Kyriba**。

---

## 项目状态

- **当前阶段**:M1 基础数据 + AC/AT 交易全流程已贯通(2026-07)
- **下一步**:M2 资金运营模块筹备中(资金计划/资金池/结算/驾驶舱)
- 详细功能与开发历史见 [`summary.md`](./summary.md)

## 技术栈

| 类别 | 选型 |
|------|------|
| 后端语言 | Java 17 |
| 后端框架 | Spring Boot 3.2.0 |
| Web 服务 | Apache CXF 4.0.3 (JAX-RS) |
| ORM | MyBatis Plus 3.5.5 |
| 数据库 | PostgreSQL 42.7.1 + Redis (Redisson 3.25.0) |
| 构建工具 | Maven 多模块(6 个子模块,2026-07-03 整合自 18) |
| 前端框架 | Vue 3 + Element Plus + Vite |

## 业务架构(五层模型)

```
决策支持层 (Cockpit / AI)
   └─> 核心业务层 (流动性 / 投融资 / 风险)
         └─> 基础操作层 (账户 / 结算 / 对账)
               └─> 集成连接层 (银企 / ERP / 市场)
                     └─> 基础支撑层 (权限 / 工作流 / 审计)
```

## 快速开始

### 后端(默认端口 8081)

```bash
# 构建 basedata + dealing(常用模块)
mvn clean package -pl basedata,dealing -am

# 启动 basedata
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar

# 自定义端口
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081
```

### 前端(开发服务器端口 3000)

```bash
cd web
npm install
npm run dev    # 启动 dev server(默认 http://localhost:3000)
npm run build  # 生产构建
```

## 模块端口分配(单库多服务)

所有业务模块共享同一 PostgreSQL 库 `opentms`,通过端口区分服务。

| 模块 | 端口 | 状态 | 说明 |
|------|------|------|------|
| basedata | 8081 | 已上线 | 基础数据(银行账户 + 金融工具) |
| dealing | 8082 | 已上线 | AC/AT 交易全流程 |
| settlement | 8087 | 规划中 | 结算 |
| valuation | 8091 | 规划中 | 估值 |
| var | 8095 | 规划中 | VaR 风险计量 |
| cockpit | 8096 | 规划中 | 驾驶舱 |
| report | 8097 | 规划中 | 报表 |

## 数据库

- **DBMS**:PostgreSQL `localhost:5432`
- **库名**:`opentms`
- **账号**:`opentms / opentms123`

## 文档导航

- [`CLAUDE.md`](./CLAUDE.md) — 开发者指南(架构 / 规范 / 编码约定)
- [`summary.md`](./summary.md) — 项目功能清单与近期开发历史
- `docs/规范/Open-TMS开发规范文档.md` — 详细后端 / 前端编码规范
- `docs/architecture/business/` — 核心业务架构设计

## 仓库结构

```
opentrm/                      # 项目根(Maven 父 POM)
├── common/                   # 公共基础(Result / BaseEntity / MybatisPlusConfig)
├── basedata/                 # 基础数据(14 Resource,合并自原 bankaccount/instrument)
├── dealing/                  # 交易管理(AC/AT Deal + Action + DealMap + Cashflow)
├── valuation/                # 估值(含 var/ 合并)
├── web/                      # 前端(Vue 3)
├── scripts/
│   ├── test/                 # 自动化测试(API + UI)
│   ├── db/                   # 数据库操作工具
│   ├── dev/                  # 开发辅助脚本
│   └── github/               # GitHub 集成
└── docs/                     # 设计与规范文档

> **2026-07-03 模块整合**:18 → 6(`bankaccount` / `instrument` → `basedata`;
> `fx` / `irs` / `hedge` / `exposure` / `impairment` / `limit` / `cashpool` → 占位;
> `fundplan` / `settlement` / `cockpit` / `report` → 规划中,后续按 M2/M3 计划推进)
```

## 许可与版权

本项目为内部研发项目,版权归 Open-TMS 团队所有。
