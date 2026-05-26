<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">作物种植档案</h2>
        <div class="page-subtitle">种植档案关联地块，表格同步展示土壤硒、pH 和风险等级</div>
      </div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增档案</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="作物、品种、地块或地区" style="width: 260px" clearable @keyup.enter="loadData" />
        <el-select v-model="query.cropType" placeholder="作物类型" clearable style="width: 140px">
          <el-option v-for="c in cropTypes" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="query.plotId" placeholder="所属地块" clearable filterable style="width: 240px">
          <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}）`" :value="p.id!" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" height="560">
        <el-table-column prop="cropType" label="作物类型" width="100" />
        <el-table-column prop="varietyName" label="品种名称" width="140" />
        <el-table-column prop="plotName" label="所属地块" min-width="180" show-overflow-tooltip />
        <el-table-column prop="region" label="地区" width="90" />
        <el-table-column prop="plotSeleniumContent" label="地块硒" width="90" />
        <el-table-column prop="plotPhValue" label="地块 pH" width="90" />
        <el-table-column prop="plotRiskLevel" label="地块风险" width="110">
          <template #default="{ row }"><el-tag :type="riskTag(row.plotRiskLevel)">{{ row.plotRiskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sowingDate" label="播种日期" width="115" />
        <el-table-column prop="harvestDate" label="采收日期" width="115" />
        <el-table-column prop="irrigationMethod" label="灌溉方式" width="140" />
        <el-table-column prop="fertilizerMethod" label="施肥方式" min-width="190" show-overflow-tooltip />
        <el-table-column prop="yieldKgMu" label="产量 kg/亩" width="120" />
        <el-table-column prop="qualityLevel" label="品质等级" width="95" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑种植档案' : '新增种植档案'" width="820px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="所属地块" prop="plotId"><el-select v-model="form.plotId" filterable style="width: 100%"><el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}）`" :value="p.id!" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品种名称" prop="varietyId"><el-select v-model="form.varietyId" filterable style="width: 100%" @change="syncVariety"><el-option v-for="v in varieties" :key="v.id" :label="`${v.cropType} - ${v.varietyName}`" :value="v.id!" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作物类型" prop="cropType"><el-input v-model="form.cropType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品种名称" prop="varietyName"><el-input v-model="form.varietyName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="播种日期" prop="sowingDate"><el-date-picker v-model="form.sowingDate" value-format="YYYY-MM-DD" type="date" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采收日期"><el-date-picker v-model="form.harvestDate" value-format="YYYY-MM-DD" type="date" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="灌溉方式" prop="irrigationMethod"><el-select v-model="form.irrigationMethod" style="width: 100%"><el-option v-for="i in irrigationTypes" :key="i" :label="i" :value="i" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="施肥方式" prop="fertilizerMethod"><el-select v-model="form.fertilizerMethod" style="width: 100%"><el-option v-for="f in fertilizerTypes" :key="f" :label="f" :value="f" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产量 kg/亩"><el-input-number v-model="form.yieldKgMu" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品质等级"><el-select v-model="form.qualityLevel" style="width: 100%"><el-option label="优" value="优" /><el-option label="良" value="良" /><el-option label="中" value="中" /></el-select></el-form-item></el-col>
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
import http from '@/api/http'
import { createResource, deleteResource, pageResource, updateResource } from '@/api/resources'
import type { CropRecord, CropVariety, FieldPlot } from '@/types'

const rows = ref<CropRecord[]>([])
const plots = ref<FieldPlot[]>([])
const varieties = ref<CropVariety[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive<any>({ current: 1, size: 10, keyword: '', cropType: '', plotId: undefined })
const cropTypes = ['棉花', '小麦', '玉米', '葡萄', '枸杞', '番茄']
const irrigationTypes = ['膜下滴灌', '滴灌', '渠灌+滴灌', '水肥一体化滴灌']
const fertilizerTypes = ['有机肥+氮磷钾平衡追肥', '基肥深施+花期追肥', '控氮稳磷增钾', '富硒示范小区叶面补硒']

function emptyForm(): CropRecord {
  return { plotId: 1, varietyId: 1, cropType: '棉花', varietyName: '新陆早78号', seasonYear: 2026, sowingDate: '2026-04-10', harvestDate: '2026-08-20', plantingAreaMu: 80, irrigationMethod: '膜下滴灌', fertilizerMethod: '有机肥+氮磷钾平衡追肥', irrigationMode: '膜下滴灌', fertilizerPlan: '有机肥+氮磷钾平衡追肥', yieldKgMu: 420, qualityLevel: '良', growthStatus: '营养生长期', remark: '' }
}
const form = reactive<CropRecord>(emptyForm())
const rules: FormRules = {
  plotId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  varietyId: [{ required: true, message: '请选择品种', trigger: 'change' }],
  cropType: [{ required: true, message: '请输入作物类型', trigger: 'blur' }],
  varietyName: [{ required: true, message: '请输入品种名称', trigger: 'blur' }],
  sowingDate: [{ required: true, message: '请选择播种日期', trigger: 'change' }],
  irrigationMethod: [{ required: true, message: '请选择灌溉方式', trigger: 'change' }],
  fertilizerMethod: [{ required: true, message: '请选择施肥方式', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<CropRecord>('/crop-records', query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  plots.value = (await pageResource<FieldPlot>('/plots', { current: 1, size: 300 })).records
  varieties.value = await http.get<any, CropVariety[]>('/varieties/all')
}

function syncVariety() {
  const variety = varieties.value.find((item) => item.id === form.varietyId)
  if (variety) {
    form.cropType = variety.cropType
    form.varietyName = variety.varietyName
  }
}

function resetQuery() {
  Object.assign(query, { current: 1, keyword: '', cropType: '', plotId: undefined })
  loadData()
}

function openDialog(row?: CropRecord) {
  Object.assign(form, emptyForm(), row || {})
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    form.seasonYear = Number((form.sowingDate || '2026').slice(0, 4))
    form.irrigationMode = form.irrigationMethod
    form.fertilizerPlan = form.fertilizerMethod
    if (form.id) await updateResource<CropRecord>('/crop-records', form.id, form)
    else await createResource<CropRecord>('/crop-records', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row: CropRecord) {
  await ElMessageBox.confirm(`确认删除 ${row.cropType} - ${row.varietyName} 档案？`, '删除确认', { type: 'warning' })
  await deleteResource('/crop-records', row.id!)
  ElMessage.success('删除成功')
  loadData()
}

function riskTag(level: string) {
  if (level === '高风险') return 'danger'
  if (level === '中度风险') return 'warning'
  if (level === '轻度预警') return 'info'
  return 'success'
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>
