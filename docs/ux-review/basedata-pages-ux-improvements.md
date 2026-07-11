# 基础数据页面 UX 改进分析报告

> **版本**: v1.0
> **作者**: UX 分析子代理
> **日期**: 2026-07-10
> **审查范围**: `web/src/views/basedata/*.vue` 共 10 个列表/详情页
> **关键资源**: CLAUDE.md · `M3-AC-AT-FX-页面改进UX审查.md` · 通用 Element Plus 最佳实践
> **不重复范围**: 默认银行账户规则弹框下拉 bug(主代理已修复)、3 个交易详情页布局(`M3-交易详情一屏布局UX原型` 已覆盖)

---

## 一、整体评估

**[B] 综合评级**: 基础数据页整体"够用但糙",遵循通用后台模板(Filter Card + Table Card + Drawer + Pagination),但缺乏统一的:
- 列表字段密度优化(冗余/缺失)
- 跨页协作的统一交互(批量操作 / 全局快捷键)
- 状态/徽章视觉一致性

**主要问题模式**:
1. **筛选区字段平铺、缺少分组**(货币列表 2 个、银行账户 6 个、国家列表 0 个) - 信息架构松散
2. **Drawer 表单 480-560px 宽度统一死板**(对 Subsidiary 12 字段列表实际不够用)
3. **新增和编辑用同一 Drawer,无分步/分组**,字段多的页面(Subsidiary 12 字段)需要垂直滚动到底
4. **表格内状态/操作杂糅**,无 summary row,无固定列优先级,1.5k+ 行数据无虚拟滚动
5. **错误处理仅 `console.error`**,无 UI 提示

**对标参考**:
- **FIS Quantum** Data Management: 列表页提供 **Multi-tab 视图**(Active/Deleted/All),支持 **inline edit** 和 **batch action**
- **Murex MX.3** Counterparty/Reference Data: 详情页用 **Master-Detail Layout**(左 30% 元数据,右 70% Tabs:Usage/Audit/Linked Entities)
- **Bloomberg AIM** Reference Data: **density 切换**(comfortable / compact / ultra-compact 三档)
- **SAP TRM** Bank Directory: 状态变更弹 **确认对话框**,显示影响笔数

---

## 二、详细改进列表

### 2.1 [P0] 跨页面筛选器分组 + 默认折叠 (适用所有列表页)

**涉及文件**:
- `BankAccountList.vue:5-40`(6 个筛选项平铺)
- `CounterpartyList.vue:5-32`(4 项)
- `DefaultBankAccountRuleList.vue:5-41`(6 项)
- `HolidayList.vue:5-19`(3 项)
- 其余 6 个(Subsidiary / Currency / Instrument 等)同款问题

**问题**:
1. **筛选项平铺**导致 Filter Card 高度 +200px,挤压首屏表格。BankAccountList 在 1080p 视口默认甚至需要向下滚动才能看到表格头。
2. **高频筛选项(关键字、状态)** 和低频筛选项(币种、类型、国家)混排,用户首次打开页面要扫一眼才能确定。
3. **无折叠机制**,即使"我有 6 个查询条件",用户也只想用 1-2 个。
4. CountryList/HolidayList 仅 0-3 个筛选项,但仍套用同样的 Filter Card 样式,视觉噪音。

**场景**:
- 财务月初想筛选"USD + 活期 + 主体 A" - 现在要点 4 个下拉、每个下拉要选;
- 业务只看"启用"状态的银行账户,但还要先点"启用"筛选。

**建议**:
```vue
<el-card class="filter-card">
  <el-form :inline="true" :model="queryForm">
    <!-- 1. 永远显示:关键字(主查询) + 状态(最常用二选) -->
    <el-form-item label="关键字">
      <el-input v-model="queryForm.keyword" placeholder="编码/名称" clearable />
    </el-form-item>
    <el-form-item label="状态">
      <el-select v-model="queryForm.status" placeholder="全部" clearable>
        <el-option label="启用" value="1" /><el-option label="停用" value="0" />
      </el-select>
    </el-form-item>
    
    <!-- 2. 折叠区:次要条件用 collapsible -->
    <el-collapse v-model="filterExpanded">
      <el-collapse-item title="高级筛选" name="advanced">
        <el-form-item label="币种">...</el-form-item>
        <el-form-item label="账户类型">...</el-form-item>
        <el-form-item label="管理主体">...</el-form-item>
        <el-form-item label="开户银行">...</el-form-item>
      </el-collapse-item>
    </el-collapse>
    
    <!-- 3. 操作按钮永远显示 -->
    <el-form-item>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleAdd">新增</el-button>
    </el-form-item>
  </el-form>
</el-card>
```
**配套改造**:
- 新增一个 useFilterExpansion() composable,持久化用户偏好(`localStorage.openTms.filterExpanded = ['BankAccountList:advanced']`)
- CountryList / Currency / TraderList 这种只有 1-2 个筛选项的页面,直接去掉 Filter Card,只保留顶部 Toolbar("查询行内 + 状态 inline badge filter")

**验收**:
- Filter Card 默认 ≤ 56px 高,首屏表格全可见;
- 点击"高级筛选"展开,使用后状态被记忆。

**对标**: Murex MX.3 Reference Data 一级筛选 1-2 项 + 折叠高级条件;Bloomberg AIM Search Bar 同款做法。
**工作量**: M (每页 ~1h,共 10 页,提取公共 hook 后 ~ 半天)
**优先级**: **P0**(影响所有 10 个列表页首屏体验)

---

### 2.2 [P0] 默认银行账户规则"管理主体必填"硬阻拦 — UX 反模式

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
- 这是后端约束,但前端 **强制必选才能查** 是**反模式**:用户进入页面看到**空表格**,以为是 bug,实际是"我没选主体"
- 与同一文件 `Counterparty` / `Instrument` / `Status` 这些**可空筛选项**不一致
- 用户首次进页面行为:`打开 → 看到空白 → 困惑`,这是金融系统的**入门级 bad UX**

**建议**:
1. **首次进页面自动请求当前用户所属管理主体**(M3 已有 `currentUser` 全局信息)并预填,然后查全部;
2. **或者**(更好的方案):**默认加载所有规则**(分页 50 条),并显示当前主体规则**高亮** + **其它主体灰显**;
3. **或者**(最小改动):把必填警告改成"ElMessage.info 提示性语句"而非"warning",并自动填入默认主体。

```js
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await pageDefaultBankAccountRule(queryForm)
    ...
  } catch (e) {
    // 不要硬性阻拦
  }
}

// onMounted 时:
const user = useCurrentUser()
queryForm.managementEntityId = user.defaultMgmtEntityId
fetchData()
```

**对标**: Bloomberg AIM 大多数 list 页首次进入直接展示默认数据,无强制筛选。
**工作量**: XS (1h)
**优先级**: **P0**

---

### 2.3 [P0] BankAccount 列表缺失"币对/IBAN/SWIFT/余额" 关键列

**文件**: `BankAccountList.vue:46-79`

**问题**:
- 表头仅有 11 列(序号、账户编号、账户名称、开户银行、币种、账户类型、管理主体、状态、创建时间、操作);
- **缺失对银行账户管理最关键的字段**:
  - **账户余额**(财务对账必看)
  - **IBAN / SWIFT Code**(跨境付款必需)
  - **账户号段是否已使用 / 未对账笔数**(运营风控)
  - **币对/限额**
- 用户进入此页面,只能看到"这是什么账户",看不到"账户里有多少钱 / 是否可用"。

**场景**:
- 财务调度:周一早 9 点想知道"USD 账户有可用余额吗" → 现在要到 Cashflow/Valuation 模块查,无法在本页面获取一手余额。

**建议**:
```vue
<el-table-column prop="balance" label="余额" align="right" width="140">
  <template #default="{ row }">
    <span class="mono">{{ formatBalance(row.balance, row.currency) }}</span>
  </template>
</el-table-column>
<el-table-column prop="iban" label="IBAN / SWIFT" min-width="200" />
```
- 配套:新增一个 `BankAccountBalanceVO` 接口,后端基于 tms_cashflow_t 和 tms_gl_t 实时聚合(每 5 分钟缓存)。
- 排序/筛选允许按余额排序(财务人员重要需求)。

**对标**: FIS Quantum Account List 必有 Current Balance 列 + Last Statement Date;Murex 同款。
**工作量**: L(后端新增聚合接口 ~1 天 + 前端适配 ~1-2h)
**优先级**: **P0**(基础数据缺失业务最核心字段,影响日常运营)

---

### 2.4 [P1] 列表行操作缺"批量"能力 + 状态切换无确认

**涉及**: `BankAccount / Currency / Subsidiary / Counterparty / Instrument / Trader / ManagementEntity / Holiday / CurrencyPair` 全部 9 页

**问题**:
1. **每个列表都有 `<el-table-column type="selection">`**(`BankAccountList.vue:46` 等 9 处),证明设计意图是**支持批量**,但**全代码无任何批量操作 handler**(grep "handleBatch" 全空);
2. 状态用 el-tag 静态显示,必须进入编辑弹框才能改,且**无修改确认**(直接覆盖);
3. 高频"启用/停用"操作,要走完整 `抽屉→保存→关闭→回到列表` 4 步,UX 耗时大。

**建议**:每个列表页批量 toolbar(顶部表格上方):
```vue
<div v-if="selectedRows.length" class="batch-toolbar">
  已选 {{ selectedRows.length }} 项
  <el-button @click="batchEnable">批量启用</el-button>
  <el-button @click="batchDisable">批量停用</el-button>
  <el-button type="danger" @click="batchDelete">批量删除</el-button>
  <el-button link @click="clearSelection">清空</el-button>
</div>
```
- 状态列改为 `el-switch` (`DefaultBankAccountRuleList.vue:79-82` 已经是 `el-switch`,确认一下其它页面对齐,采纳这条)
- **删除前确认 + 显示影响**(参考 `defaultBankAccountRule` 已有 `getReferenceCount`):银行账户/币种/对手方删除前显示"被 N 笔交易引用"。

**对标**: Murex `multi-select + batch action`,FIS Quantum 同款。
**工作量**: S(每页 ~1-2h,提取公共 BatchBar 组件后)
**优先级**: **P1**(用户高频操作,价值高)

---

### 2.5 [P1] Drawer 表单固定宽度 480-560px,缺少响应式适配

**涉及**: `BankAccountList.vue:94` / `CurrencyPairList.vue:65` / `SubsidiaryList.vue:60` / `CounterpartyList.vue:83` 等所有列表的弹框

**问题**:
1. **SubsidiaryList 有 12 字段的表单**(`SubsidiaryList.vue:60` size=560px),在 560px 宽度下每个字段 label-width=120px + input-width=剩余 ~400px,12 个字段堆叠滚动到底需 4-5 秒。
2. 字段多的页面(Instrument 11 字段 / Subsidiary 12 字段)用 drawer modal 而不是 **Step Wizard** 或 **Tabbed Dialog**,违反 M3 已建立的"分步/分组"规范。
3. drawer size **不做响应式**:1440px 大屏和 1024px 小屏都强吃 480-560px,大屏空旷、小屏拥挤。

**建议**:
1. **Subsidiary**: 抽出 `<TabbedDialog>` 组件,3 个 tab(基础信息 / 工商信息 / 联系信息)。
2. **所有 Drawer**:响应式 — 大屏(>1440px)用 fullscreen "side panel"(`size="80%"`),中小屏保持 560px。
3. **关键字段靠顶部**:Subsidiary 中 `code/name/enName/parentCode/managementEntityCode` 这些标识字段一定放第一步。

```vue
<el-drawer
  v-model="drawerVisible"
  :title="drawerTitle"
  direction="rtl"
  :size="isLargeScreen ? '80%' : '560px'"
  destroy-on-close
>
```

**对标**: Murex 详情页 master-detail + tab;FIS Quantum 抽屉 自适应。
**工作量**: M(每页 ~1.5h,加 TabbedDialog 组件 ~2h)
**优先级**: **P1**

---

### 2.6 [P1] 错误处理仅 `console.error`,用户无 UI 反馈

**涉及**: 所有 basedata 列表的 catch 块 — `BankAccountList.vue:251-255`、`CurrencyPairList.vue:154-158`、`SubsidiaryList.vue:172-176`、`CountryList.vue:135-139` 等共 ~10 处

**问题**:
```js
} catch (error) {
  console.error('Failed to fetch data:', error)
}
```
- 网络异常 / 后端 down / 401 过期都 **仅 console**,UI 仍是空白表格(loading 关闭后的空白),用户以为"这个分类没数据"。
- `<el-alert>` 错误提示没有,表格 `<el-empty>` 没有针对"网络错误" vs "无数据" 的区分。

**建议**:
```js
} catch (error) {
  if (error?.response?.status === 401) {
    ElMessage.error('登录已过期,请重新登录')
    // 触发全局 logout
  } else {
    ElMessage.error(`加载失败: ${error?.message || '请稍后重试'}`)
  }
  tableData.value = []
}
```
**更进一步**: 加一个 `LoadError` 状态:
```vue
<el-empty v-if="loadError" description="数据加载失败">
  <el-button type="primary" @click="fetchData">重试</el-button>
</el-empty>
<el-empty v-else-if="tableData.length === 0 && !loading" description="暂无数据" />
<el-table v-else :data="tableData" ...> ... </el-table>
```
**对标**: Bloomberg AIM 通用做法。
**工作量**: XS(0.5h / 页,加公共 hook 后 ~1h)
**优先级**: **P1**

---

### 2.7 [P1] BankAccount/Counterparty 状态标签与规则页不一致

**对比**:
- BankAccount / Currency / Subsidiary / Counterparty / Instrument / Trader 等:`status === '1' ? success : danger`(字符串 "1"/"0")
- `DefaultBankAccountRule`:`status === 'Active' ? success : warning`(字符串 "Active"/"Inactive")
- Holiday:`isAdjacent === '1' ? warning : info`
- AcDealDetail:`New`/`Approved`/`Canceled` → 不同颜色

**问题**:
- **同一页面内同样的"启用/停用"语义,不同基础数据用不同枚举值**(部分 "1"/"0",部分 "Active"/"Inactive",部分 "Enabled"/"Disabled");
- **状态颜色映射不统一**:同一"启用",有时 `success`(绿),有时 `primary`(蓝);
- **没有全局"<StatusTag>" 组件**,每页自定义,容易漂移。

**建议**:
1. **抽出公共 `<StatusTag type="base | rule | deal">` 组件**,统一视觉:
```vue
<template>
  <el-tag :type="type" :effect="effect" :size="size">
    <slot>{{ label }}</slot>
  </el-tag>
</template>
<script setup>
const props = defineProps({ status: String, category: { type: String, default: 'base' } })
const map = computed(() => {
  if (props.category === 'base') return { enabled: ['1','Active','Enabled'].includes(props.status), label: { 1:'启用', '0':'停用', Active:'启用', Inactive:'停用' }[props.status], type: { 启用: 'success', 停用: 'danger' } }
  ...
})
</script>
```
2. 在所有页面替换 `<el-tag :type="row.status === '1' ? 'success' : 'danger'">` 为 `<StatusTag :status="row.status" />`。

**对标**: FIS Quantum 中央 status taxonomy。
**工作量**: S(1 天抽组件 + 替换 10 页)
**优先级**: **P1**(用户体验一致性)

---

### 2.8 [P2] Holiday / Country 等小页面套用大页面模板,视觉/重量不对等

**文件**: `HolidayList.vue`、`CountryList.vue` 等

**问题**:
- Holiday 列表筛选项只有 2 个(年份、国家) + 表格 6 列,**仍用 el-card + el-form :inline + el-table + el-pagination 的"满套"模板**;
- 内容 < 600px 高度,但表单 + 表格 + 分页一起撑出 ~750px 视口,**视觉头重脚轻**;
- "创建时间"列每次都占 180px,Holiday 这种一年也就 30 行,字段密度太低。

**建议**:
1. 对小页面(字段 ≤ 7 列):
   - 去掉外层 el-card,直接"filter inline 一行 + table 一屏"(`density: compact; max-height: calc(100vh - 200px)`)
   - "创建时间" 列宽收窄到 110px(用 `format(row.createdAt, 'YYYY-MM-DD')`);
   - 分页放表格右上角(`layout="prev, pager, next"`),不独占一行 16px margin。
2. 抽出 `useCompactListLayout()` composable。

**对标**: Bloomberg AIM 列表 density 切换。
**工作量**: S(1 天,每页 1h)
**优先级**: **P2**

---

### 2.9 [P2] 货币/币种对/SubjectType 等存在重复硬编码选项,易漂移

**重复出现的硬编码**:
- BankAccount 中"账户类型":`CURRENT / TERM / MARGIN`(`BankAccountList.vue:21-23` 与 `BankAccountList.vue:115-117` 重复)
- BankAccount 中"状态":`1 / 0`
- Counterparty 中"类型":`BANK / ENTERPRISE / BROKER / INSURANCE` 出现 2 次(`CounterpartyList.vue:11-13` 与 `94-99`)
- Holiday 中"是否调休":`1 / 0`
- CurrencyPair 中"强势币种":空字符串
- CountryList 中"是否欧盟" / CurrencyList 中无 type 列表

**问题**:
- 同一枚举在多个文件各写一份,后端新增一个枚举值需要改 N 处。
- `getTypeLabel` 等函数在每个页面独立写,容易写错。

**建议**:
1. 抽出 `web/src/constants/basedata.ts` 或 `web/src/api/constants.ts`:
```ts
export const ACCOUNT_TYPE_OPTIONS = [
  { label: '活期', value: 'CURRENT' },
  { label: '定期', value: 'TERM' },
  { label: '保证金', value: 'MARGIN' }
]
export const COUNTERPARTY_TYPE_OPTIONS = [...]
export const ACCOUNT_TYPE_MAP = Object.fromEntries(ACCOUNT_TYPE_OPTIONS.map(o => [o.value, o.label]))
```
2. 配合 `useDict()` composable 从后端 `tms_dict_t` 拉取(若字典系统已实现)或本地常量。
3. 同时把状态 `1`/`0` / `Active`/`Inactive` 等也映射进来,**统一文案**。

**对标**: Bloomberg AIM 使用 PRTU 系统统一字典。
**工作量**: M(0.5-1 天,含后端字典接口若有)
**优先级**: **P2**(技术债,但降低维护成本)

---

### 2.10 [P2] 国家、币种、节假日缺"批量导入/导出 CSV"

**涉及**: `CountryList`、`CurrencyList`、`HolidayList`、`TraderList`、`CounterpartyList`

**问题**:
- M3 已支持单条 CRUD,但**没有批量导入** — 实际业务场景中财务初始化 200+ 国家假日表 / 100+ 货币 / 50+ 交易员,一条条录入极慢;
- 也没有 **CSV 导出** — 风控审计或对外汇报需要。

**建议**:
1. 顶部 toolbar 加"导入 / 导出"两个按钮:
```vue
<el-button :icon="Upload" @click="importDialogVisible = true">导入</el-button>
<el-button :icon="Download" @click="exportToCsv">导出</el-button>
```
2. `importDialogVisible` 用 `<el-upload>` + 模板下载(template.csv)和错误行高亮预览。
3. `exportToCsv` 调用后端 `GET /api/v1/.../export` 即可(若后端无对应接口,前端根据当前 `tableData` 拼字符串 export)。

**对标**: 任何企业级 TMS 必备(参考 FIS / Murex / Kyriba)。
**工作量**: M(每页 ~2h,加导入解析 ~1 天)
**优先级**: **P2**(不是 blocking,但生产环境强烈建议)

---

### 2.11 [P2] Subsidiary / Counterparty 详情缺 `Master-Detail` 视图

**文件**: `CounterpartyList.vue:62-68` 有"账户"按钮跳 `CounterpartyAccountList`,但**没有详情页**

**问题**:
- 对手方点击"详情"无法直接展开所有关联数据(如交易历史、评级变化、账户列表、信用额度等),只能跳到子页面;
- 列表中"内部评级 / 外部评级"字段在 1280px 视口下被截断,但又不提供详情页查看;
- 财务/风控人员复盘一个对手方时,需要跳 5 个页拼信息。

**建议**:
- 给 `CounterpartyList.vue` / `SubsidiaryList.vue` / `BankAccountList.vue` 增加"详情" 入口,新详情页用 M3 一屏布局原型(关键信息条 + Tabs: 基础/账户/历史交易/评级变化/审计);
- 在 Drawer 中也可提供 "查看详情 / 子项" 链接。

**对标**: Murex MX.3 Counterparty Master View 必带 6 个 tab。
**工作量**: L(2-3 天建详情页,与 M3 详情模板一致)
**优先级**: **P2**

---

### 2.12 [P2] 列表首屏缺 Summary Row (汇总行)

**涉及**: 所有列表的表格 — BankAccount / Currency / Subsidiary / FX / AT / AC

**问题**:
- 财务进入银行账户列表,首要 KPI 是"USD 总余额多少"、"多少个 USD 账户";
- 当前页面表格无任何聚合 / 汇总行(看一行行金额累加);
- 缺少"今日活跃账户数"、"本月新增" 等 KPI Banner。

**建议**:
1. 表格加 `<el-table-column type="summary">` 或 `<el-table summary-method>` + `<template #summary>`:
```vue
<el-table :data="tableData" :summary-method="getSummaries" show-summary>
...
<template #summary="{ columns, data }">
  <el-table-column v-for="col in columns" :key="col.prop">
    <template #default="{ row }">...</template>
  </el-table-column>
</template>
```
2. 列表上方加 KPI 卡片行(Total Count / Active Count / Total Balance / Avg Balance):
```vue
<div class="kpi-row">
  <KpiCard label="账户总数" :value="kpi.total" />
  <KpiCard label="启用" :value="kpi.active" type="success" />
  <KpiCard label="停用" :value="kpi.inactive" type="danger" />
  <KpiCard label="总余额" :value="kpi.totalBalance" type="primary" />
</div>
```

**对标**: FIS Quantum Account List 顶部 KPI 卡片。
**工作量**: M (每页 ~2h + 公共 KpiCard 组件)
**优先级**: **P2**

---

### 2.13 [P2] 修改无 dirty 检查 + 离开抽屉前未拦截

**文件**: 所有 Drawer 的 handleAdd / handleEdit 流程(`BankAccountList.vue:273-284` 等)

**问题**:
- Drawer 打开后,用户修改字段,点 X 或 Esc 或外部关闭,**直接丢失修改**无任何提示;
- 与交易详情页 `handleCancel` 中 isDirty 检查相比,这是**体验不对等** — 详情页有拦截,基础数据页没有。

**建议**: 抽出 `<FormDrawer>` 组件:
```vue
<FormDrawer
  v-model="drawerVisible"
  :title="drawerTitle"
  :form-ref="formRef"
  :form-data="formData"
  :rules="rules"
  :fields="fieldsConfig"
  @before-close="handleBeforeClose"
  @submit="handleSubmit"
>
```
- `handleBeforeClose` 中 if dirty 显示 `ElMessageBox.confirm("放弃当前修改?")`。

**对标**: Bloomberg AIM 通用做法。
**工作量**: S(每页 ~1h,公共组件抽取 0.5 天)
**优先级**: **P2**

---

### 2.14 [P2] "创建时间" 列展示原始 ISO,缺格式化

**文件**: 9 个列表页表格 — `BankAccountList.vue:73`、`CurrencyPairList.vue:44`、`SubsidiaryList.vue:39` 等

**问题**:
- `prop="createdAt"` 直接显示原始 ISO 字符串 `2026-07-04T22:53:35.532651`(从 UX 截图 `mgmt-entity` 看到);
- 占用 180px 宽度,但首部有时间戳,信息密度差。

**建议**:
- 列表中所有 createdAt / updatedAt 字段用 `<template #default="{ row }">`:
```vue
<el-table-column label="创建时间" width="110" align="center">
  <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
</el-table-column>
```
- `formatDate('2026-07-04T22:53:35')` → `2026-07-04 22:53`(常用格式)。
- 列宽从 180px 降到 110px。

**对标**: 通用做法。
**工作量**: XS(0.5h / 页)
**优先级**: **P2**(很多页都有,统一收益)

---

### 2.15 [P2] Cashflow 提示文案 / `<el-alert>` 内容长期停留在 v2.0 信息

**文件**:
- `AcDealList.vue:33-37`:`基于 v2.0:创建后自动生成 DealMap + Cashflow;修改软删旧 DealMap + 新建;删除级联软删`
- `FxDealList.vue:37-41`:`v3.2: DX 创建即生成 3 DealMap + 0/2 Cashflow(NDF 等 RATE_FIX)`

**问题**:
- 这是**开发日志式说明**,不应出现在生产用户眼前 — 出现 "v2.0 / v3.2" 字段会让财务用户困惑;
- alert 占用列表首屏 56px 高度且不可关闭(:closable="false")。

**建议**:
1. 这种业务规则说明应该移到"详情页右键 → 关于 / Help",而不是顶在列表页;
2. 或者作为可关闭 alert,且文案改为用户视角:"已配置自动生成现金流,可展开 Cashflow Tab 查看";
3. 列表中移除,降低首屏噪点。

**对标**: Bloomberg AIM 列表页默认无开发提示。
**工作量**: XS(0.5h)
**优先级**: **P2**(只影响 2 个交易页,但视觉污染明显)

---

### 2.16 [P2] 缺键盘快捷键(Enter 触发查询 / Esc 关闭 Drawer)

**文件**: 部分页面已支持 `@keyup.enter="handleQuery"`(`BankAccountList.vue:6`),但非所有页一致

**问题**:
- BankAccount / Currency / Subsidiary / Instrument / Counterparty / ManagementEntity 都支持 Enter 查询;
- 但是 CurrencyList / CountryList / HolidayList / TraderList 等**部分缺失**;
- Drawer 的 Esc 关闭是 Element Plus 默认行为,但**未提示**用户可用;
- 列表表格无 ←/→ 翻页快捷键。

**建议**:
1. 在所有 el-form-item 的 el-input 上加 `@keyup.enter="handleQuery"`(确保一致);
2. 在 Drawer footer 加按钮:"Esc 取消 / ⌘+Enter 保存" 灰色提示;
3. 列表页加 `keydown.left/right` 翻页(`@page-prev` / `@page-next`)。

**对标**: Bloomberg AIM 全键盘操作。
**工作量**: XS(0.5h / 页)
**优先级**: **P2**

---

### 2.17 [P2] Subsidiary 12 字段表单缺关键字自动联想

**文件**: `SubsidiaryList.vue:74-75` `managementEntityCode` 等字段是手填字符串

**问题**:
- 用户手动输入 managementEntityCode 但**没有联想**;
- 错字导致规则 / 报表找不到主体。

**建议**:
- 把这些字段从字符串输入改为 `<BaseDataPicker entity="management-entity">`,用户点击搜索下拉框选择,**避免错字**;
- 已经在 AcDeal / FxDeal 详情页用了 Picker,统一为基础数据。

**对标**: SAP TRM 强制 ID 字段都从 picker 选。
**工作量**: S(每字段 ~0.5h,共 4 个字段)
**优先级**: **P2**

---

## 三、横向对比表

| 维度 | BankAcct | CurrencyPair | Subsidiary | Currency | Counterparty | Country | Holiday | Trader | MgmtEntity | Instrument | DefaultBankRule | 业界对标 |
|------|---------|--------------|-----------|----------|--------------|---------|---------|--------|------------|-----------|----------------|----------|
| 筛选器折叠 | ❌ | ❌ | ❌ | ❌ | ❌ | n/a | ❌ | ❌ | ❌ | ❌ | ❌ | Murex 折叠 |
| 默认主体必填 | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | ⚠️ 强制 | 选填 |
| KPI Banner | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | FIS Quantum |
| 余额/关键字段 | ⚠️ 缺 | ✅ | ✅ | ✅ | ⚠️ 评级无来源 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 必备 |
| 批量操作 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Murex 必备 |
| 错误 UI 反馈 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ⚠️ 部分 | 通用 |
| 状态标签统一 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 中央 tag |
| 详情 Master-Detail | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Murex 标配 |
| Drawer 响应式 | ❌ 固定 480 | ❌ | ❌ 560 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | FIS 自适应 |
| dirty 拦截 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Bloomberg |
| 批量导入/导出 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 必备 |
| Summary Row | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 通用 |

---

## 四、优先级实施路线图

### 本周 (P0 - 必修)
1. **2.1** Filter Card 折叠高级筛选(所有 10 页,公共 hook 半价 M)
2. **2.2** DefaultBankAccountRule 移除"管理主体必填"硬阻拦 (XS)
3. **2.3** BankAccount 列表新增余额 / IBAN 列(后端 + 前端适配,L)

### 本月 (P1 - 重要)
4. **2.4** 列表批量操作公共 BatchBar + 状态 switch 替换 (S)
5. **2.5** Subsidiary / Instrument 抽出 TabbedDialog + Drawer 响应式 (M)
6. **2.6** 网络错误 UI 反馈 + 重试按钮 (XS)
7. **2.7** 抽出公共 `<StatusTag>` 替换所有硬编码 (S)

### 本季度 (P2 - 锦上添花)
8. **2.8** Country/Holiday 小页面紧凑布局 (S)
9. **2.9** 字典常量收口到 `constants/basedata.ts` (M)
10. **2.10** 批量导入/导出 CSV (M)
11. **2.11** Counterparty / Subsidiary / BankAccount Master-Detail 详情 (L)
12. **2.12** 顶部 KPI Banner + 表格 summary row (M)
13. **2.13** `<FormDrawer>` 公共组件 + dirty 拦截 (S)
14. **2.14** 时间字段统一格式化 + 列宽收紧 (XS)
15. **2.15** 移除列表页开发日志式 alert (XS)
16. **2.16** 键盘快捷键全键盘一致性 (XS)
17. **2.17** 手工 ID 字段改 BaseDataPicker (S)

---

## 五、总结

**基于数据页面 UX 现状**: **B 级**水准,**首屏体验**因为筛选器平铺而被严重拖累,**批量操作**是最大功能缺口,**错误反馈**是技术债细节。

**待改进重点**:
1. **架构** — 抽出公共组件 `<FormDrawer>` / `<StatusTag>` / `<BatchBar>` / `<KpiRow>` 解决复用;
2. **细节一致性** — 时间格式化、状态标签、键盘快捷键;
3. **关键字段缺失** — BankAccount 余额 / IBAN / 引用数。

**与业界差距**: 与 FIS Quantum 主要差在"**实时业务数据聚合**(余额 / 引用数 / KPI)+ **批量操作**";与 Murex 主要差在"**Master-Detail 视图** + **批量导入导出**"。

**建议执行节奏**: 公共组件抽取 → 批量替换 → 引入业务聚合 → Master-Detail,自下而上,每个 phase 留缓冲。

---

*UX 改进建议 - v1.0 - 2026-07-10*
