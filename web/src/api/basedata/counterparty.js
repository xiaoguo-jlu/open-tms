import request from '@/utils/request'

export function listCounterparty(params) {
  return request({
    url: '/api/counterparty/page',
    method: 'get',
    params
  })
}

export function getCounterparty(id) {
  return request({
    url: `/api/counterparty/${id}`,
    method: 'get'
  })
}

export function saveCounterparty(data) {
  return request({
    url: '/api/counterparty',
    method: 'post',
    data
  })
}

export function updateCounterparty(data) {
  return request({
    url: '/api/counterparty',
    method: 'put',
    data
  })
}

export function deleteCounterparty(id) {
  return request({
    url: `/api/counterparty/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCounterparty(ids) {
  return request({
    url: '/api/counterparty/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCounterparty(params) {
  return request({
    url: '/api/counterparty/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCounterparty(formData) {
  return request({
    url: '/api/counterparty/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}