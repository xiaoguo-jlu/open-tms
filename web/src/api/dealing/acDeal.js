import request from '@/utils/request'

// AC 交易 v2.0 API
// Base path: /api/v1/dealing/ac-deals

// 分页查询 AC 交易
export function listAcDeal(params) {
  return request({
    url: '/api/v1/dealing/ac-deals/page',
    method: 'get',
    params
  })
}

// 获取 AC 交易详情（按 ID）
export function getAcDealById(id) {
  return request({
    url: `/api/v1/dealing/ac-deals/${id}`,
    method: 'get'
  })
}

// 获取 AC 交易详情（按 dealNumber，含 DealMap/Cashflow/Action 聚合）
export function getAcDealByNumber(dealNumber) {
  return request({
    url: `/api/v1/dealing/ac-deals/number/${dealNumber}`,
    method: 'get'
  })
}

// 复制 AC 交易 — 返回可编辑字段（不含 dealNumber/id）
export function copyAcDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/ac-deals/${dealNumber}/copy`,
    method: 'get'
  })
}

// 创建 AC 交易（v2.0 - 自动生成 DealMap + Cashflow）
export function createAcDeal(data) {
  return request({
    url: '/api/v1/dealing/ac-deals',
    method: 'post',
    data
  })
}

// 更新 AC 交易（v2.0 - 软删旧 DealMap + 新建）
export function updateAcDeal(data) {
  return request({
    url: '/api/v1/dealing/ac-deals/update',
    method: 'post',
    data
  })
}

// 删除 AC 交易（v2.0 - 级联软删）
export function deleteAcDeal(id) {
  return request({
    url: `/api/v1/dealing/ac-deals/delete/${id}`,
    method: 'post'
  })
}

// ============ Action 审批 API（v2.0 - 审批仅作用于 Action） ============

// 查询某 Deal 的所有 Action
export function listActionsByDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/actions/by-deal/${dealNumber}`,
    method: 'get'
  })
}

// 查询待审批 Action 列表
export function listPendingActions(params) {
  return request({
    url: '/api/v1/dealing/actions/pending',
    method: 'get',
    params
  })
}

// 审批通过 Action
export function approveAction(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/actions/${actionNumber}/approve`,
    method: 'post',
    data
  })
}

// 审批驳回 Action
export function rejectAction(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/actions/${actionNumber}/reject`,
    method: 'post',
    data
  })
}

// ============ DealMap API ============

// 按 dealNumber 查询 DealMap 时间线
export function listDealMapByDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/dealmap/by-deal/${dealNumber}`,
    method: 'get'
  })
}

// 分页查询 DealMap
export function pageDealMap(params) {
  return request({
    url: '/api/v1/dealing/dealmap/page',
    method: 'get',
    params
  })
}

// 冲销 DealMap
export function reverseDealMap(id, data) {
  return request({
    url: `/api/v1/dealing/dealmap/${id}/reverse`,
    method: 'post',
    data
  })
}
