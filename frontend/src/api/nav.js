import request from './request'

/**
 * 获取未删除的导航菜单列表（支持名称、路由模糊搜索）
 * @param {Object} [params] { name?: string, path?: string }
 */
export function getNavListApi(params) {
  return request({
    url: '/nav/list',
    method: 'get',
    params
  })
}

/**
 * 新增导航菜单
 * @param {Object} data { name: string, path: string, icon?: string, sort?: number, isBlank?: number }
 */
export function addNavApi(data) {
  return request({
    url: '/nav',
    method: 'post',
    data
  })
}

/**
 * 修改导航菜单
 * @param {number|string} id 菜单ID
 * @param {Object} data { name: string, path: string, icon?: string, sort?: number, isBlank?: number }
 */
export function updateNavApi(id, data) {
  return request({
    url: `/nav/${id}`,
    method: 'put',
    data
  })
}

/**
 * 逻辑删除导航菜单
 * @param {number|string} id 菜单ID
 */
export function deleteNavApi(id) {
  return request({
    url: `/nav/${id}`,
    method: 'delete'
  })
}

export const navApi = {
  list: getNavListApi,
  add: addNavApi,
  update: updateNavApi,
  delete: deleteNavApi
}
