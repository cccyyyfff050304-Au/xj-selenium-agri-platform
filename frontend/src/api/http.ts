import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('xj-agri-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code !== 'undefined') {
      if (body.code !== 0) {
        ElMessage.error(body.message || '接口请求失败')
        return Promise.reject(new Error(body.message || '接口请求失败'))
      }
      return body.data
    }
    return body
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('xj-agri-token')
      router.replace('/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络请求异常')
    return Promise.reject(error)
  }
)

export default http
