<template>
  <div class="action-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="Action类型">
          <el-select v-model="queryForm.actionType" placeholder="请选择" clearable>
            <el-option label="提交" value="SUBMIT" />
            <el-option label="审批通过" value="APPROVE" />
            <el-option label="审批拒绝" value="REJECT" />
            <el-option label="执行" value="EXECUTE" />
           <el-option label="取消" value="CANCEL" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
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
        <el-table-column prop="actionType" label="Action类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getActionTypeLabel(row.actionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dealNumber" label="交易编号" width="160" />
        <el-table-column prop="dealType" label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.dealType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessUnit" label="业务单元" width="120" />
        <el-table-column prop="amount" label="金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.amount, row.currency) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="status" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
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

    <!-- Action详情弹窗 -->
    <el-dialog v-model="detailVisible" title="Action详情" width="600px">
      <el-descriptions :column="2" border v-if="currentAction">
        <el-descriptions-item label="Action类型">{{ getActionTypeLabel(currentAction.actionType) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(currentAction.status)">{{ getStatusLabel(currentAction.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易编号">{{ currentAction.dealNumber }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ getTypeLabel(currentAction.dealType) }}</el-descriptions-item>
        <el-descriptions-item label="业务单元">{{ currentAction.businessUnit }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatAmount(currentAction.amount, currentAction.currency) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentAction.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentAction.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAction.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
       <el-button type="success" @click="handleApprove" v-if="currentAction && currentAction.status === 'PENDING'">审批通过</el-button>
        <el-button type="danger" @click="handleReject" v-if="currentAction && currentAction.status === 'PENDING'">审批拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAction, approveDeal, rejectDeal } from '@/api/dealing'

const loading = ref(false)
const detailVisible = ref(false)
const currentAction = ref(null)
const tableData = ref([])

const queryForm = reactive({
  actionType: '',
  status: 'PENDING'
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getActionTypeLabel = (type) => {
  const map = { SUBMIT: '提交', APPROVE: '审批通过', REJECT: '审批拒绝', EXECUTE: '执行', CANCEL: '取消' }
  return map[type] || type
}

const getTypeLabel = (type) => {
  const map = { AC: 'AC交易', DEPOSIT: '存款', LOAN: '贷款', FX: '外汇' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
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
    const params = {
      actionType: queryForm.actionType,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listAction(params)
    tableData.value = res.data.records || res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { actionType: '', status: '' })
  handleQuery()
}

const handleView = (row) => {
  currentAction.value = row
  detailVisible.value = true
}

const handleApprove = async () => {
  if (!currentAction.value) return
  try {
    await ElMessageBox.confirm('确认审批通过?', '提示', { type: 'warning' })
    await approveDeal(currentAction.value.dealId)
    ElMessage.success('审批成功')
    detailVisible.value = false
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleReject = async () => {
  if (!currentAction.value) return
  try {
    await ElMessageBox.confirm('确认拒绝?', '提示', { type: 'warning' })
    await rejectDeal(currentAction.value.dealId, { reason: '' })
    ElMessage.success('拒绝成功')
    detailVisible.value = false
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.action-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>