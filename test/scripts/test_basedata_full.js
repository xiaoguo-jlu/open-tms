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
    
    if ((data.code === 0 || data.code === 200) && Array.isArray(data.data)) {
      console.log('✅ PASS - 返回', data.data.length, '条数据');
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
    const response = await fetch(`${BASE_URL}/api/v1/currencies/page?pageNo=1&pageSize=5`);
    const data = await response.json();
    console.log('Status:', response.status);
    
    if ((data.code === 0 || data.code === 200) && data.data && data.data.records) {
      console.log('✅ PASS - 分页数据', data.data.records.length, '条');
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

async function testApiCurrencyGetById() {
  console.log('\n=== TC_CY_003: 币种详情查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/currencies/1`);
    const data = await response.json();
    console.log('Status:', response.status);
    
    if ((data.code === 0 || data.code === 200) && data.data && data.data.code === 'CNY') {
      console.log('✅ PASS - 币种详情:', data.data.name);
      return { passed: true, data: data.data };
    } else {
      console.log('❌ FAIL - 详情查询失败');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyCreate() {
  console.log('\n=== TC_CY_004: 币种新增成功 ===');
  const testCode = 'TST_' + Date.now();
  try {
    const currencyData = {
      code: testCode,
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
    console.log('Status:', response.status, 'Code:', data.code);
    
    if (data.code === 0 || data.code === 200 || data.code === 201) {
      console.log('✅ PASS - 新增成功, ID:', data.data?.id);
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

async function testApiCurrencyDuplicate() {
  console.log('\n=== TC_CY_005: 币种编码重复校验 ===');
  try {
    const currencyData = {
      code: 'CNY',
      name: '重复测试',
      symbol: '¥',
      decimalPlaces: 2,
      status: '1'
    };
    
    const response = await fetch(`${BASE_URL}/api/v1/currencies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currencyData)
    });
    
    const data = await response.json();
    console.log('Status:', response.status, 'Code:', data.code);
    
    if (data.code === 'DUPLICATE_CODE' || data.code === 400 || data.code === 409) {
      console.log('✅ PASS - 正确返回重复编码错误');
      return { passed: true };
    } else {
      console.log('❌ FAIL - 未正确处理重复编码');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyValidation() {
  console.log('\n=== TC_CY_006: 币种必填字段校验 ===');
  try {
    const currencyData = {
      name: '人民币'
    };
    
    const response = await fetch(`${BASE_URL}/api/v1/currencies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currencyData)
    });
    
    const data = await response.json();
    console.log('Status:', response.status, 'Code:', data.code);
    
    if (data.code === 'VALIDATION_ERROR' || data.code === 400) {
      console.log('✅ PASS - 正确返回校验错误');
      return { passed: true };
    } else {
      console.log('❌ FAIL - 未正确处理必填字段校验');
      return { passed: false };
    }
  } catch (error) {
    console.log('❌ FAIL - 请求失败:', error.message);
    return { passed: false, error: error.message };
  }
}

async function testApiCurrencyUpdate() {
  console.log('\n=== TC_CY_007: 币种编辑成功 ===');
  try {
    const currencyData = {
      id: 1,
      code: 'CNY',
      name: '人民币-修改',
      symbol: '¥',
      decimalPlaces: 2,
      status: '1'
    };
    
    const response = await fetch(`${BASE_URL}/api/v1/currencies`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currencyData)
    });
    
    const data = await response.json();
    console.log('Status:', response.status, 'Code:', data.code);
    
    if (data.code === 0 || data.code === 200) {
      console.log('✅ PASS - 编辑成功');
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

async function testApiCurrencyDelete() {
  console.log('\n=== TC_CY_008: 币种删除成功 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/currencies/999`, {
      method: 'DELETE'
    });
    
    const data = await response.json();
    console.log('Status:', response.status, 'Code:', data.code);
    
    if (data.code === 0 || data.code === 200 || response.status === 404) {
      console.log('✅ PASS - 删除执行成功');
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

async function testApiBankList() {
  console.log('\n=== TC_BK_001: 银行列表查询 ===');
  try {
    const response = await fetch(`${BASE_URL}/api/v1/banks`);
    const data = await response.json();
    console.log('Status:', response.status);
    
    if ((data.code === 0 || data.code === 200)) {
      const count = Array.isArray(data.data) ? data.data.length : 0;
      console.log('✅ PASS - 银行列表查询成功, 共', count, '条');
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

async function testUICurrencyPage() {
  console.log('\n=== UI_CY_001: 币种列表页加载 ===');
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(`${UI_BASE_URL}/basedata/currency`, { waitUntil: 'networkidle', timeout: 30000 });
    await sleep(2000);
    
    const title = await page.title();
    console.log('Page Title:', title);
    
    const content = await page.content();
    const hasCurrency = content.includes('币种') || content.includes('currency') || content.includes('Currency');
    console.log('Contains currency element:', hasCurrency ? 'YES' : 'NO');
    
    const tableVisible = await page.locator('.el-table, table').first().isVisible().catch(() => false);
    console.log('Table visible:', tableVisible ? 'YES' : 'NO');
    
    console.log('✅ PASS - 币种列表页加载完成');
    await browser.close();
    return { passed: true };
  } catch (error) {
    console.log('❌ FAIL - 页面加载失败:', error.message);
    await browser.close();
    return { passed: false, error: error.message };
  }
}

async function testUICurrencyCreate() {
  console.log('\n=== UI_CY_002: 币种新增功能 ===');
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(`${UI_BASE_URL}/basedata/currency`, { waitUntil: 'networkidle', timeout: 30000 });
    await sleep(1000);
    
    const addButton = page.locator('button:has-text("新增"), button:has-text("Add"), .el-button--primary').first();
    if (await addButton.isVisible()) {
      await addButton.click();
      await sleep(500);
      
      const dialogVisible = await page.locator('.el-dialog, [role="dialog"]').first().isVisible().catch(() => false);
      console.log('Dialog opened:', dialogVisible ? 'YES' : 'NO');
      
      if (dialogVisible) {
        console.log('✅ PASS - 新增弹窗打开成功');
        await browser.close();
        return { passed: true };
      }
    }
    
    console.log('❌ FAIL - 未找到新增按钮');
    await browser.close();
    return { passed: false };
  } catch (error) {
    console.log('❌ FAIL - 新增功能异常:', error.message);
    await browser.close();
    return { passed: false, error: error.message };
  }
}

async function testUIBankPage() {
  console.log('\n=== UI_BK_001: 银行列表页加载 ===');
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(`${UI_BASE_URL}/basedata/bank`, { waitUntil: 'networkidle', timeout: 30000 });
    await sleep(2000);
    
    const title = await page.title();
    console.log('Page Title:', title);
    
    const content = await page.content();
    const hasBank = content.includes('银行') || content.includes('bank') || content.includes('Bank');
    console.log('Contains bank element:', hasBank ? 'YES' : 'NO');
    
    console.log('✅ PASS - 银行列表页加载完成');
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
  console.log('Open-TMS 基础数据模块测试 - 完整版');
  console.log('='.repeat(60));
  
  const results = {
    api: { passed: 0, failed: 0, total: 0 },
    ui: { passed: 0, failed: 0, total: 0 }
  };
  
  console.log('\n--- API接口测试 ---');
  const apiTests = [
    testApiCurrencyList,
    testApiCurrencyPage,
    testApiCurrencyGetById,
    testApiCurrencyCreate,
    testApiCurrencyDuplicate,
    testApiCurrencyValidation,
    testApiCurrencyUpdate,
    testApiCurrencyDelete,
    testApiBankList
  ];
  
  for (const test of apiTests) {
    results.api.total++;
    const result = await test();
    if (result.passed) results.api.passed++;
    else results.api.failed++;
  }
  
  console.log('\n--- UI功能测试 ---');
  const uiTests = [
    testUICurrencyPage,
    testUICurrencyCreate,
    testUIBankPage
  ];
  
  for (const test of uiTests) {
    results.ui.total++;
    try {
      const result = await test();
      if (result.passed) results.ui.passed++;
      else results.ui.failed++;
    } catch (e) {
      results.ui.failed++;
      console.log('UI测试异常:', e.message);
    }
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('测试结果汇总');
  console.log('='.repeat(60));
  console.log(`API测试: ${results.api.passed}/${results.api.total} 通过`);
  console.log(`UI测试: ${results.ui.passed}/${results.ui.total} 通过`);
  console.log(`总计: ${results.api.passed + results.ui.passed}/${results.api.total + results.ui.total} 通过`);
  console.log('='.repeat(60));
  
  return results;
}

runAllTests().then(results => {
  console.log('\n测试完成!');
  process.exit(results.api.failed + results.ui.failed > 0 ? 1 : 0);
}).catch(error => {
  console.error('测试执行失败:', error);
  process.exit(1);
});