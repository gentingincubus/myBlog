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

/**
 * 修改当前登录用户密码（无需旧密码，需确认两次新密码）
 * @param {Object} data { newPassword: string, confirmPassword: string }
 */
export function changePasswordApi(data) {
  return request({
    url: '/user/password',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息（含角色与权限标识集合）
 */
export function getUserInfoApi() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export const authApi = {
  login: loginApi,
  register: registerApi,
  changePassword: changePasswordApi,
  getUserInfo: getUserInfoApi
}

