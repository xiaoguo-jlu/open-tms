import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/basedata/currency'
  },
  {
    path: '/basedata',
    name: 'Basedata',
    children: [
      {
        path: 'business-unit',
        name: 'BusinessUnit',
        component: () => import('@/views/basedata/BusinessUnitList.vue')
      },
      {
        path: 'trader',
        name: 'Trader',
        component: () => import('@/views/basedata/TraderList.vue')
      },
      {
        path: 'currency',
        name: 'Currency',
        component: () => import('@/views/basedata/CurrencyList.vue')
      },
      {
        path: 'country',
        name: 'Country',
        component: () => import('@/views/basedata/CountryList.vue')
      },
      {
        path: 'holiday',
        name: 'Holiday',
        component: () => import('@/views/basedata/HolidayList.vue')
      },
      {
        path: 'bank',
        name: 'Bank',
        component: () => import('@/views/basedata/BankList.vue')
      },
      {
        path: 'counterparty',
        name: 'Counterparty',
        component: () => import('@/views/basedata/CounterpartyList.vue')
      },
      {
        path: 'counterparty-account',
        name: 'CounterpartyAccount',
        component: () => import('@/views/basedata/CounterpartyAccountList.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router