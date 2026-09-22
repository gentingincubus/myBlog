import request from './request'

/**
 * 测试 Redisson RRateLimiter 高压限流（1分钟只允许 1 次）
 */
export function testRateLimitApi() {
  return request({
    url: '/lab/redisson/rate-limit',
    method: 'get'
  })
}

/**
 * 重置 Redisson 限流器令牌
 */
export function resetLimiterApi() {
  return request({
    url: '/lab/redisson/reset-limiter',
    method: 'post'
  })
}

/**
 * 测试 Redisson RLock 分布式互斥锁排队（独占 5 秒任务）
 */
export function testLockQueueApi() {
  return request({
    url: '/lab/redisson/lock-queue',
    method: 'get'
  })
}

/**
 * 事务实验 A：无事务批量添加（中途故意崩溃）
 */
export function testNoTxApi(makeError = true) {
  return request({
    url: `/lab/tx/no-tx?makeError=${makeError}`,
    method: 'post'
  })
}

/**
 * 事务实验 B：声明式事务批量添加（@Transactional 自动回滚）
 */
export function testWithTxApi(makeError = true) {
  return request({
    url: `/lab/tx/with-tx?makeError=${makeError}`,
    method: 'post'
  })
}

/**
 * 清理事务演练残留测试数据
 */
export function cleanTxTestDataApi() {
  return request({
    url: '/lab/tx/clean',
    method: 'post'
  })
}

export const labApi = {
  testRateLimit: testRateLimitApi,
  resetLimiter: resetLimiterApi,
  testLockQueue: testLockQueueApi,
  testNoTx: testNoTxApi,
  testWithTx: testWithTxApi,
  cleanTxData: cleanTxTestDataApi
}
