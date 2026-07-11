import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Cockpit.vue') },
  {
    path: '/basedata', name: 'Basedata', children: [
      { path: 'trader', name: 'Trader', component: () => import('@/views/basedata/TraderList.vue') },
      { path: 'currency', name: 'Currency', component: () => import('@/views/basedata/CurrencyList.vue') },
      { path: 'country', name: 'Country', component: () => import('@/views/basedata/CountryList.vue') },
      { path: 'holiday', name: 'Holiday', component: () => import('@/views/basedata/HolidayList.vue') },
      { path: 'bank-account', name: 'BankAccount', component: () => import('@/views/basedata/BankAccountList.vue') },
      { path: 'default-bank-account-rule', name: 'DefaultBankAccountRule', component: () => import('@/views/basedata/DefaultBankAccountRuleList.vue') },
      { path: 'counterparty', name: 'Counterparty', component: () => import('@/views/basedata/CounterpartyList.vue') },
      { path: 'counterparty-account', name: 'CounterpartyAccount', component: () => import('@/views/basedata/CounterpartyAccountList.vue') },
      { path: 'currency-pair', name: 'CurrencyPair', component: () => import('@/views/basedata/CurrencyPairList.vue') },
      { path: 'subsidiary', name: 'Subsidiary', component: () => import('@/views/basedata/SubsidiaryList.vue') },
      { path: 'management-entity', name: 'ManagementEntity', component: () => import('@/views/basedata/ManagementEntityList.vue') },
      { path: 'instrument', name: 'Instrument', component: () => import('@/views/basedata/InstrumentList.vue') }
    ]
  },
  {
    path: '/dealing', name: 'Dealing', children: [
      { path: 'ac-deal', name: 'AcDealList', component: () => import('@/views/dealing/AcDealList.vue') },
      { path: 'ac-deal/detail', name: 'AcDealDetail', component: () => import('@/views/dealing/AcDealDetail.vue') },
      { path: 'ac-deal/detail/:dealNumber', name: 'AcDealDetailByNumber', component: () => import('@/views/dealing/AcDealDetail.vue') },
      { path: 'ac-deal/audit-history', name: 'AcAuditHistory', component: () => import('@/views/dealing/AuditHistoryView.vue') },
      { path: 'at-deal', name: 'AtDealList', component: () => import('@/views/dealing/AtDealList.vue') },
      { path: 'at-deal/detail', name: 'AtDealDetail', component: () => import('@/views/dealing/AtDealDetail.vue') },
      { path: 'at-deal/audit-history', name: 'AtAuditHistory', component: () => import('@/views/dealing/AuditHistoryView.vue') },
      { path: 'action', name: 'ActionList', component: () => import('@/views/dealing/ActionList.vue') },
      { path: 'fx-deal', name: 'FxDealList', component: () => import('@/views/dealing/FxDealList.vue') },
      { path: 'fx-deal/detail', name: 'FxDealDetail', component: () => import('@/views/dealing/FxDealDetail.vue') },
      { path: 'fx-deal/audit-history', name: 'FxAuditHistory', component: () => import('@/views/dealing/AuditHistoryView.vue') }
    ]
  },
  {
    path: '/approval', name: 'Approval', children: [
      { path: 'rule', name: 'ApprovalRule', component: () => import('@/views/approval/ApprovalRuleList.vue') },
      { path: 'template', name: 'WorkflowTemplate', component: () => import('@/views/approval/WorkflowTemplate.vue') },
      { path: 'task', name: 'ApprovalTask', component: () => import('@/views/approval/ApprovalTask.vue') }
    ]
  },
  {
    path: '/deposit', name: 'Deposit', children: [
      { path: 'list', name: 'DepositList', component: () => import('@/views/deposit/DepositList.vue') }
    ]
  },
  {
    path: '/loan', name: 'Loan', children: [
      { path: 'list', name: 'LoanList', component: () => import('@/views/loan/LoanList.vue') }
    ]
  },
  {
    path: '/fundplan', name: 'FundPlan', children: [
      { path: 'list', name: 'FundPlanList', component: () => import('@/views/fundplan/FundPlanList.vue') }
    ]
  },
  {
    path: '/risk', name: 'Risk', children: [
      { path: 'var', name: 'VarReportList', component: () => import('@/views/risk/VarReportList.vue') }
    ]
  },
  {
    path: '/report', name: 'Report', children: [
      { path: 'list', name: 'ReportList', component: () => import('@/views/report/ReportList.vue') }
    ]
  },
  {
    path: '/system', name: 'System', children: [
      { path: 'api-docs', name: 'ApiDocs', component: () => import('@/views/system/ApiDocs.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router