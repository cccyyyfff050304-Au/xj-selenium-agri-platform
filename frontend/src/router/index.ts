import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/layout/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true, title: '登录' } },
    {
      path: '/',
      component: AppLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页数据驾驶舱' } },
        { path: 'plots', component: () => import('@/views/PlotManage.vue'), meta: { title: '地块信息管理' } },
        { path: 'crop-records', component: () => import('@/views/CropRecords.vue'), meta: { title: '作物种植档案' } },
        { path: 'soil-samples', component: () => import('@/views/SoilSamples.vue'), meta: { title: '土壤样本管理' } },
        { path: 'weather', component: () => import('@/views/WeatherData.vue'), meta: { title: '气象数据管理' } },
        { path: 'prediction', component: () => import('@/views/Prediction.vue'), meta: { title: '富硒品质预测' } },
        { path: 'soil-assessment', component: () => import('@/views/SoilAssessment.vue'), meta: { title: '土壤环境评估' } },
        { path: 'decisions', component: () => import('@/views/DecisionCenter.vue'), meta: { title: '决策建议中心' } },
        { path: 'reports', component: () => import('@/views/ReportManage.vue'), meta: { title: '报告管理' } },
        { path: 'data-sources', component: () => import('@/views/DataSources.vue'), meta: { title: '数据来源管理' } },
        { path: 'settings', component: () => import('@/views/SystemSettings.vue'), meta: { title: '系统设置' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    return '/login'
  }
  return true
})

export default router
