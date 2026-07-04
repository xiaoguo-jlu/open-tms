import request from '@/utils/request'

/**
 * 银行账户 API(基于 basedata 模块,统一 POST 规范 — 2026-06-29)
 * 写操作一律 POST:/update 和 POST:/delete/{id}
 */

export function listBankAccount(params) {
  return request({
    url: '/api/v1/bank-accounts/page',
    method: 'get',
    params
  })
}

export function getBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}`,
    method: 'get'
  })
}

export function getAccountBalance(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/balance`,
    method: 'get'
  })
}

export function syncBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/${id}/sync`,
    method: 'post'
  })
}

export function saveBankAccount(data) {
  return request({
    url: '/api/v1/bank-accounts',
    method: 'post',
    data
  })
}

export function updateBankAccount(data) {
  return request({
    url: '/api/v1/bank-accounts/update',
    method: 'post',
    data
  })
}

export function deleteBankAccount(id) {
  return request({
    url: `/api/v1/bank-accounts/delete/${id}`,
    method: 'post'
  })
}

// 关联资源
/**
 * 银行列表 — basedata 没有独立 /banks 端点,BankAccountList 用 bankId 显示银行名
 * 这里用 BankAccount 列表去重得到 bankId → 银行名映射
 * (TODO: 后续可在 basedata 补 /api/v1/banks 端点)
 */
export function listBank(params) {
  return request({
    url: '/api/v1/bank-accounts/page',
    method: 'get',
    params: { ...params, pageSize: 1000 }
  }).then(res => {
    // 从账户数据中提取唯一 bankId
    const banks = new Map()
    const records = res.data?.records || []
    records.forEach(r => {
      if (r.bankId && !banks.has(r.bankId)) {
        banks.set(r.bankId, { id: r.bankId, name: `银行#${r.bankId}` })
      }
    })
    return { ...res, data: { records: Array.from(banks.values()), total: banks.size } }
  })
}

export function listCurrency(params) {
  return request({
    url: '/api/v1/currencies/page',
    method: 'get',
    params
  })
}

// 注意:basedata 实际端点是 /api/v1/management-entities(不是 /business-units)
export function listManagementEntity(params) {
  return request({
    url: '/api/v1/management-entities/page',
    method: 'get',
    params
  })
}
