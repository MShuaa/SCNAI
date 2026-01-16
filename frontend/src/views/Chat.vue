<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">智能问答</h2>
        <div class="top-nav-user">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
            <div class="user-role">{{ user?.role || '系统管理员' }}</div>
          </div>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <p v-if="messages.length === 0" class="messages-start">Hi，今天有什么可以帮到你吗？</p>
        <div v-for="(message, index) in messages" :key="index" class="message" :class="message.type">
          <div class="message-content">
            <div v-if="!message.isStreaming" v-html="formatMessageContent(message.content)"></div>
            <div v-else>{{ message.content }}<span class="streaming-cursor">|</span></div>
            <button v-if="!message.isStreaming" class="copy-btn" @click="copyMessage(message.content, index)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                <path d="M5 15H4C3.46957 15 2.96086 14.7893 2.58579 14.4142C2.21071 14.0391 2 13.5304 2 13V4C2 3.46957 2.21071 2.96086 2.58579 2.58579C2.96086 2.21071 3.46957 2 4 2H13C13.5304 2 14.0391 2.21071 14.4142 2.58579C14.7893 2.96086 15 3.46957 15 4V5" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span class="copy-tooltip" :class="{ 'show': message.showTooltip }">
                {{ message.tooltipText || '复制' }}
              </span>
            </button>
          </div>
          <div class="message-time">{{ message.time }}</div>
        </div>
        <div v-if="typing" class="typing-indicator">
          <span>AI正在思考</span>
          <div class="typing-dots">
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
          </div>
        </div>
      </div>

      <div class="input-container">
        <div class="input-area">
          <textarea 
            class="text-input" 
            v-model="inputMessage" 
            @keydown="handleKeyDown"
            placeholder="请输入您的问题..."
            rows="1"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || sending">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </div>
        <div class="input-actions">
          <div v-if="typing" class="typing-indicator">
            <span>AI正在思考</span>
            <div class="typing-dots">
              <div class="typing-dot"></div>
              <div class="typing-dot"></div>
              <div class="typing-dot"></div>
            </div>
          </div>
          <div>按Enter发送，Shift+Enter换行</div>
        </div>
        <div class="suggestions">
          <div v-for="(suggestion, index) in suggestions" :key="index" class="suggestion" @click="selectSuggestion(suggestion)">
            {{ suggestion }}
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed, nextTick } from 'vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { chatService } from '@/services/chat'

export default {
  name: 'Chat',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)
    const messages = ref([])
    const inputMessage = ref('')
    const sending = ref(false)
    const typing = ref(false)
    const messagesContainer = ref(null)

    const suggestions = ref([
      '什么是人工智能？',
      '如何训练神经网络？',
      '解释一下机器学习',
      '计算机视觉的应用'
    ])

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const addMessage = (content, type = 'sent') => {
      const now = new Date()
      messages.value.push({
        content,
        type,
        time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      })
      scrollToBottom()
    }

    const sendMessage = async () => {
      const message = inputMessage.value.trim()
      if (!message || sending.value) return

      addMessage(message, 'sent')
      inputMessage.value = ''
      sending.value = true
      typing.value = true

      // 创建流式回复消息
      const streamMessageIndex = messages.value.length
      messages.value.push({
        content: '',
        type: 'received',
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        isStreaming: true
      })
      scrollToBottom()

      try {
        await chatService.sendMessageStream(
          message,
          // onChunk回调 - 处理每个文本块
          (chunk) => {
            if (messages.value[streamMessageIndex]) {
              messages.value[streamMessageIndex].content += chunk
              scrollToBottom()
            }
          },
          // onComplete回调 - 处理完成
          (finalContent) => {
            if (messages.value[streamMessageIndex]) {
              messages.value[streamMessageIndex].content = finalContent
              messages.value[streamMessageIndex].isStreaming = false
              typing.value = false
              scrollToBottom()
            }
          },
          // onError回调 - 处理错误
          (error) => {
            console.error('流式消息错误:', error)
            if (messages.value[streamMessageIndex]) {
              messages.value[streamMessageIndex].content = '抱歉，我无法回答这个问题。'
              messages.value[streamMessageIndex].isStreaming = false
              typing.value = false
              scrollToBottom()
            }
          }
        )
      } catch (error) {
        console.error('发送消息错误:', error)
        if (messages.value[streamMessageIndex]) {
          messages.value[streamMessageIndex].content = '发送失败，请稍后重试。'
          messages.value[streamMessageIndex].isStreaming = false
          typing.value = false
          scrollToBottom()
        }
      } finally {
        sending.value = false
      }
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        sendMessage()
      }
    }

    const selectSuggestion = (suggestion) => {
      inputMessage.value = suggestion
    }

    const formatMessageContent = (content) => {
      // 处理分点格式，将"1. " "2. "等转换为HTML列表
      let formattedContent = content
      
      // 检查是否包含分点格式
      if (/^\d+\.\s/.test(content) || /\n\d+\.\s/.test(content)) {
        // 将内容转换为有序列表
        const items = []
        let remainingContent = content
        
        // 查找所有的分点项目（包括缩进的分点）
        const listRegex = /\n*\s*(\d+)\.\s+([\s\S]*?)(?=\n\s*\d+\.\s|$)/g
        let match
        
        while ((match = listRegex.exec(remainingContent)) !== null) {
          let itemContent = match[2].trim()
          
          // 保留原始的数字标号
          itemContent = `${match[1]}. ${itemContent}`
          
          // 检查项目是否包含小标题格式
          if (/^\d+\.\s*[一二三四五六七八九十百千]+、/.test(itemContent) || 
              /^\d+\.\s*\d+、/.test(itemContent) || 
              /^\d+\.\s*\([一二三四五六七八九十百千1234567890]+\)/.test(itemContent) ||
              /^\d+\.\s*\d+\)/.test(itemContent) ||
              /^\d+\.\s*[标题|小标题].*?[：:]/i.test(itemContent)) {
            itemContent = `<span class="content-heading">${itemContent}</span>`
          }
          
          items.push(itemContent)
        }
        
        // 只有当有多个项目时才创建列表
          if (items.length > 0) {
            let listHtml = '<ul class="message-list">'
            items.forEach(item => {
              if (item.trim()) {
                listHtml += `<li class="message-list-item">${item.trim()}</li>`
              }
            })
            listHtml += '</ul>'
            formattedContent = listHtml
          } else {
            // 如果没有提取到项目，回退到原始内容
            formattedContent = `<p>${content}</p>`
          }
      } else {
        // 对于非分点内容，处理其中可能的小标题和段落
        let paragraphs = content.split('\n')
        formattedContent = ''
        
        paragraphs.forEach(paragraph => {
          if (!paragraph.trim()) return
          
          // 同样的小标题识别规则
          let paraContent = paragraph.trim()
          if (/^[一二三四五六七八九十百千]+、/.test(paraContent) || 
              /^\d+、/.test(paraContent) || 
              /^\([一二三四五六七八九十百千1234567890]+\)/.test(paraContent) ||
              /^\d+\)/.test(paraContent) ||
              /^[标题|小标题].*?[：:]/i.test(paraContent)) {
            formattedContent += `<p class="content-heading">${paraContent}</p>`
          } else {
            formattedContent += `<p>${paraContent}</p>`
          }
        })
      }
      
      return formattedContent
    }
    
    const copyMessage = async (content, index) => {
      try {
        await navigator.clipboard.writeText(content)
        
        // 显示复制成功提示
        messages.value[index].showTooltip = true
        messages.value[index].tooltipText = '已复制!'
        
        // 3秒后隐藏提示
        setTimeout(() => {
          if (messages.value[index]) {
            messages.value[index].showTooltip = false
            messages.value[index].tooltipText = '复制'
          }
        }, 3000)
      } catch (err) {
        console.error('复制失败:', err)
      }
    }

    return {
      user,
      messages,
      inputMessage,
      sending,
      typing,
      messagesContainer,
      suggestions,
      sendMessage,
      handleKeyDown,
      selectSuggestion,
      formatMessageContent,
      copyMessage
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/chat.css';
</style>

