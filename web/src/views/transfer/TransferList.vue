<template>
  <div class="transfer-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="转账编号">
          <el-input v-model="queryForm.transferNo" placeholder="请输入转账编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="转账类型">
          <el-select v-model="queryForm.transferType" placeholder="请选择" clearable>
            <el-option label="行内转账" value="INTERNAL" />
            <el-option label="跨行转账" value="EXTERNAL" />
            <el-option label="同城转账" value="SAME_CITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING_APPROVE" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="执行中" value="EXECUTING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
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
        <el-table-column prop="transferNo" label="转账编号" width="160" />
        <el-table-column prop="transferType" label="转账类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.transferType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fromAccountName" label="转出账户" min-width="150" />
        <el-table-column prop="toAccountName" label="转入账户" min-width="150" />
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount, row.currencyCode) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="canEdit(row.status)">编辑</el-button>
            <el-button type="success" link @click="handleExecute(row)" v-if="canExecute(row.status)">执行</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="560px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="转账类型" prop="transferType">
          <el-select v-model="formData.transferType" placeholder="请选择" style="width: 100%;">
            <el-option label="行内转账" value="INTERNAL" />
            <el-option label="跨行转账" value="EXTERNAL" />
            <el-option label="同城转账" value="SAME_CITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="转出账户" prop="fromAccountId">
          <el-select v-model="formData.fromAccountId" placeholder="请选择转出账户" style="width: 100%;" filterable>
            <el-option v-for="item in accountList" :key="item.id" :label="`${item.accountName} (${item.account})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入账户" prop="toAccountId">
          <el-select v-model="formData.toAccountId" placeholder="请选择转入账户" style="width: 100%;" filterable>
            <el-option v-for="item in accountList" :key="item.id" :label="`${item.accountName} (${item.account})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转账金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="手续费" prop="fee">
          <el-input-number v-model="formData.fee" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预约时间" prop="scheduledTime">
          <el-date-picker v-model="formData.scheduledTime" type="datetime" placeholder="选择预约时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input v-model="formData.purpose" type="textarea" :rows="2" placeholder="请输入用途" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTransferTransaction, createTransferTransaction, updateTransferTransaction, executeTransferTransaction, listAccount } from '@/api/transfer'

const router = useRouter()
const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const accountList = ref([])

const queryForm = reactive({ transferNo: '', transferType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, transferType: 'INTERNAL', fromAccountId: null, toAccountId: null, amount: 0, fee: 0, scheduledTime: '', purpose: '', remark: ''
})

const rules = {
  transferType: [{ required: true, message: '请选择转账类型', trigger: 'change' }],
  fromAccountId: [{ required: true, message: '请选择转出账户', trigger: 'change' }],
  toAccountId: [{ required: true, message: '请选择转入账户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入转账金额', trigger: 'blur' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑转账' : '新增转账'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { INTERNAL: '行内转账', EXTERNAL: '跨行转账', SAME_CITY: '同城转账' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = { DRAFT: '草稿', PENDING_APPROVE: '待审批', APPROVED: '已审批', EXECUTING: '执行中', SUCCESS: '成功', FAILED: '失败' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { DRAFT: 'info', PENDING_APPROVE: 'warning', APPROVED: 'success', EXECUTING: 'warning', SUCCESS: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || 'CNY')
}

const canEdit = (status) => ['DRAFT', 'FAILED'].includes(status)
const canExecute = (status) => status === 'APPROVED'

const fetchAccountList = async () => {
  accountList.value = (await listAccount({ pageSize: 1000 })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listTransferTransaction(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { transferNo: '', transferType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, transferType: 'INTERNAL', fromAccountId: null, toAccountId: null, amount: 0, fee: 0, scheduledTime: '', purpose: '', remark: '' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleView = (row) => { router.push(`/transfer/detail?id=${row.id}`) }

const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm('确认执行该转账?', '提示', { type: 'warning' })
    await executeTransferTransaction(row.id)
    ElMessage.success('执行成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        formData.id ? await updateTransferTransaction(formData) : await createTransferTransaction(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchAccountList(); fetchData() })
</script>

<style scoped>
.transfer-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>