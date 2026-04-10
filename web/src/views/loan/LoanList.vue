<template>
  <div class="loan-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="贷款编号"><el-input v-model="queryForm.dealNo" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="贷款类型"><el-select v-model="queryForm.loanType" placeholder="请选择" clearable><el-option label="短期贷款" value="SHORT"/><el-option label="中长期贷款" value="LONG"/><el-option label="信用贷款" value="CREDIT"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="待审批" value="PENDING"/><el-option label="已审批" value="APPROVED"/><el-option label="已放款" value="DISBURSED"/><el-option label="已还清" value="REPAID"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="dealNo" label="贷款编号" width="160"/>
        <el-table-column prop="loanType" label="贷款类型" width="100"><template #default="{row}"><el-tag>{{getTypeLabel(row.loanType)}}</el-tag></template></el-table-column>
        <el-table-column prop="entityName" label="业务单元" width="120"/>
        <el-table-column prop="lenderName" label="贷款银行" width="120"/>
        <el-table-column prop="amount" label="本金" align="right" width="150"><template #default="{row}">{{formatAmount(row.amount)}}</template></el-table-column>
        <el-table-column prop="interestRate" label="利率" width="100"><template #default="{row}">{{row.interestRate}}%</template></el-table-column>
        <el-table-column prop="maturityDate" label="到期日" width="120"/>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="getStatusType(row.status)">{{getStatusLabel(row.status)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button><el-button type="success" link @click="handleRepay(row)" v-if="row.status==='DISBURSED'">还款</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize" :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end;"/>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {listLoan,repayLoan} from '@/api/loan'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({dealNo:'',loanType:'',status:''})
const pagination=reactive({pageNum:1,pageSize:10,total:0})
const getTypeLabel=t=>{const m={SHORT:'短期贷款',LONG:'中长期贷款',CREDIT:'信用贷款'};return m[t]||t}
const getStatusLabel=t=>{const m={DRAFT:'草稿',PENDING:'待审批',APPROVED:'已审批',DISBURSED:'已放款',REPAID:'已还清'};return m[t]||t}
const getStatusType=t=>{const m={DRAFT:'info',PENDING:'warning',APPROVED:'success',DISBURSED:'success',REPAID:'info'};return m[t]||'info'}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listLoan({...queryForm,...pagination});tableData.value=res.data.list||[];pagination.total=res.data.total||0}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>{pagination.pageNum=1;fetchData()}
const handleReset=()=>{Object.assign(queryForm,{dealNo:'',loanType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/loan/edit')
const handleView=t=>router.push(`/loan/detail?id=${t.id}`)
const handleRepay=async t=>{try{await repayLoan(t.id,{repayAmount:t.amount});ElMessage.success('还款成功');fetchData()}catch(e){console.error(e)}}
onMounted(()=>fetchData())
</script>
<style scoped>.loan-list{}</style>