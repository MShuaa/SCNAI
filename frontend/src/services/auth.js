import api from './api'

// 开发专用mock登录服务 - 仅在VITE_MOCK_AUTH=true时启用
const mockAuthService = {
  async login(username, password, remember) {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 简单的mock验证：用户名demo，密码demo
    if (username === 'demo' && password === 'demo') {
      const mockUser = {
        id: 1,
        username: 'demo',
        nickname: '演示用户',
        role: 'user',
        avatar: null,
        created_at: new Date().toISOString()
      }
      const mockToken = 'mock-jwt-token-' + Date.now()

      return {
        // 模拟后端返回格式：{ success: true, data: { token, user } }
        success: true,
        data: {
          token: mockToken,
          user: mockUser
        },
        message: '登录成功'
      }
    } else {
      throw new Error('用户名或密码错误')
    }
  },

  async logout() {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 300))

    return {
      success: true,
      data: {},
      message: '登出成功'
    }
  },

  async verify() {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 300))

    // 检查是否有token（实际项目中会验证token有效性）
    const mockUser = {
      id: 1,
      username: 'demo',
      nickname: '演示用户',
      role: 'user',
      avatar: null,
      created_at: new Date().toISOString()
    }

    return {
      success: true,
      data: {
        user: mockUser
      },
      message: '验证成功'
    }
  }
}

// 根据环境变量决定使用真实API还是mock
// 只有明确设置 VITE_MOCK_AUTH=true 时才使用mock
const isMockAuth = import.meta.env.VITE_MOCK_AUTH === 'true'

export const authService = isMockAuth ? mockAuthService : {
  async login(username, password, remember) {
    try {
      const response = await api.post('/auth/login', {
        username,
        password,
        remember
      })
      return response.data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  },

  async logout() {
    try {
      const response = await api.post('/auth/logout')
      return response.data
    } catch (error) {
      console.error('登出失败:', error)
      throw error
    }
  },

  async verify() {
    try {
      const response = await api.get('/auth/verify')
      return response.data
    } catch (error) {
      console.error('验证失败:', error)
      throw error
    }
  }
}

// 导出mock状态，便于调试
export const isUsingMockAuth = isMockAuth

