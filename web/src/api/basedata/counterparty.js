import request from '@/utils/request'

export function listCounterparty(params) {
  return request({
    url: '/basedata/counterparties',
    method: 'get',
    params
  })
}

export function getCounterparty(id) {
  return request({
    url: `/basedata/counterparties/${id}`,
    method: 'get'
  })
}

export function saveCounterparty(data) {
  return request({
    url: '/basedata/counterparties',
    method: 'post',
    data
  })
}

export function updateCounterparty(data) {
  return request({
    url: '/basedata/counterparties',
    method: 'put',
    data
  })
}

export function deleteCounterparty(id) {
  return request({
    url: `/basedata/counterparties/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCounterparty(ids) {
  return request({
    url: '/basedata/counterparties/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCounterparty(params) {
  return request({
    url: '/basedata/counterparties/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCounterparty(formData) {
  return request({
    url: '/basedata/counterparties/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}