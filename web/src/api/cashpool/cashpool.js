import request from '@/utils/request'

export function listCashPool(params) {
  return request({
    url: '/api/v1/cash-pools',
    method: 'get',
    params
  })
}

export function getCashPool(id) {
  return request({
    url: `/api/v1/cash-pools/${id}`,
    method: 'get'
  })
}

export function createCashPool(data) {
  return request({
    url: '/api/v1/cash-pools',
    method: 'post',
    data
  })
}

export const saveCashPool = createCashPool

export function updateCashPool(data) {
  return request({
    url: '/api/v1/cash-pools',
    method: 'put',
    data
  })
}

export function deleteCashPool(id) {
  return request({
    url: `/api/v1/cash-pools/${id}`,
    method: 'delete'
  })
}

export function listCashPoolMember(id) {
  return request({
    url: `/api/v1/cash-pools/${id}/members`,
    method: 'get'
  })
}

export function addCashPoolMember(id, data) {
  return request({
    url: `/api/v1/cash-pools/${id}/members`,
    method: 'post',
    data
  })
}

export function removeCashPoolMember(poolId, memberId) {
  return request({
    url: `/api/v1/cash-pools/${poolId}/members/${memberId}`,
    method: 'delete'
  })
}

export function listAutoRule(params) {
  return request({
    url: '/api/v1/cash-pools/rules',
    method: 'get',
    params
  })
}

export function getAutoRule(id) {
  return request({
    url: `/api/v1/cash-pools/rules/${id}`,
    method: 'get'
  })
}

export function saveAutoRule(data) {
  return request({
    url: '/api/v1/cash-pools/rules',
    method: 'post',
    data
  })
}

export function testAutoRule(id) {
  return request({
    url: `/api/v1/cash-pools/rules/${id}/test`,
    method: 'post'
  })
}

export function getCashPoolRules(id) {
  return request({
    url: `/api/v1/cash-pools/${id}/rules`,
    method: 'get'
  })
}

export const saveAutoRule2 = createCashPoolRule

export function createCashPoolRule(id, data) {
  return request({
    url: `/api/v1/cash-pools/${id}/rules`,
    method: 'post',
    data
  })
}

export function updateCashPoolRule(data) {
  return request({
    url: '/api/v1/cash-pools/rules',
    method: 'put',
    data
  })
}

export function deleteCashPoolRule(id) {
  return request({
    url: `/api/v1/cash-pools/rules/${id}`,
    method: 'delete'
  })
}

export const deleteAutoRule = deleteCashPoolRule

export const updateAutoRule = updateCashPoolRule

export function transferCashPool(id, data) {
  return request({
    url: `/api/v1/cash-pools/${id}/transfer`,
    method: 'post',
    data
  })
}

export function getCashPoolTransfers(id, params) {
  return request({
    url: `/api/v1/cash-pools/${id}/transfers`,
    method: 'get',
    params
  })
}

export function getCashPoolBalance(id) {
  return request({
    url: `/api/v1/cash-pools/${id}/balance`,
    method: 'get'
  })
}