import request from '@/utils/request'

export function listHoliday(params) {
  return request({
    url: '/holidays',
    method: 'get',
    params
  })
}

export function getHoliday(id) {
  return request({
    url: `/holidays/${id}`,
    method: 'get'
  })
}

export function saveHoliday(data) {
  return request({
    url: '/holidays',
    method: 'post',
    data
  })
}

export function updateHoliday(data) {
  return request({
    url: '/holidays',
    method: 'put',
    data
  })
}

export function deleteHoliday(id) {
  return request({
    url: `/holidays/${id}`,
    method: 'delete'
  })
}

export function batchDeleteHoliday(ids) {
  return request({
    url: '/holidays/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportHoliday(params) {
  return request({
    url: '/holidays/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importHoliday(formData) {
  return request({
    url: '/holidays/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}