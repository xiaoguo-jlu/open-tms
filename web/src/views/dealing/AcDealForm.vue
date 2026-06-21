<template>
  <div class="ac-deal-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form-container">
      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="交易编号" v-if="form.dealNumber">
        <el-input v-model="form.dealNumber" disabled />
      </el-form-item>

      <el-form-item label="业务主体" prop="businessUnit">
        <el-input v-model="form.businessUnit" placeholder="业务主体编码" />
      </el-form-item>

      <el-form-item label="交易员 ID" prop="traderId">
        <el-input-number v-model="form.traderId" :min="1" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="交易对手 ID">
        <el-input-number v-model="form.counterpartyId" :min="0" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="金融工具 ID">
        <el-input-number v-model="form.instrumentId" :min="0" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="方向" prop="direction">
        <el-radio-group v-model="form.direction">
          <el-radio value="Inflow">流入</el-radio>
          <el-radio value="Outflow">流出</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="币种" prop="currency">
        <el-select v-model="form.currency" placeholder="请选择" filterable>
          <el-option label="CNY" value="CNY" />
          <el-option label="USD" value="USD" />
          <el-option label="EUR" value="EUR" />
          <el-option label="JPY" value="JPY" />
          <el-option label="HKD" value="HKD" />
        </el-select>
      </el-form-item>

      <el-form-item label="交易日期" prop="dealDate">
        <el-date-picker v-model="form.dealDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="起息日" prop="valueDate">
        <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-divider content-position="left">AC 专属字段</el-divider>

      <el-form-item label="本方账户 ID" prop="bankAccountId">
        <el-input-number v-model="form.bankAccountId" :min="1" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="对手方账户 ID">
        <el-input-number v-model="form.counterpartyAccountId" :min="0" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="支付方式">
        <el-select v-model="form.paymentMethod" placeholder="请选择" clearable>
          <el-option label="转账" value="TRANSFER" />
          <el-option label="票据" value="CHECK" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>

      <el-form-item label="操作人" prop="operator">
        <el-input v-model="form.operator" placeholder="操作人" />
      </el-form-item>

      <el-divider content-position="left">v2.0 自动生成</el-divider>
      <el-alert type="success" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>系统将自动生成：Action (CREATE) + DealMap(ActualCashflow) + Cashflow（CREATE 不生成 DealImage）</span>
        </template>
      </el-alert>

      <div class="form-actions">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createAcDeal, updateAcDeal } from '@/api/dealing/acDeal'

const props = defineProps({
  dealData: { type: Object, default: null }
})
const emit = defineEmits(['saved', 'cancel'])

const formRef = ref()
const saving = ref(false)

const form = reactive({
  dealNumber: '',
  dealType: 'AC',
  businessUnit: '',
  traderId: 1,
  counterpartyId: null,
  instrumentId: null,
  direction: 'Outflow',
  amount: 0,
  currency: 'CNY',
  dealDate: new Date().toISOString().slice(0, 10),
  valueDate: new Date().toISOString().slice(0, 10),
  bankAccountId: 1,
  counterpartyAccountId: null,
  paymentMethod: 'TRANSFER',
  description: '',
  remark: '',
  operator: 'admin'
})

const rules = {
  businessUnit: [{ required: true, message: '业务主体不能为空', trigger: 'blur' }],
  traderId: [{ required: true, message: '交易员不能为空', trigger: 'change' }],
  direction: [{ required: true, message: '方向不能为空', trigger: 'change' }],
  amount: [{ required: true, message: '金额不能为空', trigger: 'change' }],
  currency: [{ required: true, message: '币种不能为空', trigger: 'change' }],
  dealDate: [{ required: true, message: '交易日期不能为空', trigger: 'change' }],
  valueDate: [{ required: true, message: '起息日不能为空', trigger: 'change' }],
  bankAccountId: [{ required: true, message: '本方账户不能为空', trigger: 'change' }],
  operator: [{ required: true, message: '操作人不能为空', trigger: 'blur' }]
}

watch(() => props.dealData, (val) => {
  if (val) {
    Object.assign(form, {
      dealNumber: val.dealNumber,
      dealType: 'AC',
      businessUnit: val.businessUnit,
      traderId: val.traderId || 1,
      counterpartyId: val.counterpartyId,
      instrumentId: val.instrumentId,
      direction: val.direction,
      amount: val.amount,
      currency: val.currency,
      dealDate: val.dealDate,
      valueDate: val.valueDate,
      bankAccountId: val.bankAccountId,
      counterpartyAccountId: val.counterpartyAccountId,
      paymentMethod: val.paymentMethod,
      description: val.description,
      remark: val.remark,
      operator: 'admin'
    })
  }
}, { immediate: true })

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    ElMessage.error('请检查表单填写')
    return
  }
  saving.value = true
  try {
    if (form.dealNumber) {
      await updateAcDeal(form)
    } else {
      await createAcDeal(form)
    }
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.ac-deal-form { padding: 0 12px; }
.form-actions { text-align: right; padding: 12px 0; }
</style>
