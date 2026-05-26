<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">决策建议中心</h2>
        <div class="page-subtitle">根据地块、作物、预测结果和土壤评估结果动态生成推荐、谨慎、风险三类建议</div>
      </div>
      <el-button type="success" :icon="MagicStick" :loading="generating" @click="generate">生成建议</el-button>
    </div>

    <div class="panel decision-control">
      <el-select v-model="form.plotId" filterable placeholder="选择地块" style="width: 300px" @change="handlePlotChange">
        <el-option v-for="p in plots" :key="p.id" :label="`${p.plotName}（${p.region}，${p.riskLevel}）`" :value="p.id!" />
      </el-select>
      <el-select v-model="form.cropRecordId" filterable placeholder="选择种植档案" style="width: 320px">
        <el-option v-for="r in recordsForPlot" :key="r.id" :label="`${r.cropType} - ${r.varietyName}（${r.sowingDate || '未填播期'}）`" :value="r.id!" />
      </el-select>
    </div>

    <div v-if="generated.length" class="suggestion-grid">
      <div v-for="item in generated" :key="item.id || item.title" class="suggestion-card">
        <div class="card-head">
          <div>
            <div class="muted">{{ item.suggestionType }}</div>
            <div class="card-title">{{ item.title }}</div>
          </div>
          <el-tag :type="priorityTag(item.priority)">{{ item.priority }}</el-tag>
        </div>
        <div class="card-content">{{ item.content }}</div>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="建议类型、标题、优先级" style="width: 260px" clearable @keyup.enter="loadData" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" height="520">
        <el-table-column prop="suggestionType" label="类型" width="150" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="priority" label="等级" width="90">
          <template #default="{ row }"><el-tag :type="priorityTag(row.priority)">{{ row.priority }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="plotId" label="地块ID" width="80" />
        <el-table-column prop="cropRecordId" label="档案ID" width="85" />
        <el-table-column prop="content" label="建议内容" min-width="420" show-overflow-tooltip />
        <el-table-column prop="generatedBy" label="生成人" width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="danger" @click="remove(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { deleteResource, pageResource } from '@/api/resources'
import type { CropRecord, FieldPlot } from '@/types'

const rows = ref<any[]>([])
const generated = ref<any[]>([])
const plots = ref<FieldPlot[]>([])
const records = ref<CropRecord[]>([])
const total = ref(0)
const loading = ref(false)
const generating = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })
const form = reactive<any>({ plotId: undefined, cropRecordId: undefined })
const recordsForPlot = computed(() => records.value.filter((item) => item.plotId === form.plotId))

async function loadOptions() {
  const [plotPage, recordPage] = await Promise.all([
    pageResource<FieldPlot>('/plots', { current: 1, size: 500 }),
    pageResource<CropRecord>('/crop-records', { current: 1, size: 500 })
  ])
  plots.value = plotPage.records
  records.value = recordPage.records
  if (!form.plotId && plots.value.length) {
    form.plotId = plots.value[0].id
    handlePlotChange()
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageResource<any>('/decisions', query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handlePlotChange() {
  const record = recordsForPlot.value[0]
  form.cropRecordId = record?.id
}

async function generate() {
  if (!form.plotId || !form.cropRecordId) {
    ElMessage.warning('请选择地块和对应的种植档案')
    return
  }
  generating.value = true
  try {
    const list = await http.post<any, any[]>('/decisions/generate', form)
    generated.value = list
    ElMessage.success(`已生成 ${list.length} 条动态建议`)
    loadData()
  } finally {
    generating.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.current = 1
  loadData()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除建议 ${row.title}？`, '删除确认', { type: 'warning' })
  await deleteResource('/decisions', row.id)
  ElMessage.success('删除成功')
  loadData()
}

function priorityTag(priority: string) {
  if (priority === '风险') return 'danger'
  if (priority === '谨慎') return 'warning'
  return 'success'
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<style scoped>
.decision-control {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.suggestion-card {
  min-height: 170px;
  padding: 14px;
  border: 1px solid #e3e9e5;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.05);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.card-title {
  margin-top: 6px;
  color: #17352d;
  font-weight: 700;
  line-height: 1.35;
}

.card-content {
  margin-top: 12px;
  color: #334155;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .suggestion-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}
</style>
