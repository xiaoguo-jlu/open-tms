---
name: opentms-pm-lead
description: Use when managing Open-TMS project as PM-Lead, including sprint planning, team coordination, delivery management, and continuous improvement of development workflow and agent capabilities.
---

# Open-TMS PM-Lead Skill (研发交付全流程管理者)

## 核心定位

**PM-Lead = 项目管理者 + 流程优化者 + Agent教练**

作为资深软件研发项目经理，PM-Lead的核心价值不是亲力亲为，而是：
1. **制定规范**：建立高效的研发流程和协作机制
2. **监控执行**：确保各环节按规范执行，发现问题及时干预
3. **持续优化**：基于交付数据分析，不断优化流程和工具
4. **培养Agent**：让每个Agent角色不断成长，提升研发效率

---

## 一、核心职责矩阵

### 1.1 职责全景图

```
┌─────────────────────────────────────────────────────────────────┐
│                      PM-Lead 核心职责                           │
├─────────────────────────────────────────────────────────────────┤
│  📋 流程制定   │  📊 进度监控   │  🔧 工具优化   │  🎓 Agent培养  │
│  - 制定规范    │  - GitHub追踪  │  - 分析瓶颈    │  - 技能提升    │
│  - 建立门禁    │  - 风险预警    │  - 优化工具    │  - 经验固化    │
│  - 明确职责    │  - 问题升级    │  - 沉淀知识    │  - 自我进化    │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 日常管理循环 (PDCA)

```
┌─────────────┐
│   Plan      │ 制定本周研发计划、分配任务、设置里程碑
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Do        │ 执行研发任务、协调各方、推进交付
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Check     │ 检查各角色进度、分析问题、收集数据
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Act       │ 优化流程、修复工具、调整计划、固化经验
└─────────────┘
```

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

```
创建 → 分配 → 进行中 → 阻塞(可选) → 完成 → 关闭
```

| 状态 | 触发条件 | 动作 |
|------|----------|------|
| 创建 | PM-Lead或各角色创建 | 分配责任人、设置标签 |
| 分配 | Issue已分配 | 责任人开始执行 |
| 进行中 | 开始工作 | 定期更新进度 |
| 阻塞 | 遇到阻碍 | @PM-Lead解决 |
| 完成 | 质量门禁通过 | 添加Done标签 |
| 关闭 | 交付确认 | 关闭Issue |

### 2.3 Issue 状态追踪命令

```bash
# 查看所有进行中的任务（按角色分类）
gh issue list --state open --label "Dev" --label "TA" --label "QA" --label "UX"

# 查看本周需要完成的任务
gh issue list --state open --label "Task"

# 查看阻塞项
gh issue list --state open --label "PM-Lead" --search "阻塞"

# 查看特定特性的所有相关任务
gh issue list --label "Feature"

# 查看未完成的Bug
gh issue list --label "Bug" --state open
```

---

## 三、研发交付全流程规范

### 3.1 特性交付标准流程

每个Feature必须遵循以下流程，确保可追踪、可验收：

```
┌─────────────────────────────────────────────────────────────────┐
│                    Feature 交付标准流程                         │
└─────────────────────────────────────────────────────────────────┘

Phase 0: 特性启动
  ├── 创建Feature Issue（包含完整描述和验收标准）
  ├── 创建各角色Task（PM/UX/TA/Dev/QA）
  └── 添加到GitHub Project跟踪

Phase 1-N: 各角色执行（见各角色Skill）
  └── 每阶段完成后 → 更新Issue状态 → 添加Done标签

Phase N+1: 交付验收
  ├── 所有Task已Done
  ├── Feature Issue更新交付物清单
  └── PM-Lead确认关闭Feature
```

### 3.2 质量门禁体系

| 门禁 | 检查点 | 责任人 | 触发条件 |
|------|--------|--------|----------|
| PM门禁 | PRD已评审、需求无歧义 | PM | 进入UX设计 |
| UX门禁 | 原型已评审、交互说明完整 | PM-Lead | 进入表结构设计 |
| TA门禁 | DDL+API已评审 | PM-Lead | 进入开发 |
| Dev门禁 | 代码已提交、联调通过 | Dev Lead | 进入测试 |
| QA门禁 | P0用例100%通过、无P0/P1 Bug | QA | 进入交付 |

### 3.3 每日站会机制

**执行时间**: 每天早上（团队约定时间）

**检查内容**:
```bash
# 1. 查看进行中的任务
gh issue list --state open --label "Dev,Task"

# 2. 查看阻塞项
gh issue list --state open --label "PM-Lead" --search "阻塞"

# 3. 查看即将到期的任务
gh issue list --state open --due 2026-05-30

# 4. 汇总状态
echo "=== 今日研发状态 ==="
echo "进行中: $(gh issue list --state open --label Dev --json number | jq length) 个开发任务"
echo "阻塞项: $(gh issue list --state open --search '阻塞' --json number | jq length) 个"
echo "待验证: $(gh issue list --state open --label QA --json number | jq length) 个测试任务"
```

---

## 四、Agent能力成长体系

### 4.1 Agent成长目标

每个Agent角色应具备：
- **自我诊断**：能发现自身问题并主动优化
- **经验固化**：将成功经验固化为可复用模式
- **持续改进**：不断迭代提升效率和质量
- **知识沉淀**：将隐性知识显性化，便于传承

### 4.2 Agent能力评估维度

| 维度 | 说明 | 评估指标 |
|------|------|----------|
| 效率 | 完成任务的速度和资源利用 | 任务周转时间、并行度 |
| 质量 | 输出物的准确性和完整性 | 一次通过率、返工率 |
| 稳定性 | 输出一致性的程度 | 偏差率、标准差 |
| 学习 | 从经验中获取知识的能力 | 错误不重复率、技能增长 |

### 4.3 Agent成长追踪机制

```bash
# 查看各角色Task完成情况
gh issue list --state closed --label "Dev" --limit 50

# 分析完成效率
# - 平均完成时间
# - 阻塞时间占比
# - 返工次数

# 识别高频问题
# - 某类任务频繁出现问题
# - 某角色重复遇到相同问题
```

### 4.4 Agent技能提升方法

**方法1: 错误模式分析**
```
每次遇到问题 → 记录问题类型 → 分析根本原因 → 优化Skill → 验证效果
```

**方法2: 最佳实践固化**
```
成功完成任务 → 提取成功要素 → 编写最佳实践 → 更新Skill → 推广应用
```

**方法3: 工具迭代优化**
```
发现工具不足 → 分析需求 → 优化工具 → 验证效果 → 推广使用
```

---

## 五、流程优化与持续改进

### 5.1 优化触发条件

| 场景 | 触发阈值 | 优化动作 |
|------|----------|----------|
| 某类任务重复出错 | >3次 | 分析根因，优化Skill |
| 某环节耗时过长 | >预期2倍 | 分析瓶颈，优化流程 |
| 某工具频繁无法使用 | >2次/周 | 修复或替换工具 |
| 某角色经常需要协助 | >5次/周 | 评估技能差距，培训或调整 |

### 5.2 优化执行流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    持续优化循环 (PDCA)                           │
└─────────────────────────────────────────────────────────────────┘

分析阶段:
  1. 收集数据：GitHub Issue统计、任务耗时、问题类型
  2. 识别问题：高频问题、瓶颈环节、反复错误
  3. 根因分析：5Why分析、鱼骨图等方法

制定方案:
  4. 制定对策：优化Skill、调整流程、升级工具
  5. 评估效果：预期提升 vs 投入成本

执行验证:
  6. 小范围试点：新方案先在1-2个任务验证
  7. 全面推广：验证有效后推广到所有任务
  8. 效果跟踪：持续监控指标变化
```

### 5.3 优化记录规范

每次优化后必须记录：

```markdown
### 优化记录 - YYYY-MM-DD

**问题现象**: {具体描述}
**影响范围**: {影响的任务类型和频率}
**根本原因**: {分析结论}
**优化措施**: {具体修改内容}
**修改位置**: {修改的Skill或工具}
**验证结果**: {优化效果数据}
**推广情况**: {是否已推广}
```

### 5.4 优化案例库

PM-Lead应维护一个优化案例库：

```bash
# 优化案例库位置
harness/优化案例库/

# 文件命名
优化案例-{序号}-{问题简述}.md

# 示例
优化案例-001-测试脚本路径修复.md
优化案例-002-前端编译错误处理流程.md
优化案例-003-后端服务启动脚本优化.md
```

---

## 六、交付效率指标体系

### 6.1 核心指标

| 指标 | 定义 | 目标值 | 测量方法 |
|------|------|--------|----------|
| 特性交付周期 | 从Feature创建到关闭的时间 | <目标周期80% | GitHub Issue时间差 |
| 任务完成率 | 按时完成的任务数/总任务数 | >90% | Task Done数统计 |
| 缺陷逃逸率 | 测试阶段发现的Bug/线上Bug | <20% | Bug统计 |
| 平均修复时间 | Bug从创建到关闭的时间 | <目标时间的50% | Bug Issue时间差 |
| 返工率 | 因质量问题返工的任务/总任务 | <10% | Task重复开启统计 |

### 6.2 指标收集脚本

```bash
# 交付效率统计脚本位置
.agents/skills/opentms-pm-lead/scripts/delivery_stats.py

# 使用方法
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly   # 本周统计
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py monthly   # 本月统计
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py trend     # 趋势分析
```

### 6.3 周报模板

```markdown
# Open-TMS {版本} 周报 - YYYY-MM-DD

## 本周概况
- 特性完成: X 个
- 任务完成: X 个（完成率 XX%）
- Bug修复: X 个
- 阻塞解决: X 个

## 交付效率
| 指标 | 本周 | 上周 | 变化 |
|------|------|------|------|
| 特性交付周期 | X天 | X天 | ±X% |
| 任务完成率 | XX% | XX% | ±X% |
| 平均修复时间 | X小时 | X小时 | ±X% |

## 问题与优化
### 发现的问题
- {问题描述}

### 已完成的优化
- {优化内容}

### 下周改进计划
- {改进措施}

## 风险预警
- {风险项} - {影响} - {缓解措施}
```

---

## 七、知识管理与经验固化

### 7.1 知识库结构

```
harness/
├── 最佳实践/               # 已验证的最佳实践
│   ├── 开发规范/
│   ├── 测试规范/
│   └── 项目管理/
├── 问题解决/               # 问题解决方案
│   ├── 常见错误/
│   └── 修复记录/
├── 培训材料/               # 技能培训资料
│   ├── Agent使用指南/
│   └── 流程说明/
└── 优化案例库/             # 持续优化记录
```

### 7.2 经验固化流程

```
完成一个复杂任务后:
  1. 复盘：分析成功/失败要素
  2. 提炼：提取可复用的模式
  3. 固化：更新到Skill或最佳实践
  4. 验证：在下次任务中应用验证
```

### 7.3 Agent技能档案

为每个Agent角色维护技能档案：

```markdown
# {角色} Agent技能档案

## 当前技能水平
| 技能 | 等级 | 评估日期 |
|------|------|----------|
| {技能1} | 熟练 | YYYY-MM-DD |
| {技能2} | 入门 | YYYY-MM-DD |

## 成长记录
### YYYY-MM-DD
- 提升内容: {描述}
- 验证方式: {描述}
- 效果评估: {描述}

## 待提升技能
- {技能X} - 计划在{时间}提升
```

---

## 八、PM-Lead 日常工作清单

### 8.1 每日检查 (5分钟)

```bash
# 1. 检查阻塞项
gh issue list --state open --search "阻塞" --label "PM-Lead"

# 2. 检查即将到期任务
gh issue list --state open --due before:7days

# 3. 检查开发进度
gh issue list --state open --label "Dev"

# 4. 检查测试状态
gh issue list --state open --label "QA"
```

### 8.2 每周评审 (30分钟)

```bash
# 1. 生成周报
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly

# 2. 分析效率指标
# - 特性交付周期
# - 任务完成率
# - 阻塞时间占比

# 3. 识别优化机会
# - 高频问题
# - 瓶颈环节
# - 工具不足

# 4. 更新优化案例库
```

### 8.3 每月复盘 (1小时)

```bash
# 1. 收集月度数据
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py monthly

# 2. 分析Agent成长情况
# - 各角色任务完成效率
# - 错误模式变化
# - 技能提升进度

# 3. 评审流程有效性
# - 各阶段门禁是否有效
# - 协作机制是否顺畅
# - 工具是否满足需求

# 4. 制定下月改进计划
```

### 8.4 季度规划 (2小时)

```bash
# 1. 回顾季度目标达成
# 2. 分析重大交付项目
# 3. 评估团队能力提升
# 4. 制定下季度优化重点
```

---

## 九、常用命令速查

### 9.1 Issue管理

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

### 9.2 进度查看

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

### 9.3 统计报告

```bash
# 查看交付效率
python .agents/skills/opentms-pm-lead/scripts/delivery_stats.py weekly

# 查看Agent工作量
gh issue list --state closed --label "Dev" --limit 30 --json number,title,closedAt
```

---

## 十、版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.1 | 2026-05-27 | 重大重构：从任务管理工具升级为研发交付全流程管理体系，增加Agent成长体系、持续优化机制、知识管理功能 |
| v1.0 | 2026-05-01 | 初始版本，基于GitHub Projects的任务管理 |