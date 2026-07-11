---
name: opentms-frontend-dev
description: Use when implementing Open-TMS frontend pages and components as Frontend Developer
---

# Open-TMS 前端开发 Skill (FE)

## 简介

本 skill 指导前端开发人员完成 Vue 3 (Composition API) + Element Plus + Vite 技术栈下的页面与组件开发，覆盖从 UX 原型到编译验证通过的完整流程。

---

## 一、触发条件

**触发场景**: UX 原型已完成且分配前端任务 / 需实现新页面或组件 / 重构或修复前端代码 / QA 发现前端缺陷。

---

## 二、输入要求

| 输入项 | 来源 | 说明 |
|--------|------|------|
| UX 原型文档 | UX | 界面布局和交互说明 |
| API 接口文档 | Dev/TA | 后端接口契约 |
| 模块已有代码 | `web/src/views/{模块}/` | 风格参考 |
| 设计规范 | `docs/原型/Open-TMS界面原型与设计规范.md` | 设计系统 |
| 模块历史摘要 | `web/src/views/{模块}/SUMMARY.md` | 若存在 |

可选: PRD 文档、竞品参考。

---

## 三、输出规范

### 3.1 技术栈要求

- 使用 Composition API (`<script setup>`)
- Element Plus 组件库
- Axios 封装 (`@/utils/request.js`)
- 路径别名: `@` = `web/src/`

### 3.2 文件路径规范

| 页面类型 | 路径 | 命名 |
|----------|------|------|
| 列表页 | `web/src/views/{模块}/{Entity}List.vue` | PascalCase |
| 详情页 (4模式) | `web/src/views/{模块}/{Entity}Detail.vue` | PascalCase |
| API 文件 | `web/src/api/{模块}/{entity}.js` | camelCase |
| 公共组件 | `web/src/components/{类型}/{Name}.vue` | PascalCase |
| Composable | `web/src/composables/{name}.js` | camelCase |

> 完整目录结构: `references/directory-structure.md`

### 3.3 API 函数命名

```
listXxx(params)     # 分页查询
getXxx(id)          # 详情查询
saveXxx(data)       # 新增
updateXxx(data)     # 更新
deleteXxx(id)       # 删除
```

---

## 四、执行步骤

### 步骤1: 读取输入

1. 阅读 UX 原型，理解界面布局和交互
2. 阅读 API 文档，理解数据结构 (注意后端返回是 `{ code: 200, data: {...} }` 还是直接返回 data)
3. 检查同模块已有代码，确认可复用的组件和模式

### 步骤2: 检查设计一致性

对照 `docs/原型/Open-TMS界面原型与设计规范.md`:
- 配色方案、字体、间距系统
- 已有组件 (BaseDataPicker / ModeBadge / FormContainer) 的使用方式

### 步骤3: API 层开发

1. 创建 `web/src/api/{模块}/{entity}.js`
2. 导入 `request` 实例 (`@/utils/request.js`)
3. 封装 CRUD 方法 (参照 `examples/vue-templates.md` 中的 API 封装模板)

**关键**: 更新类接口用 POST (后端是 Apache CXF JAX-RS，统一 POST update/delete)。

### 步骤4: 列表页开发

1. 创建 `{Entity}List.vue`
2. 实现: 查询表单 + 数据表格 + 分页 + Drawer 弹窗编辑

**列表页模式**:
- 查询区 (`el-form :inline` + `el-card`)
- 表格区 (`el-table` + `v-loading` + 状态列用 `el-tag`)
- 分页 (`el-pagination` v-model 双向绑定)
- 编辑弹窗 (`el-drawer` 优于 `el-dialog`)

> 完整模板: `examples/vue-templates.md`

### 步骤5: 详情页开发 (4 模式统一布局)

**推荐用单一 Detail.vue 承载 4 种模式而不是拆成 List+Edit 两个文件。** 详见 九、关键模式 9.1。

1. 创建 `{Entity}Detail.vue`
2. 通过路由 query 或 props 确定 `mode`，切换 readonly/edit 表单布局
3. 顶部操作栏根据 mode 显示不同按钮组合

### 步骤6: 组件复用

识别可复用组件并放入 `web/src/components/`:
- 已有: `BaseDataPicker` (基础数据选择器)、`ModeBadge` (模式标签)、`FormContainer`
- 新增前检查是否已有类似组件可复用

### 步骤7: 编译验证

```bash
cd web
npm run build
```

构建成功 (无 error) → 验证通过。
构建失败 → 记录错误并进入步骤8 分析。

| 错误类型 | 典型表现 | 常见原因 |
|----------|----------|----------|
| 语法错误 | `SyntaxError` | 模板语法不符合 Vue3 规范 |
| 导入错误 | `Cannot find module` | 路径或大小写不匹配 |
| 组件未注册 | component not registered | 忘记 import 或命名错误 |
| API 路径错误 | 404 | URL 与后端不一致 |

### 步骤7.5: API 一致性扫描(2026-07-11 新增)

完成 API 封装 + 页面开发后,必须跑前端 API 一致性扫描,确保与后端 OpenAPI 契约一致:

```bash
python scripts/api_scanner.py
```

**要求**:
- 评级 **A**(无问题)或 **B**(仅 P2)才能进 QA
- 评级 **C**(有 P1):修复后复审
- 评级 **D**(有 P0):**禁止提交**,先改 API 封装代码

**详细检查项**:
- 路径错(P0):`url:` 与 OpenAPI paths 不匹配
- 路径参数名错(P1):`${id}` 与 `{accountId}` 不一致
- 必传 query 缺失(P0):OpenAPI 要求 `?managementEntityId=` 但前端没传
- body 字段错(P0):DTO 字段名拼错 / 缺失 / 多余
- 无法静态分析(P2):`params: opts` 类变量传递,人工复核

**报告路径**:`docs/api/frontend-api-consistency.html`(评级 + P0/P1/P2 三段问题清单)。

**CI 模式**:`python scripts/api_scanner.py --ci`(P0 存在 exit 1)。

**与 CLAUDE.md 关系**:见 `CLAUDE.md` "API 一致性扫描" 小节。

### 步骤8: 问题分析与 Skill 优化 (仅编译失败时)

1. 定位错误文件/行号
2. 分类: Skill 模板有误 / 开发偏差 / 环境问题
3. 根据结果更新本 skill 或对应 reference 文件

### 步骤9: 生成开发摘要

更新 `web/src/views/{模块}/SUMMARY.md`:
- 本次完成的页面
- 遇到的问题及解决方案
- 编译验证结果

---

## 五、业界优秀实践 (精简)

- **Composition API**: 始终用 `<script setup>`，逻辑复用提取为 composables
- **性能**: 路由懒加载、`v-memo` 减少重渲染、表格大数据量用分页
- **状态管理**: 简单场景用 `provide`/`inject`，复杂场景用 Pinia
- **交互体验**: 操作反馈明确 (ElMessage)、Loading 状态、操作确认 (ElMessageBox)

---

## 六、与其他 Skill 的衔接

```
UX 原型 ──▶ 前端开发 ──▶ 编译验证 ──▶ QA 测试
               │              │
               ▼              ▼
            代码审查 ◀── 问题分析+Skill 优化
```

前置: opentms-ux-design(原型) / opentms-api-design(接口文档)
后续: opentms-test-case-design / opentms-review-frontend

---

## 七、质量标准

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 规范符合性 | 命名/目录/Composition API 规范 | 15% |
| 功能完整性 | 实现所有原型需求 | 20% |
| 交互正确性 | 符合 UX 交互规范 | 15% |
| 代码可维护性 | 组件复用、结构清晰 | 10% |
| 错误处理 | 网络异常 + 业务错误提示 | 10% |
| 性能 | 无明显性能问题 | 10% |
| **编译验证** | **`npm run build` 成功** | **20%** |

---

## 八、交付物检查清单

**代码**: API 文件 + 页面组件 + 目录结构正确
**功能**: 列表查询/新增/编辑/删除/分页正常
**交互**: 表单校验/错误提示/Loading 状态/成功反馈
**编译**: `npm run build` 成功，无语法/导入/组件注册错误
**摘要**: 开发摘要 + 编译验证结果已记录

---

## 九、关键模式

### 9.1 4 模式详情页: new / copy / edit / readonly 统一布局

**推荐用单一 Detail.vue 承载 4 种模式**，而不是拆成 List+Edit 两个文件。项目已有多处实践 (AcDealDetail / AtDealDetail / FxDealDetail)。

```javascript
// 模式通过路由 query 传入
const mode = computed(() => route.query.mode || 'new')
// mode 取值: 'new' | 'copy' | 'edit' | 'readonly'

// 路由进入规则:
// /dealing/ac-detail?mode=new          → 新建
// /dealing/ac-detail?mode=copy&copyFrom=AC20260701-0001 → 复制 (预填数据,清除ID)
// /dealing/ac-detail?mode=edit&id=123  → 编辑 (加载已有数据)
// /dealing/ac-detail?id=123            → 只读 (默认 mode='readonly')
```

**布局结构**:
```
┌─────────────────────────────────────────┐
│ 顶部操作栏: 返回 | 标题 + ModeBadge | 操作按钮 │
│ readonly: [复制][编辑][审批][删除]        │
│ 其他:     [取消]                        │
├─────────────────────────────────────────┤
│ 关键信息条 (仅 readonly 显示)             │
│ 交易编号 | 方向 | 状态 | 金额 | 起息日      │
├─────────────────────────────────────────┤
│ readonly 模式: 摘要区 (el-descriptions)   │
│ 非 readonly: 编辑表单 (el-form)          │
├─────────────────────────────────────────┤
│ 子 Tab 区 (仅 readonly): DealMap/现金流   │
├─────────────────────────────────────────┤
│ 底部操作栏 (仅非 readonly):              │
│ 模式标签 | [暂存] [保存] [提交审批]       │
└─────────────────────────────────────────┘
```

**关键实现**:
```javascript
// 加载数据: 仅 readonly/edit/copy 模式加载
const loadingDetail = computed(() =>
  (mode.value === 'readonly' || mode.value === 'edit' || mode.value === 'copy')
)

// 表单脏检查: 编辑/复制模式下离开时提示
const isDirty = ref(false)
// 使用 watch 监听 form 变化设置 isDirty = true

// 模式切换: 只读 → 编辑/复制
const enterEdit = () => router.replace({ query: { ...route.query, mode: 'edit' } })
const enterCopy = () => router.replace({ query: { mode: 'copy', copyFrom: detail.value.dealNumber } })
```

### 9.2 BaseDataPicker 使用规范

项目统一的基础数据选择器 (`web/src/components/picker/BaseDataPicker.vue`)，支持 11 种 preset (见 `pickerPresets.js`)。

**Props 速查**:

| Prop | 类型 | 说明 |
|------|------|------|
| `entity` | String (必填) | preset key: `'subsidiary'`/`'currency'`/`'bank-account'`/`'counterparty'` 等 |
| `modelValue` | Number/String | v-model 绑定值，字段由 preset.returnField 决定 |
| `placeholder` | String | 默认 "请选择" |
| `disabled` | Boolean | 禁用 (readonly 模式设为 true) |
| `clearable` | Boolean | 默认 true |
| `filters` | Object | 固定过滤条件，合并到每次查询 |
| `autoFilter` | Object | 响应式过滤条件 (深监听)，变更时自动重新加载 |
| `preloadRow` | Object | 外部已查到的完整行对象，picker 直接展示，不发起 API |

**关键差异**:

```
returnField = 'id'    → modelValue 绑定数字 ID ('subsidiary', 'trader', 'country', 'counterparty', 'bank-account', 'instrument' 等)
returnField = 'code'  → modelValue 绑定字符串编码 ('currency', 'management-entity')
```

**使用示例**:
```vue
<!-- 币种选择: v-model 绑定 currency code -->
<BaseDataPicker entity="currency" v-model="form.currency" :disabled="mode === 'readonly'" />

<!-- 银行账户选择: 根据币种联动过滤 -->
<BaseDataPicker entity="bank-account" v-model="form.bankAccountId"
  :autoFilter="{ currency: form.currency }"
  :preloadRow="preloadBankAccount"
  :disabled="mode === 'readonly'" />

<!-- 外部预加载行 (用于编辑模式) -->
const preloadBankAccount = ref(null)
onMounted(async () => {
  if (detail.value.bankAccountId) {
    const res = await getBankAccount(detail.value.bankAccountId)
    preloadBankAccount.value = res.data
  }
})
```

**Events**:
- `@change="(row) => ..."` — 选中行时触发，row 为完整行对象或 null
- `@clear` — 清空时触发

### 9.3 表单校验规范

**async-validator 内置规则 + el-form-item :rules**:

```javascript
// 校验规则定义
const rules = {
  code: [
    { required: true, message: '编码不能为空', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度2-50字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '名称不能为空', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '金额不能为空', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  valueDate: [
    { required: true, message: '起息日不能为空', trigger: 'change' }
  ]
}
```

**表单提交前手动校验**:
```javascript
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()  // 校验失败会 throw
    // 校验通过，执行提交...
  } catch (e) {
    // 校验失败，el-form 会自动滚动到第一个错误字段
    return
  }
}
```

**注意**:
1. `trigger: 'blur'` 用于文本输入，`trigger: 'change'` 用于选择器/日期选择器
2. 金额等数字字段使用 `el-input-number` 或在 rules 中 `type: 'number'`
3. 动态表单 (v-if 切换) 需要 `nextTick` 后再 validate

### 9.4 request.js 全局错误拦截

项目 `@/utils/request.js` 使用 Axios 拦截器统一处理:

```javascript
// 请求拦截器: 自动注入 Token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

// 响应拦截器: 统一判断 code
service.interceptors.response.use(
  response => {
    const res = response.data
    // 成功 (code === 0 或 code === 200)
    if (res.code === 0 || res.code === 200) return res
    // 业务错误 (code !== 200)
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  error => {
    // 网络异常 / HTTP 错误
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)
```

**前端代码中的使用**:
```javascript
// API 调用层直接返回 res (已经被拦截器过滤，res.code === 200)
const res = await listEntity(params)
tableData.value = res.data.records  // 直接取 data，不再判断 code

// 错误处理: 拦截器已弹 ElMessage，这里只需 catch 网络异常
try {
  await saveEntity(data)
  ElMessage.success('保存成功')
} catch (error) {
  // 业务错误已被拦截器提示，此处仅处理额外逻辑
  console.error('Submit failed:', error)
}
```

**要点**: 前端 API 调用不需要再判断 `res.code`，拦截器已统一处理；成功时直接取 `res.data`。

---

## 十、参考资源

| 资源 | 路径 |
|------|------|
| Vue 组件模板 (API封装/列表页/编辑页) | `examples/vue-templates.md` |
| 目录结构与命名规范 | `references/directory-structure.md` |
| 服务管理脚本 | `references/service-scripts.md` |
| 项目总规范 | `CLAUDE.md` |
| 设计规范 | `docs/原型/Open-TMS界面原型与设计规范.md` |

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-05 | 初始版本 |
| v1.1 | 2026-05-27 | 新增编译验证步骤 (步骤7) 和问题分析与 Skill 优化步骤 (步骤8) |
| v2.0 | 2026-07-05 | 精简: 模板→examples/vue-templates.md, 目录→references/directory-structure.md; 新增关键模式章节 (4模式详情页/BaseDataPicker/表单校验/request拦截器) |
