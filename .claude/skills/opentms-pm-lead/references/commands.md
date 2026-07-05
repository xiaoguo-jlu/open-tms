# PM-Lead 常用命令速查

> 从 SKILL.md 第九节移出。

## Issue管理

```bash
# 创建任务
gh issue create --title "[Dev] {任务名}" --body "..." --label "Dev,Task"

# 创建特性
gh issue create --title "[Feature] {特性名}" --body "..." --label "PM,Feature"

# 创建Bug
gh issue create --title "[Bug] {缺陷描述}" --body "..." --label "Bug,Dev"

# 更新状态
gh issue edit <number> --add-label "Done"

# 分配责任人
gh issue edit <number> --add-label "Dev"

# 关闭Issue
gh issue close <number>
```

## 进度查看

```bash
# 查看所有进行中
gh issue list --state open

# 按角色筛选
gh issue list --label "Dev"
gh issue list --label "QA"

# 查看阻塞项
gh issue list --search "阻塞"

# 查看已延期
gh issue list --state open --due before:2026-05-27
```

## 统计报告

```bash
# 查看交付效率
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly

# 查看Agent工作量
gh issue list --state closed --label "Dev" --limit 30 --json number,title,closedAt
```
