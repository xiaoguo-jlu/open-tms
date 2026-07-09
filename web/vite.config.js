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
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/management-entities': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/subsidiaries': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/currency-pairs': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/traders': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/currencies': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/countries': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/banks': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/counterparties': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/counterparty-accounts': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/holidays': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/dealing': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/bank-accounts': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/default-bank-account-rules': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/instruments': {
        target: 'http://localhost:8081/opentms/basedata',
        changeOrigin: true
      },
      '/api/v1/fund-plans': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/v1/settlements': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/api/v1/valuations': {
        target: 'http://localhost:8091',
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