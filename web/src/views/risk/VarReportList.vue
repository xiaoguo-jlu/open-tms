<template>
  <div class="var-report-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="报告类型"><el-select v-model="queryForm.reportType" placeholder="请选择" clearable><el-option label="历史模拟法" value="HISTORICAL"/><el-option label="方差-协方差" value="VAR_COV"/><el-option label="蒙特卡洛" value="MONTE_CARLO"/></el-select></el-form-item>
        <el-form-item label="置信度"><el-select v-model="queryForm.confidence" placeholder="请选择" clearable><el-option label="95%" value="0.95"/><el-option label="99%" value="0.99"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button type="success" @click="handleCalculate">重新计算</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="reportDate" label="报告日期" width="120"/>
        <el-table-column prop="reportType" label="计算方法" width="120"><template #default="{row}"><el-tag>{{getTypeLabel(row.reportType)}}</el-tag></template></el-table-column>
        <el-table-column prop="confidence" label="置信度" width="80"/><el-table-column prop="holdingPeriod" label="持有期" width="80"/>
        <el-table-column prop="var1Day" label="1天VaR" align="right" width="150"><template #default="{row}">{{formatAmount(row.var1Day)}}</template></el-table-column>
        <el-table-column prop="var10Day" label="10天VaR" align="right" width="150"><template #default="{row}">{{formatAmount(row.var10Day)}}</template></el-table-column>
        <el-table-column prop="expectedLoss" label="预期损失" align="right" width="150"><template #default="{row}">{{formatAmount(row.expectedLoss)}}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.status==='CALCULATED'?'success':'info'">{{row.status==='CALCULATED'?'已计算':'计算中'}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">详情</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listVarReport,calculateVar} from '@/api/risk/var'
import {ElMessage} from 'element-plus'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({reportType:'',confidence:''})
const getTypeLabel=t=>{const m={HISTORICAL:'历史模拟',VAR_COV:'方差-协方差',MONTE_CARLO:'蒙特卡洛'};return m[t]||t}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listVarReport(queryForm);tableData.value=res.data.list||[]}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>fetchData()
const handleCalculate=async()=>{try{await calculateVar({});ElMessage.success('计算任务已提交')}catch(e){console.error(e)}}
const handleView=t=>router.push(`/risk/var/detail?id=${t.id}`)
onMounted(()=>fetchData())
</script>
<style scoped>.var-report-list{}</style>