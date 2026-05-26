<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据来源管理</h2>
        <div class="page-subtitle">公开数据参考、模拟数据生成说明和示范数据记录</div>
      </div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增来源</el-button>
    </div>
    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="来源名称、类型、范围" style="width: 280px" clearable @keyup.enter="loadData" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" height="560">
        <el-table-column prop="sourceName" label="来源名称" min-width="220" />
        <el-table-column prop="sourceType" label="类型" width="120" />
        <el-table-column prop="sourceUrl" label="来源网址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="dataScope" label="数据范围" min-width="200" show-overflow-tooltip />
        <el-table-column prop="collectionTime" label="采集时间" width="170" />
        <el-table-column prop="collectionMethod" label="方式" min-width="150" />
        <el-table-column prop="simulated" label="模拟" width="80">
          <template #default="{ row }"><el-tag :type="row.simulated ? 'warning' : 'success'">{{ row.simulated ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" style="margin-top: 14px; justify-content: flex-end" @change="loadData" />
    </div>
    <el-dialog v-model="dialogVisible" title="数据来源" width="680px">
      <el-form label-width="96px">
        <el-form-item label="来源名称"><el-input v-model="form.sourceName" /></el-form-item>
        <el-form-item label="来源类型"><el-input v-model="form.sourceType" /></el-form-item>
        <el-form-item label="来源链接"><el-input v-model="form.sourceUrl" /></el-form-item>
        <el-form-item label="数据范围"><el-input v-model="form.dataScope" /></el-form-item>
        <el-form-item label="采集时间"><el-date-picker v-model="form.collectionTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="采集方式"><el-input v-model="form.collectionMethod" /></el-form-item>
        <el-form-item label="模拟数据"><el-switch v-model="form.simulated" active-text="是" inactive-text="否" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { createResource, deleteResource, pageResource, updateResource } from '@/api/resources'

const rows = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })
const form = reactive<any>({ sourceName: '', sourceType: '模拟数据', sourceUrl: '', dataScope: '', collectionTime: new Date().toISOString().slice(0, 19), collectionMethod: '人工维护', simulated: true, remark: '模拟数据，仅用于系统演示' })

async function loadData() {
  const data = await pageResource<any>('/data-sources', query)
  rows.value = data.records
  total.value = data.total
}

function openDialog(row?: any) {
  Object.assign(form, { id: undefined, sourceName: '', sourceType: '模拟数据', sourceUrl: '', dataScope: '', collectionTime: new Date().toISOString().slice(0, 19), collectionMethod: '人工维护', simulated: true, remark: '模拟数据，仅用于系统演示' }, row || {})
  dialogVisible.value = true
}

async function save() {
  if (form.id) await updateResource('/data-sources', form.id, form)
  else await createResource('/data-sources', form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除来源 ${row.sourceName}？`, '删除确认', { type: 'warning' })
  await deleteResource('/data-sources', row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
