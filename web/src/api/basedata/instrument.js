import request from '@/utils/request'

export function listInstrument(params) {
  return request({
    url: '/api/v1/instruments/page',
    method: 'get',
    params
  })
}

export function getInstrument(id) {
  return request({
    url: `/api/v1/instruments/${id}`,
    method: 'get'
  })
}

export function saveInstrument(data) {
  return request({
    url: '/api/v1/instruments',
    method: 'post',
    data
  })
}

export function updateInstrument(data) {
  return request({
    url: '/api/v1/instruments/update',
    method: 'post',
    data
  })
}

export function deleteInstrument(id) {
  return request({
    url: `/api/v1/instruments/delete/${id}`,
    method: 'post'
  })
}

export function batchDeleteInstrument(ids) {
  return request({
    url: '/api/v1/instruments/batch-delete',
    method: 'post',
    data: ids
  })
}