<template>
  <div class="approval-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 待我审批 ==================== -->
        <el-tab-pane label="待我审批" name="pending">
          <el-table :data="pendingData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="实例ID" width="80" />
            <el-table-column prop="biz_type" label="业务类型" width="120">
              <template #default="{ row }">{{ bizTypeLabel[row.biz_type] || row.biz_type }}</template>
            </el-table-column>
            <el-table-column prop="biz_id" label="单据ID" width="80" />
            <el-table-column prop="flow_name" label="流程名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="current_node_name" label="当前节点" width="130" />
            <el-table-column prop="initiator_name" label="发起人" width="100" />
            <el-table-column prop="created_at" label="提交时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
                <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
                <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 我发起的 ==================== -->
        <el-tab-pane label="我发起的" name="initiated">
          <el-table :data="initiatedData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="实例ID" width="80" />
            <el-table-column prop="biz_type" label="业务类型" width="120">
              <template #default="{ row }">{{ bizTypeLabel[row.biz_type] || row.biz_type }}</template>
            </el-table-column>
            <el-table-column prop="biz_id" label="单据ID" width="80" />
            <el-table-column prop="flow_name" label="流程名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="current_node_name" label="当前节点" width="130" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType[row.status]" size="small">{{ statusLabel[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="提交时间" width="170" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ==================== 流程定义 ==================== -->
        <el-tab-pane label="流程定义" name="flows">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddFlow">新建流程</el-button>
          </div>
          <el-table :data="flowData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="biz_type" label="业务类型" width="120">
              <template #default="{ row }">{{ bizTypeLabel[row.biz_type] || row.biz_type }}</template>
            </el-table-column>
            <el-table-column prop="flow_name" label="流程名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="version" label="版本" width="70" align="center" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditFlow(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteFlow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, prev, pager, next" :total="total"
        v-model:current-page="page" @current-change="fetchData" />
    </el-card>

    <!-- ==================== 审批操作对话框 ==================== -->
    <el-dialog v-model="actionDlg" :title="actionTitle" width="500px" @closed="resetAction">
      <el-form label-width="80px">
        <el-form-item label="业务类型">
          <span>{{ bizTypeLabel[actionRow.biz_type] || actionRow.biz_type }} (ID: {{ actionRow.biz_id }})</span>
        </el-form-item>
        <el-form-item label="流程名称">
          <span>{{ actionRow.flow_name }}</span>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="actionOpinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDlg = false">取消</el-button>
        <el-button :type="actionType === 'approve' ? 'success' : 'danger'" :loading="submitting" @click="submitAction">
          {{ actionType === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 审批详情对话框 ==================== -->
    <el-dialog v-model="detailDlg" title="审批详情" width="700px">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="业务类型">{{ bizTypeLabel[detailData.biz_type] || detailData.biz_type }}</el-descriptions-item>
        <el-descriptions-item label="单据ID">{{ detailData.biz_id }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ detailData.flow_name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType[detailData.status]" size="small">{{ statusLabel[detailData.status] || detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detailData.initiator_name }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.created_at }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin: 12px 0 8px">审批节点</h4>
      <el-steps :active="detailData.status === 'approved' ? (detailData.nodes || []).length : (detailData.current_node || 1) - 1" align-center finish-status="success" style="margin-bottom: 16px">
        <el-step v-for="n in (detailData.nodes || [])" :key="n.node_order" :title="n.node_name" />
      </el-steps>

      <h4 style="margin: 12px 0 8px">审批记录</h4>
      <el-table :data="detailData.records || []" border size="small">
        <el-table-column prop="node_name" label="节点" width="130" />
        <el-table-column prop="approver_name" label="审批人" width="100" />
        <el-table-column prop="action" label="操作" width="80">
          <template #default="{ row }">
            <el-tag :type="row.action === 'approve' ? 'success' : 'danger'" size="small">
              {{ row.action === 'approve' ? '通过' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="opinion" label="意见" min-width="150" show-overflow-tooltip />
        <el-table-column prop="created_at" label="时间" width="170" />
      </el-table>
    </el-dialog>

    <!-- ==================== 新建/编辑流程对话框 ==================== -->
    <el-dialog v-model="flowDlg" :title="isEditFlow ? '编辑流程' : '新建流程'" width="700px" @closed="resetFlowForm">
      <el-form ref="flowFormRef" :model="flowForm" :rules="flowRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="flowForm.bizType" placeholder="请选择" style="width: 100%" :disabled="isEditFlow">
                <el-option v-for="(label, key) in bizTypeLabel" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流程名称" prop="flowName">
              <el-input v-model="flowForm.flowName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input-number v-model="flowForm.version" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="flowForm.status" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">审批节点配置</el-divider>
        <div v-for="(node, idx) in flowForm.nodes" :key="idx" style="margin-bottom: 12px">
          <el-row :gutter="8" align="middle">
            <el-col :span="2"><el-tag size="small">{{ idx + 1 }}</el-tag></el-col>
            <el-col :span="7">
              <el-input v-model="node.nodeName" placeholder="节点名称" />
            </el-col>
            <el-col :span="6">
              <el-select v-model="node.approverType" placeholder="审批人类型" style="width: 100%">
                <el-option label="指定用户" value="user" />
                <el-option label="指定角色" value="role" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="node.approverId" placeholder="用户/角色ID" :min="1" controls-position="right" style="width: 100%" />
            </el-col>
            <el-col :span="3">
              <el-button type="danger" link @click="removeNode(idx)">删除</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" link @click="addNode">+ 添加审批节点</el-button>
      </el-form>
      <template #footer>
        <el-button @click="flowDlg = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFlow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFlowDefList, createFlowDef, updateFlowDef, deleteFlowDef,
  getMyPending, getMyInitiated, getInstanceDetail,
  approveInstance, rejectInstance
} from '@/api/approval'

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const page = ref(1)
const total = ref(0)

const pendingData = ref([])
const initiatedData = ref([])
const flowData = ref([])

const bizTypeLabel = {
  contract: '合同', purchase: '采购单', spot_purchase: '零星采购',
  inbound: '入库单', outbound: '出库单', return_order: '退库单',
  inventory_check: '盘点单', change_order: '变更单',
  statement: '对账单', income_split: '收入拆分',
  payment: '付款申请', reimburse: '报销单', salary: '工资单',
  hr_entry: '入职申请', hr_resign: '离职申请',
  completion: '竣工验收', gantt_task: '进度计划'
}

const statusLabel = { pending: '审批中', approved: '已通过', rejected: '已驳回', cancelled: '已取消' }
const statusType = { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }

// ====== 审批操作 ======
const actionDlg = ref(false)
const actionTitle = ref('')
const actionType = ref('')
const actionRow = ref({})
const actionOpinion = ref('')

// ====== 审批详情 ======
const detailDlg = ref(false)
const detailData = ref({})

// ====== 流程定义 ======
const flowDlg = ref(false)
const isEditFlow = ref(false)
const editFlowId = ref(null)
const flowFormRef = ref(null)
const flowForm = reactive({
  bizType: '', flowName: '', version: 1, status: 1,
  nodes: [{ nodeName: '', approverType: 'user', approverId: null }]
})
const flowRules = {
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  flowName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'pending') {
      const res = await getMyPending({ page: page.value, size: 20 })
      pendingData.value = res.data.records || []
      total.value = res.data.total || 0
    } else if (activeTab.value === 'initiated') {
      const res = await getMyInitiated({ page: page.value, size: 20 })
      initiatedData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      const res = await getFlowDefList({ page: page.value, size: 20 })
      flowData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally { loading.value = false }
}

const handleTabChange = () => { page.value = 1; fetchData() }

// ====== 审批操作 ======
const handleApprove = (row) => {
  actionRow.value = row; actionType.value = 'approve'
  actionTitle.value = '审批通过'; actionOpinion.value = ''; actionDlg.value = true
}

const handleReject = (row) => {
  actionRow.value = row; actionType.value = 'reject'
  actionTitle.value = '审批驳回'; actionOpinion.value = ''; actionDlg.value = true
}

const submitAction = async () => {
  submitting.value = true
  try {
    if (actionType.value === 'approve') {
      await approveInstance(actionRow.value.id, actionOpinion.value)
      ElMessage.success('审批通过')
    } else {
      await rejectInstance(actionRow.value.id, actionOpinion.value)
      ElMessage.success('已驳回')
    }
    actionDlg.value = false
    fetchData()
  } finally { submitting.value = false }
}

const resetAction = () => { actionRow.value = {}; actionOpinion.value = '' }

// ====== 审批详情 ======
const handleViewDetail = async (row) => {
  try {
    const res = await getInstanceDetail(row.id)
    detailData.value = res.data
    detailDlg.value = true
  } catch { /* ignore */ }
}

// ====== 流程定义 CRUD ======
const handleAddFlow = () => {
  isEditFlow.value = false; editFlowId.value = null
  Object.assign(flowForm, { bizType: '', flowName: '', version: 1, status: 1, nodes: [{ nodeName: '', approverType: 'user', approverId: null }] })
  flowDlg.value = true
}

const handleEditFlow = (row) => {
  isEditFlow.value = true; editFlowId.value = row.id
  let rawNodes = []
  try { rawNodes = JSON.parse(row.nodes_json || '[]') } catch { rawNodes = [] }
  const nodes = rawNodes.map(n => ({
    nodeName: n.node_name || n.nodeName || '',
    approverType: n.approver_type || n.approverType || 'user',
    approverId: n.approver_id || n.approverId || null
  }))
  if (nodes.length === 0) nodes.push({ nodeName: '', approverType: 'user', approverId: null })
  Object.assign(flowForm, { bizType: row.biz_type, flowName: row.flow_name, version: row.version, status: row.status, nodes })
  flowDlg.value = true
}

const handleDeleteFlow = async (row) => {
  await ElMessageBox.confirm('确定删除该流程定义？', '提示', { type: 'warning' })
  await deleteFlowDef(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const addNode = () => {
  flowForm.nodes.push({ nodeName: '', approverType: 'user', approverId: null })
}

const removeNode = (idx) => {
  if (flowForm.nodes.length <= 1) { ElMessage.warning('至少保留一个审批节点'); return }
  flowForm.nodes.splice(idx, 1)
}

const submitFlow = async () => {
  await flowFormRef.value.validate()
  const validNodes = flowForm.nodes.filter(n => n.nodeName && n.approverId)
  if (validNodes.length === 0) { ElMessage.warning('请至少配置一个完整的审批节点'); return }
  const nodesWithOrder = validNodes.map((n, i) => ({
    node_order: i + 1, node_name: n.nodeName,
    approver_type: n.approverType, approver_id: n.approverId
  }))
  const payload = {
    bizType: flowForm.bizType,
    flowName: flowForm.flowName,
    version: flowForm.version,
    status: flowForm.status,
    nodesJson: JSON.stringify(nodesWithOrder)
  }
  submitting.value = true
  try {
    if (isEditFlow.value) {
      await updateFlowDef(editFlowId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createFlowDef(payload)
      ElMessage.success('创建成功')
    }
    flowDlg.value = false
    fetchData()
  } finally { submitting.value = false }
}

const resetFlowForm = () => {
  Object.assign(flowForm, { bizType: '', flowName: '', version: 1, status: 1, nodes: [{ nodeName: '', approverType: 'user', approverId: null }] })
  flowFormRef.value?.resetFields()
}

onMounted(() => { fetchData() })
</script>
