package org.example.backend.controller.portal;

import org.example.backend.common.UserContext;
import org.example.backend.dto.BasicResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 前台门户 - 站点概况与元数据控制器（公开免登）
 */
@RestController
@RequestMapping({"/api/portal/site", "/api/site"})
public class SiteController {

    @GetMapping("/info")
    public BasicResponse<Map<String, Object>> getSiteInfo() {
        String currentUsername = UserContext.getUsername();
        Long currentUserId = UserContext.getUserId();

        Map<String, Object> info = new HashMap<>();
        info.put("title", "我的个人数字花园与奇思妙想屋");
        info.put("owner", "博主");
        info.put("status", "running");
        info.put("version", "v1.0.0");
        info.put("currentUser", currentUsername != null ? currentUsername + " (ID: " + currentUserId + ")" : "访客");
        return BasicResponse.success(info);
    }
}
