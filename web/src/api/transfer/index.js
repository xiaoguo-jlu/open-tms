import request from '@/utils/request'

export function listTransferTransaction(params) {
  return request({
    url: '/transfer/transactions',
    method: 'get',
    params
  })
}

export function getTransferTransaction(id) {
  return request({
    url: `/transfer/transactions/${id}`,
    method: 'get'
  })
}

export function createTransferTransaction(data) {
  return request({
    url: '/transfer/transactions',
    method: 'post',
    data
  })
}

export function updateTransferTransaction(data) {
  return request({
    url: '/transfer/transactions',
    method: 'put',
    data
  })
}

export function deleteTransferTransaction(id) {
  return request({
    url: `/transfer/transactions/${id}`,
    method: 'delete'
  })
}

export function submitTransferTransaction(id) {
  return request({
    url: `/transfer/transactions/${id}/submit`,
    method: 'post'
  })
}

export function executeTransferTransaction(id) {
  return request({
    url: `/transfer/transactions/${id}/execute`,
    method: 'post'
  })
}

export function cancelTransferTransaction(id, data) {
  return request({
    url: `/transfer/transactions/${id}/cancel`,
    method: 'post',
    data
  })
}

export function listAccount(params) {
  return request({
    url: '/transfer/accounts',
    method: 'get',
    params
  })
}