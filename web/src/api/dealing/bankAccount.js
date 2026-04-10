import request from '@/utils/request'

export function listBankAccount(params) {
  return request({
    url: '/bank-accounts',
    method: 'get',
    params
  })
}

export function getBankAccount(id) {
  return request({
    url: `/bank-accounts/${id}`,
    method: 'get'
  })
}

export function saveBankAccount(data) {
  return request({
    url: '/bank-accounts',
    method: 'post',
    data
  })
}

export function updateBankAccount(data) {
  return request({
    url: '/bank-accounts',
    method: 'put',
    data
  })
}

export function deleteBankAccount(id) {
  return request({
    url: `/bank-accounts/${id}`,
    method: 'delete'
  })
}

export function enableBankAccount(id) {
  return request({
    url: `/bank-accounts/${id}/enable`,
    method: 'post'
  })
}

export function disableBankAccount(id) {
  return request({
    url: `/bank-accounts/${id}/disable`,
    method: 'post'
  })
}

export function getAccountBalance(id) {
  return request({
    url: `/bank-accounts/${id}/balance`,
    method: 'get'
  })
}

export function getAccountStatement(params) {
  return request({
    url: '/bank-accounts/statement',
    method: 'get',
    params
  })
}

export function testEbankingConnection(data) {
  return request({
    url: '/ebanking/test',
    method: 'post',
    data
  })
}

export function syncBankAccount(id) {
  return request({
    url: `/bank-accounts/${id}/sync`,
    method: 'post'
  })
}

export function reconcileAccount(id) {
  return request({
    url: `/bank-accounts/${id}/reconcile`,
    method: 'post'
  })
}