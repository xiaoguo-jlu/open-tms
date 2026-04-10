<template>
  <div class="exposure-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="敞口类型"><el-select v-model="queryForm.exposureType" placeholder="请选择" clearable><el-option label="利率敞口" value="IR"/><el-option label="外汇敞口" value="FX"/><el-option label="信用敞口" value="CREDIT"/></el-select></el-form-item>
        <el-form-item label="业务单元"><el-select v-model="queryForm.entityId" placeholder="请选择" clearable filterable><el-option v-for="i in entityList" :key="i.id" :label="i.name" :value="i.id"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAddLimit">设置限额</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="exposureType" label="敞口类型" width="100"><template #default="{row}"><el-tag>{{getTypeLabel(row.exposureType)}}</el-tag></template></el-table-column>
        <el-table-column prop="entityName" label="业务单元" width="120"/>
        <el-table-column prop="currencyCode" label="币种" width="80"/>
        <el-table-column prop="exposureAmount" label="敞口金额" align="right" width="150"><template #default="{row}">{{formatAmount(row.exposureAmount)}}</template></el-table-column>
        <el-table-column prop="limitAmount" label="限额" align="right" width="150"><template #default="{row}">{{formatAmount(row.limitAmount)}}</template></el-table-column>
        <el-table-column prop="limitUsage" label="限额使用率" width="120"><template #default="{row}"><el-progress :percentage="row.limitUsage" :color="getProgressColor(row.limitUsage)"/></template></el-table-column>
        <el-table-column prop="var" label="VaR" align="right" width="150"><template #default="{row}">{{formatAmount(row.var)}}</template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">明细</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listExposure} from '@/api/risk/exposure'
import {listBusinessUnit} from '@/api/basedata'
const router=useRouter()
const loading=ref(false),tableData=ref([]),entityList=ref([])
const queryForm=reactive({exposureType:'',entityId:''})
const getTypeLabel=t=>{const m={IR:'利率',FX:'外汇',CREDIT:'信用'};return m[t]||t}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const getProgressColor=p=>p>=90?'#F56C6C':p>=70?'#E6A23C':'#67C23A'
const fetchEntity=async()=>{entityList.value=(await listBusinessUnit({pageSize:1000})).data.list||[]}
const fetchData=async()=>{loading.value=true;try{const res=await listExposure(queryForm);tableData.value=res.data.list||[]}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>fetchData()
const handleReset=()=>{Object.assign(queryForm,{exposureType:'',entityId:''});handleQuery()}
const handleAddLimit=()=>router.push('/risk/limit')
const handleView=t=>router.push(`/risk/exposure/detail?id=${t.id}`)
onMounted(()=>{fetchEntity();fetchData()})
</script>
<style scoped>.exposure-list{}</style>