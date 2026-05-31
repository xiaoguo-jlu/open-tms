<template>
  <div class="deal-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑交易' : '新建交易' }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button @click="handleSave" :loading="saving">保存</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="form-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易类型" prop="dealType">
              <el-select v-model="form.dealType" placeholder="请选择交易类型">
                <el-option label="AC交易" value="AC" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务单元" prop="businessUnitId">
              <el-input v-model="form.businessUnitName" placeholder="点击选择业务单元" readonly @click="showBusinessUnitSelector = true">
                <template #suffix>
                  <el-icon @click="showBusinessUnitSelector = true"><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易对手" prop="counterpartyId">
              <el-input v-model="form.counterpartyName" placeholder="点击选择交易对手" readonly @click="showCounterpartySelector = true">
                <template #suffix>
                  <el-icon @click="showCounterpartySelector = true"><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金融工具" prop="instrumentId">
              <el-input v-model="form.instrumentName" placeholder="点击选择金融工具" readonly @click="showInstrumentSelector = true">
                <template #suffix>
                  <el-icon @click="showInstrumentSelector = true"><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易员" prop="traderId">
              <el-input v-model="form.traderName" placeholder="点击选择交易员" readonly @click="showTraderSelector = true">
                <template #suffix>
                  <el-icon @click="showTraderSelector = true"><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="方向" prop="direction">
              <el-select v-model="form.direction" placeholder="请选择方向">
                <el-option label="流入" value="Inflow" />
                <el-option label="流出" value="Outflow" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-select v-model="form.currency" placeholder="请选择币种" filterable>
                <el-option v-for="item in currencyList" :key="item.currencyCode" :label="`${item.currencyName} (${item.currencyCode})`" :value="item.currencyCode" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易日期" prop="dealDate">
              <el-date-picker v-model="form.dealDate" type="date" placeholder="选择交易日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起息日" prop="valueDate">
              <el-date-picker v-model="form.valueDate" type="date" placeholder="选择起息日" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="本方账户" prop="bankAccountId">
              <el-select v-model="form.bankAccountId" placeholder="请选择本方账户" filterable @change="handleBankAccountChange">
                <el-option v-for="item in bankAccountList" :key="item.id" :label="`${item.bankName} - ${item.accountName}`" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对手方账户" prop="counterpartyAccountId">
              <el-select v-model="form.counterpartyAccountId" placeholder="请选择对手方账户" filterable :disabled="!form.counterpartyId">
                <el-option v-for="item in counterpartyAccountList" :key="item.id" :label="item.accountName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="付款方式" prop="paymentMethod">
              <el-select v-model="form.paymentMethod" placeholder="请选择付款方式">
                <el-option label="转账" value="Transfer" />
                <el-option label="票据" value="Bill" />
                <el-option label="其他" value="Other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入交易描述" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 业务单元选择弹窗 -->
    <el-dialog v-model="showBusinessUnitSelector" title="选择业务单元" width="600px">
      <el-form :inline="true" :model="businessUnitQuery" class="selector-search">
        <el-form-item>
          <el-input v-model="businessUnitQuery.keyword" placeholder="搜索编码/名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBusinessUnit">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="businessUnitList" @row-click="selectBusinessUnit" highlight-current-row stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
      </el-table>
      <el-pagination
        v-model:current-page="businessUnitPagination.pageNum"
        v-model:page-size="businessUnitPagination.pageSize"
        :total="businessUnitPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="searchBusinessUnit"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-dialog>

    <!-- 交易对手选择弹窗 -->
    <el-dialog v-model="showCounterpartySelector" title="选择交易对手" width="600px">
      <el-form :inline="true" :model="counterpartyQuery" class="selector-search">
        <el-form-item>
          <el-input v-model="counterpartyQuery.keyword" placeholder="搜索编码/名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCounterparty">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="counterpartyList" @row-click="selectCounterparty" highlight-current-row stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="counterpartyType" label="类型" />
      </el-table>
      <el-pagination
        v-model:current-page="counterpartyPagination.pageNum"
        v-model:page-size="counterpartyPagination.pageSize"
        :total="counterpartyPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="searchCounterparty"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-dialog>

    <!-- 金融工具选择弹窗 -->
    <el-dialog v-model="showInstrumentSelector" title="选择金融工具" width="600px">
      <el-form :inline="true" :model="instrumentQuery" class="selector-search">
        <el-form-item>
          <el-input v-model="instrumentQuery.keyword" placeholder="搜索编码/名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchInstrument">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="instrumentList" @row-click="selectInstrument" highlight-current-row stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="instrumentType" label="类型" />
      </el-table>
      <el-pagination
        v-model:current-page="instrumentPagination.pageNum"
        v-model:page-size="instrumentPagination.pageSize"
        :total="instrumentPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="searchInstrument"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-dialog>

    <!-- 交易员选择弹窗 -->
    <el-dialog v-model="showTraderSelector" title="选择交易员" width="600px">
      <el-form :inline="true" :model="traderQuery" class="selector-search">
        <el-form-item>
          <el-input v-model="traderQuery.keyword" placeholder="搜索编号/姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchTrader">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="traderList" @row-click="selectTrader" highlight-current-row stripe>
        <el-table-column prop="code" label="编号" width="120" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="department" label="部门" />
      </el-table>
      <el-pagination
        v-model:current-page="traderPagination.pageNum"
        v-model:page-size="traderPagination.pageSize"
        :total="traderPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="searchTrader"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createDeal, updateDeal, getDeal } from '@/api/dealing'
import { listBusinessUnit, listCounterparty, listCurrency, listBankAccount } from '@/api/basedata'
import { listInstrument } from '@/api/dealing'
import { listCounterpartyAccount } from '@/api/basedata'
import { listTrader } from '@/api/basedata'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.query.id)

const form = reactive({
  id: null,
  dealType: 'AC',
  businessUnitId: null,
  businessUnitName: '',
  counterpartyId: null,
  counterpartyName: '',
  instrumentId: null,
  instrumentName: '',
  traderId: null,
  traderName: '',
  direction: 'Inflow',
  amount: 0,
  currency: '',
  dealDate: '',
  valueDate: '',
  description: '',
  remark: '',
  bankAccountId: null,
  counterpartyAccountId: null,
  paymentMethod: 'Transfer'
})

const rules = {
  dealType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  businessUnitId: [{ required: true, message: '请选择业务单元', trigger: 'change' }],
  counterpartyId: [{ required: true, message: '请选择交易对手', trigger: 'change' }],
  instrumentId: [{ required: true, message: '请选择金融工具', trigger: 'change' }],
  traderId: [{ required: true, message: '请选择交易员', trigger: 'change' }],
  direction: [{ required: true, message: '请选择方向', trigger: 'change' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  dealDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }],
  valueDate: [{ required: true, message: '请选择起息日', trigger: 'change' }]
}

// 选择器显示状态
const showBusinessUnitSelector = ref(false)
const showCounterpartySelector = ref(false)
const showInstrumentSelector = ref(false)
const showTraderSelector = ref(false)

// 选择器数据
const businessUnitList = ref([])
const counterpartyList = ref([])
const instrumentList = ref([])
const traderList = ref([])
const bankAccountList = ref([])
const counterpartyAccountList = ref([])
const currencyList = ref([])

// 选择器查询条件
const businessUnitQuery = reactive({ keyword: '' })
const counterpartyQuery = reactive({ keyword: '' })
const instrumentQuery = reactive({ keyword: '' })
const traderQuery = reactive({ keyword: '' })

// 选择器分页
const businessUnitPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const counterpartyPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const instrumentPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const traderPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const searchBusinessUnit = async () => {
  try {
    const res = await listBusinessUnit({
      keyword: businessUnitQuery.keyword,
      pageNum: businessUnitPagination.pageNum,
      pageSize: businessUnitPagination.pageSize
    })
    businessUnitList.value = res.data.records || res.data.list || []
    businessUnitPagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
}

const searchCounterparty = async () => {
  try {
    const res = await listCounterparty({
      keyword: counterpartyQuery.keyword,
      pageNum: counterpartyPagination.pageNum,
      pageSize: counterpartyPagination.pageSize
    })
    counterpartyList.value = res.data.records || res.data.list || []
    counterpartyPagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
}

const searchInstrument = async () => {
  try {
    const res = await listInstrument({
      keyword: instrumentQuery.keyword,
      pageNum: instrumentPagination.pageNum,
      pageSize: instrumentPagination.pageSize
    })
    instrumentList.value = res.data.records || res.data.list || []
    instrumentPagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
}

const searchTrader = async () => {
  try {
    const res = await listTrader({
      keyword: traderQuery.keyword,
      pageNum: traderPagination.pageNum,
      pageSize: traderPagination.pageSize
    })
    traderList.value = res.data.records || res.data.list || []
    traderPagination.total = res.data.total || 0
  } catch (e) { console.error(e) }
}

const selectBusinessUnit = (row) => {
  form.businessUnitId = row.id
  form.businessUnitName = row.name
  showBusinessUnitSelector.value = false
}

const selectCounterparty = async (row) => {
  form.counterpartyId = row.id
  form.counterpartyName = row.name
  showCounterpartySelector.value = false
  // 加载对手方账户
  const res = await listCounterpartyAccount({ counterpartyId: row.id, pageSize: 100 })
  counterpartyAccountList.value = res.data.records || res.data.list || []
}

const selectInstrument = (row) => {
  form.instrumentId = row.id
  form.instrumentName = row.name
  showInstrumentSelector.value = false
}

const selectTrader = (row) => {
  form.traderId = row.id
  form.traderName = row.name
  showTraderSelector.value = false
}

const handleBankAccountChange = (val) => {
  const account = bankAccountList.value.find(item => item.id === val)
  if (account) {
    form.currency = account.currencyCode
  }
}

const fetchBasicData = async () => {
  try {
    // 加载币种列表
    const currencyRes = await listCurrency({ pageSize: 1000 })
    currencyList.value = currencyRes.data.records || currencyRes.data.list || []
    // 加载本方账户
    const bankAccountRes = await listBankAccount({ pageSize: 1000 })
    bankAccountList.value = bankAccountRes.data.records || bankAccountRes.data.list || []
  } catch (e) { console.error(e) }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        if (isEdit.value) {
          await updateDeal(form)
          ElMessage.success('更新成功')
        } else {
          await createDeal(form)
          ElMessage.success('保存成功')
        }
        handleBack()
      } catch (e) { console.error(e) }
      finally { saving.value = false }
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateDeal(form)
          ElMessage.success('提交成功')
        } else {
          await createDeal(form)
          ElMessage.success('提交成功')
        }
        handleBack()
      } catch (e) { console.error(e) }
      finally { submitting.value = false }
    }
  })
}

const handleBack = () => { router.push('/dealing/deal') }

onMounted(async () => {
  await fetchBasicData()
  if (isEdit.value) {
    const res = await getDeal(route.query.id)
    Object.assign(form, res.data)
  }
})
</script>

<style scoped>
.deal-form { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.form-container { padding: 20px 0; }
.selector-search { margin-bottom: 16px; }
</style>