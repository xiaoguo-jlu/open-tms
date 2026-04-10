<template>
  <div class="bank-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="银行代码/名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="国家">
          <el-select v-model="queryForm.countryCode" placeholder="请选择" clearable>
            <el-option v-for="item in countryList" :key="item.code" :label="item.name" :value="item.code" />
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
        <el-table-column prop="code" label="银行代码" width="120" />
        <el-table-column prop="name" label="银行名称" min-width="180" />
        <el-table-column prop="enName" label="英文名称" min-width="180" />
        <el-table-column prop="swiftCode" label="SWIFT Code" width="120" />
        <el-table-column prop="bankCode" label="行号" width="120" />
        <el-table-column prop="countryName" label="所属国家" width="100" />
        <el-table-column prop="bankType" label="银行类型" width="100" />
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
        <el-form-item label="银行代码" prop="code">
          <el-input v-model="formData.code" placeholder="内部编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="银行名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="英文名称" prop="enName">
          <el-input v-model="formData.enName" placeholder="English Name" />
        </el-form-item>
        <el-form-item label="SWIFT Code" prop="swiftCode">
          <el-input v-model="formData.swiftCode" placeholder="SWIFT代码" />
        </el-form-item>
        <el-form-item label="行号" prop="bankCode">
          <el-input v-model="formData.bankCode" placeholder="人行行号" />
        </el-form-item>
        <el-form-item label="所属国家" prop="countryCode">
          <el-select v-model="formData.countryCode" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in countryList" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="银行类型" prop="bankType">
          <el-select v-model="formData.bankType" placeholder="请选择" style="width: 100%;">
            <el-option label="央行" value="CENTRAL" />
            <el-option label="商行" value="COMMERCIAL" />
            <el-option label="外资" value="FOREIGN" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBank, saveBank, updateBank, deleteBank, listCountry } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const countryList = ref([])

const queryForm = reactive({ keyword: '', countryCode: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null,
  code: '',
  name: '',
  enName: '',
  swiftCode: '',
  bankCode: '',
  countryCode: '',
  bankType: '',
  status: '1'
})

const rules = {
  code: [{ required: true, message: '请输入银行代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入银行名称', trigger: 'blur' }],
  countryCode: [{ required: true, message: '请选择所属国家', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑银行' : '新增银行'))
const isEdit = computed(() => !!formData.id)

const fetchCountryList = async () => {
  try {
    const res = await listCountry({ pageSize: 1000 })
    countryList.value = res.data.list || []
  } catch (error) {
    console.error('Failed to fetch countries:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword,
      countryCode: queryForm.countryCode,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listBank(params)
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
  queryForm.countryCode = ''
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null, code: '', name: '', enName: '', swiftCode: '', bankCode: '', countryCode: '', bankType: '', status: '1'
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
    await ElMessageBox.confirm('确定要删除该银行吗?', '提示', { type: 'warning' })
    await deleteBank(row.id)
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
          await updateBank(formData)
          ElMessage.success('更新成功')
        } else {
          await saveBank(formData)
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
  fetchCountryList()
  fetchData()
})
</script>

<style scoped>
.bank-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>