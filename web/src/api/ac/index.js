import request from '@/utils/request'

export function listAcTransaction(params) {
  return request({
    url: '/ac/transactions',
    method: 'get',
    params
  })
}

export function getAcTransaction(id) {
  return request({
    url: `/ac/transactions/${id}`,
    method: 'get'
  })
}

export function createAcTransaction(data) {
  return request({
    url: '/ac/transactions',
    method: 'post',
    data
  })
}

export function updateAcTransaction(data) {
  return request({
    url: '/ac/transactions',
    method: 'put',
    data
  })
}

export function deleteAcTransaction(id) {
  return request({
    url: `/ac/transactions/${id}`,
    method: 'delete'
  })
}

export function generateAcFromDeal(dealId) {
  return request({
    url: `/ac/generate/${dealId}`,
    method: 'post'
  })
}

export function listAcCashflow(params) {
  return request({
    url: '/ac/cashflows',
    method: 'get',
    params
  })
}

export function getAcCashflow(id) {
  return request({
    url: `/ac/cashflows/${id}`,
    method: 'get'
  })
}

export function confirmAcCashflow(id) {
  return request({
    url: `/ac/cashflows/${id}/confirm`,
    method: 'post'
  })
}