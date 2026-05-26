import request from '@/utils/request'

export function listTrader(params) {
  return request({
    url: '/api/v1/traders/page',
    method: 'get',
    params
  })
}

export function getTrader(id) {
  return request({
    url: `/api/v1/traders/${id}`,
    method: 'get'
  })
}

export function saveTrader(data) {
  return request({
    url: '/api/v1/traders',
    method: 'post',
    data
  })
}

export function updateTrader(data) {
  return request({
    url: '/api/v1/traders',
    method: 'put',
    data
  })
}

export function deleteTrader(id) {
  return request({
    url: `/api/v1/traders/${id}`,
    method: 'delete'
  })
}

export function batchDeleteTrader(ids) {
  return request({
    url: '/api/v1/traders/batch-delete',
    method: 'post',
    data: { ids }
  })
}