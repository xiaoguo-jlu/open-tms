<template>
  <div class="deal-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>交易详情</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border class="info-section">
        <el-descriptions-item label="交易编号">{{ detail.dealNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ getTypeLabel(detail.dealType) }}</el-descriptions-item>
        <el-descriptions-item label="交易子类型">{{ detail.dealSubtype }}</el-descriptions-item>
        <el-descriptions-item label="业务单元">{{ detail.entityName }}</el-descriptions-item>
        <el-descriptions-item label="交易员">{{ detail.dealerName }}</el-descriptions-item>
        <el-descriptions-item label="交易对手">{{ detail.counterpartyName }}</el-descriptions-item>
        <el-descriptions-item label="对手方账户">{{ detail.counterpartyAccountName }}</el-descriptions-item>
        <el-descriptions-item label="金融工具">{{ detail.instrumentName }}</el-descriptions-item>
        <el-descriptions-item label="结算币种">{{ detail.currencyCode }}</el-descriptions-item>
        <el-descriptions-item label="结算金额">{{ formatAmount(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="起息日">{{ detail.valueDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日">{{ detail.maturityDate }}</el-descriptions-item>
        <el-descriptions-item label="年利率" v-if="detail.interestRate">{{ detail.interestRate }}%</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remarks }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs-container">
        <el-tab-pane label="现金流" name="cashflow">
          <el-table :data="cashflowList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="cfDate" label="现金流日期" width="120" />
            <el-table-column prop="cfType" label="类型" width="100">
              <template #default="{ row }">
                {{ getCfTypeLabel(row.cfType) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" align="right">
              <template #default="{ row }">
                {{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="currencyCode" label="币种" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'">
                  {{ row.status === 'PAID' ? '已付' : '待付' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Deal Map" name="dealmap">
          <el-table :data="dealmapList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="acType" label="AC类型" width="120" />
            <el-table-column prop="acNo" label="AC编号" width="150" />
            <el-table-column prop="cfNo" label="现金流编号" width="150" />
            <el-table-column prop="amount" label="金额" align="right">
              <template #default="{ row }">
                {{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="审批历史" name="history">
          <el-timeline>
            <el-timeline-item v-for="item in historyList" :key="item.id" :timestamp="item.operationTime" placement="top">
              <el-card>
                <h4>{{ item.operationType === 'SUBMIT' ? '提交审批' : item.operationType === 'APPROVE' ? '审批通过' : item.operationType === 'REJECT' ? '审批驳回' : item.operationType }}</h4>
                <p>操作人: {{ item.operatorName }}</p>
                <p v-if="item.remark">备注: {{ item.remark }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDeal, getDealCashflow, getDealDealmap, getDealHistory } from '@/api/dealing'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const cashflowList = ref([])
const dealmapList = ref([])
const historyList = ref([])
const activeTab = ref('cashflow')

const getTypeLabel = (type) => {
  const map = { DEPOSIT: '存款', LOAN: '贷款', FX: '外汇', INTERBANK: '同业' }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = { DRAFT: '草稿', PENDING_APPROVE: '待审批', APPROVING: '审批中', APPROVED: '已审批', REJECTED: '已驳回', EXECUTED: '已执行', SETTLED: '已结算', CANCELLED: '已撤销' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { DRAFT: 'info', PENDING_APPROVE: 'warning', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger', EXECUTED: 'success', SETTLED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getCfTypeLabel = (type) => {
  const map = { PRINCIPAL: '本金', INTEREST: '利息', FEE: '费用' }
  return map[type] || type
}

const formatAmount = (amount) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const handleBack = () => { router.push('/dealing/deal') }

onMounted(async () => {
  const id = route.query.id
  if (id) {
    detail.value = (await getDeal(id)).data
    cashflowList.value = (await getDealCashflow(id)).data || []
    dealmapList.value = (await getDealDealmap(id)).data || []
    historyList.value = (await getDealHistory(id)).data || []
  }
})
</script>

<style scoped>
.deal-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-section { margin-bottom: 20px; }
.tabs-container { margin-top: 20px; }
</style>