import api from './api'

export const dashboardService = {
  async getStats() {
    const response = await api.get('/dashboard/stats')
    return response.data
  },

  async getActivities() {
    const response = await api.get('/dashboard/activities')
    return response.data
  },

  async getRecords() {
    const response = await api.get('/dashboard/records')
    return response.data
  }
}

