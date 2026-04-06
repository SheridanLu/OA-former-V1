import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'todos',
        name: 'TodoCenter',
        component: () => import('@/views/todo/index.vue'),
        meta: { title: '待办中心' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/depts',
        name: 'DeptManage',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'system/announcements',
        name: 'AnnouncementManage',
        component: () => import('@/views/system/announcement/index.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'system/audit-logs',
        name: 'AuditLogManage',
        component: () => import('@/views/system/audit-log/index.vue'),
        meta: { title: '审计日志' }
      },
      {
        path: 'system/configs',
        name: 'ConfigManage',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'system/delegations',
        name: 'DelegationManage',
        component: () => import('@/views/system/delegation/index.vue'),
        meta: { title: '委托代理' }
      },
      {
        path: 'projects',
        name: 'ProjectManage',
        component: () => import('@/views/project/index.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'suppliers',
        name: 'SupplierManage',
        component: () => import('@/views/supplier/index.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: 'materials',
        name: 'MaterialManage',
        component: () => import('@/views/material/index.vue'),
        meta: { title: '材料管理' }
      },
      {
        path: 'contracts',
        name: 'ContractManage',
        component: () => import('@/views/contract/index.vue'),
        meta: { title: '合同管理' }
      },
      {
        path: 'purchases',
        name: 'PurchaseManage',
        component: () => import('@/views/purchase/index.vue'),
        meta: { title: '采购管理' }
      },
      {
        path: 'inventory',
        name: 'InventoryManage',
        component: () => import('@/views/inventory/index.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'progress',
        name: 'ProgressManage',
        component: () => import('@/views/progress/index.vue'),
        meta: { title: '进度变更' }
      },
      {
        path: 'finance',
        name: 'FinanceManage',
        component: () => import('@/views/finance/index.vue'),
        meta: { title: '财务管理' }
      },
      {
        path: 'hr',
        name: 'HrManage',
        component: () => import('@/views/hr/index.vue'),
        meta: { title: '人力资源' }
      },
      {
        path: 'completion',
        name: 'CompletionManage',
        component: () => import('@/views/completion/index.vue'),
        meta: { title: '竣工劳务' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title || 'MOCHU-OA'} - 施工管理系统`

  const userStore = useUserStore()

  if (to.meta.public) {
    // 已登录访问登录页，跳转首页
    if (to.path === '/login' && userStore.isLoggedIn) {
      next('/home')
      return
    }
    next()
    return
  }

  // 需要认证的页面
  if (!userStore.isLoggedIn) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  // 获取用户信息（如果还没有）
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.resetState()
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }

  next()
})

export default router
