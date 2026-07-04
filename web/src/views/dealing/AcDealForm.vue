<template>
  <div class="ac-deal-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form-container">
      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="交易编号" v-if="form.dealNumber">
        <el-input v-model="form.dealNumber" disabled />
      </el-form-item>

      <el-form-item label="管理主体" prop="managementEntity">
        <BaseDataPicker
          v-model="form.managementEntity"
          entity="management-entity"
          placeholder="请选择管理主体"
        />
      </el-form-item>

      <el-form-item label="交易员" prop="traderId">
        <BaseDataPicker
          v-model="form.traderId"
          entity="trader"
          placeholder="请选择交易员"
        />
      </el-form-item>

      <el-form-item label="交易对手">
        <BaseDataPicker
          v-model="form.counterpartyId"
          entity="counterparty"
          placeholder="请选择交易对手"
          @change="onCounterpartyChange"
        />
      </el-form-item>

      <el-form-item label="金融工具">
        <BaseDataPicker
          v-model="form.instrumentId"
          entity="instrument"
          placeholder="请选择金融工具"
          @change="row => form.instrumentName = row?.instrumentName || ''"
        />
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
        <BaseDataPicker
          v-model="form.currency"
          entity="currency"
          placeholder="请选择币种"
        />
      </el-form-item>

      <el-form-item label="交易日期" prop="dealDate">
        <el-date-picker v-model="form.dealDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="起息日" prop="valueDate">
        <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-divider content-position="left">AC 专属字段</el-divider>

      <el-form-item label="本方账户" prop="bankAccountId">
        <BaseDataPicker
          v-model="form.bankAccountId"
          entity="bank-account"
          placeholder="请选择本方银行账户"
          @change="row => form.bankAccountName = row?.accountName || ''"
        />
      </el-form-item>

      <el-form-item label="对手方账户">
        <BaseDataPicker
          v-model="form.counterpartyAccountId"
          entity="counterparty-account"
          :auto-filter="{ counterpartyId: form.counterpartyId }"
          placeholder="请选择对手方账户"
          @change="row => form.counterpartyAccountName = row?.accountName || ''"
        />
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
import BaseDataPicker from '@/components/picker/BaseDataPicker.vue'

const props = defineProps({
  dealData: { type: Object, default: null }
})
const emit = defineEmits(['saved', 'cancel'])

const formRef = ref()
const saving = ref(false)

const form = reactive({
  dealNumber: '',
  dealType: 'AC',
  managementEntity: '',
  traderId: null,
  counterpartyId: null,
  instrumentId: null,
  direction: 'Outflow',
  amount: 0,
  currency: '',
  dealDate: new Date().toISOString().slice(0, 10),
  valueDate: new Date().toISOString().slice(0, 10),
  bankAccountId: null,
  counterpartyAccountId: null,
  bankAccountName: '',
  counterpartyAccountName: '',
  instrumentName: '',
  paymentMethod: 'TRANSFER',
  description: '',
  remark: '',
  operator: 'admin'
})

const rules = {
  managementEntity: [{ required: true, message: '管理主体不能为空', trigger: 'change' }],
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
      managementEntity: val.managementEntity,
      traderId: val.traderId ?? null,
      counterpartyId: val.counterpartyId ?? null,
      instrumentId: val.instrumentId ?? null,
      direction: val.direction,
      amount: val.amount,
      currency: val.currency,
      dealDate: val.dealDate,
      valueDate: val.valueDate,
      bankAccountId: val.bankAccountId ?? null,
      counterpartyAccountId: val.counterpartyAccountId ?? null,
      bankAccountName: val.bankAccountName || '',
      counterpartyAccountName: val.counterpartyAccountName || '',
      instrumentName: val.instrumentName || '',
      paymentMethod: val.paymentMethod,
      description: val.description,
      remark: val.remark,
      operator: 'admin'
    })
  }
}, { immediate: true })

// 交易对手变化时清空对手方账户
const onCounterpartyChange = (row) => {
  form.counterpartyId = row?.id ?? null
  form.counterpartyAccountId = null
  form.counterpartyAccountName = ''
}

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