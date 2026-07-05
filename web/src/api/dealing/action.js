import request from '@/utils/request'

// Action (v2.0 - 统一 Action 待办) API
// Base path: /api/v1/dealing/actions
// 后端 ActionV2Controller 提供的统一端点

/**
 * 查询待审批 Action 列表（approvalStatus1 = Pending）
 * @param {Object} params - { pageNum, pageSize, dealType? }
 */
export function listPendingActions(params) {
  return request({
    url: '/api/v1/dealing/actions/pending',
    method: 'get',
    params
  })
}

/**
 * Action 分页查询（全部状态）
 * @param {Object} params - { pageNum, pageSize, dealNumber?, approvalStatus?, keyword? }
 */
export function listActionPage(params) {
  return request({
    url: '/api/v1/dealing/actions/page',
    method: 'get',
    params
  })
}

/**
 * 按交易编号查询 Action 列表
 */
export function listActionsByDeal(dealNumber) {
  return request({
    url: `/api/v1/dealing/actions/by-deal/${dealNumber}`,
    method: 'get'
  })
}

/**
 * Action 统计信息（Action 代办首页统计卡片）
 * @param {Object} params - { dealType? }
 * @returns {Promise<{total,pending,approved,rejected}>}
 */
export function getActionStats(params) {
  return request({
    url: '/api/v1/dealing/actions/stats',
    method: 'get',
    params
  })
}

/**
 * 统一审批通过 Action（推荐使用 — 处理 AC/AT/FX 路由）
 * @param {String} actionNumber
 * @param {Object} data - { approver, approvalRemark? }
 */
export function approveActionV2(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/actions/${actionNumber}/approve`,
    method: 'post',
    data
  })
}

/**
 * 统一审批驳回 Action（推荐使用）
 * @param {String} actionNumber
 * @param {Object} data - { approver, approvalRemark }  approvalRemark 必填
 */
export function rejectActionV2(actionNumber, data) {
  return request({
    url: `/api/v1/dealing/actions/${actionNumber}/reject`,
    method: 'post',
    data
  })
}