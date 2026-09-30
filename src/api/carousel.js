import request from './request'

/**
 * 前台门户 - 获取已启用的轮播图列表 (公开免登)
 */
export function getPortalCarouselListApi() {
  return request({
    url: '/portal/carousel/list',
    method: 'get'
  })
}

/**
 * 管理后台 - 获取轮播图列表
 * @param {Object} [params] { title?: string, status?: number }
 */
export function getAdminCarouselListApi(params) {
  return request({
    url: '/admin/carousel/list',
    method: 'get',
    params
  })
}

/**
 * 管理后台 - 新增轮播图
 * @param {Object} data { title: string, subtitle?: string, coverUrl: string, content: string, sort?: number, status?: number }
 */
export function addCarouselApi(data) {
  return request({
    url: '/admin/carousel',
    method: 'post',
    data
  })
}

/**
 * 管理后台 - 修改轮播图
 * @param {number|string} id
 * @param {Object} data
 */
export function updateCarouselApi(id, data) {
  return request({
    url: `/admin/carousel/${id}`,
    method: 'put',
    data
  })
}

/**
 * 管理后台 - 逻辑删除轮播图
 * @param {number|string} id
 */
export function deleteCarouselApi(id) {
  return request({
    url: `/admin/carousel/${id}`,
    method: 'delete'
  })
}

/**
 * 管理后台 - 更新启停状态
 * @param {number|string} id
 * @param {number} status 1: 启用, 0: 禁用
 */
export function updateCarouselStatusApi(id, status) {
  return request({
    url: `/admin/carousel/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const carouselApi = {
  portalList: getPortalCarouselListApi,
  list: getAdminCarouselListApi,
  add: addCarouselApi,
  update: updateCarouselApi,
  delete: deleteCarouselApi,
  updateStatus: updateCarouselStatusApi
}
