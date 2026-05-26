<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">土壤样本管理</h2>
        <div class="page-subtitle">pH、有机质、速效养分、硒含量、盐分和水分检测记录</div>
      </div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增样本</el-button>
    </div>
    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="样本编码、采样人" style="width: 260px" clearable @keyup.enter="loadData" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" height="560">
        <el-table-column prop="sampleCode" label="样本编码" width="170" />
        <el-table-column prop="plotId" label="地块ID" width="80" />
        <el-table-column prop="sampleDate" label="采样日期" width="120" />
        <el-table-column prop="phValue" label="pH" width="80" />
        <el-table-column prop="organicMatter" label="有机质 g/kg" width="120" />
        <el-table-column prop="availableNitrogen" label="碱解氮" width="95" />
        <el-table-column prop="availablePhosphorus" label="有效磷" width="95" />
        <el-table-column prop="availablePotassium" label="速效钾" width="95" />
        <el-table-column prop="seleniumContent" label="硒 mg/kg" width="100" />
        <el-table-column prop="salinity" label="盐分 g/kg" width="105" />
        <el-table-column prop="electricalConductivity" label="电导率" width="105" />
        <el-table-column prop="heavyMetalRisk" label="重金属" width="90">
          <template #default="{ row }"><el-tag :type="riskTag(row.heavyMetalRisk)">{{ row.heavyMetalRisk }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="110">
          <template #default="{ row }"><el-tag :type="riskTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="soilMoisture" label="含水率 %" width="105" />
        <el-table-column prop="sampler" label="采样人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑土壤样本' : '新增土壤样本'" width="780px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="地块" prop="plotId"><el-select v-model="form.plotId" filterable style="width: 100%"><el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}）`" :value="p.id!" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="样本编码" prop="sampleCode"><el-input v-model="form.sampleCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采样日期" prop="sampleDate"><el-date-picker v-model="form.sampleDate" value-format="YYYY-MM-DD" type="date" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="pH" prop="phValue"><el-input-number v-model="form.phValue" :min="4" :max="10" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有机质 g/kg"><el-input-number v-model="form.organicMatter" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="碱解氮 mg/kg"><el-input-number v-model="form.availableNitrogen" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有效磷 mg/kg"><el-input-number v-model="form.availablePhosphorus" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="速效钾 mg/kg"><el-input-number v-model="form.availablePotassium" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="硒 mg/kg"><el-input-number v-model="form.seleniumContent" :min="0" :precision="3" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="盐分 g/kg"><el-input-number v-model="form.salinity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电导率 dS/m"><el-input-number v-model="form.electricalConductivity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="重金属风险"><el-select v-model="form.heavyMetalRisk" style="width: 100%"><el-option label="低" value="低" /><el-option label="中" value="中" /><el-option label="高" value="高" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="风险等级"><el-select v-model="form.riskLevel" style="width: 100%"><el-option label="适宜" value="适宜" /><el-option label="轻度预警" value="轻度预警" /><el-option label="中度风险" value="中度风险" /><el-option label="高风险" value="高风险" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="含水率 %"><el-input-number v-model="form.soilMoisture" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采样人"><el-input v-model="form.sampler" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createResource, deleteResource, pageResource, updateResource } from '@/api/resources'
import type { FieldPlot, SoilSample } from '@/types'

const rows = ref<SoilSample[]>([])
const plots = ref<FieldPlot[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ current: 1, size: 10, keyword: '' })

function emptyForm(): SoilSample {
  return { plotId: 1, sampleCode: `SOIL-${Date.now()}`, sampleDate: new Date().toISOString().slice(0, 10), phValue: 7.8, organicMatter: 18, availableNitrogen: 80, availablePhosphorus: 18, availablePotassium: 180, seleniumContent: 0.25, salinity: 1.1, electricalConductivity: 1.8, heavyMetalRisk: '低', riskLevel: '适宜', soilMoisture: 18, sampler: '张天山', remark: '' }
}
const form = reactive<SoilSample>(emptyForm())
const rules: FormRules = {
  plotId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  sampleCode: [{ required: true, message: '请输入样本编码', trigger: 'blur' }],
  sampleDate: [{ required: true, message: '请选择采样日期', trigger: 'change' }],
  phValue: [{ required: true, message: '请输入 pH', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<SoilSample>('/soil-samples', query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const plotPage = await pageResource<FieldPlot>('/plots', { current: 1, size: 300 })
  plots.value = plotPage.records
}

function resetQuery() {
  query.keyword = ''
  query.current = 1
  loadData()
}

function openDialog(row?: SoilSample) {
  Object.assign(form, emptyForm(), row || {})
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) await updateResource<SoilSample>('/soil-samples', form.id, form)
    else await createResource<SoilSample>('/soil-samples', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row: SoilSample) {
  await ElMessageBox.confirm(`确认删除样本 ${row.sampleCode}？`, '删除确认', { type: 'warning' })
  await deleteResource('/soil-samples', row.id!)
  ElMessage.success('删除成功')
  loadData()
}

function riskTag(level?: string) {
  if (level === '高风险' || level === '高') return 'danger'
  if (level === '中度风险' || level === '中') return 'warning'
  if (level === '轻度预警') return 'info'
  return 'success'
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>
