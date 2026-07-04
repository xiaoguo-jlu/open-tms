<template>
  <div class="at-deal-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isCopy ? '复制 AT 交易' : isEdit ? '编辑 AT 交易' : '新建 AT 交易' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <!-- 顶部错误提示 (硬阻断) -->
      <el-alert
        v-if="crossCurrencyError || crossMgmtEntityError"
        :title="crossCurrencyError || crossMgmtEntityError"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="管理主体" prop="managementEntity">
              <BaseDataPicker
                v-model="form.managementEntity"
                entity="management-entity"
                placeholder="请选择管理主体"
              />
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
              <BaseDataPicker
                v-model="form.sourceAccountId"
                entity="bank-account"
                placeholder="请选择源账户"
                :disabled="isEdit"
                @change="row => {
                  form.sourceAccountNo = row?.accountNo || ''
                  sourceAccount.value = row || null
                }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标账户" prop="destAccountId">
              <BaseDataPicker
                v-model="form.destAccountId"
                entity="bank-account"
                placeholder="请选择目标账户"
                :disabled="isEdit"
                @change="row => {
                  form.destAccountNo = row?.accountNo || ''
                  destAccount.value = row || null
                }"
              />
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
              <BaseDataPicker
                v-model="form.sourceCurrency"
                entity="currency"
                placeholder="请选择源币种"
              />
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
              <BaseDataPicker
                v-model="form.destCurrency"
                entity="currency"
                placeholder="请选择目标币种"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 跨币种汇率 (AT 不支持, 但保留以提示用户改用 FX) -->
        <el-row v-if="showExchangeRate" :gutter="20">
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
                :title="`跨币种 ${form.sourceCurrency} → ${form.destCurrency}，AT 不支持跨币种转账`"
                type="error"
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
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
            :disabled="!!crossCurrencyError || !!crossMgmtEntityError"
          >
            {{ isEdit ? '保存修改' : '创建交易' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAtDeal, saveAtDeal, updateAtDeal, copyAtDeal } from '@/api/dealing'
import BaseDataPicker from '@/components/picker/BaseDataPicker.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const isCopy = computed(() => !!route.query.copyFrom)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  dealNumber: null,
  managementEntity: '',
  transferType: '',
  sourceAccountId: null,
  destAccountId: null,
  sourceAccountNo: '',
  destAccountNo: '',
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

// 保存选中的源/目标账户完整行 (用于读取 currency 和 managementEntityId)
const sourceAccount = ref(null)
const destAccount = ref(null)

// ====== AT 校验 (硬阻断) ======
// 跨币种: 用户选了不同币种,或源/目标账户的 currency 不同 → 阻断
const isCurrencyDiff = computed(() => {
  if (form.sourceCurrency && form.destCurrency && form.sourceCurrency !== form.destCurrency) return true
  if (sourceAccount.value?.currency && destAccount.value?.currency &&
      sourceAccount.value.currency !== destAccount.value.currency) return true
  return false
})

// 跨管理主体: 源/目标账户的管理主体不同 → 阻断
const isMgmtEntityDiff = computed(() => {
  if (!sourceAccount.value || !destAccount.value) return false
  if (sourceAccount.value.managementEntityId == null || destAccount.value.managementEntityId == null) return false
  return Number(sourceAccount.value.managementEntityId) !== Number(destAccount.value.managementEntityId)
})

const crossCurrencyError = computed(() => {
  if (!sourceAccount.value || !destAccount.value) return ''
  if (isCurrencyDiff.value) {
    return `AT 不支持跨币种转账 (源账户 ${sourceAccount.value.currency || form.sourceCurrency} → 目标账户 ${destAccount.value.currency || form.destCurrency})，请使用 FX 交易`
  }
  return ''
})

const crossMgmtEntityError = computed(() => {
  if (!sourceAccount.value || !destAccount.value) return ''
  if (isMgmtEntityDiff.value) {
    return `AT 不支持跨管理主体转账 (源主体=${sourceAccount.value.managementEntityId}, 目标主体=${destAccount.value.managementEntityId})`
  }
  return ''
})

// 仍保留对表单币种字段的兼容 (若用户已填但还未选账户)
const showExchangeRate = computed(() => {
  return isCurrencyDiff.value || (form.sourceCurrency && form.destCurrency && form.sourceCurrency !== form.destCurrency)
})

const validateDiffAccount = (rule, value, cb) => {
  if (value && form.sourceAccountId && value === form.sourceAccountId) {
    cb(new Error('源账户和目标账户不能相同'))
  } else {
    cb()
  }
}

const validateExchangeRate = (rule, value, cb) => {
  if (isCurrencyDiff.value && (value == null || value <= 0)) {
    cb(new Error('跨币种时汇率必须 > 0'))
  } else {
    cb()
  }
}

const rules = {
  managementEntity: [{ required: true, message: '请选择管理主体', trigger: 'change' }],
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

// 监听账户选择 → 自动同步币种和管理主体 (提升 UX)
// picker 同时返回 currency 字段, 因此填到 form.sourceCurrency/destCurrency
// 用户亦可手动调整 source/dest 币种

const loadExisting = async () => {
  if (isCopy.value) {
    await loadCopyData()
    return
  }
  if (!isEdit.value) return
  try {
    const res = await getAtDeal(route.query.id)
    Object.assign(form, res.data)
  } catch (e) { console.error(e) }
}

const loadCopyData = async () => {
  try {
    const res = await copyAtDeal(route.query.copyFrom)
    const data = res.data
    Object.assign(form, {
      id: null,
      dealNumber: null,
      managementEntity: data.managementEntity || '',
      transferType: data.transferType || '',
      sourceAccountId: data.sourceAccountId ?? null,
      destAccountId: data.destAccountId ?? null,
      sourceAccountNo: '',
      destAccountNo: '',
      sourceAmount: data.sourceAmount ?? null,
      destAmount: data.destAmount ?? null,
      sourceCurrency: data.sourceCurrency || '',
      destCurrency: data.destCurrency || '',
      exchangeRate: data.exchangeRate ?? null,
      valueDate: data.valueDate || '',
      paymentMethod: data.paymentMethod || '',
      purpose: data.purpose || '',
      operator: 'currentUser',
      remark: data.remark || ''
    })
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.error('请检查表单填写')
    return
  }
  // 前端二次防御: 跨币种/跨管理主体不能提交
  if (crossCurrencyError.value) {
    ElMessage.error(crossCurrencyError.value)
    return
  }
  if (crossMgmtEntityError.value) {
    ElMessage.error(crossMgmtEntityError.value)
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

loadExisting()
</script>

<style scoped>
.at-deal-form { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
