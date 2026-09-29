import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfoApi } from '@/api/auth'
import { getRoutesApi } from '@/api/system'

export const TOKEN_KEY = 'myblog_token'
export const USER_INFO_KEY = 'myblog_user_info'
export const ROLES_KEY = 'myblog_roles'
export const PERMS_KEY = 'myblog_perms'

function getStoredJson(key, defaultValue) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw || raw === 'undefined' || raw === 'null') {
      return defaultValue
    }
    return JSON.parse(raw)
  } catch (e) {
    localStorage.removeItem(key)
    return defaultValue
  }
}

function getStoredToken() {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token || token === 'undefined' || token === 'null') {
    return ''
  }
  return token
}

export const useUserStore = defineStore('user', () => {
  const token = ref(getStoredToken())
  const userInfo = ref(getStoredJson(USER_INFO_KEY, null))
  const roles = ref(getStoredJson(ROLES_KEY, []))
  const permissions = ref(getStoredJson(PERMS_KEY, []))
  const menuRoutes = ref([])

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
   * 拉取当前登录用户的综合信息（角色、权限列表）
   */
  async function fetchUserInfo() {
    if (!token.value) return null
    try {
      const res = await getUserInfoApi()
      if (res && res.data) {
        userInfo.value = res.data.user
        roles.value = Array.from(res.data.roles || [])
        permissions.value = Array.from(res.data.permissions || [])

        localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value))
        localStorage.setItem(ROLES_KEY, JSON.stringify(roles.value))
        localStorage.setItem(PERMS_KEY, JSON.stringify(permissions.value))
        return res.data
      }
    } catch (e) {
      console.error('拉取用户信息失败', e)
    }
    return null
  }

  /**
   * 拉取当前登录用户被授权的菜单路由树
   */
  async function fetchMenuRoutes() {
    if (!token.value) {
      menuRoutes.value = []
      return []
    }
    try {
      const res = await getRoutesApi()
      if (res && res.data) {
        menuRoutes.value = res.data
        return res.data
      }
    } catch (e) {
      console.error('拉取菜单路由树失败', e)
    }
    return []
  }

  /**
   * 退出登录，彻底清空本地缓存
   */
  function clearToken() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    menuRoutes.value = []
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(ROLES_KEY)
    localStorage.removeItem(PERMS_KEY)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    menuRoutes,
    setLoginData,
    fetchUserInfo,
    fetchMenuRoutes,
    clearToken
  }
})

