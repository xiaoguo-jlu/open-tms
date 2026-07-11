# CLAUDE.md

Open-TMS (Open Treasury Management System) — 企业级资金管理系统,对标 FIS Quantum / SAP TRM / Murex MX.3 / Kyriba。

---

## Tech Stack
- **Backend**: Java 17, SpringBoot 3.2.0, **Apache CXF 4.0.3 (JAX-RS)**, MyBatis Plus 3.5.5, Lombok 1.18.30
- **Frontend**: Vue 3, Element Plus, Vite (port 3000)
- **Database**: PostgreSQL 42.7.1 + Redis/Redisson 3.25.0 — 库名 `opentms`,user/pwd = `opentms/opentms123`
- **Build**: Maven 多模块,**单库多服务**(共享 PG,通过端口区分)

---

## Active Modules (6)
| Module | Port | Status | 说明 |
|--------|------|--------|------|
| basedata | 8081 | ✅ | CXF;银行账户+金融工具+币种对+**默认银行账户规则 v1.1** |
| dealing | 8082 | ✅ | Spring MVC;AC/AT/FX 三类交易共用 DealMap |
| fundplan | 8085 | 📋 | 资金计划 |
| valuation | 8091 | 🔄 | 估值 |
| var | 8095 | 🔄 | VaR |
| common | — | ✅ | 公共基础(Result/BaseEntity/GlobalConstants) |

---

## Build & Run
```bash
mvn clean install                                      # 构建全部
mvn clean package -pl dealing -am                      # 构建 dealing + 依赖
java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar    # 启动 dealing (8082)
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar  # 启动 basedata (8081)
cd web && npm install && npm run dev                   # 前端 dev (3000)
```

---

## Testing
```bash
python scripts/test/test_fx_deal_api.py    # FX API / test_fx_deal_ui.py
python scripts/test/test_ac_deal_api.py    # AC API
python scripts/test/test_at_deal_api.py    # AT API / test_at_deal_ui.py
python scripts/test/test_deal_api.py       # 通用 Deal API / test_deal_ui.py
python scripts/test/test_all.py            # 全部
python scripts/db/db_tool.py -t            # 列出所有表;-d 描述 -q 查询 -r 执行 SQL
```

---

## Code Organization (per module)
```
src/main/java/com/opentms/{module}/
├── controller/   # REST 端点(/api/v1/{resource})
├── service/      # 业务逻辑(impl/)
├── mapper/       # MyBatis Plus
├── entity/       # 持久化实体(继承 BaseEntity/BaseCodeEntity)
├── dto/          # 入参
├── vo/           # 出参
└── enums/        # 业务枚举
```
**分层红线**:dto/vo/entity 必须分文件;Controller 收 DTO,Service 返回 VO(`BeanUtil.copyProperties`)。

---

## Database Conventions
- 表名:`tms_{module}_{type}` — `_t` 主表 / `_d` 字典 / `_log` 日志 / `_rel` 关联 / `_his` 历史 / `_image` 镜像
- 主键:`id BIGSERIAL PRIMARY KEY`;业务编码 `xxx_code VARCHAR(50) UNIQUE`;流水号 `xxx_no`(如 `FX20260704-0001`)
- **审计字段(全表必备)**:`created_by` / `created_at` / `updated_by` / `updated_at` / `version`(@Version) / `deleted`(@TableLogic)
- **金额精度**:普通 `DECIMAL(18,2)`,汇率 `DECIMAL(18,8)`,利率 `DECIMAL(10,4)`,**资金交易(AC/Cashflow/FX)强制 `DECIMAL(38,18)`**

---

## REST API Patterns
```
GET    /api/v1/{resource}/page              # 分页
GET    /api/v1/{resource}/{id}              # 详情(id 或 dealNumber)
POST   /api/v1/{resource}                   # 新增
POST   /api/v1/{resource}/update            # 更新
POST   /api/v1/{resource}/delete/{id}       # 删除
POST   /api/v1/{resource}/{id}/submit       # 提交审批(AT 走)
POST   /api/v1/{resource}/{id}/approve      # 审批通过
POST   /api/v1/{resource}/{id}/reject       # 驳回
POST   /api/v1/{resource}/{id}/execute      # 执行(Deal 专属)
POST   /api/v1/{resource}/{id}/rate-fix     # FX NDF Rate Fix
POST   /api/v1/{resource}/images/{n}        # 历史镜像查询
```
> ⚠️ **红线**:update/delete 一律 POST。`getById` 用 `@PathVariable String`(兼容 dealNumber)。

**响应**:`Result<T>` = `{code, message, data, timestamp}`;`200/400/401/403/404/500`。
**幂等**:写操作必须支持 `X-Idempotency-Key` 请求头;`tms_deal_idempotency_t` 配套表。

---

## Frontend Conventions
- API 函数:`web/src/api/{module}/{entity}.js`,命名 `listX/getX/saveX/updateX/deleteX/copyX/rateFixX`
- 列表页:搜索区 + 工具栏 + 表格 + 分页;状态标签 `<el-tag :type="getStatusType(row.status)">`
- 视图目录:`web/src/views/{module}/`(模块名:basedata/dealing/fx/irs/...)
- 公共组件:`web/src/components/picker/BaseDataPicker.vue`(跨模块基础数据查找)、`ModeBadge.vue`(4 模式徽章)、`FormContainer.vue`
- Vite 代理:`/api/v1/dealing` → 8082,`/api/v1/*` → 8081

### API 一致性扫描(2026-07-10 新增)
```bash
python scripts/api_scanner.py                  # 扫 web/src/api → docs/api/frontend-api-consistency.html
python scripts/api_scanner.py --ci             # CI 模式,P0 存在 exit 1
bash scripts/gen-openapi.sh && python scripts/api_scanner.py   # 拉最新契约再扫
```
报告:**评级 D(有 P0)阻塞 / C(有 P1)修复后过 / B(仅 P2)通过 / A 无问题**。详见 `docs/api/FRONTEND-API-SCANNER.md` 和 `docs/api/frontend-api-consistency.html`。

---

## Key Enums(`common.constant.GlobalConstants`)
- **DealType**: AC / AT / FX
- **InstrumentType**: AC/AT/FX/DEPOSIT/LOAN/IR/EQ/BOND/SWAP/OPTION/FORWARD/OTHER(细分见 basedata)
- **DealStatus**: New → Submitted → Approved → Settled / Rejected / Canceled
- **ActionType**: CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE
- **FXFixType**: FIX_IN(固定买入金额)/ FIX_OUT(固定卖出金额)/ FIX_RATE(固定汇率)/ RATE_FIX(NDF 定价)
- **ATTransferType**: SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER

---

## Java Conventions
```java
@Transactional(rollbackFor = Exception.class)   // 写操作
@Transactional(readOnly = true)                 // 读操作
```
- 业务异常:`throw new BusinessException("message")` → `GlobalExceptionHandler` 统一捕获
- 日志:关键节点 `log.info(...)`,异常 `log.error(..., e)`(必须含堆栈);生产不输出 SQL 参数

---

## 交易架构 — DealMap v3.2(2026-07)
所有交易(AC/AT/FX)共享 `tms_deals_t` 主表 + `tms_deal_map_t` 字段映射 + `tms_actions_t` 生命周期事件。`tms_deal_map_t` 通过 `(deal_type, field_key)` 唯一定义字段,不建 N 张 Map 表。

**1 DealMap → 0/1 Cashflow 约束**(Murex MX.3 标准):`FX_BUY/SELL_AMOUNT → 1 CF`、`FX_RATE → 0 CF`、`FX_FIX(RATE_FIX) → 1 CF`(NDF settlement 差额)、`AC_AMOUNT → 1 CF`、`AT_FROM/TO_AMOUNT → 1 CF`。

**Deal/Image 镜像**:`tms_deal_image_t` / `tms_ac_deals_image_t` / `tms_at_deals_image_t` / `tms_fx_deals_image_t` 按 `image_number + version` 快照保存。**⚠️ AT 表 `27-at-deal-image-table.sql` 必须执行**(`954a4b5` 修复)。完整规范:`docs/architecture/business/DealMap落地分析.md`、`.claude/skills/opentms-dealmap-patterns/SKILL.md`。

---

## 4 模式详情页(new/copy/edit/readonly)
1 个 Vue 组件支持 4 种模式,URL query 传参:`?mode=new|copy|edit|readonly&id={dealNumber}`。

| 差异点 | new | copy | edit | readonly |
|--------|-----|------|------|----------|
| 表单禁用 | false | false | false | true |
| 初始数据 | 空 | 复制源数据 | 加载已有 | 加载已有 |
| 保存按钮 | 显示 | 显示 | 显示 | 隐藏 |
| 审批按钮 | 隐藏 | 隐藏 | 显示 | 隐藏 |
| 操作按钮 | 保存/取消 | 保存/取消 | 保存/提交审批/取消 | 编辑/复制 |

复制时后端 `getCopyData()` 调用 `EntityNameLookup` 补全 `*Name` 字段,前端 `BaseDataPicker.preloadRow` 显示名称。

---

## 审批流差异(2026-07 评审 P0-3)
| 交易类型 | 审批流 | 原因 |
|---------|--------|------|
| AC | ✅ submit → approve | 外部资金划转,需风控 |
| AT | ✅ submit → approve | 内部转账,需合规 |
| **FX** | ❌ **直接 Approved**(无审批) | 即时成交,业界惯例直入直出生成 CF |

FX `Action` 创建时 `actionStatus = Approved`,`approvalStatus1 = approvalStatus2 = Approved`。

---

## NDF Rate Fix(2026-07-05 Phase 1)
NDF(No Delivery Forward)在 `tms_fx_deals_t` 增加 5 字段:`fix_date` / `fix_currency`(默认 buyCurrency)/ `fix_market_rate` / `verifier_by` / `fix_remark`。执行 `POST /fx-deals/{id}/rate-fix`(`RateFixRequest` DTO),完成后生成 1 条 `FX_FIX` DealMap + 1 条 settlement 方向 Cashflow。
设计评审:`docs/prd/M3/M3-NDF-Rate-Fix设计评审.md`(**建议 1 行 FX_FIX + 1 CF,不要 3 行 event**)。

---

## 跨模块数据查找 — EntityNameLookup
dealing 详情/复制接口通过 `JdbcTemplate` 跨模块查询 `*Name`,**禁止 service 间循环依赖**。复制/详情必须补全:`managementEntityName` / `counterpartyName` / `traderName` / `instrumentName` / `bankAccountName`。单个 lookup 失败容错(避免详情接口 500)。

---

## 6 维审核 Skill 体系(2026-07-05)
每个研发 Phase 出口嵌入审核门禁,**P0 问题不通过不能进入下一阶段**。

| 维度 | Skill | 触发位置 | P0 必做 |
|------|-------|---------|---------|
| 需求 | `opentms-review-requirement` | Phase 1→2 | ✓ |
| UX | `opentms-review-ux` | Phase 2→3 | — |
| DB | `opentms-review-db` | Phase 3→4 | ✓ |
| API | `opentms-review-api` | Phase 4→5 | ✓ |
| 后端 | `opentms-review-backend` | Phase 5→6 | — |
| 前端 | `opentms-review-frontend` | Phase 6→7 | — |
| 测试 | `opentms-test-execution` | Phase 8→9 | ✓ |
| **6 维复审** | **6 skill 联调** | **Phase 9 交付前** | **✓** |

**评级**:A(无问题)/ B(仅 P2)/ C(有 P1)/ D(有 P0,返工)。**报告路径**:`docs/reviews/{feature}/{dimension}-review.md`。**公共规范**:`.claude/skills/opentms-review-common/SKILL.md`。

---

## 近期重要变更(最近 30 天)
| 日期 | 变更 | Commit |
|------|------|--------|
| 2026-07-08 | **默认银行账户规则 v1.1** 完整特性(11 端点 + 双方向 + 并发控制 + 审计 + 缓存) | `3e3604f` |
| 2026-07-05 | NDF Rate Fix Phase 1 + 5 字段 + Action 直接 Approved | `9ffbd23` |
| 2026-07-05 | 详情响应补全 *Name(EntityNameLookup) | `2c891bc` |
| 2026-07-05 | Skill 体系精简(-53% 上下文,+dealmap-patterns) | `9d826a2` |
| 2026-07-05 | feature-dev v1.2 + 12 角色 skill 同步 | `2671a52` |
| 2026-07-05 | 6 维审核 skill 体系 | `d1c3160` |
| 2026-07-04 | 交易复制字段带出 + 列表/路由优化 | `90c902e` |
| 2026-07-04 | Action 代办菜单 + 详情一屏布局 + 5 份 PRD | `c33596a` |
| 2026-07-03 | FX 审批 + AT 限制修复 + 银行账户菜单路径 | `d08181e` |
| 2026-07-03 | FX 一键复制功能 | `8adea38` |
| 2026-07-02 | FX v3.2 全流程 + AC/AT 复制 + BaseDataPicker | `6206f26` |
| 2026-07-01 | 模块整合 18→6 + 接口规范化 + 测试加固 | `a47f49a` |

---

## 已完成的 PRD/原型
**PRD**:`docs/prd/M1/`(AC/AT/Deal/DealMap/主体/审批/资金主体)、`docs/prd/M3/`(外汇/NDF-Rate-Fix/MM/估值)、`docs/prd/common/`(用户权限/后台管理)

**UX 原型**:`docs/原型/M3/M3-交易详情一屏布局UX原型.md`、`M3-交易详情内联编辑UX原型.md`、`fx-trading-prototype.html`(交互式)

**架构设计**:`docs/architecture/business/AC交易与现金流分离架构设计.md`(Murex MX.3 模型)、`DealMap落地分析.md`

---

## 关键 Bug 修复(防止回退)
| Commit | 修复内容 |
|--------|---------|
| `954a4b5` | `tms_at_deals_image_t` 表缺失导致 AT images 端点 500 |
| `9ffbd23` | FX/AC/AT Controller `getById` `@PathVariable Long` 改 String(兼容 dealNumber);AT 匹配失败归 200 |
| `90c902e` | 复制字段带出、列表路由、错误提示 |
| `8adea38` | FX 一键复制 |
| `d08181e` | FX 审批流、AT 限制修复、银行账户菜单路径 |

---

## 工作流红线
- 业务模块间**禁止循环依赖**;共享类必须放 `common`
- 业务异常抛 `BusinessException`,由 `GlobalExceptionHandler` 统一捕获
- 敏感数据必须脱敏(`@SensitiveInfo`),密码 BCrypt 加密
- 新功能先确定属于**五层架构**哪一层、哪个 Maven 模块
- 状态字符串先查 `GlobalConstants`,**禁止新建魔术字符串**
- 新表/字段命名严格 snake_case,审计字段不可缺
- 改表前必查 `python scripts/db/db_tool.py -d <table>`;改表后更新 `db/schema/`

---

## Git Commit
```
<type>(<scope>): <subject>
类型: feat/fix/docs/style/refactor/test/chore
```

---

## 必读文档(按需查阅)
- `summary.md`(项目根)— 模块清单与功能全景
- `docs/规范/Open-TMS开发规范文档.md`(32KB)— 详细后端/前端/DB 规范
- `.claude/skills/opentms-dealmap-patterns/SKILL.md` — DealMap v3.2 + 4 模式 + BaseDataPicker
- `.claude/skills/opentms-feature-dev/SKILL.md` — PM→UX→DB→API→FE→BE→QA 全链路编排
- `.claude/skills/opentms-review-*/SKILL.md` — 6 维审核 skill