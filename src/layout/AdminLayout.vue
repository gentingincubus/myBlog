<template>
  <div class="admin-layout">
    <!-- 左侧侧边栏 (参考主流 agic-admin 经典暗夜/高级深色主题) -->
    <aside class="admin-sidebar" :class="{ collapsed: isCollapse }">
      <!-- 品牌 Logo 栏 -->
      <div class="sidebar-logo">
        <div class="logo-box">
          <el-icon :size="22" color="#fff">
            <Management />
          </el-icon>
        </div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <span class="brand-title">MyBlog</span>
            <span class="brand-badge">ADMIN</span>
          </div>
        </transition>
      </div>

      <!-- 侧边导航菜单 -->
      <!-- 侧边导航菜单 (根据用户角色权限动态渲染) -->
      <el-scrollbar class="sidebar-scroll">
        <el-menu :default-active="activeMenu" :collapse="isCollapse" :collapse-transition="false" router
          background-color="#0f172a" text-color="#94a3b8" active-text-color="#38bdf8" class="sidebar-menu">

          <template v-for="item in menuRoutes" :key="item.id">
            <!-- 1. 目录节点 (有子级菜单) -->
            <el-sub-menu v-if="item.menuType === 'M' && item.children && item.children.length > 0"
              :index="item.path || String(item.id)">
              <template #title>
                <el-icon v-if="item.icon">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.menuName }}</span>
              </template>

              <el-menu-item v-for="child in item.children" :key="child.id" :index="child.path">
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <template #title>{{ child.menuName }}</template>
              </el-menu-item>
            </el-sub-menu>

            <!-- 2. 独立菜单项 (无子级) -->
            <el-menu-item v-else-if="item.menuType === 'C'" :index="item.path">
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              <template #title>{{ item.menuName }}</template>
            </el-menu-item>
          </template>

        </el-menu>
      </el-scrollbar>

      <!-- 底部折叠切换按钮 -->
      <div class="sidebar-footer" @click="toggleCollapse">
        <el-icon :size="18">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </aside>

    <!-- 右侧主体内容容器 -->
    <div class="admin-main-container">
      <!-- 顶部 Header 栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 返回前台入口 -->
          <el-tooltip content="返回前台首页" placement="bottom">
            <el-button link class="action-icon-btn" @click="goToHome">
              <el-icon :size="18">
                <HomeFilled />
              </el-icon>
              <span class="portal-text">前台主页</span>
            </el-button>
          </el-tooltip>

          <!-- 用户头像与信息下拉 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-profile">
              <el-avatar :size="32" class="user-avatar">
                {{ avatarText }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
              <el-icon class="arrow-icon">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" disabled>
                  <el-icon>
                    <User />
                  </el-icon> 账号资料 (ID: {{ userStore.userInfo?.id || '-' }})
                </el-dropdown-item>
                <el-dropdown-item command="portal" divided>
                  <el-icon>
                    <HomeFilled />
                  </el-icon> 访问前台
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon>
                    <Lock />
                  </el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" style="color: #f43f5e;" divided>
                  <el-icon>
                    <SwitchButton />
                  </el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 主体内容区（带页面切换动效） -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改登录密码" width="440px" destroy-on-close :close-on-click-modal="false"
      class="custom-pwd-dialog">
      <div style="font-size: 13px; color: #64748b; margin-bottom: 16px;">
        为确保账户安全，请输入新密码并再次确认。修改成功后需重新登录。
      </div>

      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="95px" status-icon>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入 6 ~ 30 位新密码"
            autocomplete="off" clearable />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码（防误输）"
            autocomplete="off" clearable />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="pwdSubmitting" @click="handlePasswordSubmit">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

// 动态授权路由菜单列表
const menuRoutes = computed(() => userStore.menuRoutes || [])

// 当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 根据路由元信息动态推断面包屑标题
const currentRouteTitle = computed(() => {
  return route.meta?.title || '管理后台'
})

onMounted(async () => {
  if (!userStore.menuRoutes || userStore.menuRoutes.length === 0) {
    await userStore.fetchMenuRoutes()
  }
  if (!userStore.permissions || userStore.permissions.length === 0) {
    await userStore.fetchUserInfo()
  }
})

// 头像文字
const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || 'A'
  return name.slice(0, 1).toUpperCase()
})

function goToHome() {
  router.push('/')
}

// ==========================================
// 修改密码相关状态与逻辑 (免原密码验证，二次确认防手误)
// ==========================================
const pwdDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref(null)

const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致，请仔细核对'))
  } else {
    callback()
  }
}

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度需在 6 ~ 30 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function openPasswordDialog() {
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

function handlePasswordSubmit() {
  pwdFormRef.value?.validate(async (valid) => {
    if (!valid) return
    pwdSubmitting.value = true
    try {
      await authApi.changePassword({
        newPassword: pwdForm.newPassword,
        confirmPassword: pwdForm.confirmPassword
      })
      ElMessage.success('密码修改成功，请使用新密码重新登录')
      pwdDialogVisible.value = false
      userStore.clearToken()
      router.replace('/login')
    } catch (err) {
      // 异常拦截器会自动 Toast 提示
    } finally {
      pwdSubmitting.value = false
    }
  })
}

function handleUserCommand(cmd) {
  if (cmd === 'portal') {
    goToHome()
  } else if (cmd === 'password') {
    openPasswordDialog()
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出管理中台吗？', '安全提示', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.clearToken()
      ElMessage.success('已安全退出登录')
      router.replace('/login')
    }).catch(() => { })
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f1f5f9;
  overflow: hidden;
}

/* 侧边栏样式 */
.admin-sidebar {
  width: 220px;
  background-color: #0f172a;
  display: flex;
  flex-direction: column;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-logo {
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.logo-box {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: linear-gradient(135deg, #0284c7, #38bdf8);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: 0.5px;
}

.brand-badge {
  font-size: 10px;
  background: rgba(56, 189, 248, 0.2);
  color: #38bdf8;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.sidebar-scroll {
  flex: 1;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #1e293b !important;
  font-weight: 600;
}

.sidebar-footer {
  height: 46px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-footer:hover {
  color: #38bdf8;
  background: rgba(255, 255, 255, 0.04);
}

/* 主体内容区容器 */
.admin-main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
}

/* 顶部 Header */
.admin-header {
  height: 58px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #0284c7;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-icon-btn {
  color: #475569;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.portal-text {
  font-size: 13px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-profile:hover {
  background: #f1f5f9;
}

.user-avatar {
  background: linear-gradient(135deg, #0284c7, #38bdf8);
  color: #fff;
  font-weight: 700;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
}

/* 内容主体 */
.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 30px;
}

/* 页面切换淡入动效 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.22s ease-out;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
