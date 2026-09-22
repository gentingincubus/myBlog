package org.example.backend.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户上下文工具类（基于 ThreadLocal）
 * 用于在同一个请求线程的 Controller / Service 中安全地获取当前登录用户身份
 */
public class UserContext {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
    }

    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息
     */
    public static void setUser(Long userId, String username) {
        USER_THREAD_LOCAL.set(UserInfo.builder()
                .userId(userId)
                .username(username)
                .build());
    }

    /**
     * 获取当前线程的用户信息
     */
    public static UserInfo getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getUserId() {
        UserInfo user = USER_THREAD_LOCAL.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户的用户名
     */
    public static String getUsername() {
        UserInfo user = USER_THREAD_LOCAL.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清理 ThreadLocal（必须在请求结束拦截器中调用，防止线程复用造成的内存泄漏和信息错乱）
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}
