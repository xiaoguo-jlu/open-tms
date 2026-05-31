import request from '@/utils/request'

// AC交易 API (dealing模块，端口8082)
// Base path: /api/v1/dealing/deals

// 分页查询交易
export function listDeal(params) {
  return request({
    url: '/api/v1/dealing/deals/page',
    method: 'get',
    params
  })
}

// 获取交易详情
export function getDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}`,
    method: 'get'
  })
}

// 按编号获取交易
export function getDealByNumber(dealNumber) {
  return request({
    url: `/api/v1/dealing/deals/number/${dealNumber}`,
    method: 'get'
  })
}

// 创建交易
export function createDeal(data) {
  return request({
    url: '/api/v1/dealing/deals',
    method: 'post',
    data
  })
}

// 更新交易
export function updateDeal(data) {
  return request({
    url: '/api/v1/dealing/deals/update',
    method: 'post',
    data
  })
}

// 提交审批
export function submitDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}/submit`,
    method: 'post'
  })
}

// 审批通过
export function approveDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}/approve`,
    method: 'post'
  })
}

// 审批拒绝
export function rejectDeal(id, data) {
  return request({
    url: `/api/v1/dealing/deals/${id}/reject`,
    method: 'post',
    data
  })
}

// 执行交易
export function executeDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}/execute`,
    method: 'post'
  })
}

// ============ Action API ============

// 分页查询Action
export function listAction(params) {
  return request({
    url: '/api/v1/dealing/actions/page',
    method: 'get',
    params
  })
}

// 按交易查Action
export function listActionByDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/actions/by-deal/${dealNumber}`,
    method: 'get'
  })
}

// ============ 镜像版本 API ============

// 镜像列表
export function listImageByDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/images/by-deal/${dealNumber}`,
    method: 'get'
  })
}

// 指定版本镜像
export function getImageVersion(dealNumber, version) {
  return request({
    url: `/api/v1/dealing/images/${dealNumber}/${version}`,
    method: 'get'
  })
}

// ============ 以下为保留的通用方法 ============

export function deleteDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}`,
    method: 'delete'
  })
}

export function saveDraftDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}/save-draft`,
    method: 'post'
  })
}

export function cancelDeal(id, data) {
  return request({
    url: `/api/v1/dealing/deals/${id}/cancel`,
    method: 'post',
    data
  })
}

export function copyDeal(id) {
  return request({
    url: `/api/v1/dealing/deals/${id}/copy`,
    method: 'post'
  })
}

export function getDealHistory(dealId) {
  return request({
    url: `/api/v1/dealing/deals/${dealId}/history`,
    method: 'get'
  })
}

export function getDealCashflow(dealId) {
  return request({
    url: `/api/v1/dealing/deals/${dealId}/cashflow`,
    method: 'get'
  })
}

export function getDealDealmap(dealId) {
  return request({
    url: `/api/v1/dealing/deals/${dealId}/dealmap`,
    method: 'get'
  })
}

export function exportDeal(params) {
  return request({
    url: '/api/v1/dealing/deals/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importDeal(formData) {
  return request({
    url: '/api/v1/dealing/deals/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function batchDeleteDeal(ids) {
  return request({
    url: '/api/v1/dealing/deals/batch-delete',
    method: 'post',
    data: { ids }
  })
}