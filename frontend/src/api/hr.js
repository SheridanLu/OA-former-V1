import request from '@/utils/request'

export function getSalaryList(params) {
  return request.get('/api/v1/hr/salaries', { params })
}

export function createSalary(data) {
  return request.post('/api/v1/hr/salaries', data)
}

export function updateSalary(id, data) {
  return request.put(`/api/v1/hr/salaries/${id}`, data)
}

export function deleteSalary(id) {
  return request.delete(`/api/v1/hr/salaries/${id}`)
}

export function getHrContractList(params) {
  return request.get('/api/v1/hr/contracts', { params })
}

export function createHrContract(data) {
  return request.post('/api/v1/hr/contracts', data)
}

export function updateHrContract(id, data) {
  return request.put(`/api/v1/hr/contracts/${id}`, data)
}

export function deleteHrContract(id) {
  return request.delete(`/api/v1/hr/contracts/${id}`)
}

export function getCertificateList(params) {
  return request.get('/api/v1/hr/certificates', { params })
}

export function createCertificate(data) {
  return request.post('/api/v1/hr/certificates', data)
}

export function updateCertificate(id, data) {
  return request.put(`/api/v1/hr/certificates/${id}`, data)
}

export function deleteCertificate(id) {
  return request.delete(`/api/v1/hr/certificates/${id}`)
}

export function getEntryList(params) {
  return request.get('/api/v1/hr/entries', { params })
}

export function createEntry(data) {
  return request.post('/api/v1/hr/entries', data)
}

export function deleteEntry(id) {
  return request.delete(`/api/v1/hr/entries/${id}`)
}

export function getResignList(params) {
  return request.get('/api/v1/hr/resigns', { params })
}

export function createResign(data) {
  return request.post('/api/v1/hr/resigns', data)
}

export function deleteResign(id) {
  return request.delete(`/api/v1/hr/resigns/${id}`)
}
