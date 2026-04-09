<template>
  <div class="todo-page">
    <!-- 统计概览 -->
    <el-row :gutter="12" style="margin-bottom: 12px">
      <el-col :span="4"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ statTotal }}</div><div class="stat-lbl">全部待办</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card warn"><div class="stat-val">{{ statUrgent }}</div><div class="stat-lbl">紧急/特急</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never" class="stat-card warn"><div class="stat-val">{{ statOverdue }}</div><div class="stat-lbl">已超期</div></el-card></el-col>
      <el-col :span="4" v-for="item in statBizItems" :key="item.bizType"><el-card shadow="never" class="stat-card"><div class="stat-val">{{ item.count }}</div><div class="stat-lbl">{{ bizTypeLabel[item.bizType] || item.bizType }}</div></el-card></el-col>
    </el-row>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline style="margin-bottom: 0">
        <el-form-item label="状态">
          <el-select v-model="queryStatus" placeholder="全部" clearable @change="handleSearch" style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryBizType" placeholder="全部" clearable @change="handleSearch" style="width: 150px">
            <el-option v-for="(label, key) in bizTypeLabel" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryPriority" placeholder="全部" clearable @change="handleSearch" style="width: 120px">
            <el-option label="普通" :value="0" />
            <el-option label="紧急" :value="1" />
            <el-option label="特急" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain size="small" :disabled="!selectedIds.length" @click="handleBatchDone">批量处理 ({{ selectedIds.length }})</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待办列表 -->
    <el-card shadow="never" style="margin-top: 12px">
      <el-table :data="tableData" v-loading="loading" stripe border @selection-change="handleSelectionChange" row-key="id">
        <el-table-column type="selection" width="45" :selectable="(row) => row.status === 0" />
        <el-table-column label="优先级" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 2" type="danger" size="small" effect="dark">特急</el-tag>
            <el-tag v-else-if="row.priority === 1" type="warning" size="small">紧急</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="待办标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'todo-overdue': row.overdue, 'todo-done': row.status === 1 }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="bizTypeTagType[row.biz_type || row.bizType] || 'info'">{{ bizTypeLabel[row.biz_type || row.bizType] || row.biz_type || row.bizType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="截止时间" width="160">
          <template #default="{ row }">
            <span v-if="row.deadline" :class="{ 'text-danger': row.overdue }">{{ row.deadline }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="催办" width="60" align="center">
          <template #default="{ row }">
            <el-badge v-if="row.remind_count || row.remindCount" :value="row.remind_count || row.remindCount" type="warning" />
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? (row.overdue ? 'danger' : 'warning') : 'success'" size="small">
              {{ row.status === 0 ? (row.overdue ? '已超期' : '待处理') : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.created_at || row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="处理时间" width="160">
          <template #default="{ row }">{{ row.handled_at || row.handledAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="primary" size="small" @click="handleDone(row)">处理</el-button>
              <el-button link type="warning" size="small" @click="handleRemind(row)">催办</el-button>
            </template>
            <span v-else class="done-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTodoList, getTodoStats, markTodoDone, batchMarkDone, remindTodo } from '@/api/todo'

// 业务类型映射
const bizTypeLabel = {
  approval_contract: '合同审批',
  approval_announcement: '公告审批',
  approval_contract_tpl: '模板审批',
  task_review: '任务审核',
  task_rejected: '任务驳回',
  milestone_approval: '里程碑审批',
  milestone_rejected: '里程碑驳回',
  change_approval: '变更审批',
  other: '其他'
}
const bizTypeTagType = {
  approval_contract: '',
  approval_announcement: 'success',
  task_review: 'warning',
  task_rejected: 'danger',
  milestone_approval: '',
  milestone_rejected: 'danger',
  change_approval: 'warning'
}

// 状态
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const queryStatus = ref(null)
const queryBizType = ref(null)
const queryPriority = ref(null)
const selectedIds = ref([])

// 统计
const statItems = ref([])
const statTotal = computed(() => statItems.value.find(i => i.bizType === '_total')?.count || 0)
const statUrgent = computed(() => statItems.value.find(i => i.bizType === '_urgent')?.count || 0)
const statOverdue = computed(() => statItems.value.find(i => i.bizType === '_overdue')?.count || 0)
const statBizItems = computed(() => statItems.value.filter(i => !i.bizType.startsWith('_')))

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (queryStatus.value !== null && queryStatus.value !== '') params.status = queryStatus.value
    if (queryBizType.value) params.bizType = queryBizType.value
    if (queryPriority.value !== null && queryPriority.value !== '') params.priority = queryPriority.value
    const res = await getTodoList(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await getTodoStats()
    statItems.value = res.data || []
  } catch {}
}

const handleSearch = () => { page.value = 1; fetchData() }
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

const handleDone = async (row) => {
  await ElMessageBox.confirm('确定标记为已处理？', '提示')
  await markTodoDone(row.id)
  ElMessage.success('已处理')
  fetchData()
  fetchStats()
}

const handleBatchDone = async () => {
  await ElMessageBox.confirm(`确定批量处理 ${selectedIds.value.length} 条待办？`, '批量处理', { type: 'warning' })
  await batchMarkDone(selectedIds.value)
  ElMessage.success(`已批量处理 ${selectedIds.value.length} 条`)
  selectedIds.value = []
  fetchData()
  fetchStats()
}

const handleRemind = async (row) => {
  await remindTodo(row.id)
  ElMessage.success('已催办')
  fetchData()
}

onMounted(() => { fetchData(); fetchStats() })
</script>

<style scoped>
.stat-card { text-align: center; padding: 8px 0; }
.stat-card .stat-val { font-size: 22px; font-weight: bold; color: #303133; }
.stat-card .stat-lbl { font-size: 12px; color: #909399; margin-top: 2px; }
.stat-card.warn .stat-val { color: #f56c6c; }
.search-card { margin-bottom: 0; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.done-text { color: #909399; font-size: 13px; }
.todo-overdue { color: #f56c6c; font-weight: 600; }
.todo-done { color: #909399; text-decoration: line-through; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>
