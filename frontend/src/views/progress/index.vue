<template>
  <div class="progress-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 进度管理 ==================== -->
        <el-tab-pane label="进度管理" name="gantt">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddGantt">新建任务</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="task_name" label="任务名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="task_type" label="类型" width="80">
              <template #default="{ row }">{{ row.task_type === 1 ? '里程碑' : '任务' }}</template>
            </el-table-column>
            <el-table-column prop="plan_start_date" label="计划开始" width="110" />
            <el-table-column prop="plan_end_date" label="计划结束" width="110" />
            <el-table-column prop="progress_pct" label="进度(%)" width="100" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditGantt(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteGantt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 变更管理 ==================== -->
        <el-tab-pane label="变更管理" name="change">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddChange">新建变更单</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="change_no" label="变更编号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="change_type" label="变更类型" width="120" />
            <el-table-column prop="title" label="变更标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="total_amount" label="变更金额" width="130" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditChange(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteChange(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>

    <!-- ==================== 甘特任务对话框 ==================== -->
    <el-dialog v-model="ganttDialogVisible" :title="isEdit ? '编辑任务' : '新建任务'" width="700px" @closed="resetGanttForm">
      <el-form ref="ganttFormRef" :model="ganttForm" :rules="ganttRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="ganttForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="ganttForm.taskType" style="width: 100%">
                <el-option label="里程碑" :value="1" />
                <el-option label="任务" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="ganttForm.taskName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划开始">
              <el-date-picker v-model="ganttForm.planStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束">
              <el-date-picker v-model="ganttForm.planEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际开始">
              <el-date-picker v-model="ganttForm.actualStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际结束">
              <el-date-picker v-model="ganttForm.actualEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="进度(%)">
              <el-input-number v-model="ganttForm.progressPct" :min="0" :max="100" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="父任务ID">
              <el-input-number v-model="ganttForm.parentId" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="ganttForm.sortOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="ganttDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitGantt">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 变更单对话框 ==================== -->
    <el-dialog v-model="changeDialogVisible" :title="isEdit ? '编辑变更单' : '新建变更单'" width="850px" @closed="resetChangeForm">
      <el-form ref="changeFormRef" :model="changeForm" :rules="changeRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="changeForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更类型" prop="changeType">
              <el-select v-model="changeForm.changeType" style="width: 100%">
                <el-option label="签证" value="visa" />
                <el-option label="业主变更" value="owner_change" />
                <el-option label="超量" value="overage" />
                <el-option label="劳务签证" value="labor_visa" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="变更标题" prop="title">
          <el-input v-model="changeForm.title" />
        </el-form-item>
        <el-form-item label="变更说明">
          <el-input v-model="changeForm.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider content-position="left">变更明细</el-divider>
        <el-button type="primary" size="small" style="margin-bottom: 12px" @click="addChangeDetail">添加明细行</el-button>
        <el-table :data="changeForm.details" border size="small">
          <el-table-column label="项目名称" min-width="120">
            <template #default="{ row }"><el-input v-model="row.itemName" size="small" /></template>
          </el-table-column>
          <el-table-column label="规格型号" width="100">
            <template #default="{ row }"><el-input v-model="row.specModel" size="small" /></template>
          </el-table-column>
          <el-table-column label="单位" width="70">
            <template #default="{ row }"><el-input v-model="row.unit" size="small" /></template>
          </el-table-column>
          <el-table-column label="计划量" width="90">
            <template #default="{ row }"><el-input-number v-model="row.planQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="实际量" width="90">
            <template #default="{ row }"><el-input-number v-model="row.actualQuantity" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" controls-position="right" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">{{ (((row.actualQuantity || 0) - (row.planQuantity || 0)) * (row.unitPrice || 0)).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="changeForm.details.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="changeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitChange">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getGanttTaskList, createGanttTask, updateGanttTask, deleteGanttTask,
  getChangeOrderList, createChangeOrder, updateChangeOrder, deleteChangeOrder
} from '@/api/progress'
import { getAllProjects } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('gantt')
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const projects = ref([])
const isEdit = ref(false)
const editId = ref(null)

// ====== 甘特任务 ======
const ganttDialogVisible = ref(false)
const ganttFormRef = ref(null)
const ganttForm = reactive({
  projectId: null, parentId: null, taskName: '', taskType: 2,
  planStartDate: '', planEndDate: '', actualStartDate: '', actualEndDate: '',
  progressPct: 0, sortOrder: 0
})
const ganttRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }]
}

// ====== 变更单 ======
const changeDialogVisible = ref(false)
const changeFormRef = ref(null)
const createEmptyDetail = () => ({ itemName: '', specModel: '', unit: '', planQuantity: null, actualQuantity: null, unitPrice: null })
const changeForm = reactive({
  projectId: null, changeType: '', title: '', description: '',
  details: [createEmptyDetail()]
})
const changeRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入变更标题', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const fn = activeTab.value === 'gantt' ? getGanttTaskList : getChangeOrderList
    const res = await fn({ page: page.value, size: 20 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadProjects = async () => {
  try { const res = await getAllProjects(); projects.value = res.data || [] } catch { /* ignore */ }
}

const handleTabChange = () => { page.value = 1; fetchData() }

// ====== 甘特任务 CRUD ======
const handleAddGantt = () => { isEdit.value = false; editId.value = null; loadProjects(); ganttDialogVisible.value = true }

const handleEditGantt = (row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  Object.assign(ganttForm, {
    projectId: row.project_id, parentId: row.parent_id, taskName: row.task_name || '', taskType: row.task_type,
    planStartDate: row.plan_start_date || '', planEndDate: row.plan_end_date || '',
    actualStartDate: row.actual_start_date || '', actualEndDate: row.actual_end_date || '',
    progressPct: row.progress_pct || 0, sortOrder: row.sort_order || 0
  })
  ganttDialogVisible.value = true
}

const resetGanttForm = () => {
  Object.assign(ganttForm, {
    projectId: null, parentId: null, taskName: '', taskType: 2,
    planStartDate: '', planEndDate: '', actualStartDate: '', actualEndDate: '',
    progressPct: 0, sortOrder: 0
  })
  ganttFormRef.value?.resetFields()
}

const submitGantt = async () => {
  await ganttFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateGanttTask(editId.value, ganttForm); ElMessage.success('更新成功') }
    else { await createGanttTask(ganttForm); ElMessage.success('创建成功') }
    ganttDialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

const handleDeleteGantt = async (row) => {
  await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
  await deleteGanttTask(row.id); ElMessage.success('删除成功'); fetchData()
}

// ====== 变更单 CRUD ======
const handleAddChange = () => { isEdit.value = false; editId.value = null; loadProjects(); changeDialogVisible.value = true }

const handleEditChange = (row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  Object.assign(changeForm, {
    projectId: row.project_id, changeType: row.change_type || '', title: row.title || '',
    description: row.description || '', details: [createEmptyDetail()]
  })
  changeDialogVisible.value = true
}

const resetChangeForm = () => {
  Object.assign(changeForm, {
    projectId: null, changeType: '', title: '', description: '',
    details: [createEmptyDetail()]
  })
  changeFormRef.value?.resetFields()
}

const addChangeDetail = () => { changeForm.details.push(createEmptyDetail()) }

const submitChange = async () => {
  await changeFormRef.value.validate()
  const details = changeForm.details.filter(d => d.itemName)
  const totalAmount = details.reduce((s, d) => s + ((d.actualQuantity || 0) - (d.planQuantity || 0)) * (d.unitPrice || 0), 0)
  const payload = { projectId: changeForm.projectId, changeType: changeForm.changeType, title: changeForm.title, description: changeForm.description, totalAmount, details }
  submitting.value = true
  try {
    if (isEdit.value) { await updateChangeOrder(editId.value, payload); ElMessage.success('更新成功') }
    else { await createChangeOrder(payload); ElMessage.success('创建成功') }
    changeDialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

const handleDeleteChange = async (row) => {
  await ElMessageBox.confirm('确定删除该变更单？', '提示', { type: 'warning' })
  await deleteChangeOrder(row.id); ElMessage.success('删除成功'); fetchData()
}

onMounted(() => { fetchData() })
</script>
