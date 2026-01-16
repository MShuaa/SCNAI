import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(Cookies.get('token') || null)
  const user = ref(null)
  const isAuthenticated = computed(() => !!token.value)

  // 登录
  const login = async (loginData) => {
    try {
      // 这里将调用auth服务的登录方法
      // const response = await authService.login(loginData)

      // 暂时使用本地状态管理，实际调用将在service中处理
      // token.value = response.token
      // user.value = response.user
      // Cookies.set('token', response.token, { expires: 7 }) // 7天过期

      return { success: true }
    } catch (error) {
      console.error('登录失败:', error)
      return { success: false, error: error.message }
    }
  }

  // 登出
  const logout = () => {
    token.value = null
    user.value = null
    Cookies.remove('token')
  }

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
  }

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    // 为兼容路由守卫和其他代码，既写入 Cookie 也写入 localStorage
    Cookies.set('token', newToken, { expires: 7 }) // 7天过期
    try {
      localStorage.setItem('token', newToken)
    } catch (e) {
      // 在 SSR 或受限环境下 localStorage 可能不可用，忽略错误
      console.warn('无法写入 localStorage token:', e)
    }
  }

  // 一键设置认证信息（兼容调用 setAuth(token, user)）
  const setAuth = (newToken, userData) => {
    setToken(newToken)
    setUser(userData)
  }

  // 清除认证信息（兼容 clearAuth() 调用）
  const clearAuth = () => {
    token.value = null
    user.value = null
    Cookies.remove('token')
    try {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    } catch (e) {
      // ignore
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
    setUser,
    setToken,
    setAuth,
    clearAuth
  }
})