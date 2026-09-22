<template>
  <div class="login-wrapper">
    <!-- 动态背景装饰泡泡 -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="login-card">
      <!-- 头部 Logo 与标题 -->
      <div class="card-header">
        <div class="logo-circle">
          <el-icon :size="28" color="#409eff"><Management /></el-icon>
        </div>
        <h2 class="title">MyBlog 管理中台</h2>
        <p class="subtitle">Personal Digital Garden & Admin Console</p>
      </div>

      <!-- Tab 切换：登录 / 注册 -->
      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        <!-- 登录表单 -->
        <el-tab-pane label="账号登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <el-button link type="primary" size="small" @click="fillDemoAccount">填入演示账号</el-button>
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>

        <!-- 注册表单 -->
        <el-tab-pane label="新用户注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            size="large"
            @keyup.enter="handleRegister"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="3~30位字母、数字或下划线"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="用户昵称" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="个性昵称，如：极客行者"
                prefix-icon="Postcard"
                clearable
              />
            </el-form-item>

            <el-form-item label="设置密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少6位密码"
                prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <el-button
              type="success"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              立 即 注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 底部快捷回到前台 -->
      <div class="card-footer">
        <router-link to="/" class="back-home-link">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回博客前台</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const rememberMe = ref(true)

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  password: ''
})

const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度在 3 到 30 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 个字符', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度需在 3 到 30 个字符之间', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 30, message: '昵称长度需在 2 到 30 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

// 快速填入默认测试账号
function fillDemoAccount() {
  loginForm.username = 'admin'
  loginForm.password = '123456'
  ElMessage.info('已填入默认演示账号 (admin / 123456)')
}

// 执行登录
async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login({
        username: loginForm.username.trim(),
        password: loginForm.password
      })

      if (res && res.data) {
        const userInfo = res.data.userInfo || res.data.user || {}
        userStore.setLoginData(res.data.token, userInfo)
        ElMessage.success(`欢迎回来，${userInfo.nickname || userInfo.username || '管理员'}！`)

        // 优先跳转到之前拦截的目标页面，默认进入后台仪表盘
        const redirectUrl = route.query.redirect || '/admin/dashboard'
        router.replace(redirectUrl)
      }
    } catch (err) {
      // 错误由 request.js 拦截器统一提示
    } finally {
      loading.value = false
    }
  })
}

// 执行注册
async function handleRegister() {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.register({
        username: registerForm.username.trim(),
        nickname: registerForm.nickname.trim(),
        password: registerForm.password
      })

      if (res && res.status) {
        ElMessage.success('注册成功！已自动切换到登录')
        loginForm.username = registerForm.username
        loginForm.password = registerForm.password
        activeTab.value = 'login'
      }
    } catch (err) {
      // 错误由 request.js 拦截器统一提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  overflow: hidden;
  padding: 20px;
}

/* 装饰光斑 */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  pointer-events: none;
}
.shape-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #38bdf8, #2563eb);
  top: -80px;
  left: -80px;
}
.shape-2 {
  width: 460px;
  height: 460px;
  background: radial-gradient(circle, #818cf8, #c084fc);
  bottom: -100px;
  right: -100px;
}
.shape-3 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, #06b6d4, #3b82f6);
  top: 40%;
  left: 65%;
}

/* 毛玻璃卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 36px 32px 28px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-circle {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
}

.subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.submit-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-top: 8px;
}

.card-footer {
  margin-top: 24px;
  text-align: center;
  border-top: 1px dashed #e2e8f0;
  padding-top: 16px;
}

.back-home-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.2s;
}

.back-home-link:hover {
  color: #2563eb;
  transform: translateX(-2px);
}
</style>
