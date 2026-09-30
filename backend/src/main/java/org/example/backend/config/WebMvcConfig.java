package org.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 核心配置类
 * 用于注册拦截器、跨域映射等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TraceIdInterceptor traceIdInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许跨域访问，配合线上 Pages 或第三方域名
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 全链路 TraceID 拦截器：拦截全站所有请求（包括白名单、错误页），为每条请求打上独立日志身份证
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**")
                .order(0);

        // 2. JWT 权限拦截器：负责鉴权校验与注入 UserContext
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",                 // 认证接口：登录 /login, 注册 /register 等公开免登
                        "/api/portal/**",               // 前台门户所有公开接口（导航 /portal/nav/**，全景 /portal/vr/**，站点 /portal/site/** 等）
                        "/api/nav/list",                // 兼容旧版前台导航接口
                        "/api/nav/portal/**",           // 兼容旧版前台导航别名
                        "/api/carousel/portal/**",      // 兼容前台轮播别名
                        "/api/vr/open/**",              // 兼容旧版前台全景公开接口
                        "/api/vr/category/list",        // 兼容旧版 VR 分类列表公开查询
                        "/api/vr/category/detail/*",    // 兼容旧版 VR 分类详情公开查询
                        "/api/vr/scene/list",           // 兼容旧版 VR 场景点位列表公开查询
                        "/api/vr/scene/detail/*",       // 兼容旧版 VR 场景详情公开查询
                        "/api/site/**",                 // 兼容旧版前台站点概况公开查询
                        "/error"                        // Spring Boot 默认全局错误路径
                )
                .order(1);
    }
}
