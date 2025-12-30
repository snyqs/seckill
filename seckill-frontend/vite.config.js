import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 后端地址可通过 VITE_API_BASE 配置，默认 http://127.0.0.1:8080
  const target = env.VITE_API_BASE || 'http://127.0.0.1:8080'

  return {
    plugins: [vue()],
    server: {
      proxy: {
        '/api': {
          target,
          changeOrigin: true,
        },
      },
    },
  }
})
