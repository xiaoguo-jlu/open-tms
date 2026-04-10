import request from '@/utils/request'
export function listVarReport(params) { return request({ url: '/risk/var', method: 'get', params }) }
export function getVarReport(id) { return request({ url: `/risk/var/${id}`, method: 'get' }) }
export function calculateVar(data) { return request({ url: '/risk/var/calculate', method: 'post', data }) }
export function listVarHistory(params) { return request({ url: '/risk/var/history', method: 'get', params }) }