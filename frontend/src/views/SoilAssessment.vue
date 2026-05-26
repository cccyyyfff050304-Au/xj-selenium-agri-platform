<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">土壤环境评估</h2>
        <div class="page-subtitle">综合 pH、有机质、土壤硒、盐分、电导率和重金属风险形成 0-100 分评价</div>
      </div>
      <el-button :icon="Refresh" @click="reload">刷新</el-button>
    </div>

    <div class="grid-2 assessment-grid">
      <div class="panel">
        <el-form label-width="98px">
          <el-form-item label="地块">
            <el-select v-model="form.plotId" filterable style="width: 100%" @change="handlePlotChange">
              <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}，${p.riskLevel}）`" :value="p.id!" />
            </el-select>
          </el-form-item>
          <el-form-item label="土壤样本">
            <el-select v-model="form.soilSampleId" filterable clearable style="width: 100%">
              <el-option v-for="s in samplesForPlot" :key="s.id" :label="`${s.sampleCode} / pH ${s.phValue} / 盐分 ${s.salinity}`" :value="s.id!" />
            </el-select>
          </el-form-item>
          <el-button type="success" :loading="running" style="width: 100%" @click="runAssessment">开始评估并保存结果</el-button>
        </el-form>

        <div v-if="result" class="result-card" style="margin-top: 16px">
          <div class="result-head">
            <div>
              <div class="muted">当前评估结果</div>
              <h3 class="result-title">{{ result.riskLevel }} · {{ result.assessmentScore }} 分</h3>
            </div>
            <el-tag size="large" :type="riskTag(result.riskLevel)">{{ result.riskLevel }}</el-tag>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="酸碱度">{{ result.phLevel }}</el-descriptions-item>
            <el-descriptions-item label="有机质">{{ result.fertilityLevel }}</el-descriptions-item>
            <el-descriptions-item label="硒背景">{{ result.seleniumLevel }}</el-descriptions-item>
            <el-descriptions-item label="盐分">{{ result.salinityLevel }}</el-descriptions-item>
            <el-descriptions-item label="电导率">{{ result.conductivityLevel }}</el-descriptions-item>
            <el-descriptions-item label="限制因子">{{ result.constraintFactor }}</el-descriptions-item>
            <el-descriptions-item label="改良建议">{{ result.improvementAdvice }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="panel chart-panel">
        <div ref="gaugeRef" class="chart compact-chart"></div>
        <div ref="radarRef" class="chart compact-chart"></div>
      </div>
    </div>

    <div class="grid-2 dashboard-grid">
      <div class="panel">
        <div ref="scoreBarRef" class="chart"></div>
      </div>
      <div class="panel">
        <div ref="riskPieRef" class="chart"></div>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="toolbar">
        <el-select v-model="query.plotId" placeholder="地块" filterable clearable style="width: 240px">
          <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}）`" :value="p.id!" />
        </el-select>
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable style="width: 150px">
          <el-option v-for="r in riskLevels" :key="r" :label="r" :value="r" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="等级、限制因子、评价说明" clearable style="width: 240px" @keyup.enter="loadHistory" />
        <el-button type="primary" :icon="Search" @click="loadHistory">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="history" height="420">
        <el-table-column prop="id" label="编号" width="75" />
        <el-table-column prop="plotId" label="地块ID" width="85" />
        <el-table-column prop="assessmentScore" label="综合评分" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="115">
          <template #default="{ row }"><el-tag :type="riskTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="phLevel" label="pH" width="90" />
        <el-table-column prop="fertilityLevel" label="有机质" width="90" />
        <el-table-column prop="seleniumLevel" label="硒背景" width="120" />
        <el-table-column prop="salinityLevel" label="盐分" width="110" />
        <el-table-column prop="constraintFactor" label="限制因子" min-width="220" show-overflow-tooltip />
        <el-table-column prop="improvementAdvice" label="改良建议" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="selectResult(row)">查看</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadHistory" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { deleteResource, pageResource } from '@/api/resources'
import type { FieldPlot, SoilAssessment, SoilSample } from '@/types'

const plots = ref<FieldPlot[]>([])
const samples = ref<SoilSample[]>([])
const history = ref<SoilAssessment[]>([])
const summary = reactive<any>({ riskDistribution: [], plotScores: [], latest: [] })
const result = ref<SoilAssessment | null>(null)
const total = ref(0)
const running = ref(false)
const loading = ref(false)
const form = reactive<any>({ plotId: undefined, soilSampleId: undefined })
const query = reactive<any>({ current: 1, size: 10, keyword: '', plotId: undefined, riskLevel: '' })
const riskLevels = ['适宜', '轻度预警', '中度风险', '高风险']

const gaugeRef = ref<HTMLDivElement>()
const radarRef = ref<HTMLDivElement>()
const scoreBarRef = ref<HTMLDivElement>()
const riskPieRef = ref<HTMLDivElement>()
let gaugeChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null
let scoreBarChart: echarts.ECharts | null = null
let riskPieChart: echarts.ECharts | null = null

const samplesForPlot = computed(() => samples.value.filter((item) => item.plotId === form.plotId))

async function reload() {
  await loadOptions()
  await loadHistory()
  await loadSummary()
}

async function loadOptions() {
  const [plotPage, samplePage] = await Promise.all([
    pageResource<FieldPlot>('/plots', { current: 1, size: 500 }),
    pageResource<SoilSample>('/soil-samples', { current: 1, size: 500 })
  ])
  plots.value = plotPage.records
  samples.value = samplePage.records
  if (!form.plotId && plots.value.length) {
    form.plotId = plots.value[0].id
    handlePlotChange()
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const data = await pageResource<SoilAssessment>('/soil-assessments', query)
    history.value = data.records
    total.value = data.total
    if (!result.value && history.value.length) {
      result.value = history.value[0]
      await nextTick()
      renderCurrentCharts()
    }
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  const data = await http.get<any, any>('/soil-assessments/summary')
  Object.assign(summary, data)
  await nextTick()
  renderSummaryCharts()
}

function handlePlotChange() {
  const sample = samplesForPlot.value[0]
  form.soilSampleId = sample?.id
}

async function runAssessment() {
  if (!form.plotId) {
    ElMessage.warning('请选择地块')
    return
  }
  running.value = true
  try {
    result.value = await http.post<any, SoilAssessment>('/soil-assessments/run', form)
    ElMessage.success('评估完成，结果已保存')
    await loadHistory()
    await loadSummary()
    await nextTick()
    renderCurrentCharts()
  } finally {
    running.value = false
  }
}

function selectResult(row: SoilAssessment) {
  result.value = row
  nextTick(renderCurrentCharts)
}

function renderCurrentCharts() {
  renderGauge()
  renderRadar()
}

function renderSummaryCharts() {
  renderScoreBar()
  renderRiskPie()
}

function renderGauge() {
  if (!gaugeRef.value) return
  gaugeChart = gaugeChart || echarts.init(gaugeRef.value)
  const score = Number(result.value?.assessmentScore || 0)
  gaugeChart.setOption({
    title: { text: '综合评分仪表盘', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    series: [{
      type: 'gauge',
      min: 0,
      max: 100,
      radius: '86%',
      progress: { show: true, width: 12 },
      axisLine: { lineStyle: { width: 12 } },
      detail: { formatter: '{value} 分', fontSize: 22 },
      data: [{ value: score, name: result.value?.riskLevel || '待评估' }]
    }]
  })
}

function renderRadar() {
  if (!radarRef.value) return
  radarChart = radarChart || echarts.init(radarRef.value)
  const values = parseRadar(result.value?.radarJson)
  radarChart.setOption({
    title: { text: '土壤指标雷达图', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: {},
    radar: {
      radius: '62%',
      indicator: [
        { name: 'pH', max: 100 },
        { name: '有机质', max: 100 },
        { name: '土壤硒', max: 100 },
        { name: '盐分', max: 100 },
        { name: '电导率', max: 100 },
        { name: '重金属', max: 100 }
      ]
    },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '单项得分' }],
      areaStyle: { color: 'rgba(47, 133, 90, 0.18)' },
      lineStyle: { color: '#2f855a' },
      itemStyle: { color: '#2f855a' }
    }]
  })
}

function renderScoreBar() {
  if (!scoreBarRef.value) return
  scoreBarChart = scoreBarChart || echarts.init(scoreBarRef.value)
  const rows = summary.plotScores || []
  scoreBarChart.setOption({
    title: { text: '多地块评分排行', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 18, top: 54, bottom: 52 },
    xAxis: { type: 'category', data: rows.map((item: any) => plotName(item.plotId)), axisLabel: { interval: 0, rotate: 30 } },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [{ type: 'bar', data: rows.map((item: any) => Number(item.score)), itemStyle: { color: '#2563eb', borderRadius: [4, 4, 0, 0] } }]
  })
}

function renderRiskPie() {
  if (!riskPieRef.value) return
  riskPieChart = riskPieChart || echarts.init(riskPieRef.value)
  const rows = summary.riskDistribution || []
  riskPieChart.setOption({
    title: { text: '风险等级分布', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['38%', '66%'],
      center: ['50%', '46%'],
      data: rows.map((item: any) => ({ name: item.riskLevel, value: Number(item.count) })),
      color: ['#2f855a', '#0891b2', '#b7791f', '#dc2626']
    }]
  })
}

function parseRadar(raw?: string) {
  try {
    const values = JSON.parse(raw || '[]')
    if (Array.isArray(values) && values.length) return values.map((item) => Number(item))
  } catch {
    return [0, 0, 0, 0, 0, 0]
  }
  return [0, 0, 0, 0, 0, 0]
}

function plotName(plotId: number) {
  const plot = plots.value.find((item) => item.id === Number(plotId))
  return plot?.plotName || `地块${plotId}`
}

function resetQuery() {
  Object.assign(query, { current: 1, keyword: '', plotId: undefined, riskLevel: '' })
  loadHistory()
}

async function remove(row: SoilAssessment) {
  await ElMessageBox.confirm(`确认删除地块 ${row.plotId} 的评估记录？`, '删除确认', { type: 'warning' })
  await deleteResource('/soil-assessments', row.id!)
  ElMessage.success('删除成功')
  result.value = null
  await reload()
}

function riskTag(level: string) {
  if (level === '高风险') return 'danger'
  if (level === '中度风险') return 'warning'
  if (level === '轻度预警') return 'info'
  return 'success'
}

function resizeCharts() {
  gaugeChart?.resize()
  radarChart?.resize()
  scoreBarChart?.resize()
  riskPieChart?.resize()
}

onMounted(async () => {
  await reload()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  gaugeChart?.dispose()
  radarChart?.dispose()
  scoreBarChart?.dispose()
  riskPieChart?.dispose()
})
</script>

<style scoped>
.assessment-grid {
  grid-template-columns: minmax(380px, 480px) 1fr;
}

.chart-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  gap: 12px;
}

.compact-chart {
  height: 330px;
}

.dashboard-grid {
  grid-template-columns: repeat(2, minmax(320px, 1fr));
  margin-top: 16px;
}

.result-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
