<template>
  <div class="ac-deal-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AC 交易详情 - {{ detail.dealNumber }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="warning" @click="handleApprove">审批</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="info-section">
        <el-descriptions-item label="交易编号">{{ detail.dealNumber }}</el-descriptions-item>
        <el-descriptions-item label="业务主体">{{ detail.businessUnit }}</el-descriptions-item>
        <el-descriptions-item label="方向">
          <el-tag :type="detail.direction === 'Inflow' ? 'success' : 'danger'">
            {{ detail.direction === 'Inflow' ? '流入' : '流出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          <span class="amount">{{ formatAmount(detail.amount, detail.currency) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="币种">{{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易日期">{{ detail.dealDate }}</el-descriptions-item>
        <el-descriptions-item label="起息日">{{ detail.valueDate }}</el-descriptions-item>
        <el-descriptions-item label="最新Action">{{ detail.latestActionNumber }}</el-descriptions-item>
        <el-descriptions-item label="本方账户">{{ detail.bankAccountId }}</el-descriptions-item>
        <el-descriptions-item label="对手账户">{{ detail.counterpartyAccountId }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs-container">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建人">{{ detail.createdBy }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="更新人">{{ detail.updatedBy }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updatedAt }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- Tab 2: DealMap 时间线 -->
        <el-tab-pane :label="`DealMap (${dealMapList.length})`" name="dealmap">
          <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
            <template #title>业务事件时间线（按 event_date 排序，v2.0 由 Action 触发，软删历史保留）</template>
          </el-alert>
          <el-table :data="dealMapList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="dealmapNumber" label="DealMap 编号" width="160" />
            <el-table-column prop="actionNumber" label="Action 编号" width="160" />
            <el-table-column prop="eventType" label="事件类型" width="140" />
            <el-table-column prop="eventStatus" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.eventStatus === 'Active' ? 'success' : 'info'">{{ row.eventStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="direction" label="方向" width="80" align="center" />
            <el-table-column prop="amount" label="金额" align="right" width="160">
              <template #default="{ row }">
                <span class="amount">{{ formatAmount(row.amount, row.currency) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="eventDate" label="事件日期" width="120" align="center" />
            <el-table-column prop="isReversal" label="冲销" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isReversal === '1'" type="warning" size="small">冲销</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="deleted" label="删除" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.deleted === '1'" type="danger" size="small">软删</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: 现金流 -->
        <el-tab-pane :label="`现金流 (${cashflowList.length})`" name="cashflow">
          <el-alert type="success" :closable="false" style="margin-bottom: 12px;">
            <template #title>由 DealMap(ActualCashflow) 自动创建；UPDATE 时 dealmap_number 指向新 DealMap</template>
          </el-alert>
          <el-table :data="cashflowList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="cflowNumber" label="现金流编号" width="160" />
            <el-table-column prop="dealmapNumber" label="关联 DealMap" width="160" />
            <el-table-column prop="direction" label="方向" width="80" align="center" />
            <el-table-column prop="amount" label="金额" align="right" width="160">
              <template #default="{ row }">
                <span class="amount">{{ formatAmount(row.amount, row.currency) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="currency" label="币种" width="80" align="center" />
            <el-table-column prop="valueDate" label="起息日" width="120" align="center" />
            <el-table-column prop="sourceType" label="来源类型" width="100" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'Created' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 4: 操作历史 -->
        <el-tab-pane :label="`操作历史 (${actionList.length})`" name="action">
          <el-alert type="warning" :closable="false" style="margin-bottom: 12px;">
            <template #title>v2.0 一笔 Deal 可有多个独立 Action；审批仅更新 Action 状态，不影响 DealMap / Cashflow</template>
          </el-alert>
          <el-table :data="actionList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="actionNumber" label="Action 编号" width="160" />
            <el-table-column prop="actionType" label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getActionTypeTag(row.actionType)">{{ getActionTypeLabel(row.actionType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="operateAt" label="操作时间" width="180" />
            <el-table-column prop="approvalStatus1" label="一级审批" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getApprovalTag(row.approvalStatus1)">{{ row.approvalStatus1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approver1" label="审批人" width="100" />
            <el-table-column prop="approvalRemark" label="审批意见" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审批弹窗 -->
    <ActionApprovalDialog v-model="approvalVisible" :deal="detail" :actions="actionList" @approved="onApproved" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAcDealByNumber, listActionsByDeal } from '@/api/dealing/acDeal'
import ActionApprovalDialog from './ActionApprovalDialog.vue'

const route = useRoute()
const router = useRouter()

const detail = ref({})
const dealMapList = ref([])
const cashflowList = ref([])
const actionList = ref([])
const activeTab = ref('basic')
const approvalVisible = ref(false)

const getStatusLabel = (s) => ({ New: '新建', Approved: '已审批', Canceled: '已删除' }[s] || s)
const getStatusType = (s) => ({ New: 'info', Approved: 'success', Canceled: 'danger' }[s] || 'info')
const getActionTypeLabel = (t) => ({ CREATE: '创建', UPDATE: '修改', DELETE: '删除', APPROVE: '审批', REJECT: '驳回' }[t] || t)
const getActionTypeTag = (t) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger' }[t] || 'info')
const getApprovalTag = (s) => ({ Approved: 'success', Pending: 'warning', Rejected: 'danger' }[s] || 'info')
const formatAmount = (amount, currency) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const handleBack = () => router.push('/dealing/ac-deal')
const handleApprove = () => { approvalVisible.value = true }
const onApproved = () => {
  approvalVisible.value = false
  loadData()
}

const loadData = async () => {
  const dealNumber = route.params.dealNumber
  if (!dealNumber) return
  try {
    const res = await getAcDealByNumber(dealNumber)
    const d = res.data
    detail.value = d
    dealMapList.value = d.dealMapList || []
    cashflowList.value = d.cashflowList || []
    // Action 列表由详情接口已返回
    actionList.value = d.actionList || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.ac-deal-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-section { margin-bottom: 20px; }
.tabs-container { margin-top: 20px; }
.amount { font-family: 'JetBrains Mono', monospace; font-weight: 600; }
</style>
