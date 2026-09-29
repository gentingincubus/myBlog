package org.example.backend.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 声明在 Controller 方法上，用于精细化鉴权控制
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {

    /**
     * 需要校验的权限标识（如 "vr:scene:add" 或 {"vr:scene:edit", "vr:scene:delete"}）
     */
    String[] value();

    /**
     * 多权限匹配逻辑，默认为 AND（必须满足全部权限）
     */
    Logical logical() default Logical.AND;
}

