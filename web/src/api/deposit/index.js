import request from '@/utils/request'

export function listDeposit(params) {
  return request({ url: '/deposit/deals', method: 'get', params })
}

export function getDeposit(id) {
  return request({ url: `/deposit/deals/${id}`, method: 'get' })
}

export function createDeposit(data) {
  return request({ url: '/deposit/deals', method: 'post', data })
}

export function updateDeposit(data) {
  return request({ url: '/deposit/deals', method: 'put', data })
}

export function deleteDeposit(id) {
  return request({ url: `/deposit/deals/${id}`, method: 'delete' })
}

export function submitDeposit(id) {
  return request({ url: `/deposit/deals/${id}/submit`, method: 'post' })
}

export function approveDeposit(id) {
  return request({ url: `/deposit/deals/${id}/approve`, method: 'post' })
}

export function rejectDeposit(id, data) {
  return request({ url: `/deposit/deals/${id}/reject`, method: 'post', data })
}

export function executeDeposit(id) {
  return request({ url: `/deposit/deals/${id}/execute`, method: 'post' })
}

export function settleDeposit(id) {
  return request({ url: `/deposit/deals/${id}/settle`, method: 'post' })
}

export function listDepositProduct(params) {
  return request({ url: '/deposit/products', method: 'get', params })
}

export function getDepositProduct(id) {
  return request({ url: `/deposit/products/${id}`, method: 'get' })
}

export function saveDepositProduct(data) {
  return request({ url: '/deposit/products', method: 'post', data })
}

export function updateDepositProduct(data) {
  return request({ url: '/deposit/products', method: 'put', data })
}

export function deleteDepositProduct(id) {
  return request({ url: `/deposit/products/${id}`, method: 'delete' })
}