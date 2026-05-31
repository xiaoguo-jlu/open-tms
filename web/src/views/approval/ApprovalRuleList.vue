<template>
  <div class="approval-rule-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="规则编码/规则名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryForm.bizType" placeholder="请选择" clearable style="width: 100%;">
            <el-option label="交易" value="DEAL" />
            <el-option label="转账" value="TRANSFER" />
            <el-option label="外汇" value="FX" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleCode" label="规则编码" width="120" />
        <el-table-column prop="ruleName" label="规则名称" min-width="180" />
        <el-table-column prop="bizType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.bizType === 'DEAL'" type="success">交易</el-tag>
            <el-tag v-else-if="row.bizType === 'TRANSFER'" type="warning">转账</el-tag>
            <el-tag v-else-if="row.bizType === 'FX'" type="info">外汇</el-tag>
            <span v-else>{{ row.bizType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amountLimit" label="金额限制" width="140" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.amountLimit) }}
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="approvalLevel" label="审批级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.approvalLevel === 1" type="success">一级审批</el-tag>
            <el-tag v-else-if="row.approvalLevel === 2" type="warning">二级审批</el-tag>
            <el-tag v-else-if="row.approvalLevel === 3" type="danger">三级审批</el-tag>
            <span v-else>{{ row.approvalLevel }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="approverType" label="审批人类型" width="100">
          <template #default="{ row }">
            {{ row.approverType === 'ROLE' ? '角色' : row.approverType === 'USER' ? '用户' : row.approverType }}
          </template>
        </el-table-column>
        <el-table-column prop="approverExpr" label="审批人表达式" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="内部编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="formData.bizType" placeholder="请选择" style="width: 100%;">
            <el-option label="交易" value="DEAL" />
            <el-option label="转账" value="TRANSFER" />
            <el-option label="外汇" value="FX" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额限制" prop="amountLimit">
          <el-input-number v-model="formData.amountLimit" :precision="2" :min="0" style="width: 100%;" placeholder="请输入金额限制" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="formData.currency" placeholder="请选择" clearable style="width: 100%;">
            <el-option v-for="item in currencyList" :key="item.code" :label="item.code + ' - ' + item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批级别" prop="approvalLevel">
          <el-select v-model="formData.approvalLevel" placeholder="请选择" style="width: 100%;">
            <el-option label="一级审批" :value="1" />
            <el-option label="二级审批" :value="2" />
            <el-option label="三级审批" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人类型" prop="approverType">
          <el-select v-model="formData.approverType" placeholder="请选择" style="width: 100%;">
            <el-option label="角色" value="ROLE" />
            <el-option label="用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人表达式" prop="approverExpr">
          <el-input v-model="formData.approverExpr" placeholder="如: role:TM 或 user:zhangsan" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 16px;">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApprovalRule, saveApprovalRule, updateApprovalRule, deleteApprovalRule } from '@/api/approval'
import { listCurrency } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const currencyList = ref([])

const queryForm = reactive({ keyword: '', bizType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null,
  ruleCode: '',
  ruleName: '',
  bizType: '',
  amountLimit: 0,
  currency: '',
  approvalLevel: 1,
  approverType: '',
  approverExpr: '',
  status: '1',
  remark: ''
})

const rules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  approvalLevel: [{ required: true, message: '请选择审批级别', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑审批规则' : '新增审批规则'))
const isEdit = computed(() => !!formData.id)

const formatAmount = (amount) => {
  if (amount == null) return ''
  return new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount)
}

const fetchCurrencyList = async () => {
  try {
    const res = await listCurrency({ pageSize: 1000 })
    currencyList.value = res.data.list || []
  } catch (error) {
    console.error('Failed to fetch currencies:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword,
      bizType: queryForm.bizType,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listApprovalRule(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.bizType = ''
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null, ruleCode: '', ruleName: '', bizType: '', amountLimit: 0, currency: '',
    approvalLevel: 1, approverType: '', approverExpr: '', status: '1', remark: ''
  })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该审批规则吗?', '提示', { type: 'warning' })
    await deleteApprovalRule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateApprovalRule(formData)
          ElMessage.success('更新成功')
        } else {
          await saveApprovalRule(formData)
          ElMessage.success('新增成功')
        }
        drawerVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit failed:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchCurrencyList()
  fetchData()
})
</script>

<script>
export default {
  name: 'ApprovalRuleList'
}
</script>

<style scoped>
.approval-rule-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>