<template>
  <div class="api-docs-page">
    <el-card class="header-card">
      <div class="header-row">
        <h2>Open-TMS 接口文档</h2>
        <div class="module-tabs">
          <el-radio-group v-model="activeModule" size="default">
            <el-radio-button label="all">全部模块</el-radio-button>
            <el-radio-button label="basedata">基于数据 (CXF)</el-radio-button>
            <el-radio-button label="dealing">交易 (Spring MVC)</el-radio-button>
          </el-radio-group>
        </div>
        <div class="actions">
          <el-button size="small" @click="reload">刷新</el-button>
          <el-tag size="small" type="info">后端 OpenAPI 3.0</el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="swagger-card">
      <div id="swagger-ui" ref="swaggerEl"></div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { SwaggerUIBundle, SwaggerUIStandalonePreset } from 'swagger-ui-dist'
import 'swagger-ui-dist/swagger-ui.css'

const swaggerEl = ref(null)
const activeModule = ref('all')
let ui = null

// urls 选择器:基于数据 + 交易
const urlsConfig = [
  { url: '/api/v1/openapi/cxf', name: '基于数据 (CXF)' },
  { url: '/v3/api-docs', name: '交易 (Spring MVC)' }
]

async function buildSwagger() {
  // 清空旧内容
  if (swaggerEl.value) {
    swaggerEl.value.innerHTML = ''
  }
  if (ui) {
    try { ui = null } catch (e) { /* noop */ }
  }

  let urls
  if (activeModule.value === 'basedata') {
    urls = [urlsConfig[0]]
  } else if (activeModule.value === 'dealing') {
    urls = [urlsConfig[1]]
  } else {
    urls = urlsConfig
  }

  try {
    ui = SwaggerUIBundle({
      urls,
      dom_id: '#swagger-ui',
      deepLinking: true,
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIStandalonePreset
      ],
      plugins: [
        SwaggerUIBundle.plugins.DownloadUrl
      ],
      layout: 'StandaloneLayout',
      docExpansion: 'list',
      defaultModelsExpandDepth: -1,
      displayRequestDuration: true,
      filter: true,
      requestInterceptor: (req) => {
        // 标记请求经过代理
        return req
      }
    })
  } catch (e) {
    ElMessage.error('Swagger UI 加载失败: ' + (e?.message || e))
    console.error('[ApiDocs] swagger build failed', e)
  }
}

function reload() {
  buildSwagger()
}

onMounted(() => {
  buildSwagger()
})

onBeforeUnmount(() => {
  if (swaggerEl.value) swaggerEl.value.innerHTML = ''
  ui = null
})

watch(activeModule, () => buildSwagger())
</script>

<style scoped>
.api-docs-page {
  padding: 0;
}
.header-card {
  margin-bottom: 12px;
}
.header-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.header-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.module-tabs {
  margin-left: 8px;
}
.actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
  align-items: center;
}
.swagger-card {
  min-height: calc(100vh - 220px);
}
#swagger-ui {
  min-height: 600px;
}
</style>