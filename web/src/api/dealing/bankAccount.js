import request from '@/utils/request'

export function listBankAccount(params) {
  return request({
    url: '/dealing/bank-account/bank-accounts',
    method: 'get',
    params
  })
}

export function getBankAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}`,
    method: 'get'
  })
}

export function saveBankAccount(data) {
  return request({
    url: '/dealing/bank-account/bank-accounts',
    method: 'post',
    data
  })
}

export function updateBankAccount(data) {
  return request({
    url: '/dealing/bank-account/bank-accounts',
    method: 'put',
    data
  })
}

export function deleteBankAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}`,
    method: 'delete'
  })
}

export function enableBankAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}/enable`,
    method: 'post'
  })
}

export function disableBankAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}/disable`,
    method: 'post'
  })
}

export function getAccountBalance(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}/balance`,
    method: 'get'
  })
}

export function getAccountStatement(params) {
  return request({
    url: '/dealing/bank-account/bank-accounts/statement',
    method: 'get',
    params
  })
}

export function testEbankingConnection(data) {
  return request({
    url: '/dealing/bank-account/ebanking/test',
    method: 'post',
    data
  })
}

export function syncBankAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}/sync`,
    method: 'post'
  })
}

export function reconcileAccount(id) {
  return request({
    url: `/dealing/bank-account/bank-accounts/${id}/reconcile`,
    method: 'post'
  })
}