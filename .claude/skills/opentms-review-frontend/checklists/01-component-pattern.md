# Checklist 01 — 前端组件模式

> 配合 `opentms-review-frontend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 公共组件复用 (FE-001)

### A1. 强制复用公共组件
- [ ] `BaseDataPicker` 用于所有引用基础数据的下拉(国家 / 币种 / 银行 / 对手方 / 账户 / 工具 / 主体)
- [ ] `ModeBadge` 用于所有表单模式(create / copy / edit / view)
- [ ] `FormContainer` 用于所有表单容器(分组 / 标题)
- [ ] `ActionApprovalDialog` 用于所有审批弹窗
- [ ] `StatusTag` 用于所有状态标签
- [ ] `AmountDisplay` 用于金额展示(千分位 + 等宽)
- [ ] `DateRangePicker` 用于日期范围

### A2. 禁止自造
- [ ] 0 自造下拉选择(未基于 BaseDataPicker)
- [ ] 0 自造状态标签(未基于 StatusTag)
- [ ] 0 自造表单容器(未基于 FormContainer)
- [ ] 同类组件复用率 100%

---

## B. Vue 3 规范 (FE-004 / FE-005)

### B1. Composition API
- [ ] 100% `<script setup>`(未使用 Options API)
- [ ] 引用用 `ref` / `reactive`
- [ ] 生命周期 `onMounted` / `onUnmounted`
- [ ] 计算属性 `computed`
- [ ] 监听 `watch` / `watchEffect`

### B2. Pinia 状态管理
- [ ] 全局状态用 `defineStore`
- [ ] 0 Vuex 残留
- [ ] Store 按模块拆分(`useUserStore` / `useDealStore`)
- [ ] State / Getter / Action 分离

### B3. 路由
- [ ] 100% 路由懒加载`() => import('@/views/...')`
- [ ] 路由元信息 `meta: { title, icon, requiresAuth }`
- [ ] 路由守卫处理登录态

---

## C. 组件命名 (FE-027 / FE-028)

### C1. 文件命名
- [ ] 组件文件名 PascalCase(`AcDealList.vue` / `BaseDataPicker.vue`)
- [ ] 多单词命名(避免单单词如 `Index.vue` / `List.vue`)
- [ ] 页面组件 vs 业务组件区分(`views/` vs `components/`)

### C2. 变量命名
- [ ] JS / TS 变量 / 函数 camelCase(`getDealById` / `submitForm`)
- [ ] 常量 UPPER_SNAKE(`DEAL_STATUS_NEW`)
- [ ] 组件实例引用 PascalCase(`AcDealListRef`)
- [ ] Props 用 camelCase(`dealNo`),HTML 用 kebab-case(`deal-no`)

---

## D. 文件组织 (FE-003)

### D1. 路径规范
- [ ] 页面 `web/src/views/{module}/{page}.vue`
- [ ] API `web/src/api/{module}/{entity}.js`
- [ ] 组件 `web/src/components/{common|business}/{Component}.vue`
- [ ] Store `web/src/store/modules/{entity}.js`
- [ ] Utils `web/src/utils/{util}.js`

### D2. 模块子目录(已存在)
- [ ] `views/ac/`, `views/approval/`, `views/basedata/`, `views/cashpool/`,
      `views/dashboard/`, `views/dealing/`, `views/deposit/`, `views/fundplan/`,
      `views/fx/`, `views/irs/`, `views/loan/`, `views/report/`, `views/risk/`,
      `views/transfer/`

---

## E. 文件大小 (FE-029)

### E1. 单文件 ≤500 行
- [ ] *.vue 文件 ≤ 500 行
- [ ] 超 500 行拆分为子组件
- [ ] 复杂逻辑抽取 composable(`useXxx`)

### E2. 复杂度
- [ ] 组件职责单一(SRP)
- [ ] Props ≤ 10 个
- [ ] Emits ≤ 10 个
- [ ] 模板行数 ≤ 200 行

---

## F. TypeScript (FE-026)

### F1. 类型化
- [ ] 关键组件 `<script setup lang="ts">`
- [ ] Props 用 `defineProps<{...}>()` 强类型
- [ ] Emits 用 `defineEmits<{...}>()` 强类型
- [ ] API 响应有类型定义(`interface DealVO {...}`)

### F2. 类型安全
- [ ] 0 `any` 类型滥用
- [ ] 0 `@ts-ignore` / `@ts-nocheck`
- [ ] 接口字段类型完整

---

## G. ESLint (FE-025)

### G1. 代码风格
- [ ] ESLint 0 error
- [ ] ESLint warning ≤ 5
- [ ] Prettier 自动格式化
- [ ] 命名规范统一(2 空格 / 单引号 / 无分号)

---

## H. UI 一致性 (FE-015 / FE-030)

### H1. Element Plus 统一
- [ ] 100% Element Plus 组件(按钮 / 输入 / 选择 / 表格 / 表单)
- [ ] 0 原生 HTML 按钮 / 输入(除特殊场景)
- [ ] 组件 size 一致(`default` 或 `small`)

### H2. 与既有页面一致
- [ ] 与 AcDealList / CountryList / InstrumentList 视觉一致
- [ ] 与同模块页面布局一致
- [ ] 按钮位置一致(列表 / 详情 / 弹窗)
- [ ] 错误提示文案统一

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
- 任何 FE-001 / FE-002 / FE-003 / FE-007 / FE-008 / FE-009 / FE-013 / FE-014 / FE-016 (P0) 未通过 → 直接降至 C
- 3 个 P0 未通过 → 直接降至 D