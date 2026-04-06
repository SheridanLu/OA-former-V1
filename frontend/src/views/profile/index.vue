<template>
  <div class="profile-page">
    <el-card>
      <template #header><span>个人信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.real_name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ userStore.userInfo?.dept_name }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ userStore.userInfo?.position }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.profile-page { max-width: 800px; }
</style>
