---
name: opentms-pm-lead
description: Use when managing Open-TMS project as PM-Lead, including sprint planning, team coordination, delivery management, and continuous improvement of development workflow and agent capabilities.
---

# Open-TMS PM-Lead Skill (研发交付全流程管理者)

## 核心定位

PM-Lead = 项目管理者 + 流程优化者 + Agent教练

核心价值: 制定规范 → 监控执行 → 持续优化 → 培养Agent

---

## 一、核心职责矩阵

```
PM-Lead 核心职责:
  - 流程制定: 制定规范、建立门禁、明确职责
  - 进度监控: GitHub追踪、风险预警、问题升级
  - 工具优化: 分析瓶颈、优化工具、沉淀知识
  - Agent培养: 技能提升、经验固化、自我进化
```

日常管理循环 (PDCA): Plan(制定计划) → Do(执行推进) → Check(检查分析) → Act(优化固化)

---

## 二、GitHub Projects 团队协作规范

### 2.1 Issue 标签体系

| 标签 | 含义 | 使用场景 |
|------|------|----------|
| `PM-Lead` | PM-Lead专属 | 战略决策、跨团队协调、流程制定 |
| `PM` | 产品经理 | 需求分析、PRD设计、用户故事 |
| `TA` | 技术架构师 | 技术方案、表结构、API设计 |
| `Dev` | 开发工程师 | 后端/前端代码开发 |
| `QA` | 测试工程师 | 测试用例设计、执行、缺陷跟踪 |
| `UX` | UX设计师 | 界面原型、交互设计 |
| `Feature` | 特性级任务 | 跨角色完整特性开发 |
| `US` | 用户故事 | 需求拆分单元 |
| `Task` | 原子任务 | 具体可执行的任务 |
| `Bug` | 缺陷 | 测试发现的bug |
| `技术债务` | 技术改进 | 代码重构、架构优化 |

### 2.2 Issue 生命周期

创建 → 分配 → 进行中 → 阻塞(可选) → 完成 → 关闭

| 状态 | 触发条件 | 动作 |
|------|----------|------|
| 创建 | PM-Lead或各角色创建 | 分配责任人、设置标签 |
| 进行中 | 开始工作 | 定期更新进度 |
| 阻塞 | 遇到阻碍 | @PM-Lead解决 |
| 完成 | 质量门禁通过 | 添加Done标签 |
| 关闭 | 交付确认 | 关闭Issue |

### 2.3 Issue 状态追踪

```bash
# 查看所有进行中的任务
gh issue list --state open --label "Dev" --label "TA" --label "QA" --label "UX"

# 查看本周任务
gh issue list --state open --label "Task"

# 查看阻塞项
gh issue list --state open --label "PM-Lead" --search "阻塞"

# 查看未完成的Bug
gh issue list --label "Bug" --state open
```

---

## 三、研发交付全流程规范

### 3.1 特性交付标准流程

```
Phase 0: 特性启动
  - 创建Feature Issue（完整描述+验收标准）
  - 创建各角色Task（PM/UX/TA/Dev/QA）
  - 添加到GitHub Project跟踪

Phase 1-N: 各角色执行（见各角色Skill）
  - 每阶段完成后更新Issue状态

Phase N+1: 交付验收
  - 所有Task已Done
  - Feature Issue更新交付物清单
  - PM-Lead确认关闭Feature
```

### 3.2 质量门禁体系

| 门禁 | 检查点 | 责任人 | 触发条件 |
|------|--------|--------|----------|
| PM门禁 | PRD已评审、需求无歧义 | PM | 进入UX设计 |
| UX门禁 | 原型已评审、交互说明完整 | PM-Lead | 进入表结构设计 |
| TA门禁 | DDL+API已评审 | PM-Lead | 进入开发 |
| Dev门禁 | 代码已提交、联调通过 | Dev Lead | 进入测试 |
| QA门禁 | P0用例100%通过、无P0/P1 Bug | QA | 进入交付 |

### 3.3 每日站会检查

```bash
# 进行中的任务
gh issue list --state open --label "Dev,Task"

# 阻塞项
gh issue list --state open --label "PM-Lead" --search "阻塞"

# 即将到期任务
gh issue list --state open --due 2026-05-30
```

---

## 四、Agent能力成长体系

### 4.1 Agent成长目标

每个Agent角色应具备: 自我诊断（发现问题主动优化）、经验固化（成功经验可复用）、持续改进（迭代提升效率）、知识沉淀（隐性知识显性化）。

### 4.2 Agent能力评估维度

| 维度 | 说明 | 评估指标 |
|------|------|----------|
| 效率 | 完成任务的速度和资源利用 | 任务周转时间、并行度 |
| 质量 | 输出物的准确性和完整性 | 一次通过率、返工率 |
| 稳定性 | 输出一致性的程度 | 偏差率、标准差 |
| 学习 | 从经验中获取知识的能力 | 错误不重复率、技能增长 |

### 4.3 Agent技能提升方法

- **错误模式分析**: 遇到问题 → 记录类型 → 分析根因 → 优化Skill → 验证效果
- **最佳实践固化**: 成功任务 → 提取要素 → 编写实践 → 更新Skill → 推广
- **工具迭代优化**: 发现不足 → 分析需求 → 优化工具 → 验证效果 → 推广

---

## 五、流程优化与持续改进

### 5.1 优化触发条件

| 场景 | 触发阈值 | 优化动作 |
|------|----------|----------|
| 某类任务重复出错 | >3次 | 分析根因，优化Skill |
| 某环节耗时过长 | >预期2倍 | 分析瓶颈，优化流程 |
| 某工具频繁无法使用 | >2次/周 | 修复或替换工具 |
| 某角色经常需要协助 | >5次/周 | 评估技能差距，培训或调整 |

### 5.2 优化执行流程 (PDCA)

分析阶段: 收集数据 → 识别问题 → 根因分析 → 制定对策 → 评估效果 → 小范围试点 → 全面推广 → 效果跟踪

### 5.3 优化记录规范

每次优化后记录: 问题现象、影响范围、根本原因、优化措施、修改位置、验证结果、推广情况。

---

## 六、交付效率指标体系

### 6.1 核心指标

| 指标 | 定义 | 目标值 |
|------|------|--------|
| 特性交付周期 | Feature创建到关闭的时间 | <目标周期80% |
| 任务完成率 | 按时完成的任务数/总任务数 | >90% |
| 缺陷逃逸率 | 测试发现的Bug/线上Bug | <20% |
| 平均修复时间 | Bug创建到关闭的时间 | <目标时间50% |
| 返工率 | 因质量返工的任务/总任务 | <10% |

### 6.2 指标收集

```bash
# 交付效率统计
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py monthly
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py trend
```

### 6.3 周报模板

```markdown
# Open-TMS {版本} 周报 - YYYY-MM-DD

## 本周概况
- 特性完成/任务完成/Bug修复/阻塞解决

## 交付效率
| 指标 | 本周 | 上周 | 变化 |

## 问题与优化
- 发现问题 / 已完成优化 / 下周改进计划

## 风险预警
- {风险项} - {影响} - {缓解措施}
```

---

## 七、知识管理与经验固化

### 7.1 知识库结构

```
harness/
├── 最佳实践/     # 已验证的最佳实践
├── 问题解决/     # 问题解决方案
├── 培训材料/     # 技能培训资料
└── 优化案例库/   # 持续优化记录
```

### 7.2 经验固化流程

完成复杂任务后: 复盘 → 提炼 → 固化到Skill → 下次验证

---

## 八、PM-Lead 日常工作清单

- **每日(5分钟)**: 检查阻塞项、即将到期任务、开发进度、测试状态
- **每周(30分钟)**: 生成周报、分析效率指标、识别优化机会、更新优化案例库
- **每月(1小时)**: 收集月度数据、分析Agent成长、评审流程有效性、制定改进计划
- **每季度(2小时)**: 回顾目标达成、分析重大交付、评估团队能力、制定优化重点

---

## 九、常用命令速查

> 完整命令参考见 `references/commands.md`。

核心命令:

```bash
# 创建任务/特性/Bug
gh issue create --title "[Dev] {任务名}" --body "..." --label "Dev,Task"
gh issue create --title "[Feature] {特性名}" --body "..." --label "PM,Feature"

# 进度查看
gh issue list --state open
gh issue list --label "Dev"
gh issue list --search "阻塞"

# 统计
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly
```

---

## 十、版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.1 | 2026-05-27 | 重大重构：从任务管理工具升级为研发交付全流程管理体系，增加Agent成长体系、持续优化机制、知识管理功能 |
| v1.0 | 2026-05-01 | 初始版本，基于GitHub Projects的任务管理 |
