import request from '@/utils/request'

export function listBank(params) {
  return request({
    url: '/basedata/banks',
    method: 'get',
    params
  })
}

export function getBank(id) {
  return request({
    url: `/basedata/banks/${id}`,
    method: 'get'
  })
}

export function saveBank(data) {
  return request({
    url: '/basedata/banks',
    method: 'post',
    data
  })
}

export function updateBank(data) {
  return request({
    url: '/basedata/banks',
    method: 'put',
    data
  })
}

export function deleteBank(id) {
  return request({
    url: `/basedata/banks/${id}`,
    method: 'delete'
  })
}

export function batchDeleteBank(ids) {
  return request({
    url: '/basedata/banks/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function exportBank(params) {
  return request({
    url: '/basedata/banks/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importBank(formData) {
  return request({
    url: '/basedata/banks/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}