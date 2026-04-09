import request from '@/utils/request'

/**
 * 公告管理 API
 */

// ====== 管理端 ======

export function getAnnouncementList(params) {
  return request.get('/api/v1/admin/announcements', { params })
}

export function getAnnouncementDetail(id) {
  return request.get(`/api/v1/admin/announcements/${id}`)
}

export function createAnnouncement(data) {
  return request.post('/api/v1/admin/announcements', data)
}

export function updateAnnouncement(id, data) {
  return request.put(`/api/v1/admin/announcements/${id}`, data)
}

export function submitApproval(id) {
  return request.patch(`/api/v1/admin/announcements/${id}/submit-approval`)
}

export function approveAnnouncement(id, data) {
  return request.patch(`/api/v1/admin/announcements/${id}/approve`, data)
}

export function rejectAnnouncement(id, data) {
  return request.patch(`/api/v1/admin/announcements/${id}/reject`, data)
}

export function publishAnnouncement(id) {
  return request.patch(`/api/v1/admin/announcements/${id}/publish`)
}

export function offlineAnnouncement(id) {
  return request.patch(`/api/v1/admin/announcements/${id}/offline`)
}

export function toggleTopAnnouncement(id) {
  return request.patch(`/api/v1/admin/announcements/${id}/toggle-top`)
}

export function deleteAnnouncement(id) {
  return request.delete(`/api/v1/admin/announcements/${id}`)
}

export function uploadAnnouncementImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', 'announcement')
  formData.append('bizId', 0)
  return request.post('/api/v1/attachments/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ====== 前台（所有登录用户） ======

export function getPublishedAnnouncements(limit = 10) {
  return request.get('/api/v1/announcements', { params: { limit } })
}

export function getPublishedAnnouncementDetail(id) {
  return request.get(`/api/v1/announcements/${id}`)
}

export function getAnnouncementComments(id) {
  return request.get(`/api/v1/announcements/${id}/comments`)
}

export function addAnnouncementComment(id, data) {
  return request.post(`/api/v1/announcements/${id}/comments`, data)
}

export function deleteAnnouncementComment(commentId) {
  return request.delete(`/api/v1/announcements/comments/${commentId}`)
}
