import request from '@/utils/request'

export function listBankAccount(params) {
  return request({
    url: '/api/v1/bank-accounts/page',
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

export function listBank(params) {
  return request({
    url: '/api/v1/banks/page',
    method: 'get',
    params
  })
}

export function listCurrency(params) {
  return request({
    url: '/api/v1/currencies/page',
    method: 'get',
    params
  })
}

export function listBusinessUnit(params) {
  return request({
    url: '/api/v1/business-units/page',
    method: 'get',
    params
  })
}
