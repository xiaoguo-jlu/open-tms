import request from '@/utils/request'

export function listTransfer(params) {
  return request({
    url: '/transfers',
    method: 'get',
    params
  })
}

export function getTransfer(id) {
  return request({
    url: `/transfers/${id}`,
    method: 'get'
  })
}

export function createTransfer(data) {
  return request({
    url: '/transfers',
    method: 'post',
    data
  })
}

export function approveTransfer(id) {
  return request({
    url: `/transfers/${id}/approve`,
    method: 'post'
  })
}

export function rejectTransfer(id, data) {
  return request({
    url: `/transfers/${id}/reject`,
    method: 'post',
    data
  })
}

export function executeTransfer(id) {
  return request({
    url: `/transfers/${id}/execute`,
    method: 'post'
  })
}

export function cancelTransfer(id, data) {
  return request({
    url: `/transfers/${id}/cancel`,
    method: 'post',
    data
  })
}