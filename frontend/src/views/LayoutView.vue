<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon :size="24"><List /></el-icon>
        <span class="header-title">Task Manager</span>
      </div>
      <div class="header-right">
        <span class="header-user">{{ authStore.username }}</span>
        <el-tag size="small" type="warning">{{ authStore.role }}</el-tag>
        <el-button text @click="handleLogout">Logout</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="layout-aside">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/tasks">
            <el-icon><Check /></el-icon>
            <span>My Tasks</span>
          </el-menu-item>
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="/news">
            <el-icon><Reading /></el-icon>
            <span>Tech News</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container { min-height: 100vh; }
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.header-left { display: flex; align-items: center; gap: 8px; }
.header-title { font-size: 18px; font-weight: 600; color: #303133; }
.header-right { display: flex; align-items: center; gap: 12px; }
.header-user { color: #606266; }
.layout-aside { background: #fff; border-right: 1px solid #e4e7ed; }
.layout-main { background: #f0f2f5; padding: 24px; }
</style>
