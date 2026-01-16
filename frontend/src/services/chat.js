import api from './api'

export const chatService = {
  async sendMessage(message) {
    const response = await api.post('/chat', { message })
    return response.data
  },

  async sendMessageStream(message, onChunk, onComplete, onError) {
    try {
      const token = localStorage.getItem('token')
      const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }

      const response = await fetch('/api/chat/stream', {
        method: 'POST',
        headers,
        body: JSON.stringify({ message })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) break
        
        buffer += decoder.decode(value, { stream: true })
        
        // 处理每一行数据
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data.trim()) {
              try {
                const parsed = JSON.parse(data)
                
                if (parsed.type === 'chunk' && onChunk) {
                  onChunk(parsed.content)
                } else if (parsed.type === 'complete' && onComplete) {
                  onComplete(parsed.content)
                } else if (parsed.type === 'error' && onError) {
                  onError(parsed.error)
                }
              } catch (e) {
                console.error('解析流式数据错误:', e)
              }
            }
          }
        }
      }
    } catch (error) {
      if (onError) {
        onError(error.message)
      }
      throw error
    }
  }
}

