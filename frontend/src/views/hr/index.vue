<template>
  <div class="hr-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 薪资管理 ==================== -->
        <el-tab-pane label="薪资管理" name="salary">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="openDialog('salary')">新建薪资</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="salary_month" label="月份" width="100" />
            <el-table-column prop="base_salary" label="基本工资" width="120" align="right" />
            <el-table-column prop="net_salary" label="实发工资" width="120" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('salary', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('salary', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 劳动合同 ==================== -->
        <el-tab-pane label="劳动合同" name="hrContract">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="openDialog('hrContract')">新建合同</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="contract_type" label="合同类型" width="120" />
            <el-table-column prop="start_date" label="开始日期" width="110" />
            <el-table-column prop="end_date" label="结束日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('hrContract', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('hrContract', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 证书管理 ==================== -->
        <el-tab-pane label="证书管理" name="certificate">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="openDialog('certificate')">新建证书</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="cert_name" label="证书名称" min-width="150" />
            <el-table-column prop="cert_type" label="证书类型" width="120" />
            <el-table-column prop="cert_no" label="证书编号" width="140" />
            <el-table-column prop="expire_date" label="到期日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('certificate', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('certificate', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 入职管理 ==================== -->
        <el-tab-pane label="入职管理" name="entry">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="openDialog('entry')">新建入职</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="applicant_name" label="姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="dept_id" label="部门ID" width="90" />
            <el-table-column prop="position" label="岗位" width="120" />
            <el-table-column prop="entry_date" label="入职日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('entry', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('entry', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 离职管理 ==================== -->
        <el-tab-pane label="离职管理" name="resign">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="openDialog('resign')">新建离职</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="resign_type" label="离职类型" width="100" />
            <el-table-column prop="resign_date" label="离职日期" width="110" />
            <el-table-column prop="resign_reason" label="离职原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('resign', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('resign', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>

    <!-- ==================== 薪资对话框 ==================== -->
    <el-dialog v-model="salaryDlg" :title="isEdit ? '编辑薪资' : '新建薪资'" width="700px" @closed="resetCurrentForm">
      <el-form ref="formRef" :model="salaryForm" :rules="salaryRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工ID" prop="userId">
              <el-input-number v-model="salaryForm.userId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工资月份" prop="salaryMonth">
              <el-input v-model="salaryForm.salaryMonth" placeholder="如: 2026-03" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="基本工资"><el-input-number v-model="salaryForm.baseSalary" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="岗位工资"><el-input-number v-model="salaryForm.positionSalary" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="绩效"><el-input-number v-model="salaryForm.performance" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="补贴"><el-input-number v-model="salaryForm.allowance" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="奖金"><el-input-number v-model="salaryForm.bonus" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="扣款"><el-input-number v-model="salaryForm.deduction" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="社保"><el-input-number v-model="salaryForm.socialInsurance" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="个税"><el-input-number v-model="salaryForm.tax" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实发工资"><el-input-number v-model="salaryForm.netSalary" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="salaryDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit('salary')">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 劳动合同对话框 ==================== -->
    <el-dialog v-model="contractDlg" :title="isEdit ? '编辑劳动合同' : '新建劳动合同'" width="600px" @closed="resetCurrentForm">
      <el-form ref="formRef" :model="contractForm" :rules="contractRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工ID" prop="userId">
              <el-input-number v-model="contractForm.userId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="contractForm.contractType" style="width: 100%">
                <el-option label="固定期限" value="固定期限" />
                <el-option label="无固定期限" value="无固定期限" />
                <el-option label="以完成工作任务为期限" value="以完成工作任务为期限" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="contractForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="contractForm.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="contractDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit('hrContract')">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 证书对话框 ==================== -->
    <el-dialog v-model="certDlg" :title="isEdit ? '编辑证书' : '新建证书'" width="650px" @closed="resetCurrentForm">
      <el-form ref="formRef" :model="certForm" :rules="certRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="证书名称" prop="certName">
              <el-input v-model="certForm.certName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证书类型" prop="certType">
              <el-select v-model="certForm.certType" style="width: 100%">
                <el-option label="注册类" value="注册类" />
                <el-option label="职称类" value="职称类" />
                <el-option label="岗位类" value="岗位类" />
                <el-option label="特种作业" value="特种作业" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="员工ID">
              <el-input-number v-model="certForm.userId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证书编号">
              <el-input v-model="certForm.certNo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证书分类">
              <el-input v-model="certForm.certCategory" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发证日期">
              <el-date-picker v-model="certForm.issueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期">
              <el-date-picker v-model="certForm.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="certDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit('certificate')">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 入职对话框 ==================== -->
    <el-dialog v-model="entryDlg" :title="isEdit ? '编辑入职' : '新建入职'" width="650px" @closed="resetCurrentForm">
      <el-form ref="formRef" :model="entryForm" :rules="entryRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="applicantName">
              <el-input v-model="entryForm.applicantName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="entryForm.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="部门ID" prop="deptId">
              <el-input-number v-model="entryForm.deptId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位">
              <el-input v-model="entryForm.position" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker v-model="entryForm.entryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学历">
              <el-select v-model="entryForm.education" clearable style="width: 100%">
                <el-option label="高中" value="高中" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="工作年限">
              <el-input-number v-model="entryForm.workYears" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="身份证号">
              <el-input v-model="entryForm.idCardNo" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="entryDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit('entry')">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 离职对话框 ==================== -->
    <el-dialog v-model="resignDlg" :title="isEdit ? '编辑离职' : '新建离职'" width="550px" @closed="resetCurrentForm">
      <el-form ref="formRef" :model="resignForm" :rules="resignRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工ID" prop="userId">
              <el-input-number v-model="resignForm.userId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="离职类型" prop="resignType">
              <el-select v-model="resignForm.resignType" style="width: 100%">
                <el-option label="主动离职" value="主动离职" />
                <el-option label="辞退" value="辞退" />
                <el-option label="合同到期" value="合同到期" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="离职日期" prop="resignDate">
              <el-date-picker v-model="resignForm.resignDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交接人ID">
              <el-input-number v-model="resignForm.handoverTo" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="离职原因">
          <el-input v-model="resignForm.resignReason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resignDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit('resign')">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSalaryList, createSalary, updateSalary, deleteSalary,
  getHrContractList, createHrContract, updateHrContract, deleteHrContract,
  getCertificateList, createCertificate, updateCertificate, deleteCertificate,
  getEntryList, createEntry, updateEntry, deleteEntry,
  getResignList, createResign, updateResign, deleteResign
} from '@/api/hr'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('salary')
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const salaryDlg = ref(false)
const contractDlg = ref(false)
const certDlg = ref(false)
const entryDlg = ref(false)
const resignDlg = ref(false)

// ====== Form models ======
const salaryForm = reactive({
  userId: null, salaryMonth: '', baseSalary: null, positionSalary: null,
  performance: null, allowance: null, bonus: null, deduction: null,
  socialInsurance: null, tax: null, netSalary: null, status: null
})
const contractForm = reactive({ userId: null, contractType: '', startDate: null, endDate: null, status: null })
const certForm = reactive({ certType: '', userId: null, certName: '', certCategory: '', certNo: '', issueDate: null, expireDate: null, status: null, warnStatus: null })
const entryForm = reactive({ applicantName: '', phone: '', deptId: null, position: '', entryDate: null, education: '', workYears: null, idCardNo: '', status: null })
const resignForm = reactive({ userId: null, resignType: '', resignDate: null, resignReason: '', handoverTo: null, status: null, handoverStatus: null })

// ====== Rules ======
const salaryRules = {
  userId: [{ required: true, message: '请输入员工ID', trigger: 'blur' }],
  salaryMonth: [{ required: true, message: '请输入工资月份', trigger: 'blur' }]
}
const contractRules = {
  userId: [{ required: true, message: '请输入员工ID', trigger: 'blur' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}
const certRules = {
  certName: [{ required: true, message: '请输入证书名称', trigger: 'blur' }],
  certType: [{ required: true, message: '请选择证书类型', trigger: 'change' }]
}
const entryRules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请输入部门ID', trigger: 'blur' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}
const resignRules = {
  userId: [{ required: true, message: '请输入员工ID', trigger: 'blur' }],
  resignType: [{ required: true, message: '请选择离职类型', trigger: 'change' }],
  resignDate: [{ required: true, message: '请选择离职日期', trigger: 'change' }]
}

const listApi = { salary: getSalaryList, hrContract: getHrContractList, certificate: getCertificateList, entry: getEntryList, resign: getResignList }
const createApi = { salary: createSalary, hrContract: createHrContract, certificate: createCertificate, entry: createEntry, resign: createResign }
const updateApi = { salary: updateSalary, hrContract: updateHrContract, certificate: updateCertificate, entry: updateEntry, resign: updateResign }
const deleteApi = { salary: deleteSalary, hrContract: deleteHrContract, certificate: deleteCertificate, entry: deleteEntry, resign: deleteResign }
const dlgMap = { salary: salaryDlg, hrContract: contractDlg, certificate: certDlg, entry: entryDlg, resign: resignDlg }
const formMap = { salary: salaryForm, hrContract: contractForm, certificate: certForm, entry: entryForm, resign: resignForm }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listApi[activeTab.value]({ page: page.value, size: 20 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleTabChange = () => { page.value = 1; fetchData() }

const openDialog = (type) => { isEdit.value = false; editId.value = null; dlgMap[type].value = true }

const openEditDialog = (type, row) => {
  isEdit.value = true; editId.value = row.id
  if (type === 'salary') {
    Object.assign(salaryForm, {
      userId: row.user_id, salaryMonth: row.salary_month || '', baseSalary: row.base_salary,
      positionSalary: row.position_salary, performance: row.performance, allowance: row.allowance,
      bonus: row.bonus, deduction: row.deduction, socialInsurance: row.social_insurance,
      tax: row.tax, netSalary: row.net_salary, status: row.status
    })
  } else if (type === 'hrContract') {
    Object.assign(contractForm, {
      userId: row.user_id, contractType: row.contract_type || '',
      startDate: row.start_date || null, endDate: row.end_date || null, status: row.status
    })
  } else if (type === 'certificate') {
    Object.assign(certForm, {
      certType: row.cert_type || '', userId: row.user_id, certName: row.cert_name || '',
      certCategory: row.cert_category || '', certNo: row.cert_no || '',
      issueDate: row.issue_date || null, expireDate: row.expire_date || null,
      status: row.status, warnStatus: row.warn_status
    })
  } else if (type === 'entry') {
    Object.assign(entryForm, {
      applicantName: row.applicant_name || '', phone: row.phone || '', deptId: row.dept_id,
      position: row.position || '', entryDate: row.entry_date || null,
      education: row.education || '', workYears: row.work_years, idCardNo: row.id_card_no || '',
      status: row.status
    })
  } else {
    Object.assign(resignForm, {
      userId: row.user_id, resignType: row.resign_type || '',
      resignDate: row.resign_date || null, resignReason: row.resign_reason || '', handoverTo: row.handover_to,
      status: row.status, handoverStatus: row.handover_status
    })
  }
  dlgMap[type].value = true
}

const resetDefaults = {
  salary: { userId: null, salaryMonth: '', baseSalary: null, positionSalary: null, performance: null, allowance: null, bonus: null, deduction: null, socialInsurance: null, tax: null, netSalary: null, status: null },
  hrContract: { userId: null, contractType: '', startDate: null, endDate: null, status: null },
  certificate: { certType: '', userId: null, certName: '', certCategory: '', certNo: '', issueDate: null, expireDate: null, status: null, warnStatus: null },
  entry: { applicantName: '', phone: '', deptId: null, position: '', entryDate: null, education: '', workYears: null, idCardNo: '', status: null },
  resign: { userId: null, resignType: '', resignDate: null, resignReason: '', handoverTo: null, status: null, handoverStatus: null }
}

const resetCurrentForm = () => {
  for (const [type, form] of Object.entries(formMap)) {
    if (dlgMap[type].value === false) {
      Object.assign(form, resetDefaults[type])
    }
  }
  formRef.value?.resetFields()
}

const submit = async (type) => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateApi[type](editId.value, formMap[type]); ElMessage.success('更新成功') }
    else { await createApi[type](formMap[type]); ElMessage.success('创建成功') }
    dlgMap[type].value = false; fetchData()
  } finally { submitting.value = false }
}

const handleDelete = async (type, row) => {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteApi[type](row.id); ElMessage.success('删除成功'); fetchData()
}

onMounted(() => { fetchData() })
</script>
