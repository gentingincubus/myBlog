import request from './request'

/**
 * 前台门户 - 获取未删除的导航菜单列表（公开免登）
 * @param {Object} [params] { name?: string, path?: string }
 */
export function getPortalNavListApi(params) {
  return request({
    url: '/portal/nav/list',
    method: 'get',
    params
  })
}

/**
 * 管理后台 - 获取导航菜单列表
 * @param {Object} [params] { name?: string, path?: string }
 */
export function getNavListApi(params) {
  return request({
    url: '/admin/nav/list',
    method: 'get',
    params
  })
}

/**
 * 管理后台 - 新增导航菜单
 * @param {Object} data { name: string, path: string, icon?: string, sort?: number, isBlank?: number }
 */
export function addNavApi(data) {
  return request({
    url: '/admin/nav',
    method: 'post',
    data
  })
}

/**
 * 管理后台 - 修改导航菜单
 * @param {number|string} id 菜单ID
 * @param {Object} data { name: string, path: string, icon?: string, sort?: number, isBlank?: number }
 */
export function updateNavApi(id, data) {
  return request({
    url: `/admin/nav/${id}`,
    method: 'put',
    data
  })
}

/**
 * 管理后台 - 逻辑删除导航菜单
 * @param {number|string} id 菜单ID
 */
export function deleteNavApi(id) {
  return request({
    url: `/admin/nav/${id}`,
    method: 'delete'
  })
}

export const navApi = {
  list: getNavListApi,
  portalList: getPortalNavListApi,
  add: addNavApi,
  update: updateNavApi,
  delete: deleteNavApi
}
