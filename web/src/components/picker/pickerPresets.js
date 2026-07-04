/**
 * BaseDataPicker 预设配置
 * 11 个基础数据实体的列表选择器配置
 *
 * 每个 preset:
 *   - resource      资源标识(URL 段,展示用)
 *   - label         中文展示名
 *   - listApi       分页查询函数 (params) => Promise<{ data: { records, total, ... } }>
 *   - getApi        单条查询函数 (id) => Promise<{ data: row }>    可选
 *   - columns       表格列定义 [{ prop, label, width }]
 *   - returnField   v-model 绑定字段 ('id' | 'code' 等)
 *   - displayFormat 行 -> 显示文本
 *   - searchFields  提示字段
 *   - defaultFilters 默认过滤参数
 */
import request from '@/utils/request'

/* ============ 工具:把分页响应转成 { records, total } ============ */
function unwrap(res) {
  return {
    records: res?.data?.records || res?.data?.list || [],
    total: res?.data?.total || 0
  }
}

/* ============ 通用 listApi 工厂 ============ */
function pageApi(url, defaults = {}) {
  return (params = {}) =>
    request({
      url,
      method: 'get',
      params: { pageNum: 1, pageSize: 20, ...defaults, ...params }
    }).then(unwrap)
}

/* ============ 各实体 listApi ============ */
const listSubsidiaryApi = pageApi('/api/v1/subsidiaries/page')
const listMgmtEntityApi = pageApi('/api/v1/management-entities/page')
const listTraderApi = pageApi('/api/v1/traders/page')
const listCurrencyApi = pageApi('/api/v1/currencies/page')
const listCountryApi = pageApi('/api/v1/countries/page')
const listHolidayApi = pageApi('/api/v1/holidays/page')
const listCounterpartyApi = pageApi('/api/v1/counterparties/page')
const listCounterpartyAccountApi = pageApi('/api/v1/counterparty-accounts/page')
const listCurrencyPairApi = pageApi('/api/v1/currency-pairs/page')
const listInstrumentApi = pageApi('/api/v1/instruments/page')
const listBankAccountApi = pageApi('/api/v1/bank-accounts/page')

/* ============ 11 个 preset ============ */
export const pickerPresets = {
  subsidiary: {
    resource: 'subsidiaries',
    label: '子公司',
    listApi: listSubsidiaryApi,
    columns: [
      { prop: 'code', label: '编码', width: 140 },
      { prop: 'name', label: '名称', minWidth: 180 },
      { prop: 'countryName', label: '国家', width: 120 },
      { prop: 'status', label: '状态', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['code', 'name'],
    defaultFilters: {}
  },

  'management-entity': {
    resource: 'management-entities',
    label: '管理主体',
    listApi: listMgmtEntityApi,
    columns: [
      { prop: 'id', label: 'ID', width: 60 },
      { prop: 'code', label: '主体编码', width: 140 },
      { prop: 'name', label: '名称', minWidth: 160 },
      { prop: 'entityType', label: '类型', width: 100 },
      { prop: 'status', label: '状态', width: 80 }
    ],
    returnField: 'code',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['id', 'code', 'name'],
    defaultFilters: {}
  },

  trader: {
    resource: 'traders',
    label: '交易员',
    listApi: listTraderApi,
    columns: [
      { prop: 'code', label: '编码', width: 120 },
      { prop: 'name', label: '姓名', minWidth: 140 },
      { prop: 'department', label: '部门', width: 120 },
      { prop: 'status', label: '状态', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['code', 'name'],
    defaultFilters: {}
  },

  currency: {
    resource: 'currencies',
    label: '币种',
    listApi: listCurrencyApi,
    columns: [
      { prop: 'code', label: '代码', width: 100 },
      { prop: 'name', label: '中文名', minWidth: 140 },
      { prop: 'enName', label: '英文名', minWidth: 140 },
      { prop: 'symbol', label: '符号', width: 80 }
    ],
    returnField: 'code',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['code', 'name'],
    defaultFilters: {}
  },

  country: {
    resource: 'countries',
    label: '国家',
    listApi: listCountryApi,
    columns: [
      { prop: 'code', label: '代码', width: 100 },
      { prop: 'name', label: '中文名', minWidth: 140 },
      { prop: 'enName', label: '英文名', minWidth: 160 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['code', 'name', 'enName'],
    defaultFilters: {}
  },

  holiday: {
    resource: 'holidays',
    label: '节假日',
    listApi: listHolidayApi,
    columns: [
      { prop: 'countryCode', label: '国家', width: 100 },
      { prop: 'holidayDate', label: '日期', width: 140 },
      { prop: 'name', label: '节日名', minWidth: 160 },
      { prop: 'isAdjacent', label: '调休', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.countryCode || ''} ${row.holidayDate || ''} ${row.name || ''}` : ''),
    searchFields: ['name', 'countryCode'],
    defaultFilters: {}
  },

  counterparty: {
    resource: 'counterparties',
    label: '交易对手',
    listApi: listCounterpartyApi,
    columns: [
      { prop: 'code', label: '编码', width: 140 },
      { prop: 'name', label: '名称', minWidth: 180 },
      { prop: 'counterpartyType', label: '类型', width: 100 },
      { prop: 'countryCode', label: '国家', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.code || ''} ${row.name ? '(' + row.name + ')' : ''}` : ''),
    searchFields: ['code', 'name'],
    defaultFilters: {}
  },

  'counterparty-account': {
    resource: 'counterparty-accounts',
    label: '对手方账户',
    listApi: listCounterpartyAccountApi,
    columns: [
      { prop: 'accountNo', label: '账号', width: 160 },
      { prop: 'accountName', label: '账户名', minWidth: 180 },
      { prop: 'counterpartyName', label: '所属对手方', width: 160 },
      { prop: 'currency', label: '币种', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.accountNo || ''} ${row.accountName ? '(' + row.accountName + ')' : ''}` : ''),
    searchFields: ['accountNo', 'accountName'],
    defaultFilters: {}
  },

  'currency-pair': {
    resource: 'currency-pairs',
    label: '币种对',
    listApi: listCurrencyPairApi,
    columns: [
      { prop: 'pairCode', label: '币种对', width: 120 },
      { prop: 'currency1', label: '货币1', width: 100 },
      { prop: 'currency2', label: '货币2', width: 100 },
      { prop: 'strongerCurrency', label: '强势币种', width: 100 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.pairCode || ''} ${row.currency1 || ''}/${row.currency2 || ''}${row.strongerCurrency ? ' ★' + row.strongerCurrency : ''}` : ''),
    searchFields: ['pairCode'],
    defaultFilters: {}
  },

  instrument: {
    resource: 'instruments',
    label: '金融工具',
    listApi: listInstrumentApi,
    columns: [
      { prop: 'instrumentCode', label: '工具编码', width: 160 },
      { prop: 'instrumentName', label: '工具名称', minWidth: 200 },
      { prop: 'instrumentType', label: '类型', width: 100 },
      { prop: 'currency', label: '币种', width: 80 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.instrumentCode || ''} ${row.instrumentName ? '(' + row.instrumentName + ')' : ''}` : ''),
    searchFields: ['instrumentCode', 'instrumentName'],
    defaultFilters: {}
  },

  'bank-account': {
    resource: 'bank-accounts',
    label: '银行账户',
    listApi: listBankAccountApi,
    columns: [
      { prop: 'accountNo', label: '账号', width: 160 },
      { prop: 'accountName', label: '账户名', minWidth: 200 },
      { prop: 'bankId', label: '银行', width: 100 },
      { prop: 'currency', label: '币种', width: 80 },
      { prop: 'accountType', label: '类型', width: 100 }
    ],
    returnField: 'id',
    displayFormat: (row) => (row ? `${row.accountNo || ''} ${row.accountName ? '(' + row.accountName + ')' : ''}` : ''),
    searchFields: ['accountNo', 'accountName'],
    defaultFilters: {}
  }
}

/* ============ 工具函数 ============ */
export function getPreset(entityKey) {
  const preset = pickerPresets[entityKey]
  if (!preset) {
    throw new Error(`[BaseDataPicker] 未知 entity: ${entityKey}`)
  }
  return preset
}

export function getPresetEntities() {
  return Object.keys(pickerPresets)
}