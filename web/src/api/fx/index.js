import request from '@/utils/request'
export function listFxDeal(params) { return request({ url: '/fx/deals', method: 'get', params }) }
export function getFxDeal(id) { return request({ url: `/fx/deals/${id}`, method: 'get' }) }
export function createFxDeal(data) { return request({ url: '/fx/deals', method: 'post', data }) }
export function updateFxDeal(data) { return request({ url: '/fx/deals', method: 'put', data }) }
export function submitFxDeal(id) { return request({ url: `/fx/deals/${id}/submit`, method: 'post' }) }
export function settleFxDeal(id) { return request({ url: `/fx/deals/${id}/settle`, method: 'post' }) }
export function listRate(params) { return request({ url: '/fx/rates', method: 'get', params }) }
export function getRateHistory(params) { return request({ url: '/fx/rates/history', method: 'get', params }) }