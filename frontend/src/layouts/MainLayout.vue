<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">MOCHU-OA</span>
        <span v-else>M</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/system/roles">
            <el-icon><Key /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          <el-menu-item index="/system/depts">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>部门管理</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="username">{{ userStore.realName || userStore.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Setting, User, Key, OfficeBuilding, Fold, Expand } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const isCollapse = ref(false)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;

  .el-menu {
    border-right: none;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 60px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
