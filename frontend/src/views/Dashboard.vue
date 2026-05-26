<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">首页数据驾驶舱</h2>
        <div class="page-subtitle">从 MySQL 实时汇总地块、作物、土壤评估、富硒预测和风险预警数据</div>
      </div>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <div class="stat-grid">
      <div v-for="item in cards" :key="item.label" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </div>
    </div>

    <div class="grid-2 dashboard-grid">
      <div class="panel">
        <div ref="cropChartRef" class="chart"></div>
      </div>
      <div class="panel">
        <div ref="seleniumChartRef" class="chart"></div>
      </div>
    </div>

    <div class="grid-2 dashboard-grid">
      <div class="panel">
        <div ref="soilRiskChartRef" class="chart"></div>
      </div>
      <div class="panel">
        <div ref="trendChartRef" class="chart"></div>
      </div>
    </div>

    <div class="grid-2 dashboard-grid">
      <div class="panel">
        <div class="section-title">最新风险预警</div>
        <el-table :data="stats.latestRiskWarnings || []" height="270">
          <el-table-column prop="plotName" label="地块" min-width="150" show-overflow-tooltip />
          <el-table-column prop="region" label="地区" width="90" />
          <el-table-column prop="riskLevel" label="风险等级" width="110">
            <template #default="{ row }"><el-tag :type="riskTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="salinity" label="盐分" width="80" />
          <el-table-column prop="electricalConductivity" label="电导率" width="90" />
        </el-table>
      </div>
      <div class="panel">
        <div class="section-title">推荐种植地块排行</div>
        <el-table :data="stats.recommendedPlotRank || []" height="270">
          <el-table-column prop="plotName" label="地块" min-width="150" show-overflow-tooltip />
          <el-table-column prop="region" label="地区" width="90" />
          <el-table-column prop="seleniumContent" label="土壤硒" width="90" />
          <el-table-column prop="phValue" label="pH" width="75" />
          <el-table-column prop="riskLevel" label="风险等级" width="110">
            <template #default="{ row }"><el-tag :type="riskTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="section-title">最近预测记录</div>
      <el-table :data="stats.recentPredictions || []" height="280">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="cropType" label="作物" width="90" />
        <el-table-column prop="varietyName" label="品种" width="130" show-overflow-tooltip />
        <el-table-column prop="plotId" label="地块ID" width="90" />
        <el-table-column prop="predictedSelenium" label="预测硒含量 mg/kg" width="160" />
        <el-table-column prop="qualityLevel" label="富硒等级" width="110">
          <template #default="{ row }"><el-tag :type="levelTag(row.qualityLevel)">{{ row.qualityLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="confidence" label="可信度" width="100" />
        <el-table-column prop="riskLevel" label="管理建议" width="100">
          <template #default="{ row }"><el-tag :type="priorityTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议" min-width="300" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import http from '@/api/http'

const stats = reactive<any>({
  plotCount: 0,
  cropRecordCount: 0,
  soilSampleCount: 0,
  predictionCount: 0,
  seleniumSuitablePlotCount: 0,
  riskWarningPlotCount: 0,
  cropTypeStats: [],
  seleniumLevelStats: [],
  soilRiskStats: [],
  recentPredictionTrend: [],
  latestRiskWarnings: [],
  recommendedPlotRank: [],
  recentPredictions: []
})

const cropChartRef = ref<HTMLDivElement>()
const seleniumChartRef = ref<HTMLDivElement>()
const soilRiskChartRef = ref<HTMLDivElement>()
const trendChartRef = ref<HTMLDivElement>()
let cropChart: echarts.ECharts | null = null
let seleniumChart: echarts.ECharts | null = null
let soilRiskChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const cards = computed(() => [
  { label: '地块总数', value: stats.plotCount },
  { label: '作物档案总数', value: stats.cropRecordCount },
  { label: '土壤样本总数', value: stats.soilSampleCount },
  { label: '预测次数', value: stats.predictionCount },
  { label: '富硒适宜地块', value: stats.seleniumSuitablePlotCount },
  { label: '风险预警地块', value: stats.riskWarningPlotCount }
])

async function loadData() {
  const data = await http.get<any, any>('/dashboard/stats')
  Object.assign(stats, data)
  await nextTick()
  renderCharts()
}

function renderCharts() {
  renderCropChart()
  renderSeleniumChart()
  renderSoilRiskChart()
  renderTrendChart()
}

function renderCropChart() {
  if (!cropChartRef.value) return
  cropChart = cropChart || echarts.init(cropChartRef.value)
  const rows = stats.cropTypeStats || []
  cropChart.setOption({
    title: { text: '作物类型分布', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['50%', '46%'],
      data: rows.map((item: any) => ({ name: item.cropType, value: Number(item.count) })),
      color: ['#2f855a', '#2563eb', '#b7791f', '#dc2626', '#0891b2', '#7c3aed']
    }]
  })
}

function renderSeleniumChart() {
  if (!seleniumChartRef.value) return
  seleniumChart = seleniumChart || echarts.init(seleniumChartRef.value)
  const rows = stats.seleniumLevelStats || []
  seleniumChart.setOption({
    title: { text: '富硒等级分布', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 54, bottom: 32 },
    xAxis: { type: 'category', data: rows.map((item: any) => item.qualityLevel) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: rows.map((item: any) => Number(item.count)), itemStyle: { color: '#2563eb', borderRadius: [4, 4, 0, 0] } }]
  })
}

function renderSoilRiskChart() {
  if (!soilRiskChartRef.value) return
  soilRiskChart = soilRiskChart || echarts.init(soilRiskChartRef.value)
  const rows = stats.soilRiskStats || []
  soilRiskChart.setOption({
    title: { text: '土壤风险等级分布', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '62%',
      center: ['50%', '46%'],
      data: rows.map((item: any) => ({ name: item.riskLevel, value: Number(item.count) })),
      color: ['#2f855a', '#0891b2', '#b7791f', '#dc2626']
    }]
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  trendChart = trendChart || echarts.init(trendChartRef.value)
  const rows = [...(stats.recentPredictionTrend || [])].reverse()
  trendChart.setOption({
    title: { text: '最近 7 次预测趋势', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 46, right: 18, top: 54, bottom: 34 },
    xAxis: { type: 'category', data: rows.map((item: any) => `${item.cropType}-${item.id}`) },
    yAxis: { type: 'value', name: 'mg/kg' },
    series: [{
      type: 'line',
      smooth: true,
      data: rows.map((item: any) => Number(item.value)),
      symbolSize: 8,
      lineStyle: { color: '#2f855a', width: 3 },
      itemStyle: { color: '#2f855a' },
      areaStyle: { color: 'rgba(47, 133, 90, 0.14)' }
    }]
  })
}

function resizeCharts() {
  cropChart?.resize()
  seleniumChart?.resize()
  soilRiskChart?.resize()
  trendChart?.resize()
}

function riskTag(level: string) {
  if (level === '高风险') return 'danger'
  if (level === '中度风险') return 'warning'
  if (level === '轻度预警') return 'info'
  return 'success'
}

function levelTag(level: string) {
  if (level === '高硒风险') return 'danger'
  if (level === '富硒') return 'success'
  if (level === '普通') return 'info'
  return 'warning'
}

function priorityTag(level: string) {
  if (level === '风险') return 'danger'
  if (level === '谨慎') return 'warning'
  return 'success'
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  cropChart?.dispose()
  seleniumChart?.dispose()
  soilRiskChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.dashboard-grid {
  grid-template-columns: repeat(2, minmax(320px, 1fr));
  margin-bottom: 16px;
}

.section-title {
  margin-bottom: 12px;
  color: #17352d;
  font-size: 16px;
  font-weight: 700;
}
</style>
