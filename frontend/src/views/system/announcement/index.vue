<template>
  <div class="announcement-page">
    <!-- 搜索 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable>
            <el-option label="通知" value="notice" />
            <el-option label="制度" value="policy" />
            <el-option label="活动" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="待审批" value="pending_approval" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已发布" value="published" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增公告</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]" size="small">{{ typeNameMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" size="small">{{ statusNameMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="60">
          <template #default="{ row }">
            <el-tag v-if="row.is_top === 1" type="danger" size="small">顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher_name" label="发布人" width="100" />
        <el-table-column prop="approver_name" label="审批人" width="100" />
        <el-table-column prop="publish_time" label="发布时间" width="170" />
        <el-table-column prop="created_at" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="360">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" v-if="row.status === 'draft' || row.status === 'rejected'" @click="handleSubmitApproval(row)">提交审批</el-button>
            <el-button link type="success" v-if="row.status === 'pending_approval'" @click="handleApprove(row)">审批通过</el-button>
            <el-button link type="danger" v-if="row.status === 'pending_approval'" @click="handleReject(row)">驳回</el-button>
            <el-button link type="success" v-if="row.status === 'draft'" @click="handlePublish(row)">发布</el-button>
            <el-button link type="warning" v-if="row.status === 'published'" @click="handleOffline(row)">下线</el-button>
            <el-button link :type="row.is_top === 1 ? 'info' : 'primary'" @click="handleToggleTop(row)">
              {{ row.is_top === 1 ? '取消置顶' : '置顶' }}
            </el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" top="5vh" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type">
            <el-option label="通知" value="notice" />
            <el-option label="制度" value="policy" />
            <el-option label="活动" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="form.expire_time" type="datetime" placeholder="选择过期时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-input v-model="form.scope" placeholder="all 或逗号分隔部门ID" />
        </el-form-item>
        <el-form-item label="公告图片">
          <div class="image-list">
            <div class="image-item" v-for="(img, idx) in imageList" :key="idx">
              <el-image :src="img" style="width: 100px; height: 100px" fit="cover" />
              <el-icon class="image-remove" @click="removeImage(idx)"><Close /></el-icon>
            </div>
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              @change="handleImageSelect"
            >
              <div class="image-upload-btn">
                <el-icon size="24"><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div
            ref="editorRef"
            class="rich-editor"
            contenteditable="true"
            @input="handleEditorInput"
            @paste="handleEditorPaste"
            v-html="form.content"
          ></div>
          <div class="editor-tip">支持粘贴图片：直接Ctrl+V粘贴剪贴板中的图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审批意见弹窗 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalDialogTitle" width="450px">
      <el-form>
        <el-form-item label="审批意见">
          <el-input v-model="approvalRemark" type="textarea" :rows="3" placeholder="请输入审批意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button :type="approvalAction === 'approve' ? 'success' : 'danger'" @click="confirmApproval" :loading="approvalLoading">
          {{ approvalAction === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Plus } from '@element-plus/icons-vue'
import {
  getAnnouncementList, createAnnouncement, updateAnnouncement,
  publishAnnouncement, offlineAnnouncement, toggleTopAnnouncement, deleteAnnouncement,
  submitApproval, approveAnnouncement, rejectAnnouncement, uploadAnnouncementImage
} from '@/api/announcement'

const typeNameMap = { notice: '通知', policy: '制度', activity: '活动' }
const typeTagMap = { notice: '', policy: 'warning', activity: 'success' }
const statusNameMap = {
  draft: '草稿', pending_approval: '待审批', approved: '已通过',
  rejected: '已驳回', published: '已发布', offline: '已下线', expired: '已过期'
}
const statusTagMap = {
  draft: 'info', pending_approval: 'warning', approved: 'success',
  rejected: 'danger', published: 'success', offline: 'warning', expired: 'danger'
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const editorRef = ref(null)
const imageList = ref([])

// 审批弹窗
const approvalDialogVisible = ref(false)
const approvalDialogTitle = ref('')
const approvalAction = ref('')
const approvalRemark = ref('')
const approvalLoading = ref(false)
const approvalTargetId = ref(null)

const queryForm = reactive({ title: '', type: '', status: '', page: 1, size: 20 })
const form = reactive({ id: null, title: '', content: '', type: 'notice', expire_time: null, is_top: 0, scope: 'all', images: '' })

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.page = 1; fetchData() }
const handleReset = () => {
  Object.assign(queryForm, { title: '', type: '', status: '', page: 1, size: 20 })
  fetchData()
}

const handleAdd = () => {
  Object.assign(form, { id: null, title: '', content: '', type: 'notice', expire_time: null, is_top: 0, scope: 'all', images: '' })
  imageList.value = []
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
  nextTick(() => {
    if (editorRef.value) editorRef.value.innerHTML = ''
  })
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id, title: row.title, content: row.content, type: row.type,
    expire_time: row.expire_time, is_top: row.is_top, scope: row.scope,
    images: row.images || ''
  })
  try {
    imageList.value = row.images ? JSON.parse(row.images) : []
  } catch { imageList.value = [] }
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
  nextTick(() => {
    if (editorRef.value) editorRef.value.innerHTML = form.content || ''
  })
}

const handleEditorInput = () => {
  if (editorRef.value) {
    form.content = editorRef.value.innerHTML
  }
}

const handleEditorPaste = async (e) => {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (!file) return
      try {
        const res = await uploadAnnouncementImage(file)
        const url = res.data.file_path || res.data.filePath
        if (url) {
          document.execCommand('insertImage', false, url)
          form.content = editorRef.value.innerHTML
        }
      } catch {
        ElMessage.error('图片上传失败')
      }
      return
    }
  }
}

const handleImageSelect = async (uploadFile) => {
  try {
    const res = await uploadAnnouncementImage(uploadFile.raw)
    const url = res.data.file_path || res.data.filePath
    if (url) {
      imageList.value.push(url)
      form.images = JSON.stringify(imageList.value)
    }
  } catch {
    ElMessage.error('图片上传失败')
  }
}

const removeImage = (idx) => {
  imageList.value.splice(idx, 1)
  form.images = JSON.stringify(imageList.value)
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    form.images = JSON.stringify(imageList.value)
    if (form.id) {
      await updateAnnouncement(form.id, form)
    } else {
      await createAnnouncement(form)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleSubmitApproval = (row) => {
  ElMessageBox.confirm(`确定提交公告 "${row.title}" 进行审批吗？`, '提交审批').then(async () => {
    await submitApproval(row.id)
    ElMessage.success('已提交审批')
    fetchData()
  }).catch(() => {})
}

const handleApprove = (row) => {
  approvalTargetId.value = row.id
  approvalAction.value = 'approve'
  approvalDialogTitle.value = `审批通过 — ${row.title}`
  approvalRemark.value = ''
  approvalDialogVisible.value = true
}

const handleReject = (row) => {
  approvalTargetId.value = row.id
  approvalAction.value = 'reject'
  approvalDialogTitle.value = `驳回 — ${row.title}`
  approvalRemark.value = ''
  approvalDialogVisible.value = true
}

const confirmApproval = async () => {
  approvalLoading.value = true
  try {
    const data = { remark: approvalRemark.value }
    if (approvalAction.value === 'approve') {
      await approveAnnouncement(approvalTargetId.value, data)
      ElMessage.success('审批通过，已自动发布')
    } else {
      await rejectAnnouncement(approvalTargetId.value, data)
      ElMessage.success('已驳回')
    }
    approvalDialogVisible.value = false
    fetchData()
  } finally {
    approvalLoading.value = false
  }
}

const handlePublish = (row) => {
  ElMessageBox.confirm(`确定发布公告 "${row.title}" 吗？`, '提示').then(async () => {
    await publishAnnouncement(row.id)
    ElMessage.success('发布成功')
    fetchData()
  }).catch(() => {})
}

const handleOffline = (row) => {
  ElMessageBox.confirm(`确定下线公告 "${row.title}" 吗？`, '提示').then(async () => {
    await offlineAnnouncement(row.id)
    ElMessage.success('已下线')
    fetchData()
  }).catch(() => {})
}

const handleToggleTop = async (row) => {
  await toggleTopAnnouncement(row.id)
  ElMessage.success(row.is_top === 1 ? '已取消置顶' : '已置顶')
  fetchData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除公告 "${row.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.search-card { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }

.rich-editor {
  width: 100%;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  background: #fff;

  &:focus {
    border-color: #409eff;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
    margin: 8px 0;
  }
}

.editor-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;

  .image-remove {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    background: #f56c6c;
    color: #fff;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;

    &:hover {
      background: #e6413e;
    }
  }
}

.image-upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  gap: 4px;
  font-size: 12px;

  &:hover {
    border-color: #409eff;
    color: #409eff;
  }
}
</style>
