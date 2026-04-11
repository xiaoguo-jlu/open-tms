import request from '@/utils/request'

export function listBank(params) {
  return request({
    url: '/api/bank/page',
    method: 'get',
    params
  })
}

export function getBank(id) {
  return request({
    url: `/api/bank/${id}`,
    method: 'get'
  })
}

export function saveBank(data) {
  return request({
    url: '/api/bank',
    method: 'post',
    data
  })
}

export function updateBank(data) {
  return request({
    url: '/api/bank',
    method: 'put',
    data
  })
}

export function deleteBank(id) {
  return request({
    url: `/api/bank/${id}`,
    method: 'delete'
  })
}

export function batchDeleteBank(ids) {
  return request({
    url: '/api/bank/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportBank(params) {
  return request({
    url: '/api/bank/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importBank(formData) {
  return request({
    url: '/api/bank/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}