---
name: opentms-feature-dev
description: Use when implementing a complete Open-TMS feature from design to testing, orchestrating all team role skills (PM→UX→DB→API→FE→BE→QA). 每个 Phase 出口嵌入 6 维审核门禁。
references:
  - skill: opentms-review-common
    files:
      - SKILL.md
      - standards/rating-system.md
---

# Open-TMS 特性全流程开发 Skill (Orchestrator)

## 简介

本skill是Open-TMS项目的**特性级全流程开发编排器**，串联产品设计(PM)→UX设计→表结构设计→接口设计→后端开发→前端开发→测试用例设计→测试执行的全部环节。它不是替代各角色的专业技能，而是确保一个特性从启动到交付的完整生命周期被有序管理、各环节衔接顺畅、交付质量可控。

**核心价值：**
- 一站式指导：从0到1完成一个特性的全流程开发
- 环环相扣：明确每个环节的输入和产出，确保上下游有序衔接
- 质量门禁：每个环节结束前有明确的完成标准，避免带着问题流入下游
- 进度可视：提供清单化跟踪模板，随时了解特性开发的整体进度

---

## 一、触发条件

**当需要进行以下工作时，触发本skill：**

- PM-Lead分配一个完整Feature的开发
- 新模块/新功能的首次开发
- 跨模块功能需要协调多个角色协作
- 需要一个清晰的端到端开发计划

**触发信号：**
- PM-Lead创建Feature级别的GitHub Issue
- PM完成PRD并创建了对应的开发Task
- 版本规划中标记为"全流程开发"的特性

---

## 二、输入要求

### 2.1 必须输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| 功能需求 | PM/用户提供 | 特性要做什么 |
| 功能特性清单 | `open-tms功能特性清单.md` | 业界对标参考 |
| 开发规范 | `docs/规范/Open-TMS开发规范文档.md` | 全流程规范 |
| 团队协作规范 | `open-tms团队协作规范.md` | GitHub Projects操作指南 |
| 设计系统 | `docs/原型/Open-TMS界面原型与设计规范.md` | 设计规范 |

### 2.2 可选输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| 业务架构设计 | BA提供 | 已有业务架构设计 |
| 技术方案 | TA提供 | 已有技术方案 |
| 竞品资料 | PM收集 | 竞品功能参考 |

---

## 三、全流程工作流总览

### 3.1 特性开发全流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                        特性启动 (Feature Kickoff)                │
│  创建Feature Issue → 创建各角色Task → 确定版本范围和优先级       │
│  ⚠️ 所有Task必须添加到GitHub Project进行跟踪                     │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   Phase 1: 产品设计 (PM)                                        │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  Skill: opentms-product-design                          │   │
│   │  输出: PRD文档 (.md) + Feature Issue + US + Task        │   │
│   └─────────────────────────────────────────────────────────┘   │
│   质量门禁: PRD已评审通过 ✓                                    │
│   ⚠️ PM Task → Done                                           │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #1: 需求审核 (P0 必做)                            │
│   Skill: opentms-review-requirement                            │
│   报告: docs/reviews/{feature}/requirement-review.md           │
│   通过(A/B) → 进 Phase 2 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   Phase 2: UX交互设计 (UX)                                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  Skill: opentms-ux-design                               │   │
│   │  输出: UX原型文档 + 交互说明                             │   │
│   └─────────────────────────────────────────────────────────┘   │
│   质量门禁: 原型已评审通过 ✓                                   │
│   ⚠️ UX Task → Done                                           │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #2: UX 审核                                       │
│   Skill: opentms-review-ux                                     │
│   报告: docs/reviews/{feature}/ux-review.md                    │
│   通过(A/B) → 进 Phase 3 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                ┌────────────────────────────────────┐
                │        是否需要业务架构设计?         │
                └────────────┬───────────┬───────────┘
                            YES          NO
                             │            │
                             ▼            │
              ┌───────────────────────────┐│
              │ Phase 3a: 业务架构设计(BA) ││
              │ opentms-business-architect ││
              └───────────────────────────┘│
                             │            │
                             ▼            ▼
                ┌────────────────────────────────────┐
                │        并行设计阶段 (Phases 3+4)    │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 3: 表结构设计 (TA/DB)    │  │
                │  │ Skill: opentms-db-design     │  │
                │  │ 输出: DDL SQL脚本              │  │
                │  └───────────────────────────────┘  │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 4: 接口设计 (TA/API)     │  │
                │  │ Skill: opentms-api-design     │  │
                │  │ 输出: API接口文档              │  │
                │  └───────────────────────────────┘  │
                └────────────────────────────────────┘
                                    │
                   质量门禁: DDL已评审 + API已评审 ✓
                   ⚠️ TA Task → Done
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #3: DB 审核 (P0 必做)                             │
│   Skill: opentms-review-db                                     │
│   报告: docs/reviews/{feature}/db-review.md                    │
│   通过(A/B) → 进 Phase 5 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #4: API 审核 (P0 必做)                            │
│   Skill: opentms-review-api                                    │
│   报告: docs/reviews/{feature}/api-review.md                   │
│   通过(A/B) → 进 Phase 5 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                ┌────────────────────────────────────┐
                │        并行开发阶段 (Phases 5+6)    │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 5: 后端开发 (BE)         │  │
                │  │ Skill: opentms-backend-dev    │  │
                │  │ 输出: Java代码 (Controller/     │  │
                │  │       Service/Mapper/Entity)   │  │
                │  └───────────────────────────────┘  │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 6: 前端开发 (FE)         │  │
                │  │ Skill: opentms-frontend-dev   │  │
                │  │ 输出: Vue代码 (组件/页面/API)   │  │
                │  └───────────────────────────────┘  │
                └────────────────────────────────────┘
                                    │
                   质量门禁: 后端单元测试通过 + 前后端联调通过 ✓
                   ⚠️ Dev Task → Done
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #5: 后端代码审核                                  │
│   Skill: opentms-review-backend                                │
│   报告: docs/reviews/{feature}/backend-review.md               │
│   通过(A/B) → 进 Phase 6 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #6: 前端代码审核                                  │
│   Skill: opentms-review-frontend                               │
│   报告: docs/reviews/{feature}/frontend-review.md              │
│   通过(A/B) → 进 Phase 7 | C → 修复后复审 | D → 返工          │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                ┌────────────────────────────────────┐
                │   ⚠️ 启动本地服务 (测试前置条件)    │
                │                                     │
                │  后端: run_backend.py start         │
                │  前端: npm run dev                   │
                └────────────────────────────────────┘
                                    │
                                    ▼
                ┌────────────────────────────────────┐
                │        测试阶段 (Phases 7+8)        │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 7: 测试用例设计 (QA)     │  │
                │  │ Skill: opentms-test-case-     │  │
                │  │        design                 │  │
                │  │ 输出: 测试用例文档             │  │
                │  └───────────────────────────────┘  │
                │                                     │
                │  ┌───────────────────────────────┐  │
                │  │ Phase 8: 测试执行 (QA)         │  │
                │  │ Skill: opentms-test-execution │  │
                │  │ 输出: 测试报告 + Bug单         │  │
                │  └───────────────────────────────┘  │
                │                                     │
                │  ⚠️ 发现Bug → 立即修复 → 回归验证   │◄──┐
                │       (高优Bug阻塞测试)              │   │
                │                                     │   │
                │  ⚠️ QA Task → Done                │   │
                └────────────────────────────────────┘   │
                                    │                    │
                   质量门禁: P0用例100%通过 ✓            │
                                    │                    │
                                    ▼                    │
┌─────────────────────────────────────────────────────────────────┐
│   🔒 审核门禁 #7: 测试报告审核 (QA 自评)                        │
│   Skill: opentms-test-execution                                │
│   通过 → 进 Phase 9 (交付前总审核)                             │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│   🔒 Phase 9: 交付前总审核 (6 维全量复审)                        │
│   PM-Lead 触发,6 维审核 skill 各跑一次                          │
│   输出综合报告 → 全部 P0 通过才能关闭 Feature                  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                        特性交付 (Feature Done)                   │
│  关闭Feature Issue → 更新发布说明 → 标记版本                    │
│  ⚠️ 所有Task已Done → Feature关闭                               │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 各阶段角色分工

| 阶段 | 角色 | Skill                        | 核心产出 |
|------|------|------------------------------|----------|
| Phase 1 | PM | `opentms-product-design`     | PRD文档、Feature/US/Task |
| Phase 2 | UX | `opentms-ux-design`          | UX原型、交互说明 |
| Phase 3a | BA | `opentms-business-architect` | 业务架构设计（按需） |
| Phase 3 | TA/DB | `opentms-db-design`          | DDL SQL脚本 |
| Phase 4 | TA/API | `opentms-api-design`         | API接口文档 |
| Phase 5 | BE | `opentms-backend-dev`        | Java后端代码 |
| Phase 6 | FE | `opentms-frontend-dev`       | Vue前端代码 |
| Phase 7 | QA | `opentms-test-case-design`   | 测试用例 |
| Phase 8 | QA | `opentms-test-execution`     | 测试报告、Bug单 |

### 3.3 特性开发时间线（参考）

```
Week 1: PM设计 ──▶ UX设计 ──▶ 表结构+API并行设计
                              │
Week 2:                       └──▶ 后端开发 ──▶ 前后端联调
                                       │
Week 3:                                └──▶ 前端开发 ──┐
                                                        │
Week 4:                                                 └──▶ 测试设计 ──▶ 测试执行 ──▶ 交付
```

**时间估算原则**：
- 简单CRUD特性：1-2周（PM 1d + UX 1d + BE 2d + FE 2d + QA 2d）
- 中等复杂度特性：2-3周（含审批流、业务规则）
- 高复杂度特性：3-4周（含多产品、银行集成、异常流程）

---

## 四、阶段执行详述

### Phase 0: 特性启动

**目的**：明确特性范围和目标，创建跟踪设施。

**操作**：

1. **确认特性基本信息**
   - 特性名称
   - 所属模块
   - 目标版本
   - 优先级（P0/P1/P2）

2. **创建Feature Issue**
   ```bash
   gh issue create \
     --title "[Feature] {特性名称}" \
     --body "## 特性描述\n{一句话描述}\n\n## 目标版本\n{版本号}\n\n## 验收标准\n- [ ] {验收条件}\n\n## 阶段跟踪\n- [ ] PM: PRD完成\n- [ ] UX: 原型完成\n- [ ] TA: 表结构+API完成\n- [ ] BE: 后端开发完成\n- [ ] FE: 前端开发完成\n- [ ] QA: 测试通过" \
     --label "PM,Feature"
   ```

3. **创建各角色Task**
   ```bash
   # PM设计任务
   gh issue create --title "[PM] {特性名称}PRD设计" --body "..." --label "PM,Task"
   # UX设计任务
   gh issue create --title "[UX] {特性名称}界面设计" --body "..." --label "UX,Task"
   # TA数据库设计任务
   gh issue create --title "[TA] {特性名称}表结构设计" --body "..." --label "TA,Task"
   # TA接口设计任务
   gh issue create --title "[TA] {特性名称}接口设计" --body "..." --label "TA,Task"
   # 后端开发任务
   gh issue create --title "[Dev] {特性名称}后端开发" --body "..." --label "Dev,Task"
   # 前端开发任务
   gh issue create --title "[Dev] {特性名称}前端开发" --body "..." --label "Dev,Task"
   # 测试任务
   gh issue create --title "[QA] {特性名称}测试" --body "..." --label "QA,Task"
   ```

4. **创建特性跟踪表**

   在特性Feature Issue中维护进度跟踪表：
   ```
   ## 全流程进度跟踪
   | 阶段 | 角色 | 状态 | 产出物 | 链接 |
   |------|------|------|--------|------|
   | PM设计 | PM | ⬜ 待开始 | PRD | - |
   | UX设计 | UX | ⬜ 待开始 | 原型 | - |
   | 表结构设计 | TA | ⬜ 待开始 | DDL | - |
   | 接口设计 | TA | ⬜ 待开始 | API文档 | - |
   | 后端开发 | BE | ⬜ 待开始 | Java代码 | - |
   | 前端开发 | FE | ⬜ 待开始 | Vue代码 | - |
   | 测试设计 | QA | ⬜ 待开始 | 测试用例 | - |
   | 测试执行 | QA | ⬜ 待开始 | 测试报告 | - |
   ```

---

### Phase 1: 产品设计

**目的**：完成PRD，明确功能范围、业务规则和验收标准。

**操作**：

1. **调用skill**: `opentms-product-design`
2. **依赖检查**: 检查是否有前置依赖特性未完成，中断流程并提示
3. **核验产出**:
   - [ ] PRD包含完整的功能描述
   - [ ] 业务规则无歧义
   - [ ] 验收标准可量化、可测试
   - [ ] 字段定义完整（类型、必填、说明）
   - [ ] 优先级标注清晰（P0/P1/P2）

4. **完成后更新Task**:
   ```bash
   # 更新PM Task状态
   gh issue edit <pm-task-issue-number> --add-label "Done"

   # 更新Feature Issue进度
   gh issue edit <feature-issue-number> --body "## PM设计\n✅ 已完成"
   ```

**质量门禁** — 进入下一阶段前必须满足：

| 检查项 | 说明 |
|--------|------|
| PRD评审通过 | PM-Lead在Feature Issue中标记PRD已评审 |
| Feature/US/Task已创建 | GitHub Projects中已创建工作项 |
| PRD已归档 | 存入 `docs/prd/{模块}/` |
| PM Task已标记Done | GitHub Project状态已更新 |

---

### Phase 2: UX交互设计

**目的**：完成界面原型设计，涵盖所有用户交互场景。

**操作**：

1. **调用skill**: `opentms-ux-design`
2. **输入确认**: 从PRD获取功能清单、用户角色、业务流程
3. **产出要求**:
   - 列表页原型（查询条件、表格列定义、操作按钮）
   - 编辑页原型（表单字段、校验规则、提交流程）
   - 详情页原型（数据展示、状态标识）
   - 流程页面原型（多步骤/审批流程）
   - 空状态/错误状态/加载状态设计
   - 移动端适配（如有需求）

4. **完成后更新Task**:
   ```bash
   # 更新UX Task状态
   gh issue edit <ux-task-issue-number> --add-label "Done"

   # 更新Feature Issue进度
   gh issue edit <feature-issue-number> --body "## UX设计\n✅ 已完成"
   ```

**质量门禁** — 进入下一阶段前必须满足：

| 检查项 | 说明 |
|--------|------|
| 原型已评审通过 | UX Lead/PM确认原型满足需求 |
| 交互说明完整 | 所有操作路径和状态变化已说明 |
| 设计规范符合 | 配色、字体、间距与设计系统一致 |
| 原型已归档 | 存入 `docs/原型/` |
| UX Task已标记Done | GitHub Project状态已更新 |

---

### Phase 3+4: 数据库 + 接口并行设计

**目的**：基于PRD和原型，并行完成数据库设计和接口设计。

#### Phase 3: 表结构设计

1. **调用skill**: `opentms-table-design`
2. **输入**: PRD + 原型 + `Open-TMS开发规范文档.md`
3. **产出要求**:
   - 完整的DDL SQL脚本
   - 表字段完整（含审计字段）
   - 索引设计（业务查询索引）
   - 约束设计（唯一约束、CHECK约束）

#### Phase 4: 接口设计

1. **调用skill**: `opentms-api-design`
2. **输入**: PRD + 原型 + 表结构（初步）
3. **产出要求**:
   - RESTful接口定义
   - 请求/响应结构
   - 参数校验规则
   - 错误码定义
   - 权限要求

**协作说明**:
- 表结构和接口设计可并行推进
- 先确定核心实体，再同步设计表和API
- 接口中的字段类型需与表设计一致
- 复杂查询接口需在表设计中考虑索引

**完成后更新Task**:
```bash
# 更新TA Task状态
gh issue edit <ta-task-issue-number> --add-label "Done"

# 更新Feature Issue进度
gh issue edit <feature-issue-number> --body "## 表结构+API设计\n✅ 已完成"
```

**质量门禁** — 进入开发前必须满足：

| 检查项 | 说明 |
|--------|------|
| DDL脚本已评审 | TA已评审表结构设计 |
| API文档已评审 | TA已评审接口设计 |
| 前后端数据模型一致 | 接口字段与业务字段匹配 |
| 已创建数据库（如为新模块） | DDL已在目标数据库执行 |
| TA Task已标记Done | GitHub Project状态已更新 |

---

### Phase 5+6: 后端 + 前端并行开发

**目的**：基于设计产物，并行完成前后端代码开发。

#### Phase 5: 后端开发

1. **调用skill**: `opentms-backend-dev`
2. **输入**: API接口文档 + 表结构DDL + PRD
3. **开发顺序**:
   ```
   Entity → Mapper → Service (impl) → Controller
   ```
4. **产出要求**:
   - Entity类 → 对应数据库表
   - Mapper接口 → 数据访问
   - Service实现 → 业务逻辑
   - Controller资源 → REST API
   - DTO/VO → 数据传输对象
   - 单元测试 → 核心业务逻辑

5. **开发完成提交**:
   ```bash
   # 提交后端代码
   git add .
   git commit -m "feat({module}): 完成{特性名}后端开发"
   git push origin <branch>

   # 更新Task状态到GitHub Project
   gh issue edit <issue-number> --add-label "Done"
   ```

#### Phase 6: 前端开发

1. **调用skill**: `opentms-frontend-dev`
2. **输入**: UX原型 + API接口文档 + 界面设计规范
3. **开发顺序**:
   ```
   API层 (api/xxx.js) → Store/状态 → 组件 → 页面 → 路由
   ```
4. **产出要求**:
   - API调用封装
   - 列表页（搜索/分页/操作）
   - 编辑页（表单/校验/提交）
   - 详情页（数据展示）
   - 交互状态（加载/空状态/错误）

5. **开发完成提交**:
   ```bash
   # 提交前端代码
   git add .
   git commit -m "feat(web/{module}): 完成{特性名}前端开发"
   git push origin <branch>

   # 更新Task状态到GitHub Project
   gh issue edit <issue-number> --add-label "Done"
   ```

**前后端联调检查点**：
- [ ] 后端API可正常访问
- [ ] 前端API调用正常
- [ ] 数据格式一致
- [ ] 错误处理正常
- [ ] 分页查询正常

**⚠️ 前后端联调后必须启动本地服务供测试执行**

**后端服务启动**:
```bash
python .agents/skills/opentms-backend-dev/scripts/run_backend.py start
```

**前端服务启动**:
```bash
cd web && npm run dev
```

**质量门禁** — 进入测试前必须满足：

| 检查项 | 说明 |
|--------|------|
| 后端单元测试通过 | mvn test 全部通过 |
| 前后端联调通过 | 所有接口联调验证 |
| 代码已提交 | 代码已push到远程分支 |
| 后端服务已启动 | `run_backend.py start` 执行成功 |
| 前端服务已启动 | `npm run dev` 执行成功 |
| 无阻塞Bug | 无P0/P1级未修复Bug |

---

### Phase 7+8: 测试设计与执行

**目的**：完成测试用例设计和测试执行。

#### Phase 7: 测试用例设计

1. **调用skill**: `opentms-test-case-design`
2. **输入**: PRD + API文档 + UX原型
3. **产出要求**:
   - API测试用例（覆盖所有接口）
   - UI测试用例（覆盖所有页面）
   - E2E测试用例（覆盖核心流程）
   - 测试数据设计

4. **测试用例完成后**:
   ```bash
   # 更新Task状态
   gh issue edit <issue-number> --add-label "Done"
   ```

#### Phase 8: 测试执行

1. **调用skill**: `opentms-test-execution`
2. **输入**: 测试用例 + 可测试环境
3. **执行顺序**:
   ```
   P0用例（冒烟） → P1用例（功能） → P2用例（增强）
   ```
4. **缺陷处理流程**:
   ```
   发现Bug → 创建Bug Issue → 高优Bug立刻修复 → QA回归验证 → 关闭Bug
   ```

   **发现Bug时**:
   ```bash
   # 创建Bug Issue
   gh issue create \
     --title "[Bug] {模块}-{缺陷简述}" \
     --body "## 缺陷描述\n{详细描述}\n\n## 复现步骤\n1. ...\n\n## 预期结果\n...\n\n## 实际结果\n..." \
     --label "Bug,Dev"
   ```

   **高优Bug（P0/P1）处理**:
   - 阻塞测试执行
   - 通知开发立即修复
   - 修复后QA立即回归验证
   - 验证通过后继续测试

   **中低Bug（P2/P3）处理**:
   - 记录到缺陷列表
   - 当前版本修复或记录到下一版本
   - 不阻塞测试继续

5. **Bug修复后验证流程**:
   ```bash
   # 1. 开发修复Bug并提交代码
   git add . && git commit -m "fix({module}): 修复{Bug编号}" && git push

   # 2. QA拉取最新代码重新启动服务
   python .agents/skills/opentms-backend-dev/scripts/run_backend.py restart

   # 3. QA执行相关用例回归验证
   python .agents/skills/opentms-test-execution/scripts/run_tests.py run <suite>

   # 4. 验证通过后关闭Bug
   gh issue close <bug-issue-number>
   ```

6. **测试完成后**:
   ```bash
   # 更新测试Task状态
   gh issue edit <issue-number> --add-label "Done"

   # 更新Feature Issue进度
   gh issue edit <feature-issue-number> --body "## 测试状态\n✅ 测试通过"
   ```

**质量门禁** — 特性完成前必须满足：

| 检查项 | 说明 |
|--------|------|
| P0用例100%通过 | 核心功能完整可用 |
| 所有P0/P1 Bug已修复 | 高优缺陷必须在本版本修复 |
| 测试报告已生成 | 存入 `docs/testreport/` |
| QA确认可交付 | QA在Feature Issue中标记测试通过 |

---

### Phase 9: 特性交付

**目的**：完成特性交付，更新发布说明。

**操作**：

1. **最终检查**
   - [ ] 所有Phase的Task都已标记Done
   - [ ] Feature Issue中所有阶段都是✅状态

2. **更新Feature Issue**
   ```bash
   gh issue edit <feature-issue-number> --body "## 状态\n已完成\n\n## 交付物\n- PRD: {链接}\n- UX原型: {链接}\n- DDL: {链接}\n- API文档: {链接}\n- 后端代码: {链接}\n- 前端代码: {链接}\n- 测试报告: {链接}\n\n## 缺陷列表\n- BUG_XXX: {描述} - 已关闭\n\n## 遗留问题\n- {遗留问题}"
   ```

3. **更新版本发布说明**
   ```markdown
   ## v{版本号} 发布说明
   
   ### 新增特性
   - {特性名称}: {简要描述}
   
   ### 修复缺陷
   - BUG_XXX: {描述}
   
   ### 已知问题
   - {已知问题}
   ```

4. **关闭Feature Issue**
   ```bash
   gh issue close <feature-issue-number>
   ```

---

## 五、进度跟踪模板

### 5.1 特性级进度看板

将以下模板贴在Feature Issue的body中，每完成一个阶段更新状态：

```markdown
## 🎯 {特性名称} - 全流程进度

### 基本信息
| 项目 | 内容 |
|------|------|
| 模块 | {所属模块} |
| 目标版本 | M1-x |
| 优先级 | P0/P1/P2 |
| PM | {负责人} |
| UX | {负责人} |
| Dev | {负责人} |
| QA | {负责人} |

### 阶段进度
| 阶段 | 状态 | 开始日 | 完成日 | 产出物 | 评审人 |
|------|------|--------|--------|--------|--------|
| PM设计 | ✅ 已完成 | MM-DD | MM-DD | [PRD](链接) | PM-Lead |
| UX设计 | ✅ 已完成 | MM-DD | MM-DD | [原型](链接) | PM-Lead |
| 表结构设计 | ⬜ 进行中 | MM-DD | - | [DDL](链接) | TA |
| 接口设计 | ⬜ 待开始 | - | - | [API](链接) | TA |
| 后端开发 | ⬜ 待开始 | - | - | [代码](链接) | BE |
| 前端开发 | ⬜ 待开始 | - | - | [代码](链接) | FE |
| 测试设计 | ⬜ 待开始 | - | - | [用例](链接) | QA |
| 测试执行 | ⬜ 待开始 | - | - | [报告](链接) | QA |

### 缺陷列表
| 编号 | 描述 | 严重程度 | 状态 |
|------|------|----------|------|
| - | - | - | - |

### 风险与阻塞
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| - | - | - |
```

### 5.2 阶段状态定义

| 状态 | 标识 | 含义 |
|------|------|------|
| 待开始 | ⬜ | 尚未开始，前置依赖还未满足 |
| 进行中 | 🔄 | 正在执行中 |
| 已完成 | ✅ | 已完成并通过质量门禁 |
| 已阻塞 | 🔴 | 因外部依赖无法继续 |
| 已跳过 | ⏭️ | 本特性不需要此阶段 |

---

### 5.3 产出物与审核对应表

| Phase | 产出物 | 审核维度 | 审核 Skill |
|-------|--------|---------|-----------|
| 1 | PRD / Feature Issue / US | 需求 | opentms-review-requirement |
| 2 | UX 原型 / 交互说明 | UX | opentms-review-ux |
| 3 | DDL SQL 脚本 | DB | opentms-review-db |
| 4 | API 接口文档 | API | opentms-review-api |
| 5 | Java 代码 | 后端 | opentms-review-backend |
| 6 | Vue 代码 | 前端 | opentms-review-frontend |
| 7 | 测试用例 | QA 自评 | opentms-test-case-design |
| 8 | 测试报告 / Bug 单 | QA 自评 | opentms-test-execution |
| 9 | 全部交付物 | 6 维全量复审 | 6 个 review skill 联调 |

---

### 5.4 审核门禁体系(新增, 2026-07-05)

**核心原则**:每个 Phase 完成后, **必须** 启动对应审核 skill, 审核通过才能进入下一阶段。

#### 5.4.1 7 级审核门禁映射表

| Phase | 触发审核 | Skill | 必须 P0 通过 |
|-------|---------|-------|-------------|
| 1 PM 设计 | 需求审核 | opentms-review-requirement | ✓ |
| 2 UX 设计 | UX 审核 | opentms-review-ux | ✓ |
| 3 DB 设计 | DB 审核 | opentms-review-db | ✓ |
| 4 API 设计 | API 审核 | opentms-review-api | ✓ |
| 5 后端开发 | 后端审核 | opentms-review-backend | ✓ |
| 6 前端开发 | 前端审核 | opentms-review-frontend | ✓ |
| 7 测试设计 | QA 评审 | (QA 自评) | ✓ |
| 8 测试执行 | 测试报告审核 | opentms-test-execution | ✓ |
| **9 交付** | **6 维全量复审** | **6 个 review skill** | **✓** |

#### 5.4.2 评级体系

| 评级 | 含义 | 后续动作 |
|------|------|---------|
| **A** | 无 P0/P1/P2 问题 | 直接进入下一 Phase |
| **B** | 仅 P2 问题(可优化但不阻塞) | 直接进入下一 Phase,P2 记录到 `[待优化]` |
| **C** | 有 P1 问题(必须修复后通过) | 立即修复 → 复审 → 通过才能进下一 Phase |
| **D** | 有 P0 问题(必须返工) | 退回该 Phase 返工 → 重新产出 → 重新审核 |

#### 5.4.3 问题严重度

| 等级 | 定义 | 示例 | 阻塞 |
|------|------|------|------|
| **P0** | 阻塞性:违反核心规范/破坏存量/合规风险 | 缺失审计字段、模块循环依赖、缺失幂等键、命名违规 | 强阻塞 |
| **P1** | 重要:影响可维护性/可测试性/扩展性 | 缺单元测试、错误码不规范、阈值硬编码 | 修复后通过 |
| **P2** | 优化:代码风格/文档/注释 | 命名风格、注释不全、变量命名 | 不阻塞 |

#### 5.4.4 审核不通过的处置

| 评级 | 处置流程 |
|------|---------|
| A/B | ✅ 通过 → 关闭当前 Phase Task → 进入下一 Phase |
| C | 🟡 修复 P1 → 提交 → 复审 → 通过后进入下一 Phase |
| D | 🔴 退回 Phase N → 返工(不只是修 P0, 重做整个 Phase) → 重新产出 → 重新审核 |

#### 5.4.5 审核输出

- **报告路径**: `docs/reviews/{feature-name}/{dimension}-review.md`
- **报告模板**: `.claude/skills/opentms-review-common/templates/report-base.md`
- **评级同步**: 评级 (A/B/C/D) 同步到 GitHub Issue
- **签字**: 审核人 / 审核日期 / 复审日期(如有)

#### 5.4.6 公共规范引用

所有审核 skill 共享公共基础: `.claude/skills/opentms-review-common/SKILL.md`
- 评级体系 (`standards/rating-system.md`)
- 流程标准 (`standards/workflow.md`)
- 报告模板 (`templates/report-base.md`)
- 一页纸摘要 (`templates/summary.md`)

#### 5.4.7 Phase 9: 交付前总审核

**触发时机**: 特性交付前, PM-Lead 启动

**执行流程**:
1. PM-Lead 触发,6 维审核 skill 各跑一次(requirement / ux / db / api / backend / frontend)
2. 每个审核 skill 输出独立的审核报告
3. PM-Lead 汇总输出综合报告 (`docs/reviews/{feature}/final-review.md`)
4. 全部 P0 通过才能关闭 Feature Issue

**综合报告模板**:
```markdown
# {Feature Name} 交付前总审核报告

| 审核维度 | Skill | 评级 | P0 | P1 | P2 | 报告链接 |
|---------|-------|------|----|----|----|---------|
| 需求 | opentms-review-requirement | A/B/C/D | N | N | N | [link] |
| UX | opentms-review-ux | ... | ... | ... | ... | [link] |
| DB | opentms-review-db | ... | ... | ... | ... | [link] |
| API | opentms-review-api | ... | ... | ... | ... | [link] |
| 后端 | opentms-review-backend | ... | ... | ... | ... | [link] |
| 前端 | opentms-review-frontend | ... | ... | ... | ... | [link] |

**总评级**: {A/B/C/D}
**关闭条件**: 所有维度评级 ≥ B 且无 P0 问题
```

---

## 六、各阶段产出物路径总表

| 阶段 | 产出物类型 | 存放路径 |
|------|-----------|----------|
| PM设计 | PRD文档 | `docs/prd/{模块}/{特性名}PRD.md` |
| PM设计 | 设计摘要 | `docs/prd/{模块}/SUMMARY.md` |
| UX设计 | 界面原型 | `docs/原型/{模块}UX原型.md` |
| UX设计 | 设计摘要 | `docs/原型/{模块}/SUMMARY.md` |
| 表结构设计 | DDL脚本 | `db/schema/{序号}-{模块}.sql` |
| 接口设计 | API文档 | `docs/api/{模块}/{序号}-{接口名}.md` |
| 后端开发 | Java代码 | `{module}/src/main/java/com/opentms/{module}/` |
| 后端开发 | 开发摘要 | `{module}/SUMMARY.md` |
| 前端开发 | Vue代码 | `web/src/views/{module}/` |
| 前端开发 | 开发摘要 | `web/src/{module}/SUMMARY.md` |
| 测试设计 | 测试用例 | `docs/testcase/{类型}/{模块}测试用例.md` |
| 测试执行 | 测试报告 | `docs/testreport/{模块}/{版本}/` |
| 测试执行 | 缺陷报告 | `docs/bugreport/{模块}/BUG_XXX.md` |

---

## 七、质量门禁总表

### 7.1 全流程门禁清单

| 阶段 | 门禁项 | 责任人 | 触发下一阶段 |
|------|--------|--------|-------------|
| PM→UX | PRD已评审通过 + 需求审核评级 A/B | PM-Lead | UX启动 |
| UX→设计 | 原型已评审通过 + UX审核评级 A/B | PM-Lead | 表结构+API启动 |
| 设计→开发 | DDL+API已评审 + DB/API审核评级 A/B | TA | 后端+前端启动 |
| 开发→测试 | 联调通过+无阻塞Bug + 后端/前端审核评级 A/B | Dev Lead | QA启动 |
| 测试→交付 | P0通过率100%+无高优Bug + 测试报告审核通过 | QA | 特性关闭 |
| 交付前 | 6 维全量复审全部 P0 通过 | PM-Lead | Feature 关闭 |

> ⚠️ **新增(2026-07-05)**:每个阶段门禁现在包含对应的**审核评级门禁**,
> 详见 5.4 节"审核门禁体系"。审核评级 C/D 不能进入下一阶段。

### 7.2 门禁触发规则

| 场景 | 处理 |
|------|------|
| 当前阶段未完成 | 不能进入下一阶段 |
| 当前阶段有阻塞 | 解决阻塞后再进入下一阶段 |
| 当前阶段有高风险 | 风险评估通过后再进入下一阶段 |
| 当前阶段有争议 | 由PM-Lead裁决 |
| 审核评级 A/B | ✅ 直接进入下一阶段 |
| 审核评级 C | 🟡 修复 P1 后复审, 通过才能进入下一阶段 |
| 审核评级 D | 🔴 退回该 Phase 返工, 重新审核通过才能进入下一阶段 |

### 7.3 审核与门禁的对应关系

```
Phase N 产出 → 调用 opentms-review-{N} → 评级判定
                                          │
                              ┌───────────┼───────────┐
                              ▼           ▼           ▼
                          A/B 通过      C 复审       D 返工
                              │           │           │
                              ▼           ▼           ▼
                          进入 N+1    修复后复审    退回 Phase N
```

---

## 八、常见场景指南

### 8.1 简单CRUD特性（快速路径）

对于仅有CRUD操作的简单特性（如基础数据维护），可以跳过部分阶段：

```
PM设计 ──▶ UX设计 ──▶ 表结构+API设计 ──▶ 后端+前端并行 ──▶ 测试
                                              │
                                (可跳过：业务架构、复杂测试)
```

**简化原则**：
- 无需业务架构设计（直接进入表结构）
- API设计可与后端开发合并（快速熟悉）
- 测试以API测试为主（UI测试可简化）
- 无需E2E测试（仅验证核心CRUD）

### 8.2 复杂业务特性（完整路径）

对于有审批流、多状态流转、与外部系统交互的特性：

```
PM设计 ──▶ BA业务架构 ──▶ UX设计 ──▶ 表结构+API设计 ──▶ BE+FE并行 ──▶ 测试
                                                                     │
                                          (必需：全流程测试，含E2E)
```

**强化原则**：
- 必须做业务架构设计（明确领域模型）
- API设计必须提前评审（跨模块接口需对齐）
- 测试必须含E2E（验证完整业务流程）
- 必须做异常流程测试

### 8.3 跨模块特性

涉及多个模块交互的特性需要特别注意：

```
PM设计（明确定义模块边界）
    │
    ├──▶ 模块A: UX→表→API→BE/FE
    │
    └──▶ 模块B: UX→表→API→BE/FE
              │
              ▼
        集成测试（跨模块联调）
```

**关键点**：
- 接口契约必须在各模块开发前对齐
- 集成测试专用用例覆盖模块间交互
- 数据一致性需设计验证

---

### 8.4 紧急 Bug 修复路径

```
生产 Bug → 创建 Hotfix Task → 直接进入开发 → 简化测试 → Hotfix 上线
                │                                       │
                └─────── PM-Lead 口头批准 ──────────────┘
```

详见 `open-tms团队协作规范.md` 中的 Hotfix 流程。

---

### 8.5 审核跳过的例外(新增)

**默认规则**: 所有特性必须按 5.4 节"审核门禁体系"在每个 Phase 出口触发审核。

**例外情况**(可跳过审核):

| 例外类型 | 适用场景 | 跳过条件 | 后续动作 |
|---------|---------|---------|---------|
| **Hotfix** | 紧急修复 P0/P1 生产 Bug | PR 描述注明 "Hotfix - 跳过审核" | PM-Lead 事后审计 |
| **文档修改** | 仅修改 `.md` 注释/文档 | 无任何代码变更 | 无需审核 |
| **配置调整** | 调整端口/超时等参数 | PM-Lead 口头确认 | 记录到 CHANGELOG |

**禁止跳过审核**:
- 任何涉及新表/新字段的 DDL
- 任何新增/修改 API
- 任何业务逻辑变更
- 任何数据库 schema 变更

**跳过后补救**:
- Hotfix: 下一个 Sprint 必须补一次集中审核(PM-Lead 安排)
- 文档/配置: 无需补救

---

## 九、工具自优化原则

### 9.1 工具问题处理原则

**核心原则：当发现工具脚本有问题时，应该修复工具，而不是绕过工具。**

| 发现的问题 | 正确做法 | 错误做法 |
|-----------|----------|----------|
| 测试脚本路径错误 | 修复脚本中的路径配置 | 改用curl手写命令 |
| 测试脚本功能缺陷 | 修复脚本功能 | 放弃脚本另写命令 |
| 服务启动脚本异常 | 修复启动脚本 | 手动启动服务 |

### 9.2 自优化执行流程

```
发现工具问题 → 分析问题根因 → 修复工具脚本 → 验证修复 → 继续执行
     │                                    │
     │         ⚠️ 禁止：跳过工具/绕过工具  │
     └────────────────────────────────────┘
```

### 9.3 典型场景示例

**场景1：测试脚本run_tests.py路径配置错误**
```
错误做法：放弃使用run_tests.py，改用curl逐个测试
正确做法：检查TEST_DIR路径配置，修复脚本，重新运行
```

**场景2：后端启动脚本run_backend.py启动失败**
```
错误做法：手动执行java -jar命令
正确做法：检查脚本中的JAR路径和端口配置，修复脚本
```

### 9.4 自优化记录

当修复工具脚本后，应记录修复内容：

```markdown
### 工具自优化记录
- 日期: YYYY-MM-DD
- 问题: {问题描述}
- 根因: {根本原因}
- 修复: {修复内容}
- 验证: {验证结果}
```

---

## 十、与其他Skill的关系

### 10.1 本skill编排的角色Skill

| 角色 | Skill | 职责 |
|------|-------|------|
| PM | `opentms-product-design` | 产品需求设计 |
| PM | `opentms-pm` | 产品管理 |
| BA | `opentms-business-architect` | 业务架构设计 |
| UX | `opentms-ux-design` | UX交互设计 |
| UX | `opentms-ux` | UX设计 |
| TA | `opentms-ta` | 技术架构 |
| TA | `opentms-table-design` | 表结构设计 |
| TA | `opentms-api-design` | 接口设计 |
| BE | `opentms-backend-dev` | 后端开发 |
| FE | `opentms-frontend-dev` | 前端开发 |
| QA | `opentms-test-case-design` | 测试用例设计 |
| QA | `opentms-test-execution` | 测试执行 |

### 10.2 本skill调用的审核Skill(6 + 1 公共)

每个 Phase 出口嵌入审核门禁,以下 6 个审核 skill + 1 个公共基础:

| 审核 Skill | 审核维度 | 触发位置 | 必须 P0 |
|-----------|---------|---------|---------|
| `opentms-review-common` | 公共基础(评级/报告/流程) | 所有审核前置引用 | — |
| `opentms-review-requirement` | 需求 | Phase 1 → 2 | ✓ |
| `opentms-review-ux` | UX | Phase 2 → 3 | — |
| `opentms-review-db` | DB | Phase 3 → 4 | ✓ |
| `opentms-review-api` | API | Phase 4 → 5 | ✓ |
| `opentms-review-backend` | 后端代码 | Phase 5 → 6 | — |
| `opentms-review-frontend` | 前端代码 | Phase 6 → 7 | — |
| `opentms-test-execution` | 测试报告 | Phase 8 → 9 | ✓ |
| 6 维全量复审 | Phase 9 交付前 | ✓ |

> 详见 5.4 节"审核门禁体系"。

### 10.3 本skill与其它skill的核心区别

| 对比维度 | 本skill | 各角色skill |
|----------|---------|-------------|
| 定位 | **编排器/指挥者** | **执行者** |
| 关注点 | **流程完整性**——确保各阶段衔接、门禁达标 | **专业深度**——确保本领域技术最佳 |
| 受众 | PM-Lead/项目协调者 | 各角色专业人员 |
| 视角 | **水平视角**——跨阶段、跨角色 | **垂直视角**——本领域纵向深入 |
| 产出 | Feature进度跟踪、质量门禁检查、审核门禁触发 | 专业交付物（PRD/原型/代码/测试用例） |
| 嵌入审核 | 在每个 Phase 出口触发 6 维审核 | 不直接触发审核 |

---

## 十一、交付物检查清单

### 11.1 特性全流程总清单

**产品阶段**：
- [ ] PRD已归档到 `docs/prd/`
- [ ] Feature/US/Task已创建
- [ ] PRD已评审通过

**UX阶段**：
- [ ] 原型已归档到 `docs/原型/`
- [ ] 原型已评审通过
- [ ] 所有页面状态（加载/空/错误）已设计

**设计阶段**：
- [ ] DDL脚本已归档到 `db/schema/`
- [ ] API文档已归档到 `docs/api/`
- [ ] DDL+API已评审通过
- [ ] 数据库已创建表

**开发阶段**：
- [ ] 后端代码已提交
- [ ] 前端代码已提交
- [ ] 后端单元测试通过
- [ ] 前后端联调通过

**测试阶段**：
- [ ] 测试用例已归档到 `docs/testcase/`
- [ ] P0用例100%执行
- [ ] P0用例100%通过
- [ ] 测试报告已归档到 `docs/testreport/`
- [ ] 无未修复的P0/P1 Bug

**交付阶段**：
- [ ] Feature Issue已更新
- [ ] 发布说明已更新
- [ ] Feature Issue已关闭

---

## 附录A：快速启动模板

```bash
# === 快速启动一个特性的全流程 ===
# 1. 确定特性基本信息
NAME="{特性名称}"
MODULE="{模块名}"
VERSION="M1-x"
PRIORITY="P0"

# 2. 创建Feature Issue
gh issue create \
  --title "[Feature] $NAME" \
  --body "## 特性描述\n...\n## 目标版本\n$VERSION\n## 优先级\n$PRIORITY" \
  --label "PM,Feature"

FEATURE_ISSUE={上一步返回的issue编号}

# 3. 创建各角色Task
gh issue create --title "[PM] ${NAME}PRD设计" --body "关联Feature: #$FEATURE_ISSUE" --label "PM,Task"
gh issue create --title "[UX] ${NAME}界面设计" --body "关联Feature: #$FEATURE_ISSUE" --label "UX,Task"
gh issue create --title "[TA] ${NAME}表结构设计" --body "关联Feature: #$FEATURE_ISSUE" --label "TA,Task"
gh issue create --title "[TA] ${NAME}接口设计" --body "关联Feature: #$FEATURE_ISSUE" --label "TA,Task"
gh issue create --title "[Dev] ${NAME}后端开发" --body "关联Feature: #$FEATURE_ISSUE" --label "Dev,Task"
gh issue create --title "[Dev] ${NAME}前端开发" --body "关联Feature: #$FEATURE_ISSUE" --label "Dev,Task"
gh issue create --title "[QA] ${NAME}测试" --body "关联Feature: #$FEATURE_ISSUE" --label "QA,Task"

# 4. 更新Feature Issue添加进度跟踪表
gh issue edit $FEATURE_ISSUE --body "## 进度跟踪\n| 阶段 | 状态 |\n|------|------|\n| PM设计 | ⬜ 待开始 |\n| UX设计 | ⬜ 待开始 |\n| 表结构设计 | ⬜ 待开始 |\n| 接口设计 | ⬜ 待开始 |\n| 后端开发 | ⬜ 待开始 |\n| 前端开发 | ⬜ 待开始 |\n| 测试设计 | ⬜ 待开始 |\n| 测试执行 | ⬜ 待开始 |"

echo "特性 $NAME 已启动！Feature Issue: #$FEATURE_ISSUE"
```

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.2 | 2026-07-05 | **审核门禁体系集成**: 1. 在流程图每个 Phase 出口嵌入 7 级审核门禁(需求/UX/DB/API/后端/前端/QA) 2. 新增 5.4 节"审核门禁体系"(评级 A/B/C/D + P0/P1/P2) 3. 新增 5.3 节"产出物与审核对应表" 4. 新增 Phase 9 "交付前总审核"(6 维全量复审) 5. 新增 8.5 节"审核跳过的例外" 6. 更新 7.1-7.3 质量门禁总表, 整合审核评级 |
| v1.1 | 2026-05-27 | 1. 完善GitHub Project协同：各阶段完成后必须更新Task状态为Done 2. 增加开发后启动本地服务步骤：后端run_backend.py start + 前端npm run dev 3. 完善Bug修复流程：高优Bug立即修复→QA回归验证→关闭Bug 4. 流程图标注关键节点 |
| v1.0 | 2026-05-01 | 初始版本 |
