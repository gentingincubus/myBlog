<template>
  <div class="home-container">
    <div class="cards-wrapper">
      <!-- 快捷横幅：进入管理后台 -->
      <div class="admin-portal-banner">
        <div class="banner-left">
          <span class="banner-badge">PRO</span>
          <span class="banner-text">现代化后台管理已上线（侧边栏布局 + 路由守卫 + 独立控制台）</span>
        </div>
        <div class="banner-right">
          <el-button type="primary" size="default" icon="Management" @click="$router.push('/admin')">
            进入管理后台
          </el-button>
          <el-button v-if="!userStore.token" size="default" icon="User" @click="$router.push('/login')">
            登录 / 注册
          </el-button>
        </div>
      </div>

      <!-- 站点信息卡片 -->
      <el-card class="box-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">🌿 站点概览 (MyBlog)</span>
            <el-tag type="success" effect="dark">后端已连通</el-tag>
          </div>
        </template>

        <div class="content">
          <div class="status-box">
            <el-alert v-if="!siteData" title="点击下方按钮，测试 /api/site/info 接口" type="info" show-icon :closable="false" />
            <el-descriptions v-else :column="1" border>
              <el-descriptions-item label="站点标题">{{ siteData.title }}</el-descriptions-item>
              <el-descriptions-item label="站长">{{ siteData.owner }}</el-descriptions-item>
              <el-descriptions-item label="运行状态">
                <el-tag type="success">{{ siteData.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="版本号">{{ siteData.version }}</el-descriptions-item>
              <el-descriptions-item label="当前鉴权访问者">
                <el-tag type="primary" effect="plain">{{ siteData.currentUser || '未知' }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="action-box">
            <el-button type="primary" :loading="loadingSite" @click="fetchSiteInfo">刷新站点信息</el-button>
          </div>
        </div>
      </el-card>

      <!-- 用户登录与 Token 鉴权测试卡片 -->
      <el-card class="box-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">🔐 登录与 JWT Token 测试</span>
            <el-tag :type="userStore.token ? 'success' : 'warning'" effect="dark">
              {{ userStore.token ? '已登录' : '未登录' }}
            </el-tag>
          </div>
        </template>

        <div class="content">
          <!-- 未登录：提供登录与注册切换卡片 -->
          <div v-if="!userStore.token">
            <el-tabs v-model="activeTab" class="auth-tabs">
              <!-- 登录 Tab -->
              <el-tab-pane label="🔑 账号登录" name="login">
                <el-form :model="loginForm" label-width="70px" style="margin-top: 15px">
                  <el-form-item label="账号">
                    <el-input v-model="loginForm.username" placeholder="请输入用户名 (如 admin)" />
                  </el-form-item>
                  <el-form-item label="密码">
                    <el-input v-model="loginForm.password" type="password" show-password
                      placeholder="请输入密码 (如 123456)" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="loadingLogin" @click="handleLogin" style="width: 100%">
                      立即登录（校验 BCrypt 并生成 JWT）
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <!-- 注册 Tab -->
              <el-tab-pane label="📝 新用户注册" name="register">
                <el-form :model="registerForm" label-width="70px" style="margin-top: 15px">
                  <el-form-item label="账号">
                    <el-input v-model="registerForm.username" placeholder="3~20 位字母/数字 (如 testuser)" />
                  </el-form-item>
                  <el-form-item label="密码">
                    <el-input v-model="registerForm.password" type="password" show-password
                      placeholder="6~30 位字符 (如 123456)" />
                  </el-form-item>
                  <el-form-item label="昵称">
                    <el-input v-model="registerForm.nickname" placeholder="用户昵称 (选填，默认同账号)" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="success" :loading="loadingRegister" @click="handleRegister" style="width: 100%">
                      立即注册（BCrypt 密文落库）
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 登录成功展示面板 -->
          <div v-else class="logged-panel">
            <el-result icon="success" title="登录成功！" sub-title="已成功从后端颁发 JWT 数字通行证">
              <template #extra>
                <div class="token-display">
                  <p class="token-title">🔑 本地存储的 JWT Token (截取预览):</p>
                  <el-input :model-value="userStore.token" type="textarea" :rows="2" readonly />
                  <p class="user-desc">
                    👤 登录用户：<b>{{ userStore.userInfo?.nickname }}</b> ({{ userStore.userInfo?.username }})
                  </p>
                </div>
                <el-button type="danger" plain @click="handleLogout" style="margin-top: 15px">
                  退出登录（清除 Token）
                </el-button>
              </template>
            </el-result>
          </div>
        </div>
      </el-card>

      <!-- 站点导航管理卡片 (雪花ID + 审计填充 + 逻辑删除 + Validation + 修改 + 新增) -->
      <el-card v-if="userStore.token" class="box-card full-width-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">🧭 站点导航菜单管理 (全套 CRUD + 企业级规范实战)</span>
            <div>
              <el-button type="primary" size="small" @click="openAddDialog">➕ 新增菜单</el-button>
              <el-button type="info" size="small" plain @click="fetchNavList" :loading="loadingNavs">🔄 刷新</el-button>
            </div>
          </div>
        </template>

        <div class="nav-content">
          <!-- 🔍 条件筛选搜索栏 -->
          <div class="search-filter-bar">
            <el-form :inline="true" :model="searchNavForm" class="filter-form">
              <el-form-item label="菜单名称">
                <el-input v-model="searchNavForm.name" placeholder="输入名称模糊搜索" clearable @clear="fetchNavList"
                  @keyup.enter="fetchNavList" style="width: 170px;" />
              </el-form-item>
              <el-form-item label="路由路径">
                <el-input v-model="searchNavForm.path" placeholder="输入路径模糊搜索" clearable @clear="fetchNavList"
                  @keyup.enter="fetchNavList" style="width: 170px;" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loadingNavs" @click="fetchNavList">
                  🔍 搜索
                </el-button>
                <el-button @click="handleResetSearch">
                  🔄 重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 导航菜单数据表格 -->
          <el-table :data="navList" border stripe v-loading="loadingNavs" style="width: 100%">
            <el-table-column prop="id" label="雪花ID (19位 Long)" min-width="180">
              <template #default="{ row }">
                <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="菜单名称与图标" min-width="140">
              <template #default="{ row }">
                <span>{{ row.icon }} <b>{{ row.name }}</b></span>
              </template>
            </el-table-column>
            <el-table-column prop="path" label="跳转路由" min-width="120" />
            <el-table-column label="排序权重 (计算值)" width="125" align="center">
              <template #default="{ row }">
                <el-tooltip :content="'原始值: ' + row.sort + ' (VO计算: * 10)'" placement="top">
                  <el-tag type="warning" effect="plain">{{ row.displaySort }} (原: {{ row.sort }})</el-tag>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="创建人 (内存组装)" min-width="170" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.creatorName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间 (自动注入)" width="175">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="更新时间 (自动刷新)" width="175">
              <template #default="{ row }">
                {{ formatTime(row.updateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog(row)">修改</el-button>
                <el-popconfirm title="确定要删除该导航菜单吗？" @confirm="handleDeleteNav(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">逻辑删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 🧪 Redisson 企业级分布式高并发演练场 -->
      <el-card v-if="userStore.token" class="box-card full-width-card lab-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">🧪 Redisson 企业级高并发演练实验室</span>
            <el-tag type="danger" effect="dark" size="small">Redisson 3.35.0 实战</el-tag>
          </div>
        </template>

        <div class="lab-body">
          <div class="lab-section">
            <div class="section-header">
              <span class="badge">1️⃣ RRateLimiter 分布式高压限流器</span>
              <el-tag type="warning" size="small">全站 1 分钟仅限 1 次通行</el-tag>
            </div>
            <p class="lab-tip">
              💡 <b>防刷原理解密</b>：基于 Redis 令牌桶算法。点击测试！第一次将顺利秒过；1分钟内再次点击，将被 Redisson 令牌桶当场拦截并提示限流！
            </p>
            <div class="lab-actions">
              <el-button type="warning" :loading="loadingLimiter" @click="handleTestRateLimit">
                ⚡ 发起限流测试（1分钟/次）
              </el-button>
              <el-button type="info" plain :loading="loadingReset" @click="handleResetLimiter">
                🔄 重置限流器（清空令牌桶）
              </el-button>
            </div>
          </div>

          <el-divider />

          <div class="lab-section">
            <div class="section-header">
              <span class="badge">2️⃣ RLock 分布式互斥排队锁</span>
              <el-tag type="danger" size="small">任务独占 5 秒 + 看门狗自动续期</el-tag>
            </div>
            <p class="lab-tip">
              💡 <b>并发排队解密</b>：任务执行需独占 5 秒。你可以打开两个浏览器标签页同时点击，第二个人会被强行阻塞排队，等待第一个人释放锁后自动接力执行！
            </p>
            <div class="lab-actions">
              <el-button type="danger" :loading="loadingLock" @click="handleTestLockQueue">
                🔒 发起独占任务并排队（占用 5 秒）
              </el-button>
            </div>
          </div>

          <el-divider />

          <!-- 3️⃣ 数据库事务 @Transactional 对比演练 -->
          <div class="lab-section">
            <div class="section-header">
              <span class="badge">3️⃣ 数据库事务（@Transactional）对比演练</span>
              <el-tag type="success" size="small">数据一致性底线保障</el-tag>
            </div>
            <p class="lab-tip">
              💡 <b>实验场景</b>：模拟批量插入两项导航数据，但在插入第1条后<b>中途故意模拟系统严重崩溃</b>！
              对比观察【无事务】时的脏数据残留，与【有事务 @Transactional】时的全自动时光倒流（Rollback）。
            </p>
            <div class="lab-actions">
              <el-button type="danger" plain :loading="loadingNoTx" @click="handleTestNoTx">
                💥 实验 A：无事务（中途报错看脏数据）
              </el-button>
              <el-button type="success" :loading="loadingWithTx" @click="handleTestWithTx">
                🛡️ 实验 B：有事务（@Transactional 自动回滚）
              </el-button>
              <el-button type="info" plain :loading="loadingCleanTx" @click="handleCleanTx">
                🧹 一键清理演练脏数据
              </el-button>
            </div>
          </div>

          <div v-if="labLogs.length > 0" class="lab-console">
            <div class="console-title">📜 实时实验控制台日志：</div>
            <div v-for="(log, idx) in labLogs" :key="idx" class="console-line" :class="log.type">
              <span class="log-time">[{{ log.time }}]</span> {{ log.msg }}
            </div>
          </div>
        </div>
      </el-card>

      <!-- 新增 / 修改 导航菜单 Dialog 弹窗 -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '✏️ 修改导航菜单' : '➕ 新增导航菜单'" width="460px" destroy-on-close>
        <el-form :model="navForm" label-width="90px">
          <el-form-item label="菜单名称" required>
            <el-input v-model="navForm.name" placeholder="如：花活工坊 (必填)" />
          </el-form-item>
          <el-form-item label="路由路径" required>
            <el-input v-model="navForm.path" placeholder="如：/playground (必填)" />
          </el-form-item>
          <el-form-item label="菜单图标">
            <el-input v-model="navForm.icon" placeholder="Emoji 或图标名 (如：🧪、🛠️)" />
          </el-form-item>
          <el-form-item label="排序权重">
            <el-input-number v-model="navForm.sort" :min="0" :max="999" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="loadingSubmit" @click="handleSubmitNav">
              确定保存
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSiteInfoApi } from '@/api/site'
import { loginApi, registerApi } from '@/api/auth'
import { getNavListApi, addNavApi, updateNavApi, deleteNavApi } from '@/api/nav'
import { testRateLimitApi, resetLimiterApi, testLockQueueApi, testNoTxApi, testWithTxApi, cleanTxTestDataApi } from '@/api/lab'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 站点信息状态
const siteData = ref(null)
const loadingSite = ref(false)

const fetchSiteInfo = async () => {
  loadingSite.value = true
  try {
    const res = await getSiteInfoApi()
    siteData.value = res.data || res
    ElMessage.success('成功从后端获取站点数据！')
  } catch (err) {
    siteData.value = null
    // 错误已由 request.js 响应拦截器统一弹窗提示，此处无需重复弹窗
  } finally {
    loadingSite.value = false
  }
}

// 标签页切换状态
const activeTab = ref('login')

// 登录表单数据
const loginForm = reactive({
  username: 'admin',
  password: '123456'
})
const loadingLogin = ref(false)

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  nickname: ''
})
const loadingRegister = ref(false)

// 格式化时间显示（去掉中间的 T）
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ')
}

// 导航菜单 CRUD 状态
const navList = ref([])
const loadingNavs = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)
const loadingSubmit = ref(false)

// 导航搜索筛选条件
const searchNavForm = reactive({
  name: '',
  path: ''
})

const navForm = reactive({
  name: '',
  path: '',
  icon: '',
  sort: 1
})

// 获取导航菜单列表（支持名称与路由筛选）
const fetchNavList = async () => {
  if (!userStore.token) return
  loadingNavs.value = true
  try {
    const params = {}
    if (searchNavForm.name && searchNavForm.name.trim()) {
      params.name = searchNavForm.name.trim()
    }
    if (searchNavForm.path && searchNavForm.path.trim()) {
      params.path = searchNavForm.path.trim()
    }
    const res = await getNavListApi(params)
    navList.value = res.data || []
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loadingNavs.value = false
  }
}

// 重置筛选条件
const handleResetSearch = () => {
  searchNavForm.name = ''
  searchNavForm.path = ''
  fetchNavList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  currentEditId.value = null
  navForm.name = ''
  navForm.path = ''
  navForm.icon = '🌟'
  navForm.sort = navList.value.length + 1
  dialogVisible.value = true
}

// 打开修改弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  currentEditId.value = row.id
  navForm.name = row.name
  navForm.path = row.path
  navForm.icon = row.icon
  navForm.sort = row.sort
  dialogVisible.value = true
}

// 提交保存（新增或修改）
const handleSubmitNav = async () => {
  loadingSubmit.value = true
  try {
    if (isEdit.value) {
      // 执行修改 (会触发 updateTime 自动刷新)
      const res = await updateNavApi(currentEditId.value, navForm)
      if (res.status) {
        ElMessage.success('🎉 导航修改成功！updateTime 已自动更新')
        dialogVisible.value = false
        // fetchNavList()
      }
    } else {
      // 执行新增 (会触发雪花算法 ID、createTime、createBy 自动注入)
      const res = await addNavApi(navForm)
      if (res.status) {
        ElMessage.success(`🎉 导航新增成功！雪花算法ID: ${res.data}`)
        dialogVisible.value = false
        fetchNavList()
      }
    }
  } catch (err) {
    // Validation 参数校验失败会自动弹窗提示
  } finally {
    loadingSubmit.value = false
  }
}

// 逻辑删除
const handleDeleteNav = async (id) => {
  try {
    const res = await deleteNavApi(id)
    if (res.status) {
      ElMessage.success('已成功逻辑删除（数据库保留但 deleted 变为 1）')
      fetchNavList()
    }
  } catch (err) {
    // 错误处理
  }
}

// 监听登录状态变化，登录后自动加载菜单列表
watch(() => userStore.token, (newToken) => {
  if (newToken) {
    fetchNavList()
  } else {
    navList.value = []
  }
}, { immediate: true })

// 执行登录
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loadingLogin.value = true
  try {
    const res = await loginApi(loginForm)
    if (res.status) {
      // 保存 Token 和用户信息到 Pinia / localStorage
      userStore.setLoginData(res.data.token, res.data.userInfo)
      ElMessage.success(`欢迎回来，${res.data.userInfo.nickname}！`)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (err) {
    // 错误已被拦截器统一处理
  } finally {
    loadingLogin.value = false
  }
}

// 执行注册
const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }

  loadingRegister.value = true
  try {
    const res = await registerApi(registerForm)
    if (res.status) {
      ElMessage.success('🎉 注册成功！已自动填充并切换到登录界面')
      // 自动填充登录表单并切换回登录 Tab
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password
      activeTab.value = 'login'
      // 清空注册表单
      registerForm.username = ''
      registerForm.password = ''
      registerForm.nickname = ''
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (err) {
    // 错误由拦截器统一弹窗展示
  } finally {
    loadingRegister.value = false
  }
}

// 退出登录
const handleLogout = () => {
  userStore.clearToken()
  siteData.value = null
  ElMessage.info('已安全退出登录')
}

// ===================================
// 🧪 Redisson 分布式演练实验室逻辑
// ===================================
const loadingLimiter = ref(false)
const loadingReset = ref(false)
const loadingLock = ref(false)
const labLogs = ref([])

const addLabLog = (msg, type = 'info') => {
  const time = new Date().toLocaleTimeString()
  labLogs.value.unshift({ time, msg, type })
  if (labLogs.value.length > 8) labLogs.value.pop()
}

// 1. 发起高压限流测试
const handleTestRateLimit = async () => {
  loadingLimiter.value = true
  try {
    const res = await testRateLimitApi()
    ElMessage.success(res.data.message)
    addLabLog(res.data.message, 'success')
  } catch (err) {
    addLabLog(err.message || '触发限流拦截', 'error')
  } finally {
    loadingLimiter.value = false
  }
}

// 重置限流器
const handleResetLimiter = async () => {
  loadingReset.value = true
  try {
    const res = await resetLimiterApi()
    ElMessage.info(res.data)
    addLabLog(res.data, 'info')
  } catch (err) {
    // 错误由拦截器处理
  } finally {
    loadingReset.value = false
  }
}

// 2. 发起独占分布式排队锁测试
const handleTestLockQueue = async () => {
  loadingLock.value = true
  addLabLog('⏳ 正在向 Redisson 申请分布式锁并加入排队...', 'warning')
  try {
    const res = await testLockQueueApi()
    ElMessage.success(res.data.message)
    addLabLog(`${res.data.message} [线程: ${res.data.thread}, 耗时: ${res.data.waitTimeMs}ms]`, 'success')
  } catch (err) {
    addLabLog(err.message || '排队超时或失败', 'error')
  } finally {
    loadingLock.value = false
  }
}

// ===================================
// 🏛️ 数据库事务 @Transactional 实验逻辑
// ===================================
const loadingNoTx = ref(false)
const loadingWithTx = ref(false)
const loadingCleanTx = ref(false)

// 实验 A：无事务
const handleTestNoTx = async () => {
  loadingNoTx.value = true
  addLabLog('⚠️ 正在执行【无事务】插入，中途故意模拟系统崩溃...', 'warning')
  try {
    await testNoTxApi(true)
  } catch (err) {
    addLabLog(`💥 业务抛出异常: ${err.message}`, 'error')
    addLabLog('⚠️ 【翻车现场】：由于没有事务，请看上方表格，第1条【无事务测试】已沦为脏数据强行存入！', 'error')
    fetchNavList()
  } finally {
    loadingNoTx.value = false
  }
}

// 实验 B：有事务
const handleTestWithTx = async () => {
  loadingWithTx.value = true
  addLabLog('🛡️ 正在执行【@Transactional 事务保障】插入，中途同样模拟系统崩溃...', 'info')
  try {
    await testWithTxApi(true)
  } catch (err) {
    addLabLog(`💥 业务抛出异常: ${err.message}`, 'error')
    addLabLog('🛡️ 【回滚生效】：Spring 感知到异常，全自动触发 Rollback！已插入的第1条数据被干净撤销，零脏数据残留！', 'success')
    fetchNavList()
  } finally {
    loadingWithTx.value = false
  }
}

// 清理演练脏数据
const handleCleanTx = async () => {
  loadingCleanTx.value = true
  try {
    const res = await cleanTxTestDataApi()
    ElMessage.success(res.data)
    addLabLog(res.data, 'info')
    fetchNavList()
  } catch (err) {
    // 错误处理
  } finally {
    loadingCleanTx.value = false
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
  padding: 30px 20px;
}

.cards-wrapper {
  display: flex;
  gap: 24px;
  max-width: 900px;
  width: 100%;
  flex-wrap: wrap;
}

.box-card {
  flex: 1;
  min-width: 360px;
}

.full-width-card {
  flex: 1 1 100%;
  width: 100%;
}

.nav-content {
  margin-top: 10px;
}

.nav-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.status-box {
  margin: 15px 0;
}

.action-box {
  display: flex;
  justify-content: flex-end;
}

.logged-panel {
  text-align: left;
}

.token-display {
  background: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  margin-top: 10px;
  text-align: left;
}

.token-title {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 8px 0;
}

.user-desc {
  font-size: 14px;
  color: #334155;
  margin: 10px 0 0 0;
}

.search-filter-bar {
  background-color: #f8fafc;
  padding: 14px 16px 0 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  border: 1px solid #f1f5f9;
}

.lab-card {
  margin-top: 25px;
  border-top: 3px solid #f56c6c;
}

.lab-body {
  text-align: left;
}

.lab-section {
  padding: 8px 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.badge {
  font-size: 15px;
  font-weight: bold;
  color: #1e293b;
}

.lab-tip {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 14px 0;
  line-height: 1.5;
}

.lab-actions {
  display: flex;
  gap: 12px;
}

.lab-console {
  margin-top: 20px;
  background: #0f172a;
  border-radius: 8px;
  padding: 14px 16px;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 13px;
  color: #e2e8f0;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}

.console-title {
  color: #94a3b8;
  font-size: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid #334155;
  padding-bottom: 4px;
}

.console-line {
  line-height: 1.8;
}

.console-line.success {
  color: #4ade80;
}

.console-line.error {
  color: #f87171;
}

.console-line.warning {
  color: #fbbf24;
}

.console-line.info {
  color: #38bdf8;
}

.log-time {
  color: #64748b;
  margin-right: 6px;
}

.admin-portal-banner {
  grid-column: 1 / -1;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.08);
  flex-wrap: wrap;
  gap: 12px;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.banner-badge {
  background: #38bdf8;
  color: #0f172a;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 4px;
}

.banner-text {
  color: #f1f5f9;
  font-size: 14px;
  font-weight: 500;
}

.banner-right {
  display: flex;
  gap: 10px;
}
</style>
