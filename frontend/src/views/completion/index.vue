<template>
  <div class="completion-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="竣工验收" name="finish">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="plan_finish_date" label="计划竣工日期" width="130" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="劳务结算" name="labor">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="settlement_no" label="结算编号" width="140" />
            <el-table-column prop="settlement_amount" label="结算金额" width="130" align="right">
              <template #default="{ row }">{{ row.settlement_amount ? Number(row.settlement_amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="案件管理" name="case">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="case_name" label="案件名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="case_type" label="案件类型" width="120" />
            <el-table-column prop="summary" label="案件摘要" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="异常工单" name="exception">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="biz_type" label="业务类型" width="120" />
            <el-table-column prop="fail_reason" label="失败原因" min-width="250" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'warning' : 'success'" size="small">
                  {{ row.status === 1 ? '待处理' : '已处理' }}
                </el-tag>
              </template>
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
import { getCompletionList, getLaborList, getCaseList, getExceptionList } from '@/api/completion'

const loading = ref(false)
const activeTab = ref('finish')
const tableData = ref([])
const total = ref(0)
const page = ref(1)

const apiMap = {
  finish: getCompletionList,
  labor: getLaborList,
  case: getCaseList,
  exception: getExceptionList
}

const fetchData = async () => {
  loading.value = true
  try {
    const fn = apiMap[activeTab.value]
    const res = await fn({ page: page.value, size: 20 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleTabChange = () => { page.value = 1; fetchData() }

onMounted(() => { fetchData() })
</script>
