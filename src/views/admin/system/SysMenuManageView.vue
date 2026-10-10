<template>
  <div class="sys-menu-container">
    <el-card shadow="never" class="table-card">
      <!-- 顶部操作栏 -->
      <div class="action-header">
        <el-button type="primary" icon="Plus" v-hasPermi="['sys:menu:add']" @click="openMenuDialog(null)">
          新增顶级目录/菜单
        </el-button>
        <el-button type="warning" icon="Sort" v-hasPermi="['sys:menu:edit']" @click="openSortDialog(0)">
          可视化拖拽排序
        </el-button>
        <el-button icon="Refresh" @click="loadData">刷新</el-button>
        <el-button @click="toggleExpandAll">{{ isExpandAll ? '全部折叠' : '全部展开' }}</el-button>
      </div>

      <!-- 菜单与权限树形表格 -->
      <el-table v-if="refreshTable" v-loading="loading" :data="menuList" row-key="id" :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border style="width: 100%">
        <el-table-column prop="menuName" label="菜单/权限名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :size="18">
              <component :is="row.icon" />
            </el-icon>
            <span v-else class="text-slate">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="primary" effect="dark">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success" effect="dark">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 'F'" type="warning" effect="dark">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识符" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.perms" effect="plain" class="font-mono">
              {{ row.perms }}
            </el-tag>
            <span v-else class="text-slate">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.path || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.component || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.menuType === 'M' || (row.children && row.children.length > 0)"
              type="warning" link icon="Sort" v-hasPermi="['sys:menu:edit']"
              @click="openSortDialog(row.id)">
              子项排序
            </el-button>
            <el-button type="primary" link icon="Plus" v-hasPermi="['sys:menu:add']" :disabled="row.menuType === 'F'"
              @click="openMenuDialog(row, true)">
              新增子项
            </el-button>
            <el-button type="success" link icon="Edit" v-hasPermi="['sys:menu:edit']"
              @click="openMenuDialog(row, false)">
              修改
            </el-button>
            <el-button type="danger" link icon="Delete" v-hasPermi="['sys:menu:delete']" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 1：新增 / 编辑菜单与权限 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="menuForm.parentId" :data="parentOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }" value-key="id"
            placeholder="选择上级菜单 (不选默认为顶级)" check-strictly clearable style="width: 100%" />
        </el-form-item>

        <el-form-item label="节点类型" prop="menuType">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio-button value="M">目录</el-radio-button>
            <el-radio-button value="C">菜单</el-radio-button>
            <el-radio-button value="F">按钮操作</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="名称" prop="menuName">
          <el-input v-model="menuForm.menuName" placeholder="如：全景场景管理 或 新增场景" />
        </el-form-item>

        <el-form-item v-if="menuForm.menuType !== 'F'" label="菜单图标">
          <el-input v-model="menuForm.icon" placeholder="Element Plus 图标名，如 View, Odometer" />
        </el-form-item>

        <el-form-item v-if="menuForm.menuType !== 'F'" label="路由地址" prop="path">
          <el-input v-model="menuForm.path" placeholder="如：/admin/vr/scene" />
        </el-form-item>

        <el-form-item v-if="menuForm.menuType === 'C'" label="组件路径">
          <el-input v-model="menuForm.component" placeholder="如：admin/vr/VrSceneManageView" />
        </el-form-item>

        <el-form-item v-if="menuForm.menuType !== 'M'" label="权限标识符">
          <el-input v-model="menuForm.perms" placeholder="如：vr:scene:add 或 sys:user:list" />
        </el-form-item>

        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="menuForm.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitMenuForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 2：可视化拖拽排序 (vuedraggable) -->
    <el-dialog
      v-model="sortDialogVisible"
      title="菜单层级可视化拖拽排序"
      width="680px"
      destroy-on-close
      class="sort-dialog"
    >
      <div class="sort-dialog-body">
        <!-- 切换要排序的层级 -->
        <div class="sort-level-selector">
          <span class="selector-label">选择排序层级：</span>
          <el-select
            v-model="currentSortParentId"
            placeholder="请选择要排序的目录层级"
            style="flex: 1;"
            @change="onSortLevelChange"
          >
            <el-option :value="0" label="📁 【顶级主导航与一级目录】" />
            <el-option
              v-for="item in sortableFolderOptions"
              :key="item.id"
              :value="item.id"
              :label="`📂 ${item.menuName} (子项数量: ${item.children?.length || 0})`"
            />
          </el-select>
        </div>

        <!-- 提示栏 -->
        <el-alert
          type="info"
          :closable="false"
          show-icon
          class="sort-alert"
        >
          <template #title>
            <span>按住左侧 <strong>手柄图标</strong> 上下拖拽即可调整菜单顺序。排在上方者序号更小、展示更靠前。保存后侧边栏将立即实时刷新！</span>
          </template>
        </el-alert>

        <!-- 拖拽卡片列表 -->
        <div v-if="draggableList.length > 0" class="sort-list-wrapper">
          <draggable
            v-model="draggableList"
            item-key="id"
            animation="250"
            handle=".drag-handle"
            ghost-class="sort-ghost"
            chosen-class="sort-chosen"
            class="drag-list-container"
          >
            <template #item="{ element, index }">
              <div class="drag-item-card">
                <!-- 拖拽手柄 -->
                <div class="drag-handle" title="按住上下拖拽">
                  <el-icon :size="18"><Rank /></el-icon>
                </div>

                <!-- 序号徽标 -->
                <div class="sort-order-badge">{{ index + 1 }}</div>

                <!-- 菜单图标 -->
                <div class="menu-icon-box">
                  <el-icon v-if="element.icon" :size="18">
                    <component :is="element.icon" />
                  </el-icon>
                  <span v-else class="empty-icon">-</span>
                </div>

                <!-- 菜单名称与路径 -->
                <div class="menu-info-box">
                  <div class="menu-title-row">
                    <span class="menu-title">{{ element.menuName }}</span>
                    <el-tag size="small" :type="getTypeTag(element.menuType)" effect="light">
                      {{ getTypeName(element.menuType) }}
                    </el-tag>
                  </div>
                  <span v-if="element.path" class="menu-path-tag">{{ element.path }}</span>
                </div>

                <!-- 权重值预览 -->
                <div class="sort-weight-preview">
                  <span class="weight-label">新权重:</span>
                  <span class="weight-val">{{ (index + 1) * 10 }}</span>
                </div>
              </div>
            </template>
          </draggable>
        </div>
        <el-empty v-else description="该层级下暂无子菜单项可排序" :image-size="80" />
      </div>

      <template #footer>
        <div class="sort-dialog-footer">
          <el-button @click="sortDialogVisible = false">取消</el-button>
          <el-button @click="resetCurrentSort">恢复原序</el-button>
          <el-button
            type="primary"
            :loading="savingSort"
            :disabled="draggableList.length <= 1"
            @click="submitMenuSort"
          >
            保存并应用新排序
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'
import { useUserStore } from '@/stores/user'
import {
  getMenuTableApi,
  addMenuApi,
  editMenuApi,
  deleteMenuApi,
  updateMenuSortApi
} from '@/api/system'

const userStore = useUserStore()
const loading = ref(false)
const menuList = ref([])
const refreshTable = ref(true)
const isExpandAll = ref(true)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMenuTableApi()
    menuList.value = res.data || []
  } catch (err) {
    ElMessage.error(err.message || '加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

const toggleExpandAll = () => {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

// ================= 新增 / 编辑菜单弹窗 =================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)
const menuFormRef = ref(null)
const parentOptions = ref([])

const menuForm = reactive({
  id: undefined,
  parentId: 0,
  menuName: '',
  menuType: 'C',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 1,
  visible: 1,
  status: 1
})

const menuRules = {
  menuName: [{ required: true, message: '请输入菜单/按钮名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择节点类型', trigger: 'change' }]
}

const openMenuDialog = (row, isAddChild = false) => {
  parentOptions.value = [
    {
      id: 0,
      menuName: '顶级目录',
      children: menuList.value
    }
  ]

  if (isAddChild) {
    isEdit.value = false
    dialogTitle.value = `为 [${row.menuName}] 添加子项`
    Object.assign(menuForm, {
      id: undefined,
      parentId: row.id,
      menuName: '',
      menuType: row.menuType === 'M' ? 'C' : 'F',
      path: '',
      component: '',
      perms: '',
      icon: '',
      sort: 1,
      visible: 1,
      status: 1
    })
  } else if (row) {
    isEdit.value = true
    dialogTitle.value = `修改 [${row.menuName}]`
    Object.assign(menuForm, {
      id: row.id,
      parentId: row.parentId || 0,
      menuName: row.menuName,
      menuType: row.menuType,
      path: row.path,
      component: row.component,
      perms: row.perms,
      icon: row.icon,
      sort: row.sort,
      visible: row.visible,
      status: row.status
    })
  } else {
    isEdit.value = false
    dialogTitle.value = '新增顶级目录/菜单'
    Object.assign(menuForm, {
      id: undefined,
      parentId: 0,
      menuName: '',
      menuType: 'M',
      path: '',
      component: '',
      perms: '',
      icon: '',
      sort: 1,
      visible: 1,
      status: 1
    })
  }

  dialogVisible.value = true
}

const submitMenuForm = async () => {
  if (!menuFormRef.value) return
  await menuFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await editMenuApi(menuForm)
        ElMessage.success('菜单修改成功！')
      } else {
        await addMenuApi(menuForm)
        ElMessage.success('菜单创建成功！')
      }
      dialogVisible.value = false
      await loadData()
      await userStore.fetchMenuRoutes()
    } catch (err) {
      ElMessage.error(err.message || '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除 [${row.menuName}] 吗？如果存在子项将被系统拦截保护。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMenuApi(row.id)
      ElMessage.success('删除成功')
      await loadData()
      await userStore.fetchMenuRoutes()
    } catch (err) {
      ElMessage.error(err.message || '删除失败')
    }
  })
}

// ================= 可视化拖拽排序 (vuedraggable) =================
const sortDialogVisible = ref(false)
const savingSort = ref(false)
const currentSortParentId = ref(0)
const sortableFolderOptions = ref([])
const draggableList = ref([])
let originalSortSnapshot = []

/**
 * 递归收集所有可包含子项的目录/菜单节点供层级下拉选择
 */
function collectFolderOptions(list) {
  const result = []
  function traverse(items) {
    if (!items || items.length === 0) return
    for (const item of items) {
      if (item.menuType === 'M' || (item.children && item.children.length > 0)) {
        result.push(item)
      }
      if (item.children && item.children.length > 0) {
        traverse(item.children)
      }
    }
  }
  traverse(list)
  return result
}

/**
 * 获取指定父级 ID 下的平级子项列表
 */
function getItemsForParentId(parentId) {
  if (parentId === 0) {
    return (menuList.value || []).map(item => ({ ...item }))
  }
  function findNode(items, id) {
    for (const item of items) {
      if (item.id === id) return item
      if (item.children) {
        const found = findNode(item.children, id)
        if (found) return found
      }
    }
    return null
  }
  const parentNode = findNode(menuList.value, parentId)
  return (parentNode?.children || []).map(item => ({ ...item }))
}

/**
 * 打开拖拽排序弹窗
 * @param {number|string} targetParentId 要排序的目标父级ID (0表示顶级)
 */
const openSortDialog = (targetParentId = 0) => {
  currentSortParentId.value = targetParentId
  sortableFolderOptions.value = collectFolderOptions(menuList.value)
  const items = getItemsForParentId(targetParentId)
  draggableList.value = items
  originalSortSnapshot = JSON.parse(JSON.stringify(items))
  sortDialogVisible.value = true
}

/**
 * 切换排序层级下拉框
 */
const onSortLevelChange = (newParentId) => {
  const items = getItemsForParentId(newParentId)
  draggableList.value = items
  originalSortSnapshot = JSON.parse(JSON.stringify(items))
}

/**
 * 恢复原序
 */
const resetCurrentSort = () => {
  draggableList.value = JSON.parse(JSON.stringify(originalSortSnapshot))
  ElMessage.info('已恢复为原始排序')
}

/**
 * 保存并应用新排序
 */
const submitMenuSort = async () => {
  if (draggableList.value.length === 0) return
  savingSort.value = true
  try {
    const payload = draggableList.value.map((item, index) => ({
      id: item.id,
      sort: (index + 1) * 10
    }))
    await updateMenuSortApi(payload)
    ElMessage.success('菜单排序保存成功，侧边栏已实时同步！')
    sortDialogVisible.value = false
    await loadData()
    await userStore.fetchMenuRoutes()
  } catch (err) {
    ElMessage.error(err.message || '更新排序失败')
  } finally {
    savingSort.value = false
  }
}

function getTypeTag(type) {
  if (type === 'M') return 'primary'
  if (type === 'C') return 'success'
  return 'warning'
}

function getTypeName(type) {
  if (type === 'M') return '目录'
  if (type === 'C') return '菜单'
  return '按钮'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sys-menu-container {
  padding: 16px;
}

.table-card {
  border-radius: 8px;
}

.action-header {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.font-mono {
  font-family: monospace;
}

.text-slate {
  color: #94a3b8;
}

/* ================= 拖拽排序弹窗样式 ================= */
.sort-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sort-level-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.selector-label {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
}

.sort-alert {
  border-radius: 6px;
}

.sort-list-wrapper {
  max-height: 420px;
  overflow-y: auto;
  padding: 4px;
}

.drag-list-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.drag-item-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  user-select: none;
}

.drag-item-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
}

.drag-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  color: #94a3b8;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.15s;
}

.drag-handle:hover {
  color: #3b82f6;
  background: #f1f5f9;
}

.drag-handle:active {
  cursor: grabbing;
}

.sort-order-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 6px;
  font-weight: 700;
  font-size: 13px;
  font-family: monospace;
}

.menu-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: #475569;
}

.empty-icon {
  color: #cbd5e1;
}

.menu-info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.menu-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}

.menu-path-tag {
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sort-weight-preview {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #f1f5f9;
}

.weight-val {
  font-weight: 700;
  color: #0f172a;
  font-family: monospace;
}

/* 拖拽动画效果 */
.sort-ghost {
  opacity: 0.4;
  background: #e0f2fe !important;
  border: 1px dashed #0284c7 !important;
}

.sort-chosen {
  background: #f0f9ff !important;
  box-shadow: 0 8px 16px rgba(14, 165, 233, 0.15) !important;
}

.sort-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
