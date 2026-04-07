<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">MOCHU-OA</span>
        <span v-else>M</span>
      </div>
      <div class="menu-scroll">
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
          <el-menu-item index="/todos">
            <el-icon><List /></el-icon>
            <template #title>待办中心</template>
          </el-menu-item>

          <el-menu-item index="/projects">
            <el-icon><Folder /></el-icon>
            <template #title>项目管理</template>
          </el-menu-item>
          <el-menu-item index="/suppliers">
            <el-icon><Van /></el-icon>
            <template #title>供应商管理</template>
          </el-menu-item>
          <el-menu-item index="/materials">
            <el-icon><Box /></el-icon>
            <template #title>材料管理</template>
          </el-menu-item>
          <el-menu-item index="/contracts">
            <el-icon><Tickets /></el-icon>
            <template #title>合同管理</template>
          </el-menu-item>
          <el-menu-item index="/purchases">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>采购管理</template>
          </el-menu-item>
          <el-menu-item index="/inventory">
            <el-icon><House /></el-icon>
            <template #title>库存管理</template>
          </el-menu-item>
          <el-menu-item index="/progress">
            <el-icon><DataLine /></el-icon>
            <template #title>进度变更</template>
          </el-menu-item>
          <el-menu-item index="/finance">
            <el-icon><Money /></el-icon>
            <template #title>财务管理</template>
          </el-menu-item>
          <el-menu-item index="/hr">
            <el-icon><Avatar /></el-icon>
            <template #title>人力资源</template>
          </el-menu-item>
          <el-menu-item index="/completion">
            <el-icon><Finished /></el-icon>
            <template #title>竣工劳务</template>
          </el-menu-item>
          <el-menu-item index="/approval">
            <el-icon><Stamp /></el-icon>
            <template #title>流程审批</template>
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
            <el-menu-item index="/system/announcements">
              <el-icon><Bell /></el-icon>
              <template #title>公告管理</template>
            </el-menu-item>
            <el-menu-item index="/system/audit-logs">
              <el-icon><Document /></el-icon>
              <template #title>审计日志</template>
            </el-menu-item>
            <el-menu-item index="/system/configs">
              <el-icon><Tools /></el-icon>
              <template #title>系统配置</template>
            </el-menu-item>
            <el-menu-item index="/system/delegations">
              <el-icon><Switch /></el-icon>
              <template #title>委托代理</template>
            </el-menu-item>
            <el-menu-item index="/system/contract-tpl">
              <el-icon><Tickets /></el-icon>
              <template #title>合同模板</template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
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
import { HomeFilled, Setting, User, Key, OfficeBuilding, Bell, List, Fold, Expand, Document, Tools, Switch, Folder, Van, Box, Tickets, ShoppingCart, House, DataLine, Money, Avatar, Finished, Stamp } from '@element-plus/icons-vue'

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
  display: flex;
  flex-direction: column;

  .el-menu {
    border-right: none;
    // 非折叠时子菜单内联展开，不需要额外处理
  }
}

// 折叠模式下弹出菜单样式修正
:deep(.el-menu--vertical.el-menu--collapse .el-sub-menu .el-menu--inline) {
  display: none !important;
}
:deep(.el-menu--vertical:not(.el-menu--collapse) .el-sub-menu .el-menu--inline) {
  display: block;
}

.menu-scroll {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: thin;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }

  // 确保子菜单展开后可见且可滚动
  .el-sub-menu .el-menu {
    background-color: #1f2d3d !important;
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
