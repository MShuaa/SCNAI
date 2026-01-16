import { createApp } from 'vue'
import App from '@/App.vue'
import router from '@/router'
import pinia from '@/stores'
import { setupElementPlus } from '@/plugins/element-plus'
import '@/assets/css/common.css'
import '@fortawesome/fontawesome-free/css/all.min.css'

const app = createApp(App)
app.use(router)
app.use(pinia)
setupElementPlus(app)
app.mount('#app')

