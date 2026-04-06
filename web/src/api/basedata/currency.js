import request from '@/utils/request'

export function listCurrency(params) {
  return request({
    url: '/currencies',
    method: 'get',
    params
  })
}

export function getCurrency(id) {
  return request({
    url: `/currencies/${id}`,
    method: 'get'
  })
}

export function saveCurrency(data) {
  return request({
    url: '/currencies',
    method: 'post',
    data
  })
}

export function updateCurrency(data) {
  return request({
    url: '/currencies',
    method: 'put',
    data
  })
}

export function deleteCurrency(id) {
  return request({
    url: `/currencies/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCurrency(ids) {
  return request({
    url: '/currencies/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCurrency(params) {
  return request({
    url: '/currencies/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCurrency(formData) {
  return request({
    url: '/currencies/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}