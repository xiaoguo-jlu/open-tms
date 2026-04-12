import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api/v1/business-units': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/traders': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/currencies': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/countries': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/banks': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/counterparties': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/counterparty-accounts': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/holidays': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/deals': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/bank-accounts': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/v1/instruments': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/v1/fund-plans': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/v1/cash-pools': {
        target: 'http://localhost:8086',
        changeOrigin: true
      },
      '/api/v1/settlements': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/api/v1/limits': {
        target: 'http://localhost:8088',
        changeOrigin: true
      },
      '/api/v1/fx-deals': {
        target: 'http://localhost:8089',
        changeOrigin: true
      },
      '/api/v1/irs-deals': {
        target: 'http://localhost:8090',
        changeOrigin: true
      },
      '/api/v1/valuations': {
        target: 'http://localhost:8091',
        changeOrigin: true
      },
      '/api/v1/exposures': {
        target: 'http://localhost:8092',
        changeOrigin: true
      },
      '/api/v1/hedge-relations': {
        target: 'http://localhost:8093',
        changeOrigin: true
      },
      '/api/v1/impairments': {
        target: 'http://localhost:8094',
        changeOrigin: true
      },
      '/api/v1/var-reports': {
        target: 'http://localhost:8095',
        changeOrigin: true
      },
      '/api/v1/cockpit': {
        target: 'http://localhost:8096',
        changeOrigin: true
      },
      '/api/v1/reports': {
        target: 'http://localhost:8097',
        changeOrigin: true
      }
    }
  }
})