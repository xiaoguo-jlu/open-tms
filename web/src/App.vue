<template>
  <el-container class="app-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">Open-TMS</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>管理驾驶舱</span></el-menu-item>
        <el-sub-menu index="basedata">
          <template #title><el-icon><Grid /></el-icon><span>基础数据</span></template>
          <el-menu-item index="/basedata/business-unit">业务单元管理</el-menu-item>
          <el-menu-item index="/basedata/trader">交易员管理</el-menu-item>
          <el-menu-item index="/basedata/currency">币种管理</el-menu-item>
          <el-menu-item index="/basedata/country">国家/地区管理</el-menu-item>
          <el-menu-item index="/basedata/holiday">节假日管理</el-menu-item>
          <el-menu-item index="/basedata/bank">银行信息管理</el-menu-item>
          <el-menu-item index="/basedata/counterparty">交易对手管理</el-menu-item>
          <el-menu-item index="/basedata/counterparty-account">对手方银行账户</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="dealing">
          <template #title><el-icon><Wallet /></el-icon><span>交易管理</span></template>
          <el-menu-item index="/dealing/deal">交易录入</el-menu-item>
          <el-menu-item index="/dealing/bank-account">银行账户管理</el-menu-item>
          <el-menu-item index="/dealing/instrument">金融工具管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="approval">
          <template #title><el-icon><CircleCheck /></el-icon><span>审批管理</span></template>
          <el-menu-item index="/approval/template">审批流程配置</el-menu-item>
          <el-menu-item index="/approval/task">审批任务</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="transfer">
          <template #title><el-icon><Switch /></el-icon><span>转账管理</span></template>
          <el-menu-item index="/transfer/list">转账交易</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ac">
          <template #title><el-icon><Coin /></el-icon><span>现金流管理</span></template>
          <el-menu-item index="/ac/list">AC交易</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="cashpool">
          <template #title><el-icon><Money /></el-icon><span>资金管理</span></template>
          <el-menu-item index="/cashpool/overview">头寸概览</el-menu-item>
          <el-menu-item index="/cashpool/limit">限额管理</el-menu-item>
          <el-menu-item index="/cashpool/pool">现金池管理</el-menu-item>
          <el-menu-item index="/cashpool/auto-rule">自动调拨规则</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="risk">
          <template #title><el-icon><Warning /></el-icon><span>风险管理</span></template>
          <el-menu-item index="/risk/exposure">敞口管理</el-menu-item>
          <el-menu-item index="/risk/var">VaR报告</el-menu-item>
          <el-menu-item index="/risk/hedge">套期保值</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="report">
          <template #title><el-icon><DataLine /></el-icon><span>报表管理</span></template>
          <el-menu-item index="/report/list">报表查询</el-menu-item>
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
import { DataAnalysis, Grid, Wallet, CircleCheck, Switch, Coin, Money, Warning, DataLine } from '@element-plus/icons-vue'
const route = useRoute()
const activeMenu = computed(() => route.path)
</script>
<style>
*{margin:0;padding:0;box-sizing:border-box}body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}.app-container{height:100vh}.sidebar{background-color:#304156}.logo{height:60px;line-height:60px;text-align:center;color:#fff;font-size:20px;font-weight:bold}.menu{border:none;background-color:#304156}.menu .el-menu-item,.menu .el-sub-menu__title{color:#bfcbd9;font-size:14px}.menu .el-menu-item:hover,.menu .el-sub-menu__title:hover{background-color:#263445}.menu .el-menu-item.is-active{background-color:#409EFF!important;color:#fff}.header{background:#fff;display:flex;justify-content:space-between;align-items:center;padding:0 24px;box-shadow:0 1px 4px rgba(0,0,0,0.08)}.header-title{font-size:16px;font-weight:500;color:#303133}.header-user{display:flex;align-items:center}.user-name{color:#606266;cursor:pointer}.main-content{background:#f0f2f5;padding:20px}
</style>