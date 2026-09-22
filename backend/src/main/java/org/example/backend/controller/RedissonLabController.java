package org.example.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.BizException;
import org.example.backend.dto.BasicResponse;
import org.redisson.api.RLock;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 企业级分布式高并发实战演练控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lab/redisson")
public class RedissonLabController {

    @Autowired
    private RedissonClient redissonClient;

    private static final String LIMITER_KEY = "lab:limiter:ai-task";
    private static final String LOCK_KEY = "lab:lock:heavy-task";

    /**
     * 实战 1：RRateLimiter 高压限流测试（规则：1分钟只允许 1 个任务通行）
     */
    @GetMapping("/rate-limit")
    public BasicResponse<Map<String, Object>> testRateLimit() {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(LIMITER_KEY);

        // 初始化限流规则：全实例共享，每 1 分钟发放 1 个令牌（若已有规则不会重复覆盖）
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.MINUTES);

        // 尝试非阻塞获取 1 个令牌
        boolean acquired = rateLimiter.tryAcquire(1);
        if (!acquired) {
            log.warn("⛔ 请求被 Redisson 令牌桶拦截！触发 1 分钟限流");
            throw new BizException("⛔ 【Redisson RRateLimiter 限流生效】当前接口已被限流！1分钟内全站仅允许执行1次，请稍后再试！");
        }

        log.info("🎉 成功获取到 Redisson 令牌，任务启动！");
        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("message", "🎉 成功夺取执行令牌！任务开始运行");
        res.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return BasicResponse.success(res);
    }

    /**
     * 重置限流器（方便随时重试，无需干等 1 分钟）
     */
    @PostMapping("/reset-limiter")
    public BasicResponse<String> resetLimiter() {
        redissonClient.getRateLimiter(LIMITER_KEY).delete();
        log.info("🔄 限流器状态已清空重置");
        return BasicResponse.success("已成功重置限流器令牌桶，可立即重新测试！");
    }

    /**
     * 实战 2：RLock 分布式排他锁与阻塞排队测试（模拟耗时 5 秒的独占任务）
     */
    @GetMapping("/lock-queue")
    public BasicResponse<Map<String, Object>> testLockQueue() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        long start = System.currentTimeMillis();

        try {
            // 尝试拿锁：最长排队等待 10 秒；拿到锁后独占 30 秒（看门狗机制会自动续期）
            log.info("⏳ 线程 {} 开始尝试获取分布式锁...", Thread.currentThread().getName());
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new BizException("⛔ 排队超时！当前排队人数过多，未能获取到执行锁");
            }

            try {
                long waitTime = System.currentTimeMillis() - start;
                log.info("🔒 线程 {} 成功加锁！排队耗时: {} ms，开始独占处理 5 秒耗时任务...", Thread.currentThread().getName(), waitTime);

                // 模拟高耗时 AI / 报表处理（占用 5 秒）
                Thread.sleep(5000);

                Map<String, Object> res = new HashMap<>();
                res.put("status", "SUCCESS");
                res.put("thread", Thread.currentThread().getName());
                res.put("waitTimeMs", waitTime);
                res.put("message", waitTime > 500
                        ? "✅ 任务执行成功！你刚才在队列中足足排队等待了 " + (waitTime / 1000) + " 秒！"
                        : "✅ 任务执行成功！你是第一个抢到锁的，未排队直接运行！");
                res.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                return BasicResponse.success(res);
            } finally {
                // 确保只有当前线程持有时才释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("🔓 线程 {} 任务结束，已安全释放分布式锁！", Thread.currentThread().getName());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("任务排队被中断");
        }
    }
}
