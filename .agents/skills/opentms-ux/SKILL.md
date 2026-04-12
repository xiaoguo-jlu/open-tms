---
name: opentms-ux
description: Use when designing Open-TMS UI/UX as UX Designer
---

# Open-TMS UX Designer Skill

## Overview
UX负责用户交互设计、界面原型、用户体验优化。使用GitHub Projects管理设计任务。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms

## 核心职责

### 1. 领取设计任务
```bash
# 查看分配给自己的任务
gh issue list --label "UX"

# 开始设计
gh issue edit <number> --body "## 设计状态\nIn Progress\n\n## 设计计划\n1. 低保真原型\n2. 高保真设计\n3. 交互说明"
```

### 2. 设计交付
```bash
# 创建设计任务
gh issue create \
  --title "[UX] 银行账户管理页面设计" \
  --body "## 设计要求

### 页面类型
账户管理 - 列表页

### 功能需求
1. 账户列表展示（表格）
2. 新增/编辑/删除按钮
3. 搜索/筛选功能
4. 账户余额显示

### 设计规范
- 主色调: 企业蓝 #1E40AF
- 辅助色: 灰色 #6B7280
- 背景: 白色 #FFFFFF
- 间距: 8px网格系统
- 字体: 思源黑体 / Inter

### 交互说明
- 新增: 弹窗表单
- 编辑: 页面跳转
- 删除: 二次确认弹窗
- 列表: 支持分页和排序

### 设计稿
Figma链接: https://figma.com/xxx
(可使用图片链接替代)

### 输出物
- 页面布局图
- 组件状态说明
- 交互流程图" \
  --label "UX,Task"
```

### 3. 设计完成
```bash
# 更新设计状态
gh issue edit <number> --body "## 设计交付

### 已完成
- [x] 账户列表页原型
- [x] 新增账户弹窗
- [x] 编辑账户页面
- [x] 删除确认弹窗

### 设计稿
![设计稿](图片链接)

### 评审意见
- PM: 确认通过
- Dev: 确认可实现" 

# 关闭设计任务
gh issue close <number>
```

## 设计规范

### 配色方案
```
主色: #1E40AF (企业蓝)
辅助色: #6B7280 (灰色)
背景: #F9FAFB (浅灰)
白色: #FFFFFF
成功: #10B981
警告: #F59E0B
错误: #EF4444
```

### 组件规范
- 按钮: 高度40px，圆角4px
- 输入框: 高度36px，圆角4px
- 表格: 行高48px
- 卡片: 圆角8px，阴影

### 间距系统
```
xs: 4px
sm: 8px
md: 16px
lg: 24px
xl: 32px
```

### 字体
```
标题: 20px, 600 weight
副标题: 16px, 500 weight
正文: 14px, 400 weight
小字: 12px, 400 weight
```

## 设计模板

### 页面设计
```markdown
## 页面名称
[页面名称]

### 页面类型
列表页 / 表单页 / 详情页

### 功能
1. 功能1
2. 功能2

### 布局
[布局简述]

### 组件
| 组件 | 位置 | 说明 |
|------|------|------|
| 搜索栏 | 顶部 | 支持关键字搜索 |
| 表格 | 中部 | 列表展示 |
| 分页 | 底部 | 10/20/50条每页 |

### 交互
- 点击新增: 打开弹窗
- 点击编辑: 跳转页面
- 点击删除: 确认弹窗
```

### 组件设计
```markdown
## 组件名称
[组件名称]

### 状态
- 默认: 样式描述
- 悬停: 样式描述
- 激活: 样式描述
- 禁用: 样式描述

### 尺寸
- 宽度: 自适应/固定值
- 高度: 固定值

### 交互
[交互说明]
```

## 交付检查清单
- [ ] 是否满足用户体验
- [ ] 是否与PM确认需求
- [ ] 是否与Dev对齐实现
- [ ] 设计稿是否完整

## 快速参考

| 操作 | 命令 |
|------|------|
| 查看设计任务 | `gh issue list --label "UX"` |
| 创建设计任务 | `gh issue create --label "UX,Task"` |
| 更新设计 | `gh issue edit <number> --body` |
| 完成设计 | `gh issue close <number>` |

## 设计输出要求
1. 提供Figma/图片链接
2. 说明交互流程
3. 提供组件状态说明
4. 标注关键尺寸