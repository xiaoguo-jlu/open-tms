import request from '@/utils/request'

export function listCurrencyPair(params) {
  return request({
    url: '/api/v1/currency-pairs/page',
    method: 'get',
    params
  })
}

export function getCurrencyPair(id) {
  return request({
    url: `/api/v1/currency-pairs/${id}`,
    method: 'get'
  })
}

export function saveCurrencyPair(data) {
  return request({
    url: '/api/v1/currency-pairs',
    method: 'post',
    data
  })
}

export function updateCurrencyPair(data) {
  return request({
    url: '/api/v1/currency-pairs/update',
    method: 'post',
    data
  })
}

export function deleteCurrencyPair(id) {
  return request({
    url: `/api/v1/currency-pairs/delete/${id}`,
    method: 'post'
  })
}