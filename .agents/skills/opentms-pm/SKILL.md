---
name: opentms-pm
description: Use when managing Open-TMS requirements and features as Product Manager
---

# Open-TMS Product Manager Skill

## Overview
PM负责需求分析、功能设计、PRD撰写、验收。使用GitHub Projects管理需求。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms

## 核心职责

### 1. 创建需求 (Feature)
```bash
gh issue create \
  --title "[Feature] 银行账户CRUD功能" \
  --body "## 业务背景
企业财务人员需要管理多个银行账户。

## 功能需求
1. 新增银行账户
2. 编辑银行账户信息
3. 删除银行账户（逻辑删除）
4. 账户余额查询

## 验收标准
- [ ] 新增账户成功保存到数据库
- [ ] 编辑后数据正确更新
- [ ] 删除后账户状态变为停用
- [ ] 余额显示正确" \
  --label "PM,Feature"
```

### 2. 创建用户故事 (US)
```bash
gh issue create \
  --title "[US] 财务人员可查看账户余额" \
  --body "## 用户场景
作为财务人员，我希望能够实时查看各银行账户的余额，以便了解资金状况。

## 验收条件
- 账户列表显示余额
- 余额精确到分
- 支持刷新" \
  --label "PM,US"
```

### 3. 创建Bug
```bash
gh issue create \
  --title "[Bug] 账户新增未校验必填项" \
  --body "## 问题描述
新增账户时，不填写账户名称可直接保存，导致数据异常。

## 复现步骤
1. 打开账户管理页面
2. 点击新增按钮
3. 不填写账户名称，直接点击保存
4. 预期：提示账户名称必填
5. 实际：保存成功

## 严重程度
高" \
  --label "PM,Bug"
```

### 4. 需求管理
```bash
# 查看自己创建的需求
gh issue list --label "PM"

# 查看Feature类型
gh issue list --label "Feature"

# 更新需求描述
gh issue edit <number> --body "新描述内容"

# 验收通过后关闭
gh issue close <number>
```

## 需求模板

### Feature模板
```markdown
## 业务背景
[说明业务场景和需求来源]

## 功能需求
1. [需求点1]
2. [需求点2]
3. [需求点3]

## 验收标准
- [ ] 验收点1
- [ ] 验收点2

## 优先级
- P0: 必须
- P1: 重要
- P2: 可选
```

### US模板
```markdown
## 用户角色
[谁在执行这个操作]

## 用户场景
作为[角色]，我希望[功能]，以便[收益]。

## 验收条件
- [ ] 条件1
- [ ] 条件2

## 优先级
高/中/低
```

## 交付检查清单
- [ ] 需求描述是否完整
- [ ] 优先级是否合理
- [ ] 验收标准是否明确
- [ ] 是否与UX/TA对齐设计方案

## 快速参考

| 操作 | 命令 |
|------|------|
| 创建Feature | `gh issue create --label "PM,Feature"` |
| 创建US | `gh issue create --label "PM,US"` |
| 创建Bug | `gh issue create --label "PM,Bug"` |
| 查看需求 | `gh issue list --label "PM"` |
| 关闭任务 | `gh issue close <number>` |