package org.example.backend.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.backend.annotation.Logical;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.common.BizException;
import org.example.backend.common.UserContext;
import org.example.backend.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 接口级细粒度权限校验切面
 * 配合 @RequiresPermissions 注解，拦截所有越权请求
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    public static final String ALL_PERMISSION = "*:*:*";

    @Autowired
    private ISysMenuService sysMenuService;

    @Around("@annotation(requiresPermissions)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermissions requiresPermissions) throws Throwable {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "请先登录后操作");
        }

        String[] requiredPerms = requiresPermissions.value();
        if (requiredPerms == null || requiredPerms.length == 0) {
            return joinPoint.proceed();
        }

        // 获取用户所持有的所有权限字符标识
        Set<String> userPerms = sysMenuService.getPermissionsByUserId(userId);
        if (CollUtil.isEmpty(userPerms)) {
            log.warn("用户 ID [{}] 尝试访问未授权接口: 所需权限 {}", userId, requiredPerms);
            throw new BizException(403, "没有该操作的访问权限");
        }

        // 超级管理员或全局特权直接放行
        if (userPerms.contains(ALL_PERMISSION)) {
            return joinPoint.proceed();
        }

        boolean hasPerm;
        if (requiresPermissions.logical() == Logical.AND) {
            hasPerm = true;
            for (String perm : requiredPerms) {
                if (!hasPermissionMatch(userPerms, perm)) {
                    hasPerm = false;
                    break;
                }
            }
        } else {
            hasPerm = false;
            for (String perm : requiredPerms) {
                if (hasPermissionMatch(userPerms, perm)) {
                    hasPerm = true;
                    break;
                }
            }
        }

        if (!hasPerm) {
            log.warn("用户 ID [{}] 权限不足！持有权限: {}, 缺少权限: {}", userId, userPerms, requiredPerms);
            throw new BizException(403, "没有该操作的访问权限");
        }

        return joinPoint.proceed();
    }

    /**
     * 支持通配符匹配（如 vr:scene:* 匹配 vr:scene:add）
     */
    private boolean hasPermissionMatch(Set<String> userPerms, String requiredPerm) {
        if (StrUtil.isBlank(requiredPerm)) {
            return true;
        }
        if (userPerms.contains(requiredPerm) || userPerms.contains(ALL_PERMISSION)) {
            return true;
        }
        for (String perm : userPerms) {
            if (perm.endsWith(":*")) {
                String prefix = perm.substring(0, perm.length() - 1);
                if (requiredPerm.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }
}

