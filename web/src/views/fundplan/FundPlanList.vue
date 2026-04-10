<template>
  <div class="fund-plan-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="计划名称"><el-input v-model="queryForm.planName" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="计划类型"><el-select v-model="queryForm.planType" placeholder="请选择" clearable><el-option label="收入计划" value="INCOME"/><el-option label="支出计划" value="EXPENSE"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="已提交" value="SUBMITTED"/><el-option label="已审批" value="APPROVED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="planNo" label="计划编号" width="140"/>
        <el-table-column prop="planName" label="计划名称" min-width="150"/>
        <el-table-column prop="planType" label="计划类型" width="100"><template #default="{row}"><el-tag>{{row.planType==='INCOME'?'收入计划':'支出计划'}}</el-tag></template></el-table-column>
        <el-table-column prop="periodType" label="周期" width="80"><template #default="{row}"><el-tag>{{row.periodType==='YEAR'?'年度':'月度'}}</el-tag></template></el-table-column>
        <el-table-column prop="totalAmount" label="计划总额" align="right" width="150"><template #default="{row}">{{formatAmount(row.totalAmount)}}</template></el-table-column>
        <el-table-column prop="usedAmount" label="已执行" align="right" width="150"><template #default="{row}">{{formatAmount(row.usedAmount)}}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.status==='APPROVED'?'success':row.status==='SUBMITTED'?'warning':'info'">{{row.status==='DRAFT'?'草稿':row.status==='SUBMITTED'?'已提交':'已审批'}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleEdit(row)">编辑</el-button><el-button type="primary" link @click="handleView(row)">明细</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize" :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end;"/>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listFundPlan} from '@/api/fundplan'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({planName:'',planType:'',status:''})
const pagination=reactive({pageNum:1,pageSize:10,total:0})
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listFundPlan({...queryForm,...pagination});tableData.value=res.data.list||[];pagination.total=res.data.total||0}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>{pagination.pageNum=1;fetchData()}
const handleReset=()=>{Object.assign(queryForm,{planName:'',planType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/fundplan/edit')
const handleEdit=t=>router.push(`/fundplan/edit?id=${t.id}`)
const handleView=t=>router.push(`/fundplan/detail?id=${t.id}`)
onMounted(()=>fetchData())
</script>
<style scoped>.fund-plan-list{}</style>