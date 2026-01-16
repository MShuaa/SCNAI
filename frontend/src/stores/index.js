import { createPinia } from 'pinia'
import { createApp } from 'vue'

const pinia = createPinia()

export default pinia

// 在main.js中调用：app.use(pinia)
export { pinia }
