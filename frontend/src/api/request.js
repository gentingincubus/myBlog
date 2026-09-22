import axios from 'axios'
import { ElMessage } from 'element-plus'
import { TOKEN_KEY, useUserStore } from '@/stores/user'

// 创建 Axios 实例，自动读取当前环境的 baseURL
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 携带专属的前缀 Token 进行鉴权
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

function handleUnauthorized(message) {
  const userStore = useUserStore()
  userStore.clearToken()
  ElMessage.error(message || '未登录或登录已过期，请重新登录')
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.href = `/login?redirect=${redirect}`
  }
}

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果返回的是二进制流（比如文件导出、图片流等），直接返回原数据
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res
    }

    // 业务状态码为 401 处理
    if (res && res.code === 401) {
      handleUnauthorized(res.message)
      return Promise.reject(new Error(res.message || '未授权'))
    }

    // 业务失败统一拦截：当后端返回 status: false 时，自动弹窗提示后端错误信息并 reject
    if (res && res.status === false) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }

    return res
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络请求错误'

    // HTTP 状态码为 401 处理
    if (status === 401) {
      handleUnauthorized(msg)
    } else {
      ElMessage.error(msg)
    }

    return Promise.reject(error)
  }
)

export default request
