import request from '@/utils/request'

// AT 交易 (Account Transfer) API
// Base path: /api/v1/dealing/at-deals

// 分页查询 AT 交易
export function pageAtDeals(params) {
  return request({
    url: '/api/v1/dealing/at-deals/page',
    method: 'get',
    params
  })
}

// 按 ID 获取 AT 交易详情
export function getAtDeal(id) {
  return request({
    url: `/api/v1/dealing/at-deals/${id}`,
    method: 'get'
  })
}

// 按 dealNumber 获取 AT 交易详情
export function getAtDealByNumber(dealNumber) {
  return request({
    url: `/api/v1/dealing/at-deals/number/${dealNumber}`,
    method: 'get'
  })
}

// 创建 AT 交易
export function saveAtDeal(data) {
  return request({
    url: '/api/v1/dealing/at-deals',
    method: 'post',
    data
  })
}

// 更新 AT 交易
export function updateAtDeal(data) {
  return request({
    url: '/api/v1/dealing/at-deals/update',
    method: 'post',
    data
  })
}

// 删除 AT 交易
export function deleteAtDeal(id) {
  return request({
    url: `/api/v1/dealing/at-deals/${id}/delete`,
    method: 'post'
  })
}

// 查询 AT 交易的所有 DealMap（双腿）
export function listAtDealMaps(dealNumber) {
  return request({
    url: `/api/v1/dealing/at-deals/${dealNumber}/dealmap`,
    method: 'get'
  })
}

// 查询 AT 交易的所有 Cashflow
export function listAtCashflows(dealNumber) {
  return request({
    url: `/api/v1/dealing/at-deals/${dealNumber}/cashflow`,
    method: 'get'
  })
}

// 查询 AT 交易的所有 Action
export function listAtActions(dealNumber) {
  return request({
    url: `/api/v1/dealing/at-deals/${dealNumber}/actions`,
    method: 'get'
  })
}

// 查询 AT 交易的所有镜像
export function listAtImages(dealNumber) {
  return request({
    url: `/api/v1/dealing/at-deals/${dealNumber}/images`,
    method: 'get'
  })
}

// 审批通过 Action
export function approveAtAction(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/at-deals/actions/${actionNumber}/approve`,
    method: 'post',
    data
  })
}

// 驳回 Action
export function rejectAtAction(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/at-deals/actions/${actionNumber}/reject`,
    method: 'post',
    data
  })
}
