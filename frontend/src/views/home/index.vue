<template>
  <div class="home-page">
    <!-- 欢迎栏 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div>
          <h2>{{ greeting }}，{{ userStore.realName || userStore.username || '用户' }}</h2>
          <p class="welcome-date">{{ currentDate }}</p>
        </div>
        <div class="todo-badge" v-if="homeData.todo_count > 0">
          <el-badge :value="homeData.todo_count" type="danger">
            <el-button type="primary" plain>待办事项</el-button>
          </el-badge>
        </div>
      </div>
    </el-card>

    <!-- 公告图片轮播 -->
    <el-card class="carousel-card" v-if="carouselImages.length > 0">
      <template #header>
        <span>公告图片</span>
      </template>
      <el-carousel :interval="4000" height="280px" indicator-position="outside">
        <el-carousel-item v-for="(img, idx) in carouselImages" :key="idx">
          <div class="carousel-wrapper" @click="openAnnouncementDetail(img.announcementId)">
            <img :src="img.url" :alt="img.title" class="carousel-img" />
            <div class="carousel-title">{{ img.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 快捷入口 -->
    <el-card class="shortcut-card" v-if="homeData.shortcuts && homeData.shortcuts.length">
      <template #header>
        <span>快捷入口</span>
      </template>
      <div class="shortcut-grid">
        <div
          class="shortcut-item"
          v-for="item in homeData.shortcuts"
          :key="item.func_code"
        >
          <el-icon size="28"><component :is="item.func_icon || 'Grid'" /></el-icon>
          <span>{{ item.func_name }}</span>
        </div>
      </div>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="announcement-card">
      <template #header>
        <span>最新公告</span>
      </template>
      <div v-if="homeData.announcements && homeData.announcements.length">
        <div
          class="announcement-item"
          v-for="item in homeData.announcements"
          :key="item.id"
          @click="openAnnouncementDetail(item.id)"
        >
          <div class="announcement-left">
            <el-tag v-if="item.is_top === 1" type="danger" size="small" class="top-tag">顶</el-tag>
            <span class="announcement-title">{{ item.title }}</span>
          </div>
          <span class="announcement-time">{{ item.publish_time }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无公告" :image-size="80" />
    </el-card>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailData.title" width="700px" top="5vh" destroy-on-close>
      <div class="detail-meta">
        <span>发布人：{{ detailData.publisher_name }}</span>
        <span>发布时间：{{ detailData.publish_time }}</span>
      </div>
      <!-- 详情图片轮播 -->
      <el-carousel v-if="detailImages.length > 0" :interval="4000" height="300px" class="detail-carousel">
        <el-carousel-item v-for="(img, idx) in detailImages" :key="idx">
          <img :src="img" class="detail-carousel-img" />
        </el-carousel-item>
      </el-carousel>
      <div class="detail-content" v-html="detailData.content"></div>

      <!-- 评论区 -->
      <el-divider>评论 ({{ comments.length }})</el-divider>
      <div class="comment-section">
        <div class="comment-input">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="2"
            placeholder="发表评论..."
            maxlength="500"
            show-word-limit
          />
          <el-button type="primary" @click="handleAddComment" :loading="commentLoading" :disabled="!commentText.trim()">
            发表
          </el-button>
        </div>
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-header">
              <span class="comment-user">{{ c.user_name || c.userName }}</span>
              <span class="comment-time">{{ c.created_at || c.createdAt }}</span>
            </div>
            <div class="comment-body">{{ c.content }}</div>
            <el-button
              v-if="c.user_id === userStore.userId || c.userId === userStore.userId"
              link type="danger" size="small"
              @click="handleDeleteComment(c.id)"
            >删除</el-button>
          </div>
        </div>
        <el-empty v-else description="暂无评论" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getHomeData } from '@/api/auth'
import {
  getPublishedAnnouncementDetail, getAnnouncementComments,
  addAnnouncementComment, deleteAnnouncementComment
} from '@/api/announcement'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')

const userStore = useUserStore()

const homeData = reactive({
  todo_count: 0,
  announcements: [],
  shortcuts: []
})

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 轮播图片：从公告的images字段提取
const carouselImages = computed(() => {
  const imgs = []
  if (!homeData.announcements) return imgs
  for (const ann of homeData.announcements) {
    if (ann.images) {
      try {
        const parsed = JSON.parse(ann.images)
        if (Array.isArray(parsed)) {
          for (const url of parsed) {
            imgs.push({ url, title: ann.title, announcementId: ann.id })
          }
        }
      } catch { /* ignore */ }
    }
  }
  return imgs
})

// 公告详情
const detailVisible = ref(false)
const detailData = ref({})
const detailImages = ref([])
const comments = ref([])
const commentText = ref('')
const commentLoading = ref(false)

const openAnnouncementDetail = async (id) => {
  try {
    const res = await getPublishedAnnouncementDetail(id)
    detailData.value = res.data
    // 解析图片
    try {
      detailImages.value = res.data.images ? JSON.parse(res.data.images) : []
    } catch { detailImages.value = [] }
    detailVisible.value = true
    // 加载评论
    loadComments(id)
  } catch {
    ElMessage.error('获取公告详情失败')
  }
}

const loadComments = async (id) => {
  try {
    const res = await getAnnouncementComments(id)
    comments.value = res.data || []
  } catch {
    comments.value = []
  }
}

const handleAddComment = async () => {
  if (!commentText.value.trim()) return
  commentLoading.value = true
  try {
    await addAnnouncementComment(detailData.value.id, { content: commentText.value })
    commentText.value = ''
    ElMessage.success('评论成功')
    loadComments(detailData.value.id)
  } catch {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await deleteAnnouncementComment(commentId)
    ElMessage.success('已删除')
    loadComments(detailData.value.id)
  } catch {
    ElMessage.error('删除失败')
  }
}

let pollTimer = null

const fetchHome = async () => {
  try {
    const res = await getHomeData()
    Object.assign(homeData, res.data)
  } catch (e) {
    // error handled by interceptor
  }
}

onMounted(() => {
  fetchHome()
  pollTimer = setInterval(fetchHome, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  h2 {
    font-size: 20px;
    color: #303133;
    margin-bottom: 8px;
  }

  .welcome-date {
    color: #909399;
    font-size: 14px;
  }
}

// 轮播
.carousel-card {
  .carousel-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    cursor: pointer;
  }

  .carousel-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 4px;
  }

  .carousel-title {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 16px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
    color: #fff;
    font-size: 14px;
    border-radius: 0 0 4px 4px;
  }
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  color: #606266;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }

  span {
    font-size: 13px;
  }
}

.announcement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f5f7fa;
  }
}

.announcement-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  flex-shrink: 0;
}

.announcement-title {
  color: #303133;

  &:hover {
    color: #409eff;
  }
}

.announcement-time {
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
}

// 详情弹窗
.detail-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-carousel {
  margin-bottom: 16px;

  .detail-carousel-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #f5f7fa;
  }
}

.detail-content {
  line-height: 1.8;
  font-size: 14px;
  color: #303133;

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
    margin: 8px 0;
  }
}

// 评论区
.comment-section {
  margin-top: 12px;
}

.comment-input {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;

  .el-input {
    flex: 1;
  }
}

.comment-list {
  max-height: 400px;
  overflow-y: auto;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.comment-user {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.comment-time {
  color: #c0c4cc;
  font-size: 12px;
}

.comment-body {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 4px;
}
</style>
