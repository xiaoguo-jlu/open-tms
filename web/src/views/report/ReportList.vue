<template>
  <div class="report-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="报表名称"><el-input v-model="queryForm.reportName" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="报表类型"><el-select v-model="queryForm.reportType" placeholder="请选择" clearable><el-option label="资金日报" value="DAILY"/><el-option label="资金周报" value="WEEKLY"/><el-option label="资产负债表" value="BALANCE"/><el-option label="损益表" value="PROFIT"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="reportCode" label="报表编码" width="120"/><el-table-column prop="reportName" label="报表名称" min-width="180"/>
        <el-table-column prop="reportType" label="报表类型" width="100"><template #default="{row}"><el-tag>{{getTypeLabel(row.reportType)}}</el-tag></template></el-table-column>
        <el-table-column prop="period" label="报表期间" width="120"/><el-table-column prop="generateTime" label="生成时间" width="180"/>
        <el-table-column label="操作" width="200" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button><el-button type="success" link @click="handleExport(row)">导出</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {listReport,exportReport} from '@/api/report'
import {ElMessage} from 'element-plus'
const loading=ref(false),tableData=ref([])
const queryForm=reactive({reportName:'',reportType:''})
const getTypeLabel=t=>{const m={DAILY:'资金日报',WEEKLY:'资金周报',BALANCE:'资产负债表',PROFIT:'损益表'};return m[t]||t}
const fetchData=async()=>{loading.value=true;try{const res=await listReport(queryForm);tableData.value=res.data.list||[]}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>fetchData()
const handleReset=()=>{Object.assign(queryForm,{reportName:'',reportType:''});handleQuery()}
const handleView=t=>ElMessage.info('查看报表: '+t.reportName)
const handleExport=async t=>{try{await exportReport(t.reportCode,{period:t.period});ElMessage.success('导出成功')}catch(e){console.error(e)}}
onMounted(()=>fetchData())
</script>
<style scoped>.report-list{}</style>