<template>
  <div class="instrument-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="编码/名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="金融工具类型">
          <el-select v-model="queryForm.instrumentType" placeholder="请选择" clearable>
            <el-option v-for="item in typeList" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column prop="instrumentCode" label="工具编码" width="140" />
        <el-table-column prop="instrumentName" label="工具名称" min-width="180" />
        <el-table-column prop="enName" label="英文名称" min-width="180" />
        <el-table-column prop="instrumentType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.instrumentType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="underlying" label="标的资产" width="120" />
        <el-table-column prop="exchange" label="交易所" width="120" />
        <el-table-column prop="currency" label="币种" width="80" />
        <el-table-column prop="remark" label="描述" min-width="200" />
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
        <el-form-item label="工具编码" prop="instrumentCode">
          <el-input v-model="formData.instrumentCode" placeholder="唯一编码" :disabled="isEdit" maxlength="50" />
        </el-form-item>
        <el-form-item label="工具名称" prop="instrumentName">
          <el-input v-model="formData.instrumentName" placeholder="请输入工具名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="英文名称" prop="enName">
          <el-input v-model="formData.enName" placeholder="请输入英文名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="工具类型" prop="instrumentType">
          <el-select v-model="formData.instrumentType" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in typeList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标的资产" prop="underlying">
          <el-input v-model="formData.underlying" placeholder="请输入标的资产" maxlength="50" />
        </el-form-item>
        <el-form-item label="交易所" prop="exchange">
          <el-input v-model="formData.exchange" placeholder="请输入交易所" maxlength="50" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="formData.currency" placeholder="请输入币种" maxlength="10" />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { listInstrument, saveInstrument, updateInstrument, deleteInstrument } from '@/api/dealing'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])

const typeList = [
  { label: '股票', value: 'STOCK' },
  { label: '债券', value: 'BOND' },
  { label: '期权', value: 'OPTION' },
  { label: '期货', value: 'FUTURE' },
  { label: '互换', value: 'SWAP' },
  { label: '远期', value: 'FORWARD' },
  { label: '即期', value: 'SPOT' },
  { label: '结构性产品', value: 'STRUCTURED' },
  { label: 'ETF', value: 'ETF' },
  { label: '其他', value: 'OTHER' }
]

const queryForm = reactive({ keyword: '', instrumentType: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, instrumentCode: '', instrumentName: '', enName: '', instrumentType: '',
  underlying: '', exchange: '', currency: '', remark: '', status: '1'
})

const rules = {
  instrumentCode: [{ required: true, message: '请输入工具编码', trigger: 'blur' }, { maxlength: 50, message: '编码长度不能超过50', trigger: 'blur' }],
  instrumentName: [{ required: true, message: '请输入工具名称', trigger: 'blur' }, { maxlength: 200, message: '名称长度不能超过200', trigger: 'blur' }],
  instrumentType: [{ required: true, message: '请选择工具类型', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑金融工具' : '新增金融工具'))
const isEdit = computed(() => !!formData.id)

const getTypeLabel = (type) => typeList.find(t => t.value === type)?.label || type

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const res = await listInstrument(params)
    tableData.value = res.data.records || res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleQuery = () => { pagination.pageNum = 1; fetchData() }

const handleReset = () => {
  Object.assign(queryForm, { keyword: '', instrumentType: '', status: '' })
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, { id: null, instrumentCode: '', instrumentName: '', enName: '',
    instrumentType: '', underlying: '', exchange: '', currency: '', remark: '', status: '1' })
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  drawerVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该金融工具吗?', '提示', { type: 'warning' })
    await deleteInstrument(row.id)
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
        formData.id ? await updateInstrument(formData) : await saveInstrument(formData)
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        drawerVisible.value = false
        fetchData()
      } catch (e) { console.error(e) }
      finally { submitLoading.value = false }
    }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.instrument-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>