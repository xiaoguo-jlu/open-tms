<template>
  <el-container class="app-container">
    <!-- 左侧菜单 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">Open-TMS</div>
      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
      >
        <el-sub-menu index="basedata">
          <template #title>
            <span>基础数据</span>
          </template>
          <el-menu-item index="/basedata/business-unit">业务单元</el-menu-item>
          <el-menu-item index="/basedata/trader">交易员</el-menu-item>
          <el-menu-item index="/basedata/currency">币种</el-menu-item>
          <el-menu-item index="/basedata/country">国家/地区</el-menu-item>
          <el-menu-item index="/basedata/bank">银行</el-menu-item>
          <el-menu-item index="/basedata/counterparty">交易对手</el-menu-item>
          <el-menu-item index="/basedata/counterparty-account">对手方账户</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">资金管理系统</div>
        <div class="header-right">
          <span>管理员</span>
          <el-button type="danger" size="small">退出</el-button>
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

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
.app-container { height: 100vh; }
.sidebar { background-color: #304156; }
.logo { height: 50px; line-height: 50px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; border-bottom: 1px solid #1f2d3d; }
.menu { border: none; background-color: #304156; }
.menu .el-menu-item, .menu .el-sub-menu__title { color: #bfcbd9; }
.menu .el-menu-item:hover, .menu .el-sub-menu__title:hover { background-color: #263445; }
.menu .el-menu-item.is-active { background-color: #409eff !important; color: #fff; }
.header { background-color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.header-left { font-size: 16px; font-weight: 500; }
.header-right { display: flex; align-items: center; gap: 15px; }
.main-content { background-color: #f0f2f5; padding: 20px; }
</style>