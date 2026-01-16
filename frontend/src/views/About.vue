<template>
  <!-- 复用全局布局：侧边栏 + 顶部导航，保持与其他页面风格一致 -->
  <div class="container">
    <ParticlesBackground />
    <Sidebar />
    <main class="main-content">
      <div class="top-nav">
        <h2 class="top-nav-title">帮助 / 关于</h2>
        <div class="top-nav-user">
          <div class="user-avatar"><i class="fas fa-user"></i></div>
          <div class="user-info">
            <div class="user-name">{{ user?.username || '管理员' }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <!-- 页面为静态文档，后端无依赖；需要更新文案时直接修改此文件 -->
        <div class="card-body" style="padding:20px;">
          <!-- 功能简介 -->
          <h3>功能简介</h3>
          <p>本系统提供图像识别、识别历史管理、智能问答与植物图鉴等功能，面向农业/园艺场景的快速植物识别与养护建议。</p>

          <!-- 详细使用教程 -->
          <h3 style="margin-top:12px;">使用教程（步骤）</h3>
          <ol>
            <li><strong>登录</strong>：使用分配的账号登录系统，若无账号请联系管理员开户。</li>
            <li><strong>图像识别</strong>：进入“图像识别”页面，点击上传或拖拽图片，提交后等待识别结果。</li>
            <li><strong>查看结果</strong>：识别完成后在识别结果中查看推荐植物名与置信度，必要时进入详情查看相似度与养护建议。</li>
            <li><strong>历史记录</strong>：在“识别历史”可以导出或筛选记录，便于后续分析与追溯。</li>
            <li><strong>智能问答</strong>：进入“智能问答（Chat）”咨询养护问题或识别疑问，系统会基于模型给出建议。</li>
          </ol>

          <!-- 常见问题 -->
          <h3 style="margin-top:12px;">常见问题（FAQ）</h3>
          <ul>
            <li><strong>识别失败／结果不准确：</strong>尽量上传清晰、主体占比高的图片；避免强逆光和模糊。</li>
            <li><strong>上传失败或超时：</strong>检查网络，图片体积建议小于 5MB；若持续失败，请联系技术支持。</li>
            <li><strong>如何导出历史记录：</strong>进入“识别历史”，点击导出按钮选择所需时间范围即可。</li>
            <li><strong>如何删除个人数据：</strong>在个人信息页面提交删除请求或联系管理员处理。</li>
          </ul>

          <!-- 版本更新日志 -->
          <h3 style="margin-top:12px;">版本更新日志</h3>
          <ul>
            <li><strong>2025-01-08 — v1.2.0</strong>：上线智能问答模块；优化识别模型与提升稳定性。</li>
            <li><strong>2024-11-20 — v1.1.0</strong>：新增植物图鉴页面；支持历史记录导出功能。</li>
            <li><strong>2024-08-05 — v1.0.0</strong>：首个稳定版本，包含识别、历史、个人中心等基础功能。</li>
          </ul>

          <!-- 联系方式：带复制功能 -->
          <h3 style="margin-top:12px;">联系我们</h3>
          <p>如需帮助或反馈，请通过以下方式联系我们：</p>
          <p>
            邮箱：<code ref="emailRef">support@scnai.example.com</code>
            <button class="btn btn-sm btn-secondary" @click="copyEmail" style="margin-left:8px;">复制邮箱</button>
          </p>
          <p>电话：<code>+86-400-000-0000</code></p>

          <!-- 用户协议与隐私政策简要 -->
          <h3 style="margin-top:12px;">用户协议与隐私政策（简要）</h3>
          <p>我们仅在获得用户授权的情况下收集必要数据，用于提供识别与服务功能。用户可在个人中心查看和管理个人信息，若需完整协议请通过上方邮箱联系我们获取。</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
// 中文注释：该页面为纯静态帮助/关于页，复用全局组件与样式，包含少量交互（复制邮箱）
import ParticlesBackground from '@/components/ParticlesBackground.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

export default {
  name: 'About',
  components: { ParticlesBackground, Sidebar },
  setup() {
    const authStore = useAuthStore()
    const user = computed(() => authStore.user)

    // 复制邮箱到剪贴板，简洁实现避免引入额外依赖
    const copyEmail = async () => {
      const email = 'support@scnai.example.com'
      try {
        await navigator.clipboard.writeText(email)
        alert('已复制邮箱：' + email)
      } catch (err) {
        // 兼容回退
        const textarea = document.createElement('textarea')
        textarea.value = email
        document.body.appendChild(textarea)
        textarea.select()
        try {
          document.execCommand('copy')
          alert('已复制邮箱：' + email)
        } catch (e) {
          alert('复制失败，请手动复制：' + email)
        } finally {
          document.body.removeChild(textarea)
        }
      }
    }

    return { user, copyEmail }
  }
}
</script>

<style scoped>
@import '@/assets/css/common.css';
/* 放大并突出卡片内各部分标题，提升可读性和分块感 */
.card-body h3 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 14px 0;
  color: #ffffff; /* 标题使用纯白以在深色背景中高对比度显示 */
  text-shadow: 0 2px 4px rgba(0,0,0,0.45); /* 轻微阴影提升可读性 */
}

/* 适当增大正文/列表字体，改善段落间距 */
.card-body p,
.card-body ol,
.card-body ul {
  font-size: 14px;
  line-height: 1.7;
  color: #ffffff; /* 正文使用白色，提升可读性 */
}

/* 小按钮与代码样式微调 */
.card-body .btn {
  vertical-align: middle;
}
.card-body code {
  background: rgba(15,23,42,0.04);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
</style>  


