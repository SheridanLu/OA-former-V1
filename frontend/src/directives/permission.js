import { useUserStore } from '@/stores/user'

/**
 * v-permission 指令 — 对照 V3.2 权限控制
 * 用法: v-permission="'user:create'" 或 v-permission="['user:create', 'user:update']"
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []

    if (value) {
      const required = Array.isArray(value) ? value : [value]
      const hasPermission = required.some(p => permissions.includes(p))

      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}
