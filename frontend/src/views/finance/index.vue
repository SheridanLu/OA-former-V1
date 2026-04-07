<template>
  <div class="finance-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="结算单" name="statement">
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
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDelete('statement', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="付款申请" name="payment">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="payment_no" label="付款编号" width="140" />
            <el-table-column prop="contract_id" label="合同ID" width="90" />
            <el-table-column prop="amount" label="申请金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="发票管理" name="invoice">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="invoice_no" label="发票编号" width="160" />
            <el-table-column prop="invoice_type" label="类型" width="120" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="报销管理" name="reimburse">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="reimburse_no" label="报销编号" width="140" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ row.amount ? Number(row.amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatementList, deleteStatement, getPaymentList, getInvoiceList, getReimburseList, getCostLedgerList } from '@/api/finance'

const loading = ref(false)
const activeTab = ref('statement')
const tableData = ref([])
const total = ref(0)
const page = ref(1)

const apiMap = {
  statement: getStatementList,
  payment: getPaymentList,
  invoice: getInvoiceList,
  reimburse: getReimburseList,
  costLedger: getCostLedgerList
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

const handleDelete = async (type, row) => {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  if (type === 'statement') await deleteStatement(row.id)
  ElMessage.success('删除成功'); fetchData()
}

onMounted(() => { fetchData() })
</script>
