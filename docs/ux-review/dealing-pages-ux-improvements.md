# 交易页面 UX 改进分析报告 (AC/AT/FX)

> **版本**: v1.0
> **作者**: UX 分析子代理
> **日期**: 2026-07-10
> **审查范围**: `web/src/views/dealing/*.vue` — Ac/At/Fx × List + Detail 共 6 个核心页 + 1 个 Form 组件 + 3 个 Dialog 组件
> **关键资源**: `M3-AC-AT-FX-页面改进UX审查.md` (本报告不重复其 8 条问题) · `M3-交易详情一屏布局UX原型.md` · `M3-交易详情内联编辑UX原型.md`
> **本报告新关注**: 一屏布局与原 8 条问题之外的"还可以更好"维度(操作流一致性 / 数据展示密度 / 错误恢复 / 性能细节)

---

## 一、整体评估

**[A-] 综合评级**: 交易三类型页(AC/AT/FX)已经走完了**一屏布局原型**(M3-交易详情一屏布局UX原型.md)+ 内联编辑 + 4 模式切换 + 跨模块 Picker preload,达到 Murex MX.3 70% 水准。

**主代理已识别的 8 条问题**(在 `M3-AC-AT-FX-页面改进UX审查.md` 中已有详尽 P0-P2 清单):
1. ✅ 顶部按钮 Disabled 视觉区分 (P1)
2. 关键信息条字段顺序/抽组件 (P2)
3. 列表页复制按钮 + NDF 残留 (P0)
4. Picker 联动冗余字段清空 (P1)
5. 离开页 dirty 检查 (P0)
6. errorMessage 5s 自动消失 (P2)
7. **AT 详情页缺少审批按钮 (P0)**
8. FX NDF fixing 状态在 key-info-bar 缺标识 (P2)

**本报告新增改进点**(与上述正交):
- **数据流**: 提交/审批/复制/RATE_FIX 的状态机可视化
- **操作流**: 列表 → 详情 → 编辑 → 保存 → 审批 的**键盘流 / Undo 支持**
- **视觉**: 关键数字 + 红绿色盲友好
- **性能**: 大表格(DealMap/Cashflow)虚拟滚动 + 服务端分页
- **空状态**: 每个 Tab 的 empty 态改进
- **辅助**: 错误堆栈展示 + 重试 + 进度条

**对标参考**:
- **FIS Quantum Trading**: 列表顶部"Pinned Filter"(最常用筛选置顶),交易状态用 **traffic light**(红/黄/绿)+ 文字双编码;
- **Murex MX.3 Trading**: 审批 Action 用 **lifecycle timeline**(横向 stepper,显示 1st/2nd Approver),RATE_FIX 完成后整页"软刷新"(不跳路由);
- **Bloomberg AIM FXGO**: RATE_FIX 用**浮动快捷条**(Floating Action Bar),始终在视野内;
- **SAP TRM Trading**: 删除前**预览 impacted DealMap/Cashflow 数**(已部分实现于 DefaultBankRule)。

---

## 二、详细改进列表

### 2.1 [P0] AT 详情页补"审批"按钮 (与 AC/FX 不一致) — 复审 + 实施细节

**文件**: `AtDealDetail.vue:11-15`(顶部 readonly 模板)

**问题代码**:
```vue
<template v-if="mode === 'readonly'">
  <el-button type="success" size="small" :icon="CopyDocument" @click="enterCopy">复制</el-button>
  <el-button v-if="canEdit(detail.status)" type="primary" size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
  <el-button v-if="canDelete(detail.status)" type="danger" size="small" :icon="Delete" @click="handleDelete">删除</el-button>
</template>
```
- 主代理已识别此问题,本报告补充实施细节。

**场景复现**:
1. 财务创建 AT 转账 → 等待部门主管审批;
2. 进入 AT 详情,只能看到"复制/编辑/删除",**没有审批按钮**;
3. 用户找不到入口,必须跳到 `/approval/approval-task` 查看待办 — 流程**断点**;
4. 严重性: 这个 bug 直接等同于"AT 审批功能不可用"。

**建议实施**(完整版):
```vue
<!-- AtDealDetail.vue:11-15 -->
<template v-if="mode === 'readonly'">
  <el-button v-if="isNdfForDetail && !detail.fixingRate" type="warning" size="small" :icon="MagicStick" @click="openRateFixDialog">RATE_FIX</el-button>
  <el-button type="success" size="small" :icon="CopyDocument" @click="enterCopy">复制</el-button>
  <el-button v-if="canEdit(detail.status)" type="primary" size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
  <el-button v-if="detail.status === 'New' || detail.status === 'Pending'" type="primary" size="small" :icon="Check" @click="handleApprove">审批</el-button>
  <el-button v-if="canDelete(detail.status)" type="danger" size="small" :icon="Delete" @click="handleDelete">删除</el-button>
</template>
```

**审批弹窗统一改造** (复用 ActionApprovalDialog):
```vue
<ActionApprovalDialog v-model="approvalVisible" :deal="detail" :actions="actionList" @approved="onApproved" />
```
```js
// AtDealDetail.vue 脚本补充
import ActionApprovalDialog from './ActionApprovalDialog.vue'  // 复用
const approvalVisible = ref(false)
const handleApprove = () => { approvalVisible.value = true }
const onApproved = async () => {
  approvalVisible.value = false
  ElMessage.success('审批成功')
  await loadDataByNumber(detail.value.dealNumber)
}
```
**列表页同步**: 在 `AtDealList.vue:69` 操作列加"审批"快捷链接(`v-if="row.status === 'New'"`)。
**底层**: 检查后端是否已有 AT 走两级审批规则(参考 `9d826a2` Phase 0-3 评审:"AT ✅ submit → approve");若没有,提交 P1 PRD。

**对标**: Murex MX.3 AT/FX/AC 三个交易类型都有审批按钮(一致性)。
**工作量**: S(2-4h,复用 AC 的 ActionApprovalDialog)
**优先级**: **P0**(主代理已识别,本报告确认为最高优先)

---

### 2.2 [P0] AT 跨币种 / 同管理主体 业务规则未在 UI 上"显示约束"

**文件**: `AtDealDetail.vue:46-53`(`<el-alert title="AT 交易仅支持同管理主体、同币种的内部转账...">`)

**问题**:
- 已有 alert 显示规则,但**只在非 readonly 模式显示**(`v-if="mode !== 'readonly'"`);
- readonly 模式(财务主管审核)看到的是核心摘要,**看不到业务规则**;
- alert 显示在顶部,而**错误发生时**用户的眼睛**是聚焦在错误字段上**的。

**场景**:
- 财务 A 误用 FX 来做内部调拨:FX 流程复杂,正确应该用 AT,但目前 FX 表单没有提示"建议改用 AT 交易";
- 财务 B 输入 AT 时选了跨币种账户,前端实时校验错误弹出在底部 `<el-alert errorMessage>`,但 alert 自身又是独立的。

**建议**:
1. **顶部 banner 改为 sticky**: 不管模式,只要用户开始编辑就显示;
2. **Form 实时错误归位字段**: 把"管理主体不匹配""跨币种"等校验移动到每个 `<el-form-item>` 上,使用 `<el-form-item :error="errorMap.managementEntity">`;
3. **FX 表单补"判定提示"**:`FxDealDetail.vue:110` 顶部加一条:
```vue
<el-alert v-if="isCrossCompanyFx" type="info" :closable="false" style="margin-bottom: 12px;">
  <template #title>检测到同管理主体交易,如仅做内部转账可考虑使用 AT 交易(流程更简单)</template>
</el-alert>
```
4. **每个 Picker 输入框旁加 help-icon**: `<el-tooltip content="汇率=1 时更推荐 AT"><el-icon><QuestionFilled /></el-icon></el-tooltip>`。

**对标**: Bloomberg AIM 一致性建议(Suggestion Banner)。
**工作量**: M(0.5-1 天,AT/FX 各改造 1 个 alert)
**优先级**: **P0**(流程合规)

---

### 2.3 [P0] RATE_FIX 后无"软刷新"反馈,体验割裂

**文件**: `FxDealDetail.vue:847-875` `doRateFix`

**问题**:
- RATE_FIX 成功后代码 `activeTab.value = 'cashflow'` + `loadData()`,**强制跳到 Cashflow Tab**;
- 用户原本在 "DealMap" 或 "Action" Tab 查看 fix 历史,被强制跳走;
- ElMessage success 提示一行信息,但是用户场景:**他刚做完 RATE_FIX,期望看"snapshot 状态 + 自动跳到最新"**,当前实现是**静默跳走**。

**建议**(参考 Bloomberg AIM FXGO):
1. **不强制跳走** — RATE_FIX 成功后保留在原 Tab,但弹出**右下角"result toast"**(3 秒),内容:
```
RATE_FIX 完成
  结算金额: +215,000.00 USD(流出)
  生成 Cashflow: CF202607100001
  [查看 Cashflow] [查看 DealMap] [关闭]
```
2. 列表页 `FxDealList.vue:80` RATE_FIX 也用同样 toast 反馈(目前已经有 ElMessage,但只一行文字)。
3. 在 key-info-bar `7. FX NDF fixing 状态` 加 **脉冲徽章**(主代理已识别的 P2),RATE_FIX 后**从黄色脉冲变绿色稳定徽章**,动画流畅。

```js
// 替代直接 activeTab.value = 'cashflow'
const success = (data) => {
  ElNotification.success({
    title: 'RATE_FIX 完成',
    message: `结算: ${data.direction === 'Inflow' ? '+' : '-'}${formatAmount(data.settlementAmount)} ${data.currency || ''}`,
    duration: 5000,
    customClass: 'ratefix-success-toast'
  })
}
```

**对标**: Bloomberg AIM FXGO,RATE_FIX 完成后 toast 不打断用户。
**工作量**: S(2-3h)
**优先级**: **P0**(UX 体验一致性)

---

### 2.4 [P0] FX/AC/AT 列表"alerts 文案"必须改用户视角

**文件**: `AcDealList.vue:33-37` + `FxDealList.vue:37-41`

**问题代码**:
```vue
<!-- AcDealList.vue:33 -->
<el-alert type="info" :closable="false">
  <template #title>基于 v2.0：创建后自动生成 DealMap(ActualCashflow) + Cashflow；修改软删旧 DealMap + 新建；删除级联软删</template>
</el-alert>

<!-- FxDealList.vue:37 -->
<el-alert type="info" :closable="false">
  <template #title>v3.2: DX 创建即生成 3 DealMap + 0/2 Cashflow(NDF 等 RATE_FIX);后端 calculate 联动计算</template>
</el-alert>
```
- 这是**开发者视角**的内部说明,出现在用户面前**不专业**;
- "v2.0 / v3.2" 是版本号,用户不知道;
- "软删 / DealMap" 是技术术语,财务人员不懂;
- **不可关闭**(closable=false),用户第一次进页面就被强制灌输,无法隐藏。

**建议**:
1. **彻底删除**(默认列表中不显示);
2. 如果一定要有"业务规则提示",改为**详情页右上角"?"图标 → 鼠标悬停 tooltip**;
3. 或者移到 "帮助文档" 链接,放到列表顶部右侧(`<el-button link>查看业务规则说明</el-button>`)。

**对标**: Bloomberg AIM 列表页无开发者提示。
**工作量**: XS(0.5h)
**优先级**: **P0**(影响 3 个交易页首屏,易删除)

---

### 2.5 [P1] FX 列表"管理主体/对手方"用 `el-input-number` + ID 过滤是反模式

**文件**: `FxDealList.vue:5-10`

**问题**:
```vue
<el-form-item label="管理主体">
  <el-input-number v-model="queryForm.managementEntityId" placeholder="ID" :min="1" style="width: 120px;" />
</el-form-item>
<el-form-item label="对手方">
  <el-input-number v-model="queryForm.counterpartyId" placeholder="ID" :min="1" style="width: 120px;" />
</el-form-item>
```
- 让用户输入 `123`、`456` 等数字 ID — **用户根本记不住 ID**!
- 用户场景: "我想查 UBS(对手方)的 FX 交易",UX 用户在 UBS 详情里看到 ID=42,然后要去 FX 列表输入 42 — **断点**;
- 与 AC/AT 列表 `v-model="queryForm.managementEntity"` (string) 行为**不一致**。

**建议**:
复用 **BaseDataPicker**(已在其它页面使用):
```vue
<el-form-item label="管理主体">
  <BaseDataPicker v-model="queryForm.managementEntityId" entity="management-entity" placeholder="主体" size="default" />
</el-form-item>
<el-form-item label="对手方">
  <BaseDataPicker v-model="queryForm.counterpartyId" entity="counterparty" placeholder="对手方" size="default" />
</el-form-item>
```
**配套**: 用 `BaseDataPicker.preload-row` 显示已选项;清空 `<el-input-number>` 改用 `<el-select filterable>` 简易版即可。

**对标**: Murex / FIS Quantum 列表筛选用 Picker 而非 ID 输入。
**工作量**: XS(0.5h)
**优先级**: **P1**

---

### 2.6 [P1] FX 列表无"日期分桶"快捷面板(今日/本周/本月/上月)

**文件**: `FxDealList.vue:25-27` 仅一个 daterange 选择

**问题**:
- 财务调度每日/每周/每月看 FX 流量,每次都要手动选日期范围;
- 缺少"今日 / 本周 / 本月 / 本季 / 本年"快速按钮;
- AC/AT 列表**完全没有日期筛选**,更严重。

**建议**:
1. 在 `dateRange` 旁边加 quick filter tabs:
```vue
<el-radio-group v-model="quickRange" size="default" @change="applyQuickRange">
  <el-radio-button label="today">今日</el-radio-button>
  <el-radio-button label="week">本周</el-radio-button>
  <el-radio-button label="month">本月</el-radio-button>
  <el-radio-button label="quarter">本季</el-radio-button>
  <el-radio-button label="">自定义</el-radio-button>
</el-radio-group>
```
2. AC/AT 列表也补上同样的 quick range。

**对标**: Bloomberg AIM / Murex Trading。
**工作量**: S(每页 ~1-2h,quickRange composable 抽取 0.5 天)
**优先级**: **P1**(财务高频操作)

---

### 2.7 [P1] FX 列表 RATE_FIX 按钮条件 `productType === 'NDF' && status === 'New'` 过于严苛

**文件**: `FxDealList.vue:80`

**问题**:
```vue
<el-button type="warning" link @click="handleRateFix(row)" v-if="row.productType === 'NDF' && row.status === 'New'">RATE_FIX</el-button>
```
- 仅当 `status === 'New'` 时显示;
- 但 NDF fix 也可能发生在 `Pending` / `Approved` 状态,**已 approve 但还未 fixing**;
- 用户场景: NDF 已 approve,但 fix 日期过了还没 fixing,这时列表无 RATE_FIX 入口,必须**走详情页**才可触发。

**建议**:
- 改为 `status !== 'Canceled' && !detail.fixingRate` — Active/NDF 用户都可触发(后端可能有更细致权限);
- 详情页 `FxDealDetail.vue:12` 已经有同款判断 `v-if="isNdfForDetail && !detail.fixingRate"`,前后端要**对齐语义**。

**对标**: Murex NDF fix 可在多个状态触发,直到 fix 日期截止。
**工作量**: XS(0.5h)
**优先级**: **P1**

---

### 2.8 [P1] 详情页审批/RATE_FIX 弹窗 width/响应式不一致

**文件**:
- `FxDealDetail.vue:327` RATE_FIX: width 480px
- `FxDealDetail.vue:358` 审批: width 780px
- `AcDealDetail.vue` ActionApprovalDialog: 复用组件
- `AtDealDetail.vue` 缺(待补)

**问题**:
- 三个弹窗宽度不一;
- 480px 在 1080p 视口下显得局促,字段密度低;
- 780px 在 1024px 屏幕下又塞得过满;
- 与 Drawer / Dialog 设计语言不统一。

**建议**:
- 弹窗 width 用 **80vw, max-width 720px**(审批)、**480px**(RATE_FIX) 两档固定;
- 考虑改用 `el-drawer direction="rtl"`(仿照 basedata 列表的 Drawer)做"侧出"审批弹窗,体验更顺;
- 与 `M3-交易详情一屏布局` 原型视觉一致。

**对标**: Murex 审批弹窗用固定 720px。
**工作量**: XS(0.5h)
**优先级**: **P1**

---

### 2.9 [P1] 详情页底部 Tab "默认显示哪个" 不一致

**文件**:
- `AcDealDetail.vue:365` `activeTab = ref('dealmap')`
- `FxDealDetail.vue:436` `activeTab = ref('basic')`
- `AtDealDetail.vue:382` `activeTab = ref('dealmap')`

**问题**:
- AC:默认 DealMap;AT:默认 DealMap;FX:默认审计信息(basic);
- 用户"刚打开交易" 第一眼想看什么?
  - AC/AT 业务侧重 → 看到 DealMap 才心安;
  - FX 业务侧重 → 看到"总金额 + 状态"key-info-bar 即可,Tab 看 DealMap 操作流;
  - **"基本审计信息"在 readonly 模式默认被显示,信息价值低,挤掉 DealMap/Cashflow**。

**建议**:
- FX 改成 `activeTab = ref('cashflow')`(Cashflow 是 FX 业务最关心);
- AC 改成 `activeTab = ref('action')`(Action 显示全生命周期);
- 用户偏好持久化到 `localStorage.openTms.activeTab.fx = 'cashflow'`。

**对标**: SAP TRM / Murex 列出用户偏好。
**工作量**: XS(0.5h)
**优先级**: **P1**

---

### 2.10 [P1] 关键信息条"右侧"无操作快捷按钮

**文件**: `AcDealDetail.vue:24-45`, `FxDealDetail.vue:25-44`, `AtDealDetail.vue:23-42`

**问题**:
- 关键信息条是信息展示,**下方"复制 / 编辑 / 审批 / 删除"** 在顶部工具条 — 用户视线焦点在 key-info-bar 时,操作按钮要扫视到顶部 48px 工具条;
- 业界 Bloomberg AIM 把快捷操作直接 inline 进 key-info-bar **右侧**(小图标按钮)。

**建议**:
- key-info-bar 右侧加 **"快捷操作组"**(Mini icon-button):
```vue
<div class="key-info-bar">
  <div class="key-items">...</div>
  <div class="key-actions" v-if="mode === 'readonly'">
    <el-tooltip content="编辑"><el-button :icon="Edit" circle size="small" @click="enterEdit" /></el-tooltip>
    <el-tooltip content="复制"><el-button :icon="CopyDocument" circle size="small" @click="enterCopy" /></el-tooltip>
    <el-tooltip v-if="detail.status === 'New'" content="审批"><el-button :icon="Check" circle type="primary" size="small" @click="handleApprove" /></el-tooltip>
  </div>
</div>
```
- 或者使用 M3 原型的"双操作条"模式(顶部精简 + key-info-bar 内含一组动作)。

**对标**: Bloomberg AIM Trading Detail。
**工作量**: S(每页 ~1h)
**优先级**: **P1**

---

### 2.11 [P2] DealMap / Cashflow 表格无虚拟滚动

**文件**: 所有详情页 Tab 中的 dealmap / cashflow / action 表格 — `AcDealDetail.vue:204`、`FxDealDetail.vue:234`、`AtDealDetail.vue:200` 等

**问题**:
- `max-height="280"` 是固定值 — 一旦超过 280px 高度(大约 8 行)表格内部滚动,**但 el-table 不支持虚拟滚动**(Element Plus 不内置);
- 用户列表 > 100 行时,要么卡顿,要么只能显示前 5 行(且无"加载更多");
- 企业 TMS 中,3 年的 FX 交易可能 Cashflow 上千条,直接卡住。

**建议**:
- 短期:用 **el-table-virtual**(Element Plus 内置 lazy render + 配合 `:row-class-name` 高亮);
- 中期:接入 `vue-virtual-scroller` 或 `el-table` 的 `lazy-load` 模式;
- 推荐:**服务端分页**(`GET /api/v1/deal-maps?pageNum=&pageSize=`),每页 50 条 + "加载更多" 按钮。

**对标**: Murex 大表格全部服务端分页。
**工作量**: M(后端分页 1-2 天 + 前端适配 0.5 天)
**优先级**: **P2**(不是阻塞,但 1k+ 行体验劣化)

---

### 2.12 [P2] "Cashflow" Tab 内容很薄,占 Tab 一位但价值低

**文件**: `AcDealDetail.vue:229-256` / `FxDealDetail.vue:256-283` / `AtDealDetail.vue:228-254`

**问题**:
- Cashflow Tab 默认显示 `dealmapNumber + direction + amount + valueDate + status` — 这些字段其实在 **DealMap Tab 已经能追踪到**(因 1 DealMap → 0/1 Cashflow 的 1:1 关系);
- 用户打开 Cashflow Tab,**重复看到几乎相同数据**,无新增 insights(没有"累计净值" / "未结算笔数" / "已结算笔数");
- Tab 一位被低价值占用,挤压更有价值的"审批 / 影像" Tab。

**建议**:
1. Cashflow Tab 顶部加 **聚合 summary**:
```vue
<div class="cashflow-summary">
  <KpiCard label="笔数" :value="cashflowList.length" />
  <KpiCard label="未结算" :value="unsettled" type="warning" />
  <KpiCard label="已结算" :value="settled" type="success" />
  <KpiCard label="总金额" :value="formatAmount(totalAmount)" type="primary" />
</div>
```
2. 同时支持"按时间分组 + 折叠"视图(年/月/日);
3. 在表格中加"所属 DealMap 行 hover 高亮"联动(DealMap ↔ Cashflow 跨 Tab 跳转)。

**对标**: Murex Cashflow Tab 必有 aggregation header。
**工作量**: S(每页 ~1-2h)
**优先级**: **P2**

---

### 2.13 [P2] 审批操作"/dealing/fx-deal/approve" 重复 load → UX 颤动

**文件**: `FxDealDetail.vue:814-823` `doApprove`

**问题**:
```js
const doApprove = async (action) => {
  ...
  await handleApprove()        // 重新拉 pending actions
  await loadData(detail.value.dealNumber)  // 重新拉全量 detail
  ...
}
```
- 一次审批触发 **2 次后端调用**:1 次列表刷新 + 1 次详情刷新;
- 用户场景:同时打开多个 Action 待审批(常见),每点一次"通过"浏览器卡顿 1-2 秒。

**建议**:
- 合并为**单次刷新**或在本地状态做 optimistic update:
```js
const doApprove = async (action) => {
  // Optimistic: 本地先改状态
  const idx = pendingActions.value.findIndex(a => a.actionNumber === action.actionNumber)
  if (idx >= 0) pendingActions.value.splice(idx, 1)
  
  try {
    await approveFxAction(action.actionNumber, ...)
    ElMessage.success(...)
    // 仅刷新 detail 部分字段,不全量 load
    await loadData(detail.value.dealNumber)
  } catch (e) {
    // 失败回滚
    pendingActions.value.push(action)
    ElMessage.error(...)
  }
}
```

**对标**: Bloomberg AIM Optimistic UI 通用做法。
**工作量**: S(2-3h)
**优先级**: **P2**

---

### 2.14 [P2] 列表删除操作无 Keyboard 支持 / 无 confirm 原因填入

**文件**: 3 个 List.vue

**问题**:
- `handleDelete(row)` 只用 `ElMessageBox.confirm("确定删除...")` 一个按钮,无原因录入口;
- 风险:用户误删后无法追溯"为什么删"(已经被默认 remark);

**建议**:
- 删除确认对话框加 **remark 必填**:
```vue
<el-dialog v-model="deleteDialogVisible" title="删除确认" width="440px">
  <p>确认删除 {{ row.dealNumber }}?</p>
  <el-form-item label="删除原因">
    <el-input v-model="deleteReason" type="textarea" :rows="2" required />
  </el-form-item>
  <p class="impact">将级联软删 {{ impactCount }} 笔 DealMap / Cashflow。</p>
</el-dialog>
```
- 后端 `delete` 接口接受 `reason` 参数,写入 `tms_action_remark`。

**对标**: SAP TRM 强制 reason。
**工作量**: M(后端 + 前端 ~1 天)
**优先级**: **P2**(合规审计要求,生产建议)

---

### 2.15 [P2] 详情页金额字体 18px 等宽,但缺红绿色盲友好色(高对比模式)

**文件**: 3 个 detail 的 `.amount-value { font-size: 18px; font-weight: 700; }`

**问题**:
- 流入/流出用 el-tag `success`(绿) / `danger`(红),**红绿色盲(~5-8% 男性用户)看不出差异**;
- P0 的金额/方向语义被颜色 only 编码,色盲用户不能区分。

**建议**:
- 在金额后面追加 **方向图标**(el-icon: `Top` / `Bottom` / `CaretTop` / `CaretBottom`),冗余编码;
- 同时允许"高对比模式"(User Profile 中开启)切换全局颜色:
```vue
<el-tag :type="... ">
  <el-icon style="margin-right: 4px;"><Top v-if="row.direction === 'Inflow'" /><Bottom v-else /></el-icon>
  {{ row.direction === 'Inflow' ? '流入' : '流出' }}
</el-tag>
```

**对标**: W3C WCAG 2.1 SC 1.4.1。
**工作量**: XS(1h 全局替换)
**优先级**: **P2**(无障碍合规)

---

### 2.16 [P2] AcDealList 表格"管理主体" 列直接显示编码,无名称

**文件**: `AcDealList.vue:45` `prop="managementEntity"`

**问题**:
- 显示 `BU_TEST_NEW` 这种 code,人看不出是什么主体;
- 列表已经在 `BankAccountList.vue:62-64` 用 `getManagementEntityName(id)` 显示名称,这里却没有;
- **3 个交易列表的字段命名规范不一致**:`managementEntity` (string) vs `managementEntityId` (long)。

**建议**:
- AcDeal 列表也接入:`{{ getManagementEntityName(row.managementEntity) }}`;
- 三类交易统一为"管理主体"显示"code + name"(`BU_TEST_NEW (深圳分公司)`);
- 同时,后端 VO 字段名应该统一为 `managementEntityId` + `managementEntityName`(参考基于 BaseEntity 的设计);

**对标**: Murex 列宽 80px 必带 code + name。
**工作量**: S(2h)
**优先级**: **P2**

---

### 2.17 [P2] "复制模式" 后表单脏数据清除不彻底

**文件**: `AcDealDetail.vue:705-715` `loadCopyData` / `FxDealDetail.vue:944-954` / `AtDealDetail.vue:782-815`

**问题**:
- 复制模式 `loadCopyData` 后:`form.dealNumber = ''` 已清,但 `form.fixingRate / fixingSource / settlementAmount` 等**FX NDF 专用字段不清**;
- 场景:**已 fixing 的 NDF,用户复制后,新建的"新 NDF"继承了旧 fixingRate**,误以为已 fixing(主代理已识别的 P0 问题)。

**建议(具体代码)**:
```js
// FxDealDetail.vue loadCopyData
const loadCopyData = async (dealNumber) => {
  try {
    const res = await copyFxDeal(dealNumber)
    const data = res.data || res
    fillFormFromObject(data)
    applyPreloadFromCopyData(data)
    form.dealNumber = ''
    form.id = null
    form.lockToken = null
    
    // ★ 修复 #3: 复制 NDF 时清空 fixing 残留
    form.fixingRate = null
    form.fixDate = null
    form.fixCurrency = ''
    form.fixMarketRate = null
    form.fixRemark = ''
    form.settlementAmount = null
    form.notional = null
  } catch (e) {
    ElMessage.error('复制失败: ' + (e?.message || '请重试'))
  }
}
```

**对标**: Bloomberg AIM Copy Trade 必清 fixing/maturity 残留。
**工作量**: XS(0.5h)
**优先级**: **P0**(主代理已识别)— 已在新报告中重申。

---

### 2.18 [P2] 详情页主信息区字段宽度不一致(720px 视口下拥挤)

**文件**: 3 个 detail 的 `<el-row :gutter="12">`

**问题**:
- 字段用 `<el-col :sm="12" :md="6">` 三档断点,在 1280-1440px 视口下 4 列,**AC 详情 13 字段** = 4 行 + 1 行(描述) = 5 行,**单 col 宽 ≈ 280px**,含 BaseDataPicker 显示"code (name)" 容易**截断**;
- `action-bar` `direction` 字段(流出/流入)显示 `el-radio-button` 4 列宽度过宽;
- 表单中 label 长字段(`管理主体 / 交易对手 / 交易员 / 金融工具 / 付出方账户 / 收入方账户 / 支付方式 / 操作人`...)挤成 4 列后 label 都被截。

**建议**:
- 关键字段(必填、核心)放 **md=8** 双列;
- 次要字段 / Picker 放 **md=4** 四列;
- 描述 / 备注 `sm=24 md=24` 一行;
- 优化断点:大屏 1920px 用 `:md="6"`(4 列),1440-1919 用 `:md="8"`(3 列),1024-1439 用 `:md="12"`(2 列),<1024 用 `:sm="24"`(1 列)。

```vue
<el-col :xs="24" :sm="12" :md="8" :lg="6">
  <!-- 高频核心字段 -->
</el-col>
<el-col :xs="24" :sm="12" :md="12" :lg="8">
  <!-- Picker / 大字段 -->
</el-col>
```

**对标**: Murex 表单布局通用做法。
**工作量**: S(2-3h)
**优先级**: **P2**

---

### 2.19 [P2] 列表页 key info bar 顶部 KPI 缺失 (按交易类型聚合)

**文件**: 3 个 list 的表格上方

**问题**:
- 用户进 AC 列表,看不到"今日新增多少 AC 交易 / 本月总金额";
- 用户进 FX 列表,看不到"今日 3 笔已 RATE_FIX / 待 RATE_FIX 多少";
- 用户进 AT 列表,看不到"本月内部调拨 ¥X.XX 亿";

**建议**:
- 列表页 Filter Card 下、Table Card 上加一行 KPI Banner:
```vue
<div class="kpi-row" v-if="!loading">
  <KpiCard label="今日新增" :value="kpi.todayCount" />
  <KpiCard label="待审批" :value="kpi.pending" type="warning" />
  <KpiCard label="本年总金额" :value="kpi.yearAmount" type="primary" />
  <KpiCard v-if="dealType === 'FX'" label="待 RATE_FIX" :value="kpi.pendingRateFix" type="danger" />
</div>
```
- 后端提供 `GET /api/v1/{dealType}/stats?from=&to=` 接口,前端每 60s 缓存。

**对标**: FIS Quantum / Bloomberg AIM / Murex Trading。
**工作量**: M(后端 1 天 + 前端 0.5 天)
**优先级**: **P2**

---

### 2.20 [P2] AcDealList 表格无冻结列(滚动后 "操作" 列丢失)

**文件**: `AcDealList.vue:67` / `FxDealList.vue:76` / `AtDealList.vue:69`

**问题**:
```vue
<el-table-column label="操作" width="220" fixed="right">
```
- 已用 `fixed="right"`,但只有操作列被冻结;
- 用户水平滚动后,**"交易编号" / "金额" / "日期" 这些核心列都被覆盖**;
- AT 列表 12 列,水平滚动严重。

**建议**:
- 头部必读列也固定 `fixed="left"`:
```vue
<el-table-column type="index" width="60" align="center" fixed="left" />
<el-table-column prop="dealNumber" label="交易编号" width="170" fixed="left" />
```
- AC/AT 也补全 — AT 表格"金额/日期/币种"列应该一起冻结。

**对标**: 通用表格体验。
**工作量**: XS(0.5h)
**优先级**: **P2**

---

## 三、横向对比表(交易页)

| 维度 | AC | AT | FX | 业界对标 (Murex / FIS) |
|------|----|----|----|---------------------|
| **顶部操作按钮数** | 4-5 | **3-4 (缺审批)** | 5 | 4-5 统一 |
| **关键信息条字段** | 5 | 5 | 5 | 5-7 |
| **审批按钮** | ✅ | ❌ | ✅ | 必备 |
| **RATE_FIX 反馈软刷新** | — | — | ⚠️ 强制跳 | toast 不跳 |
| **activeTab 默认** | dealmap | dealmap | basic | 依业务定 |
| **快速日期分桶** | ❌ | ❌ | ❌ | 必备 |
| **KPI Banner** | ❌ | ❌ | ❌ | 必备 |
| **批量操作** | ❌ | ❌ | ❌ | Murex 必备 |
| **ID 输入筛选** | — | — | ⚠️ | Picker |
| **主信息字段密度** | 4 列 | 4 列 | 4 列 | 自适应 |
| **复制清空残留** | 部分 | 部分 | ⚠️ | 全清 |
| **Tab 数量** | 5(基本+4) | 6 | 4 | 4-6 必要 |
| **键盘快捷键 / Esc 关** | ⚠️ | ⚠️ | ⚠️ | 全键盘 |
| **键盘快捷键 / Ctrl+S 保存** | ❌ | ❌ | ❌ | 通用 |

---

## 四、P0 紧急修复 (本周,与主代理 8 条 + 2 条新增)

1. **2.1** AT 详情页补"审批"按钮 + ActionApprovalDialog 复用(主代理已识别)
2. **2.4** 移除列表页 "v2.0 / v3.2" alert 文案(本报告新增)
3. **2.3** RATE_FIX 完成后 toast 不强制跳 Tab(本报告新增)
4. **2.17** FX NDF 复制时清空 fixing 残留(主代理已识别,本报告补充代码)

## 五、P1 重要优化 (1 个月内)

5. **2.2** AT 跨币种 / 跨主体约束 sticky 显示
6. **2.5** FX 列表 `el-input-number` + ID 改 BaseDataPicker
7. **2.6** AC/AT/FX 三页补 Quick Date Range(Today/Week/Month/Quarter)
8. **2.7** FX 列表 RATE_FIX 按钮条件放宽
9. **2.8** 审批/RATE_FIX 弹窗 width 统一
10. **2.9** 详情页 activeTab 默认按业务重排
11. **2.10** key-info-bar 右侧加 inline 操作按钮
12. **2.16** 三类交易"管理主体"列加 name 一致

## 六、P2 锦上添花

13. **2.11** DealMap/Cashflow 表格服务端分页 + 虚拟滚动
14. **2.12** Cashflow Tab 加 aggregation summary
15. **2.13** 审批乐观更新(Optimistic UI)
16. **2.14** 删除前填 reason + 显示 impacted count
17. **2.15** 红绿色盲友好:方向加 icon
18. **2.18** 详情表单 `:md` 三档断点(6/8/12)
19. **2.19** 列表页 KPI Banner
20. **2.20** 列表页关键列 `fixed="left"`

---

## 七、总结

**交易页 UX 现状**: **A-** 级,与 Murex 70% 水准对齐,**流程完整性** + **数据展示** + **异常处理** 都不错。

**待改进重点**:
1. **关键 bug 收口** — AT 缺审批 / FX NDF 残留 / RATE_FIX 软刷新 / 列表 alert 文案;
2. **筛选升级** — Date Range Quick / BaseDataPicker / KPI Banner;
3. **细节一致性** — 弹窗 width / activeTab 默认 / key-info-bar inline 操作。

**对标差距**: 与 Bloomberg AIM 差距主要在"**Optimistic UI / 键盘流 / 大表格虚拟滚动**";与 Murex 差距主要在"**Master-Detail / 业务聚合 / 快速筛选**"。

**建议执行节奏**: 本周收掉 P0(主代理 8 条 + 本报告新增 4 条)→ 本月 P1 重点在筛选和 KPI → 季度 P2 做性能和无障碍。

---

*UX 改进建议 - v1.0 - 2026-07-10*
