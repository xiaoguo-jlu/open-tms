<template>
  <div class="at-deal-detail one-screen">
    <!-- 顶部工具条 -->
    <div class="action-bar top">
      <div class="left">
        <el-button :icon="ArrowLeft" size="small" @click="handleBack">返回</el-button>
        <span class="page-title">AT 交易详情 - {{ detail.dealNumber || (mode === 'new' ? '新建' : '复制') }}</span>
        <ModeBadge :mode="mode" :copy-from="route.query.copyFrom" />
      </div>
      <div class="right">
        <template v-if="mode === 'readonly'">
          <el-button type="success" size="small" :icon="CopyDocument" @click="enterCopy">复制</el-button>
          <el-button v-if="canEdit(detail.status)" type="primary" size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
          <el-button v-if="canDelete(detail.status)" type="danger" size="small" :icon="Delete" @click="handleDelete">删除</el-button>
        </template>
        <template v-else>
          <el-button size="small" @click="handleCancel">取消</el-button>
        </template>
      </div>
    </div>

    <!-- 关键信息条 -->
    <div class="key-info-bar" v-if="mode === 'readonly'">
      <div class="key-item">
        <span class="key-label">交易编号</span>
        <span class="key-value mono">{{ detail.dealNumber || '-' }}</span>
      </div>
      <div class="key-item">
        <el-tag type="primary" effect="dark">{{ getTransferTypeLabel(detail.transferType) }}</el-tag>
      </div>
      <div class="key-item">
        <el-tag :type="getStatusType(detail.status)" effect="dark">{{ getStatusLabel(detail.status) }}</el-tag>
      </div>
      <div class="key-item highlight">
        <span class="key-label">转账金额</span>
        <span class="key-value mono lg">{{ formatAmount(detail.amount || detail.sourceAmount, '') }} {{ detail.currency || detail.sourceCurrency || '' }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">起息日</span>
        <span class="key-value">{{ detail.valueDate || '-' }}</span>
      </div>
    </div>

    <el-card class="detail-card" v-loading="loadingDetail" :body-style="{ padding: '12px 16px' }">
      <!-- 业务规则说明 (new/edit/copy 模式显示) -->
      <el-alert
        v-if="mode !== 'readonly'"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 8px"
        title="AT 交易仅支持同管理主体、同币种的内部转账，跨主体/跨币种请使用 FX 交易"
      />

      <!-- 错误提示 -->
      <el-alert
        v-if="mode !== 'readonly' && errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
        style="margin-bottom: 8px"
      />
      <el-alert
        v-if="mode === 'readonly' && errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
        style="margin-bottom: 8px"
      />

      <!-- 主信息区: readonly 摘要 -->
      <section v-if="mode === 'readonly'" class="main-info-area">
        <!-- 左列: 交易要素 -->
        <div class="info-col">
          <h3 class="section-title-sm">交易要素</h3>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid">
            <el-descriptions-item label="管理主体">{{ detail.managementEntity || '-' }}</el-descriptions-item>
            <el-descriptions-item label="转账类型">{{ getTransferTypeLabel(detail.transferType) }}</el-descriptions-item>
            <el-descriptions-item label="付出方账户">{{ detail.sourceAccountName || (detail.sourceAccountId ? `ID:${detail.sourceAccountId}` : '-') }}</el-descriptions-item>
            <el-descriptions-item label="收入方账户">{{ detail.destAccountName || (detail.destAccountId ? `ID:${detail.destAccountId}` : '-') }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ getPaymentMethodLabel(detail.paymentMethod) }}</el-descriptions-item>
            <el-descriptions-item label="操作人">{{ detail.operator || '-' }}</el-descriptions-item>
            <el-descriptions-item label="资金用途" :span="2">{{ detail.purpose || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 右列: 金额 & 日期 -->
        <div class="info-col">
          <h3 class="section-title-sm">金额 & 日期</h3>
          <div class="amount-block">
            <div class="amount-row">
              <span class="amount-label">转账金额</span>
              <span class="amount-value mono">{{ formatAmount(detail.amount || detail.sourceAmount, detail.currency || detail.sourceCurrency) }}</span>
            </div>
            <div class="amount-row">
              <span class="amount-label">目标金额</span>
              <span class="amount-value mono">{{ formatAmount(detail.destAmount || detail.amount, detail.destCurrency || detail.currency) }}</span>
            </div>
            <div class="amount-row">
              <span class="amount-label">汇率</span>
              <span class="amount-value mono">{{ detail.exchangeRate ? Number(detail.exchangeRate).toFixed(8) : '1.00000000' }}</span>
            </div>
          </div>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid" style="margin-top: 8px;">
            <el-descriptions-item label="起息日">{{ detail.valueDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ getPaymentMethodLabel(detail.paymentMethod) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <!-- 主信息区: edit/new/copy 表单 -->
      <section v-else class="edit-form-area">
        <h3 class="section-title-sm">交易要素</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="edit-form">
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="管理主体" prop="managementEntity">
                <BaseDataPicker v-model="form.managementEntity" entity="management-entity" placeholder="管理主体" size="small" @change="onManagementEntityChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="转账类型" prop="transferType">
                <el-select v-model="form.transferType" placeholder="转账类型" size="small" style="width: 100%;">
                  <el-option label="同公司转账" value="SAME_COMPANY" />
                  <el-option label="内部调拨" value="INTERNAL" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="付出方账户" prop="sourceAccountId">
                <BaseDataPicker v-model="form.sourceAccountId" entity="bank-account" placeholder="付出方账户" size="small" :auto-filter="sourceFilter" :preload-row="sourceAccount" :disabled="mode === 'edit'" @change="onSourceAccountChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="收入方账户" prop="destAccountId">
                <BaseDataPicker v-model="form.destAccountId" entity="bank-account" placeholder="收入方账户" size="small" :auto-filter="destFilter" :preload-row="destAccount" :disabled="mode === 'edit'" @change="onDestAccountChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="转账金额" prop="amount">
                <el-input-number v-model="form.amount" :precision="2" :step="1000" :min="0" size="small" style="width: 100%;" placeholder="0.00" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="币种" prop="currency">
                <el-input v-model="form.currency" placeholder="选择账户后自动填充" size="small" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="起息日" prop="valueDate">
                <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%;" placeholder="起息日" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="支付方式" prop="paymentMethod">
                <el-select v-model="form.paymentMethod" placeholder="支付方式" size="small" style="width: 100%;">
                  <el-option label="内部转账" value="INTERNAL" />
                  <el-option label="SWIFT 电汇" value="SWIFT" />
                  <el-option label="RTGS" value="RTGS" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="操作人">
                <el-input v-model="form.operator" placeholder="操作人" size="small" />
              </el-form-item>
            </el-col>
            <el-col :xs="24">
              <el-form-item label="资金用途" prop="purpose">
                <el-input v-model="form.purpose" type="textarea" :rows="1" placeholder="资金用途" maxlength="500" show-word-limit />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </section>

      <!-- ====== Tabs: readonly 模式才显示 ====== -->
      <section v-if="mode === 'readonly'" class="section-tabs">
        <el-tabs v-model="activeTab" class="compact-tabs">
          <el-tab-pane label="审计信息" name="basic">
            <el-descriptions :column="3" :size="'small'" border>
              <el-descriptions-item label="最新 Action">{{ detail.latestActionNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ detail.createdBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ detail.createdAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新人">{{ detail.updatedBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="`DealMap (${dealMapList.length})`" name="dealmap">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('dealmap')">展开全部</el-button>
            </div>
            <el-table :data="dealMapList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="dealmapNumber" label="DealMap 编号" width="160" />
              <el-table-column prop="eventType" label="事件类型" width="130" align="center">
                <template #default="{ row }">
                  <el-tag :type="getEventTypeTag(row.eventType)" size="small">{{ row.eventType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="方向" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.direction === 'Outflow' ? 'danger' : 'success'" size="small">{{ row.direction === 'Outflow' ? '流出' : '流入' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" align="right" width="140">
                <template #default="{ row }">
                  <span class="mono">{{ formatAmount(row.amount, row.currency) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="valueDate" label="起息日" width="110" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.eventStatus === 'Active' ? 'success' : 'info'" size="small">{{ row.eventStatus }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="dealMapList.length === 0" description="暂无 DealMap 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane :label="`Cashflow (${cashflowList.length})`" name="cashflow">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('cashflow')">展开全部</el-button>
            </div>
            <el-table :data="cashflowList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="cflowNumber" label="现金流编号" width="170" />
              <el-table-column prop="dealmapNumber" label="关联 DealMap" width="170" />
              <el-table-column label="方向" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.direction === 'Outflow' ? 'danger' : 'success'" size="small">{{ row.direction === 'Outflow' ? '流出' : '流入' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" align="right" width="140">
                <template #default="{ row }">
                  <span class="mono">{{ formatAmount(row.amount, row.currency) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="valueDate" label="起息日" width="110" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'Created' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="cashflowList.length === 0" description="暂无 Cashflow 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane label="GL Entry" name="gl-entry">
            <el-alert type="warning" :closable="false" style="margin-bottom: 8px;">
              <template #title>会计分录功能待 M1.3 实现 (预计 {{ expectedGlEntryCount }} 笔: SOURCE 2 + DEST 2)</template>
            </el-alert>
            <el-descriptions :column="2" :size="'small'" border>
              <el-descriptions-item label="转账类型">{{ getTransferTypeLabel(detail.transferType) }}</el-descriptions-item>
              <el-descriptions-item label="币种情况">
                {{ detail.sourceCurrency === detail.destCurrency ? '同币种' : '跨币种 (汇率 ' + detail.exchangeRate + ')' }}
              </el-descriptions-item>
              <el-descriptions-item label="源金额"><span class="mono">{{ formatAmount(detail.sourceAmount, detail.sourceCurrency) }}</span></el-descriptions-item>
              <el-descriptions-item label="目标金额"><span class="mono">{{ formatAmount(detail.destAmount, detail.destCurrency) }}</span></el-descriptions-item>
              <el-descriptions-item label="规则码">AT_TRANSFER_DEFAULT</el-descriptions-item>
              <el-descriptions-item label="分录笔数">{{ expectedGlEntryCount }} 笔</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="`Action (${actionList.length})`" name="action">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('action')">展开全部</el-button>
            </div>
            <el-table :data="actionList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="actionNumber" label="Action 编号" width="170" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="operator" label="操作人" width="90" />
              <el-table-column prop="operateAt" label="操作时间" width="160" />
              <el-table-column label="一级审批" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalType(row.approvalStatus1)" size="small">{{ row.approvalStatus1 || '-' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="二级审批" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalType(row.approvalStatus2)" size="small">{{ row.approvalStatus2 || '-' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="actionList.length === 0" description="暂无 Action 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane :label="`镜像 (${imageList.length})`" name="image">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('image')">展开全部</el-button>
            </div>
            <el-table :data="imageList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="imageNumber" label="镜像编号" width="170" />
              <el-table-column prop="version" label="版本" width="70" align="center" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="{ row }"><el-tag size="small">{{ row.imageType }}</el-tag></template>
              </el-table-column>
              <el-table-column prop="operator" label="操作人" width="100" />
              <el-table-column prop="operateAt" label="操作时间" width="170" />
              <el-table-column label="快照状态" width="100" align="center">
                <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
              </el-table-column>
            </el-table>
            <el-empty v-if="imageList.length === 0" description="暂无镜像数据" :image-size="60" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </el-card>

    <!-- 底部固定操作条 (仅 edit/new/copy 时显示) -->
    <transition name="fade-up">
      <div v-if="mode !== 'readonly'" class="action-bar bottom">
        <div class="left">
          <el-tag v-if="mode === 'edit'" type="primary" effect="dark" size="small"><el-icon><Edit /></el-icon> 编辑中</el-tag>
          <el-tag v-else-if="mode === 'new'" type="success" effect="dark" size="small"><el-icon><Plus /></el-icon> 新建</el-tag>
          <el-tag v-else-if="mode === 'copy'" type="warning" effect="dark" size="small"><el-icon><DocumentCopy /></el-icon> 复制自 {{ route.query.copyFrom }}</el-tag>
        </div>
        <div class="right">
          <el-button size="small" @click="handleCancel">取消</el-button>
          <el-button type="primary" size="small" :loading="saving" :icon="Check" @click="handleSave">保存</el-button>
        </div>
      </div>
    </transition>

    <!-- 展开全部 Dialog -->
    <el-dialog v-model="fullDialogVisible" :title="fullDialogTitle" width="80%" top="5vh" destroy-on-close>
      <el-table :data="fullDialogData" stripe size="small" max-height="70vh">
        <el-table-column v-for="col in fullDialogColumns" :key="col.prop" :prop="col.prop" :label="col.label" :width="col.width" :align="col.align || 'left'" :show-overflow-tooltip="true">
          <template v-if="col.tag" #default="{ row }">
            <el-tag :type="col.tag(row)" size="small">{{ col.formatter ? col.formatter(row) : row[col.prop] }}</el-tag>
          </template>
          <template v-else-if="col.formatter" #default="{ row }">
            <span class="mono">{{ col.formatter(row) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, CopyDocument, Delete, DocumentCopy, Edit, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import {
  getAtDeal, listAtDealMaps, listAtCashflows, listAtActions, listAtImages,
  saveAtDeal, updateAtDeal, copyAtDeal
} from '@/api/dealing/atDeal'
import BaseDataPicker from '@/components/picker/BaseDataPicker.vue'
import ModeBadge from '@/components/common/ModeBadge.vue'

const route = useRoute()
const router = useRouter()

const mode = computed(() => {
  if (route.query.new) return 'new'
  if (route.query.edit) return 'edit'
  if (route.query.copyFrom) return 'copy'
  return 'readonly'
})

const detail = ref({})
const dealMapList = ref([])
const cashflowList = ref([])
const actionList = ref([])
const imageList = ref([])
const activeTab = ref('dealmap')
const loadingDetail = ref(false)
const errorMessage = ref('')

const fullDialogVisible = ref(false)
const fullDialogTitle = ref('')
const fullDialogData = ref([])
const fullDialogColumns = ref([])

const expectedGlEntryCount = computed(() => {
  if (!detail.value || !detail.value.transferType) return 0
  return 4
})

const getTransferTypeLabel = (type) => {
  const map = { SAME_COMPANY: '同公司', CROSS_COMPANY: '跨公司', CROSS_BORDER: '跨境', INTERNAL: '内部调拨' }
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
const getActionTypeTag = (type) => {
  const map = { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger' }
  return map[type] || 'info'
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
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + (currency ? ' ' + currency : '')
}
const canEdit = (status) => status === 'New' || status === 'Rejected'
const canDelete = (status) => status === 'New' || status === 'Rejected'

const formRef = ref()
const saving = ref(false)

const emptyForm = () => ({
  id: null,
  dealNumber: null,
  managementEntity: '',
  transferType: 'SAME_COMPANY',
  sourceAccountId: null,
  destAccountId: null,
  amount: null,
  currency: '',
  valueDate: '',
  paymentMethod: 'INTERNAL',
  purpose: '',
  operator: 'currentUser',
  remark: '',
  exchangeRate: 1
})

const form = reactive(emptyForm())

const rules = {
  managementEntity: [{ required: true, message: '请选择管理主体', trigger: 'change' }],
  transferType: [{ required: true, message: '请选择转账类型', trigger: 'change' }],
  sourceAccountId: [{ required: true, message: '请选择付出方账户', trigger: 'change' }],
  destAccountId: [{ required: true, message: '请选择收入方账户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入转账金额', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择同币种的账户', trigger: 'change' }],
  valueDate: [{ required: true, message: '请选择起息日', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const sourceAccount = ref(null)
const destAccount = ref(null)
const selectedMgmtEntity = ref(null)

const sourceFilter = computed(() => {
  if (!selectedMgmtEntity.value) return {}
  return { managementEntityId: selectedMgmtEntity.value }
})
const destFilter = computed(() => sourceFilter.value)

const errorMessageComputed = computed(() => {
  if (selectedMgmtEntity.value && form.managementEntity &&
      form.managementEntity !== selectedMgmtEntity.value) {
    return `管理主体已变更为 ${form.managementEntity}，请重新选择付出/收入方账户`
  }
  if (sourceAccount.value && selectedMgmtEntity.value &&
      Number(sourceAccount.value.managementEntityId) !== Number(selectedMgmtEntity.value)) {
    return `付出方账户 ${sourceAccount.value.accountNo} 不属于当前管理主体`
  }
  if (destAccount.value && selectedMgmtEntity.value &&
      Number(destAccount.value.managementEntityId) !== Number(selectedMgmtEntity.value)) {
    return `收入方账户 ${destAccount.value.accountNo} 不属于当前管理主体`
  }
  if (sourceAccount.value && destAccount.value &&
      sourceAccount.value.currency && destAccount.value.currency &&
      sourceAccount.value.currency !== destAccount.value.currency) {
    return `AT 不支持跨币种转账 (${sourceAccount.value.currency} → ${destAccount.value.currency})，请使用 FX 交易`
  }
  if (form.sourceAccountId && form.destAccountId &&
      Number(form.sourceAccountId) === Number(form.destAccountId)) {
    return '付出方和收入方账户不能相同'
  }
  return ''
})

const onManagementEntityChange = (row) => {
  if (!row) {
    selectedMgmtEntity.value = null
    return
  }
  selectedMgmtEntity.value = row.id
  form.sourceAccountId = null
  form.destAccountId = null
  sourceAccount.value = null
  destAccount.value = null
  form.currency = ''
}

const onSourceAccountChange = (row) => {
  sourceAccount.value = row
  if (row && row.id) form.sourceAccountId = row.id
  syncCurrency()
  formRef.value?.clearValidate(['sourceAccountId', 'destAccountId'])
}

const onDestAccountChange = (row) => {
  destAccount.value = row
  if (row && row.id) form.destAccountId = row.id
  syncCurrency()
  formRef.value?.clearValidate(['sourceAccountId', 'destAccountId'])
}

const syncCurrency = () => {
  const srcCcy = sourceAccount.value?.currency
  const dstCcy = destAccount.value?.currency
  if (srcCcy && dstCcy && srcCcy === dstCcy) {
    form.currency = srcCcy
  } else {
    form.currency = ''
  }
}

const fillFormFromObject = (src) => {
  Object.assign(form, {
    id: src.id ?? null,
    dealNumber: src.dealNumber ?? null,
    managementEntity: src.managementEntity || '',
    transferType: src.transferType || 'SAME_COMPANY',
    sourceAccountId: src.sourceAccountId ?? null,
    destAccountId: src.destAccountId ?? null,
    amount: src.amount ?? src.sourceAmount ?? src.destAmount ?? null,
    currency: src.currency || src.sourceCurrency || src.destCurrency || '',
    valueDate: src.valueDate || '',
    paymentMethod: src.paymentMethod || 'INTERNAL',
    purpose: src.purpose || '',
    operator: 'currentUser',
    remark: src.remark || '',
    exchangeRate: src.exchangeRate || 1
  })
}

const isDirty = computed(() => {
  if (mode.value === 'new' || mode.value === 'copy') return true
  return JSON.stringify(form) !== JSON.stringify(detail.value)
})

const handleBack = () => router.push('/dealing/at-deal')

const enterEdit = () => {
  fillFormFromObject(detail.value)
  router.replace({ path: '/dealing/at-deal/detail', query: { id: detail.value.id, edit: 1 } })
}

const enterCopy = async () => {
  try {
    const res = await copyAtDeal(detail.value.dealNumber)
    fillFormFromObject(res.data || res)
    form.id = null
    form.dealNumber = null
    router.replace({ path: '/dealing/at-deal/detail', query: { copyFrom: detail.value.dealNumber } })
  } catch (e) {
    ElMessage.error(e?.message || '复制失败')
  }
}

const handleCancel = async () => {
  if ((mode.value === 'edit' || mode.value === 'copy') && isDirty.value) {
    try {
      await ElMessageBox.confirm('放弃当前修改?', '确认取消', { type: 'warning', confirmButtonText: '放弃修改', cancelButtonText: '继续编辑' })
    } catch (e) {
      return
    }
  }
  if (mode.value === 'copy' || mode.value === 'edit') {
    if (detail.value.id) {
      router.replace({ path: '/dealing/at-deal/detail', query: { id: detail.value.id } })
    } else {
      handleBack()
    }
  } else {
    handleBack()
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    errorMessage.value = '请检查表单填写'
    setTimeout(() => { errorMessage.value = '' }, 5000)
    return
  }
  const inlineError = errorMessageComputed.value
  if (inlineError) {
    errorMessage.value = inlineError
    setTimeout(() => { errorMessage.value = '' }, 5000)
    return
  }
  saving.value = true
  try {
    const submitForm = {
      ...form,
      sourceAmount: form.amount,
      destAmount: form.amount,
      sourceCurrency: form.currency,
      destCurrency: form.currency,
      exchangeRate: 1
    }
    if (mode.value === 'edit' && form.id) {
      await updateAtDeal(submitForm)
      ElMessage.success('保存成功')
      await loadData(form.id)
      router.replace({ path: '/dealing/at-deal/detail', query: { id: form.id } })
    } else {
      const createData = { ...submitForm }
      delete createData.id
      delete createData.dealNumber
      const res = await saveAtDeal(createData)
      const newId = res.data?.id
      ElMessage.success('创建成功')
      if (newId) {
        router.replace({ path: '/dealing/at-deal/detail', query: { id: newId } })
      } else {
        handleBack()
      }
    }
  } catch (e) {
    errorMessage.value = e?.message || '保存失败'
    setTimeout(() => { errorMessage.value = '' }, 5000)
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除 AT 交易 ${detail.value.dealNumber}?`, '提示', { type: 'warning' })
    await request({
      url: `/api/v1/dealing/at-deals/${detail.value.id}/delete`,
      method: 'post'
    })
    ElMessage.success('删除成功')
    handleBack()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const openFullDialog = (tabName) => {
  if (tabName === 'dealmap') {
    fullDialogTitle.value = `DealMap (${dealMapList.value.length})`
    fullDialogData.value = dealMapList.value
    fullDialogColumns.value = [
      { prop: 'dealmapNumber', label: 'DealMap 编号', width: 160 },
      { prop: 'actionNumber', label: 'Action 编号', width: 160 },
      { prop: 'eventType', label: '事件类型', width: 130, tag: getEventTypeTag },
      { prop: 'eventStatus', label: '状态', width: 90, align: 'center', tag: (r) => r.eventStatus === 'Active' ? 'success' : 'info' },
      { prop: 'direction', label: '方向', width: 90, align: 'center', tag: (r) => r.direction === 'Outflow' ? 'danger' : 'success', formatter: (r) => r.direction === 'Outflow' ? '流出' : '流入' },
      { prop: 'amount', label: '金额', width: 140, align: 'right', formatter: (r) => formatAmount(r.amount, r.currency) },
      { prop: 'eventDate', label: '事件日期', width: 110, align: 'center' },
      { prop: 'description', label: '描述', width: 200 }
    ]
  } else if (tabName === 'cashflow') {
    fullDialogTitle.value = `Cashflow (${cashflowList.value.length})`
    fullDialogData.value = cashflowList.value
    fullDialogColumns.value = [
      { prop: 'cflowNumber', label: '现金流编号', width: 170 },
      { prop: 'dealmapNumber', label: '关联 DealMap', width: 170 },
      { prop: 'direction', label: '方向', width: 90, align: 'center', tag: (r) => r.direction === 'Outflow' ? 'danger' : 'success', formatter: (r) => r.direction === 'Outflow' ? '流出' : '流入' },
      { prop: 'amount', label: '金额', width: 140, align: 'right', formatter: (r) => formatAmount(r.amount, r.currency) },
      { prop: 'currency', label: '币种', width: 80, align: 'center' },
      { prop: 'valueDate', label: '起息日', width: 110, align: 'center' },
      { prop: 'status', label: '状态', width: 90, align: 'center', tag: (r) => r.status === 'Created' ? 'success' : 'info' }
    ]
  } else if (tabName === 'action') {
    fullDialogTitle.value = `Action (${actionList.value.length})`
    fullDialogData.value = actionList.value
    fullDialogColumns.value = [
      { prop: 'actionNumber', label: 'Action 编号', width: 170 },
      { prop: 'actionType', label: '类型', width: 100, align: 'center', tag: getActionTypeTag, formatter: getActionTypeLabel },
      { prop: 'operator', label: '操作人', width: 90 },
      { prop: 'operateAt', label: '操作时间', width: 170 },
      { prop: 'approvalStatus1', label: '一级审批', width: 100, align: 'center', tag: getApprovalType },
      { prop: 'approvalStatus2', label: '二级审批', width: 100, align: 'center', tag: getApprovalType },
      { prop: 'approver1', label: '审批人', width: 100 },
      { prop: 'approvalRemark', label: '审批意见', width: 200 }
    ]
  } else if (tabName === 'image') {
    fullDialogTitle.value = `镜像版本 (${imageList.value.length})`
    fullDialogData.value = imageList.value
    fullDialogColumns.value = [
      { prop: 'imageNumber', label: '镜像编号', width: 170 },
      { prop: 'version', label: '版本', width: 70, align: 'center' },
      { prop: 'imageType', label: '类型', width: 100, align: 'center' },
      { prop: 'operator', label: '操作人', width: 100 },
      { prop: 'operateAt', label: '操作时间', width: 170 },
      { prop: 'status', label: '快照状态', width: 100, align: 'center' }
    ]
  }
  fullDialogVisible.value = true
}

const loadData = async (id) => {
  if (!id) return
  loadingDetail.value = true
  try {
    const res = await getAtDeal(id)
    detail.value = res.data
    if (detail.value.dealNumber) {
      dealMapList.value = (await listAtDealMaps(detail.value.dealNumber)).data || []
      cashflowList.value = (await listAtCashflows(detail.value.dealNumber)).data || []
      actionList.value = (await listAtActions(detail.value.dealNumber)).data || []
      imageList.value = (await listAtImages(detail.value.dealNumber)).data || []
    }
    fillFormFromObject(detail.value)
  } catch (e) {
    console.error(e)
  } finally {
    loadingDetail.value = false
  }
}

const loadAccountById = async (id) => {
  try {
    const res = await request({
      url: '/api/v1/bank-accounts/page',
      method: 'get',
      params: { pageNum: 1, pageSize: 20, keyword: String(id) }
    })
    const records = res?.data?.records || res?.data?.list || []
    return records.find(r => Number(r.id) === Number(id)) || null
  } catch (e) {
    return null
  }
}

const loadCopyData = async (dealNumber) => {
  try {
    const res = await copyAtDeal(dealNumber)
    const data = res.data || res
    Object.assign(form, {
      id: null,
      dealNumber: null,
      managementEntity: data.managementEntity || '',
      transferType: data.transferType || 'SAME_COMPANY',
      sourceAccountId: data.sourceAccountId ?? null,
      destAccountId: data.destAccountId ?? null,
      amount: data.sourceAmount ?? data.destAmount ?? null,
      currency: data.sourceCurrency || data.destCurrency || '',
      valueDate: data.valueDate || '',
      paymentMethod: data.paymentMethod || 'INTERNAL',
      purpose: data.purpose || '',
      operator: 'currentUser',
      remark: data.remark || ''
    })
    if (data.sourceAccountId) {
      const acc = await loadAccountById(data.sourceAccountId)
      if (acc) sourceAccount.value = acc
    }
    if (data.destAccountId) {
      const acc = await loadAccountById(data.destAccountId)
      if (acc) destAccount.value = acc
    }
    if (sourceAccount.value?.managementEntityId) {
      selectedMgmtEntity.value = sourceAccount.value.managementEntityId
    }
  } catch (e) {
    console.error(e)
  }
}

const init = async () => {
  if (mode.value === 'new') {
    Object.assign(form, emptyForm())
    return
  }
  if (mode.value === 'copy') {
    const copyFrom = route.query.copyFrom
    if (copyFrom) await loadCopyData(copyFrom)
    return
  }
  const id = route.query.id
  if (!id) return
  await loadData(id)
  if (mode.value === 'edit') {
    fillFormFromObject(detail.value)
  }
}

onMounted(init)
</script>

<style scoped>
.at-deal-detail { padding-bottom: 64px; }

/* === 顶部工具条 === */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 48px;
}
.action-bar .right { display: flex; gap: 6px; align-items: center; }
.action-bar .left { display: flex; gap: 8px; align-items: center; }
.page-title { font-size: 14px; font-weight: 600; color: #303133; }

.action-bar.bottom {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  border-top: 1px solid #ebeef5;
  border-bottom: none;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 99;
  height: 56px;
}

/* === 关键信息条 === */
.key-info-bar {
  display: flex;
  align-items: center;
  height: 80px;
  padding: 0 20px;
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
.key-value.lg { font-size: 18px; font-weight: 700; color: #303133; }
.key-item.highlight .key-value { color: #409eff; }

@media (max-width: 1599px) {
  .key-info-bar .key-item:nth-child(5) { display: none; }
}
@media (max-width: 1199px) {
  .key-info-bar .key-item:nth-child(2),
  .key-info-bar .key-item:nth-child(5) { display: none; }
}
@media (max-width: 959px) {
  .key-info-bar { flex-wrap: wrap; height: auto; padding: 8px; }
  .key-info-bar .key-item { flex: 1 1 50%; }
}

/* === 主信息区 === */
.detail-card { margin-top: 4px; }
.main-info-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 480px;
}
.info-col { min-width: 0; }
.section-title-sm {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px 0;
  padding-left: 6px;
  border-left: 3px solid #409eff;
  line-height: 1.2;
}
.summary-grid :deep(.el-descriptions__label) { width: 90px; padding: 4px 8px; font-size: 12px; }
.summary-grid :deep(.el-descriptions__content) { padding: 4px 8px; font-size: 12px; }

@media (max-width: 1199px) {
  .main-info-area { grid-template-columns: 1fr; }
}

.amount-block { background: #fafbfc; border-radius: 4px; padding: 10px 12px; }
.amount-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.amount-row + .amount-row { border-top: 1px dashed #e4e7ed; }
.amount-label { font-size: 12px; color: #606266; }
.amount-value { font-size: 18px; font-weight: 700; color: #303133; font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }

/* === 编辑表单 === */
.edit-form-area { min-height: 480px; }
.edit-form { padding: 0 4px; }
.edit-form :deep(.el-form-item) { margin-bottom: 12px; }
.edit-form :deep(.el-form-item__label) { font-weight: 500; font-size: 12px; line-height: 1.4; padding-bottom: 2px; }

/* === Tabs 区 === */
.section-tabs { margin-top: 8px; }
.compact-tabs :deep(.el-tabs__header) { margin: 0 0 4px 0; }
.compact-tabs :deep(.el-tabs__item) { padding: 0 12px; height: 32px; line-height: 32px; font-size: 13px; }
.compact-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }
.compact-tabs :deep(.el-tab-pane) { max-height: 360px; overflow: hidden; }
.tab-toolbar { display: flex; justify-content: flex-end; margin-bottom: 4px; }

.compact-table :deep(.el-table__row) { height: 36px; }
.compact-table :deep(.el-table__cell) { padding: 4px 0; font-size: 12px; }

/* === 通用 === */
.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }

.fade-up-enter-active,
.fade-up-leave-active { transition: all 0.2s ease; }
.fade-up-enter-from,
.fade-up-leave-to { opacity: 0; transform: translateY(10px); }
</style>