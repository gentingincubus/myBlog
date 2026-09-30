<script setup>
import { ref, watch } from 'vue'
import { MdEditor } from 'md-editor-v3'
import { ElMessage } from 'element-plus'
import { uploadImageApi } from '@/api/upload'
import 'md-editor-v3/lib/style.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: String,
    default: '460px'
  },
  placeholder: {
    type: String,
    default: '请输入 Markdown 富文本内容，支持截图粘贴、本地图片拖拽与上传...'
  },
  theme: {
    type: String,
    default: 'light'
  },
  previewTheme: {
    type: String,
    default: 'default'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'save'])

const content = ref(props.modelValue || '')

watch(() => props.modelValue, (val) => {
  if (val !== content.value) {
    content.value = val || ''
  }
})

function handleChange(val) {
  emit('update:modelValue', val)
  emit('change', val)
}

function handleSave(val) {
  emit('save', val)
}

/**
 * 🌟 核心：富文本图片上传事件，直连 Cloudflare R2 的 richtext/images/ 专用文件夹
 */
async function handleUploadImg(files, callback) {
  if (!files || files.length === 0) return

  try {
    const uploadPromises = Array.from(files).map(async (file) => {
      // 校验文件格式
      const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/svg+xml']
      if (!validTypes.includes(file.type)) {
        ElMessage.error(`不支持的文件类型: ${file.name}`)
        throw new Error('文件类型不支持')
      }
      // 校验大小 (10MB)
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.error(`文件 ${file.name} 超过 10MB 限制`)
        throw new Error('文件过大')
      }

      // 上传到 Cloudflare R2 的 richtext/images/ 专用目录
      const res = await uploadImageApi(file, 'rich_text_image')
      if (res && res.data) {
        return res.data
      }
      throw new Error('上传返回数据为空')
    })

    const urls = await Promise.all(uploadPromises)
    callback(urls)
    ElMessage.success(`成功上传 ${urls.length} 张图片到 Cloudflare R2`)
  } catch (err) {
    console.error('富文本插图上传失败', err)
    ElMessage.error('图片上传失败，请检查网络或配置')
  }
}

// 常用富文本工具栏配置
const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  '-',
  'title',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  'revoke',
  'next',
  '=',
  'pageFullscreen',
  'fullscreen',
  'preview',
  'catalog'
]
</script>

<template>
  <div class="custom-md-editor">
    <MdEditor
      v-model="content"
      :style="{ height: height }"
      :theme="theme"
      :preview-theme="previewTheme"
      :toolbars="toolbars"
      :placeholder="placeholder"
      :disabled="disabled"
      @on-change="handleChange"
      @on-save="handleSave"
      @on-upload-img="handleUploadImg"
    />
  </div>
</template>

<style scoped>
.custom-md-editor {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

:deep(.md-editor) {
  border-radius: 8px;
  border-color: #e2e8f0;
}
</style>
