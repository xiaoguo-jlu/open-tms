import request from '@/utils/request'
export function listHedge(params) { return request({ url: '/risk/hedge', method: 'get', params }) }
export function getHedge(id) { return request({ url: `/risk/hedge/${id}`, method: 'get' }) }
export function createHedge(data) { return request({ url: '/risk/hedge', method: 'post', data }) }
export function updateHedge(data) { return request({ url: '/risk/hedge', method: 'put', data }) }
export function approveHedge(id) { return request({ url: `/risk/hedge/${id}/approve`, method: 'post' }) }
export function executeHedge(id) { return request({ url: `/risk/hedge/${id}/execute`, method: 'post' }) }
export function getHedgeEffect(id) { return request({ url: `/risk/hedge/${id}/effect`, method: 'get' }) }