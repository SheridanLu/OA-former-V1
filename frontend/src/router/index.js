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
