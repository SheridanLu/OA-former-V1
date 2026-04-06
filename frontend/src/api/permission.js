import request from '@/utils/request'

/**
 * 权限 API — 对照 V3.2 §5.9.5
 */

export function getPermissionList(module) {
  const params = module ? { module } : {}
  return request.get('/api/v1/admin/permissions', { params })
}
