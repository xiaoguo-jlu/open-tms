---
name: opentms-qa
description: Use when testing Open-TMS features as QA Engineer
---

# Open-TMS QA Engineer Skill

## Overview
QA负责测试用例编写、功能测试、缺陷跟踪。使用GitHub Projects管理测试任务。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms

## 核心职责

### 1. 领取测试任务
```bash
# 查看分配给自己的任务
gh issue list --label "QA"

# 开始测试
gh issue edit <number> --body "## 测试状态\nIn Progress\n\n## 测试计划\n1. 功能测试\n2. 接口测试"
```

### 2. 执行测试
```bash
# 功能测试
# - 验证需求功能是否实现
# - 检查边界条件
# - 验证异常处理

# 接口测试
# - 使用Postman或curl测试API
# - 验证参数校验
# - 验证返回格式

# 记录测试结果
gh issue edit <number> --body "## 测试结果
### 功能测试
- [ ] 新增账户成功
- [ ] 编辑账户成功
- [ ] 删除账户成功

### 接口测试
- POST /api/v1/bank-accounts - 201
- GET /api/v1/bank-accounts - 200
- PUT /api/v1/bank-accounts/{id} - 200
- DELETE /api/v1/bank-accounts/{id} - 204

### 测试结论
通过 / 不通过"
```

### 3. 创建Bug
```bash
gh issue create \
  --title "[Bug] 账户余额显示精度问题" \
  --body "## Bug描述

### 环境
- 数据库: PostgreSQL
- 版本: M1-2
- 浏览器: Chrome

### 复现步骤
1. 打开账户管理页面
2. 查看账户余额
3. 余额为100.00显示为100

### 预期
余额应显示为100.00（保留两位小数）

### 实际
余额显示为100（无小数位）

### 严重程度
中 - 影响用户体验

### 修复建议
前端格式化金额函数需处理小数位" \
  --label "QA,Bug"
```

### 4. 测试完成
```bash
# 更新测试结果
gh issue edit <number> --body "## 测试报告

### 测试概况
- 测试用例数: 20
- 通过: 18
- 失败: 2
- 阻塞: 0

### 缺陷列表
1. [Bug] 账户余额精度 - 中
2. [Bug] 新增未校验必填 - 高

### 结论
基本通过，有2个中低优先级问题" 

# 关闭测试任务
gh issue close <number>
```

## 测试用例模板
```markdown
## 功能: 银行账户新增

### 用例1: 正常新增
- 前置: 登录系统，进入账户管理
- 操作: 填写必填项，点击保存
- 预期: 保存成功，列表显示新账户

### 用例2: 必填项校验
- 前置: 登录系统，进入账户管理
- 操作: 不填写账户名称，点击保存
- 预期: 提示"账户名称必填"

### 用例3: 账号唯一性
- 前置: 已存在账号123456
- 操作: 新增账户账号填写123456
- 预期: 提示"账号已存在"
```

## 缺陷报告模板
```markdown
## 缺陷标题
[Bug] 具体问题描述

## 环境
- 版本:
- 浏览器:
- 数据库:

## 复现步骤
1. 步骤1
2. 步骤2
3. 步骤3

## 预期
期望的结果

## 实际
实际的结果

## 严重程度
高/中/低

## 截图
[如有]

## 修复建议
建议的修复方式
```

## 交付检查清单
- [ ] 测试用例是否覆盖核心功能
- [ ] 缺陷是否清晰可复现
- [ ] 测试报告是否完整
- [ ] 缺陷是否跟踪闭环

## 快速参考

| 操作 | 命令 |
|------|------|
| 查看测试任务 | `gh issue list --label "QA"` |
| 创建Bug | `gh issue create --label "QA,Bug"` |
| 更新测试结果 | `gh issue edit <number> --body` |
| 关闭任务 | `gh issue close <number>` |