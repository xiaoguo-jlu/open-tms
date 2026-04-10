<template>
  <div class="position-limit">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="限额类型">
          <el-select v-model="queryForm.limitType" placeholder="请选择" clearable>
            <el-option label="主体限额" value="ENTITY" />
            <el-option label="银行限额" value="BANK" />
            <el-option label="币种限额" value="CURRENCY" />
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
        <el-table-column prop="limitNo" label="限额编号" width="140" />
        <el-table-column prop="limitType" label="限额类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.limitType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="limitName" label="限额主体" min-width="150" />
        <el-table-column prop="dayLimit" label="日间限额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.dayLimit) }}
          </template>
        </el-table-column>
        <el-table-column prop="nightLimit" label="日终限额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.nightLimit) }}
          </template>
        </el-table-column>
        <el-table-column prop="warningYellow" label="黄线预警" width="100" align="center">
          <template #default="{ row }">
            {{ row.warningYellow ? row.warningYellow + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="warningRed" label="红线预警" width="100" align="center">
          <template #default="{ row }">
            {{ row.warningRed ? row.warningRed + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="限额编号" prop="limitNo">
          <el-input v-model="formData.limitNo" placeholder="唯一编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="限额类型" prop="limitType">
          <el-select v-model="formData.limitType" placeholder="请选择" style="width: 100%;" @change="handleTypeChange">
            <el-option label="主体限额" value="ENTITY" />
            <el-option label="银行限额" value="BANK" />
            <el-option label="币种限额" value="CURRENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="限额主体" prop="limitName">
          <el-select v-model="formData.limitName" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in subjectList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日间限额" prop="dayLimit">
          <el-input-number v-model="formData.dayLimit" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="日终限额" prop="nightLimit">
          <el-input-number v-model="formData.nightLimit" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="黄线预警" prop="warningYellow">
          <el-input-number v-model="formData.warningYellow" :min="0" :max="100" :controls="false" style="width: 100%;" placeholder="百分比" />
        </el-form-item>
        <el-form-item label="红线预警" prop="warningRed">
          <el-input-number v-model="formData.warningRed" :min="0" :max="100" :controls="false" style="width: 100%;" placeholder="百分比" />
        </el-form-item>
        <el-form-item label="预警方式" prop="warningMethod">
          <el-checkbox-group v-model="formData.warningMethod">
            <el-checkbox label="SYSTEM">系统消息</el-checkbox>
            <el-checkbox label="EMAIL">邮件</el-checkbox>
            <el-checkbox label="SMS">短信</el-checkbox>
          </el-checkbox-group>
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
import { listPositionLimit, savePositionLimit, updatePositionLimit, deletePositionLimit } from '@/api/cashpool'
import { listBusinessUnit, listBank, listCurrency } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const subjectList = ref([])

const queryForm = reactive({ limitType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, limitNo: '', limitType: '', limitName: '', dayLimit: 0, nightLimit: 0,
  warningYellow: null, warningRed: null, warningMethod: [], status: '1'
})

const rules = {
  limitNo: [{ required: true, message: '请输入限额编号', trigger: 'blur' }],
  limitType: [{ required: true, message: '请选择限额类型', trigger: 'change' }],
  limitName: [{ required: true, message: '请选择限额主体', trigger: 'change' }],
  dayLimit: [{ required: true, message: '请输入日间限额', trigger: 'blur' }],
  nightLimit: [{ required: true, message: '请输入日终限额', trigger: 'blur' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑限额' : '新增限额'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { ENTITY: '主体限额', BANK: '银行限额', CURRENCY: '币种限额' }
  return map[type] || type
}

const formatAmount = (amount) => {
  if (!amount) return '-'
  return '¥' + new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const handleTypeChange = async () => {
  formData.limitName = ''
  if (formData.limitType === 'ENTITY') {
    subjectList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []
  } else if (formData.limitType === 'BANK') {
    subjectList.value = (await listBank({ pageSize: 1000 })).data.list || []
  } else if (formData.limitType === 'CURRENCY') {
    subjectList.value = (await listCurrency({ pageSize: 1000 })).data.list || []
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listPositionLimit(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { limitType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, limitNo: '', limitType: '', limitName: '', dayLimit: 0, nightLimit: 0, warningYellow: null, warningRed: null, warningMethod: [], status: '1' })
  subjectList.value = []
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row, warningMethod: row.warningMethod ? row.warningMethod.split(',') : [] })
  handleTypeChange()
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该限额吗?', '提示', { type: 'warning' })
    await deletePositionLimit(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      const data = { ...formData, warningMethod: formData.warningMethod.join(',') }
      try {
        formData.id ? await updatePositionLimit(data) : await savePositionLimit(data)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.position-limit { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>