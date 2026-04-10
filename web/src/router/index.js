import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Cockpit.vue') },
  {
    path: '/basedata', name: 'Basedata', children: [
      { path: 'business-unit', name: 'BusinessUnit', component: () => import('@/views/basedata/BusinessUnitList.vue') },
      { path: 'trader', name: 'Trader', component: () => import('@/views/basedata/TraderList.vue') },
      { path: 'currency', name: 'Currency', component: () => import('@/views/basedata/CurrencyList.vue') },
      { path: 'country', name: 'Country', component: () => import('@/views/basedata/CountryList.vue') },
      { path: 'holiday', name: 'Holiday', component: () => import('@/views/basedata/HolidayList.vue') },
      { path: 'bank', name: 'Bank', component: () => import('@/views/basedata/BankList.vue') },
      { path: 'counterparty', name: 'Counterparty', component: () => import('@/views/basedata/CounterpartyList.vue') },
      { path: 'counterparty-account', name: 'CounterpartyAccount', component: () => import('@/views/basedata/CounterpartyAccountList.vue') }
    ]
  },
  {
    path: '/dealing', name: 'Dealing', children: [
      { path: 'deal', name: 'DealList', component: () => import('@/views/dealing/DealList.vue') },
      { path: 'deal/edit', name: 'DealEdit', component: () => import('@/views/dealing/DealEdit.vue') },
      { path: 'deal/detail', name: 'DealDetail', component: () => import('@/views/dealing/DealDetail.vue') },
      { path: 'bank-account', name: 'BankAccount', component: () => import('@/views/dealing/BankAccountList.vue') },
      { path: 'instrument', name: 'Instrument', component: () => import('@/views/dealing/InstrumentList.vue') }
    ]
  },
  {
    path: '/approval', name: 'Approval', children: [
      { path: 'template', name: 'WorkflowTemplate', component: () => import('@/views/approval/WorkflowTemplate.vue') },
      { path: 'task', name: 'ApprovalTask', component: () => import('@/views/approval/ApprovalTask.vue') }
    ]
  },
  {
    path: '/transfer', name: 'Transfer', children: [
      { path: 'list', name: 'TransferList', component: () => import('@/views/transfer/TransferList.vue') }
    ]
  },
  {
    path: '/ac', name: 'Ac', children: [
      { path: 'list', name: 'AcList', component: () => import('@/views/ac/AcTransactionList.vue') }
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
    path: '/fx', name: 'Fx', children: [
      { path: 'list', name: 'FxList', component: () => import('@/views/fx/FxDealList.vue') }
    ]
  },
  {
    path: '/irs', name: 'Irs', children: [
      { path: 'list', name: 'IrsList', component: () => import('@/views/irs/IrsDealList.vue') }
    ]
  },
  {
    path: '/cashpool', name: 'Cashpool', children: [
      { path: 'overview', name: 'PositionOverview', component: () => import('@/views/cashpool/PositionOverview.vue') },
      { path: 'limit', name: 'PositionLimit', component: () => import('@/views/cashpool/PositionLimit.vue') },
      { path: 'pool', name: 'CashPoolList', component: () => import('@/views/cashpool/CashPoolList.vue') },
      { path: 'auto-rule', name: 'AutoRuleList', component: () => import('@/views/cashpool/AutoRuleList.vue') }
    ]
  },
  {
    path: '/risk', name: 'Risk', children: [
      { path: 'exposure', name: 'ExposureList', component: () => import('@/views/risk/ExposureList.vue') },
      { path: 'var', name: 'VarReportList', component: () => import('@/views/risk/VarReportList.vue') },
      { path: 'hedge', name: 'HedgeList', component: () => import('@/views/risk/HedgeList.vue') }
    ]
  },
  {
    path: '/report', name: 'Report', children: [
      { path: 'list', name: 'ReportList', component: () => import('@/views/report/ReportList.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router