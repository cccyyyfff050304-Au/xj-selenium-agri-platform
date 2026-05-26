<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">气象数据管理</h2>
        <div class="page-subtitle">温度、降水、日照、太阳辐射、风速和湿度监测</div>
      </div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增气象记录</el-button>
    </div>
    <div class="grid-2">
      <div class="panel">
        <div class="toolbar">
          <el-input v-model="query.keyword" placeholder="地区" clearable @keyup.enter="loadData" />
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </div>
        <div ref="chartRef" class="chart"></div>
      </div>
      <div class="panel">
        <el-table v-loading="loading" :data="rows" height="420">
          <el-table-column prop="region" label="地区" width="90" />
          <el-table-column prop="recordDate" label="日期" width="110" />
          <el-table-column prop="avgTemp" label="均温" width="80" />
          <el-table-column prop="precipitation" label="降水" width="80" />
          <el-table-column prop="sunshineHours" label="日照" width="80" />
          <el-table-column prop="solarRadiation" label="辐射" width="80" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑气象记录' : '新增气象记录'" width="640px">
      <el-form :model="form" label-width="104px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="地区"><el-select v-model="form.region" style="width: 100%"><el-option v-for="r in regions" :key="r" :label="r" :value="r" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="日期"><el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="平均气温"><el-input-number v-model="form.avgTemp" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最高气温"><el-input-number v-model="form.maxTemp" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最低气温"><el-input-number v-model="form.minTemp" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="降水 mm"><el-input-number v-model="form.precipitation" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="日照 h"><el-input-number v-model="form.sunshineHours" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="辐射 MJ/m2"><el-input-number v-model="form.solarRadiation" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="风速 m/s"><el-input-number v-model="form.windSpeed" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度 %"><el-input-number v-model="form.humidity" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { createResource, pageResource, updateResource } from '@/api/resources'
import type { WeatherRecord } from '@/types'

const rows = ref<WeatherRecord[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
const query = reactive({ current: 1, size: 10, keyword: '' })
const regions = ['乌鲁木齐', '昌吉', '石河子', '阿克苏', '喀什', '库尔勒', '吐鲁番', '哈密', '伊犁']
const form = reactive<WeatherRecord>({ region: '昌吉', recordDate: new Date().toISOString().slice(0, 10), avgTemp: 18, maxTemp: 26, minTemp: 10, precipitation: 1.2, sunshineHours: 8, solarRadiation: 18, windSpeed: 2.1, humidity: 42 })

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<WeatherRecord>('/weather-records', query)
    rows.value = data.records
    total.value = data.total
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value) return
  chart = chart || echarts.init(chartRef.value)
  const ordered = [...rows.value].reverse()
  chart.setOption({
    title: { text: '气象趋势', textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'axis' },
    legend: { top: 28, data: ['平均气温', '降水'] },
    grid: { left: 38, right: 24, top: 72, bottom: 32 },
    xAxis: { type: 'category', data: ordered.map((item) => `${item.region}-${item.recordDate}`) },
    yAxis: [{ type: 'value' }],
    series: [
      { name: '平均气温', type: 'line', smooth: true, data: ordered.map((item) => item.avgTemp), color: '#dc2626' },
      { name: '降水', type: 'bar', data: ordered.map((item) => item.precipitation), color: '#2563eb' }
    ]
  })
}

function openDialog(row?: WeatherRecord) {
  Object.assign(form, { region: '昌吉', recordDate: new Date().toISOString().slice(0, 10), avgTemp: 18, maxTemp: 26, minTemp: 10, precipitation: 1.2, sunshineHours: 8, solarRadiation: 18, windSpeed: 2.1, humidity: 42 }, row || {})
  dialogVisible.value = true
}

async function save() {
  if (form.id) await updateResource<WeatherRecord>('/weather-records', form.id, form)
  else await createResource<WeatherRecord>('/weather-records', form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => chart?.resize())
})
onBeforeUnmount(() => chart?.dispose())
</script>
