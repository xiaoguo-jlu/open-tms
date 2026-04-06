<template>
  <div class="currency-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="币种代码/名称" clearable @keyup.enter="handleQuery" />
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
        <el-table-column prop="currencyCode" label="币种代码" width="120" />
        <el-table-column prop="currencyName" label="币种名称" width="150" />
        <el-table-column prop="currencySymbol" label="符号" width="80" />
        <el-table-column prop="decimalPlaces" label="小数位数" width="100" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="币种代码" prop="currencyCode">
          <el-input v-model="formData.currencyCode" placeholder="如: CNY, USD" />
        </el-form-item>
        <el-form-item label="币种名称" prop="currencyName">
          <el-input v-model="formData.currencyName" placeholder="如: 人民币, 美元" />
        </el-form-item>
        <el-form-item label="币种符号" prop="currencySymbol">
          <el-input v-model="formData.currencySymbol" placeholder="如: ¥, $" />
        </el-form-item>
        <el-form-item label="小数位数" prop="decimalPlaces">
          <el-input-number v-model="formData.decimalPlaces" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_URL = '/api/currency'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const queryForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const tableData = ref([])

const formData = reactive({
  id: null,
  currencyCode: '',
  currencyName: '',
  currencySymbol: '',
  decimalPlaces: 2,
  status: '1'
})

const rules = {
  currencyCode: [{ required: true, message: '请输入币种代码', trigger: 'blur' }],
  currencyName: [{ required: true, message: '请输入币种名称', trigger: 'blur' }],
  decimalPlaces: [{ required: true, message: '请输入小数位数', trigger: 'blur' }]
}

const dialogTitle = ref('')

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await axios.get(`${API_URL}/page`, { params })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      pagination.total = res.data.data.total
    }
  } catch (error) {
    ElMessage.error('加载失败')
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
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增币种'
  Object.assign(formData, { id: null, currencyCode: '', currencyName: '', currencySymbol: '', decimalPlaces: 2, status: '1' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑币种'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该币种吗?', '提示', { type: 'warning' })
    await axios.delete(`${API_URL}/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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
          await axios.put(API_URL, formData)
          ElMessage.success('更新成功')
        } else {
          await axios.post(API_URL, formData)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.currency-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>