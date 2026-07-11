# M3 - 交易详情一屏布局 UX 原型

> **版本**: v1.0
> **作者**: UX 子代理
> **日期**: 2026-07-05
> **目标**: 实现在 1920×1080 一屏内 (默认视口) 看到交易所有核心信息,dealmap/cashflow/gl entry 仍以子页签形式存在。

---

## 一、设计原则

### 1.1 核心原则
| 原则 | 说明 |
|------|------|
| **一屏可见 (One-Screen-First)** | 1080p 视口下,关键信息 + Tabs 顶部无需滚动即可看见 |
| **信息密度优先** | 关键数据(编号/金额/状态/日期)顶部集中展示,降低视觉负担 |
| **三段式层次** | 工具条 / 摘要 / Tabs,各占合理高度,层次分明 |
| **响应式退化** | 中屏/小屏下自动从 2 列降为 1 列,Tab 允许换行 |
| **模式切换平滑** | readonly/edit/new/copy 切换仅替换主信息区,关键信息条与 Tabs 保持一致 |

### 1.2 关键约束
- 不修改后端代码 / API / router 配置
- 保持 4 模式(new/copy/edit/readonly)切换的现有逻辑
- 保持 ModeBadge 组件
- 编辑模式时,关键信息条 + 主信息区 切换为表单

---

## 二、目标视口

| 档位 | 分辨率 | 占比 | 关键策略 |
|------|--------|------|----------|
| **大屏 (Desktop L)** | ≥1600px | 主要目标 | 5 列横排 / 2 列主区 / 4 tab 横向 |
| **中屏 (Desktop M)** | 1200-1599px | 兼容 | 4 列横排 / 2 列主区 / 4 tab 横向 (压缩字号) |
| **小屏 (Laptop)** | 960-1199px | 兼容 | 3 列横排 / 2 列主区 / 2 tab 一行 |
| **极小屏 (Tablet)** | <960px | 退化 | 2 列横排 / 1 列主区 / 1 tab 滚动 |

**默认设计视口**: 1920×1080 (Full HD), 16:9。

---

## 三、布局规范 (3 段式)

### 3.1 整体结构

```
┌──────────────────────────────────────────────────────────────────┐
│ [← 返回] FX 交易详情 - FX202607050001  [复制] [编辑] [审批]  工具条: 48px
├──────────────────────────────────────────────────────────────────┤
│ 关键信息条 (高度 80px) - 4 列横排:                                │
│   [交易编号] | [产品类型] | [状态] | [总金额]  | [到期日]          │
│   字体 16px bold                                                    │
├──────────────────────────────────────────────────────────────────┤
│ 主信息区 (高度 480px, 60% 一屏) - 2 列:                            │
│  左列 (50%):  交易要素                                              │
│  右列 (50%):  金额 & 日期                                          │
├──────────────────────────────────────────────────────────────────┤
│ 子页签区 (高度 360px, 40% 一屏) - Tabs:                            │
│  [DealMap] [Cashflow] [GL Entry] [Action] [Approval]            │
│  Tab 内容: max-height 320px, 内部滚动                              │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 各区高度分配 (合计 1080px)

```
┌─────────────────────────────────────────────────┐
│ Browser Title Bar + Tabs (浏览器): 80px         │
├─────────────────────────────────────────────────┤
│ Layout Sidebar (侧栏,若存在): 0-200px            │
├─────────────────────────────────────────────────┤
│ Breadcrumb (面包屑): 28px                       │
│ Action Bar Top (工具条): 48px                   │
│ Padding (margin): 24px                          │
│ Key Info Bar (关键信息条): 80px                  │
│ Main Info Area (主信息区): 480px                │
│ Tabs Section (子页签区): 360px                  │
│ (Mode 编辑模式下底部操作条): 64px               │
├─────────────────────────────────────────────────┤
│ 合计: ~1080px (Full HD 一屏)                    │
└─────────────────────────────────────────────────┘
```

### 3.3 三段详细规范

#### A. 顶部工具条 (48px)
- 高度 48px,flex justify-between
- 左侧:返回按钮 + 标题(交易类型 - 编号)
- 右侧:操作按钮(复制/编辑/审批/删除) readonly 模式
- 编辑模式:仅"取消"按钮 + 右侧面包屑区显示 ModeBadge
- 背景 #fff,底边 1px solid #ebeef5
- 固定 (affix position=top)

#### B. 关键信息条 (80px) — 核心创新点
```
[交易编号: FX202607050001]  [SPOT 标签]  [Active 标签]  [总金额: ¥1,000.00]  [到期: 2026-10-04]
```
- 高度 80px,padding 16px 24px
- 背景渐变 (淡蓝: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%))
- 内部 flex 横排,每个 item 占 1 列
- 字段字体: 16px bold 等宽数字
- 状态/产品类型用 el-tag 彩色
- 4 列响应式:
  - 大屏: 5 列横排
  - 中屏: 4 列横排 (隐藏"到期")
  - 小屏: 3 列横排 (隐藏"产品类型")
  - 极小: 2 列横排 (仅显示编号 + 状态)

#### C. 主信息区 (480px) — 双列布局
- 高度固定 480px (含 padding)
- 内部 2 列 grid: 1fr 1fr,gap 16px
- **左列 (交易要素)**: 主体/对手/产品/币种对 (el-descriptions 2列紧凑)
- **右列 (金额 & 日期)**: 卖/买/汇率/金额/日期 (金额字体大 18-20px, 数字用 mono 字体)

#### D. 子页签区 (360px)
- 高度固定 360px
- el-tabs,tab 头部 36px
- Tab 内容: max-height 320px,内部滚动
- 表格紧凑: row-height 36px, font 12px
- 默认显示前 5 行, "展开全部" 按钮 → 全屏 modal

---

## 四、字段密度表

### 4.1 FX 交易

| 区域 | 字段 | 字号 | 优先级 |
|------|------|------|--------|
| **关键信息条** | 交易编号 | 16px bold | P0 |
| | 产品类型 (SPOT/FWD/NDF) | 14px tag | P0 |
| | 状态 (Active/New) | 14px tag | P0 |
| | 总金额 (卖出+买入) | 16px mono bold | P0 |
| | 到期日 | 14px | P1 |
| **主信息区-左列** | 管理主体 | 14px | P0 |
| | 交易对手 | 14px | P0 |
| | 交易员 | 14px | P1 |
| | 金融工具 | 14px | P1 |
| | 币种对 | 14px | P0 |
| **主信息区-右列** | 卖出金额 + 币种 | 18px mono bold | P0 |
| | 买入金额 + 币种 | 18px mono bold | P0 |
| | 成交汇率 | 16px mono | P0 |
| | 市场汇率 / 点差 | 13px | P1 |
| | 交易日 / 交割日 | 13px | P0 |
| | 到期日 / 期限 | 13px | P1 |
| **Tab-审计** | 创建人/时间/版本/备注 | 12px | P2 |

### 4.2 AC 交易

| 区域 | 字段 | 字号 | 优先级 |
|------|------|------|--------|
| **关键信息条** | 交易编号 | 16px bold | P0 |
| | 方向 (流入/流出) | 14px tag | P0 |
| | 状态 | 14px tag | P0 |
| | 金额 + 币种 | 16px mono bold | P0 |
| | 起息日 | 14px | P0 |
| **主信息区-左列** | 管理主体 | 14px | P0 |
| | 交易对手 | 14px | P0 |
| | 金融工具 | 14px | P1 |
| | 本方账户 | 14px | P0 |
| **主信息区-右列** | 金额 + 币种 | 18px mono bold | P0 |
| | 交易日期 / 起息日 | 13px | P0 |
| | 支付方式 | 13px | P1 |
| | 操作人 | 13px | P2 |

### 4.3 AT 交易

| 区域 | 字段 | 字号 | 优先级 |
|------|------|------|--------|
| **关键信息条** | 交易编号 | 16px bold | P0 |
| | 转账类型 | 14px tag | P0 |
| | 状态 | 14px tag | P0 |
| | 转账金额 + 币种 | 16px mono bold | P0 |
| | 起息日 | 14px | P0 |
| **主信息区-左列** | 管理主体 | 14px | P0 |
| | 转账类型 | 14px | P0 |
| | 付出方账户 | 14px | P0 |
| | 收入方账户 | 14px | P0 |
| **主信息区-右列** | 转账金额 | 18px mono bold | P0 |
| | 目标金额 | 18px mono bold | P0 |
| | 汇率 (恒为 1) | 13px | P2 |
| | 起息日 / 支付方式 | 13px | P0 |
| | 操作人 / 资金用途 | 13px | P2 |

---

## 五、Tab 区域设计

### 5.1 Tab 列表

| Tab | 内容 | 高度策略 |
|-----|------|----------|
| **审计信息** | 创建/更新/版本/备注 | 紧凑 el-descriptions 2列 |
| **DealMap** | DealMap 列表 (时间线/表格) | 表格默认显示 5 行,展开全部 |
| **Cashflow** | Cashflow 列表 | 表格默认显示 5 行,展开全部 |
| **GL Entry** | 会计分录 (M1.3 待开发) | 描述 + 规则码 + 笔数 |
| **Action** | Action 历史 | 表格默认显示 5 行,展开全部 |
| **Approval** (AC/AT) | 审批记录 | 表格 |
| **镜像版本** (AT) | Image 历史 | 表格 |

### 5.2 Tab 高度策略

```
┌────────────────────────────────────────────┐
│ Tab Headers (高度 36px)                     │
├────────────────────────────────────────────┤
│                                            │
│ Tab Content Area (max-height: 320px)       │
│ ┌────────────────────────────────────┐   │
│ │ 表格默认显示 5 行                     │   │
│ │ row-height: 36px                     │   │
│ │ font: 12px                           │   │
│ │                                     │   │
│ │ ...                                 │   │
│ └────────────────────────────────────┘   │
│ [展开全部 ↑↓] 按钮                          │
└────────────────────────────────────────────┘
```

### 5.3 展开策略
- 默认状态:Tab 内容区显示前 5 行
- "展开全部" 按钮:打开全屏 el-dialog 显示完整数据
- 行 hover 高亮 + 链接到详情

---

## 六、组件规范

### 6.1 关键信息条 (KeyInfoBar)

```vue
<template>
  <div class="key-info-bar">
    <div class="key-item">
      <span class="key-label">交易编号</span>
      <span class="key-value mono">{{ dealNumber }}</span>
    </div>
    <div class="key-item">
      <el-tag type="success" effect="dark">{{ productType }}</el-tag>
    </div>
    <div class="key-item">
      <el-tag :type="getStatusType(status)">{{ statusLabel }}</el-tag>
    </div>
    <div class="key-item highlight">
      <span class="key-label">总金额</span>
      <span class="key-value mono lg">{{ totalAmount }}</span>
    </div>
    <div class="key-item">
      <span class="key-label">到期</span>
      <span class="key-value">{{ maturityDate }}</span>
    </div>
  </div>
</template>
```

**CSS 关键样式**:
- 高度: 80px
- 渐变背景: `linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%)`
- 字段: `.key-value.mono.lg` → `font-family: 'JetBrains Mono'; font-size: 18px; font-weight: 700;`

### 6.2 数字等宽字体

```css
.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
.amount-lg { font-size: 20px; font-weight: 700; }
```

### 6.3 表格紧凑行

```css
.compact-table :deep(.el-table__row) { height: 36px; }
.compact-table :deep(.el-table__cell) { padding: 4px 0; font-size: 12px; }
```

### 6.4 Tab 紧凑

```css
.compact-tabs :deep(.el-tabs__header) { margin: 0 0 8px 0; }
.compact-tabs :deep(.el-tabs__item) { padding: 0 12px; height: 32px; line-height: 32px; font-size: 13px; }
.compact-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }
.compact-tabs :deep(.el-tab-pane) { max-height: 320px; overflow-y: auto; }
```

---

## 七、响应式适配

### 7.1 断点规则

| 断点 | 宽度 | 关键信息条 | 主信息区 | Tab 区 |
|------|------|-----------|---------|--------|
| 大屏 | ≥1600px | 5 列横排 | 2 列 | 4 tab 横向 |
| 中屏 | 1200-1599px | 4 列 (隐藏到期) | 2 列 | 4 tab 横向 (压缩) |
| 小屏 | 960-1199px | 3 列 (隐藏类型/到期) | 2 列 | 2 tab 一行 |
| 极小 | <960px | 2 列 (编号 + 状态) | 1 列 | 1 tab + 滚动 |

### 7.2 CSS 媒体查询

```css
@media (max-width: 1599px) {
  .key-info-bar .key-item:nth-child(5) { display: none; }
}
@media (max-width: 1199px) {
  .key-info-bar .key-item:nth-child(2),
  .key-info-bar .key-item:nth-child(5) { display: none; }
  .main-info-area { grid-template-columns: 1fr; }
}
@media (max-width: 959px) {
  .key-info-bar .key-item { flex: 1 1 50%; }
  .compact-tabs :deep(.el-tabs__item) { padding: 0 8px; font-size: 12px; }
}
```

---

## 八、修改的文件清单

1. **F:\code\opencode\opentrm\web\src\views\dealing\FxDealDetail.vue** — 整体重写
2. **F:\code\opencode\opentrm\web\src\views\dealing\AcDealDetail.vue** — 整体重写
3. **F:\code\opencode\opentrm\web\src\views\dealing\AtDealDetail.vue** — 整体重写

---

## 九、关键设计决策 (5-7 条)

1. **三段式布局**: 工具条 (48px) / 关键信息条 (80px) / 主信息区 (480px) / Tab 区 (360px) = 1080p 一屏总和。
2. **关键信息条渐变高亮**: 使用淡蓝渐变背景突出核心数据,数字使用 JetBrains Mono 等宽字体 16-18px。
3. **Tab 高度固定 360px**: 默认显示 5 行,提供"展开全部"按钮打开全屏 dialog。
4. **响应式断点**: 大屏 5 列 → 中屏 4 列 → 小屏 3 列 → 极小 2 列。
5. **模式切换平滑**: readonly 显示摘要,edit/new/copy 显示表单,关键信息条与 Tab 区不变。
6. **CSS 优化**: 表格行高 36px,Tab 头部 32px,关闭多余 padding/margin,使用 :deep() 调整 element-plus 默认样式。

---

## 十、后续建议 (3 条)

1. **可考虑抽出 `<DealDetailLayout>` 通用组件**: 三个详情页布局逻辑高度相似,后续可抽取统一组件。
2. **关键信息条可加入状态徽章动画**: 如 Active 状态加呼吸灯,提升视觉反馈。
3. **Tab 区可支持拖拽排序与固定**: 高频 Tab (DealMap/Cashflow) 可允许用户固定到首位。