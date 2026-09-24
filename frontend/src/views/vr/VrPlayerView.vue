<template>
  <div ref="containerRef" class="vr-player-container">
    <!-- WebGL Canvas 挂载容器 -->
    <div ref="canvasWrapperRef" class="canvas-wrapper" />

    <!-- 初始全景加载中 Loading 遮罩 -->
    <transition name="fade">
      <div v-if="loading" class="vr-loading-mask">
        <div class="loading-content">
          <div class="loading-spinner" />
          <h2 class="loading-title">顺峰山公园 720° VR 全景漫游</h2>
          <p class="loading-sub">正在通过 Cloudflare R2 极速拉取高精度场景数据...</p>
        </div>
      </div>
    </transition>

    <!-- 顶部常驻导航控制栏 -->
    <header class="vr-top-bar" :class="{ 'hidden-bar': isImmersive }">
      <!-- 园区分类切换胶囊 -->
      <div class="category-tabs">
        <div class="brand-tag">
          <span class="pulse-dot" />
          <span class="brand-name">顺峰全景</span>
        </div>

        <div class="tab-list">
          <button v-for="cat in categoryList" :key="cat.id" class="cat-tab-btn"
            :class="{ active: currentCategoryId === cat.id }" @click="switchCategory(cat.id)">
            {{ cat.name }}
            <span v-if="!cat.mapUrl" class="no-map-mini">展馆</span>
          </button>
        </div>
      </div>

      <!-- 右侧控制工具群 -->
      <div class="action-tools">
        <!-- 视角切换器 -->
        <el-dropdown trigger="click" @command="handleChangeView">
          <button class="tool-btn" :title="'当前视角: ' + currentViewName">
            <el-icon>
              <View />
            </el-icon>
            <span class="tool-text">{{ currentViewName }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu class="dark-dropdown">
              <el-dropdown-item command="NORMAL">
                <el-icon>
                  <PictureFilled />
                </el-icon> 正常视角 (75° 沉浸)
              </el-dropdown-item>
              <el-dropdown-item command="PLANET">
                <el-icon>
                  <Coordinate />
                </el-icon> 小行星视角 (超广角全景)
              </el-dropdown-item>
              <el-dropdown-item command="FISHEYE">
                <el-icon>
                  <Aim />
                </el-icon> 鱼眼视角 (球体微曲透视)
              </el-dropdown-item>
              <el-dropdown-item command="CRYSTAL">
                <el-icon>
                  <Sunrise />
                </el-icon> 水晶球视角 (空间俯瞰)
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 自动旋转开关 -->
        <button class="tool-btn" :class="{ active: isAutoRotate }" :title="isAutoRotate ? '暂停自动巡航' : '开启自动巡航'"
          @click="toggleAutoRotate">
          <el-icon :class="{ 'spin-icon': isAutoRotate }">
            <RefreshRight />
          </el-icon>
          <span class="tool-text">{{ isAutoRotate ? '巡航中' : '巡航' }}</span>
        </button>

        <!-- 背景音乐开关 -->
        <button class="tool-btn" :class="{ active: isBgmPlaying }" :title="isBgmPlaying ? '关闭背景音乐' : '播放悠扬音乐'"
          @click="toggleBgm">
          <el-icon :class="{ 'vinyl-rotate': isBgmPlaying }">
            <Headset />
          </el-icon>
          <span class="tool-text">音乐</span>
        </button>

        <!-- 地图导览开关（若当前分类无地图底图，则自动隐藏） -->
        <button v-if="currentCategory?.mapUrl" class="tool-btn" :class="{ active: isShowMap }" title="导览地图与点位"
          @click="isShowMap = !isShowMap">
          <el-icon>
            <MapLocation />
          </el-icon>
          <span class="tool-text">地图</span>
        </button>

        <!-- 全屏模式切换 -->
        <button class="tool-btn icon-only" title="全屏沉浸" @click="toggleFullscreen">
          <el-icon>
            <FullScreen />
          </el-icon>
        </button>

        <!-- 返回主站门户 -->
        <button class="tool-btn icon-only" title="返回博客首页" @click="goHome">
          <el-icon>
            <HomeFilled />
          </el-icon>
        </button>
      </div>
    </header>

    <!-- 导览底图悬浮窗 (有地图底图时可用) -->
    <transition name="map-slide">
      <div v-if="isShowMap && currentCategory?.mapUrl" class="floating-map-box"
        :class="{ 'is-minimized': isMapMinimized }">
        <div class="map-box-header">
          <div class="map-box-title">
            <el-icon>
              <Compass />
            </el-icon>
            <span>{{ currentCategory.name }} · 导览地图</span>
          </div>
          <div class="map-box-actions">
            <button class="map-min-btn" @click="isMapMinimized = !isMapMinimized">
              {{ isMapMinimized ? '展开' : '折叠' }}
            </button>
            <button class="map-close-btn" @click="isShowMap = false">×</button>
          </div>
        </div>

        <div v-show="!isMapMinimized" class="map-box-body">
          <div class="map-inner-wrapper">
            <img :src="currentCategory.mapUrl" class="guide-map-img" draggable="false" />

            <!-- 地图标注点群（百分比坐标，零漂移） -->
            <div v-for="scene in sceneList" :key="scene.id" class="map-pin"
              :class="{ 'is-current': currentScene?.id === scene.id }"
              :style="{ left: scene.leftPercent + '%', top: scene.topPercent + '%' }" @click.stop="switchScene(scene)">
              <!-- 当前选中场景的实时视角导向锥形雷达 -->
              <div v-if="currentScene?.id === scene.id" class="radar-sector"
                :style="{ transform: `translate(-50%, -50%) rotate(${cameraHeading}deg)` }" />

              <!-- 图钉主体 -->
              <div class="pin-marker-dot">
                <span class="dot-core" />
              </div>

              <!-- 场景名称气泡 -->
              <div class="pin-tooltip">
                {{ scene.name }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 底部场景卡片抽屉 (缩略图轮播) -->
    <footer class="vr-bottom-carousel" :class="{ 'carousel-collapsed': isCarouselCollapsed }">
      <div class="carousel-toggle-bar">
        <button class="toggle-drawer-btn" @click="isCarouselCollapsed = !isCarouselCollapsed">
          <el-icon :size="14">
            <ArrowDown v-if="!isCarouselCollapsed" />
            <ArrowUp v-else />
          </el-icon>
          <span>{{ isCarouselCollapsed ? '展开场景列表 (' + sceneList.length + ')' : '收起' }}</span>
        </button>
      </div>

      <div v-show="!isCarouselCollapsed" class="carousel-scroll-zone">
        <div v-for="scene in sceneList" :key="scene.id" class="scene-thumb-card"
          :class="{ active: currentScene?.id === scene.id }" @click="switchScene(scene)">
          <div class="thumb-img-wrapper">
            <img :src="scene.previewUrl || scene.panoramaUrl" class="thumb-img" draggable="false" />
            <div class="thumb-active-overlay">
              <span class="live-dot" />
              <span>当前场景</span>
            </div>
          </div>
          <div class="thumb-caption" :title="scene.name">
            {{ scene.name }}
          </div>
        </div>
      </div>
    </footer>

    <!-- 当前场景名称与初始航向水印 -->
    <div class="scene-watermark" :class="{ 'hidden-bar': isImmersive }">
      <h1 class="watermark-title">{{ currentScene?.name || '顺峰全景漫游' }}</h1>
      <p class="watermark-sub">
        <span>{{ currentCategory?.name || '园区' }}</span>
        <span class="dot-divider">•</span>
        <span>视角: {{ currentViewName }}</span>
        <span class="dot-divider">•</span>
        <span>720° 全景漫游</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import * as TWEEN from '@tweenjs/tween.js'
import { vrApi } from '@/api/vr'

const route = useRoute()
const router = useRouter()

// DOM 引用
const containerRef = ref(null)
const canvasWrapperRef = ref(null)

// 响应式业务状态
const loading = ref(true)
const categoryList = ref([])
const currentCategoryId = ref(null)
const sceneList = ref([])
const currentScene = ref(null)

// 交互界面状态
const isShowMap = ref(true)
const isMapMinimized = ref(false)
const isCarouselCollapsed = ref(false)
const isAutoRotate = ref(true)
const isBgmPlaying = ref(false)
const isImmersive = ref(false)
const currentViewName = ref('正常视角')
const cameraHeading = ref(0) // 雷达朝向角度（0~360度）

// 视点预设
const VIEW_PRESETS = {
  NORMAL: { x: -8, y: 0, z: -2, fov: 75, maxDistance: 12, name: '正常视角' },
  PLANET: { x: 10, y: 35, z: 10, fov: 140, maxDistance: 45, name: '小行星视角' },
  FISHEYE: { x: -8, y: 15, z: -8, fov: 100, maxDistance: 40, name: '鱼眼视角' },
  CRYSTAL: { x: -50, y: 50, z: -50, fov: 75, maxDistance: 110, name: '水晶球视角' }
}

// 纯原生 Three.js 变量 (严禁经过 Vue Proxy 代理，防止 GPU 矩阵更新冲突与性能断崖)
let scene = null
let camera = null
let renderer = null
let controls = null
let textureLoader = null
let animFrameId = null
let currentSphere = null
let bgmAudio = null
let isTransitioning = false

// 当前选中的分类计算属性
const currentCategory = computed(() => {
  return categoryList.value.find(c => c.id === currentCategoryId.value) || null
})

// ==========================================
// 1. 数据驱动初始化
// ==========================================
async function initData() {
  try {
    const res = await vrApi.categoryList({ status: 1 })
    if (res.data && res.data.length > 0) {
      categoryList.value = res.data

      // 判断路由参数是否指定了 categoryId 或英文 code
      const queryCatId = route.query.categoryId
      const queryCatCode = route.query.code

      let targetCat = null
      if (queryCatId) {
        targetCat = categoryList.value.find(c => String(c.id) === String(queryCatId))
      } else if (queryCatCode) {
        targetCat = categoryList.value.find(c => c.code === queryCatCode)
      }

      if (!targetCat) {
        // 默认选择第一个分类
        targetCat = categoryList.value[0]
      }

      currentCategoryId.value = targetCat.id
      await loadCategoryScenes(targetCat.id)
    } else {
      ElMessage.warning('暂无开启的 VR 园区分类')
      loading.value = false
    }
  } catch (err) {
    ElMessage.error('获取全景数据失败')
    loading.value = false
  }
}

// 加载指定分类下的场景列表
async function loadCategoryScenes(categoryId) {
  try {
    const res = await vrApi.sceneList({ categoryId, status: 1 })
    if (res.data && res.data.length > 0) {
      sceneList.value = res.data

      // 如果有指定的 sceneId
      const querySceneId = route.query.sceneId
      let targetScene = null
      if (querySceneId) {
        targetScene = sceneList.value.find(s => String(s.id) === String(querySceneId))
      }
      if (!targetScene) {
        targetScene = sceneList.value[0]
      }

      // 如果 Three.js 尚未初始化，先初始化 3D 引擎；若已初始化，则切换场景
      if (!scene) {
        initThree(targetScene)
      } else {
        switchScene(targetScene)
      }
    } else {
      sceneList.value = []
      currentScene.value = null
      ElMessage.info('该园区下暂无场景')
      loading.value = false
    }
  } catch (err) {
    ElMessage.error('拉取场景列表失败')
  }
}

// 切换园区
async function switchCategory(catId) {
  if (currentCategoryId.value === catId) return
  currentCategoryId.value = catId
  loading.value = true
  await loadCategoryScenes(catId)
}

// ==========================================
// 2. Three.js 核心场景构建
// ==========================================
function initThree(initialScene) {
  if (!canvasWrapperRef.value) return

  // 1. 创建场景
  scene = new THREE.Scene()

  // 2. 创建透视相机（以开场小行星视角为初始视角）
  const width = window.innerWidth
  const height = window.innerHeight
  camera = new THREE.PerspectiveCamera(140, width / height, 0.1, 1000)
  camera.position.set(10, 35, 10)

  // 3. WebGL 渲染器
  renderer = new THREE.WebGLRenderer({
    antialias: true,
    powerPreference: 'high-performance'
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  canvasWrapperRef.value.appendChild(renderer.domElement)

  // 4. 轨道控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = isAutoRotate.value
  controls.autoRotateSpeed = 0.6
  controls.minDistance = 1
  controls.maxDistance = 50

  // 5. 纹理加载管理器
  textureLoader = new THREE.TextureLoader()

  // 6. 首个场景全景球体装配
  currentScene.value = initialScene
  loadSphereMesh(initialScene.panoramaUrl, () => {
    loading.value = false
    // 触发经典的开场飞入动效（从俯视小行星逐渐俯冲至正常视界）
    introOpeningAnimation(initialScene.initialDeg || 0)
  })

  // 7. 启动渲染循环
  animate()

  // 8. 监听窗口尺寸变化
  window.addEventListener('resize', onWindowResize)
}

// 装配球体全景网格（反转法线，让相机在球体内部往外看）
function loadSphereMesh(url, onLoadCallback) {
  textureLoader.load(
    url,
    (texture) => {
      texture.colorSpace = THREE.SRGBColorSpace
      texture.minFilter = THREE.LinearFilter
      texture.generateMipmaps = false // 全景等距柱状图关闭 mipmap 大幅缩减显存占用

      // 创建内径 40 的高细分球面
      const geometry = new THREE.SphereGeometry(40, 64, 40)
      geometry.scale(1, 1, -1) // 反向贴图

      const material = new THREE.MeshBasicMaterial({
        map: texture,
        transparent: true,
        opacity: 1
      })

      const mesh = new THREE.Mesh(geometry, material)
      scene.add(mesh)
      currentSphere = mesh

      if (onLoadCallback) onLoadCallback()
    },
    undefined,
    (err) => {
      ElMessage.error('全景图片加载失败，请检查网络或直链')
      loading.value = false
    }
  )
}

// 开场俯冲动画：从小行星视角平滑俯冲进入
function introOpeningAnimation(initialDeg = 0) {
  controls.autoRotate = false

  const start = { x: 10, y: 35, z: 10, fov: 140 }
  const target = { x: -8, y: 0, z: -2, fov: 75 }

  new TWEEN.Tween(start)
    .to(target, 3200)
    .easing(TWEEN.Easing.Cubic.Out)
    .onUpdate(() => {
      camera.position.set(start.x, start.y, start.z)
      camera.fov = start.fov
      camera.updateProjectionMatrix()
      controls.target.set(0, 0, 0)
    })
    .onComplete(() => {
      controls.autoRotate = isAutoRotate.value
      controls.maxDistance = VIEW_PRESETS.NORMAL.maxDistance
      currentViewName.value = '正常视角'
    })
    .start()
}

// ==========================================
// 3. 显存泄露防御型场景切换 (Cross-Fade 交叉平滑淡入淡出)
// ==========================================
function switchScene(sceneItem) {
  if (currentScene.value?.id === sceneItem.id || isTransitioning) return
  isTransitioning = true
  currentScene.value = sceneItem

  // 1. 加载新场景贴图
  textureLoader.load(sceneItem.panoramaUrl, (newTexture) => {
    newTexture.colorSpace = THREE.SRGBColorSpace
    newTexture.minFilter = THREE.LinearFilter
    newTexture.generateMipmaps = false

    // 2. 构建新球体，初始透明度为 0
    const geometry = new THREE.SphereGeometry(40, 64, 40)
    geometry.scale(1, 1, -1)

    const newMaterial = new THREE.MeshBasicMaterial({
      map: newTexture,
      transparent: true,
      opacity: 0
    })

    const nextSphere = new THREE.Mesh(geometry, newMaterial)
    scene.add(nextSphere)

    const oldSphere = currentSphere

    // 3. 利用 TWEEN 执行双球透明度交叉渐变
    const fadeObj = { opacity: 0 }
    new TWEEN.Tween(fadeObj)
      .to({ opacity: 1 }, 1200)
      .easing(TWEEN.Easing.Quadratic.InOut)
      .onUpdate(() => {
        newMaterial.opacity = fadeObj.opacity
        if (oldSphere) {
          oldSphere.material.opacity = 1 - fadeObj.opacity
        }
      })
      .onComplete(() => {
        // 🌟 核心显存释放：将旧网格从场景彻底移除并 Dispose 显存
        if (oldSphere) {
          scene.remove(oldSphere)
          oldSphere.geometry.dispose()
          if (oldSphere.material.map) {
            oldSphere.material.map.dispose()
          }
          oldSphere.material.dispose()
        }

        currentSphere = nextSphere
        isTransitioning = false
      })
      .start()
  })
}

// ==========================================
// 4. 视角预设平滑切换 (小行星 / 水晶球 / 鱼眼 / 正常)
// ==========================================
function handleChangeView(modeKey) {
  const preset = VIEW_PRESETS[modeKey]
  if (!preset) return

  currentViewName.value = preset.name

  const start = {
    x: camera.position.x,
    y: camera.position.y,
    z: camera.position.z,
    fov: camera.fov
  }

  controls.maxDistance = preset.maxDistance

  new TWEEN.Tween(start)
    .to({ x: preset.x, y: preset.y, z: preset.z, fov: preset.fov }, 2000)
    .easing(TWEEN.Easing.Cubic.Out)
    .onUpdate(() => {
      camera.position.set(start.x, start.y, start.z)
      camera.fov = start.fov
      camera.updateProjectionMatrix()
      controls.target.set(0, 0, 0)
    })
    .start()
}

// 自动巡航开关
function toggleAutoRotate() {
  isAutoRotate.value = !isAutoRotate.value
  if (controls) {
    controls.autoRotate = isAutoRotate.value
  }
}

// 背景音乐开关
function toggleBgm() {
  if (!bgmAudio) {
    bgmAudio = new Audio('https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/bliss.mp3')
    bgmAudio.loop = true
  }

  if (isBgmPlaying.value) {
    bgmAudio.pause()
    isBgmPlaying.value = false
  } else {
    bgmAudio.play().then(() => {
      isBgmPlaying.value = true
    }).catch(() => {
      ElMessage.warning('浏览器限制自动发声，请再次点击')
    })
  }
}

// 全屏模式
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    containerRef.value?.requestFullscreen().catch(() => { })
  } else {
    document.exitFullscreen().catch(() => { })
  }
}

function goHome() {
  router.push('/')
}

// ==========================================
// 5. 渲染循环与指南针/雷达偏角计算
// ==========================================
function animate(time) {
  animFrameId = requestAnimationFrame(animate)

  // 驱动 Tween 补间引擎
  TWEEN.update(time)

  // 控制器更新
  if (controls) {
    controls.update()
  }

  // 实时换算相机的水平航向偏角（以度数为单位驱动地图导向锥）
  if (camera) {
    // 根据相机的世界坐标矢量计算 XZ 平面的极角
    const deg = Math.atan2(camera.position.x, camera.position.z) * (180 / Math.PI)
    cameraHeading.value = Math.round(deg + 180) // 映射到 0~360 顺时针
  }

  // 渲染单帧
  if (renderer && scene && camera) {
    renderer.render(scene, camera)
  }
}

function onWindowResize() {
  if (!camera || !renderer) return
  const w = window.innerWidth
  const h = window.innerHeight
  camera.aspect = w / h
  camera.updateProjectionMatrix()
  renderer.setSize(w, h)
}

// ==========================================
// 6. 生命周期管理
// ==========================================
onMounted(() => {
  initData()
})

onBeforeUnmount(() => {
  // 1. 停止动画循环
  if (animFrameId) {
    cancelAnimationFrame(animFrameId)
  }
  window.removeEventListener('resize', onWindowResize)

  // 2. 清理全部 Tween 补间
  TWEEN.removeAll()

  // 3. 释放控制器
  if (controls) {
    controls.dispose()
  }

  // 4. 彻底释放 GPU 显存
  if (currentSphere) {
    currentSphere.geometry.dispose()
    if (currentSphere.material.map) {
      currentSphere.material.map.dispose()
    }
    currentSphere.material.dispose()
  }

  if (renderer) {
    renderer.dispose()
    if (renderer.domElement && renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement)
    }
  }

  // 5. 关闭音乐
  if (bgmAudio) {
    bgmAudio.pause()
    bgmAudio = null
  }
})
</script>

<style scoped>
.vr-player-container {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
  user-select: none;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.canvas-wrapper {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 优雅加载遮罩 */
.vr-loading-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #0f172a 0%, #020617 100%);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.loading-content {
  text-align: center;
}

.loading-spinner {
  width: 54px;
  height: 54px;
  border: 3px solid rgba(56, 189, 248, 0.15);
  border-top-color: #38bdf8;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 0.9s infinite linear;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.loading-sub {
  font-size: 13px;
  color: #94a3b8;
}

/* 顶部操作条 */
.vr-top-bar {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 20;
  pointer-events: none;
  transition: opacity 0.3s;
}

.category-tabs {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 4px 6px;
  border-radius: 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.brand-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px 0 6px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #38bdf8;
  box-shadow: 0 0 10px #38bdf8;
}

.brand-name {
  color: #f8fafc;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.tab-list {
  display: flex;
  gap: 4px;
}

.cat-tab-btn {
  background: transparent;
  border: none;
  outline: none;
  color: #94a3b8;
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.cat-tab-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.cat-tab-btn.active {
  background: #0284c7;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 10px rgba(2, 132, 199, 0.4);
}

.no-map-mini {
  font-size: 10px;
  background: rgba(255, 255, 255, 0.2);
  padding: 1px 4px;
  border-radius: 4px;
}

/* 右侧工具组 */
.action-tools {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-btn {
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f1f5f9;
  height: 38px;
  padding: 0 14px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;
}

.tool-btn.icon-only {
  width: 38px;
  padding: 0;
  justify-content: center;
}

.tool-btn:hover {
  background: rgba(30, 41, 59, 0.9);
  border-color: #38bdf8;
  color: #38bdf8;
}

.tool-btn.active {
  background: #0284c7;
  border-color: #38bdf8;
  color: #fff;
}

.spin-icon {
  animation: spin 3s infinite linear;
}

.vinyl-rotate {
  animation: spin 4s infinite linear;
}

/* 水印 */
.scene-watermark {
  position: absolute;
  top: 76px;
  left: 24px;
  pointer-events: none;
  z-index: 10;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.8);
}

.watermark-title {
  color: #f8fafc;
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 4px 0;
  letter-spacing: 1px;
}

.watermark-sub {
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot-divider {
  opacity: 0.5;
}

/* 导览地图悬浮窗 */
.floating-map-box {
  position: absolute;
  top: 76px;
  right: 20px;
  width: 340px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  overflow: hidden;
  z-index: 25;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-map-box.is-minimized {
  width: 220px;
}

.map-box-header {
  height: 38px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
  font-size: 12px;
  font-weight: 600;
}

.map-box-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.map-box-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-min-btn,
.map-close-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 12px;
  transition: color 0.2s;
}

.map-close-btn {
  font-size: 16px;
  line-height: 1;
}

.map-min-btn:hover,
.map-close-btn:hover {
  color: #fff;
}

.map-box-body {
  padding: 10px;
}

.map-inner-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
}

.guide-map-img {
  display: block;
  width: 100%;
  height: auto;
  pointer-events: none;
}

/* 地图标注点 */
.map-pin {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  z-index: 2;
  transition: transform 0.2s;
}

.map-pin:hover {
  transform: translate(-50%, -50%) scale(1.3);
  z-index: 5;
}

.map-pin.is-current {
  z-index: 10;
  transform: translate(-50%, -50%) scale(1.2);
}

.pin-marker-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(2, 132, 199, 0.4);
  border: 2px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-core {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #38bdf8;
}

.map-pin.is-current .pin-marker-dot {
  background: rgba(245, 158, 11, 0.5);
  border-color: #fef08a;
  box-shadow: 0 0 12px #f59e0b;
}

.map-pin.is-current .dot-core {
  background: #f59e0b;
}

/* 雷达导向扇形锥（跟随相机方向） */
.radar-sector {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 64px;
  height: 64px;
  pointer-events: none;
  background: conic-gradient(from -30deg at 50% 50%,
      rgba(56, 189, 248, 0) 0deg,
      rgba(56, 189, 248, 0.5) 30deg,
      rgba(56, 189, 248, 0) 60deg);
  border-radius: 50%;
  transform-origin: center center;
  z-index: -1;
}

.pin-tooltip {
  position: absolute;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.9);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s;
}

.map-pin:hover .pin-tooltip,
.map-pin.is-current .pin-tooltip {
  opacity: 1;
}

/* 底部场景轮播抽屉 */
.vr-bottom-carousel {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  width: min(92vw, 960px);
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 10px 14px;
  z-index: 20;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.vr-bottom-carousel.carousel-collapsed {
  padding: 4px 14px;
  width: auto;
  border-radius: 20px;
}

.carousel-toggle-bar {
  display: flex;
  justify-content: center;
}

.toggle-drawer-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 10px;
}

.toggle-drawer-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.carousel-scroll-zone {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 4px 4px;
  scrollbar-width: thin;
  scrollbar-color: #334155 transparent;
}

.carousel-scroll-zone::-webkit-scrollbar {
  height: 4px;
}

.carousel-scroll-zone::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 4px;
}

.scene-thumb-card {
  flex-shrink: 0;
  width: 104px;
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.scene-thumb-card:hover {
  transform: translateY(-4px);
}

.thumb-img-wrapper {
  position: relative;
  width: 100%;
  height: 68px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  background: #1e293b;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scene-thumb-card.active .thumb-img-wrapper {
  border-color: #38bdf8;
  box-shadow: 0 0 16px rgba(56, 189, 248, 0.6);
}

.thumb-active-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(2, 132, 199, 0.95));
  padding: 2px 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 9px;
  color: #fff;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
}

.scene-thumb-card.active .thumb-active-overlay {
  opacity: 1;
}

.live-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #fff;
}

.thumb-caption {
  font-size: 11px;
  color: #cbd5e1;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.scene-thumb-card.active .thumb-caption {
  color: #38bdf8;
  font-weight: 600;
}

/* 动效 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.map-slide-enter-active,
.map-slide-leave-active {
  transition: all 0.3s ease;
}

.map-slide-enter-from,
.map-slide-leave-to {
  opacity: 0;
  transform: translateY(-16px) scale(0.95);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .vr-top-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .action-tools {
    justify-content: flex-end;
  }

  .scene-watermark {
    top: 120px;
  }

  .floating-map-box {
    top: auto;
    bottom: 120px;
    right: 12px;
    width: 280px;
  }

  .vr-bottom-carousel {
    width: 96vw;
    bottom: 8px;
  }
}
</style>
