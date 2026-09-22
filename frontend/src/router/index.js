import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { TOKEN_KEY } from '@/stores/user'

import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import DashboardView from '@/views/admin/DashboardView.vue'
import NavManageView from '@/views/admin/NavManageView.vue'
import LabView from '@/views/admin/LabView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: '博客前台' }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '账号登录 - MyBlog' }
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: DashboardView,
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'nav',
        name: 'admin-nav',
        component: NavManageView,
        meta: { title: '导航管理', requiresAuth: true }
      },
      {
        path: 'lab',
        name: 'admin-lab',
        component: LabView,
        meta: { title: '技术实验室', requiresAuth: true }
      }
    ]
  },
  // 未匹配路径兜底回到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 🌟 全局路由守卫：保护 /admin 路由与页面 Title 控制
router.beforeEach((to, from, next) => {
  // 设置浏览器标签页标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} | MyBlog`
  }

  const rawToken = localStorage.getItem(TOKEN_KEY)
  const hasToken = rawToken && rawToken !== 'undefined' && rawToken !== 'null'

  // 1. 判断是否进入需要鉴权的路由（所有 /admin/** 路由）
  if (to.matched.some(record => record.meta.requiresAuth) || to.path.startsWith('/admin')) {
    if (!hasToken) {
      ElMessage.warning('请先登录后访问管理中台')
      // 记录来源路径，登录成功后自动回跳
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  }

  next()
})

export default router
