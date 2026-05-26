<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">报告管理</h2>
        <div class="page-subtitle">生成、预览、打印地块适宜性、土壤评估、富硒预测和综合决策报告</div>
      </div>
      <el-button type="success" :icon="DocumentAdd" @click="openGenerate">生成报告</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-select v-model="query.reportType" placeholder="报告类型" clearable style="width: 220px">
          <el-option v-for="t in reportTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="报告名称、类型" style="width: 260px" clearable @keyup.enter="loadData" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" height="560">
        <el-table-column prop="reportName" label="报告名称" min-width="260" show-overflow-tooltip />
        <el-table-column prop="reportType" label="类型" width="190" />
        <el-table-column prop="relatedPlotId" label="关联地块" width="100" />
        <el-table-column prop="sourceSummary" label="数据来源说明" min-width="260" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="320" show-overflow-tooltip />
        <el-table-column prop="createdBy" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="生成时间" width="170" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="preview(row)">预览</el-button>
            <el-button link type="success" @click="printReport(row)">打印</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" title="生成报告" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="104px">
        <el-form-item label="报告名称" prop="reportName"><el-input v-model="form.reportName" /></el-form-item>
        <el-form-item label="报告类型" prop="reportType">
          <el-select v-model="form.reportType" style="width: 100%">
            <el-option v-for="t in reportTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联地块">
          <el-select v-model="form.relatedPlotId" filterable clearable style="width: 100%">
            <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}，${p.riskLevel}）`" :value="p.id!" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" :loading="generating" @click="generate">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="报告预览" width="900px" class="report-dialog">
      <div class="preview-toolbar">
        <el-button type="success" :icon="Printer" @click="printReport(previewData)">浏览器打印 / 导出 PDF</el-button>
      </div>
      <div class="report-preview" v-html="previewData?.content || ''"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { DocumentAdd, Printer, Refresh, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { deleteResource, pageResource } from '@/api/resources'
import type { FieldPlot } from '@/types'

const rows = ref<any[]>([])
const plots = ref<FieldPlot[]>([])
const previewData = ref<any | null>(null)
const total = ref(0)
const loading = ref(false)
const generating = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const formRef = ref<FormInstance>()
const reportTypes = ['地块富硒种植适宜性报告', '土壤环境综合评估报告', '作物富硒品质预测报告', '智慧农业综合决策报告']
const query = reactive<any>({ current: 1, size: 10, keyword: '', reportType: '' })
const form = reactive<any>({ reportName: '', reportType: reportTypes[3], relatedPlotId: undefined })
const rules: FormRules = {
  reportName: [{ required: true, message: '请输入报告名称', trigger: 'blur' }],
  reportType: [{ required: true, message: '请选择报告类型', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<any>('/reports', query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadPlots() {
  plots.value = (await pageResource<FieldPlot>('/plots', { current: 1, size: 500 })).records
}

function openGenerate() {
  const now = new Date().toISOString().slice(0, 10)
  Object.assign(form, { reportName: `${form.reportType}-${now}`, reportType: reportTypes[3], relatedPlotId: plots.value[0]?.id })
  dialogVisible.value = true
}

async function generate() {
  await formRef.value?.validate()
  generating.value = true
  try {
    const report = await http.post<any, any>('/reports/generate', form)
    ElMessage.success(`已生成：${report.reportName}`)
    dialogVisible.value = false
    previewData.value = report
    previewVisible.value = true
    loadData()
  } finally {
    generating.value = false
  }
}

async function preview(row: any) {
  previewData.value = await http.get<any, any>(`/reports/${row.id}/preview`)
  previewVisible.value = true
}

async function printReport(row: any) {
  const report = row?.content ? row : await http.get<any, any>(`/reports/${row.id}/preview`)
  const win = window.open('', '_blank')
  if (!win) {
    ElMessage.warning('浏览器阻止了打印窗口，请允许弹窗后重试')
    return
  }
  win.document.write(`
    <!doctype html>
    <html>
      <head>
        <title>${report.reportName}</title>
        <meta charset="utf-8" />
        <style>
          body { margin: 0; padding: 32px; color: #1f2937; font-family: "Microsoft YaHei", Arial, sans-serif; line-height: 1.7; }
          h1 { color: #17352d; font-size: 26px; }
          h2 { margin-top: 24px; color: #17352d; font-size: 18px; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; }
          p { margin: 8px 0; }
          @media print { body { padding: 18mm; } }
        </style>
      </head>
      <body>${report.content || ''}</body>
    </html>
  `)
  win.document.close()
  win.focus()
  setTimeout(() => win.print(), 200)
}

function resetQuery() {
  Object.assign(query, { current: 1, keyword: '', reportType: '' })
  loadData()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除报告 ${row.reportName}？`, '删除确认', { type: 'warning' })
  await deleteResource('/reports', row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadPlots()
  loadData()
})
</script>

<style scoped>
.preview-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.report-preview {
  max-height: 68vh;
  overflow: auto;
  padding: 24px 30px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  line-height: 1.8;
}

.report-preview :deep(h1) {
  margin-top: 0;
  color: #17352d;
  font-size: 26px;
}

.report-preview :deep(h2) {
  margin-top: 22px;
  color: #17352d;
  font-size: 18px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 6px;
}
</style>
