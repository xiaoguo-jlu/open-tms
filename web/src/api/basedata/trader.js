import request from '@/utils/request'

export function listTrader(params) {
  return request({
    url: '/api/trader/page',
    method: 'get',
    params
  })
}

export function getTrader(id) {
  return request({
    url: `/api/trader/${id}`,
    method: 'get'
  })
}

export function saveTrader(data) {
  return request({
    url: '/api/trader',
    method: 'post',
    data
  })
}

export function updateTrader(data) {
  return request({
    url: '/api/trader',
    method: 'put',
    data
  })
}

export function deleteTrader(id) {
  return request({
    url: `/api/trader/${id}`,
    method: 'delete'
  })
}

export function batchDeleteTrader(ids) {
  return request({
    url: '/api/trader/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportTrader(params) {
  return request({
    url: '/api/trader/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importTrader(formData) {
  return request({
    url: '/api/trader/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}