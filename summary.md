# Open-TMS 项目概览清单

> 文档版本: 2026-05-29
> 最后更新: 每次项目结构、功能、代码变更时刷新

---

## 一、项目基本信息

| 项目 | 内容 |
|------|------|
| **项目名称** | Open-TMS (Open Treasury Management System) |
| **项目目标** | 打造面向全球集团企业的企业资金管理系统 |
| **对标产品** | FIS Quantum、SAP资金管理、Murex、Kyriba |
| **核心价值** | 流动性管理、交易结算、敞口估值、风险控制 |
| **当前版本** | M1 (基础数据模块) 开发中 |
| **代码目录** | E:\code-project\open-tms\open-tms |

---

## 二、技术架构

### 2.1 后端技术栈
| 类别 | 选型 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | SpringBoot + CXF | 3.2.0 |
| ORM | MyBatis Plus | 3.5.5 |
| 数据库 | PostgreSQL | - |
| 缓存 | Redis + Redisson | 3.25.0 |
| 消息 | RocketMQ | - |

### 2.2 前端技术栈
| 类别 | 选型 |
|------|------|
| 框架 | Vue3 |
| UI组件 | Element Plus |
| 路由 | vue-router |

### 2.3 系统架构
```
前端 (Vue3 + Element Plus)
           │
    API Gateway
           │
┌──────────┴──────────┬───────────────┐
│ basedata │ cashpool │ dealing       │
│ 基础数据  │ 资金池   │ 交易管理       │
├──────────┴──────────┴───────────────┤
│         PostgreSQL + Redis          │
└─────────────────────────────────────┘
```

---

## 三、模块清单

### 3.1 Maven模块 (后端)

| 模块 | 目录 | 状态 | 主要类 |
|------|------|------|--------|
| **common** | common/ | ✅ 已完成 | MybatisPlusConfig, GlobalConstants, Result, BaseCodeEntity |
| **basedata** | basedata/ | 🔄 开发中 | Bank, BusinessUnit, Counterparty, CounterpartyAccount, Currency, Country, Holiday, Trader |
| **dealing** | dealing/ | 🔄 开发中 | Deal, DealController |
| **cashpool** | cashpool/ | 🔄 开发中 | CashPool, CashPoolController |
| **fundplan** | fundplan/ | 🔄 开发中 | FundPlan, FundPlanController |
| **fx** | fx/ | 🔄 开发中 | FxDeal, FxDealController |
| **irs** | irs/ | 🔄 开发中 | IrsDeal, IrsDealController |
| **valuation** | valuation/ | 🔄 开发中 | ValuationController |
| **var** | var/ | 🔄 开发中 | VarReportController |
| **bankaccount** | bankaccount/ | 🔄 开发中 | BankAccount, BankAccountController |
| **hedge** | hedge/ | 🔄 开发中 | HedgeRelation, HedgeRelationController |
| **impairment** | impairment/ | 🔄 开发中 | Impairment, ImpairmentController |
| **limit** | limit/ | 🔄 开发中 | Limit, LimitController |
| **report** | report/ | 🔄 开发中 | Report, ReportController |
| **instrument** | instrument/ | 🔄 开发中 | Instrument, InstrumentController |

### 3.2 前端页面 (web/src/views)

| 模块 | 页面文件 |
|------|----------|
| **basedata** | BankList, BusinessUnitList, CounterpartyList, CounterpartyAccountList, CurrencyList, CountryList, HolidayList, TraderList |
| **dealing** | DealList, DealEdit, DealDetail, BankAccountList, InstrumentList |
| **cashpool** | CashPoolList, AutoRuleList, PositionOverview, PositionLimit |
| **fundplan** | FundPlanList |
| **fx** | FxDealList |
| **irs** | IrsDealList |
| **risk** | ExposureList, HedgeList, VarReportList |
| **loan** | LoanList |
| **deposit** | DepositList |
| **transfer** | TransferList |
| **approval** | ApprovalTask, WorkflowTemplate |
| **dashboard** | Cockpit |
| **report** | ReportList |

---

## 四、产品功能全景

### M1 基础数据模块 (当前重点)
- [x] 组织架构与权限管理
- [x] 资金管理主体设计
- [x] 业务单元 CRUD + 分页查询
- [x] 银行管理 CRUD + 分页查询
- [x] 交易对手管理 CRUD
- [x] 账户管理 (对手方账户)
- [x] 币种/国家/节假日 CRUD + 分页查询
- [x] 交易员管理 CRUD + 分页查询
- [ ] 完整前后端联调

### M2 资金运营模块
- [ ] 存款交易
- [ ] 贷款交易
- [ ] 支付结算
- [ ] 现金池管理
- [ ] 现金流量预测
- [ ] 资金计划管理
- [ ] 流动性限额监控

### M3 金融工具模块
- [ ] 外汇交易
- [ ] 利率掉期(IRS)
- [ ] 金融工具估值

### M4 风险管理模块
- [ ] 减值计算(IFRS 9)
- [ ] 套期保值
- [ ] 市场风险VaR
- [ ] 敞口管理

### M5 分析报表模块
- [ ] 报表分析
- [ ] 管理驾驶舱

---

## 五、文档资产

### 5.1 PRD文档 (docs/prd/)
| 版本 | 文档 | 状态 |
|------|------|------|
| M1 | M1-基础数据PRD | ✅ |
| M1 | M1-组织架构与权限管理PRD | ✅ |
| M1 | M1-交易基础数据PRD | ✅ |
| M1 | M1-银行账户管理PRD | ✅ |
| M1 | M1-资金管理主体PRD | ✅ |
| M1 | M1-AC交易基础数据PRD | ✅ |
| M1 | M1-交易录入与管理PRD | ✅ |
| M1 | M1-交易审批流程PRD | ✅ |
| M1 | M1-实际现金流AC交易PRD | ✅ |
| M1 | M1-账户转账AT交易PRD | ✅ |
| M1 | M1-金融工具InstrumentPRD | ✅ |
| M2 | M2-存款交易/贷款交易/支付结算/现金池/现金流量预测/资金计划/流动性限额监控 | 📋 |
| M3 | M3-外汇交易/利率掉期/金融工具估值 | 📋 |
| M4 | M4-减值/套期保值/VaR/敞口 | 📋 |
| M5 | M5-报表分析/管理驾驶舱 | 📋 |

### 5.2 UX原型 (docs/原型/)
| 文件 | 状态 |
|------|------|
| Open-TMS整体设计方案.md | ✅ |
| Open-TMS界面原型与设计规范.md | ✅ |
| 基础数据界面原型.md | ✅ |
| M1-基础数据模块UX原型.md | ✅ |
| M2-资金运营模块UX原型.md | 📋 |
| M3-金融工具模块UX原型.md | 📋 |
| M4-风险管理模块UX原型.md | 📋 |
| M5-分析报表模块UX原型.md | 📋 |

### 5.3 API文档 (docs/api/)
| 模块 | 文档 |
|------|------|
| cashpool | 现金池API |
| cockpit | 驾驶舱API |
| dealing | 交易API |
| exposure | 敞口API |
| fundplan | 资金计划API |
| fx | 外汇API |
| hedge | 套保API |
| impairment | 减值API |
| instrument | 金融工具API |
| irs | 利率掉期API |
| limit | 限额API |
| report | 报表API |
| settlement | 结算API |
| valuation | 估值API |
| var | VaR API |

### 5.4 测试用例 (docs/testcase/)
- 基础数据API测试用例.md
- 基础数据UI测试用例.md
- 业务单元API测试用例.md
- 银行API测试用例.md
- 对手方账户API测试用例.md
- BUG单_基础数据模块.md
- 测试报告_基础数据模块.md

---

## 六、团队协作

### 角色定义
| 角色 | 职责 | GitHub Label |
|------|------|-------------|
| **PM-Lead** | 项目整体规划、进度管理、交付把关 | PM-Lead |
| **PM** | 需求分析、PRD撰写、功能验收 | PM |
| **TA** | 技术选型、架构设计、数据库设计 | TA |
| **Dev** | 功能开发、代码实现 | Dev |
| **QA** | 测试用例、测试执行、缺陷跟踪 | QA |
| **UX** | 界面设计、交互设计 | UX |

### 协作规范
- 项目管理: GitHub Projects
- 任务流转: PM需求 → Dev开发 → QA测试 → UX界面 → PM-Lead验收
- 详细规范见 `open-tms团队协作规范.md`

---

## 七、Skills资产

| Skill | 说明 |
|-------|------|
| opentms-pm-lead | 项目管理技能 |
| opentms-product-design | 产品设计技能 |
| opentms-business-architect | 业务架构技能(对标FIS Quantum, Murex, SAP TRM, Kyriba) |
| opentms-ux-design | UX设计技能 |
| opentms-db-design | 数据库设计技能 |
| opentms-api-design | API设计技能 |
| opentms-frontend-dev | 前端开发技能 |
| opentms-backend-dev | 后端开发技能 |
| opentms-dev | 通用开发技能 |
| opentms-test-case-design | 测试用例设计技能 |
| opentms-test-execution | 测试执行技能 |
| opentms-feature-dev | 完整功能开发编排技能(PM→UX→DB→API→FE→BE→QA) |

---

## 九、工具资产

| 工具 | 说明 |
|------|------|
| `db_tool.py` | PostgreSQL数据库操作工具(执行SQL/查看表结构/修复字段) |

### db_tool.py 使用方法
```bash
python db_tool.py -t                           # 列出所有表
python db_tool.py -d tms_country_t             # 查看表结构
python db_tool.py -q tms_country_t            # 查询表数据
python db_tool.py -s "SELECT * FROM ..."       # 执行SQL
python db_tool.py -f                           # 修复所有表缺失的remark列
python db_tool.py -r db/schema/fix.sql         # 执行SQL文件
```

---

## 十、最近提交记录 (Git)

```
5bc95e2 - fix: implement queryPage for Country and Trader services
958879e - fix: improve error handling for Country/Trader delete operations
ffd44a4 - fix: optimize skills and test scripts based on bug fixes
3d7ae7b - feat(opentms-pm-lead): complete rewrite - 研发交付全流程管理体系
aa7b4fd - feat(opentms-feature-dev): fix 4 major workflow gaps
```

---

## 十一、Country模块开发记录 (2026-05-29)

### 完成内容
- [x] 后端重构: JDBC → MyBatis-Plus ORM
- [x] Entity字段修正: `countryNo` → `areaCode` (数据库字段名`country_no`)
- [x] Service层重写: 继承ServiceImpl，实现CRUD+分页
- [x] Controller标准化: 统一Result响应，PUT/DELETE方法RESTful化
- [x] 前端API适配: updateCountry改PUT，deleteCountry改DELETE
- [x] 前端分页数据解析修复: 支持MyBatis-Plus的`records`格式
- [x] 数据库修复: 添加remark字段

### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/countries/page` | 分页查询 |
| GET | `/api/v1/countries/{id}` | 详情查询 |
| POST | `/api/v1/countries` | 新增 |
| PUT | `/api/v1/countries` | 更新 |
| DELETE | `/api/v1/countries/{id}` | 删除 |

### 遗留问题
- [ ] MetaObjectHandler自动填充未生效(原因: BasedataEntity与BaseEntity注解冲突)
- [x] 前端CountryList.vue与后端API联调 - 前端代理已验证，后端API正常

---

## 十二、项目状态总结

```
✅ 完成: 项目规划、功能清单、技术选型、团队协作规范
✅ 完成: M1基础数据PRD全部完成
✅ 完成: Country模块后端CRUD + 分页查询 (已验证)
✅ 完成: Country前端分页数据解析修复
📋 设计: M2-M5功能PRD框架
📋 待启动: M2资金运营模块开发
📋 待完成: Playwright UI自动化测试(浏览器安装中)
```

**当前开发重点**:
1. ✅ Country模块后端完成并验证通过
2. ✅ 前端分页解析修复已部署到Vite开发服务器
3. 📋 其他基础数据模块(Trader/Holiday/Currency)参考Country模式重构
4. 📋 M2资金运营模块开发启动

---

*本文档随项目进度实时更新*