<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统设置</h2>
        <div class="page-subtitle">当前用户、接口状态和操作日志</div>
      </div>
      <el-button :icon="Refresh" @click="loadLogs">刷新日志</el-button>
    </div>
    <div class="grid-2">
      <div class="panel">
        <el-descriptions title="当前用户" :column="1" border>
          <el-descriptions-item label="账号">{{ auth.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ auth.user?.realName }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ auth.user?.role }}</el-descriptions-item>
          <el-descriptions-item label="认证方式">JWT</el-descriptions-item>
          <el-descriptions-item label="接口文档">/swagger-ui.html</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="panel">
        <div class="toolbar">
          <el-input v-model="query.keyword" placeholder="用户名、模块、操作" clearable @keyup.enter="loadLogs" />
          <el-button type="primary" :icon="Search" @click="loadLogs">查询</el-button>
        </div>
        <el-table :data="logs" height="360">
          <el-table-column prop="username" label="用户" width="100" />
          <el-table-column prop="moduleName" label="模块" width="100" />
          <el-table-column prop="operationType" label="操作" width="100" />
          <el-table-column prop="requestUri" label="地址" min-width="180" show-overflow-tooltip />
          <el-table-column prop="resultStatus" label="结果" width="80" />
          <el-table-column prop="createdAt" label="时间" width="170" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { pageResource } from '@/api/resources'

const auth = useAuthStore()
const logs = ref<any[]>([])
const query = reactive({ current: 1, size: 20, keyword: '' })

async function loadLogs() {
  logs.value = (await pageResource<any>('/operation-logs', query)).records
}

onMounted(() => {
  auth.loadUser()
  loadLogs()
})
</script>
