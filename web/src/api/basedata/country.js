import request from '@/utils/request'

export function listCountry(params) {
  return request({
    url: '/api/country/page',
    method: 'get',
    params
  })
}

export function getCountry(id) {
  return request({
    url: `/api/country/${id}`,
    method: 'get'
  })
}

export function saveCountry(data) {
  return request({
    url: '/api/country',
    method: 'post',
    data
  })
}

export function updateCountry(data) {
  return request({
    url: '/api/country',
    method: 'put',
    data
  })
}

export function deleteCountry(id) {
  return request({
    url: `/api/country/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCountry(ids) {
  return request({
    url: '/api/country/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportCountry(params) {
  return request({
    url: '/api/country/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importCountry(formData) {
  return request({
    url: '/api/country/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}