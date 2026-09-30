<template>
  <div class="sys-user-container">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索栏与操作 -->
      <div class="filter-header">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="账号状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户数据列表 -->
      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="100" align="center" />
        <el-table-column prop="username" label="登录用户名" min-width="140">
          <template #default="{ row }">
            <span class="font-bold">{{ row.username }}</span>
            <el-tag v-if="row.id === 1" size="small" type="danger" effect="dark" class="ml-2">超管</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户昵称" min-width="140" />
        <el-table-column label="账号状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" :disabled="row.id === 1"
              @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="UserFilled" v-hasPermi="['sys:user:role']" :disabled="row.id === 1"
              @click="openRoleDialog(row)">
              分配角色
            </el-button>
            <el-button type="warning" link icon="Key" v-hasPermi="['sys:user:resetPwd']"
              @click="openResetPwdDialog(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-box">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 弹窗 1：分配角色 -->
    <el-dialog v-model="roleDialogVisible" title="分配用户角色" width="480px" destroy-on-close>
      <div v-loading="roleLoading" class="role-dialog-content">
        <p class="dialog-tips">
          当前用户：<strong>{{ currentUser?.username }}</strong>（{{ currentUser?.nickname || '未设置昵称' }}）
        </p>
        <el-checkbox-group v-model="selectedRoleIds" class="role-checkbox-group">
          <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id"
            :disabled="role.id === 1 && currentUser?.id !== 1" border class="role-checkbox-item">
            {{ role.roleName }}
            <span class="role-key">({{ role.roleKey }})</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="submitAssignRoles">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 2：重置密码 -->
    <el-dialog v-model="pwdDialogVisible" title="管理员重置用户密码" width="420px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="用户名">
          <el-input :value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入 6~30 位新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSubmitting" @click="submitResetPwd">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserPageApi,
  getRoleListApi,
  getUserRoleIdsApi,
  assignUserRolesApi,
  updateUserStatusApi,
  resetUserPasswordApi
} from '@/api/system'

const loading = ref(false)
const userList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: undefined
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPageApi(queryParams)
    if (res && res.data) {
      userList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error(err.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.username = ''
  queryParams.status = undefined
  handleQuery()
}

const handleStatusChange = async (row) => {
  const actionText = row.status === 1 ? '启用' : '禁用'
  try {
    await updateUserStatusApi(row.id, row.status)
    ElMessage.success(`用户 [${row.username}] 已成功${actionText}`)
  } catch (err) {
    row.status = row.status === 1 ? 0 : 1 // 失败回滚
    ElMessage.error(err.message || '更新状态失败')
  }
}

// ================= 分配角色逻辑 =================
const roleDialogVisible = ref(false)
const roleLoading = ref(false)
const roleSubmitting = ref(false)
const currentUser = ref(null)
const allRoles = ref([])
const selectedRoleIds = ref([])

const openRoleDialog = async (row) => {
  currentUser.value = row
  roleDialogVisible.value = true
  roleLoading.value = true
  try {
    const [rolesRes, userRolesRes] = await Promise.all([
      getRoleListApi(),
      getUserRoleIdsApi(row.id)
    ])
    allRoles.value = rolesRes.data || []
    selectedRoleIds.value = userRolesRes.data || []
  } catch (err) {
    ElMessage.error(err.message || '获取角色信息失败')
  } finally {
    roleLoading.value = false
  }
}

const submitAssignRoles = async () => {
  roleSubmitting.value = true
  try {
    await assignUserRolesApi(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功！')
    roleDialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '分配角色失败')
  } finally {
    roleSubmitting.value = false
  }
}

// ================= 重置密码逻辑 =================
const pwdDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  newPassword: ''
})

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur' }
  ]
}

const openResetPwdDialog = (row) => {
  currentUser.value = row
  pwdForm.newPassword = ''
  pwdDialogVisible.value = true
}

const submitResetPwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSubmitting.value = true
    try {
      await resetUserPasswordApi(currentUser.value.id, pwdForm.newPassword)
      ElMessage.success(`用户 [${currentUser.value.username}] 密码重置成功！`)
      pwdDialogVisible.value = false
    } catch (err) {
      ElMessage.error(err.message || '密码重置失败')
    } finally {
      pwdSubmitting.value = false
    }
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sys-user-container {
  padding: 16px;
}

.table-card {
  border-radius: 8px;
}

.filter-header {
  margin-bottom: 16px;
}

.pagination-box {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.font-bold {
  font-weight: 600;
}

.ml-2 {
  margin-left: 8px;
}

.role-dialog-content {
  padding: 10px 0;
}

.dialog-tips {
  margin-bottom: 16px;
  color: #64748b;
  font-size: 14px;
}

.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-checkbox-item {
  margin-right: 0 !important;
  margin-left: 0 !important;
  width: 100%;
}

.role-key {
  color: #94a3b8;
  font-size: 12px;
  margin-left: 4px;
}
</style>
