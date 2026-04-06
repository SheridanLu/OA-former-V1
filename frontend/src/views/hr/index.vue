<template>
  <div class="hr-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="薪资管理" name="salary">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="month" label="月份" width="100" />
            <el-table-column prop="base_salary" label="基本工资" width="120" align="right" />
            <el-table-column prop="actual_salary" label="实发工资" width="120" align="right" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="劳动合同" name="hrContract">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="contract_type" label="合同类型" width="120" />
            <el-table-column prop="start_date" label="开始日期" width="110" />
            <el-table-column prop="end_date" label="结束日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="证书管理" name="certificate">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="cert_name" label="证书名称" min-width="150" />
            <el-table-column prop="cert_type" label="证书类型" width="120" />
            <el-table-column prop="expire_date" label="到期日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="入职管理" name="entry">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="real_name" label="姓名" width="100" />
            <el-table-column prop="dept_id" label="部门ID" width="90" />
            <el-table-column prop="entry_date" label="入职日期" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="离职管理" name="resign">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="user_id" label="员工ID" width="90" />
            <el-table-column prop="resign_date" label="离职日期" width="110" />
            <el-table-column prop="resign_reason" label="离职原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
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
import { getSalaryList, getHrContractList, getCertificateList, getEntryList, getResignList } from '@/api/hr'

const loading = ref(false)
const activeTab = ref('salary')
const tableData = ref([])
const total = ref(0)
const page = ref(1)

const apiMap = {
  salary: getSalaryList,
  hrContract: getHrContractList,
  certificate: getCertificateList,
  entry: getEntryList,
  resign: getResignList
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

onMounted(() => { fetchData() })
</script>
