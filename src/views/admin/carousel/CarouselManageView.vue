<template>
  <div class="carousel-manage-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索与操作栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent>
          <el-form-item label="轮播标题">
            <el-input
              v-model="queryForm.title"
              placeholder="模糊搜索标题"
              clearable
              prefix-icon="Search"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="已启用" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <div class="action-buttons">
          <el-button
            v-hasPermi="['site:carousel:add']"
            type="success"
            icon="Plus"
            @click="openAddDialog"
          >
            新增轮播
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="carouselList"
        stripe
        border
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column prop="id" label="ID" width="185" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="封面图" width="130" align="center">
          <template #default="{ row }">
            <div class="table-cover-box">
              <el-image
                :src="row.coverUrl"
                fit="cover"
                class="table-cover-img"
                :preview-src-list="[row.coverUrl]"
                preview-teleported
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="轮播标题与副标题" min-width="220">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="main-title">{{ row.title }}</div>
              <div v-if="row.subtitle" class="sub-title">{{ row.subtitle }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序权重" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="plain">{{ row.sort }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="!hasPermi(['site:carousel:edit'])"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link icon="View" @click="handlePreview(row)">
              预览介绍
            </el-button>
            <el-button
              v-hasPermi="['site:carousel:edit']"
              type="primary"
              link
              icon="Edit"
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPermi="['site:carousel:delete']"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑首页轮播图' : '新增首页轮播图'"
      width="820px"
      top="4vh"
      destroy-on-close
      :close-on-click-modal="false"
      class="carousel-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="95px"
        size="default"
      >
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="轮播标题" prop="title">
              <el-input v-model="form.title" placeholder="如：顺峰山公园 · 中华第一牌坊" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序权重" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="副标/标语" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="如：顺德之门，气势磅礴的岭南建筑丰碑" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverUrl">
          <div class="cover-uploader-box">
            <div
              v-if="form.coverUrl"
              class="cover-preview-card"
              v-loading="uploadCoverLoading"
              element-loading-text="正在上传..."
              element-loading-background="rgba(255, 255, 255, 0.85)"
            >
              <el-image
                :src="form.coverUrl"
                fit="cover"
                class="cover-img"
                :preview-src-list="[form.coverUrl]"
                preview-teleported
              />
              <div class="cover-mask">
                <el-upload
                  :show-file-list="false"
                  :http-request="handleCoverUpload"
                  :disabled="uploadCoverLoading"
                  accept="image/*"
                >
                  <el-button type="primary" size="small" icon="Refresh" :loading="uploadCoverLoading">替换封面</el-button>
                </el-upload>
                <el-button type="danger" size="small" icon="Delete" :disabled="uploadCoverLoading" @click="form.coverUrl = ''">删除</el-button>
              </div>
            </div>

            <el-upload
              v-else
              class="cover-uploader"
              drag
              :show-file-list="false"
              :http-request="handleCoverUpload"
              :disabled="uploadCoverLoading"
              accept="image/*"
            >
              <div
                class="cover-drop-inner"
                v-loading="uploadCoverLoading"
                element-loading-text="正在上传..."
                element-loading-background="rgba(255, 255, 255, 0.85)"
              >
                <template v-if="!uploadCoverLoading">
                  <el-icon class="uploader-icon"><Plus /></el-icon>
                  <div class="el-upload__text">
                    点击或拖拽上传封面图到 <em>Cloudflare R2</em> (carousel/covers/)
                  </div>
                </template>
              </div>
            </el-upload>
          </div>
          <el-input v-model="form.coverUrl" placeholder="或直接填写 Cloudflare R2 图片直链 URL" style="margin-top: 8px" />
        </el-form-item>

        <el-form-item label="卡片富文本" prop="content">
          <div class="form-rich-editor">
            <div class="rich-tip">
              <span>💡 卡片翻转后将展示以下 Markdown 富文本介绍。支持粘贴截图/拖拽图片，将自动上传至 R2 (richtext/images/)</span>
            </div>
            <Edit
              v-model="form.content"
              height="360px"
              placeholder="请输入介绍内容，支持 Markdown 排版、图片、链接、表格等..."
            />
          </div>
        </el-form-item>

        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">立即启用展示</el-radio>
            <el-radio :label="0">暂时禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确 定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情预览抽屉 (模拟卡片翻转后呈现效果) -->
    <el-drawer
      v-model="previewDrawerVisible"
      title="卡片翻转效果 · Markdown 介绍预览"
      size="560px"
      destroy-on-close
    >
      <div v-if="currentPreviewRow" class="preview-drawer-content">
        <div class="preview-hero">
          <img :src="currentPreviewRow.coverUrl" class="preview-hero-img" />
          <div class="preview-hero-overlay">
            <h3>{{ currentPreviewRow.title }}</h3>
            <p v-if="currentPreviewRow.subtitle">{{ currentPreviewRow.subtitle }}</p>
          </div>
        </div>
        <el-divider content-position="left">卡片背面富文本详情</el-divider>
        <div class="preview-markdown-body">
          <ParseText :text="currentPreviewRow.content" theme="light" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { carouselApi } from '@/api/carousel'
import { uploadImageApi } from '@/api/upload'
import { useUserStore } from '@/stores/user'
import ParseText from '@/components/md-editor-v3/parseText.vue'
import Edit from '@/components/md-editor-v3/edit.vue'

const userStore = useUserStore()

function hasPermi(permissions) {
  if (!permissions || permissions.length === 0) return true
  const userPerms = userStore.permissions || []
  return userPerms.includes('*:*:*') || permissions.some(p => userPerms.includes(p))
}

const loading = ref(false)
const carouselList = ref([])

const queryForm = reactive({
  title: '',
  status: undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const uploadCoverLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  subtitle: '',
  coverUrl: '',
  content: '',
  sort: 0,
  status: 1
})

const rules = {
  title: [
    { required: true, message: '请输入轮播标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过 100 个字符', trigger: 'blur' }
  ],
  coverUrl: [
    { required: true, message: '请上传或填入封面图片', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入富文本介绍内容', trigger: 'blur' }
  ]
}

const previewDrawerVisible = ref(false)
const currentPreviewRow = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await carouselApi.list({
      title: queryForm.title ? queryForm.title.trim() : undefined,
      status: queryForm.status
    })
    if (res && res.data) {
      carouselList.value = res.data
    }
  } catch (err) {
    // 错误已被 request.js 拦截器提示
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  loadData()
}

function handleReset() {
  queryForm.title = ''
  queryForm.status = undefined
  loadData()
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    subtitle: '',
    coverUrl: '',
    content: '',
    sort: carouselList.value.length ? (carouselList.value[carouselList.value.length - 1].sort || 0) + 1 : 1,
    status: 1
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle || '',
    coverUrl: row.coverUrl,
    content: row.content,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

// 封面图片上传至 Cloudflare R2 (carousel/covers/)
async function handleCoverUpload(options) {
  if (uploadCoverLoading.value) return
  const file = options.file
  uploadCoverLoading.value = true
  try {
    const res = await uploadImageApi(file, 'carousel_image')
    if (res && res.data) {
      form.coverUrl = res.data
      formRef.value?.clearValidate('coverUrl')
      ElMessage.success('封面图片已成功上传至 Cloudflare R2！')
    }
  } catch (err) {
    console.error('封面上传失败:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || '封面上传失败，请重试')
  } finally {
    uploadCoverLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        title: form.title.trim(),
        subtitle: form.subtitle ? form.subtitle.trim() : '',
        coverUrl: form.coverUrl.trim(),
        content: form.content,
        sort: form.sort,
        status: form.status
      }

      if (isEdit.value) {
        await carouselApi.update(form.id, payload)
        ElMessage.success('轮播图修改成功')
      } else {
        await carouselApi.add(payload)
        ElMessage.success('轮播图新增成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (err) {
      // request.js 自动报错
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleStatusChange(row, newStatus) {
  try {
    await carouselApi.updateStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用该轮播' : '已禁用该轮播')
  } catch (err) {
    row.status = newStatus === 1 ? 0 : 1
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除轮播【${row.title}】吗？删除后前台将不再展示。`, '安全提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await carouselApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (err) { }
  }).catch(() => { })
}

function handlePreview(row) {
  currentPreviewRow.value = row
  previewDrawerVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.carousel-manage-page {
  padding: 20px;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-cover-box {
  width: 90px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background-color: #f1f5f9;
}

.table-cover-img {
  width: 100%;
  height: 100%;
}

.title-cell {
  text-align: left;
}

.main-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.sub-title {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

/* 封面上传控件 */
.cover-uploader-box {
  width: 100%;
}

.cover-uploader :deep(.el-upload-dragger) {
  padding: 20px;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
}

.cover-drop-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}

.uploader-icon {
  font-size: 28px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.cover-preview-card {
  position: relative;
  width: 100%;
  height: 170px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.25s;
}

.cover-preview-card:hover .cover-mask {
  opacity: 1;
}

.form-rich-editor {
  width: 100%;
}

.rich-tip {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

/* 预览抽屉样式 */
.preview-drawer-content {
  display: flex;
  flex-direction: column;
}

.preview-hero {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.preview-hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: #fff;
}

.preview-hero-overlay h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.preview-hero-overlay p {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.preview-markdown-body {
  padding: 8px 0;
}
</style>
