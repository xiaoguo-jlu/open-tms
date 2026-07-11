# 规则页面 UX 改进分析报告 (Default Bank Account Rule 优先)

> **版本**: v1.0
> **作者**: UX 分析子代理
> **日期**: 2026-07-10
> **审查范围**:
>   - **核心**:`web/src/views/basedata/DefaultBankAccountRuleList.vue` (已修复弹框下拉)
>   - **次要**:`web/src/views/approval/ApprovalRuleList.vue` + `web/src/views/approval/WorkflowTemplate.vue`
> **不重复**: 主代理已修的 DefaultBankAccountRuleList 弹框下拉 bug
> **关键资源**: CLAUDE.md · `summmary.md` (默认银行账户规则 v1.1 特性) · M3-AC-AT-FX UX 审查(共享多模式规范)

---

## 一、整体评估

**[B+] 综合评级**: DefaultBankAccountRuleList 已经做到 **Murex 65% 水准** — 11 端点 + 双方向 + 并发控制 + 审计 + 缓存都做了,体验比一般业务系统强。但仍有 **6 处明显改进点**:

**核心优点**:
1. ✅ 双方向 Inflow/Outflow 区分;
2. ✅ 优先级 inline-edit(el-input-number) — 业界领先;
3. ✅ Status el-switch inline toggle;
4. ✅ 审计历史时间线(el-timeline)展示;
5. ✅ 影响笔数(refCount)在删除确认前提示;
6. ✅ 并发冲突 409 弹窗提示刷新;
7. ✅ 必填规则(主体必填)前置 + 失败静默。

**主要改进方向**:
- **详情页** — v1.1 没有详情页(只有 Edit Dialog);
- **可解释性** — "为什么这条规则被命中 / 这条规则覆盖谁" 是用户最关心的;
- **批量** — 12 个规则如何批量启用 / 优先级排序;
- **优先级冲突** — Active 唯一约束未在 UI 阶段预防,只能等保存时 409 报错。

**对标参考**:
- **FIS Quantum Rule Engine**: 列表 + 详情(Tab: 规则表达式 / 命中示例 / 命中历史 / 影响范围)
- **Murex MX.3 Routing Rules**: 规则可视化编辑器(图节点)
- **Bloomberg AIM FX Dealing Rule**: 规则用 **decision tree** 展示 + 拖拽排序

---

## 二、详细改进列表

### 2.1 [P0] DefaultBankAccountRule **"管理主体必填"硬阻拦**导致首屏空白

**文件**: `DefaultBankAccountRuleList.vue:314-318`

**问题**:
```js
const handleQuery = async () => {
  if (!queryForm.managementEntityId) {
    ElMessage.warning('请先选择管理主体')
    return
  }
  ...
}
```
- **用户首次进页面看到空白表格**,认为是 bug;
- 与本页内"对手方 / 金融产品 / 币种 / 状态"**全可空筛选项不一致**;
- 在 `basedata-pages-ux-improvements.md` 的 2.2 也已识别(全局问题),此处聚焦**该页面的"预填"方案**。

**建议**:
1. **从全局 session 读取当前用户默认主体**,自动预填:
```js
import { useUserStore } from '@/stores/user'  // Pinia store(若已有)
const userStore = useUserStore()
const defaultMgmtEntityId = userStore.currentUser?.defaultMgmtEntityId

onMounted(() => {
  if (defaultMgmtEntityId) {
    queryForm.managementEntityId = defaultMgmtEntityId
    fetchData()
  } else {
    // 进入"等待用户选主体"态:
    needsMgmtSelection.value = true
    loadBaseData({ silent: true })
  }
})
```
2. **首屏空状态**:
```vue
<el-empty v-if="needsMgmtSelection" description="请选择管理主体开始查询">
  <el-button type="primary" @click="() => $refs.mgmtSelect.focus()">选择主体</el-button>
</el-empty>
```
**对标**: Bloomberg AIM 任何 list 默认带"default user"查询。
**工作量**: XS(1h,后端 user profile 默认主体若没有则创建字段)
**优先级**: **P0**

---

### 2.2 [P0] 缺少规则"详情页" — 用户无法查看命中示例 / 影响范围 / 历史

**文件**: `DefaultBankAccountRuleList.vue`(无 CounterpartyDetail.vue 类型)

**问题**:
- v1.1 完整实现 11 个端点 + 11 个缓存策略,但**只有 list + edit dialog**,没有 detail;
- 用户场景: 规则经理想审计"这条规则过去 30 天被多少交易命中 / 是否被上个月某次调账改了优先级",只能跳 SQL 查 audit log;
- **审计历史 → 时间线 → 弹窗**(el-dialog 800px)目前是从操作列"审计"按钮触发,但**没有"规则本身的说明 / 命中示例 / 监控" tab**;
- 用户列表的"操作"列已经 4 个按钮(编辑/复制/审计/删除),缺一个"详情"。

**场景**:
1. 风控: 为什么 USD SPOT 默认走账户 A? → 看规则 + 历史 → 改优先级;
2. 财务: 同事误改了规则导致 Outflow 走错账户 → 查命中历史 + 调账;
3. 审计: 季度合规 — 列示所有 Active 规则 + 命中笔数。

**建议**:
新增 `DefaultBankAccountRuleDetail.vue` 路由 `/basedata/default-bank-account-rule/detail?ruleNumber=xxx`,沿用 M3 一屏布局:
- **关键信息条**:规则编号 / 主体 / 默认账户 / 优先级 / 状态;
- **Tab**:
  - "基本信息" — 主体 / 对手方 / 金融产品 / 方向 / 币种(通配 = ALL);
  - "命中历史" — list 形式 `RuleHitVO`(`hit_count / last_hit_at / hit_examples`);
  - "审计" — 复用 `handleAuditLog` 的 el-timeline;
  - "影响范围" — 显示"现在被多少交易引用"(已有 `getReferenceCount`,但**只在删除前显示**,详情页一直缺失)。

```vue
<!-- 新详情页结构 -->
<div class="rule-detail one-screen">
  <div class="action-bar top">...</div>
  <div class="key-info-bar">
    <div class="key-item">规则编号: {{ rule.ruleNumber }}</div>
    <div class="key-item">主体: {{ rule.managementEntityName }}</div>
    <div class="key-item">默认账户: {{ rule.bankAccountName }}</div>
    <div class="key-item highlight">优先级: {{ rule.priority }}</div>
    <div class="key-item"><StatusTag :status="rule.status" /></div>
  </div>
  <el-tabs>
    <el-tab-pane label="基本信息">...</el-tab-pane>
    <el-tab-pane :label="`命中历史 (${hitCount})`">
      <el-table :data="hitList">...</el-table>
    </el-tab-pane>
    <el-tab-pane label="审计">
      <el-timeline>...</el-timeline>
    </el-tab-pane>
    <el-tab-pane label="影响范围">影响 {{ refCount }} 笔交易 · 列表 / Tab 4</el-tab-pane>
  </el-tabs>
</div>
```

**对标**: Murex MX.3 Routing Rule — Rule Detail 必有 4 tab(定义 / 命中 / 影响 / 审计)。
**工作量**: L(后端 `RuleHitVO` 接口 + 前端详情页 ~3 天)
**优先级**: **P0**(v1.1 缺失最高价值的功能)

---

### 2.3 [P0] 11 端点缓存策略用户不可见 — 监控/统计缺位

**文件**: `DefaultBankAccountRuleList.vue` 列表展示(tableData 字段 + KPI)

**问题**:
- v1.1 实现 11 端点 + 缓存,但**用户在列表页看不到任何"性能 / 缓存 / 命中率"信息**;
- 风控要回答:"本月规则引擎被查了多少次 / 命中率 / 误中次数"。

**建议**(KPI Banner):
- 在 Filter Card 与 Table 之间加一行 KPI 卡片:
```vue
<div class="kpi-row">
  <KpiCard label="总规则" :value="kpi.total" />
  <KpiCard label="启用" :value="kpi.active" type="success" />
  <KpiCard label="停用" :value="kpi.inactive" type="danger" />
  <KpiCard label="本月命中" :value="kpi.hits" type="primary" />
  <KpiCard label="本周新建" :value="kpi.newThisWeek" type="warning" />
</div>
```
- 后端新增 `GET /api/v1/default-bank-account-rules/stats?mgmtId=...` 接口,聚合 tms_rule_hit_log_t。

**对标**: FIS Quantum Rule Audit Dashboard。
**工作量**: M(后端 0.5 天 + 前端 0.5 天)
**优先级**: **P0**

---

### 2.4 [P1] 优先级 inline-edit 缺乏"撤销 / 重做"和"批量调整"

**文件**: `DefaultBankAccountRuleList.vue:71-76`

**问题**:
```vue
<el-input-number v-model="row.priority" :min="0" :max="9999" :step="10" size="small" @change="(val) => handlePriorityChange(row, val)" />
```
- 单 inline-edit 没有"撤销"机制,误改后只能再编辑对话框;
- 多条规则优先级**批量调整**没有(用户场景: 10 条同优先级规则整体加 10 让位);
- `handlePriorityChange` 错误处理仅 `ElMessage.warning('规则已被他人修改,请刷新')`,但**没有本地乐观更新** — UX 延迟感重。

**建议**:
1. **乐观更新**: 改完 el-input-number 后立即本地更新 `row.priority`,后台失败时 revert:
```js
const handlePriorityChange = async (row, val) => {
  const oldVal = row.priority
  row.priority = val  // 乐观更新
  try {
    const detail = await getDefaultBankAccountRule(row.id)
    if (detail.code !== 200) {
      row.priority = oldVal  // revert
      return
    }
    const res = await updateDefaultBankAccountRule({ ...detail.data, priority: val })
    if (res.code === 200) {
      ElMessage.success('优先级已更新')
    } else if (res.code === 409) {
      ElMessage.warning('规则已被他人修改,请刷新')
      row.priority = oldVal
      handleQuery()
    } else {
      row.priority = oldVal
      ElMessage.error(res.message || '更新失败')
    }
  } catch (e) {
    row.priority = oldVal
    ElMessage.error('更新异常:' + e.message)
  }
}
```
2. **批量调整优先级**:Toolbar 加"批量 +10" / "批量 -10" / "批量设高优":
```vue
<el-button v-if="selectedRows.length" @click="batchAdjustPriority(10)">批量优先级+10</el-button>
<el-button v-if="selectedRows.length" @click="batchAdjustPriority(-10)">批量-10</el-button>
```
3. **拖拽排序** (BankAccountRule 可按 priority 排序),用 `<el-draggable-list>` 或 sortable.js。

**对标**: Murex MX.3 Routing Rule Editor 拖拽改优先级。
**工作量**: M(乐观更新 0.5h + 批量 4h + 拖拽 1 天)
**优先级**: **P1**

---

### 2.5 [P1] 规则表达式可视化缺失 — 用户看不到"匹配什么 / 不匹配什么"

**文件**: `DefaultBankAccountRuleList.vue:78-79`

**问题**:
- 规则使用 "ALL / 通配" 语义,但**当前以 `el-tag 'ALL'` 文本显示**,用户看不出来;
- 用户看不到"这条规则会和哪些交易匹配"(只在规则计算时返回 hit_examples)。

**建议**:
1. **规则列表加 "匹配预览"列**(可选,默认折叠):
```vue
<el-table-column label="匹配维度" min-width="180">
  <template #default="{ row }">
    <span class="match-summary">
      <el-tag v-if="!row.counterpartyId" size="small">对手方</el-tag>
      <el-tag v-if="!row.instrumentId" size="small">产品</el-tag>
      <el-tag v-if="!row.currency" size="small">币种</el-tag>
      <el-tag v-if="row.counterpartyName">{{ row.counterpartyName }}</el-tag>
      <el-tag v-if="row.instrumentName">{{ row.instrumentName }}</el-tag>
      <el-tag v-if="row.currency">{{ row.currency }}</el-tag>
      <el-tag>{{ row.direction }}</el-tag>
    </span>
  </template>
</el-table-column>
```
2. **新增 `<RuleMatchAnalyzer>` 组件**(详情页 + 编辑对话框下方): 让用户**输入交易参数,看到此规则是否命中**:
```vue
<RuleMatchAnalyzer :rule="rule" @match="onMatchResult" />
```
3. **乐观预览**: 在 Edit Dialog 选完所有通配字段后,底部显示"该规则将匹配: USD Outflow × AC × Bank-A 范围内 N 笔历史交易"。

**对标**: Murex MX.3 Routing Rule Test Bench。
**工作量**: L(2-3 天,匹配分析器本身 1-2 天)
**优先级**: **P1**

---

### 2.6 [P1] 字段名"必填"标记与"通配"语义混在一起导致误存

**文件**: `DefaultBankAccountRuleList.vue:130-149`

**问题**:
```vue
<el-form-item label="对手方">
  <el-select v-model="editForm.counterpartyId" placeholder="ALL 通配" clearable filterable>
```
- "对手方 / 金融产品 / 币种" 都是 `clearable`(可空 = 通配 ALL),用户可能误以为必填而放弃保存;
- "管理主体 / 方向 / 默认账户 / 优先级" 是必填;
- 字段 label 上**没有视觉区分**:
  - 必填字段没有 `*`;
  - 通配字段没有"~"或"All"前缀;
- 用户保存时报"必填"错,可能漏指"账户"也可能漏指"主体"。

**建议**:
1. **视觉区分**:
   - 必填字段 label 加红色 `*`;
   - 通配字段 label 加"~"前缀(可空=ALL) + placeholder "(通配=ALL,留空)" 灰色提示;
2. **保存前预提示**: 在 Edit Dialog 底部加 `<el-alert type="info">此规则匹配: {{ summaryMatch }},如以下 X 个交易</el-alert>`;
3. **Step Wizard**: 1) 主体 + 方向 → 2) 维度(对手/产品/币种,都是 ALL 可跳过)→ 3) 默认账户 → 4) 优先级 + 状态;
4. **必填 vs 非必填字段组分组**(Steps 或折叠面板)。

**对标**: SAP TRM Routing Rule Editor 步骤化。
**工作量**: M(标签 + 提示 1-2h,Wizard 1 天)
**优先级**: **P1**

---

### 2.7 [P1] 表格列宽在 1440px 视口下"默认账户" 列被截断

**文件**: `DefaultBankAccountRuleList.vue:68-70`

**问题**:
```vue
<el-table-column prop="bankAccountName" label="默认账户" min-width="180">
```
- 11 个字段 × 平均 120px + 操作列 220px + 选择列 50px = ~1480px,**1440px 视口下横向滚动**;
- 用户最关心的"默认账户"列用了 `min-width=180`,但前面 5 个固定列(序号/规则编号/对手方/金融产品/方向)宽度没控制好。

**建议**:
1. 列宽度量:
   - `序号 50` + `规则编号 140` + `对手方 100` + `金融产品 140` + `方向 80` + `币种 70` + `默认账户 220` + `优先级 80` + `状态 80` + `生效日 100` + `操作 200` = 1260px,**1440px 视口正好不滚动**;
2. 抽出 `<BankAccountRuleTable>` 组件,封装最优列宽;
3. 同时加 `density` 三档切换(comfortable/compact/ultra);
4. 添加 `show-overflow-tooltip` 到所有文本列,避免截断难读。

**对标**: Bloomberg AIM Reference Rule List 列宽优化。
**工作量**: XS(0.5-1h)
**优先级**: **P1**

---

### 2.8 [P2] 优先级、状态 inline-edit 无 Undo / Toast Persistence

**问题**:
- 用户连续改 5 条规则优先级,每次改完 el-message 闪一下没了,**实际改了什么记不住**;
- 误改后无 Undo 机制。

**建议**:
1. 攒批 5s 后统一提示:
```js
const recentChanges = ref([])
const handlePriorityChange = async (row, val) => {
  recentChanges.value.push({ ruleNumber: row.ruleNumber, oldPriority: row.priority, newPriority: val, ts: Date.now() })
  scheduleFlushChanges()
}
const scheduleFlushChanges = () => {
  if (flushTimer) clearTimeout(flushTimer)
  flushTimer = setTimeout(() => {
    // 一次性调用 batch update
    ElNotification({
      title: '批量操作',
      message: `${recentChanges.length} 条规则已更新`,
      duration: 0,
      position: 'bottom-right',
      actions: [
        { text: '撤销', onClick: revertChanges },
        { text: '关闭', onClick: () => {} }
      ]
    })
  }, 5000)
}
```
2. **可撤销的 UndoStack** 用 `useUndo` composable(类似 VSCode)。

**对标**: Bloomberg AIM inline edit 撤销。
**工作量**: S(2-3h)
**优先级**: **P2**

---

### 2.9 [P2] 审计历史 display field 用 `<pre>` JSON 不渲染,需要"复制"按钮

**文件**: `DefaultBankAccountRuleList.vue:213-222`

**问题**:
```vue
<pre v-if="item.oldValue" style="color: #f56c6c; font-size: 12px">{{ item.oldValue }}</pre>
<pre v-if="item.newValue" style="color: #67c23a; font-size: 12px">{{ item.newValue }}</pre>
```
- 后端 `oldValue/newValue` 是 JSON 字符串,直接 `<pre>` 显示是**单调字符**,没有字段名高亮、对比;
- 没有"复制"按钮(实际场景: 用户想复制变更到 Confluence / Slack);
- 用户得手动 ⌘+A 才能选,容易错选到其它字段。

**建议**:
1. **格式化为可对比表格**:
```vue
<el-descriptions :column="2" :size="'small'" border>
  <el-descriptions-item label="优先级">{{ item.diff.priority?.from || '-' }} → {{ item.diff.priority?.to }}</el-descriptions-item>
  <el-descriptions-item label="状态">{{ item.diff.status?.from || '-' }} → {{ item.diff.status?.to }}</el-descriptions-item>
  ...
</el-descriptions>
```
2. **复制按钮**:
```vue
<el-button size="small" link @click="copyChange(item)">复制变更</el-button>
```
3. **Diff 算法**: 用 `jsdiff` 计算出 user-friendly diff(类似 Git diff);

**对标**: Murex Audit Log View。
**工作量**: S(2-3h)
**优先级**: **P2**

---

### 2.10 [P2] "批量删除 / 批量启停"按钮虽然 selection 列存在,但无 handler

**文件**: `DefaultBankAccountRuleList.vue:47`(selection 列存在)

**问题**:
- 列表已有 `<el-table-column type="selection">`,但**完全没有 batch 操作 handler**(grep "handleBatchEnable / handleBatchDelete" 全空);
- 高频操作(季度清理 50 条已停用旧规则)只能一条条点。

**建议**:
- 顶部 batch toolbar(在 basedata 列表改进的 2.4 也提了,这里专注于规则页):
```vue
<div v-if="selectedRows.length" class="batch-toolbar">
  已选 {{ selectedRows.length }} 项
  <el-button @click="batchEnable(true)">批量启用</el-button>
  <el-button @click="batchEnable(false)">批量停用</el-button>
  <el-button type="danger" @click="batchDelete">批量删除</el-button>
  <el-button link @click="clearSelection">清空</el-button>
</div>
```
- 后端提供 `POST /api/v1/default-bank-account-rules/batch-enable` / `batch-delete`(在 v1.1 11 端点基础上扩展);
- 删除前 refCount 校验,若有任何一条被引用,**阻塞整批删除**。

**对标**: Murex 多选批量操作通用做法。
**工作量**: S(2-3h)
**优先级**: **P2**

---

### 2.11 [P2] 4 个下拉 + 1 个关键字筛选平铺,可分多步过滤

**文件**: `DefaultBankAccountRuleList.vue:5-41`

**问题**:
- 6 个筛选条件一字排开,占 100-120px 视口高度;
- 用户场景: 我就想"按主体查 Active 规则",4 个筛选 (主体/对手方/产品/币种) 用不到。

**建议**:
- 同 `basedata-pages-ux-improvements.md` 的 2.1 — 但**优先级保持必须选主体**的设计(因为是 multi-tenant 必要):
```vue
<el-form-item label="管理主体" required>
  <el-select v-model="queryForm.managementEntityId" placeholder="主体" clearable filterable>...</el-select>
</el-form-item>
<el-form-item label="状态">
  <el-radio-group v-model="queryForm.status" @change="handleQuery">
    <el-radio-button label="">全部</el-radio-button>
    <el-radio-button label="Active">启用</el-radio-button>
    <el-radio-button label="Inactive">停用</el-radio-button>
  </el-radio-group>
</el-form-item>
<!-- 高级条件折叠 -->
<el-collapse>
  <el-collapse-item title="高级筛选(对手方 / 产品 / 币种 / 关键字)">
    ...其它筛选项
  </el-collapse-item>
</el-collapse>
```
- 高级条件用 localStorage 记忆"用户上次展开的状态"。

**对标**: Murex 折叠高级筛选。
**工作量**: XS(0.5-1h,公共 hook)
**优先级**: **P2**

---

### 2.12 [P2] "🌙 同时 启动状态/优先级 双 inline-edit" 易致冲突

**文件**: `DefaultBankAccountRuleList.vue:71-82`

**问题**:
```vue
<el-input-number v-model="row.priority" ... @change="(val) => handlePriorityChange(row, val)" />
...
<el-switch v-model="row.status" :active-value="'Active'" :inactive-value="'Inactive'"
  @change="(val) => handleStatusChange(row, val)" />
```
- 用户**快速**切换一条规则的 status 时,**同时**改 priority,后端 409 概率激增;
- 每次 inline-edit 触发**完整 GET + UPDATE**,不是 diff API;
- 网络抖动时用户体验差(见 2.4)。

**建议**:
1. **批量更新**:用户编辑 N 条后,Toolbar 出现"保存所有变更"按钮,统一 commit;
2. **diff 接口**:`POST /api/v1/default-bank-account-rules/{id}/patch` 接受 `{patch: {priority: ...}, version: ...}`,乐观锁;
3. **loading state**: 提交中给 row 加 loading 高亮(`el-table :row-class-name`)。

**对标**: Bloomberg AIM inline edit batch pattern。
**工作量**: M(后端 + 前端 ~1-2 天)
**优先级**: **P2**

---

### 2.13 [P2] 复制规则时 `description` 字段被 `[复制]` 前缀覆盖原始说明

**文件**: `DefaultBankAccountRuleList.vue:438`

**问题**:
```js
description: `[复制] ${res.data.description || ''}`
```
- 用户场景: 复制一条"USD SPOT 默认收账账户" 改为 "EUR SPOT 默认收账账户",复制后 description 变成 `[复制] USD SPOT 默认收账账户`;
- 应该复制时**清空 description**,让用户重新填写(避免误用)。

**建议**:
```js
description: ''  // 清空,让用户重新写
```
- 复制后 Edit Dialog 自动 focus 到 description 输入框(`autofocus`)。

**对标**: Murex Copy Rule 默认清空 description。
**工作量**: XS(0.5h)
**优先级**: **P2**

---

### 2.14 [P2] 与"审批规则 / 工作流模板"两个规则页面规范不统一

**文件**:
- `web/src/views/approval/ApprovalRuleList.vue`
- `web/src/views/approval/WorkflowTemplate.vue`

**问题**:
- 三个"规则类"页面(DefaultBankAccountRule + ApprovalRule + WorkflowTemplate)各自独立 UI,**没有共享的"规则框架"组件**;
- 通用能力**:rule-list-shell.vue** (Filter/Table/KPI/批量工具条) + **rule-detail-shell.vue**(key-info + Tabs:基本信息/命中/审计/影响);
- 每加一条规则类型都要重新写一遍。

**建议**:
- 提取 `web/src/components/rule/` 目录:
  - `RuleListShell.vue` — Filter Card + Batch Toolbar + Table + KPI Banner;
  - `RuleDetailShell.vue` — KeyInfoBar + Tabs(基本信息/历史/审计/影响);
- 让这三个规则页都基于此,规则业务逻辑独立注入 props;
- 这套组件可类比"BI Dashboard Shell"。

**对标**: FIS Quantum Routing Rule Builder。
**工作量**: L(2-3 天)
**优先级**: **P2**(技术债,但利于后续规则扩展)

---

### 2.15 [P2] 编辑对话框宽度 720px 在小屏不够,Draft 自动保存缺失

**文件**: `DefaultBankAccountRuleList.vue:108`

**问题**:
- `el-dialog width="720px"` 在 1280px 屏幕下适合,但 1024px 屏幕会顶到边;
- 表单改了 5 个字段后,误点 X,**直接丢失修改无提示**(与详情页同款问题);
- 无 Draft 自动保存(刷新页面丢失全部工作)。

**建议**:
1. **响应式宽度**: `:width="isLargeScreen ? '720px' : '95vw'"`;
2. **dirty 拦截**: 同 basedata 2.13,加 `<FormDrawer>` 或 `<FormDialog>` 包装;
3. **LocalStorage 草稿**: 表单字段 watch 后 `localStorage.openTms.draft.bankRule = ...`,自动恢复("上次未保存的草稿"提示)。

**对标**: Bloomberg AIM Draft Auto-Save。
**工作量**: S(2-3h)
**优先级**: **P2**

---

## 三、横向对比表(规则页)

| 维度 | DefaultBankAccountRule | ApprovalRule | WorkflowTemplate | 业界对标 |
|------|----------------------|-------------|-------------------|----------|
| **11 端点 v1.1** | ✅ | ⚠️ 不完整 | ⚠️ 不完整 | Murex 11~13 端点 |
| **详情页** | ❌ (仅 Edit Dialog) | ❌ | ❌ | Murex 必备 |
| **优先级 inline edit** | ✅ | ❌ | ❌ | Bloomberg 通用 |
| **状态 inline switch** | ✅ | ❌ | ❌ | Bloomberg 通用 |
| **审计历史 Timeline** | ✅ el-timeline | ⚠️ 不一致 | ⚠️ 不一致 | Murex 标配 |
| **影响范围 (refCount)** | ✅ 删除前提示 | ❌ | ❌ | SAP 必备 |
| **并发控制 409 → 弹窗刷新** | ✅ | ❌ | ❌ | Murex 标配 |
| **批量启停 / 删除** | ❌ 选中列无 handler | ❌ | ❌ | 通用 |
| **匹配预览 / Hit 测试** | ❌ | ❌ | ❌ | Murex 必备 |
| **KPI Banner** | ❌ | ❌ | ❌ | FIS Quantum |
| **拖拽排序 (priority)** | ❌ | ❌ | ❌ | Murex 标配 |
| **草稿自动保存** | ❌ | ❌ | ❌ | 通用 |
| **Diff 友好显示** | ⚠️ `<pre>` JSON | ❌ | ❌ | Murex 必备 |
| **Dirty 拦截** | ❌ | ❌ | ❌ | 通用 |

---

## 四、P0 紧急修复 (本周)

1. **2.1** 移除硬阻拦,自动预填当前用户默认主体 (XS)
2. **2.2** 新增规则详情页(含 hit 历史 / 影响范围 Tab) (L,3 天)
3. **2.3** KPI Banner: 总规则 / 启用 / 停用 / 本月命中 (M,1 天)

## 五、P1 重要优化 (1 个月内)

4. **2.4** 优先级 inline-edit 加乐观更新 + 批量调整 + 拖拽 (M,2 天)
5. **2.5** 规则可视化匹配预览(`<RuleMatchAnalyzer>`) (L,2-3 天)
6. **2.6** 必填 vs 通配字段视觉区分 + Step Wizard (M,1 天)
7. **2.7** 表格列宽度量优化 + show-overflow-tooltip (XS,0.5h)

## 六、P2 锦上添花

8. **2.8** Undo / 批量操作 toast persistence (S,2-3h)
9. **2.9** 审计历史 diff 友好化 + 复制按钮 (S,2-3h)
10. **2.10** 批量删除 + refCount 阻塞校验 (S,2-3h)
11. **2.11** 筛选器折叠 + 记忆 (XS,1h)
12. **2.12** diff 接口 + batch commit + loading state (M,1-2 天)
13. **2.13** 复制规则清空 description (XS,0.5h)
14. **2.14** 抽取 `RuleListShell` / `RuleDetailShell` 共享组件 (L,2-3 天)
15. **2.15** 编辑对话框响应式 + dirty 拦截 + Draft 保存 (S,2-3h)

---

## 七、总结

**DefaultBankAccountRule UX 现状**: **B+ 级**,v1.1 已经基本打通"双方向 + 优先级 + 状态 + 审计 + 并发"五大场景,但**详情页缺失**是最大短板 — 所有"为什么这条规则被命中" / "过去改了什么" / "覆盖哪些交易"都查不到。

**待改进重点**:
1. **架构补完** — Detail 页 + KPI Banner + 命中历史(后端需新增 `RuleHitVO` 接口);
2. **交互优化** — 乐观更新 / 批量操作 / 拖拽排序;
3. **统一规范** — 抽取 `RuleListShell` / `RuleDetailShell` 组件,让三类规则页一致。

**对标差距**: 与 Murex MX.3 Routing Rule 主要差"**详情页 + 命中测试**";与 Bloomberg AIM 主要差"**批量操作 + 拖拽**";与 SAP TRM 主要差"**KPI + 影响范围可视化**"。

**建议执行节奏**: 本周收掉 P0(预填 + 详情页 + KPI)→ 本月 P1(乐观更新 + 匹配分析)→ 季度 P2(shell 抽取 + 批量操作)。

---

*UX 改进建议 - v1.0 - 2026-07-10*
