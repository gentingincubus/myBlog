<template>
  <div class="mainPageContainer">
    <!-- 顶部导航栏 (原汁原味还原动效与滑动形态，融入管理后台胶囊入口) -->
    <transition name="head" appear>
      <div v-show="isShowHead" class="headContainer transition"
        :class="isHeadTop ? 'headContainer_active' : 'headContainer_hidden'">
        <div class="head transition">
          <div class="head-left flexRowAlign">
            <div v-if="isHeadTop" class="editorText transition">MADE BY GENTING</div>
            <div v-else class="editorText transition">顺峰山VR</div>
          </div>

          <img src="@/assets/img/head.png" class="portrait transition" alt="avatar" />

          <!-- 右侧导航 + 胶囊入口容器 -->
          <div class="head-right flexRowAlign">
            <!-- 滚轮切页导航 -->
            <div class="navContainer flexCol transition">
              <div class="nav flexRow transition">
                <div v-for="(item, index) of navBtnList" :key="index" class="navBtn flexRowAlign transition"
                  :style="{ color: index === navIndex ? 'aqua' : '' }" @click="changeNav($event, index)">
                  {{ item.name }}
                </div>
              </div>
              <!-- 导航下滑动的青色指示线 -->
              <div class="navbar transition" :style="{ width: navbarWidth + 'px', left: navbarLeft + 'px' }"></div>
            </div>

            <!-- 🌟 融入顶部右上角的精致后台胶囊入口 -->
            <div class="capsule-wrap">
              <div v-if="userStore.token" class="capsule-btn admin-mode" title="已登录，点击进入管理中台" @click="goToAdmin">
                <span class="status-dot"></span>
                <span class="capsule-text">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员'
                  }}</span>
                <el-icon :size="13">
                  <Right />
                </el-icon>
              </div>

              <div v-else class="capsule-btn login-mode" title="点击前往登录 / 注册" @click="goToLogin">
                <el-icon :size="14">
                  <User />
                </el-icon>
                <span class="capsule-text">登录后台</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 页面滚屏容器 -->
    <div ref="mainPageRef" class="mainPage" :style="{ top: -mainPageScrollTop + 'px' }">
      <!-- 区域 1：首屏巨幕宣传 -->
      <div class="mainContainer area_1 clearfix">
        <div class="contentContainer flexColCenter">
          <div class="content">顺峰山公园</div>
          <div class="content">顺峰揽胜</div>
          <div ref="startViewBtnRef" class="startViewBtn" @click="startView">
            开始浏览
          </div>
        </div>
        <div class="footerContainer"></div>
      </div>

      <!-- 区域 2：顺峰揽胜 · 数字化空间概览 (升级优雅卡片) -->
      <div class="mainContainer area_2 flexColCenter">
        <div class="area2-content flexColCenter">
          <span class="section-tag">DIGITAL GARDEN & SCENERY</span>
          <h2 class="area2-title">顺峰揽胜 · 数字化探索空间</h2>
          <p class="area2-desc">
            依山傍水，峰峦叠翠。将顺峰山岭南古典园林风骨与现代化全栈技术底座相交融，打造集数字漫游、知识沉淀与高并发实验于一体的数字花园。
          </p>

          <div class="intro-cards-row">
            <div class="intro-card">
              <div class="intro-icon">🏞️</div>
              <h3 class="intro-heading">岭南风骨</h3>
              <p class="intro-p">青云塔下，桂畔湖畔，饱览南国名胜与古典园林神韵。</p>
            </div>

            <div class="intro-card">
              <div class="intro-icon">🌐</div>
              <h3 class="intro-heading">全景漫游</h3>
              <p class="intro-p">720° 视角沉浸式记录山水地标，触手可及的云端漫步体验。</p>
            </div>

            <div class="intro-card">
              <div class="intro-icon">⚡</div>
              <h3 class="intro-heading">现代全栈</h3>
              <p class="intro-p">Spring Boot 3 + Redis 缓存 + Redisson 强劲技术底座支撑。</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 区域 3：顺峰山 VR 地图 -->
      <div class="mainContainer area_3">
        <div class="mapContainer">
          <img class="mapImg" src="@/assets/img/thirdArea/leftMap.jpg" alt="青云湖地图" @click="goVR('leftMapPC')" />
          <img class="mapImg" src="@/assets/img/thirdArea/rightMap.jpg" alt="桂畔湖地图" @click="goVR('rightMapPC')" />
        </div>
        <div class="mapBackground"></div>
      </div>

      <!-- 区域 4：🌟 数字花园 · 站点导航矩阵 (原 78 完美升级) -->
      <div class="mainContainer area_4 flexColCenter">
        <div class="area4-wrapper flexColCenter">
          <div class="area4-header flexColCenter">
            <span class="section-tag">EXPLORE & NAVIGATION</span>
            <h2 class="area4-title">数字花园 · 站点导航传送门</h2>
            <p class="area4-desc">
              实时读取 Redis 旁路缓存中的站点导航，轻触卡片即可一键穿梭抵至各模块
            </p>
          </div>

          <!-- 导航卡片网格矩阵 -->
          <div class="nav-matrix-grid">
            <!-- 🌟 恒定置顶：管理中台专属传送门卡片 -->
            <div class="matrix-card admin-portal-card" @click="goToAdmin">
              <div class="card-badge">ADMIN</div>
              <div class="card-icon">⚙️</div>
              <div class="card-meta">
                <h4 class="card-title">管理控制中台</h4>
                <p class="card-detail">进入后台管理系统，体验分布式锁、限流器与事务控制台</p>
                <div class="card-jump">
                  <span>进入后台</span>
                  <el-icon>
                    <Right />
                  </el-icon>
                </div>
              </div>
            </div>

            <!-- 动态数据库 / Redis 渲染出来的导航卡片 -->
            <div v-for="nav in dynamicNavList" :key="nav.id" class="matrix-card" @click="handleNavJump(nav)">
              <div class="card-badge">{{ nav.isBlank ? '新窗口' : '站内' }}</div>
              <div class="card-icon">{{ nav.icon || '📌' }}</div>
              <div class="card-meta">
                <h4 class="card-title">{{ nav.name }}</h4>
                <p class="card-detail">{{ nav.path }}</p>
                <div class="card-jump">
                  <span>立即访问</span>
                  <el-icon>
                    <Right />
                  </el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { navApi } from '@/api/nav'

const router = useRouter()
const userStore = useUserStore()

const navIndex = ref(0)
const navbarWidth = ref(0)
const navbarLeft = ref(0)
const navBtnList = reactive([
  { name: '首页' },
  { name: '空间概览' },
  { name: '漫游地图' },
  { name: '站点导航' }
])

// 页面滑动状态
const mainPageScrollTop = ref(0)
const scrollDuration = 600
const isHeadTop = ref(true)
const isScroll = ref(false)
const isShowHead = ref(true)
const mainPageRef = ref(null)
const startViewBtnRef = ref(null)

// 动态导航列表
const dynamicNavList = ref([])

// 加载后台配置并缓存在 Redis 的导航数据
async function loadNavList() {
  try {
    const res = await navApi.list()
    if (res && res.data) {
      dynamicNavList.value = res.data
    }
  } catch (err) {
    // 页面静默容错
  }
}

// 点击卡片跳转
function handleNavJump(nav) {
  if (!nav.path) return
  if (nav.isBlank === 1 || nav.path.startsWith('http://') || nav.path.startsWith('https://')) {
    window.open(nav.path, '_blank')
  } else {
    router.push(nav.path)
  }
}

function goToAdmin() {
  router.push('/admin')
}

function goToLogin() {
  router.push('/login')
}

// 监听导航变化，联动滑块位置
watch(navIndex, (newValue, oldValue) => {
  if (oldValue !== newValue && isShowHead.value) {
    isHeadTop.value = newValue === 0
    nextTick(() => {
      const btnList = document.getElementsByClassName('navBtn')
      if (btnList && btnList[navIndex.value]) {
        const btn = btnList[navIndex.value]
        navbarWidth.value = btn.clientWidth
        navbarLeft.value = btn.offsetLeft
      }
    })
  }
})

// 点击导航项
function changeNav(e, index) {
  navIndex.value = index
  navbarLeft.value = e.target.offsetLeft
  const page = document.getElementsByClassName('mainContainer')
  if (page && page[navIndex.value]) {
    mainPageScrollTop.value = page[navIndex.value].offsetTop
  }
}

// 滚轮事件监听 (全屏滚轮切页)
function scrollChange(e) {
  if (!isScroll.value) {
    isScroll.value = true
    const direction = e.wheelDelta < 0 ? 'down' : 'up'
    pageScroll(direction)
    isShowHead.value = direction !== 'down'
    setTimeout(() => {
      isScroll.value = false
    }, scrollDuration)
  }
}

// 页面翻页计算
function pageScroll(direction, num = 1) {
  const page = document.getElementsByClassName('mainContainer')
  if (!page || page.length === 0) return
  const pageCount = page.length
  if (direction === 'down') {
    if (navIndex.value < pageCount - 1) {
      navIndex.value += num
    }
  } else {
    if (navIndex.value > 0) {
      navIndex.value -= num
    }
  }
  if (page[navIndex.value]) {
    mainPageScrollTop.value = page[navIndex.value].offsetTop
  }
}

// 开始浏览按钮动效与自动翻页
function startView(e) {
  const rect = e.target.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  const ripple = document.createElement('div')
  ripple.className = 'ripple'
  ripple.style.left = x + 'px'
  ripple.style.top = y + 'px'
  e.target.appendChild(ripple)

  setTimeout(() => {
    ripple.remove()
    pageScroll('down', 1)
  }, 650)
}

function goVR(mapName) {
  if (mapName === 'leftMapPC' || mapName === 'west_park') {
    router.push({ path: '/vr', query: { code: 'west_park' } })
  } else if (mapName === 'rightMapPC' || mapName === 'east_park') {
    router.push({ path: '/vr', query: { code: 'east_park' } })
  } else {
    router.push('/vr')
  }
}

onMounted(() => {
  loadNavList()
  nextTick(() => {
    const btnList = document.getElementsByClassName('navBtn')
    if (btnList && btnList[navIndex.value]) {
      navbarWidth.value = btnList[navIndex.value].clientWidth
    }
  })
  window.addEventListener('wheel', scrollChange, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('wheel', scrollChange)
})
</script>

<style scoped>
/* 字体定义 */
@font-face {
  font-family: 'FZZJ-HYJTJF';
  src: url("@/assets/font/FZZJ-HYJTJF_compress.ttf");
}

@font-face {
  font-family: 'tengxiang';
  src: url("@/assets/font/tengxiang_compress.ttf");
}

/* 核心布局 */
.mainPageContainer {
  --rem: calc((100vw / 750) * 40);
  position: relative;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background-color: #000;
}

.mainPage {
  position: absolute;
  width: 100%;
  font-size: 16px;
  transition: all 0.5s linear;
}

.mainPage::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  background: transparent;
}

.mainContainer {
  width: 100%;
  height: 100vh;
  position: relative;
}

/* ================= 区域 1：首屏宣传大图 ================= */
.area_1 {
  background-color: black;
  background-image: url('@/assets/img/firstArea/background.png');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
}

.area_1 .contentContainer {
  width: 100%;
  height: 100%;
}

.area_1 .contentContainer .content {
  color: white;
  font-family: 'FZZJ-HYJTJF', sans-serif;
  line-height: 1.15;
}

.area_1 .contentContainer .content:nth-child(1) {
  font-size: calc(var(--rem) * 3);
  text-shadow: 6px 6px 2px black;
  margin-bottom: 10px;
}

.area_1 .contentContainer .content:nth-child(2) {
  font-size: calc(var(--rem) * 1.5);
  text-shadow: 3px 3px 2px black;
  margin-bottom: 30px;
}

/* ================= 区域 2：顺峰揽胜 · 空间概览 ================= */
.area_2 {
  background: linear-gradient(135deg, #064e3b 0%, #065f46 50%, #0f172a 100%);
  padding: 60px 24px;
  box-sizing: border-box;
}

.area2-content {
  max-width: 960px;
  width: 100%;
  text-align: center;
}

.section-tag {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #34d399;
  background: rgba(52, 211, 153, 0.12);
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(52, 211, 153, 0.3);
  margin-bottom: 16px;
}

.area2-title {
  font-size: clamp(28px, 3.2vw, 42px);
  color: #f8fafc;
  margin: 0 0 16px;
  font-weight: 800;
  letter-spacing: 1px;
}

.area2-desc {
  font-size: 15px;
  line-height: 1.8;
  color: #cbd5e1;
  max-width: 720px;
  margin: 0 auto 40px;
}

.intro-cards-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  width: 100%;
}

.intro-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 30px 24px;
  text-align: left;
  transition: all 0.3s;
}

.intro-card:hover {
  transform: translateY(-6px);
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(52, 211, 153, 0.5);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.3);
}

.intro-icon {
  font-size: 32px;
  margin-bottom: 14px;
}

.intro-heading {
  font-size: 18px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 8px;
}

.intro-p {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.6;
  margin: 0;
}

/* ================= 区域 3：顺峰山 VR 地图 ================= */
.area_3 {
  position: relative;
  background: linear-gradient(60deg, #654ea3 0%, #eaafc8 100%);
  min-height: 500px;
  min-width: 850px;
}

.area_3 .mapBackground {
  position: absolute;
  inset: 0;
  margin: auto;
  width: 75%;
  height: 85%;
  filter: blur(1px);
  opacity: 0.3;
  border-radius: 10px;
  background-color: #fff;
  z-index: 1;
}

.area_3 .mapContainer {
  position: absolute;
  inset: 0;
  margin: auto;
  width: 75%;
  height: 85%;
  z-index: 10;
  display: flex;
  justify-content: space-evenly;
  align-items: center;
}

.area_3 .mapContainer .mapImg {
  height: 85%;
  border-radius: 10px;
  filter: brightness(0.6);
  cursor: pointer;
  transition: all 0.3s;
}

.area_3 .mapContainer .mapImg:hover {
  filter: brightness(1);
  transform: scale(1.02);
}

/* ================= 区域 4：🌟 站点导航矩阵 ================= */
.area_4 {
  background: linear-gradient(60deg, #543ab7 0%, #00acc1 100%);
  padding: 60px 24px;
  box-sizing: border-box;
  overflow-y: auto;
}

.area4-wrapper {
  max-width: 1120px;
  width: 100%;
}

.area4-header {
  text-align: center;
  margin-bottom: 32px;
}

.area4-title {
  font-size: clamp(28px, 3.2vw, 42px);
  color: #ffffff;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 1px;
}

.area4-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.nav-matrix-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  width: 100%;
}

.matrix-card {
  position: relative;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  padding: 22px 20px;
  cursor: pointer;
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
}

.matrix-card:hover {
  transform: translateY(-6px);
  background: rgba(255, 255, 255, 0.26);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.25);
}

.matrix-card.admin-portal-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.35), rgba(99, 102, 241, 0.35));
  border: 1.5px solid #38bdf8;
  box-shadow: 0 8px 30px rgba(56, 189, 248, 0.25);
}

.matrix-card.admin-portal-card:hover {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.5), rgba(99, 102, 241, 0.5));
  box-shadow: 0 16px 40px rgba(56, 189, 248, 0.45);
}

.card-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.25);
  color: #e2e8f0;
}

.admin-portal-card .card-badge {
  background: #38bdf8;
  color: #0f172a;
}

.card-icon {
  font-size: 34px;
  margin-bottom: 12px;
}

.card-meta {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 6px;
}

.card-detail {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.5;
  margin: 0 0 16px;
  flex: 1;
  word-break: break-all;
}

.card-jump {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #38bdf8;
  transition: transform 0.2s;
}

.matrix-card:hover .card-jump {
  transform: translateX(4px);
  color: #7dd3fc;
}

/* ================= 头部导航栏与胶囊入口 ================= */
.headContainer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.headContainer .head {
  position: relative;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  width: 92%;
  align-items: center;
}

.head-left {
  flex-shrink: 0;
}

/* 左侧艺术字体 */
.headContainer .head .editorText {
  font-size: calc(var(--rem) * 0.5);
  font-family: 'tengxiang', sans-serif;
  letter-spacing: 1px;
}

/* 中间头像 */
.headContainer .head .portrait {
  position: absolute;
  inset: 0;
  margin: auto;
  flex-shrink: 1;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

/* 导航栏容器 */
.headContainer .head .navContainer {
  position: relative;
  height: 100%;
}

.headContainer .head .navContainer .nav {
  align-items: center;
}

.headContainer .head .navContainer .nav .navBtn {
  margin-right: 22px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

.headContainer .head .navContainer .nav .navBtn:hover {
  color: aqua !important;
}

.headContainer .head .navContainer .navbar {
  position: relative;
  height: 3px;
  background-color: aqua;
  transition: all 0.3s ease;
}

/* 🌟 右上角胶囊按钮 */
.capsule-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}

.capsule-btn.admin-mode {
  background: rgba(56, 189, 248, 0.18);
  border: 1px solid rgba(56, 189, 248, 0.5);
  color: #38bdf8;
  backdrop-filter: blur(8px);
}

.capsule-btn.admin-mode:hover {
  background: rgba(56, 189, 248, 0.35);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.3);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #34d399;
  box-shadow: 0 0 6px #34d399;
}

.capsule-btn.login-mode {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: #ffffff;
  backdrop-filter: blur(8px);
}

.capsule-btn.login-mode:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #38bdf8;
  border-color: #38bdf8;
}

/* 导航处于顶部形态 */
.headContainer_active .head {
  height: 90px;
  border-bottom: 3px solid rgba(192, 192, 192, 0.5);
}

.headContainer_active .head .editorText {
  color: white;
}

.headContainer_active .head .portrait {
  width: 70px;
  height: 70px;
}

.headContainer_active .head .navBtn {
  color: white;
}

/* 导航下滑后的隐藏形态 */
.headContainer_hidden {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.headContainer_hidden .head {
  height: 54px;
}

.headContainer_hidden .head .editorText {
  color: #0f172a;
  font-size: 22px;
}

.headContainer_hidden .head .portrait {
  width: 0px;
  height: 0px;
  opacity: 0;
}

.headContainer_hidden .head .navBtn {
  color: #334155;
}

.headContainer_hidden .capsule-btn.admin-mode {
  background: #0284c7;
  color: #ffffff;
  border-color: #0284c7;
}

.headContainer_hidden .capsule-btn.login-mode {
  background: #f1f5f9;
  color: #0f172a;
  border-color: #cbd5e1;
}

/* 弹性工具类 */
.flexRow {
  display: flex;
  flex-direction: row;
}

.flexCol {
  display: flex;
  flex-direction: column;
}

.flexRowCenter {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.flexRowAlign {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.flexColCenter {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.transition {
  transition: all 0.5s ease-out;
}

/* 按钮动画 */
.startViewBtn {
  position: relative;
  width: 300px;
  height: 80px;
  border-radius: 50px;
  line-height: 80px;
  font-size: 25px;
  text-align: center;
  color: white;
  overflow: hidden;
  user-select: none;
  cursor: pointer;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  z-index: 2;
}

.startViewBtn::before {
  content: "";
  position: absolute;
  inset: -5px;
  border-radius: 50px;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  filter: blur(20px);
  z-index: -1;
}

.startViewBtn:hover::before,
.startViewBtn:hover {
  animation: liuguang 8s infinite;
}

@keyframes liuguang {
  100% {
    background-position: -400% 0;
  }
}

:deep(.ripple) {
  position: absolute;
  background-color: #fff;
  transform: translate(-50%, -50%);
  pointer-events: none;
  border-radius: 50%;
  animation: ripple 1s linear infinite;
  z-index: 10;
}

@keyframes ripple {
  0% {
    width: 0;
    height: 0;
    opacity: 0.5;
  }

  100% {
    width: 500px;
    height: 500px;
    opacity: 0;
  }
}

/* 导航栏进入离开过渡 */
.head-enter-active {
  transform: translateY(0);
  animation: headDown 0.3s linear;
}

.head-leave-active {
  transform: translateY(-100%);
  opacity: 0;
  animation: headUp 0.3s linear;
}

@keyframes headDown {
  0% {
    transform: translateY(-100%);
  }

  100% {
    transform: translateY(0);
  }
}

@keyframes headUp {
  0% {
    transform: translateY(0);
    opacity: 1;
  }

  100% {
    transform: translateY(-100%);
    opacity: 0;
  }
}
</style>
