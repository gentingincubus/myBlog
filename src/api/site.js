import request from './request'

/**
 * 前台门户 - 获取站点基础信息（公开免登）
 */
export function getSiteInfoApi() {
  return request({
    url: '/portal/site/info',
    method: 'get'
  })
}
