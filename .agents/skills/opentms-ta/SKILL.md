---
name: opentms-ta
description: Use when designing Open-TMS technical architecture and database schema as Technical Architect
---

# Open-TMS Technical Architect Skill

## Overview
TA负责技术选型、架构设计、关键技术方案、技术标准制定。使用GitHub Projects管理技术设计任务。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms
- **当前模块**: basedata, bankaccount (M1-2)

## 核心职责

### 1. 数据库设计
```bash
# 创建表设计任务
gh issue create \
  --title "[TA] tms_bank_account_t表设计" \
  --body "## 设计目标
设计银行账户表，用于存储企业银行账户信息。

## 表结构

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | UUID | 主键 | 自生成 |
| account_name | VARCHAR(100) | 账户名称 | 必填 |
| account_no | VARCHAR(50) | 账号 | 唯一 |
| bank_id | UUID | 所属银行 | 外键 |
| currency_code | CHAR(3) | 币种 | 外键 |
| balance | DECIMAL(18,2) | 余额 | |
| status | VARCHAR(20) | 状态 | ACTIVE/INACTIVE |

## 设计决策
1. 使用UUID作为主键，支持分布式场景
2. 账户号唯一性约束
3. 逻辑删除字段is_deleted

## 评审问题
- [ ] 是否需要支持多账号体系？
- [ ] 余额字段精度？(当前2位)

## 依赖
- tms_bank_t (银行表)
- tms_currency_t (币种表)" \
  --label "TA,Task"
```

### 2. 技术方案设计
```bash
# 创建技术方案任务
gh issue create \
  --title "[TA] 账户余额扣减方案" \
  --body "## 问题背景
AT交易需要校验账户余额，需要明确扣减机制。

## 方案对比

### 方案A：实时扣减
- 优点：余额实时准确
- 缺点：高并发下可能存在锁竞争

### 方案B：预占机制
- 优点：支持高并发
- 缺点：实现复杂，需要处理预占超时

## 建议
采用方案A（实时扣减），M1版本交易量不大的情况下够用。

## 待确认
- [ ] 扣减时机：创建时还是执行时？
- [ ] 余额不足如何处理？"
  --label "TA,Task"
```

### 3. 架构评审
```bash
# 查看分配给自己的任务
gh issue list --label "TA"

# 更新设计
gh issue edit <number> --body "## 最新设计..."

# 标记完成
gh issue close <number>
```

## 技术规范

### Java代码规范
- 使用Lombok简化代码
- Service层接口+实现类分离
- 使用MyBatis Plus的QueryWrapper

### 数据库规范
- 表名: tms_{module}_t
- 主键: UUID (Java用String)
- 时间: TIMESTAMP带时区
- 软删除: is_deleted字段

### API规范
- RESTful风格
- 返回统一Result结构
- 分页用Page类

## 交付检查清单
- [ ] 技术方案是否可行
- [ ] 设计文档是否完整
- [ ] 是否与Dev/PM确认
- [ ] 是否有评审记录

## 快速参考

| 操作 | 命令 |
|------|------|
| 创建设计任务 | `gh issue create --label "TA,Task"` |
| 查看设计任务 | `gh issue list --label "TA"` |
| 更新设计 | `gh issue edit <number> --body` |
| 关闭任务 | `gh issue close <number>` |

## 表设计模板
```markdown
## 表名
tms_{module}_t

## 说明
[表用途]

## 字段

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | UUID | 主键 | PK |
| name | VARCHAR(100) | 名称 | NOT NULL |
| code | VARCHAR(50) | 代码 | UNIQUE |

## 索引
- idx_code: code字段

## 关联
- 外键: bank_id → tms_bank_t(id)

## 评审问题
- [ ] 问题1
- [ ] 问题2
```