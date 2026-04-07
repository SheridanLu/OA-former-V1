<template>
  <div class="completion-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 竣工验收 ==================== -->
        <el-tab-pane label="竣工验收" name="finish">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('finish')">新建竣工验收</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="plan_finish_date" label="计划竣工日期" width="130" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('finish', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('finish', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 劳务结算 ==================== -->
        <el-tab-pane label="劳务结算" name="labor">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('labor')">新建劳务结算</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="settlement_no" label="结算编号" width="140" />
            <el-table-column prop="settlement_amount" label="结算金额" width="130" align="right">
              <template #default="{ row }">{{ row.settlement_amount ? Number(row.settlement_amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="paid_amount" label="已付金额" width="130" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('labor', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('labor', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 案件管理 ==================== -->
        <el-tab-pane label="案件管理" name="case">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('case')">新建案例</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="case_name" label="案件名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="case_type" label="案件类型" width="120" />
            <el-table-column prop="summary" label="案件摘要" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('case', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('case', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 异常工单 ==================== -->
        <el-tab-pane label="异常工单" name="exception">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('exception')">新建异常工单</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="biz_type" label="业务类型" width="120" />
            <el-table-column prop="biz_id" label="业务ID" width="90" />
            <el-table-column prop="fail_reason" label="失败原因" min-width="250" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'warning' : 'success'" size="small">
                  {{ row.status === 1 ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDelete('exception', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>

    <!-- ==================== 竣工验收对话框 ==================== -->
    <el-dialog v-model="finishDlg" :title="isEdit ? '编辑竣工验收' : '新建竣工验收'" width="650px" @closed="resetForm">
      <el-form ref="formRef" :model="finishForm" :rules="finishRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="finishForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划竣工日期">
              <el-date-picker v-model="finishForm.planFinishDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="验收标题" prop="title">
          <el-input v-model="finishForm.title" />
        </el-form-item>
        <el-form-item label="完工内容">
          <el-input v-model="finishForm.finishContent" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="自检结果">
          <el-input v-model="finishForm.selfCheckResult" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="遗留问题">
          <el-input v-model="finishForm.remainingIssues" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFinish">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 劳务结算对话框 ==================== -->
    <el-dialog v-model="laborDlg" :title="isEdit ? '编辑劳务结算' : '新建劳务结算'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="laborForm" :rules="laborRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="laborForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="劳务合同ID" prop="contractId">
              <el-input-number v-model="laborForm.contractId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="结算金额" prop="settlementAmount">
              <el-input-number v-model="laborForm.settlementAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="已付金额">
              <el-input-number v-model="laborForm.paidAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申请付款" prop="applyPayAmount">
              <el-input-number v-model="laborForm.applyPayAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="laborDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitLabor">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 案例对话框 ==================== -->
    <el-dialog v-model="caseDlg" :title="isEdit ? '编辑案例' : '新建案例'" width="650px" @closed="resetForm">
      <el-form ref="formRef" :model="caseForm" :rules="caseRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="案例名称" prop="caseName">
              <el-input v-model="caseForm.caseName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="案例类型">
              <el-select v-model="caseForm.caseType" clearable style="width: 100%">
                <el-option label="质量案例" value="质量案例" />
                <el-option label="安全案例" value="安全案例" />
                <el-option label="进度案例" value="进度案例" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联项目">
          <el-select v-model="caseForm.projectId" filterable clearable placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="案例摘要">
          <el-input v-model="caseForm.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="案例内容">
          <el-input v-model="caseForm.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="caseDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCase">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 异常工单对话框 ==================== -->
    <el-dialog v-model="exceptionDlg" title="新建异常工单" width="550px" @closed="resetForm">
      <el-form ref="formRef" :model="exceptionForm" :rules="exceptionRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="exceptionForm.bizType" style="width: 100%">
                <el-option label="入库" value="inbound" />
                <el-option label="出库" value="outbound" />
                <el-option label="付款" value="payment" />
                <el-option label="结算" value="statement" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务单据ID" prop="bizId">
              <el-input-number v-model="exceptionForm.bizId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="失败原因" prop="failReason">
          <el-input v-model="exceptionForm.failReason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理人ID">
          <el-input-number v-model="exceptionForm.handlerId" :min="1" controls-position="right" style="width: 220px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exceptionDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitException">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCompletionList, createCompletion, updateCompletion, deleteCompletion,
  getLaborList, createLabor, updateLabor, deleteLabor,
  getCaseList, createCase, updateCase, deleteCase,
  getExceptionList, createException, deleteException
} from '@/api/completion'
import { getAllProjects } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('finish')
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const projects = ref([])
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const finishDlg = ref(false)
const laborDlg = ref(false)
const caseDlg = ref(false)
const exceptionDlg = ref(false)

// ====== Forms ======
const finishForm = reactive({ projectId: null, title: '', planFinishDate: null, finishContent: '', selfCheckResult: '', remainingIssues: '' })
const laborForm = reactive({ projectId: null, contractId: null, settlementAmount: null, paidAmount: null, applyPayAmount: null })
const caseForm = reactive({ projectId: null, caseName: '', caseType: '', summary: '', content: '' })
const exceptionForm = reactive({ bizType: '', bizId: null, failReason: '', handlerId: null })

// ====== Rules ======
const finishRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入验收标题', trigger: 'blur' }]
}
const laborRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  contractId: [{ required: true, message: '请输入劳务合同ID', trigger: 'blur' }],
  settlementAmount: [{ required: true, message: '请输入结算金额', trigger: 'blur' }],
  applyPayAmount: [{ required: true, message: '请输入申请付款金额', trigger: 'blur' }]
}
const caseRules = {
  caseName: [{ required: true, message: '请输入案例名称', trigger: 'blur' }]
}
const exceptionRules = {
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  bizId: [{ required: true, message: '请输入业务单据ID', trigger: 'blur' }],
  failReason: [{ required: true, message: '请输入失败原因', trigger: 'blur' }]
}

const listApi = { finish: getCompletionList, labor: getLaborList, case: getCaseList, exception: getExceptionList }
const deleteApiMap = { finish: deleteCompletion, labor: deleteLabor, case: deleteCase, exception: deleteException }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listApi[activeTab.value]({ page: page.value, size: 20 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadProjects = async () => {
  try { const res = await getAllProjects(); projects.value = res.data || [] } catch { /* ignore */ }
}

const handleTabChange = () => { page.value = 1; fetchData() }

const handleAdd = (type) => {
  isEdit.value = false; editId.value = null; loadProjects()
  if (type === 'finish') finishDlg.value = true
  else if (type === 'labor') laborDlg.value = true
  else if (type === 'case') caseDlg.value = true
  else exceptionDlg.value = true
}

const handleEdit = (type, row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  if (type === 'finish') {
    Object.assign(finishForm, {
      projectId: row.project_id, title: row.title || '', planFinishDate: row.plan_finish_date || null,
      finishContent: row.finish_content || '', selfCheckResult: row.self_check_result || '',
      remainingIssues: row.remaining_issues || ''
    })
    finishDlg.value = true
  } else if (type === 'labor') {
    Object.assign(laborForm, {
      projectId: row.project_id, contractId: row.contract_id,
      settlementAmount: row.settlement_amount, paidAmount: row.paid_amount,
      applyPayAmount: row.apply_pay_amount
    })
    laborDlg.value = true
  } else if (type === 'case') {
    Object.assign(caseForm, {
      projectId: row.project_id, caseName: row.case_name || '', caseType: row.case_type || '',
      summary: row.summary || '', content: row.content || ''
    })
    caseDlg.value = true
  }
}

const resetForm = () => {
  Object.assign(finishForm, { projectId: null, title: '', planFinishDate: null, finishContent: '', selfCheckResult: '', remainingIssues: '' })
  Object.assign(laborForm, { projectId: null, contractId: null, settlementAmount: null, paidAmount: null, applyPayAmount: null })
  Object.assign(caseForm, { projectId: null, caseName: '', caseType: '', summary: '', content: '' })
  Object.assign(exceptionForm, { bizType: '', bizId: null, failReason: '', handlerId: null })
  formRef.value?.resetFields()
}

const handleDelete = async (type, row) => {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteApiMap[type](row.id); ElMessage.success('删除成功'); fetchData()
}

const submitFinish = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateCompletion(editId.value, finishForm); ElMessage.success('更新成功') }
    else { await createCompletion(finishForm); ElMessage.success('创建成功') }
    finishDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitLabor = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateLabor(editId.value, laborForm); ElMessage.success('更新成功') }
    else { await createLabor(laborForm); ElMessage.success('创建成功') }
    laborDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitCase = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateCase(editId.value, caseForm); ElMessage.success('更新成功') }
    else { await createCase(caseForm); ElMessage.success('创建成功') }
    caseDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitException = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createException(exceptionForm); ElMessage.success('创建成功')
    exceptionDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

onMounted(() => { fetchData() })
</script>
