package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.vo.MenuTreeVo;
import org.example.backend.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 菜单与权限业务接口
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID获取其所拥有的所有权限字符标识集合 (如 ["vr:scene:add", "sys:user:list"])
     */
    Set<String> getPermissionsByUserId(Long userId);

    /**
     * 根据用户ID获取授权的动态路由菜单树 (仅包含 M 目录与 C 菜单，过滤 F 按钮，供左侧侧边栏渲染)
     */
    List<MenuTreeVo> getRoutersByUserId(Long userId);

    /**
     * 获取全量权限树 (包含 M 目录、C 菜单、F 按钮，供角色分配权限弹窗使用)
     */
    List<MenuTreeVo> getAllMenuTree();

    /**
     * 获取菜单管理表格树 (包含全字段与各层级，供菜单管理页面使用)
     */
    List<MenuTreeVo> getMenuTableTree();

    /**
     * 校验菜单是否有子节点（用于删除前防护拦截）
     */
    boolean hasChildByMenuId(Long menuId);
}

