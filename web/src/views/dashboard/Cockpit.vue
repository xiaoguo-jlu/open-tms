<template>
  <div class="dashboard">
    <el-row :gutter="16" class="summary-row">
      <el-col :span="6"><el-card shadow="hover"><div class="card"><div class="label">资金总头寸</div><div class="value">{{formatAmount(totalPosition)}}</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="card"><div class="label">本月收入</div><div class="value income">{{formatAmount(monthIncome)}}</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="card"><div class="label">本月支出</div><div class="value expense">{{formatAmount(monthExpense)}}</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="card"><div class="label">待审批</div><div class="value warning">{{pendingCount}}</div></div></el-card></el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header><span>头寸趋势</span></template><div style="height:250px;">趋势图表</div></el-card></el-col>
      <el-col :span="12"><el-card><template #header><span>业务单元头寸分布</span></template><div style="height:250px;">饼图</div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="8"><el-card><template #header><span>交易统计</span></template><div class="stat-item">存款: {{depositCount}}笔</div><div class="stat-item">贷款: {{loanCount}}笔</div><div class="stat-item">外汇: {{fxCount}}笔</div></el-card></el-col>
      <el-col :span="8"><el-card><template #header><span>风险指标</span></template><div class="stat-item">VaR: {{formatAmount(varValue)}}</div><div class="stat-item">敞口: {{formatAmount(exposure)}}</div><div class="stat-item">限额使用: {{limitUsage}}%</div></el-card></el-col>
      <el-col :span="8"><el-card><template #header><span>待办事项</span></template><div class="stat-item">待审批: {{pendingCount}}笔</div><div class="stat-item">待收款: {{receivableCount}}笔</div><div class="stat-item">待付款: {{payableCount}}笔</div></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import {ref,onMounted} from 'vue'
const totalPosition=ref(0),monthIncome=ref(0),monthExpense=ref(0),pendingCount=ref(0)
const depositCount=ref(0),loanCount=ref(0),fxCount=ref(0),varValue=ref(0),exposure=ref(0),limitUsage=ref(0)
const receivableCount=ref(0),payableCount=ref(0)
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
onMounted(()=>{})
</script>
<style scoped>.dashboard{}.summary-row{margin-bottom:16px;}.card{text-align:center}.label{font-size:14px;color:#909399;margin-bottom:8px}.value{font-size:24px;font-weight:600;color:#303133}.value.income{color:#67C23A}.value.expense{color:#F56C6C}.value.warning{color:#E6A23C;cursor:pointer}.stat-item{margin:8px 0}
</style>