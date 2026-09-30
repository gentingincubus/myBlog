<template>
  <div class="portal-root">
    <!-- 顶部玻璃态导航栏 -->
    <header class="portal-header">
      <div class="header-inner">
        <!-- Logo 与站点标语 -->
        <div class="brand-group">
          <div class="brand-icon">✨</div>
          <div class="brand-texts">
            <span class="brand-title">Genting Incubus</span>
            <span class="brand-subtitle">数字空间 · 技术中台</span>
          </div>
        </div>

        <!-- 右侧操作区：登录/进入后台 -->
        <div class="header-actions">
          <template v-if="userStore.token">
            <div class="user-badge" :title="'当前登录：' + (userStore.userInfo?.nickname || userStore.userInfo?.username)">
              <div class="user-avatar-circle">
                <el-icon><User /></el-icon>
              </div>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
            </div>

            <button class="btn-primary" @click="goToAdmin">
              <el-icon><Platform /></el-icon>
              <span>进入管理中台</span>
            </button>

            <button class="btn-ghost" title="退出登录" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </template>

          <template v-else>
            <button class="btn-ghost" @click="goToLogin">
              <el-icon><User /></el-icon>
              <span>登录 / 注册</span>
            </button>
            <button class="btn-primary" @click="goToLogin">
              <span>立即进入后台</span>
              <el-icon><Right /></el-icon>
            </button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="portal-main">
      <!-- 英雄巨幕 Banner -->
      <section class="hero-section">
        <div class="hero-badge">
          <span class="pulse-dot"></span>
          <span>MULTI-SYSTEM PORTAL & BLOG PLATFORM</span>
        </div>
        <h1 class="hero-title">
          探索数字边界与 <span class="gradient-text">全栈实践</span>
        </h1>
        <p class="hero-desc">
          基于 Spring Boot 3 与现代前端的多项目协同架构 · 聚合 VR 沉浸体验、AIGC 智能助理与技术博客矩阵
        </p>

        <!-- 快捷指标栏 -->
        <div class="hero-stats-row">
          <div class="stat-pill" @click="jumpToVr">
            <span class="stat-icon">👓</span>
            <span class="stat-label">顺峰山 720° VR</span>
            <span class="stat-tag">已上线</span>
          </div>
          <div class="stat-pill" @click="goToAdmin">
            <span class="stat-icon">⚙️</span>
            <span class="stat-label">RBAC 综合管理后台</span>
            <span class="stat-tag live">在线运行</span>
          </div>
          <div class="stat-pill" @click="scrollToSection('nav-section')">
            <span class="stat-icon">🧭</span>
            <span class="stat-label">站点导航中枢</span>
            <span class="stat-tag">{{ navList.length }} 个节点</span>
          </div>
        </div>
      </section>

      <!-- 核心矩阵专区 (Featured Spaces) -->
      <section class="section-container">
        <div class="section-title-bar">
          <div class="title-left">
            <span class="section-sup">CORE MATRIX</span>
            <h2 class="section-main-title">核心业务旗舰</h2>
          </div>
          <p class="section-sub-desc">独立微服务与沉浸式体验子系统</p>
        </div>

        <div class="featured-grid">
          <!-- 旗舰 1：顺峰山 720° VR 全景漫游 -->
          <div class="feature-card vr-featured-card" @click="jumpToVr">
            <div class="card-bg-glow"></div>
            <div class="card-content-wrap">
              <div class="card-top-tags">
                <span class="badge vr-badge">720° VR 全景</span>
                <span class="badge tech-badge">Three.js / WebGL</span>
                <span class="badge domain-badge">vr.gentingincubus.com</span>
              </div>
              <h3 class="feature-name">顺峰山公园 · 720° 数字孪生空间</h3>
              <p class="feature-detail">
                高精度 720° 全景沉浸式漫游中华第一牌坊、青云古塔与桂畔湖湿地。支持小行星/正常/鱼眼多重视角切换、全园区点位穿梭漫游与 3D 毛玻璃景点导览。
              </p>
              <div class="feature-action-row">
                <div class="action-btn-link">
                  <span>立即启程漫游</span>
                  <el-icon><Right /></el-icon>
                </div>
                <span class="link-hint">点击直达独立 VR 子站 ↗</span>
              </div>
            </div>
          </div>

          <!-- 旗舰 2：管理后台中台系统 -->
          <div class="feature-card admin-featured-card" @click="goToAdmin">
            <div class="card-content-wrap">
              <div class="card-top-tags">
                <span class="badge admin-badge">综合运营中台</span>
                <span class="badge tech-badge">Spring Boot 3 + Vue 3</span>
              </div>
              <h3 class="feature-name">全站统一运营管理中心</h3>
              <p class="feature-detail">
                集中式管控全站 RBAC 角色与菜单权限体系、VR 园区分类及热点编辑器、毛玻璃导览轮播图，以及 Redis 旁路缓存与 R2 图片存储。
              </p>
              <div class="feature-action-row">
                <div class="action-btn-link">
                  <span>进入后台管理</span>
                  <el-icon><Right /></el-icon>
                </div>
                <span class="link-hint">/admin/dashboard</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 站点导航中枢 (基于 siteNav 接口动态渲染) -->
      <section id="nav-section" class="section-container">
        <div class="section-title-bar">
          <div class="title-left">
            <span class="section-sup">SITE NAVIGATION</span>
            <h2 class="section-main-title">站点导航与探索传送门</h2>
          </div>
          <p class="section-sub-desc">读取后台配置的站点外链与工具箱，轻触卡片即可直接跳转</p>
        </div>

        <!-- 加载中状态 -->
        <div v-if="navLoading" class="nav-loading-box">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>正在拉取站点导航矩阵...</span>
        </div>

        <!-- 导航卡片列表 -->
        <div v-else class="nav-cards-grid">
          <div
            v-for="nav in navList"
            :key="nav.id"
            class="nav-card-item"
            @click="handleNavClick(nav)"
          >
            <div class="nav-card-header">
              <div class="nav-card-icon">{{ nav.icon || '🔗' }}</div>
              <span class="nav-target-badge">{{ nav.isBlank === 1 ? '新窗口' : '站内' }}</span>
            </div>
            <h4 class="nav-card-name">{{ nav.name }}</h4>
            <p class="nav-card-path">{{ nav.path }}</p>
            <div class="nav-card-footer">
              <span class="jump-text">点击访问</span>
              <el-icon><Right /></el-icon>
            </div>
          </div>

          <!-- 兜底提示：如果后台尚未添加导航 -->
          <div v-if="navList.length === 0" class="nav-empty-box">
            <p>暂无配置导航项，可在管理后台【导航管理】中随时添加！</p>
            <button class="btn-primary" @click="goToAdmin">去添加导航</button>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部版权栏 -->
    <footer class="portal-footer">
      <div class="footer-inner">
        <div class="footer-left">
          <span class="footer-brand">Genting Incubus</span>
          <span class="footer-divider">|</span>
          <span class="footer-copy">© 2026 Genting. All Rights Reserved.</span>
        </div>
        <div class="footer-right">
          <span>Powered by Spring Boot 3 & Vue 3 · Nginx Reverse Proxy</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Right,
  Platform,
  SwitchButton,
  Loading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { navApi } from '@/api/nav'

const router = useRouter()
const userStore = useUserStore()

// 独立 VR 站点地址（由环境变量提供，本地开发为 5174，线上为 vr.gentingincubus.com）
const vrUrl = import.meta.env.VITE_VR_URL || 'http://localhost:5174'

// 站点导航列表状态
const navLoading = ref(true)
const navList = ref([])

async function loadNavList() {
  navLoading.value = true
  try {
    const res = await navApi.portalList()
    if (res && res.data) {
      navList.value = res.data
    }
  } catch (err) {
    // 接口降级容错
  } finally {
    navLoading.value = false
  }
}

function handleNavClick(nav) {
  if (!nav.path) return
  if (nav.isBlank === 1 || nav.path.startsWith('http://') || nav.path.startsWith('https://')) {
    window.open(nav.path, '_blank')
  } else {
    router.push(nav.path)
  }
}

function jumpToVr() {
  window.open(vrUrl, '_blank')
}

function goToAdmin() {
  router.push('/admin/dashboard')
}

function goToLogin() {
  router.push('/login')
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已安全退出登录')
}

function scrollToSection(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  loadNavList()
  if (userStore.token && (!userStore.userInfo || !userStore.userInfo.username)) {
    userStore.fetchUserInfo().catch(() => {})
  }
})
</script>

<style scoped>
.portal-root {
  min-height: 100vh;
  width: 100%;
  background: radial-gradient(circle at 50% 10%, #1e1b4b 0%, #0f172a 45%, #020617 100%);
  color: #f8fafc;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 顶部玻璃态导航 */
.portal-header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 68px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-inner {
  max-width: 1240px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand-group {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-icon {
  font-size: 26px;
}

.brand-texts {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #f8fafc;
}

.brand-subtitle {
  font-size: 11px;
  color: #94a3b8;
  letter-spacing: 1px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 9999px;
  font-size: 13px;
  color: #cbd5e1;
}

.user-avatar-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #38bdf8;
  color: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* 按钮规范 */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #0284c7, #2563eb);
  border: 1px solid rgba(56, 189, 248, 0.3);
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(2, 132, 199, 0.35);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(2, 132, 199, 0.5);
  background: linear-gradient(135deg, #0369a1, #1d4ed8);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 9999px;
  font-size: 13px;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

/* 主内容容器 */
.portal-main {
  flex: 1;
  max-width: 1240px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 24px 80px;
  box-sizing: border-box;
}

/* 巨幕 Banner */
.hero-section {
  text-align: center;
  padding: 60px 20px 48px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(56, 189, 248, 0.1);
  border: 1px solid rgba(56, 189, 248, 0.3);
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: #38bdf8;
  margin-bottom: 24px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #38bdf8;
  box-shadow: 0 0 10px #38bdf8;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
}

.hero-title {
  font-size: 46px;
  font-weight: 800;
  line-height: 1.25;
  margin: 0 0 16px;
  color: #f8fafc;
}

.gradient-text {
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: 17px;
  color: #94a3b8;
  max-width: 720px;
  margin: 0 auto 36px;
  line-height: 1.6;
}

.hero-stats-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.stat-pill:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(56, 189, 248, 0.4);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 18px;
}

.stat-label {
  font-size: 14px;
  color: #e2e8f0;
}

.stat-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.1);
  color: #94a3b8;
}

.stat-tag.live {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
}

/* 分区通用排版 */
.section-container {
  margin-top: 60px;
}

.section-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.section-sup {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #38bdf8;
  display: block;
  margin-bottom: 4px;
}

.section-main-title {
  font-size: 24px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0;
}

.section-sub-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

/* 核心矩阵卡片网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 24px;
}

.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 32px 28px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-4px);
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.35);
}

.vr-featured-card {
  background: linear-gradient(135deg, rgba(6, 78, 59, 0.25) 0%, rgba(15, 23, 42, 0.4) 100%);
  border-color: rgba(16, 185, 129, 0.3);
}

.vr-featured-card:hover {
  border-color: #34d399;
  box-shadow: 0 16px 36px rgba(16, 185, 129, 0.2);
}

.admin-featured-card {
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.25) 0%, rgba(15, 23, 42, 0.4) 100%);
  border-color: rgba(59, 130, 246, 0.3);
}

.admin-featured-card:hover {
  border-color: #60a5fa;
  box-shadow: 0 16px 36px rgba(59, 130, 246, 0.2);
}

.card-top-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.badge.vr-badge {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

.badge.admin-badge {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.4);
}

.badge.tech-badge {
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
}

.badge.domain-badge {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.feature-name {
  font-size: 21px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 12px;
}

.feature-detail {
  font-size: 14px;
  color: #94a3b8;
  line-height: 1.6;
  margin: 0 0 24px;
}

.feature-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.action-btn-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #38bdf8;
  transition: gap 0.2s ease;
}

.feature-card:hover .action-btn-link {
  gap: 10px;
  color: #7dd3fc;
}

.link-hint {
  font-size: 12px;
  color: #64748b;
}

/* 导航卡片列表 */
.nav-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.nav-card-item {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.nav-card-item:hover {
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(56, 189, 248, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.nav-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.nav-card-icon {
  font-size: 24px;
}

.nav-target-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
  color: #94a3b8;
}

.nav-card-name {
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0 0 6px;
}

.nav-card-path {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-card-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 12px;
  color: #38bdf8;
  font-weight: 500;
}

.nav-loading-box,
.nav-empty-box {
  padding: 48px;
  text-align: center;
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* 底部栏 */
.portal-footer {
  width: 100%;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(2, 6, 23, 0.6);
  padding: 24px 0;
  font-size: 13px;
  color: #64748b;
}

.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-brand {
  font-weight: 600;
  color: #94a3b8;
}

.footer-divider {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.15);
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  .featured-grid {
    grid-template-columns: 1fr;
  }
}
</style>
