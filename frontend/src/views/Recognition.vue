<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">图像识别</h2>
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

      <div class="recognition-area">
        <section class="upload-section">
          <h2 class="section-title">
            <i class="fas fa-cloud-arrow-up"></i>
            上传图像
          </h2>

          <div class="upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
            <div class="upload-icon">
              <i class="fas fa-image"></i>
            </div>
            <p class="upload-text">点击或拖拽图像到此处</p>
            <p class="upload-hint">支持 JPG、PNG 格式，最大 3MB 的图像文件</p>
            <input type="file" ref="fileInput" @change="handleFileSelect" accept="image/jpeg, image/png" style="display: none;">
          </div>

          <div v-if="previewImage" class="preview-container">
            <img class="preview-image" :src="previewImage" alt="预览图像">
          </div>

          <div v-if="previewImage" class="controls">
            <button class="btn btn-secondary" @click="cancelUpload">
              <i class="fas fa-times"></i>
              取消
            </button>
            <button class="btn btn-primary" @click="startRecognition" :disabled="recognizing">
              <i v-if="recognizing" class="fas fa-spinner fa-spin"></i>
              <i v-else class="fas fa-search"></i>
              {{ recognizing ? '识别中...' : '开始识别' }}
            </button>
          </div>
        </section>

        <section class="result-section">
          <h2 class="section-title">
            <i class="fas fa-chart-bar"></i>
            识别结果
          </h2>

          <div class="status-area">
            <h3 class="status-title">识别状态</h3>
            <div class="status-content">
              <p v-if="!result && !recognizing" class="status-default">等待图像上传...</p>
              <p v-else-if="recognizing" class="status-processing">正在识别中...</p>
              <p v-else-if="result" class="status-success">识别完成</p>
            </div>
          </div>

          <div v-if="result" class="result-area">
            <div class="result-card">
              <div class="result-header">
                <h3 class="result-title">病虫害识别结果</h3>
                <span class="result-badge badge-success">已完成</span>
              </div>
              <div class="result-details">
                <div class="detail-item">
                  <span class="detail-label">病害类型</span>
                  <span class="detail-value">{{ result.diseaseType }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">置信度</span>
                  <div>
                    <span class="detail-value">{{ (result.confidence * 100).toFixed(1) }}%</span>
                    <div class="progress-container">
                      <div class="progress-bar" :style="{ width: (result.confidence * 100) + '%' }"></div>
                    </div>
                  </div>
                </div>
                <div class="detail-item">
                  <span class="detail-label">严重程度</span>
                  <div class="severity-indicator">
                    <div class="severity-dot" :class="getSeverityClass(result.severity)"></div>
                    <span class="detail-value">{{ result.severity }}</span>
                  </div>
                </div>
                <div class="detail-item">
                  <span class="detail-label">识别时间</span>
                  <span class="detail-value">{{ result.identifyTime }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="result" class="action-buttons">
            <button class="btn btn-secondary" @click="reIdentify">
              <i class="fas fa-redo"></i>
              重新识别
            </button>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { recognitionService } from '@/services/recognition'

export default {
  name: 'Recognition',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)
    const fileInput = ref(null)
    const previewImage = ref('')
    const selectedFile = ref(null)
    const recognizing = ref(false)
    const result = ref(null)

    const triggerFileInput = () => {
      fileInput.value?.click()
    }

    const handleFileSelect = (event) => {
      const file = event.target.files[0]
      if (file) {
        handleFile(file)
      }
    }

    const handleDrop = (event) => {
      const file = event.dataTransfer.files[0]
      if (file && file.type.startsWith('image/')) {
        handleFile(file)
      }
    }

    const handleFile = (file) => {
      if (file.size > 3 * 1024 * 1024) {
        alert('文件大小不能超过3MB')
        return
      }
      selectedFile.value = file
      const reader = new FileReader()
      reader.onload = (e) => {
        previewImage.value = e.target.result
      }
      reader.readAsDataURL(file)
    }

    const cancelUpload = () => {
      previewImage.value = ''
      selectedFile.value = null
      result.value = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    }

    const startRecognition = async () => {
      if (!selectedFile.value) return

      recognizing.value = true
      result.value = null

      try {
        const response = await recognitionService.recognize(selectedFile.value)
        if (response.success) {
          result.value = response.data
        } else {
          alert('识别失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('识别错误:', error)
        alert('识别失败，请稍后重试')
      } finally {
        recognizing.value = false
      }
    }

    const reIdentify = () => {
      cancelUpload()
    }

    const getSeverityClass = (severity) => {
      if (severity === '重度') return 'severity-high'
      if (severity === '中度') return 'severity-medium'
      return 'severity-low'
    }

    return {
      user,
      fileInput,
      previewImage,
      recognizing,
      result,
      triggerFileInput,
      handleFileSelect,
      handleDrop,
      cancelUpload,
      startRecognition,
      reIdentify,
      getSeverityClass
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/recognition.css';
</style>

