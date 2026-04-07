<template>
  <div class="finance-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 结算单 ==================== -->
        <el-tab-pane label="结算单" name="statement">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('statement')">新建结算单</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="statement_no" label="结算编号" width="140" />
            <el-table-column prop="contract_id" label="合同ID" width="90" />
            <el-table-column prop="period" label="期间" width="90" />
            <el-table-column prop="current_output" label="本期产值" width="130" align="right">
              <template #default="{ row }">{{ row.current_output ? Number(row.current_output).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('statement', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('statement', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 付款申请 ==================== -->
        <el-tab-pane label="付款申请" name="payment">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('payment')">新建付款申请</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="payment_no" label="付款编号" width="140" />
            <el-table-column prop="contract_id" label="合同ID" width="90" />
            <el-table-column prop="payee_name" label="收款方" width="130" />
            <el-table-column prop="amount" label="申请金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('payment', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('payment', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 发票管理 ==================== -->
        <el-tab-pane label="发票管理" name="invoice">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('invoice')">新建发票</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="invoice_no" label="发票编号" width="160" />
            <el-table-column prop="invoice_type" label="类型" width="120" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="invoice_date" label="开票日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('invoice', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('invoice', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 报销管理 ==================== -->
        <el-tab-pane label="报销管理" name="reimburse">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('reimburse')">新建报销</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="reimburse_no" label="报销编号" width="140" />
            <el-table-column prop="reimburse_type" label="类型" width="120" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('reimburse', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('reimburse', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 成本台账 (只读) ==================== -->
        <el-tab-pane label="成本台账" name="costLedger">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="cost_type" label="费用类型" width="120" />
            <el-table-column prop="cost_subtype" label="费用子类" width="120" />
            <el-table-column prop="amount" label="金额" width="130" align="right" />
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>

    <!-- ==================== 结算单对话框 ==================== -->
    <el-dialog v-model="statementDlg" :title="isEdit ? '编辑结算单' : '新建结算单'" width="700px" @closed="resetForm('statement')">
      <el-form ref="statementFormRef" :model="statementForm" :rules="statementRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="statementForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联合同" prop="contractId">
              <el-input-number v-model="statementForm.contractId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="期间" prop="period">
              <el-input v-model="statementForm.period" placeholder="如: 2026-03" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合同含税金额" prop="contractAmount">
              <el-input-number v-model="statementForm.contractAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="形象进度(%)">
              <el-input-number v-model="statementForm.progressRatio" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="本期产值">
              <el-input-number v-model="statementForm.currentOutput" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="累计产值">
              <el-input-number v-model="statementForm.cumulativeOutput" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="本期收款">
              <el-input-number v-model="statementForm.currentCollection" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="累计收款">
              <el-input-number v-model="statementForm.cumulativeCollection" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="statementDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStatement">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 付款申请对话框 ==================== -->
    <el-dialog v-model="paymentDlg" :title="isEdit ? '编辑付款申请' : '新建付款申请'" width="700px" @closed="resetForm('payment')">
      <el-form ref="paymentFormRef" :model="paymentForm" :rules="paymentRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="paymentForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款类型" prop="paymentType">
              <el-select v-model="paymentForm.paymentType" style="width: 100%">
                <el-option label="进度款" value="进度款" />
                <el-option label="结算款" value="结算款" />
                <el-option label="质保金" value="质保金" />
                <el-option label="预付款" value="预付款" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同ID" prop="contractId">
              <el-input-number v-model="paymentForm.contractId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款金额" prop="amount">
              <el-input-number v-model="paymentForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="收款方" prop="payeeName">
              <el-input v-model="paymentForm.payeeName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开户行">
              <el-input v-model="paymentForm.payeeBank" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="银行账号">
              <el-input v-model="paymentForm.payeeAccount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="paymentForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPayment">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 发票对话框 ==================== -->
    <el-dialog v-model="invoiceDlg" :title="isEdit ? '编辑发票' : '新建发票'" width="700px" @closed="resetForm('invoice')">
      <el-form ref="invoiceFormRef" :model="invoiceForm" :rules="invoiceRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发票号码" prop="invoiceNo">
              <el-input v-model="invoiceForm.invoiceNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select v-model="invoiceForm.invoiceType" style="width: 100%">
                <el-option label="增值税专用发票" value="增值税专用发票" />
                <el-option label="增值税普通发票" value="增值税普通发票" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="invoiceForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率(%)">
              <el-input-number v-model="invoiceForm.taxRate" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税额">
              <el-input-number v-model="invoiceForm.taxAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="开票日期" prop="invoiceDate">
              <el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="invoiceForm.bizType" style="width: 100%">
                <el-option label="结算" value="statement" />
                <el-option label="付款" value="payment" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务单据ID" prop="bizId">
              <el-input-number v-model="invoiceForm.bizId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开票方">
          <el-input v-model="invoiceForm.invoiceParty" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="invoiceDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitInvoice">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 报销对话框 ==================== -->
    <el-dialog v-model="reimburseDlg" :title="isEdit ? '编辑报销' : '新建报销'" width="550px" @closed="resetForm('reimburse')">
      <el-form ref="reimburseFormRef" :model="reimburseForm" :rules="reimburseRules" label-width="100px">
        <el-form-item label="报销类型" prop="reimburseType">
          <el-select v-model="reimburseForm.reimburseType" style="width: 100%">
            <el-option label="差旅费" value="差旅费" />
            <el-option label="办公费" value="办公费" />
            <el-option label="材料费" value="材料费" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报销金额" prop="amount">
              <el-input-number v-model="reimburseForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门ID" prop="deptId">
              <el-input-number v-model="reimburseForm.deptId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联项目">
          <el-select v-model="reimburseForm.projectId" filterable clearable placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="reimburseForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reimburseDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReimburse">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStatementList, createStatement, updateStatement, deleteStatement,
  getPaymentList, createPayment, updatePayment, deletePayment,
  getInvoiceList, createInvoice, updateInvoice, deleteInvoice,
  getReimburseList, createReimburse, updateReimburse, deleteReimburse,
  getCostLedgerList
} from '@/api/finance'
import { getAllProjects } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('statement')
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const projects = ref([])
const isEdit = ref(false)
const editId = ref(null)

const statementDlg = ref(false)
const paymentDlg = ref(false)
const invoiceDlg = ref(false)
const reimburseDlg = ref(false)

const statementFormRef = ref(null)
const paymentFormRef = ref(null)
const invoiceFormRef = ref(null)
const reimburseFormRef = ref(null)

// ====== Forms ======
const statementForm = reactive({
  projectId: null, contractId: null, period: '', contractAmount: null,
  progressRatio: null, currentOutput: null, cumulativeOutput: null,
  currentCollection: null, cumulativeCollection: null
})
const paymentForm = reactive({
  projectId: null, paymentType: '', contractId: null, amount: null,
  payeeName: '', payeeBank: '', payeeAccount: '', remark: ''
})
const invoiceForm = reactive({
  invoiceNo: '', invoiceType: '', amount: null, taxRate: null, taxAmount: null,
  invoiceDate: '', bizType: '', bizId: null, invoiceParty: ''
})
const reimburseForm = reactive({
  reimburseType: '', amount: null, deptId: null, projectId: null, description: ''
})

// ====== Rules ======
const statementRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  contractId: [{ required: true, message: '请输入合同ID', trigger: 'blur' }],
  period: [{ required: true, message: '请输入期间', trigger: 'blur' }],
  contractAmount: [{ required: true, message: '请输入合同含税金额', trigger: 'blur' }]
}
const paymentRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  paymentType: [{ required: true, message: '请选择付款类型', trigger: 'change' }],
  contractId: [{ required: true, message: '请输入合同ID', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  payeeName: [{ required: true, message: '请输入收款方名称', trigger: 'blur' }]
}
const invoiceRules = {
  invoiceNo: [{ required: true, message: '请输入发票号码', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '请选择开票日期', trigger: 'change' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  bizId: [{ required: true, message: '请输入业务单据ID', trigger: 'blur' }]
}
const reimburseRules = {
  reimburseType: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入报销金额', trigger: 'blur' }],
  deptId: [{ required: true, message: '请输入部门ID', trigger: 'blur' }]
}

const apiMap = {
  statement: getStatementList,
  payment: getPaymentList,
  invoice: getInvoiceList,
  reimburse: getReimburseList,
  costLedger: getCostLedgerList
}

const deleteMap = { statement: deleteStatement, payment: deletePayment, invoice: deleteInvoice, reimburse: deleteReimburse }

const fetchData = async () => {
  loading.value = true
  try {
    const fn = apiMap[activeTab.value]
    const res = await fn({ page: page.value, size: 20 })
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
  if (type === 'statement') statementDlg.value = true
  else if (type === 'payment') paymentDlg.value = true
  else if (type === 'invoice') invoiceDlg.value = true
  else reimburseDlg.value = true
}

const handleEdit = (type, row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  if (type === 'statement') {
    Object.assign(statementForm, {
      projectId: row.project_id, contractId: row.contract_id, period: row.period || '',
      contractAmount: row.contract_amount, progressRatio: row.progress_ratio,
      currentOutput: row.current_output, cumulativeOutput: row.cumulative_output,
      currentCollection: row.current_collection, cumulativeCollection: row.cumulative_collection
    })
    statementDlg.value = true
  } else if (type === 'payment') {
    Object.assign(paymentForm, {
      projectId: row.project_id, paymentType: row.payment_type || '', contractId: row.contract_id,
      amount: row.amount, payeeName: row.payee_name || '', payeeBank: row.payee_bank || '',
      payeeAccount: row.payee_account || '', remark: row.remark || ''
    })
    paymentDlg.value = true
  } else if (type === 'invoice') {
    Object.assign(invoiceForm, {
      invoiceNo: row.invoice_no || '', invoiceType: row.invoice_type || '', amount: row.amount,
      taxRate: row.tax_rate, taxAmount: row.tax_amount, invoiceDate: row.invoice_date || '',
      bizType: row.biz_type || '', bizId: row.biz_id, invoiceParty: row.invoice_party || ''
    })
    invoiceDlg.value = true
  } else {
    Object.assign(reimburseForm, {
      reimburseType: row.reimburse_type || '', amount: row.amount,
      deptId: row.dept_id, projectId: row.project_id, description: row.description || ''
    })
    reimburseDlg.value = true
  }
}

const resetForm = (type) => {
  if (type === 'statement') {
    Object.assign(statementForm, { projectId: null, contractId: null, period: '', contractAmount: null, progressRatio: null, currentOutput: null, cumulativeOutput: null, currentCollection: null, cumulativeCollection: null })
    statementFormRef.value?.resetFields()
  } else if (type === 'payment') {
    Object.assign(paymentForm, { projectId: null, paymentType: '', contractId: null, amount: null, payeeName: '', payeeBank: '', payeeAccount: '', remark: '' })
    paymentFormRef.value?.resetFields()
  } else if (type === 'invoice') {
    Object.assign(invoiceForm, { invoiceNo: '', invoiceType: '', amount: null, taxRate: null, taxAmount: null, invoiceDate: '', bizType: '', bizId: null, invoiceParty: '' })
    invoiceFormRef.value?.resetFields()
  } else {
    Object.assign(reimburseForm, { reimburseType: '', amount: null, deptId: null, projectId: null, description: '' })
    reimburseFormRef.value?.resetFields()
  }
}

const handleDelete = async (type, row) => {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteMap[type](row.id); ElMessage.success('删除成功'); fetchData()
}

const submitStatement = async () => {
  await statementFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateStatement(editId.value, statementForm); ElMessage.success('更新成功') }
    else { await createStatement(statementForm); ElMessage.success('创建成功') }
    statementDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitPayment = async () => {
  await paymentFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updatePayment(editId.value, paymentForm); ElMessage.success('更新成功') }
    else { await createPayment(paymentForm); ElMessage.success('创建成功') }
    paymentDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitInvoice = async () => {
  await invoiceFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateInvoice(editId.value, invoiceForm); ElMessage.success('更新成功') }
    else { await createInvoice(invoiceForm); ElMessage.success('创建成功') }
    invoiceDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitReimburse = async () => {
  await reimburseFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateReimburse(editId.value, reimburseForm); ElMessage.success('更新成功') }
    else { await createReimburse(reimburseForm); ElMessage.success('创建成功') }
    reimburseDlg.value = false; fetchData()
  } finally { submitting.value = false }
}

onMounted(() => { fetchData() })
</script>
