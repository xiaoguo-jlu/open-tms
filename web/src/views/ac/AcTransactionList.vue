<template>
  <div class="ac-transaction-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="AC编号">
          <el-input v-model="queryForm.acNo" placeholder="请输入AC编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="关联交易">
          <el-input v-model="queryForm.dealNo" placeholder="请输入交易编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完结" value="CLOSED" />
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
        <el-table-column prop="acNo" label="AC编号" width="160" />
        <el-table-column prop="dealNo" label="关联交易" width="160" />
        <el-table-column prop="acType" label="AC类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.acType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currencyCode" label="币种" width="80" />
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="cfCount" label="现金流数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleConfirm(row)" v-if="row.status === 'PENDING'">确认</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="AC类型" prop="acType">
          <el-select v-model="formData.acType" placeholder="请选择" style="width: 100%;">
            <el-option label="本金" value="PRINCIPAL" />
            <el-option label="利息" value="INTEREST" />
            <el-option label="费用" value="FEE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联交易" prop="dealId">
          <el-select v-model="formData.dealId" placeholder="请选择关联交易" style="width: 100%;" filterable>
            <el-option v-for="item in dealList" :key="item.id" :label="item.dealNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="币种" prop="currencyCode">
          <el-select v-model="formData.currencyCode" placeholder="请选择币种" style="width: 100%;">
            <el-option v-for="item in currencyList" :key="item.currencyCode" :label="item.currencyName" :value="item.currencyCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="起息日" prop="valueDate">
          <el-date-picker v-model="formData.valueDate" type="date" placeholder="选择起息日" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="到期日" prop="maturityDate">
          <el-date-picker v-model="formData.maturityDate" type="date" placeholder="选择到期日" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { listAcTransaction, createAcTransaction, updateAcTransaction, deleteAcTransaction, confirmAcCashflow } from '@/api/ac'
import { listDeal } from '@/api/dealing'
import { listCurrency } from '@/api/basedata'

const router = useRouter()
const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const dealList = ref([])
const currencyList = ref([])

const queryForm = reactive({ acNo: '', dealNo: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, acType: '', dealId: null, currencyCode: '', amount: 0, valueDate: '', maturityDate: '', description: ''
})

const rules = {
  acType: [{ required: true, message: '请选择AC类型', trigger: 'change' }],
  dealId: [{ required: true, message: '请选择关联交易', trigger: 'change' }],
  currencyCode: [{ required: true, message: '请选择币种', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑AC交易' : '新增AC交易'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { PRINCIPAL: '本金', INTEREST: '利息', FEE: '费用' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CLOSED: '已完结' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', CONFIRMED: 'success', CLOSED: 'info' }
  return map[status] || 'info'
}

const formatAmount = (amount) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const fetchDealList = async () => {
  dealList.value = (await listDeal({ pageSize: 1000 })).data.list || []
}

const fetchCurrencyList = async () => {
  currencyList.value = (await listCurrency({ pageSize: 1000 })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listAcTransaction(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { acNo: '', dealNo: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, acType: '', dealId: null, currencyCode: '', amount: 0, valueDate: '', maturityDate: '', description: '' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleView = (row) => { router.push(`/ac/detail?id=${row.id}`) }

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认该AC交易?', '提示', { type: 'warning' })
    await confirmAcCashflow(row.id)
    ElMessage.success('确认成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该AC交易吗?', '提示', { type: 'warning' })
    await deleteAcTransaction(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        formData.id ? await updateAcTransaction(formData) : await createAcTransaction(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchDealList(); fetchCurrencyList(); fetchData() })
</script>

<style scoped>
.ac-transaction-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>