<template>
  <div class="contract-page">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="合同名称">
          <el-input v-model="queryForm.contractName" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="合同类型">
          <el-select v-model="queryForm.contractType" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="t in contractTypes" :key="t.code" :label="t.label" :value="t.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="待审批" value="pending" />
            <el-option label="已审批" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已终止" value="terminated" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px">
      <div style="margin-bottom: 12px">
        <el-button type="primary" @click="handleAdd">新建合同</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="contract_no" label="合同编号" width="140" />
        <el-table-column prop="contract_name" label="合同名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contract_type" label="类型" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.contract_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount_with_tax" label="含税金额" width="130" align="right">
          <template #default="{ row }">{{ row.amount_with_tax ? Number(row.amount_with_tax).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="party_a" label="甲方" width="130" show-overflow-tooltip />
        <el-table-column prop="party_b" label="乙方" width="130" show-overflow-tooltip />
        <el-table-column prop="sign_date" label="签订日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)"
              :disabled="row.status === 'pending' || row.status === 'approved'">编辑</el-button>
            <el-button type="info" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="warning" link size="small" @click="handleSubmitApproval(row)"
              v-if="row.status === 'draft' || row.status === 'rejected'">提交审批</el-button>
            <el-button type="success" link size="small" @click="handleApprove(row)"
              v-if="row.status === 'pending'">通过</el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)"
              v-if="row.status === 'pending'">驳回</el-button>
            <el-button type="success" link size="small" @click="handlePrint(row)"
              v-if="row.status === 'approved'">打印</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)"
              v-if="row.status !== 'pending' && row.status !== 'approved'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, sizes, prev, pager, next, jumper" :total="total" :page-sizes="[10, 20, 50]"
        v-model:current-page="queryForm.page" v-model:page-size="queryForm.size"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <!-- 新建/编辑合同 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑合同' : '新建合同'" width="900px" @closed="resetForm" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="合同名称" prop="contractName">
          <el-input v-model="form.contractName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="form.contractType" style="width: 100%" @change="onTypeChange" :disabled="isEdit">
                <el-option v-for="t in contractTypes" :key="t.code" :label="t.label" :value="t.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="form.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="含税金额" prop="amountWithTax">
              <el-input-number v-model="form.amountWithTax" :precision="2" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不含税金额" prop="amountWithoutTax">
              <el-input-number v-model="form.amountWithoutTax" :precision="2" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率(%)" prop="taxRate">
              <el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="签订日期">
              <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="甲方"><el-input v-model="form.partyA" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乙方"><el-input v-model="form.partyB" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

        <!-- 模板字段填写区 -->
        <el-divider v-if="tplFields.length > 0">模板字段填写</el-divider>
        <div v-if="tplLoading" style="text-align: center; padding: 20px">
          <el-icon class="is-loading"><Loading /></el-icon> 加载模板字段...
        </div>
        <div v-if="tplError" style="padding: 10px">
          <el-alert :title="tplError" type="warning" show-icon :closable="false" />
        </div>
        <template v-for="field in tplFields" :key="field.field_key">
          <el-form-item :label="field.field_name" :required="field.required === 1">
            <el-input v-if="field.field_type === 'text'" v-model="form.fieldValues[field.field_key]"
              :placeholder="field.placeholder || ''" :maxlength="field.max_length || undefined" />
            <el-input-number v-else-if="field.field_type === 'number'" v-model="form.fieldValues[field.field_key]"
              controls-position="right" style="width: 100%" />
            <el-date-picker v-else-if="field.field_type === 'date'" v-model="form.fieldValues[field.field_key]"
              type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            <el-select v-else-if="field.field_type === 'select'" v-model="form.fieldValues[field.field_key]" style="width: 100%">
              <el-option v-for="opt in parseOptions(field.options_json)" :key="opt" :label="opt" :value="opt" />
            </el-select>
            <el-input v-else-if="field.field_type === 'textarea'" v-model="form.fieldValues[field.field_key]"
              type="textarea" :rows="3" :placeholder="field.placeholder || ''" :maxlength="field.max_length || undefined" />
            <el-input v-else v-model="form.fieldValues[field.field_key]" :placeholder="field.placeholder || ''" />
          </el-form-item>
        </template>

        <!-- 合同正文编辑区(模板渲染后可编辑) -->
        <el-divider v-if="tplHtml">合同正文编辑</el-divider>
        <div v-if="tplHtml" class="contract-editor-wrap">
          <div class="editor-toolbar">
            <el-button size="small" @click="loadTplPreview">刷新预览</el-button>
          </div>
          <div ref="contentEditorRef" class="contract-editor" contenteditable="true"
            @input="onContentInput" v-html="editableContent"></div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '保存草稿' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 合同预览弹窗 -->
    <el-dialog v-model="previewVisible" title="合同预览" width="80%" top="3vh" destroy-on-close>
      <div class="contract-preview" v-html="previewHtml"></div>
    </el-dialog>

    <!-- 打印弹窗 -->
    <el-dialog v-model="printVisible" title="合同打印预览" width="90%" top="2vh" destroy-on-close>
      <div class="print-actions" style="margin-bottom: 12px; text-align: right">
        <el-button type="primary" @click="doPrint">打印合同</el-button>
      </div>
      <iframe ref="printFrame" :srcdoc="printHtml" style="width: 100%; height: 75vh; border: 1px solid #ebeef5"></iframe>
    </el-dialog>

    <!-- 审批意见弹窗 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalDialogTitle" width="450px">
      <el-form>
        <el-form-item label="审批意见">
          <el-input v-model="approvalRemark" type="textarea" :rows="3" placeholder="请输入审批意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button :type="approvalAction === 'approve' ? 'success' : 'danger'" @click="confirmApproval" :loading="approvalLoading">
          {{ approvalAction === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  getContractList, createContract, updateContract, deleteContract,
  submitContractApproval, approveContract, rejectContract,
  previewContractContent, getPrintableContract, saveContractContent
} from '@/api/contract'
import { getAllProjects } from '@/api/project'
import { getContractTypes, getActiveTplVersion, getVersionFields, previewVersion } from '@/api/contractTpl'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const projects = ref([])
const contractTypes = ref([])
const contentEditorRef = ref(null)

const tplFields = ref([])
const tplLoading = ref(false)
const tplError = ref('')
const tplHtml = ref('')
const editableContent = ref('')
const activeTplVersionId = ref(null)

// 预览
const previewVisible = ref(false)
const previewHtml = ref('')

// 打印
const printVisible = ref(false)
const printHtml = ref('')
const printFrame = ref(null)

// 审批弹窗
const approvalDialogVisible = ref(false)
const approvalDialogTitle = ref('')
const approvalAction = ref('')
const approvalRemark = ref('')
const approvalLoading = ref(false)
const approvalTargetId = ref(null)

const statusMap = {
  draft: { text: '草稿', type: 'info' },
  pending: { text: '待审批', type: 'warning' },
  approved: { text: '已审批', type: 'success' },
  rejected: { text: '已驳回', type: 'danger' },
  terminated: { text: '已终止', type: 'info' }
}

const queryForm = reactive({ contractName: '', contractType: '', status: '', page: 1, size: 20 })

const form = reactive({
  contractName: '', contractType: null, projectId: null, supplierId: null,
  amountWithTax: null, amountWithoutTax: null, taxRate: null,
  signDate: null, startDate: null, endDate: null, partyA: '', partyB: '', remark: '',
  fieldValues: {}, content: ''
})

const rules = {
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  amountWithTax: [{ required: true, message: '请输入含税金额', trigger: 'blur' }],
  amountWithoutTax: [{ required: true, message: '请输入不含税金额', trigger: 'blur' }],
  taxRate: [{ required: true, message: '请输入税率', trigger: 'blur' }]
}

const typeLabel = (code) => {
  const t = contractTypes.value.find(i => i.code === code)
  return t ? t.label : code
}

const parseOptions = (json) => {
  if (!json) return []
  try { return JSON.parse(json) } catch { return [] }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getContractList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadProjects = async () => {
  try { const res = await getAllProjects(); projects.value = res.data || [] } catch { /* ignore */ }
}

const onTypeChange = async (type) => {
  tplFields.value = []; tplError.value = ''; tplHtml.value = ''; editableContent.value = ''
  form.fieldValues = {}; activeTplVersionId.value = null
  if (!type) return

  tplLoading.value = true
  try {
    const res = await getActiveTplVersion(type)
    if (res.code === 200 && res.data) {
      activeTplVersionId.value = res.data.id
      const fieldRes = await getVersionFields(res.data.id)
      tplFields.value = fieldRes.data || []
      for (const f of tplFields.value) {
        form.fieldValues[f.field_key] = f.default_value || ''
      }
      // 加载模板HTML用于编辑
      const htmlRes = await previewVersion(res.data.id)
      tplHtml.value = htmlRes.data || ''
      editableContent.value = tplHtml.value
    } else {
      tplError.value = '该合同类型尚未配置模板，请联系管理员'
    }
  } catch (e) {
    tplError.value = e?.response?.data?.message || '该合同类型尚未配置模板'
  } finally { tplLoading.value = false }
}

const loadTplPreview = async () => {
  if (!activeTplVersionId.value) return
  const res = await previewVersion(activeTplVersionId.value)
  editableContent.value = res.data || ''
}

const onContentInput = () => {
  if (contentEditorRef.value) {
    form.content = contentEditorRef.value.innerHTML
  }
}

const handleSearch = () => { queryForm.page = 1; fetchData() }
const handleReset = () => {
  queryForm.contractName = ''; queryForm.contractType = ''; queryForm.status = ''; queryForm.page = 1; fetchData()
}

const handleAdd = () => {
  isEdit.value = false; editId.value = null; tplFields.value = []; tplError.value = ''
  tplHtml.value = ''; editableContent.value = ''; activeTplVersionId.value = null
  loadProjects(); dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  Object.assign(form, {
    contractName: row.contract_name, contractType: row.contract_type,
    projectId: row.project_id, supplierId: row.supplier_id,
    amountWithTax: row.amount_with_tax, amountWithoutTax: row.amount_without_tax,
    taxRate: row.tax_rate, signDate: row.sign_date || null, startDate: row.start_date || null,
    endDate: row.end_date || null, partyA: row.party_a || '', partyB: row.party_b || '',
    remark: row.remark || '', fieldValues: {}, content: row.content || ''
  })
  if (row.content) {
    editableContent.value = row.content
    tplHtml.value = row.content
  }
  if (row.contract_type) onTypeChange(row.contract_type)
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    contractName: '', contractType: null, projectId: null, supplierId: null,
    amountWithTax: null, amountWithoutTax: null, taxRate: null,
    signDate: null, startDate: null, endDate: null, partyA: '', partyB: '', remark: '',
    fieldValues: {}, content: ''
  })
  tplFields.value = []; tplError.value = ''; tplHtml.value = ''; editableContent.value = ''
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  await formRef.value.validate()

  for (const f of tplFields.value) {
    if (f.required === 1) {
      const val = form.fieldValues[f.field_key]
      if (!val && val !== 0) {
        ElMessage.warning(`请填写模板字段: ${f.field_name}`)
        return
      }
    }
  }

  const payload = { ...form }
  if (form.fieldValues && Object.keys(form.fieldValues).length > 0) {
    const strValues = {}
    for (const [key, val] of Object.entries(form.fieldValues)) {
      strValues[key] = val !== null && val !== undefined ? String(val) : ''
    }
    payload.fieldValues = strValues
  }
  // 获取编辑器中的内容
  if (contentEditorRef.value) {
    payload.content = contentEditorRef.value.innerHTML
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateContract(editId.value, payload)
      // 如果有编辑内容，也保存正文
      if (payload.content) {
        await saveContractContent(editId.value, payload.content)
      }
      ElMessage.success('更新成功')
    } else {
      await createContract(payload)
      ElMessage.success('已保存为草稿')
    }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

// 提交审批
const handleSubmitApproval = (row) => {
  ElMessageBox.confirm(`确定提交合同"${row.contract_name}"进行审批吗？`, '提交审批').then(async () => {
    await submitContractApproval(row.id)
    ElMessage.success('已提交审批')
    fetchData()
  }).catch(() => {})
}

// 审批通过
const handleApprove = (row) => {
  approvalTargetId.value = row.id
  approvalAction.value = 'approve'
  approvalDialogTitle.value = `审批通过 — ${row.contract_name}`
  approvalRemark.value = ''
  approvalDialogVisible.value = true
}

// 审批驳回
const handleReject = (row) => {
  approvalTargetId.value = row.id
  approvalAction.value = 'reject'
  approvalDialogTitle.value = `驳回 — ${row.contract_name}`
  approvalRemark.value = ''
  approvalDialogVisible.value = true
}

const confirmApproval = async () => {
  approvalLoading.value = true
  try {
    const data = { remark: approvalRemark.value }
    if (approvalAction.value === 'approve') {
      await approveContract(approvalTargetId.value, data)
      ElMessage.success('审批通过，合同已生成')
    } else {
      await rejectContract(approvalTargetId.value, data)
      ElMessage.success('已驳回')
    }
    approvalDialogVisible.value = false
    fetchData()
  } finally { approvalLoading.value = false }
}

// 预览
const handlePreview = async (row) => {
  try {
    const res = await previewContractContent(row.id)
    previewHtml.value = res.data || '<p>暂无内容</p>'
    previewVisible.value = true
  } catch {
    ElMessage.error('预览失败')
  }
}

// 打印
const handlePrint = async (row) => {
  try {
    const res = await getPrintableContract(row.id)
    printHtml.value = res.data || ''
    printVisible.value = true
  } catch {
    ElMessage.error('生成打印版失败')
  }
}

const doPrint = () => {
  if (printFrame.value) {
    printFrame.value.contentWindow.print()
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除合同"${row.contract_name}"？`, '提示', { type: 'warning' })
  await deleteContract(row.id); ElMessage.success('删除成功'); fetchData()
}

onMounted(() => {
  fetchData()
  getContractTypes().then(res => { contractTypes.value = res.data || [] })
})
</script>

<style scoped lang="scss">
.search-card { margin-bottom: 0; }

.contract-editor-wrap {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.contract-editor {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  outline: none;

  &:focus {
    background: #fafafa;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
  }
}

.contract-preview {
  max-height: 70vh;
  overflow-y: auto;
  padding: 24px;
  border: 1px solid #ebeef5;
  background: #fff;
  line-height: 1.8;
  font-size: 14px;

  :deep(.field-value) {
    border-bottom: 1px solid #333;
    padding: 0 4px;
  }
}
</style>
