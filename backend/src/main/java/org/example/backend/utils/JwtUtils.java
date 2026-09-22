package org.example.backend.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 签发与解析工具类
 */
public class JwtUtils {

    /** Token 签名秘钥（切记保密） */
    private static final byte[] SECRET_KEY = "myblog_secret_key_nightdream_jwt_2026".getBytes(StandardCharsets.UTF_8);

    /** Token 有效期：7 天 (7 * 24 * 60 * 60 * 1000 毫秒) */
    private static final long EXPIRE_TIME = 1 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token 字符串
     */
    public static String createToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        // 设置过期时间
        payload.put("expire_time", System.currentTimeMillis() + EXPIRE_TIME);

        return JWTUtil.createToken(payload, SECRET_KEY);
    }

    /**
     * 校验 Token 是否合法
     *
     * @param token 待校验的 token
     * @return true 合法，false 非法或已过期
     */
    public static boolean verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            boolean verify = JWTUtil.verify(token, SECRET_KEY);
            if (!verify) {
                return false;
            }
            // 判断是否过期
            JWT jwt = JWTUtil.parseToken(token);
            Object expireTimeObj = jwt.getPayload("expire_time");
            if (expireTimeObj != null) {
                long expireTime = Long.parseLong(expireTimeObj.toString());
                return System.currentTimeMillis() < expireTime;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中解析获取用户ID
     */
    public static Long getUserId(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object userId = jwt.getPayload("userId");
            return userId != null ? Long.parseLong(userId.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中解析获取用户名
     */
    public static String getUsername(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object username = jwt.getPayload("username");
            return username != null ? username.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
