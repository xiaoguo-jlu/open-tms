# opentms-review-common

Open-TMS 审核体系的**公共基础 skill**。

被 6 个审核 skill 强制引用:
- `opentms-review-requirement`
- `opentms-review-ux`
- `opentms-review-db`
- `opentms-review-api`
- `opentms-review-backend`
- `opentms-review-frontend`

## 目录结构

```
opentms-review-common/
├── SKILL.md                  # 公共规范总入口
├── README.md                 # 本文件
├── templates/
│   ├── report-base.md        # 审核报告 Markdown 模板(强制)
│   └── summary.md            # 一页纸摘要模板
├── standards/
│   ├── rating-system.md      # A/B/C/D 评级体系
│   └── workflow.md           # 审核流程标准
└── examples/
    └── sample-review.md      # 审核报告示例(FX Deal PRD)
```

## 如何被引用

每个审核 skill 必须在 `SKILL.md` 顶部声明引用:

```yaml
---
name: opentms-review-requirement
description: ...
references:
  - skill: opentms-review-common
    files:
      - SKILL.md
      - templates/report-base.md
      - standards/rating-system.md
---
```

## 提供的公共能力

| 能力 | 文件 | 说明 |
|------|------|------|
| 评级体系 | `standards/rating-system.md` | A/B/C/D + P0/P1/P2 定义 |
| 流程标准 | `standards/workflow.md` | 审核触发→执行→报告→处置 |
| 报告模板 | `templates/report-base.md` | 统一 Markdown 格式 |
| 摘要模板 | `templates/summary.md` | Issue 中快速展示 |
| 报告示例 | `examples/sample-review.md` | 完整示例 |

## 与 feature-dev 集成

`opentms-feature-dev` 在每个 Phase 出口强制触发审核门禁,
审核 skill 统一从本 skill 加载公共规范。

详见 `.claude/skills/opentms-feature-dev/SKILL.md` 第 5.4 节。