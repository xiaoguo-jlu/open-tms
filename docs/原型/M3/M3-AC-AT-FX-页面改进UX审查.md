# AC/AT/FX 页面 UX 审查

> **版本**: v1.0
> **作者**: UX 设计师
> **日期**: 2026-07-05
> **审查范围**: `web/src/views/dealing/{Ac,At,Fx}DealDetail.vue` + `web/src/views/dealing/{Ac,At,Fx}DealList.vue`
> **不重复范围**: 一屏布局(M3-交易详情一屏布局UX原型.md)、内联编辑(M3-交易详情内联编辑UX原型.md)

---

## 一、整体评估

**[B+] 综合评级**: 已经在三页上完整实现了"三段式布局 + 4 模式统一框架 + 底部固定操作条 + 顶部操作按钮 + Picker preload 修复 + 复制联动",整体质量优于 90% 业内自研 TMS。但仍有 **7 处具体可改进点**,集中在 **细节一致性、错误处理、Disabled 状态语义、复制按钮可见性、底部状态提示** 等方面,均为 P0~P2 不同优先级。

**对标参考**:
- **FIS Quantum**: 详情页按钮 Disabled 时有灰底斜纹/灰色文字变浅的视觉弱化效果, 不允许 hover。
- **Murex MX.3**: 关键信息条使用 "业务图标 + 字段值" 组合, 用户一眼能识别业务含义。
- **Bloomberg AIM**: 表单 dirty 检查在离开/切换 Tab 时自动拦截, 不依赖用户主动取消按钮。

---

## 二、详细问题列表

### 2.1 问题 1: 顶部操作按钮无 Disabled 状态视觉区分 (P1)

- **影响**: P1 (中) — 影响所有 readonly 模式用户对"我能做什么"的判断
- **现状**:
  - AC 详情页: "编辑" 按钮只在 `detail.status !== 'Canceled'` 时显示 (v-if 删除), 不显示 = 用户猜不透为什么。
  - AT 详情页: 用 `canEdit(status)` / `canDelete(status)` 函数判断, 但按钮**始终渲染**,仅 v-if 隐藏部分场景。
  - FX 详情页: "审批" 按钮 `v-if="detail.status === 'New' || detail.status === 'Pending'"`,其它状态直接消失, 用户不知道为什么"审批"不见了。
  - 三页**没有 disabled 视觉状态**(例如 `Approved` 状态的交易, "编辑" 应该 disabled + tooltip "已审批交易不可编辑"),业界 FIS Quantum 会有明显的灰色 + 鼠标 hover 提示。

  代码样例 (AcDealDetail.vue:13-14):
  ```vue
  <el-button v-if="detail.status !== 'Canceled'" type="primary" :icon="Edit" @click="enterEdit">编辑</el-button>
  <el-button v-if="detail.status === 'New'" type="primary" :icon="Check" @click="handleApprove">审批</el-button>
  ```
  问题:Approved 状态下, 用户看不到"编辑"按钮, 会困惑是 bug 还是权限问题。

- **建议**:
  ```vue
  <el-button
    type="primary"
    :icon="Edit"
    :disabled="detail.status === 'Approved' || detail.status === 'Canceled'"
    @click="enterEdit"
  >编辑</el-button>
  <el-tooltip v-if="detail.status === 'Approved'" content="已审批交易不可编辑" placement="top">
    <el-icon class="info-icon"><InfoFilled /></el-icon>
  </el-tooltip>
  ```
  配套 CSS:
  ```css
  .action-bar .el-button:disabled { cursor: not-allowed; opacity: 0.55; }
  .action-bar .el-button:disabled:hover { background: #f5f7fa; }
  ```

- **受益页面**: AC/AT/FX (共同)

---

### 2.2 问题 2: 关键信息条字段过密 + 字段顺序不一致 (P2)

- **影响**: P2 (低) — 视觉一致性, 不影响功能
- **现状**:
  - 3 个详情页关键信息条字段顺序混乱:
    - AC: 交易编号 | 方向 | 状态 | 金额 | 起息日
    - AT: 交易编号 | 转账类型 | 状态 | 转账金额 | 起息日
    - FX: 交易编号 | 产品类型 | 状态 | 总金额 | 到期日
  - "状态"位置都在第 3 位,但 "类型" (方向/转账类型/产品类型) 在第 2 位混乱,金额/币种应在最显眼位置但当前被"类型"挤到第 4 位。
  - 业界最佳实践 (Murex): 关键信息条顺序应是 **业务身份 → 状态 → 金额 → 时间**, 让用户在 1.5 秒内捕获最关键信息。

- **建议**: 统一字段顺序为 **交易编号 → 类型(方向/转账/产品) → 状态 → 金额(突出) → 时间**, 并抽出公共组件 `<KeyInfoBar>`:
  ```vue
  <KeyInfoBar
    :deal-number="detail.dealNumber"
    :type-info="{ label: '方向', value: '流入', type: 'success' }"
    :status-info="{ value: detail.status, label: getStatusLabel(detail.status) }"
    :amount-info="{ value: formatAmount(detail.amount), currency: detail.currency, highlight: true }"
    :date-info="{ label: '起息日', value: detail.valueDate }"
  />
  ```

- **受益页面**: AC/AT/FX (共同)

---

### 2.3 问题 3: 列表页"复制"按钮在 V2.0 已修复但 FX 列表页逻辑分叉 (P0)

- **影响**: P0 (高) — 影响 FX 列表页用户复制 NDF 交易的核心场景
- **现状**:
  - AC 列表页 (AcDealList.vue:69): `<el-button type="success" link @click="handleCopy(row)">复制</el-button>` — 正常
  - AT 列表页 (AtDealList.vue:71): `<el-button type="success" link @click="handleCopy(row)">复制</el-button>` — 正常
  - FX 列表页 (FxDealList.vue:79): `<el-button type="success" link @click="handleCopy(row)">复制</el-button>` — 正常

  **真正问题**: 三个列表页的"复制"按钮都是**始终显示**,但是:
  - 已 Canceled 状态的交易不应允许复制 (复制了也是无效状态)
  - FX NDF 交易复制后, fixingSource/nominal 应清空, 否则用户复制后 fix 字段残留导致误以为已 fixing
  - 三个列表页都缺少 **复制成功的 Toast 提示** (AC/AT/FX 列表复制都是 `router.push`, 没有 ElMessage 反馈)

- **建议**:
  ```vue
  <!-- 列表页统一加 v-if 控制 -->
  <el-button
    type="success"
    link
    :disabled="row.status === 'Canceled'"
    @click="handleCopy(row)"
  >复制</el-button>
  ```
  handleCopy 中增加提示:
  ```js
  const handleCopy = (row) => {
    router.push(`/dealing/${dealType}/detail?copyFrom=${row.dealNumber}`)
    // 不弹 Toast — 跳转本身就明确了 "复制模式" 标识, 详情页底部的 ModeBadge 已经显示
  }
  ```
  **FX 复制时清空 NDF fixing 字段** (在 FxDealDetail.vue loadCopyData 中):
  ```js
  // 复制模式:清空 NDF fixing 残留, 防止误以为已 fixing
  form.fixingRate = null
  form.fixingSource = ''
  form.notional = null
  form.settlementAmount = null
  ```

- **受益页面**: FX (主要), AC/AT (按钮 disabled 视觉)

---

### 2.4 问题 4: Picker 联动后 ID 残留导致编辑保存时 N+1 查询 / 脏数据 (P1)

- **影响**: P1 (中) — 影响编辑保存正确性
- **现状**:
  - 3 个详情页都使用 `BaseDataPicker` 联动,例如 AcDealDetail.vue:153-160:
  ```vue
  <BaseDataPicker v-model="form.counterpartyId" entity="counterparty"
    :preload-row="preloadRows.counterparty"
    @change="onCounterpartyChange" />
  ```
  - `onCounterpartyChange` 中只更新 `form.counterpartyId`,但 `form.counterpartyName` 等冗余字段未清空。
  - 编辑模式 (修改 counterparty 后又改回原值) → 保存时 payload 包含旧 name + 新 id,后端 VO 反序列化混乱。

- **建议**: 在每个 Picker `@change` 回调中**显式清空冗余字段**,或者在 BaseDataPicker 内置 emit 多字段 (`change-full` 返回完整 row,自动同步 name/id):
  ```js
  const onCounterpartyChange = (row) => {
    if (!row) {
      form.counterpartyId = null
      form.counterpartyName = ''
      return
    }
    form.counterpartyId = row.id
    form.counterpartyName = row.name
    // 同时清空依赖字段(对手方账户)
    form.counterpartyAccountId = null
    form.counterpartyAccountName = ''
  }
  ```
  或者更优雅: 给 BaseDataPicker 加 `prop="form.counterpartyName"` / `clear-other` props 自动联动。

- **受益页面**: AC (主要), FX, AT

---

### 2.5 问题 5: 离开页面前无 dirty 检查 (浏览器关闭/路由切换) (P0)

- **影响**: P0 (高) — 数据丢失风险
- **现状**:
  - 三个详情页都只在**点击"取消"按钮时**用 `isDirty` 校验 (AcDealDetail.vue:539-542, 564-581):
  ```js
  const isDirty = computed(() => {
    if (mode.value === 'new' || mode.value === 'copy') return true
    return JSON.stringify(form) !== JSON.stringify(detail.value)
  })
  ```
  - 但用户在 edit 模式下:浏览器关闭、刷新、点击面包屑、切换路由 (例如点击左侧菜单去别的页面) 都不会触发 `handleCancel`,**直接数据丢失**。
  - `JSON.stringify` 比对在表单包含嵌套对象 (例如 `fixingSource`) 时性能差且不稳定。

- **建议**: 在每个详情页加 `onBeforeRouteLeave` + `onBeforeUnmount` 拦截:
  ```js
  import { onBeforeRouteLeave } from 'vue-router'

  onBeforeRouteLeave((to, from, next) => {
    if (mode.value !== 'readonly' && isDirty.value) {
      ElMessageBox.confirm(
        '当前有未保存的修改,确定离开?',
        '数据未保存',
        { type: 'warning', confirmButtonText: '放弃离开', cancelButtonText: '继续编辑' }
      ).then(() => next()).catch(() => next(false))
    } else {
      next()
    }
  })

  window.addEventListener('beforeunload', (e) => {
    if (mode.value !== 'readonly' && isDirty.value) {
      e.preventDefault()
      e.returnValue = '当前有未保存的修改,确定离开?'
    }
  })
  ```
  进一步: 用 `deepClone` + `hashCode` 替代 `JSON.stringify`,规避嵌套对象和 key 顺序问题。

- **受益页面**: AC/AT/FX (共同)

---

### 2.6 问题 6: errorMessage 5 秒后自动消失, 用户来不及看清错误 (P2)

- **影响**: P2 (低) — 错误反馈体验
- **现状**:
  - 三个详情页都使用同样的 5 秒 setTimeout (AcDealDetail.vue:587-588):
  ```js
  errorMessage.value = '请检查表单填写'
  setTimeout(() => { errorMessage.value = '' }, 5000)
  ```
  - 问题: 长错误信息 (例如 "管理主体已变更为 X, 请重新选择付出/收入方账户") 5 秒一闪而过, 用户必须刷新页面或重操作才能再看。
  - 错误信息紧贴表单顶部, 在大屏 (1920px) 下距离首字段 1 米远, 用户根本没看到。

- **建议**:
  1. **延长显示时间**: 错误消息默认 8 秒, 严重错误 (500/网络) 12 秒, 提供 "复制错误详情" 按钮。
  2. **错误归位字段**: form 校验失败时,用 `formRef.value.scrollToField(prop)` 自动滚动到第一个错误字段 (Element Plus 内置 API)。
  3. **错误级别视觉化**:
     - 校验错误: 黄色 `type="warning"` + 字段红字
     - API 业务错误: 红色 `type="error"` + 错误码
     - 网络/500 错误: 红色 + 重试按钮
  ```js
  const handleSave = async () => {
    try {
      await formRef.value.validate()
    } catch (errors) {
      errorMessage.value = '请检查表单填写'
      // 自动滚动到第一个错误字段
      const firstField = Object.keys(errors)[0]
      formRef.value.scrollToField(firstField)
      return
    }
    // ...
  }
  ```

- **受益页面**: AC/AT/FX (共同)

---

### 2.7 问题 7: AT 详情页缺少"审批"按钮 (与 AC/FX 不一致) (P0)

- **影响**: P0 (高) — AT 业务功能缺失, 用户体验割裂
- **现状**:
  - AC 详情页 (AcDealDetail.vue:14): 有 `handleApprove` 按钮 + `<ActionApprovalDialog>`
  - FX 详情页 (FxDealDetail.vue:15): 有 `handleApprove` 按钮 + 内联审批弹窗
  - **AT 详情页**: 没有"审批"按钮!只有 `复制/编辑/删除` 三个按钮 (AtDealDetail.vue:11-15):
  ```vue
  <template v-if="mode === 'readonly'">
    <el-button type="success" :icon="CopyDocument" @click="enterCopy">复制</el-button>
    <el-button v-if="canEdit(detail.status)" type="primary" :icon="Edit" @click="enterEdit">编辑</el-button>
    <el-button v-if="canDelete(detail.status)" type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
  </template>
  ```
  AT `canEdit` 仅在 `status === 'New' || status === 'Rejected'`, 但**业务上 AT 也是需要审批流程的** (Murex 中 AT 交易触发等额转账审批, 内部调拨通常需要财务总监审批)。

- **建议**: 补齐 AT 详情页的审批按钮 + 弹窗:
  ```vue
  <!-- AtDealDetail.vue 顶部 readonly 模板 -->
  <el-button v-if="detail.status === 'New'" type="primary" :icon="Check" @click="handleApprove">审批</el-button>
  ```
  ```js
  import ActionApprovalDialog from './ActionApprovalDialog.vue'  // 或新建 AtActionApprovalDialog

  const approvalVisible = ref(false)
  const handleApprove = () => { approvalVisible.value = true }
  ```
  同步在列表页 (AtDealList.vue:71) 增加"审批"快捷链接。

- **受益页面**: AT (主要), 涉及业务完整性

---

### 2.8 问题 8: FX NDF fixing 字段"待 RATE_FIX"状态在 key-info-bar 缺标识 (P2)

- **影响**: P2 (低) — 视觉提示不足
- **现状**:
  - FX 详情页 key-info-bar 只显示 5 个固定字段 (编号/类型/状态/总金额/到期日), NDF 的 "fixingRate 待定" 状态仅在主信息区有"待 RATE_FIX"灰色文字 (FxDealDetail.vue:75-76)。
  - 用户扫一眼关键信息条会以为"交易已完成", 但其实 NDF 还差 fixing。
  - 业界 Bloomberg AIM 对 NDF/CCS 等"待 fixing/待结算"状态会在 key-info-bar 加黄色脉冲徽章。

- **建议**: 在 FX 关键信息条增加 "Fixing 状态" 字段 (仅 NDF 显示):
  ```vue
  <div class="key-item" v-if="isNdfForDetail">
    <span class="key-label">Fixing 状态</span>
    <el-tag :type="detail.fixingRate ? 'success' : 'warning'" effect="dark" size="default">
      <el-icon v-if="!detail.fixingRate" class="pulse"><Clock /></el-icon>
      {{ detail.fixingRate ? '已 Fixing' : '待 RATE_FIX' }}
    </el-tag>
  </div>
  ```
  配套 CSS 脉冲动画:
  ```css
  .pulse { animation: pulse 1.5s ease-in-out infinite; }
  @keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.4; }
  }
  ```

- **受益页面**: FX (主要), 其它 NDF-like 场景可参考

---

## 三、横向对比表

| 维度 | AC | AT | FX | 业界对标 (FIS/Murex) |
|------|----|----|----|---------------------|
| **顶部操作按钮数量** | 5 (复制/编辑/审批/删除/取消) | 3 (复制/编辑/删除)**缺审批** | 5 (RATE_FIX/复制/编辑/审批/删除) | 4-5 (差异: AT 应补审批) |
| **关键信息条字段** | 5 (编号/方向/状态/金额/起息日) | 5 (编号/转账类型/状态/金额/起息日) | 5 (编号/产品类型/状态/金额/到期日) | 5-7 |
| **Picker preload** | ✅ 已实现 (applyPreloadFromCopyData) | ⚠️ 部分 (loadAccountById 但无 preload-row) | ✅ 已实现 (applyPreloadFromCopyData) | 必备 |
| **底部固定操作条** | ✅ | ✅ | ✅ | 必备 |
| **ModeBadge** | ✅ | ✅ | ✅ | 必备 |
| **dirty 检查** | ⚠️ 仅 handleCancel | ⚠️ 仅 handleCancel | ⚠️ 仅 handleCancel | onBeforeRouteLeave + beforeunload |
| **disabled 视觉** | ❌ 仅 v-if 隐藏 | ❌ 仅 v-if 隐藏 | ❌ 仅 v-if 隐藏 | 灰色 + tooltip |
| **错误 5s 自动消失** | ⚠️ | ⚠️ | ⚠️ | 8-12s + 复制按钮 |
| **审批按钮** | ✅ | ❌ | ✅ | 必备 |
| **NDF fixing 提示** | — | — | ⚠️ 仅主信息区文字 | key-info-bar 徽章 + 脉冲 |
| **列表页复制按钮** | ✅ + disabled 未实现 | ✅ + disabled 未实现 | ✅ + NDF 字段残留 | + NDF 字段清空 |
| **离开页面拦截** | ❌ | ❌ | ❌ | onBeforeRouteLeave |

---

## 四、P0 紧急修复 (本周)

### 修复 1: AT 详情页补"审批"按钮 (问题 7)
**文件**: `web/src/views/dealing/AtDealDetail.vue`
**改动**: 增加 `handleApprove` 方法 + `approvalVisible` 状态 + 审批弹窗 (复用 AC 的 `ActionApprovalDialog.vue` 或新建)。
**验收**: AT readonly 模式顶部 4 个按钮 (复制/编辑/审批/删除), `status === 'New'` 时审批按钮可点。

### 修复 2: 全局 dirty 检查 + onBeforeRouteLeave (问题 5)
**文件**: 3 个 Detail.vue
**改动**: 加 `onBeforeRouteLeave` + `beforeunload` 拦截, 提示"数据未保存"。
**验收**: 在 edit 模式修改任意字段, 试图点击左侧菜单离开时弹出确认框, 选"放弃离开"才放行。

### 修复 3: FX 列表页/详情页 NDF 复制时清空 fixing 字段 (问题 3)
**文件**: `FxDealDetail.vue` 的 `loadCopyData`
**改动**: 复制时清空 `fixingRate / fixingSource / notional / settlementAmount`, 防止误以为已 fixing。
**验收**: 复制一个已 fixing 的 NDF, 新建后 key-info-bar 的 fixing 状态为"待 RATE_FIX"。

---

## 五、P1 重要优化 (1 个月内)

### 优化 1: 顶部操作按钮 Disabled 视觉统一 (问题 1)
**改动**: 3 个 Detail.vue 顶部 readonly 模板, 把 `v-if="..."` 改为 `:disabled="..."` + tooltip。
**验收**: Approved 状态的交易,"编辑"按钮灰显, hover 提示"已审批不可编辑"。

### 优化 2: Picker 联动后冗余字段清空 (问题 4)
**改动**: `BaseDataPicker.vue` 增加 `@change-full` 事件, 在 3 个 Detail.vue 的 onChange 回调中清空冗余 name 字段。
**验收**: 编辑 AC, 修改 counterparty 后又改回原 counterparty, 保存后后端 VO 只包含 id 无脏 name。

### 优化 3: 列表页复制按钮按状态禁用 (问题 3)
**改动**: 3 个 List.vue, Canceled 状态的复制按钮 `:disabled="true"` + tooltip。
**验收**: Canceled 交易的复制按钮灰显, hover 提示"已取消交易不可复制"。

---

## 六、P2 锦上添花

### 1. 关键信息条字段顺序统一 + 抽出 `<KeyInfoBar>` 组件 (问题 2)
### 2. errorMessage 自动滚动到错误字段 + 延长至 8s (问题 6)
### 3. FX NDF Fixing 状态在 key-info-bar 加脉冲徽章 (问题 8)
### 4. 关键信息条使用 "业务图标 + 字段值" 组合 (对标 Murex)
### 5. 增加键盘快捷键 Esc 取消 / Ctrl+S 保存 (M3-内联编辑原型 14.2 后续建议)
### 6. 增加变更对比 diff (M3-内联编辑原型 14.4 后续建议)
### 7. localStorage 自动保存草稿 24h (M3-内联编辑原型 14.3 后续建议)

---

## 七、总结

**3 个详情页 UX 现状**: 90 分水准, 三段式布局已固化为肌肉记忆, 操作流程清晰。
**待改进**: 主要在**业务完整性 (AT 缺审批) + 容错性 (dirty 检查) + Disabled 视觉** 三方面。
**建议优先级**: P0 (本周) → P1 (1 个月内) → P2 (1 个季度), 按 M3 业务线串联修复。
**对标差距**: 与 FIS Quantum 差距主要在 "键盘快捷键 / 业务快捷键 / 大数据量下的虚拟滚动"; 与 Murex 差距主要在 "审批流程可视化 / 多级审批权限"。

---

*UX 审查产出 - v1.0 - 2026-07-05*