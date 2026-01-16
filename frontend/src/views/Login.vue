<template>
  <div>
    <ParticlesBackground />
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <img src="@/assets/images/icons/logo.jpg" alt="SCNAI植鉴">
            <div>
              <h1 class="login-title">SCNAI植鉴</h1>
              <p class="login-subtitle">智能农业 · 精准识别</p>
            </div>
          </div>
        </div>
        <div class="login-body">
          <div v-if="errorMessage" class="error-message show">{{ errorMessage }}</div>

          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label" for="username">用户名</label>
              <input type="text" id="username" v-model="username" class="form-control" placeholder="请输入用户名" required>
            </div>
            <div class="form-group">
              <label class="form-label" for="password">密码</label>
              <input type="password" id="password" v-model="password" class="form-control" placeholder="请输入密码" required>
            </div>
            <div class="form-help">
              <div class="form-remember">
                <input type="checkbox" id="remember" v-model="remember">
                <label for="remember">记住我</label>
              </div>
              <a href="#" @click.prevent="handleForgotPassword" class="form-forgot">忘记密码？</a>
            </div>
            <button type="submit" class="btn-login" :disabled="loading">
              <span v-if="loading"><i class="fas fa-spinner fa-spin"></i> 登录中...</span>
              <span v-else>登录</span>
            </button>
          </form>
        </div>
        <div class="login-footer">
          <p class="login-footer-text">© 2025 SCNAI植鉴 | <a href="#" @click.prevent="handleSupport" class="login-footer-link">技术支持</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/services/auth'

export default {
  name: 'Login',
  components: {
    ParticlesBackground
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const authStore = useAuthStore()

    const username = ref('')
    const password = ref('')
    const remember = ref(false)
    const loading = ref(false)
    const errorMessage = ref('')

    // 检查是否记住密码
    onMounted(() => {
      const rememberMe = localStorage.getItem('rememberMe') === 'true'
      if (rememberMe) {
        const savedUsername = localStorage.getItem('savedUsername')
        if (savedUsername) {
          username.value = savedUsername
          remember.value = true
        }
      }
    })

    const handleLogin = async () => {
      errorMessage.value = ''
      loading.value = true

      try {
        const response = await authService.login(username.value, password.value, remember.value)

        if (response.success) {
          // 后端返回的数据结构是 { success: true, data: { token, user } }
          authStore.setAuth(response.data.token, response.data.user)

          // 保存记住密码选项
          localStorage.setItem('rememberMe', remember.value)
          if (remember.value) {
            localStorage.setItem('savedUsername', username.value)
          } else {
            localStorage.removeItem('savedUsername')
          }

          // 跳转到目标页面或仪表盘
          const redirect = route.query.redirect || '/dashboard'
          router.push(redirect)
        } else {
          errorMessage.value = response.error || '用户名或密码错误'
        }
      } catch (error) {
        console.error('登录错误:', error)
        errorMessage.value = error.response?.data?.error || '登录失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    const handleForgotPassword = () => {
      alert('请联系管理员重置密码')
    }

    const handleSupport = () => {
      alert('技术支持电话：400-123-4567')
    }

    return {
      username,
      password,
      remember,
      loading,
      errorMessage,
      handleLogin,
      handleForgotPassword,
      handleSupport
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/login.css';
</style>

