<template>
  <el-container class="app-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">Open-TMS</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>管理驾驶舱</span></el-menu-item>
        <el-sub-menu index="basedata">
          <template #title><el-icon><Grid /></el-icon><span>基础数据</span></template>
          <el-menu-item index="/basedata/subsidiary">子公司管理</el-menu-item>
          <el-menu-item index="/basedata/management-entity">资金管理主体</el-menu-item>
          <el-menu-item index="/basedata/trader">交易员管理</el-menu-item>
          <el-menu-item index="/basedata/currency">币种管理</el-menu-item>
          <el-menu-item index="/basedata/country">国家/地区管理</el-menu-item>
          <el-menu-item index="/basedata/holiday">节假日管理</el-menu-item>
          <el-menu-item index="/basedata/counterparty">交易对手管理</el-menu-item>
          <el-menu-item index="/basedata/counterparty-account">对手方银行账户</el-menu-item>
          <el-menu-item index="/basedata/currency-pair">币种对管理</el-menu-item>
          <el-menu-item index="/basedata/instrument">金融工具管理</el-menu-item>
          <el-menu-item index="/basedata/bank-account">银行账户管理</el-menu-item>
          <el-menu-item index="/basedata/default-bank-account-rule">默认银行账户规则</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="dealing">
          <template #title><el-icon><Wallet /></el-icon><span>交易管理</span></template>
          <el-menu-item index="/dealing/ac-deal">AC 交易 (单边收付)</el-menu-item>
          <el-menu-item index="/dealing/at-deal">AT 转账 (双边划转)</el-menu-item>
          <el-menu-item index="/dealing/fx-deal">FX 外汇交易</el-menu-item>
          <el-menu-item index="/dealing/action">Action 待办</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="approval">
          <template #title><el-icon><CircleCheck /></el-icon><span>审批管理</span></template>
          <el-menu-item index="/approval/rule">审批规则</el-menu-item>
          <el-menu-item index="/approval/template">审批流程配置</el-menu-item>
          <el-menu-item index="/approval/task">审批任务</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="risk">
          <template #title><el-icon><Warning /></el-icon><span>风险管理</span></template>
          <el-menu-item index="/risk/var">VaR报告</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="report">
          <template #title><el-icon><DataLine /></el-icon><span>报表管理</span></template>
          <el-menu-item index="/report/list">报表查询</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system">
          <template #title><el-icon><Document /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/api-docs">接口文档 (Swagger UI)</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">资金管理系统</div>
        <div class="header-user">
          <el-dropdown><span class="user-name">管理员</span>
            <template #dropdown><el-dropdown-menu><el-dropdown-item>退出</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content"><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, Grid, Wallet, CircleCheck, Switch, Money, Warning, DataLine, Document } from '@element-plus/icons-vue'
const route = useRoute()
const activeMenu = computed(() => route.path)
</script>
<style>
*{margin:0;padding:0;box-sizing:border-box}body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}.app-container{height:100vh}.sidebar{background-color:#304156}.logo{height:60px;line-height:60px;text-align:center;color:#fff;font-size:20px;font-weight:bold}.menu{border:none;background-color:#304156}.menu .el-menu-item,.menu .el-sub-menu__title{color:#bfcbd9;font-size:14px}.menu .el-menu-item:hover,.menu .el-sub-menu__title:hover{background-color:#263445}.menu .el-menu-item.is-active{background-color:#409EFF!important;color:#fff}.header{background:#fff;display:flex;justify-content:space-between;align-items:center;padding:0 24px;box-shadow:0 1px 4px rgba(0,0,0,0.08)}.header-title{font-size:16px;font-weight:500;color:#303133}.header-user{display:flex;align-items:center}.user-name{color:#606266;cursor:pointer}.main-content{background:#f0f2f5;padding:20px}
</style>
