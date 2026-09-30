import request from './request'

// ==================== 菜单与权限管理 ====================

/** 获取当前用户授权的路由菜单树 (侧边栏渲染) */
export function getRoutesApi() {
  return request({
    url: '/admin/system/menu/routes',
    method: 'get'
  })
}

/** 获取全量权限树 (供角色授权弹窗使用，含 M、C、F) */
export function getAllMenuTreeApi() {
  return request({
    url: '/admin/system/menu/tree',
    method: 'get'
  })
}

/** 获取菜单表格树 (菜单管理列表) */
export function getMenuTableApi() {
  return request({
    url: '/admin/system/menu/table',
    method: 'get'
  })
}

/** 新增菜单/按钮 */
export function addMenuApi(data) {
  return request({
    url: '/admin/system/menu/add',
    method: 'post',
    data
  })
}

/** 修改菜单/按钮 */
export function editMenuApi(data) {
  return request({
    url: '/admin/system/menu/edit',
    method: 'put',
    data
  })
}

/** 删除菜单/按钮 */
export function deleteMenuApi(id) {
  return request({
    url: `/admin/system/menu/${id}`,
    method: 'delete'
  })
}

// ==================== 角色管理 ====================

/** 获取角色列表 */
export function getRoleListApi() {
  return request({
    url: '/admin/system/role/list',
    method: 'get'
  })
}

/** 获取角色已绑定的菜单权限ID列表 */
export function getRoleMenuIdsApi(roleId) {
  return request({
    url: `/admin/system/role/${roleId}/menus`,
    method: 'get'
  })
}

/** 为角色分配菜单权限 */
export function assignRoleMenusApi(roleId, menuIds) {
  return request({
    url: `/admin/system/role/${roleId}/menus`,
    method: 'post',
    data: menuIds
  })
}

/** 新增角色 */
export function addRoleApi(data) {
  return request({
    url: '/admin/system/role/add',
    method: 'post',
    data
  })
}

/** 修改角色 */
export function editRoleApi(data) {
  return request({
    url: '/admin/system/role/edit',
    method: 'put',
    data
  })
}

/** 删除角色 */
export function deleteRoleApi(id) {
  return request({
    url: `/admin/system/role/${id}`,
    method: 'delete'
  })
}

// ==================== 用户管理 ====================

/** 分页查询用户列表 */
export function getUserPageApi(params) {
  return request({
    url: '/admin/system/user/page',
    method: 'get',
    params
  })
}

/** 获取指定用户的角色ID列表 */
export function getUserRoleIdsApi(userId) {
  return request({
    url: `/admin/system/user/${userId}/roles`,
    method: 'get'
  })
}

/** 为用户分配角色 */
export function assignUserRolesApi(userId, roleIds) {
  return request({
    url: `/admin/system/user/${userId}/roles`,
    method: 'post',
    data: { roleIds }
  })
}

/** 修改用户状态 (启用/禁用) */
export function updateUserStatusApi(userId, status) {
  return request({
    url: `/admin/system/user/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

/** 管理员重置用户密码 */
export function resetUserPasswordApi(userId, newPassword) {
  return request({
    url: `/admin/system/user/${userId}/resetPwd`,
    method: 'post',
    data: { newPassword }
  })
}
