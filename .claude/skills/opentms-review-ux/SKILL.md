---
name: opentms-review-ux
description: |
  Open-TMS UX 审核 Skill。由 UX Reviewer / Frontend Lead 调用,用于审核
  Vue 3 页面 / 组件的 UI/UX 实现,确保符合 Open-TMS 既有规范
  (BaseDataPicker / ModeBadge / FormContainer)、FIS Quantum / Murex MX.3
  / Bloomberg AIM 企业 UX 标准,以及 Element Plus 最佳实践。

  Trigger: "UX 审核"、"UX 评审"、"页面审核"、"UX review"、"前端交互审核"
---

# opentms-review-ux

UX 审核 — 对 Vue 3 页面 / 组件 进行结构化 UX 审核,确保符合
Open-TMS 既有页面规范与成熟资金管理系统 (FIS Quantum / Murex MX.3) UX 标准。

> **本 skill 遵循** `opentms-review-common` 公共规范 — 统一评级体系、报告格式、调用方式、归档路径。

---

## 输入

- 待审核的 `.vue` 文件路径(必填,可多个)
- 所属模块名(必填,如 `dealing` / `basedata`)
- 关联的 UX 设计稿路径(可选)
- 是否新增 / 修改 / 重构(必填)

## 输出

- 审核报告: `docs/reviews/{feature-name}/ux-review.md`
- 按 `templates/report.md` 填充

## 工作流程

1. **加载公共规范** — 读取 `opentms-review-common/SKILL.md`
2. **读取 Vue 文件** — 用 `Read` 工具读取页面 / 组件源码
3. **静态检查** — 用 `Grep` 搜索关键模式(console.error / 公共组件 / 状态色 / 联动)
4. **对标检查** — 对比 Open-TMS 既有页面(AcDealList / FxDealList / CountryList)
5. **加载 checklist** — 按 `checklists/01-页面审核清单.md` 逐项打勾
6. **逐项审核** — 按下方 YAML checklist 逐项判定 PASS/FAIL
7. **输出报告** — 评级 A/B/C/D + P0/P1/P2 问题清单 + 整改建议

---

## 审核项结构化清单 (YAML 数组)

```yaml
ux_review_items:

  # ============= 用户列出的 2 点 =============

  - id: UX-001
    name: 操作步骤 ≤5 步,简单高效
    severity: P0
    standard: 核心录入流程(新建 / 编辑 / 提交 / 审批 / 执行)≤5 步完成
    check_method: |
      1. 读取 *.vue 文件,识别 el-dialog / router.push 调用;
      2. 模拟从「点击新建」到「成功保存」的完整路径;
      3. 统计用户操作步数(输入 + 点击 + 确认)。
    pass_criteria: 核心流程 ≤5 步
    failure_action: 退回前端重构

  - id: UX-002
    name: 页面风格与 Open-TMS 其他相似特性一致
    severity: P0
    standard: 与已有合并页面(AcDealList / FxDealList / CountryList)视觉 / 交互 / 组件一致
    check_method: |
      1. Grep 同目录下 *.vue,对比 el-table 列定义 / el-form schema / 操作列;
      2. 对比 BaseDataPicker / ModeBadge / FormContainer 使用方式;
      3. 校验 el-tag status type 统一(已约定映射)。
    pass_criteria: 与最近合并的同类型页面差异点 ≤3 处
    failure_action: 退回前端对齐

  # ============= 业界补充审核项 (FIS Quantum / Murex MX.3 / Bloomberg AIM) =============

  - id: UX-003
    name: 1080p 一屏可见核心信息
    severity: P1
    standard: FIS Quantum 标准 — 1920×1080 下首屏可见关键信息
    check_method: Read *.vue,识别 TopBar / 关键信息卡高度;模拟 1080p 渲染。
    pass_criteria: 关键信息首屏可见率 100%
    failure_action: 优化布局

  - id: UX-004
    name: 列表页四件套 (搜索 + 工具栏 + 表格 + 分页)
    severity: P1
    standard: Open-TMS CLAUDE.md 「列表页四件套」强制要求
    check_method: Read *.vue,识别 搜索区 / 工具栏 / el-table / el-pagination 四区块。
    pass_criteria: 四件套齐全;缺一即扣分
    failure_action: 补充缺失区块

  - id: UX-005
    name: 详情页布局统一 (TopBar + 关键信息 + 主信息 + Tabs)
    severity: P1
    standard: Open-TMS 既有详情页约定(已合并)
    check_method: Read *.vue,识别四大区块。
    pass_criteria: 四大区块齐全
    failure_action: 按布局重构

  - id: UX-006
    name: 模式徽章可见 (新建/复制/编辑中)
    severity: P1
    standard: 使用 ModeBadge 标识当前模式(create/copy/edit/view)
    check_method: |
      Grep `ModeBadge|mode-badge` 在 *.vue 中的使用;
      检查 import 来源(`@/components/common/ModeBadge`)。
    pass_criteria: ModeBadge 必现且 mode 值正确
    failure_action: 补充 ModeBadge

  - id: UX-007
    name: 状态色统一 (Open-TMS 约定)
    severity: P1
    standard: New=info / Active=success / Canceled=danger / Rejected=danger / Draft=warning
    check_method: |
      Grep `:type="getStatusType` 检查实现;
      校验与 CLAUDE.md 「状态展示」一致。
    pass_criteria: 100% 符合映射表
    failure_action: 修正状态映射

  - id: UX-008
    name: 错误提示用 ElMessage 而非 console.error
    severity: P0
    standard: Element Plus 最佳实践 / FIS Quantum 错误反馈规范
    check_method: |
      Grep `console.error|console.warn|console.log` 在 *.vue 中的使用;
      校验 catch 块是否调用 ElMessage。
    pass_criteria: catch 块 100% 使用 ElMessage;无 console.* 残留
    failure_action: 替换为 ElMessage

  - id: UX-009
    name: 公共组件复用 (BaseDataPicker / ModeBadge / FormContainer)
    severity: P1
    standard: Open-TMS 现有公共组件库强制复用
    check_method: |
      Grep `BaseDataPicker|ModeBadge|FormContainer|ActionApprovalDialog` import;
      若自行实现,需说明原因。
    pass_criteria: 同类组件复用率 100%
    failure_action: 替换为公共组件

  - id: UX-010
    name: 按钮位置一致 (列表/详情/弹窗/审批)
    severity: P1
    standard: Open-TMS 既有页面约定
    check_method: Read *.vue,对比同模块其他页面按钮顺序。
    pass_criteria: 关键操作按钮位置 100% 一致
    failure_action: 调整按钮顺序

  - id: UX-011
    name: 录入联动 (币种对切换自动填币种)
    severity: P1
    standard: Murex MX.3 录入联动最佳实践
    check_method: Read *.vue,识别 @change 监听器。
    pass_criteria: 至少 3 处典型联动场景实现
    failure_action: 补充联动逻辑

  - id: UX-012
    name: 复制/新建/编辑 区分清晰
    severity: P2
    standard: Murex MX.3 表单模式
    check_method: Read *.vue,识别 copy 模式入口;校验 ModeBadge + Title + 默认值。
    pass_criteria: 三种模式入口独立可见
    failure_action: 增强模式区分

  - id: UX-013
    name: 操作反馈 (loading, success, error)
    severity: P1
    standard: Element Plus 最佳实践
    check_method: Read *.vue,校验 :loading 绑定与 catch/then 反馈。
    pass_criteria: 所有写操作 100% 有反馈
    failure_action: 补充反馈

  - id: UX-014
    name: 列表密度 (企业级高密度表格)
    severity: P0
    standard: FIS Quantum 标准 / Bloomberg AIM 表格密度
    check_method: Read *.vue,校验 el-table size='small';1080p 计算可见行数。
    pass_criteria: 1080p 可见行数 ≥10
    failure_action: 调整表格密度

  - id: UX-015
    name: 搜索/筛选/排序 完整
    severity: P1
    standard: FIS Quantum 列表搜索规范
    check_method: |
      Grep `el-input|el-select|el-date-picker|sortable` 在列表页;
      校验后端 API 支持对应参数。
    pass_criteria: ≥3 个筛选字段 + ≥1 个可排序列
    failure_action: 补充筛选/排序

  - id: UX-016
    name: 批量操作 (列表多选 + 批量审批)
    severity: P1
    standard: Open-TMS 既有批量操作页面
    check_method: |
      Grep `el-table` 中 :selection / @selection-change;
      Grep 批量 API 调用。
    pass_criteria: 至少支持批量审批或批量删除一项
    failure_action: 补充批量操作

  - id: UX-017
    name: 状态设计 (空/错误/加载)
    severity: P1
    standard: Element Plus 设计规范
    check_method: Read *.vue,识别三种状态实现。
    pass_criteria: 三种状态都有处理
    failure_action: 补充缺失状态

  - id: UX-018
    name: 快捷键 (Ctrl+F5 / Enter / Esc)
    severity: P2
    standard: Bloomberg AIM 键盘操作习惯
    check_method: Grep `@keyup|keydown` 在 *.vue 中的使用。
    pass_criteria: 至少 2 个全局快捷键
    failure_action: 补充快捷键

  - id: UX-019
    name: Tab 切换不丢数据
    severity: P2
    standard: Element Plus 表单最佳实践
    check_method: Read *.vue,识别 v-model 与 keep-alive 使用。
    pass_criteria: Tab 切换不丢数据
    failure_action: 修复丢数据问题

  - id: UX-020
    name: 响应式 (大屏/中屏/小屏)
    severity: P2
    standard: Vue 3 响应式设计规范
    check_method: Grep `@media|flex-wrap` 等响应式样式。
    pass_criteria: 主流分辨率下不出现横向滚动条
    failure_action: 补充响应式

  - id: UX-021
    name: 无障碍 (a11y / 键盘导航 / 焦点管理)
    severity: P2
    standard: WCAG 2.1 AA 标准
    check_method: Grep `aria-label|tabindex`;用 Chrome DevTools 检查 focus。
    pass_criteria: 主要交互元素可达
    failure_action: 补充 a11y

  - id: UX-022
    name: 与 Murex/FIS Quantum UX 模式对比
    severity: P2
    standard: FIS Quantum / Murex MX.3 用户研究
    check_method: 对比 Murex MX.3 / FIS Quantum 录屏,识别差异。
    pass_criteria: 关键场景至少 70% 一致
    failure_action: 长期规划

  - id: UX-023
    name: 4 模式统一布局 (new/copy/edit/readonly)
    severity: P1
    standard: 详情页必须实现 4 种模式的统一布局 — 新建(new)/复制(copy)/编辑(edit)/只读(readonly),通过 ModeBadge + disabled 状态区分,不创建 4 个独立页面
    check_method: |
      1. Read *.vue 详情页,识别是否有独立的 create/edit/copy/detail 页面
      2. Grep `mode` 变量,验证通过 computed 控制 disabled/readonly
      3. 校验 ModeBadge 对 4 种模式均正确显示
    pass_criteria: 1 个详情页组件覆盖 4 种模式;不创建 4 个独立页面
    failure_action: 合并为单组件多模式
```

---

## 审核流程 (Agent 可执行)

### Step 1: 范围确认
```bash
# 通过 Glob 定位待审核页面
web/src/views/{module}/{name}.vue
```

### Step 2: 静态检查 (Read / Grep)

| 模式 | 用途 |
|------|------|
| `Read *.vue` | 读取源码,识别布局 / 组件使用 |
| `Grep "console.error"` | 检测用户可见错误处理 |
| `Grep "BaseDataPicker\|ModeBadge"` | 检测公共组件复用 |
| `Grep ":type=\"getStatusType\""` | 检测状态色一致性 |
| `Grep "@change"` | 检测联动逻辑 |
| `Grep ":loading"` | 检测操作反馈 |

### Step 3: 对标检查
对比 `web/src/views/dealing/ac/AcDealList.vue` 或 `web/src/views/basedata/CountryList.vue`。

### Step 4: 评级与输出
- 按 3 级 (P0/P1/P2) 打标问题;
- 按 `templates/report.md` 输出报告;
- 按 `opentms-review-common` 评级:
  - 含 P0 → D (返工)
  - 含 P1 → C (修复后复审)
  - 仅 P2 → B (通过,记录待优化)
  - 无任何问题 → A

### Step 5: 整改建议
- 每个问题提供具体代码片段或修改路径;
- 标注预计工时;
- 归档到 `docs/reviews/{feature-name}/ux-review.md`。

---

## 一票否决 (P0 直判 D)

- **UX-001**: 核心流程 >5 步 → D
- **UX-002**: 与 Open-TMS 同类页面差异 >5 处 → D
- **UX-008**: catch 块使用 console.error 而非 ElMessage → D
- **UX-014**: 1080p 可见行数 <5 → D

---

## 协作关系

```
opentms-ux-design (UX 设计)
   └─→ opentms-frontend-dev (前端实现)
        └─→ opentms-review-ux (本次) ★ UX 审核
             └─→ opentms-review-frontend (前端代码审核)
                  └─→ opentms-test-execution (QA 测试)
```

**与 opentms-review-frontend 区别**:
- `opentms-review-ux`: 用户交互、视觉一致性、操作体验
- `opentms-review-frontend`: 代码风格、组件命名、API 字段绑定

---

## 相关文件

- `checklists/01-页面审核清单.md` — 页面级清单
- `references/standards.md` — UX 标准映射表
- `templates/report.md` — 审核报告模板
- `../../opentms-review-common/SKILL.md` — 公共规范
- `../../../CLAUDE.md` — Open-TMS 项目规范
- `../../../web/src/views/dealing/` — 既有参考页面

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.1 | 2026-07-05 | 新增 UX-023: 4 模式统一布局检查 |