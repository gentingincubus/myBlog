<template>
  <div class="storage-manage-container">
    <!-- 1. 顶部存储看板指标卡片 -->
    <el-row :gutter="16" class="stats-row">
      <!-- 卡片 1: 存储容量与免费配额进度 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card quota-card">
          <div class="stat-header">
            <span class="stat-title">Cloudflare R2 存储容量</span>
            <el-tag size="small" type="primary" effect="plain">{{ stats.bucketName || 'myblog-vr' }}</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-number">
              {{ stats.totalSizeFormatted || '0 B' }}
              <span class="stat-sub">/ 10 GB 免费额度</span>
            </div>
            <div class="progress-box">
              <el-progress
                :percentage="quotaPercent"
                :status="progressStatus"
                :stroke-width="10"
                :text-inside="false"
              />
            </div>
            <div class="stat-desc">
              <span>域名: {{ publicDomain }}</span>
              <el-link type="primary" :underline="false" class="copy-link" @click="copyText(stats.publicUrl)">复制域名</el-link>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 卡片 2: 存储文件总数与健康度 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">对象文件总数</span>
            <el-tag size="small" type="success" effect="plain">正常在用: {{ stats.activeCount || 0 }}</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-number">
              {{ stats.totalFileCount || 0 }}
              <span class="stat-unit">个文件</span>
            </div>
            <div class="active-ratio-bar">
              <span class="ratio-text">健康引用率: {{ healthRate }}%</span>
            </div>
            <div class="stat-desc">
              <span>覆盖全景原图、预览图、园区底图与轮播图</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 卡片 3: 孤儿垃圾文件与一键清理入口 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card orphan-card" :class="{ 'has-orphan': (stats.orphanCount || 0) > 0 }">
          <div class="stat-header">
            <span class="stat-title">孤儿废弃文件 (待清理)</span>
            <el-tag :type="(stats.orphanCount || 0) > 0 ? 'danger' : 'info'" size="small">
              {{ (stats.orphanCount || 0) > 0 ? '需清理' : '纯净状态' }}
            </el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-number orphan-number">
              {{ stats.orphanCount || 0 }}
              <span class="stat-sub">个 (约 {{ stats.orphanSizeFormatted || '0 B' }})</span>
            </div>
            <div class="orphan-action-box">
              <el-button
                type="danger"
                size="small"
                icon="Delete"
                v-hasPermi="['system:storage:clean']"
                :disabled="!stats.orphanCount || stats.orphanCount <= 0"
                :loading="cleaning"
                @click="handleCleanOrphans"
              >
                一键清理孤儿废图
              </el-button>
            </div>
            <div class="stat-desc warning-desc">
              <span>* 测试残留、场景覆盖替换或已删场景的云端文件</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 文件列表与工具栏 -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-form :inline="true" :model="queryForm" class="filter-form" @submit.prevent>
            <el-form-item label="业务分类">
              <el-select
                v-model="queryForm.prefix"
                placeholder="全部业务目录"
                clearable
                style="width: 180px"
                @change="handleFilterChange"
              >
                <el-option label="全部目录" value="" />
                <el-option label="VR全景原图 (panoramas/)" value="vr/panoramas/" />
                <el-option label="VR低清预览 (previews/)" value="vr/previews/" />
                <el-option label="VR补地Logo (nadirs/)" value="vr/nadirs/" />
                <el-option label="园区地图底图 (maps/)" value="vr/maps/" />
                <el-option label="门户轮播图 (carousel/)" value="carousel/" />
                <el-option label="用户头像 (avatar/)" value="avatar/" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-checkbox
                v-model="queryForm.onlyOrphan"
                border
                @change="handleFilterChange"
              >
                仅看孤儿垃圾文件
              </el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" icon="Refresh" :loading="loading" @click="loadData">
                体检扫描 / 刷新
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-right">
          <el-button
            type="danger"
            plain
            icon="Delete"
            v-hasPermi="['system:storage:clean']"
            :disabled="!stats.orphanCount || stats.orphanCount <= 0"
            :loading="cleaning"
            @click="handleCleanOrphans"
          >
            一键清理所有孤儿 ({{ stats.orphanCount || 0 }})
          </el-button>
        </div>
      </div>

      <!-- 文件数据表格 -->
      <el-table
        v-loading="loading"
        :data="pagedFiles"
        border
        stripe
        style="width: 100%"
        class="storage-table"
      >
        <!-- 缩略预览 -->
        <el-table-column label="文件预览" width="96" align="center">
          <template #default="{ row }">
            <div class="preview-cell">
              <!-- 大图流量保护：>= 1MB (1048576 字节) 时不主动发起网络加载，点击再全屏预览 -->
              <el-tooltip
                v-if="isImageFile(row.key) && isLargeFile(row.size)"
                :content="`大文件 (${row.sizeFormatted})，已启用防刷流与性能保护，点击查看大图`"
                placement="top"
              >
                <div class="large-file-placeholder" @click="openViewer(row.url)">
                  <el-icon class="placeholder-icon"><PictureFilled /></el-icon>
                  <span class="placeholder-text">大图</span>
                  <span class="placeholder-badge">防刷流</span>
                </div>
              </el-tooltip>

              <!-- 小于 1MB 的轻量图片：正常懒加载缩略图 -->
              <el-image
                v-else-if="isImageFile(row.key)"
                :src="row.url"
                :preview-src-list="[row.url]"
                fit="cover"
                class="thumb-image"
                preview-teleported
                loading="lazy"
              >
                <template #error>
                  <div class="image-slot-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>

              <!-- 非图片文件 (如配置、文本等) -->
              <el-icon v-else :size="24" class="file-icon"><Document /></el-icon>
            </div>
          </template>
        </el-table-column>

        <!-- 对象 Key 路径 -->
        <el-table-column label="存储对象 Key / 路径" min-width="260">
          <template #default="{ row }">
            <div class="key-cell">
              <span class="file-key" :title="row.key">{{ row.key }}</span>
              <el-tooltip content="复制对象 Key" placement="top">
                <el-button link type="primary" icon="CopyDocument" size="small" @click="copyText(row.key)" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 所属业务 -->
        <el-table-column label="业务类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getBusinessTypeTagType(row.businessType)">
              {{ row.businessType || '其他文件' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 文件大小 -->
        <el-table-column prop="sizeFormatted" label="大小" width="110" align="center" sortable :sort-method="sortSizeMethod">
          <template #default="{ row }">
            <span class="font-mono">{{ row.sizeFormatted }}</span>
          </template>
        </el-table-column>

        <!-- 引用状态 (是否为孤儿) -->
        <el-table-column label="引用状态" min-width="190">
          <template #default="{ row }">
            <div class="ref-status-cell">
              <el-tag v-if="row.isOrphan" type="danger" effect="dark" size="small">
                孤儿废图 (未引用)
              </el-tag>
              <div v-else class="ref-active-box">
                <el-tag type="success" effect="plain" size="small">正常引用</el-tag>
                <span v-if="row.referencedBy" class="ref-detail-text" :title="row.referencedBy">
                  {{ row.referencedBy }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 最后修改时间 -->
        <el-table-column prop="lastModified" label="修改时间" width="170" align="center" sortable>
          <template #default="{ row }">
            <span class="time-text">{{ row.lastModified || '-' }}</span>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isImageFile(row.key)"
              link
              type="primary"
              icon="View"
              @click="openViewer(row.url)"
            >
              预览
            </el-button>
            <el-button link type="primary" icon="Link" @click="copyText(row.url)">
              复制链接
            </el-button>
            <el-button
              link
              type="danger"
              icon="Delete"
              v-hasPermi="['system:storage:delete']"
              @click="handleDeleteFile(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredFiles.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 🌟 全屏大图预览查看器 (按需触发，打开时才拉取网络大图，平时 0 流量消耗) -->
    <el-image-viewer
      v-if="isViewerOpen"
      :url-list="[previewViewerUrl]"
      teleported
      @close="isViewerOpen = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import {
  getStorageStatsApi,
  listStorageFilesApi,
  deleteStorageFileApi,
  cleanOrphanFilesApi
} from '@/api/storage'

// 统计数据
const stats = ref({
  bucketName: '',
  publicUrl: '',
  totalFileCount: 0,
  totalSizeBytes: 0,
  totalSizeFormatted: '0 B',
  freeQuotaBytes: 10737418240,
  usagePercent: 0,
  orphanCount: 0,
  orphanSizeBytes: 0,
  orphanSizeFormatted: '0 B',
  activeCount: 0
})

// 表格数据与状态
const loading = ref(false)
const cleaning = ref(false)
const fileList = ref([])

// 筛选条件
const queryForm = reactive({
  prefix: '',
  onlyOrphan: false
})

// 本地分页
const currentPage = ref(1)
const pageSize = ref(20)

// 配额使用百分比 (保留 2 位小数)
const quotaPercent = computed(() => {
  const p = stats.value.usagePercent || 0
  return Number(Math.min(p, 100).toFixed(2))
})

// 配额进度条状态颜色
const progressStatus = computed(() => {
  if (quotaPercent.value > 90) return 'exception'
  if (quotaPercent.value > 70) return 'warning'
  return 'primary'
})

// 公开访问域名提取
const publicDomain = computed(() => {
  const url = stats.value.publicUrl || ''
  return url.replace(/^https?:\/\//, '')
})

// 健康引用比例
const healthRate = computed(() => {
  const total = stats.value.totalFileCount || 0
  if (total === 0) return 100
  const active = stats.value.activeCount || 0
  return Math.round((active / total) * 100)
})

// 筛选过滤后的文件列表
const filteredFiles = computed(() => {
  return fileList.value.filter(file => {
    if (queryForm.onlyOrphan && !file.isOrphan) {
      return false
    }
    if (queryForm.prefix && !file.key.startsWith(queryForm.prefix)) {
      return false
    }
    return true
  })
})

// 分页切片数据
const pagedFiles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredFiles.value.slice(start, start + pageSize.value)
})

// 🌟 流量优化阈值：超过 1MB (1048576 字节) 的大图默认不加载缩略图，避免并发请求原图耗费流量与造成卡顿
const LARGE_FILE_THRESHOLD = 1048576

// 全屏大图预览查看器状态
const isViewerOpen = ref(false)
const previewViewerUrl = ref('')

/**
 * 判断文件是否为超过 1MB 的大文件
 */
function isLargeFile(size) {
  return (size || 0) >= LARGE_FILE_THRESHOLD
}

/**
 * 打开全屏大图预览
 */
function openViewer(url) {
  if (!url) return
  previewViewerUrl.value = url
  isViewerOpen.value = true
}

/**
 * 排序文件大小
 */
function sortSizeMethod(a, b) {
  return (a.size || 0) - (b.size || 0)
}

/**
 * 判断是否为图片
 */
function isImageFile(key = '') {
  const lower = key.toLowerCase()
  return (
    lower.endsWith('.jpg') ||
    lower.endsWith('.jpeg') ||
    lower.endsWith('.png') ||
    lower.endsWith('.webp') ||
    lower.endsWith('.gif') ||
    lower.endsWith('.svg')
  )
}

/**
 * 业务分类标签颜色
 */
function getBusinessTypeTagType(type) {
  switch (type) {
    case '全景原图':
      return 'primary'
    case '全景低清图':
      return 'success'
    case '补地遮罩Logo':
      return 'warning'
    case '园区地图':
      return 'warning'
    case '轮播图':
      return 'info'
    case '用户头像':
      return ''
    default:
      return 'info'
  }
}

/**
 * 复制文本工具
 */
async function copyText(text) {
  if (!text) return
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
    } else {
      const input = document.createElement('textarea')
      input.value = text
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}

/**
 * 加载数据 (统计指标 + 文件列表)
 */
async function loadData() {
  loading.value = true
  try {
    const [statsRes, filesRes] = await Promise.all([
      getStorageStatsApi(),
      listStorageFilesApi({
        prefix: queryForm.prefix || undefined,
        onlyOrphan: queryForm.onlyOrphan || undefined
      })
    ])

    if (statsRes && statsRes.data) {
      stats.value = statsRes.data
    }

    if (filesRes && filesRes.data) {
      fileList.value = filesRes.data || []
    }
  } catch (e) {
    console.error('加载存储数据失败:', e)
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  currentPage.value = 1
  loadData()
}

function handleSizeChange(val) {
  pageSize.value = val
  currentPage.value = 1
}

function handleCurrentChange(val) {
  currentPage.value = val
}

/**
 * 一键清理所有孤儿垃圾文件
 */
async function handleCleanOrphans() {
  const orphanCount = stats.value.orphanCount || 0
  const orphanSize = stats.value.orphanSizeFormatted || '0 B'

  if (orphanCount <= 0) {
    ElMessage.info('当前存储桶内暂无孤儿垃圾文件，无需清理')
    return
  }

  try {
    await ElMessageBox.confirm(
      `检测到当前存储桶中存在 <strong>${orphanCount}</strong> 个未被引用的孤儿垃圾文件，预计释放 <strong>${orphanSize}</strong> 存储空间。<br><br><span style="color: #f56c6c;">警告：此操作将直接向 Cloudflare R2 发送物理删除指令，文件不可恢复。是否立即执行清理？</span>`,
      '确认清理孤儿废图',
      {
        confirmButtonText: '立即物理清理',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        dangerouslyUseHTMLString: true,
        type: 'warning'
      }
    )

    cleaning.value = true
    const res = await cleanOrphanFilesApi()
    if (res && res.data) {
      const data = res.data
      ElNotification({
        title: '孤儿垃圾清理完成',
        message: `成功清理 ${data.cleanedCount} 个文件，释放存储空间 ${data.freedSizeFormatted}！`,
        type: 'success',
        duration: 5000
      })
      await loadData()
    }
  } catch (action) {
    if (action !== 'cancel') {
      console.error('清理孤儿文件失败:', action)
    }
  } finally {
    cleaning.value = false
  }
}

/**
 * 手动删除单个文件
 */
async function handleDeleteFile(row) {
  try {
    await ElMessageBox.confirm(
      `确定要从 Cloudflare R2 物理删除以下文件吗？<br><br><code style="word-break: break-all;">${row.key}</code><br><br><span style="color: #f56c6c;">删除后公网 CDN 链接将失效且无法恢复。</span>`,
      '物理删除警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        dangerouslyUseHTMLString: true,
        type: 'error'
      }
    )

    const res = await deleteStorageFileApi(row.url)
    if (res && res.data) {
      ElMessage.success('文件已物理删除')
      await loadData()
    }
  } catch (action) {
    if (action !== 'cancel') {
      console.error('删除文件失败:', action)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.storage-manage-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 顶部指标卡片 */
.stats-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.25s ease;
  height: 100%;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.stat-sub {
  font-size: 13px;
  font-weight: normal;
  color: #64748b;
  margin-left: 6px;
}

.stat-unit {
  font-size: 14px;
  font-weight: normal;
  color: #64748b;
  margin-left: 4px;
}

.progress-box {
  margin: 4px 0;
}

.stat-desc {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  word-break: break-all;
}

.copy-link {
  font-size: 12px;
  margin-left: 8px;
  flex-shrink: 0;
}

.active-ratio-bar {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #10b981;
  font-weight: 600;
}

.orphan-card.has-orphan {
  border-color: #fca5a5;
  background: linear-gradient(180deg, #fff 0%, #fff5f5 100%);
}

.orphan-number {
  color: #ef4444;
}

.orphan-action-box {
  margin-top: 2px;
}

.warning-desc {
  color: #f87171;
}

/* 工具栏与表格 */
.table-card {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.filter-form {
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.storage-table {
  border-radius: 6px;
  overflow: hidden;
}

.preview-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 🌟 大图防刷流占位卡片样式 */
.large-file-placeholder {
  width: 54px;
  height: 52px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  padding: 2px;
  box-sizing: border-box;
}

.large-file-placeholder:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.15);
}

.placeholder-icon {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 2px;
  transition: color 0.2s;
}

.large-file-placeholder:hover .placeholder-icon {
  color: #2563eb;
}

.placeholder-text {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  line-height: 1.1;
  transition: color 0.2s;
}

.large-file-placeholder:hover .placeholder-text {
  color: #2563eb;
}

.placeholder-badge {
  font-size: 9px;
  color: #f59e0b;
  font-weight: 700;
  transform: scale(0.85);
  line-height: 1;
  margin-top: 1px;
}

.thumb-image {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.image-slot-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background: #f1f5f9;
  color: #94a3b8;
  border-radius: 6px;
}

.file-icon {
  color: #64748b;
}

.key-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.file-key {
  font-family: monospace;
  font-size: 13px;
  color: #1e293b;
  word-break: break-all;
}

.font-mono {
  font-family: monospace;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.ref-status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ref-active-box {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.ref-detail-text {
  font-size: 12px;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}

.time-text {
  font-size: 12px;
  color: #64748b;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
