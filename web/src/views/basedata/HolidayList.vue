<template>
  <div class="holiday-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="年份">
          <el-date-picker v-model="queryForm.year" type="year" placeholder="选择年份" value-format="YYYY" />
        </el-form-item>
        <el-form-item label="国家">
          <el-select v-model="queryForm.countryCode" placeholder="请选择" clearable>
            <el-option v-for="item in countryList" :key="item.code" :label="item.name" :value="item.code" />
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
        <el-table-column prop="holidayDate" label="日期" width="120" />
        <el-table-column prop="holidayName" label="节假日名称" min-width="150" />
        <el-table-column prop="countryCode" label="国家代码" width="120" />
        <el-table-column prop="countryName" label="国家名称" width="120" />
        <el-table-column prop="isAdjustment" label="是否调休" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isAdjustment === '1' ? 'warning' : 'info'">
              {{ row.isAdjustment === '1' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" />
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
        <el-form-item label="日期" prop="holidayDate">
          <el-date-picker v-model="formData.holidayDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="节假日名称" prop="holidayName">
          <el-input v-model="formData.holidayName" placeholder="如: 春节, 元旦" />
        </el-form-item>
        <el-form-item label="国家" prop="countryCode">
          <el-select v-model="formData.countryCode" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in countryList" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否调休" prop="isAdjustment">
          <el-radio-group v-model="formData.isAdjustment">
            <el-radio value="1">是</el-radio>
            <el-radio value="0">否</el-radio>
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
import { listHoliday, saveHoliday, updateHoliday, deleteHoliday, listCountry } from '@/api/basedata'

const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const countryList = ref([])

const currentYear = new Date().getFullYear().toString()
const queryForm = reactive({ year: currentYear, countryCode: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null,
  holidayDate: '',
  holidayName: '',
  countryCode: '',
  isAdjustment: '0',
  remark: ''
})

const rules = {
  holidayDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  holidayName: [{ required: true, message: '请输入节假日名称', trigger: 'blur' }],
  countryCode: [{ required: true, message: '请选择国家', trigger: 'change' }],
  isAdjustment: [{ required: true, message: '请选择是否调休', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑节假日' : '新增节假日'))
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
      year: queryForm.year,
      countryCode: queryForm.countryCode,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listHoliday(params)
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
  queryForm.year = currentYear
  queryForm.countryCode = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null, holidayDate: '', holidayName: '', countryCode: '', isAdjustment: '0', remark: ''
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
    await ElMessageBox.confirm('确定要删除该节假日吗?', '提示', { type: 'warning' })
    await deleteHoliday(row.id)
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
          await updateHoliday(formData)
          ElMessage.success('更新成功')
        } else {
          await saveHoliday(formData)
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
.holiday-list { }
.filter-card { margin-bottom: 16px; }
.table-card { }
</style>