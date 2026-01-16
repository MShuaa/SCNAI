<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">识别详情</h2>
        <div class="top-nav-user">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
          </div>
        </div>
      </div>

      <!-- 返回按钮 -->
      <div class="card" style="margin-bottom: 20px;">
        <div class="card-body">
          <button class="btn btn-secondary" @click="goBack">
            <i class="fas fa-arrow-left"></i> 返回识别历史
          </button>
        </div>
      </div>

      <!-- 识别详情 -->
      <div class="card" v-if="record">
        <div class="card-header">
          <h3 class="card-title">识别记录详情</h3>
          <div class="card-icon"><i class="fas fa-file-alt"></i></div>
        </div>
        <div class="card-body">
          <div class="detail-info">
            <!-- 基础信息 -->
            <div class="info-group">
              <h4>基础信息</h4>
              <div class="info-grid">
                <div><strong>ID:</strong> {{ record.id }}</div>
                <div><strong>植物:</strong> {{ record.plantName || record.plant_name || '丝瓜' }}</div>
                <div><strong>病虫害:</strong> {{ record.diseaseTypeName || record.disease_type_name || '-' }}</div>
                <div><strong>置信度:</strong> {{ formatConfidence(record.confidence) }}</div>
                <div><strong>严重程度:</strong> <span :class="getSeverityClass(record.severity)">{{ record.severity }}</span></div>
              </div>
            </div>

            <!-- 位置时间 -->
            <div class="info-group">
              <h4>位置 & 时间</h4>
              <div class="info-grid">
                <div v-if="(record.locationLat || record.location_lat) && (record.locationLng || record.location_lng)">
                  <strong>经纬度:</strong> {{ (record.locationLat || record.location_lat).toFixed(4) }}, {{ (record.locationLng || record.location_lng).toFixed(4) }}
                </div>
                <div><strong>识别时间:</strong> {{ formatDateTime(record.identifyTime || record.identify_time) }}</div>
                <div><strong>创建时间:</strong> {{ formatDateTime(record.createdAt || record.created_at) }}</div>
              </div>
            </div>

            <!-- 症状处理 -->
            <div class="info-group" v-if="record.symptoms || (record.treatmentPlan || record.treatment_plan)">
              <h4>诊断信息</h4>
              <div v-if="record.symptoms" class="text-block">
                <strong>症状:</strong> {{ record.symptoms }}
              </div>
              <div v-if="record.treatmentPlan || record.treatment_plan" class="text-block">
                <strong>处理方案:</strong> {{ record.treatmentPlan || record.treatment_plan }}
              </div>
            </div>

            <!-- 图片 -->
            <div class="info-group" v-if="record.imageUrl || record.image_url">
              <h4>识别图片</h4>
              <img :src="record.imageUrl || record.image_url" :alt="record.diseaseTypeName || record.disease_type_name" class="detail-image" />
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-else-if="loading" class="card">
        <div class="card-body" style="text-align: center; padding: 40px;">
          <span>加载中...</span>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="card">
        <div class="card-body" style="text-align: center; padding: 40px;">
          <i class="fas fa-exclamation-triangle" style="font-size: 48px; color: #f56565; margin-bottom: 20px;"></i>
          <h3>记录不存在</h3>
          <p style="color: #a8b2d1;">无法找到指定的识别记录</p>
          <button class="btn btn-primary" @click="goBack" style="margin-top: 20px;">
            返回识别历史
          </button>
        </div>
      </div>

    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { recordService } from '@/services/record'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'Visualization',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(true)
    const record = ref(null)

    const loadRecord = async () => {
      loading.value = true
      const recordId = parseInt(route.params.recordId)

      if (!recordId) {
        loading.value = false
        return
      }

      try {
        const response = await recordService.getRecordById(recordId)
        if (response.success) {
          record.value = response.data
        } else {
          record.value = null
        }
      } catch (error) {
        console.error('获取识别记录详情失败:', error)
        record.value = null
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
        minute: '2-digit',
        second: '2-digit'
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


    const goBack = () => {
      router.push({ name: 'History' })
    }

    onMounted(loadRecord)

    return {
      user,
      loading,
      record,
      formatDateTime,
      formatConfidence,
      getSeverityClass,
      goBack
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.info-group {
  padding: 20px;
  border: 1px solid #2d3748;
  border-radius: 8px;
  background: rgba(26, 32, 44, 0.5);
}

.info-group h4 {
  margin: 0 0 15px 0;
  color: #64ffda;
  font-size: 16px;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.info-grid > div {
  color: #e2e8f0;
  font-size: 14px;
}

.info-grid strong {
  color: #a8b2d1;
  margin-right: 8px;
}

.text-block {
  margin-top: 10px;
  padding: 12px;
  background: rgba(45, 55, 72, 0.5);
  border-radius: 6px;
  border-left: 3px solid #64ffda;
  color: #e2e8f0;
  line-height: 1.6;
}

.detail-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 复用common.css中的badge样式 */

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

