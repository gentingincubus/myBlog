package org.example.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.common.BizException;
import org.example.backend.entity.SysRole;
import org.example.backend.entity.SysRoleMenu;
import org.example.backend.entity.SysUserRole;
import org.example.backend.mapper.SysRoleMapper;
import org.example.backend.mapper.SysRoleMenuMapper;
import org.example.backend.mapper.SysUserRoleMapper;
import org.example.backend.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色业务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Set<String> getRoleKeysByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        // 超级管理员默认直接具备 admin 角色
        if (userId == 1L) {
            return Set.of("admin");
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptySet();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = this.listByIds(roleIds);
        return roles.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(SysRole::getRoleKey)
                .collect(Collectors.toSet());
    }

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return this.listByIds(roleIds);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            throw new BizException("角色ID不能为空");
        }
        // 1. 删除旧的菜单权限关联
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        // 2. 插入新选中的菜单权限
        if (CollUtil.isNotEmpty(menuIds)) {
            for (Long menuId : menuIds) {
                sysRoleMenuMapper.insert(SysRoleMenu.builder()
                        .roleId(roleId)
                        .menuId(menuId)
                        .build());
            }
        }

        // 3. 清理 Redis 中的权限缓存（以 sys:perms:* 为前缀）
        if (stringRedisTemplate != null) {
            try {
                Set<String> keys = stringRedisTemplate.keys("sys:perms:*");
                if (CollUtil.isNotEmpty(keys)) {
                    stringRedisTemplate.delete(keys);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new BizException("用户ID不能为空");
        }
        // 1. 删除用户原有的角色绑定
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        // 2. 插入新的角色绑定
        if (CollUtil.isNotEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                sysUserRoleMapper.insert(SysUserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build());
            }
        }

        // 3. 清理该用户的权限缓存
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete("sys:perms:" + userId);
            } catch (Exception ignored) {
            }
        }
    }
}

