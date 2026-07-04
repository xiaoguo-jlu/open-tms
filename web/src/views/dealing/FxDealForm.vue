<template>
  <div class="fx-deal-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form-container">
      <el-divider content-position="left">通用信息</el-divider>

      <el-form-item label="交易编号" v-if="form.dealNumber">
        <el-input v-model="form.dealNumber" disabled />
      </el-form-item>

      <el-form-item label="管理主体" prop="managementEntityId">
        <BaseDataPicker
          v-model="form.managementEntityId"
          entity="management-entity"
          placeholder="请选择管理主体"
          @change="onManagementEntityChange"
        />
      </el-form-item>

      <el-form-item label="交易对手" prop="counterpartyId">
        <BaseDataPicker
          v-model="form.counterpartyId"
          entity="counterparty"
          placeholder="请选择交易对手"
        />
      </el-form-item>

      <el-form-item label="交易员" prop="traderId">
        <BaseDataPicker
          v-model="form.traderId"
          entity="trader"
          placeholder="请选择交易员"
        />
      </el-form-item>

      <el-form-item label="金融工具" prop="instrumentId">
        <BaseDataPicker
          v-model="form.instrumentId"
          entity="instrument"
          placeholder="请选择金融工具(自动决定产品类型)"
          @change="onInstrumentChange"
        />
      </el-form-item>

      <el-form-item label="币种对" prop="currencyPairId">
        <BaseDataPicker
          v-model="form.currencyPairId"
          entity="currency-pair"
          placeholder="请选择币种对"
          @change="onCurrencyPairChange"
        />
      </el-form-item>

      <el-divider content-position="left">交易要素（联动计算）</el-divider>
      <el-alert type="success" :closable="false" style="margin-bottom: 12px;">
        <template #title>联动 1：卖出金额 / 买入金额 / 成交汇率 - 填任意 2 个，第 3 个自动计算（300ms 防抖）</template>
      </el-alert>

      <el-form-item label="卖出金额" prop="sellAmount">
        <div class="input-with-ccy">
          <el-input-number v-model="form.sellAmount" :min="0" :precision="2" :controls="false" style="flex:1;" @change="triggerCalculate" />
          <el-input v-model="form.sellCurrency" placeholder="币种" disabled style="width: 80px; margin-left: 8px;" />
        </div>
      </el-form-item>

      <el-form-item label="买入金额" prop="buyAmount">
        <div class="input-with-ccy">
          <el-input-number v-model="form.buyAmount" :min="0" :precision="2" :controls="false" style="flex:1;" @change="triggerCalculate" />
          <el-input v-model="form.buyCurrency" placeholder="币种" disabled style="width: 80px; margin-left: 8px;" />
        </div>
      </el-form-item>

      <el-form-item label="成交汇率" prop="exchangeRate">
        <el-input-number v-model="form.exchangeRate" :min="0" :precision="8" :controls="false" style="width: 100%;" @change="triggerCalculate" />
      </el-form-item>

      <el-alert type="info" :closable="false" style="margin-bottom: 12px; margin-top: 12px;">
        <template #title>联动 2：成交汇率 / 市场汇率 / 点差 - 填任意 2 个，第 3 个自动计算</template>
      </el-alert>

      <el-form-item label="市场汇率" prop="marketRate">
        <el-input-number v-model="form.marketRate" :min="0" :precision="8" :controls="false" style="width: 100%;" @change="triggerCalculate" />
      </el-form-item>

      <el-form-item label="点差(bp)" prop="spreadBp">
        <el-input-number v-model="form.spreadBp" :precision="4" :controls="false" style="width: 100%;" @change="triggerCalculate" />
      </el-form-item>

      <el-divider content-position="left">日期信息</el-divider>

      <el-form-item label="交易日" prop="tradeDate">
        <el-date-picker v-model="form.tradeDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" @change="triggerCalculate" />
      </el-form-item>

      <el-form-item label="交割日" prop="valueDate">
        <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" @change="triggerCalculate" />
      </el-form-item>

      <el-form-item label="期限(天)">
        <el-input v-model="form.termDays" disabled placeholder="自动计算 = 交割日 - 交易日" />
      </el-form-item>

      <el-form-item label="到期日">
        <el-input v-model="form.maturityDate" disabled placeholder="= 交割日" />
      </el-form-item>

      <!-- NDF 特有字段（只在有 fixingSource 时显示） -->
      <template v-if="isNdf">
        <el-divider content-position="left">NDF 特有</el-divider>
        <el-alert type="warning" :closable="false" style="margin-bottom: 12px;">
          <template #title>NDF 创建不生成 Cashflow，待到 RATE_FIX 后生成 1 条差额 CF</template>
        </el-alert>

        <el-form-item label="名义本金">
          <el-input-number v-model="form.notional" :min="0" :precision="2" :controls="false" style="width: 100%;" disabled />
          <div class="field-hint">= 卖出金额</div>
        </el-form-item>

        <el-form-item label="Fixing 来源" prop="fixingSource">
          <el-select v-model="form.fixingSource" placeholder="请选择 fixing 汇率来源" clearable style="width: 100%;">
            <el-option label="BLOOMBERG BFIX" value="BLOOMBERG BFIX" />
            <el-option label="Reuters FX" value="Reuters FX" />
            <el-option label="中国外汇交易中心 CFETS" value="中国外汇交易中心 CFETS" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>

      <el-form-item label="操作人" prop="operator">
        <el-input v-model="form.operator" placeholder="操作人" />
      </el-form-item>

      <el-divider content-position="left">v3.2 自动生成</el-divider>
      <el-alert type="success" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>系统将自动生成：Action(DEAL) + 3 DealMap(FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE) + {{ isNdf ? '0' : '2' }} Cashflow{{ isNdf ? '(NDF 等 RATE_FIX)' : '' }}</span>
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
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { calculateFxDeal, createFxDeal, updateFxDeal } from '@/api/dealing/fxDeal'
import BaseDataPicker from '@/components/picker/BaseDataPicker.vue'

const props = defineProps({
  dealData: { type: Object, default: null }
})
const emit = defineEmits(['saved', 'cancel'])

const formRef = ref()
const saving = ref(false)
let calculateTimer = null

const form = reactive({
  dealNumber: '',
  managementEntityId: null,
  counterpartyId: null,
  traderId: null,
  instrumentId: null,
  currencyPairId: null,
  sellCurrency: '',
  sellAmount: null,
  buyCurrency: '',
  buyAmount: null,
  exchangeRate: null,
  marketRate: null,
  spreadBp: null,
  tradeDate: new Date().toISOString().slice(0, 10),
  valueDate: new Date().toISOString().slice(0, 10),
  termDays: '',
  maturityDate: '',
  notional: null,
  fixingSource: '',
  description: '',
  remark: '',
  operator: 'admin'
})

const isNdf = computed(() => {
  return form.fixingSource && form.fixingSource.length > 0
})

const rules = {
  managementEntityId: [{ required: true, message: '管理主体不能为空', trigger: 'change' }],
  counterpartyId: [{ required: true, message: '交易对手不能为空', trigger: 'change' }],
  traderId: [{ required: true, message: '交易员不能为空', trigger: 'change' }],
  instrumentId: [{ required: true, message: '金融工具不能为空', trigger: 'change' }],
  currencyPairId: [{ required: true, message: '币种对不能为空', trigger: 'change' }],
  sellAmount: [{ required: true, message: '卖出金额不能为空', trigger: 'change' }],
  buyAmount: [{ required: true, message: '买入金额不能为空', trigger: 'change' }],
  exchangeRate: [{ required: true, message: '成交汇率不能为空', trigger: 'change' }],
  marketRate: [{ required: true, message: '市场汇率不能为空', trigger: 'change' }],
  spreadBp: [{ required: true, message: '点差不能为空', trigger: 'change' }],
  tradeDate: [{ required: true, message: '交易日不能为空', trigger: 'change' }],
  valueDate: [{ required: true, message: '交割日不能为空', trigger: 'change' }],
  operator: [{ required: true, message: '操作人不能为空', trigger: 'blur' }]
}

// 币种对选中后自动填充 sell/buy 币种
const onCurrencyPairChange = (row) => {
  if (!row) return
  form.sellCurrency = row.baseCurrency || row.currency1 || ''
  form.buyCurrency = row.quoteCurrency || row.currency2 || ''
  triggerCalculate()
}

// 管理主体 picker 返回 code，但后端需要 Long id
const onManagementEntityChange = (row) => {
  if (!row) return
  form.managementEntityId = row.id
}

// 金融工具变更
const onInstrumentChange = (row) => {
  if (!row) return
  const type = row.instrumentType || ''
  // 如果 instrumentType 含 NDF，则默认显示 fixingSource
  if (type.toUpperCase().includes('NDF')) {
    if (!form.fixingSource) form.fixingSource = 'BLOOMBERG BFIX'
  } else {
    form.fixingSource = ''
    form.notional = null
  }
}

// NDF 名义本金 = 卖出金额
watch(() => form.sellAmount, (val) => {
  if (isNdf.value && val) {
    form.notional = val
  }
})

// 触发后端 calculate（300ms 防抖）
const triggerCalculate = () => {
  if (calculateTimer) clearTimeout(calculateTimer)
  calculateTimer = setTimeout(doCalculate, 300)
}

const doCalculate = async () => {
  const req = {
    sellAmount: form.sellAmount,
    buyAmount: form.buyAmount,
    exchangeRate: form.exchangeRate,
    marketRate: form.marketRate,
    spreadBp: form.spreadBp,
    tradeDate: form.tradeDate,
    valueDate: form.valueDate
  }
  // 检查是否至少填了 2 个金额/汇率字段
  let filled = 0
  if (req.sellAmount != null) filled++
  if (req.buyAmount != null) filled++
  if (req.exchangeRate != null) filled++
  if (req.marketRate != null) filled++
  if (req.spreadBp != null) filled++
  if (filled < 2) return

  try {
    const res = await calculateFxDeal(req)
    const d = res.data
    if (d.buyAmount != null) form.buyAmount = d.buyAmount
    if (d.sellAmount != null) form.sellAmount = d.sellAmount
    if (d.exchangeRate != null) form.exchangeRate = d.exchangeRate
    if (d.marketRate != null) form.marketRate = d.marketRate
    if (d.spreadBp != null) form.spreadBp = d.spreadBp
    if (d.termDays != null) form.termDays = d.termDays
    if (d.maturityDate != null) form.maturityDate = d.maturityDate
  } catch (e) {
    console.warn('Calculate failed:', e?.message || e)
  }
}

// 编辑时加载数据
watch(() => props.dealData, (val) => {
  if (val) {
    Object.assign(form, {
      dealNumber: val.dealNumber,
      managementEntityId: val.managementEntityId ?? null,
      counterpartyId: val.counterpartyId ?? null,
      traderId: val.traderId ?? null,
      instrumentId: val.instrumentId ?? null,
      currencyPairId: val.currencyPairId ?? null,
      sellCurrency: val.sellCurrency || '',
      sellAmount: val.sellAmount,
      buyCurrency: val.buyCurrency || '',
      buyAmount: val.buyAmount,
      exchangeRate: val.exchangeRate,
      marketRate: val.marketRate,
      spreadBp: val.spreadBp,
      tradeDate: val.tradeDate,
      valueDate: val.valueDate,
      termDays: val.termDays || '',
      maturityDate: val.maturityDate || '',
      notional: val.notional ?? null,
      fixingSource: val.fixingSource || '',
      description: val.description || '',
      remark: val.remark || '',
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
      await updateFxDeal(form)
    } else {
      await createFxDeal(form)
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
.fx-deal-form { padding: 0 12px; }
.form-actions { text-align: right; padding: 12px 0; }
.input-with-ccy { display: flex; align-items: center; }
.field-hint { color: #909399; font-size: 12px; margin-top: 4px; }
</style>