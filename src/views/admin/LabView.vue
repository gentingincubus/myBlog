<template>
  <div class="lab-page">
    <el-card shadow="never" class="lab-card">
      <template #header>
        <div class="card-header">
          <span class="title"><el-icon><Cpu /></el-icon> 高并发与事务安全演练实验室</span>
          <el-tag type="danger" effect="dark" size="small">Redisson 3.35.0 + Spring Tx 实战</el-tag>
        </div>
      </template>

      <!-- 实验 1：RRateLimiter 分布式高压限流器 -->
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

      <!-- 实验 2：RLock 分布式互斥排队锁 -->
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

      <!-- 实验 3：数据库事务 @Transactional 对比演练 -->
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
            💥 执行【无事务】插入（故意崩溃看脏数据残留）
          </el-button>
          <el-button type="success" :loading="loadingWithTx" @click="handleTestWithTx">
            🛡️ 执行【有事务】插入（崩溃后自动回滚干干净净）
          </el-button>
          <el-button type="info" plain :loading="loadingClean" @click="handleCleanTxData">
            🧹 磁盘物理删除（清理测试垃圾数据）
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- 🖥️ 实时终端监视器 -->
      <div class="terminal-monitor">
        <div class="terminal-header">
          <div class="mac-dots">
            <span class="dot red"></span>
            <span class="dot yellow"></span>
            <span class="dot green"></span>
          </div>
          <span class="terminal-title">🖥️ 实时高并发演练与调度控制台日志</span>
          <el-button link type="primary" size="small" @click="clearLogs">清空日志</el-button>
        </div>
        <div ref="terminalBodyRef" class="terminal-body">
          <div v-if="labLogs.length === 0" class="log-empty">
            暂无演练活动日志，点击上方实验按钮开始观察高并发与事务调度...
          </div>
          <div v-for="(log, idx) in labLogs" :key="idx" class="log-line" :class="log.type">
            <span class="log-time">[{{ log.time }}]</span>
            <span class="log-text">{{ log.text }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { labApi } from '@/api/lab'

const loadingLimiter = ref(false)
const loadingReset = ref(false)
const loadingLock = ref(false)
const loadingNoTx = ref(false)
const loadingWithTx = ref(false)
const loadingClean = ref(false)

const labLogs = ref([
  { time: getCurrentTime(), type: 'info', text: '🚀 演练实验室监控引擎就绪，等待下发高并发实验指令...' }
])
const terminalBodyRef = ref(null)

function getCurrentTime() {
  const d = new Date()
  return d.toTimeString().split(' ')[0] + '.' + String(d.getMilliseconds()).padStart(3, '0')
}

function appendLog(type, text) {
  labLogs.value.push({
    time: getCurrentTime(),
    type,
    text
  })
  nextTick(() => {
    if (terminalBodyRef.value) {
      terminalBodyRef.value.scrollTop = terminalBodyRef.value.scrollHeight
    }
  })
}

function clearLogs() {
  labLogs.value = []
}

// 1. 测试令牌桶限流
async function handleTestRateLimit() {
  loadingLimiter.value = true
  appendLog('warn', '⚡ 发起 RRateLimiter 令牌申请请求...')
  try {
    const res = await labApi.testRateLimit()
    if (res && res.status) {
      appendLog('success', `🎉 通行成功！${res.message || res.data}`)
      ElMessage.success(res.message || '顺利获取令牌通过！')
    }
  } catch (err) {
    appendLog('error', `🚫 请求被拦截！${err.message || '触发高频访问限流'}`)
  } finally {
    loadingLimiter.value = false
  }
}

// 2. 重置限流器
async function handleResetLimiter() {
  loadingReset.value = true
  appendLog('info', '🔄 正在请求重置 Redisson RRateLimiter 令牌桶...')
  try {
    const res = await labApi.resetLimiter()
    if (res && res.status) {
      appendLog('success', '✅ 令牌桶已重置！可以再次立即通行')
      ElMessage.success('令牌桶已重置')
    }
  } catch (err) {
    appendLog('error', `重置失败: ${err.message}`)
  } finally {
    loadingReset.value = false
  }
}

// 3. 测试分布式锁排队
async function handleTestLockQueue() {
  loadingLock.value = true
  appendLog('warn', '🔒 正在尝试获取 Redisson 分布式锁，若有人在执行将在此阻塞排队...')
  try {
    const res = await labApi.testLockQueue()
    if (res && res.status) {
      appendLog('success', `🔓 独占任务已执行完成并释放锁！${res.message || res.data}`)
      ElMessage.success(res.message || '任务执行完毕！')
    }
  } catch (err) {
    appendLog('error', `任务异常: ${err.message}`)
  } finally {
    loadingLock.value = false
  }
}

// 4. 测试无事务
async function handleTestNoTx() {
  loadingNoTx.value = true
  appendLog('warn', '💥 【无事务实验】：开始插入第1条数据，并准备触发故意崩溃...')
  try {
    await labApi.testNoTx(true)
  } catch (err) {
    appendLog('error', `💥 业务正如预期崩溃！报错信息: ${err.message}`)
    appendLog('warn', '⚠️ 请注意：第1条【无事务测试】数据已被永久写死在数据库中！产生了脏数据残留！')
  } finally {
    loadingNoTx.value = false
  }
}

// 5. 测试有事务
async function handleTestWithTx() {
  loadingWithTx.value = true
  appendLog('info', '🛡️ 【事务回滚实验】：开启 @Transactional，准备在中途崩溃...')
  try {
    await labApi.testWithTx(true)
  } catch (err) {
    appendLog('error', `💥 业务正如预期崩溃！Spring 声明式事务拦截生效！`)
    appendLog('success', '🎉 时光倒流触发！MySQL 与 Redis 均已自动回滚，数据库没有残留任何脏数据！')
  } finally {
    loadingWithTx.value = false
  }
}

// 6. 清理演练测试数据
async function handleCleanTxData() {
  loadingClean.value = true
  appendLog('info', '🧹 正在调用专属原生物理 SQL 清理演练测试数据...')
  try {
    const res = await labApi.cleanTxData()
    appendLog('success', `✨ 清理完毕！${res.message || '已彻底物理抹平测试数据'}`)
    ElMessage.success('测试数据物理清理完毕')
  } catch (err) {
    appendLog('error', `清理失败: ${err.message}`)
  } finally {
    loadingClean.value = false
  }
}
</script>

<style scoped>
.lab-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.lab-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
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

.section-header .badge {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
}

.lab-tip {
  font-size: 13px;
  color: #64748b;
  margin: 6px 0 14px;
  line-height: 1.6;
}

.lab-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 终端模拟器 */
.terminal-monitor {
  background: #0f172a;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.terminal-header {
  background: #1e293b;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.mac-dots {
  display: flex;
  gap: 6px;
}

.mac-dots .dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}
.dot.red { background: #ef4444; }
.dot.yellow { background: #f59e0b; }
.dot.green { background: #10b981; }

.terminal-title {
  color: #cbd5e1;
  font-size: 13px;
  font-weight: 600;
}

.terminal-body {
  height: 220px;
  overflow-y: auto;
  padding: 14px;
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.7;
}

.log-empty {
  color: #64748b;
  text-align: center;
  padding: 40px 0;
}

.log-line {
  margin-bottom: 4px;
  word-break: break-all;
}

.log-time {
  color: #64748b;
  margin-right: 8px;
}

.log-line.info .log-text { color: #38bdf8; }
.log-line.warn .log-text { color: #fbbf24; }
.log-line.success .log-text { color: #34d399; }
.log-line.error .log-text { color: #f87171; }
</style>
