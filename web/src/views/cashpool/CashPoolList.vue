<template>
  <div class="cash-pool-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="池名称">
          <el-input v-model="queryForm.poolName" placeholder="请输入池名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="池类型">
          <el-select v-model="queryForm.poolType" placeholder="请选择" clearable>
            <el-option label="物理池" value="PHYSICAL" />
            <el-option label="虚拟池" value="VIRTUAL" />
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
        <el-table-column prop="poolNo" label="池编号" width="120" />
        <el-table-column prop="poolName" label="池名称" min-width="180" />
        <el-table-column prop="poolType" label="池类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.poolType === 'PHYSICAL' ? 'primary' : 'success'">
              {{ row.poolType === 'PHYSICAL' ? '物理池' : '虚拟池' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentPoolName" label="上级池" width="120" />
        <el-table-column prop="entityName" label="所属业务单元" width="140" />
        <el-table-column prop="memberCount" label="成员数量" width="100" align="center" />
        <el-table-column prop="totalBalance" label="总余额" align="right" width="150">
          <template #default="{ row }">
            {{ formatAmount(row.totalBalance) }}
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
            <el-button type="primary" link @click="handleViewMember(row)">成员</el-button>
            <el-button type="warning" link @click="handleViewRule(row)">规则</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" direction="rtl" size="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="池编号" prop="poolNo">
          <el-input v-model="formData.poolNo" placeholder="唯一编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="池名称" prop="poolName">
          <el-input v-model="formData.poolName" placeholder="请输入池名称" />
        </el-form-item>
        <el-form-item label="池类型" prop="poolType">
          <el-radio-group v-model="formData.poolType">
            <el-radio value="PHYSICAL">物理池</el-radio>
            <el-radio value="VIRTUAL">虚拟池</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级池" prop="parentPoolId">
          <el-select v-model="formData.parentPoolId" placeholder="请选择" style="width: 100%;" clearable>
            <el-option v-for="item in poolList" :key="item.id" :label="item.poolName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属业务单元" prop="entityId">
          <el-select v-model="formData.entityId" placeholder="请选择" style="width: 100%;" filterable>
            <el-option v-for="item in entityList" :key="item.id" :label="item.name" :value="item.id" />
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

    <el-dialog v-model="memberVisible" title="池成员管理" width="800px">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAddMember">添加成员</el-button>
      </div>
      <el-table :data="memberList" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="accountNo" label="账户编号" width="120" />
        <el-table-column prop="accountName" label="账户名称" min-width="150" />
        <el-table-column prop="bankName" label="开户银行" min-width="150" />
        <el-table-column prop="balance" label="余额" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.balance) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemoveMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCashPool, saveCashPool, updateCashPool, deleteCashPool, listCashPoolMember } from '@/api/cashpool'
import { listBusinessUnit } from '@/api/basedata'

const router = useRouter()
const loading = ref(false)
const drawerVisible = ref(false)
const memberVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const poolList = ref([])
const entityList = ref([])
const memberList = ref([])
const currentPoolId = ref(null)

const queryForm = reactive({ poolName: '', poolType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, poolNo: '', poolName: '', poolType: 'PHYSICAL', parentPoolId: null, entityId: null, status: '1'
})

const rules = {
  poolNo: [{ required: true, message: '请输入池编号', trigger: 'blur' }],
  poolName: [{ required: true, message: '请输入池名称', trigger: 'blur' }],
  poolType: [{ required: true, message: '请选择池类型', trigger: 'change' }],
  entityId: [{ required: true, message: '请选择业务单元', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑现金池' : '新增现金池'))
const isEdit = computed(() => !!formData.id)

const formatAmount = (amount) => {
  if (!amount) return '-'
  return '¥' + new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}

const fetchEntityList = async () => {
  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []
}

const fetchPoolList = async () => {
  poolList.value = (await listCashPool({ pageSize: 1000 })).data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listCashPool(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { poolName: '', poolType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, poolNo: '', poolName: '', poolType: 'PHYSICAL', parentPoolId: null, entityId: null, status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该现金池吗?', '提示', { type: 'warning' })
    await deleteCashPool(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleViewMember = async (row) => {
  currentPoolId.value = row.id
  const res = await listCashPoolMember(row.id)
  memberList.value = res.data || []
  memberVisible.value = true
}

const handleViewRule = (row) => {
  router.push({ path: '/cashpool/auto-rule', query: { poolId: row.id, poolName: row.poolName } })
}

const handleAddMember = () => { ElMessage.info('添加成员功能') }
const handleRemoveMember = (row) => { ElMessage.info('移除成员功能') }

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        formData.id ? await updateCashPool(formData) : await saveCashPool(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchEntityList(); fetchPoolList(); fetchData() })
</script>

<style scoped>
.cash-pool-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>