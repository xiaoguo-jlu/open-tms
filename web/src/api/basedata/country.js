import request from '@/utils/request'

export function listCountry(params) {
  return request({
    url: '/api/v1/countries/page',
    method: 'get',
    params
  })
}

export function getCountry(id) {
  return request({
    url: `/api/v1/countries/${id}`,
    method: 'get'
  })
}

export function saveCountry(data) {
  return request({
    url: '/api/v1/countries',
    method: 'post',
    data
  })
}

export function updateCountry(data) {
  return request({
    url: '/api/v1/countries',
    method: 'put',
    data
  })
}

export function deleteCountry(id) {
  return request({
    url: `/api/v1/countries/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCountry(ids) {
  return request({
    url: '/api/v1/countries/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCountry(params) {
  return request({
    url: '/api/v1/countries/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCountry(formData) {
  return request({
    url: '/api/v1/countries/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}