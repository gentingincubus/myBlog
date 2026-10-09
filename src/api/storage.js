import request from './request'

/**
 * 获取存储桶全局体检与使用量统计 (总容量、总文件数、孤儿废图统计)
 */
export function getStorageStatsApi() {
  return request({
    url: '/admin/storage/stats',
    method: 'get'
  })
}

/**
 * 浏览存储桶文件明细列表
 * @param {Object} [params] { prefix?: string, onlyOrphan?: boolean }
 */
export function listStorageFilesApi(params) {
  return request({
    url: '/admin/storage/files',
    method: 'get',
    params
  })
}

/**
 * 手动物理删除存储桶中的单个文件
 * @param {string} url 文件直链
 */
export function deleteStorageFileApi(url) {
  return request({
    url: '/admin/storage/file',
    method: 'delete',
    params: { url }
  })
}

/**
 * 一键全自动批量清理所有孤儿垃圾文件 (释放 R2 空间)
 */
export function cleanOrphanFilesApi() {
  return request({
    url: '/admin/storage/clean-orphans',
    method: 'post'
  })
}

