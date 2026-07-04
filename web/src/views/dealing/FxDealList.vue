<template>
  <div class="fx-deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="管理主体">
          <el-input-number v-model="queryForm.managementEntityId" placeholder="ID" :min="1" style="width: 120px;" />
        </el-form-item>
        <el-form-item label="对手方">
          <el-input-number v-model="queryForm.counterpartyId" placeholder="ID" :min="1" style="width: 120px;" />
        </el-form-item>
        <el-form-item label="产品类型">
          <el-select v-model="queryForm.productType" placeholder="全部" clearable style="width: 100px;">
            <el-option label="SPOT" value="SPOT" />
            <el-option label="FWD" value="FWD" />
            <el-option label="NDF" value="NDF" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px;">
            <el-option label="New" value="New" />
            <el-option label="Active" value="Active" />
            <el-option label="Canceled" value="Canceled" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 260px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新建 FX 交易</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
        <template #title>
          <span>v3.2: DX 创建即生成 3 DealMap + 0/2 Cashflow(NDF 等 RATE_FIX)；后端 calculate 联动计算</span>
        </template>
      </el-alert>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dealNumber" label="交易编号" width="170">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.dealNumber }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="productType" label="产品类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getProductTypeTag(row.productType)" size="small">{{ row.productType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="卖出" width="180">
          <template #default="{ row }">
            <span class="amount">{{ formatAmount(row.sellAmount) }}</span>
            <span class="ccy"> {{ row.sellCurrency }}</span>
          </template>
        </el-table-column>
        <el-table-column label="买入" width="180">
          <template #default="{ row }">
            <span class="amount">{{ formatAmount(row.buyAmount) }}</span>
            <span class="ccy"> {{ row.buyCurrency }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeRate" label="成交汇率" width="100" align="right">
          <template #default="{ row }">{{ row.exchangeRate ? row.exchangeRate.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="tradeDate" label="交易日" width="110" align="center" />
        <el-table-column prop="valueDate" label="交割日" width="110" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="row.status !== 'Canceled'">编辑</el-button>
            <el-button type="warning" link @click="handleRateFix(row)" v-if="row.productType === 'NDF' && row.status === 'New'">RATE_FIX</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="row.status !== 'Canceled'">删除</el-button>
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

    <!-- 编辑抽屉 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="700px" destroy-on-close>
      <FxDealForm v-if="drawerVisible" :deal-data="editingDeal" @saved="onSaved" @cancel="drawerVisible = false" />
    </el-drawer>

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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFxDeal, deleteFxDeal, rateFixFxDeal } from '@/api/dealing/fxDeal'
import FxDealForm from './FxDealForm.vue'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const drawerTitle = ref('新建 FX 交易')
const editingDeal = ref(null)

// RATE_FIX
const rateFixVisible = ref(false)
const rateFixing = ref(false)
const fixingDeal = ref(null)
const rateFixForm = reactive({ fixingRate: null, operator: 'admin' })

const queryForm = reactive({
  managementEntityId: null,
  counterpartyId: null,
  productType: '',
  status: ''
})
const dateRange = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getStatusLabel = (s) => ({ New: '新建', Active: '活跃', Canceled: '已删除' }[s] || s)
const getStatusType = (s) => ({ New: 'info', Active: 'success', Canceled: 'danger' }[s] || 'info')
const getProductTypeTag = (t) => ({ SPOT: 'success', FWD: 'warning', NDF: 'info' }[t] || 'info')
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await listFxDeal(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }
const handleReset = () => {
  Object.assign(queryForm, { managementEntityId: null, counterpartyId: null, productType: '', status: '' })
  dateRange.value = []
  handleQuery()
}
const handleAdd = () => {
  editingDeal.value = null
  drawerTitle.value = '新建 FX 交易'
  drawerVisible.value = true
}
const handleView = (row) => {
  router.push(`/dealing/fx-deal/detail?dealNumber=${row.dealNumber}`)
}
const handleEdit = (row) => {
  editingDeal.value = row
  drawerTitle.value = `编辑 FX 交易 - ${row.dealNumber}`
  drawerVisible.value = true
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除 FX 交易 ${row.dealNumber}？将级联软删 Deal/DealMap/Cashflow。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteFxDeal(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
const handleRateFix = (row) => {
  fixingDeal.value = row
  rateFixForm.fixingRate = null
  rateFixForm.operator = 'admin'
  rateFixVisible.value = true
}
const doRateFix = async () => {
  if (!rateFixForm.fixingRate) {
    ElMessage.error('请输入 Fixing 汇率')
    return
  }
  rateFixing.value = true
  try {
    const res = await rateFixFxDeal(fixingDeal.value.id, rateFixForm)
    ElMessage.success(`RATE_FIX 完成，差额: ${res.data?.settlementAmount}`)
    rateFixVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || 'RATE_FIX 失败')
  } finally {
    rateFixing.value = false
  }
}
const onSaved = () => {
  drawerVisible.value = false
  ElMessage.success('保存成功')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.fx-deal-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
.amount { font-family: 'JetBrains Mono', monospace; font-weight: 600; }
.ccy { color: #909399; font-size: 12px; }
</style>