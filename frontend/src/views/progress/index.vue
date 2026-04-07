<template>
  <div class="progress-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="进度管理" name="gantt">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="task_name" label="任务名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="plan_start_date" label="计划开始" width="110" />
            <el-table-column prop="plan_end_date" label="计划结束" width="110" />
            <el-table-column prop="progress_pct" label="进度(%)" width="100" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="变更管理" name="change">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="change_no" label="变更编号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="change_type" label="变更类型" width="120" />
            <el-table-column prop="description" label="变更说明" min-width="200" show-overflow-tooltip />
            <el-table-column prop="total_amount" label="变更金额" width="130" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGanttTaskList, getChangeOrderList } from '@/api/progress'

const loading = ref(false)
const activeTab = ref('gantt')
const tableData = ref([])
const total = ref(0)
const page = ref(1)

const fetchData = async () => {
  loading.value = true
  try {
    const fn = activeTab.value === 'gantt' ? getGanttTaskList : getChangeOrderList
    const res = await fn({ page: page.value, size: 20 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleTabChange = () => { page.value = 1; fetchData() }

onMounted(() => { fetchData() })
</script>
