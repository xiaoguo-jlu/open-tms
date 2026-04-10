import request from '@/utils/request'

export function listInstrument(params) {
  return request({
    url: '/instruments',
    method: 'get',
    params
  })
}

export function getInstrument(id) {
  return request({
    url: `/instruments/${id}`,
    method: 'get'
  })
}

export function saveInstrument(data) {
  return request({
    url: '/instruments',
    method: 'post',
    data
  })
}

export function updateInstrument(data) {
  return request({
    url: '/instruments',
    method: 'put',
    data
  })
}

export function deleteInstrument(id) {
  return request({
    url: `/instruments/${id}`,
    method: 'delete'
  })
}

export function listInstrumentType() {
  return request({
    url: '/instruments/types',
    method: 'get'
  })
}