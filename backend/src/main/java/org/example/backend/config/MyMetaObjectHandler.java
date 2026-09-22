package org.example.backend.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.example.backend.common.UserContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 公共元数据对象处理器
 * 用于在实体持久化时自动填充公共字段（创建时间、更新时间、创建人、逻辑删除标识）
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入数据时的自动填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("【MyBatis-Plus】执行 insertFill 自动注入公共审计字段...");

        LocalDateTime now = LocalDateTime.now();

        // 1. 自动填充创建时间与更新时间
        this.setFieldValByName("createTime", now, metaObject);
        this.setFieldValByName("updateTime", now, metaObject);

        // 2. 自动从当前请求的 UserContext (ThreadLocal) 中提取当前操作人 ID
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            this.setFieldValByName("createBy", currentUserId, metaObject);
        }

        // 3. 自动初始化未删除状态
        this.setFieldValByName("deleted", 0, metaObject);
    }

    /**
     * 更新数据时的自动填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("【MyBatis-Plus】执行 updateFill 自动更新修改时间...");

        // 自动更新修改时间为当前时刻
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
