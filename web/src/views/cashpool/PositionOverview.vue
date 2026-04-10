<template>
  <div class="position-overview">
    <el-row :gutter="16" class="summary-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <div class="card-label">集团总头寸</div>
            <div class="card-value">{{ formatAmount(totalSummary.totalBalance) }}</div>
            <div class="card-extra">可用: {{ formatAmount(totalSummary.availableBalance) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <div class="card-label">今日收入</div>
            <div class="card-value income">{{ formatAmount(totalSummary.todayIncome) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <div class="card-label">今日支出</div>
            <div class="card-value expense">{{ formatAmount(totalSummary.todayExpense) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <div class="card-label">预警数量</div>
            <div class="card-value warning" @click="handleViewAlerts">{{ totalSummary.alertCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="汇总维度">
          <el-radio-group v-model="queryForm.dimension" @change="handleDimensionChange">
            <el-radio-button value="ENTITY">业务单元</el-radio-button>
            <el-radio-button value="CURRENCY">币种</el-radio-button>
            <el-radio-button value="BANK">银行</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="summaryList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dimensionName" label="维度名称" min-width="150" />
        <el-table-column prop="totalBalance" label="总余额" align="right" width="180">
          <template #default="{ row }">
            {{ formatAmount(row.totalBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="availableBalance" label="可用余额" align="right" width="180">
          <template #default="{ row }">
            <span :class="{ 'text-warning': row.availableBalance < row.warningThreshold }">
              {{ formatAmount(row.availableBalance) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="frozenAmount" label="冻结金额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.frozenAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="inTransitAmount" label="在途资金" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.inTransitAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="accountCount" label="账户数" width="100" align="center" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="chart-card">
      <template #header>
        <span>头寸趋势 (近7天)</span>
      </template>
      <div v-loading="chartLoading" style="height: 300px;">
        <div id="trendChart" style="width: 100%; height: 100%;"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getPositionSummary, getPositionTrend } from '@/api/cashpool'

const router = useRouter()
const loading = ref(false)
const chartLoading = ref(false)
const totalSummary = ref({ totalBalance: 0, availableBalance: 0, todayIncome: 0, todayExpense: 0, alertCount: 0 })
const summaryList = ref([])
const trendData = ref([])

const queryForm = reactive({ dimension: 'ENTITY' })

const formatAmount = (amount) => {
  if (!amount) return '¥0.00'
  return '¥' + new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const fetchSummary = async () => {
  loading.value = true
  try {
    const res = await getPositionSummary({ dimension: queryForm.dimension })
    totalSummary.value = res.data.summary || {}
    summaryList.value = res.data.list || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const fetchTrend = async () => {
  chartLoading.value = true
  try {
    const res = await getPositionTrend({ days: 7 })
    trendData.value = res.data || []
    renderChart()
  } catch (e) { console.error(e) }
  finally { chartLoading.value = false }
}

const renderChart = () => {
  const chartEl = document.getElementById('trendChart')
  if (!chartEl || !trendData.value.length) return
  chartEl.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #909399;">趋势图表 (需 ECharts 库)</div>'
}

const handleDimensionChange = () => { fetchSummary() }

const handleRefresh = () => { fetchSummary(); fetchTrend() }

const handleViewAlerts = () => { router.push('/cashpool/alert') }

const handleViewDetail = (row) => { router.push({ path: '/cashpool/account', query: { dimension: queryForm.dimension, dimensionId: row.dimensionId } }) }

onMounted(() => { fetchSummary(); fetchTrend() })
</script>

<style scoped>
.position-overview { }
.summary-cards { margin-bottom: 16px; }
.summary-card { cursor: pointer; }
.card-content { text-align: center; }
.card-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.card-value { font-size: 24px; font-weight: 600; color: #303133; }
.card-value.income { color: #67C23A; }
.card-value.expense { color: #F56C6C; }
.card-value.warning { color: #E6A23C; cursor: pointer; }
.card-extra { font-size: 12px; color: #909399; margin-top: 4px; }
.filter-card { margin-bottom: 16px; }
.table-card { margin-bottom: 16px; }
.chart-card { }
.text-warning { color: #E6A23C; font-weight: 500; }
</style>