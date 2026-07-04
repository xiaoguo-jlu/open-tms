<template>
  <div class="ac-deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="交易编号">
          <el-input v-model="queryForm.keyword" placeholder="请输入交易编号" clearable @keyup.enter="handleQuery" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="queryForm.direction" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="流入" value="Inflow" />
            <el-option label="流出" value="Outflow" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="新建" value="New" />
            <el-option label="已审批" value="Approved" />
            <el-option label="已删除" value="Canceled" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理主体">
          <el-input v-model="queryForm.managementEntity" placeholder="管理主体编码" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新建 AC 交易</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
        <template #title>
          <span>基于 v2.0：创建后自动生成 DealMap(ActualCashflow) + Cashflow；修改软删旧 DealMap + 新建；删除级联软删</span>
        </template>
      </el-alert>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dealNumber" label="交易编号" width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.dealNumber }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="managementEntity" label="管理主体" width="100" />
        <el-table-column prop="direction" label="方向" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'">
              {{ row.direction === 'Inflow' ? '流入' : '流出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" align="right" width="160">
          <template #default="{ row }">
            <span class="amount">{{ formatAmount(row.amount, row.currency) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="valueDate" label="起息日" width="110" align="center" />
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center" />
        <el-table-column prop="latestActionNumber" label="最新Action" width="160" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleCopy(row)">复制</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="row.status === 'New'">编辑</el-button>
            <el-button type="warning" link @click="handleApprove(row)" v-if="row.status === 'New'">审批</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="row.status === 'New'">删除</el-button>
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

    <!-- 编辑抽屉 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="640px" destroy-on-close>
      <AcDealForm v-if="drawerVisible" :deal-data="editingDeal" @saved="onSaved" @cancel="drawerVisible = false" />
    </el-drawer>

    <!-- 审批弹窗 -->
    <ActionApprovalDialog v-model="approvalVisible" :deal="approvingDeal" @approved="onApproved" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAcDeal, deleteAcDeal, copyAcDeal } from '@/api/dealing/acDeal'
import AcDealForm from './AcDealForm.vue'
import ActionApprovalDialog from './ActionApprovalDialog.vue'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const drawerTitle = ref('新建 AC 交易')
const editingDeal = ref(null)
const approvalVisible = ref(false)
const approvingDeal = ref(null)

const queryForm = reactive({
  keyword: '',
  status: '',
  direction: '',
  managementEntity: ''
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getStatusLabel = (status) => {
  const map = { New: '新建', Approved: '已审批', Canceled: '已删除' }
  return map[status] || status
}
const getStatusType = (status) => {
  const map = { New: 'info', Approved: 'success', Canceled: 'danger' }
  return map[status] || 'info'
}
const formatAmount = (amount, currency) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listAcDeal(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }
const handleReset = () => {
  Object.assign(queryForm, { keyword: '', status: '', direction: '', managementEntity: '' })
  handleQuery()
}
const handleAdd = () => {
  editingDeal.value = null
  drawerTitle.value = '新建 AC 交易'
  drawerVisible.value = true
}
const handleCopy = async (row) => {
  try {
    const res = await copyAcDeal(row.dealNumber)
    editingDeal.value = res.data // copy API 返回的 DTO 不含 dealNumber/id，保存时会走 create 逻辑
    drawerTitle.value = `复制 AC 交易 - 基于 ${row.dealNumber}`
    drawerVisible.value = true
  } catch (e) {
    ElMessage.error('获取复制数据失败')
    console.error(e)
  }
}
const handleView = (row) => {
  router.push(`/dealing/ac-deal/detail/${row.dealNumber}`)
}
const handleEdit = (row) => {
  editingDeal.value = row
  drawerTitle.value = `编辑 AC 交易 - ${row.dealNumber}`
  drawerVisible.value = true
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除 AC 交易 ${row.dealNumber}？\n将级联软删 Deal/DealMap/Cashflow，并记录 DealImage。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteAcDeal(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
const handleApprove = (row) => {
  approvingDeal.value = row
  approvalVisible.value = true
}
const onSaved = () => {
  drawerVisible.value = false
  ElMessage.success('保存成功')
  fetchData()
}
const onApproved = () => {
  approvalVisible.value = false
  ElMessage.success('审批成功')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.ac-deal-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
.amount { font-family: 'JetBrains Mono', monospace; font-weight: 600; }
</style>
