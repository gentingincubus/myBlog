<template>
  <div class="vr-category-manage-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索与操作栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent>
          <el-form-item label="园区/分类名称">
            <el-input v-model="queryForm.name" placeholder="模糊搜索名称" clearable prefix-icon="Search"
              @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="英文标识">
            <el-input v-model="queryForm.code" placeholder="匹配标识编码" clearable prefix-icon="Key"
              @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 110px;">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <div class="action-buttons">
          <el-button type="success" icon="Plus" @click="openAddDialog">新增 VR 分类</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="categoryList" stripe border style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="分类 ID" width="185" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="园区名称与标识" min-width="170">
          <template #default="{ row }">
            <div class="category-name-cell">
              <strong>{{ row.name }}</strong>
              <span class="code-badge">{{ row.code }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="导览地图底图" width="140" align="center">
          <template #default="{ row }">
            <div v-if="row.mapUrl" class="map-thumb-box">
              <el-image :src="row.mapUrl" :preview-src-list="[row.mapUrl]" fit="cover" class="map-thumb"
                preview-teleported />
            </div>
            <el-tag v-else type="info" size="small" effect="plain">无底图(纯漫游)</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="包含点位数" width="110" align="center">
          <template #default="{ row }">
            <el-tooltip content="点击查看该分类下的场景" placement="top">
              <el-button link type="primary" @click="goToScenes(row.id)">
                <el-tag :type="row.sceneCount > 0 ? 'success' : 'info'" style="cursor: pointer;">
                  {{ row.sceneCount || 0 }} 个点位
                </el-tag>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="园区简介" min-width="200" show-overflow-tooltip />

        <el-table-column prop="sort" label="排序" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="plain">{{ row.sort }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.mapUrl" type="success" link size="small" icon="Compass" @click="goToMapEditor(row.id)">
              打点编辑
            </el-button>
            <el-button type="primary" link size="small" icon="Edit" @click="openEditDialog(row)">
              修改
            </el-button>
            <el-popconfirm :title="`确定删除分类【${row.name}】吗？若有下属点位将被拦截保护。`" width="260" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 修改 对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑 VR 园区分类' : '新增 VR 园区分类'" width="560px" destroy-on-close
      :close-on-click-modal="false" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" class="dialog-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：顺峰山公园-西区" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="英文标识码" prop="code">
          <el-input v-model="form.code" placeholder="例如：west_park, east_park, dragon_boat" maxlength="50"
            show-word-limit />
          <div class="form-tip">用于 URL 路由标识或程序内快速引用，全站唯一且推荐小写下划线</div>
        </el-form-item>

        <el-form-item label="导览地图底图" prop="mapUrl">
          <div class="custom-upload-wrapper">
            <!-- 已上传状态：展示底图微缩卡片，支持放大预览、点击重新上传、删除 -->
            <div v-if="form.mapUrl" class="upload-image-card map-card">
              <el-image
                :src="form.mapUrl"
                fit="cover"
                class="card-img"
                :preview-src-list="[form.mapUrl]"
                preview-teleported
              />
              <div class="card-mask">
                <div class="mask-action-list">
                  <span class="mask-action-btn" title="查看底图大图" @click="openImagePreview(form.mapUrl)">
                    <el-icon :size="16"><ZoomIn /></el-icon>
                    <span>预览</span>
                  </span>
                  <el-upload
                    class="reupload-trigger"
                    :show-file-list="false"
                    :http-request="handleMapUpload"
                    accept="image/*"
                  >
                    <span class="mask-action-btn" title="重新选择底图替换">
                      <el-icon :size="16"><Refresh /></el-icon>
                      <span>重新上传</span>
                    </span>
                  </el-upload>
                  <span class="mask-action-btn danger-btn" title="删除底图" @click="form.mapUrl = ''">
                    <el-icon :size="16"><Delete /></el-icon>
                    <span>删除</span>
                  </span>
                </div>
              </div>
              <div class="card-status-badge">
                <el-icon><CircleCheckFilled /></el-icon>
                <span>底图已直传 R2</span>
              </div>
            </div>

            <!-- 未上传状态：Element 拖拽/点击上传卡片 -->
            <el-upload
              v-else
              class="map-uploader-dropzone"
              drag
              :show-file-list="false"
              :http-request="handleMapUpload"
              accept="image/*"
              :disabled="uploadingMap"
            >
              <div v-loading="uploadingMap" element-loading-text="导览底图正在直传 Cloudflare R2..." class="dropzone-inner">
                <el-icon class="dropzone-icon"><Compass /></el-icon>
                <div class="dropzone-text">
                  点击或拖拽上传 <em>园区导览底图</em>
                </div>
                <div class="dropzone-tip">
                  建议高分辨率平面/鸟瞰图，支持最大 100MB 直传（可选，展馆类无地图可不上传）
                </div>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="园区/分类简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述该园区或展区特色..." maxlength="255"
            show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态启用">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用"
                inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存更新' : '立即创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 大图放大查看器 (支持点开全屏看大图、缩放、旋转) -->
    <el-image-viewer
      v-if="isViewerOpen"
      :url-list="[previewViewerUrl]"
      @close="isViewerOpen = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { vrApi } from '@/api/vr'

const router = useRouter()

const loading = ref(false)
const categoryList = ref([])

// 大图全屏查看器状态
const isViewerOpen = ref(false)
const previewViewerUrl = ref('')

function openImagePreview(url) {
  if (!url) return
  previewViewerUrl.value = url
  isViewerOpen.value = true
}

// 搜索表单
const queryForm = reactive({
  name: '',
  code: '',
  status: undefined
})

// 对话框表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploadingMap = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  code: '',
  mapUrl: '',
  description: '',
  sort: 1,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入分类/园区名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入唯一英文标识码', trigger: 'blur' }]
}

// 获取分类列表
async function fetchCategories() {
  loading.value = true
  try {
    const res = await vrApi.categoryList(queryForm)
    if (res.data) {
      categoryList.value = res.data
    }
  } catch (err) {
    // 错误在 request 拦截器中统一弹出
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  fetchCategories()
}

function handleReset() {
  queryForm.name = ''
  queryForm.code = ''
  queryForm.status = undefined
  fetchCategories()
}

// 打开新增
function openAddDialog() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.mapUrl = ''
  form.description = ''
  form.sort = (categoryList.value.length + 1) * 10
  form.status = 1
  dialogVisible.value = true
}

// 打开编辑
function openEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.mapUrl = row.mapUrl || ''
  form.description = row.description || ''
  form.sort = row.sort || 0
  form.status = row.status !== undefined ? row.status : 1
  dialogVisible.value = true
}

// 地图底图直传 R2
async function handleMapUpload(options) {
  const file = options.file
  uploadingMap.value = true
  try {
    const res = await vrApi.uploadImage(file, 'vr_map')
    if (res.data) {
      form.mapUrl = res.data
      ElMessage.success('底图已成功上传至 Cloudflare R2！')
    }
  } catch (err) {
    ElMessage.error('底图上传失败，请重试')
  } finally {
    uploadingMap.value = false
  }
}

// 提交保存
function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await vrApi.updateCategory(form.id, {
          name: form.name,
          code: form.code,
          mapUrl: form.mapUrl,
          description: form.description,
          sort: form.sort,
          status: form.status
        })
        ElMessage.success('分类更新成功')
      } else {
        await vrApi.addCategory({
          name: form.name,
          code: form.code,
          mapUrl: form.mapUrl,
          description: form.description,
          sort: form.sort,
          status: form.status
        })
        ElMessage.success('分类创建成功')
      }
      dialogVisible.value = false
      fetchCategories()
    } catch (err) {
      // 错误已由 request 拦截
    } finally {
      submitting.value = false
    }
  })
}

// 逻辑删除
async function handleDelete(id) {
  try {
    await vrApi.deleteCategory(id)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (err) {
    // 拦截器已处理错误提示
  }
}

// 跳转到该分类下的场景列表
function goToScenes(categoryId) {
  router.push({
    path: '/admin/vr/scene',
    query: { categoryId }
  })
}

// 跳转到可视化打点编辑器
function goToMapEditor(categoryId) {
  router.push({
    path: '/admin/vr/editor',
    query: { categoryId }
  })
}

function formatTime(timeStr) {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ')
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.vr-category-manage-page {
  padding-bottom: 24px;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.query-form {
  margin-bottom: -18px;
}

.category-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.code-badge {
  font-size: 11px;
  color: #64748b;
  font-family: monospace;
}

.map-thumb-box {
  width: 72px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  display: inline-block;
  cursor: pointer;
}

.map-thumb {
  width: 100%;
  height: 100%;
}

.custom-upload-wrapper {
  width: 100%;
}

.upload-image-card {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  background: #0f172a;
  transition: all 0.3s ease;
}

.map-card {
  height: 150px;
}

.card-img {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
}

.card-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.72);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.upload-image-card:hover .card-mask {
  opacity: 1;
}

.mask-action-list {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mask-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #f8fafc;
  font-size: 12px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.16);
  transition: all 0.2s ease;
  user-select: none;
}

.mask-action-btn:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-2px);
}

.mask-action-btn.danger-btn:hover {
  background: rgba(239, 68, 68, 0.85);
  color: #fff;
}

.reupload-trigger :deep(.el-upload) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(16, 185, 129, 0.9);
  color: #fff;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 12px;
  pointer-events: none;
}

.map-uploader-dropzone {
  width: 100%;
}

.map-uploader-dropzone :deep(.el-upload) {
  width: 100%;
  display: block;
}

.map-uploader-dropzone :deep(.el-upload-dragger) {
  width: 100%;
  padding: 20px 14px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.map-uploader-dropzone :deep(.el-upload-dragger:hover) {
  border-color: #3b82f6;
  background: #eff6ff;
}

.dropzone-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.dropzone-icon {
  font-size: 36px;
  color: #3b82f6;
  margin-bottom: 6px;
}

.dropzone-text {
  font-size: 13px;
  color: #334155;
  margin-bottom: 4px;
}

.dropzone-text em {
  color: #3b82f6;
  font-style: normal;
  font-weight: 600;
}

.dropzone-tip {
  font-size: 12px;
  color: #94a3b8;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
