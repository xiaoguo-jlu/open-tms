import request from '@/utils/request'

export function listTrader(params) {
  return request({
    url: '/traders',
    method: 'get',
    params
  })
}

export function getTrader(id) {
  return request({
    url: `/traders/${id}`,
    method: 'get'
  })
}

export function saveTrader(data) {
  return request({
    url: '/traders',
    method: 'post',
    data
  })
}

export function updateTrader(data) {
  return request({
    url: '/traders',
    method: 'put',
    data
  })
}

export function deleteTrader(id) {
  return request({
    url: `/traders/${id}`,
    method: 'delete'
  })
}

export function batchDeleteTrader(ids) {
  return request({
    url: '/traders/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportTrader(params) {
  return request({
    url: '/traders/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importTrader(formData) {
  return request({
    url: '/traders/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}