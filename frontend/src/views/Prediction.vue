<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">富硒品质预测</h2>
        <div class="page-subtitle">土壤硒、pH、有机质、盐分、电导率、气象和水肥管理共同驱动可解释预测</div>
      </div>
      <el-button :icon="Refresh" @click="reload">刷新</el-button>
    </div>

    <div class="grid-2 prediction-grid">
      <div class="panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="116px">
          <el-form-item label="地块" prop="plotId">
            <el-select v-model="form.plotId" filterable style="width: 100%" @change="handlePlotChange">
              <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}，Se ${p.seleniumContent}）`" :value="p.id!" />
            </el-select>
          </el-form-item>
          <el-form-item label="种植档案">
            <el-select v-model="form.cropRecordId" filterable clearable style="width: 100%" @change="handleRecordChange">
              <el-option v-for="r in recordsForPlot" :key="r.id" :label="`${r.cropType} - ${r.varietyName}（${r.sowingDate || '未填播期'}）`" :value="r.id!" />
            </el-select>
          </el-form-item>
          <el-form-item label="土壤样本">
            <el-select v-model="form.soilSampleId" filterable clearable style="width: 100%" @change="handleSampleChange">
              <el-option v-for="s in samplesForPlot" :key="s.id" :label="`${s.sampleCode} / pH ${s.phValue} / Se ${s.seleniumContent}`" :value="s.id!" />
            </el-select>
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="作物类型" prop="cropType"><el-select v-model="form.cropType" style="width: 100%"><el-option v-for="c in cropTypes" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="品种名称" prop="varietyName"><el-input v-model="form.varietyName" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="土壤硒 mg/kg" prop="soilSelenium"><el-input-number v-model="form.soilSelenium" :min="0" :precision="3" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="pH" prop="phValue"><el-input-number v-model="form.phValue" :min="4" :max="10" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="有机质 g/kg" prop="organicMatter"><el-input-number v-model="form.organicMatter" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="盐分 g/kg" prop="salinity"><el-input-number v-model="form.salinity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="电导率 dS/m" prop="electricalConductivity"><el-input-number v-model="form.electricalConductivity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="平均温度 ℃" prop="avgTemperature"><el-input-number v-model="form.avgTemperature" :min="-20" :max="45" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="降水量 mm" prop="precipitation"><el-input-number v-model="form.precipitation" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="灌溉方式" prop="irrigationMethod"><el-select v-model="form.irrigationMethod" style="width: 100%"><el-option v-for="i in irrigationTypes" :key="i" :label="i" :value="i" /></el-select></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="施肥方式" prop="fertilizerMethod"><el-select v-model="form.fertilizerMethod" style="width: 100%"><el-option v-for="f in fertilizerTypes" :key="f" :label="f" :value="f" /></el-select></el-form-item></el-col>
          </el-row>
          <el-button type="success" :loading="running" style="width: 100%" @click="runPrediction">提交预测并保存记录</el-button>
        </el-form>
      </div>

      <div class="panel">
        <div v-if="result" class="result-card">
          <div class="result-head">
            <div>
              <div class="muted">本次预测结果</div>
              <h3 class="result-title">{{ result.qualityLevel }} · {{ result.riskLevel }}</h3>
            </div>
            <el-tag size="large" :type="levelTag(result.qualityLevel)">{{ result.predictedSelenium }} mg/kg</el-tag>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="作物品种">{{ result.cropType }} / {{ result.varietyName }}</el-descriptions-item>
            <el-descriptions-item label="可信度">{{ percent(result.confidence) }}</el-descriptions-item>
            <el-descriptions-item label="模型版本">{{ result.modelVersion }}</el-descriptions-item>
            <el-descriptions-item label="影响因素">{{ result.factorExplanation }}</el-descriptions-item>
            <el-descriptions-item label="种植建议">{{ result.suggestion }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="选择地块和作物后提交预测" :image-size="120" />
        <div ref="factorChartRef" class="chart factor-chart"></div>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="toolbar">
        <el-select v-model="query.cropType" placeholder="作物类型" clearable style="width: 140px">
          <el-option v-for="c in cropTypes" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="query.plotId" placeholder="地块" filterable clearable style="width: 240px">
          <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}）`" :value="p.id!" />
        </el-select>
        <el-select v-model="query.qualityLevel" placeholder="富硒等级" clearable style="width: 140px">
          <el-option v-for="l in qualityLevels" :key="l" :label="l" :value="l" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="品种、等级、创建人" clearable style="width: 220px" @keyup.enter="loadHistory" />
        <el-button type="primary" :icon="Search" @click="loadHistory">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="history" height="440">
        <el-table-column prop="id" label="编号" width="75" />
        <el-table-column prop="cropType" label="作物" width="90" />
        <el-table-column prop="varietyName" label="品种" width="130" show-overflow-tooltip />
        <el-table-column prop="plotId" label="地块ID" width="80" />
        <el-table-column prop="soilSelenium" label="土壤硒" width="90" />
        <el-table-column prop="phValue" label="pH" width="75" />
        <el-table-column prop="predictedSelenium" label="预测硒" width="95" />
        <el-table-column prop="qualityLevel" label="等级" width="100">
          <template #default="{ row }"><el-tag :type="levelTag(row.qualityLevel)">{{ row.qualityLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="confidence" label="可信度" width="90">
          <template #default="{ row }">{{ percent(row.confidence) }}</template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadHistory" />
    </div>

    <el-dialog v-model="detailVisible" title="预测详情" width="780px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="作物">{{ detail.cropType }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ detail.varietyName }}</el-descriptions-item>
        <el-descriptions-item label="土壤硒">{{ detail.soilSelenium }} mg/kg</el-descriptions-item>
        <el-descriptions-item label="pH">{{ detail.phValue }}</el-descriptions-item>
        <el-descriptions-item label="有机质">{{ detail.organicMatter }} g/kg</el-descriptions-item>
        <el-descriptions-item label="盐分">{{ detail.salinity }} g/kg</el-descriptions-item>
        <el-descriptions-item label="电导率">{{ detail.electricalConductivity }} dS/m</el-descriptions-item>
        <el-descriptions-item label="气象">{{ detail.avgTemperature }} ℃ / {{ detail.precipitation }} mm</el-descriptions-item>
        <el-descriptions-item label="等级">{{ detail.qualityLevel }}</el-descriptions-item>
        <el-descriptions-item label="可信度">{{ percent(detail.confidence) }}</el-descriptions-item>
        <el-descriptions-item label="解释" :span="2">{{ detail.factorExplanation }}</el-descriptions-item>
        <el-descriptions-item label="建议" :span="2">{{ detail.suggestion }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { deleteResource, pageResource } from '@/api/resources'
import type { CropRecord, FieldPlot, SeleniumPrediction, SoilSample } from '@/types'

const plots = ref<FieldPlot[]>([])
const records = ref<CropRecord[]>([])
const samples = ref<SoilSample[]>([])
const history = ref<SeleniumPrediction[]>([])
const result = ref<SeleniumPrediction | null>(null)
const detail = ref<SeleniumPrediction | null>(null)
const total = ref(0)
const running = ref(false)
const loading = ref(false)
const detailVisible = ref(false)
const formRef = ref<FormInstance>()
const factorChartRef = ref<HTMLDivElement>()
let factorChart: echarts.ECharts | null = null

const cropTypes = ['棉花', '小麦', '玉米', '葡萄', '枸杞', '番茄']
const qualityLevels = ['低硒', '普通', '富硒', '高硒风险']
const irrigationTypes = ['膜下滴灌', '滴灌', '渠灌+滴灌', '水肥一体化滴灌']
const fertilizerTypes = ['有机肥+氮磷钾平衡追肥', '基肥深施+花期追肥', '控氮稳磷增钾', '富硒示范小区叶面补硒']

const form = reactive<any>({
  plotId: undefined,
  cropRecordId: undefined,
  soilSampleId: undefined,
  cropType: '棉花',
  varietyName: '',
  soilSelenium: 0.22,
  phValue: 7.8,
  organicMatter: 18,
  salinity: 1.1,
  electricalConductivity: 1.8,
  avgTemperature: 23,
  precipitation: 5,
  irrigationMethod: '膜下滴灌',
  fertilizerMethod: '有机肥+氮磷钾平衡追肥'
})
const query = reactive<any>({ current: 1, size: 10, keyword: '', cropType: '', plotId: undefined, qualityLevel: '' })
const rules: FormRules = {
  plotId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  cropType: [{ required: true, message: '请选择作物类型', trigger: 'change' }],
  varietyName: [{ required: true, message: '请输入品种名称', trigger: 'blur' }],
  soilSelenium: [{ required: true, message: '请输入土壤硒含量', trigger: 'blur' }],
  phValue: [{ required: true, message: '请输入 pH', trigger: 'blur' }],
  organicMatter: [{ required: true, message: '请输入有机质', trigger: 'blur' }],
  salinity: [{ required: true, message: '请输入盐分', trigger: 'blur' }],
  electricalConductivity: [{ required: true, message: '请输入电导率', trigger: 'blur' }],
  avgTemperature: [{ required: true, message: '请输入平均温度', trigger: 'blur' }],
  precipitation: [{ required: true, message: '请输入降水量', trigger: 'blur' }],
  irrigationMethod: [{ required: true, message: '请选择灌溉方式', trigger: 'change' }],
  fertilizerMethod: [{ required: true, message: '请选择施肥方式', trigger: 'change' }]
}

const recordsForPlot = computed(() => records.value.filter((item) => item.plotId === form.plotId))
const samplesForPlot = computed(() => samples.value.filter((item) => item.plotId === form.plotId))

async function reload() {
  await loadOptions()
  await loadHistory()
}

async function loadOptions() {
  const [plotPage, recordPage, samplePage] = await Promise.all([
    pageResource<FieldPlot>('/plots', { current: 1, size: 500 }),
    pageResource<CropRecord>('/crop-records', { current: 1, size: 500 }),
    pageResource<SoilSample>('/soil-samples', { current: 1, size: 500 })
  ])
  plots.value = plotPage.records
  records.value = recordPage.records
  samples.value = samplePage.records
  if (!form.plotId && plots.value.length) {
    form.plotId = plots.value[0].id
    handlePlotChange()
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const data = await pageResource<SeleniumPrediction>('/predictions', query)
    history.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handlePlotChange() {
  const plot = plots.value.find((item) => item.id === form.plotId)
  if (plot) {
    form.soilSelenium = Number(plot.seleniumContent)
    form.phValue = Number(plot.phValue)
    form.organicMatter = Number(plot.organicMatter)
    form.salinity = Number(plot.salinity)
    form.electricalConductivity = Number(plot.electricalConductivity)
    form.irrigationMethod = plot.irrigationType || form.irrigationMethod
  }
  const record = recordsForPlot.value[0]
  if (record) {
    form.cropRecordId = record.id
    applyRecord(record)
  }
  const sample = samplesForPlot.value[0]
  if (sample) {
    form.soilSampleId = sample.id
    applySample(sample)
  }
}

function handleRecordChange() {
  const record = records.value.find((item) => item.id === form.cropRecordId)
  if (record) applyRecord(record)
}

function handleSampleChange() {
  const sample = samples.value.find((item) => item.id === form.soilSampleId)
  if (sample) applySample(sample)
}

function applyRecord(record: CropRecord) {
  form.cropType = record.cropType || form.cropType
  form.varietyName = record.varietyName || form.varietyName
  form.irrigationMethod = record.irrigationMethod || form.irrigationMethod
  form.fertilizerMethod = record.fertilizerMethod || form.fertilizerMethod
}

function applySample(sample: SoilSample) {
  form.soilSelenium = Number(sample.seleniumContent ?? form.soilSelenium)
  form.phValue = Number(sample.phValue ?? form.phValue)
  form.organicMatter = Number(sample.organicMatter ?? form.organicMatter)
  form.salinity = Number(sample.salinity ?? form.salinity)
  form.electricalConductivity = Number(sample.electricalConductivity ?? form.electricalConductivity)
}

async function runPrediction() {
  await formRef.value?.validate()
  running.value = true
  try {
    result.value = await http.post<any, SeleniumPrediction>('/predictions/run', form)
    ElMessage.success('预测完成，结果已保存')
    await loadHistory()
    await nextTick()
    renderFactorChart(result.value)
  } finally {
    running.value = false
  }
}

function renderFactorChart(row: SeleniumPrediction | null) {
  if (!factorChartRef.value || !row) return
  factorChart = factorChart || echarts.init(factorChartRef.value)
  const factors = parseFactors(row.factorContribution)
  factorChart.setOption({
    title: { text: '影响因子贡献', left: 0, textStyle: { fontSize: 16, color: '#17352d' } },
    tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
    grid: { left: 80, right: 20, top: 56, bottom: 28 },
    xAxis: { type: 'value', max: 60, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: factors.map((item) => item.factor) },
    series: [{ type: 'bar', data: factors.map((item) => item.contribution), itemStyle: { color: '#2f855a', borderRadius: [0, 4, 4, 0] } }]
  })
}

function parseFactors(raw?: string) {
  try {
    const parsed = JSON.parse(raw || '[]')
    if (Array.isArray(parsed)) {
      return parsed.map((item) => ({ factor: item.factor, contribution: Number(item.contribution) }))
    }
  } catch {
    return []
  }
  return []
}

function resetQuery() {
  Object.assign(query, { current: 1, keyword: '', cropType: '', plotId: undefined, qualityLevel: '' })
  loadHistory()
}

function openDetail(row: SeleniumPrediction) {
  detail.value = row
  detailVisible.value = true
}

async function remove(row: SeleniumPrediction) {
  await ElMessageBox.confirm(`确认删除 ${row.cropType} - ${row.varietyName} 的预测记录？`, '删除确认', { type: 'warning' })
  await deleteResource('/predictions', row.id!)
  ElMessage.success('删除成功')
  loadHistory()
}

function percent(value?: number) {
  if (value === undefined || value === null) return '-'
  const num = Number(value)
  return num <= 1 ? `${Math.round(num * 100)}%` : `${num}%`
}

function levelTag(level: string) {
  if (level === '高硒风险') return 'danger'
  if (level === '富硒') return 'success'
  if (level === '普通') return 'info'
  return 'warning'
}

function resizeChart() {
  factorChart?.resize()
}

onMounted(async () => {
  await reload()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  factorChart?.dispose()
})
</script>

<style scoped>
.prediction-grid {
  grid-template-columns: minmax(420px, 520px) 1fr;
}

.result-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.factor-chart {
  height: 310px;
  margin-top: 16px;
}
</style>
