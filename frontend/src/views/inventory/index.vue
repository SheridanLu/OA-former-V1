<template>
  <div class="inventory-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="入库管理" name="inbound">
          <el-table :data="inboundData" v-loading="loading" stripe border>
            <el-table-column prop="inbound_no" label="入库单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="contract_id" label="合同ID" width="100" />
            <el-table-column prop="warehouse" label="仓库" width="120" />
            <el-table-column prop="inbound_date" label="入库日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
          <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
            layout="total, prev, pager, next" :total="total"
            v-model:current-page="page" @current-change="fetchData" />
        </el-tab-pane>

        <el-tab-pane label="出库管理" name="outbound">
          <el-table :data="outboundData" v-loading="loading" stripe border>
            <el-table-column prop="outbound_no" label="出库单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="outbound_type" label="出库类型" width="120" />
            <el-table-column prop="outbound_date" label="出库日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="退货管理" name="return">
          <el-table :data="returnData" v-loading="loading" stripe border>
            <el-table-column prop="return_no" label="退货单号" width="140" />
            <el-table-column prop="project_id" label="项目ID" width="90" />
            <el-table-column prop="dispose_method" label="处理方式" width="120" />
            <el-table-column prop="return_date" label="退货日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInboundList, getOutboundList, getReturnList, getStockList } from '@/api/inventory'

const loading = ref(false)
const activeTab = ref('inbound')
const inboundData = ref([])
const outboundData = ref([])
const returnData = ref([])
const stockData = ref([])
const total = ref(0)
const page = ref(1)

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

const handleTabChange = () => { page.value = 1; fetchData() }

onMounted(() => { fetchData() })
</script>
