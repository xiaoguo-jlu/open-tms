<template>
  <div class="deposit-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="存款编号"><el-input v-model="queryForm.dealNo" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="存款类型"><el-select v-model="queryForm.depositType" placeholder="请选择" clearable><el-option label="定期存款" value="TERM"/><el-option label="活期存款" value="CURRENT"/><el-option label="通知存款" value="NOTICE"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="待审批" value="PENDING"/><el-option label="已审批" value="APPROVED"/><el-option label="已执行" value="EXECUTED"/><el-option label="已到期" value="MATURED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="dealNo" label="存款编号" width="160"/>
        <el-table-column prop="depositType" label="存款类型" width="100"><template #default="{row}"><el-tag>{{getTypeLabel(row.depositType)}}</el-tag></template></el-table-column>
        <el-table-column prop="entityName" label="业务单元" width="120"/>
        <el-table-column prop="counterpartyName" label="存款银行" width="120"/>
        <el-table-column prop="amount" label="金额" align="right" width="150"><template #default="{row}">{{formatAmount(row.amount)}}</template></el-table-column>
        <el-table-column prop="interestRate" label="利率" width="100"><template #default="{row}">{{row.interestRate}}%</template></el-table-column>
        <el-table-column prop="maturityDate" label="到期日" width="120"/>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="getStatusType(row.status)">{{getStatusLabel(row.status)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button><el-button type="primary" link @click="handleEdit(row)" v-if="canEdit(row.status)">编辑</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize" :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end;"/>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {listDeposit} from '@/api/deposit'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({dealNo:'',depositType:'',status:''})
const pagination=reactive({pageNum:1,pageSize:10,total:0})
const getTypeLabel=t=>{const m={TERM:'定期存款',CURRENT:'活期存款',NOTICE:'通知存款'};return m[t]||t}
const getStatusLabel=t=>{const m={DRAFT:'草稿',PENDING:'待审批',APPROVED:'已审批',EXECUTED:'已执行',MATURED:'已到期'};return m[t]||t}
const getStatusType=t=>{const m={DRAFT:'info',PENDING:'warning',APPROVED:'success',EXECUTED:'success',MATURED:'info'};return m[t]||'info'}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const canEdit=t=>['DRAFT','REJECTED'].includes(t)
const fetchData=async()=>{loading.value=true;try{const res=await listDeposit({...queryForm,...pagination});tableData.value=res.data.list||[];pagination.total=res.data.total||0}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>{pagination.pageNum=1;fetchData()}
const handleReset=()=>{Object.assign(queryForm,{dealNo:'',depositType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/deposit/edit')
const handleView=t=>router.push(`/deposit/detail?id=${t.id}`)
const handleEdit=t=>router.push(`/deposit/edit?id=${t.id}`)
onMounted(()=>fetchData())
</script>
<style scoped>.deposit-list{}</style>