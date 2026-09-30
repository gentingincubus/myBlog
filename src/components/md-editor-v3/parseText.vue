<script setup>
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  theme: {
    type: String,
    default: 'dark' // 'dark' | 'light'
  },
  previewTheme: {
    type: String,
    default: 'default' // 'default' | 'github' | 'vuepress' | 'mk-cute' | 'smart-blue' | 'cyanosis'
  }
})

const displayText = computed(() => props.text || '')
</script>

<template>
  <div class="custom-parse-text" :class="[theme === 'dark' ? 'theme-dark' : 'theme-light']">
    <MdPreview
      :model-value="displayText"
      :theme="theme"
      :preview-theme="previewTheme"
      style="background-color: transparent;"
    />
  </div>
</template>

<style scoped>
.custom-parse-text {
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.7;
}

:deep(.md-editor-preview) {
  padding: 8px 12px;
  background-color: transparent !important;
  color: inherit;
}

/* 深色模式下的文字与元素微调，适应毛玻璃暗调背景 */
.theme-dark :deep(.md-editor-preview) {
  color: #e2e8f0;
}

.theme-dark :deep(h1),
.theme-dark :deep(h2),
.theme-dark :deep(h3),
.theme-dark :deep(h4) {
  color: #f8fafc;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  padding-bottom: 6px;
  margin-top: 16px;
  margin-bottom: 10px;
}

.theme-dark :deep(blockquote) {
  background: rgba(255, 255, 255, 0.05);
  border-left: 4px solid #38bdf8;
  color: #94a3b8;
  padding: 8px 14px;
  border-radius: 4px;
  margin: 12px 0;
}

.theme-dark :deep(code) {
  background: rgba(255, 255, 255, 0.1);
  color: #38bdf8;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.theme-dark :deep(pre) {
  background: rgba(15, 23, 42, 0.85) !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.theme-dark :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  margin: 8px 0;
}

.theme-dark :deep(hr) {
  border-color: rgba(255, 255, 255, 0.1);
}

.theme-dark :deep(a) {
  color: #38bdf8;
  text-decoration: underline;
}

.theme-dark :deep(ul),
.theme-dark :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}
</style>
