<template>
  <div class="at-deal-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AT 交易详情</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border class="info-section">
        <el-descriptions-item label="交易编号">{{ detail.dealNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="转账类型">{{ getTransferTypeLabel(detail.transferType) }}</el-descriptions-item>
        <el-descriptions-item label="业务单元">{{ detail.businessUnit }}</el-descriptions-item>
        <el-descriptions-item label="源账户 ID">{{ detail.sourceAccountId }}</el-descriptions-item>
        <el-descriptions-item label="目标账户 ID">{{ detail.destAccountId }}</el-descriptions-item>
        <el-descriptions-item label="源金额" align="right">
          {{ formatAmount(detail.sourceAmount, detail.sourceCurrency) }}
        </el-descriptions-item>
        <el-descriptions-item label="目标金额" align="right">
          {{ formatAmount(detail.destAmount, detail.destCurrency) }}
        </el-descriptions-item>
        <el-descriptions-item label="汇率" align="right">{{ detail.exchangeRate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="起息日">{{ detail.valueDate }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentMethodLabel(detail.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ detail.purpose || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近 Action">{{ detail.latestActionNumber }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createdBy }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs-container">
        <!-- 双腿 DealMap 时间线 -->
        <el-tab-pane label="双腿 DealMap" name="dealmap">
          <el-timeline>
            <el-timeline-item
              v-for="dm in dealMapList"
              :key="dm.id"
              :timestamp="dm.eventDate"
              :type="dm.direction === 'Outflow' ? 'danger' : 'success'"
              placement="top"
            >
              <el-card shadow="never">
                <div class="dm-title">
                  <el-tag :type="getEventTypeTag(dm.eventType)">{{ dm.eventType }}</el-tag>
                  <el-tag :type="dm.direction === 'Outflow' ? 'danger' : 'success'">
                    {{ dm.direction === 'Outflow' ? '流出 (SOURCE)' : '流入 (DESTINATION)' }}
                  </el-tag>
                  <span class="dm-number">{{ dm.dealmapNumber }}</span>
                </div>
                <div class="dm-body">
                  <div>金额：<b>{{ formatAmount(dm.amount, dm.currency) }}</b></div>
                  <div>起息日：{{ dm.valueDate }}</div>
                  <div>状态：{{ dm.eventStatus }}</div>
                  <div>说明：{{ dm.description }}</div>
                  <div>触发 Action：{{ dm.actionNumber }}</div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <!-- Cashflow 列表 -->
        <el-tab-pane label="Cashflow" name="cashflow">
          <el-table :data="cashflowList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="cflowNumber" label="现金流编号" width="180" />
            <el-table-column prop="dealmapNumber" label="关联 DealMap" width="180" />
            <el-table-column prop="direction" label="方向" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.direction === 'Outflow' ? 'danger' : 'success'">
                  {{ row.direction === 'Outflow' ? '流出' : '流入' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bankAccount" label="本方账户" width="120" />
            <el-table-column prop="counterpartyAccount" label="对手方账户" width="120" />
            <el-table-column label="金额" align="right" width="180">
              <template #default="{ row }">
                {{ formatAmount(row.amount, row.currency) }}
              </template>
            </el-table-column>
            <el-table-column prop="valueDate" label="起息日" width="120" />
            <el-table-column prop="status" label="状态" width="100" align="center" />
            <el-table-column prop="sourceType" label="来源类型" width="120" />
          </el-table>
        </el-tab-pane>

        <!-- Action 列表（含审批/驳回） -->
        <el-tab-pane label="Action 历史" name="action">
          <el-table :data="actionList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="actionNumber" label="Action 编号" width="180" />
            <el-table-column prop="actionType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag>{{ getActionTypeLabel(row.actionType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="operateAt" label="操作时间" width="180" />
            <el-table-column prop="approvalStatus1" label="一级审批" width="100">
              <template #default="{ row }">
                <el-tag :type="getApprovalType(row.approvalStatus1)">{{ row.approvalStatus1 || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approvalStatus2" label="二级审批" width="100">
              <template #default="{ row }">
                <el-tag :type="getApprovalType(row.approvalStatus2)">{{ row.approvalStatus2 || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approvalRemark" label="审批备注" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="success"
                  link
                  :disabled="row.approvalStatus1 === 'Approved'"
                  @click="handleApprove(row)"
                >审批</el-button>
                <el-button
                  type="danger"
                  link
                  :disabled="row.approvalStatus1 === 'Rejected'"
                  @click="handleReject(row)"
                >驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 镜像快照列表 -->
        <el-tab-pane label="镜像版本" name="image">
          <el-table :data="imageList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="imageNumber" label="镜像编号" width="180" />
            <el-table-column prop="version" label="版本" width="80" align="center" />
            <el-table-column prop="imageType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.imageType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="operateAt" label="操作时间" width="180" />
            <el-table-column prop="status" label="快照状态" width="100" align="center" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAtDeal,
  listAtDealMaps,
  listAtCashflows,
  listAtActions,
  listAtImages,
  approveAtAction,
  rejectAtAction
} from '@/api/dealing'

const route = useRoute()
const router = useRouter()

const detail = ref({})
const dealMapList = ref([])
const cashflowList = ref([])
const actionList = ref([])
const imageList = ref([])
const activeTab = ref('dealmap')

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

const getActionTypeLabel = (type) => {
  const map = { CREATE: '创建', UPDATE: '修改', DELETE: '删除', APPROVE: '审批', REJECT: '驳回' }
  return map[type] || type
}

const getApprovalType = (s) => {
  if (s === 'Approved') return 'success'
  if (s === 'Rejected') return 'danger'
  return 'info'
}

const getEventTypeTag = (type) => {
  if (type === 'AccountTransfer') return ''
  if (type === 'ActualCashflow') return 'success'
  return 'info'
}

const formatAmount = (amount, currency) => {
  if (amount == null) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const handleApprove = async (row) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入审批意见', '审批通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '可填写审批备注（可选）'
    })
    await approveAtAction(row.actionNumber, { approver: 'currentUser', remark: remark || '' })
    ElMessage.success('审批成功')
    loadActions()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleReject = async (row) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (v) => (v && v.length > 0 ? true : '驳回原因必填')
    })
    await rejectAtAction(row.actionNumber, { approver: 'currentUser', remark })
    ElMessage.success('驳回成功')
    loadActions()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleBack = () => { router.push('/dealing/at-deal') }

const loadActions = async () => {
  if (detail.value.dealNumber) {
    actionList.value = (await listAtActions(detail.value.dealNumber)).data || []
  }
}

onMounted(async () => {
  const id = route.query.id
  if (id) {
    try {
      detail.value = (await getAtDeal(id)).data
      if (detail.value.dealNumber) {
        dealMapList.value = (await listAtDealMaps(detail.value.dealNumber)).data || []
        cashflowList.value = (await listAtCashflows(detail.value.dealNumber)).data || []
        actionList.value = (await listAtActions(detail.value.dealNumber)).data || []
        imageList.value = (await listAtImages(detail.value.dealNumber)).data || []
      }
    } catch (e) { console.error(e) }
  }
})
</script>

<style scoped>
.at-deal-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-section { margin-bottom: 20px; }
.tabs-container { margin-top: 20px; }
.dm-title { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.dm-number { color: #909399; font-size: 12px; }
.dm-body { line-height: 1.7; font-size: 13px; }
</style>
