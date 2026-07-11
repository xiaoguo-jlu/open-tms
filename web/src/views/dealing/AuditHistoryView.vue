<template>
  <div class="audit-history-view one-screen">
    <!-- 顶部面包屑 + 操作 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: getDealListPath() }">{{ dealTypeLabel }}交易</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: getDetailPath() }">{{ dealTypeLabel }}交易详情</el-breadcrumb-item>
        <el-breadcrumb-item>审计历史 / V{{ version || '-' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="actions">
        <el-button :icon="ArrowLeft" size="small" @click="handleBack">返回</el-button>
        <el-button type="primary" size="small" :icon="Position" :disabled="!hasDetail" @click="handleCompareLatest">
          对比最新版本
        </el-button>
      </div>
    </div>

    <!-- 关键信息条 -->
    <div class="key-info-bar" v-if="hasDetail && deal.dealNumber">
      <div class="key-item">
        <span class="key-label">交易编号</span>
        <span class="key-value mono">{{ deal.dealNumber }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">版本</span>
        <span class="key-value mono lg">V{{ deal.version || version }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">镜像类型</span>
        <el-tag :type="getImageTypeTag(deal.imageType)" effect="dark" size="small">{{ deal.imageType || '-' }}</el-tag>
      </div>
      <div class="key-item">
        <span class="key-label">状态</span>
        <el-tag :type="getStatusType(deal.status)" effect="dark" size="small">{{ getStatusLabel(deal.status) }}</el-tag>
      </div>
      <div class="key-item">
        <span class="key-label">操作人</span>
        <span class="key-value">{{ deal.operator || '-' }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">操作时间</span>
        <span class="key-value">{{ deal.operateAt || '-' }}</span>
      </div>
    </div>

    <el-card v-loading="loading" class="content-card" :body-style="{ padding: '12px 16px' }">
      <!-- 错误提示 -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
        style="margin-bottom: 8px"
      />

      <!-- 只读模式提示 -->
      <el-alert
        v-if="hasDetail"
        type="info"
        :closable="false"
        style="margin-bottom: 12px;"
      >
        <template #title>
          <span>
            当前展示 <b>V{{ deal.version || version }}</b> 历史快照（只读，不可编辑）。
            <span v-if="deal.imageType">此版本镜像类型: <el-tag :type="getImageTypeTag(deal.imageType)" size="small">{{ deal.imageType }}</el-tag></span>
          </span>
        </template>
      </el-alert>

      <!-- 3 段折叠面板 -->
      <el-collapse v-model="activeNames">
        <!-- 段 1: Deal 基本信息 -->
        <el-collapse-item title="Deal 基本信息" name="deal">
          <el-descriptions :column="3" :size="'small'" border>
            <el-descriptions-item label="交易编号">{{ deal.dealNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="版本">V{{ deal.version || version || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(deal.status)" size="small">{{ getStatusLabel(deal.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="交易日期">{{ deal.tradeDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="起息日">{{ deal.valueDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="镜像类型">
              <el-tag :type="getImageTypeTag(deal.imageType)" size="small">{{ deal.imageType || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="操作人">{{ deal.operator || '-' }}</el-descriptions-item>
            <el-descriptions-item label="操作时间" :span="2">{{ deal.operateAt || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-if="!hasDeal" description="此版本无 Deal 镜像数据" :image-size="60" style="margin-top: 8px" />
        </el-collapse-item>

        <!-- 段 2: DealMap 字段 -->
        <el-collapse-item :title="`DealMap 字段 (${dealMapList.length})`" name="dealmap">
          <el-table :data="dealMapList" stripe size="small" max-height="360" class="compact-table">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="fieldKey" label="字段键" width="180" />
            <el-table-column prop="value" label="值" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="mono">{{ formatValue(row.value) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="currency" label="币种" width="80" align="center" />
            <el-table-column prop="version" label="版本" width="80" align="center">
              <template #default="{ row }">
                <span class="mono">V{{ row.version || deal.version || version }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="dealmapNumber" label="DealMap 编号" width="170" show-overflow-tooltip />
          </el-table>
          <el-empty
            v-if="dealMapList.length === 0"
            description="此版本无 DealMap 镜像数据"
            :image-size="60"
            style="margin-top: 8px"
          />
        </el-collapse-item>

        <!-- 段 3: 现金流 -->
        <el-collapse-item :title="`现金流 (${cashflowList.length})`" name="cashflow">
          <el-table :data="cashflowList" stripe size="small" max-height="360" class="compact-table">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="cflowNumber" label="现金流编号" width="170" />
            <el-table-column label="方向" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'" size="small">{{ row.direction }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="金额" align="right" width="160">
              <template #default="{ row }">
                <span class="mono">{{ formatAmount(row.amount, row.currency) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="currency" label="币种" width="80" align="center" />
            <el-table-column label="我方账户" width="120" align="center">
              <template #default="{ row }">
                <span v-if="row.bankAccountId" class="mono">ID:{{ row.bankAccountId }}</span>
                <span v-else style="color: #c0c4cc;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="对手方账户" width="130" align="center">
              <template #default="{ row }">
                <span v-if="row.counterpartyBankAccountId" class="mono">ID:{{ row.counterpartyBankAccountId }}</span>
                <span v-else style="color: #c0c4cc;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="imageType" label="镜像类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getImageTypeTag(row.imageType)" size="small">{{ row.imageType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="90" />
            <el-table-column prop="operateAt" label="操作时间" width="170" />
          </el-table>
          <el-empty
            v-if="cashflowList.length === 0"
            description="此版本无现金流镜像数据（早期版本未启用现金流镜像，可忽略）"
            :image-size="60"
            style="margin-top: 8px"
          />
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Position } from '@element-plus/icons-vue'
import { getVersionDetail } from '@/api/dealing/auditHistory'

const route = useRoute()
const router = useRouter()

// 状态
const dealNumber = computed(() => route.query.dealNumber || route.params.dealNumber || '')
const version = computed(() => {
  const v = route.query.version
  if (v == null || v === '') return null
  return Number(v)
})
const dealType = computed(() => {
  // 从 path 推断: /dealing/ac-deal/audit-history → ac / at / fx
  const path = route.path || ''
  if (path.startsWith('/dealing/ac-deal')) return 'ac'
  if (path.startsWith('/dealing/at-deal')) return 'at'
  if (path.startsWith('/dealing/fx-deal')) return 'fx'
  return 'ac'
})
const dealTypeLabel = computed(() => ({ ac: 'AC', at: 'AT', fx: 'FX' }[dealType.value] || 'AC'))

const loading = ref(false)
const errorMessage = ref('')
const activeNames = ref(['deal', 'dealmap', 'cashflow'])

const deal = ref({})
const dealMapList = ref([])
const cashflowList = ref([])

const hasDeal = computed(() => !!deal.value && !!deal.value.dealNumber)
const hasDetail = computed(() => hasDeal.value || dealMapList.value.length > 0 || cashflowList.value.length > 0)

// 路由 helper
const getDealListPath = () => `/dealing/${dealType.value}-deal`
const getDetailPath = () => `/dealing/${dealType.value}-deal/detail?dealNumber=${encodeURIComponent(dealNumber.value)}`

// 显示辅助
const getStatusLabel = (s) => ({ New: '新建', Approved: '已审批', Settled: '已清算', Rejected: '已驳回', Canceled: '已取消', Active: '活跃' }[s] || s || '-')
const getStatusType = (s) => ({ New: 'info', Approved: 'success', Settled: 'success', Rejected: 'danger', Canceled: 'info', Active: 'success' }[s] || 'info')
const getImageTypeTag = (t) => {
  const map = { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', RATE_FIX: 'info', STATUS_CHANGE: 'primary' }
  return map[t] || 'info'
}
const formatAmount = (amount, currency) => {
  if (amount === null || amount === undefined || amount === '') return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 18 }).format(Number(amount)) + (currency ? ' ' + currency : '')
}
const formatValue = (v) => {
  if (v === null || v === undefined || v === '') return '-'
  // 大数字(>1e6)按 8 位小数;否则 2 位
  const n = Number(v)
  if (Number.isFinite(n)) {
    if (n > 1_000_000 || n < -1_000_000 || !Number.isInteger(n)) {
      return n.toFixed(Math.abs(n) < 1 ? 8 : 2)
    }
    return n.toLocaleString('zh-CN')
  }
  return String(v)
}

const loadDetail = async () => {
  if (!dealNumber.value || version.value == null) {
    errorMessage.value = '缺少 dealNumber 或 version 参数'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await getVersionDetail(dealNumber.value, version.value)
    const d = res?.data || {}
    deal.value = d.deal || {}
    dealMapList.value = d.dealMap || []
    cashflowList.value = d.cashflows || []
    if (!hasDetail.value) {
      errorMessage.value = '未找到该版本的镜像数据'
    }
  } catch (e) {
    deal.value = {}
    dealMapList.value = []
    cashflowList.value = []
    errorMessage.value = e?.message || '加载版本详情失败'
    ElMessage.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  if (dealNumber.value) {
    router.push(getDetailPath())
  } else {
    router.push(getDealListPath())
  }
}

const handleCompareLatest = () => {
  if (!dealNumber.value) return
  // 当前 detail 接口不返回最新版本号,这里默认跳转 detail 触发重新加载
  // Phase 2 增强(对比):调 listVersions 取最大 version 后跳详情
  ElMessage.info('对比最新版本: 请先返回详情页查看最新版本号,后续 Phase 增强支持此处一键跳转')
}

onMounted(loadDetail)
</script>

<style scoped>
.audit-history-view { padding: 0; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 48px;
}
.breadcrumb { font-size: 13px; }
.actions { display: flex; gap: 8px; }

.key-info-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  height: auto;
  min-height: 80px;
  padding: 12px 20px;
  margin: 8px 0;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
  border: 1px solid #d9ecff;
  border-radius: 6px;
  gap: 24px;
}
.key-item { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.key-label { font-size: 12px; color: #909399; }
.key-value { font-size: 14px; color: #303133; font-weight: 500; }
.key-value.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
.key-value.lg { font-size: 18px; font-weight: 700; color: #409eff; }

.content-card { margin-top: 4px; }

.compact-table :deep(.el-table__row) { height: 36px; }
.compact-table :deep(.el-table__cell) { padding: 4px 0; font-size: 12px; }

.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
</style>