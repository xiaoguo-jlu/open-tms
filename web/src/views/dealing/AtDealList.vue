<template>
  <div class="at-deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="交易编号">
          <el-input v-model="queryForm.keyword" placeholder="请输入交易编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="转账类型">
          <el-select v-model="queryForm.transferType" placeholder="请选择" clearable>
            <el-option label="同公司" value="SAME_COMPANY" />
            <el-option label="跨公司" value="CROSS_COMPANY" />
            <el-option label="跨境" value="CROSS_BORDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="新建" value="New" />
            <el-option label="已审批" value="Approved" />
            <el-option label="已驳回" value="Rejected" />
            <el-option label="已清算" value="Settled" />
            <el-option label="已取消" value="Canceled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新建 AT 交易</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dealNumber" label="交易编号" width="160" />
        <el-table-column label="转账类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTransferTypeLabel(row.transferType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceAccountId" label="源账户" width="100" align="center" />
        <el-table-column prop="destAccountId" label="目标账户" width="100" align="center" />
        <el-table-column label="源金额" align="right" width="180">
          <template #default="{ row }">
            {{ formatAmount(row.sourceAmount, row.sourceCurrency) }}
          </template>
        </el-table-column>
        <el-table-column label="目标金额" align="right" width="180">
          <template #default="{ row }">
            {{ formatAmount(row.destAmount, row.destCurrency) }}
          </template>
        </el-table-column>
        <el-table-column label="汇率" align="right" width="100">
          <template #default="{ row }">
            {{ row.exchangeRate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="valueDate" label="起息日" width="120" />
        <el-table-column label="支付方式" width="100" align="center">
          <template #default="{ row }">
            {{ getPaymentMethodLabel(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleCopy(row)">复制</el-button>
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="canEdit(row.status)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="canDelete(row.status)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageAtDeals, deleteAtDeal } from '@/api/dealing'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  keyword: '',
  transferType: '',
  status: ''
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getTransferTypeLabel = (type) => {
  const map = { SAME_COMPANY: '同公司', CROSS_COMPANY: '跨公司', CROSS_BORDER: '跨境' }
  return map[type] || type
}

const getPaymentMethodLabel = (method) => {
  const map = { INTERNAL: '内部转账', SWIFT: 'SWIFT 电汇', RTGS: 'RTGS 实时结算' }
  return map[method] || method
}

const getStatusLabel = (status) => {
  const map = {
    New: '新建', Approved: '已审批', Rejected: '已驳回',
    Settled: '已清算', Canceled: '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    New: 'info', Approved: 'success', Rejected: 'danger',
    Settled: 'success', Canceled: 'info'
  }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (amount == null) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const canEdit = (status) => status === 'New' || status === 'Rejected'
const canDelete = (status) => status === 'New' || status === 'Rejected'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageAtDeals({
      keyword: queryForm.keyword,
      transferType: queryForm.transferType,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records || res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { keyword: '', transferType: '', status: '' })
  handleQuery()
}

const handleAdd = () => { router.push('/dealing/at-deal/form') }

const handleCopy = (row) => { router.push(`/dealing/at-deal/form?copyFrom=${row.dealNumber}`) }

const handleView = (row) => { router.push(`/dealing/at-deal/detail?id=${row.id}`) }

const handleEdit = (row) => { router.push(`/dealing/at-deal/form?id=${row.id}`) }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除交易 ${row.dealNumber}?`, '提示', { type: 'warning' })
    await deleteAtDeal(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.at-deal-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>
