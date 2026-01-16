<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">数据可视化</h2>
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

      <!-- 筛选条件 -->
      <div class="filter-card">
        <div class="filter-header">
          <h3 class="filter-title">数据筛选</h3>
        </div>
        <div class="filter-body">
          <div class="filter-row">
            <div class="filter-item">
              <label class="filter-label">时间范围</label>
              <select class="form-control" v-model="filters.timeRange">
                <option value="7">最近7天</option>
                <option value="30" selected>最近30天</option>
                <option value="90">最近90天</option>
                <option value="180">最近半年</option>
                <option value="365">最近一年</option>
              </select>
            </div>
            <div class="filter-item">
              <label class="filter-label">区域选择</label>
              <select class="form-control" v-model="filters.area">
                <option value="all">全部区域</option>
                <option value="east">东区</option>
                <option value="west">西区</option>
                <option value="south">南区</option>
                <option value="north">北区</option>
                <option value="center">中心区</option>
              </select>
            </div>
            <div class="filter-item">
              <label class="filter-label">病虫害类型</label>
              <select class="form-control" v-model="filters.diseaseType">
                <option value="all">全部类型</option>
                <option value="powdery">白粉病</option>
                <option value="downy">霜霉病</option>
                <option value="anthracnose">炭疽病</option>
                <option value="virus">病毒病</option>
                <option value="aphid">蚜虫</option>
              </select>
            </div>
            <div class="filter-item">
              <label class="filter-label">无人机编号</label>
              <select class="form-control" v-model="filters.droneId">
                <option value="all">全部无人机</option>
                <option value="drone001">无人机 #001</option>
                <option value="drone002">无人机 #002</option>
                <option value="drone003">无人机 #003</option>
              </select>
            </div>
            <div class="filter-action">
              <button class="btn btn-primary" @click="applyFilter">
                <i class="fas fa-filter"></i> 应用筛选
              </button>
              <button class="btn btn-secondary" @click="resetFilter">
                <i class="fas fa-redo"></i> 重置
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="card stats-card">
          <div class="stats-icon blue">
            <i class="fas fa-map-marker-alt"></i>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.totalArea || '1,250' }}</div>
            <div class="stats-label">监测总面积 (亩)</div>
          </div>
        </div>
        <div class="card stats-card">
          <div class="stats-icon green">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.healthyRate || '87.5%' }}</div>
            <div class="stats-label">健康率</div>
          </div>
        </div>
        <div class="card stats-card">
          <div class="stats-icon yellow">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.warningCount || '42' }}</div>
            <div class="stats-label">警告数量</div>
          </div>
        </div>
        <div class="card stats-card">
          <div class="stats-icon red">
            <i class="fas fa-bug"></i>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.detectionCount || '364' }}</div>
            <div class="stats-label">总识别次数</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">病虫害分布</h3>
            <div class="card-icon">
              <i class="fas fa-chart-pie"></i>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="diseaseChart"></canvas>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">健康度趋势</h3>
            <div class="card-icon">
              <i class="fas fa-chart-line"></i>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="healthChart"></canvas>
          </div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">各区域病虫害分布</h3>
            <div class="card-icon">
              <i class="fas fa-chart-bar"></i>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="areaChart"></canvas>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">识别效率统计</h3>
            <div class="card-icon">
              <i class="fas fa-chart-line"></i>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="efficiencyChart"></canvas>
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
                  <span :class="getSeverityBadgeClass(record.severity)">{{ record.severity }}</span>
                </td>
                <td>
                  <span :class="getStatusBadgeClass(record.status)">{{ record.status }}</span>
                </td>
                <td>
                  <button class="btn btn-secondary btn-sm" @click="viewDetail(record)">查看详情</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination">
          <button class="pagination-btn" @click="prevPage" :disabled="currentPage === 1">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button 
            v-for="page in totalPages" 
            :key="page"
            class="pagination-btn" 
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button class="pagination-btn" @click="nextPage" :disabled="currentPage === totalPages">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboard'
import { visualizationService } from '@/services/visualization'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'Visualization',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const user = authStore.user

    const filters = ref({
      timeRange: '30',
      area: 'all',
      diseaseType: 'all',
      droneId: 'all'
    })

    const stats = ref({
      totalArea: '1,250',
      healthyRate: '87.5%',
      warningCount: '42',
      detectionCount: '364'
    })

    const records = ref([
      { date: '2025-11-03', area: '东区', disease: '白粉病', count: 15, severity: '中度', status: '已处理' },
      { date: '2025-11-03', area: '西区', disease: '霜霉病', count: 8, severity: '重度', status: '处理中' },
      { date: '2025-11-02', area: '南区', disease: '蚜虫', count: 23, severity: '中度', status: '已处理' },
      { date: '2025-11-02', area: '北区', disease: '炭疽病', count: 6, severity: '重度', status: '已处理' },
      { date: '2025-11-01', area: '中心区', disease: '病毒病', count: 12, severity: '中度', status: '已处理' }
    ])

    const currentPage = ref(1)
    const totalPages = ref(3)

    const diseaseChart = ref(null)
    const healthChart = ref(null)
    const areaChart = ref(null)
    const efficiencyChart = ref(null)

    let chartInstances = {
      disease: null,
      health: null,
      area: null,
      efficiency: null
    }

    const getSeverityBadgeClass = (severity) => {
      if (severity === '重度') return 'badge badge-danger'
      if (severity === '中度') return 'badge badge-warning'
      return 'badge badge-success'
    }

    const getStatusBadgeClass = (status) => {
      if (status === '已处理') return 'badge badge-success'
      if (status === '处理中') return 'badge badge-warning'
      return 'badge badge-info'
    }

    const initCharts = async () => {
      await nextTick()
      
      // 动态导入Chart.js
      const { Chart, registerables } = await import('chart.js')
      Chart.register(...registerables)

      // 病虫害分布图表
      if (diseaseChart.value && !chartInstances.disease) {
        const ctx = diseaseChart.value.getContext('2d')
        chartInstances.disease = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: ['白粉病', '霜霉病', '炭疽病', '病毒病', '蚜虫', '其他'],
            datasets: [{
              data: [35, 25, 15, 10, 12, 3],
              backgroundColor: [
                '#f56565', '#f6d34d', '#38b2ac', '#68d391', '#805ad5', '#718096'
              ],
              borderWidth: 2,
              borderColor: '#172a45'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'right',
                labels: {
                  color: '#a8b2d1',
                  font: { size: 12 },
                  padding: 20
                }
              }
            },
            cutout: '60%'
          }
        })
      }

      // 健康度趋势图表
      if (healthChart.value && !chartInstances.health) {
        const ctx = healthChart.value.getContext('2d')
        chartInstances.health = new Chart(ctx, {
          type: 'line',
          data: {
            labels: ['10/27', '10/28', '10/29', '10/30', '10/31', '11/1', '11/2', '11/3'],
            datasets: [{
              label: '健康度 (%)',
              data: [92, 90, 88, 85, 83, 80, 85, 87],
              borderColor: '#64ffda',
              backgroundColor: 'rgba(100, 255, 218, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              x: {
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' }
              },
              y: {
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' },
                min: 70,
                max: 100
              }
            },
            plugins: {
              legend: { display: false }
            }
          }
        })
      }

      // 各区域病虫害分布图表
      if (areaChart.value && !chartInstances.area) {
        const ctx = areaChart.value.getContext('2d')
        chartInstances.area = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: ['东区', '西区', '南区', '北区', '中心区'],
            datasets: [
              { label: '白粉病', data: [35, 28, 22, 18, 15], backgroundColor: '#f56565' },
              { label: '霜霉病', data: [25, 30, 18, 22, 15], backgroundColor: '#f6d34d' },
              { label: '炭疽病', data: [15, 12, 18, 15, 10], backgroundColor: '#38b2ac' },
              { label: '病毒病', data: [10, 8, 12, 10, 15], backgroundColor: '#68d391' },
              { label: '蚜虫', data: [12, 15, 20, 18, 10], backgroundColor: '#805ad5' }
            ]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              x: {
                stacked: true,
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' }
              },
              y: {
                stacked: true,
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' }
              }
            },
            plugins: {
              legend: {
                position: 'top',
                labels: { color: '#a8b2d1', font: { size: 12 }, padding: 15 }
              }
            }
          }
        })
      }

      // 识别效率统计图表
      if (efficiencyChart.value && !chartInstances.efficiency) {
        const ctx = efficiencyChart.value.getContext('2d')
        chartInstances.efficiency = new Chart(ctx, {
          type: 'line',
          data: {
            labels: ['10/27', '10/28', '10/29', '10/30', '10/31', '11/1', '11/2', '11/3'],
            datasets: [
              {
                label: '识别数量',
                data: [42, 38, 45, 50, 47, 52, 48, 55],
                borderColor: '#64ffda',
                backgroundColor: 'transparent',
                borderWidth: 2,
                yAxisID: 'y'
              },
              {
                label: '识别准确率 (%)',
                data: [92, 93, 91, 94, 95, 93, 94, 96],
                borderColor: '#f6d34d',
                backgroundColor: 'transparent',
                borderWidth: 2,
                yAxisID: 'y1'
              }
            ]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              x: {
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' }
              },
              y: {
                type: 'linear',
                display: true,
                position: 'left',
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#a8b2d1' },
                title: { display: true, text: '识别数量', color: '#a8b2d1' }
              },
              y1: {
                type: 'linear',
                display: true,
                position: 'right',
                grid: { drawOnChartArea: false },
                ticks: { color: '#a8b2d1' },
                min: 80,
                max: 100,
                title: { display: true, text: '准确率 (%)', color: '#a8b2d1' }
              }
            },
            plugins: {
              legend: {
                position: 'top',
                labels: { color: '#a8b2d1', font: { size: 12 }, padding: 15 }
              }
            }
          }
        })
      }
    }

    const applyFilter = async () => {
      try {
        // 这里可以调用API获取筛选后的数据
        const response = await visualizationService.getData(filters.value)
        if (response.success) {
          // 更新数据
          updateCharts()
        }
      } catch (error) {
        console.error('筛选失败:', error)
      }
    }

    const resetFilter = () => {
      filters.value = {
        timeRange: '30',
        area: 'all',
        diseaseType: 'all',
        droneId: 'all'
      }
      initCharts()
    }

    const updateCharts = () => {
      // 更新图表数据的逻辑
      if (chartInstances.disease) {
        // 更新图表
      }
    }

    const exportData = () => {
      alert('数据导出功能开发中...')
    }

    const viewDetail = (record) => {
      alert(`查看详情: ${record.area} - ${record.disease}`)
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
      }
    }

    const goToPage = (page) => {
      currentPage.value = page
    }

    onMounted(async () => {
      try {
        // 加载数据
        const recordsResponse = await dashboardService.getRecords()
        if (recordsResponse.success) {
          records.value = recordsResponse.data
        }
        
        // 初始化图表
        await initCharts()
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    })

    onUnmounted(() => {
      // 销毁图表实例
      Object.values(chartInstances).forEach(chart => {
        if (chart) {
          chart.destroy()
        }
      })
    })

    return {
      user,
      filters,
      stats,
      records,
      currentPage,
      totalPages,
      diseaseChart,
      healthChart,
      areaChart,
      efficiencyChart,
      getSeverityBadgeClass,
      getStatusBadgeClass,
      applyFilter,
      resetFilter,
      exportData,
      viewDetail,
      prevPage,
      nextPage,
      goToPage
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/visualization.css';
</style>

