import request from '@/utils/request'

export function listBankAccount(params) {
  return request({
    url: '/api/v1/bank-accounts',
    method: 'get',
    params
  })
}

export function getBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}`,
    method: 'get'
  })
}

export function saveBankAccount(data) {
  return request({
    url: '/api/v1/bank-accounts',
    method: 'post',
    data
  })
}

export function updateBankAccount(data) {
  return request({
    url: '/api/v1/bank-accounts',
    method: 'put',
    data
  })
}

export function deleteBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}`,
    method: 'delete'
  })
}

export function getAccountBalance(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/balance`,
    method: 'get'
  })
}

export function getAccountTransactions(id, params) {
  return request({
    url: `/api/v1/bank-accounts/${id}/transactions`,
    method: 'get',
    params
  })
}

export function batchDeleteBankAccount(ids) {
  return request({
    url: '/api/v1/bank-accounts/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function syncBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/sync`,
    method: 'post'
  })
}

export function reconcileAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/reconcile`,
    method: 'post'
  })
}

export function testEbankingConnection(data) {
  return request({
    url: '/api/v1/bank-accounts/ebanking/test',
    method: 'post',
    data
  })
}

export function enableBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/enable`,
    method: 'post'
  })
}

export function disableBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/disable`,
    method: 'post'
  })
}