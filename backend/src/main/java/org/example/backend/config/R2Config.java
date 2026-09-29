package org.example.backend.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Cloudflare R2 / AWS S3 兼容客户端配置
 */
@Slf4j
@Configuration
public class R2Config {

    @Value("${r2.endpoint:https://5923ef4425a92d629e07cbb86b887ba1.r2.cloudflarestorage.com}")
    private String endpoint;

    @Value("${r2.access-key-id:}")
    private String accessKeyId;

    @Value("${r2.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        // 安全防御：避免因本地未配置或读取为空导致整个 Spring Boot 启动失败
        String key = StrUtil.isNotBlank(accessKeyId) ? accessKeyId.trim() : "dummy_access_key";
        String secret = StrUtil.isNotBlank(secretAccessKey) ? secretAccessKey.trim() : "dummy_secret_key";

        if ("dummy_access_key".equals(key)) {
            log.warn("【R2存储告警】未读取到有效 r2.access-key-id，使用占位凭证初始化客户端（文件上传功能将受限）");
        } else {
            log.info("【R2存储就绪】已成功装载 Cloudflare R2 访问密钥: {}****", key.substring(0, Math.min(6, key.length())));
        }

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(key, secret)
                ))
                .region(Region.of("auto"))
                .build();
    }
}
