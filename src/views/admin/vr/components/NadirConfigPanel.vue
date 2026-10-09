<template>
  <div class="nadir-config-section">
    <div class="nadir-header-row">
      <div class="header-left">
        <el-icon class="nadir-icon"><Compass /></el-icon>
        <span class="nadir-title">{{ title }}</span>
        <el-tooltip :content="tipText" placement="top">
          <el-icon class="tip-icon"><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>

      <div class="header-right">
        <!-- 如果是场景级，提供是否继承园区配置开关 -->
        <el-radio-group v-if="isSceneLevel" v-model="localConfig.overrideMode" size="small" @change="emitChange">
          <el-radio-button :value="'inherit'">继承园区配置</el-radio-button>
          <el-radio-button :value="'custom'">场景单独定制</el-radio-button>
          <el-radio-button :value="'disabled'">不显示补地</el-radio-button>
        </el-radio-group>

        <!-- 如果是园区分类级，提供开启/关闭开关 -->
        <el-switch
          v-else
          v-model="localConfig.nadirEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="启用补地"
          inactive-text="关闭"
          @change="emitChange"
        />
      </div>
    </div>

    <!-- 展开配置面板（仅在需要配置时展示） -->
    <div v-if="shouldShowForm" class="nadir-body-content">
      <!-- 表单配置区 -->
      <div class="form-section">
        <!-- 第一行：补地类型 -->
        <el-form-item label="补地类型" label-width="85px">
          <el-radio-group v-model="localConfig.type" size="default" @change="onConfigUpdate">
            <el-radio-button value="stamp">动态罗盘印章</el-radio-button>
            <el-radio-button value="image">自定义图片Logo</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 第二行：遮罩半径调节 -->
        <el-form-item label="遮罩半径" label-width="85px">
          <div class="slider-row">
            <el-slider
              v-model="localConfig.nadirRadius"
              :min="8"
              :max="30"
              :step="0.5"
              style="flex: 1;"
              @input="onConfigUpdate"
            />
            <span class="radius-value-badge">{{ localConfig.nadirRadius }}</span>
          </div>
          <div class="form-subtip">
            三脚架展开范围大时可调大（推荐：常规 14 ~ 18，大脚架 19 ~ 25）
          </div>
        </el-form-item>

        <!-- 动态印章专属选项 -->
        <template v-if="localConfig.type === 'stamp'">
          <!-- 环绕主文案 -->
          <el-form-item label="环绕主文案" label-width="85px">
            <el-input
              v-model="localConfig.nadirText"
              placeholder="如：genting拍摄 / 顺峰山公园"
              maxlength="20"
              clearable
              @input="onConfigUpdate"
            />
          </el-form-item>

          <!-- 中心文字与底部小标 并排 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="中心文字" label-width="85px">
                <el-input
                  v-model="localConfig.nadirCenterText"
                  placeholder="如：720° / VR"
                  maxlength="8"
                  clearable
                  @input="onConfigUpdate"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="底部小标" label-width="85px">
                <el-input
                  v-model="localConfig.nadirSubText"
                  placeholder="如：720° SPATIAL PANORAMA"
                  maxlength="25"
                  clearable
                  @input="onConfigUpdate"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 颜色调配 并排 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="背景颜色" label-width="85px">
                <div class="color-picker-cell">
                  <el-color-picker
                    v-model="localConfig.nadirBgColor"
                    show-alpha
                    @change="onConfigUpdate"
                  />
                  <span class="color-hex-tag">{{ localConfig.nadirBgColor || 'rgba(11, 19, 41, 0.90)' }}</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="文字/线条" label-width="85px">
                <div class="color-picker-cell">
                  <el-color-picker
                    v-model="localConfig.nadirTextColor"
                    @change="onConfigUpdate"
                  />
                  <span class="color-hex-tag">{{ localConfig.nadirTextColor || '#38bdf8' }}</span>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 自定义图片上传 -->
        <template v-else>
          <el-form-item label="图片Logo" label-width="85px">
            <div class="image-uploader-box">
              <div v-if="localConfig.nadirImageUrl" class="image-preview-wrap">
                <img :src="localConfig.nadirImageUrl" class="uploaded-img" />
                <div class="del-btn" @click="localConfig.nadirImageUrl = ''; onConfigUpdate()">
                  <el-icon><Delete /></el-icon>
                  <span>移除重新上传</span>
                </div>
              </div>
              <el-upload
                v-else
                class="img-drop"
                drag
                :show-file-list="false"
                :http-request="handleCustomImageUpload"
                :disabled="uploadingImage"
                accept="image/*"
              >
                <div
                  class="upload-drop-inner"
                  v-loading="uploadingImage"
                  element-loading-text="正在上传..."
                  element-loading-background="rgba(255, 255, 255, 0.9)"
                >
                  <template v-if="!uploadingImage">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-tip">点击或拖拽上传圆形/方形透明 PNG</div>
                    <div class="upload-subtip">建议尺寸 512x512 或以上</div>
                  </template>
                </div>
              </el-upload>
            </div>
          </el-form-item>
        </template>
      </div>

      <!-- 下方：独立实时预览大卡片 -->
      <div class="preview-bottom-section">
        <div class="preview-header-bar">
          <div class="preview-title">
            <el-icon><View /></el-icon>
            <span>脚底实时印章效果预览</span>
          </div>
          <span class="preview-sub-badge">
            当前半径: <strong>{{ localConfig.nadirRadius }}</strong>（水平贴合于全景球脚下）
          </span>
        </div>

        <div class="preview-stage-container">
          <div class="preview-canvas-wrap">
            <canvas ref="previewCanvasRef" class="preview-canvas" width="220" height="220"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { Compass, QuestionFilled, Plus, Delete, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { vrApi } from '@/api/vr'

const props = defineProps({
  // 传入的 JSON 字符串或对象
  modelValue: {
    type: [String, Object],
    default: ''
  },
  // 是否为场景点位级 (true: 场景级支持继承/定制/关闭; false: 园区分类级)
  isSceneLevel: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '三脚架补地遮罩设置 (Nadir Patch)'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const previewCanvasRef = ref(null)
const uploadingImage = ref(false)

const localConfig = reactive({
  overrideMode: 'inherit', // 'inherit' | 'custom' | 'disabled' (仅场景级生效)
  nadirEnabled: 1,         // 1: 启用, 0: 禁用 (仅分类级生效)
  type: 'stamp',           // 'stamp' | 'image'
  nadirRadius: 16,         // 默认半径适度放大到 16，遮蔽三脚架更稳固
  nadirText: 'genting拍摄',
  nadirSubText: '720° SPATIAL PANORAMA',
  nadirCenterText: '720°',
  nadirBgColor: 'rgba(11, 19, 41, 0.90)',
  nadirTextColor: '#38bdf8',
  nadirBorderColor: '#38bdf8',
  nadirImageUrl: ''
})

const tipText = computed(() => {
  return props.isSceneLevel
    ? '若当前全景三脚架范围较大或有特殊定制需求，可选择场景单独定制；默认自动继承当前园区分类的补地配置。'
    : '园区全局补地：该园区分类下的所有全景将默认应用此印章或图片，一劳永逸遮盖三脚架穿帮。'
})

const shouldShowForm = computed(() => {
  if (props.isSceneLevel) {
    return localConfig.overrideMode === 'custom'
  }
  return localConfig.nadirEnabled === 1
})

// 解析传入的 modelValue
function parseValue(val) {
  if (!val) {
    if (props.isSceneLevel) {
      localConfig.overrideMode = 'inherit'
    } else {
      localConfig.nadirEnabled = 1
    }
    return
  }

  let obj = val
  if (typeof val === 'string') {
    try {
      obj = JSON.parse(val)
    } catch (e) {
      obj = {}
    }
  }

  if (props.isSceneLevel) {
    if (obj.nadirEnabled === 0 || obj.nadirEnabled === false) {
      localConfig.overrideMode = 'disabled'
    } else if (obj.overrideMode === 'custom' || obj.isCustom) {
      localConfig.overrideMode = 'custom'
    } else {
      localConfig.overrideMode = obj.overrideMode || 'inherit'
    }
  } else {
    localConfig.nadirEnabled = obj.nadirEnabled !== undefined ? obj.nadirEnabled : 1
  }

  localConfig.type = obj.type || (obj.nadirImageUrl ? 'image' : 'stamp')
  localConfig.nadirRadius = obj.nadirRadius !== undefined ? Number(obj.nadirRadius) : 16
  localConfig.nadirText = obj.nadirText !== undefined ? obj.nadirText : 'genting拍摄'
  localConfig.nadirSubText = obj.nadirSubText !== undefined ? obj.nadirSubText : '720° SPATIAL PANORAMA'
  localConfig.nadirCenterText = obj.nadirCenterText !== undefined ? obj.nadirCenterText : '720°'
  localConfig.nadirBgColor = obj.nadirBgColor || 'rgba(11, 19, 41, 0.90)'
  localConfig.nadirTextColor = obj.nadirTextColor || '#38bdf8'
  localConfig.nadirBorderColor = obj.nadirBorderColor || '#38bdf8'
  localConfig.nadirImageUrl = obj.nadirImageUrl || ''
}

watch(
  () => props.modelValue,
  (newVal) => {
    parseValue(newVal)
    nextTick(renderPreview)
  },
  { immediate: true }
)

function onConfigUpdate() {
  localConfig.nadirBorderColor = localConfig.nadirTextColor
  emitChange()
  renderPreview()
}

function emitChange() {
  let resultJson = ''

  if (props.isSceneLevel) {
    if (localConfig.overrideMode === 'inherit') {
      resultJson = '' // 继承留空
    } else if (localConfig.overrideMode === 'disabled') {
      resultJson = JSON.stringify({ nadirEnabled: 0 })
    } else {
      resultJson = JSON.stringify({
        overrideMode: 'custom',
        nadirEnabled: 1,
        type: localConfig.type,
        nadirRadius: localConfig.nadirRadius,
        nadirText: localConfig.nadirText,
        nadirSubText: localConfig.nadirSubText,
        nadirCenterText: localConfig.nadirCenterText,
        nadirBgColor: localConfig.nadirBgColor,
        nadirTextColor: localConfig.nadirTextColor,
        nadirBorderColor: localConfig.nadirBorderColor,
        nadirImageUrl: localConfig.nadirImageUrl
      })
    }
  } else {
    resultJson = JSON.stringify({
      nadirEnabled: localConfig.nadirEnabled,
      type: localConfig.type,
      nadirRadius: localConfig.nadirRadius,
      nadirText: localConfig.nadirText,
      nadirSubText: localConfig.nadirSubText,
      nadirCenterText: localConfig.nadirCenterText,
      nadirBgColor: localConfig.nadirBgColor,
      nadirTextColor: localConfig.nadirTextColor,
      nadirBorderColor: localConfig.nadirBorderColor,
      nadirImageUrl: localConfig.nadirImageUrl
    })
  }

  emit('update:modelValue', resultJson)
  emit('change', resultJson)
}

// 图片上传处理
async function handleCustomImageUpload(options) {
  if (uploadingImage.value) return
  uploadingImage.value = true
  try {
    const res = await vrApi.uploadImage(options.file, 'vr_nadir')
    if (res.data) {
      localConfig.nadirImageUrl = res.data
      onConfigUpdate()
      ElMessage.success('补地图片已成功上传至 Cloudflare R2！')
    }
  } catch (err) {
    console.error('上传补地图片失败:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || '图片上传失败，请重试')
  } finally {
    uploadingImage.value = false
  }
}

// 绘制实时印章预览
function renderPreview() {
  const canvas = previewCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const size = canvas.width
  ctx.clearRect(0, 0, size, size)

  const center = size / 2
  const outerRadius = size * 0.44
  const innerRadius = size * 0.36
  const centerRingRadius = size * 0.2

  if (localConfig.type === 'image' && localConfig.nadirImageUrl) {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      ctx.save()
      ctx.beginPath()
      ctx.arc(center, center, outerRadius, 0, Math.PI * 2)
      ctx.clip()
      ctx.drawImage(img, center - outerRadius, center - outerRadius, outerRadius * 2, outerRadius * 2)
      ctx.restore()
    }
    img.src = localConfig.nadirImageUrl
    return
  }

  // 1. 底盘
  ctx.save()
  ctx.beginPath()
  ctx.arc(center, center, outerRadius, 0, Math.PI * 2)
  ctx.fillStyle = localConfig.nadirBgColor || 'rgba(11, 19, 41, 0.90)'
  ctx.fill()
  ctx.lineWidth = 3
  ctx.strokeStyle = localConfig.nadirTextColor || '#38bdf8'
  ctx.stroke()
  ctx.restore()

  // 2. 双同心环
  ctx.save()
  ctx.beginPath()
  ctx.arc(center, center, innerRadius, 0, Math.PI * 2)
  ctx.lineWidth = 1.2
  ctx.strokeStyle = localConfig.nadirTextColor || '#38bdf8'
  ctx.setLineDash([4, 3])
  ctx.stroke()
  ctx.restore()

  // 3. 上部弧形文字
  const text = localConfig.nadirText || 'genting拍摄'
  if (text) {
    ctx.save()
    ctx.fillStyle = localConfig.nadirTextColor || '#38bdf8'
    ctx.font = 'bold 15px "PingFang SC", "Microsoft YaHei", sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'

    const chars = text.split('')
    const arcRadius = (outerRadius + innerRadius) / 2
    const charSpacing = 0.22
    const totalArc = (chars.length - 1) * charSpacing
    const startAngle = -Math.PI / 2 - totalArc / 2

    chars.forEach((char, i) => {
      const angle = startAngle + i * charSpacing
      ctx.save()
      ctx.translate(center + Math.cos(angle) * arcRadius, center + Math.sin(angle) * arcRadius)
      ctx.rotate(angle + Math.PI / 2)
      ctx.fillText(char, 0, 0)
      ctx.restore()
    })
    ctx.restore()
  }

  // 4. 下部小字
  const subText = localConfig.nadirSubText || '720° SPATIAL PANORAMA'
  if (subText) {
    ctx.save()
    ctx.fillStyle = localConfig.nadirTextColor || '#38bdf8'
    ctx.font = '600 8.5px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'

    const subChars = subText.split('')
    const subRadius = innerRadius * 0.88
    const subSpacing = 0.12
    const totalSubArc = (subChars.length - 1) * subSpacing
    const startSubAngle = Math.PI / 2 + totalSubArc / 2

    subChars.forEach((char, i) => {
      const angle = startSubAngle - i * subSpacing
      ctx.save()
      ctx.translate(center + Math.cos(angle) * subRadius, center + Math.sin(angle) * subRadius)
      ctx.rotate(angle - Math.PI / 2)
      ctx.fillText(char, 0, 0)
      ctx.restore()
    })
    ctx.restore()
  }

  // 5. 中心罗盘与刻度
  ctx.save()
  ctx.beginPath()
  ctx.arc(center, center, centerRingRadius, 0, Math.PI * 2)
  ctx.lineWidth = 1.5
  ctx.strokeStyle = localConfig.nadirTextColor || '#38bdf8'
  ctx.stroke()

  ctx.beginPath()
  ctx.moveTo(center - centerRingRadius * 0.9, center)
  ctx.lineTo(center - centerRingRadius * 0.55, center)
  ctx.moveTo(center + centerRingRadius * 0.55, center)
  ctx.lineTo(center + centerRingRadius * 0.9, center)
  ctx.moveTo(center, center - centerRingRadius * 0.9)
  ctx.lineTo(center, center - centerRingRadius * 0.55)
  ctx.moveTo(center, center + centerRingRadius * 0.55)
  ctx.lineTo(center, center + centerRingRadius * 0.9)
  ctx.stroke()

  const centerText = localConfig.nadirCenterText || '720°'
  ctx.fillStyle = localConfig.nadirTextColor || '#38bdf8'
  ctx.font = 'bold 18px sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(centerText, center, center)
  ctx.restore()
}

onMounted(() => {
  nextTick(renderPreview)
})
</script>

<style scoped>
.nadir-config-section {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 20px;
}

.nadir-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nadir-icon {
  font-size: 18px;
  color: #0284c7;
}

.nadir-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.tip-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.nadir-body-content {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #cbd5e1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  width: 100%;
}

.slider-row {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
}

.radius-value-badge {
  background: #e0f2fe;
  color: #0284c7;
  font-size: 13px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
}

.form-subtip {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
  line-height: 1.4;
}

.color-picker-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-hex-tag {
  font-size: 12px;
  color: #475569;
  font-family: monospace;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

/* 下方独立预览卡片 */
.preview-bottom-section {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.preview-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.preview-sub-badge {
  font-size: 12px;
  color: #64748b;
}

.preview-sub-badge strong {
  color: #0284c7;
}

.preview-stage-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 14px 0 6px;
}

.preview-canvas-wrap {
  width: 220px;
  height: 220px;
  background: radial-gradient(circle at center, #1e293b 0%, #0f172a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15), inset 0 0 16px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(56, 189, 248, 0.2);
}

.preview-canvas {
  width: 200px;
  height: 200px;
}

.image-preview-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.uploaded-img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #cbd5e1;
}

.del-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #ef4444;
  font-size: 12px;
  cursor: pointer;
}

.image-uploader-box {
  width: 100%;
}

.img-drop :deep(.el-upload) {
  display: block;
}

.img-drop :deep(.el-upload-dragger) {
  padding: 16px;
  min-height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.upload-drop-inner {
  width: 100%;
  height: 100%;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-drop-inner .upload-icon {
  font-size: 24px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.upload-drop-inner .upload-tip {
  font-size: 12px;
  color: #475569;
  font-weight: 500;
}

.upload-drop-inner .upload-subtip {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
