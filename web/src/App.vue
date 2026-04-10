<template>
  <el-container class="app-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">Open-TMS</div>
      <el-menu :default-active="activeMenu" router class="menu">
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
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-title">资金管理系统</div>
        <div class="header-user">
          <el-dropdown>
            <span class="user-name">管理员</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Grid } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
.app-container { height: 100vh; }
.sidebar { background-color: #304156; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 20px; font-weight: bold; }
.menu { border: none; background-color: #304156; }
.menu .el-menu-item, .menu .el-sub-menu__title { color: #bfcbd9; font-size: 14px; }
.menu .el-menu-item:hover, .menu .el-sub-menu__title:hover { background-color: #263445; }
.menu .el-menu-item.is-active { background-color: #409EFF !important; color: #fff; }
.header { background: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.header-title { font-size: 16px; font-weight: 500; color: #303133; }
.header-user { display: flex; align-items: center; }
.user-name { color: #606266; cursor: pointer; }
.main-content { background: #f0f2f5; padding: 20px; }
</style>