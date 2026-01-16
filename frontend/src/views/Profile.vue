<template>
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">个人信息</h2>
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

      <div class="profile-section">
        <div class="profile-card">
          <div class="profile-header">
            <div class="profile-avatar">
              <i class="fas fa-circle-user"></i>
            </div>
            <div class="profile-name">{{ user?.username || '管理员' }}</div>
            <div class="profile-role">{{ user?.role || '系统管理员' }}</div>
          </div>
          <div class="profile-body">
            <div class="profile-info">
              <div class="info-item">
                <div class="info-label">用户名</div>
                <div class="info-value">{{ user?.username || 'admin' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">角色</div>
                <div class="info-value">{{ user?.role || '系统管理员' }}</div>
              </div>
            </div>
            <div class="profile-actions">
              <button class="btn btn-primary" @click="editProfile">
                <i class="fas fa-edit"></i> 编辑信息
              </button>
              <button class="btn btn-secondary" @click="changePassword">
                <i class="fas fa-key"></i> 修改密码
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { computed } from 'vue'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Profile',
  components: {
    ParticlesBackground,
    Sidebar
  },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    const editProfile = () => {
      alert('编辑功能开发中...')
    }

    const changePassword = () => {
      alert('修改密码功能开发中...')
    }

    return {
      user,
      editProfile,
      changePassword
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/profile.css';
</style>

