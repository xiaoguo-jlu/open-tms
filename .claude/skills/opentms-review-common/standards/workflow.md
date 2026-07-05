# Open-TMS 审核流程标准

> 本文档定义审核的完整工作流程,被 6 个审核 skill 强制遵循。

---

## 1. 审核生命周期

```
触发审核 → 加载审核项 → 执行审核 → 输出报告 → 评级判定 → 流程处置
   │                                                    │
   │                                                    ├── A/B → 进入下一 Phase
   │                                                    ├── C → 修复后复审
   │                                                    └── D → 退回该 Phase 返工
   │
   └── 归档报告 → docs/reviews/{feature}/{dimension}-review.md
```

---

## 2. 审核触发条件

### 2.1 在 feature-dev 流程内(强制)

每个 Phase 出口必须触发审核:

| Phase | 触发审核 Skill | 时机 |
|-------|---------------|------|
| Phase 1 PM 设计 | opentms-review-requirement | PRD 完成后 |
| Phase 2 UX 设计 | opentms-review-ux | 原型完成后 |
| Phase 3 DB 设计 | opentms-review-db | DDL 完成后 |
| Phase 4 API 设计 | opentms-review-api | API 文档完成后 |
| Phase 5 后端开发 | opentms-review-backend | 代码提交前 |
| Phase 6 前端开发 | opentms-review-frontend | 代码提交前 |
| Phase 7 测试设计 | (QA 自评) | 测试用例完成后 |
| Phase 8 测试执行 | opentms-test-execution | 测试报告生成后 |
| Phase 9 交付 | (6 维全量复审) | 交付前 |

### 2.2 在 feature-dev 流程外(可选)

- 重大重构前(PM-Lead 决定)
- 外部审计要求时
- 上线前最终审核

---

## 3. 审核执行步骤

### Step 1: 接收审核请求

PM-Lead 或 Phase 责任人发起:
```
请审核 {产出物类型}: {文件路径}
所属 Phase: {Phase N}
所属模块: {Maven 模块名}
调用 skill: opentms-review-{dimension}
```

### Step 2: 加载审核 Skill

调用对应的审核 skill(6 个之一),加载:
- `opentms-review-common/SKILL.md`(公共规范)
- 审核项 checklist(各 skill 自带)
- 关联的规范文档(CLAUDE.md / 开发规范文档)

### Step 3: 执行逐项审核

按 checklist 逐项检查,记录:

```yaml
- id: REQ-001
  name: 业务字段完整性
  severity: P0
  result: PASS / FAIL
  comment: "{具体说明}"
```

### Step 4: 输出审核报告

按 `templates/report-base.md` 模板生成报告:
- 路径: `docs/reviews/{feature-name}/{dimension}-review.md`
- 评级: A/B/C/D
- P0/P1/P2 问题清单

### Step 5: 同步到 GitHub Issue

在 Feature Issue 中添加审核结论:

```markdown
## Phase {N} 审核结论

- 审核维度: {requirement / ux / db / api / backend / frontend}
- 评级: **{A/B/C/D}**
- 审核人: {角色}
- 审核日期: YYYY-MM-DD
- 报告: docs/reviews/{feature}/{dimension}-review.md
- 下一步: {进入下一 Phase / 修复后复审 / 返工}
```

### Step 6: 流程处置

| 评级 | 处置 |
|------|------|
| A/B | ✅ 通过 → 关闭当前 Phase Task → 进入下一 Phase |
| C | 🟡 修复 P1 → 复审 → 通过后进入下一 Phase |
| D | 🔴 退回 Phase N → 返工 → 重新产出 → 重新审核 |

---

## 4. 复审流程

**触发条件**:评级为 C 时(修复 P1 后)

**流程**:
1. 责任人修复 P1 问题
2. 提交修复(commit + push)
3. 重新调用审核 skill
4. 输出新报告(标注 "复审")
5. 评级 A/B 才能通过

**禁止**:
- 直接跳过审核(违反流程门禁)
- 仅口头确认 P1 已修复(必须重新走审核 skill)

---

## 5. 返工流程

**触发条件**:评级为 D 时(含 P0 问题)

**流程**:
1. PM-Lead 在 Feature Issue 中标注 "返工"
2. 责任人重新执行整个 Phase(不仅是修复 P0)
3. 重新产出全部交付物
4. 重新调用审核 skill
5. 评级 A/B/C 才能通过

**核心原则**:
- P0 零容忍
- 返工不是仅修 P0,而是重做整个 Phase
- 避免 "凑合修一下" 导致问题扩散

---

## 6. 审核报告归档

### 6.1 路径规则

```
docs/reviews/
├── {feature-name-1}/
│   ├── requirement-review.md
│   ├── ux-review.md
│   ├── db-review.md
│   ├── api-review.md
│   ├── backend-review.md
│   ├── frontend-review.md
│   └── final-review.md       # Phase 9 总审核
├── {feature-name-2}/
│   └── ...
└── INDEX.md                   # 审核索引(可选)
```

### 6.2 Feature Issue 关联

每个 Feature Issue 必须关联所有审核报告:

```markdown
## 审核记录

| Phase | 审核维度 | 评级 | 报告链接 |
|-------|---------|------|---------|
| 1 | 需求 | A | [requirement-review](docs/reviews/...) |
| 2 | UX | B | [ux-review](docs/reviews/...) |
| 3 | DB | A | [db-review](docs/reviews/...) |
| ... | ... | ... | ... |
```

---

## 7. 审核与流程门禁

审核通过是进入下一 Phase 的**必要条件**(非充分条件):

```
Phase N 完成
   │
   ▼
触发审核(opentms-review-{N})
   │
   ├── 评级 A/B → 通过门禁 → 进入 Phase N+1
   ├── 评级 C   → 修复 → 复审 → 通过 → 进入 Phase N+1
   └── 评级 D   → 返工 Phase N → 重新产出 → 重新审核
```

---

## 8. 审核质量度量

PM-Lead 应持续跟踪:

| 指标 | 公式 | 目标 |
|------|------|------|
| 一次通过率 | (A+B 数) / 总审核数 | ≥ 70% |
| 平均返工次数 | 总返工次数 / Feature 数 | ≤ 0.3 |
| 平均修复时长 | 总修复时长 / 复审次数 | ≤ 4h |
| 高频 P0 数 | 反复出现的 P0 类型数 | 持续下降 |

---

## 9. 常见问题

### Q1: 如何处理 "审核范围争议"?

答:由 PM-Lead 裁决。审核 skill 不应越界审核职责范围之外的内容。

### Q2: 紧急修复(Hotfix)是否需要审核?

答:见 `opentms-feature-dev` 第 8.5 节。Hotfix 可跳过审核,但需在 PR 描述中注明。

### Q3: 同一 Phase 是否可多次审核?

答:可以。修复后复审、返工后重审都是同 Phase 的多次审核。

### Q4: 审核报告是否可简化?

答:不可以。所有审核报告必须按 `templates/report-base.md` 完整输出。

---

## 10. 相关文档

- `opentms-review-common/SKILL.md` — 公共规范
- `opentms-feature-dev/SKILL.md` 第 5.4 节 — 审核门禁体系
- `standards/rating-system.md` — 评级体系
- `templates/report-base.md` — 报告模板