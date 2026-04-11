import request from '@/utils/request'

export function listHoliday(params) {
  return request({
    url: '/api/holiday/page',
    method: 'get',
    params
  })
}

export function getHoliday(id) {
  return request({
    url: `/api/holiday/${id}`,
    method: 'get'
  })
}

export function saveHoliday(data) {
  return request({
    url: '/api/holiday',
    method: 'post',
    data
  })
}

export function updateHoliday(data) {
  return request({
    url: '/api/holiday',
    method: 'put',
    data
  })
}

export function deleteHoliday(id) {
  return request({
    url: `/api/holiday/${id}`,
    method: 'delete'
  })
}

export function batchDeleteHoliday(ids) {
  return request({
    url: '/api/holiday/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportHoliday(params) {
  return request({
    url: '/api/holiday/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importHoliday(formData) {
  return request({
    url: '/api/holiday/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}