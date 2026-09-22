package org.example.backend.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局序列化配置类
 * 1. 解决雪花算法 19 位 Long 型主键传输给前端时的 JavaScript 精度丢失问题 (转为 String)
 * 2. 解决 LocalDateTime 默认带 ISO-8601 中间字母 'T' 的问题，统一格式化为 yyyy-MM-dd HH:mm:ss
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 1. 将所有的 Long / long 类型转为 JSON 字符串输出
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // 2. 全局将 LocalDateTime 序列化与反序列化为 yyyy-MM-dd HH:mm:ss（消除丑陋的字母 'T'）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        };
    }
}
