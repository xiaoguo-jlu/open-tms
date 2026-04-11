import request from '@/utils/request'

export function listInstrument(params) {
  return request({
    url: '/dealing/instrument/instruments',
    method: 'get',
    params
  })
}

export function getInstrument(id) {
  return request({
    url: `/dealing/instrument/instruments/${id}`,
    method: 'get'
  })
}

export function saveInstrument(data) {
  return request({
    url: '/dealing/instrument/instruments',
    method: 'post',
    data
  })
}

export function updateInstrument(data) {
  return request({
    url: '/dealing/instrument/instruments',
    method: 'put',
    data
  })
}

export function deleteInstrument(id) {
  return request({
    url: `/dealing/instrument/instruments/${id}`,
    method: 'delete'
  })
}

export function listInstrumentType() {
  return request({
    url: '/dealing/instrument/instruments/types',
    method: 'get'
  })
}