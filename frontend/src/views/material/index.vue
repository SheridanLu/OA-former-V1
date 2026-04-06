<template>
  <div class="material-page">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="材料名称">
          <el-input v-model="queryForm.materialName" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="queryForm.category" placeholder="请输入" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
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
        <el-button type="primary" @click="handleAdd">新增材料</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="material_code" label="材料编码" width="140" />
        <el-table-column prop="material_name" label="材料名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="spec_model" label="规格型号" width="150" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="base_price" label="基准价" width="110" align="right">
          <template #default="{ row }">{{ row.base_price ? Number(row.base_price).toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 16px; justify-content: flex-end" background
        layout="total, sizes, prev, pager, next, jumper" :total="total" :page-sizes="[10, 20, 50]"
        v-model:current-page="queryForm.page" v-model:page-size="queryForm.size"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑材料' : '新增材料'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="材料名称" prop="materialName">
          <el-input v-model="form.materialName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格型号"><el-input v-model="form.specModel" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit"><el-input v-model="form.unit" placeholder="如：吨、米、个" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类"><el-input v-model="form.category" placeholder="如：钢材、混凝土" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基准价">
              <el-input-number v-model="form.basePrice" :precision="2" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialList, createMaterial, updateMaterial, deleteMaterial } from '@/api/material'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)

const queryForm = reactive({ materialName: '', category: '', status: '', page: 1, size: 20 })

const form = reactive({
  materialName: '', specModel: '', unit: '', category: '', basePrice: null, status: 'active'
})

const rules = {
  materialName: [{ required: true, message: '请输入材料名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMaterialList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.page = 1; fetchData() }
const handleReset = () => { queryForm.materialName = ''; queryForm.category = ''; queryForm.status = ''; queryForm.page = 1; fetchData() }

const handleAdd = () => { isEdit.value = false; editId.value = null; dialogVisible.value = true }

const handleEdit = (row) => {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, {
    materialName: row.material_name, specModel: row.spec_model || '',
    unit: row.unit, category: row.category || '',
    basePrice: row.base_price, status: row.status
  })
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, { materialName: '', specModel: '', unit: '', category: '', basePrice: null, status: 'active' })
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) { await updateMaterial(editId.value, form); ElMessage.success('更新成功') }
    else { await createMaterial(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除材料"${row.material_name}"？`, '提示', { type: 'warning' })
  await deleteMaterial(row.id); ElMessage.success('删除成功'); fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>.search-card { margin-bottom: 0; }</style>
