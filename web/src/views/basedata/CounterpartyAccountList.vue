<template>
  <div class="counterparty-account-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="对手方">
          <el-select v-model="queryForm.counterpartyId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开户银行">
          <el-select v-model="queryForm.bankId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in bankList" :key="item.id" :label="item.name" :value="item.id" />
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
        <el-table-column prop="accountNo" label="账户编号" width="120" />
        <el-table-column prop="counterpartyName" label="对手方" min-width="150" />
        <el-table-column prop="bankName" label="开户银行" min-width="150" />
        <el-table-column prop="accountName" label="账户名称" min-width="180" />
        <el-table-column prop="account" label="账号" width="200" />
        <el-table-column prop="currencyName" label="币种" width="80" />
        <el-table-column prop="accountType" label="账户类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.accountType) }}</el-tag>
          </template>
        </el-table-column>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="480px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="账户编号" prop="accountNo">
          <el-input v-model="formData.accountNo" placeholder="唯一编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="对手方" prop="counterpartyId">
          <el-select v-model="formData.counterpartyId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in counterpartyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开户银行" prop="bankId">
          <el-select v-model="formData.bankId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in bankList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="formData.accountName" placeholder="账户户名" />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="formData.account" placeholder="银行账号" />
        </el-form-item>
        <el-form-item label="币种" prop="currencyCode">
          <el-select v-model="formData.currencyCode" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in currencyList" :key="item.currencyCode" :label="item.currencyName" :value="item.currencyCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="formData.accountType" placeholder="请选择" style="width: 100%;">
            <el-option label="基本户" value="BASIC" />
            <el-option label="一般户" value="GENERAL" />
          </el-select>
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
import { listCounterpartyAccount, saveCounterpartyAccount, updateCounterpartyAccount, deleteCounterpartyAccount, listCounterparty, listBank, listCurrency } from '@/api/basedata'

const route = useRoute()
const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const counterpartyList = ref([])
const bankList = ref([])
const currencyList = ref([])

const queryForm = reactive({ counterpartyId: route.query.counterpartyId || '', bankId: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null,
  accountNo: '',
  counterpartyId: '',
  bankId: '',
  accountName: '',
  account: '',
  currencyCode: '',
  accountType: '',
  status: '1'
})

const rules = {
  accountNo: [{ required: true, message: '请输入账户编号', trigger: 'blur' }],
  counterpartyId: [{ required: true, message: '请选择对手方', trigger: 'change' }],
  bankId: [{ required: true, message: '请选择开户银行', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  currencyCode: [{ required: true, message: '请选择币种', trigger: 'change' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑对手方账户' : '新增对手方账户'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { BASIC: '基本户', GENERAL: '一般户' }
  return map[type] || type
}

const fetchCounterpartyList = async () => {
  try {
    const res = await listCounterparty({ pageSize: 1000 })
    counterpartyList.value = res.data.list || []
  } catch (error) {
    console.error('Failed to fetch counterparties:', error)
  }
}

const fetchBankList = async () => {
  try {
    const res = await listBank({ pageSize: 1000 })
    bankList.value = res.data.list || []
  } catch (error) {
    console.error('Failed to fetch banks:', error)
  }
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
      counterpartyId: queryForm.counterpartyId,
      bankId: queryForm.bankId,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listCounterpartyAccount(params)
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
  queryForm.counterpartyId = ''
  queryForm.bankId = ''
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null, accountNo: '', counterpartyId: '', bankId: '', accountName: '', account: '', currencyCode: '', accountType: '', status: '1'
  })
  if (route.query.counterpartyId) {
    formData.counterpartyId = parseInt(route.query.counterpartyId)
  }
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该账户吗?', '提示', { type: 'warning' })
    await deleteCounterpartyAccount(row.id)
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
          await updateCounterpartyAccount(formData)
          ElMessage.success('更新成功')
        } else {
          await saveCounterpartyAccount(formData)
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
  fetchCounterpartyList()
  fetchBankList()
  fetchCurrencyList()
  fetchData()
})
</script>

<style scoped>
.counterparty-account-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>