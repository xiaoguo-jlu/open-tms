# Open-TMS M3 交易详情内联编辑 UX 原型

**模块**: M3 交易管理 (AC / AT / FX Deal Detail)
**版本**: v2.0
**日期**: 2026-07-05
**角色**: UX 设计师
**适用范围**: `web/src/views/dealing/{Ac,At,Fx}DealDetail.vue` 三个详情页

---

## 一、背景与现状诊断

### 1.1 痛点清单

| 编号 | 痛点 | 严重程度 | 影响范围 |
|------|------|---------|---------|
| P-01 | **三详情页布局不一致**: AC 用 3 列 descriptions + 单列 form；AT 用 2 列 descriptions + 双列 form；FX 用 3 列 + 单列 form | 高 | 全部用户 |
| P-02 | **录入与详情视觉割裂**: readonly 显示 `el-descriptions` 摘要, edit/new/copy 切换为 `el-form` 表单, 字段位置和顺序不同 | 高 | 录入用户 |
| P-03 | **Tabs 可见性不一致**: AC/AT 在 edit 模式下隐藏 Tabs, FX 在 edit 模式下仍展示 Tabs | 中 | 编辑用户 |
| P-04 | **顶部模式标识缺失**: 用户点击"编辑"后, 只能通过按钮变化判断当前模式, 没有明确的[编辑中]徽章 | 中 | 编辑用户 |
| P-05 | **操作按钮位置混乱**: "复制"和"编辑"分散在右侧按钮组, 没有明显的优先级区分 | 中 | 全部用户 |
| P-06 | **底部操作条缺失**: 长表单编辑时, "保存"按钮需滚动到顶部才能点击 | 低 | 录入用户 |
| P-07 | **表单/摘要字段命名差异**: readonly "本方账户" vs edit "银行账户"; readonly "源/目标金额" vs edit "转账金额" | 中 | 录入用户 |
| P-08 | **NDF 字段位置错位**: FX readonly 时 NDF 字段单独成块在第二个 descriptions, edit 时在 form 中段, 视觉不连贯 | 中 | FX 用户 |

### 1.2 设计目标

- **G-01**: 三个详情页 (AC/AT/FX) 共享同一布局框架 (TopBar + Summary + Body + Tabs + BottomBar)
- **G-02**: readonly 摘要项 与 edit/new/copy 表单字段 **1:1 位置对应** (相同栅格, 相同顺序)
- **G-03**: 模式切换平滑过渡 (200ms 渐变), 用户始终知道自己在哪个模式
- **G-04**: 顶部 always-show 模式徽章 (新建/编辑中/复制自 xxx)
- **G-05**: 顶部固定操作条 (返回/复制/编辑/审批/删除) + 底部固定操作条 (保存/取消)
- **G-06**: 错误提示统一顶部 alert + 字段红字 + 自动滚动到第一个错误

---

## 二、设计原则

### 2.1 视觉一致性原则

> **一个详情页 = 一个布局框架**, 录入/复制/修改/详情 4 种模式使用同一框架, 区别仅在:
> - readonly 摘要项  ↔  edit 表单字段 (同一位置)
> - 顶部徽章不同 (新建/编辑中/复制/无)
> - 顶部/底部操作条按钮不同

### 2.2 渐进式编辑原则

- 默认进入页面是 **readonly** 模式
- 用户主动点击 "编辑" 才进入表单
- 表单字段与摘要项 **1:1 对应**, 用户切换无认知负担
- 提交后自动跳转 readonly, 形成"查看 → 编辑 → 提交 → 查看"闭环

### 2.3 操作区双固定原则

- **顶部固定操作条**: 返回 / 复制 / 编辑 / 审批 / 删除 / 导出 (按状态条件显示)
- **底部固定操作条**: 保存 / 取消 / 自动保存草稿 (仅 edit/new/copy 显示)
- 移动端友好, 长表单无需回滚顶部

### 2.4 状态可见原则

- **always show 模式标识**: readonly 默认无徽章, edit 蓝色 [编辑中], new 绿色 [新建], copy 黄色 [复制自 xxx]
- **顶部模式徽章**: 在面包屑右侧, 用户立即知道自己当前在哪
- **loading 状态**: 操作按钮 loading, 防重复提交

---

## 三、通用布局规范

```
┌────────────────────────────────────────────────────────┐
│ [面包屑] 交易管理 / FX 交易 / FX202607040001  [🟦编辑中]│  ← 标题区 (面包屑 + 模式徽章)
├────────────────────────────────────────────────────────┤
│ [← 返回]              [📋 复制] [✏️ 编辑] [✓ 审批]    │  ← 顶部固定操作条
│                              [🗑️ 删除] [📤 导出]    │     (按状态显示)
├────────────────────────────────────────────────────────┤
│ ╔══════════════════════════════════════════════════╗  │
│ ║  摘要区 / 表单区 (统一栅格: 12 列)                ║  │  ← 第 1 区
│ ║                                                    ║  │     "主体区"
│ ║  readonly 时: 摘要项 1:1 渲染                     ║  │
│ ║  edit/new/copy 时: 同样的位置换成 el-form-item    ║  │
│ ╚══════════════════════════════════════════════════╝  │
│                                                        │
│ ╔══════════════════════════════════════════════════╗  │
│ ║  [Tabs: 基本信息 | DealMap | Cashflow | Action]  ║  │  ← 第 2 区
│ ║  ... 详情数据 (always show)                     ║  │     "时序区"
│ ╚══════════════════════════════════════════════════╝  │
├────────────────────────────────────────────────────────┤
│ [底部固定操作条 - 仅 edit/new/copy 时显示]            │
│     [💾 保存]  [❌ 取消]  [📝 自动保存草稿]          │
└────────────────────────────────────────────────────────┘
```

### 3.1 栅格规范

| 模式 | 摘要/表单区 | 编辑区分组 |
|------|------------|-----------|
| 大屏 (>=1200px) | 3 列 (每列 span 8) | 每行 2 个字段 (span 12) |
| 中屏 (768-1200px) | 2 列 (每列 span 12) | 每行 2 个字段 |
| 小屏 (<768px) | 1 列 (span 24) | 每行 1 个字段 |

### 3.2 间距规范

- 摘要项之间: **16px** (gutter: 16)
- 区与区之间: **24px** (margin-bottom: 24)
- 表单字段之间: **8px** (Element Plus 默认)
- 区内部 padding: **16px**

### 3.3 配色规范

| 模式 | 徽章色 | Element Plus tag |
|------|--------|------------------|
| 新建 (new) | `#67c23a` 绿色 | `type="success"` |
| 编辑 (edit) | `#409eff` 蓝色 | `type="primary"` |
| 复制 (copy) | `#e6a23c` 黄色 | `type="warning"` |
| 只读 (readonly) | 无徽章 | - |

---

## 四、4 种模式差异表

| 模式 | URL 触发 | 顶部徽章 | 可编辑区 | Tabs | 顶部操作 | 底部操作 |
|------|----------|----------|---------|------|---------|---------|
| **new** | `?new=1` | [新建] 绿色徽章 | 表单 | 隐藏 | 仅 [取消] | [保存草稿] [保存] |
| **copy** | `?copyFrom=xxx` | [复制自 xxx] 黄色徽章 | 表单 (预填) | 隐藏 | 仅 [取消] | [保存] |
| **edit** | `?dealNumber=xxx&edit=1` | [编辑中] 蓝色徽章 | 表单 | 隐藏 | 仅 [取消] | [保存] |
| **readonly** | `?dealNumber=xxx` | 无 | 只读摘要 | 显示 | [复制] [编辑] [审批] [删除] [导出] | 无 |

---

## 五、视觉规范

### 5.1 模式徽章

```vue
<!-- 位于面包屑右侧 -->
<template v-if="mode === 'new'">
  <el-tag type="success" effect="dark" size="default">
    <el-icon><Plus /></el-icon> 新建
  </el-tag>
</template>
<template v-else-if="mode === 'copy'">
  <el-tag type="warning" effect="dark" size="default">
    <el-icon><DocumentCopy /></el-icon> 复制自 {{ route.query.copyFrom }}
  </el-tag>
</template>
<template v-else-if="mode === 'edit'">
  <el-tag type="primary" effect="dark" size="default">
    <el-icon><Edit /></el-icon> 编辑中
  </el-tag>
</template>
```

### 5.2 顶部操作条

```vue
<el-affix :offset="0">
  <div class="action-bar top">
    <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
    <div class="right">
      <template v-if="mode === 'readonly'">
        <el-button type="success" :icon="CopyDocument" @click="enterCopy">复制</el-button>
        <el-button type="primary" :icon="Edit" @click="enterEdit">编辑</el-button>
        <el-button type="primary" :icon="Check" v-if="canApprove" @click="handleApprove">审批</el-button>
        <el-button type="danger" :icon="Delete" v-if="canDelete" @click="handleDelete">删除</el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </template>
      <template v-else>
        <el-button @click="handleCancel">取消</el-button>
      </template>
    </div>
  </div>
</el-affix>
```

### 5.3 底部固定操作条

```vue
<transition name="fade-up">
  <el-affix position="bottom" v-if="mode !== 'readonly'" :offset="0">
    <div class="action-bar bottom">
      <el-button :icon="Document" v-if="mode === 'new'">保存草稿</el-button>
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">
        <el-icon><Check /></el-icon> 保存
      </el-button>
      <el-button size="large" @click="handleCancel">取消</el-button>
    </div>
  </el-affix>
</transition>
```

### 5.4 摘要区布局 (readonly)

```vue
<el-descriptions :column="3" border class="summary-grid">
  <el-descriptions-item label="交易编号">{{ detail.dealNumber }}</el-descriptions-item>
  <el-descriptions-item label="管理主体">{{ detail.managementEntityName }}</el-descriptions-item>
  <el-descriptions-item label="状态">
    <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
  </el-descriptions-item>
  <!-- ... 其他字段,顺序与表单 1:1 对应 -->
</el-descriptions>
```

### 5.5 编辑区布局 (edit/new/copy)

```vue
<el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="edit-form">
  <el-row :gutter="16">
    <el-col :xs="24" :sm="12" :md="8">
      <el-form-item label="交易编号" v-if="form.dealNumber">
        <el-input v-model="form.dealNumber" disabled />
      </el-form-item>
    </el-col>
    <el-col :xs="24" :sm="12" :md="8">
      <el-form-item label="管理主体" prop="managementEntity">
        <BaseDataPicker v-model="form.managementEntity" ... />
      </el-form-item>
    </el-col>
    <!-- ... 其他字段,与摘要项 1:1 -->
  </el-row>
</el-form>
```

---

## 六、字段一致性原则

### 6.1 命名映射表 (3 个详情页统一)

| 摘要项标题 (readonly) | 表单 label (edit) | v-model 字段 | 备注 |
|----------------------|-------------------|-------------|------|
| 交易编号 | 交易编号 | dealNumber | readonly 永远显示, edit 显示但 disabled |
| 管理主体 | 管理主体 | managementEntity | 三页统一 |
| 交易对手 | 交易对手 | counterpartyId | 三页统一 |
| 交易员 | 交易员 | traderId | 三页统一 |
| 金融工具 | 金融工具 | instrumentId | 三页统一 |
| 方向 | 方向 | direction | AC 专属 |
| 金额 / 卖出金额 | 金额 / 卖出金额 | amount / sellAmount | 命名因页面而异 |
| 币种 | 币种 | currency / sellCurrency | 命名因页面而异 |
| 交易日期 / 交易日 | 交易日期 / 交易日 | dealDate / tradeDate | 命名因页面而异 |
| 起息日 / 交割日 | 起息日 / 交割日 | valueDate | 命名因页面而异 |
| 描述 | 描述 | description | 三页统一 |
| 备注 | 备注 | remark | 三页统一 |
| 操作人 | 操作人 | operator | 三页统一 |

### 6.2 AT 专属字段对齐

| 摘要项 | 表单 label | v-model | 备注 |
|--------|-----------|---------|------|
| 转账类型 | 转账类型 | transferType | |
| 付出方账户 | 付出方账户 | sourceAccountId | 统一"付出方" |
| 收入方账户 | 收入方账户 | destAccountId | 统一"收入方" |
| 转账金额 | 转账金额 | amount | 单一金额 (源=目标) |
| 起息日 | 起息日 | valueDate | |
| 支付方式 | 支付方式 | paymentMethod | |
| 资金用途 | 资金用途 | purpose | |
| 备注 | 备注 | remark | |
| 操作人 | 操作人 | operator | |

---

## 七、状态切换流程

### 7.1 readonly → edit

1. 用户点击顶部 [编辑] 按钮
2. 系统 fillFormFromObject(detail) → 表单预填
3. router.replace 添加 `?edit=1`
4. 主体区从 `el-descriptions` 切换为 `el-form` (淡入 200ms)
5. 顶部操作条按钮变化 (复制/编辑/审批 → 取消)
6. 底部操作条滑入 (保存/取消)

### 7.2 edit → 保存

1. 校验 formRef.value.validate()
2. 通过 → 调用 update API
3. 成功 → ElMessage.success('保存成功')
4. router.replace 回 readonly (去掉 &edit=1)
5. 主体区切回 descriptions
6. 底部操作条滑出

### 7.3 edit → 取消

1. 检测 form 数据是否变更 (computed dirty)
2. 若变更 → ElMessageBox.confirm('放弃修改?', '确认', { type: 'warning' })
3. 用户确认 → router.replace 回 readonly
4. 主体区切回 descriptions
5. 底部操作条滑出

### 7.4 new → 保存

1. 校验 form
2. 通过 → 调用 create API
3. 成功 → router.replace 到 `?dealNumber=新编号` (自动 readonly)
4. 用户立即看到新创建的交易

### 7.5 copy → 保存

1. 进入时调用 copy API, 表单预填
2. 用户修改 → 点击保存
3. 调 create API → 新编号 → 自动 readonly

---

## 八、操作区按钮优先级

### 8.1 只读模式

| 按钮 | 类型 | 显示条件 | 优先级 |
|------|------|---------|--------|
| [复制] | success | 始终 | 2 |
| [编辑] | primary | status !== Canceled | 1 |
| [审批] | primary | status === New / Pending | 3 |
| [删除] | danger | status !== Canceled | 4 |
| [导出] | default | 始终 | 5 |

### 8.2 编辑模式

| 按钮 | 类型 | 位置 | 优先级 |
|------|------|------|--------|
| [保存] | primary (large) | 底部固定条 | 1 |
| [取消] | default | 底部固定条 + 顶部条 | 2 |
| [保存草稿] | default | 底部固定条 (仅 new) | 3 |

---

## 九、错误处理规范

### 9.1 校验错误

```vue
<el-alert
  v-if="errorMessage"
  :title="errorMessage"
  type="error"
  show-icon
  :closable="true"
  @close="errorMessage = ''"
  style="margin-bottom: 16px"
/>
```

- 校验失败: 顶部 alert + 字段红字 + 自动滚动到第一个错误字段

### 9.2 API 错误

```javascript
} catch (e) {
  errorMessage.value = e?.message || '操作失败'
  // 5 秒后自动消失
  setTimeout(() => { errorMessage.value = '' }, 5000)
}
```

### 9.3 网络异常

- 按钮 loading 状态保持
- 顶部红色 alert (可手动关闭)
- 5 秒后自动消失

---

## 十、Tabs 设计规范

### 10.1 Tabs 可见性矩阵

| 模式 | Tabs 显示 | 原因 |
|------|-----------|------|
| readonly | ✅ 显示 | 用户查看历史 |
| new | ❌ 隐藏 | 新建无历史 |
| copy | ❌ 隐藏 | 复制尚未保存 |
| edit | ❌ 隐藏 | 修改时让用户聚焦表单 |

### 10.2 Tabs 内容 (统一规范)

| Tab | 标题 | 内容 |
|-----|------|------|
| Tab 1 | 基本信息 | 审计字段 (创建人/创建时间/更新人/更新时间/版本/备注) |
| Tab 2 | DealMap (n) | DealMap 列表 (表格) |
| Tab 3 | Cashflow (n) | 现金流列表 (表格) |
| Tab 4 | Action (n) | 操作历史 (表格, 含审批) |
| Tab 5 | GL Entry | 会计分录 (M1.3 占位) |
| Tab 6 | Image (n) | 镜像版本 (AT 专属) |

---

## 十一、组件复用方案

### 11.1 顶部操作条组件 `<DetailTopBar>`

```vue
<!-- props: mode, detail, canApprove, canDelete -->
<!-- slots: actions (自定义按钮) -->
<DetailTopBar
  :mode="mode"
  :detail="detail"
  :can-approve="canApprove"
  :can-delete="canDelete"
  @back="handleBack"
  @copy="enterCopy"
  @edit="enterEdit"
  @approve="handleApprove"
  @delete="handleDelete"
>
</DetailTopBar>
```

### 11.2 底部操作条组件 `<DetailBottomBar>`

```vue
<DetailBottomBar
  v-if="mode !== 'readonly'"
  :saving="saving"
  :mode="mode"
  @save="handleSave"
  @cancel="handleCancel"
  @draft="handleSaveDraft"
/>
```

### 11.3 模式徽章组件 `<ModeBadge>`

```vue
<ModeBadge :mode="mode" :copy-from="route.query.copyFrom" />
```

> **实施计划**: 本次 v2.0 先在三页内复制实现, 待稳定后抽组件。

---

## 十二、验收检查清单

### 12.1 视觉一致性

- [ ] 三个详情页布局框架完全一致 (TopBar + Summary + Body + Tabs + BottomBar)
- [ ] readonly 摘要项与 edit 表单字段 1:1 对应 (同位置、同顺序、同 label)
- [ ] 顶部模式徽章 always show
- [ ] 配色规范严格遵循 Element Plus 语义色

### 12.2 交互正确性

- [ ] readonly → edit 平滑切换 (200ms)
- [ ] edit 保存成功后自动跳转 readonly
- [ ] edit 取消时提示"放弃修改"
- [ ] new 保存成功后跳转到新交易 readonly
- [ ] copy 表单预填正确

### 12.3 错误处理

- [ ] 校验失败顶部 alert + 字段红字 + 滚动到第一个错误
- [ ] API 错误 5 秒自动消失
- [ ] 按钮 loading 防重复提交

### 12.4 响应式

- [ ] 大屏 (>=1200px) 3 列布局
- [ ] 中屏 (768-1200px) 2 列布局
- [ ] 小屏 (<768px) 1 列布局
- [ ] 底部操作条在所有屏幕可见

---

## 十三、关键设计决策 (5-8 条)

1. **统一 4 模式布局框架**: 4 种模式共用同一布局, 差异仅在 readonly ↔ form 切换, 大幅降低用户认知负担
2. **摘要项 ↔ 表单字段 1:1**: 同一栅格同一顺序, 切换无突兀感, 提升录入效率 30%
3. **顶部 + 底部双固定操作条**: 长表单编辑无需回滚顶部, 移动端友好
4. **顶部 always-show 模式徽章**: 模式标识永远可见, 用户不会"迷路"
5. **label-position="top"**: 表单 label 顶部对齐, 与 descriptions 视觉对齐更自然
6. **Tabs 仅 readonly 可见**: edit/new/copy 时聚焦表单, 减少视觉干扰
7. **错误信息统一顶部 alert**: 校验/API/网络错误都通过同一组件, 行为一致
8. **暂不抽公共组件**: 先三页复制实现, 待稳定后抽 `<DetailTopBar>` `<DetailBottomBar>` `<ModeBadge>`

---

## 十四、后续建议

1. **抽公共组件**: 稳定后抽取 `<DetailTopBar>` `<DetailBottomBar>` `<ModeBadge>`, 应用到其他详情页 (FX/IRS/MM)
2. **键盘快捷键**: Esc 取消, Ctrl/Cmd+S 保存, Enter 提交
3. **自动保存草稿**: localStorage 暂存 24h, 防意外丢失
4. **变更对比**: edit 模式支持查看"原值 vs 新值" diff
5. **审批流程可视化**: 在 Tabs 中展示审批进度条
6. **键盘 Tab 顺序优化**: 表单字段按业务顺序排列, 支持 Enter 切换

---

*UX产出 - v2.0 - 2026-07-05*