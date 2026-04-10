<template>
  <div class="workflow-template">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="流程名称">
          <el-input v-model="queryForm.templateName" placeholder="请输入流程名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="适用业务">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable>
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
            <el-option label="转账" value="TRANSFER" />
          </el-select>
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

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="templateNo" label="流程编码" width="120" />
        <el-table-column prop="templateName" label="流程名称" min-width="150" />
        <el-table-column prop="businessType" label="适用业务" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nodeCount" label="节点数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleConfigNode(row)">配置节点</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="流程编码" prop="templateNo">
          <el-input v-model="formData.templateNo" placeholder="唯一编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="流程名称" prop="templateName">
          <el-input v-model="formData.templateName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="适用业务" prop="businessType">
          <el-select v-model="formData.businessType" placeholder="请选择" style="width: 100%;">
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
            <el-option label="转账" value="TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用机构" prop="entityIds">
          <el-select v-model="formData.entityIds" placeholder="请选择" style="width: 100%;" multiple clearable filterable>
            <el-option v-for="item in entityList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

    <el-dialog v-model="nodeVisible" title="审批节点配置" width="800px">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAddNode">添加节点</el-button>
      </div>
      <el-table :data="nodeList" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="nodeNo" label="节点编码" width="100" />
        <el-table-column prop="nodeName" label="节点名称" width="120" />
        <el-table-column prop="nodeType" label="节点类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getNodeTypeLabel(row.nodeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approverType" label="审批人类型" width="100">
          <template #default="{ row }">
            {{ getApproverTypeLabel(row.approverType) }}
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="120" />
        <el-table-column prop="timeoutHours" label="超时时间" width="100">
          <template #default="{ row }">
            {{ row.timeoutHours ? row.timeoutHours + '小时' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditNode(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDeleteNode(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkflowTemplate, saveWorkflowTemplate, updateWorkflowTemplate, deleteWorkflowTemplate, listWorkflowNode, saveWorkflowNode, deleteWorkflowNode } from '@/api/approval'
import { listBusinessUnit } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const nodeVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const entityList = ref([])
const nodeList = ref([])
const currentTemplateId = ref(null)

const queryForm = reactive({ templateName: '', businessType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, templateNo: '', templateName: '', businessType: '', entityIds: [], description: '', status: '1'
})

const rules = {
  templateNo: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  templateName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  businessType: [{ required: true, message: '请选择适用业务', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑流程模板' : '新增流程模板'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { DEPOSIT: '存款', LOAN: '贷款', FX: '外汇', TRANSFER: '转账' }
  return map[type] || type
}

const getNodeTypeLabel = (type) => {
  const map = { NORMAL: '普通', COUNTERSIGN: '会签', ORSIGN: '或签' }
  return map[type] || type
}

const getApproverTypeLabel = (type) => {
  const map = { USER: '指定用户', ROLE: '角色', UPLOAD: '上级' }
  return map[type] || type
}

const fetchEntityList = async () => {
  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listWorkflowTemplate(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { templateName: '', businessType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, templateNo: '', templateName: '', businessType: '', entityIds: [], description: '', status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row, entityIds: row.entityIds ? row.entityIds.split(',').map(Number) : [] })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该流程模板吗?', '提示', { type: 'warning' })
    await deleteWorkflowTemplate(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleConfigNode = async (row) => {
  currentTemplateId.value = row.id
  const res = await listWorkflowNode(row.id)
  nodeList.value = res.data || []
  nodeVisible.value = true
}

const handleAddNode = () => { ElMessage.info('添加节点功能') }
const handleEditNode = (row) => { ElMessage.info('编辑节点功能') }
const handleDeleteNode = async (row) => {
  try {
    await deleteWorkflowNode(currentTemplateId.value, row.id)
    ElMessage.success('删除成功')
    const res = await listWorkflowNode(currentTemplateId.value)
    nodeList.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      const data = { ...formData, entityIds: formData.entityIds.join(',') }
      try {
        formData.id ? await updateWorkflowTemplate(data) : await saveWorkflowTemplate(data)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchEntityList(); fetchData() })
</script>

<style scoped>
.workflow-template { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>