package org.example.backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;

/**
 * 缓存与事务双写一致性通用工具类
 * 全局通用静态方法，任何 Service / Controller 均可直接一行调用：CacheUtils.deleteAfterCommit(...)
 */
@Slf4j
@Component
public class CacheUtils {

    private static StringRedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        CacheUtils.redisTemplate = redisTemplate;
    }

    /**
     * 事务安全删除单个缓存 Key：
     * 1. 若当前处于 Spring 事务中，强制在事务 Commit 成功落盘之后再执行删除，彻底杜绝并发脏读；
     * 2. 若无事务环境，则立即执行删除。
     *
     * @param cacheKey 缓存键
     */
    public static void deleteAfterCommit(String cacheKey) {
        if (cacheKey == null || redisTemplate == null) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisTemplate.delete(cacheKey);
                    log.info("🎉 事务 Commit 成功，afterCommit 自动清理缓存: {}", cacheKey);
                }
            });
        } else {
            redisTemplate.delete(cacheKey);
        }
    }

    /**
     * 事务安全批量删除多个缓存 Keys
     *
     * @param cacheKeys 缓存键集合
     */
    public static void deleteAfterCommit(Collection<String> cacheKeys) {
        if (cacheKeys == null || cacheKeys.isEmpty() || redisTemplate == null) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisTemplate.delete(cacheKeys);
                    log.info("🎉 事务 Commit 成功，afterCommit 批量清理缓存: {} 个", cacheKeys.size());
                }
            });
        } else {
            redisTemplate.delete(cacheKeys);
        }
    }
}
