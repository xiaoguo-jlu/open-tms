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
- 质量门禁：每个环节出口嵌入审核门禁(6维审核体系)，避免带着问题流入下游
- 进度可视：提供清单化跟踪模板，随时了解特性开发的整体进度

---

## 一、触发条件

**触发信号：**
- PM-Lead分配一个完整Feature的开发
- 新模块/新功能的首次开发
- 跨模块功能需要协调多个角色协作
- PM-Lead创建Feature级别的GitHub Issue
- PM完成PRD并创建了对应的开发Task

---

## 二、输入要求

### 2.1 必须输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| 功能需求 | PM/用户提供 | 特性要做什么 |
| 功能特性清单 | `open-tms功能特性清单.md` | 业界对标参考 |
| 开发规范 | `docs/规范/Open-TMS开发规范文档.md` | 全流程规范 |
| 团队协作规范 | `open-tms团队协作规范.md` | GitHub Projects操作指南 |

### 2.2 可选输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| 业务架构设计 | BA提供 | 已有业务架构设计 |
| 技术方案 | TA提供 | 已有技术方案 |
| 竞品资料 | PM收集 | 竞品功能参考 |

---

## 三、全流程工作流总览

### 3.1 特性开发全流程(9 Phase + 7 审核门禁)

```
Phase 0: 特性启动 → 创建Feature Issue + 各角色Task → GitHub Project跟踪

Phase 1: 产品设计 (PM) → opentms-product-design → PRD文档
  🔒 [审核门禁#1] opentms-review-requirement → A/B通过进Phase 2 / C修复复审 / D返工

Phase 2: UX交互设计 (UX) → opentms-ux-design → UX原型 + 交互说明
  🔒 [审核门禁#2] opentms-review-ux → A/B通过进Phase 3/4 / C修复复审 / D返工

Phase 3a: 业务架构设计 (BA, 按需) → opentms-business-architect

Phase 3+4: 并行设计阶段
  ├─ Phase 3: 表结构设计 (TA/DB) → opentms-db-design → DDL SQL脚本
  └─ Phase 4: 接口设计 (TA/API) → opentms-api-design → API接口文档
  🔒 [审核门禁#3] opentms-review-db → A/B通过
  🔒 [审核门禁#4] opentms-review-api → A/B通过

Phase 5+6: 并行开发阶段 (⚠️ 联调后启动本地服务供测试)
  ├─ Phase 5: 后端开发 (BE) → opentms-backend-dev → Java代码
  └─ Phase 6: 前端开发 (FE) → opentms-frontend-dev → Vue代码
  🔒 [审核门禁#5] opentms-review-backend → A/B通过
  🔒 [审核门禁#6] opentms-review-frontend → A/B通过

Phase 7+8: 测试阶段
  ├─ Phase 7: 测试用例设计 (QA) → opentms-test-case-design → 测试用例
  └─ Phase 8: 测试执行 (QA) → opentms-test-execution → 测试报告 + Bug单
      ⚠️ 发现Bug → 立即修复 → 回归验证

Phase 9: 交付前总审核 → 6维全量复审(6个review skill各跑一次)
  → 全部P0通过 → 关闭Feature Issue → 特性交付
```

### 3.2 各阶段角色分工

| 阶段 | 角色 | Skill | 核心产出 |
|------|------|------|----------|
| Phase 1 | PM | `opentms-product-design` | PRD文档、Feature/US/Task |
| Phase 2 | UX | `opentms-ux-design` | UX原型、交互说明 |
| Phase 3a | BA | `opentms-business-architect` | 业务架构设计(按需) |
| Phase 3 | TA/DB | `opentms-db-design` | DDL SQL脚本 |
| Phase 4 | TA/API | `opentms-api-design` | API接口文档 |
| Phase 5 | BE | `opentms-backend-dev` | Java后端代码 |
| Phase 6 | FE | `opentms-frontend-dev` | Vue前端代码 |
| Phase 7 | QA | `opentms-test-case-design` | 测试用例 |
| Phase 8 | QA | `opentms-test-execution` | 测试报告、Bug单 |

### 3.3 时间估算参考

| 复杂度 | 周期 | 典型工时分布 |
|--------|------|-------------|
| 简单CRUD | 1-2周 | PM 1d + UX 1d + BE 2d + FE 2d + QA 2d |
| 中等(含审批流) | 2-3周 | PM 2d + UX 2d + BA 1d + BE 4d + FE 3d + QA 3d |
| 高复杂度(多产品/银行集成) | 3-4周 | PM 3d + UX 3d + BA 3d + BE 5d + FE 5d + QA 5d |

---

## 四、阶段执行详述

### Phase 0: 特性启动

**目的**：明确特性范围和目标，创建跟踪设施。

**操作**：

1. 确认特性基本信息：名称、所属模块、目标版本、优先级(P0/P1/P2)
2. 创建Feature Issue(含全流程进度跟踪表)
3. 创建各角色Task(PM/UX/TA/Dev/QA)，关联Feature Issue
4. 所有Task添加到GitHub Project跟踪

> 快速启动的bash模板见 `references/quickstart-template.md`
> 进度跟踪模板见 `references/progress-template.md`

**质量门禁**: Feature Issue已创建 + 所有角色Task已创建 → 进入 Phase 1

---

### Phase 1: 产品设计 (PM)

**目的**：完成PRD，明确功能范围、业务规则和验收标准。

**操作**：
1. 调用skill: `opentms-product-design`
2. 依赖检查: 检查是否有前置依赖特性未完成，中断流程并提示
3. 核验产出: PRD包含完整功能描述、业务规则无歧义、验收标准可量化、字段定义完整

**产出**: PRD文档 → `docs/prd/{模块}/` | Feature/US/Task → GitHub Project

**审核门禁**: 🔒 `opentms-review-requirement` — 评级A/B通过进Phase 2，C修复复审，D返工

---

### Phase 2: UX交互设计 (UX)

**目的**：完成界面原型设计，涵盖所有用户交互场景。

**操作**：
1. 调用skill: `opentms-ux-design`
2. 输入确认: 从PRD获取功能清单、用户角色、业务流程
3. 产出: 列表页/编辑页/详情页原型、流程页面原型、空状态/错误状态/加载状态设计

**产出**: UX原型文档 → `docs/原型/` | 交互说明

**审核门禁**: 🔒 `opentms-review-ux` — 评级A/B通过进Phase 3/4，C修复复审，D返工

---

### Phase 3+4: 数据库 + 接口并行设计

**目的**：基于PRD和原型，并行完成数据库设计和接口设计。

#### Phase 3: 表结构设计
1. 调用skill: `opentms-db-design`
2. 产出: 完整DDL SQL脚本(含审计字段、索引、约束)
3. 存入 `db/schema/`

#### Phase 4: 接口设计
1. 调用skill: `opentms-api-design`
2. 产出: RESTful接口定义(请求/响应结构、参数校验、错误码、权限)
3. 存入 `docs/api/`

**协作说明**: 表结构和接口设计可并行推进；接口字段类型需与表设计一致；复杂查询接口需在表设计中考虑索引。

**审核门禁**:
- 🔒 `opentms-review-db` — DDL审核，评级A/B通过
- 🔒 `opentms-review-api` — API审核，评级A/B通过

---

### Phase 5+6: 后端 + 前端并行开发

**目的**：基于设计产物，并行完成前后端代码开发。

#### Phase 5: 后端开发
1. 调用skill: `opentms-backend-dev`
2. 开发顺序: Entity → Mapper → Service (impl) → Controller
3. 产出: Entity/Mapper/Service/Controller/DTO/VO/单元测试
4. 提交: `git commit -m "feat({module}): 完成{特性名}后端开发"`

#### Phase 6: 前端开发
1. 调用skill: `opentms-frontend-dev`
2. 开发顺序: API层(api/xxx.js) → Store/状态 → 组件 → 页面 → 路由
3. 产出: API封装、列表页、编辑页、详情页、交互状态
4. 提交: `git commit -m "feat(web/{module}): 完成{特性名}前端开发"`

**联调检查点**: 后端API可访问 / 前端API调用正常 / 数据格式一致 / 错误处理正常 / 分页正常

**⚠️ 联调后必须启动本地服务**:
```bash
python .agents/skills/opentms-backend-dev/scripts/run_backend.py start  # 后端
cd web && npm run dev                                                    # 前端
```

**审核门禁**:
- 🔒 `opentms-review-backend` — 后端代码审核，评级A/B通过
- 🔒 `opentms-review-frontend` — 前端代码审核，评级A/B通过

---

### Phase 7+8: 测试设计与执行

#### Phase 7: 测试用例设计
1. 调用skill: `opentms-test-case-design`
2. 输入: PRD + API文档 + UX原型
3. 产出: API测试用例 + UI测试用例 + E2E测试用例 + 测试数据

#### Phase 8: 测试执行
1. 调用skill: `opentms-test-execution`
2. 执行顺序: P0用例(冒烟) → P1用例(功能) → P2用例(增强)

**Bug处理流程**:
- 发现Bug → 创建Bug Issue → 高优Bug(P0/P1)立即修复 → QA回归验证 → 关闭Bug
- 中低Bug(P2/P3)记录到缺陷列表，不阻塞测试继续

**质量门禁**: P0用例100%通过 + 所有P0/P1 Bug已修复 + 测试报告已生成

---

### Phase 9: 特性交付

**目的**：完成特性交付，更新发布说明。

**操作**：
1. 最终检查: 所有Phase的Task都已标记Done
2. 更新Feature Issue(含交付物链接、缺陷列表、遗留问题)
3. 更新版本发布说明
4. 关闭Feature Issue: `gh issue close <feature-issue-number>`

---

## 五、审核门禁体系

**核心原则**: 每个 Phase 完成后，**必须**启动对应审核 skill，审核通过才能进入下一阶段。

### 5.1 7 级审核门禁映射表

| Phase | 触发审核 | Skill | P0 必须通过 |
|-------|---------|-------|------------|
| 1 PM设计 | 需求审核 | opentms-review-requirement | ✓ |
| 2 UX设计 | UX审核 | opentms-review-ux | ✓ |
| 3 DB设计 | DB审核 | opentms-review-db | ✓ |
| 4 API设计 | API审核 | opentms-review-api | ✓ |
| 5 后端开发 | 后端审核 | opentms-review-backend | ✓ |
| 6 前端开发 | 前端审核 | opentms-review-frontend | ✓ |
| 7-8 测试 | QA自评 | opentms-test-execution | ✓ |
| **9 交付** | **6维全量复审** | **6个review skill** | **✓** |

### 5.2 评级体系

| 评级 | 含义 | 后续动作 |
|------|------|---------|
| **A** | 无P0/P1/P2问题 | 直接进入下一Phase |
| **B** | 仅P2问题(可优化不阻塞) | 直接进入下一Phase，P2记录到`[待优化]` |
| **C** | 有P1问题(必须修复) | 立即修复 → 复审 → 通过才能进下一Phase |
| **D** | 有P0问题(必须返工) | 退回该Phase返工 → 重新产出 → 重新审核 |

### 5.3 问题严重度

| 等级 | 定义 | 示例 | 阻塞 |
|------|------|------|------|
| **P0** | 阻塞性:违反核心规范/破坏存量/合规风险 | 缺失审计字段、模块循环依赖、缺失幂等键 | 强阻塞 |
| **P1** | 重要:影响可维护性/可测试性 | 缺单元测试、错误码不规范、阈值硬编码 | 修复后通过 |
| **P2** | 优化:代码风格/文档 | 命名风格、注释不全 | 不阻塞 |

### 5.4 审核输出

- **报告路径**: `docs/reviews/{feature-name}/{dimension}-review.md`
- **报告模板**: `.claude/skills/opentms-review-common/templates/report-base.md`
- **评级同步**: 评级(A/B/C/D)同步到GitHub Issue
- **签字**: 审核人 / 审核日期 / 复审日期(如有)

### 5.5 公共规范引用

所有审核skill共享公共基础: `.claude/skills/opentms-review-common/SKILL.md`
- 评级体系(`standards/rating-system.md`)
- 流程标准(`standards/workflow.md`)
- 报告模板(`templates/report-base.md`)

### 5.6 Phase 9: 交付前总审核

**触发时机**: 特性交付前，PM-Lead启动

**执行流程**:
1. 6维审核skill各跑一次(requirement / ux / db / api / backend / frontend)
2. 每个审核skill输出独立审核报告
3. PM-Lead汇总输出综合报告: `docs/reviews/{feature}/final-review.md`

**关闭条件**: 所有维度评级 >= B 且无P0问题

---

## 六、质量门禁总表

| 阶段 | 门禁项 | 触发下一阶段 |
|------|--------|-------------|
| Phase 1→2 | PRD评审通过 + 需求审核评级A/B | UX启动 |
| Phase 2→3/4 | 原型评审通过 + UX审核评级A/B | 设计启动 |
| Phase 3/4→5/6 | DDL+API评审通过 + DB/API审核评级A/B | 开发启动 |
| Phase 5/6→7 | 联调通过+无阻塞Bug + 后端/前端审核评级A/B | QA启动 |
| Phase 8→9 | P0通过率100%+无高优Bug | 总审核 |
| Phase 9→交付 | 6维全量复审全部P0通过 | Feature关闭 |

**门禁触发规则**:
- 审核评级 A/B → ✅ 直接进入下一阶段
- 审核评级 C → 🟡 修复P1后复审，通过才能进入
- 审核评级 D → 🔴 退回该Phase返工，重新审核

---

## 七、工具自优化原则

**核心原则：当发现工具脚本有问题时，应该修复工具，而不是绕过工具。**

| 发现的问题 | 正确做法 | 错误做法 |
|-----------|----------|----------|
| 测试脚本路径错误 | 修复脚本中的路径配置 | 改用curl手写命令 |
| 测试脚本功能缺陷 | 修复脚本功能 | 放弃脚本另写命令 |
| 服务启动脚本异常 | 修复启动脚本 | 手动启动服务 |

**自优化记录模板**:
```markdown
- 日期: YYYY-MM-DD
- 问题: {问题描述}
- 根因: {根本原因}
- 修复: {修复内容}
- 验证: {验证结果}
```

---

## 八、与其他Skill的关系

### 8.1 编排的角色Skill

| 角色 | Skill | 职责 |
|------|-------|------|
| PM | `opentms-product-design` | 产品需求设计 |
| BA | `opentms-business-architect` | 业务架构设计 |
| UX | `opentms-ux-design` | UX交互设计 |
| TA | `opentms-db-design` | 表结构设计 |
| TA | `opentms-api-design` | 接口设计 |
| BE | `opentms-backend-dev` | 后端开发 |
| FE | `opentms-frontend-dev` | 前端开发 |
| QA | `opentms-test-case-design` | 测试用例设计 |
| QA | `opentms-test-execution` | 测试执行 |

### 8.2 审核门禁Skill(6 + 1公共)

| 审核Skill | 审核维度 | 触发位置 |
|-----------|---------|---------|
| `opentms-review-common` | 公共基础(评级/报告/流程) | 所有审核前置引用 |
| `opentms-review-requirement` | 需求 | Phase 1 → 2 |
| `opentms-review-ux` | UX | Phase 2 → 3/4 |
| `opentms-review-db` | DB | Phase 3 → 5 |
| `opentms-review-api` | API | Phase 4 → 5 |
| `opentms-review-backend` | 后端代码 | Phase 5 → 6 |
| `opentms-review-frontend` | 前端代码 | Phase 6 → 7 |

### 8.3 本skill与角色skill的区别

| 对比维度 | 本skill (编排器) | 各角色skill (执行者) |
|----------|-----------------|---------------------|
| 定位 | 流程完整性——确保阶段衔接、门禁达标 | 专业深度——确保本领域最佳实践 |
| 受众 | PM-Lead/项目协调者 | 各角色专业人员 |
| 视角 | 水平视角——跨阶段、跨角色 | 垂直视角——本领域纵向深入 |
| 产出 | 进度跟踪、质量门禁检查、审核触发 | 专业交付物(PRD/原型/代码/测试) |

---

## 参考文档索引

以下参考文档从本SKILL.md中提取，按需查阅：

| 文档 | 路径 | 内容 |
|------|------|------|
| 快速启动模板 | `references/quickstart-template.md` | 一键创建Feature Issue + Task的bash脚本 |
| 场景指南 | `references/scenario-guide.md` | 简单CRUD/复杂业务/跨模块/Hotfix/审核跳过例外 |
| 产出物路径 | `references/output-paths.md` | 各阶段产出物存放路径总表 |
| 交付物检查清单 | `references/delivery-checklist.md` | 全流程交付物逐项检查清单 |
| 进度跟踪模板 | `references/progress-template.md` | Feature Issue进度看板模板 + 状态定义 |

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.3 | 2026-07-05 | **精简优化**: 1. 流程图从ASCII方块图改为文字描述 2. 参考内容外移到references/(快速启动/场景指南/产出路径/检查清单/进度模板) 3. 合并审核门禁体系(原5.3+5.4+7) 4. 删除重复的质量门禁表格 5. 从~1175行精简至~350行 |
| v1.2 | 2026-07-05 | 审核门禁体系集成(7级门禁+Phase 9总审核+评级A/B/C/D) |
| v1.1 | 2026-05-27 | GitHub Project协同 + 本地服务启动 + Bug修复流程 |
| v1.0 | 2026-05-01 | 初始版本 |
