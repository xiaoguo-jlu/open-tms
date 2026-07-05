# Checklist 03 — 前端错误与体验

> 配合 `opentms-review-frontend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 错误处理 (FE-008 / FE-009 / FE-014)

### A1. catch 块错误处理
```javascript
// 正确:用户可见 ElMessage
try {
  await saveDeal(data)
  ElMessage.success('保存成功')
  router.push('/dealing/ac')
} catch (err) {
  ElMessage.error('保存失败: ' + err.message)
}

// 错误:仅 console
catch (err) {
  console.error(err)  // 用户看不到
}
```

### A2. 验证
- [ ] 100% catch 块用 ElMessage / ElMessageBox
- [ ] 0 `console.error` / `console.log` / `console.warn`
- [ ] 0 `alert()`(应用 ElMessageBox.confirm)
- [ ] 0 静默失败

---

## B. 表单交互 (FE-013 / FE-014)

### B1. 表单校验
- [ ] el-form :rules 完整(必填 / 格式 / 长度)
- [ ] 必填项标红 `*`
- [ ] blur 时触发校验
- [ ] 失败时滚动到第一个错误项
- [ ] 成功提示 ElMessage.success

### B2. 保存按钮状态
- [ ] 提交按钮 :loading 防重复
- [ ] 成功后自动跳转列表
- [ ] 失败后保留表单数据
- [ ] 关闭弹窗前确认(`ElMessageBox.confirm`)

### B3. 弹窗操作
- [ ] Esc 关闭弹窗
- [ ] 点击遮罩不关闭(`:close-on-click-modal="false"` 写操作)
- [ ] 关闭按钮(X)可关闭
- [ ] 弹窗内 loading 状态

---

## C. 状态设计 (FE-011)

### C1. 加载状态
- [ ] el-table v-loading
- [ ] el-skeleton 骨架屏(列表)
- [ ] el-button :loading
- [ ] 全屏 loading(必要时)

### C2. 空状态
- [ ] 列表为空显示 el-empty
- [ ] 搜索无结果显示 el-empty + 提示
- [ ] 错误状态显示重试按钮
- [ ] 0 空白页面(未处理空数据)

### C3. 错误状态
- [ ] 网络错误显示重试
- [ ] 500 错误友好提示
- [ ] 401 自动跳转登录
- [ ] 0 直接抛白屏

---

## D. 联动逻辑 (FE-012)

### D1. 典型联动场景
- [ ] 币种对(currency_pair)切换 → 自动填币种(buy_ccy / sell_ccy)
- [ ] 管理主体(legal_entity)切换 → 默认账户列表
- [ ] 交易类型(deal_type)切换 → 联动字段显示
- [ ] 银行账户选择 → 显示余额 / 币种
- [ ] 金融工具选择 → 显示产品参数

### D2. 联动实现
```vue
<el-select v-model="form.currencyPair" @change="onCurrencyPairChange">
  <el-option v-for="cp in currencyPairs" :key="cp" :label="cp" :value="cp" />
</el-select>

<script setup>
const onCurrencyPairChange = (val) => {
  const [buy, sell] = val.split('/')
  form.buyCcy = buy
  form.sellCcy = sell
}
</script>
```

---

## E. 操作反馈 (FE-014)

### E1. 成功反馈
- [ ] 保存 ElMessage.success
- [ ] 删除 ElMessage.success
- [ ] 审批 ElMessage.success
- [ ] 执行 ElMessage.success
- [ ] 跳转列表 / 详情

### E2. 失败反馈
- [ ] 失败 ElMessage.error + 保留表单
- [ ] 提示具体错误原因(后端 message)
- [ ] 0 笼统"操作失败"

### E3. 警告 / 确认
- [ ] 删除前 ElMessageBox.confirm
- [ ] 提交前 ElMessageBox.confirm(重要操作)
- [ ] 离开未保存表单提示
- [ ] 0 直接操作无确认

---

## F. 模式区分 (FE-030)

### F1. create / copy / edit / view
- [ ] ModeBadge 显示当前模式
- [ ] create 标题"新建 xxx"
- [ ] copy 标题"复制 xxx" + 清空 id / no
- [ ] edit 标题"编辑 xxx [编号]"
- [ ] view 标题"查看 xxx [编号]" + 字段只读

### F2. 字段只读
- [ ] view 模式字段 :disabled
- [ ] 已提交单据字段 :disabled
- [ ] 流程结束字段 :disabled
- [ ] 0 字段状态混乱

---

## G. 列表页四件套 (FE-016 / FE-030)

### G1. 搜索区
- [ ] 至少 1 个搜索字段(编号 / 名称)
- [ ] 至少 2 个筛选字段(状态 / 类型 / 时间)
- [ ] 搜索按钮 + 重置按钮
- [ ] 防抖 300ms 或点击触发

### G2. 工具栏
- [ ] 新建按钮(主色,右上)
- [ ] 批量操作(可选)
- [ ] 列设置 / 导出(可选)

### G3. 表格
- [ ] 列顺序合理(关键在前)
- [ ] 状态列 el-tag 统一
- [ ] 操作列 ≤3 个,多余"更多"下拉
- [ ] 金额右对齐 + 千分位
- [ ] 时间 YYYY-MM-DD HH:mm:ss
- [ ] 空数据显示 el-empty

### G4. 分页
- [ ] el-pagination 显示
- [ ] :page-sizes 10/20/50/100
- [ ] :total 绑定

---

## H. 详情页布局 (FE-030)

### H1. TopBar
- [ ] 返回按钮(左上)
- [ ] 关键操作按钮(按状态显示)
- [ ] ModeBadge 显示 view

### H2. 关键信息卡
- [ ] 编号 / 名称 / 状态徽章
- [ ] 金额字段大字号 + 等宽
- [ ] 创建时间 / 创建人

### H3. 主信息区
- [ ] 完整展示所有字段(只读)
- [ ] 标签清晰分组

### H4. Tabs
- [ ] 子信息 / 操作 / 审批 / 日志 / 现金流
- [ ] Tab 切换不丢数据
- [ ] 默认第一个 Tab

---

## I. 国际化 (FE-022)

### I1. i18n
- [ ] 关键文案 `t('xxx')` 包裹
- [ ] vue-i18n 配置 zh-CN / en-US
- [ ] ≥50% 关键文案 i18n 化
- [ ] 0 硬编码中文长文案

### I2. 预留扩展
- [ ] 文案与代码分离
- [ ] 占位符 `t('deal.no', { no: dealNo })`
- [ ] 多语言切换组件

---

## J. 性能优化 (FE-023 / FE-024 / FE-029)

### J1. 渲染性能
- [ ] v-if / v-show 合理使用
- [ ] 列表 key 稳定(避免 index)
- [ ] 大列表虚拟滚动(`el-table-v2`)
- [ ] 计算属性缓存

### J2. 加载性能
- [ ] 路由懒加载
- [ ] 组件异步加载(`defineAsyncComponent`)
- [ ] 图片懒加载
- [ ] CDN 加速

### J3. 响应式
- [ ] 1920 / 1440 / 1280 三种分辨率
- [ ] 0 横向滚动条
- [ ] flex / grid 布局
- [ ] 移动端基础适配

### J4. 浏览器兼容
- [ ] Chrome / Edge / Firefox 最新 2 版
- [ ] 0 ES2020+ 未兼容语法
- [ ] polyfill 配置

---

## 审核结论

通过项数 / 总项数 = ____%

| 等级 | 通过率 |
|------|--------|
| A | ≥95% |
| B | ≥85% |
| C | ≥70% |
| D | <70% |

**额外扣分项**:
- 任何 FE-008 / FE-009 / FE-013 / FE-014 / FE-016 (P0) 未通过 → 直接降至 C
- 3 个 P0 未通过 → 直接降至 D