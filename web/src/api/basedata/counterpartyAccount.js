import request from '@/utils/request'

export function listCounterpartyAccount(params) {
  return request({
    url: '/counterparty-accounts',
    method: 'get',
    params
  })
}

export function getCounterpartyAccount(id) {
  return request({
    url: `/counterparty-accounts/${id}`,
    method: 'get'
  })
}

export function saveCounterpartyAccount(data) {
  return request({
    url: '/counterparty-accounts',
    method: 'post',
    data
  })
}

export function updateCounterpartyAccount(data) {
  return request({
    url: '/counterparty-accounts',
    method: 'put',
    data
  })
}

export function deleteCounterpartyAccount(id) {
  return request({
    url: `/counterparty-accounts/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCounterpartyAccount(ids) {
  return request({
    url: '/counterparty-accounts/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCounterpartyAccount(params) {
  return request({
    url: '/counterparty-accounts/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCounterpartyAccount(formData) {
  return request({
    url: '/counterparty-accounts/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}