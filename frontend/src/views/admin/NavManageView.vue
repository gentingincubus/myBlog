<template>
  <div class="nav-manage-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索与操作栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent>
          <el-form-item label="菜单名称">
            <el-input
              v-model="queryForm.name"
              placeholder="模糊匹配名称"
              clearable
              prefix-icon="Search"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="路由路径">
            <el-input
              v-model="queryForm.path"
              placeholder="模糊匹配路径"
              clearable
              prefix-icon="Link"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <div class="action-buttons">
          <el-button type="success" icon="Plus" @click="openAddDialog">新增导航</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="navList"
        stripe
        border
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column prop="id" label="雪花 ID" width="185" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="菜单图标与名称" min-width="160">
          <template #default="{ row }">
            <span class="nav-name-cell">
              <span class="icon-span">{{ row.icon || '📌' }}</span>
              <strong>{{ row.name }}</strong>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="path" label="跳转路由" min-width="150" />

        <el-table-column label="排序权重" width="130" align="center">
          <template #default="{ row }">
            <el-tooltip :content="'原始值: ' + row.sort + ' (展示计算: * 10)'" placement="top">
              <el-tag type="warning" effect="plain">{{ row.displaySort }} (原: {{ row.sort }})</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="创建人" min-width="160" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ row.creatorName }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" icon="Edit" @click="openEditDialog(row)">
              修改
            </el-button>
            <el-popconfirm title="确定要删除该导航菜单吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 修改 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改导航菜单' : '新增导航菜单'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
        label-position="right"
      >
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formData.name" placeholder="例如：技术博客" />
        </el-form-item>

        <el-form-item label="跳转路由" prop="path">
          <el-input v-model="formData.path" placeholder="例如：/blog 或 https://..." />
        </el-form-item>

        <el-form-item label="图标 Emoji" prop="icon">
          <el-input v-model="formData.icon" placeholder="输入 Emoji 图标，例如：📝 / 🚀 / 💡" />
        </el-form-item>

        <el-form-item label="排序权重" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>

        <el-form-item label="新窗口打开" prop="isBlank">
          <el-switch v-model="formData.isBlank" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { navApi } from '@/api/nav'

const loading = ref(false)
const navList = ref([])

// 检索表单
const queryForm = reactive({
  name: '',
  path: ''
})

// 对话框表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  name: '',
  path: '',
  icon: '📌',
  sort: 1,
  isBlank: 0
})

const formRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入跳转路由', trigger: 'blur' }]
}

async function loadNavList() {
  loading.value = true
  try {
    const params = {}
    if (queryForm.name.trim()) params.name = queryForm.name.trim()
    if (queryForm.path.trim()) params.path = queryForm.path.trim()
    const res = await navApi.list(params)
    if (res && res.data) {
      navList.value = res.data
    }
  } catch (err) {
    // request.js 统一处理
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  loadNavList()
}

function handleReset() {
  queryForm.name = ''
  queryForm.path = ''
  loadNavList()
}

function openAddDialog() {
  isEdit.value = false
  currentId.value = null
  formData.name = ''
  formData.path = ''
  formData.icon = '📌'
  formData.sort = 1
  formData.isBlank = 0
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  currentId.value = row.id
  formData.name = row.name
  formData.path = row.path
  formData.icon = row.icon || '📌'
  formData.sort = row.sort || 0
  formData.isBlank = row.isBlank || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await navApi.update(currentId.value, formData)
        ElMessage.success('导航菜单修改成功！')
      } else {
        await navApi.add(formData)
        ElMessage.success('导航菜单添加成功！')
      }
      dialogVisible.value = false
      loadNavList()
    } catch (err) {
      // 错误统一提示
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(id) {
  try {
    await navApi.delete(id)
    ElMessage.success('导航菜单已成功删除')
    loadNavList()
  } catch (err) {}
}

function formatTime(timeStr) {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ')
}

onMounted(() => {
  loadNavList()
})
</script>

<style scoped>
.nav-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.query-form {
  margin-bottom: -18px;
}

.nav-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.icon-span {
  font-size: 16px;
}
</style>
