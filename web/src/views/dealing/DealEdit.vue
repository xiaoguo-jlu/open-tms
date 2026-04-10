<template>
  <div class="deal-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑交易' : '新建交易' }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button @click="handleSaveDraft" :loading="saving">暂存</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          </div>
        </div>
      </template>

      <el-steps :active="stepActive" finish-status="success" align-center>
        <el-step title="基本信息" />
        <el-step title="交易要素" />
        <el-step title="确认提交" />
      </el-steps>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="form-container">
        <div v-show="stepActive === 0">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="交易类型" prop="dealType">
                <el-select v-model="form.dealType" placeholder="请选择交易类型" @change="handleDealTypeChange">
                  <el-option label="存款" value="DEPOSIT" />
                  <el-option label="贷款" value="LOAN" />
                  <el-option label="外汇" value="FX" />
                  <el-option label="同业" value="INTERBANK" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交易子类型" prop="dealSubtype">
                <el-select v-model="form.dealSubtype" placeholder="请选择">
                  <el-option v-for="item in subtypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="业务单元" prop="entityId">
                <el-input v-model="form.entityName" placeholder="点击选择业务单元" readonly @click="showEntitySelector = true">
                  <template #suffix>
                    <el-icon @click="showEntitySelector = true"><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交易员" prop="dealerId">
                <el-input v-model="form.dealerName" placeholder="点击选择交易员" readonly @click="showDealerSelector = true">
                  <template #suffix>
                    <el-icon @click="showDealerSelector = true"><Search /></el-icon>
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
              <el-form-item label="对手方账户" prop="counterpartyAccountId">
                <el-select v-model="form.counterpartyAccountId" placeholder="请选择对手方账户" filterable :disabled="!form.counterpartyId">
                  <el-option v-for="item in accountList" :key="item.id" :label="item.accountName" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="金融工具" prop="instrumentId">
                <el-input v-model="form.instrumentName" placeholder="点击选择金融工具" readonly @click="showInstrumentSelector = true">
                  <template #suffix>
                    <el-icon @click="showInstrumentSelector = true"><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结算币种" prop="currencyCode">
                <el-select v-model="form.currencyCode" placeholder="请选择币种" filterable>
                  <el-option v-for="item in currencyList" :key="item.currencyCode" :label="`${item.currencyName} (${item.currencyCode})`" :value="item.currencyCode" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="stepActive === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="结算金额" prop="amount">
                <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" style="width: 100%;" />
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
              <el-form-item label="到期日" prop="maturityDate">
                <el-date-picker v-model="form.maturityDate" type="date" placeholder="选择到期日" value-format="YYYY-MM-DD" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.dealType === 'DEPOSIT' || form.dealType === 'LOAN' || form.dealType === 'INTERBANK'">
              <el-form-item label="年利率" prop="interestRate">
                <el-input-number v-model="form.interestRate" :min="0" :max="100" :precision="4" :controls="false" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="form.dealType === 'FX'">
            <el-col :span="8">
              <el-form-item label="买入币种" prop="buyCurrency">
                <el-select v-model="form.buyCurrency" placeholder="请选择">
                  <el-option v-for="item in currencyList" :key="item.currencyCode" :label="item.currencyCode" :value="item.currencyCode" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="卖出币种" prop="sellCurrency">
                <el-select v-model="form.sellCurrency" placeholder="请选择">
                  <el-option v-for="item in currencyList" :key="item.currencyCode" :label="item.currencyCode" :value="item.currencyCode" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="交易汇率" prop="exchangeRate">
                <el-input-number v-model="form.exchangeRate" :min="0" :precision="8" :controls="false" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="form.dealType === 'LOAN'">
            <el-col :span="12">
              <el-form-item label="还款方式" prop="repaymentMethod">
                <el-select v-model="form.repaymentMethod" placeholder="请选择">
                  <el-option label="到期还本付息" value="MATURITY" />
                  <el-option label="按月付息" value="MONTHLY" />
                  <el-option label="按季付息" value="QUARTERLY" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="担保方式" prop="guaranteeType">
                <el-select v-model="form.guaranteeType" placeholder="请选择">
                  <el-option label="信用" value="CREDIT" />
                  <el-option label="抵押" value="MORTGAGE" />
                  <el-option label="质押" value="PLEDGE" />
                  <el-option label="保证" value="GUARANTEE" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="form.dealType === 'DEPOSIT' || form.dealType === 'INTERBANK'">
            <el-col :span="12">
              <el-form-item label="计息方式" prop="interestType">
                <el-select v-model="form.interestType" placeholder="请选择">
                  <el-option label="定期" value="REGULAR" />
                  <el-option label="活期" value="CURRENT" />
                  <el-option label="按月付息" value="MONTHLY" />
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
              <el-form-item label="备注" prop="remarks">
                <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="stepActive === 2">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="交易编号">{{ form.dealNo || '系统自动生成' }}</el-descriptions-item>
            <el-descriptions-item label="交易类型">{{ getTypeLabel(form.dealType) }}</el-descriptions-item>
            <el-descriptions-item label="交易子类型">{{ form.dealSubtype }}</el-descriptions-item>
            <el-descriptions-item label="业务单元">{{ form.entityName }}</el-descriptions-item>
            <el-descriptions-item label="交易员">{{ form.dealerName }}</el-descriptions-item>
            <el-descriptions-item label="交易对手">{{ form.counterpartyName }}</el-descriptions-item>
            <el-descriptions-item label="结算币种">{{ form.currencyCode }}</el-descriptions-item>
            <el-descriptions-item label="结算金额">{{ form.amount }}</el-descriptions-item>
            <el-descriptions-item label="起息日">{{ form.valueDate }}</el-descriptions-item>
            <el-descriptions-item label="到期日">{{ form.maturityDate }}</el-descriptions-item>
            <el-descriptions-item label="年利率" v-if="form.interestRate">{{ form.interestRate }}%</el-descriptions-item>
            <el-descriptions-item label="描述">{{ form.description }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-form>

      <div class="step-buttons">
        <el-button v-if="stepActive > 0" @click="stepActive--">上一步</el-button>
        <el-button v-if="stepActive < 2" type="primary" @click="stepActive++">下一步</el-button>
      </div>
    </el-card>

    <el-dialog v-model="showEntitySelector" title="选择业务单元" width="600px">
      <el-table :data="entityList" @row-click="selectEntity" highlight-current-row>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="showDealerSelector" title="选择交易员" width="600px">
      <el-table :data="dealerList" @row-click="selectDealer" highlight-current-row>
        <el-table-column prop="code" label="编号" width="120" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="department" label="部门" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="showCounterpartySelector" title="选择交易对手" width="600px">
      <el-table :data="counterpartyList" @row-click="selectCounterparty" highlight-current-row>
        <el-table-column prop="code" label="编号" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="counterpartyType" label="类型" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="showInstrumentSelector" title="选择金融工具" width="600px">
      <el-table :data="instrumentList" @row-click="selectInstrument" highlight-current-row>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="instrumentType" label="类型" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createDeal, updateDeal, getDeal } from '@/api/dealing'
import { listBusinessUnit, listTrader, listCounterparty, listCounterpartyAccount, listCurrency } from '@/api/basedata'
import { listInstrument } from '@/api/dealing'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const stepActive = ref(0)
const saving = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.query.id)

const form = reactive({
  id: null, dealType: '', dealSubtype: '', entityId: null, entityName: '',
  dealerId: null, dealerName: '', counterpartyId: null, counterpartyName: '',
  counterpartyAccountId: null, instrumentId: null, instrumentName: '',
  currencyCode: '', amount: 0, valueDate: '', maturityDate: '',
  interestRate: null, interestType: '', exchangeRate: null,
  buyCurrency: '', sellCurrency: '', repaymentMethod: '', guaranteeType: '',
  description: '', remarks: ''
})

const rules = {
  dealType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  dealSubtype: [{ required: true, message: '请选择交易子类型', trigger: 'change' }],
  entityId: [{ required: true, message: '请选择业务单元', trigger: 'change' }],
  dealerId: [{ required: true, message: '请选择交易员', trigger: 'change' }],
  counterpartyId: [{ required: true, message: '请选择交易对手', trigger: 'change' }],
  currencyCode: [{ required: true, message: '请选择结算币种', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  valueDate: [{ required: true, message: '请选择起息日', trigger: 'change' }],
  maturityDate: [{ required: true, message: '请选择到期日', trigger: 'change' }]
}

const subtypeOptions = computed(() => {
  const map = {
    DEPOSIT: [{ label: '定期存款', value: 'TERM' }, { label: '活期存款', value: 'CURRENT' }, { label: '大额存单', value: 'CD' }, { label: '通知存款', value: 'NOTICE' }],
    LOAN: [{ label: '短期贷款', value: 'SHORT' }, { label: '中长期贷款', value: 'LONG' }, { label: '信用贷款', value: 'CREDIT' }, { label: '抵押贷款', value: 'MORTGAGE' }],
    FX: [{ label: '即期外汇', value: 'SPOT' }, { label: '远期外汇', value: 'FORWARD' }, { label: 'NDF', value: 'NDF' }, { label: '外汇掉期', value: 'SWAP' }],
    INTERBANK: [{ label: '同业存放', value: 'DEPOSIT' }, { label: '存放同业', value: 'PLACEMENT' }, { label: '同业拆借', value: 'CALL' }]
  }
  return map[form.dealType] || []
})

const showEntitySelector = ref(false)
const showDealerSelector = ref(false)
const showCounterpartySelector = ref(false)
const showInstrumentSelector = ref(false)

const entityList = ref([])
const dealerList = ref([])
const counterpartyList = ref([])
const instrumentList = ref([])
const accountList = ref([])
const currencyList = ref([])

const getTypeLabel = (type) => {
  const map = { DEPOSIT: '存款', LOAN: '贷款', FX: '外汇', INTERBANK: '同业' }
  return map[type] || type
}

const handleDealTypeChange = () => { form.dealSubtype = '' }

const selectEntity = (row) => { form.entityId = row.id; form.entityName = row.name; showEntitySelector.value = false }
const selectDealer = (row) => { form.dealerId = row.id; form.dealerName = row.name; showDealerSelector.value = false }
const selectCounterparty = async (row) => {
  form.counterpartyId = row.id; form.counterpartyName = row.name; showCounterpartySelector.value = false
  const res = await listCounterpartyAccount({ counterpartyId: row.id, pageSize: 100 })
  accountList.value = res.data.list || []
}
const selectInstrument = (row) => { form.instrumentId = row.id; form.instrumentName = row.name; showInstrumentSelector.value = false }

const fetchLists = async () => {
  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []
  dealerList.value = (await listTrader({ pageSize: 1000 })).data.list || []
  counterpartyList.value = (await listCounterparty({ pageSize: 1000 })).data.list || []
  instrumentList.value = (await listInstrument({ pageSize: 1000 })).data.list || []
  currencyList.value = (await listCurrency({ pageSize: 1000 })).data.list || []
}

const handleSaveDraft = async () => {
  saving.value = true
  try {
    isEdit.value ? await updateDeal(form) : await createDeal(form)
    ElMessage.success('暂存成功')
    handleBack()
  } catch (e) { console.error(e) }
  finally { saving.value = false }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    isEdit.value ? await updateDeal(form) : await createDeal(form)
    ElMessage.success('提交成功')
    handleBack()
  } catch (e) { console.error(e) }
  finally { submitting.value = false }
}

const handleBack = () => { router.push('/dealing/deal') }

onMounted(async () => {
  await fetchLists()
  if (isEdit.value) {
    const res = await getDeal(route.query.id)
    Object.assign(form, res.data)
  }
})
</script>

<style scoped>
.deal-edit { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.form-container { padding: 20px 0; }
.step-buttons { margin-top: 20px; text-align: center; }
</style>