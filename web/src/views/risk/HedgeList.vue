<template>
  <div class="hedge-list">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="套保编号"><el-input v-model="queryForm.hedgeNo" placeholder="请输入" clearable/></el-form-item>
        <el-form-item label="套保类型"><el-select v-model="queryForm.hedgeType" placeholder="请选择" clearable><el-option label="利率套保" value="IR_HEDGE"/><el-option label="外汇套保" value="FX_HEDGE"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryForm.status" placeholder="请选择" clearable><el-option label="草稿" value="DRAFT"/><el-option label="待审批" value="PENDING"/><el-option label="已生效" value="ACTIVE"/><el-option label="已到期" value="EXPIRED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="success" @click="handleAdd">新增</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center"/>
        <el-table-column prop="hedgeNo" label="套保编号" width="160"/>
        <el-table-column prop="hedgeType" label="套保类型" width="100"><template #default="{row}"><el-tag>{{row.hedgeType==='IR_HEDGE'?'利率':'外汇'}}</el-tag></template></el-table-column>
        <el-table-column prop="hedgeRatio" label="套保比率" width="100"><template #default="{row}">{{row.hedgeRatio}}%</template></el-table-column>
        <el-table-column prop="hedgeAmount" label="套保金额" align="right" width="150"><template #default="{row}">{{formatAmount(row.hedgeAmount)}}</template></el-table-column>
        <el-table-column prop="cost" label="套保成本" align="right" width="120"><template #default="{row}">{{formatAmount(row.cost)}}</template></el-table-column>
        <el-table-column prop="effectiveness" label="有效性" width="120"><template #default="{row}"><el-progress :percentage="row.effectiveness" :color="row.effectiveness>=80?'#67C23A':'#E6A23C'"/></template></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.status==='ACTIVE'?'success':row.status==='EXPIRED'?'info':'warning'">{{getStatusLabel(row.status)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button type="primary" link @click="handleView(row)">查看</el-button><el-button type="success" link @click="handleEffect(row)">效果</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import {ref,reactive,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {listHedge} from '@/api/risk/hedge'
import {ElMessage} from 'element-plus'
const router=useRouter()
const loading=ref(false),tableData=ref([])
const queryForm=reactive({hedgeNo:'',hedgeType:'',status:''})
const getStatusLabel=t=>{const m={DRAFT:'草稿',PENDING:'待审批',ACTIVE:'已生效',EXPIRED:'已到期'};return m[t]||t}
const formatAmount=a=>a?new Intl.NumberFormat('zh-CN',{minimumFractionDigits:2}).format(a):'-'
const fetchData=async()=>{loading.value=true;try{const res=await listHedge(queryForm);tableData.value=res.data.list||[]}catch(e){console.error(e)}finally{loading.value=false}}
const handleQuery=()=>fetchData()
const handleReset=()=>{Object.assign(queryForm,{hedgeNo:'',hedgeType:'',status:''});handleQuery()}
const handleAdd=()=>router.push('/risk/hedge/edit')
const handleView=t=>router.push(`/risk/hedge/detail?id=${t.id}`)
const handleEffect=async t=>{try{const res=await t.id;ElMessage.success('套保效果: '+res)}catch(e){console.error(e)}}
onMounted(()=>fetchData())
</script>
<style scoped>.hedge-list{}</style>