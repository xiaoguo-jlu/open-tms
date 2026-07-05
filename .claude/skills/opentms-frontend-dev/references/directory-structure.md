# Frontend Directory Structure & Naming Reference

> 源文件: SKILL.md 附录A/B，此处集中存放。CLAUDE.md 始终为权威来源。

## 文件命名规范

```bash
# 页面组件 (PascalCase)
CurrencyList.vue
DealEdit.vue
AcDealDetail.vue

# 公共组件 (PascalCase)
BaseDataPicker.vue
ModeBadge.vue
FormContainer.vue

# API 文件 (camelCase, 目录按模块)
basedata/index.js
basedata/currency.js
dealing/index.js

# Composable (camelCase)
usePagination.js
useFormValidation.js
```

## 变量命名

```javascript
// 常量 (UPPER_SNAKE)
const MAX_PAGE_SIZE = 100

// 响应式基础值 (ref)
const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)

// 响应式对象 (reactive)
const queryForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive({ id: null, code: '', name: '', status: '1' })

// 计算属性 (computed)
const isEdit = computed(() => !!formData.id)
const drawerTitle = computed(() => (formData.id ? '编辑' : '新增'))
```

## 完整目录结构

```
web/src/
├── api/                              # API 调用层
│   ├── basedata/
│   │   ├── index.js                 # 模块主入口 (通用)
│   │   ├── currency.js              # 币种 API
│   │   ├── country.js               # 国家 API
│   │   ├── businessUnit.js          # 业务单元 API
│   │   └── ...
│   ├── dealing/
│   │   ├── index.js                 # 交易主 API
│   │   └── ...
│   └── common.js                    # 通用 API 方法
│
├── assets/                          # 静态资源
│   ├── styles/
│   │   └── common.css               # 公共样式
│   └── images/
│
├── components/                      # 组件
│   ├── common/                      #  通用组件 (ModeBadge, FormContainer)
│   ├── picker/                      #  选择器组件 (BaseDataPicker)
│   └── business/                    #  业务组件
│
├── composables/                     # 组合式函数
│   └── usePagination.js
│
├── router/                          # 路由
│   └── index.js
│
├── store/                           # 状态管理 (Pinia)
│   └── modules/
│
├── utils/                           # 工具函数
│   ├── request.js                   # Axios 封装
│   └── format.js                    # 格式化函数
│
└── views/                           # 页面视图
    ├── basedata/
    │   ├── CountryList.vue           # 列表页
    │   ├── CountryEdit.vue           # 编辑页
    │   ├── CurrencyList.vue
    │   └── SUMMARY.md
    ├── dealing/
    │   ├── AcDealDetail.vue          # 4 模式统一详情页
    │   ├── AtDealDetail.vue
    │   ├── FxDealDetail.vue
    │   └── SUMMARY.md
    ├── approval/
    ├── dashboard/
    └── ...
```

## 页面类型与路径规范

| 页面类型 | 路径 | 命名 |
|----------|------|------|
| 列表页 | `web/src/views/{模块}/{Entity}List.vue` | 搜索 + 表格 + Drawer 弹窗 |
| 详情页 | `web/src/views/{模块}/{Entity}Detail.vue` | 4 模式: new/copy/edit/readonly |
| 编辑页 | `web/src/views/{模块}/{Entity}Edit.vue` | 独立编辑页 (较少使用) |
| 通用组件 | `web/src/components/{类型}/{Name}.vue` | |
| API 模块 | `web/src/api/{模块}/{entity}.js` | |
