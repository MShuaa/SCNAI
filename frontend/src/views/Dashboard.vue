<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">主控制台</h2>
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

      <!-- 无人机状态 -->
      <div class="card drone-status">
        <img src="@/assets/images/icons/shigua.jpeg" alt="无人机" class="drone-image">
        <div class="drone-info">
          <h3 class="drone-name">农业监测无人机 #001</h3>
          <div class="drone-stats">
            <div class="drone-stat-item">
              <div class="drone-stat-value">{{ stats?.drone?.status || '正常' }}</div>
              <div class="drone-stat-label">状态</div>
            </div>
            <div class="drone-stat-item">
              <div class="drone-stat-value">{{ stats?.drone?.battery || 85 }}%</div>
              <div class="drone-stat-label">电量</div>
            </div>
            <div class="drone-stat-item">
              <div class="drone-stat-value">{{ stats?.drone?.range || 12.5 }}km</div>
              <div class="drone-stat-label">航程</div>
            </div>
            <div class="drone-stat-item">
              <div class="drone-stat-value">{{ stats?.drone?.endurance || 25 }}分钟</div>
              <div class="drone-stat-label">续航</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 地图和天气容器 -->
      <div class="map-weather-container">
        <div class="card field-map">
          <img src="@/assets/images/background/map.jpeg"
            alt="农田地图" class="map-image">
          <div class="map-overlay">
            <div style="text-align: center;">
              <h3 style="font-size: 24px; margin-bottom: 10px;">丝瓜种植区监测地图</h3>
              <p style="color: #a8b2d1; margin-bottom: 20px;">点击地图上的标记查看详细信息</p>
              <button class="btn btn-primary">
                <i class="fas fa-play"></i> 开始无人机巡航
              </button>
            </div>
          </div>
        </div>

        <div class="card weather-info">
          <div class="card-header">
            <h3 class="card-title">实时天气</h3>
            <div class="card-icon">
              <i class="fas fa-cloud-sun"></i>
            </div>
          </div>
          <div class="weather-content">
            <div class="weather-current">
              <div class="weather-temp">{{ stats?.weather?.temp || 26 }}°C</div>
              <div class="weather-desc">{{ stats?.weather?.desc || '晴朗' }}</div>
              <div class="weather-location">
                <i class="fas fa-map-marker-alt"></i>
                <span>丝瓜种植区 A区</span>
              </div>
            </div>
            <div class="weather-stats">
              <div class="weather-stat">
                <div class="stat-label">湿度</div>
                <div class="stat-value">{{ stats?.weather?.humidity || 65 }}%</div>
                <i class="fas fa-tint"></i>
              </div>
              <div class="weather-stat">
                <div class="stat-label">风速</div>
                <div class="stat-value">{{ stats?.weather?.windSpeed || 3.2 }} m/s</div>
                <i class="fas fa-wind"></i>
              </div>
              <div class="weather-stat">
                <div class="stat-label">气压</div>
                <div class="stat-value">{{ stats?.weather?.pressure || 1013 }} hPa</div>
                <i class="fas fa-compress"></i>
              </div>
            </div>
            
            <!-- 未来天气预测 -->
            <div class="weather-forecast">
              <h4 class="forecast-title">未来预测</h4>
              <div class="forecast-items">
                <div class="forecast-item">
                  <div class="forecast-time">2h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-sun"></i>
                  </div>
                  <div class="forecast-temp">{{ stats?.weather?.forecast?.twoHours?.temp || 25 }}°</div>
                  <div class="forecast-desc">{{ (stats?.weather?.forecast?.twoHours?.desc || '晴朗').substring(0, 2) }}</div>
                </div>
                <div class="forecast-item">
                  <div class="forecast-time">6h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-cloud"></i>
                  </div>
                  <div class="forecast-temp">{{ stats?.weather?.forecast?.sixHours?.temp || 23 }}°</div>
                  <div class="forecast-desc">{{ (stats?.weather?.forecast?.sixHours?.desc || '多云').substring(0, 2) }}</div>
                </div>
                <div class="forecast-item">
                  <div class="forecast-time">12h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-cloud-rain"></i>
                  </div>
                  <div class="forecast-temp">{{ stats?.weather?.forecast?.twelveHours?.temp || 20 }}°</div>
                  <div class="forecast-desc">{{ (stats?.weather?.forecast?.twelveHours?.desc || '小雨').substring(0, 2) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="card activity-log">
        <div class="card-header">
          <h3 class="card-title">最近活动</h3>
          <div class="card-icon">
            <i class="fas fa-history"></i>
          </div>
        </div>
        <div id="activities-container">
          <div v-if="loading" style="display: flex; justify-content: center; align-items: center; height: 100px;">
            <span class="loader"></span>
          </div>
          <div v-else>
            <div v-for="(activity, index) in activities" :key="index" class="activity-item">
              <div class="activity-icon" :class="getActivityIconClass(activity.type)">
                <i :class="getActivityIcon(activity.type)"></i>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.action }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="card data-table-card">
        <div class="card-header">
          <h3 class="card-title">详细数据记录</h3>
          <div class="card-actions">
            <button class="btn btn-secondary" @click="exportData">
              <i class="fas fa-download"></i> 导出数据
            </button>
          </div>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>日期</th>
                <th>区域</th>
                <th>病虫害类型</th>
                <th>识别数量</th>
                <th>严重程度</th>
                <th>处理状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(record, index) in records" :key="index">
                <td>{{ record.date }}</td>
                <td>{{ record.area }}</td>
                <td>{{ record.disease }}</td>
                <td>{{ record.count }}</td>
                <td>
                  <span class="badge" :class="getSeverityBadgeClass(record.severity)">{{ record.severity }}</span>
                </td>
                <td>
                  <span class="badge" :class="getStatusBadgeClass(record.status)">{{ record.status }}</span>
                </td>
                <td>
                  <button class="btn btn-secondary btn-sm">查看详情</button>
                </td>
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
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboard'

export default {
  name: 'Dashboard',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const stats = ref(null)
    const activities = ref([])
    const records = ref([])
    const loading = ref(true)

    const user = computed(() => authStore.user)

    const loadData = async () => {
      try {
        loading.value = true
        const [statsRes, activitiesRes, recordsRes] = await Promise.all([
          dashboardService.getStats(),
          dashboardService.getActivities(),
          dashboardService.getRecords()
        ])
        stats.value = statsRes.data
        activities.value = activitiesRes.data
        records.value = recordsRes.data
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }

    const getActivityIcon = (type) => {
      const icons = {
        '识别': 'fas fa-search',
        '操作': 'fas fa-cog',
        '查看': 'fas fa-eye'
      }
      return icons[type] || 'fas fa-circle'
    }

    const getActivityIconClass = (type) => {
      const classes = {
        '识别': 'detection',
        '操作': 'maintenance',
        '查看': 'update'
      }
      return classes[type] || ''
    }

    const getSeverityBadgeClass = (severity) => {
      if (severity === '重度') return 'badge-danger'
      if (severity === '中度') return 'badge-warning'
      return 'badge-success'
    }

    const getStatusBadgeClass = (status) => {
      if (status === '已处理') return 'badge-success'
      if (status === '处理中') return 'badge-warning'
      return 'badge-danger'
    }

    const exportData = () => {
      alert('导出功能开发中...')
    }

    onMounted(() => {
      loadData()
    })

    return {
      user,
      stats,
      activities,
      records,
      loading,
      getActivityIcon,
      getActivityIconClass,
      getSeverityBadgeClass,
      getStatusBadgeClass,
      exportData
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/dashboard.css';
</style>

