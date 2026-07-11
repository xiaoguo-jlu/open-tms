import request from '@/utils/request'

/**
 * 审计历史 API (v1.0 — 2 端点)
 *
 * 端点规范 (来自 docs/api/cashflow-enhance-API.md):
 * - GET /api/v1/dealing/deals/{dealNumber}/versions
 *   分页返回该交易的所有镜像版本(默认按 version desc)
 * - GET /api/v1/dealing/deals/{dealNumber}/versions/{version}
 *   返回该版本的 3 段合并镜像数据(deal + dealMap + cashflows)
 *
 * 写入端点 0 个(只读视图)。
 */

// 版本列表 — 镜像版本分页列表
export function listVersions(dealNumber, params) {
  return request({
    url: `/api/v1/dealing/deals/${dealNumber}/versions`,
    method: 'get',
    params
  })
}

// 版本详情 — 单个版本的 3 段合并镜像
export function getVersionDetail(dealNumber, version) {
  return request({
    url: `/api/v1/dealing/deals/${dealNumber}/versions/${version}`,
    method: 'get'
  })
}