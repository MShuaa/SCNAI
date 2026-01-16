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

        <!-- 搜索和筛选 -->
        <div style="padding: 20px; border-bottom: 1px solid #2d3748;">
          <div style="display: flex; gap: 20px; flex-wrap: wrap;">
            <el-input v-model="searchQuery" placeholder="搜索植物..." @input="handleSearch" clearable style="width: 200px;" />
            <el-select v-model="categoryFilter" placeholder="选择类别" @change="handleFilter" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option label="蔬菜" value="vegetable" />
              <el-option label="水果" value="fruit" />
              <el-option label="谷物" value="grain" />
            </el-select>
          </div>
        </div>

        <!-- 植物列表 -->
        <div v-if="loading" style="text-align: center; padding: 40px;">
          <span>加载中...</span>
        </div>
        <div v-else-if="filteredPlants.length === 0" style="text-align: center; padding: 40px;">
          <i class="fas fa-leaf" style="font-size: 48px; color: #a8b2d1; margin-bottom: 20px;"></i>
          <h3>未找到相关植物</h3>
          <p style="color: #a8b2d1;">尝试调整搜索条件</p>
        </div>
        <div v-else style="padding: 20px;">
          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px;">
            <div v-for="plant in filteredPlants" :key="plant.id" class="plant-card" @click="viewDetail(plant)">
              <img :src="plant.image_url" :alt="plant.name" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px;" />
              <div style="padding: 15px;">
                <h4 style="margin: 0 0 5px 0; color: #fff;">{{ plant.name }}</h4>
                <p style="margin: 0; color: #64ffda; font-size: 14px; font-style: italic;">{{ plant.scientific_name }}</p>
                <div style="margin-top: 10px; display: flex; justify-content: space-between; font-size: 12px; color: #a8b2d1;">
                  <span>{{ plant.disease_count }}种病害</span>
                  <span>{{ plant.pest_count }}种虫害</span>
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
import { ref, onMounted, computed } from 'vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'Atlas',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const loading = ref(true)
    const plants = ref([])
    const searchQuery = ref('')
    const categoryFilter = ref('')

    // 模拟数据 - 等待后端实现后移除
    const mockPlants = [
      { id: 1, name: '丝瓜', scientific_name: 'Luffa cylindrica', category: 'vegetable', image_url: '/src/assets/images/icons/shigua.jpeg', disease_count: 8, pest_count: 5 },
      { id: 2, name: '黄瓜', scientific_name: 'Cucumis sativus', category: 'vegetable', image_url: '/src/assets/images/background/map.jpeg', disease_count: 12, pest_count: 7 },
      { id: 3, name: '西红柿', scientific_name: 'Solanum lycopersicum', category: 'vegetable', image_url: '/src/assets/images/background/map.jpeg', disease_count: 15, pest_count: 9 },
      { id: 4, name: '苹果', scientific_name: 'Malus domestica', category: 'fruit', image_url: '/src/assets/images/background/map.jpeg', disease_count: 6, pest_count: 4 },
      { id: 5, name: '水稻', scientific_name: 'Oryza sativa', category: 'grain', image_url: '/src/assets/images/background/map.jpeg', disease_count: 10, pest_count: 8 }
    ]

    const filteredPlants = computed(() => {
      let filtered = plants.value
      if (categoryFilter.value) filtered = filtered.filter(p => p.category === categoryFilter.value)
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(p =>
          p.name.toLowerCase().includes(query) || p.scientific_name.toLowerCase().includes(query)
        )
      }
      return filtered
    })

    const loadData = async () => {
      loading.value = true
      await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载
      plants.value = mockPlants
      loading.value = false
    }

    const handleSearch = () => {} // 实时搜索，使用计算属性过滤
    const handleFilter = () => {} // 筛选改变，使用计算属性过滤

    const viewDetail = (plant) => ElMessage.info(`${plant.name}详情查看功能开发中...`)

    onMounted(loadData)

    return { user, loading, plants, searchQuery, categoryFilter, filteredPlants, handleSearch, handleFilter, viewDetail }
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
