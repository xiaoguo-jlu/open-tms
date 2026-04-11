import request from '@/utils/request'

export function listFxDeal(params) {
  return request({
    url: '/api/v1/fx-deals',
    method: 'get',
    params
  })
}

export function getFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}`,
    method: 'get'
  })
}

export function createFxDeal(data) {
  return request({
    url: '/api/v1/fx-deals',
    method: 'post',
    data
  })
}

export function updateFxDeal(data) {
  return request({
    url: '/api/v1/fx-deals',
    method: 'put',
    data
  })
}

export function deleteFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}`,
    method: 'delete'
  })
}

export function submitFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}/submit`,
    method: 'post'
  })
}

export function approveFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}/approve`,
    method: 'post'
  })
}

export function rejectFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}/reject`,
    method: 'post'
  })
}

export function executeFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}/execute`,
    method: 'post'
  })
}

export function closeFxDeal(id) {
  return request({
    url: `/api/v1/fx-deals/${id}/close`,
    method: 'post'
  })
}