<template>
  <div class="irs-deal-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="交易编号"><el-input v-model="queryForm.dealNo" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="IRS类型"><el-select v-model="queryForm.irsType" placeholder="请选择" clearable><el-option label="普通IRS" value="VANILLA"/><el-option label="结构化IRS" value="STRUCTURED"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="待审批" value="PENDING"/><el-option label="已审批" value="APPROVED"/><el-option label="已生效" value="ACTIVE"/><el-option label="已终止" value="TERMINATED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="dealNo" label="交易编号" width="160"/>
        <el-table-column prop="irsType" label="IRS类型" width="100"><template #default="{row}"><el-tag>{{row.irsType==='VANILLA'?'普通IRS':'结构化IRS'}}</el-tag></template></el-table-column>
        <el-table-column prop="notional" label="名义本金" align="right" width="150"><template #default="{row}">{{formatAmount(row.notional)}}</template></el-table-column>
        <el-table-column prop="fixedRate" label="固定利率" width="100"><template #default="{row}">{{row.fixedRate}}%</template></el-table-column>
        <el-table-column prop="floatingRate" label="浮动利率" width="100"><template #default="{row}">{{row.floatingRate}}%</template></el-table-column>
        <el-table-column prop="startDate" label="起息日" width="120"/><el-table-column prop="endDate" label="到期日" width="120"/>
        <el-table-column prop="mktValue" label="市值" align="right" width="150"><template #default="{row}">{{formatAmount(row.mktValue)}}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.status==='ACTIVE'?'success':row.status==='TERMINATED'?'info':'warning'">{{getStatusLabel(row.status)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button><el-button type="primary" link @click="handleValuation(row)">估值</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize" :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end;"/>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listIrsDeal,getIrsValuation} from '@/api/irs'
import {ElMessage} from 'element-plus'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({dealNo:'',irsType:'',status:''})
const pagination=reactive({pageNum:1,pageSize:10,total:0})
const getStatusLabel=t=>{const m={DRAFT:'草稿',PENDING:'待审批',APPROVED:'已审批',ACTIVE:'已生效',TERMINATED:'已终止'};return m[t]||t}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listIrsDeal({...queryForm,...pagination});tableData.value=res.data.list||[];pagination.total=res.data.total||0}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>{pagination.pageNum=1;fetchData()}
const handleReset=()=>{Object.assign(queryForm,{dealNo:'',irsType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/irs/edit')
const handleView=t=>router.push(`/irs/detail?id=${t.id}`)
const handleValuation=async t=>{try{const res=await getIrsValuation(t.id);ElMessage.success('市值: '+formatAmount(res.data.mktValue))}catch(e){console.error(e)}}
onMounted(()=>fetchData())
</script>
<style scoped>.irs-deal-list{}</style>