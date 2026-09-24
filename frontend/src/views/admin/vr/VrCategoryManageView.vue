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
          <div class="map-upload-container">
            <!-- 预览框 -->
            <div v-if="form.mapUrl" class="upload-preview-box">
              <el-image :src="form.mapUrl" fit="cover" class="preview-img" :preview-src-list="[form.mapUrl]" />
              <div class="preview-mask">
                <el-icon class="remove-btn" @click="form.mapUrl = ''">
                  <Delete />
                </el-icon>
              </div>
            </div>

            <!-- 上传按钮组件 -->
            <el-upload v-else class="map-uploader" :show-file-list="false" :http-request="handleMapUpload"
              accept="image/*">
              <div class="upload-placeholder" v-loading="uploadingMap">
                <el-icon :size="24">
                  <UploadFilled />
                </el-icon>
                <div class="upload-text">点击上传底图 (直传 R2)</div>
              </div>
            </el-upload>
          </div>
          <el-input v-model="form.mapUrl" placeholder="或直接粘贴 Cloudflare R2 图片直链 URL" style="margin-top: 8px;"
            clearable />
          <div class="form-tip">可选。室内展馆若无导览图可留空，前台会自动隐藏地图面板</div>
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

.map-upload-container {
  width: 100%;
}

.upload-preview-box {
  position: relative;
  width: 100%;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
}

.preview-img {
  width: 100%;
  height: 100%;
}

.preview-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.preview-mask:hover {
  opacity: 1;
}

.remove-btn {
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  background: rgba(239, 68, 68, 0.8);
  padding: 6px;
  border-radius: 50%;
}

.upload-placeholder {
  width: 100%;
  height: 110px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  gap: 8px;
  background: #f8fafc;
  transition: all 0.2s;
}

.upload-placeholder:hover {
  border-color: #38bdf8;
  color: #0284c7;
}

.upload-text {
  font-size: 13px;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
