import request from '@/utils/request'
export function listReport(params) { return request({ url: '/report/list', method: 'get', params }) }
export function getReportData(reportCode, params) { return request({ url: `/report/${reportCode}/data`, method: 'get', params }) }
export function exportReport(reportCode, params) { return request({ url: `/report/${reportCode}/export`, method: 'get', params, responseType: 'blob' }) }
export function saveReportTemplate(data) { return request({ url: '/report/template', method: 'post', data }) }
export function listDashboard(params) { return request({ url: '/dashboard/list', method: 'get', params }) }
export function getDashboardData(id) { return request({ url: `/dashboard/${id}/data`, method: 'get' }) }