import request from '@/utils/request'

export function getPositionSummary(params) {
  return request({
    url: '/position/summary',
    method: 'get',
    params
  })
}

export function getPositionDetail(params) {
  return request({
    url: '/position/detail',
    method: 'get',
    params
  })
}

export function getPositionAccount(params) {
  return request({
    url: '/position/accounts',
    method: 'get',
    params
  })
}

export function getPositionTrend(params) {
  return request({
    url: '/position/trend',
    method: 'get',
    params
  })
}

export function listPositionLimit(params) {
  return request({
    url: '/position/limits',
    method: 'get',
    params
  })
}

export function getPositionLimit(id) {
  return request({
    url: `/position/limits/${id}`,
    method: 'get'
  })
}

export function savePositionLimit(data) {
  return request({
    url: '/position/limits',
    method: 'post',
    data
  })
}

export function updatePositionLimit(data) {
  return request({
    url: '/position/limits',
    method: 'put',
    data
  })
}

export function deletePositionLimit(id) {
  return request({
    url: `/position/limits/${id}`,
    method: 'delete'
  })
}

export function listPositionAlert(params) {
  return request({
    url: '/position/alerts',
    method: 'get',
    params
  })
}

export function handlePositionAlert(id, data) {
  return request({
    url: `/position/alerts/${id}/handle`,
    method: 'post',
    data
  })
}

export function getDailyReport(params) {
  return request({
    url: '/position/reports/daily',
    method: 'get',
    params
  })
}

export function exportPositionReport(params) {
  return request({
    url: '/position/reports/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}