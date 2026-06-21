<template>
  <div class="at-deal-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑 AT 交易' : '新建 AT 交易' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务单元" prop="businessUnit">
              <el-input v-model="form.businessUnit" placeholder="如 BU001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转账类型" prop="transferType">
              <el-select v-model="form.transferType" placeholder="请选择转账类型" style="width: 100%">
                <el-option label="同公司转账" value="SAME_COMPANY" />
                <el-option label="跨公司转账" value="CROSS_COMPANY" />
                <el-option label="跨境转账" value="CROSS_BORDER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="源账户" prop="sourceAccountId">
              <el-select
                v-model="form.sourceAccountId"
                placeholder="请选择源账户"
                filterable
                style="width: 100%"
                :disabled="isEdit"
              >
                <el-option
                  v-for="acc in accountList"
                  :key="acc.id"
                  :label="`${acc.accountNo} (${acc.accountName})`"
                  :value="acc.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标账户" prop="destAccountId">
              <el-select
                v-model="form.destAccountId"
                placeholder="请选择目标账户"
                filterable
                style="width: 100%"
                :disabled="isEdit"
              >
                <el-option
                  v-for="acc in accountList"
                  :key="acc.id"
                  :label="`${acc.accountNo} (${acc.accountName})`"
                  :value="acc.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="源金额" prop="sourceAmount">
              <el-input-number
                v-model="form.sourceAmount"
                :precision="2"
                :step="1000"
                :min="0"
                style="width: 100%"
                placeholder="0.00"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源币种" prop="sourceCurrency">
              <el-select v-model="form.sourceCurrency" placeholder="请选择币种" style="width: 100%">
                <el-option v-for="c in currencyList" :key="c.currencyNo" :label="c.currencyNo" :value="c.currencyNo" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标金额" prop="destAmount">
              <el-input-number
                v-model="form.destAmount"
                :precision="2"
                :step="1000"
                :min="0"
                style="width: 100%"
                placeholder="0.00"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标币种" prop="destCurrency">
              <el-select v-model="form.destCurrency" placeholder="请选择币种" style="width: 100%">
                <el-option v-for="c in currencyList" :key="c.currencyNo" :label="c.currencyNo" :value="c.currencyNo" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="isCrossCurrency">
          <el-col :span="12">
            <el-form-item label="汇率" prop="exchangeRate">
              <el-input-number
                v-model="form.exchangeRate"
                :precision="6"
                :step="0.0001"
                :min="0"
                style="width: 100%"
                placeholder="如 7.2000"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label=" ">
              <el-alert
                :title="`跨币种 ${form.sourceCurrency} → ${form.destCurrency}，请填写汇率`"
                type="warning"
                :closable="false"
                show-icon
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起息日" prop="valueDate">
              <el-date-picker
                v-model="form.valueDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="选择到账日"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付方式" prop="paymentMethod">
              <el-select v-model="form.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
                <el-option label="内部转账 (INTERNAL)" value="INTERNAL" />
                <el-option label="SWIFT 电汇" value="SWIFT" />
                <el-option label="RTGS 实时结算" value="RTGS" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="资金用途" prop="purpose">
          <el-input
            v-model="form.purpose"
            type="textarea"
            :rows="3"
            placeholder="请描述资金用途"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label=" ">
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建交易' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAtDeal, saveAtDeal, updateAtDeal } from '@/api/dealing'
import { listBankAccount, listCurrency } from '@/api/basedata'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const submitting = ref(false)
const formRef = ref(null)

const accountList = ref([])
const currencyList = ref([])

const form = reactive({
  id: null,
  dealNumber: null,
  businessUnit: '',
  transferType: '',
  sourceAccountId: null,
  destAccountId: null,
  sourceAmount: null,
  destAmount: null,
  sourceCurrency: '',
  destCurrency: '',
  exchangeRate: null,
  valueDate: '',
  paymentMethod: '',
  purpose: '',
  operator: 'currentUser',
  remark: ''
})

const isCrossCurrency = computed(() => {
  return form.sourceCurrency && form.destCurrency && form.sourceCurrency !== form.destCurrency
})

const validateDiffAccount = (rule, value, cb) => {
  if (value && form.sourceAccountId && value === form.sourceAccountId) {
    cb(new Error('源账户和目标账户不能相同'))
  } else {
    cb()
  }
}

const validateExchangeRate = (rule, value, cb) => {
  if (isCrossCurrency.value && (value == null || value <= 0)) {
    cb(new Error('跨币种时汇率必须 > 0'))
  } else {
    cb()
  }
}

const rules = {
  businessUnit: [{ required: true, message: '请输入业务单元', trigger: 'blur' }],
  transferType: [{ required: true, message: '请选择转账类型', trigger: 'change' }],
  sourceAccountId: [
    { required: true, message: '请选择源账户', trigger: 'change' },
    { validator: validateDiffAccount, trigger: 'change' }
  ],
  destAccountId: [
    { required: true, message: '请选择目标账户', trigger: 'change' },
    { validator: validateDiffAccount, trigger: 'change' }
  ],
  sourceAmount: [{ required: true, message: '请输入源金额', trigger: 'blur' }],
  destAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  sourceCurrency: [{ required: true, message: '请选择源币种', trigger: 'change' }],
  destCurrency: [{ required: true, message: '请选择目标币种', trigger: 'change' }],
  exchangeRate: [{ validator: validateExchangeRate, trigger: 'blur' }],
  valueDate: [{ required: true, message: '请选择起息日', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// 监听币种：同币种自动填汇率 1，跨币种清空汇率
watch([() => form.sourceCurrency, () => form.destCurrency], ([s, d]) => {
  if (s && d && s === d) {
    if (form.exchangeRate == null) form.exchangeRate = 1
  } else if (s && d && s !== d) {
    form.exchangeRate = null
  }
})

const fetchAccounts = async () => {
  try {
    const res = await listBankAccount({ pageSize: 1000 })
    accountList.value = res.data.records || res.data.list || []
  } catch (e) { console.error(e) }
}

const fetchCurrencies = async () => {
  try {
    const res = await listCurrency({ pageSize: 1000 })
    currencyList.value = res.data.records || res.data.list || []
  } catch (e) { console.error(e) }
}

const loadExisting = async () => {
  if (!isEdit.value) return
  try {
    const res = await getAtDeal(route.query.id)
    Object.assign(form, res.data)
  } catch (e) { console.error(e) }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.error('请检查表单填写')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAtDeal(form)
      ElMessage.success('修改成功')
    } else {
      await saveAtDeal(form)
      ElMessage.success('创建成功')
    }
    router.push('/dealing/at-deal')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleBack = () => { router.push('/dealing/at-deal') }

onMounted(async () => {
  await Promise.all([fetchAccounts(), fetchCurrencies()])
  await loadExisting()
})
</script>

<style scoped>
.at-deal-form { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
