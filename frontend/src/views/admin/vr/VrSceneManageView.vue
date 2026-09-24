<template>
  <div class="vr-scene-manage-page">
    <el-card shadow="never" class="table-card">
      <!-- 搜索筛选栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent>
          <el-form-item label="所属园区">
            <el-select v-model="queryForm.categoryId" placeholder="全部园区" clearable style="width: 180px;"
              @change="handleQuery">
              <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="场景名称">
            <el-input v-model="queryForm.name" placeholder="搜索场景名称" clearable prefix-icon="Search"
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
          <el-button type="warning" icon="Compass" @click="goToMapEditor(queryForm.categoryId)">
            可视化打点编辑器
          </el-button>
          <el-button type="success" icon="Plus" @click="openAddDialog">新增场景点位</el-button>
        </div>
      </div>

      <!-- 场景数据表格 -->
      <el-table v-loading="loading" :data="sceneList" stripe border style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="点位 ID" width="185" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="缩略图" width="90" align="center">
          <template #default="{ row }">
            <div class="thumb-container">
              <el-image v-if="row.previewUrl" :src="row.previewUrl"
                :preview-src-list="[row.previewUrl, row.panoramaUrl]" fit="cover" class="thumb-img"
                preview-teleported />
              <span v-else class="no-thumb">无图</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="场景名称与所属园区" min-width="180">
          <template #default="{ row }">
            <div class="scene-info-cell">
              <strong>{{ row.name }}</strong>
              <el-tag size="small" type="info" effect="plain" class="cat-tag">
                {{ row.categoryName || '未分配' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="全景原图 (Cloudflare R2)" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <a :href="row.panoramaUrl" target="_blank" class="panorama-link">
              <el-icon>
                <PictureFilled />
              </el-icon>
              <span>{{ row.panoramaUrl }}</span>
            </a>
          </template>
        </el-table-column>

        <el-table-column label="地图坐标 (防漂移百分比)" width="190" align="center">
          <template #default="{ row }">
            <div class="coord-tag-group">
              <el-tag size="small" type="primary" effect="light">X: {{ row.leftPercent }}%</el-tag>
              <el-tag size="small" type="success" effect="light">Y: {{ row.topPercent }}%</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="初始航向角" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="warning">{{ row.initialDeg }}°</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="80" align="center" />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="warning" link size="small" icon="Location" @click="goToMapEditor(row.categoryId, row.id)">
              定位打点
            </el-button>
            <el-button type="primary" link size="small" icon="Edit" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-popconfirm :title="`确定删除场景【${row.name}】吗？`" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 修改 对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑 VR 场景点位' : '新增 VR 场景点位'" width="640px" destroy-on-close
      :close-on-click-modal="false" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" class="dialog-form">
        <el-form-item label="所属园区" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择所属 VR 园区/分类" style="width: 100%;">
            <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="场景名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：伏波桥、美的体育广场、牌坊" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 360 全景原图上传 -->
        <el-form-item label="360 全景原图" prop="panoramaUrl">
          <div class="upload-row">
            <el-upload class="single-uploader" :show-file-list="false" :http-request="handlePanoramaUpload"
              accept="image/*">
              <el-button type="primary" :loading="uploadingPano" icon="Upload">上传全景原图 (直传R2)</el-button>
            </el-upload>
            <span class="upload-tip-text">建议尺寸：4096x2048 或 8192x4096 球形等距贴图 (2:1)</span>
          </div>
          <el-input v-model="form.panoramaUrl" placeholder="全景图片直链 URL" style="margin-top: 8px;" clearable />
          <div v-if="form.panoramaUrl" class="image-preview-bar">
            <el-image :src="form.panoramaUrl" fit="cover" class="pano-preview" :preview-src-list="[form.panoramaUrl]" />
          </div>
        </el-form-item>

        <!-- 缩略图预览上传 -->
        <el-form-item label="场景缩略图" prop="previewUrl">
          <div class="upload-row">
            <el-upload class="single-uploader" :show-file-list="false" :http-request="handlePreviewUpload"
              accept="image/*">
              <el-button :loading="uploadingPreview" icon="Picture">上传缩略图 (直传R2)</el-button>
            </el-upload>
            <span class="upload-tip-text">用于场景选择抽屉与打点弹窗微缩图</span>
          </div>
          <el-input v-model="form.previewUrl" placeholder="缩略图直链 URL（可选）" style="margin-top: 8px;" clearable />
        </el-form-item>

        <!-- 地图打点坐标 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="打点 X 轴 (%)" prop="leftPercent">
              <el-input-number v-model="form.leftPercent" :min="0" :max="100" :precision="2" :step="0.5"
                controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打点 Y 轴 (%)" prop="topPercent">
              <el-input-number v-model="form.topPercent" :min="0" :max="100" :precision="2" :step="0.5"
                controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="初始航向角" prop="initialDeg">
              <el-input-number v-model="form.initialDeg" :min="-180" :max="180" controls-position="right"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="启用状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { vrApi } from '@/api/vr'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const sceneList = ref([])
const categoryOptions = ref([])

// 搜索条件
const queryForm = reactive({
  categoryId: undefined,
  name: '',
  status: undefined
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploadingPano = ref(false)
const uploadingPreview = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  categoryId: null,
  name: '',
  panoramaUrl: '',
  previewUrl: '',
  leftPercent: 0,
  topPercent: 0,
  initialDeg: 0,
  sort: 1,
  status: 1
})

const formRules = {
  categoryId: [{ required: true, message: '请选择所属园区分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  panoramaUrl: [{ required: true, message: '请输入或上传全景原图 URL', trigger: 'blur' }]
}

// 获取分类下拉列表
async function fetchCategories() {
  try {
    const res = await vrApi.categoryList()
    if (res.data) {
      categoryOptions.value = res.data
    }
  } catch (err) { }
}

// 获取场景列表
async function fetchScenes() {
  loading.value = true
  try {
    const res = await vrApi.sceneList(queryForm)
    if (res.data) {
      sceneList.value = res.data
    }
  } catch (err) {
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  fetchScenes()
}

function handleReset() {
  queryForm.categoryId = undefined
  queryForm.name = ''
  queryForm.status = undefined
  fetchScenes()
}

// 打开新增
function openAddDialog() {
  isEdit.value = false
  form.id = null
  form.categoryId = queryForm.categoryId || (categoryOptions.value[0]?.id ?? null)
  form.name = ''
  form.panoramaUrl = ''
  form.previewUrl = ''
  form.leftPercent = 50.00
  form.topPercent = 50.00
  form.initialDeg = 0
  form.sort = (sceneList.value.length + 1) * 10
  form.status = 1
  dialogVisible.value = true
}

// 打开编辑
function openEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.categoryId = row.categoryId
  form.name = row.name
  form.panoramaUrl = row.panoramaUrl
  form.previewUrl = row.previewUrl || ''
  form.leftPercent = Number(row.leftPercent) || 0
  form.topPercent = Number(row.topPercent) || 0
  form.initialDeg = row.initialDeg || 0
  form.sort = row.sort || 0
  form.status = row.status !== undefined ? row.status : 1
  dialogVisible.value = true
}

// 上传全景图直传 R2
async function handlePanoramaUpload(options) {
  const file = options.file
  uploadingPano.value = true
  try {
    const res = await vrApi.uploadImage(file, 'vr_panorama')
    if (res.data) {
      form.panoramaUrl = res.data
      ElMessage.success('360 全景原图已成功上传到 Cloudflare R2！')
    }
  } catch (err) {
    ElMessage.error('全景原图上传失败')
  } finally {
    uploadingPano.value = false
  }
}

// 上传预览缩略图直传 R2
async function handlePreviewUpload(options) {
  const file = options.file
  uploadingPreview.value = true
  try {
    const res = await vrApi.uploadImage(file, 'vr_preview')
    if (res.data) {
      form.previewUrl = res.data
      ElMessage.success('场景缩略图上传成功！')
    }
  } catch (err) {
    ElMessage.error('缩略图上传失败')
  } finally {
    uploadingPreview.value = false
  }
}

// 提交表单
function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await vrApi.updateScene(form.id, { ...form })
        ElMessage.success('场景修改成功')
      } else {
        await vrApi.addScene({ ...form })
        ElMessage.success('场景点位创建成功')
      }
      dialogVisible.value = false
      fetchScenes()
    } catch (err) {
    } finally {
      submitting.value = false
    }
  })
}

// 删除场景
async function handleDelete(id) {
  try {
    await vrApi.deleteScene(id)
    ElMessage.success('场景点位已删除')
    fetchScenes()
  } catch (err) { }
}

// 跳转到可视化打点编辑器
function goToMapEditor(categoryId, sceneId) {
  router.push({
    path: '/admin/vr/editor',
    query: {
      categoryId: categoryId || queryForm.categoryId,
      sceneId: sceneId || undefined
    }
  })
}

onMounted(async () => {
  await fetchCategories()
  // 支持路由传入初始 categoryId (例如从分类列表点进来)
  if (route.query.categoryId) {
    queryForm.categoryId = String(route.query.categoryId)
  }
  fetchScenes()
})
</script>

<style scoped>
.vr-scene-manage-page {
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

.action-buttons {
  display: flex;
  gap: 12px;
}

.thumb-container {
  width: 56px;
  height: 42px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.no-thumb {
  font-size: 11px;
  color: #94a3b8;
}

.scene-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cat-tag {
  align-self: flex-start;
}

.panorama-link {
  color: #0284c7;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.panorama-link:hover {
  text-decoration: underline;
}

.coord-tag-group {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-tip-text {
  font-size: 12px;
  color: #94a3b8;
}

.image-preview-bar {
  margin-top: 8px;
  width: 100%;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.pano-preview {
  width: 100%;
  height: 100%;
}
</style>
