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
      { path: 'counterparty', name: 'Counterparty', component: () => import('@/views/basedata/CounterpartyList.vue') },
      { path: 'counterparty-account', name: 'CounterpartyAccount', component: () => import('@/views/basedata/CounterpartyAccountList.vue') },
      { path: 'currency-pair', name: 'CurrencyPair', component: () => import('@/views/basedata/CurrencyPairList.vue') },
      { path: 'subsidiary', name: 'Subsidiary', component: () => import('@/views/basedata/SubsidiaryList.vue') },
      { path: 'business-unit', name: 'BusinessUnit', component: () => import('@/views/basedata/BusinessUnitList.vue') },
      { path: 'instrument', name: 'Instrument', component: () => import('@/views/basedata/InstrumentList.vue') }
    ]
  },
  {
    path: '/dealing', name: 'Dealing', children: [
      { path: 'deal', name: 'DealList', component: () => import('@/views/dealing/DealList.vue') },
      { path: 'deal/form', name: 'DealForm', component: () => import('@/views/dealing/DealForm.vue') },
      { path: 'deal/detail', name: 'DealDetail', component: () => import('@/views/dealing/DealDetail.vue') },
      { path: 'action', name: 'ActionList', component: () => import('@/views/dealing/ActionList.vue') },
      { path: 'ac-deal', name: 'AcDealList', component: () => import('@/views/dealing/AcDealList.vue') },
      { path: 'ac-deal/detail/:dealNumber', name: 'AcDealDetail', component: () => import('@/views/dealing/AcDealDetail.vue') },
      { path: 'at-deal', name: 'AtDealList', component: () => import('@/views/dealing/AtDealList.vue') },
      { path: 'at-deal/form', name: 'AtDealForm', component: () => import('@/views/dealing/AtDealForm.vue') },
      { path: 'at-deal/detail', name: 'AtDealDetail', component: () => import('@/views/dealing/AtDealDetail.vue') }
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
    path: '/risk', name: 'Risk', children: [
      { path: 'var', name: 'VarReportList', component: () => import('@/views/risk/VarReportList.vue') }
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