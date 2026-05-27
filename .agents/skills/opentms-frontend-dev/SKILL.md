---
name: opentms-frontend-dev
description: Use when implementing Open-TMS frontend pages and components as Frontend Developer
---

# Open-TMS 前端开发 Skill (FE)

## 简介

本skill用于Open-TMS项目的前端代码开发，指导开发人员完成从UX原型到可运行前端代码的完整流程。

---

## 一、触发条件

**当需要进行以下工作时，触发本skill：**

- UX完成界面原型设计，分配前端开发任务
- 需要实现新的前端页面或组件
- 需要优化/重构已有前端代码
- 需要修复前端缺陷

**触发信号：**
- UX在GitHub Projects创建Task分配给前端
- PM-Lead分配前端开发任务
- QA发现前端缺陷

---

## 二、输入要求

### 2.1 必须输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| UX原型文档 | UX提供 | 界面原型和交互说明 |
| API接口文档 | Dev提供 | 后端接口定义 |
| 总体设计规范 | `docs/规范/Open-TMS开发规范文档.md` | 前端开发规范 |
| 界面设计规范 | `docs/原型/Open-TMS界面原型与设计规范.md` | 设计系统规范 |
| 模块已有代码 | `web/src/views/{模块}/` | 同模块已有代码参考 |
| 模块历史摘要 | `web/src/{模块}/SUMMARY.md` | 本模块历史开发记录（若存在） |

### 2.2 可选输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| PRD文档 | PM提供 | 功能需求说明 |
| 竞品实现参考 | Dev自行收集 | 其他系统实现参考 |

---

## 三、输出规范

### 3.1 交付件输出标准

#### 3.1.1 Vue组件标准

每个Vue组件必须：
- 使用Composition API (`<script setup>`)
- 遵循项目命名规范
- 包含完整的交互逻辑
- 响应式设计适配

#### 3.1.2 目录结构规范

```
web/src/
├── api/                              # API接口调用
│   ├── basedata/
│   │   ├── index.js                 # 币种API
│   │   ├── businessUnit.js          # 业务单元API
│   │   └── ...
│   ├── dealing/
│   │   ├── index.js                 # 交易API
│   │   └── ...
│   └── common.js                    # 通用API方法
├── assets/                          # 静态资源
│   ├── styles/
│   │   └── common.css               # 公共样式
│   └── images/
├── components/                       # 公共组件
│   ├── common/
│   └── business/
├── composables/                     # 组合式函数
│   └── usePagination.js
├── router/                         # 路由配置
│   └── index.js
├── store/                          # 状态管理
│   └── modules/
├── utils/                          # 工具函数
│   ├── request.js                  # Axios封装
│   └── format.js                   # 格式化函数
└── views/                          # 页面视图
    ├── basedata/
    │   ├── CurrencyList.vue        # 币种列表页
    │   ├── CurrencyEdit.vue        # 币种编辑页
    │   └── SUMMARY.md              # 模块开发摘要
    ├── dealing/
    │   ├── DealList.vue           # 交易列表页
    │   ├── DealEdit.vue           # 交易编辑页
    │   └── DealDetail.vue         # 交易详情页
    └── ...
```

### 3.2 存放路径规范

| 页面类型 | 存放路径 |
|----------|----------|
| 列表页 | `web/src/views/{模块}/{Entity}List.vue` |
| 编辑页 | `web/src/views/{模块}/{Entity}Edit.vue` |
| 详情页 | `web/src/views/{模块}/{Entity}Detail.vue` |
| 组件 | `web/src/components/{类型}/{ComponentName}.vue` |
| API | `web/src/api/{模块}/{Entity}.js` |

### 3.3 开发摘要标准

每次完成一组页面开发后，生成开发摘要，存放在模块的`SUMMARY.md`：

```
# {模块名} 前端开发摘要

## 最近更新
- **日期**: YYYY-MM-DD
- **开发者**: FE
- **本次完成**: {页面列表}

## 开发过程记录

### YYYY-MM-DD - {本次主题}
**完成内容**:
- {已完成的页面1}
- {已完成的页面2}

**遇到的问题**:
- {问题1} → {解决方案}
- {问题2} → {解决方案}

**性能优化**:
- {优化项}

**待确认事项**:
- {待确认事项1}
- {待确认事项2}

### 历史记录
- YYYY-MM-DD: {开发主题} - 完成{页面列表}
```

---

## 四、执行步骤

### 步骤1：读取输入

**目的**：理解界面设计和接口需求。

**操作**：

1. 阅读UX原型文档，理解界面布局和交互
2. 阅读API接口文档，理解数据结构和接口调用
3. 检查同模块已有代码，了解项目风格
4. 确认是否有相似页面可参考

**输出**：
- 确认开发范围
- 识别开发重点和难点
- 列出需要确认的问题

### 步骤2：检查设计一致性

**目的**：确保开发符合设计规范。

**操作**：

1. 读取 `docs/原型/Open-TMS界面原型与设计规范.md`
2. 确认以下规范：
   - [ ] 配色方案
   - [ ] 字体规范
   - [ ] 间距系统
   - [ ] 组件样式
   - [ ] 交互行为

### 步骤3：API层开发

**目的**：封装后端接口调用。

**操作**：

1. 创建API文件：`web/src/api/{模块}/{Entity}.js`
2. 封装接口调用方法：
   - 列表查询
   - 详情查询
   - 新增保存
   - 更新保存
   - 删除
   - 业务操作

**API封装规范**：
```javascript
import request from '@/utils/request'

export function listCurrency(params) {
  return request({
    url: '/api/v1/currencies',
    method: 'get',
    params
  })
}

export function getCurrency(id) {
  return request({
    url: `/api/v1/currencies/${id}`,
    method: 'get'
  })
}

export function saveCurrency(data) {
  return request({
    url: '/api/v1/currencies',
    method: 'post',
    data
  })
}

export function updateCurrency(data) {
  return request({
    url: '/api/v1/currencies',
    method: 'put',
    data
  })
}

export function deleteCurrency(id) {
  return request({
    url: `/api/v1/currencies/${id}`,
    method: 'delete'
  })
}
```

### 步骤4：列表页开发

**目的**：实现数据展示和查询功能。

**操作**：

1. 创建列表页组件：`web/src/views/{模块}/{Entity}List.vue`
2. 实现以下功能：
   - 查询表单
   - 数据表格
   - 分页组件
   - 新增/编辑/删除按钮
   - 弹窗表单

**列表页模板**：
```vue
<template>
  <div class="entity-list">
    <!-- 查询区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="编码/名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="480px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 16px;">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listEntity, saveEntity, updateEntity, deleteEntity } from '@/api/{module}'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])

const queryForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({ id: null, code: '', name: '', status: '1' })

const rules = {
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑' : '新增'))
const isEdit = computed(() => !!formData.id)

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listEntity(params)
    tableData.value = res.data.records || res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, code: '', name: '', status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除吗?', '提示', { type: 'warning' })
    await deleteEntity(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateEntity(formData)
          ElMessage.success('更新成功')
        } else {
          await saveEntity(formData)
          ElMessage.success('新增成功')
        }
        drawerVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit failed:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.entity-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>
```

### 步骤5：编辑页开发（如需要）

**目的**：实现复杂表单页面。

**操作**：

1. 创建编辑页组件：`web/src/views/{模块}/{Entity}Edit.vue`
2. 实现分步向导（若需要）
3. 实现动态表单（根据类型显示不同字段）
4. 实现数据联动

**编辑页模板**：
```vue
<template>
  <div class="entity-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑' : '新建' }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button @click="handleSaveDraft" :loading="saving">暂存</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          </div>
        </div>
      </template>

      <el-steps :active="stepActive" finish-status="success" align-center>
        <el-step title="基本信息" />
        <el-step title="详细信息" />
        <el-step title="确认提交" />
      </el-steps>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="form-container">
        <!-- 步骤1：基本信息 -->
        <div v-show="stepActive === 0">
          <!-- 基本字段表单 -->
        </div>

        <!-- 步骤2：详细信息 -->
        <div v-show="stepActive === 1">
          <!-- 详细字段表单 -->
        </div>

        <!-- 步骤3：确认提交 -->
        <div v-show="stepActive === 2">
          <el-descriptions :column="2" border>
            <!-- 确认信息展示 -->
          </el-descriptions>
        </div>
      </el-form>

      <div class="step-buttons">
        <el-button v-if="stepActive > 0" @click="stepActive--">上一步</el-button>
        <el-button v-if="stepActive < 2" type="primary" @click="stepActive++">下一步</el-button>
      </div>
    </el-card>
  </div>
</template>
```

### 步骤6：组件复用

**目的**：提取公共组件，提高代码复用性。

**操作**：

1. 识别可复用组件：
   - 通用表格组件
   - 分页组件
   - 表单组件
   - 选择器组件
2. 创建公共组件：`web/src/components/`
3. 在业务页面中引用

### 步骤7：创建GitHub Project工作项

**目的**：按照团队协作规范，更新任务状态。

**操作**：

```bash
# 更新任务状态
gh issue edit <issue-number> --add-label "Done"

# 或创建新的缺陷任务
gh issue create --title "[Bug] {描述}" --body "## 缺陷描述\n..." --label "FE,Bug"
```

### 步骤8：生成开发摘要

**目的**：记录开发过程，便于追溯。

**操作**：

1. 更新模块的 `SUMMARY.md`
2. 记录：
   - 本次完成的页面
   - 遇到的问题及解决方案
   - 性能优化项
   - 待确认事项

---

## 五、业界优秀实践

### 5.1 Vue 3最佳实践

**1. Composition API**
- 使用 `<script setup>` 语法
- 逻辑复用使用 composables
- 响应式数据使用 `ref`/`reactive`

**2. 性能优化**
- 使用 `v-memo` 减少不必要的重渲染
- 路由懒加载
- 组件异步加载
- 图片懒加载

**3. 状态管理**
- 简单状态使用 `provide`/`inject`
- 复杂状态使用 Pinia
- 避免滥用全局状态

### 5.2 企业级前端特殊要求

**1. 表格性能**
- 大数据量使用虚拟滚动
- 分页加载减少单次请求数据量
- 表格列配置可持久化

**2. 表单处理**
- 动态表单生成
- 表单级联联动
- 复杂校验规则
- 暂存功能

**3. 交互体验**
- 操作反馈明确
- Loading状态展示
- 错误提示友好
- 操作确认机制

---

## 六、与其他Skill的衔接

### 6.1 前置依赖

| 前置Skill | 依赖内容 | 说明 |
|-----------|----------|------|
| UX交互设计 | 界面原型 | 明确界面布局和交互 |
| 后端接口设计 | API接口文档 | 明确接口调用方式 |

### 6.2 后续触发

| 后续Skill | 触发条件 | 输出 |
|-----------|----------|------|
| 测试用例设计 | 页面开发完成 | 测试用例 |
| 代码审查 | 代码提交 | Review意见 |

### 6.3 协作流程

```
UX原型 ──▶ 前端开发 ──▶ QA测试
             │
             ▼
          代码审查
```

---

## 七、质量标准

### 7.1 代码质量检查点

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 规范符合性 | 符合项目命名和结构规范 | 20% |
| 功能完整性 | 实现所有原型需求 | 25% |
| 交互正确性 | 符合UX交互规范 | 20% |
| 代码可维护性 | 结构清晰、可复用 | 15% |
| 错误处理 | 完善的异常处理 | 10% |
| 性能表现 | 无明显性能问题 | 10% |

### 7.2 评审通过标准

- [ ] 所有原型需求已实现
- [ ] 接口调用正确
- [ ] 交互行为符合规范
- [ ] 代码结构清晰
- [ ] 无明显bug

---

## 八、交付物检查清单

### 8.1 代码检查

- [ ] API文件创建正确
- [ ] 组件命名符合规范
- [ ] 目录结构正确
- [ ] 响应式布局正常

### 8.2 功能检查

- [ ] 列表查询正常
- [ ] 新增保存成功
- [ ] 编辑更新成功
- [ ] 删除功能正常
- [ ] 分页功能正常

### 8.3 交互检查

- [ ] 表单校验正常
- [ ] 错误提示友好
- [ ] 成功提示明确
- [ ] 加载状态展示

### 8.4 GitHub状态更新

- [ ] 任务状态已更新
- [ ] 开发摘要已记录

---

## 九、附录

### 附录A：命名规范

**文件命名**
```
# 页面组件（PascalCase）
CurrencyList.vue
DealEdit.vue

# 公共组件（PascalCase）
DataTable.vue
TreeSelector.vue

# API文件（kebab-case）
currency.js
business-unit.js
```

**变量命名**
```javascript
// 常量（全部大写）
const MAX_PAGE_SIZE = 100

// 响应式变量（ref）
const loading = ref(false)
const tableData = ref([])

// 响应式对象（reactive）
const queryForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

// 计算属性
const isEdit = computed(() => !!formData.id)
```

### 附录B：目录结构模板

```
web/src/
├── api/
│   ├── {module}/
│   │   ├── index.js          # 模块主入口
│   │   ├── {entity}.js       # 实体API
│   │   └── SUMMARY.md        # 模块摘要
│   └── common.js
├── views/
│   ├── {module}/
│   │   ├── {Entity}List.vue
│   │   ├── {Entity}Edit.vue
│   │   ├── {Entity}Detail.vue
│   │   └── SUMMARY.md
│   └── ...
└── ...
```

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |

---

## 附录E：服务管理脚本

### 前端服务管理

**脚本位置**: `.agents/skills/opentms-frontend-dev/scripts/run_frontend.py`

**功能**:
- 启动/停止/重启前端开发服务器
- 检查前端运行状态

**使用方法**:
```bash
# 启动前端 (http://localhost:3000)
python .agents/skills/opentms-frontend-dev/scripts/run_frontend.py start

# 停止前端
python .agents/skills/opentms-frontend-dev/scripts/run_frontend.py stop

# 重启前端
python .agents/skills/opentms-frontend-dev/scripts/run_frontend.py restart

# 检查状态
python .agents/skills/opentms-frontend-dev/scripts/run_frontend.py status
```

**依赖**:
- Node.js 和 npm 必须已安装
- 前端依赖已安装 (`cd web && npm install`)

**端口**: 3000 (默认)