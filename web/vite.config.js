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
      '/api/v1/basedata': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/dealing': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/dealing/bank-account': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/v1/dealing/instrument': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/v1/fundplan': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/v1/cashpool': {
        target: 'http://localhost:8086',
        changeOrigin: true
      },
      '/api/v1/settlement': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/api/v1/limit': {
        target: 'http://localhost:8088',
        changeOrigin: true
      },
      '/api/v1/fx': {
        target: 'http://localhost:8089',
        changeOrigin: true
      },
      '/api/v1/irs': {
        target: 'http://localhost:8090',
        changeOrigin: true
      },
      '/api/v1/valuation': {
        target: 'http://localhost:8091',
        changeOrigin: true
      },
      '/api/v1/exposure': {
        target: 'http://localhost:8092',
        changeOrigin: true
      },
      '/api/v1/hedge': {
        target: 'http://localhost:8093',
        changeOrigin: true
      },
      '/api/v1/impairment': {
        target: 'http://localhost:8094',
        changeOrigin: true
      },
      '/api/v1/var': {
        target: 'http://localhost:8095',
        changeOrigin: true
      },
      '/api/v1/cockpit': {
        target: 'http://localhost:8096',
        changeOrigin: true
      },
      '/api/v1/report': {
        target: 'http://localhost:8097',
        changeOrigin: true
      }
    }
  }
})