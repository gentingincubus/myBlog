import request from './request'

// ==========================================
// 1. VR 分类 / 园区管理 API
// ==========================================

/**
 * 获取 VR 分类列表
 * @param {Object} [params] { name?: string, code?: string, status?: number }
 */
export function getVrCategoryListApi(params) {
  return request({
    url: '/vr/category/list',
    method: 'get',
    params
  })
}

/**
 * 获取单个 VR 分类详情
 * @param {string|number} id 分类 ID
 */
export function getVrCategoryDetailApi(id) {
  return request({
    url: `/vr/category/detail/${id}`,
    method: 'get'
  })
}

/**
 * 新增 VR 分类
 * @param {Object} data { name: string, code: string, mapUrl?: string, description?: string, sort?: number, status?: number }
 */
export function addVrCategoryApi(data) {
  return request({
    url: '/vr/category',
    method: 'post',
    data
  })
}

/**
 * 修改 VR 分类
 * @param {string|number} id 分类 ID
 * @param {Object} data { name: string, code: string, mapUrl?: string, description?: string, sort?: number, status?: number }
 */
export function updateVrCategoryApi(id, data) {
  return request({
    url: `/vr/category/${id}`,
    method: 'put',
    data
  })
}

/**
 * 逻辑删除 VR 分类
 * @param {string|number} id 分类 ID
 */
export function deleteVrCategoryApi(id) {
  return request({
    url: `/vr/category/${id}`,
    method: 'delete'
  })
}

// ==========================================
// 2. VR 全景场景点位 API
// ==========================================

/**
 * 获取 VR 场景列表
 * @param {Object} [params] { categoryId?: string|number, name?: string, status?: number }
 */
export function getVrSceneListApi(params) {
  return request({
    url: '/vr/scene/list',
    method: 'get',
    params
  })
}

/**
 * 获取单个 VR 场景详情
 * @param {string|number} id 场景 ID
 */
export function getVrSceneDetailApi(id) {
  return request({
    url: `/vr/scene/detail/${id}`,
    method: 'get'
  })
}

/**
 * 新增 VR 场景
 * @param {Object} data { categoryId: string|number, name: string, panoramaUrl: string, previewUrl?: string, topPercent?: number, leftPercent?: number, initialDeg?: number, sort?: number, status?: number }
 */
export function addVrSceneApi(data) {
  return request({
    url: '/vr/scene',
    method: 'post',
    data
  })
}

/**
 * 修改 VR 场景
 * @param {string|number} id 场景 ID
 * @param {Object} data
 */
export function updateVrSceneApi(id, data) {
  return request({
    url: `/vr/scene/${id}`,
    method: 'put',
    data
  })
}

/**
 * 逻辑删除 VR 场景
 * @param {string|number} id 场景 ID
 */
export function deleteVrSceneApi(id) {
  return request({
    url: `/vr/scene/${id}`,
    method: 'delete'
  })
}

// ==========================================
// 3. 通用图片上传 API (直传 Cloudflare R2)
// ==========================================

/**
 * 上传图片文件
 * @param {File} file 文件对象
 * @param {'vr_panorama'|'vr_preview'|'vr_map'|'avatar'|'blog_image'} type 业务类型
 */
export function uploadImageApi(file, type = 'blog_image') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const vrApi = {
  // 分类
  categoryList: getVrCategoryListApi,
  categoryDetail: getVrCategoryDetailApi,
  addCategory: addVrCategoryApi,
  updateCategory: updateVrCategoryApi,
  deleteCategory: deleteVrCategoryApi,
  // 场景
  sceneList: getVrSceneListApi,
  sceneDetail: getVrSceneDetailApi,
  addScene: addVrSceneApi,
  updateScene: updateVrSceneApi,
  deleteScene: deleteVrSceneApi,
  // 上传
  uploadImage: uploadImageApi
}

