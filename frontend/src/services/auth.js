import api from './api'

export const authService = {
  async login(username, password, remember) {
    const response = await api.post('/auth/login', {
      username,
      password,
      remember
    })
    return response.data
  },

  async logout() {
    const response = await api.post('/auth/logout')
    return response.data
  },

  async verify() {
    const response = await api.get('/auth/verify')
    return response.data
  }
}

