import request from '@/utils/request'

// 资金管理主体 API
// 路径: /api/v1/management-entities

export function listManagementEntity(params) {
  return request({
    url: '/api/v1/management-entities/page',
    method: 'get',
    params
  })
}

export function getManagementEntity(id) {
  return request({
    url: `/api/v1/management-entities/${id}`,
    method: 'get'
  })
}

export function getManagementEntityTree() {
  return request({
    url: '/api/v1/management-entities/tree',
    method: 'get'
  })
}

export function saveManagementEntity(data) {
  return request({
    url: '/api/v1/management-entities',
    method: 'post',
    data
  })
}

export function updateManagementEntity(data) {
  return request({
    url: '/api/v1/management-entities',
    method: 'put',
    data
  })
}

export function deleteManagementEntity(id) {
  return request({
    url: `/api/v1/management-entities/${id}`,
    method: 'delete'
  })
}

export function batchDeleteManagementEntity(ids) {
  return request({
    url: '/api/v1/management-entities/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportManagementEntity(params) {
  return request({
    url: '/api/v1/management-entities/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importManagementEntity(formData) {
  return request({
    url: '/api/v1/management-entities/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
