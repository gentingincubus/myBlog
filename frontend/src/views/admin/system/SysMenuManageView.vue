<template>
  <div class="sys-menu-container">
    <el-card shadow="never" class="table-card">
      <!-- 顶部操作栏 -->
      <div class="action-header">
        <el-button type="primary" icon="Plus" v-hasPermi="['sys:menu:add']" @click="openMenuDialog(null)">
          新增顶级目录/菜单
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
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
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

    <!-- 弹窗：新增 / 编辑菜单与权限 -->
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMenuTableApi,
  addMenuApi,
  editMenuApi,
  deleteMenuApi
} from '@/api/system'

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
    // 为指定节点新增子项
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
    // 修改当前项
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
    // 新增顶级项
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
      loadData()
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
      loadData()
    } catch (err) {
      ElMessage.error(err.message || '删除失败')
    }
  })
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
</style>
