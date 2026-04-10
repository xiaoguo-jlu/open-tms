<template>
  <div class="bank-account-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="账户名称">
          <el-input v-model="queryForm.accountName" placeholder="请输入账户名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="开户银行">
          <el-select v-model="queryForm.bankId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in bankList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单元">
          <el-select v-model="queryForm.entityId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in entityList" :key="item.id" :label="item.name" :value="item.id" />
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
        <el-table-column prop="accountName" label="账户名称" min-width="150" />
        <el-table-column prop="account" label="账号" width="180" />
        <el-table-column prop="bankName" label="开户银行" min-width="150" />
        <el-table-column prop="entityName" label="所属业务单元" width="120" />
        <el-table-column prop="currencyCode" label="币种" width="80" />
        <el-table-column prop="accountType" label="账户类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.accountType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.balance) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="success" link @click="handleSync(row)" v-if="row.status === '1'">同步</el-button>
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
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="账户编号" prop="accountNo">
          <el-input v-model="formData.accountNo" placeholder="唯一编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="formData.accountName" placeholder="账户名称" />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="formData.account" placeholder="银行账号" />
        </el-form-item>
        <el-form-item label="开户银行" prop="bankId">
          <el-select v-model="formData.bankId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in bankList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属业务单元" prop="entityId">
          <el-select v-model="formData.entityId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in entityList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
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
            <el-option label="专户" value="SPECIAL" />
            <el-option label="临时户" value="TEMPORARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户性质" prop="accountNature">
          <el-select v-model="formData.accountNature" placeholder="请选择" style="width: 100%;">
            <el-option label="结算户" value="SETTLEMENT" />
            <el-option label="储蓄户" value="SAVING" />
            <el-option label="保证金户" value="MARGIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否归集" prop="isCollected">
          <el-switch v-model="formData.isCollected" :active-value="'1'" :inactive-value="'0'" />
        </el-form-item>
        <el-form-item label="归集方向" prop="collectDirection" v-if="formData.isCollected === '1'">
          <el-radio-group v-model="formData.collectDirection">
            <el-radio value="UP">上收</el-radio>
            <el-radio value="DOWN">下拨</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="归集主账户" prop="mainAccountId" v-if="formData.isCollected === '1'">
          <el-select v-model="formData.mainAccountId" placeholder="请选择" style="width: 100%;" filterable clearable>
            <el-option v-for="item in mainAccountList" :key="item.id" :label="item.accountName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日间限额" prop="dayLimit">
          <el-input-number v-model="formData.dayLimit" :min="0" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="日终限额" prop="nightLimit">
          <el-input-number v-model="formData.nightLimit" :min="0" :precision="2" :controls="false" style="width: 100%;" />
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

    <el-dialog v-model="detailVisible" title="账户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账户编号">{{ detailData.accountNo }}</el-descriptions-item>
        <el-descriptions-item label="账户名称">{{ detailData.accountName }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ detailData.account }}</el-descriptions-item>
        <el-descriptions-item label="开户银行">{{ detailData.bankName }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ formatAmount(detailData.balance) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '1' ? 'success' : 'danger'">{{ detailData.status === '1' ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBankAccount, saveBankAccount, updateBankAccount, deleteBankAccount, getAccountBalance, syncBankAccount } from '@/api/dealing'
import { listBank, listBusinessUnit, listCurrency } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const detailVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const detailData = ref({})
const bankList = ref([])
const entityList = ref([])
const currencyList = ref([])
const mainAccountList = ref([])

const queryForm = reactive({ accountName: '', bankId: '', entityId: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, accountNo: '', accountName: '', account: '', bankId: '', entityId: '',
  currencyCode: '', accountType: '', accountNature: '', isCollected: '0',
  collectDirection: '', mainAccountId: null, dayLimit: 0, nightLimit: 0, status: '1'
})

const rules = {
  accountNo: [{ required: true, message: '请输入账户编号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  bankId: [{ required: true, message: '请选择开户银行', trigger: 'change' }],
  entityId: [{ required: true, message: '请选择业务单元', trigger: 'change' }],
  currencyCode: [{ required: true, message: '请选择币种', trigger: 'change' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  accountNature: [{ required: true, message: '请选择账户性质', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑账户' : '新增账户'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => {
  const map = { BASIC: '基本户', GENERAL: '一般户', SPECIAL: '专户', TEMPORARY: '临时户', SETTLEMENT: '结算户', SAVING: '储蓄户', MARGIN: '保证金户' }
  return map[type] || type
}

const formatAmount = (amount) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const fetchLists = async () => {
  bankList.value = (await listBank({ pageSize: 1000 })).data.list || []
  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []
  currencyList.value = (await listCurrency({ pageSize: 1000 })).data.list || []
  mainAccountList.value = (await listBankAccount({ pageSize: 1000, isCollected: '1' })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listBankAccount(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { accountName: '', bankId: '', entityId: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, accountNo: '', accountName: '', account: '', bankId: '', entityId: '', currencyCode: '', accountType: '', accountNature: '', isCollected: '0', collectDirection: '', mainAccountId: null, dayLimit: 0, nightLimit: 0, status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleView = async (row) => {
  detailData.value = (await getAccountBalance(row.id)).data
  detailVisible.value = true
}

const handleSync = async (row) => {
  try {
    await syncBankAccount(row.id)
    ElMessage.success('同步成功')
    fetchData()
  } catch (e) { console.error(e) }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该账户吗?', '提示', { type: 'warning' })
    await deleteBankAccount(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        formData.id ? await updateBankAccount(formData) : await saveBankAccount(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchLists(); fetchData() })
</script>

<style scoped>
.bank-account-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>