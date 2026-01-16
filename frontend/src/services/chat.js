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
      let fullContent = ''

      while (true) {
        const { done, value } = await reader.read()

        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // 处理每一行数据 (SSE格式)
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.trim() === '') continue

          // Spring Boot SSE格式: data:{"type":"...","content":"..."}
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data) {
              try {
                const parsed = JSON.parse(data)

                if (parsed.type === 'start') {
                  // 开始事件,重置内容
                  fullContent = ''
                } else if (parsed.type === 'chunk' && onChunk) {
                  // 内容块事件
                  fullContent += parsed.content
                  onChunk(parsed.content)
                } else if (parsed.type === 'complete' && onComplete) {
                  // 完成事件
                  onComplete(parsed.content || fullContent)
                } else if (parsed.type === 'error' && onError) {
                  // 错误事件
                  onError(parsed.error)
                }
              } catch (e) {
                console.error('解析流式数据错误:', e, '原始数据:', data)
              }
            }
          }
        }
      }
    } catch (error) {
      console.error('sendMessageStream错误:', error)
      if (onError) {
        onError(error.message)
      }
      throw error
    }
  }
}
