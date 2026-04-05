import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

// 页面组件 - 业务单元
import BusinessUnitList from './views/basedata/BusinessUnitList.vue'
import BusinessUnitForm from './views/basedata/BusinessUnitForm.vue'

// 页面组件 - 币种
import CurrencyList from './views/basedata/CurrencyList.vue'
import CurrencyForm from './views/basedata/CurrencyForm.vue'

// 页面组件 - 国家
import CountryList from './views/basedata/CountryList.vue'
import CountryForm from './views/basedata/CountryForm.vue'

// 页面组件 - 银行
import BankList from './views/basedata/BankList.vue'
import BankForm from './views/basedata/BankForm.vue'

// 页面组件 - 交易对手
import CounterpartyList from './views/basedata/CounterpartyList.vue'
import CounterpartyForm from './views/basedata/CounterpartyForm.vue'

// 页面组件 - 对手方账户
import CounterpartyAccountList from './views/basedata/CounterpartyAccountList.vue'
import CounterpartyAccountForm from './views/basedata/CounterpartyAccountForm.vue'

// 页面组件 - 交易员
import TraderList from './views/basedata/TraderList.vue'
import TraderForm from './views/basedata/TraderForm.vue'

// 路由配置
const routes = [
  { path: '/', redirect: '/basedata/currency' },
  // 业务单元
  { path: '/basedata/business-unit', component: BusinessUnitList },
  { path: '/basedata/business-unit/add', component: BusinessUnitForm },
  { path: '/basedata/business-unit/edit/:id', component: BusinessUnitForm },
  // 币种
  { path: '/basedata/currency', component: CurrencyList },
  { path: '/basedata/currency/add', component: CurrencyForm },
  { path: '/basedata/currency/edit/:id', component: CurrencyForm },
  // 国家
  { path: '/basedata/country', component: CountryList },
  { path: '/basedata/country/add', component: CountryForm },
  { path: '/basedata/country/edit/:id', component: CountryForm },
  // 银行
  { path: '/basedata/bank', component: BankList },
  { path: '/basedata/bank/add', component: BankForm },
  { path: '/basedata/bank/edit/:id', component: BankForm },
  // 交易对手
  { path: '/basedata/counterparty', component: CounterpartyList },
  { path: '/basedata/counterparty/add', component: CounterpartyForm },
  { path: '/basedata/counterparty/edit/:id', component: CounterpartyForm },
  // 对手方账户
  { path: '/basedata/counterparty-account', component: CounterpartyAccountList },
  { path: '/basedata/counterparty-account/add', component: CounterpartyAccountForm },
  { path: '/basedata/counterparty-account/edit/:id', component: CounterpartyAccountForm },
  // 交易员
  { path: '/basedata/trader', component: TraderList },
  { path: '/basedata/trader/add', component: TraderForm },
  { path: '/basedata/trader/edit/:id', component: TraderForm },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')