import { useUserStore } from '@/stores/user'

/**
 * 按钮级细粒度权限控制自定义指令
 * 用法：
 * <el-button v-hasPermi="['vr:scene:add']">新增场景</el-button>
 * <el-button v-hasPermi="['vr:scene:edit', 'vr:scene:delete']">批量操作</el-button>
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []

    if (value && Array.isArray(value) && value.length > 0) {
      const requiredPermissions = value
      // 超级管理员特权标识 *:*:* 无条件放行
      const hasPermission =
        permissions.includes('*:*:*') ||
        requiredPermissions.some(perm => permissions.includes(perm))

      if (!hasPermission) {
        // 无权限则直接从 DOM 树彻底移除该元素，保证界面安全与干净
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请指定权限标识，例如 v-hasPermi="['vr:scene:add']"`)
    }
  }
}

