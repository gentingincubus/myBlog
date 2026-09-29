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
    path: '/vr',
    name: 'vr-player',
    component: () => import('@/views/vr/VrPlayerView.vue'),
    meta: { title: '顺峰山 720° VR 全景漫游' }
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
      },
      {
        path: 'vr/category',
        name: 'admin-vr-category',
        component: () => import('@/views/admin/vr/VrCategoryManageView.vue'),
        meta: { title: 'VR园区分类', requiresAuth: true }
      },
      {
        path: 'vr/scene',
        name: 'admin-vr-scene',
        component: () => import('@/views/admin/vr/VrSceneManageView.vue'),
        meta: { title: 'VR场景管理', requiresAuth: true }
      },
      {
        path: 'vr/editor',
        name: 'admin-vr-editor',
        component: () => import('@/views/admin/vr/VrMapEditorView.vue'),
        meta: { title: 'VR打点编辑器', requiresAuth: true, perms: 'vr:editor:view' }
      },
      // 🌟 系统管理子模块
      {
        path: 'system/user',
        name: 'admin-system-user',
        component: () => import('@/views/admin/system/SysUserManageView.vue'),
        meta: { title: '用户管理', requiresAuth: true, perms: 'sys:user:list' }
      },
      {
        path: 'system/role',
        name: 'admin-system-role',
        component: () => import('@/views/admin/system/SysRoleManageView.vue'),
        meta: { title: '角色管理', requiresAuth: true, perms: 'sys:role:list' }
      },
      {
        path: 'system/menu',
        name: 'admin-system-menu',
        component: () => import('@/views/admin/system/SysMenuManageView.vue'),
        meta: { title: '菜单管理', requiresAuth: true, perms: 'sys:menu:list' }
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

// 🌟 全局路由守卫：保护 /admin 路由与 RBAC 权限过滤
router.beforeEach(async (to, from, next) => {
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
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }

    // 2. 动态拉取当前用户权限与路由
    const { useUserStore } = await import('@/stores/user')
    const userStore = useUserStore()

    if (!userStore.permissions || userStore.permissions.length === 0) {
      await Promise.all([
        userStore.fetchUserInfo(),
        userStore.fetchMenuRoutes()
      ])
    }

    // 3. 校验页面访问权限 (非超管且目标路由配置了特定权限时)
    const requiredPerm = to.meta?.perms
    if (requiredPerm) {
      const perms = userStore.permissions || []
      const isSuperAdmin = perms.includes('*:*:*')
      const hasPerm = isSuperAdmin || perms.includes(requiredPerm)

      if (!hasPerm) {
        ElMessage.error('对不起，您暂无访问该模块的权限！')
        return next('/admin/dashboard')
      }
    }
  }

  next()
})

export default router
