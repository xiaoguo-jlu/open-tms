import request from '@/utils/request'

export function listApprovalRule(params) {
  return request({
    url: '/api/v1/approval-rules/page',
    method: 'get',
    params
  })
}

export function getApprovalRule(id) {
  return request({
    url: `/api/v1/approval-rules/${id}`,
    method: 'get'
  })
}

export function saveApprovalRule(data) {
  return request({
    url: '/api/v1/approval-rules',
    method: 'post',
    data
  })
}

export function updateApprovalRule(data) {
  return request({
    url: '/api/v1/approval-rules/update',
    method: 'post',
    data
  })
}

export function deleteApprovalRule(id) {
  return request({
    url: `/api/v1/approval-rules/${id}`,
    method: 'delete'
  })
}