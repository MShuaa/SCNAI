<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <img src="@/assets/images/icons/logo.jpg"
          alt="SCNAI植鉴">
        <h1>SCNAI植鉴</h1>
      </div>
    </div>
    <nav class="sidebar-menu">
      <router-link to="/dashboard" class="sidebar-menu-item" :class="{ active: $route.name === 'Dashboard' }">
        <i class="fas fa-gauge-simple"></i>
        <span>主控制台</span>
      </router-link>
      <router-link to="/visualization" class="sidebar-menu-item" :class="{ active: $route.name === 'Visualization' }">
        <i class="fas fa-chart-bar"></i>
        <span>数据可视化</span>
      </router-link>
      <router-link to="/recognition" class="sidebar-menu-item" :class="{ active: $route.name === 'Recognition' }">
        <i class="fas fa-camera"></i>
        <span>图像识别</span>
      </router-link>
      <router-link to="/history" class="sidebar-menu-item" :class="{ active: $route.name === 'History' }">
        <i class="fas fa-history"></i>
        <span>识别历史</span>
      </router-link>
      <router-link to="/chat" class="sidebar-menu-item" :class="{ active: $route.name === 'Chat' }">
        <i class="fas fa-comments"></i>
        <span>智能问答</span>
      </router-link>
      <router-link to="/atlas" class="sidebar-menu-item" :class="{ active: $route.name === 'Atlas' }">
        <i class="fas fa-leaf"></i>
        <span>植物图鉴</span>
      </router-link>
      <router-link to="/profile" class="sidebar-menu-item" :class="{ active: $route.name === 'Profile' }">
        <i class="fas fa-user"></i>
        <span>个人信息</span>
      </router-link>
      <a href="#" @click.prevent="handleLogout" class="sidebar-menu-item">
        <i class="fas fa-sign-out-alt"></i>
        <span>退出登录</span>
      </a>
    </nav>
  </aside>
</template>

<script>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/services/auth'

export default {
  name: 'Sidebar',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const handleLogout = async () => {
      try {
        await authService.logout()
        authStore.clearAuth()
        router.push('/login')
      } catch (error) {
        console.error('登出失败:', error)
        authStore.clearAuth()
        router.push('/login')
      }
    }

    return {
      handleLogout
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
</style>

