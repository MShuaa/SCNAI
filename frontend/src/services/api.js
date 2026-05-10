import axios from 'axios'

// 根据环境变量决定baseURL
// 开发环境使用代理: /api -> http://localhost:5000/api
// 生产环境或局域网访问: 直接使用完整URL
const getBaseURL = () => {
  // 如果设置了API_URL环境变量,直接使用
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL
  }
  // 否则使用相对路径(通过Vite代理)
  return '/api'
}

const api = axios.create({
  baseURL: getBaseURL(),
  timeout: 60000, // 从10秒增加到60秒，适应模型生成时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response?.status === 401) {
      // Token过期或无效，清除认证信息并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

