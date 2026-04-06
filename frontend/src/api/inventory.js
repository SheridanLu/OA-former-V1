import request from '@/utils/request'

export function getInboundList(params) {
  return request.get('/api/v1/inventory/inbound', { params })
}

export function createInbound(data) {
  return request.post('/api/v1/inventory/inbound', data)
}

export function updateInbound(id, data) {
  return request.put(`/api/v1/inventory/inbound/${id}`, data)
}

export function deleteInbound(id) {
  return request.delete(`/api/v1/inventory/inbound/${id}`)
}

export function getOutboundList(params) {
  return request.get('/api/v1/inventory/outbound', { params })
}

export function createOutbound(data) {
  return request.post('/api/v1/inventory/outbound', data)
}

export function updateOutbound(id, data) {
  return request.put(`/api/v1/inventory/outbound/${id}`, data)
}

export function deleteOutbound(id) {
  return request.delete(`/api/v1/inventory/outbound/${id}`)
}

export function getReturnList(params) {
  return request.get('/api/v1/inventory/return', { params })
}

export function createReturn(data) {
  return request.post('/api/v1/inventory/return', data)
}

export function deleteReturn(id) {
  return request.delete(`/api/v1/inventory/return/${id}`)
}

export function getStockList(params) {
  return request.get('/api/v1/inventory/stock', { params })
}
