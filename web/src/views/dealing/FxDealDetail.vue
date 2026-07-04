<template>
  <div class="fx-deal-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>FX 交易详情 - {{ detail.dealNumber }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button v-if="isNdf && !detail.fixingRate" type="warning" @click="rateFixVisible = true">RATE_FIX</el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息摘要 -->
      <el-descriptions :column="3" border class="info-section">
        <el-descriptions-item label="交易编号">{{ detail.dealNumber }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">
          <el-tag :type="getProductTypeTag(detail.productType)">{{ detail.productType || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="卖出">
          <span class="amount">{{ formatAmount(detail.sellAmount) }} {{ detail.sellCurrency }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="买入">
          <span class="amount">{{ formatAmount(detail.buyAmount) }} {{ detail.buyCurrency }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="成交汇率">{{ detail.exchangeRate ? detail.exchangeRate.toFixed(8) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="市场汇率">{{ detail.marketRate ? detail.marketRate.toFixed(8) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="点差(bp)">{{ detail.spreadBp ? detail.spreadBp.toFixed(4) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易日">{{ detail.tradeDate }}</el-descriptions-item>
        <el-descriptions-item label="交割日">{{ detail.valueDate }}</el-descriptions-item>
        <el-descriptions-item label="期限(天)">{{ detail.termDays ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期日">{{ detail.maturityDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- NDF 特有字段 -->
      <el-descriptions v-if="detail.fixingSource || detail.fixingRate" :column="2" border style="margin-top: 12px;">
        <el-descriptions-item label="Fixing 来源">{{ detail.fixingSource }}</el-descriptions-item>
        <el-descriptions-item label="名义本金">{{ formatAmount(detail.notional) }}</el-descriptions-item>
        <el-descriptions-item label="Fixing 汇率">
          <span v-if="detail.fixingRate" class="amount">{{ detail.fixingRate.toFixed(8) }}</span>
          <span v-else style="color: #909399;">待 RATE_FIX</span>
        </el-descriptions-item>
        <el-descriptions-item label="结算金额">
          <span v-if="detail.settlementAmount != null" :class="detail.settlementAmount >= 0 ? 'positive' : 'negative'">
            {{ formatAmount(detail.settlementAmount) }}
          </span>
          <span v-else style="color: #909399;">-</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" class="tabs-container">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="管理主体">{{ entityNames.managementEntity || (detail.managementEntityId ? 'ID:' + detail.managementEntityId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="交易对手">{{ entityNames.counterparty || (detail.counterpartyId ? 'ID:' + detail.counterpartyId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="交易员">{{ entityNames.trader || (detail.traderId ? 'ID:' + detail.traderId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="金融工具">{{ entityNames.instrument || (detail.instrumentId ? 'ID:' + detail.instrumentId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="币种对">{{ entityNames.currencyPair || (detail.currencyPairId ? 'ID:' + detail.currencyPairId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="最新 Action">{{ detail.latestActionNumber }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ detail.createdBy }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="更新人">{{ detail.updatedBy }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updatedAt }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- Tab 2: DealMap -->
        <el-tab-pane :label="`DealMap (${dealMapList.length})`" name="dealmap">
          <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
            <template #title>
              v3.2: 3-4 行 DealMap(BUY/SELL/RATE[/FIX])，每行 1 字段 amount_or_rate，dealmap_type 区分。1 DealMap 最多生成 1 条 CF。
            </template>
          </el-alert>
          <el-table :data="dealMapList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="dealmapNumber" label="DealMap 编号" width="170" />
            <el-table-column prop="actionNumber" label="Action 编号" width="170" />
            <el-table-column label="DealMap 类型" width="160" align="center">
              <template #default="{ row }">
                <el-tag :type="getDealmapTypeTag(row.dealmapType)" size="small">{{ row.dealmapType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amountOrRate" label="amount_or_rate" align="right" width="200">
              <template #default="{ row }">
                <span class="amount" v-if="row.dealmapType === 'FX_RATE' || row.dealmapType === 'FX_FIX'">
                  {{ row.amountOrRate ? parseFloat(row.amountOrRate).toFixed(8) : '-' }}
                </span>
                <span class="amount" v-else>
                  {{ formatAmount(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="触发 CF" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.dealmapType === 'FX_RATE'" type="info" size="small">否</el-tag>
                <el-tag v-else-if="getCflowForDealmap(row.dealmapNumber)" type="success" size="small">是</el-tag>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="direction" label="方向" width="80" align="center" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: Cashflow -->
        <el-tab-pane :label="`Cashflow (${cashflowList.length})`" name="cashflow">
          <el-alert type="success" :closable="false" style="margin-bottom: 12px;">
            <template #title>
              v3.2: 一条 Cashflow 最多由 1 条 DealMap 触发。CF 通过 dealmap_number 反向关联 DealMap。
            </template>
          </el-alert>
          <el-table :data="cashflowList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="cflowNumber" label="现金流编号" width="170" />
            <el-table-column prop="dealmapNumber" label="触发 DealMap" width="170">
              <template #default="{ row }">
                <span class="amount">{{ row.dealmapNumber }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="direction" label="方向" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'" size="small">{{ row.direction }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" align="right" width="180">
              <template #default="{ row }">
                <span class="amount">{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="currency" label="币种" width="80" align="center" />
            <el-table-column prop="valueDate" label="起息日" width="110" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'Created' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 4: Action -->
        <el-tab-pane :label="`Action (${actionList.length})`" name="action">
          <el-alert type="warning" :closable="false" style="margin-bottom: 12px;">
            <template #title>v3.2: FX Action 共 4 种 - DEAL / UPDATE / DELETE / RATE_FIX（无审批流）</template>
          </el-alert>
          <el-table :data="actionList" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="actionNumber" label="Action 编号" width="170" />
            <el-table-column label="类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getActionTypeTag(row.actionType)">{{ getActionTypeLabel(row.actionType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="operateAt" label="操作时间" width="180" />
            <el-table-column prop="actionStatus" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.actionStatus === 'Approved' ? 'success' : 'warning'" size="small">{{ row.actionStatus }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- RATE_FIX 对话框 -->
    <el-dialog v-model="rateFixVisible" title="NDF RATE_FIX" width="420px" destroy-on-close>
      <el-form :model="rateFixForm" label-width="100px">
        <el-form-item label="Fixing 汇率">
          <el-input-number v-model="rateFixForm.fixingRate" :min="0" :precision="8" :controls="false" style="width: 100%;" placeholder="例: 7.1500" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="rateFixForm.operator" placeholder="操作人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateFixVisible = false">取消</el-button>
        <el-button type="primary" :loading="rateFixing" @click="doRateFix">确认 RATE_FIX</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFxDeal, rateFixFxDeal } from '@/api/dealing/fxDeal'
import { getManagementEntity } from '@/api/basedata/managementEntity'
import { getCounterparty } from '@/api/basedata/counterparty'
import { getTrader } from '@/api/basedata/trader'
import { getInstrument } from '@/api/basedata/instrument'
import { getCurrencyPair } from '@/api/basedata/currencyPair'

const route = useRoute()
const router = useRouter()

const detail = ref({})
const dealMapList = ref([])
const cashflowList = ref([])
const actionList = ref([])
const activeTab = ref('basic')

// 实体名称缓存（用于显示名称而非 ID）
const entityNames = reactive({
  managementEntity: '',
  counterparty: '',
  trader: '',
  instrument: '',
  currencyPair: ''
})

// RATE_FIX
const rateFixVisible = ref(false)
const rateFixing = ref(false)
const rateFixForm = ref({ fixingRate: null, operator: 'admin' })

const isNdf = computed(() => {
  return detail.value.fixingSource && detail.value.fixingSource.length > 0
})

const getStatusLabel = (s) => ({ New: '新建', Active: '活跃', Canceled: '已删除' }[s] || s)
const getStatusType = (s) => ({ New: 'info', Active: 'success', Canceled: 'danger' }[s] || 'info')
const getProductTypeTag = (t) => ({ SPOT: 'success', FWD: 'warning', NDF: 'info' }[t] || 'info')
const getActionTypeLabel = (t) => ({ DEAL: '创建', UPDATE: '修改', DELETE: '删除', RATE_FIX: 'Fixing' }[t] || t)
const getActionTypeTag = (t) => ({ DEAL: 'success', UPDATE: 'warning', DELETE: 'danger', RATE_FIX: 'info' }[t] || 'info')
const getDealmapTypeTag = (t) => {
  if (!t) return 'info'
  if (t.startsWith('FX_BUY')) return 'success'
  if (t.startsWith('FX_SELL')) return 'warning'
  if (t === 'FX_RATE') return 'info'
  if (t === 'FX_FIX') return 'danger'
  return 'info'
}
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const getCflowForDealmap = (dealmapNumber) => {
  return cashflowList.value.find(c => c.dealmapNumber === dealmapNumber)
}

const handleBack = () => router.push('/dealing/fx-deal')

const doRateFix = async () => {
  if (!rateFixForm.value.fixingRate) {
    ElMessage.error('请输入 Fixing 汇率')
    return
  }
  rateFixing.value = true
  try {
    const res = await rateFixFxDeal(detail.value.id, rateFixForm.value)
    ElMessage.success(`RATE_FIX 完成，差额: ${res.data?.settlementAmount}`)
    rateFixVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.message || 'RATE_FIX 失败')
  } finally {
    rateFixing.value = false
  }
}

const loadData = async () => {
  const dealNumber = route.query.dealNumber
  if (!dealNumber) return
  try {
    const res = await getFxDeal(dealNumber)
    const d = res.data
    detail.value = d
    dealMapList.value = d.dealMapList || []
    cashflowList.value = d.cashflowList || []
    actionList.value = d.actionList || []
    // 异步解析实体名称
    resolveEntityNames(d)
  } catch (e) {
    console.error(e)
  }
}

// 根据 ID 异步获取各实体名称
const resolveEntityNames = async (d) => {
  const fetchers = []
  // managementEntityId
  if (d.managementEntityId) {
    fetchers.push(
      getManagementEntity(d.managementEntityId).then(r => {
        const data = r?.data || r
        entityNames.managementEntity = data.name || data.code || ''
      }).catch(() => {})
    )
  }
  // counterpartyId
  if (d.counterpartyId) {
    fetchers.push(
      getCounterparty(d.counterpartyId).then(r => {
        const data = r?.data || r
        entityNames.counterparty = data.name || data.code || ''
      }).catch(() => {})
    )
  }
  // traderId
  if (d.traderId) {
    fetchers.push(
      getTrader(d.traderId).then(r => {
        const data = r?.data || r
        entityNames.trader = data.name || data.code || ''
      }).catch(() => {})
    )
  }
  // instrumentId
  if (d.instrumentId) {
    fetchers.push(
      getInstrument(d.instrumentId).then(r => {
        const data = r?.data || r
        entityNames.instrument = data.instrumentName || data.instrumentCode || ''
      }).catch(() => {})
    )
  }
  // currencyPairId
  if (d.currencyPairId) {
    fetchers.push(
      getCurrencyPair(d.currencyPairId).then(r => {
        const data = r?.data || r
        entityNames.currencyPair = data.pairCode || ''
      }).catch(() => {})
    )
  }
  await Promise.allSettled(fetchers)
}

onMounted(loadData)
</script>

<style scoped>
.fx-deal-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-section { margin-bottom: 12px; }
.tabs-container { margin-top: 20px; }
.amount { font-family: 'JetBrains Mono', monospace; font-weight: 600; }
.positive { color: #67c23a; font-weight: 600; }
.negative { color: #f56c6c; font-weight: 600; }
</style>