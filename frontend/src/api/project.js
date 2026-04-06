import request from '@/utils/request'

export function getProjectList(params) {
  return request.get('/api/v1/projects', { params })
}

export function getAllProjects() {
  return request.get('/api/v1/projects/all')
}

export function getProjectById(id) {
  return request.get(`/api/v1/projects/${id}`)
}

export function createProject(data) {
  return request.post('/api/v1/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/api/v1/projects/${id}`, data)
}

export function updateProjectStatus(id, status) {
  return request.patch(`/api/v1/projects/${id}/status`, { status })
}

export function deleteProject(id) {
  return request.delete(`/api/v1/projects/${id}`)
}
