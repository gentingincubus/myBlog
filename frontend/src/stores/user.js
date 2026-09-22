import { defineStore } from 'pinia'
import { ref } from 'vue'

export const TOKEN_KEY = 'myblog_token'
export const USER_INFO_KEY = 'myblog_user_info'

/**
 * 健壮地从 localStorage 解析用户信息，避免 "undefined" 导致 JSON.parse 崩溃
 */
function getStoredUserInfo() {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    if (!raw || raw === 'undefined' || raw === 'null') {
      return null
    }
    return JSON.parse(raw)
  } catch (e) {
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

/**
 * 健壮地从 localStorage 获取 Token，过滤 "undefined" 字符串
 */
function getStoredToken() {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token || token === 'undefined' || token === 'null') {
    return ''
  }
  return token
}

export const useUserStore = defineStore('user', () => {
  const token = ref(getStoredToken())
  const userInfo = ref(getStoredUserInfo())

  /**
   * 保存登录成功的 Token 和用户信息
   */
  function setLoginData(newToken, newUserInfo) {
    const safeToken = newToken || ''
    const safeUserInfo = newUserInfo || null

    token.value = safeToken
    userInfo.value = safeUserInfo

    if (safeToken) {
      localStorage.setItem(TOKEN_KEY, safeToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }

    if (safeUserInfo) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(safeUserInfo))
    } else {
      localStorage.removeItem(USER_INFO_KEY)
    }
  }

  /**
   * 退出登录，彻底清空本地缓存
   */
  function clearToken() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  return { token, userInfo, setLoginData, clearToken }
})
