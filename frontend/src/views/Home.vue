<template>
  <div>
    <ParticlesBackground />
    <div class="hero-container">
      <div class="hero-content">
        <img src="@/assets/images/icons/logo.jpg" alt="SCNAI植鉴" class="hero-logo">
        <h1 class="hero-title">SCNAI植鉴</h1>
        <p class="hero-subtitle">智能农业 · 精准识别</p>
        <p class="hero-description">
          基于人工智能和无人机技术，实现丝瓜病虫害的自动识别、精准诊断和科学防治。
          提高农业生产效率，减少农药使用，保障农产品质量安全。
        </p>
        <div class="hero-buttons">
          <button class="hero-btn hero-btn-primary" @click="goToLogin">
            <i class="fas fa-right-to-bracket"></i> 登录系统
          </button>
          <button class="hero-btn hero-btn-secondary" @click="scrollToFeatures">
            <i class="fas fa-info-circle"></i> 了解更多
          </button>
        </div>

        <div class="features" ref="featuresRef">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-helicopter-symbol"></i>
            </div>
            <h3 class="feature-title">无人机巡检</h3>
            <p class="feature-description">
              利用无人机进行大面积农田巡检，快速获取高分辨率图像数据，覆盖范围广，效率高。
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-brain"></i>
            </div>
            <h3 class="feature-title">AI智能识别</h3>
            <p class="feature-description">
              基于深度学习算法，自动识别丝瓜病虫害类型，准确率高，识别速度快。
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-chart-line"></i>
            </div>
            <h3 class="feature-title">数据可视化</h3>
            <p class="feature-description">
              通过图表直观展示病虫害分布情况和发展趋势，辅助决策。
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-comments"></i>
            </div>
            <h3 class="feature-title">智能问答</h3>
            <p class="feature-description">
              基于知识图谱的智能问答系统，提供专业的病虫害防治建议。
            </p>
          </div>
        </div>

        <div class="footer">
          <p>© 2025 SCNAI植鉴 版权所有</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ParticlesBackground from '@/components/ParticlesBackground.vue'

export default {
  name: 'Home',
  components: {
    ParticlesBackground
  },
  setup() {
    const router = useRouter()
    const featuresRef = ref(null)

    const goToLogin = () => {
      router.push('/login')
    }

    const scrollToFeatures = () => {
      if (featuresRef.value) {
        featuresRef.value.scrollIntoView({ behavior: 'smooth' })
      }
    }

    onMounted(() => {
      const token = localStorage.getItem('token')
      if (token) {
        router.push('/dashboard')
      }
    })

    return {
      goToLogin,
      scrollToFeatures,
      featuresRef
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
@import '@/assets/css/index.css';
</style>

