import request from '@/utils/request'

export function listInstrument(params) {
  return request({
    url: '/api/v1/instruments',
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
    url: '/api/v1/instruments',
    method: 'put',
    data
  })
}

export function deleteInstrument(id) {
  return request({
    url: `/api/v1/instruments/${id}`,
    method: 'delete'
  })
}

export function batchDeleteInstrument(ids) {
  return request({
    url: '/api/v1/instruments/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportInstrument(params) {
  return request({
    url: '/api/v1/instruments/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importInstrument(formData) {
  return request({
    url: '/api/v1/instruments/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}