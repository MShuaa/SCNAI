import api from './api'

export const visualizationService = {
  async getData(params = {}) {
    const response = await api.get('/visualization/data', { params })
    return response.data
  }
}

