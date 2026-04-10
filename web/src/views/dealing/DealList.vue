<template>
  <div class="deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="交易编号">
          <el-input v-model="queryForm.dealNo" placeholder="请输入交易编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="queryForm.dealType" placeholder="请选择" clearable>
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
            <el-option label="同业" value="INTERBANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单元">
          <el-select v-model="queryForm.entityId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in entityList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易对手">
          <el-select v-model="queryForm.counterpartyId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING_APPROVE" />
            <el-option label="审批中" value="APPROVING" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已执行" value="EXECUTED" />
            <el-option label="已结算" value="SETTLED" />
            <el-option label="已撤销" value="CANCELLED" />
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
        <el-table-column prop="dealNo" label="交易编号" width="160" />
        <el-table-column prop="dealType" label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.dealType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dealSubtype" label="交易子类型" width="120" />
        <el-table-column prop="entityName" label="业务单元" width="120" />
        <el-table-column prop="counterpartyName" label="交易对手" width="120" />
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount, row.currencyCode) }}
          </template>
        </el-table-column>
        <el-table-column prop="valueDate" label="起息日" width="120" />
        <el-table-column prop="maturityDate" label="到期日" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="canEdit(row.status)">编辑</el-button>
            <el-button type="success" link @click="handleSubmit(row)" v-if="canSubmit(row.status)">提交</el-button>
            <el-button type="warning" link @click="handleCancel(row)" v-if="canCancel(row.status)">撤销</el-button>
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
import { listDeal, submitDeal, cancelDeal } from '@/api/dealing'
import { listBusinessUnit, listCounterparty } from '@/api/basedata'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const entityList = ref([])
const counterpartyList = ref([])

const queryForm = reactive({
  dealNo: '',
  dealType: '',
  entityId: '',
  counterpartyId: '',
  status: '',
  dateRange: []
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getTypeLabel = (type) => {
  const map = { DEPOSIT: '存款', LOAN: '贷款', FX: '外汇', INTERBANK: '同业' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = {
    DRAFT: '草稿', PENDING_APPROVE: '待审批', APPROVING: '审批中',
    APPROVED: '已审批', REJECTED: '已驳回', EXECUTED: '已执行',
    SETTLED: '已结算', CANCELLED: '已撤销'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    DRAFT: 'info', PENDING_APPROVE: 'warning', APPROVING: 'warning',
    APPROVED: 'success', REJECTED: 'danger', EXECUTED: 'success',
    SETTLED: 'success', CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const canEdit = (status) => ['DRAFT', 'REJECTED'].includes(status)
const canSubmit = (status) => status === 'DRAFT'
const canCancel = (status) => ['PENDING_APPROVE', 'APPROVING', 'APPROVED'].includes(status)

const fetchEntityList = async () => {
  try {
    const res = await listBusinessUnit({ pageSize: 1000 })
    entityList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const fetchCounterpartyList = async () => {
  try {
    const res = await listCounterparty({ pageSize: 1000 })
    counterpartyList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      dealNo: queryForm.dealNo,
      dealType: queryForm.dealType,
      entityId: queryForm.entityId,
      counterpartyId: queryForm.counterpartyId,
      status: queryForm.status,
      startDate: queryForm.dateRange?.[0],
      endDate: queryForm.dateRange?.[1],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listDeal(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { dealNo: '', dealType: '', entityId: '', counterpartyId: '', status: '', dateRange: [] })
  handleQuery()
}

const handleAdd = () => { router.push('/dealing/deal/edit') }

const handleView = (row) => { router.push(`/dealing/deal/detail?id=${row.id}`) }

const handleEdit = (row) => { router.push(`/dealing/deal/edit?id=${row.id}`) }

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交审批?', '提示', { type: 'warning' })
    await submitDeal(row.id)
    ElMessage.success('提交成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认撤销该交易?', '提示', { type: 'warning' })
    await cancelDeal(row.id, { reason: '' })
    ElMessage.success('撤销成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchEntityList(); fetchCounterpartyList(); fetchData() })
</script>

<style scoped>
.deal-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>