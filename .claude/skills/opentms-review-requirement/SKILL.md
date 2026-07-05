---
name: opentms-review-requirement
description: |
  Open-TMS 需求审核 Skill。由 Business Architect / PM-Lead 调用,用于审核
  PRD (产品需求文档)、功能规格、用户故事,确保符合 Open-TMS 整体规范、
  五层架构(决策/核心/基础/集成/支撑)、业界对标(FIS Quantum / Murex MX.3
  / SAP TRM / Kyriba),以及与存量 M1 已贯通特性的兼容性。

  Trigger: "需求审核"、"需求评审"、"PRD 审核"、"需求 check"、"需求 review"
---

# opentms-review-requirement

需求审核 — 对 PRD / 功能规格 / 用户故事 进行结构化审核,确保符合
Open-TMS 规范与成熟资金系统对标。

## 输入

- PRD Markdown 文件路径(必填)
- 对应功能模块 / Maven 模块名(必填,如 `basedata`、`dealing`)
- 是否新增 / 修改 / 扩展 既有功能(必填)

## 输出

- `templates/report.md` 填充后的审核报告

## 工作流程

1. 读取 PRD 文件,识别所有业务对象、字段、状态、规则
2. 对照 `checklists/01-field-completeness.md`、`02-state-machine.md`、`03-cross-module.md`
3. 加载 `references/standards.md` 业界对标基准
4. 逐项执行审核,记录发现
5. 输出报告,总评级 A/B/C/D

## 审核项结构化清单

```yaml
checklist:
  # ============= 用户列出的 4 点 =============
  - id: REQ-001
    name: 业务字段完整性
    severity: P0
    standard: 与 FIS Quantum / Murex MX.3 业务对象对比,核心字段缺失即不通过
    check_method: |
      1. 列出 PRD 中所有业务对象
      2. 对比 references/standards.md 中 FIS Quantum/Murex 业务对象清单
      3. 标识缺失字段(P0 字段必须 100% 覆盖)
    pass_criteria: P0 字段全部覆盖;P1 字段缺失需有明确延期说明
    failure_action: 退回 PM 补充

  - id: REQ-002
    name: 符合 Open-TMS 整体规范,扩展性
    severity: P0
    standard: 符合 CLAUDE.md 五层架构 + 18 Maven 模块边界 + GlobalConstants 枚举
    check_method: |
      1. 验证业务对象归属的五层(决策/核心/基础/集成/支撑)
      2. 验证所属 Maven 模块(基于ata/dealing/...)
      3. 验证状态/类型是否使用 GlobalConstants 而非魔术字符串
    pass_criteria: 五层归属明确 + 模块归属清晰 + 0 魔术字符串
    failure_action: 退回 PM 修正架构归属

  - id: REQ-003
    name: 存量特性影响 (基础数据/规则/交易)
    severity: P0
    standard: 不得破坏 M1 已贯通特性(basedata 14 Resource + dealing AC/AT)
    check_method: |
      1. 识别 PRD 中涉及到的存量资源引用(country / currency / bank /
         counterparty / business_unit / instrument / deal / action)
      2. 验证是否新增字段会破坏现有 schema / API / 前端
      3. 验证是否需要数据迁移脚本
    pass_criteria: 影响范围已列出,无破坏性变更或附带迁移脚本
    failure_action: 退回 PM 评估影响范围

  - id: REQ-004
    name: 业界对标 (FIS/Murex), 差异合理性
    severity: P1
    standard: 与 FIS Quantum / Murex MX.3 / SAP TRM / Kyriba 核心能力对标
    check_method: |
      1. 加载 references/standards.md
      2. 对每条业界能力标注 Open-TMS 覆盖情况(覆盖 / 部分 / 缺失)
      3. 缺失能力必须有明确理由(短期不规划 / 复杂度太高)
    pass_criteria: 业界核心能力 ≥ 80% 覆盖;缺失有文档化理由
    failure_action: PM 补充差异说明

  # ============= 业界补充审核项 =============

  - id: REQ-005
    name: 多法律实体/多账套预留
    severity: P0
    standard: 业务对象必须包含 legal_entity_id / book_id 字段(Murex/FIS Quantum 必备)
    check_method: |
      1. grep "legal_entity" / "legalEntity" / "book" / "账套" / "法人"
      2. 若主表无此字段,标 P0 缺失
    pass_criteria: 100% 主业务对象含 legal_entity_id 字段
    failure_action: 退回 PM

  - id: REQ-006
    name: 字段业务含义是否单义
    severity: P0
    standard: FIS Quantum 严格规范 — 一个字段只表达一个业务含义
    check_method: |
      1. 列出 PRD 中所有字段
      2. 识别含混字段(如 status 既表达启用/禁用又表达业务状态)
      3. 字段命名应自解释(无需文档也能猜到含义)
    pass_criteria: 0 字段含义重叠;0 字段需要查阅文档才能理解
    failure_action: 退回 PM 拆分字段

  - id: REQ-007
    name: 业务规则可配置 (阈值/税率不应硬编码)
    severity: P1
    standard: 成熟系统 90%+ 业务规则配置化,无硬编码
    check_method: |
      1. grep PRD 中的数字/百分比/阈值
      2. 识别是否写死(如 "汇率超过 5% 触发审批")
      3. 阈值类必须可配置
    pass_criteria: 业务阈值/税率/限额全部配置化或预留配置点
    failure_action: 退回 PM 抽取配置项

  - id: REQ-008
    name: 状态机完整性
    severity: P0
    standard: Murex 平均每个对象 8-15 个状态,包含初始/中间/终态/异常
    check_method: |
      1. 列出 PRD 中所有业务对象的状态定义
      2. 验证状态流转图(初始 → 中间 → 终态)
      3. 验证异常状态(Rejected/Canceled/Expired/Failed)
      4. 验证不允许的状态组合(如 "审批通过 + 已驳回")
    pass_criteria: 每个业务对象状态机完整,含异常路径
    failure_action: 退回 PM 补充状态机

  - id: REQ-009
    name: 金额/币种字段是否成对
    severity: P0
    standard: 任何金额字段必须有 currency 字段,避免单币种遗留
    check_method: |
      1. grep PRD 中所有 amount/price/qty/balance 字段
      2. 验证每个金额字段都有对应 *_ccy 字段
    pass_criteria: 100% 金额字段配对币种
    failure_action: 退回 PM 补充

  - id: REQ-010
    name: 时区/日期字段语义
    severity: P1
    standard: 必须明确交易日(trade_date)/ 起息日(value_date)/ 交割日(settlement_date)
    check_method: |
      1. grep PRD 中所有 *date / *time 字段
      2. 验证是否有歧义(如 "date" 既表示交易又表示起息)
      3. 验证时区字段(tz / timezone)
    pass_criteria: 日期字段语义清晰,无歧义
    failure_action: 退回 PM 澄清字段语义

  - id: REQ-011
    name: 跨模块引用是否形成循环依赖
    severity: P1
    standard: Open-TMS 严禁模块循环依赖
    check_method: |
      1. 列出 PRD 引用的其他模块
      2. 验证引用方向单向(基于ata ← dealing ← valuation)
      3. 共享类放 common 模块
    pass_criteria: 引用关系有向无环图(DAG)
    failure_action: PM 重新设计模块边界

  - id: REQ-012
    name: 性能边界
    severity: P1
    standard: 大数据量场景必须有性能预估(FIS Quantum 主表 1000 万行)
    check_method: |
      1. 列出大数据量场景(交易明细/现金流/历史)
      2. 验证是否需要分区表 / 归档策略
      3. 验证分页策略
    pass_criteria: 主表性能边界有说明
    failure_action: 退回 PM 补充性能设计

  - id: REQ-013
    name: 合规审计要求
    severity: P0
    standard: 金融监管要求数据保留 ≥ 5 年,审计日志不可篡改
    check_method: |
      1. 验证 PRD 含审计字段(created_by/at/updated_by/at/version/deleted)
      2. 验证是否有 *_log / *_his 历史表
      3. 验证数据保留策略
    pass_criteria: 审计字段齐全 + 历史表设计 + 保留策略
    failure_action: 退回 PM 补充审计设计

  - id: REQ-014
    name: 与现有 Open-TMS 5 份 PRD 术语一致性
    severity: P1
    standard: 全项目统一术语:管理主体(legal_entity)、对手方(counterparty)、币种对(ccy_pair)
    check_method: |
      1. grep Open-TMS 现有 PRD/CLAUDE.md 中的术语
      2. 验证新 PRD 使用一致术语
      3. 验证无别名/同义词混用
    pass_criteria: 0 术语不一致
    failure_action: 退回 PM 统一术语
```

## 评级体系

- **A**: 无 P0/P1 问题
- **B**: 仅 P2 问题(可优化但不阻塞)
- **C**: 有 P1 问题(必须修复后通过)
- **D**: 有 P0 问题(必须返工)

## 调用示例

```
请审核 PRD: docs/prd/m2/fx-deal.md
模块: fx (新建)
调用 skill: opentms-review-requirement
```

## 相关 Skills

- `opentms-product-design` — PRD 编写规范
- `opentms-business-architect` — 业务架构与对标
- `opentms-pm-lead` — 需求管理与立项