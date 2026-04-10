import request from '@/utils/request'

export function listDeal(params) {
  return request({
    url: '/dealing/deals',
    method: 'get',
    params
  })
}

export function getDeal(id) {
  return request({
    url: `/dealing/deals/${id}`,
    method: 'get'
  })
}

export function createDeal(data) {
  return request({
    url: '/dealing/deals',
    method: 'post',
    data
  })
}

export function updateDeal(data) {
  return request({
    url: '/dealing/deals',
    method: 'put',
    data
  })
}

export function deleteDeal(id) {
  return request({
    url: `/dealing/deals/${id}`,
    method: 'delete'
  })
}

export function submitDeal(id) {
  return request({
    url: `/dealing/deals/${id}/submit`,
    method: 'post'
  })
}

export function approveDeal(id) {
  return request({
    url: `/dealing/deals/${id}/approve`,
    method: 'post'
  })
}

export function rejectDeal(id, data) {
  return request({
    url: `/dealing/deals/${id}/reject`,
    method: 'post',
    data
  })
}

export function cancelDeal(id, data) {
  return request({
    url: `/dealing/deals/${id}/cancel`,
    method: 'post',
    data
  })
}

export function copyDeal(id) {
  return request({
    url: `/dealing/deals/${id}/copy`,
    method: 'post'
  })
}

export function changeDeal(id, data) {
  return request({
    url: `/dealing/deals/${id}/change`,
    method: 'post',
    data
  })
}

export function getDealCashflow(dealId) {
  return request({
    url: `/dealing/deals/${dealId}/cashflow`,
    method: 'get'
  })
}

export function getDealDealmap(dealId) {
  return request({
    url: `/dealing/deals/${dealId}/dealmap`,
    method: 'get'
  })
}

export function getDealHistory(dealId) {
  return request({
    url: `/dealing/deals/${dealId}/history`,
    method: 'get'
  })
}

export function exportDeal(params) {
  return request({
    url: '/dealing/deals/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}