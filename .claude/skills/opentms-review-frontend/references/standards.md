# Open-TMS 前端标准与对标参考

> 本文档收录 Open-TMS 前端审核所依据的标准映射表、业界对标资料、组件规范。

---

## 1. Open-TMS 前端技术栈(CLAUDE.md)

| 维度 | 技术 |
|------|------|
| 框架 | Vue 3 |
| UI 库 | Element Plus |
| 构建 | Vite |
| 状态 | Pinia |
| 路由 | Vue Router 4 |
| HTTP | Axios |
| 时间 | dayjs |
| 测试 | Vitest / Cypress |

---

## 2. 目录结构(CLAUDE.md 强制)

```
web/src/
├── api/             # API 调用统一封装
│   └── {module}/
│       └── {entity}.js  # 命名:listX / getX / saveX / updateX / deleteX
├── components/      # 公共组件
│   ├── common/      # BaseDataPicker / ModeBadge / FormContainer 等
│   └── business/    # 业务组件
├── views/           # 页面
│   └── {module}/    # ac/ approval/ basedata/ cashpool/ dashboard/
│                    # dealing/ deposit/ fundplan/ fx/ irs/ loan/
│                    # report/ risk/ transfer/
├── store/           # Pinia 状态
├── router/          # 路由
├── utils/           # 工具函数
└── assets/          # 静态资源
```

---

## 3. API 函数命名(CLAUDE.md 强制)

```javascript
// web/src/api/{module}/{entity}.js
import request from '@/utils/request'

export function listDeal(query) {
  return request({ url: '/api/v1/deal/page', method: 'get', params: query })
}

export function getDeal(id) {
  return request({ url: `/api/v1/deal/${id}`, method: 'get' })
}

export function saveDeal(data) {
  return request({ url: '/api/v1/deal', method: 'post', data })
}

export function updateDeal(data) {
  return request({ url: '/api/v1/deal/update', method: 'post', data })
}

export function deleteDeal(id) {
  return request({ url: `/api/v1/deal/delete/${id}`, method: 'post' })
}

export function submitDeal(id) {
  return request({ url: `/api/v1/deal/${id}/submit`, method: 'post' })
}

export function approveDeal(id) {
  return request({ url: `/api/v1/deal/${id}/approve`, method: 'post' })
}

export function rejectDeal(id) {
  return request({ url: `/api/v1/deal/${id}/reject`, method: 'post' })
}

export function executeDeal(id) {
  return request({ url: `/api/v1/deal/${id}/execute`, method: 'post' })
}
```

---

## 4. 公共组件清单(强制复用)

| 组件 | 路径 | 用途 |
|------|------|------|
| `BaseDataPicker` | `@/components/common/BaseDataPicker.vue` | 基础数据下拉选择 |
| `ModeBadge` | `@/components/common/ModeBadge.vue` | 表单模式标识 |
| `FormContainer` | `@/components/common/FormContainer.vue` | 表单容器(分组/标题) |
| `ActionApprovalDialog` | `@/components/common/ActionApprovalDialog.vue` | 通用审批弹窗 |
| `StatusTag` | `@/components/common/StatusTag.vue` | 状态标签 |
| `AmountDisplay` | `@/components/common/AmountDisplay.vue` | 金额展示(千分位+等宽) |
| `DateRangePicker` | `@/components/common/DateRangePicker.vue` | 日期范围 |

**引用规则**: 同类场景必须复用,禁止自造。

---

## 5. 状态色映射表

| 状态 (Status) | el-tag type | 含义 |
|---------------|-------------|------|
| `New` | `info` | 新建待提交 |
| `Draft` | `warning` | 草稿 |
| `Submitted` | `primary` | 已提交待审批 |
| `Approved` | `success` | 已审批 |
| `Rejected` | `danger` | 已驳回 |
| `Executing` | `warning` | 执行中 |
| `Executed` | `success` | 已执行 |
| `Settled` | `success` | 已结算 |
| `Canceled` | `danger` | 已取消 |
| `Closed` | `info` | 已关闭 |
| `Active` | `success` | 启用中 |
| `Disabled` | `info` | 禁用 |

---

## 6. 模式徽章 (ModeBadge) 映射

| 模式 | 显示文案 | 颜色 |
|------|----------|------|
| `create` | 新建中 | primary |
| `copy` | 复制中 | warning |
| `edit` | 编辑中 | warning |
| `view` | 查看 | info |

---

## 7. 列表页四件套规范

```
┌─────────────────────────────────────┐
│ 搜索区: [输入框] [筛选] [搜索][重置] │
├─────────────────────────────────────┤
│ 工具栏: [+ 新建] [批量] [导出]      │
├─────────────────────────────────────┤
│ 表格                                │
│ ┌────┬─────┬─────┬─────┬─────┐     │
│ │ □  │ 编号│ 名称│ 状态│ 操作│     │
│ ├────┼─────┼─────┼─────┼─────┤     │
│ │ □  │ A001│ ... │ ●   │ ✎ ✕ │     │
│ └────┴─────┴─────┴─────┴─────┘     │
├─────────────────────────────────────┤
│ 总数: 100  [< 1 2 3 ... 10 >] 10/页 │
└─────────────────────────────────────┘
```

---

## 8. 详情页统一布局

```
┌─────────────────────────────────────┐
│ [<] 详情 AC20260703-0001  [编辑][删除]│ ← TopBar + ModeBadge(view)
├─────────────────────────────────────┤
│ 关键信息卡                          │
│ 编号 / 名称 / 状态徽章 / 金额 / 创建人 │
├─────────────────────────────────────┤
│ 主信息区                            │
│ ┌─ 基础信息 ──────┐ ┌─ 业务信息 ─┐ │
│ │ 字段1: 值       │ │ 字段: 值   │ │
│ └─────────────────┘ └────────────┘ │
├─────────────────────────────────────┤
│ [基本信息] [操作记录] [审批记录] [日志]│ ← Tabs
│ ┌─ 内容 ─────────────────────────┐ │
│ │ Tab1 / Tab2 / Tab3 内容        │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

---

## 9. 错误提示规范

```javascript
// 正确:用户可见
ElMessage.error('保存失败: ' + err.message)
ElMessage.success('保存成功')
ElMessage.warning('请检查必填项')
ElMessageBox.confirm('确定删除吗?', '提示', { type: 'warning' })

// 错误:仅 console
console.error(err)  // 仅用于调试日志,必须同时 ElMessage
```

---

## 10. 字段格式规范

| 字段类型 | 显示格式 | 示例 |
|----------|----------|------|
| 金额 | 千分位 + 2 位小数 + 等宽 | `1,234,567.89` |
| 汇率 | 8 位小数 | `7.12345678` |
| 利率 | 4 位小数 + % | `3.8500%` |
| 时间 | YYYY-MM-DD HH:mm:ss | `2026-07-05 14:30:00` |
| 日期 | YYYY-MM-DD | `2026-07-05` |
| 编号 | 等宽字体 | `AC20260705-0001` |

---

## 11. 按钮位置约定

| 场景 | 按钮位置 |
|------|----------|
| 列表页操作列 | 右侧,3 个以内,「更多」下拉 |
| 详情页 TopBar | 右上,按状态显示 |
| 弹窗底部 | 右下,取消(左) / 确认(右,主色) |
| 审批弹窗 | 底部:驳回(左) / 拒绝(中) / 通过(右,主色) |
| 表单底部 | 右下:取消(左) / 保存(右,主色) / 提交并审批(可选) |

---

## 12. 业界对标要点

### 12.1 FIS Quantum

- **列表密度**: 紧凑表格,单屏 15+ 行
- **录入**: 模式徽章 + 联动下拉 + 快捷键
- **状态**: 颜色统一 (绿/黄/红/蓝)
- **审批**: 弹窗 + 操作历史可见
- **多窗口**: 支持多窗口并行录入

### 12.2 Murex MX.3

- **录入**: 5 步内完成核心操作
- **联动**: 币种对 → 自动填币种;管理主体 → 默认账户
- **详情**: TopBar + 关键信息 + 主信息 + Tabs
- **批量**: 多选 + 批量审批/取消
- **工作流**: 流程可视化

### 12.3 SAP TRM / Kyriba

- **字段一致性**: 全局字段命名统一
- **状态机**: 显式状态流 + 颜色
- **审计**: 操作日志 + 审批轨迹

### 12.4 Bloomberg AIM

- **快捷键**: Ctrl+F5 刷新 / Enter 提交 / Esc 关闭
- **键盘**: 主要操作可纯键盘完成
- **多窗口**: 支持多窗口并行录入
- **响应速度**: <100ms 操作反馈

---

## 13. 快捷键约定 (P2 增强)

| 快捷键 | 功能 | 适用范围 |
|--------|------|----------|
| `Ctrl + F5` | 刷新当前页 | 全局 |
| `Enter` | 提交表单 | 表单页 |
| `Esc` | 关闭弹窗 | 弹窗 |
| `Ctrl + N` | 新建 | 列表页 |
| `Ctrl + S` | 保存 | 表单页 |
| `Ctrl + F` | 搜索聚焦 | 列表页 |
| `Tab` / `Shift+Tab` | 焦点切换 | 全局 |

---

## 14. Vue 3 最佳实践

| 项 | 实践 |
|----|------|
| API | Composition API (`<script setup>`) |
| 状态 | Pinia |
| 类型 | TypeScript(`<script setup lang="ts">`) |
| 路由 | Vue Router 4 懒加载 |
| HTTP | Axios + 拦截器 |
| 表单 | Element Plus el-form + el-form-item |
| 时间 | dayjs(避免 moment) |
| 测试 | Vitest + Vue Test Utils |

---

## 15. Element Plus 最佳实践

| 项 | 实践 |
|----|------|
| 加载 | `v-loading` / `:loading` |
| 空状态 | `el-empty` |
| 确认 | `ElMessageBox.confirm` |
| 通知 | `ElMessage` / `ElNotification` |
| 表格 | `el-table` + `:data` + `el-table-column` |
| 表单 | `el-form` + `:model` + `:rules` |
| 分页 | `el-pagination` |
| 选择 | `el-select` + `el-option` |
| 日期 | `el-date-picker` |

---

## 16. 与 Open-TMS 既有页面参考

| 模块 | 参考页面 | 路径 |
|------|----------|------|
| dealing | AC 交易列表 | `web/src/views/dealing/ac/AcDealList.vue` |
| dealing | AT 转账列表 | `web/src/views/dealing/transfer/` |
| basedata | 银行账户 | `web/src/views/basedata/BankAccountList.vue` |
| basedata | 金融工具 | `web/src/views/basedata/InstrumentList.vue` |
| basedata | 国家 | `web/src/views/basedata/CountryList.vue` |

**审核时需对比以上页面**:
- 按钮位置 / 操作列布局
- 搜索区 / 工具栏布局
- 状态标签颜色
- 表格列定义
- 详情页 Tabs 结构

---

## 17. 前端审核项补强清单

### FE-001 公共组件复用
- BaseDataPicker / ModeBadge / FormContainer / StatusTag / AmountDisplay
- 禁止自造通用组件

### FE-007 API 模块化
- `web/src/api/{module}/{entity}.js`
- 函数命名 `listX / getX / saveX / updateX / deleteX`
- 0 *.vue 直接 axios/request 调用

### FE-008 错误处理
- request.js 拦截器统一处理
- 401 跳转登录 / 403 无权限 / 500 系统异常 / 400 业务异常

### FE-012 联动逻辑
- 币种对 → 自动填币种
- 管理主体 → 默认账户
- 交易类型 → 联动字段

### FE-013 表单校验
- async-validator + el-form :rules
- 必填项 / 格式 / 长度校验

### FE-014 表单反馈
- :loading 防重复提交
- ElMessage.success / error

### FE-016 列表分页
- el-pagination + 10/20/50/100

### FE-017 搜索防抖
- 300ms debounce

### FE-019 时间格式
- YYYY-MM-DD HH:mm:ss
- dayjs + formatDate util

### FE-020 金额千分位
- `toLocaleString` / formatAmount util

### FE-027 组件命名
- PascalCase (AcDealList / BaseDataPicker)

### FE-029 文件大小
- ≤500 行,超 500 行拆分

### FE-030 与既有页面一致
- 视觉 / 交互 / 组件复用
- 差异点 ≤3 处