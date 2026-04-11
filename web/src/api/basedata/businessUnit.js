import request from '@/utils/request'

export function listBusinessUnit(params) {
  return request({
    url: '/api/business-unit/page',
    method: 'get',
    params
  })
}

export function getBusinessUnit(id) {
  return request({
    url: `/api/business-unit/${id}`,
    method: 'get'
  })
}

export function saveBusinessUnit(data) {
  return request({
    url: '/api/business-unit',
    method: 'post',
    data
  })
}

export function updateBusinessUnit(data) {
  return request({
    url: '/api/business-unit',
    method: 'put',
    data
  })
}

export function deleteBusinessUnit(id) {
  return request({
    url: `/api/business-unit/${id}`,
    method: 'delete'
  })
}

export function batchDeleteBusinessUnit(ids) {
  return request({
    url: '/api/business-unit/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportBusinessUnit(params) {
  return request({
    url: '/api/business-unit/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importBusinessUnit(formData) {
  return request({
    url: '/api/business-unit/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}