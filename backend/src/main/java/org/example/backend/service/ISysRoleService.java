package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色业务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID获取角色标识列表 (如 ["admin", "common"])
     */
    Set<String> getRoleKeysByUserId(Long userId);

    /**
     * 根据用户ID获取其拥有的角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 获取指定角色已分配的菜单/按钮ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 为角色分配菜单与按钮权限
     */
    void assignRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 为用户分配角色
     */
    void assignUserRoles(Long userId, List<Long> roleIds);
}

