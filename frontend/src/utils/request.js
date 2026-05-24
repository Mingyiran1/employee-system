import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  // 使用代理（推荐，适合开发环境）
  baseURL: '/api',
  // 或者直接连接后端（如果代理不工作）
  // baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是Blob类型响应（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 1) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg))
    }
    return res
  },
  error => {
    const { response } = error
    if (response) {
      if (response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        router.push('/login')
      } else if (response.status === 403) {
        ElMessage.error('没有权限访问该资源')
      } else {
        ElMessage.error(response.data?.msg || '请求失败')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
