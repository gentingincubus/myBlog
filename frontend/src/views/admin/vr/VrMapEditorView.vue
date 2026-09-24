<template>
  <div class="vr-map-editor-page">
    <!-- 顶部综合工具栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-icon :size="20" class="header-icon">
          <Compass />
        </el-icon>
        <span class="header-title">VR 可视化导览打点编辑器</span>

        <!-- 园区选择器 -->
        <el-select v-model="currentCategoryId" placeholder="选择园区分类" class="category-selector"
          @change="handleCategoryChange">
          <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name + (cat.mapUrl ? ' (有底图)' : ' (无底图)')"
            :value="cat.id" />
        </el-select>
      </div>

      <!-- 中间操作工具：缩放与模式 -->
      <div v-if="currentCategory?.mapUrl" class="header-center">
        <el-button-group class="zoom-btn-group">
          <el-tooltip content="缩小 (快捷键: 鼠标滚轮)" placement="top">
            <el-button icon="Minus" @click="handleZoomOut" />
          </el-tooltip>
          <el-button class="zoom-level-btn" @click="resetZoom">
            {{ Math.round(zoomScale * 100) }}%
          </el-button>
          <el-tooltip content="放大 (快捷键: 鼠标滚轮)" placement="top">
            <el-button icon="Plus" @click="handleZoomIn" />
          </el-tooltip>
          <el-tooltip content="自适应居中" placement="top">
            <el-button icon="FullScreen" @click="fitToScreen">适应窗口</el-button>
          </el-tooltip>
        </el-button-group>

        <!-- 实时鼠标坐标显示 -->
        <div class="cursor-coord-badge">
          <span class="coord-label">光标坐标：</span>
          <span class="coord-val">X: {{ cursorCoord.x }}% | Y: {{ cursorCoord.y }}%</span>
        </div>
      </div>

      <div class="header-right">
        <el-tag v-if="hasModified" type="danger" effect="dark" class="dirty-tag">
          有未保存的改动
        </el-tag>
        <el-button type="primary" icon="Check" :loading="saving" :disabled="!hasModified" @click="saveAllCoordinates">
          保存所有点位坐标
        </el-button>
      </div>
    </header>

    <!-- 主体工作区 -->
    <div class="editor-workspace">
      <!-- 左侧场景抽屉列表 -->
      <aside class="scene-drawer" :class="{ collapsed: isDrawerCollapsed }">
        <div class="drawer-header">
          <span class="drawer-title">场景点位列表 ({{ sceneList.length }})</span>
          <el-button link :icon="isDrawerCollapsed ? 'Expand' : 'Fold'"
            @click="isDrawerCollapsed = !isDrawerCollapsed" />
        </div>

        <el-scrollbar v-show="!isDrawerCollapsed" class="drawer-scroll">
          <div v-if="sceneList.length === 0" class="drawer-empty">
            <el-empty description="当前分类下暂无场景" :image-size="70">
              <el-button type="primary" size="small" @click="goToAddScene">前往添加场景</el-button>
            </el-empty>
          </div>

          <div v-for="scene in sceneList" :key="scene.id" class="scene-card"
            :class="{ active: selectedSceneId === scene.id, modified: scene.isModified }" @click="selectScene(scene)">
            <div class="card-thumb">
              <el-image v-if="scene.previewUrl" :src="scene.previewUrl" fit="cover" class="thumb-img" />
              <span v-else class="no-img">VR</span>
            </div>

            <div class="card-info">
              <div class="card-title-row">
                <span class="card-title">{{ scene.name }}</span>
                <span v-if="scene.isModified" class="mod-dot" title="已调整尚未保存">•</span>
              </div>
              <div class="card-coords">
                <span class="coord-item">X: {{ scene.leftPercent }}%</span>
                <span class="coord-item">Y: {{ scene.topPercent }}%</span>
              </div>
            </div>

            <el-tooltip content="在地图上聚焦定位" placement="top">
              <el-button link type="primary" icon="Aim" class="focus-btn" @click.stop="focusPin(scene)" />
            </el-tooltip>
          </div>
        </el-scrollbar>
      </aside>

      <!-- 右侧无限画布容器 -->
      <main ref="viewportRef" class="map-viewport"
        :class="{ 'is-grabbing': isPanning, 'has-selected': !!selectedSceneId }" @wheel.prevent="handleWheel"
        @mousedown="handleCanvasMouseDown" @mousemove="handleCanvasMouseMove" @mouseup="handleCanvasMouseUp"
        @mouseleave="handleCanvasMouseUp">
        <!-- 空状态：分类没有导览底图 -->
        <div v-if="!currentCategory?.mapUrl" class="no-map-placeholder">
          <el-empty description="当前园区/分类暂未配置导览地图底图" :image-size="120">
            <template #description>
              <div class="no-map-desc">
                <p>当前园区（如室内展馆）未上传俯视导览图，前台将以无地图纯漫游模式运行。</p>
                <p>如需使用地图打点导览，请先前往【分类管理】上传底图图片。</p>
              </div>
            </template>
            <el-button type="primary" icon="Edit" @click="goToEditCategory">
              前往分类管理配置底图
            </el-button>
          </el-empty>
        </div>

        <!-- 缩放与平移变换容器 -->
        <div v-else class="map-transform-layer" :style="{
          transform: `translate(${panOffset.x}px, ${panOffset.y}px) scale(${zoomScale})`,
          transformOrigin: '0 0'
        }">
          <!-- 地图底图实体 -->
          <div ref="mapContainerRef" class="map-inner-container" @click="handleMapClick">
            <img ref="mapImgRef" :src="currentCategory.mapUrl" class="map-image" draggable="false"
              @load="onMapImageLoaded" />

            <!-- 打点图钉层 (全部挂载在地图容器百分比内，缩放不偏离) -->
            <div v-for="scene in sceneList" :key="scene.id" class="map-pin" :class="{
              'pin-selected': selectedSceneId === scene.id,
              'pin-modified': scene.isModified
            }" :style="{
                left: scene.leftPercent + '%',
                top: scene.topPercent + '%'
              }" @mousedown.stop="startPinDrag($event, scene)" @click.stop="selectScene(scene)">
              <!-- 图钉主体水滴或雷达图标 -->
              <div class="pin-marker">
                <div class="pin-pulse" />
                <div class="pin-head">
                  <el-icon :size="14">
                    <LocationFilled />
                  </el-icon>
                </div>
                <div class="pin-arrow" />
              </div>

              <!-- 图钉标签 -->
              <div class="pin-label">
                <span class="label-name">{{ scene.name }}</span>
                <span class="label-coords">{{ scene.leftPercent }}%, {{ scene.topPercent }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 画布辅助悬浮提示 -->
        <div v-if="currentCategory?.mapUrl" class="canvas-tips">
          <span>💡 交互技巧：滚轮缩放 | 按住空白处拖动画布 | 点击任意处移动选中图钉 | 拖拽图钉精准定位</span>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { vrApi } from '@/api/vr'

const route = useRoute()
const router = useRouter()

// 数据定义
const categoryList = ref([])
const currentCategoryId = ref(null)
const sceneList = ref([])
const selectedSceneId = ref(null)
const saving = ref(false)
const isDrawerCollapsed = ref(false)

// 画布视图控制
const viewportRef = ref(null)
const mapContainerRef = ref(null)
const mapImgRef = ref(null)

const zoomScale = ref(1)
const panOffset = reactive({ x: 100, y: 80 })
const cursorCoord = reactive({ x: '0.00', y: '0.00' })

// 拖拽画布平移状态
const isPanning = ref(false)
const panStart = reactive({ mouseX: 0, mouseY: 0, origX: 0, origY: 0 })

// 拖拽图钉状态
const isDraggingPin = ref(false)
const draggingScene = ref(null)

// 当前选中的分类对象
const currentCategory = computed(() => {
  return categoryList.value.find(c => c.id === currentCategoryId.value) || null
})

// 是否存在未保存的点位改动
const hasModified = computed(() => {
  return sceneList.value.some(s => s.isModified)
})

// 加载分类列表
async function fetchCategories() {
  try {
    const res = await vrApi.categoryList()
    if (res.data && res.data.length > 0) {
      categoryList.value = res.data

      // 如果路由传了 categoryId，优先使用
      const queryCatId = route.query.categoryId
      if (queryCatId && categoryList.value.some(c => c.id === Number(queryCatId) || c.id === queryCatId)) {
        currentCategoryId.value = queryCatId
      } else {
        // 默认选择第一个有底图的分类
        const withMap = categoryList.value.find(c => c.mapUrl)
        currentCategoryId.value = withMap ? withMap.id : categoryList.value[0].id
      }
      await fetchScenes()
    }
  } catch (err) { }
}

// 加载当前分类下的场景列表
async function fetchScenes() {
  if (!currentCategoryId.value) return
  try {
    const res = await vrApi.sceneList({ categoryId: currentCategoryId.value })
    if (res.data) {
      sceneList.value = res.data.map(item => ({
        ...item,
        leftPercent: Number(item.leftPercent) || 50,
        topPercent: Number(item.topPercent) || 50,
        isModified: false
      }))

      // 如果路由带了 sceneId，高亮并聚焦该点位
      const querySceneId = route.query.sceneId
      if (querySceneId) {
        selectedSceneId.value = querySceneId
      } else if (sceneList.value.length > 0) {
        selectedSceneId.value = sceneList.value[0].id
      }
    }
  } catch (err) { }
}

// 切换园区分类
async function handleCategoryChange() {
  selectedSceneId.value = null
  await fetchScenes()
  nextTick(() => {
    fitToScreen()
  })
}

// 地图图片加载完成后自适应居中
function onMapImageLoaded() {
  fitToScreen()
}

// 自适应居中窗口
function fitToScreen() {
  if (!viewportRef.value || !mapContainerRef.value) return
  const vpRect = viewportRef.value.getBoundingClientRect()
  const mapRect = mapContainerRef.value.getBoundingClientRect()

  const imgW = mapImgRef.value?.naturalWidth || 1200
  const imgH = mapImgRef.value?.naturalHeight || 800

  const scaleW = (vpRect.width - 120) / imgW
  const scaleH = (vpRect.height - 120) / imgH
  const bestScale = Math.min(Math.max(Math.min(scaleW, scaleH), 0.2), 1.5)

  zoomScale.value = Number(bestScale.toFixed(2))

  // 居中
  const finalW = imgW * bestScale
  const finalH = imgH * bestScale
  panOffset.x = Math.round((vpRect.width - finalW) / 2)
  panOffset.y = Math.round((vpRect.height - finalH) / 2)
}

function resetZoom() {
  zoomScale.value = 1
  fitToScreen()
}

function handleZoomIn() {
  zoomScale.value = Math.min(Number((zoomScale.value + 0.15).toFixed(2)), 4.0)
}

function handleZoomOut() {
  zoomScale.value = Math.max(Number((zoomScale.value - 0.15).toFixed(2)), 0.2)
}

// 滚轮缩放（以鼠标当前点为中心平滑缩放）
function handleWheel(e) {
  if (!viewportRef.value) return
  const vpRect = viewportRef.value.getBoundingClientRect()
  const mouseX = e.clientX - vpRect.left
  const mouseY = e.clientY - vpRect.top

  const delta = e.deltaY < 0 ? 0.12 : -0.12
  const oldScale = zoomScale.value
  let newScale = Math.min(Math.max(oldScale + delta, 0.2), 4.0)
  newScale = Number(newScale.toFixed(2))

  if (oldScale === newScale) return

  // 计算围绕鼠标缩放的平移补偿
  panOffset.x = mouseX - (mouseX - panOffset.x) * (newScale / oldScale)
  panOffset.y = mouseY - (mouseY - panOffset.y) * (newScale / oldScale)
  zoomScale.value = newScale
}

// 画布鼠标按下：开始拖拽平移
function handleCanvasMouseDown(e) {
  // 左键或中键拖动画布（避免图钉拖拽时冒泡）
  if (e.button === 0 || e.button === 1) {
    isPanning.value = true
    panStart.mouseX = e.clientX
    panStart.mouseY = e.clientY
    panStart.origX = panOffset.x
    panStart.origY = panOffset.y
  }
}

// 画布鼠标移动：更新平移或图钉拖拽位置，并计算实时百分比坐标
function handleCanvasMouseMove(e) {
  // 1. 如果正在平移画布
  if (isPanning.value) {
    const dx = e.clientX - panStart.mouseX
    const dy = e.clientY - panStart.mouseY
    panOffset.x = panStart.origX + dx
    panOffset.y = panStart.origY + dy
  }

  // 2. 如果正在拖拽图钉
  if (isDraggingPin.value && draggingScene.value && mapContainerRef.value) {
    const mapRect = mapContainerRef.value.getBoundingClientRect()
    const xPct = Math.min(Math.max(((e.clientX - mapRect.left) / mapRect.width) * 100, 0), 100)
    const yPct = Math.min(Math.max(((e.clientY - mapRect.top) / mapRect.height) * 100, 0), 100)

    draggingScene.value.leftPercent = Number(xPct.toFixed(2))
    draggingScene.value.topPercent = Number(yPct.toFixed(2))
    draggingScene.value.isModified = true
  }

  // 3. 计算并展示光标在地图上的百分比坐标
  if (mapContainerRef.value) {
    const mapRect = mapContainerRef.value.getBoundingClientRect()
    if (
      e.clientX >= mapRect.left &&
      e.clientX <= mapRect.right &&
      e.clientY >= mapRect.top &&
      e.clientY <= mapRect.bottom
    ) {
      const xPct = Math.min(Math.max(((e.clientX - mapRect.left) / mapRect.width) * 100, 0), 100)
      const yPct = Math.min(Math.max(((e.clientY - mapRect.top) / mapRect.height) * 100, 0), 100)
      cursorCoord.x = xPct.toFixed(2)
      cursorCoord.y = yPct.toFixed(2)
    }
  }
}

// 释放拖拽
function handleCanvasMouseUp() {
  isPanning.value = false
  if (isDraggingPin.value) {
    isDraggingPin.value = false
    draggingScene.value = null
  }
}

// 点击地图空白处：若有选中场景，直接将图钉传送到点击位置！
function handleMapClick(e) {
  if (!selectedSceneId.value || !mapContainerRef.value) return
  const scene = sceneList.value.find(s => s.id === selectedSceneId.value)
  if (!scene) return

  const mapRect = mapContainerRef.value.getBoundingClientRect()
  const xPct = Math.min(Math.max(((e.clientX - mapRect.left) / mapRect.width) * 100, 0), 100)
  const yPct = Math.min(Math.max(((e.clientY - mapRect.top) / mapRect.height) * 100, 0), 100)

  scene.leftPercent = Number(xPct.toFixed(2))
  scene.topPercent = Number(yPct.toFixed(2))
  scene.isModified = true

  ElMessage.success(`已将【${scene.name}】定位到 X: ${scene.leftPercent}%, Y: ${scene.topPercent}%`)
}

// 开始拖动图钉
function startPinDrag(e, scene) {
  isDraggingPin.value = true
  draggingScene.value = scene
  selectedSceneId.value = scene.id
}

// 选择场景卡片
function selectScene(scene) {
  selectedSceneId.value = scene.id
}

// 聚焦图钉（将视野平移居中到指定图钉）
function focusPin(scene) {
  selectedSceneId.value = scene.id
  if (!viewportRef.value || !mapContainerRef.value) return

  const vpRect = viewportRef.value.getBoundingClientRect()
  const imgW = mapImgRef.value?.naturalWidth || 1200
  const imgH = mapImgRef.value?.naturalHeight || 800

  // 放大到 100% 视距
  zoomScale.value = 1.0

  const targetX = (scene.leftPercent / 100) * imgW
  const targetY = (scene.topPercent / 100) * imgH

  panOffset.x = Math.round(vpRect.width / 2 - targetX)
  panOffset.y = Math.round(vpRect.height / 2 - targetY)
}

// 一键保存所有修改过的点位坐标
async function saveAllCoordinates() {
  const modifiedList = sceneList.value.filter(s => s.isModified)
  if (modifiedList.length === 0) {
    ElMessage.info('没有需要保存的坐标改动')
    return
  }

  saving.value = true
  try {
    // 逐个提交更新
    for (const item of modifiedList) {
      await vrApi.updateScene(item.id, {
        categoryId: item.categoryId,
        name: item.name,
        panoramaUrl: item.panoramaUrl,
        previewUrl: item.previewUrl,
        topPercent: item.topPercent,
        leftPercent: item.leftPercent,
        initialDeg: item.initialDeg,
        sort: item.sort,
        status: item.status
      })
      item.isModified = false
    }
    ElMessage.success(`🎉 成功保存 ${modifiedList.length} 个场景的打点坐标！`)
  } catch (err) {
    ElMessage.error('保存失败，请检查网络或后端状态')
  } finally {
    saving.value = false
  }
}

function goToAddScene() {
  router.push({
    path: '/admin/vr/scene',
    query: { categoryId: currentCategoryId.value }
  })
}

function goToEditCategory() {
  router.push('/admin/vr/category')
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.vr-map-editor-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 110px);
  background: #090d16;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

/* 顶部综合工具栏 */
.editor-header {
  height: 54px;
  background: #0f172a;
  border-bottom: 1px solid #1e293b;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: #38bdf8;
}

.header-title {
  color: #f1f5f9;
  font-weight: 600;
  font-size: 15px;
}

.category-selector {
  width: 220px;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 18px;
}

.zoom-btn-group :deep(.el-button) {
  background: #1e293b;
  border-color: #334155;
  color: #cbd5e1;
}

.zoom-btn-group :deep(.el-button:hover) {
  background: #334155;
  color: #38bdf8;
}

.zoom-level-btn {
  width: 68px;
  font-weight: 600;
  font-size: 13px;
}

.cursor-coord-badge {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid #334155;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  gap: 4px;
}

.coord-val {
  color: #38bdf8;
  font-family: monospace;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dirty-tag {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    opacity: 0.8;
  }

  50% {
    opacity: 1;
  }

  100% {
    opacity: 0.8;
  }
}

/* 主体工作区 */
.editor-workspace {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

/* 左侧场景抽屉 */
.scene-drawer {
  width: 280px;
  background: #0f172a;
  border-right: 1px solid #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  flex-shrink: 0;
  z-index: 5;
}

.scene-drawer.collapsed {
  width: 52px;
}

.drawer-header {
  height: 44px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #1e293b;
  color: #cbd5e1;
  font-size: 13px;
  font-weight: 600;
}

.drawer-scroll {
  flex: 1;
  padding: 10px;
}

.scene-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #1e293b;
  border: 1px solid transparent;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.scene-card:hover {
  background: #253349;
  border-color: #38bdf8;
}

.scene-card.active {
  background: #1e3a5f;
  border-color: #0284c7;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.2);
}

.scene-card.modified {
  border-left: 3px solid #f43f5e;
}

.card-thumb {
  width: 44px;
  height: 34px;
  border-radius: 4px;
  overflow: hidden;
  background: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.no-img {
  font-size: 10px;
  color: #64748b;
  font-weight: 700;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title {
  font-size: 13px;
  color: #f1f5f9;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mod-dot {
  color: #f43f5e;
  font-size: 18px;
  line-height: 0;
}

.card-coords {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

.coord-item {
  font-family: monospace;
}

.focus-btn {
  color: #94a3b8;
}

.focus-btn:hover {
  color: #38bdf8;
}

/* 右侧画布区域 */
.map-viewport {
  flex: 1;
  background-color: #0b0f19;
  background-image: radial-gradient(#1e293b 1px, transparent 1px);
  background-size: 24px 24px;
  position: relative;
  overflow: hidden;
  cursor: grab;
  user-select: none;
}

.map-viewport.is-grabbing {
  cursor: grabbing;
}

.map-viewport.has-selected {
  cursor: crosshair;
}

/* 平移与缩放变换层 */
.map-transform-layer {
  position: absolute;
  top: 0;
  left: 0;
  will-change: transform;
}

/* 地图内容容器 */
.map-inner-container {
  position: relative;
  display: inline-block;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  overflow: visible;
}

.map-image {
  display: block;
  max-width: none;
  pointer-events: none;
}

/* 图钉样式 */
.map-pin {
  position: absolute;
  transform: translate(-50%, -100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: move;
  z-index: 20;
  transition: transform 0.15s ease-out;
}

.map-pin:hover {
  z-index: 30;
  transform: translate(-50%, -105%) scale(1.1);
}

.map-pin.pin-selected {
  z-index: 40;
  transform: translate(-50%, -108%) scale(1.2);
}

.pin-marker {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pin-head {
  width: 28px;
  height: 28px;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  background: linear-gradient(135deg, #0284c7, #38bdf8);
  border: 2px solid #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
}

.pin-head .el-icon {
  transform: rotate(45deg);
  color: #fff;
}

.pin-selected .pin-head {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  border-color: #fef08a;
  box-shadow: 0 0 16px rgba(245, 158, 11, 0.8);
}

.pin-pulse {
  position: absolute;
  top: 14px;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid #38bdf8;
  opacity: 0;
  pointer-events: none;
  animation: radar-pulse 2s infinite ease-out;
}

.pin-selected .pin-pulse {
  border-color: #f59e0b;
  display: block;
}

@keyframes radar-pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.6);
    opacity: 0.9;
  }

  100% {
    transform: translate(-50%, -50%) scale(1.8);
    opacity: 0;
  }
}

.pin-label {
  margin-top: 4px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  padding: 2px 8px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
  white-space: nowrap;
}

.label-name {
  font-size: 11px;
  color: #f8fafc;
  font-weight: 600;
}

.label-coords {
  font-size: 9px;
  color: #94a3b8;
  font-family: monospace;
}

/* 底部操作小贴士 */
.canvas-tips {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 6px 16px;
  border-radius: 20px;
  color: #94a3b8;
  font-size: 12px;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

/* 无底图空状态 */
.no-map-placeholder {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
}

.no-map-desc {
  max-width: 480px;
  line-height: 1.6;
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
