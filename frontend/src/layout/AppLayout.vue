<template>
  <el-container class="app-shell">
    <el-aside width="246px" class="app-aside">
      <div class="brand">
        <div class="brand-mark">Se</div>
        <div>
          <div class="brand-title">新疆富硒农业平台</div>
          <div class="brand-subtitle">品质预测与智慧决策</div>
        </div>
      </div>
      <el-menu :default-active="$route.path" router class="nav-menu" background-color="#13352d" text-color="#dbe8e1" active-text-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-title">{{ $route.meta.title }}</div>
          <div class="header-date">系统时间 {{ today }}</div>
        </div>
        <div class="header-actions">
          <el-tag type="success" effect="plain">{{ auth.user?.role || 'ADMIN' }}</el-tag>
          <span class="user-name">{{ auth.user?.realName || auth.user?.username || '管理员' }}</span>
          <el-button :icon="SwitchButton" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  DataBoard,
  Location,
  Notebook,
  Cpu,
  Cloudy,
  Aim,
  TrendCharts,
  Guide,
  Document,
  Connection,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const menus = [
  { path: '/dashboard', title: '首页数据驾驶舱', icon: DataBoard },
  { path: '/plots', title: '地块信息管理', icon: Location },
  { path: '/crop-records', title: '作物种植档案', icon: Notebook },
  { path: '/soil-samples', title: '土壤样本管理', icon: Cpu },
  { path: '/weather', title: '气象数据管理', icon: Cloudy },
  { path: '/prediction', title: '富硒品质预测', icon: Aim },
  { path: '/soil-assessment', title: '土壤环境评估', icon: TrendCharts },
  { path: '/decisions', title: '决策建议中心', icon: Guide },
  { path: '/reports', title: '报告管理', icon: Document },
  { path: '/data-sources', title: '数据来源管理', icon: Connection },
  { path: '/settings', title: '系统设置', icon: Setting }
]

const today = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }))

onMounted(() => {
  auth.loadUser()
})

function handleLogout() {
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f4f7f4;
}

.app-aside {
  background: #13352d;
  box-shadow: 8px 0 24px rgba(19, 53, 45, 0.16);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 76px;
  padding: 0 18px;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #f5b942;
  color: #17352d;
  font-size: 18px;
  font-weight: 800;
}

.brand-title {
  font-size: 17px;
  font-weight: 800;
}

.brand-subtitle {
  margin-top: 4px;
  color: #a9c5b6;
  font-size: 12px;
}

.nav-menu {
  border-right: 0;
  padding: 10px;
}

.nav-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 0;
  border-radius: 8px;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: #2f855a;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e3e9e5;
}

.header-title {
  color: #17352d;
  font-size: 20px;
  font-weight: 800;
}

.header-date {
  margin-top: 5px;
  color: #64748b;
  font-size: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #334155;
  font-weight: 600;
}

.app-main {
  padding: 20px;
}
</style>
