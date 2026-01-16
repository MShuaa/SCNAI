import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 使用函数式配置并通过 loadEnv 读取 env，避免在 CJS 环境下直接访问 import.meta.env 导致 undefined
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const useProxy = env.VITE_USE_PROXY === 'true'

  return {
    plugins: [vue()],
    server: {
      host: true,
      port: 3000,
      proxy: useProxy ? {
        '/api': {
          target: 'http://localhost:5000',
          changeOrigin: true
        }
      } : {}
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})

