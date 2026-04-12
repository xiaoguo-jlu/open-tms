import request from '@/utils/request'

export function listBank(params) {
  return request({
    url: '/api/v1/banks/page',
    method: 'get',
    params
  })
}

export function getBank(id) {
  return request({
    url: `/api/v1/banks/${id}`,
    method: 'get'
  })
}

export function saveBank(data) {
  return request({
    url: '/api/v1/banks',
    method: 'post',
    data
  })
}

export function updateBank(data) {
  return request({
    url: '/api/v1/banks',
    method: 'put',
    data
  })
}

export function deleteBank(id) {
  return request({
    url: `/api/v1/banks/${id}`,
    method: 'delete'
  })
}

export function batchDeleteBank(ids) {
  return request({
    url: '/api/v1/banks/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportBank(params) {
  return request({
    url: '/api/v1/banks/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importBank(formData) {
  return request({
    url: '/api/v1/banks/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}