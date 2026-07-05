<template>
  <div class="action-list">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-total" shadow="hover" @click="handleSwitchTab('ALL')">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">Action 全部</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-pending" shadow="hover" @click="handleSwitchTab('PENDING')">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-approved" shadow="hover" @click="handleSwitchTab('APPROVED')">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-rejected" shadow="hover" @click="handleSwitchTab('REJECTED')">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="已通过" name="APPROVED" />
        <el-tab-pane label="已驳回" name="REJECTED" />
        <el-tab-pane label="全部" name="ALL" />
      </el-tabs>
    </el-card>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="Action 编号 / 交易编号" clearable style="width: 220px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="queryForm.dealType" placeholder="全部" clearable style="width: 140px">
            <el-option label="AC 单边收付" value="AC" />
            <el-option label="AT 双边划转" value="AT" />
            <el-option label="FX 外汇" value="FX" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Action 列表 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        :row-style="{ height: '48px' }"
        :cell-style="{ padding: '6px 0' }"
        empty-text="暂无 Action 数据"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="actionNumber" label="Action 编号" width="180" />
        <el-table-column prop="dealNumber" label="交易编号" width="180" />
        <el-table-column prop="dealType" label="交易类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDealTypeTag(row.dealType)" size="small">{{ getDealTypeLabel(row.dealType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actionType" label="Action 类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="operateAt" label="操作时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.operateAt) }}</template>
        </el-table-column>
        <el-table-column prop="approvalStatus1" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalTag(row.approvalStatus1)" size="small">{{ getApprovalLabel(row.approvalStatus1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approver1" label="审批人" width="100">
          <template #default="{ row }">{{ row.approver1 || '-' }}</template>
        </el-table-column>
        <el-table-column prop="approvalRemark" label="审批意见" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.approvalRemark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button
              type="success"
              link
              size="small"
              :icon="Check"
              :disabled="row.approvalStatus1 !== 'Pending'"
              @click="handleApprove(row)"
            >审批</el-button>
            <el-button
              type="danger"
              link
              size="small"
              :icon="Close"
              :disabled="row.approvalStatus1 !== 'Pending'"
              @click="handleReject(row)"
            >驳回</el-button>
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

    <!-- 审批 / 驳回 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="Action 编号">
          <span>{{ currentAction?.actionNumber }}</span>
        </el-form-item>
        <el-form-item label="交易编号">
          <span>{{ currentAction?.dealNumber }}</span>
        </el-form-item>
        <el-form-item label="Action 类型">
          <el-tag :type="getActionTypeTag(currentAction?.actionType)" size="small">{{ getActionTypeLabel(currentAction?.actionType) }}</el-tag>
        </el-form-item>
        <el-form-item label="审批人" prop="approver">
          <el-input v-model="dialogForm.approver" placeholder="请输入审批人" />
        </el-form-item>
        <el-form-item label="审批意见" :prop="dialogMode === 'reject' ? 'approvalRemark' : ''">
          <el-input
            v-model="dialogForm.approvalRemark"
            type="textarea"
            :rows="3"
            :placeholder="dialogMode === 'reject' ? '驳回时审批意见必填' : '可选'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          :type="dialogMode === 'approve' ? 'success' : 'danger'"
          :loading="submitting"
          @click="handleDialogSubmit"
        >{{ dialogMode === 'approve' ? '审批通过' : '驳回' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, View, Check, Close,
  Document, Bell, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import {
  listPendingActions, listActionPage, getActionStats,
  approveActionV2, rejectActionV2
} from '@/api/dealing/action'

const router = useRouter()

// ====== 状态 ======
const activeTab = ref('PENDING')
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])

const queryForm = reactive({
  keyword: '',
  dealType: ''
})

const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const stats = reactive({ total: 0, pending: 0, approved: 0, rejected: 0 })

// ====== 弹窗 ======
const dialogVisible = ref(false)
const dialogMode = ref('approve') // 'approve' | 'reject'
const currentAction = ref(null)
const dialogForm = reactive({ approver: 'admin', approvalRemark: '' })
const dialogFormRef = ref(null)

const dialogRules = {
  approver: [{ required: true, message: '审批人不能为空', trigger: 'blur' }]
}

const dialogTitle = computed(() =>
  dialogMode.value === 'approve' ? '审批通过 Action' : '驳回 Action'
)

// ====== 枚举/格式化工具 ======
const getActionTypeLabel = (t) => (
  { CREATE: '创建', UPDATE: '修改', DELETE: '删除', APPROVE: '审批', REJECT: '驳回', DEAL: 'DEAL', SUBMIT: '提交', EXECUTE: '执行', CANCEL: '取消' }[t] || t || '-'
)
const getActionTypeTag = (t) => (
  { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger', DEAL: 'primary' }[t] || 'info'
)
const getDealTypeLabel = (t) => (
  { AC: 'AC 交易', AT: 'AT 转账', FX: 'FX 外汇', DEPOSIT: '存款', LOAN: '贷款' }[t] || t || '-'
)
const getDealTypeTag = (t) => (
  { AC: 'primary', AT: 'success', FX: 'warning' }[t] || 'info'
)
const getApprovalLabel = (s) => (
  { Pending: '待审批', Approved: '已通过', Rejected: '已驳回' }[s] || s || '-'
)
const getApprovalTag = (s) => (
  { Pending: 'warning', Approved: 'success', Rejected: 'danger' }[s] || 'info'
)

const formatDateTime = (val) => {
  if (!val) return '-'
  // 处理 ISO 时间字符串
  if (typeof val === 'string') {
    return val.replace('T', ' ').substring(0, 19)
  }
  return String(val)
}

// ====== 数据加载 ======
const fetchStats = async () => {
  try {
    const res = await getActionStats({ dealType: queryForm.dealType || undefined })
    Object.assign(stats, res.data || { total: 0, pending: 0, approved: 0, rejected: 0 })
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword || undefined,
      dealType: queryForm.dealType || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    let res
    if (activeTab.value === 'PENDING') {
      // 使用 /pending 端点 — 仅返回 Pending
      res = await listPendingActions(params)
    } else if (activeTab.value === 'ALL') {
      // 全部 tab — 不过滤状态
      res = await listActionPage(params)
    } else {
      // 其他 tab 使用 /page 端点 + 过滤状态（映射 Tab 值到后端实际状态字符串）
      const statusMap = { APPROVED: 'Approved', REJECTED: 'Rejected' }
      res = await listActionPage({
        ...params,
        approvalStatus: statusMap[activeTab.value] || undefined
      })
    }
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载 Action 列表失败', e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const fetchAll = async () => {
  await Promise.all([fetchData(), fetchStats()])
}

// ====== 事件处理 ======
const handleTabChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleSwitchTab = (tab) => {
  activeTab.value = tab
  handleTabChange()
}

const handleQuery = () => {
  pagination.pageNum = 1
  fetchAll()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.dealType = ''
  pagination.pageNum = 1
  fetchAll()
}

const handleView = (row) => {
  const map = {
    AC: '/dealing/ac-deal/detail',
    AT: '/dealing/at-deal/detail',
    FX: '/dealing/fx-deal/detail'
  }
  const path = map[row.dealType] || '/dealing/ac-deal/detail'
  router.push({ path, query: { dealNumber: row.dealNumber } })
}

const openDialog = (row, mode) => {
  currentAction.value = row
  dialogMode.value = mode
  dialogForm.approver = 'admin'
  dialogForm.approvalRemark = ''
  dialogVisible.value = true
}

const handleApprove = (row) => {
  if (row.approvalStatus1 !== 'Pending') {
    ElMessage.warning('该 Action 已审批')
    return
  }
  openDialog(row, 'approve')
}

const handleReject = (row) => {
  if (row.approvalStatus1 !== 'Pending') {
    ElMessage.warning('该 Action 已审批')
    return
  }
  openDialog(row, 'reject')
}

const handleDialogClose = () => {
  dialogForm.approver = 'admin'
  dialogForm.approvalRemark = ''
  currentAction.value = null
  dialogFormRef.value?.clearValidate()
}

const handleDialogSubmit = async () => {
  if (!currentAction.value) return
  try {
    await dialogFormRef.value?.validate()
  } catch (e) {
    return
  }
  if (dialogMode.value === 'reject' && !dialogForm.approvalRemark.trim()) {
    ElMessage.error('驳回时审批意见必填')
    return
  }

  submitting.value = true
  try {
    const fn = dialogMode.value === 'approve' ? approveActionV2 : rejectActionV2
    await fn(currentAction.value.actionNumber, {
      approver: dialogForm.approver,
      approvalRemark: dialogForm.approvalRemark
    })
    ElMessage.success(dialogMode.value === 'approve' ? '审批通过成功' : '驳回成功')
    dialogVisible.value = false
    await fetchAll()
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
.action-list { }
.stats-row { margin-bottom: 16px; }
.stat-card {
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ebeef5;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}
.stat-total .stat-icon { background: #909399; }
.stat-pending .stat-icon { background: #e6a23c; }
.stat-approved .stat-icon { background: #67c23a; }
.stat-rejected .stat-icon { background: #f56c6c; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.tab-card { margin-bottom: 16px; }
.tab-card :deep(.el-card__body) { padding: 0 20px; }
.tab-card :deep(.el-tabs__nav-wrap::after) { height: 0; }

.filter-card { margin-bottom: 16px; }
.table-card { }

/* 表格行高 48px */
:deep(.el-table__row) { height: 48px; }
:deep(.el-table__row td) { padding: 6px 0; }
</style>