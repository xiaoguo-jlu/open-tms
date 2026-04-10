import request from '@/utils/request'

export function listCashPool(params) {
  return request({
    url: '/cashpools',
    method: 'get',
    params
  })
}

export function getCashPool(id) {
  return request({
    url: `/cashpools/${id}`,
    method: 'get'
  })
}

export function saveCashPool(data) {
  return request({
    url: '/cashpools',
    method: 'post',
    data
  })
}

export function updateCashPool(data) {
  return request({
    url: '/cashpools',
    method: 'put',
    data
  })
}

export function deleteCashPool(id) {
  return request({
    url: `/cashpools/${id}`,
    method: 'delete'
  })
}

export function listCashPoolMember(poolId) {
  return request({
    url: `/cashpools/${poolId}/members`,
    method: 'get'
  })
}

export function addCashPoolMember(poolId, data) {
  return request({
    url: `/cashpools/${poolId}/members`,
    method: 'post',
    data
  })
}

export function removeCashPoolMember(poolId, memberId) {
  return request({
    url: `/cashpools/${poolId}/members/${memberId}`,
    method: 'delete'
  })
}

export function listAutoRule(params) {
  return request({
    url: '/cashpools/rules',
    method: 'get',
    params
  })
}

export function getAutoRule(id) {
  return request({
    url: `/cashpools/rules/${id}`,
    method: 'get'
  })
}

export function saveAutoRule(data) {
  return request({
    url: '/cashpools/rules',
    method: 'post',
    data
  })
}

export function updateAutoRule(data) {
  return request({
    url: '/cashpools/rules',
    method: 'put',
    data
  })
}

export function deleteAutoRule(id) {
  return request({
    url: `/cashpools/rules/${id}`,
    method: 'delete'
  })
}

export function testAutoRule(id) {
  return request({
    url: `/cashpools/rules/${id}/test`,
    method: 'post'
  })
}