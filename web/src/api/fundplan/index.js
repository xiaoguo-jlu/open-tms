import request from '@/utils/request'
export function listFundPlan(params) { return request({ url: '/fundplan/plans', method: 'get', params }) }
export function getFundPlan(id) { return request({ url: `/fundplan/plans/${id}`, method: 'get' }) }
export function createFundPlan(data) { return request({ url: '/fundplan/plans', method: 'post', data }) }
export function updateFundPlan(data) { return request({ url: '/fundplan/plans', method: 'put', data }) }
export function deleteFundPlan(id) { return request({ url: `/fundplan/plans/${id}`, method: 'delete' }) }
export function listFundPlanItem(planId) { return request({ url: `/fundplan/plans/${planId}/items`, method: 'get' }) }
export function saveFundPlanItem(planId, data) { return request({ url: `/fundplan/plans/${planId}/items`, method: 'post', data }) }
export function listForecast(params) { return request({ url: '/forecast/cashflow', method: 'get', params }) }
export function getForecastTrend(params) { return request({ url: '/forecast/trend', method: 'get', params }) }