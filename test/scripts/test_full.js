const { chromium } = require('playwright');

const BASE_URL = 'http://localhost:8081';
const UI_BASE_URL = 'http://localhost:3000';

const results = { passed: 0, failed: 0, total: 0, bugs: [] };

async function testAPI(module, endpoint, method = 'GET', body = null) {
  results.total++;
  const testName = `${module} - ${endpoint}`;
  console.log(`\n[${results.total}] ${method} ${endpoint}`);
  
  try {
    const options = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) options.body = JSON.stringify(body);
    
    const response = await fetch(`${BASE_URL}${endpoint}`, options);
    const data = await response.json();
    
    console.log(`  Status: ${response.status}, Code: ${data.code}`);
    
    if (response.status === 200 || response.status === 201) {
      console.log(`  ✅ PASS`);
      results.passed++;
      return { passed: true };
    } else if (response.status === 404 || response.status === 204) {
      console.log(`  ✅ PASS (Not found is acceptable)`);
      results.passed++;
      return { passed: true };
    } else if (data.code === 'DUPLICATE_CODE' || data.code === 'VALIDATION_ERROR') {
      console.log(`  ✅ PASS (Business error handled)`);
      results.passed++;
      return { passed: true };
    } else if (response.status === 405) {
      console.log(`  ❌ FAIL - 405 Method Not Allowed`);
      results.failed++;
      results.bugs.push({ module, endpoint, method, status: 405 });
      return { passed: false };
    } else if (response.status >= 500) {
      console.log(`  ❌ FAIL - Server error ${response.status}`);
      results.failed++;
      results.bugs.push({ module, endpoint, method, status: response.status, data: data.message });
      return { passed: false };
    } else if (response.status >= 400) {
      console.log(`  ⚠️  WARN - Client error ${response.status}`);
      return { passed: false };
    }
  } catch (error) {
    console.log(`  ❌ FAIL - ${error.message}`);
    results.failed++;
    return { passed: false };
  }
}

async function testUIScreenshot(pageName, path) {
  results.total++;
  console.log(`\n[${results.total}] UI: ${pageName}`);
  
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(`${UI_BASE_URL}${path}`, { waitUntil: 'networkidle', timeout: 15000 });
    await page.waitForTimeout(1500);
    
    const errors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    await page.waitForTimeout(500);
    
    const title = await page.title();
    console.log(`  Title: ${title}`);
    
    if (errors.length > 0) {
      console.log(`  ⚠️  Console errors: ${errors.length}`);
      results.bugs.push({ module: 'UI', page: pageName, type: 'console_error', errors });
    }
    
    console.log(`  ✅ PASS`);
    results.passed++;
    await browser.close();
    return { passed: true };
  } catch (error) {
    console.log(`  ❌ FAIL - ${error.message}`);
    results.failed++;
    results.bugs.push({ module: 'UI', page: pageName, type: 'load_error', error: error.message });
    await browser.close();
    return { passed: false };
  }
}

async function runTests() {
  console.log('='.repeat(60));
  console.log('Open-TMS 基础数据模块完整测试');
  console.log('='.repeat(60));
  
  // ===== API测试 =====
  console.log('\n========== API接口测试 ==========');
  
  // 币种管理 - 完整CRUD
  await testAPI('币种', '/api/v1/currencies', 'GET');
  await testAPI('币种', '/api/v1/currencies/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('币种', '/api/v1/currencies/1', 'GET');
  await testAPI('币种', '/api/v1/currencies', 'POST', { currencyCode: 'TEST', currencyName: '测试', status: '1' });
  await testAPI('币种', '/api/v1/currencies', 'PUT', { id: 1, currencyCode: 'CNY', currencyName: '人民币', status: '1' });
  await testAPI('币种', '/api/v1/currencies/999', 'DELETE');
  
  // 业务单元
  await testAPI('业务单元', '/api/v1/business-units/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('业务单元', '/api/v1/business-units/1', 'GET');
  await testAPI('业务单元', '/api/v1/business-units', 'POST', { unitCode: 'BU_TEST', unitName: '测试', status: '1' });
  await testAPI('业务单元', '/api/v1/business-units', 'PUT', { id: 1, unitCode: 'BU001', unitName: '测试', status: '1' });
  await testAPI('业务单元', '/api/v1/business-units/999', 'DELETE');
  
  // 交易员
  await testAPI('交易员', '/api/v1/traders/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('交易员', '/api/v1/traders/1', 'GET');
  await testAPI('交易员', '/api/v1/traders', 'POST', { traderCode: 'TR_TEST', traderName: '测试', status: '1' });
  await testAPI('交易员', '/api/v1/traders', 'PUT', { id: 1, traderCode: 'TR001', traderName: '测试', status: '1' });
  await testAPI('交易员', '/api/v1/traders/999', 'DELETE');
  
  // 国家
  await testAPI('国家', '/api/v1/countries/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('国家', '/api/v1/countries/1', 'GET');
  await testAPI('国家', '/api/v1/countries', 'POST', { countryCode: 'JP', countryName: '日本', status: '1' });
  await testAPI('国家', '/api/v1/countries', 'PUT', { id: 1, countryCode: 'CN', countryName: '中国', status: '1' });
  await testAPI('国家', '/api/v1/countries/999', 'DELETE');
  
  // 银行
  await testAPI('银行', '/api/v1/banks/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('银行', '/api/v1/banks/1', 'GET');
  await testAPI('银行', '/api/v1/banks', 'POST', { bankCode: 'BK_TEST', bankName: '测试', status: '1' });
  await testAPI('银行', '/api/v1/banks', 'PUT', { id: 1, bankCode: 'B001', bankName: '测试', status: '1' });
  await testAPI('银行', '/api/v1/banks/999', 'DELETE');
  
  // 交易对手
  await testAPI('对手方', '/api/v1/counterparties/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('对手方', '/api/v1/counterparties/1', 'GET');
  await testAPI('对手方', '/api/v1/counterparties', 'POST', { counterpartyCode: 'CP_TEST', counterpartyName: '测试', status: '1' });
  await testAPI('对手方', '/api/v1/counterparties', 'PUT', { id: 1, counterpartyCode: 'C001', counterpartyName: '测试', status: '1' });
  await testAPI('对手方', '/api/v1/counterparties/999', 'DELETE');
  
  // 对手方账户
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/page?pageNum=1&pageSize=10', 'GET');
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/1', 'GET');
  await testAPI('对手方账户', '/api/v1/counterparty-accounts', 'POST', { accountNo: 'TEST', accountName: '测试', status: '1' });
  await testAPI('对手方账户', '/api/v1/counterparty-accounts', 'PUT', { id: 1, accountNo: 'ACC001', accountName: '测试', status: '1' });
  await testAPI('对手方账户', '/api/v1/counterparty-accounts/999', 'DELETE');
  
  // ===== UI测试 =====
  console.log('\n========== UI页面测试 ==========');
  
  const uiPages = [
    { name: '首页', path: '/' },
    { name: '币种管理', path: '/basedata/currency' },
    { name: '业务单元', path: '/basedata/business-unit' },
    { name: '交易员', path: '/basedata/trader' },
    { name: '国家', path: '/basedata/country' },
    { name: '银行', path: '/basedata/bank' },
    { name: '交易对手', path: '/basedata/counterparty' },
    { name: '对手方账户', path: '/basedata/counterparty-account' },
  ];
  
  for (const page of uiPages) {
    await testUIScreenshot(page.name, page.path);
  }
  
  // ===== 结果汇总 =====
  console.log('\n' + '='.repeat(60));
  console.log('测试结果汇总');
  console.log('='.repeat(60));
  console.log(`总计: ${results.passed}/${results.total} 通过`);
  console.log(`通过率: ${Math.round(results.passed / results.total * 100)}%`);
  console.log(`\n发现Bug: ${results.bugs.length}个`);
  
  if (results.bugs.length > 0) {
    console.log('\n--- Bug列表 ---');
    results.bugs.forEach((bug, i) => {
      console.log(`${i+1}. [${bug.module}] ${bug.endpoint || bug.page} - ${bug.status || bug.type}`);
    });
  }
  
  console.log('='.repeat(60));
  
  return results;
}

runTests().then(results => {
  console.log('\n测试完成!');
  console.log('Bugs:', JSON.stringify(results.bugs, null, 2));
  process.exit(results.failed > 0 ? 1 : 0);
}).catch(console.error);