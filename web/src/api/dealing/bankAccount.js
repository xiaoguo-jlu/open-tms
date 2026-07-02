import request from '@/utils/request'

/**
 * 银行账户 API(基于 basedata 模块,统一 POST 规范 — 2026-06-29)
 * 用于 dealing 模块的本方账户下拉选择,功能等同 basedata/bankAccount.js
 * 仅保留 dealing 业务实际调用的方法
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
