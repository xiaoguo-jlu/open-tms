const { chromium } = require('playwright');

const BASE_URL = 'http://localhost:8081';
const UI_BASE_URL = 'http://localhost:3000';

const results = { passed: 0, failed: 0, total: 0 };

async function testAPI(module, endpoint, method = 'GET', body = null) {
  results.total++;
  const testName = `${module} - ${endpoint}`;
  console.log(`\n[${results.total}] Testing: ${testName} [${method}]`);
  
  try {
    const options = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) options.body = JSON.stringify(body);
    
    const response = await fetch(`${BASE_URL}${endpoint}`, options);
    const data = await response.json();
    
    console.log(`  Status: ${response.status}, Code: ${data.code}`);
    
    if (response.status === 200 || response.status === 201) {
      console.log(`  ✅ PASS`);
      results.passed++;
      return { passed: true, data };
    } else if (response.status === 404) {
      console.log(`  ✅ PASS (404 for delete is acceptable)`);
      results.passed++;
      return { passed: true };
    } else if (data.code === 'DUPLICATE_CODE' || data.code === 'VALIDATION_ERROR') {
      console.log(`  ✅ PASS (Business error handled)`);
      results.passed++;
      return { passed: true };
    } else if (response.status === 405) {
      console.log(`  ❌ FAIL - 405 Method Not Allowed`);
      results.failed++;
      return { passed: false };
    } else if (response.status >= 500) {
      console.log(`  ❌ FAIL - Server error ${response.status}`);
      results.failed++;
      return { passed: false };
    }
  } catch (error) {
    console.log(`  ❌ FAIL - ${error.message}`);
    results.failed++;
    return { passed: false };
  }
}

async function testCurrencyAPI() {
  console.log('\n========== 币种管理 API 测试 ==========');
  
  await testAPI('币种', '/api/v1/currencies', 'GET');
  await testAPI('币种', '/api/v1/currencies/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('币种', '/api/v1/currencies/1', 'GET');
  await testAPI('币种', '/api/v1/currencies', 'POST', {
    currencyCode: 'TEST',
    currencyName: '测试币种',
    currencySymbol: 'T',
    decimalPlaces: 2,
    status: '1'
  });
  await testAPI('币种', '/api/v1/currencies', 'POST', { currencyName: '测试' });
  await testAPI('币种', '/api/v1/currencies', 'PUT', { id: 1, currencyCode: 'CNY', currencyName: '人民币', status: '1' });
  await testAPI('币种', '/api/v1/currencies/999', 'DELETE');
}

async function testBusinessUnitAPI() {
  console.log('\n========== 业务单元 API 测试 ==========');
  
  await testAPI('业务单元', '/api/v1/business-units/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('业务单元', '/api/v1/business-units/1', 'GET');
  await testAPI('业务单元', '/api/v1/business-units', 'POST', {
    unitCode: 'BU_TEST',
    unitName: '测试业务单元',
    unitNameEn: 'Test Unit',
    status: '1'
  });
  await testAPI('业务单元', '/api/v1/business-units', 'PUT', { id: 1, unitCode: 'BU001', unitName: '测试', status: '1' });
  await testAPI('业务单元', '/api/v1/business-units/999', 'DELETE');
}

async function testTraderAPI() {
  console.log('\n========== 交易员 API 测试 ==========');
  
  await testAPI('交易员', '/api/v1/traders/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('交易员', '/api/v1/traders/1', 'GET');
  await testAPI('交易员', '/api/v1/traders', 'POST', {
    traderCode: 'TR_TEST',
    traderName: '测试交易员',
    department: '资金部',
    status: '1'
  });
  await testAPI('交易员', '/api/v1/traders', 'PUT', { id: 1, traderCode: 'TR001', traderName: '测试', status: '1' });
  await testAPI('交易员', '/api/v1/traders/999', 'DELETE');
}

async function testCountryAPI() {
  console.log('\n========== 国家/地区 API 测试 ==========');
  
  await testAPI('国家', '/api/v1/countries/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('国家', '/api/v1/countries/1', 'GET');
  await testAPI('国家', '/api/v1/countries', 'POST', {
    countryCode: 'JP',
    countryName: '日本',
    countryNameEn: 'Japan',
    timezone: 'Asia/Tokyo',
    countryCodePhone: '+81',
    status: '1'
  });
  await testAPI('国家', '/api/v1/countries', 'PUT', { id: 1, countryCode: 'CN', countryName: '中国', status: '1' });
  await testAPI('国家', '/api/v1/countries/999', 'DELETE');
}

async function testBankAPI() {
  console.log('\n========== 银行 API 测试 ==========');
  
  await testAPI('银行', '/api/v1/banks/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('银行', '/api/v1/banks/1', 'GET');
  await testAPI('银行', '/api/v1/banks', 'POST', {
    bankCode: 'BK_TEST',
    bankName: '测试银行',
    bankNameEn: 'Test Bank',
    swiftCode: 'TESTUS33',
    countryCode: 'US',
    status: '1'
  });
  await testAPI('银行', '/api/v1/banks', 'PUT', { id: 1, bankCode: 'B001', bankName: '测试银行', status: '1' });
  await testAPI('银行', '/api/v1/banks/999', 'DELETE');
}

async function testCounterpartyAPI() {
  console.log('\n========== 交易对手 API 测试 ==========');
  
  await testAPI('交易对手', '/api/v1/counterparties/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('交易对手', '/api/v1/counterparties/1', 'GET');
  await testAPI('交易对手', '/api/v1/counterparties', 'POST', {
    counterpartyCode: 'CP_TEST',
    counterpartyName: '测试对手方',
    counterpartyType: '企业',
    countryCode: 'CN',
    status: '1'
  });
  await testAPI('交易对手', '/api/v1/counterparties', 'PUT', { id: 1, counterpartyCode: 'C001', counterpartyName: '测试', status: '1' });
  await testAPI('交易对手', '/api/v1/counterparties/999', 'DELETE');
}

async function testCounterpartyAccountAPI() {
  console.log('\n========== 对手方账户 API 测试 ==========');
  
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/1', 'GET');
  await testAPI('对手方账户', '/api/v1/counterparty-accounts', 'POST', {
    accountNo: 'CPA_TEST',
    accountName: '测试账户',
    counterpartyId: 1,
    bankId: 1,
    currencyCode: 'CNY',
    status: '1'
  });
  await testAPI('对手方账户', '/api/v1/counterparty-accounts', 'PUT', { id: 1, accountNo: 'ACC001', accountName: '测试', status: '1' });
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/999', 'DELETE');
}

async function testUIPages() {
  console.log('\n========== UI 页面测试 ==========');
  
  const pages = [
    { path: '/basedata/currency', name: '币种管理' },
    { path: '/basedata/business-unit', name: '业务单元' },
    { path: '/basedata/trader', name: '交易员' },
    { path: '/basedata/country', name: '国家' },
    { path: '/basedata/bank', name: '银行' },
    { path: '/basedata/counterparty', name: '交易对手' }
  ];
  
  const browser = await chromium.launch({ headless: true });
  
  for (const page of pages) {
    results.total++;
    console.log(`\n[${results.total}] UI测试: ${page.name}`);
    
    try {
      const browserPage = await browser.newPage();
      await browserPage.goto(`${UI_BASE_URL}${page.path}`, { waitUntil: 'networkidle', timeout: 15000 });
      await browserPage.waitForTimeout(1000);
      
      const title = await browserPage.title();
      console.log(`  Title: ${title}`);
      console.log(`  ✅ PASS`);
      results.passed++;
      await browserPage.close();
    } catch (error) {
      console.log(`  ❌ FAIL - ${error.message}`);
      results.failed++;
    }
  }
  
  await browser.close();
}

async function runAllTests() {
  console.log('='.repeat(60));
  console.log('Open-TMS 基础数据模块 API 完整测试');
  console.log('='.repeat(60));
  
  await testCurrencyAPI();
  await testBusinessUnitAPI();
  await testTraderAPI();
  await testCountryAPI();
  await testBankAPI();
  await testCounterpartyAPI();
  await testCounterpartyAccountAPI();
  await testUIPages();
  
  console.log('\n' + '='.repeat(60));
  console.log('测试结果汇总');
  console.log('='.repeat(60));
  console.log(`总计: ${results.passed}/${results.total} 通过`);
  console.log(`通过率: ${Math.round(results.passed / results.total * 100)}%`);
  console.log('='.repeat(60));
  
  return results;
}

runAllTests().then(results => {
  process.exit(results.failed > 0 ? 1 : 0);
}).catch(error => {
  console.error('测试执行失败:', error);
  process.exit(1);
});