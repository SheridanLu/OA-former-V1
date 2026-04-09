import request from '@/utils/request'

export function getContractList(params) {
  return request.get('/api/v1/contracts', { params })
}

export function getContractById(id) {
  return request.get(`/api/v1/contracts/${id}`)
}

export function createContract(data) {
  return request.post('/api/v1/contracts', data)
}

export function updateContract(id, data) {
  return request.put(`/api/v1/contracts/${id}`, data)
}

export function saveContractContent(id, content) {
  return request.put(`/api/v1/contracts/${id}/content`, { content })
}

export function submitContractApproval(id) {
  return request.patch(`/api/v1/contracts/${id}/submit-approval`)
}

export function approveContract(id, data) {
  return request.patch(`/api/v1/contracts/${id}/approve`, data)
}

export function rejectContract(id, data) {
  return request.patch(`/api/v1/contracts/${id}/reject`, data)
}

export function previewContractContent(id) {
  return request.get(`/api/v1/contracts/${id}/preview`)
}

export function getPrintableContract(id) {
  return request.get(`/api/v1/contracts/${id}/printable`)
}

export function updateContractStatus(id, status) {
  return request.patch(`/api/v1/contracts/${id}/status`, { status })
}

export function deleteContract(id) {
  return request.delete(`/api/v1/contracts/${id}`)
}
