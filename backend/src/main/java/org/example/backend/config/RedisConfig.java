package org.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 统一序列化配置类
 * 彻底消灭 Spring 默认 JDK 序列化产生的二进制乱码 (\xac\xed\x00\x05)
 * 让 Tiny RDM 中存储的 Key 和 Value 都是纯净、漂亮的 JSON 文本
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 与 HashKey 均使用 String 序列化规则
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 与 HashValue 均使用标准的 JSON 序列化规则
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.password:}")
    private String redisPassword;

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * 配置 RedissonClient 企业级分布式协调客户端
     */
    @Bean
    public org.redisson.api.RedissonClient redissonClient() {
        org.redisson.config.Config config = new org.redisson.config.Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        var singleServer = config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDatabase);

        if (cn.hutool.core.util.StrUtil.isNotBlank(redisPassword)) {
            singleServer.setPassword(redisPassword);
        }

        return org.redisson.Redisson.create(config);
    }
}
