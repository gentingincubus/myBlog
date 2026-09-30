import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 根据当前运行模式（development / production）加载对应的 .env 文件
  const env = loadEnv(mode, process.cwd())

  const apiPrefix = env.VITE_API_BASE_URL || '/api'
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      proxy: {
        [apiPrefix]: {
          target: proxyTarget,
          changeOrigin: true,
          // 如果后端实际没有 /api 前缀，可开启 rewrite，目前后端保持 /api 前缀所以无需 rewrite
          // rewrite: (path) => path.replace(new RegExp(`^${apiPrefix}`), '')
        }
      }
    }
  }
})
