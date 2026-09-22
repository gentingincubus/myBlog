<template>
  <div class="dashboard-page">
    <!-- 欢迎顶栏卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-inner">
        <div class="welcome-avatar">
          <el-avatar :size="64" class="hero-avatar">
            {{ avatarText }}
          </el-avatar>
        </div>
        <div class="welcome-info">
          <h2 class="welcome-title">
            早安，{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}！
          </h2>
          <p class="welcome-sub">
            欢迎回到 MyBlog 管理控制台。当前系统运行正常，Redis 缓存加速与 Redisson 高并发排队组件就绪。
          </p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" icon="Compass" @click="$router.push('/admin/nav')">导航管理</el-button>
          <el-button icon="Cpu" @click="$router.push('/admin/lab')">高并发实验室</el-button>
        </div>
      </div>
    </el-card>

    <!-- 系统指标卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon-wrap bg-blue">
            <el-icon :size="24" color="#0284c7"><DataAnalysis /></el-icon>
          </div>
          <div class="stat-meta">
            <span class="stat-label">导航菜单总数</span>
            <span class="stat-val">{{ navTotal }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon-wrap bg-green">
            <el-icon :size="24" color="#10b981"><CircleCheckFilled /></el-icon>
          </div>
          <div class="stat-meta">
            <span class="stat-label">Redis 缓存层</span>
            <span class="stat-val text-green">Online</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon-wrap bg-purple">
            <el-icon :size="24" color="#8b5cf6"><Lock /></el-icon>
          </div>
          <div class="stat-meta">
            <span class="stat-label">Redisson 分布式锁</span>
            <span class="stat-val text-purple">RLock 就绪</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon-wrap bg-amber">
            <el-icon :size="24" color="#f59e0b"><Document /></el-icon>
          </div>
          <div class="stat-meta">
            <span class="stat-label">全链路日志</span>
            <span class="stat-val text-amber">MDC 追踪开启</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 架构组件与技术栈状态栏 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Platform /></el-icon> 系统技术底座架构</span>
              <el-tag size="small" type="success">全栈运行良好</el-tag>
            </div>
          </template>

          <div class="tech-grid">
            <div class="tech-item">
              <span class="tech-name">后端底座</span>
              <span class="tech-val">Spring Boot 3.3.4 (Java 21 LTS)</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">持久化引擎</span>
              <span class="tech-val">MySQL 8.0 + MyBatis-Plus 3.5.9</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">高速缓存 / 锁</span>
              <span class="tech-val">Docker Redis 7.2 + Redisson 3.35.0</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">认证与上下文</span>
              <span class="tech-val">JWT + ThreadLocal + UserContext</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">日志排障体系</span>
              <span class="tech-val">SLF4J + MDC (X-Trace-Id) + Logback 归档</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">前端技术栈</span>
              <span class="tech-val">Vue 3 (Vite) + Element Plus + Pinia</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Operation /></el-icon> 快捷工作台</span>
            </div>
          </template>

          <div class="quick-actions">
            <div class="action-tile" @click="$router.push('/admin/nav')">
              <div class="tile-icon bg-blue"><el-icon><Compass /></el-icon></div>
              <div class="tile-text">
                <strong>导航配置</strong>
                <small>管理前台顶部菜单与外链</small>
              </div>
            </div>

            <div class="action-tile" @click="$router.push('/admin/lab')">
              <div class="tile-icon bg-purple"><el-icon><Cpu /></el-icon></div>
              <div class="tile-text">
                <strong>技术实验室</strong>
                <small>分布式锁排队与事务回滚演练</small>
              </div>
            </div>

            <div class="action-tile" @click="$router.push('/')">
              <div class="tile-icon bg-green"><el-icon><HomeFilled /></el-icon></div>
              <div class="tile-text">
                <strong>访问前台</strong>
                <small>查看博客门户与公开展现</small>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { navApi } from '@/api/nav'

const userStore = useUserStore()
const navTotal = ref(0)

const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || 'A'
  return name.slice(0, 1).toUpperCase()
})

async function fetchNavCount() {
  try {
    const res = await navApi.list()
    if (res && res.data) {
      navTotal.value = res.data.length
    }
  } catch (err) {
    // 静默
  }
}

onMounted(() => {
  fetchNavCount()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}

.welcome-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.hero-avatar {
  background: linear-gradient(135deg, #0284c7, #38bdf8);
  font-size: 24px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.25);
}

.welcome-info {
  flex: 1;
  min-width: 240px;
}

.welcome-title {
  margin: 0 0 6px;
  font-size: 20px;
  color: #0f172a;
}

.welcome-sub {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.welcome-actions {
  display: flex;
  gap: 10px;
}

/* 统计卡片 */
.stat-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 8px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-blue { background: #e0f2fe; }
.bg-green { background: #dcfce7; }
.bg-purple { background: #f3e8ff; }
.bg-amber { background: #fef3c7; }

.stat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.stat-val {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.text-green { color: #10b981; }
.text-purple { color: #8b5cf6; }
.text-amber { color: #f59e0b; }

/* 信息卡片 */
.info-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #0f172a;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}

.tech-item {
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #edf2f7;
}

.tech-name {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.tech-val {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

/* 快捷磁贴 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-tile {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.action-tile:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateX(3px);
}

.tile-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.tile-text {
  display: flex;
  flex-direction: column;
}

.tile-text strong {
  font-size: 14px;
  color: #0f172a;
}

.tile-text small {
  font-size: 12px;
  color: #64748b;
}
</style>
