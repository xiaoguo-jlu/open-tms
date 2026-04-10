<template>
  <div class="auto-rule-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="规则名称">
          <el-input v-model="queryForm.ruleName" placeholder="请输入规则名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="触发类型">
          <el-select v-model="queryForm.triggerType" placeholder="请选择" clearable>
            <el-option label="余额触发" value="BALANCE" />
            <el-option label="时间触发" value="TIME" />
            <el-option label="手动触发" value="MANUAL" />
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
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleNo" label="规则编号" width="120" />
        <el-table-column prop="ruleName" label="规则名称" min-width="180" />
        <el-table-column prop="poolName" label="所属池" width="120" />
        <el-table-column prop="triggerType" label="触发类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.triggerType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="triggerCondition" label="触发条件" min-width="150">
          <template #default="{ row }">
            {{ row.triggerCondition }}
          </template>
        </el-table-column>
        <el-table-column prop="amountType" label="调拨金额" width="100">
          <template #default="{ row }">
            {{ getAmountTypeLabel(row.amountType) }}
          </template>
        </el-table-column>
        <el-table-column prop="needApprove" label="需审批" width="80" align="center">
          <template #default="{ row }">
            {{ row.needApprove === '1' ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleTest(row)">测试</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="560px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="规则编号" prop="ruleNo">
          <el-input v-model="formData.ruleNo" placeholder="唯一编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="所属现金池" prop="poolId">
          <el-select v-model="formData.poolId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in poolList" :key="item.id" :label="item.poolName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发类型" prop="triggerType">
          <el-select v-model="formData.triggerType" placeholder="请选择" style="width: 100%;" @change="handleTriggerChange">
            <el-option label="余额触发" value="BALANCE" />
            <el-option label="时间触发" value="TIME" />
            <el-option label="手动触发" value="MANUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件" prop="triggerCondition" v-if="formData.triggerType === 'BALANCE'">
          <el-input v-model="formData.triggerCondition" placeholder="如: balance < 100000" />
        </el-form-item>
        <el-form-item label="触发时间" prop="triggerTime" v-if="formData.triggerType === 'TIME'">
          <el-time-picker v-model="formData.triggerTime" placeholder="选择时间" format="HH:mm" value-format="HH:mm" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="调拨金额" prop="amountType">
          <el-select v-model="formData.amountType" placeholder="请选择" style="width: 100%;">
            <el-option label="固定金额" value="FIXED" />
            <el-option label="按比例" value="PERCENTAGE" />
            <el-option label="剩余金额" value="REMAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额值" prop="amountValue" v-if="formData.amountType === 'FIXED' || formData.amountType === 'PERCENTAGE'">
          <el-input-number v-model="formData.amountValue" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="需审批" prop="needApprove">
          <el-switch v-model="formData.needApprove" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="审批额度" prop="approveLimit" v-if="formData.needApprove === '1'">
          <el-input-number v-model="formData.approveLimit" :min="0" :precision="2" :controls="false" style="width: 100%;" placeholder="超过此额度需审批" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAutoRule, saveAutoRule, updateAutoRule, deleteAutoRule, testAutoRule, listCashPool } from '@/api/cashpool'

const route = useRoute()
const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const poolList = ref([])

const queryForm = reactive({ ruleName: '', triggerType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, ruleNo: '', ruleName: '', poolId: '', triggerType: '', triggerCondition: '',
  triggerTime: '', amountType: '', amountValue: null, needApprove: '0', approveLimit: null, status: '1'
})

const rules = {
  ruleNo: [{ required: true, message: '请输入规则编号', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  poolId: [{ required: true, message: '请选择现金池', trigger: 'change' }],
  triggerType: [{ required: true, message: '请选择触发类型', trigger: 'change' }],
  amountType: [{ required: true, message: '请选择调拨金额', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑规则' : '新增规则'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { BALANCE: '余额触发', TIME: '时间触发', MANUAL: '手动触发' }
  return map[type] || type
}

const getAmountTypeLabel = (type) => {
  const map = { FIXED: '固定', PERCENTAGE: '比例', REMAIN: '剩余' }
  return map[type] || type
}

const handleTriggerChange = () => {
  formData.triggerCondition = ''
  formData.triggerTime = ''
}

const fetchPoolList = async () => {
  poolList.value = (await listCashPool({ pageSize: 1000 })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, poolId: route.query.poolId, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listAutoRule(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { ruleName: '', triggerType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, ruleNo: '', ruleName: '', poolId: route.query.poolId || '', triggerType: '', triggerCondition: '', triggerTime: '', amountType: '', amountValue: null, needApprove: '0', approveLimit: null, status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗?', '提示', { type: 'warning' })
    await deleteAutoRule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleTest = async (row) => {
  try {
    await testAutoRule(row.id)
    ElMessage.success('规则测试通过')
  } catch (e) { console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        formData.id ? await updateAutoRule(formData) : await saveAutoRule(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchPoolList(); fetchData() })
</script>

<style scoped>
.auto-rule-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>