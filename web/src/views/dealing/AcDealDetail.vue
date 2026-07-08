<template>
  <div class="ac-deal-detail one-screen">
    <!-- 顶部工具条 -->
    <div class="action-bar top">
      <div class="left">
        <el-button :icon="ArrowLeft" size="small" @click="handleBack">返回</el-button>
        <span class="page-title">AC 交易详情 - {{ detail.dealNumber || (mode === 'new' ? '新建' : '复制') }}</span>
        <ModeBadge :mode="mode" :copy-from="route.query.copyFrom" />
      </div>
      <div class="right">
        <template v-if="mode === 'readonly'">
          <el-button type="success" size="small" :icon="CopyDocument" @click="enterCopy">复制</el-button>
          <el-button v-if="detail.status !== 'Canceled'" type="primary" size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
          <el-button v-if="detail.status === 'New'" type="primary" size="small" :icon="Check" @click="handleApprove">审批</el-button>
          <el-button v-if="detail.status !== 'Canceled'" type="danger" size="small" :icon="Delete" @click="handleDelete">删除</el-button>
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
        <el-tag :type="detail.direction === 'Inflow' ? 'success' : 'danger'" effect="dark">
          {{ detail.direction === 'Inflow' ? '流入' : '流出' }}
        </el-tag>
      </div>
      <div class="key-item">
        <el-tag :type="getStatusType(detail.status)" effect="dark">{{ getStatusLabel(detail.status) }}</el-tag>
      </div>
      <div class="key-item highlight">
        <span class="key-label">金额</span>
        <span class="key-value mono lg">{{ formatAmount(detail.amount, '') }} {{ detail.currency || '' }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">起息日</span>
        <span class="key-value">{{ detail.valueDate || '-' }}</span>
      </div>
    </div>

    <el-card class="detail-card" v-loading="loadingDetail" :body-style="{ padding: '12px 16px' }">
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

      <!-- 主信息区: readonly 摘要 -->
      <section v-if="mode === 'readonly'" class="main-info-area">
        <!-- 左列: 交易要素 -->
        <div class="info-col">
          <h3 class="section-title-sm">交易要素</h3>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid">
            <el-descriptions-item label="管理主体">{{ detail.managementEntity || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易员">{{ detail.traderId ? `ID:${detail.traderId}` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易对手">{{ detail.counterpartyId ? `ID:${detail.counterpartyId}` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="金融工具">{{ detail.instrumentName || (detail.instrumentId ? `ID:${detail.instrumentId}` : '-') }}</el-descriptions-item>
            <el-descriptions-item label="本方账户">{{ detail.bankAccountName || (detail.bankAccountId ? `ID:${detail.bankAccountId}` : '-') }}</el-descriptions-item>
            <el-descriptions-item label="对手方账户">{{ detail.counterpartyAccountName || (detail.counterpartyAccountId ? `ID:${detail.counterpartyAccountId}` : '-') }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 右列: 金额 & 日期 -->
        <div class="info-col">
          <h3 class="section-title-sm">金额 & 日期</h3>
          <div class="amount-block">
            <div class="amount-row">
              <span class="amount-label">金额</span>
              <span class="amount-value mono">{{ formatAmount(detail.amount, '') }} {{ detail.currency || '' }}</span>
            </div>
            <div class="amount-row">
              <span class="amount-label">方向</span>
              <el-tag :type="detail.direction === 'Inflow' ? 'success' : 'danger'" size="small">
                {{ detail.direction === 'Inflow' ? '流入' : '流出' }}
              </el-tag>
            </div>
          </div>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid" style="margin-top: 8px;">
            <el-descriptions-item label="交易日期">{{ detail.dealDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="起息日">{{ detail.valueDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ detail.paymentMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最新 Action">{{ detail.latestActionNumber || '-' }}</el-descriptions-item>
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
                <BaseDataPicker v-model="form.managementEntity" entity="management-entity" placeholder="管理主体" size="small" :preload-row="preloadRows.managementEntity" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="方向" prop="direction">
                <el-radio-group v-model="form.direction" size="small">
                  <el-radio-button value="Inflow">流入</el-radio-button>
                  <el-radio-button value="Outflow">流出</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="金额" prop="amount">
                <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" size="small" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="币种" prop="currency">
                <BaseDataPicker v-model="form.currency" entity="currency" placeholder="币种" size="small" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易日期" prop="dealDate">
                <el-date-picker v-model="form.dealDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="起息日" prop="valueDate">
                <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="支付方式">
                <el-select v-model="form.paymentMethod" placeholder="支付方式" size="small" clearable style="width: 100%;">
                  <el-option label="转账" value="TRANSFER" />
                  <el-option label="票据" value="CHECK" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易员" prop="traderId">
                <BaseDataPicker v-model="form.traderId" entity="trader" placeholder="交易员" size="small" :preload-row="preloadRows.trader" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易对手">
                <BaseDataPicker v-model="form.counterpartyId" entity="counterparty" placeholder="交易对手" size="small" :preload-row="preloadRows.counterparty" @change="onCounterpartyChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="金融工具">
                <BaseDataPicker v-model="form.instrumentId" entity="instrument" placeholder="金融工具" size="small" :preload-row="preloadRows.instrument" @change="row => form.instrumentName = row?.instrumentName || ''" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="本方账户" prop="bankAccountId">
                <BaseDataPicker v-model="form.bankAccountId" entity="bank-account" placeholder="本方账户" size="small" :preload-row="preloadRows.bankAccount" @change="row => form.bankAccountName = row?.accountName || ''" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="对手方账户">
                <BaseDataPicker v-model="form.counterpartyAccountId" entity="counterparty-account" :auto-filter="{ counterpartyId: form.counterpartyId }" placeholder="对手方账户" size="small" :preload-row="preloadRows.counterpartyAccount" @change="row => form.counterpartyAccountName = row?.accountName || ''" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="操作人" prop="operator">
                <el-input v-model="form.operator" placeholder="操作人" size="small" />
              </el-form-item>
            </el-col>
            <el-col :xs="24">
              <el-form-item label="描述">
                <el-input v-model="form.description" type="textarea" :rows="1" maxlength="500" show-word-limit />
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
              <el-table-column prop="actionNumber" label="Action 编号" width="160" />
              <el-table-column prop="eventType" label="事件类型" width="120" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.eventStatus === 'Active' ? 'success' : 'info'" size="small">{{ row.eventStatus }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="方向" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'" size="small">{{ row.direction }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" align="right" width="140">
                <template #default="{ row }">
                  <span class="mono">{{ formatAmount(row.amount, row.currency) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="eventDate" label="事件日期" width="110" align="center" />
            </el-table>
            <el-empty v-if="dealMapList.length === 0" description="暂无 DealMap 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane :label="`现金流 (${cashflowList.length})`" name="cashflow">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('cashflow')">展开全部</el-button>
            </div>
            <el-table :data="cashflowList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="cflowNumber" label="现金流编号" width="160" />
              <el-table-column prop="dealmapNumber" label="关联 DealMap" width="160" />
              <el-table-column label="方向" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'" size="small">{{ row.direction }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" align="right" width="140">
                <template #default="{ row }">
                  <span class="mono">{{ formatAmount(row.amount, row.currency) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="currency" label="币种" width="70" align="center" />
              <el-table-column prop="valueDate" label="起息日" width="100" align="center" />
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
              <template #title>会计分录功能待 M1.3 实现 (预计 {{ expectedGlEntryCount }} 笔: 1 DR + 1 CR)</template>
            </el-alert>
            <el-descriptions :column="2" :size="'small'" border>
              <el-descriptions-item label="事件类型">ActualCashflow</el-descriptions-item>
              <el-descriptions-item label="交易方向">
                <el-tag :type="detail.direction === 'Inflow' ? 'success' : 'danger'" size="small">
                  {{ detail.direction === 'Inflow' ? '流入' : '流出' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="金额"><span class="mono">{{ formatAmount(detail.amount, detail.currency) }}</span></el-descriptions-item>
              <el-descriptions-item label="币种">{{ detail.currency }}</el-descriptions-item>
              <el-descriptions-item label="规则码">AC_{{ (detail.direction || '').toUpperCase() }}_DEFAULT</el-descriptions-item>
              <el-descriptions-item label="分录笔数">{{ expectedGlEntryCount }} 笔</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="`操作历史 (${actionList.length})`" name="action">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('action')">展开全部</el-button>
            </div>
            <el-table :data="actionList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="actionNumber" label="Action 编号" width="160" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="operator" label="操作人" width="90" />
              <el-table-column prop="operateAt" label="操作时间" width="160" />
              <el-table-column label="一级审批" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalTag(row.approvalStatus1)" size="small">{{ row.approvalStatus1 || '-' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="二级审批" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalTag(row.approvalStatus2)" size="small">{{ row.approvalStatus2 || '-' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="actionList.length === 0" description="暂无 Action 数据" :image-size="60" />
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

    <!-- 审批弹窗 -->
    <ActionApprovalDialog v-model="approvalVisible" :deal="detail" :actions="actionList" @approved="onApproved" />

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
import { getAcDealByNumber, createAcDeal, updateAcDeal, deleteAcDeal, copyAcDeal } from '@/api/dealing/acDeal'
import ActionApprovalDialog from './ActionApprovalDialog.vue'
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
const activeTab = ref('dealmap')
const loadingDetail = ref(false)
const approvalVisible = ref(false)
const errorMessage = ref('')

const fullDialogVisible = ref(false)
const fullDialogTitle = ref('')
const fullDialogData = ref([])
const fullDialogColumns = ref([])

const getStatusLabel = (s) => ({ New: '新建', Approved: '已审批', Canceled: '已删除' }[s] || s)
const getStatusType = (s) => ({ New: 'info', Approved: 'success', Canceled: 'danger' }[s] || 'info')
const getActionTypeLabel = (t) => ({ CREATE: '创建', UPDATE: '修改', DELETE: '删除', APPROVE: '审批', REJECT: '驳回' }[t] || t)
const getActionTypeTag = (t) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger' }[t] || 'info')
const getApprovalTag = (s) => ({ Approved: 'success', Pending: 'warning', Rejected: 'danger' }[s] || 'info')
const formatAmount = (amount, currency) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount) + (currency ? ' ' + currency : '')
}

const formRef = ref()
const saving = ref(false)

const emptyForm = () => ({
  dealNumber: '',
  dealType: 'AC',
  managementEntity: '',
  managementEntityName: '',
  traderId: null,
  traderName: '',
  counterpartyId: null,
  counterpartyName: '',
  instrumentId: null,
  instrumentName: '',
  direction: 'Outflow',
  amount: 0,
  currency: '',
  dealDate: new Date().toISOString().slice(0, 10),
  valueDate: new Date().toISOString().slice(0, 10),
  bankAccountId: null,
  bankAccountName: '',
  counterpartyAccountId: null,
  counterpartyAccountName: '',
  paymentMethod: 'TRANSFER',
  description: '',
  remark: '',
  operator: 'admin'
})

const form = reactive(emptyForm())

const rules = {
  managementEntity: [{ required: true, message: '管理主体不能为空', trigger: 'change' }],
  traderId: [{ required: true, message: '交易员不能为空', trigger: 'change' }],
  direction: [{ required: true, message: '方向不能为空', trigger: 'change' }],
  amount: [{ required: true, message: '金额不能为空', trigger: 'change' }],
  currency: [{ required: true, message: '币种不能为空', trigger: 'change' }],
  dealDate: [{ required: true, message: '交易日期不能为空', trigger: 'change' }],
  valueDate: [{ required: true, message: '起息日不能为空', trigger: 'change' }],
  bankAccountId: [{ required: true, message: '本方账户不能为空', trigger: 'change' }],
  operator: [{ required: true, message: '操作人不能为空', trigger: 'blur' }]
}

const fillFormFromObject = (src) => {
  Object.assign(form, {
    dealNumber: src.dealNumber || '',
    dealType: 'AC',
    managementEntity: src.managementEntity || '',
    managementEntityName: src.managementEntityName || '',
    traderId: src.traderId ?? null,
    traderName: src.traderName || '',
    counterpartyId: src.counterpartyId ?? null,
    counterpartyName: src.counterpartyName || '',
    instrumentId: src.instrumentId ?? null,
    direction: src.direction || 'Outflow',
    amount: src.amount ?? 0,
    currency: src.currency || '',
    dealDate: src.dealDate || new Date().toISOString().slice(0, 10),
    valueDate: src.valueDate || new Date().toISOString().slice(0, 10),
    bankAccountId: src.bankAccountId ?? null,
    counterpartyAccountId: src.counterpartyAccountId ?? null,
    bankAccountName: src.bankAccountName || '',
    counterpartyAccountName: src.counterpartyAccountName || '',
    instrumentName: src.instrumentName || '',
    paymentMethod: src.paymentMethod || 'TRANSFER',
    description: src.description || '',
    remark: src.remark || '',
    operator: src.operator || 'admin'
  })
}

/**
 * 2026-07-05 修复 #1: AC 复制后表单字段全部丢失
 * <p>原因: fillFormFromObject 没有把后端返回的关联实体名称 preload 到 BaseDataPicker,
 * 导致 Picker 只显示 ID。</p>
 * <p>修复: 增加 preloadRows (reactive) + applyPreloadFromCopyData() 工具, 通过 :preload-row
 * 让 Picker 在挂载时就直接展示 "code (name)" 形式, 而非 ID 数字。</p>
 */
const preloadRows = reactive({
  managementEntity: null,
  trader: null,
  counterparty: null,
  instrument: null,
  bankAccount: null,
  counterpartyAccount: null
})

function applyPreloadFromCopyData(src) {
  // name 字段填入 Picker preloadRow, 让复制后直接展示 "code (name)"
  // management-entity Picker 用 returnField='code', 所以 preloadRow 不需要 id, 只需 code+name
  if (src.managementEntity) {
    preloadRows.managementEntity = {
      code: src.managementEntity,
      name: src.managementEntityName && src.managementEntityName.includes('(')
        ? src.managementEntityName.substring(src.managementEntityName.indexOf('(') + 1, src.managementEntityName.indexOf(')'))
        : (src.managementEntityName || '')
    }
  }
  if (src.traderId && src.traderName) preloadRows.trader = pickCodeName(src.traderName, src.traderId)
  if (src.counterpartyId && src.counterpartyName) preloadRows.counterparty = pickCodeName(src.counterpartyName, src.counterpartyId)
  if (src.instrumentId && src.instrumentName) {
    const { instrumentCode, instrumentName } = splitInstrument(src.instrumentName, src.instrumentId)
    preloadRows.instrument = { instrumentCode, instrumentName, id: src.instrumentId }
  }
  if (src.bankAccountId && src.bankAccountName) {
    const { accountNo, accountName } = splitAccount(src.bankAccountName, src.bankAccountId)
    preloadRows.bankAccount = { accountNo, accountName, id: src.bankAccountId }
  }
  if (src.counterpartyAccountId && src.counterpartyAccountName) {
    const { accountNo, accountName } = splitAccount(src.counterpartyAccountName, src.counterpartyAccountId)
    preloadRows.counterpartyAccount = { accountNo, accountName, id: src.counterpartyAccountId }
  }
}

/** 从 "code (name)" 形式的字符串提取 code/name */
function pickCodeName(nameStr, id) {
  if (!nameStr) return null
  let code = nameStr
  let name = nameStr
  if (nameStr.includes('(')) {
    code = nameStr.substring(0, nameStr.indexOf('(')).trim()
    name = nameStr.substring(nameStr.indexOf('(') + 1, nameStr.lastIndexOf(')')).trim()
  }
  return { id, code, name }
}

function splitInstrument(nameStr, id) {
  if (!nameStr) return { instrumentCode: null, instrumentName: null }
  if (nameStr.includes('(')) {
    return {
      instrumentCode: nameStr.substring(0, nameStr.indexOf('(')).trim(),
      instrumentName: nameStr.substring(nameStr.indexOf('(') + 1, nameStr.lastIndexOf(')')).trim()
    }
  }
  return { instrumentCode: nameStr, instrumentName: '' }
}

function splitAccount(nameStr, id) {
  if (!nameStr) return { accountNo: null, accountName: null }
  if (nameStr.includes('(')) {
    return {
      accountNo: nameStr.substring(0, nameStr.indexOf('(')).trim(),
      accountName: nameStr.substring(nameStr.indexOf('(') + 1, nameStr.lastIndexOf(')')).trim()
    }
  }
  return { accountNo: nameStr, accountName: '' }
}

const onCounterpartyChange = (row) => {
  form.counterpartyId = row?.id ?? null
  form.counterpartyAccountId = null
  form.counterpartyAccountName = ''
}

const isDirty = computed(() => {
  if (mode.value === 'new' || mode.value === 'copy') return true
  return JSON.stringify(form) !== JSON.stringify(detail.value)
})

const handleBack = () => router.push('/dealing/ac-deal')

const enterEdit = () => {
  fillFormFromObject(detail.value)
  router.replace({ path: '/dealing/ac-deal/detail', query: { dealNumber: detail.value.dealNumber, edit: 1 } })
}

const enterCopy = async () => {
  try {
    const res = await copyAcDeal(detail.value.dealNumber)
    const data = res.data || res
    fillFormFromObject(data)
    applyPreloadFromCopyData(data)
    form.dealNumber = ''
    router.replace({ path: '/dealing/ac-deal/detail', query: { copyFrom: detail.value.dealNumber } })
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
    if (detail.value.dealNumber) {
      router.replace({ path: '/dealing/ac-deal/detail', query: { dealNumber: detail.value.dealNumber } })
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
  } catch (e) {
    errorMessage.value = '请检查表单填写'
    setTimeout(() => { errorMessage.value = '' }, 5000)
    return
  }
  saving.value = true
  try {
    let res
    if (mode.value === 'edit' && form.dealNumber) {
      res = await updateAcDeal(form)
      ElMessage.success('保存成功')
      await loadData(form.dealNumber)
      router.replace({ path: '/dealing/ac-deal/detail', query: { dealNumber: form.dealNumber } })
    } else {
      const createData = { ...form }
      delete createData.dealNumber
      res = await createAcDeal(createData)
      const newNumber = res.data?.dealNumber
      ElMessage.success('创建成功')
      if (newNumber) {
        router.replace({ path: '/dealing/ac-deal/detail', query: { dealNumber: newNumber } })
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
    await ElMessageBox.confirm(
      `确认删除 AC 交易 ${detail.value.dealNumber}？\n将级联软删 Deal/DealMap/Cashflow，并记录 DealImage。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteAcDeal(detail.value.id)
    ElMessage.success('删除成功')
    handleBack()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleApprove = () => { approvalVisible.value = true }
const onApproved = async () => {
  approvalVisible.value = false
  ElMessage.success('审批成功')
  await loadData(detail.value.dealNumber)
}

const expectedGlEntryCount = computed(() => {
  if (!detail.value || !detail.value.direction) return 0
  return 2
})

const openFullDialog = (tabName) => {
  if (tabName === 'dealmap') {
    fullDialogTitle.value = `DealMap (${dealMapList.value.length})`
    fullDialogData.value = dealMapList.value
    fullDialogColumns.value = [
      { prop: 'dealmapNumber', label: 'DealMap 编号', width: 160 },
      { prop: 'actionNumber', label: 'Action 编号', width: 160 },
      { prop: 'eventType', label: '事件类型', width: 140 },
      { prop: 'eventStatus', label: '状态', width: 100, align: 'center', tag: (r) => r.eventStatus === 'Active' ? 'success' : 'info' },
      { prop: 'direction', label: '方向', width: 80, align: 'center' },
      { prop: 'amount', label: '金额', width: 140, align: 'right', formatter: (r) => formatAmount(r.amount, r.currency) },
      { prop: 'eventDate', label: '事件日期', width: 120, align: 'center' },
      { prop: 'description', label: '描述', width: 200 }
    ]
  } else if (tabName === 'cashflow') {
    fullDialogTitle.value = `现金流 (${cashflowList.value.length})`
    fullDialogData.value = cashflowList.value
    fullDialogColumns.value = [
      { prop: 'cflowNumber', label: '现金流编号', width: 160 },
      { prop: 'dealmapNumber', label: '关联 DealMap', width: 160 },
      { prop: 'direction', label: '方向', width: 80, align: 'center', tag: (r) => r.direction === 'Inflow' ? 'success' : 'danger' },
      { prop: 'amount', label: '金额', width: 140, align: 'right', formatter: (r) => formatAmount(r.amount, r.currency) },
      { prop: 'currency', label: '币种', width: 80, align: 'center' },
      { prop: 'valueDate', label: '起息日', width: 110, align: 'center' },
      { prop: 'status', label: '状态', width: 90, align: 'center', tag: (r) => r.status === 'Created' ? 'success' : 'info' }
    ]
  } else if (tabName === 'action') {
    fullDialogTitle.value = `操作历史 (${actionList.value.length})`
    fullDialogData.value = actionList.value
    fullDialogColumns.value = [
      { prop: 'actionNumber', label: 'Action 编号', width: 160 },
      { prop: 'actionType', label: '类型', width: 100, align: 'center', tag: getActionTypeTag, formatter: getActionTypeLabel },
      { prop: 'operator', label: '操作人', width: 90 },
      { prop: 'operateAt', label: '操作时间', width: 160 },
      { prop: 'approvalStatus1', label: '一级审批', width: 100, align: 'center', tag: getApprovalTag },
      { prop: 'approvalStatus2', label: '二级审批', width: 100, align: 'center', tag: getApprovalTag },
      { prop: 'approvalRemark', label: '审批意见', width: 200 }
    ]
  }
  fullDialogVisible.value = true
}

const loadData = async (dealNumber) => {
  if (!dealNumber) return
  loadingDetail.value = true
  try {
    const res = await getAcDealByNumber(dealNumber)
    const d = res.data
    detail.value = d
    dealMapList.value = d.dealMapList || []
    cashflowList.value = d.cashflowList || []
    actionList.value = d.actionList || []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败:请重试')
  } finally {
    loadingDetail.value = false
  }
}

const loadCopyData = async (dealNumber) => {
  try {
    const res = await copyAcDeal(dealNumber)
    const data = res.data || res
    fillFormFromObject(data)
    applyPreloadFromCopyData(data)
    form.dealNumber = ''
  } catch (e) {
    ElMessage.error('复制失败: ' + (e?.message || '请重试'))
  }
}

const init = async () => {
  if (mode.value === 'new') {
    Object.assign(form, emptyForm())
    return
  }
  // copy 模式: 直接通过 copyFrom 拉取可复制字段
  if (mode.value === 'copy') {
    const copyFrom = route.query.copyFrom
    if (copyFrom) await loadCopyData(copyFrom)
    return
  }
  const num = route.query.dealNumber || route.params.dealNumber
  if (!num) return
  await loadData(num)
  if (mode.value === 'edit') {
    fillFormFromObject(detail.value)
  }
}

onMounted(init)
</script>

<style scoped>
.ac-deal-detail { padding-bottom: 64px; }

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

.fade-up-enter-active,
.fade-up-leave-active { transition: all 0.2s ease; }
.fade-up-enter-from,
.fade-up-leave-to { opacity: 0; transform: translateY(10px); }
</style>