<template>
  <div class="sys-role-container">
    <el-card shadow="never" class="table-card">
      <!-- 顶部操作栏 -->
      <div class="action-header">
        <el-button type="primary" icon="Plus" v-hasPermi="['sys:role:add']" @click="openRoleDialog()">
          新增角色
        </el-button>
        <el-button icon="Refresh" @click="loadData">刷新</el-button>
      </div>

      <!-- 角色数据表格 -->
      <el-table v-loading="loading" :data="roleList" border stripe style="width: 100%">
        <el-table-column prop="id" label="角色ID" width="100" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="140">
          <template #default="{ row }">
            <span class="font-bold">{{ row.roleName }}</span>
            <el-tag v-if="row.roleKey === 'admin'" size="small" type="danger" effect="dark" class="ml-2">
              全站最高权限
            </el-tag>
            <el-tag v-else-if="row.roleKey === 'common'" size="small" type="info" class="ml-2">
              新用户默认
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleKey" label="权限字符" min-width="130">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.roleKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="显示顺序" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Check" v-hasPermi="['sys:role:perm']"
              :disabled="row.roleKey === 'admin'" @click="openPermDialog(row)">
              分配权限
            </el-button>
            <el-button type="success" link icon="Edit" v-hasPermi="['sys:role:edit']" @click="openRoleDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" link icon="Delete" v-hasPermi="['sys:role:delete']"
              :disabled="row.roleKey === 'admin' || row.roleKey === 'common'" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 1：新增 / 编辑角色 -->
    <el-dialog v-model="roleDialogVisible" :title="isEditRole ? '修改角色信息' : '创建新角色'" width="480px" destroy-on-close>
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="如：VR全景运营员" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="如：vr_editor (唯一标识)"
            :disabled="isEditRole && (roleForm.roleKey === 'admin' || roleForm.roleKey === 'common')" />
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="可选备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="submitRoleForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 2：分配权限（核心树形复选框） -->
    <el-dialog v-model="permDialogVisible" title="分配菜单与按钮权限" width="560px" destroy-on-close>
      <div v-loading="permLoading" class="perm-dialog-content">
        <div class="perm-tips">
          正在为角色 <strong>{{ currentRole?.roleName }}</strong>（{{ currentRole?.roleKey }}）勾选授权项：
        </div>
        <div class="perm-tree-box">
          <el-tree ref="permTreeRef" :data="menuTree" show-checkbox node-key="id"
            :props="{ label: 'menuName', children: 'children' }" default-expand-all :expand-on-click-node="false">
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-tags">
                  <el-tag v-if="data.menuType === 'M'" size="small" type="primary" effect="plain">目录</el-tag>
                  <el-tag v-else-if="data.menuType === 'C'" size="small" type="success" effect="plain">菜单</el-tag>
                  <el-tag v-else-if="data.menuType === 'F'" size="small" type="warning" effect="plain">按钮</el-tag>
                  <span v-if="data.perms" class="node-perm">[{{ data.perms }}]</span>
                </span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="submitPerms">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRoleListApi,
  addRoleApi,
  editRoleApi,
  deleteRoleApi,
  getAllMenuTreeApi,
  getRoleMenuIdsApi,
  assignRoleMenusApi
} from '@/api/system'

const loading = ref(false)
const roleList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleListApi()
    roleList.value = res.data || []
  } catch (err) {
    ElMessage.error(err.message || '加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// ================= 角色增改逻辑 =================
const roleDialogVisible = ref(false)
const isEditRole = ref(false)
const roleSubmitting = ref(false)
const roleFormRef = ref(null)

const roleForm = reactive({
  id: undefined,
  roleName: '',
  roleKey: '',
  sort: 1,
  status: 1,
  remark: ''
})

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色权限字符', trigger: 'blur' }]
}

const openRoleDialog = (row) => {
  isEditRole.value = !!row
  if (row) {
    Object.assign(roleForm, row)
  } else {
    Object.assign(roleForm, {
      id: undefined,
      roleName: '',
      roleKey: '',
      sort: 1,
      status: 1,
      remark: ''
    })
  }
  roleDialogVisible.value = true
}

const submitRoleForm = async () => {
  if (!roleFormRef.value) return
  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return
    roleSubmitting.value = true
    try {
      if (isEditRole.value) {
        await editRoleApi(roleForm)
        ElMessage.success('角色信息已更新')
      } else {
        await addRoleApi(roleForm)
        ElMessage.success('新角色创建成功')
      }
      roleDialogVisible.value = false
      loadData()
    } catch (err) {
      ElMessage.error(err.message || '保存失败')
    } finally {
      roleSubmitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要彻底删除角色 [${row.roleName}] 吗？`, '删除警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRoleApi(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (err) {
      ElMessage.error(err.message || '删除失败')
    }
  })
}

// ================= 分配权限（树形勾选）逻辑 =================
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitting = ref(false)
const currentRole = ref(null)
const menuTree = ref([])
const permTreeRef = ref(null)

const openPermDialog = async (row) => {
  currentRole.value = row
  permDialogVisible.value = true
  permLoading.value = true

  try {
    const [treeRes, menuIdsRes] = await Promise.all([
      getAllMenuTreeApi(),
      getRoleMenuIdsApi(row.id)
    ])
    menuTree.value = treeRes.data || []
    const checkedKeys = menuIdsRes.data || []

    await nextTick()
    if (permTreeRef.value) {
      // 过滤只勾选叶子节点，防止 el-tree 父子联动把所有未完全勾选的父级当成全选
      const leafKeys = []
      const filterLeaf = (nodes) => {
        for (const item of nodes) {
          if (!item.children || item.children.length === 0) {
            if (checkedKeys.includes(item.id)) {
              leafKeys.push(item.id)
            }
          } else {
            filterLeaf(item.children)
          }
        }
      }
      filterLeaf(menuTree.value)
      permTreeRef.value.setCheckedKeys(leafKeys)
    }
  } catch (err) {
    ElMessage.error(err.message || '加载权限树失败')
  } finally {
    permLoading.value = false
  }
}

const submitPerms = async () => {
  if (!permTreeRef.value) return
  // 获取完全选中的节点 + 半选中的父节点（保证菜单目录也能被正常查出）
  const checkedKeys = permTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permTreeRef.value.getHalfCheckedKeys()
  const finalKeys = [...checkedKeys, ...halfCheckedKeys]

  permSubmitting.value = true
  try {
    await assignRoleMenusApi(currentRole.value.id, finalKeys)
    ElMessage.success(`角色 [${currentRole.value.roleName}] 权限分配保存成功！`)
    permDialogVisible.value = false
  } catch (err) {
    ElMessage.error(err.message || '权限保存失败')
  } finally {
    permSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sys-role-container {
  padding: 16px;
}

.table-card {
  border-radius: 8px;
}

.action-header {
  margin-bottom: 16px;
}

.font-bold {
  font-weight: 600;
}

.ml-2 {
  margin-left: 8px;
}

.perm-dialog-content {
  padding: 8px 0;
}

.perm-tips {
  margin-bottom: 12px;
  color: #475569;
  font-size: 14px;
}

.perm-tree-box {
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  background-color: #f8fafc;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
  font-size: 13px;
}

.tree-tags {
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-perm {
  color: #64748b;
  font-family: monospace;
  font-size: 12px;
}
</style>
