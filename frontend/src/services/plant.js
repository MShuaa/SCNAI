import api from './api'

/**
 * 植物信息相关API服务
 */
export const plantService = {
  /**
   * 获取所有植物列表
   */
  async getAllPlants() {
    try {
      const response = await api.get('/plants')
      return response.data
    } catch (error) {
      console.error('获取植物列表失败:', error)
      throw error
    }
  },

  /**
   * 根据ID获取植物详情
   * @param {number} id 植物ID
   */
  async getPlantById(id) {
    try {
      const response = await api.get(`/plants/${id}`)
      return response.data
    } catch (error) {
      console.error('获取植物详情失败:', error)
      throw error
    }
  },

  /**
   * 模糊搜索植物
   * @param {string} keyword 搜索关键词
   */
  async searchPlants(keyword) {
    try {
      const response = await api.get('/plants/search/keyword', {
        params: { keyword }
      })
      return response.data
    } catch (error) {
      console.error('搜索植物失败:', error)
      throw error
    }
  },

  /**
   * 根据名称精确查询植物
   * @param {string} name 植物名称
   */
  async searchPlantByName(name) {
    try {
      const response = await api.get('/plants/search', {
        params: { name }
      })
      return response.data
    } catch (error) {
      console.error('按名称查询植物失败:', error)
      throw error
    }
  }
}

export default plantService
