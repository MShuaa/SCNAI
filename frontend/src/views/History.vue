<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">识别历史</h2>
        <div class="top-nav-user">
          <div class="user-avatar"><i class="fas fa-user"></i></div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">识别历史记录</h3>
          <div class="card-icon"><i class="fas fa-history"></i></div>
        </div>

        <!-- 操作按钮 -->
        <div style="padding: 20px; border-bottom: 1px solid #2d3748;">
          <button class="btn btn-secondary" @click="exportData">
            <i class="fas fa-download"></i> 导出
          </button>
        </div>

        <!-- 历史记录表格 -->
        <div v-if="loading" style="text-align: center; padding: 40px;">
          <span>加载中...</span>
        </div>
        <div v-else-if="records.length === 0" style="text-align: center; padding: 40px;">
          <i class="fas fa-history" style="font-size: 48px; color: #a8b2d1; margin-bottom: 20px;"></i>
          <h3>暂无历史记录</h3>
          <p style="color: #a8b2d1;">使用识别功能后将显示在这里</p>
        </div>
        <div v-else class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>时间</th><th>植物</th><th>病虫害</th><th>严重程度</th><th>置信度</th><th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in sortedRecords" :key="record.id">
                <td>{{ formatDateTime(record.identify_time || record.created_at) }}</td>
                <td>{{ record.plant_name }}</td>
                <td>{{ record.disease_type_name }}</td>
                <td><span :class="getSeverityClass(record.severity)">{{ record.severity }}</span></td>
                <td>{{ formatConfidence(record.confidence) }}</td>
                <td><button class="btn btn-sm btn-secondary" @click="viewDetail(record.id)">详情</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { recordService } from '@/services/record'
import { ElMessage } from 'element-plus'

export default {
  name: 'History',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(true)
    const records = ref([])

    // 按时间排序的计算属性
    const sortedRecords = computed(() => {
      return records.value.sort((a, b) => new Date(b.identify_time || b.created_at) - new Date(a.identify_time || a.created_at))
    })

    const loadData = async () => {
      loading.value = true
      try {
        const response = await recordService.getRecords()
        if (response.success) {
          records.value = response.data.records || []
        } else {
          ElMessage.error(response.message || '获取识别历史失败')
          records.value = []
        }
      } catch (error) {
        console.error('获取识别历史失败:', error)
        ElMessage.error('获取识别历史失败，请稍后重试')
        records.value = []
      } finally {
        loading.value = false
      }
    }

    const formatDateTime = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatConfidence = (confidence) => {
      if (confidence === null || confidence === undefined) return '0.0%'
      const value = typeof confidence === 'string' ? parseFloat(confidence) : confidence
      return (value * 100).toFixed(1) + '%'
    }

    const getSeverityClass = (severity) => {
      if (severity === '重度') return 'badge badge-danger'
      if (severity === '中度') return 'badge badge-warning'
      return 'badge badge-success'
    }

    // 跳转到详情页面，传递记录ID
    const viewDetail = (recordId) => {
      router.push({ name: 'Visualization', params: { recordId } })
    }

    const exportData = () => ElMessage.info('导出功能开发中...')

    onMounted(loadData)

    return { user, loading, records, sortedRecords, loadData, formatDateTime, formatConfidence, getSeverityClass, viewDetail, exportData }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
</style>
