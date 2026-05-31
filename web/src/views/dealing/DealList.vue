<template>
  <div class="deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="交易编号">
          <el-input v-model="queryForm.dealNumber" placeholder="请输入交易编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="queryForm.dealType" placeholder="请选择" clearable>
            <el-option label="AC交易" value="AC" />
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单元">
          <el-select v-model="queryForm.businessUnitId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in businessUnitList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易对手">
          <el-select v-model="queryForm.counterpartyId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="新建" value="New" />
            <el-option label="已提交" value="Submitted" />
            <el-option label="已审批" value="Approved" />
            <el-option label="已拒绝" value="Rejected" />
            <el-option label="已执行" value="Executed" />
            <el-option label="已结算" value="Settled" />
            <el-option label="已取消" value="Canceled" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="queryForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新建交易</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dealNumber" label="交易编号" width="160" />
        <el-table-column prop="dealType" label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.dealType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessUnit" label="业务单元" width="120" />
        <el-table-column prop="direction" label="方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'">
              {{ row.direction === 'Inflow' ? '流入' : '流出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount, row.currency) }}
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="dealDate" label="交易日期" width="120" />
        <el-table-column prop="valueDate" label="起息日" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="canEdit(row.status)">编辑</el-button>
            <el-button type="success" link @click="handleSubmit(row)" v-if="canSubmit(row.status)">提交</el-button>
            <el-button type="warning" link @click="handleApprove(row)" v-if="canApprove(row.status)">审批</el-button>
            <el-button type="danger" link @click="handleReject(row)" v-if="canReject(row.status)">拒绝</el-button>
            <el-button type="primary" link @click="handleExecute(row)" v-if="canExecute(row.status)">执行</el-button>
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
import { listDeal, submitDeal, approveDeal, rejectDeal, executeDeal } from '@/api/dealing'
import { listBusinessUnit, listCounterparty } from '@/api/basedata'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const businessUnitList = ref([])
const counterpartyList = ref([])

const queryForm = reactive({
  dealNumber: '',
  dealType: '',
  businessUnitId: '',
  counterpartyId: '',
  status: '',
  dateRange: []
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getTypeLabel = (type) => {
  const map = { AC: 'AC交易', DEPOSIT: '存款', LOAN: '贷款', FX: '外汇' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = {
    New: '新建', Submitted: '已提交', Approved: '已审批',
    Rejected: '已拒绝', Executed: '已执行', Settled: '已结算', Canceled: '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    New: 'info', Submitted: 'warning', Approved: 'success',
    Rejected: 'danger', Executed: 'success', Settled: 'success', Canceled: 'info'
  }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const canEdit = (status) => ['New', 'Rejected'].includes(status)
const canSubmit = (status) => status === 'New'
const canApprove = (status) => status === 'Submitted'
const canReject = (status) => status === 'Submitted'
const canExecute = (status) => status === 'Approved'

const fetchBusinessUnitList = async () => {
  try {
    const res = await listBusinessUnit({ pageSize: 1000 })
    businessUnitList.value = res.data.records || res.data.list || []
  } catch (e) { console.error(e) }
}

const fetchCounterpartyList = async () => {
  try {
    const res = await listCounterparty({ pageSize: 1000 })
    counterpartyList.value = res.data.records || res.data.list || []
  } catch (e) { console.error(e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      dealNumber: queryForm.dealNumber,
      dealType: queryForm.dealType,
      businessUnitId: queryForm.businessUnitId,
      counterpartyId: queryForm.counterpartyId,
      status: queryForm.status,
      startDate: queryForm.dateRange?.[0],
      endDate: queryForm.dateRange?.[1],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listDeal(params)
    tableData.value = res.data.records || res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { dealNumber: '', dealType: '', businessUnitId: '', counterpartyId: '', status: '', dateRange: [] })
  handleQuery()
}

const handleAdd = () => { router.push('/dealing/deal/form') }

const handleView = (row) => { router.push(`/dealing/deal/detail?id=${row.id}`) }

const handleEdit = (row) => { router.push(`/dealing/deal/form?id=${row.id}`) }

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交审批?', '提示', { type: 'warning' })
    await submitDeal(row.id)
    ElMessage.success('提交成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审批通过?', '提示', { type: 'warning' })
    await approveDeal(row.id)
    ElMessage.success('审批成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该交易?', '提示', { type: 'warning' })
    await rejectDeal(row.id, { reason: '' })
    ElMessage.success('拒绝成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm('确认执行该交易?', '提示', { type: 'warning' })
    await executeDeal(row.id)
    ElMessage.success('执行成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchBusinessUnitList(); fetchCounterpartyList(); fetchData() })
</script>

<style scoped>
.deal-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>