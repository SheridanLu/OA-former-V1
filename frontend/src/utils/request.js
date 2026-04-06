import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 生成 UUID v4
 */
function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截器 — 对照 V3.2 §3.2
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 客户端类型标识
    config.headers['X-Client-Type'] = 'pc'
    // 请求追踪ID — V3.2 spec
    config.headers['X-Request-Id'] = uuid()
    // 写操作幂等键 — V3.2 spec
    const method = (config.method || '').toUpperCase()
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      config.headers['X-Idempotency-Key'] = uuid()
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // Token 自动刷新 — 同步到 localStorage 和 Pinia
    const newToken = response.headers['x-new-token']
    if (newToken) {
      localStorage.setItem('token', newToken)
      // 延迟导入避免循环依赖，更新 Pinia store
      import('@/stores/user').then(({ useUserStore }) => {
        const userStore = useUserStore()
        userStore.token = newToken
      })
    }

    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('您没有权限执行此操作')
      } else if (status === 404) {
        ElMessage.error(data?.message || '数据不存在或已被删除')
      } else if (status === 409) {
        ElMessage.error(data?.message || '数据已被修改，请刷新后重试')
      } else if (status === 423) {
        ElMessage.error(data?.message || '账号已锁定')
      } else if (status === 429) {
        ElMessage.error(data?.message || '请求过于频繁')
      } else if (status === 500) {
        ElMessage.error('系统繁忙，请稍后重试')
      } else if (status === 502) {
        ElMessage.error('服务暂时不可用')
      } else {
        ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

export default request
