<template>
  <div class="home-container">
    <el-card class="welcome-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">🎬 NightDream AI 短剧生成平台</span>
          <el-tag type="success" effect="dark">前端工程初始化完成</el-tag>
        </div>
      </template>

      <div class="content">
        <p class="desc">技术栈：Vue 3 + Vite + Pinia + Element Plus + Axios</p>
        <el-divider />

        <div class="status-box">
          <el-alert
            title="已配置 Vite 反向代理"
            type="info"
            description="前端所有 /api 请求将自动转发到后端 http://localhost:8080"
            show-icon
            :closable="false"
          />
        </div>

        <div class="action-box">
          <el-button type="primary" @click="testApi">测试后端联调</el-button>
          <el-button @click="showMessage">Element Plus 测试</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const showMessage = () => {
  ElMessage.success('Element Plus 组件库加载正常！')
}

const testApi = async () => {
  try {
    const res = await request.get('/health')
    ElMessage.success('后端通信成功: ' + JSON.stringify(res))
  } catch (err) {
    // 后端尚未启动时的正常提示
    ElMessage.warning('后端尚未启动（稍后启动 Spring Boot 后可在此测试联调）')
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.welcome-card {
  width: 600px;
  max-width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.desc {
  color: #606266;
  font-size: 14px;
}

.status-box {
  margin: 20px 0;
}

.action-box {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
