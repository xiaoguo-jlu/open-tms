<template>
  <div class="deal-approval-rule-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="管理主体">
          <el-select v-model="queryForm.managementEntityId" placeholder="全部" clearable filterable @change="handleQuery">
            <el-option v-for="item in managementEntityList" :key="item.id" :label="getManagementEntityLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @change="handleQuery">
            <el-option label="启用" value="Active" />
            <el-option label="停用" value="Inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="对手方">
          <el-select v-model="queryForm.counterpartyId" placeholder="全部" clearable filterable @change="handleQuery">
            <el-option v-for="item in counterpartyList" :key="item.id" :label="getCounterpartyLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金融工具">
          <el-select v-model="queryForm.instrumentId" placeholder="全部" clearable filterable @change="handleQuery">
            <el-option v-for="item in instrumentList" :key="item.id" :label="getInstrumentLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易员">
          <el-select v-model="queryForm.dealerId" placeholder="全部" clearable filterable @change="handleQuery">
            <el-option v-for="item in traderList" :key="item.id" :label="getTraderLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Action Type">
          <el-select v-model="queryForm.actionType" placeholder="全部" clearable @change="handleQuery">
            <el-option v-for="item in actionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
          <el-button type="warning" @click="handleOpenMatchDialog">Match 测试</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border empty-text="暂无交易审批规则">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleNumber" label="规则编号" width="170">
          <template #default="{ row }">
            <span class="text-mono">{{ row.ruleNumber || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="managementEntityName" label="管理主体" min-width="140">
          <template #default="{ row }">{{ row.managementEntityName || formatWildcard(row.managementEntityId) }}</template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="对手方" min-width="140">
          <template #default="{ row }">{{ row.counterpartyName || formatWildcard(row.counterpartyId) }}</template>
        </el-table-column>
        <el-table-column prop="instrumentName" label="金融工具" min-width="140">
          <template #default="{ row }">{{ row.instrumentName || formatWildcard(row.instrumentId) }}</template>
        </el-table-column>
        <el-table-column prop="dealerName" label="交易员" min-width="120">
          <template #default="{ row }">{{ row.dealerName || formatWildcard(row.dealerId) }}</template>
        </el-table-column>
        <el-table-column prop="actionType" label="Action Type" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalLevel" label="审批层级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalLevelTag(row.approvalLevel)" size="small">{{ getApprovalLevelLabel(row.approvalLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level1Roles" label="L1 角色" min-width="160">
          <template #default="{ row }">
            <RoleTags :roles="row.level1RolesDisplay || row.level1Roles" />
          </template>
        </el-table-column>
        <el-table-column prop="level2Roles" label="L2 角色" min-width="160">
          <template #default="{ row }">
            <RoleTags :roles="row.level2RolesDisplay || row.level2Roles" />
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="Active"
              inactive-value="Inactive"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleAuditLog(row)">审计</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handleQuery"
        @size-change="handleQuery"
        style="margin-top: 12px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑交易审批规则' : '新增交易审批规则'" width="820px" @closed="handleEditClosed">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="120px">
        <el-alert v-if="editForm.id" type="warning" :closable="false" style="margin-bottom: 12px">
          此规则已加载，编辑提交时将校验 lockToken，若规则被他人修改会提示刷新。
        </el-alert>

        <el-divider content-position="left">5 维匹配要素</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="管理主体">
              <el-select v-model="editForm.managementEntityId" placeholder="ALL 通配" clearable filterable style="width: 100%">
                <el-option v-for="item in managementEntityList" :key="item.id" :label="getManagementEntityLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则编号">
              <el-input v-model="editForm.ruleNumber" disabled placeholder="系统自动生成" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="对手方">
              <el-select v-model="editForm.counterpartyId" placeholder="ALL 通配" clearable filterable style="width: 100%">
                <el-option v-for="item in counterpartyList" :key="item.id" :label="getCounterpartyLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金融工具">
              <el-select v-model="editForm.instrumentId" placeholder="ALL 通配" clearable filterable style="width: 100%">
                <el-option v-for="item in instrumentList" :key="item.id" :label="getInstrumentLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="交易员">
              <el-select v-model="editForm.dealerId" placeholder="ALL 通配" clearable filterable style="width: 100%">
                <el-option v-for="item in traderList" :key="item.id" :label="getTraderLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Action Type" prop="actionType">
              <el-select v-model="editForm.actionType" placeholder="请选择 Action Type" style="width: 100%">
                <el-option v-for="item in actionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">审批策略</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="审批层级" prop="approvalLevel">
              <el-select v-model="editForm.approvalLevel" placeholder="请选择审批层级" style="width: 100%">
                <el-option v-for="item in approvalLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="editForm.priority" :min="0" :max="9999" :step="10" controls-position="right" style="width: 160px" />
              <span class="field-hint">0-9999，越大越优先</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col v-if="editForm.approvalLevel === 'LEVEL_1' || editForm.approvalLevel === 'LEVEL_2'" :span="12">
            <el-form-item label="L1 角色" prop="level1Roles">
              <el-select
                v-model="editForm.level1Roles"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入 L1 角色"
                style="width: 100%"
              >
                <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="editForm.approvalLevel === 'LEVEL_2'" :span="12">
            <el-form-item label="L2 角色" prop="level2Roles">
              <el-select
                v-model="editForm.level2Roles"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入 L2 角色"
                style="width: 100%"
              >
                <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert v-if="editForm.approvalLevel === 'LEVEL_0'" type="info" :closable="false" style="margin-bottom: 12px">
          无需审批时，L1/L2 角色字段隐藏并自动清空。
        </el-alert>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="editForm.status" active-value="Active" inactive-value="Inactive" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="effectiveDateRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="业务说明">
          <el-input v-model="editForm.description" placeholder="例如: HSBC FX SUBMIT 二层审批规则" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="matchDialogVisible" title="Match 测试" width="820px">
      <el-form ref="matchFormRef" :model="matchForm" :rules="matchRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="管理主体">
              <el-select v-model="matchForm.managementEntityId" placeholder="可留空" clearable filterable style="width: 100%">
                <el-option v-for="item in managementEntityList" :key="item.id" :label="getManagementEntityLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对手方">
              <el-select v-model="matchForm.counterpartyId" placeholder="可留空" clearable filterable style="width: 100%">
                <el-option v-for="item in counterpartyList" :key="item.id" :label="getCounterpartyLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="金融工具">
              <el-select v-model="matchForm.instrumentId" placeholder="可留空" clearable filterable style="width: 100%">
                <el-option v-for="item in instrumentList" :key="item.id" :label="getInstrumentLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交易员">
              <el-select v-model="matchForm.dealerId" placeholder="可留空" clearable filterable style="width: 100%">
                <el-option v-for="item in traderList" :key="item.id" :label="getTraderLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Action Type" prop="actionType">
              <el-select v-model="matchForm.actionType" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in actionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">匹配结果</el-divider>
      <div v-loading="matching" class="match-result">
        <el-empty v-if="!matchResult" description="请输入 5 维要素后点击开始匹配" :image-size="60" />
        <template v-else>
          <el-alert
            :type="matchResult.matched ? 'success' : 'warning'"
            :title="matchResult.matched ? '已命中交易审批规则' : '未命中新规则，后端将按降级策略处理'"
            :closable="false"
            style="margin-bottom: 12px"
          />
          <el-descriptions :column="3" size="small" border>
            <el-descriptions-item label="规则编号">{{ matchResult.matchedRule?.ruleNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批层级">
              <el-tag :type="getApprovalLevelTag(matchResult.approvalLevel)" size="small">{{ getApprovalLevelLabel(matchResult.approvalLevel) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">{{ matchResult.matchedRule?.priority ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="L1 角色"><RoleTags :roles="matchResult.level1Roles" /></el-descriptions-item>
            <el-descriptions-item label="L2 角色"><RoleTags :roles="matchResult.level2Roles" /></el-descriptions-item>
            <el-descriptions-item label="精确度">{{ matchResult.matchedRule?.specificityScore ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="3">{{ matchResult.matchedRule?.createdAt || matchResult.matchedRule?.createdTime || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-table v-if="candidateList.length" :data="candidateList" stripe size="small" style="margin-top: 12px" max-height="220">
            <el-table-column prop="ruleNumber" label="候选规则" width="170" />
            <el-table-column prop="approvalLevel" label="层级" width="110">
              <template #default="{ row }">
                <el-tag :type="getApprovalLevelTag(row.approvalLevel)" size="small">{{ getApprovalLevelLabel(row.approvalLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="specificityScore" label="精确度" width="90" align="center" />
            <el-table-column prop="priority" label="优先级" width="90" align="center" />
            <el-table-column prop="won" label="结果" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.won ? 'success' : 'info'" size="small">{{ row.won ? '胜出' : '候选' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="matchedDimensions" label="命中维度" min-width="180">
              <template #default="{ row }">{{ normalizeRoles(row.matchedDimensions).join(', ') || '-' }}</template>
            </el-table-column>
          </el-table>
        </template>
      </div>
      <template #footer>
        <el-button @click="matchDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="matching" @click="handleMatch">开始匹配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审计历史" width="820px">
      <el-timeline v-if="auditLogs.length">
        <el-timeline-item v-for="item in auditLogs" :key="item.id" :timestamp="item.operatedAt" placement="top">
          <h4>{{ item.operation }} · {{ item.operator }}</h4>
          <p class="audit-remark">{{ item.remark || '无备注' }}</p>
          <el-collapse>
            <el-collapse-item title="变更详情">
              <pre v-if="item.oldValue" class="audit-old">{{ formatJson(item.oldValue) }}</pre>
              <pre v-if="item.newValue" class="audit-new">{{ formatJson(item.newValue) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审计记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import {
  pageDealApprovalRule,
  getDealApprovalRule,
  saveDealApprovalRule,
  updateDealApprovalRule,
  deleteDealApprovalRule,
  enableDealApprovalRule,
  disableDealApprovalRule,
  matchDealApprovalRule,
  getDealApprovalRuleAuditLogs,
  getDealApprovalRuleReferenceCount
} from '@/api/basedata/dealApprovalRule'
import { listManagementEntity } from '@/api/basedata/managementEntity'
import { listCounterparty } from '@/api/basedata/counterparty'
import { listInstrument } from '@/api/basedata/instrument'
import { listTrader } from '@/api/basedata/trader'

const RoleTags = defineComponent({
  name: 'RoleTags',
  props: {
    roles: { type: [Array, String], default: () => [] }
  },
  setup(props) {
    return () => {
      const values = normalizeRoles(props.roles)
      if (!values.length) return h('span', { class: 'empty-text' }, '-')
      return h('div', { class: 'role-tags' }, values.map(role => h(ElTag, { size: 'small', type: 'info', style: { marginRight: '4px', marginBottom: '4px' } }, () => role)))
    }
  }
})

const actionTypeOptions = [
  { label: 'CREATE 创建', value: 'CREATE' },
  { label: 'SUBMIT 提交审批', value: 'SUBMIT' },
  { label: 'APPROVE 审批通过', value: 'APPROVE' },
  { label: 'REJECT 驳回', value: 'REJECT' },
  { label: 'EXECUTE 执行', value: 'EXECUTE' }
]

const approvalLevelOptions = [
  { label: '无需审批', value: 'LEVEL_0' },
  { label: '一层审批', value: 'LEVEL_1' },
  { label: '二层审批', value: 'LEVEL_2' }
]

const roleOptions = [
  { label: 'RISK_MANAGER 风控经理', value: 'RISK_MANAGER' },
  { label: 'COMPLIANCE_OFFICER 合规专员', value: 'COMPLIANCE_OFFICER' },
  { label: 'TREASURY_DIRECTOR 财资总监', value: 'TREASURY_DIRECTOR' },
  { label: 'TREASURY_MANAGER 财资经理', value: 'TREASURY_MANAGER' },
  { label: 'FINANCE_MANAGER 财务经理', value: 'FINANCE_MANAGER' },
  { label: 'CFO 首席财务官', value: 'CFO' },
  { label: 'TRADER 交易员', value: 'TRADER' },
  { label: 'DEALER 交易主管', value: 'DEALER' },
  { label: 'BACK_OFFICE 后台清算', value: 'BACK_OFFICE' },
  { label: 'ADMIN 管理员', value: 'ADMIN' }
]

const loading = ref(false)
const saving = ref(false)
const matching = ref(false)
const tableData = ref([])
const total = ref(0)
const managementEntityList = ref([])
const counterpartyList = ref([])
const instrumentList = ref([])
const traderList = ref([])
const editDialogVisible = ref(false)
const matchDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const auditLogs = ref([])
const editFormRef = ref(null)
const matchFormRef = ref(null)
const matchResult = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  managementEntityId: null,
  counterpartyId: null,
  instrumentId: null,
  dealerId: null,
  actionType: null,
  status: null
})

const editForm = reactive(createEmptyEditForm())
const matchForm = reactive(createEmptyMatchForm())

const effectiveDateRange = computed({
  get() {
    if (!editForm.startDate && !editForm.endDate) return []
    return [editForm.startDate, editForm.endDate]
  },
  set(value) {
    editForm.startDate = value?.[0] || null
    editForm.endDate = value?.[1] || null
  }
})

const candidateList = computed(() => matchResult.value?.candidates || [])

const editRules = {
  actionType: [{ required: true, message: 'Action Type 必填', trigger: 'change' }],
  approvalLevel: [{ required: true, message: '审批层级必填', trigger: 'change' }],
  priority: [{ required: true, type: 'number', min: 0, max: 9999, message: '优先级范围 0-9999', trigger: 'blur' }]
}

const matchRules = {
  actionType: [{ required: true, message: 'Action Type 必填', trigger: 'change' }]
}

function createEmptyEditForm() {
  return {
    id: null,
    lockToken: null,
    ruleNumber: null,
    managementEntityId: null,
    counterpartyId: null,
    instrumentId: null,
    dealerId: null,
    actionType: 'SUBMIT',
    approvalLevel: 'LEVEL_1',
    level1Roles: [],
    level2Roles: [],
    priority: 100,
    status: 'Active',
    startDate: null,
    endDate: null,
    description: '',
    remark: '',
    version: 0
  }
}

function createEmptyMatchForm() {
  return {
    managementEntityId: null,
    counterpartyId: null,
    instrumentId: null,
    dealerId: null,
    actionType: 'SUBMIT'
  }
}

function normalizeRoles(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.filter(Boolean)
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) return parsed.filter(Boolean)
    } catch (e) {
      // ignore: fallback to comma split
    }
    return value.split(',').map(v => v.trim()).filter(Boolean)
  }
  return []
}

watch(() => editForm.approvalLevel, (level) => {
  if (level === 'LEVEL_0') {
    editForm.level1Roles = []
    editForm.level2Roles = []
  } else if (level === 'LEVEL_1') {
    editForm.level2Roles = []
  }
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await pageDealApprovalRule({ ...queryForm })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
    ElMessage.error('查询异常:' + (e?.message || e))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    pageNum: 1,
    pageSize: 20,
    managementEntityId: null,
    counterpartyId: null,
    instrumentId: null,
    dealerId: null,
    actionType: null,
    status: null
  })
  handleQuery()
}

const loadBaseData = async ({ silent = false } = {}) => {
  const errors = []
  try {
    const res = await listManagementEntity({ pageNum: 1, pageSize: 1000 })
    managementEntityList.value = res?.data?.records || []
  } catch (e) {
    errors.push(`主体:${e?.message || e}`)
  }
  try {
    const res = await listCounterparty({ pageNum: 1, pageSize: 1000 })
    counterpartyList.value = res?.data?.records || []
  } catch (e) {
    errors.push(`对手方:${e?.message || e}`)
  }
  try {
    const res = await listInstrument({ pageNum: 1, pageSize: 1000 })
    instrumentList.value = res?.data?.records || []
  } catch (e) {
    errors.push(`金融工具:${e?.message || e}`)
  }
  try {
    const res = await listTrader({ pageNum: 1, pageSize: 1000 })
    traderList.value = res?.data?.records || []
  } catch (e) {
    errors.push(`交易员:${e?.message || e}`)
  }
  if (errors.length) {
    console.warn('[DealApprovalRule] 基础数据部分加载失败:', errors)
    if (!silent) ElMessage.warning(`基础数据加载失败(${errors.join(' / ')}),下拉可能为空`)
  }
}

const handleAdd = async () => {
  Object.assign(editForm, createEmptyEditForm())
  editDialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDealApprovalRule(row.id || row.ruleNumber)
    const detail = normalizeRuleDetail(res.data)
    Object.assign(editForm, createEmptyEditForm(), detail, {
      level1Roles: normalizeRoles(detail.level1Roles),
      level2Roles: normalizeRoles(detail.level2Roles)
    })
    editDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载失败:' + (e?.message || e))
  }
}

const handleSave = async () => {
  try {
    await editFormRef.value?.validate()
    validateApprovalRoles()
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
    return
  }
  saving.value = true
  try {
    const payload = buildSubmitPayload()
    if (editForm.id) {
      await updateDealApprovalRule(payload)
      ElMessage.success('更新成功')
    } else {
      await saveDealApprovalRule(payload)
      ElMessage.success('新增成功')
    }
    editDialogVisible.value = false
    await handleQuery()
  } catch (e) {
    if (e?.message?.includes('409')) {
      ElMessageBox.confirm(e.message, '规则已被他人修改', {
        confirmButtonText: '刷新',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => handleQuery()).catch(() => {})
    } else {
      ElMessage.error('保存异常:' + (e?.message || e))
    }
  } finally {
    saving.value = false
  }
}

const handleEditClosed = () => {
  editFormRef.value?.resetFields()
}

const handleDelete = async (row) => {
  try {
    const refRes = await getDealApprovalRuleReferenceCount(row.id)
    const refCount = refRes.data?.totalCount || 0
    const msg = refCount > 0 ? `该规则已被 ${refCount} 笔交易引用，确认删除?` : '确认删除该规则?'
    await ElMessageBox.confirm(msg, '删除确认', { type: 'warning' })
    await deleteDealApprovalRule(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除异常:' + (e?.message || e))
  }
}

const handleStatusChange = async (row, val) => {
  try {
    const fn = val === 'Active' ? enableDealApprovalRule : disableDealApprovalRule
    await fn(row.id)
    ElMessage.success(val === 'Active' ? '启用成功' : '停用成功')
  } catch (e) {
    row.status = val === 'Active' ? 'Inactive' : 'Active'
    ElMessage.error('操作异常:' + (e?.message || e))
  }
}

const handleOpenMatchDialog = () => {
  Object.assign(matchForm, createEmptyMatchForm(), {
    managementEntityId: queryForm.managementEntityId,
    counterpartyId: queryForm.counterpartyId,
    instrumentId: queryForm.instrumentId,
    dealerId: queryForm.dealerId,
    actionType: queryForm.actionType || 'SUBMIT'
  })
  matchResult.value = null
  matchDialogVisible.value = true
}

const handleMatch = async () => {
  try {
    await matchFormRef.value?.validate()
  } catch (e) {
    return
  }
  matching.value = true
  try {
    const params = Object.fromEntries(Object.entries(matchForm).filter(([, value]) => value !== null && value !== '' && value !== undefined))
    const res = await matchDealApprovalRule(params)
    matchResult.value = res.data || null
  } catch (e) {
    ElMessage.error('匹配异常:' + (e?.message || e))
  } finally {
    matching.value = false
  }
}

const handleAuditLog = async (row) => {
  try {
    const res = await getDealApprovalRuleAuditLogs(row.id, { pageNum: 1, pageSize: 50 })
    auditLogs.value = res.data?.records || []
    auditDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载审计失败:' + (e?.message || e))
  }
}

function normalizeRuleDetail(data) {
  if (!data) return {}
  if (data.dealApprovalRule) return data.dealApprovalRule
  return data
}

function buildSubmitPayload() {
  const payload = {
    id: editForm.id,
    lockToken: editForm.lockToken,
    managementEntityId: editForm.managementEntityId || null,
    counterpartyId: editForm.counterpartyId || null,
    instrumentId: editForm.instrumentId || null,
    dealerId: editForm.dealerId || null,
    actionType: editForm.actionType,
    approvalLevel: editForm.approvalLevel,
    level1Roles: normalizeRoles(editForm.level1Roles),
    level2Roles: normalizeRoles(editForm.level2Roles),
    priority: Number(editForm.priority || 0),
    status: editForm.status,
    startDate: editForm.startDate || null,
    endDate: editForm.endDate || null,
    description: editForm.description || null,
    remark: editForm.remark || null,
    version: editForm.version
  }
  if (!payload.id) {
    delete payload.id
    delete payload.lockToken
    delete payload.version
  }
  return payload
}

function validateApprovalRoles() {
  const l1 = normalizeRoles(editForm.level1Roles)
  const l2 = normalizeRoles(editForm.level2Roles)
  if (editForm.approvalLevel === 'LEVEL_0' && (l1.length || l2.length)) {
    throw new Error('无需审批时 L1/L2 角色必须为空')
  }
  if (editForm.approvalLevel === 'LEVEL_1' && !l1.length) {
    throw new Error('一层审批必须配置 L1 角色')
  }
  if (editForm.approvalLevel === 'LEVEL_1' && l2.length) {
    throw new Error('一层审批时 L2 角色必须为空')
  }
  if (editForm.approvalLevel === 'LEVEL_2' && (!l1.length || !l2.length)) {
    throw new Error('二层审批必须配置 L1 和 L2 角色')
  }
  if (l1.length > 5 || l2.length > 5) {
    throw new Error('角色列表长度不能超过 5')
  }
}

function getActionTypeLabel(type) {
  const found = actionTypeOptions.find(item => item.value === type)
  return found ? found.value : (type || '-')
}

function getActionTypeTag(type) {
  return { CREATE: 'success', SUBMIT: 'primary', APPROVE: 'success', REJECT: 'danger', EXECUTE: 'warning' }[type] || 'info'
}

function getApprovalLevelLabel(level) {
  return { LEVEL_0: '无需审批', LEVEL_1: '一层审批', LEVEL_2: '二层审批' }[level] || level || '-'
}

function getApprovalLevelTag(level) {
  return { LEVEL_0: 'success', LEVEL_1: 'warning', LEVEL_2: 'danger' }[level] || 'info'
}

function formatWildcard(id) {
  return id ? `ID:${id}` : 'ALL'
}

function getManagementEntityLabel(item) {
  const code = item.code || item.entityCode || item.managementEntityCode
  const name = item.name || item.entityName || item.managementEntityName
  return code && name ? `${code} (${name})` : (name || code || `ID:${item.id}`)
}

function getCounterpartyLabel(item) {
  const code = item.code || item.counterpartyCode
  const name = item.name || item.counterpartyName
  return code && name ? `${code} (${name})` : (name || code || `ID:${item.id}`)
}

function getInstrumentLabel(item) {
  const code = item.instrumentCode || item.code
  const name = item.instrumentName || item.name
  return code && name ? `${code} (${name})` : (name || code || `ID:${item.id}`)
}

function getTraderLabel(item) {
  const code = item.traderCode || item.code || item.username
  const name = item.traderName || item.name || item.realName
  return code && name ? `${code} (${name})` : (name || code || `ID:${item.id}`)
}

function formatJson(value) {
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value, null, 2)
  } catch (e) {
    return String(value)
  }
}

onMounted(async () => {
  await loadBaseData({ silent: true })
  await handleQuery()
})
</script>

<style scoped>
.deal-approval-rule-list { padding: 16px; }
.filter-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.text-mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; font-size: 12px; }
.field-hint { margin-left: 8px; color: #909399; font-size: 12px; }
.empty-text { color: #909399; }
.role-tags { display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
.match-result { min-height: 120px; }
.audit-remark { color: #909399; font-size: 12px; margin: 4px 0 8px; }
.audit-old { color: #f56c6c; font-size: 12px; white-space: pre-wrap; }
.audit-new { color: #67c23a; font-size: 12px; white-space: pre-wrap; }
:deep(.el-select) { min-width: 160px; }
</style>
