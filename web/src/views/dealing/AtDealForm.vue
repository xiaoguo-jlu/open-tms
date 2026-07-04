<template>
  <div class="at-deal-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isCopy ? '复制 AT 交易' : isEdit ? '编辑 AT 交易' : '新建 AT 交易' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <!-- 业务规则说明 -->
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
        title="AT 交易仅支持同管理主体、同币种的内部转账，跨主体/跨币种请使用 FX 交易"
      />

      <!-- 错误提示（硬阻断） -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
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
                @change="onManagementEntityChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转账类型" prop="transferType">
              <el-select v-model="form.transferType" placeholder="请选择转账类型" style="width: 100%">
                <el-option label="同公司转账" value="SAME_COMPANY" />
                <el-option label="内部调拨" value="INTERNAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="付出方账户" prop="sourceAccountId">
              <BaseDataPicker
                v-model="form.sourceAccountId"
                entity="bank-account"
                placeholder="请选择付出方账户"
                :auto-filter="sourceFilter"
                :preload-row="sourceAccount"
                :disabled="isEdit"
                @change="onSourceAccountChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收入方账户" prop="destAccountId">
              <BaseDataPicker
                v-model="form.destAccountId"
                entity="bank-account"
                placeholder="请选择收入方账户"
                :auto-filter="destFilter"
                :preload-row="destAccount"
                :disabled="isEdit"
                @change="onDestAccountChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="转账金额" prop="amount">
              <el-input-number
                v-model="form.amount"
                :precision="2"
                :step="1000"
                :min="0"
                style="width: 100%"
                placeholder="0.00"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-input
                v-model="form.currency"
                placeholder="选择账户后自动填充"
                disabled
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
            :disabled="!!errorMessage"
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
import request from '@/utils/request'
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
  transferType: 'SAME_COMPANY',
  sourceAccountId: null,
  destAccountId: null,
  amount: null,
  currency: '',
  valueDate: '',
  paymentMethod: 'INTERNAL',
  purpose: '',
  operator: 'currentUser',
  remark: ''
})

// 选中的账户完整行 (读取 currency 和 managementEntityId)
const sourceAccount = ref(null)
const destAccount = ref(null)
const selectedMgmtEntity = ref(null)

// 过滤：源账户只显示选中管理主体下的账户
const sourceFilter = computed(() => {
  if (!selectedMgmtEntity.value) return {}
  return { managementEntityId: selectedMgmtEntity.value }
})
const destFilter = computed(() => sourceFilter.value)

// ====== 校验逻辑：同主体同币种 ======

const errorMessage = computed(() => {
  // 跨主体
  if (selectedMgmtEntity.value && form.managementEntity &&
      form.managementEntity !== selectedMgmtEntity.value) {
    return `管理主体已变更为 ${form.managementEntity}，请重新选择付出/收入方账户`
  }
  if (sourceAccount.value && selectedMgmtEntity.value &&
      Number(sourceAccount.value.managementEntityId) !== Number(selectedMgmtEntity.value)) {
    return `付出方账户 ${sourceAccount.value.accountNo} 不属于当前管理主体`
  }
  if (destAccount.value && selectedMgmtEntity.value &&
      Number(destAccount.value.managementEntityId) !== Number(selectedMgmtEntity.value)) {
    return `收入方账户 ${destAccount.value.accountNo} 不属于当前管理主体`
  }
  // 跨币种
  if (sourceAccount.value && destAccount.value &&
      sourceAccount.value.currency && destAccount.value.currency &&
      sourceAccount.value.currency !== destAccount.value.currency) {
    return `AT 不支持跨币种转账 (${sourceAccount.value.currency} → ${destAccount.value.currency})，请使用 FX 交易`
  }
  // 源=目标
  if (form.sourceAccountId && form.destAccountId &&
      Number(form.sourceAccountId) === Number(form.destAccountId)) {
    return '付出方和收入方账户不能相同'
  }
  return ''
})

// ====== Picker 事件 ======

const onManagementEntityChange = (row) => {
  if (!row) {
    selectedMgmtEntity.value = null
    return
  }
  selectedMgmtEntity.value = row.id
  // 切换管理主体 → 清空账户选择
  form.sourceAccountId = null
  form.destAccountId = null
  sourceAccount.value = null
  destAccount.value = null
  form.currency = ''
}

const onSourceAccountChange = (row) => {
  sourceAccount.value = row
  // picker 通过 update:modelValue 已经设置 form.sourceAccountId
  // 这里确保是数字类型
  if (row && row.id) {
    form.sourceAccountId = row.id
  }
  syncCurrency()
  // 清除 dest 的校验错误 (因为 source 变了, dest 的"相同"校验可能需要重跑)
  formRef.value?.clearValidate(['sourceAccountId', 'destAccountId'])
}

const onDestAccountChange = (row) => {
  destAccount.value = row
  if (row && row.id) {
    form.destAccountId = row.id
  }
  syncCurrency()
  formRef.value?.clearValidate(['sourceAccountId', 'destAccountId'])
}

// 选完两个账户后自动统一币种
const syncCurrency = () => {
  const srcCcy = sourceAccount.value?.currency
  const dstCcy = destAccount.value?.currency
  if (srcCcy && dstCcy && srcCcy === dstCcy) {
    form.currency = srcCcy
  } else {
    form.currency = ''
  }
}

// ====== 校验规则 ======

const validateDiffAccount = (rule, value, cb) => {
  // 根据 rule.field 判断是 source 还是 dest，比较另一方
  const other = rule.field === 'sourceAccountId' ? form.destAccountId : form.sourceAccountId
  if (value && other && Number(value) === Number(other)) {
    cb(new Error('付出方和收入方账户不能相同'))
  } else {
    cb()
  }
}

const rules = {
  managementEntity: [{ required: true, message: '请选择管理主体', trigger: 'change' }],
  transferType: [{ required: true, message: '请选择转账类型', trigger: 'change' }],
  sourceAccountId: [
    { required: true, message: '请选择付出方账户', trigger: 'change' },
    { validator: validateDiffAccount, trigger: 'change' }
  ],
  destAccountId: [
    { required: true, message: '请选择收入方账户', trigger: 'change' },
    { validator: validateDiffAccount, trigger: 'change' }
  ],
  amount: [{ required: true, message: '请输入转账金额', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择同币种的账户', trigger: 'change' }],
  valueDate: [{ required: true, message: '请选择起息日', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// ====== 加载 / 复制 ======

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
      transferType: data.transferType || 'SAME_COMPANY',
      sourceAccountId: data.sourceAccountId ?? null,
      destAccountId: data.destAccountId ?? null,
      amount: data.sourceAmount ?? data.destAmount ?? null,
      currency: data.sourceCurrency || data.destCurrency || '',
      valueDate: data.valueDate || '',
      paymentMethod: data.paymentMethod || 'INTERNAL',
      purpose: data.purpose || '',
      operator: 'currentUser',
      remark: data.remark || ''
    })
    // 加载源/目标账户完整行（用于 picker 显示）
    if (data.sourceAccountId) {
      const acc = await loadAccountById(data.sourceAccountId)
      if (acc) sourceAccount.value = acc
    }
    if (data.destAccountId) {
      const acc = await loadAccountById(data.destAccountId)
      if (acc) destAccount.value = acc
    }
    // 同步管理主体选中状态
    if (data.managementEntity) {
      // 数值类型的管理主体 ID 不在 copy DTO 中, 通过源账户反推
      if (sourceAccount.value?.managementEntityId) {
        selectedMgmtEntity.value = sourceAccount.value.managementEntityId
      }
    }
  } catch (e) { console.error(e) }
}

// 通过 ID 加载银行账户完整信息（用于复制时的展示）
const loadAccountById = async (id) => {
  try {
    // 优先尝试 id 精确搜索
    const res = await request({
      url: '/api/v1/bank-accounts/page',
      method: 'get',
      params: { pageNum: 1, pageSize: 20, keyword: String(id) }
    })
    const records = res?.data?.records || res?.data?.list || []
    return records.find(r => Number(r.id) === Number(id)) || null
  } catch (e) {
    return null
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.error('请检查表单填写')
    return
  }
  if (errorMessage.value) {
    ElMessage.error(errorMessage.value)
    return
  }
  submitting.value = true
  try {
    // 把单一 amount/currency 映射回 source/dest 字段（兼容后端 DTO）
    const submitForm = {
      ...form,
      sourceAmount: form.amount,
      destAmount: form.amount,
      sourceCurrency: form.currency,
      destCurrency: form.currency,
      exchangeRate: 1
    }
    if (isEdit.value) {
      await updateAtDeal(submitForm)
      ElMessage.success('修改成功')
    } else {
      await saveAtDeal(submitForm)
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