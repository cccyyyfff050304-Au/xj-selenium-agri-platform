<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">地块信息管理</h2>
        <div class="page-subtitle">地块编号、土壤环境、富硒背景和风险等级统一维护</div>
      </div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增地块</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="地块名称或编号" style="width: 220px" clearable @keyup.enter="loadData" />
        <el-select v-model="query.region" placeholder="所属地区" clearable style="width: 140px">
          <el-option v-for="r in regions" :key="r" :label="r" :value="r" />
        </el-select>
        <el-select v-model="query.soilType" placeholder="土壤类型" clearable style="width: 140px">
          <el-option v-for="s in soilTypes" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable style="width: 140px">
          <el-option v-for="r in riskLevels" :key="r" :label="r" :value="r" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" height="560">
        <el-table-column prop="plotCode" label="地块编号" width="130" />
        <el-table-column prop="plotName" label="地块名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="region" label="地区" width="90" />
        <el-table-column prop="longitude" label="经度" width="105" />
        <el-table-column prop="latitude" label="纬度" width="105" />
        <el-table-column prop="areaMu" label="面积/亩" width="95" />
        <el-table-column prop="soilType" label="土壤类型" width="100" />
        <el-table-column prop="phValue" label="pH" width="70" />
        <el-table-column prop="organicMatter" label="有机质" width="90" />
        <el-table-column prop="seleniumContent" label="土壤硒" width="90" />
        <el-table-column prop="salinity" label="盐分" width="80" />
        <el-table-column prop="electricalConductivity" label="电导率" width="90" />
        <el-table-column prop="heavyMetalRisk" label="重金属" width="90">
          <template #default="{ row }"><el-tag :type="riskTag(row.heavyMetalRisk)">{{ row.heavyMetalRisk }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="110">
          <template #default="{ row }"><el-tag :type="riskTag(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地块' : '新增地块'" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="地块编号" prop="plotCode"><el-input v-model="form.plotCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="地块名称" prop="plotName"><el-input v-model="form.plotName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属地区" prop="region"><el-select v-model="form.region" style="width: 100%"><el-option v-for="r in regions" :key="r" :label="r" :value="r" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="土壤类型" prop="soilType"><el-select v-model="form.soilType" style="width: 100%"><el-option v-for="s in soilTypes" :key="s" :label="s" :value="s" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="经度" prop="longitude"><el-input-number v-model="form.longitude" :precision="6" :min="73" :max="97" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="纬度" prop="latitude"><el-input-number v-model="form.latitude" :precision="6" :min="34" :max="50" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="面积/亩" prop="areaMu"><el-input-number v-model="form.areaMu" :min="1" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="pH" prop="phValue"><el-input-number v-model="form.phValue" :min="4" :max="10" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有机质 g/kg" prop="organicMatter"><el-input-number v-model="form.organicMatter" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="土壤硒 mg/kg" prop="seleniumContent"><el-input-number v-model="form.seleniumContent" :min="0" :precision="3" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="盐分 g/kg" prop="salinity"><el-input-number v-model="form.salinity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电导率 dS/m" prop="electricalConductivity"><el-input-number v-model="form.electricalConductivity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="重金属风险" prop="heavyMetalRisk"><el-select v-model="form.heavyMetalRisk" style="width: 100%"><el-option label="低" value="低" /><el-option label="中" value="中" /><el-option label="高" value="高" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="风险等级" prop="riskLevel"><el-select v-model="form.riskLevel" style="width: 100%"><el-option v-for="r in riskLevels" :key="r" :label="r" :value="r" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="灌溉方式"><el-select v-model="form.irrigationType" style="width: 100%"><el-option v-for="i in irrigationTypes" :key="i" :label="i" :value="i" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.managerName" /></el-form-item></el-col>
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
import type { FieldPlot } from '@/types'

const rows = ref<FieldPlot[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ current: 1, size: 10, keyword: '', region: '', soilType: '', riskLevel: '' })
const regions = ['乌鲁木齐', '昌吉', '石河子', '阿克苏', '喀什', '库尔勒', '吐鲁番', '哈密', '伊犁']
const soilTypes = ['灰漠土', '灌淤土', '棕漠土', '风沙土', '草甸土']
const riskLevels = ['适宜', '轻度预警', '中度风险', '高风险']
const irrigationTypes = ['膜下滴灌', '滴灌', '渠灌+滴灌', '水肥一体化滴灌']

function emptyForm(): FieldPlot {
  return {
    plotCode: '',
    plotName: '',
    region: '昌吉',
    longitude: 87.31,
    latitude: 44.01,
    areaMu: 100,
    soilType: '灌淤土',
    phValue: 7.8,
    organicMatter: 18,
    seleniumContent: 0.25,
    salinity: 1.1,
    electricalConductivity: 1.7,
    heavyMetalRisk: '低',
    riskLevel: '适宜',
    irrigationType: '膜下滴灌',
    managerName: '',
    status: '正常',
    remark: ''
  }
}

const form = reactive<FieldPlot>(emptyForm())
const rules: FormRules = {
  plotCode: [{ required: true, message: '请输入地块编号', trigger: 'blur' }],
  plotName: [{ required: true, message: '请输入地块名称', trigger: 'blur' }],
  region: [{ required: true, message: '请选择所属地区', trigger: 'change' }],
  soilType: [{ required: true, message: '请选择土壤类型', trigger: 'change' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  areaMu: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  phValue: [{ required: true, message: '请输入 pH', trigger: 'blur' }],
  organicMatter: [{ required: true, message: '请输入有机质', trigger: 'blur' }],
  seleniumContent: [{ required: true, message: '请输入土壤硒含量', trigger: 'blur' }],
  salinity: [{ required: true, message: '请输入盐分', trigger: 'blur' }],
  electricalConductivity: [{ required: true, message: '请输入电导率', trigger: 'blur' }],
  heavyMetalRisk: [{ required: true, message: '请选择重金属风险', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<FieldPlot>('/plots', query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { current: 1, keyword: '', region: '', soilType: '', riskLevel: '' })
  loadData()
}

function openDialog(row?: FieldPlot) {
  Object.assign(form, emptyForm(), row || {})
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) await updateResource<FieldPlot>('/plots', form.id, form)
    else await createResource<FieldPlot>('/plots', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row: FieldPlot) {
  await ElMessageBox.confirm(`删除后无法恢复，确认删除 ${row.plotName}？`, '删除确认', { type: 'warning', confirmButtonText: '确认删除' })
  await deleteResource('/plots', row.id!)
  ElMessage.success('删除成功')
  loadData()
}

function riskTag(level: string) {
  if (level === '高风险' || level === '高') return 'danger'
  if (level === '中度风险' || level === '中') return 'warning'
  if (level === '轻度预警') return 'info'
  return 'success'
}

onMounted(loadData)
</script>
