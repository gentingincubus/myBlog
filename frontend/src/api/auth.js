import request from './request'

/**
 * 用户登录
 * @param {Object} data { username: string, password: string }
 */
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data { username: string, password: string, nickname?: string }
 */
export function registerApi(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const authApi = {
  login: loginApi,
  register: registerApi
}

