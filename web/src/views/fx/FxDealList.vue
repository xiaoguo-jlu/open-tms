<template>
  <div class="fx-deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="交易编号"><el-input v-model="queryForm.dealNo" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="交易类型"><el-select v-model="queryForm.dealType" placeholder="请选择" clearable><el-option label="即期" value="SPOT"/><el-option label="远期" value="FORWARD"/><el-option label="掉期" value="SWAP"/><el-option label="NDF" value="NDF"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="待审批" value="PENDING"/><el-option label="已审批" value="APPROVED"/><el-option label="已交割" value="SETTLED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="dealNo" label="交易编号" width="160"/>
        <el-table-column prop="dealType" label="交易类型" width="100"><template #default="{row}"><el-tag>{{getTypeLabel(row.dealType)}}</el-tag></template></el-table-column>
        <el-table-column prop="buyCurrency" label="买入币种" width="80"/><el-table-column prop="sellCurrency" label="卖出币种" width="80"/>
        <el-table-column prop="amount" label="金额" align="right" width="150"><template #default="{row}">{{formatAmount(row.amount)}}</template></el-table-column>
        <el-table-column prop="rate" label="汇率" width="120"/><el-table-column prop="valueDate" label="交割日" width="120"/>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.status==='SETTLED'?'success':row.status==='APPROVED'?'warning':'info'">{{getStatusLabel(row.status)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize" :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end;"/>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listFxDeal} from '@/api/fx'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({dealNo:'',dealType:'',status:''})
const pagination=reactive({pageNum:1,pageSize:10,total:0})
const getTypeLabel=t=>{const m={SPOT:'即期',FORWARD:'远期',SWAP:'掉期',NDF:'NDF'};return m[t]||t}
const getStatusLabel=t=>{const m={DRAFT:'草稿',PENDING:'待审批',APPROVED:'已审批',SETTLED:'已交割'};return m[t]||t}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listFxDeal({...queryForm,...pagination});tableData.value=res.data.list||[];pagination.total=res.data.total||0}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>{pagination.pageNum=1;fetchData()}
const handleReset=()=>{Object.assign(queryForm,{dealNo:'',dealType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/fx/edit')
const handleView=t=>router.push(`/fx/detail?id=${t.id}`)
onMounted(()=>fetchData())
</script>
<style scoped>.fx-deal-list{}</style>