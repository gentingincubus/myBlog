package org.example.backend.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 全链路日志 TraceID 拦截器
 * 核心职责：
 * 1. 在请求到达的第一时间生成全局唯一的 TraceID 并植入 MDC 线程便签；
 * 2. 将 TraceID 回传给 HTTP 响应头 (X-Trace-Id)，便于前端或运维按 ID 精准排障；
 * 3. 请求结束（afterCompletion）时彻底清理 MDC，杜绝 Tomcat 线程池复用导致的 TraceID 串号！
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    /** 日志便签 Key 名称，与 logback-spring.xml 中的 %X{traceId} 保持一致 */
    public static final String TRACE_ID_KEY = "traceId";
    /** HTTP 请求/响应头中的 TraceID 标识 */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 优先读取上游服务或前端传入的 TraceID，没有则由后端自动生成 16 位短 UUID
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID().substring(0, 16);
        }

        // 2. 存入 SLF4J MDC 线程便签（当前线程后续的所有 log.info / log.error 都会自动带上该 ID）
        MDC.put(TRACE_ID_KEY, traceId);

        // 3. 将 TraceID 写入响应头，方便前端网络调试定位问题
        response.setHeader(HEADER_TRACE_ID, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 【核心避坑要点】：请求结束后彻底清除 MDC，防止 Tomcat 线程复用串号！
        MDC.remove(TRACE_ID_KEY);
    }
}
