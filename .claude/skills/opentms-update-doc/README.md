# opentms-update-doc Skill

> v1.0 — 2026-07-11

## 一句话

在 `git commit` 前**自动更新 4 类文档**:`summary.md` / `CLAUDE.md` / `open-tms功能特性清单.md` + 调用 self-improve。

## 文件

| 文件 | 作用 |
|------|------|
| `SKILL.md` | 完整 skill 文档(触发/规则/决策/关系) |
| `scripts/update_doc.py` | 主程序(扫描 git diff → changelog → 自动 patch) |
| `scripts/install-hook.sh` | pre-commit hook 安装脚本 |

## 快速使用

### 1) 手动跑(不安装 hook)

```bash
python .claude/skills/opentms-update-doc/scripts/update_doc.py              # 全流程
python .claude/skills/opentms-update-doc/scripts/update_doc.py --check-only  # 只检查
python .claude/skills/opentms-update-doc/scripts/update_doc.py --unstaged    # 看未 staged
```

### 2) 安装 pre-commit hook(推荐)

```bash
bash .claude/skills/opentms-update-doc/scripts/install-hook.sh
```

之后 `git commit` 时自动跑。

### 3) 跳过 hook(紧急)

```bash
git commit --no-verify
```

## 4 类文档更新规则

| 文档 | 检测 | 自动改 | 模式 |
|------|------|--------|------|
| `summary.md` | 所有变更 | ✅ | 追加"📅 自动更新 {date}"小节 |
| `CLAUDE.md` | 模块/枚举/表 | 保守 | 只打印建议,**需手动确认** |
| `open-tms功能特性清单.md` | 新 PRD/原型/DDL | 保守 | 只打印建议 |
| self-improve | 任何 | 模拟 | 追加到 summary 末尾 |

## 触发场景

- ✅ `git commit`(pre-commit hook)
- ✅ 特性完成时(`opentms-feature-dev` Phase 5/6/8/9 末尾)
- ✅ Bug 修复后(`opentms-bug-fix` Step 6 改进措施落地)
- ✅ 用户手动说"update doc"
- ✅ 每周一次(PM-Lead 周报生成时)

## 与其他 Skill 关系

```
opentms-pm-lead
       ↓ 触发
opentms-update-doc ←──┐
       ↓               │
   更新 4 类文档         │ 调用
       ↓               │
   summary.md ─────────┘ (self-improve 模式)
```

详见 `SKILL.md` 第六章。
