<template>
  <div class="approval-task">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable>
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
            <el-option label="转账" value="TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务编号">
          <el-input v-model="queryForm.businessNo" placeholder="请输入业务编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessNo" label="业务编号" width="160" />
        <el-table-column prop="businessTitle" label="业务标题" min-width="180" />
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount, row.currencyCode) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyUserName" label="申请人" width="100" />
        <el-table-column prop="currentNodeName" label="当前节点" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleApprove(row)" v-if="row.status === 'PENDING'">审批</el-button>
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

    <el-dialog v-model="detailVisible" title="审批详情" width="800px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="业务类型">{{ getTypeLabel(currentTask.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">{{ getStatusLabel(currentTask.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="业务编号">{{ currentTask.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentTask.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="业务标题" :span="2">{{ currentTask.businessTitle }}</el-descriptions-item>
        <el-descriptions-item label="金额" :span="2">{{ formatAmount(currentTask.amount, currentTask.currencyCode) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>审批记录</el-divider>
      <el-timeline>
        <el-timeline-item v-for="item in approvalHistory" :key="item.id" :timestamp="item.operationTime" placement="top">
          <el-card>
            <p><strong>{{ item.operationType === 'APPROVE' ? '审批通过' : item.operationType === 'REJECT' ? '审批驳回' : item.operationType }}</strong></p>
            <p>审批人: {{ item.operatorName }}</p>
            <p v-if="item.remark">审批意见: {{ item.remark }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <template #footer v-if="currentTask?.status === 'PENDING'">
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">驳回</el-button>
        <el-button type="primary" @click="handleApproveConfirm">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApprovalTask, getApprovalTask, approveTask, rejectTask, listApprovalHistory } from '@/api/approval'

const loading = ref(false)
const detailVisible = ref(false)
const tableData = ref([])
const currentTask = ref(null)
const approvalHistory = ref([])

const queryForm = reactive({ businessType: '', businessNo: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getTypeLabel = (type) => {
  const map = { DEPOSIT: '存款', LOAN: '贷款', FX: '外汇', TRANSFER: '转账' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listApprovalTask(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { businessType: '', businessNo: '', status: '' })
  handleQuery()
}

const handleView = async (row) => {
  currentTask.value = (await getApprovalTask(row.id)).data
  approvalHistory.value = (await listApprovalHistory(row.businessId)).data || []
  detailVisible.value = true
}

const handleApprove = (row) => { handleView(row) }

const handleApproveConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认审批通过?', '提示', { type: 'warning' })
    await approveTask(currentTask.value.id, { remark: '' })
    ElMessage.success('审批通过')
    detailVisible.value = false
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleReject = async () => {
  try {
    await ElMessageBox.prompt('请输入驳回原因', '驳回', { type: 'warning' })
      .then(async ({ value }) => {
        await rejectTask(currentTask.value.id, { remark: value })
        ElMessage.success('已驳回')
        detailVisible.value = false
        fetchData()
      })
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.approval-task { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>