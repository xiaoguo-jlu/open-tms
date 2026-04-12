import request from '@/utils/request'

export function listCurrency(params) {
  return request({
    url: '/api/v1/currencies/page',
    method: 'get',
    params
  })
}

export function getCurrency(id) {
  return request({
    url: `/api/v1/currencies/${id}`,
    method: 'get'
  })
}

export function saveCurrency(data) {
  return request({
    url: '/api/v1/currencies',
    method: 'post',
    data
  })
}

export function updateCurrency(data) {
  return request({
    url: '/api/v1/currencies',
    method: 'put',
    data
  })
}

export function deleteCurrency(id) {
  return request({
    url: `/api/v1/currencies/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCurrency(ids) {
  return request({
    url: '/api/v1/currencies/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCurrency(params) {
  return request({
    url: '/api/v1/currencies/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCurrency(formData) {
  return request({
    url: '/api/v1/currencies/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}