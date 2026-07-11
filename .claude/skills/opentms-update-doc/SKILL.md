---
name: opentms-update-doc
description: |
  Open-TMS 文档自动更新 Skill。在 git commit 前自动触发,
  根据代码和文档的 diff,自动更新 summary.md / CLAUDE.md / open-tms功能特性清单.md,
  并调用 self-improve 技能更新项目总结。
  主代理手动调用或通过 .git/hooks/pre-commit 自动调用。

  Trigger: "update doc"、"更新文档"、"commit 前"、"self-improve"、"项目总结"
---

# opentms-update-doc

Open-TMS **文档自动更新** Skill —— 在 git commit 前根据代码变动自动更新 4 类文档,
**避免文档腐烂 + 强制知识沉淀**。

> **核心价值**:让 Open-TMS 的项目进度、规范、功能清单始终与代码同步,
> 让 PM-Lead 不用手动维护,让团队成员随时拿到最新文档。

---

## 一、Skill 触发

| 触发方式 | 时机 | 执行者 |
|---------|------|--------|
| **pre-commit hook** | `git commit` 前 | `scripts/update-doc-precommit.sh`(自动) |
| **手动调用** | 用户主对话说"update doc" | 主代理(Claude) |
| **特性完成时** | Phase 5/6/8/9 完成 | 主代理 |
| **每周一次** | 周报生成时 | PM-Lead |

**manual 用法**:
```bash
bash scripts/update-doc.py                    # 全量更新
bash scripts/update-doc.py --only summary     # 只更新 summary
bash scripts/update-doc.py --check-only       # 只检查不写
```

---

## 二、4 类文档更新规则

### 2.1 `summary.md`(项目总览)

**检测 diff**:
- 新增模块(`modules/{new-module}/` 出现)
- 新增端点(`@Path` 出现)
- 新增/修改的 `.vue` 页面
- 新增的 `db/schema/*.sql`
- 新增的 Skill

**更新内容**:
- 模块清单表
- "近期重要变更" 章节
- "已完成 PRD/原型" 章节
- 文档版本号 + 日期

### 2.2 `CLAUDE.md`(开发规范)

**检测 diff**:
- 新增公共枚举/常量 → 提示加到 "Key Enums" 章节
- 新增公共模式(交易架构/审批流) → 提示加到对应小节
- 新增模块 → 更新 "Active Modules" 表
- 新增数据库表 → 提示加到 "Database Conventions"
- 新增端点规范 → 提示加到 "REST API Patterns"

**更新方式**:
- **保守模式**(默认): 只**打印建议**,主代理确认后改 CLAUDE.md
- **激进模式**(`--auto`): 自动 patch

### 2.3 `open-tms功能特性清单.md`

**检测 diff**:
- 新增 `docs/prd/M*/` 目录 → 加到"已完成 PRD"列表
- 新增 `docs/原型/M*/` HTML → 加到"UX 原型"列表
- 新增 `db/schema/*-v*.sql` → 加到"已完成特性"列表

**更新内容**:
- 维护"特性完成进度"表
- 按 M1-M5 分类

### 2.4 调用 self-improve 更新项目总结

> 注:本项目**没有**真正的 self-improve skill 库,这里调用的是"self-improve"模式
> —— 主代理读 `summary.md` + `CLAUDE.md` + git log,**自动追加一段"本次更新"小节**。

**更新方式**:
```markdown
## 2026-07-11 自动更新
- 新增模块 X
- 新增端点 /api/v1/...
- 新增页面 /path
- 修复 bug #N
```

---

## 三、pre-commit hook 安装

### 3.1 自动安装

```bash
bash .claude/skills/opentms-update-doc/scripts/install-hook.sh
```

把 `scripts/update-doc-precommit.sh` 复制到 `.git/hooks/pre-commit` 并加 +x。

### 3.2 hook 逻辑

```bash
#!/bin/bash
# pre-commit hook
echo "[opentms-update-doc] 检查文档更新..."

# 1) 跑 update-doc.py --check-only
python .claude/skills/opentms-update-doc/scripts/update_doc.py --check-only

# 2) 如果有需要更新,提示开发者
if [ $? -ne 0 ]; then
    echo "[WARN] 检测到文档需要更新,先跑: bash scripts/update-doc.py"
    echo "[AUTO] 自动执行更新..."
    python .claude/skills/opentms-update-doc/scripts/update_doc.py --auto
    # 把更新后的文档加入 commit
    git add summary.md CLAUDE.md "open-tms功能特性清单.md"
fi
```

---

## 四、执行脚本设计

### 4.1 `scripts/update_doc.py`(主程序)

**输入**:`git diff --staged --name-status`(staged 文件)

**输出**:3 个 .md 文件的 diff 报告 + 自动 patch(可选)

**核心算法**:
```
1. git diff --staged --name-status → 变更文件列表
2. 分类:
   - basedata/dealing/pom.xml  → 模块变更
   - controller/**Resource.java → 端点变更
   - web/src/views/**.vue → 页面变更
   - db/schema/*.sql → DDL 变更
   - .claude/skills/*/SKILL.md → Skill 变更
3. 累积到"本次更新"小节
4. 自动追加到 summary.md
5. 检查并提示 CLAUDE.md 更新
6. 检查并更新 open-tms功能特性清单.md
```

### 4.2 预置 changelog 提取器

| 文件类型 | 提取信息 | 工具 |
|---------|---------|------|
| `*.java` Controller | `@Path` 注解 + `@GET/@POST` 方法签名 | regex |
| `*.vue` | 路由 path(`web/src/router/index.js`) | regex |
| `*.sql` | `CREATE TABLE` 表名 | regex |
| `SKILL.md` | skill name + description | YAML frontmatter |
| `pom.xml` | module 名 | XML parse |

---

## 五、关键设计决策

| 决策 | 原因 |
|------|------|
| **pre-commit 默认只 --check-only** | 避免每次 commit 都改文档,产生噪音 commit |
| **`--auto` 模式需用户显式开启** | 大改动要人眼确认,避免误改 CLAUDE.md |
| **保留 `git add` 旧行为** | 文档更新作为单独 commit(由主代理加 `--commit-docs`) |
| **不强制 commit 时更新** | 频繁 commit 的开发者不会被频繁打断 |
| **提供 dry-run** | 调试 / 预览 |

---

## 六、与其他 Skill 关系

| Skill | 关系 |
|------|------|
| `opentms-pm-lead` | PM-Lead 触发特性完成时调用本 skill |
| `opentms-bug-fix` | 修 bug 后调用本 skill 更新 summary/CLAUDE.md |
| `opentms-feature-dev` | Phase 5/6/8/9 完成时调用本 skill |
| `self-improve`(虚构) | 本 skill 在末尾模拟 self-improve,追加"本次更新"段 |

---

## 七、参考案例

参考最近已完成的 5 个 bug 修复 + 6 阶段特性流程:
- `summary.md` 应该反映这些工作
- `CLAUDE.md` 应该记录新规约(REST 响应 5 字段)
- `open-tms功能特性清单.md` 应该记录新完成特性

---

## 八、版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-11 | 初版,4 类文档自动更新 + pre-commit hook |
