import request from '@/utils/request'

/**
 * 交易审批规则 API — 12 端点
 *
 * 基础路径: /api/v1/deal-approval-rules
 * 写操作: POST
 * match/test-match: GET
 */

export function pageDealApprovalRule(data) {
  return request({
    url: '/api/v1/deal-approval-rules/page',
    method: 'post',
    data
  })
}

export function getDealApprovalRule(id) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}`,
    method: 'get'
  })
}

export function saveDealApprovalRule(data) {
  return request({
    url: '/api/v1/deal-approval-rules',
    method: 'post',
    data
  })
}

export function updateDealApprovalRule(data) {
  return request({
    url: '/api/v1/deal-approval-rules/update',
    method: 'post',
    data
  })
}

export function deleteDealApprovalRule(id) {
  return request({
    url: `/api/v1/deal-approval-rules/delete/${id}`,
    method: 'post'
  })
}

export function enableDealApprovalRule(id) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}/enable`,
    method: 'post'
  })
}

export function disableDealApprovalRule(id) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}/disable`,
    method: 'post'
  })
}

/**
 * 运行时匹配
 * @param params {managementEntityId?, counterpartyId?, instrumentId?, dealerId?, actionType}
 */
export function matchDealApprovalRule(params) {
  return request({
    url: '/api/v1/deal-approval-rules/match',
    method: 'get',
    params
  })
}

/**
 * 测试匹配 — 返回候选规则列表
 */
export function testMatchDealApprovalRule(params) {
  return request({
    url: '/api/v1/deal-approval-rules/test-match',
    method: 'get',
    params
  })
}

export function getDealApprovalRuleAuditLogs(id, params) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}/audit-logs`,
    method: 'get',
    params
  })
}

export function getDealApprovalRuleReferenceCount(id) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}/reference-count`,
    method: 'get'
  })
}

export function getDealApprovalRuleImages(id, params) {
  return request({
    url: `/api/v1/deal-approval-rules/${id}/images`,
    method: 'get',
    params
  })
}
