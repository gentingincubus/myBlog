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
                        "/api/auth/**",                 // 登录 /register, /login 等
                        "/api/nav/list",                // 首页公共导航列表，无需登录公开访问
                        "/api/vr/category/list",        // VR 分类列表公开查询
                        "/api/vr/category/detail/*",    // VR 分类详情公开查询
                        "/api/vr/scene/list",           // VR 场景点位列表公开查询
                        "/api/vr/scene/detail/*",       // VR 场景详情公开查询
                        "/error"                        // Spring Boot 默认全局错误路径
                )
                .order(1);
    }
}
