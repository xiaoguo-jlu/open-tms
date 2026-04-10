import request from '@/utils/request'

export function listWorkflowTemplate(params) {
  return request({
    url: '/approval/templates',
    method: 'get',
    params
  })
}

export function getWorkflowTemplate(id) {
  return request({
    url: `/approval/templates/${id}`,
    method: 'get'
  })
}

export function saveWorkflowTemplate(data) {
  return request({
    url: '/approval/templates',
    method: 'post',
    data
  })
}

export function updateWorkflowTemplate(data) {
  return request({
    url: '/approval/templates',
    method: 'put',
    data
  })
}

export function deleteWorkflowTemplate(id) {
  return request({
    url: `/approval/templates/${id}`,
    method: 'delete'
  })
}

export function listWorkflowNode(templateId) {
  return request({
    url: `/approval/templates/${templateId}/nodes`,
    method: 'get'
  })
}

export function saveWorkflowNode(templateId, data) {
  return request({
    url: `/approval/templates/${templateId}/nodes`,
    method: 'post',
    data
  })
}

export function deleteWorkflowNode(templateId, nodeId) {
  return request({
    url: `/approval/templates/${templateId}/nodes/${nodeId}`,
    method: 'delete'
  })
}

export function listApprovalTask(params) {
  return request({
    url: '/approval/tasks',
    method: 'get',
    params
  })
}

export function getApprovalTask(id) {
  return request({
    url: `/approval/tasks/${id}`,
    method: 'get'
  })
}

export function approveTask(id, data) {
  return request({
    url: `/approval/tasks/${id}/approve`,
    method: 'post',
    data
  })
}

export function rejectTask(id, data) {
  return request({
    url: `/approval/tasks/${id}/reject`,
    method: 'post',
    data
  })
}

export function transferTask(id, data) {
  return request({
    url: `/approval/tasks/${id}/transfer`,
    method: 'post',
    data
  })
}

export function delegateTask(id, data) {
  return request({
    url: `/approval/tasks/${id}/delegate`,
    method: 'post',
    data
  })
}

export function listApprovalHistory(businessId) {
  return request({
    url: `/approval/history/${businessId}`,
    method: 'get'
  })
}