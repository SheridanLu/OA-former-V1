<template>
  <div class="role-page">
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.roleName" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="role_name" label="角色名称" width="150" />
        <el-table-column prop="role_code" label="角色编码" width="150" />
        <el-table-column prop="data_scope" label="数据权限" width="120">
          <template #default="{ row }">
            {{ dataScopeMap[row.data_scope] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handlePermissions(row)">权限</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="role_name">
          <el-input v-model="form.role_name" />
        </el-form-item>
        <el-form-item label="角色编码" prop="role_code">
          <el-input v-model="form.role_code" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="数据权限" prop="data_scope">
          <el-select v-model="form.data_scope">
            <el-option v-for="(label, key) in dataScopeMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限配置 -->
    <el-dialog v-model="permDialogVisible" title="配置权限" width="480px">
      <el-tree
        ref="permTreeRef"
        :data="permissionList"
        show-checkbox
        node-key="id"
        :props="{ label: 'perm_name', children: 'children' }"
        :default-checked-keys="checkedPermIds"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPerms" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '@/api/role'
import { getPermissionList } from '@/api/permission'

const dataScopeMap = { 1: '全部数据', 2: '本部门', 3: '本项目', 4: '仅个人', 5: '自定义' }

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const permDialogVisible = ref(false)
const permissionList = ref([])
const checkedPermIds = ref([])
const currentRoleId = ref(null)
const formRef = ref(null)
const permTreeRef = ref(null)

const queryForm = reactive({ roleName: '', page: 1, size: 20 })
const form = reactive({ id: null, role_name: '', role_code: '', data_scope: 4, remark: '' })

const rules = {
  role_name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  role_code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRoleList(queryForm)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.page = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { roleName: '', page: 1, size: 20 }); fetchData() }

const handleAdd = () => {
  Object.assign(form, { id: null, role_name: '', role_code: '', data_scope: 4, remark: '' })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, role_name: row.role_name, role_code: row.role_code, data_scope: row.data_scope, remark: row.remark })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateRole(form.id, form)
    } else {
      await createRole(form)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除角色 "${row.role_name}" 吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handlePermissions = async (row) => {
  currentRoleId.value = row.id
  try {
    const [permRes, checkedRes] = await Promise.all([
      getPermissionList(),
      getRolePermissions(row.id)
    ])
    permissionList.value = permRes.data
    checkedPermIds.value = checkedRes.data
    permDialogVisible.value = true
  } catch (e) {}
}

const handleSubmitPerms = async () => {
  submitLoading.value = true
  try {
    const checkedIds = permTreeRef.value.getCheckedKeys()
    const halfIds = permTreeRef.value.getHalfCheckedKeys()
    await updateRolePermissions(currentRoleId.value, [...checkedIds, ...halfIds])
    ElMessage.success('权限配置成功')
    permDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
