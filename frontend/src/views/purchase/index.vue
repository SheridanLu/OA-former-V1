<template>
  <div class="purchase-page">
    <el-card shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="待审批" value="pending" />
            <el-option label="已审批" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="采购清单" name="list">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddPurchase">新建采购清单</el-button>
          </div>
          <el-table :data="purchaseData" v-loading="loading" stripe border>
            <el-table-column prop="list_no" label="清单编号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="total_amount" label="总金额" width="130" align="right">
              <template #default="{ row }">{{ row.total_amount ? Number(row.total_amount).toLocaleString() : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="{'draft':'info','pending':'warning','approved':'success','rejected':'danger'}[row.status]" size="small">
                  {{ {'draft':'草稿','pending':'待审批','approved':'已审批','rejected':'已驳回'}[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDeletePurchase(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="purchaseTotal > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="purchaseTotal"
            v-model:current-page="queryForm.page" @current-change="fetchPurchases" />
        </el-tab-pane>

        <el-tab-pane label="零星采购" name="spot">
          <div style="margin-bottom: 12px">
            <el-button type="primary" @click="handleAddSpot">新建零星采购</el-button>
          </div>
          <el-table :data="spotData" v-loading="spotLoading" stripe border>
            <el-table-column prop="purchase_no" label="采购编号" width="140" />
            <el-table-column prop="item_name" label="物品名称" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="100" align="right" />
            <el-table-column prop="unit_price" label="单价" width="110" align="right" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ Number(row.amount).toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="{'draft':'info','pending':'warning','approved':'success'}[row.status]" size="small">
                  {{ {'draft':'草稿','pending':'待审批','approved':'已审批'}[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDeleteSpot(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="spotTotal > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="spotTotal"
            v-model:current-page="spotPage" @current-change="fetchSpots" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseList, deletePurchase, getSpotPurchaseList, deleteSpotPurchase } from '@/api/purchase'

const loading = ref(false)
const spotLoading = ref(false)
const purchaseData = ref([])
const spotData = ref([])
const purchaseTotal = ref(0)
const spotTotal = ref(0)
const activeTab = ref('list')
const spotPage = ref(1)

const queryForm = reactive({ status: '', page: 1, size: 20 })

const fetchPurchases = async () => {
  loading.value = true
  try {
    const res = await getPurchaseList(queryForm)
    purchaseData.value = res.data.records || []
    purchaseTotal.value = res.data.total || 0
  } finally { loading.value = false }
}

const fetchSpots = async () => {
  spotLoading.value = true
  try {
    const res = await getSpotPurchaseList({ page: spotPage.value, size: 20 })
    spotData.value = res.data.records || []
    spotTotal.value = res.data.total || 0
  } finally { spotLoading.value = false }
}

const handleSearch = () => { queryForm.page = 1; fetchPurchases() }
const handleReset = () => { queryForm.status = ''; queryForm.page = 1; fetchPurchases() }

const handleAddPurchase = () => { ElMessage.info('采购清单创建功能开发中') }
const handleAddSpot = () => { ElMessage.info('零星采购创建功能开发中') }

const handleDeletePurchase = async (row) => {
  await ElMessageBox.confirm('确定删除该采购清单？', '提示', { type: 'warning' })
  await deletePurchase(row.id); ElMessage.success('删除成功'); fetchPurchases()
}

const handleDeleteSpot = async (row) => {
  await ElMessageBox.confirm('确定删除该零星采购？', '提示', { type: 'warning' })
  await deleteSpotPurchase(row.id); ElMessage.success('删除成功'); fetchSpots()
}

onMounted(() => { fetchPurchases(); fetchSpots() })
</script>
