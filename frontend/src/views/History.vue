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

        <!-- 筛选条件 -->
        <div style="padding: 20px; border-bottom: 1px solid #2d3748;">
          <div style="display: flex; gap: 20px; flex-wrap: wrap;">
            <el-select v-model="statusFilter" placeholder="处理状态" @change="loadData" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option label="未处理" value="未处理" />
              <el-option label="已处理" value="已处理" />
            </el-select>
            <button class="btn btn-secondary" @click="exportData">
              <i class="fas fa-download"></i> 导出
            </button>
          </div>
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
                <th>时间</th><th>病虫害</th><th>置信度</th><th>状态</th><th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in records" :key="record.id">
                <td>{{ formatDate(record.created_at) }}</td>
                <td>{{ record.disease_name }}</td>
                <td>{{ record.confidence }}%</td>
                <td><span class="badge" :class="getStatusClass(record.status)">{{ record.status }}</span></td>
                <td><button class="btn btn-sm btn-secondary" @click="viewDetail(record)">详情</button></td>
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
import { ElMessage } from 'element-plus'

export default {
  name: 'History',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(true)
    const records = ref([])
    const statusFilter = ref('')

    // 模拟数据 - 等待后端实现后移除
    const mockData = [
      { id: 1, created_at: '2024-01-15', disease_name: '白粉病', confidence: 85, status: '未处理' },
      { id: 2, created_at: '2024-01-14', disease_name: '炭疽病', confidence: 92, status: '已处理' },
      { id: 3, created_at: '2024-01-13', disease_name: '病毒病', confidence: 78, status: '未处理' }
    ]

    const loadData = async () => {
      loading.value = true
      await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载
      records.value = statusFilter.value ? mockData.filter(r => r.status === statusFilter.value) : mockData
      loading.value = false
    }

    const formatDate = (dateString) => new Date(dateString).toLocaleDateString('zh-CN')
    const getStatusClass = (status) => status === '已处理' ? 'badge-success' : 'badge-danger'

    const viewDetail = (record) => ElMessage.info('详情查看功能开发中...')
    const exportData = () => ElMessage.info('导出功能开发中...')

    onMounted(loadData)

    return { user, loading, records, statusFilter, loadData, formatDate, getStatusClass, viewDetail, exportData }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
</style>
