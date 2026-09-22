import request from './request'

/**
 * 获取站点基础信息
 */
export function getSiteInfoApi() {
  return request({
    url: '/site/info',
    method: 'get'
  })
}
