<template>
  <div class="inventory-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 入库管理 ==================== -->
        <el-tab-pane label="入库管理" name="inbound">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('inbound')">新建入库单</el-button>
          </div>
          <el-table :data="inboundData" v-loading="loading" stripe border>
            <el-table-column prop="inbound_no" label="入库单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="contract_id" label="合同ID" width="100" />
            <el-table-column prop="warehouse" label="仓库" width="120" />
            <el-table-column prop="inbound_date" label="入库日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('inbound', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('inbound', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="total"
            v-model:current-page="page" @current-change="fetchData" />
        </el-tab-pane>

        <!-- ==================== 出库管理 ==================== -->
        <el-tab-pane label="出库管理" name="outbound">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('outbound')">新建出库单</el-button>
          </div>
          <el-table :data="outboundData" v-loading="loading" stripe border>
            <el-table-column prop="outbound_no" label="出库单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="outbound_type" label="出库类型" width="120" />
            <el-table-column prop="outbound_date" label="出库日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('outbound', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('outbound', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 退货管理 ==================== -->
        <el-tab-pane label="退货管理" name="return">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAdd('return')">新建退货单</el-button>
          </div>
          <el-table :data="returnData" v-loading="loading" stripe border>
            <el-table-column prop="return_no" label="退货单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="dispose_method" label="处理方式" width="120" />
            <el-table-column prop="return_date" label="退货日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit('return', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('return', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 库存查询 ==================== -->
        <el-tab-pane label="库存查询" name="stock">
          <el-table :data="stockData" v-loading="loading" stripe border>
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="material_id" label="材料ID" width="90" />
            <el-table-column prop="current_quantity" label="当前库存" width="120" align="right" />
            <el-table-column prop="avg_price" label="均价" width="120" align="right" />
            <el-table-column prop="total_amount" label="库存金额" width="120" align="right" />
            <el-table-column prop="updated_at" label="更新时间" width="170" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 入库单对话框 ==================== -->
    <el-dialog v-model="inboundDialogVisible" :title="isEdit ? '编辑入库单' : '新建入库单'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="inboundForm" :rules="inboundRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="inboundForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联合同" prop="contractId">
              <el-input-number v-model="inboundForm.contractId" :min="1" controls-position="right" style="width: 100%" placeholder="合同ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库">
              <el-input v-model="inboundForm.warehouse" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库日期">
              <el-date-picker v-model="inboundForm.inboundDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="inboundForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inboundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitInbound">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 出库单对话框 ==================== -->
    <el-dialog v-model="outboundDialogVisible" :title="isEdit ? '编辑出库单' : '新建出库单'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="outboundForm" :rules="outboundRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="outboundForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库类型" prop="outboundType">
              <el-select v-model="outboundForm.outboundType" style="width: 100%">
                <el-option label="领用" value="领用" />
                <el-option label="调拨" value="调拨" />
                <el-option label="报废" value="报废" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="出库日期">
          <el-date-picker v-model="outboundForm.outboundDate" type="date" value-format="YYYY-MM-DD" style="width: 220px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="outboundForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOutbound">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 退货单对话框 ==================== -->
    <el-dialog v-model="returnDialogVisible" :title="isEdit ? '编辑退货单' : '新建退货单'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="returnForm" :rules="returnRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="returnForm.projectId" filterable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处置方式" prop="disposeMethod">
              <el-select v-model="returnForm.disposeMethod" style="width: 100%">
                <el-option label="退回供应商" value="退回供应商" />
                <el-option label="项目调拨" value="项目调拨" />
                <el-option label="报废处置" value="报废处置" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="目标项目">
              <el-select v-model="returnForm.targetProjectId" filterable clearable placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.project_name || p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退货日期">
              <el-date-picker v-model="returnForm.returnDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReturn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInboundList, createInbound, updateInbound, deleteInbound,
  getOutboundList, createOutbound, updateOutbound, deleteOutbound,
  getReturnList, createReturn, updateReturn, deleteReturn,
  getStockList
} from '@/api/inventory'
import { getAllProjects } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('inbound')
const inboundData = ref([])
const outboundData = ref([])
const returnData = ref([])
const stockData = ref([])
const total = ref(0)
const page = ref(1)
const projects = ref([])

const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const inboundDialogVisible = ref(false)
const outboundDialogVisible = ref(false)
const returnDialogVisible = ref(false)

const inboundForm = reactive({ projectId: null, contractId: null, warehouse: '', inboundDate: null, remark: '' })
const outboundForm = reactive({ projectId: null, outboundType: '', outboundDate: null, remark: '' })
const returnForm = reactive({ projectId: null, disposeMethod: '', targetProjectId: null, returnDate: null, remark: '' })

const inboundRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  contractId: [{ required: true, message: '请输入关联合同', trigger: 'blur' }]
}
const outboundRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  outboundType: [{ required: true, message: '请选择出库类型', trigger: 'change' }]
}
const returnRules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  disposeMethod: [{ required: true, message: '请选择处置方式', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'inbound') {
      const res = await getInboundList({ page: page.value, size: 20 })
      inboundData.value = res.data.records || []; total.value = res.data.total || 0
    } else if (activeTab.value === 'outbound') {
      const res = await getOutboundList({ page: page.value, size: 20 })
      outboundData.value = res.data.records || []; total.value = res.data.total || 0
    } else if (activeTab.value === 'return') {
      const res = await getReturnList({ page: page.value, size: 20 })
      returnData.value = res.data.records || []; total.value = res.data.total || 0
    } else {
      const res = await getStockList({ page: page.value, size: 20 })
      stockData.value = res.data.records || []; total.value = res.data.total || 0
    }
  } finally { loading.value = false }
}

const loadProjects = async () => {
  try { const res = await getAllProjects(); projects.value = res.data || [] } catch { /* ignore */ }
}

const handleTabChange = () => { page.value = 1; fetchData() }

const handleAdd = (type) => {
  isEdit.value = false; editId.value = null; loadProjects()
  if (type === 'inbound') inboundDialogVisible.value = true
  else if (type === 'outbound') outboundDialogVisible.value = true
  else returnDialogVisible.value = true
}

const handleEdit = (type, row) => {
  isEdit.value = true; editId.value = row.id; loadProjects()
  if (type === 'inbound') {
    Object.assign(inboundForm, {
      projectId: row.project_id, contractId: row.contract_id,
      warehouse: row.warehouse || '', inboundDate: row.inbound_date || null, remark: row.remark || ''
    })
    inboundDialogVisible.value = true
  } else if (type === 'outbound') {
    Object.assign(outboundForm, {
      projectId: row.project_id, outboundType: row.outbound_type || '',
      outboundDate: row.outbound_date || null, remark: row.remark || ''
    })
    outboundDialogVisible.value = true
  } else {
    Object.assign(returnForm, {
      projectId: row.project_id, disposeMethod: row.dispose_method || '',
      targetProjectId: row.target_project_id, returnDate: row.return_date || null, remark: row.remark || ''
    })
    returnDialogVisible.value = true
  }
}

const resetForm = () => {
  Object.assign(inboundForm, { projectId: null, contractId: null, warehouse: '', inboundDate: null, remark: '' })
  Object.assign(outboundForm, { projectId: null, outboundType: '', outboundDate: null, remark: '' })
  Object.assign(returnForm, { projectId: null, disposeMethod: '', targetProjectId: null, returnDate: null, remark: '' })
  formRef.value?.resetFields()
}

const handleDelete = async (type, row) => {
  const labels = { inbound: '入库单', outbound: '出库单', return: '退货单' }
  await ElMessageBox.confirm(`确定删除该${labels[type]}？`, '提示', { type: 'warning' })
  if (type === 'inbound') await deleteInbound(row.id)
  else if (type === 'outbound') await deleteOutbound(row.id)
  else await deleteReturn(row.id)
  ElMessage.success('删除成功'); fetchData()
}

const submitInbound = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateInbound(editId.value, inboundForm); ElMessage.success('更新成功') }
    else { await createInbound(inboundForm); ElMessage.success('创建成功') }
    inboundDialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitOutbound = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateOutbound(editId.value, outboundForm); ElMessage.success('更新成功') }
    else { await createOutbound(outboundForm); ElMessage.success('创建成功') }
    outboundDialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

const submitReturn = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateReturn(editId.value, returnForm); ElMessage.success('更新成功') }
    else { await createReturn(returnForm); ElMessage.success('创建成功') }
    returnDialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

onMounted(() => { fetchData() })
</script>
