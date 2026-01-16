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

      <!-- 综合部分：左两列（上两卡 + 下近期记录），右列天气占两行 -->
      <div class="comprehensive-grid" ref="compRef">
        <div class="overview-card card">
          <div class="card-header">
            <h3 class="card-title">数据概览卡片组</h3>
          </div>
          <div class="card-body overview-body">
            <ul class="overview-list">
              <li>今日识别： <strong>{{ overview.todayCount }}</strong></li>
              <li>成功率： <strong>{{ overview.successRate }}%</strong></li>
              <li>累计： <strong>{{ overview.totalCount }}</strong> 次</li>
            </ul>
          </div>
        </div>

        <div class="quick-entry-card card">
          <div class="card-header">
            <h3 class="card-title">快捷功能入口</h3>
          </div>
          <div class="card-body quick-entry-body">
            <div class="quick-icons">
              <button class="quick-btn" @click="goRecognition"><i class="fas fa-magnifying-glass"></i><div>识别</div></button>
              <button class="quick-btn" @click="goChat"><i class="fas fa-comments"></i><div>问答</div></button>
              <button class="quick-btn" @click="goAtlas"><i class="fas fa-leaf"></i><div>图鉴</div></button>
            </div>
          </div>
        </div>

        <div class="weather-card card" ref="weatherRef" style="grid-row: 1 / span 2;">
          <div class="card-header">
            <h3 class="card-title">天气与养护提醒</h3>
            <div class="card-icon"><i class="fas fa-cloud-sun"></i></div>
          </div>
          <div class="weather-content">
            <div class="weather-current">
              <div class="weather-temp">{{ weather.temp }}°C</div>
              <div class="weather-desc">{{ weather.advice || '晴朗' }}</div>
              <div class="weather-location">
                <i class="fas fa-map-marker-alt"></i>
                <span>丝瓜种植区 A区</span>
              </div>
            </div>

            <div class="weather-stats">
              <div class="weather-stat">
                <div class="stat-label">湿度</div>
                <div class="stat-value">{{ weather.humidity }}%</div>
                <i class="fas fa-tint"></i>
              </div>
              <div class="weather-stat">
                <div class="stat-label">风速</div>
                <div class="stat-value">{{ weather.windSpeed || 3.2 }} m/s</div>
                <i class="fas fa-wind"></i>
              </div>
              <div class="weather-stat">
                <div class="stat-label">气压</div>
                <div class="stat-value">{{ weather.pressure || 1013 }} hPa</div>
                <i class="fas fa-compress"></i>
              </div>
            </div>

            <div class="weather-forecast">
              <h4 class="forecast-title">未来预测</h4>
              <div class="forecast-items">
                <div class="forecast-item">
                  <div class="forecast-time">2h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-sun"></i>
                  </div>
                  <div class="forecast-temp">{{ weather.forecast?.twoHours?.temp || 25 }}°</div>
                  <div class="forecast-desc">{{ (weather.forecast?.twoHours?.desc || '晴朗').substring(0, 4) }}</div>
                </div>
                <div class="forecast-item">
                  <div class="forecast-time">6h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-cloud"></i>
                  </div>
                  <div class="forecast-temp">{{ weather.forecast?.sixHours?.temp || 23 }}°</div>
                  <div class="forecast-desc">{{ (weather.forecast?.sixHours?.desc || '多云').substring(0, 4) }}</div>
                </div>
                <div class="forecast-item">
                  <div class="forecast-time">12h后</div>
                  <div class="forecast-icon">
                    <i class="fas fa-cloud-rain"></i>
                  </div>
                  <div class="forecast-temp">{{ weather.forecast?.twelveHours?.temp || 20 }}°</div>
                  <div class="forecast-desc">{{ (weather.forecast?.twelveHours?.desc || '小雨').substring(0, 4) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="recent-records card" style="grid-column: 1 / span 2;">
          <div class="card-header"><h3 class="card-title">近期识别记录（最近3条）</h3></div>
          <div class="card-body recent-body">
            <div class="recent-list-vertical">
              <div v-for="rec in recentRecords" :key="rec.id" class="recent-row">
                <div class="row-left">
                  <div class="name">{{ rec.plant_name }}</div>
                  <div class="time">{{ formatDateShort(rec.identify_time) }}</div>
                </div>
                <div class="row-right">
                  <div class="confidence">{{ formatConfidence(rec.confidence) }}</div>
                  <button class="btn btn-link" @click="goToRecord(rec.id)">详情</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- note: recent records moved into comprehensive grid above -->

      <!-- 智能推荐（无符号、多项展示） -->
      <div class="card recommend-card">
        <div class="card-header"><h3 class="card-title">智能推荐</h3></div>
        <div class="card-body recommend-body">
          <div class="recommend-list">
            <div class="recommend-item">💡 秋季推荐养护植物：菊花、桂花、茉莉</div>
            <div class="recommend-item">⚠️ 当前高发病虫害：白粉病</div>
            <div class="recommend-item">🧪 本周建议检测土壤湿度：中心区采样点A</div>
            <div class="recommend-item">🔔 建议提醒：下周注意晚霜，适时覆盖</div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { recordService } from '@/services/record'
import { useRouter } from 'vue-router'

export default {
  name: 'Dashboard',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const loading = ref(true)
    const user = computed(() => authStore.user)

    // 使用默认的 mock 值以避免页面全为 0，后续在有真实数据时覆盖
    const overview = ref({
      todayCount: 5,
      successRate: 92,
      totalCount: 128
    })

    const weather = ref({
      temp: 25,
      humidity: 60,
      advice: '今天适合浇水'
    })

    const mockRecentRecords = [
      { id: 101, plant_name: '玫瑰', thumbnail_url: '/images/thumb1.jpg', identify_time: '2025-01-15T10:30:00', confidence: 0.98 },
      { id: 102, plant_name: '向日葵', thumbnail_url: '/images/thumb2.jpg', identify_time: '2025-01-14T08:20:00', confidence: 0.85 },
      { id: 103, plant_name: '仙人掌', thumbnail_url: '/images/thumb3.jpg', identify_time: '2025-01-13T14:50:00', confidence: 0.91 }
    ]
    const recentRecords = ref(mockRecentRecords)

    const router = useRouter()
    const compRef = ref(null)
    const weatherRef = ref(null)

    const syncHeights = () => {
      if (!compRef.value || !weatherRef.value) return
      const h = weatherRef.value.clientHeight
      compRef.value.style.height = h + 'px'
    }

    const loadData = async () => {
      loading.value = true
      try {
        // 获取统计信息（后端仅返回 totalRecognitions）
        const statsRes = await recordService.getStats()
        if (statsRes && statsRes.success && statsRes.data && Number(statsRes.data.totalRecognitions) > 0) {
          overview.value.totalCount = statsRes.data.totalRecognitions
        }

        // 获取最近若干条记录用于展示和计算今日数据
        const recordsRes = await recordService.getRecords({ page: 1, pageSize: 100 })
        let allRecords = []
        if (recordsRes && recordsRes.success) {
          allRecords = recordsRes.data.records || []
        }

        // 如后端返回真实记录则覆盖 mock 值；否则保留默认 mock
        if (allRecords.length > 0) {
          const todayStr = new Date().toISOString().slice(0, 10)
          const todayRecords = allRecords.filter(r => (r.identify_time || r.created_at || '').slice(0, 10) === todayStr)
          overview.value.todayCount = todayRecords.length
          const successCount = allRecords.filter(r => {
            const v = typeof r.confidence === 'string' ? parseFloat(r.confidence) : r.confidence
            return v >= 0.8
          }).length
          overview.value.successRate = allRecords.length ? Math.round((successCount / allRecords.length) * 100) : overview.value.successRate
        }

        // recent 3 - use mock for preview (keep backend fetch for stats)
        recentRecords.value = mockRecentRecords
        await nextTick()
        syncHeights()
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }

    const formatDateShort = (dateString) => {
      if (!dateString) return ''
      const d = new Date(dateString)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }

    const formatConfidence = (confidence) => {
      if (confidence === null || confidence === undefined) return '0.0%'
      const v = typeof confidence === 'string' ? parseFloat(confidence) : confidence
      return (v * 100).toFixed(1) + '%'
    }

    const goRecognition = () => router.push({ name: 'Recognition' })
    const goChat = () => router.push({ name: 'Chat' })
    const goAtlas = () => router.push({ name: 'Atlas' })
    const goToRecord = (id) => {
      if (!id) return
      router.push({ name: 'Visualization', params: { recordId: id } })
    }

    onMounted(() => {
      loadData()
      window.addEventListener('resize', syncHeights)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', syncHeights)
    })

    return {
      user,
      overview,
      weather,
      recentRecords,
      loading,
      formatDateShort,
      formatConfidence,
      goRecognition,
      goChat,
      goAtlas
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/dashboard.css';
</style>

