import request from '@/utils/request'

export function listHoliday(params) {
  return request({
    url: '/api/v1/holidays/page',
    method: 'get',
    params
  })
}

export function getHoliday(id) {
  return request({
    url: `/api/v1/holidays/${id}`,
    method: 'get'
  })
}

export function saveHoliday(data) {
  return request({
    url: '/api/v1/holidays',
    method: 'post',
    data
  })
}

export function updateHoliday(data) {
  return request({
    url: '/api/v1/holidays',
    method: 'put',
    data
  })
}

export function deleteHoliday(id) {
  return request({
    url: `/api/v1/holidays/${id}`,
    method: 'delete'
  })
}

export function batchDeleteHoliday(ids) {
  return request({
    url: '/api/v1/holidays/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportHoliday(params) {
  return request({
    url: '/api/v1/holidays/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importHoliday(formData) {
  return request({
    url: '/api/v1/holidays/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}