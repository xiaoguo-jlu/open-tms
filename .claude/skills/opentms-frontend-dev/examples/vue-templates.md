# Vue Templates Reference

> 源文件: SKILL.md 步骤3-5 的代码模板，此处集中存放，主文件通过引用使用。

## API 封装模板

```javascript
import request from '@/utils/request'

export function listEntity(params) {
  return request({
    url: '/api/v1/entities/page',
    method: 'get',
    params
  })
}

export function getEntity(id) {
  return request({
    url: `/api/v1/entities/${id}`,
    method: 'get'
  })
}

export function saveEntity(data) {
  return request({
    url: '/api/v1/entities',
    method: 'post',
    data
  })
}

export function updateEntity(data) {
  return request({
    url: '/api/v1/entities/update',
    method: 'post',
    data
  })
}

export function deleteEntity(id) {
  return request({
    url: `/api/v1/entities/delete/${id}`,
    method: 'post'
  })
}
```

## 列表页模板 (Drawer 弹窗式)

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

    <!-- 编辑弹窗 (Drawer) -->
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

const handleQuery = () => { pagination.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.keyword = ''; queryForm.status = ''; handleQuery() }

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
    if (error !== 'cancel') console.error('Delete failed:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) { await updateEntity(formData); ElMessage.success('更新成功') }
      else { await saveEntity(formData); ElMessage.success('新增成功') }
      drawerVisible.value = false
      fetchData()
    } catch (error) { console.error('Submit failed:', error) }
    finally { submitLoading.value = false }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.filter-card { margin-bottom: 16px; }
</style>
```

## 编辑页模板 (Steps 分步式)

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
        <div v-show="stepActive === 0"><!-- 基本字段 --></div>
        <div v-show="stepActive === 1"><!-- 详细字段 --></div>
        <div v-show="stepActive === 2">
          <el-descriptions :column="2" border><!-- 确认信息 --></el-descriptions>
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

## 4 模式详情页模板

用于 `new` / `copy` / `edit` / `readonly` 四种模式的统一页面。详见 SKILL.md 步骤10 "关键模式 - 4 模式详情页"。
