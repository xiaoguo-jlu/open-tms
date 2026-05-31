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
        <el-descriptions-item label="交易编号">{{ detail.dealNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ getTypeLabel(detail.dealType) }}</el-descriptions-item>
        <el-descriptions-item label="业务单元">{{ detail.businessUnit }}</el-descriptions-item>
        <el-descriptions-item label="交易对手">{{ detail.counterpartyName }}</el-descriptions-item>
        <el-descriptions-item label="对手方账户">{{ detail.counterpartyAccountName }}</el-descriptions-item>
        <el-descriptions-item label="金融工具">{{ detail.instrumentName }}</el-descriptions-item>
        <el-descriptions-item label="交易员">{{ detail.traderName }}</el-descriptions-item>
        <el-descriptions-item label="方向">
          <el-tag :type="detail.direction === 'Inflow' ? 'success' : 'danger'">
            {{ detail.direction === 'Inflow' ? '流入' : '流出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="本方账户">{{ detail.bankAccountName }}</el-descriptions-item>
        <el-descriptions-item label="交易金额" align="right">
          {{ formatAmount(detail.amount, detail.currency) }}
        </el-descriptions-item>
        <el-descriptions-item label="币种">{{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="交易日期">{{ detail.dealDate }}</el-descriptions-item>
        <el-descriptions-item label="起息日">{{ detail.valueDate }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ detail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs-container">
        <el-tab-pane label="Action历史" name="action">
          <el-table :data="actionList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="actionType" label="Action类型" width="120">
              <template #default="{ row }">
                {{ getActionTypeLabel(row.actionType) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="120" />
            <el-table-column prop="operationTime" label="操作时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getActionStatusType(row.status)">{{ getActionStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="镜像版本" name="image">
          <el-table :data="imageList" stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="version" label="版本号" width="100" />
            <el-table-column prop="createdBy" label="创建人" width="120" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getImageStatusType(row.status)">{{ getImageStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewImage(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDeal, listActionByDeal, listImageByDeal } from '@/api/dealing'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const actionList = ref([])
const imageList = ref([])
const activeTab = ref('action')

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

const getActionTypeLabel = (type) => {
  const map = { SUBMIT: '提交', APPROVE: '审批通过', REJECT: '审批拒绝', EXECUTE: '执行', CANCEL: '取消' }
  return map[type] || type
}

const getActionStatusLabel = (status) => {
  const map = { PENDING: '待处理', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const getActionStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const getImageStatusLabel = (status) => {
  const map = { ACTIVE: '当前版本', ARCHIVED: '归档' }
  return map[status] || status
}

const getImageStatusType = (status) => {
  const map = { ACTIVE: 'success', ARCHIVED: 'info' }
  return map[status] || 'info'
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + ' ' + (currency || '')
}

const handleViewImage = (row) => {
  router.push(`/dealing/deal/image?dealNumber=${detail.value.dealNumber}&version=${row.version}`)
}

const handleBack = () => { router.push('/dealing/deal') }

onMounted(async () => {
  const id = route.query.id
  if (id) {
    try {
      detail.value = (await getDeal(id)).data
      // 加载Action历史
      if (detail.value.dealNumber) {
        actionList.value = (await listActionByDeal(detail.value.dealNumber)).data || []
        imageList.value = (await listImageByDeal(detail.value.dealNumber)).data || []
      }
    } catch (e) { console.error(e) }
  }
})
</script>

<style scoped>
.deal-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-section { margin-bottom: 20px; }
.tabs-container { margin-top: 20px; }
</style>