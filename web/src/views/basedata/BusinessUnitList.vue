<template>
  <div class="management-entity-list">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span>基础数据</span>
      <span class="separator">/</span>
      <span>组织架构</span>
      <span class="separator">/</span>
      <span class="current">资金管理主体</span>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>资金管理主体</h1>
      <div class="header-actions">
        <el-button type="success" @click="handleAdd">+ 新增</el-button>
      </div>
    </div>

    <!-- 主内容区：左侧组织树 + 右侧列表 -->
    <div class="main-content">
      <!-- 左侧组织树 -->
      <div class="left-panel">
        <div class="tree-header">
          <el-input v-model="treeSearch" placeholder="搜索主体" prefix-icon="Search" clearable />
        </div>
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="code"
          :filter-node-method="filterTreeNode"
          :highlight-current="true"
          @node-click="handleTreeNodeClick"
          class="org-tree"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="node-label">{{ node.label }}</span>
              <span v-if="data.entityType" class="node-type-badge" :style="getTypeBadgeStyle(data.entityType)">
                {{ getTypeLabel(data.entityType) }}
              </span>
            </span>
          </template>
        </el-tree>
      </div>

      <!-- 右侧主体列表 -->
      <div class="right-panel">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-input
            v-model="queryForm.keyword"
            placeholder="编码/名称"
            prefix-icon="Search"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px;"
          />
          <el-select v-model="queryForm.entityType" placeholder="主体类型" clearable style="width: 140px;">
            <el-option label="总部" value="HEADQUARTER" />
            <el-option label="子公司" value="SUBSIDIARY" />
            <el-option label="分公司" value="BRANCH" />
            <el-option label="代表处" value="REPRESENTATIVE" />
          </el-select>
          <el-select v-model="queryForm.status" placeholder="状态" clearable style="width: 100px;">
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
          <el-button @click="handleReset">重置</el-button>
        </div>

        <!-- 数据表格 -->
        <div class="table-container">
          <el-table
            :data="tableData"
            v-loading="loading"
            stripe
            @row-hover="handleRowHover"
          >
            <el-table-column type="selection" width="40" align="center" />
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="name" label="名称" min-width="180" />
            <el-table-column prop="enName" label="英文名称" min-width="150" />
            <el-table-column prop="entityType" label="类型" width="100" align="center">
              <template #default="{ row }">
                <span class="entity-type-tag" :style="getTypeBadgeStyle(row.entityType)">
                  {{ getTypeLabel(row.entityType) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <span class="status-tag" :class="row.status === '1' ? 'status-active' : 'status-inactive'">
                  {{ row.status === '1' ? '启用' : '停用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="legalRepresentative" label="法人代表" width="100" />
            <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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
        </div>
      </div>
    </div>

    <!-- 新增/编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      direction="rtl"
      size="520px"
      :before-close="handleDrawerClose"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <!-- 基础信息 - 默认展开 -->
        <div class="collapse-group">
          <div class="collapse-header" @click="toggleSection('basic')">
            <span>基础信息</span>
            <el-icon><ArrowDown v-if="sections.basic" /><ArrowRight v-else /></el-icon>
          </div>
          <div v-show="sections.basic" class="collapse-content">
            <el-form-item label="主体编码" prop="code">
              <el-input v-model="formData.code" placeholder="唯一标识，支持字母数字" :disabled="isEdit" maxlength="50" show-word-limit />
            </el-form-item>
            <el-form-item label="主体名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入主体名称" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="英文名称" prop="enName">
              <el-input v-model="formData.enName" placeholder="English Name" maxlength="200" />
            </el-form-item>
            <el-form-item label="主体类型" prop="entityType">
              <el-radio-group v-model="formData.entityType">
                <el-radio value="HEADQUARTER">总部</el-radio>
                <el-radio value="SUBSIDIARY">子公司</el-radio>
                <el-radio value="BRANCH">分公司</el-radio>
                <el-radio value="REPRESENTATIVE">代表处</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="所属集团" prop="parentCode">
              <el-select v-model="formData.parentCode" placeholder="请选择所属集团" clearable filterable :disabled="isEdit" style="width: 100%;">
                <el-option v-for="item in parentOptions" :key="item.code" :label="item.name" :value="item.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="法人代表" prop="legalRepresentative">
              <el-input v-model="formData.legalRepresentative" placeholder="请输入法人代表" maxlength="50" />
            </el-form-item>
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input v-model="formData.unifiedSocialCreditCode" placeholder="18位统一社会信用代码" maxlength="18" />
            </el-form-item>
            <el-form-item label="注册地址" prop="registeredAddress">
              <el-input v-model="formData.registeredAddress" type="textarea" placeholder="请输入注册地址" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="成立日期" prop="establishmentDate">
              <el-date-picker v-model="formData.establishmentDate" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio value="1">启用</el-radio>
                <el-radio value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>

        <!-- 监管信息 - 默认折叠 -->
        <div class="collapse-group">
          <div class="collapse-header" @click="toggleSection('regulatory')">
            <span>监管信息</span>
            <el-icon><ArrowDown v-if="sections.regulatory" /><ArrowRight v-else /></el-icon>
          </div>
          <div v-show="sections.regulatory" class="collapse-content">
            <el-form-item label="监管机构" prop="regulatoryAuthority">
              <el-input v-model="formData.regulatoryAuthority" placeholder="请输入监管机构" maxlength="100" />
            </el-form-item>
            <el-form-item label="金融许可证号" prop="financialLicenseNo">
              <el-input v-model="formData.financialLicenseNo" placeholder="请输入金融许可证号" maxlength="100" />
            </el-form-item>
            <el-form-item label="注册资本" prop="registeredCapital">
              <div class="input-with-currency">
                <el-input v-model="formData.registeredCapital" placeholder="请输入注册资本" type="number" />
                <el-select v-model="formData.capitalCurrency" placeholder="币种" style="width: 100px;">
                  <el-option v-for="item in currencyOptions" :key="item.code" :label="item.code" :value="item.code" />
                </el-select>
              </div>
            </el-form-item>
            <el-form-item label="LCR要求(%)" prop="lcrRequirement">
              <el-input v-model="formData.lcrRequirement" placeholder="0-100" type="number" />
            </el-form-item>
            <el-form-item label="NSFR要求(%)" prop="nsfrRequirement">
              <el-input v-model="formData.nsfrRequirement" placeholder="0-100" type="number" />
            </el-form-item>
          </div>
        </div>

        <!-- 会计税务 - 默认折叠 -->
        <div class="collapse-group">
          <div class="collapse-header" @click="toggleSection('accounting')">
            <span>会计税务</span>
            <el-icon><ArrowDown v-if="sections.accounting" /><ArrowRight v-else /></el-icon>
          </div>
          <div v-show="sections.accounting" class="collapse-content">
            <el-form-item label="会计准则" prop="accountingStandard">
              <el-select v-model="formData.accountingStandard" placeholder="请选择会计准则" style="width: 100%;">
                <el-option label="IFRS" value="IFRS" />
                <el-option label="USGAAP" value="USGAAP" />
                <el-option label="CNGBA" value="CNGBA" />
              </el-select>
            </el-form-item>
            <el-form-item label="报表本位币" prop="reportingCurrency">
              <el-select v-model="formData.reportingCurrency" placeholder="请选择币种" style="width: 100%;">
                <el-option v-for="item in currencyOptions" :key="item.code" :label="item.code" :value="item.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="增值税一般纳税人" prop="vatGeneralTaxpayer">
              <el-radio-group v-model="formData.vatGeneralTaxpayer">
                <el-radio :value="true">是</el-radio>
                <el-radio :value="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="handleDrawerClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="主体详情" direction="rtl" size="600px">
      <div class="entity-detail" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="主体编码">{{ detailData.code }}</el-descriptions-item>
          <el-descriptions-item label="主体名称">{{ detailData.name }}</el-descriptions-item>
          <el-descriptions-item label="英文名称">{{ detailData.enName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主体类型">
            <span class="entity-type-tag" :style="getTypeBadgeStyle(detailData.entityType)">
              {{ getTypeLabel(detailData.entityType) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="法人代表">{{ detailData.legalRepresentative || '-' }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ detailData.unifiedSocialCreditCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册地址" :span="2">{{ detailData.registeredAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="成立日期">{{ detailData.establishmentDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="detailData.status === '1' ? 'status-active' : 'status-inactive'">
              {{ detailData.status === '1' ? '启用' : '停用' }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section-title">监管信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="监管机构">{{ detailData.regulatoryAuthority || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金融许可证号">{{ detailData.financialLicenseNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册资本">{{ detailData.registeredCapital ? detailData.registeredCapital + ' ' + (detailData.capitalCurrency || 'CNY') : '-' }}</el-descriptions-item>
          <el-descriptions-item label="LCR要求">{{ detailData.lcrRequirement ? detailData.lcrRequirement + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="NSFR要求">{{ detailData.nsfrRequirement ? detailData.nsfrRequirement + '%' : '-' }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section-title">会计税务</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会计准则">{{ detailData.accountingStandard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报表本位币">{{ detailData.reportingCurrency || '-' }}</el-descriptions-item>
          <el-descriptions-item label="增值税一般纳税人">{{ detailData.vatGeneralTaxpayer ? '是' : '否' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import {
  listManagementEntity,
  getManagementEntity,
  getManagementEntityTree,
  saveManagementEntity,
  updateManagementEntity,
  deleteManagementEntity
} from '@/api/basedata/businessUnit'

const loading = ref(false)
const drawerVisible = ref(false)
const detailDrawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const tableData = ref([])
const treeData = ref([])
const treeSearch = ref('')
const parentOptions = ref([])
const currencyOptions = ref([
  { code: 'CNY' },
  { code: 'USD' },
  { code: 'EUR' },
  { code: 'HKD' },
  { code: 'JPY' },
  { code: 'GBP' }
])
const detailData = ref(null)

const queryForm = reactive({
  keyword: '',
  entityType: '',
  status: ''
})

const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null,
  code: '',
  name: '',
  enName: '',
  entityType: 'HEADQUARTER',
  parentCode: '',
  legalRepresentative: '',
  unifiedSocialCreditCode: '',
  registeredAddress: '',
  officeAddress: '',
  establishmentDate: '',
  status: '1',
  regulatoryAuthority: '',
  financialLicenseNo: '',
  registeredCapital: null,
  capitalCurrency: 'CNY',
  lcrRequirement: null,
  nsfrRequirement: null,
  accountingStandard: '',
  reportingCurrency: '',
  vatGeneralTaxpayer: false
})

const sections = reactive({
  basic: true,
  regulatory: false,
  accounting: false
})

const rules = {
  code: [{ required: true, message: '请输入主体编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
  entityType: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const drawerTitle = computed(() => (formData.id ? '编辑资金管理主体' : '新增资金管理主体'))
const isEdit = computed(() => !!formData.id)

// 主体类型映射
const entityTypeMap = {
  HEADQUARTER: { label: '总部', bgColor: '#DBEAFE', color: '#1D4ED8' },
  SUBSIDIARY: { label: '子公司', bgColor: '#D1FAE5', color: '#047857' },
  BRANCH: { label: '分公司', bgColor: '#FEF3C7', color: '#B45309' },
  REPRESENTATIVE: { label: '代表处', bgColor: '#E2E8F0', color: '#64748B' }
}

const getTypeLabel = (type) => entityTypeMap[type]?.label || type
const getTypeBadgeStyle = (type) => {
  const style = entityTypeMap[type]
  if (!style) return {}
  return {
    backgroundColor: style.bgColor,
    color: style.color,
    borderRadius: '9999px',
    padding: '2px 8px',
    fontSize: '12px'
  }
}

// 监听树搜索
watch(treeSearch, (val) => {
  treeRef.value?.filter(val)
})

const filterTreeNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value) || data.code.includes(value)
}

const fetchTree = async () => {
  try {
    const res = await getManagementEntityTree()
    treeData.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch tree:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword,
      entityType: queryForm.entityType,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await listManagementEntity(params)
    tableData.value = res.data.records || res.data.list || []
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
  queryForm.entityType = ''
  queryForm.status = ''
  handleQuery()
}

const handleTreeNodeClick = (data) => {
  queryForm.parentCode = data.code
  handleQuery()
}

const handleRowHover = (row) => {
  // 可以在这里做行高亮处理
}

const toggleSection = (section) => {
  sections[section] = !sections[section]
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    code: '',
    name: '',
    enName: '',
    entityType: 'HEADQUARTER',
    parentCode: '',
    legalRepresentative: '',
    unifiedSocialCreditCode: '',
    registeredAddress: '',
    officeAddress: '',
    establishmentDate: '',
    status: '1',
    regulatoryAuthority: '',
    financialLicenseNo: '',
    registeredCapital: null,
    capitalCurrency: 'CNY',
    lcrRequirement: null,
    nsfrRequirement: null,
    accountingStandard: '',
    reportingCurrency: '',
    vatGeneralTaxpayer: false
  })
  sections.basic = true
  sections.regulatory = false
  sections.accounting = false
  formRef.value?.resetFields()
  drawerVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  sections.basic = true
  sections.regulatory = false
  sections.accounting = false
  drawerVisible.value = true
}

const handleDetail = async (row) => {
  try {
    const res = await getManagementEntity(row.id)
    detailData.value = res.data
    detailDrawerVisible.value = true
  } catch (error) {
    console.error('Failed to fetch detail:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除【${row.name}】吗？删除后不可恢复。`,
      '提示',
      { type: 'warning' }
    )
    await deleteManagementEntity(row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
    }
  }
}

const handleDrawerClose = (done) => {
  if (formRef.value) {
    formRef.value.validate((valid) => {
      if (!valid) return
      ElMessageBox.confirm('确定要关闭吗？未保存的数据将丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => done()).catch(() => {})
    })
  } else {
    done()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateManagementEntity(formData)
          ElMessage.success('更新成功')
        } else {
          await saveManagementEntity(formData)
          ElMessage.success('新增成功')
        }
        drawerVisible.value = false
        fetchData()
        fetchTree()
      } catch (error) {
        console.error('Submit failed:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchTree()
  fetchData()
})
</script>

<style scoped>
.management-entity-list {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  font-size: 14px;
  color: #64748B;
  margin-bottom: 16px;
}

.breadcrumb .separator {
  margin: 0 8px;
}

.breadcrumb .current {
  color: #1E293B;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.main-content {
  display: flex;
  gap: 16px;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  width: 240px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
}

.tree-header {
  margin-bottom: 12px;
}

.org-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-label {
  flex: 1;
}

.node-type-badge {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 4px;
}

.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.table-container {
  flex: 1;
}

.entity-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 12px;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-active {
  background: #D1FAE5;
  color: #047857;
}

.status-inactive {
  background: #E2E8F0;
  color: #64748B;
}

/* 抽屉折叠组样式 */
.collapse-group {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
}

.collapse-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #F8FAFC;
  cursor: pointer;
  font-weight: 500;
  color: #1E293B;
}

.collapse-header:hover {
  background: #F1F5F9;
}

.collapse-content {
  padding: 16px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #E2E8F0;
}

/* 详情页样式 */
.entity-detail {
  padding: 0 16px;
}

.detail-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 24px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #E2E8F0;
}

.input-with-currency {
  display: flex;
  gap: 8px;
}

.input-with-currency .el-input {
  flex: 1;
}
</style>