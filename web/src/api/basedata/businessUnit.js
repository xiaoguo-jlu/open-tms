import request from '@/utils/request'

export function listBusinessUnit(params) {
  return request({
    url: '/api/v1/business-units/page',
    method: 'get',
    params
  })
}

export function getBusinessUnit(id) {
  return request({
    url: `/api/v1/business-units/${id}`,
    method: 'get'
  })
}

export function saveBusinessUnit(data) {
  return request({
    url: '/api/v1/business-units',
    method: 'post',
    data
  })
}

export function updateBusinessUnit(data) {
  return request({
    url: '/api/v1/business-units',
    method: 'put',
    data
  })
}

export function deleteBusinessUnit(id) {
  return request({
    url: `/api/v1/business-units/${id}`,
    method: 'delete'
  })
}

export function batchDeleteBusinessUnit(ids) {
  return request({
    url: '/api/v1/business-units/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportBusinessUnit(params) {
  return request({
    url: '/api/v1/business-units/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importBusinessUnit(formData) {
  return request({
    url: '/api/v1/business-units/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}