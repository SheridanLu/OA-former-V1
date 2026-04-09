import request from '@/utils/request'

export function getTodoList(params) {
  return request.get('/api/v1/todos', { params })
}

export function getTodoCount() {
  return request.get('/api/v1/todos/count')
}

export function getTodoStats() {
  return request.get('/api/v1/todos/stats')
}

export function markTodoDone(id) {
  return request.patch(`/api/v1/todos/${id}/done`)
}

export function batchMarkDone(ids) {
  return request.patch('/api/v1/todos/batch-done', { ids })
}

export function remindTodo(id) {
  return request.patch(`/api/v1/todos/${id}/remind`)
}
