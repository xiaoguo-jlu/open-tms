import request from '@/utils/request'

// FX 交易 v3.2 API
// Base path: /api/v1/dealing/fx-deals

// 后端统一计算接口
export function calculateFxDeal(data) {
  return request({
    url: '/api/v1/dealing/fx-deals/calculate',
    method: 'post',
    data
  })
}

// 分页查询 FX 交易
export function listFxDeal(params) {
  return request({
    url: '/api/v1/dealing/fx-deals/page',
    method: 'get',
    params
  })
}

// 获取 FX 交易详情（按 dealNumber，含 DealMap/Cashflow/Action）
export function getFxDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/fx-deals/${dealNumber}`,
    method: 'get'
  })
}

// 创建 FX 交易（DEAL Action）
export function createFxDeal(data) {
  return request({
    url: '/api/v1/dealing/fx-deals',
    method: 'post',
    data
  })
}

// 更新 FX 交易（UPDATE Action）
export function updateFxDeal(data) {
  return request({
    url: '/api/v1/dealing/fx-deals/update',
    method: 'post',
    data
  })
}

// 删除 FX 交易（DELETE Action）
export function deleteFxDeal(id) {
  return request({
    url: `/api/v1/dealing/fx-deals/delete/${id}`,
    method: 'post'
  })
}

// 获取可复制字段（id/dealNumber 置 null）
export function copyFxDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/fx-deals/${dealNumber}/copy`,
    method: 'get'
  })
}

// NDF RATE_FIX
export function rateFixFxDeal(id, data) {
  return request({
    url: `/api/v1/dealing/fx-deals/${id}/rate-fix`,
    method: 'post',
    data
  })
}