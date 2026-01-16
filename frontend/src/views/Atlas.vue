<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">植物图鉴</h2>
        <div class="top-nav-user">
          <div class="user-avatar"><i class="fas fa-user"></i></div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">植物图鉴</h3>
          <div class="card-icon"><i class="fas fa-leaf"></i></div>
        </div>

        <!-- 搜索功能 -->
        <div style="padding: 20px; border-bottom: 1px solid #2d3748;">
          <div style="display: flex; gap: 20px; flex-wrap: wrap;">
            <el-input
              v-model="searchQuery"
              placeholder="搜索植物名称或学名..."
              clearable
              style="width: 300px;"
              :prefix-icon="Search"
            />
            <div style="color: #a8b2d1; font-size: 14px; align-self: center;">
              共 {{ plants.length }} 种植物
            </div>
          </div>
        </div>

        <!-- 植物列表 -->
        <div v-if="loading" style="text-align: center; padding: 40px;">
          <span>加载中...</span>
        </div>
        <div v-else-if="plants.length === 0" style="text-align: center; padding: 40px;">
          <i class="fas fa-leaf" style="font-size: 48px; color: #a8b2d1; margin-bottom: 20px;"></i>
          <h3>未找到相关植物</h3>
          <p style="color: #a8b2d1;">尝试调整搜索条件</p>
        </div>
        <div v-else style="padding: 20px;">
          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;">
            <div v-for="plant in plants" :key="plant.id" class="plant-card" @click="viewDetail(plant)">
              <div v-if="plant.imageUrl" style="width: 100%; height: 150px; background: linear-gradient(135deg, #1e3a5f 0%, #2d3748 100%); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #64ffda; font-size: 24px;">
                <i class="fas fa-leaf"></i>
              </div>
              <div v-else style="width: 100%; height: 150px; background: linear-gradient(135deg, #1e3a5f 0%, #2d3748 100%); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #64ffda; font-size: 24px;">
                <i class="fas fa-leaf"></i>
              </div>
              <div style="padding: 15px;">
                <h4 style="margin: 0 0 5px 0; color: #fff; font-size: 16px; font-weight: 600;">{{ plant.name }}</h4>
                <p style="margin: 0; color: #64ffda; font-size: 14px; font-style: italic;" v-if="plant.scientificName">
                  {{ plant.scientificName }}
                </p>
                <div style="margin-top: 10px; display: flex; justify-content: space-between; font-size: 12px; color: #a8b2d1;">
                  <span v-if="plant.family">{{ plant.family }}</span>
                  <span v-if="plant.genus">{{ plant.genus }}</span>
                </div>
                <div style="margin-top: 8px; font-size: 12px; color: #a8b2d1; line-height: 1.4;" v-if="plant.description">
                  {{ plant.description.length > 50 ? plant.description.substring(0, 50) + '...' : plant.description }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { plantService } from '@/services/plant'
import { ElMessage } from 'element-plus'

export default {
  name: 'Atlas',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(false)
    const plants = ref([])
    const allPlants = ref([]) // 存储所有植物数据，用于本地筛选
    const searchQuery = ref('')

    // 移除类别筛选，后端没有category字段
    // const categoryFilter = ref('')

    const filteredPlants = computed(() => {
      let filtered = allPlants.value
      if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase().trim()
        filtered = filtered.filter(p =>
          p.name && p.name.toLowerCase().includes(query) ||
          p.scientificName && p.scientificName.toLowerCase().includes(query)
        )
      }
      return filtered
    })

    const loadData = async () => {
      try {
        loading.value = true
        const response = await plantService.getAllPlants()
        if (response.success) {
          allPlants.value = response.data || []
          plants.value = [...allPlants.value] // 初始化显示数据
        } else {
          ElMessage.error(response.error || '获取植物数据失败')
        }
      } catch (error) {
        console.error('加载植物数据失败:', error)
        ElMessage.error('加载植物数据失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 搜索处理函数 - 调用后端搜索API
    const handleSearch = async () => {
      const query = searchQuery.value.trim()
      if (!query) {
        // 如果搜索框为空，显示所有数据
        plants.value = [...allPlants.value]
        return
      }

      try {
        loading.value = true
        const response = await plantService.searchPlants(query)
        if (response.success) {
          plants.value = response.data || []
        } else {
          ElMessage.error(response.error || '搜索失败')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 监听搜索输入，实现防抖搜索
    let searchTimer = null
    watch(searchQuery, () => {
      if (searchTimer) clearTimeout(searchTimer)
      searchTimer = setTimeout(() => {
        handleSearch()
      }, 300) // 300ms防抖
    })

    const viewDetail = (plant) => {
      // 跳转到植物详情页面
      router.push(`/plant/${plant.id}`)
    }

    onMounted(loadData)

    return {
      user,
      loading,
      plants: filteredPlants, // 使用计算属性返回过滤后的数据
      searchQuery,
      handleSearch,
      viewDetail
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';

.plant-card {
  background-color: #1e3a5f;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.plant-card:hover {
  transform: translateY(-2px);
}
</style>
