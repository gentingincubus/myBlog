import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 预留后续 Token 携带逻辑
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果返回的是二进制数据（比如导出文件、音视频流）直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res
    }
    return res
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络请求错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
