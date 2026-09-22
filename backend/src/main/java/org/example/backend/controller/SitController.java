package org.example.backend.controller;

import org.example.backend.common.UserContext;
import org.example.backend.dto.BasicResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 站点信息控制器（受 JWT 拦截器保护）
 */
@RestController
@RequestMapping("/api/site")
public class SitController {

    @GetMapping("/info")
    public BasicResponse<Map<String, Object>> getSiteInfo() {
        // 从当前请求的 ThreadLocal 上下文中提取当前操作者
        String currentUsername = UserContext.getUsername();
        Long currentUserId = UserContext.getUserId();

        Map<String, Object> info = new HashMap<>();
        info.put("title", "我的个人数字花园与奇思妙想屋");
        info.put("owner", "博主");
        info.put("status", "running");
        info.put("version", "v1.0.0");
        info.put("currentUser", currentUsername + " (ID: " + currentUserId + ")");
        return BasicResponse.success(info);
    }
}
