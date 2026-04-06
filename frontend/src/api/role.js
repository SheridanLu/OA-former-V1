import request from '@/utils/request'

/**
 * 角色管理 API — 对照 V3.2 §5.9.2
 */

export function getRoleList(params) {
  return request.get('/api/v1/admin/roles', { params })
}

export function getRoleDetail(id) {
  return request.get(`/api/v1/admin/roles/${id}`)
}

export function createRole(data) {
  return request.post('/api/v1/admin/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/api/v1/admin/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/api/v1/admin/roles/${id}`)
}

export function getRolePermissions(id) {
  return request.get(`/api/v1/admin/roles/${id}/permissions`)
}

export function updateRolePermissions(id, permissionIds) {
  return request.put(`/api/v1/admin/roles/${id}/permissions`, { permission_ids: permissionIds })
}
