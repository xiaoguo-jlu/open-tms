import request from '@/utils/request'

/**
 * 默认银行账户规则 API(v1.1 — 11 端点)
 *
 * 写操作:POST 规范
 * match:GET(运行时,支持 dualDirection=true)
 */

export function pageDefaultBankAccountRule(data) {
  return request({
    url: '/api/v1/default-bank-account-rules/page',
    method: 'post',
    data
  })
}

export function getDefaultBankAccountRule(id) {
  return request({
    url: `/api/v1/default-bank-account-rules/${id}`,
    method: 'get'
  })
}

export function saveDefaultBankAccountRule(data) {
  return request({
    url: '/api/v1/default-bank-account-rules',
    method: 'post',
    data
  })
}

export function updateDefaultBankAccountRule(data) {
  return request({
    url: '/api/v1/default-bank-account-rules/update',
    method: 'post',
    data
  })
}

export function deleteDefaultBankAccountRule(id) {
  return request({
    url: `/api/v1/default-bank-account-rules/delete/${id}`,
    method: 'post'
  })
}

export function enableDefaultBankAccountRule(id) {
  return request({
    url: `/api/v1/default-bank-account-rules/${id}/enable`,
    method: 'post'
  })
}

export function disableDefaultBankAccountRule(id) {
  return request({
    url: `/api/v1/default-bank-account-rules/${id}/disable`,
    method: 'post'
  })
}

/**
 * ★ v1.1 运行时匹配(支持双方向)
 * @param params {managementEntityId, counterpartyId?, instrumentId?, direction?, currency?, dualDirection=true}
 */
export function matchDefaultBankAccount(params) {
  return request({
    url: '/api/v1/default-bank-account-rules/match',
    method: 'get',
    params: { ...params, dualDirection: true }
  })
}

/**
 * ★ v1.1 测试匹配(返回所有命中)
 */
export function testMatchDefaultBankAccount(params) {
  return request({
    url: '/api/v1/default-bank-account-rules/test-match',
    method: 'get',
    params
  })
}

/**
 * ★ v1.1 审计历史
 */
export function getDefaultBankAccountRuleAuditLogs(id, params) {
  return request({
    url: `/api/v1/default-bank-account-rules/${id}/audit-logs`,
    method: 'get',
    params
  })
}

/**
 * ★ v1.1 被引用数
 */
export function getDefaultBankAccountRuleReferenceCount(id) {
  return request({
    url: `/api/v1/default-bank-account-rules/${id}/reference-count`,
    method: 'get'
  })
}