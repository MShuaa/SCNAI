<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <div style="display: flex; align-items: center; gap: 15px;">
          <button class="btn btn-secondary btn-sm" @click="$router.go(-1)">
            <i class="fas fa-arrow-left"></i> 返回
          </button>
          <h2 class="top-nav-title">植物详情</h2>
        </div>
        <div class="top-nav-user">
          <div class="user-avatar"><i class="fas fa-user"></i></div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="card" style="text-align: center; padding: 60px;">
        <i class="fas fa-spinner fa-spin" style="font-size: 32px; color: #64ffda; margin-bottom: 20px;"></i>
        <h3>加载中...</h3>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="card" style="text-align: center; padding: 60px;">
        <i class="fas fa-exclamation-triangle" style="font-size: 48px; color: #f56565; margin-bottom: 20px;"></i>
        <h3>加载失败</h3>
        <p style="color: #a8b2d1; margin-bottom: 20px;">{{ error }}</p>
        <button class="btn btn-primary" @click="loadPlantDetail">重试</button>
      </div>

      <!-- 植物详情内容 -->
      <div v-else-if="plant" class="plant-detail">
        <!-- 头部信息 -->
        <div class="card">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 15px;">
              <div style="width: 60px; height: 60px; border-radius: 12px; background: linear-gradient(135deg, #1e3a5f 0%, #2d3748 100%); display: flex; align-items: center; justify-content: center; color: #64ffda; font-size: 30px;">
                <i class="fas fa-leaf"></i>
              </div>
              <div>
                <h2 style="margin: 0; color: #fff; font-size: 24px;">{{ plant.name }}</h2>
                <p style="margin: 5px 0 0 0; color: #64ffda; font-style: italic;" v-if="plant.scientificName">
                  {{ plant.scientificName }}
                </p>
                <div style="margin-top: 5px; font-size: 14px; color: #a8b2d1;">
                  <span v-if="plant.family">{{ plant.family }}</span>
                  <span v-if="plant.family && plant.genus"> · </span>
                  <span v-if="plant.genus">{{ plant.genus }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 详细信息卡片 -->
        <div class="detail-grid">
          <!-- 基本信息 -->
          <div class="card">
            <div class="card-header">
              <h3 class="card-title">基本信息</h3>
              <div class="card-icon"><i class="fas fa-info-circle"></i></div>
            </div>
            <div class="card-content">
              <div v-if="plant.description" class="info-section">
                <h4>描述</h4>
                <p>{{ plant.description }}</p>
              </div>
              <div v-if="plant.growthHabit" class="info-section">
                <h4>生长习性</h4>
                <p>{{ plant.growthHabit }}</p>
              </div>
              <div v-if="plant.plantingMethod" class="info-section">
                <h4>种植方法</h4>
                <p>{{ plant.plantingMethod }}</p>
              </div>
            </div>
          </div>

          <!-- 病害防治 -->
          <div class="card">
            <div class="card-header">
              <h3 class="card-title">病害防治</h3>
              <div class="card-icon"><i class="fas fa-shield-alt"></i></div>
            </div>
            <div class="card-content">
              <div v-if="plant.commonDiseases" class="info-section">
                <h4>常见病害</h4>
                <div v-if="parsedDiseases && parsedDiseases.length > 0">
                  <div v-for="disease in parsedDiseases" :key="disease.name" class="disease-item">
                    <span class="disease-name">{{ disease.name }}</span>
                    <span class="disease-code">{{ disease.code }}</span>
                  </div>
                </div>
                <div v-else>
                  <p>{{ plant.commonDiseases }}</p>
                </div>
              </div>
              <div v-if="plant.preventionTips" class="info-section">
                <h4>预防措施</h4>
                <p>{{ plant.preventionTips }}</p>
              </div>
            </div>
          </div>

          <!-- 其他信息 -->
          <div class="card">
            <div class="card-header">
              <h3 class="card-title">其他信息</h3>
              <div class="card-icon"><i class="fas fa-calendar-alt"></i></div>
            </div>
            <div class="card-content">
              <div class="info-row">
                <span class="info-label">创建时间:</span>
                <span class="info-value">{{ formatDate(plant.createdAt) }}</span>
              </div>
              <div class="info-row" v-if="plant.updatedAt && plant.updatedAt !== plant.createdAt">
                <span class="info-label">更新时间:</span>
                <span class="info-value">{{ formatDate(plant.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { plantService } from '@/services/plant'
import { ElMessage } from 'element-plus'

export default {
  name: 'PlantDetail',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const route = useRoute()
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(true)
    const error = ref('')
    const plant = ref(null)

    const parsedDiseases = computed(() => {
      if (!plant.value?.commonDiseases) return null
      try {
        return JSON.parse(plant.value.commonDiseases)
      } catch {
        return null
      }
    })

    const loadPlantDetail = async () => {
      const plantId = route.params.id
      if (!plantId) {
        error.value = '无效的植物ID'
        loading.value = false
        return
      }

      try {
        loading.value = true
        error.value = ''
        const response = await plantService.getPlantById(plantId)
        if (response.success) {
          plant.value = response.data
        } else {
          error.value = response.error || '获取植物详情失败'
          ElMessage.error(error.value)
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('获取植物详情失败:', err)
        ElMessage.error('获取植物详情失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return '未知'
      try {
        return new Date(dateString).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch {
        return dateString
      }
    }

    onMounted(loadPlantDetail)

    return {
      user,
      loading,
      error,
      plant,
      parsedDiseases,
      loadPlantDetail,
      formatDate
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';

.plant-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.card-content {
  padding: 20px;
}

.info-section {
  margin-bottom: 20px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-section h4 {
  margin: 0 0 10px 0;
  color: #64ffda;
  font-size: 16px;
  font-weight: 600;
}

.info-section p {
  margin: 0;
  color: #a8b2d1;
  line-height: 1.6;
  white-space: pre-line;
}

.disease-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  background-color: rgba(100, 255, 218, 0.1);
  border-radius: 6px;
  border-left: 3px solid #64ffda;
}

.disease-name {
  color: #fff;
  font-weight: 500;
}

.disease-code {
  color: #64ffda;
  font-size: 12px;
  background-color: rgba(100, 255, 218, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #2d3748;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #a8b2d1;
  font-weight: 500;
}

.info-value {
  color: #fff;
  text-align: right;
}
</style>
