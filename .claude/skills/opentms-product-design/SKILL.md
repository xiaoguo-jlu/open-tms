---
name: opentms-product-design
description: Use when designing Open-TMS product requirements and PRD as Product Manager
---

# Open-TMS 产品设计 Skill (PM)

## 简介

本skill用于Open-TMS项目的产品需求设计（PRD），指导产品经理（PM）完成从需求分析到PRD产出的完整流程。

---

## 一、触发条件

当需要进行以下工作时触发：为新模块编写PRD、为已有模块新增功能、用户提出新功能需求、优化已有功能。

触发信号：PM-Lead分配设计任务、用户直接提出功能需求、产品规划中标记为待设计的特性。

---

## 二、输入要求

### 2.1 必须输入

- **功能特性清单**: `open-tms功能特性清单.md` — 业界对标参考，定义功能优先级
- **总体设计规范**: `docs/规范/Open-TMS开发规范文档.md` — 开发规范
- **团队协作规范**: `open-tms团队协作规范.md` — GitHub Projects操作指南
- **模块历史摘要**: `docs/prd/{模块}/SUMMARY.md` — 本模块历史设计记录（若存在）
- **已有相关PRD**: `docs/prd/` — 同模块或相关模块的已有PRD

### 2.2 可选输入

- **用户原始需求**: 用户提供的原始需求描述
- **竞品调研资料**: FIS Quantum/SAP/Murex功能参考
- **技术约束**: TA提供的技术可行性约束

### 2.3 已有 PRD 索引

以下是已完成的 5 份 PRD,新 PRD 应交叉引用并避免冲突:

| PRD | 路径 | 核心内容 | 关键字段 |
|-----|------|----------|----------|
| 银行账户管理 | docs/prd/M1-基础数据/docs/M1-银行账户管理PRD.md | 银行账户CRUD、账户层级、银企直连配置 | bank_account_code, account_no, bank_id, currency, account_type |
| 金融工具定义 | docs/prd/M1-基础数据/docs/M1-金融工具PRD.md | 金融工具产品定义(AC/AT/FX/IRS等) | instrument_code, instrument_type, underlying_ccy_pair |
| AC交易 | docs/prd/M1-基础数据/docs/M1-AC交易PRD.md | 实体现金流交易全流程 | deal_no, buy_amount, sell_amount, value_date, counterparty_id |
| AT交易 | docs/prd/M1-基础数据/docs/M1-AT交易PRD.md | 内部转账交易全流程 | deal_no, from_account_id, to_account_id, transfer_amount |
| 交易对手方 | docs/prd/M1-基础数据/docs/M1-交易对手方PRD.md | 对手方管理、信用评级 | counterparty_code, legal_entity_id, credit_rating |

**交叉引用检查**: 新 PRD 必须对照此表验证是否引用已有实体定义,避免字段语义冲突。

---

## 三、输出规范

### 3.1 交付件输出标准

#### 3.1.1 PRD文档标准

每个PRD必须包含: 模块概述、功能清单（含字段设计和业务流程）、业务规则、验收标准、界面原型（UX待设计）、接口需求。

模板详见 `references/prd-template.md`。

#### 3.1.2 优先级定义

| 优先级 | 定义 | 说明 |
|--------|------|------|
| P0 | 核心功能 | 必须具备，系统可用 |
| P1 | 重要功能 | 版本规划内实现 |
| P2 | 增强功能 | 可后续迭代 |

#### 3.1.3 状态定义

| 状态 | 定义 |
|------|------|
| 草稿 | 正在编写中 |
| 待评审 | 等待团队评审 |
| 已评审 | 评审通过 |
| 已冻结 | 暂停开发 |

### 3.2 存放路径规范

```
docs/prd/
├── M1-基础数据/docs/    # M1模块PRD + SUMMARY.md
├── M2-资金运营/docs/    # M2模块PRD
├── M3-金融工具/docs/    # M3模块PRD
├── M4-风险管理/docs/    # M4模块PRD
├── M5-分析报表/docs/    # M5模块PRD
└── common/docs/          # 公共特性
```

**路径选择**: 总体设计放 `docs/prd/`，公共特性放 `docs/prd/common/docs/`，模块特性放 `docs/prd/{模块}/docs/`。

### 3.3 设计摘要标准

每次完成一组特性PRD后，更新 `docs/prd/{模块}/SUMMARY.md`，记录: 完成的功能、遇到的问题及解决方案、待确认事项。模板格式见原附录B。

---

## 四、执行步骤

### 步骤1：业界洞察

读取 `open-tms功能特性清单.md`，对标FIS Quantum、SAP资金管理、Murex等业界产品，识别目标功能在业界的主流实现方式。

输出: PRD中增加"业界对标"小节。对标模板见 `references/industry-benchmarks.md`。

### 步骤2：读取历史摘要

检查 `docs/prd/{模块}/SUMMARY.md`，了解已完成特性、遇到的问题、已有设计决策。若为新模块则创建新摘要文件。

### 步骤3：PRD撰写

1. 识别对其他特性的前置依赖，如有未完成的前置依赖，中断并建议先完成
2. 根据输出规范模板编写PRD，确保包含所有必要章节
3. 功能描述清晰无歧义，验收标准可量化可测试，字段定义完整
4. 字段命名、类型、审计字段规范 > 详见 `CLAUDE.md` 相关章节。

输出: PRD文件保存到对应路径，命名格式 `{模块}-{功能名}PRD.md`。

### 步骤4：检查总体设计规范

读取 `docs/规范/Open-TMS开发规范文档.md`，对照检查PRD中的命名、字段类型、编码规范、审计字段、幂等设计。

> 详细检查清单（命名规范、字段类型、审计字段、幂等设计）详见 `CLAUDE.md` 的 Key Conventions 与 Required Audit Fields 章节。

检查结果处理:

| 检查结果 | 处理方式 |
|----------|----------|
| 符合规范 | 进入下一步 |
| 不符合规范，且PRD设计不合理 | 修改PRD至符合规范 |
| 不符合规范，但PRD设计合理 | 询问用户是否修改规范 |

### 步骤5：创建GitHub Project工作项

根据PRD内容创建 Feature、US、Task。具体 `gh issue create` 命令格式 > 详见 `opentms-pm-lead` 第九节。

分配规则: Feature(`PM,Feature`) / US(`PM,US`) / 后端开发Task(`Dev,Task`) / 前端开发Task(`UX,Task`) / 测试Task(`QA,Task`)。

### 步骤6：生成设计摘要

更新 `docs/prd/{模块}/SUMMARY.md`，记录本次完成的功能、遇到的问题、待确认事项。

---

## 五、与其他Skill的衔接

### 5.1 前置依赖: （无）— 本skill为设计起点

### 5.2 后续触发: UX交互设计 → 业务架构设计 → 技术方案设计 → 后端/前端代码开发 → 测试用例设计

### 5.3 协作流程

```
PM → UX交互设计 / 技术方案设计 → 后端/前端开发 → 测试用例设计 → 测试执行
```

---

## 六、质量标准

### 6.1 PRD质量检查点

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 功能完整性 | 覆盖用户需求的100% | 20% |
| 描述清晰度 | 无歧义、易理解 | 20% |
| 验收标准明确性 | 可量化、可测试 | 20% |
| 字段定义完整性 | 必填、类型、说明齐全 | 15% |
| 业务流程正确性 | 符合业务逻辑 | 15% |
| 规范符合性 | 符合总体设计规范 | 10% |

### 6.2 量化指标

| 指标 | 目标值 | 最低值 |
|------|--------|--------|
| 验收标准覆盖率 | 100% | 90% |
| 字段定义完整率 | 100% | 95% |
| 规范符合率 | 100% | 95% |
| 描述歧义数量 | 0 | <=2 |

### 6.3 评审通过标准

- [ ] 所有P0功能已包含在PRD中
- [ ] 验收标准可量化、可测试
- [ ] 无重大描述歧义
- [ ] 符合总体设计规范
- [ ] 已创建对应的GitHub工作项

---

## 七、交付物检查清单

### 7.1 PRD文件

- [ ] 文件命名符合规范
- [ ] 存放路径正确
- [ ] 包含所有必要章节
- [ ] 优先级标注清晰、验收标准明确、字段定义完整

### 7.2 设计摘要

- [ ] 已更新SUMMARY.md（含完成内容、问题、待确认事项）

### 7.3 GitHub工作项

- [ ] Feature/US/Task已创建，Label正确分配

### 7.4 规范检查

- [ ] 命名、字段类型、审计字段、幂等设计符合规范 > 详见 `CLAUDE.md`

---

## 八、附录

- **附录A - PRD 模板**: 见 `references/prd-template.md`
- **附录B - SUMMARY 模板**: 见原附录B（设计摘要模板，格式参见步骤6输出说明）
- **附录C - 功能特性清单参考**: 六大模块定位见 `references/industry-benchmarks.md`
- **业界对标模板**: 见 `references/industry-benchmarks.md`

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |
