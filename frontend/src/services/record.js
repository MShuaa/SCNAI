import api from './api'

export const recordService = {
  /**
   * 获取识别记录列表（分页）
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码（从1开始）
   * @param {number} params.pageSize - 每页数量
   * @returns {Promise} 返回分页数据
   */
  async getRecords(params = { page: 1, pageSize: 10 }) {
    const response = await api.get('/records', { params })
    return response.data
  },

  /**
   * 根据ID获取识别记录详情
   * @param {number} id - 记录ID
   * @returns {Promise} 返回记录详情
   */
  async getRecordById(id) {
    const response = await api.get(`/records/${id}`)
    return response.data
  },

  /**
   * 获取识别记录统计数据
   * @returns {Promise} 返回统计数据
   */
  async getStats() {
    const response = await api.get('/records/stats')
    return response.data
  }
}
