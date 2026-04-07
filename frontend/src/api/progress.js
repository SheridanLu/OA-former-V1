import request from '@/utils/request'

export function getGanttTaskList(params) {
  return request.get('/api/v1/progress/gantt', { params })
}

export function createGanttTask(data) {
  return request.post('/api/v1/progress/gantt', data)
}

export function updateGanttTask(id, data) {
  return request.put(`/api/v1/progress/gantt/${id}`, data)
}

export function deleteGanttTask(id) {
  return request.delete(`/api/v1/progress/gantt/${id}`)
}

export function getChangeOrderDetails(id) {
  return request.get(`/api/v1/progress/changes/${id}/details`)
}

export function getChangeOrderList(params) {
  return request.get('/api/v1/progress/changes', { params })
}

export function createChangeOrder(data) {
  return request.post('/api/v1/progress/changes', data)
}

export function updateChangeOrder(id, data) {
  return request.put(`/api/v1/progress/changes/${id}`, data)
}

export function deleteChangeOrder(id) {
  return request.delete(`/api/v1/progress/changes/${id}`)
}
