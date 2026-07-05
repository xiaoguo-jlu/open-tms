---
name: opentms-review-common
description: |
  Open-TMS 审核公共基础 — 被 6 个审核 skill 引用(requirement / ux / db / api / backend / frontend)。
  提供统一评级体系、报告格式、调用方式、文档归档路径。
  本 skill 不直接执行审核,只提供公共规范。

  Trigger: 任何审核类 skill 调用前应先加载本 skill 作为基础。
---

# opentms-review-common

Open-TMS **审核体系公共基础** — 6 维度审核 skill 共享的规范与模板。

> 本 skill 是**元 skill**,不直接执行任何维度的审核,而是为 6 个审核 skill 提供
> 统一规范,确保审核报告格式、评级口径、调用方式、归档路径完全一致。

---

## 1. 适用范围

被以下 6 个审核 skill **强制引用**:

| 审核 Skill | 审核维度 | 触发位置 |
|-----------|---------|---------|
| `opentms-review-requirement` | 需求审核 | Phase 1 → Phase 2 |
| `opentms-review-ux` | UX 审核 | Phase 2 → Phase 3 |
| `opentms-review-db` | DB 审核 | Phase 3 → Phase 4 |
| `opentms-review-api` | API 审核 | Phase 4 → Phase 5 |
| `opentms-review-backend` | 后端代码审核 | Phase 5 → Phase 6 |
| `opentms-review-frontend` | 前端代码审核 | Phase 6 → Phase 7 |

---

## 2. 统一评级体系

### 2.1 评级 (Rating)

| 评级 | 含义 | 后续动作 |
|------|------|---------|
| **A** | 无 P0/P1/P2 问题 | 直接进入下一 Phase |
| **B** | 仅 P2 问题(可优化但不阻塞) | 直接进入下一 Phase,P2 记录到 `[待优化]` |
| **C** | 有 P1 问题(必须修复后通过) | 立即修复 → 复审 → 通过才能进下一 Phase |
| **D** | 有 P0 问题(必须返工) | 退回该 Phase 返工 → 重新审核 |

### 2.2 问题严重度 (Severity)

| 等级 | 定义 | 示例 | 阻塞 |
|------|------|------|------|
| **P0** | 阻塞性问题:违反核心规范/破坏存量/合规风险 | 缺失审计字段、模块循环依赖、缺失幂等键、命名违规 | **强阻塞** |
| **P1** | 重要问题:影响可维护性/可测试性/扩展性 | 缺少单元测试、错误码不规范、阈值硬编码 | 修复后通过 |
| **P2** | 优化建议:代码风格/文档/注释 | 命名风格、注释不全、变量命名 | **不阻塞**,记录到待优化 |

### 2.3 总评级计算规则

```
总评级 = MAX(所有审核项的严重度)
  - 含 P0 → D
  - 无 P0 含 P1 → C
  - 无 P0/P1 含 P2 → B
  - 无任何问题 → A
```

---

## 3. 统一审核报告格式

### 3.1 报告路径

```
docs/reviews/{feature-name}/{dimension}-review.md
```

**示例**:
```
docs/reviews/fx-deal/api-review.md
docs/reviews/fx-deal/db-review.md
docs/reviews/fx-deal/requirement-review.md
```

### 3.2 报告 Markdown 模板

使用 `templates/report-base.md`(见同目录)。所有 6 个审核 skill 必须遵循该模板。

### 3.3 报告必备章节

每份审核报告必须包含:

1. **审核元数据** — 审核对象 / 审核人 / 审核日期 / 审核维度
2. **总评级** — A/B/C/D + 一句话总结
3. **审核项清单** — 表格列出所有审核项、严重度、是否通过
4. **P0 问题详情** — 阻塞性问题清单(必须含修复建议)
5. **P1 问题详情** — 重要问题清单
6. **P2 优化建议** — 不阻塞,记录到待优化
7. **下一步动作** — 通过 → 进下一 Phase / 不通过 → 返工该 Phase

---

## 4. 统一调用方式

### 4.1 标准调用格式

```
请审核 {产出物类型}: {文件路径}
所属 Phase: {Phase N}
所属模块: {Maven 模块名}
调用 skill: opentms-review-{dimension}
```

### 4.2 调用示例

```
请审核 PRD: docs/prd/m2/fx-deal.md
所属 Phase: Phase 1
所属模块: fx
调用 skill: opentms-review-requirement

请审核 DDL: db/schema/15-fx.sql
所属 Phase: Phase 3
所属模块: fx
调用 skill: opentms-review-db

请审核 API 文档: docs/api/fx/03-fx-deal-api.md
所属 Phase: Phase 4
所属模块: fx
调用 skill: opentms-review-api
```

### 4.3 输出位置

审核完成后,自动生成报告到 `docs/reviews/{feature-name}/{dimension}-review.md`,
同时在 GitHub Issue 中同步评级。

---

## 5. 审核不通过的处置

| 评级 | 处置 |
|------|------|
| **A/B** | 通过,继续下一 Phase |
| **C** | 修复 P1 问题 → 复审 → 通过后继续 |
| **D** | 返工该 Phase → 重新产出 → 重新审核 |

**核心原则**:P0 问题**零容忍**,必须返工或修复后才能进入下一 Phase。

---

## 6. 与 feature-dev 的集成

`opentms-feature-dev` 在每个 Phase 出口强制插入审核门禁:

```
Phase 1 PM 设计 → [需求审核 opentms-review-requirement] → Phase 2
Phase 2 UX 设计 → [UX 审核 opentms-review-ux] → Phase 3
Phase 3 DB 设计 → [DB 审核 opentms-review-db] → Phase 4
Phase 4 API 设计 → [API 审核 opentms-review-api] → Phase 5
Phase 5 后端开发 → [后端审核 opentms-review-backend] → Phase 6
Phase 6 前端开发 → [前端审核 opentms-review-frontend] → Phase 7
Phase 7 测试设计 → [QA 自评] → Phase 8
Phase 8 测试执行 → [测试报告审核] → 交付
Phase 9 交付前总审核 (6 维全量复审)
```

详见 `.claude/skills/opentms-feature-dev/SKILL.md` 第 5.4 节"审核门禁体系"。

---

## 7. 跳过审核的例外

仅以下情况可跳过审核:

| 例外 | 条件 | 后续动作 |
|------|------|---------|
| **Hotfix** | 紧急修复 P0/P1 Bug | PR 描述中注明 "Hotfix - 跳过审核" |
| **文档修改** | 仅修改 `.md` 注释/文档 | 无需审核 |
| **配置调整** | 调整参数(如端口/超时) | 需 PM-Lead 口头确认 |

**禁止跳过**:
- 任何涉及新表/新字段的 DDL
- 任何新增/修改 API
- 任何业务逻辑变更

---

## 8. 相关文件

- `templates/report-base.md` — 报告 Markdown 模板
- `templates/summary.md` — 一页纸摘要模板
- `standards/rating-system.md` — 评级体系详细定义
- `standards/workflow.md` — 审核流程标准
- `examples/sample-review.md` — 审核报告示例

---

## 9. 相关 Skills

- `opentms-feature-dev` — 全流程编排器(在每 Phase 出口嵌入本体系)
- `opentms-pm-lead` — PM-Lead 管理(负责关闭 Feature Issue)
- 6 个审核 skill: `opentms-review-{requirement,ux,db,api,backend,frontend}`

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本 — 审核公共规范体系 |