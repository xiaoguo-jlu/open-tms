<template>
  <div class="rule-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="管理主体" required>
          <el-select v-model="queryForm.managementEntityId" placeholder="请选择主体" clearable filterable @change="handleQuery">
            <el-option v-for="item in managementEntityList" :key="item.id" :label="item.name" :value="item.id" />
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
            <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金融产品">
          <el-select v-model="queryForm.instrumentId" placeholder="全部" clearable filterable @change="handleQuery">
            <el-option v-for="item in instrumentList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="币种">
          <el-select v-model="queryForm.currency" placeholder="全部" clearable @change="handleQuery">
            <el-option label="USD" value="USD" />
            <el-option label="CNY" value="CNY" />
            <el-option label="EUR" value="EUR" />
            <el-option label="JPY" value="JPY" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="规则编号/描述" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">🔍 查询</el-button>
          <el-button @click="handleReset">↻ 重置</el-button>
          <el-button type="success" @click="handleAdd">+ 新增规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleNumber" label="规则编号" width="160">
          <template #default="{ row }">
            <span class="text-mono">{{ row.ruleNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="对手方" width="120">
          <template #default="{ row }">{{ row.counterpartyName || 'ALL' }}</template>
        </el-table-column>
        <el-table-column prop="instrumentName" label="金融产品" width="180">
          <template #default="{ row }">{{ row.instrumentName || 'ALL' }}</template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDirectionType(row.direction)">{{ row.direction }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center">
          <template #default="{ row }">{{ row.currency || 'ALL' }}</template>
        </el-table-column>
        <el-table-column prop="bankAccountName" label="默认账户" min-width="180">
          <template #default="{ row }">{{ row.bankAccountName || `#${row.bankAccountId}` }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.priority" :min="0" :max="9999" :step="10" size="small" controls-position="right"
              style="width: 90px" @change="(val) => handlePriorityChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="Active" inactive-value="Inactive"
              @change="(val) => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="生效日" width="110" align="center">
          <template #default="{ row }">{{ row.startDate || '立即' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleCopy(row)">复制</el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑规则' : '新增规则'" width="720px" @closed="handleEditClosed">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px">
        <el-alert v-if="editForm.id" type="warning" :closable="false" style="margin-bottom: 12px">
          🔒 此规则已加载 · 数据更新于 {{ editForm.updatedAt }} · 编辑提交时若规则被他人修改会提示刷新
        </el-alert>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="管理主体" prop="managementEntityId">
              <el-select v-model="editForm.managementEntityId" placeholder="请选择主体" :disabled="!!editForm.id" filterable>
                <el-option v-for="item in managementEntityList" :key="item.id" :label="item.name" :value="item.id" />
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
              <el-select v-model="editForm.counterpartyId" placeholder="ALL 通配" clearable filterable>
                <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金融产品">
              <el-select v-model="editForm.instrumentId" placeholder="ALL 通配" clearable filterable>
                <el-option v-for="item in instrumentList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="方向" prop="direction">
              <el-select v-model="editForm.direction" placeholder="请选择">
                <el-option label="Inflow (收)" value="Inflow" />
                <el-option label="Outflow (付)" value="Outflow" />
                <el-option label="ALL (通配)" value="ALL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种">
              <el-select v-model="editForm.currency" placeholder="ALL 通配" clearable>
                <el-option label="USD" value="USD" />
                <el-option label="CNY" value="CNY" />
                <el-option label="EUR" value="EUR" />
                <el-option label="JPY" value="JPY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="默认账户" prop="bankAccountId">
              <el-select v-model="editForm.bankAccountId" placeholder="请选择账户" filterable>
                <el-option v-for="item in filteredBankAccounts" :key="item.id"
                  :label="`${item.accountName} #${item.id} (${item.currency})`" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始生效日">
              <el-date-picker v-model="editForm.startDate" type="date" placeholder="立即生效" value-format="YYYY-MM-DD"
                style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="editForm.priority" :min="0" :max="9999" :step="10" />
              <span style="margin-left: 8px; color: #909399; font-size: 12px">范围 0-9999</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="editForm.status" active-value="Active" inactive-value="Inactive" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="业务说明">
          <el-input v-model="editForm.description" placeholder="例如:USD SPOT 默认收账账户" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-alert type="info" :closable="false" v-if="editForm.id">
          校验:Active 唯一约束(同维度组合) · priority 0-9999 · 账户归属主体
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审计历史对话框 -->
    <el-dialog v-model="auditDialogVisible" title="审计历史" width="800px">
      <el-timeline>
        <el-timeline-item v-for="item in auditLogs" :key="item.id" :timestamp="item.operatedAt" placement="top">
          <h4>{{ item.operation }} · {{ item.operator }}</h4>
          <p style="color: #909399; font-size: 12px">{{ item.remark || '无备注' }}</p>
          <el-collapse>
            <el-collapse-item title="变更详情">
              <pre v-if="item.oldValue" style="color: #f56c6c; font-size: 12px">{{ item.oldValue }}</pre>
              <pre v-if="item.newValue" style="color: #67c23a; font-size: 12px">{{ item.newValue }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageDefaultBankAccountRule,
  getDefaultBankAccountRule,
  saveDefaultBankAccountRule,
  updateDefaultBankAccountRule,
  deleteDefaultBankAccountRule,
  enableDefaultBankAccountRule,
  disableDefaultBankAccountRule,
  getDefaultBankAccountRuleAuditLogs,
  getDefaultBankAccountRuleReferenceCount
} from '@/api/basedata/defaultBankAccountRule'
import { listManagementEntity } from '@/api/basedata/managementEntity'
import { listCounterparty } from '@/api/basedata/counterparty'
import { listInstrument } from '@/api/basedata/instrument'
import { listBankAccount } from '@/api/basedata/bankAccount'

// ============= State =============
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const managementEntityList = ref([])
const counterpartyList = ref([])
const instrumentList = ref([])
const bankAccountList = ref([])
const editDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const auditLogs = ref([])
const editFormRef = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  managementEntityId: null,
  counterpartyId: null,
  instrumentId: null,
  currency: null,
  status: null,
  keyword: null
})

const editForm = reactive({
  id: null,
  lockToken: null,
  ruleNumber: null,
  managementEntityId: null,
  counterpartyId: null,
  instrumentId: null,
  direction: null,
  currency: null,
  bankAccountId: null,
  priority: 0,
  startDate: null,
  status: 'Active',
  description: '',
  remark: '',
  version: 0
})

const editRules = {
  managementEntityId: [{ required: true, message: '主体必填', trigger: 'change' }],
  direction: [{ required: true, message: '方向必填', trigger: 'change' }],
  bankAccountId: [{ required: true, message: '默认账户必填', trigger: 'change' }],
  priority: [{ required: true, type: 'number', min: 0, max: 9999, message: '范围 0-9999', trigger: 'blur' }]
}

// ============= Computed =============
const filteredBankAccounts = computed(() => {
  if (!editForm.managementEntityId) return bankAccountList.value
  // BankAccount 字段已对齐 DB:businessUnitId(原 managementEntityId)
  return bankAccountList.value.filter(a => a.businessUnitId === editForm.managementEntityId)
})

// ============= Methods =============
const getDirectionType = (dir) => {
  if (dir === 'Inflow') return 'success'
  if (dir === 'Outflow') return 'warning'
  return 'info'
}

const handleQuery = async () => {
  if (!queryForm.managementEntityId) {
    ElMessage.warning('请先选择管理主体')
    return
  }
  loading.value = true
  try {
    const res = await pageDefaultBankAccountRule(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询异常:' + e.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 20, counterpartyId: null, instrumentId: null, currency: null, status: null, keyword: null })
  handleQuery()
}

const loadBaseData = async () => {
  try {
    const [mgmt, cp, ins, ba] = await Promise.all([
      listManagementEntity({ pageSize: 1000 }),
      listCounterparty({ pageSize: 1000 }),
      listInstrument({ pageSize: 1000 }),
      listBankAccount({ pageSize: 1000 })
    ])
    managementEntityList.value = mgmt.data?.records || []
    counterpartyList.value = cp.data?.records || []
    instrumentList.value = ins.data?.records || []
    bankAccountList.value = ba.data?.records || []
  } catch (e) {
    console.warn('基础数据加载失败:', e.message)
  }
}

const handleAdd = () => {
  Object.assign(editForm, {
    id: null,
    lockToken: null,
    ruleNumber: null,
    managementEntityId: queryForm.managementEntityId,
    counterpartyId: null,
    instrumentId: null,
    direction: 'Inflow',
    currency: null,
    bankAccountId: null,
    priority: 0,
    startDate: null,
    status: 'Active',
    description: '',
    remark: '',
    version: 0
  })
  editDialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDefaultBankAccountRule(row.id)
    if (res.code === 200 && res.data) {
      Object.assign(editForm, res.data)
      editDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('加载失败:' + e.message)
  }
}

const handleCopy = async (row) => {
  try {
    const res = await getDefaultBankAccountRule(row.id)
    if (res.code === 200 && res.data) {
      Object.assign(editForm, res.data, {
        id: null,
        lockToken: null,
        ruleNumber: null,
        description: `[复制] ${res.data.description || ''}`
      })
      editDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('复制失败:' + e.message)
  }
}

const handleSave = async () => {
  try {
    await editFormRef.value.validate()
    saving.value = true
    let res
    if (editForm.id) {
      res = await updateDefaultBankAccountRule(editForm)
    } else {
      res = await saveDefaultBankAccountRule(editForm)
    }
    if (res.code === 200) {
      ElMessage.success(editForm.id ? '更新成功' : '新增成功')
      editDialogVisible.value = false
      handleQuery()
    } else if (res.code === 409) {
      // ★ v1.1 并发冲突
      ElMessageBox.confirm(res.message, '规则已被他人修改', {
        confirmButtonText: '刷新',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => handleQuery()).catch(() => {})
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    if (e?.message) ElMessage.error('保存异常:' + e.message)
  } finally {
    saving.value = false
  }
}

const handleEditClosed = () => {
  editFormRef.value?.resetFields()
}

const handleDelete = async (row) => {
  try {
    // ★ v1.1 显示被引用 N
    const refRes = await getDefaultBankAccountRuleReferenceCount(row.id)
    const refCount = refRes.data?.totalCount || 0
    const msg = refCount > 0
      ? `该规则已被 ${refCount} 笔交易引用,确认删除?`
      : '确认删除该规则?'
    await ElMessageBox.confirm(msg, '删除确认', { type: 'warning' })
    const res = await deleteDefaultBankAccountRule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      handleQuery()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除异常:' + e.message)
  }
}

const handleStatusChange = async (row, val) => {
  try {
    const fn = val === 'Active' ? enableDefaultBankAccountRule : disableDefaultBankAccountRule
    const res = await fn(row.id)
    if (res.code === 200) {
      ElMessage.success(val === 'Active' ? '启用成功' : '停用成功')
    } else {
      row.status = val === 'Active' ? 'Inactive' : 'Active' // revert
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    row.status = val === 'Active' ? 'Inactive' : 'Active'
    ElMessage.error('操作异常:' + e.message)
  }
}

const handlePriorityChange = async (row, val) => {
  try {
    const detail = await getDefaultBankAccountRule(row.id)
    if (detail.code !== 200) return
    const res = await updateDefaultBankAccountRule({ ...detail.data, priority: val })
    if (res.code === 200) {
      ElMessage.success('优先级已更新')
    } else if (res.code === 409) {
      ElMessage.warning('规则已被他人修改,请刷新')
      handleQuery()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新异常:' + e.message)
  }
}

const handleAuditLog = async (row) => {
  try {
    const res = await getDefaultBankAccountRuleAuditLogs(row.id, { pageNum: 1, pageSize: 50 })
    if (res.code === 200) {
      auditLogs.value = res.data.records || []
      auditDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('加载审计失败:' + e.message)
  }
}

// ============= Lifecycle =============
onMounted(() => {
  loadBaseData()
})
</script>

<style scoped>
.rule-list { padding: 16px; }
.filter-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.text-mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; }
</style>