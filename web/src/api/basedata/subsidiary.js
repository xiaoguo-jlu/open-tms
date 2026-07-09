import request from '@/utils/request'

export function listSubsidiary(params) {
  return request({
    url: '/api/v1/subsidiaries/page',
    method: 'get',
    params
  })
}

export function getSubsidiary(id) {
  return request({
    url: `/api/v1/subsidiaries/${id}`,
    method: 'get'
  })
}

export function saveSubsidiary(data) {
  return request({
    url: '/api/v1/subsidiaries',
    method: 'post',
    data
  })
}

export function updateSubsidiary(data) {
  return request({
    url: '/api/v1/subsidiaries/update',
    method: 'post',
    data
  })
}

export function deleteSubsidiary(id) {
  return request({
    url: `/api/v1/subsidiaries/delete/${id}`,
    method: 'post'
  })
}