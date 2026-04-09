import request from '@/utils/request'

// ====== 里程碑管理 ======

export function getMilestoneList(params) {
  return request.get('/api/v1/progress/milestones', { params })
}

export function getAllMilestones(params) {
  return request.get('/api/v1/progress/milestones/all', { params })
}

export function createMilestone(data) {
  return request.post('/api/v1/progress/milestones', data)
}

export function updateMilestone(id, data) {
  return request.put(`/api/v1/progress/milestones/${id}`, data)
}

export function deleteMilestone(id) {
  return request.delete(`/api/v1/progress/milestones/${id}`)
}

export function getMilestoneDeps(id) {
  return request.get(`/api/v1/progress/milestones/${id}/deps`)
}

export function submitMilestoneApproval(id) {
  return request.patch(`/api/v1/progress/milestones/${id}/submit-approval`)
}

export function approveMilestone(id, data) {
  return request.patch(`/api/v1/progress/milestones/${id}/approve`, data)
}

export function rejectMilestone(id, data) {
  return request.patch(`/api/v1/progress/milestones/${id}/reject`, data)
}

export function lockMilestone(id) {
  return request.patch(`/api/v1/progress/milestones/${id}/lock`)
}

export function completeMilestone(id, data) {
  return request.patch(`/api/v1/progress/milestones/${id}/complete`, data)
}

// ====== 甘特任务 ======

export function getGanttTaskList(params) {
  return request.get('/api/v1/progress/gantt', { params })
}

export function getGanttTree(params) {
  return request.get('/api/v1/progress/gantt/tree', { params })
}

export function getGanttTaskById(id) {
  return request.get(`/api/v1/progress/gantt/${id}`)
}

export function createGanttTask(data) {
  return request.post('/api/v1/progress/gantt', data)
}

export function updateGanttTask(id, data) {
  return request.put(`/api/v1/progress/gantt/${id}`, data)
}

export function updateGanttTaskStatus(id, status) {
  return request.patch(`/api/v1/progress/gantt/${id}/status`, { status })
}

export function deleteGanttTask(id) {
  return request.delete(`/api/v1/progress/gantt/${id}`)
}

export function startTask(id) {
  return request.patch(`/api/v1/progress/gantt/${id}/start`)
}

export function submitTaskReview(id) {
  return request.patch(`/api/v1/progress/gantt/${id}/submit-review`)
}

export function approveTask(id, data) {
  return request.patch(`/api/v1/progress/gantt/${id}/approve`, data)
}

export function rejectTask(id, data) {
  return request.patch(`/api/v1/progress/gantt/${id}/reject`, data)
}

export function updateTaskProgress(id, progressPct) {
  return request.patch(`/api/v1/progress/gantt/${id}/progress`, { progressPct })
}

export function getTaskDependencies(id) {
  return request.get(`/api/v1/progress/gantt/${id}/dependencies`)
}

// ====== 预警管理 ======

export function getWarnings(params) {
  return request.get('/api/v1/progress/warnings', { params })
}

export function getWarningConfigs(params) {
  return request.get('/api/v1/progress/warning-configs', { params })
}

export function updateWarningConfig(id, data) {
  return request.put(`/api/v1/progress/warning-configs/${id}`, data)
}

// ====== 延期原因 ======

export function addDelayReason(data) {
  return request.post('/api/v1/progress/delay-reasons', data)
}

export function getDelayReasons(taskId) {
  return request.get(`/api/v1/progress/gantt/${taskId}/delay-reasons`)
}

// ====== 审计日志 ======

export function getAuditLogs(params) {
  return request.get('/api/v1/progress/audit-logs', { params })
}

// ====== 统计分析 ======

export function getProjectStats(params) {
  return request.get('/api/v1/progress/stats', { params })
}

export function getStatusDistribution(params) {
  return request.get('/api/v1/progress/stats/status-distribution', { params })
}

export function calcCriticalPath(projectId) {
  return request.post('/api/v1/progress/calc-critical-path', null, { params: { projectId } })
}

// ====== 导出 ======

export function exportProgressPlan(params) {
  return request.get('/api/v1/progress/export/progress-plan', { params })
}

export function exportMilestoneReport(params) {
  return request.get('/api/v1/progress/export/milestone-report', { params })
}

export function exportAcceptanceDoc(milestoneId) {
  return request.get(`/api/v1/progress/export/acceptance/${milestoneId}`)
}

// ====== 变更管理 ======

export function getChangeOrderList(params) {
  return request.get('/api/v1/progress/changes', { params })
}

export function getChangeOrderById(id) {
  return request.get(`/api/v1/progress/changes/${id}`)
}

export function getChangeOrderDetails(id) {
  return request.get(`/api/v1/progress/changes/${id}/details`)
}

export function createChangeOrder(data) {
  return request.post('/api/v1/progress/changes', data)
}

export function updateChangeOrder(id, data) {
  return request.put(`/api/v1/progress/changes/${id}`, data)
}

export function updateChangeOrderStatus(id, status) {
  return request.patch(`/api/v1/progress/changes/${id}/status`, { status })
}

export function deleteChangeOrder(id) {
  return request.delete(`/api/v1/progress/changes/${id}`)
}
