import api from './api'

export const recognitionService = {
  async recognize(imageFile) {
    const formData = new FormData()
    formData.append('image', imageFile)
    
    const response = await api.post('/recognition', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  }
}

