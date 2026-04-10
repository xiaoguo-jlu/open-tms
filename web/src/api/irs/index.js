import request from '@/utils/request'
export function listIrsDeal(params) { return request({ url: '/irs/deals', method: 'get', params }) }
export function getIrsDeal(id) { return request({ url: `/irs/deals/${id}`, method: 'get' }) }
export function createIrsDeal(data) { return request({ url: '/irs/deals', method: 'post', data }) }
export function updateIrsDeal(data) { return request({ url: '/irs/deals', method: 'put', data }) }
export function submitIrsDeal(id) { return request({ url: `/irs/deals/${id}/submit`, method: 'post' }) }
export function getIrsValuation(id) { return request({ url: `/irs/deals/${id}/valuation`, method: 'get' }) }
export function listIrsTrade(params) { return request({ url: '/irs/trades', method: 'get', params }) }