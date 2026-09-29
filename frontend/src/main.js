import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

// 注册 Element Plus 所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

import hasPermi from './directive/permission/hasPermi'

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 🌟 全局注册按钮级细粒度权限控制指令 v-hasPermi
app.directive('hasPermi', hasPermi)

app.mount('#app')
