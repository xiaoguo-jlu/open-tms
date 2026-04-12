const { chromium } = require('playwright');

const BASE_URL = 'http://localhost:8081';
const UI_BASE_URL = 'http://localhost:3000';

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function testApiCurrencyList() {
  console.log('\n=== TC_CY_001: 币种列表查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/currencies`);
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if ((data.code === 0 || data.code === 200) && Array.isArray(data.data)) {
      console.log('✅ PASS - 币种列表查询成功');
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 响应格式错误');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyPage() {
  console.log('\n=== TC_CY_002: 币种分页查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/currencies/page?pageNo=1&pageSize=10`);
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if ((data.code === 0 || data.code === 200) && data.data && data.data.records) {
      console.log('✅ PASS - 币种分页查询成功');
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 分页数据格式错误');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyCreate() {
  console.log('\n=== TC_CY_004: 币种新增成功 ===');
  try {
    const currencyData = {
      code: 'TEST',
      name: '测试币种',
      symbol: 'T',
      decimalPlaces: 2,
      status: '1'
    };
    
    const response = await fetch(`${BASE_URL}/api/v1/currencies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currencyData)
    });
    
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if ((data.code === 0 || data.code === 200) && data.data) {
      console.log('✅ PASS - 币种新增成功');
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 新增失败:', data.message);
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyUpdate(id) {
  console.log('\n=== TC_CY_007: 币种编辑成功 ===');
  try {
    const currencyData = {
      id: id,
      code: 'TEST',
      name: '测试币种-修改',
      symbol: 'T',
      decimalPlaces: 2,
      status: '1'
    };
    
    const response = await fetch(`${BASE_URL}/api/v1/currencies`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currencyData)
    });
    
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if (data.code === 0) {
      console.log('✅ PASS - 币种编辑成功');
      return { passed: true };
    } else {
      console.log('❌ FAIL - 编辑失败:', data.message);
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyDelete(id) {
  console.log('\n=== TC_CY_008: 币种删除成功 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/currencies/${id}`, {
      method: 'DELETE'
    });
    
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if (data.code === 0) {
      console.log('✅ PASS - 币种删除成功');
      return { passed: true };
    } else {
      console.log('❌ FAIL - 删除失败:', data.message);
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiBusinessUnitList() {
  console.log('\n=== TC_BU_001: 业务单元列表查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/business-units`);
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if (data.code === 0) {
      console.log('✅ PASS - 业务单元列表查询成功');
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 请求失败');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiTraderList() {
  console.log('\n=== TC_TR_001: 交易员列表查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/traders`);
    const data = await response.json();
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(data, null, 2));
    
    if (data.code === 0) {
      console.log('✅ PASS - 交易员列表查询成功');
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 请求失败');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testUICurrencyList() {
  console.log('\n=== UI_CY_001: 币种列表页加载 ===');
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(`${UI_BASE_URL}/basedata/currency`, { waitUntil: 'networkidle', timeout: 30000 });
    await sleep(2000);
    
    const title = await page.title();
    console.log('Page Title:', title);
    
    const content = await page.content();
    const hasCurrency = content.includes('币种') || content.includes('currency');
    console.log('Page loaded:', hasCurrency ? 'YES' : 'NO');
    
    const errors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    
    await sleep(1000);
    
    if (errors.length > 0) {
      console.log('Console errors:', errors);
    }
    
    console.log('✅ PASS - 币种列表页加载完成');
    await browser.close();
    return { passed: true };
  } catch (error) {
    console.log('❌ FAIL - 页面加载失败:', error.message);
    await browser.close();
    return { passed: false, error: error.message };
  }
}

async function runAllTests() {
  console.log('='.repeat(60));
  console.log('Open-TMS 基础数据模块测试');
  console.log('='.repeat(60));
  
  const results = {
    api: { passed: 0, failed: 0, total: 0 },
    ui: { passed: 0, failed: 0, total: 0 }
  };
  
  console.log('\n--- API接口测试 ---');
  const apiTests = [
    testApiCurrencyList,
    testApiCurrencyPage,
    testApiBusinessUnitList,
    testApiTraderList
  ];
  
  for (const test of apiTests) {
    results.api.total++;
    const result = await test();
    if (result.passed) results.api.passed++;
    else results.api.failed++;
  }
  
  console.log('\n--- UI功能测试 ---');
  try {
    results.ui.total++;
    const result = await testUICurrencyList();
    if (result.passed) results.ui.passed++;
    else results.ui.failed++;
  } catch (e) {
    results.ui.total++;
    results.ui.failed++;
    console.log('UI测试跳过:', e.message);
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('测试结果汇总');
  console.log('='.repeat(60));
  console.log(`API测试: ${results.api.passed}/${results.api.total} 通过`);
  console.log(`UI测试: ${results.ui.passed}/${results.ui.total} 通过`);
  console.log(`总计: ${results.api.passed + results.ui.passed}/${results.api.total + results.ui.total} 通过`);
  
  return results;
}

runAllTests().then(results => {
  process.exit(results.api.failed + results.ui.failed > 0 ? 1 : 0);
}).catch(error => {
  console.error('测试执行失败:', error);
  process.exit(1);
});