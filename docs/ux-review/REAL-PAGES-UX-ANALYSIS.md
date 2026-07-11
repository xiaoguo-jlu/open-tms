# 真实页面 UX 改进分析报告(基于 9 张实际截图)

> **版本**: v1.0
> **作者**: UX 分析子代理
> **日期**: 2026-07-10
> **审查范围**: 9 张生产环境真实页面截图(基于 `_recon.json` DOM 数据)
> **对标**: FIS Quantum / Murex MX.3 / SAP TRM / Bloomberg AIM / Kyriba
> **不重复**: `basedata-pages-ux-improvements.md` / `dealing-pages-ux-improvements.md` / `rules-pages-ux-improvements.md` 已识别的通用问题
> **本报告聚焦**: 仅基于真实截图观察到的、跨页面共性、必须解决的"首页级"问题

---

## 一、整体评估

**[C+] 综合评级**: 真实页面截图显示 **首屏体验严重缺陷**,3 类页面各有 P0 阻塞:

1. **基础数据页(Basedata)**:筛选器平铺(BankAccount 7 项 / CurrencyPair 3 项 / Subsidiary 3 项)+ BankAccount 缺余额 + 创建时间列宽溢出 + "Total 0" 显示错位
2. **交易页(Dealing)**:FX 列表顶部 v3.2 开发日志 alert(强制灌输)+ FX/AC 列表"管理主体" 列直接显示编码 + AT 列表缺审批按钮入口
3. **规则页(Rules)**:DefaultBankAccountRule 首屏空白 + Total 0 错位(实际 0 行,但 UI 误以为有 bug)
4. **详情页(Detail)**:AcDealDetail / FxDealDetail "扩展信息" 一栏垂直被压缩到 24px 高度,关键字段值不显示
5. **共性**:无面包屑导航 + 无批量操作 + 状态颜色不统一(启用/Active/Inactive 混用)

**对标参考**:
- **FIS Quantum** Trading List:顶部"Pinned Filter + Action Shortcuts",列表上方 KPI Banner。
- **Murex MX.3** Counterparty:列表 "Last Activity" / "Open Positions" 列直接显示业务量。
- **Bloomberg AIM**:Detail 页"面包屑 + Tab + Key Info Bar" 三段式布局。
- **SAP TRM**:任何 list 页默认加载 + 0 行时显示 empty state illustration。

---

## 二、问题清单(10 条)

### 2.1 [P0] DefaultBankAccountRule 首屏空白表(0 行 / 12 列)— 用户认为是 bug

**截图**:`DefaultBankAccountRuleList.png` — 7 个筛选项(管理主体、状态、对手方、金融产品、币种、关键字、)+ 表格 0 行 + Total 0
**问题**:
- 用户首次进入此页面,**看到一张空白表格**,几乎都以为是 bug;
- 顶部 6 个筛选条件一字排开,用户即使想查也不知道先选哪个;
- 与同模块的 BankAccount / Subsidiary(3 筛选项 + 直接展示 7 行数据)对比,**体验断崖式下降**;
- `_recon.json` 显示 `table_row_count: 0`(主代理手工筛掉主体后没查数据)。

**改进建议**:
1. **预填默认主体**:从 `useUserStore().currentUser.defaultMgmtEntityId` 读取并预填 → 直接查数据;
2. **空状态引导**:若用户无默认主体,显示 `<el-empty description="请选择管理主体开始查询">` + "去选择主体" 按钮;
3. **删除"管理主体必填"硬阻拦**(`handleQuery` 中的 `if (!queryForm.managementEntityId)` 改为允许空查询 + 默认主体预填)。

```vue
<!-- 改进后首屏:主体预填 + 表格直接展示 -->
<el-form :inline="true" :model="queryForm">
  <el-form-item label="管理主体">
    <BaseDataPicker v-model="queryForm.managementEntityId" entity="management-entity" placeholder="主体" />
  </el-form-item>
  <el-form-item label="状态">
    <el-radio-group v-model="queryForm.status">
      <el-radio-button label="">全部</el-radio-button>
      <el-radio-button label="Active">启用</el-radio-button>
      <el-radio-button label="Inactive">停用</el-radio-button>
    </el-radio-group>
  </el-form-item>
  ...
</el-form>
```

**对标**: Bloomberg AIM 任何 list 默认带"default user"查询;Murex 列表首屏必填主体但保留 hover 引导。
**工作量**: XS(1h,需后端 user profile 默认主体接口若没有则新增字段)
**优先级**: **P0**(影响所有财务/风控人员的"第一次"体验)

---

### 2.2 [P0] FX 列表顶部"v3.2 开发日志" alert — 用户视角困惑

**截图**:`FxDealList.png` 顶部 alert 文字:`v3.2: DX 创建即生成 3 DealMap + 0/2 Cashflow(NDF 等 RATE_FIX);后端 calculate 联动计算`
**问题**:
- 这是**开发者视角**的内部说明 — "v3.2 / DealMap / Cashflow / NDF / RATE_FIX / calculate" 等技术术语,财务用户**完全不懂**;
- alert `:closable="false"` 不可关闭,用户每次进入页面都被强制灌输;
- 占用列表首屏 56px 高度,挤压表格;
- 与 `AcDealList` 的"基于 v2.0..." 同款问题。

**改进建议**:
1. **彻底删除** — 列表顶部不再显示任何开发说明;
2. 若一定要有,改为"用户视角" 文案,例如:`已配置自动生成现金流,可在详情页 Cashflow Tab 查看`;
3. **可关闭** alert(:closable="true"),用户选择永久隐藏;
4. 帮助图标(`<el-icon><QuestionFilled /></el-icon>`)hover 显示 tooltip,把详细规则放到"业务规则" 链接。

**对标**: Bloomberg AIM / Murex 列表页默认无开发提示。
**工作量**: XS(0.5h,2 个页面)
**优先级**: **P0**(影响 2 个交易页首屏,易删除)

---

### 2.3 [P0] 列表筛选 6 项平铺 — BankAccount 首屏挤出表格

**截图**:`BankAccountList.png` 顶部 6 个筛选条件(关键字、开户银行、币种、账户类型、管理主体、状态)+ Filter Card 高度 ~140px
**问题**:
- 6 个筛选条件一字排开,Filter Card 高度 ~140px,挤压首屏表格;
- 1080p 视口下用户需**滚动一次**才能看到表格头;
- 高频筛选项(关键字、状态)与低频筛选项(开户银行、账户类型、管理主体)混排,无分组;
- 与 `DefaultBankAccountRule` 同款问题(7 项)。

**改进建议**:
```vue
<el-form :inline="true">
  <!-- 1. 永远显示:关键字 + 状态(高频二选) -->
  <el-form-item label="关键字">
    <el-input v-model="query.keyword" placeholder="账户号/名称" clearable />
  </el-form-item>
  <el-form-item label="状态">
    <el-radio-group v-model="query.status">
      <el-radio-button label="">全部</el-radio-button>
      <el-radio-button label="1">启用</el-radio-button>
      <el-radio-button label="0">停用</el-radio-button>
    </el-radio-group>
  </el-form-item>
  
  <!-- 2. 高级筛选折叠 -->
  <el-popover placement="bottom" :width="500" trigger="click">
    <template #reference>
      <el-button :icon="Filter">高级筛选 <el-icon><ArrowDown /></el-icon></el-button>
    </template>
    <el-form-item label="开户银行">...</el-form-item>
    <el-form-item label="币种">...</el-form-item>
    <el-form-item label="账户类型">...</el-form-item>
    <el-form-item label="管理主体">...</el-form-item>
  </el-popover>
  
  <!-- 3. 已选徽标 -->
  <el-tag v-if="query.currency" closable @close="query.currency = ''">{{ currencyMap[query.currency] }}</el-tag>
  
  <el-button type="primary" @click="handleQuery">查询</el-button>
  <el-button @click="handleReset">重置</el-button>
</el-form>
```
- 持久化用户展开状态(`localStorage.openTms.filterExpanded`)。

**对标**: Murex MX.3 Reference Data 一级筛选 1-2 项 + 折叠高级条件;Bloomberg AIM Search Bar 同款做法。
**工作量**: M(每页 ~1h,10 页共 ~2 天,提取公共 hook 后 ~ 半天)
**优先级**: **P0**(影响所有 10 个列表页首屏体验)

---

### 2.4 [P0] BankAccount 缺余额/IBAN/SWIFT 关键列 — 财务对账无法在该页完成

**截图**:`BankAccountList.png` 表格仅 11 列(序号、账户编号、账户名称、开户银行、币种、账户类型、管理主体、状态、创建时间、操作)
**问题**:
- 银行账户管理最关键的字段全部缺失:
  - **账户余额**(财务对账必看)
  - **IBAN / SWIFT Code**(跨境付款必需)
  - **未对账笔数 / 限额**(运营风控)
  - **最近对账时间**
- 用户进此页面,只能看到"这是什么账户",看不到"账户里有多少钱 / 是否可用";
- 财务调度周一早 9 点想查"USD 账户可用余额",要跳到 Cashflow/Valuation 模块。

**改进建议**:
```vue
<el-table-column label="余额" align="right" width="160" sortable>
  <template #default="{ row }">
    <span class="mono amount">{{ formatBalance(row.balance, row.currency) }}</span>
  </template>
</el-table-column>
<el-table-column label="IBAN / SWIFT" min-width="220" show-overflow-tooltip>
  <template #default="{ row }">{{ row.iban || '-' }} <span v-if="row.swift" class="swift">/ {{ row.swift }}</span></template>
</el-table-column>
<el-table-column label="对账时间" width="110" align="center">
  <template #default="{ row }">{{ formatDate(row.lastReconcileAt) }}</template>
</el-table-column>
```
- 配套:后端新增 `BankAccountBalanceVO` 接口(基于 tms_cashflow_t + tms_gl_t 聚合,每 5 分钟缓存);
- 排序/筛选允许按余额排序。

**对标**: FIS Quantum Account List 必有 Current Balance + Last Statement Date;Murex 同款。
**工作量**: L(后端聚合 1 天 + 前端适配 2h)
**优先级**: **P0**(基础数据缺失业务最核心字段,影响日常运营)

---

### 2.5 [P0] FX 列表"管理主体/对手方"用 `el-input-number` + ID — 用户记不住 ID

**截图**:`FxDealList.png` 顶部 `管理主体 [-][ID][+] 对手方 [-][ID][+]`
**问题**:
- 让用户输入 `123`、`456` 等纯数字 ID — 用户**根本记不住** ID;
- 用户场景:"我想查 UBS 的 FX 交易" → 在 UBS 详情里看到 ID=42 → 然后去 FX 列表输入 42,**断点**;
- 与同模块 `AcDealList` / `AtDealList` 的 string 输入行为**不一致**;
- 与 `BaseDataPicker` 已建立的"下拉搜索"规范不符。

**改进建议**:
```vue
<el-form-item label="管理主体">
  <BaseDataPicker v-model="queryForm.managementEntityId" entity="management-entity" placeholder="选择主体" />
</el-form-item>
<el-form-item label="对手方">
  <BaseDataPicker v-model="queryForm.counterpartyId" entity="counterparty" placeholder="选择对手方" />
</el-form-item>
```
- 复用 `web/src/components/picker/BaseDataPicker.vue`(已有);
- 显示已选 name(预填 preloadRow);
- 清空 / 清除 badge。

**对标**: Murex / FIS Quantum / Bloomberg AIM 列表筛选用 Picker 而非 ID 输入。
**工作量**: XS(0.5h)
**优先级**: **P0**(影响用户日常查询流程)

---

### 2.6 [P0] 无批量操作栏 — 选择列存在但无 handler

**截图**:`BankAccountList.png` / `CurrencyPairList.png` / `SubsidiaryList.png` / `DefaultBankAccountRuleList.png` 全部都有 `<el-table-column type="selection">`,**但实际无任何批量操作**
**问题**:
- 表格已经有**选择列**,证明设计意图是支持批量,但**全代码无任何批量操作 handler**(grep `handleBatch` 全空);
- 高频"启用/停用"操作要走完整"抽屉 → 保存 → 关闭 → 回到列表" 4 步,UX 耗时大;
- 风控人员季度清理 50 条已停用规则只能一条条点;
- 删除前无影响笔数提示(虽然 `getReferenceCount` 已实现但只在删除按钮触发)。

**改进建议**:
```vue
<el-table @selection-change="rows => selectedRows = rows">
  <el-table-column type="selection" width="50" />
  ...
</el-table>

<!-- 选中后表格上方出现 BatchBar -->
<div v-if="selectedRows.length" class="batch-bar">
  <span>已选 <b>{{ selectedRows.length }}</b> 项</span>
  <el-button @click="batchEnable(true)">批量启用</el-button>
  <el-button @click="batchEnable(false)">批量停用</el-button>
  <el-button type="danger" @click="batchDelete">批量删除</el-button>
  <el-button link @click="clearSelection">清空</el-button>
</div>
```
- 删除前 refCount 校验,若有被引用条目,弹出 `ElMessageBox.confirm("X 条被 Y 笔交易引用,确认删除?")`;
- 提取公共 `<BatchBar>` 组件,10 个列表页通用。

**对标**: Murex / FIS Quantum 批量操作通用做法。
**工作量**: S(每页 ~1-2h,提取公共组件后 0.5 天)
**优先级**: **P0**(影响所有 9 个 basedata + 3 个 dealing 列表页)

---

### 2.7 [P1] 创建时间列宽溢出(180px ISO 时间戳)— 影响密度

**截图**:`BankAccountList.png` 第 1 行:`2026-07-04T23:01:35.836478...` 显示占 4 行,挤压其他列
**问题**:
- `prop="createdAt"` 直接显示原始 ISO 字符串 `2026-07-04T23:01:35.836478`;
- 列宽 180px,但内容溢出压缩到多行,信息密度差;
- 整个表格无"格式化为 YYYY-MM-DD HH:mm" 的统一处理。

**改进建议**:
```vue
<el-table-column label="创建时间" width="140" align="center">
  <template #default="{ row }">
    <el-tooltip :content="row.createdAt">
      <span>{{ formatDate(row.createdAt) }}</span>
    </el-tooltip>
  </template>
</el-table-column>
```
- `formatDate('2026-07-04T23:01:35.836478')` → `2026-07-04 23:01`;
- 列宽从 180px 降到 140px,腾出 40px 给其他字段。

**对标**: 通用做法,Bloomberg AIM 时间统一格式。
**工作量**: XS(每页 0.5h,提取公共工具函数后)
**优先级**: **P1**(影响所有 9 个列表页,但视觉影响小)

---

### 2.8 [P1] "Total 0" 错位显示(实际 7 行)— Total 数据来源错误

**截图**:`BankAccountList.png` / `SubsidiaryList.png` 分页区域显示 `Total 0` 但表格内显示 7 行数据
**问题**:
- el-pagination 显示 `Total 0`,但表格中实际渲染 7 条记录 — 这是**数据 bug**,用户困惑;
- `_recon.json` 显示 `table_row_count: 7` 但分页 `total=0`;
- 推测原因:后端 `Result.total=0` 但 `Result.data.length=7`(分页响应字段错配);
- 影响所有列表页,**用户认为"系统有问题"**。

**改进建议**:
1. **后端修复**(根本):返回的 `Result.total` 必须等于 `data.length`(分页模式)或 `total = data.length`(全量模式);
2. **前端兜底**(短期):`total = data.length` 当 total === 0 但 data.length > 0 时;
3. **测试用例**:新增 `pageXxx` 接口的 total 字段测试。

**对标**: 通用数据准确性,所有 TMS 系统必备。
**工作量**: XS(后端 0.5h / 前端 0.5h)
**优先级**: **P1**(影响所有列表页用户信任,但 0.5h 修复)

---

### 2.9 [P1] 缺面包屑导航 — 用户无"返回上级"路径

**截图**:全部 9 个页面顶部 header 仅显示"资金管理系统" / "管理员",**无 breadcrumb**
**问题**:
- 用户进入 `DefaultBankAccountRuleList` / `FxDealList` 等深层页面,**无面包屑**,只能点浏览器后退;
- 移动端 / 多页签场景下完全迷失;
- 详情页(AcDealDetail / FxDealDetail)有"返回"按钮,但无路径指示。

**改进建议**:
```vue
<el-breadcrumb separator="/">
  <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
  <el-breadcrumb-item :to="{ path: '/basedata' }">基础数据</el-breadcrumb-item>
  <el-breadcrumb-item>银行账户管理</el-breadcrumb-item>
</el-breadcrumb>
```
- 路由 meta 配置 `breadcrumb: ['基础数据', '银行账户管理']`;
- 详情页:`首页 > 交易管理 > FX 外汇交易 > FX202607100001`;
- 提取 `<BreadcrumbBar>` 公共组件。

**对标**: 任何企业级系统必备(Bloomberg AIM / Murex / SAP 通用做法)。
**工作量**: S(1 天,配置路由 meta + 组件)
**优先级**: **P1**(导航一致性,影响所有 9 个页面)

---

### 2.10 [P1] 详情页 activeTab 默认值不合理 + 关键字段被压缩

**截图**:`FxDealDetail.png` 默认 Tab = `审计信息`,显示"最新 Action / 创建人 / 创建时间 / 更新人..." 等基础元数据,**但用户实际想看 DealMap / Cashflow**;`AcDealDetail.png` 默认 Tab = `DealMap` 但显示"No Data"
**问题**:
- **FX 详情**:用户进入交易详情,**最关心的是 DealMap 和 Cashflow**(业务核心),但默认 Tab 是"审计信息" — **"基本审计信息"在 readonly 模式默认显示,信息价值低,挤掉 DealMap/Cashflow**;
- **AC 详情**:默认 Tab 是 `DealMap`,但因为 `_recon.json` 中 `table_row_count: 0`(空数据),用户啥都看不到,**无任何引导**(无 "无 DealMap,请检查交易状态" 文案);
- "总金额 / 方向 / 交易类型" 关键信息条被压缩到 ~80px 高(只剩 `-` 字段值),**完全无视觉重量**;
- 与 M3 已建立的"一屏布局 UX 原型" 规范不符。

**改进建议**:
1. **FX 默认 Tab 改 `cashflow`**(业务核心):
```js
// FxDealDetail.vue:436
const activeTab = ref('cashflow')  // 由 'basic' 改为 'cashflow'
```
2. **AC 默认 Tab 改 `action`**(显示全生命周期),`dealmap` 改用户偏好可配置;
3. **关键信息条字段值缺失时显示空状态**:
```vue
<div class="key-info-bar">
  <div class="key-item highlight">
    <span class="label">总金额</span>
    <span class="value">-</span>
  </div>
  <div v-if="!detail.dealNumber" class="key-item-empty">
    <el-empty :image-size="60" description="交易未保存,无 DealMap / Cashflow 数据" />
  </div>
</div>
```
4. **空 Tab 文案**:每个 Tab 在 0 行时显示 `<el-empty description="暂无 DealMap 数据,请先保存交易">` + "去保存" 按钮(若 mode === 'new')。

**对标**: SAP TRM / Murex 列出用户偏好 + 默认显示业务核心 Tab。
**工作量**: XS(0.5h,activeTab 改 + 空状态文案)
**优先级**: **P1**(影响所有 3 个详情页首屏)

---

## 三、横向对比表

| 维度 | BankAcct | CurrencyPair | Subsidiary | DefaultBankRule | AcDeal | AtDeal | FxDeal | AcDealDetail | FxDealDetail | 业界对标 |
|------|---------|--------------|-----------|----------------|--------|--------|--------|--------------|--------------|----------|
| **首屏非空** | ✅ 7 行 | ✅ 29 行 | ✅ 7 行 | **❌ 0 行** | ✅ 10 行 | ✅ 3 行 | ✅ 10 行 | n/a | n/a | Murex 必备 |
| **无开发日志 alert** | ✅ | ✅ | ✅ | ✅ | **❌ v2.0** | ✅ | **❌ v3.2** | ⚠️ M1.3 | ✅ | Bloomberg |
| **筛选器折叠** | **❌** 6 项 | ⚠️ 3 项 | ⚠️ 3 项 | **❌** 7 项 | ⚠️ 4 项 | ⚠️ 3 项 | **❌** 6 项 | n/a | n/a | Murex |
| **余额 / IBAN 列** | **❌** | n/a | n/a | n/a | ✅ | ✅ | ✅ | n/a | n/a | FIS Quantum |
| **BaseDataPicker** | ⚠️ | n/a | n/a | n/a | ⚠️ | ⚠️ | **❌ ID 输入** | ✅ | ✅ | Murex 必备 |
| **批量操作** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | n/a | n/a | 必备 |
| **面包屑** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | ⚠️ 返回按钮 | ⚠️ 返回按钮 | 必备 |
| **创建时间格式化** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | **❌** | n/a | n/a | 通用 |
| **Total 准确** | **❌ Total 0 / 7 行** | n/a | **❌ Total 0 / 7 行** | ✅ | **❌ Total 14 / 10 行** | ✅ | **❌ Total 15 / 10 行** | n/a | n/a | 必备 |
| **activeTab 默认** | n/a | n/a | n/a | n/a | n/a | n/a | n/a | ⚠️ DealMap 空 | **❌ 审计信息** | Murex |

---

## 四、P0 紧急修复(本周)

1. **2.1** DefaultBankAccountRule 预填默认主体 + 移除硬阻拦(XS)
2. **2.2** 删除 FX/AC 列表"v2.0 / v3.2" 开发日志 alert(XS)
3. **2.3** BankAccount 等 6 个列表页筛选器折叠(M,公共 hook)
4. **2.4** BankAccount 新增余额 / IBAN / 对账时间列(L)
5. **2.5** FX 列表"管理主体/对手方"改 BaseDataPicker(XS)
6. **2.6** 10 个列表页批量操作栏(S,公共 BatchBar)

## 五、P1 重要优化(本月)

7. **2.7** 创建时间格式化 + 列宽收紧(XS,公共工具函数)
8. **2.8** 修复 Total 0 / 实际有数据的 bug(XS)
9. **2.9** 全局面包屑导航(S,公共组件)
10. **2.10** 详情页 activeTab 默认值 + 关键信息条空状态(XS)

---

## 六、总结

**真实页面 UX 现状**: **C+ 级**水准,与 FIS Quantum / Murex 主要差距在"**首屏引导 + 关键字段缺失 + 批量操作 + 开发日志污染**"。

**与之前 3 份 UX 报告的差异**:
- 之前 3 份报告基于代码(`web/src/views/*.vue`),聚焦**架构补完**(KPI Banner / 详情页 / Master-Detail);
- 本报告基于**真实截图观察**,聚焦**首页级缺陷**(0 行表 / 开发日志 / ID 输入 / Total bug / 无面包屑) — **用户首次进入就遇到的痛点**。

**建议执行节奏**:本周收掉 6 个 P0(2 个 XS / 2 个 M / 1 个 L / 1 个 S)→ 本月 P1 4 个(XS/S 为主)→ 季度再做 KPI / 详情页 / Master-Detail 等架构补完。

---

*UX 改进建议 - v1.0 - 2026-07-10*