import request from '@/utils/request'

export function listCurrency(params) {
  return request({
    url: '/basedata/currencies',
    method: 'get',
    params
  })
}

export function getCurrency(id) {
  return request({
    url: `/basedata/currencies/${id}`,
    method: 'get'
  })
}

export function saveCurrency(data) {
  return request({
    url: '/basedata/currencies',
    method: 'post',
    data
  })
}

export function updateCurrency(data) {
  return request({
    url: '/basedata/currencies',
    method: 'put',
    data
  })
}

export function deleteCurrency(id) {
  return request({
    url: `/basedata/currencies/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCurrency(ids) {
  return request({
    url: '/basedata/currencies/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCurrency(params) {
  return request({
    url: '/basedata/currencies/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCurrency(formData) {
  return request({
    url: '/basedata/currencies/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}