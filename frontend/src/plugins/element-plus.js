import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

export function setupElementPlus(app) {
  // 注册Element Plus
  app.use(ElementPlus, {
    size: 'default', // 默认尺寸
    zIndex: 3000
  })

  // 注册所有图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
}
