---
name: opentms-pm-lead
description: Use when managing Open-TMS project with GitHub Projects as PM-Lead
---

# Open-TMS PM-Lead Skill

## Overview
PM-Lead负责项目整体规划、进度管理、团队协调、交付把关。使用GitHub Projects进行任务管理。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms
- **标签**: PM, TA, PM-Lead, Dev, QA, UX, Feature, US, Task, Bug, 技术债务

## 核心职责

### 1. 创建任务
```bash
# 创建任务（使用标签区分责任人）
gh issue create --title "[M1-2] 银行账户表设计" --body "## 背景\n..." --label "TA,Task"

# 创建阻塞项
gh issue create --title "🔴 账户余额扣减机制确认" --body "阻塞项说明" --label "PM-Lead"

# 创建需求
gh issue create --title "[Feature] 银行账户CRUD" --body "## 需求描述" --label "PM,Feature"
```

### 2. 任务分配
使用Label标识责任人：
- `TA` - 技术架构师任务
- `Dev` - 开发工程师任务
- `QA` - 测试工程师任务
- `UX` - UX设计任务
- `PM` - 产品经理任务

### 3. 追踪进度
```bash
# 查看所有任务
gh issue list

# 查看特定标签任务
gh issue list --label "PM-Lead"
gh issue list --label "Todo"

# 查看详情
gh issue view <number>
```

### 4. 验收完成
```bash
# 关闭任务
gh issue close <number>

# 添加完成说明
gh issue edit <number> --body "## 完成情况\n- [x] 已完成..."
```

## 任务状态流转
```
Todo → In Progress → Done
```

## 常用操作
```bash
# 查看M1相关任务
gh issue list --label "M1" 2>/dev/null || gh issue list

# 创建用户故事
gh issue create --title "[US] 财务人员查看账户余额" --body "## 用户场景" --label "PM,US"

# 标记阻塞项
gh issue create --title "🔴 阻塞项标题" --body "阻塞原因" --label "PM-Lead"
```

## 交付检查清单
- [ ] 项目计划是否清晰
- [ ] 各角色任务是否明确分配
- [ ] 阻塞问题是否及时解决
- [ ] 交付物质量是否达标

## 快速参考

| 操作 | 命令 |
|------|------|
| 创建任务 | `gh issue create --title "标题" --body "描述" --label "标签"` |
| 查看任务 | `gh issue list` |
| 查看详情 | `gh issue view <number>` |
| 编辑任务 | `gh issue edit <number> --title "新标题"` |
| 关闭任务 | `gh issue close <number>` |
| 添加标签 | `gh issue edit <number> --add-label "新标签"` |