package org.example.backend.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.UserContext;
import org.example.backend.dto.BasicResponse;
import org.example.backend.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT 认证拦截器
 * 用于拦截需要权限校验的接口，解析 Token 并注入 UserContext
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求 OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 从请求头中提取 Authorization 或 token
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else if (StrUtil.isBlank(token)) {
            token = request.getHeader("token");
        }

        // 2. 校验 Token 有效性
        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return returnUnauthorizedResponse(response, "未登录或登录已过期，请重新登录");
        }

        // 3. 解析 Token 中的用户信息并存入当前线程上下文 (ThreadLocal)
        Long userId = JwtUtils.getUserId(token);
        String username = JwtUtils.getUsername(token);

        if (userId == null || StrUtil.isBlank(username)) {
            return returnUnauthorizedResponse(response, "无效的登录凭证");
        }

        UserContext.setUser(userId, username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完毕，彻底移除当前线程的 UserContext，防止 Tomcat 线程池复用导致上下文污染或内存泄露
        UserContext.remove();
    }

    /**
     * 向前端输出 401 未授权的 JSON 错误响应
     */
    private boolean returnUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        BasicResponse<Void> errorResp = BasicResponse.<Void>builder()
                .status(false)
                .code(401)
                .message(message)
                .data(null)
                .build();

        response.getWriter().write(JSONUtil.toJsonStr(errorResp));
        return false;
    }
}
