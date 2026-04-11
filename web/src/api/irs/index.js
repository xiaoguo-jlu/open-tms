import request from '@/utils/request'

export function listIrsDeal(params) {
  return request({
    url: '/api/v1/irs-deals',
    method: 'get',
    params
  })
}

export function getIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}`,
    method: 'get'
  })
}

export function createIrsDeal(data) {
  return request({
    url: '/api/v1/irs-deals',
    method: 'post',
    data
  })
}

export function updateIrsDeal(data) {
  return request({
    url: '/api/v1/irs-deals',
    method: 'put',
    data
  })
}

export function deleteIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}`,
    method: 'delete'
  })
}

export function submitIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}/submit`,
    method: 'post'
  })
}

export function approveIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}/approve`,
    method: 'post'
  })
}

export function rejectIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}/reject`,
    method: 'post'
  })
}

export function getIrsValuation(id) {
  return request({
    url: `/api/v1/irs-deals/${id}/valuation`,
    method: 'get'
  })
}

export function settleIrsDeal(id) {
  return request({
    url: `/api/v1/irs-deals/${id}/settle`,
    method: 'post'
  })
}