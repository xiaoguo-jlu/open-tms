import request from '@/utils/request'
export function listExposure(params) { return request({ url: '/risk/exposure', method: 'get', params }) }
export function getExposureDetail(id) { return request({ url: `/risk/exposure/${id}`, method: 'get' }) }
export function listExposureLimit(params) { return request({ url: '/risk/limits', method: 'get', params }) }
export function saveExposureLimit(data) { return request({ url: '/risk/limits', method: 'post', data }) }
export function updateExposureLimit(data) { return request({ url: '/risk/limits', method: 'put', data }) }
export function getExposureTrend(params) { return request({ url: '/risk/exposure/trend', method: 'get', params }) }