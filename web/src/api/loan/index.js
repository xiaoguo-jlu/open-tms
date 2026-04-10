import request from '@/utils/request'

export function listLoan(params) { return request({ url: '/loan/deals', method: 'get', params }) }
export function getLoan(id) { return request({ url: `/loan/deals/${id}`, method: 'get' }) }
export function createLoan(data) { return request({ url: '/loan/deals', method: 'post', data }) }
export function updateLoan(data) { return request({ url: '/loan/deals', method: 'put', data }) }
export function deleteLoan(id) { return request({ url: `/loan/deals/${id}`, method: 'delete' }) }
export function submitLoan(id) { return request({ url: `/loan/deals/${id}/submit`, method: 'post' }) }
export function approveLoan(id) { return request({ url: `/loan/deals/${id}/approve`, method: 'post' }) }
export function executeLoan(id) { return request({ url: `/loan/deals/${id}/execute`, method: 'post' }) }
export function repayLoan(id, data) { return request({ url: `/loan/deals/${id}/repay`, method: 'post', data }) }