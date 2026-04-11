import request from '@/utils/request'

export function listDeal(params) {
  return request({
    url: '/api/v1/deals',
    method: 'get',
    params
  })
}

export function getDeal(id) {
  return request({
    url: `/api/v1/deals/${id}`,
    method: 'get'
  })
}

export function createDeal(data) {
  return request({
    url: '/api/v1/deals',
    method: 'post',
    data
  })
}

export function updateDeal(data) {
  return request({
    url: '/api/v1/deals',
    method: 'put',
    data
  })
}

export function deleteDeal(id) {
  return request({
    url: `/api/v1/deals/${id}`,
    method: 'delete'
  })
}

export function saveDraftDeal(id) {
  return request({
    url: `/api/v1/deals/${id}/save-draft`,
    method: 'post'
  })
}

export function submitDeal(id) {
  return request({
    url: `/api/v1/deals/${id}/submit`,
    method: 'post'
  })
}

export function approveDeal(id) {
  return request({
    url: `/api/v1/deals/${id}/approve`,
    method: 'post'
  })
}

export function rejectDeal(id, data) {
  return request({
    url: `/api/v1/deals/${id}/reject`,
    method: 'post',
    data
  })
}

export function cancelDeal(id, data) {
  return request({
    url: `/api/v1/deals/${id}/cancel`,
    method: 'post',
    data
  })
}

export function copyDeal(id) {
  return request({
    url: `/api/v1/deals/${id}/copy`,
    method: 'post'
  })
}

export function getDealHistory(dealId) {
  return request({
    url: `/api/v1/deals/${dealId}/history`,
    method: 'get'
  })
}

export function getDealCashflow(dealId) {
  return request({
    url: `/api/v1/deals/${dealId}/cashflow`,
    method: 'get'
  })
}

export function getDealDealmap(dealId) {
  return request({
    url: `/api/v1/deals/${dealId}/dealmap`,
    method: 'get'
  })
}

export function exportDeal(params) {
  return request({
    url: '/api/v1/deals/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importDeal(formData) {
  return request({
    url: '/api/v1/deals/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function batchDeleteDeal(ids) {
  return request({
    url: '/api/v1/deals/batch-delete',
    method: 'post',
    data: { ids }
  })
}